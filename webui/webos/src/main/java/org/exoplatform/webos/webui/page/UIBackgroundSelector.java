/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.webos.webui.page;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 27, 2010
 */
@ComponentConfig
(
  id = "backgroundSelector",
  template = "system:/groovy/portal/webui/page/UIBackgroundSelector.gtmpl", 
  events ={
   @EventConfig(listeners = UIBackgroundSelector.UploadActionListener.class),
   @EventConfig(listeners = UIBackgroundSelector.CloseActionListener.class),
   @EventConfig(listeners = UIBackgroundSelector.DeleteActionListener.class, confirm = "UIBackgroundSelector.confirm.deleteImage"),
   @EventConfig(listeners = UIBackgroundSelector.PreviewActionListener.class),    
   @EventConfig(name = "Choose", listeners = UIBackgroundSelector.SelectItemActionListener.class)
  }
)
public class UIBackgroundSelector extends UIContainer
{
   public static final String IMAGE_LABEL = "imageLabel";
   public static final String[] BACKGROUND_BEAN_FIELD = {IMAGE_LABEL};
   public static final String[] ACTIONS = {"Choose", "Preview", "Delete"};
   public static final int PAGE_SIZE = 5;

   private UIBackgroundUploadForm uploadForm;
   private UIVirtualList imageList;
   private String previewImage;
   private static Log log = ExoLogger.getLogger("portal:UIBackgroundSelector");

   public UIBackgroundSelector() throws Exception
   {
      UIRepeater uiRepeater = createUIComponent(UIRepeater.class, null, null);
      uiRepeater.configure(IMAGE_LABEL, BACKGROUND_BEAN_FIELD, ACTIONS);

      imageList = addChild(UIVirtualList.class, null, "UIBackgroundImageList");
      imageList.setPageSize(PAGE_SIZE);
      imageList.setUIComponent(uiRepeater);
   }

   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIDesktopPage uiDesktopPage = Util.getUIPortalApplication().findFirstComponentOfType(UIDesktopPage.class);
      if (uiDesktopPage == null)
      {
         //Make sure this selector is run with UIDesktopPage
         UIMaskWorkspace maskWorkspace = getAncestorOfType(UIMaskWorkspace.class);
         maskWorkspace.createEvent("Close", Event.Phase.DECODE, context).broadcast();
         PortalRequestContext prcontext = Util.getPortalRequestContext();
         prcontext.sendRedirect(prcontext.getRequestURI());
         return;
      }

      ListAccess<DesktopBackground> imgAccess = new ListAccessImpl<DesktopBackground>(DesktopBackground.class,
         getDesktopBackgrounds(context));
      imageList.dataBind(new LazyPageList<DesktopBackground>(imgAccess, PAGE_SIZE));
                                                  
      DesktopBackgroundService service = getApplicationComponent(DesktopBackgroundService.class);

      UIPortal uiPortal = Util.getUIPortal();
      DesktopBackground previewBackground = service.getDesktopBackground(new PortalKey(uiPortal.getOwnerType(), uiPortal.getOwner()), getPreviewImage()); 
      if (previewBackground == null)
      {
         setPreviewImage(null);
         previewBackground = service.getCurrentDesktopBackground(uiDesktopPage.getPageId());
      }
      refreshDesktopBackground(previewBackground, false);

      super.processRender(context);
   }

   public UIBackgroundUploadForm getUploadForm()
   {
      return uploadForm;
   }

   public void setUploadForm(UIBackgroundUploadForm uploadForm)
   {
      uploadForm.setReferrer(this);
      this.uploadForm = uploadForm;
   }

   public void setPreviewImage(String previewImage)
   {
      this.previewImage = previewImage;
   }

   public String getPreviewImage()
   {
      return previewImage;
   }

   private void refreshDesktopBackground(DesktopBackground desktopBackground, boolean save)
   {
      UIDesktopPage uiDesktopPage = Util.getUIPortalApplication().findFirstComponentOfType(UIDesktopPage.class);
      if (uiDesktopPage != null)
      {
         if (save)
         {
            uiDesktopPage.setDesktopBackground(desktopBackground);
         }
         else
         {
            uiDesktopPage.showDesktopBackground(desktopBackground);
         }
      }
   }

   public static class UploadActionListener extends EventListener<UIBackgroundSelector>
   {
      @Override
      public void execute(Event<UIBackgroundSelector> event) throws Exception
      {
         UIBackgroundSelector selector = event.getSource();         
         if (selector.getUploadForm() == null)
         {
            UIBackgroundUploadForm uploadForm = selector.createUIComponent(UIBackgroundUploadForm.class, null, null);
            selector.setUploadForm(uploadForm);
         }

         UIMaskWorkspace maskWorkspace = selector.getAncestorOfType(UIMaskWorkspace.class);
         maskWorkspace.setUIComponent(selector.getUploadForm());
         Util.getPortalRequestContext().addUIComponentToUpdateByAjax(maskWorkspace);
      }
   }

   public static class CloseActionListener extends EventListener<UIBackgroundSelector>
   {
      @Override
      public void execute(Event<UIBackgroundSelector> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIBackgroundSelector selector = event.getSource();
         selector.setPreviewImage(null);

         UIMaskWorkspace maskWorkspace = selector.getAncestorOfType(UIMaskWorkspace.class);
         maskWorkspace.createEvent("Close", Event.Phase.DECODE, context).broadcast();

         DesktopBackgroundService backgroundService = selector.getApplicationComponent(DesktopBackgroundService.class);
         UIDesktopPage uiDesktopPage = Util.getUIPortalApplication().findFirstComponentOfType(UIDesktopPage.class);
         selector.refreshDesktopBackground(backgroundService.getCurrentDesktopBackground(uiDesktopPage.getPageId()), true);
      }
   }

   public static class SelectItemActionListener extends EventListener<UIBackgroundSelector>
   {
      @Override
      public void execute(Event<UIBackgroundSelector> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIBackgroundSelector selector = event.getSource();
         selector.setPreviewImage(null);
         String selectedItem = context.getRequestParameter(OBJECTID);

         DesktopBackgroundService backgroundService = selector.getApplicationComponent(DesktopBackgroundService.class);

         try
         {
            UIDesktopPage uiDesktopPage = Util.getUIPortalApplication().findFirstComponentOfType(UIDesktopPage.class);
            backgroundService.setSelectedBackgroundImage(uiDesktopPage.getPageId(), selectedItem);
            UIMaskWorkspace maskWorkspace = selector.getAncestorOfType(UIMaskWorkspace.class);
            maskWorkspace.createEvent("Close", Event.Phase.DECODE, context).broadcast();
         }
         catch (IllegalStateException e)
         {
            log.warn(e.getMessage());
            Util.getUIPortalApplication().addMessage(new ApplicationMessage("UIBackgroundSelector.msg.notExists.image",
               null, ApplicationMessage.WARNING));
            context.addUIComponentToUpdateByAjax(selector);
         }

         UIDesktopPage uiDesktopPage = Util.getUIPortalApplication().findFirstComponentOfType(UIDesktopPage.class);
         selector.refreshDesktopBackground(backgroundService.getCurrentDesktopBackground(uiDesktopPage.getPageId()), true);
      }
   }

   public static class PreviewActionListener extends EventListener<UIBackgroundSelector>
   {
      @Override
      public void execute(Event<UIBackgroundSelector> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIBackgroundSelector selector = event.getSource();
         selector.setPreviewImage(context.getRequestParameter(OBJECTID));

         DesktopBackgroundService backgroundService = selector.getApplicationComponent(DesktopBackgroundService.class);
         
         UIPortal uiPortal = Util.getUIPortal();
         DesktopBackground previewBackground = backgroundService.getDesktopBackground(new PortalKey(uiPortal.getOwnerType(), uiPortal.getOwner()), selector.getPreviewImage());
         if (previewBackground == null)
         {
            log.warn("Can't found image :" + selector.getPreviewImage());
            Util.getUIPortalApplication().addMessage(new ApplicationMessage("UIBackgroundSelector.msg.notExists.image",
               null, ApplicationMessage.WARNING));
            selector.setPreviewImage(null);
            context.addUIComponentToUpdateByAjax(selector);
            return;
         }

         selector.refreshDesktopBackground(previewBackground, false);
      }
   }

   public static class DeleteActionListener extends EventListener<UIBackgroundSelector>
   {
      @Override
      public void execute(Event<UIBackgroundSelector> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIBackgroundSelector selector = event.getSource();
         String selectedItem = context.getRequestParameter(OBJECTID);

         DesktopBackgroundService backgroundService = selector.getApplicationComponent(DesktopBackgroundService.class);

         UIPortal uiPortal = Util.getUIPortal();
         try
         {
            backgroundService.removeBackgroundImage(new PortalKey(uiPortal.getOwnerType(), uiPortal.getOwner()), selectedItem);
         }
         catch (IllegalStateException e)
         {
            log.warn(e.getMessage());
         }
         UIDesktopPage uiDesktopPage = Util.getUIPortalApplication().findFirstComponentOfType(UIDesktopPage.class);
         if (selectedItem != null && selectedItem.equals(uiDesktopPage.getCurrBackgroundLabel()))
         {
            backgroundService.setSelectedBackgroundImage(uiDesktopPage.getPageId(), null);
            uiDesktopPage.setDesktopBackground(null);
         }
         context.addUIComponentToUpdateByAjax(selector);
      }
   }
      
   private List<DesktopBackground> getDesktopBackgrounds(WebuiRequestContext context) throws Exception
   {
      DesktopBackgroundService backgroundService = getApplicationComponent(DesktopBackgroundService.class);

      UIPortal uiPortal = Util.getUIPortal();
      List<DesktopBackground> backgrounds = backgroundService.findDesktopBackgrounds(new PortalKey(uiPortal.getOwnerType(), uiPortal.getOwner()));
      Collections.sort(backgrounds, new Comparator<DesktopBackground>() {
         @Override
         public int compare(DesktopBackground o1, DesktopBackground o2)
         {
            if (o1.getImageLabel() == null)
            {
               return 1;
            }
            if (o2.getImageLabel() == null)
            {
               return -1;
            }
            return o1.getImageLabel().compareTo(o2.getImageLabel());
         }
      });
      return backgrounds;
   }
   
}
