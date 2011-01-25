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
package org.exoplatform.webos.common.filter;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.filter.Filter;
import org.exoplatform.webos.webui.page.UIDesktopPage;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Create an user Desktop page whenever user access the portal
 * 
 * @author <a href="mailto:ndkhoi168@gmail.com">Nguyen Duc Khoi</a>
 */
public class LazyDesktopPageCreation implements Filter
{
   protected static final Log log = ExoLogger.getLogger(LazyDesktopPageCreation.class);
   private static final String PAGE_ID = "webos";
   private static final String PAGE_TITLE = "WebOS Page";
   private static final String NODE_NAME = "classicWebosPage";
   private static final String NODE_LABEL = "WebOS Page";

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String userName = httpRequest.getRemoteUser();
      try
      {
         ExoContainer container = ExoContainerContext.getCurrentContainer();
         RequestLifeCycle.begin(container);
         
         if (userName != null)
         {
            try
            {
               //Need this code to add Class obj to realClass of UIPage
               Class.forName(UIDesktopPage.class.getName());
            }
            catch (ClassNotFoundException e)
            {
               throw new ServletException(e);
            }
            createPortalConfig(container, userName);
            createUserPage(container, userName);
         }
      }
      catch (Throwable t)
      {
         log.debug("Could not create User Portal Config or User Desktop Page for " + userName, t);
      }
      finally
      {
         chain.doFilter(httpRequest, response);
         RequestLifeCycle.end();
      }
   }

   private void createPortalConfig(ExoContainer container, String userName) throws Exception
   {
      DataStorage storage = (DataStorage) container.getComponentInstance(DataStorage.class);
      PortalConfig portalConfig = storage.getPortalConfig(PortalConfig.USER_TYPE, userName);

      if (portalConfig == null)
      {
         UserPortalConfigService configService = (UserPortalConfigService)PortalContainer.getComponent(UserPortalConfigService.class);
         configService.createUserSite(userName);
      }
   }
   
   private void createUserPage(ExoContainer container, String userName) throws Exception
   {
      DataStorage storage = (DataStorage) container.getComponentInstance(DataStorage.class);

      Page page = storage.getPage(PortalConfig.USER_TYPE + "::" + userName + "::" + PAGE_ID);
      if (page == null)
      {
         page = new Page();
         page.setName(PAGE_ID);
         page.setTitle(PAGE_TITLE);
         page.setFactoryId(UIDesktopPage.DESKTOP_FACTORY_ID);
         page.setShowMaxWindow(true);
         page.setOwnerType(PortalConfig.USER_TYPE);
         page.setOwnerId(userName);
         storage.create(page);
      }

      PageNavigation pageNavigation = storage.getPageNavigation(PortalConfig.USER_TYPE, userName);
      PageNode pageNode = null;
      if (pageNavigation == null)
      {
         pageNavigation = new PageNavigation();
         pageNavigation.setOwnerType(PortalConfig.USER_TYPE);
         pageNavigation.setOwnerId(userName);
         storage.create(pageNavigation);
      }
      else
      {
         pageNode = pageNavigation.getNode(NODE_NAME);
      }

      if (pageNode == null)
      {
         pageNode = new PageNode();
         pageNode.setName(NODE_NAME);
         pageNode.setUri(NODE_NAME);
         pageNode.setLabel(NODE_LABEL);

         pageNavigation.addNode(pageNode);
      }

      pageNode.setVisibility(Visibility.SYSTEM);
      pageNode.setPageReference(page.getPageId());

      storage.save(pageNavigation);
   }
}