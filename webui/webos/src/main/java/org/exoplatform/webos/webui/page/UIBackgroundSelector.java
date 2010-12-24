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
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
   @EventConfig(name = "View", listeners = UIBackgroundSelector.SelectItemActionListener.class)
  }
)
public class UIBackgroundSelector extends UIContainer
{
   public static final String IMAGE_LABEL = "imageLabel";
   public static final String[] BACKGROUND_BEAN_FIELD = {IMAGE_LABEL};
   public static final String[] ACTIONS = {"View", "Preview", "Delete"};
   public static final int PAGE_SIZE = 5;

   private UIBackgroundUploadForm uploadForm;
   private UIGrid  imageList;
   private String previewImage;
   private static Log log = ExoLogger.getLogger("portal:UIBackgroundSelector");

   public UIBackgroundSelector() throws Exception
   {
      imageList = createUIComponent(UIGrid.class, null, "UIBackgroundImageList");
      imageList.configure(IMAGE_LABEL, BACKGROUND_BEAN_FIELD, ACTIONS);
      imageList.getUIPageIterator().setId("UIListBackgroundsIterator");
      imageList.getUIPageIterator().setParent(this);
      addChild(imageList);
   }

   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      int currPage = imageList.getUIPageIterator().getCurrentPage();

      ListAccess<DesktopBackground> imgAccess = new ListAccessImpl<DesktopBackground>(DesktopBackground.class,
         getDesktopBackgrounds(context));
      imageList.getUIPageIterator().setPageList(new LazyPageList<DesktopBackground>(imgAccess, PAGE_SIZE));

      int availPage = imageList.getUIPageIterator().getAvailablePage();
      if (currPage > availPage)
      {
         currPage = availPage;
      }
      imageList.getUIPageIterator().setCurrentPage(currPage);

      DesktopBackgroundService service = getApplicationComponent(DesktopBackgroundService.class);
      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      DesktopBackground previewBackground = service.getUserDesktopBackground(userId, getPreviewImage()); 
      if (previewBackground == null)
      {
         setPreviewImage(null);
         previewBackground = service.getCurrentDesktopBackground(userId);
      }
      refreshDesktopBackground(previewBackground);

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

   private void refreshDesktopBackground(DesktopBackground desktopBackground)
   {
      UIDesktopPage uiDesktopPage = Util.getUIPortalApplication().findFirstComponentOfType(UIDesktopPage.class);
      if (uiDesktopPage != null)
      {
         uiDesktopPage.showDesktopBackground(desktopBackground);
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
         String userId = ConversationState.getCurrent().getIdentity().getUserId();
         selector.refreshDesktopBackground(backgroundService.getCurrentDesktopBackground(userId));
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
         String userId = ConversationState.getCurrent().getIdentity().getUserId();

         try
         {
            backgroundService.setSelectedBackgroundImage(userId, selectedItem);
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

         selector.refreshDesktopBackground(backgroundService.getCurrentDesktopBackground(userId));
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
         String userId = ConversationState.getCurrent().getIdentity().getUserId();

         DesktopBackground previewBackground = backgroundService.getUserDesktopBackground(userId, selector.getPreviewImage());
         if (previewBackground == null)
         {
            log.warn("Can't found image :" + selector.getPreviewImage());
            Util.getUIPortalApplication().addMessage(new ApplicationMessage("UIBackgroundSelector.msg.notExists.image",
               null, ApplicationMessage.WARNING));
            selector.setPreviewImage(null);
            context.addUIComponentToUpdateByAjax(selector);
            return;
         }

         selector.refreshDesktopBackground(previewBackground);
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

         String userId = ConversationState.getCurrent().getIdentity().getUserId();
         DesktopBackgroundService backgroundService = selector.getApplicationComponent(DesktopBackgroundService.class);

         try
         {
            backgroundService.removeBackgroundImage(userId, selectedItem);
         }
         catch (IllegalStateException e)
         {
            log.warn(e.getMessage());
         }

         context.addUIComponentToUpdateByAjax(selector);
      }
   }
      
   private List<DesktopBackground> getDesktopBackgrounds(WebuiRequestContext context)
   {
      DesktopBackgroundService backgroundService = getApplicationComponent(DesktopBackgroundService.class);
	   
      return backgroundService.getUserDesktopBackgrounds(context.getRemoteUser());
   }
   
}
