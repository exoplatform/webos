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
package org.exoplatform.webos.toolbar;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webos.webui.page.UIDesktopPage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <a href="mailto:ndkhoi168@gmail.com">Nguyen Duc Khoi</a>
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/webui/component/UIUserToolBarDesktopPortlet.gtmpl",
   events = {@EventConfig(name = "AddDefaultDashboard", listeners = UIUserToolbarDesktopPortlet.AddDashboardActionListener.class)})
public class UIUserToolbarDesktopPortlet extends UIPortletApplication
{
   public static String DEFAULT_TAB_NAME = "Tab_0";
   
   public UIUserToolbarDesktopPortlet() throws Exception
   {
   }

   public PageNavigation getCurrentUserNavigation() throws Exception
   {
      String remoteUser = Util.getPortalRequestContext().getRemoteUser();
      return getPageNavigation(PortalConfig.USER_TYPE + "::" + remoteUser);
   }

   private PageNavigation getPageNavigation(String owner) throws Exception
   {
      List<PageNavigation> allNavigations = Util.getUIPortalApplication().getNavigations();
      for (PageNavigation nav : allNavigations)
      {
         if (nav.getOwner().equals(owner))
            return nav;
      }
      return null;
   }

   public PageNode getSelectedPageNode() throws Exception
   {
      return Util.getUIPortal().getSelectedNode();
   }

   private boolean isWebOSNode(PageNode pageNode) throws Exception
   {
      if (pageNode == null)
      {
         return false;
      }
      String pageRef = pageNode.getPageReference();
      DataStorage ds = getApplicationComponent(DataStorage.class);
      Page page = ds.getPage(pageRef);
      return page != null && UIDesktopPage.DESKTOP_FACTORY_ID.equals(page.getFactoryId());
   }

   private PageNode getFirstNonWebOSNode(ArrayList<PageNode> nodes) throws Exception
   {
      for (PageNode node : nodes)
      {
         if (!isWebOSNode(node))
         {
            return node;
         }
      }
      return null;
   }

   static public class AddDashboardActionListener extends EventListener<UIUserToolbarDesktopPortlet>
   {

      private final static String PAGE_TEMPLATE = "dashboard";

      private static Log logger = ExoLogger.getExoLogger(AddDashboardActionListener.class);

      public void execute(Event<UIUserToolbarDesktopPortlet> event) throws Exception
      {
         UIUserToolbarDesktopPortlet toolBarPortlet = event.getSource();
         String nodeName = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

         PageNavigation cachedNavigation = toolBarPortlet.getCurrentUserNavigation();

         // Update navigation for prevent create first node which already existed
         DataStorage dataStorage = toolBarPortlet.getApplicationComponent(DataStorage.class);
         PageNavigation userNavigation =
            dataStorage.getPageNavigation(cachedNavigation.getOwnerType(), cachedNavigation.getOwnerId());
         cachedNavigation.merge(userNavigation);

         UserPortalConfigService configService = toolBarPortlet.getApplicationComponent(UserPortalConfigService.class);
         if (configService != null && cachedNavigation.getNodes().size() < 1 ||
               cachedNavigation.getNodes().size() == 1 && toolBarPortlet.isWebOSNode(cachedNavigation.getNodes().get(0)))
         {
            createDashboard(nodeName, cachedNavigation, toolBarPortlet);
         }
         else
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(
               prContext.getPortalURI() + toolBarPortlet.getFirstNonWebOSNode(cachedNavigation.getNodes()).getName());
         }
      }

      private static void createDashboard(String _nodeName, PageNavigation _pageNavigation,
         UIUserToolbarDesktopPortlet toolbarPortlet)
      {
         UserPortalConfigService _configService = toolbarPortlet.getApplicationComponent(UserPortalConfigService.class);
         try
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            if (_nodeName == null)
            {
               logger.debug("Parsed nodeName is null, hence use Tab_0 as default name");
               _nodeName = DEFAULT_TAB_NAME;
            }
            Page page =
               _configService.createPageTemplate(PAGE_TEMPLATE, _pageNavigation.getOwnerType(), _pageNavigation
                  .getOwnerId());
            page.setTitle(_nodeName);
            page.setName(_nodeName);

            PageNode pageNode = new PageNode();
            pageNode.setName(_nodeName);
            pageNode.setLabel(prContext.getApplicationResourceBundle().getString("UIUserToolBarDashboard.page.ClickAndType"));
            pageNode.setResolvedLabel(prContext.getApplicationResourceBundle());
            pageNode.setUri(_nodeName);
            pageNode.setPageReference(page.getPageId());

            _pageNavigation.addNode(pageNode);
            DataStorage ds = toolbarPortlet.getApplicationComponent(DataStorage.class);
            ds.create(page);
            ds.save(_pageNavigation);

            prContext.getResponse().sendRedirect(prContext.getPortalURI() + _nodeName);
         }
         catch (Exception ex)
         {
            logger.info("Could not create default dashboard page", ex);
         }
      }
   }
}
