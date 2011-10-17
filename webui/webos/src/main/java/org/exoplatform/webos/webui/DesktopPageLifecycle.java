/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.webos.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 8/10/11
 */
public class DesktopPageLifecycle implements ApplicationLifecycle<PortalRequestContext>
{

   public static String DESKTOP_FACTORY_ID = "Desktop";
   public static final String PAGE_ID = "webos";
   public static final String PAGE_TITLE = "WebOS Page";
   public static final String NODE_NAME = "classicWebosPage";
   public static final String NODE_LABEL = "WebOS Page";

   public void onInit(Application app) throws Exception
   {
   }

   public void onStartRequest(Application app, PortalRequestContext context) throws Exception
   {
      String userName = context.getRemoteUser();
      if(userName != null && SiteType.USER == context.getSiteType() && userName.equals(context.getSiteName()) &&NODE_NAME.equals(context.getNodePath()))
      {
         //We are sure that the user 's site exists as the UserSiteLifecycle is invoked before DesktopPageLifecycle
         UserPortalConfig pconfig = context.getUserPortalConfig();
         UserPortal userPortal = pconfig.getUserPortal();
         UserNode rootNode = userPortal.getNode(userPortal.getNavigation(context.getSiteKey()), Scope.CHILDREN, UserNodeFilterConfig.builder().build(), null);
         if(rootNode.getChild(NODE_NAME) == null)
         {
            DataStorage storage = (DataStorage)PortalContainer.getComponent(DataStorage.class);
            Page page = storage.getPage(SiteType.USER.getName() + "::" + userName + "::" + PAGE_ID);
            if(page == null)
            {
               page = new Page();
               //We don't use the constant defined in UIDesktopPage even if they are accessible from current class.
               //As DesktopPageLifecycle is invoked before entering UI layer, it's not logical to have this class
               //depends on UI class
               page.setName(PAGE_ID);
               page.setTitle(PAGE_TITLE);
               page.setFactoryId(DESKTOP_FACTORY_ID);
               page.setShowMaxWindow(true);
               page.setOwnerType(SiteType.USER.getName());
               page.setOwnerId(userName);
               storage.create(page);
            }

            UserNode desktopNode = rootNode.addChild(NODE_NAME);
            desktopNode.setLabel(NODE_LABEL);
            desktopNode.setPageRef(page.getPageId());

            userPortal.saveNode(rootNode, null);
         }
      }
   }

   public void onFailRequest(Application app, PortalRequestContext context, RequestFailure failureType) throws Exception
   {
   }

   public void onEndRequest(Application app, PortalRequestContext context) throws Exception
   {
   }

   public void onDestroy(Application app) throws Exception
   {
   }
}
