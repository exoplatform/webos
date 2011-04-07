/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webos.services.desktop.exception.ImageQuantityException;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormUploadInput;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIBackgroundUploadForm.SaveActionListener.class),
   @EventConfig(listeners = UIBackgroundUploadForm.AddActionListener.class),
   @EventConfig(listeners = UIBackgroundUploadForm.RemoveActionListener.class),
   @EventConfig(listeners = UIBackgroundUploadForm.BackActionListener.class)})
public class UIBackgroundUploadForm extends UIForm
{

   public static final String MULTI_IMAGE = "Image";

   public static final String[] ACTIONS = {"Save", "Back"};

   private UIComponent referrer;

   public UIBackgroundUploadForm() throws Exception
   {
      DesktopBackgroundService backgroundService = getApplicationComponent(DesktopBackgroundService.class);
      int sizeLimit = backgroundService.getSizeLimit();
      Class<?>[] paramTypes = new Class[]{String.class, String.class, int.class, boolean.class};
      Object[] paramValues = new Object[]{MULTI_IMAGE, null, sizeLimit, true};
      addUIFormInput(makeMultiValueInputSet(MULTI_IMAGE, UIFormUploadInput.class, paramTypes, paramValues));

      setActions(ACTIONS);
   }

   public UIComponent getReferrer()
   {
      return referrer;
   }

   public void setReferrer(UIComponent referrer)
   {
      this.referrer = referrer;
   }

   private UIFormInput makeMultiValueInputSet(String name, Class<? extends UIFormInputBase> type, 
		   										Class[] paramTypes, Object[] paramValues)throws Exception
   {
      UIFormMultiValueInputSet multiInput = new UIFormMultiValueInputSet(name, null);
      multiInput.setType(type);
      multiInput.setConstructorParameterTypes(paramTypes);
      multiInput.setConstructorParameterValues(paramValues);
      return multiInput;
   }

   private void backToImageList(WebuiRequestContext requestContext)
   {
      UIMaskWorkspace maskWorkspace = getAncestorOfType(UIMaskWorkspace.class);

      maskWorkspace.setUIComponent(getReferrer());
      requestContext.addUIComponentToUpdateByAjax(maskWorkspace);
   }

   static public class SaveActionListener extends EventListener<UIBackgroundUploadForm>
   {
      @Override
      public void execute(Event<UIBackgroundUploadForm> event) throws Exception
      {
         UIBackgroundUploadForm uploadForm = event.getSource();
         UIFormMultiValueInputSet multiInput = uploadForm.findComponentById(UIBackgroundUploadForm.MULTI_IMAGE);

         UIFormUploadInput uploadInput;
         StringBuilder invalidFiles = new StringBuilder();
         for (UIComponent child : multiInput.getChildren())
         {
            if (child instanceof UIFormUploadInput)
            {
               uploadInput =(UIFormUploadInput)child;
               UploadResource uploadResource =uploadInput.getUploadResource();
               if (uploadResource == null)
               {
                  continue;
               }
               if (!uploadResource.getMimeType().startsWith("image/"))
               {
                  invalidFiles.append(uploadResource.getFileName()).append(", ");                  
                  continue;
               }
               try
               {
                  saveToDB(uploadInput);
               }
               catch (ImageQuantityException e)
               {
                  ApplicationMessage msg = new ApplicationMessage("UIBackgroundUploadForm.msg.image.quantity.exceed",
                     new String[] {String.valueOf(e.getQuantity())}, ApplicationMessage.ERROR);
                  Util.getUIPortalApplication().addMessage(msg);
                  event.getRequestContext().addUIComponentToUpdateByAjax(uploadForm);
                  return;
               }
               cleanUploadedFile(uploadInput);               
            }
         }

         if (invalidFiles.length() > 0)
         {
//            Temporary not show the file name, need to backport GTNPORTAL-1240 first
//            invalidFiles.delete(invalidFiles.lastIndexOf(", "), invalidFiles.length());
//            ApplicationMessage msg = new ApplicationMessage("UIBackgroundUploadForm.msg.invalid.image",
//               new String[] {invalidFiles.toString()}, ApplicationMessage.WARNING);
//            msg.setArgsLocalized(false);
            ApplicationMessage msg = new ApplicationMessage("UIBackgroundUploadForm.msg.invalid.image",
               null, ApplicationMessage.ERROR);
            Util.getUIPortalApplication().addMessage(msg);
            event.getRequestContext().addUIComponentToUpdateByAjax(uploadForm);
            return;                        
         }
         uploadForm.backToImageList(event.getRequestContext());
      }

      private boolean saveToDB(UIFormUploadInput uploadInput) throws Exception
      {
         UploadResource uploadResource =uploadInput.getUploadResource();
         DesktopBackgroundService backgroundService = uploadInput.getApplicationComponent(DesktopBackgroundService.class);

         UIPortal uiPortal = Util.getUIPortal();
         backgroundService.uploadBackgroundImage(new PortalKey(uiPortal.getOwnerType(), uiPortal.getOwner()), uploadResource.getFileName(),
            uploadResource.getMimeType(), "UTF-8", uploadInput.getUploadDataAsStream());
         return true;
      }

      private void cleanUploadedFile(UIFormUploadInput uploadInput)
      {
         UploadService uploadService = uploadInput.getApplicationComponent(UploadService.class);
         uploadService.removeUploadResource(uploadInput.getUploadId());         
      }
   }

   static public class AddActionListener extends EventListener<UIBackgroundUploadForm>
   {
      @Override
      public void execute(Event<UIBackgroundUploadForm> event) throws Exception
      {
         UIBackgroundUploadForm uploadForm = event.getSource();
         WebuiRequestContext rcontext = event.getRequestContext();
         rcontext.addUIComponentToUpdateByAjax(uploadForm);
      }
   }

   static public class RemoveActionListener extends EventListener<UIBackgroundUploadForm>
   {
      @Override
      public void execute(Event<UIBackgroundUploadForm> event) throws Exception
      {
         UIBackgroundUploadForm uploadForm = event.getSource();
         WebuiRequestContext rcontext = event.getRequestContext();
         rcontext.addUIComponentToUpdateByAjax(uploadForm);
      }
   }

   static public class BackActionListener extends EventListener<UIBackgroundUploadForm>
   {
      @Override
      public void execute(Event<UIBackgroundUploadForm> event) throws Exception
      {
         UIBackgroundUploadForm uploadForm = event.getSource();
         uploadForm.backToImageList(event.getRequestContext());
      }
   }
}
