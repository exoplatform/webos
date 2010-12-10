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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
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
   @EventConfig(name = "SelectItem", listeners = UIBackgroundSelector.SelectItemActionListener.class)
  }
)
public class UIBackgroundSelector extends UIContainer
{
   private UIBackgroundUploadForm uploadForm;   

   public UIBackgroundSelector() throws Exception
   {
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
         // TODO Auto-generated method stub
         UIBackgroundSelector selector = event.getSource();
         UIMaskWorkspace maskWorkspace = selector.getAncestorOfType(UIMaskWorkspace.class);
         
         maskWorkspace.setUIComponent(null);
         maskWorkspace.setWindowSize(-1, -1);
         event.getRequestContext().addUIComponentToUpdateByAjax(maskWorkspace);
      }
   }
   
   public static class SelectItemActionListener extends EventListener<UIBackgroundSelector>
   {
      @Override
      public void execute(Event<UIBackgroundSelector> event) throws Exception
      {
         WebuiRequestContext context = event.getRequestContext();
         UIBackgroundSelector selector = event.getSource();
         String selectedItem = context.getRequestParameter(OBJECTID);
         UIMaskWorkspace maskWorkspace = selector.getAncestorOfType(UIMaskWorkspace.class);
         maskWorkspace.setUIComponent(null);
         maskWorkspace.setWindowSize(-1, -1);
         context.addUIComponentToUpdateByAjax(maskWorkspace);
         
         DesktopBackgroundService backgroundService = (DesktopBackgroundService)selector.getApplicationComponent(DesktopBackgroundService.class);
         backgroundService.setSelectedBackgroundImage(context.getRemoteUser(), selectedItem);
      }
   }
      
   private List<DesktopBackground> getDesktopBackgrounds(WebuiRequestContext context)
   {
      DesktopBackgroundService backgroundService = getApplicationComponent(DesktopBackgroundService.class);
	   
      return backgroundService.getUserDesktopBackgrounds(context.getRemoteUser());
   }
   
}
