/**
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.webui.application.PortletState;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/** Created by The eXo Platform SARL Author : Anh Nguyen ntuananh.vn@gmail.com Oct 18, 2007 */
@ComponentConfig(template = "system:/groovy/portal/webui/application/UIAddNewApplication.gtmpl", events = {
   @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class),
   @EventConfig(listeners = UIAddNewApplication.AddApplicationActionListener.class)})
public class UIAddNewApplication extends UIContainer
{

   private List<ApplicationCategory> listAppCategories = new ArrayList<ApplicationCategory>();

   private UIComponent uiComponentParent;

   private boolean isInPage;
   private UserACL userACL;

   public UIAddNewApplication() throws Exception
   {
      userACL = getApplicationComponent(UserACL.class);
      initApplicationCategories();
   }

   public List<ApplicationCategory> getApplicationCategories() throws Exception
   {
      return listAppCategories;
   }

   private void initApplicationCategories() throws Exception
   {
      ApplicationRegistryService prService = getApplicationComponent(ApplicationRegistryService.class);
      String remoteUser = WebuiRequestContext.getCurrentInstance().getRemoteUser();
      if (remoteUser == null || remoteUser.equals(""))
         return;

      List<ApplicationCategory> appCategories = prService.getApplicationCategories(new ApplicationCategoryComparator());

      Iterator<ApplicationCategory> cateItr = appCategories.iterator();
      while (cateItr.hasNext())
      {
         ApplicationCategory cate = cateItr.next();

         if (!hasPermission(cate.getAccessPermissions()) || filterApps(cate).size() < 1)
         {
            cateItr.remove();
         }
         else
         {
            Collections.sort(cate.getApplications(), new ApplicationComparator());
         }
      }

      listAppCategories = appCategories;
   }

   private List<Application> filterApps(ApplicationCategory applicationCategory)
   {
      List<Application> applications = new ArrayList<Application>();

      if (applicationCategory.getApplications() == null)
      {
         return applications;
      }
      for (Application app : applicationCategory.getApplications())
      {
         if (hasPermission(app.getAccessPermissions()))
         {
            applications.add(app);
         }
      }
      applicationCategory.setApplications(applications);
      return applications;
   }

   private boolean hasPermission(List<String> accessPermissions)
   {
      if (accessPermissions == null || accessPermissions.size() == 0)
      {
         return false;
      }

      for (String permission : accessPermissions)
      {
         if (userACL.hasPermission(permission))
         {
            return true;
         }
      }
      return false;
   }

   public UIComponent getUiComponentParent()
   {
      return uiComponentParent;
   }

   public void setUiComponentParent(UIComponent uiComponentParent)
   {
      this.uiComponentParent = uiComponentParent;
   }

   public boolean isInPage()
   {
      return isInPage;
   }

   public void setInPage(boolean isInPage)
   {
      this.isInPage = isInPage;
   }

   private Application getApplication(String id) throws Exception
   {

      List<ApplicationCategory> pCategories = getApplicationCategories();

      for (ApplicationCategory pCategory : pCategories)
      {
         List<Application> applications = pCategory.getApplications();
         for (Application application : applications)
         {
            if (application.getId().equals(id))
            {
               return application;
            }
         }
      }

      return null;
   }

   /**
    * Add Application to UiPage
    *
    * @param event
    * @throws Exception
    */
   private static void addApplicationToPage(Event<UIAddNewApplication> event, boolean atStartup) throws Exception
   {
      UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
      UIPortal uiPortal = uiPortalApp.getShowedUIPortal();
      PortalRequestContext pcontext = Util.getPortalRequestContext();

      UIDesktopPage uiDesktopPage = uiPortal.findFirstComponentOfType(UIDesktopPage.class);
      if (uiDesktopPage == null)
      {
         pcontext.addUIComponentToUpdateByAjax(uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID));
         UIMaskWorkspace maskWorkspace = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         maskWorkspace.createEvent("Close", Event.Phase.DECODE, event.getRequestContext()).broadcast();
         pcontext.setFullRender(true);
         return;
      }

      String applicationId = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

      Application application = event.getSource().getApplication(applicationId);
      ApplicationType appType = application.getType();
      
      UIPortlet uiPortlet = uiDesktopPage.createUIComponent(UIPortlet.class, null, null);
      ApplicationState appState;

      // TODO: Check if there 's already a portlet window of this portlet. A CloneApplicationState
      // should be created in such case
      appState = new TransientApplicationState<Object>(application.getContentId());

      uiPortlet.setState(new PortletState(appState, appType));
      uiPortlet.setPortletInPortal(false);

      if (atStartup) {
      	uiPortlet.getProperties().setProperty("appStatus", "HIDE");
      }

      String portletName = application.getApplicationName();
  		String displayName = application.getDisplayName();
			if (displayName != null) {
				uiPortlet.setTitle(displayName);
			} 
			else if (portletName != null) {
				uiPortlet.setTitle(portletName);
			}
			uiPortlet.setDescription(application.getDescription());
			List<String> accessPers = application.getAccessPermissions();
			String[] accessPermissions = accessPers.toArray(new String[accessPers
					.size()]);
			uiPortlet.setAccessPermissions(accessPermissions);
      

      // Add portlet to page
      uiDesktopPage.addChild(uiPortlet);

      if (uiDesktopPage.isModifiable())
      {
         Page page = (Page)PortalDataMapper.buildModelObject(uiDesktopPage);
         if (page.getChildren() == null)
         {
            page.setChildren(new ArrayList<ModelObject>());
         }
         DataStorage dataService = uiPortalApp.getApplicationComponent(DataStorage.class);
         dataService.save(page);
         
         //Rebuild the uiPage to synchronize (storageId, storageName) mapping
         page = dataService.getPage(page.getPageId());
         page.setModifiable(true);
         uiDesktopPage.getChildren().clear();
         PortalDataMapper.toUIPage(uiDesktopPage, page);
      }

      UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      pcontext.setFullRender(true);
      pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
   }

   static class ApplicationCategoryComparator implements Comparator<ApplicationCategory>
   {
      public int compare(ApplicationCategory cat1, ApplicationCategory cat2)
      {
         return cat1.getDisplayName().compareToIgnoreCase(cat2.getDisplayName());
      }
   }

   static class ApplicationComparator implements Comparator<Application>
   {
      public int compare(Application p1, Application p2)
      {
         return p1.getDisplayName().compareToIgnoreCase(p2.getDisplayName());
      }
   }

   static public class AddApplicationActionListener extends EventListener<UIAddNewApplication>
   {
      public void execute(Event<UIAddNewApplication> event) throws Exception
      {
         if (event.getSource().isInPage())
         {
            addApplicationToPage(event, false);
         }
      }
   }
}
