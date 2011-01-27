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

package org.exoplatform.dashboard.webui.component;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webos.webui.page.UIDesktopPage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@ComponentConfig(template = "app:/groovy/webui/component/dashboard/UIWebOSTabPaneDashboard.gtmpl", events = {
   @EventConfig(confirm = "UIWebOSTabPaneDashboard.msg.deleteTab", name = "DeleteTab", listeners = UIWebOSTabPaneDashboard.DeleteTabActionListener.class),
   @EventConfig(name = "AddDashboard", listeners = UIWebOSTabPaneDashboard.AddDashboardActionListener.class),
   @EventConfig(name = "RenameTabLabel", listeners = UIWebOSTabPaneDashboard.RenameTabLabelActionListener.class)})
public class UIWebOSTabPaneDashboard extends UIContainer
{

   private static Log logger = ExoLogger.getExoLogger(UIWebOSTabPaneDashboard.class);

   private UserPortalConfigService configService;

   private DataStorage dataService;

   private PageNavigation pageNavigation;

   private UIPortal uiPortal;

   final public static String PAGE_TEMPLATE = "dashboard";

   public UIWebOSTabPaneDashboard() throws Exception
   {
      configService = getApplicationComponent(UserPortalConfigService.class);
      dataService = getApplicationComponent(DataStorage.class);
      uiPortal = Util.getUIPortal();
      initPageNavigation();
   }

   private void initPageNavigation() throws Exception
   {
      //String remoteUser = Util.getPortalRequestContext().getRemoteUser();
      //pageNavigation = getPageNavigation(PortalConfig.USER_TYPE + "::" + remoteUser);
      //TODO: Check this part carefully
      this.pageNavigation = uiPortal.getSelectedNavigation();
   }

   public int getCurrentNumberOfTabs() throws Exception
   {
      return getSameSiblingsNode().size();
   }

   public List<PageNode> getSameSiblingsNode() throws Exception
   {
      List<PageNode> siblings = getPageNavigation().getNodes();
      List<PageNode> selectedPath = Util.getUIPortal().getSelectedPath();
      if (selectedPath != null && selectedPath.size() > 1)
      {
         PageNode currentParent = selectedPath.get(selectedPath.size() - 2);
         siblings = currentParent.getChildren();
      }
      return filterWebOSNode(siblings);
//      return siblings;
   }

   private List<PageNode> filterWebOSNode(List<PageNode> pageNodes) throws Exception
   {
      if (pageNodes == null || pageNodes.size() == 0)
      {
         return pageNodes;
      }
      List<PageNode> tempNodes = new ArrayList<PageNode>(pageNodes);
      PageNode node;
      for (int i = 0; i < tempNodes.size(); i++)
      {
         node = tempNodes.get(i);
         if (isWebOSNode(node))
         {
            tempNodes.remove(node);
         }
      }
      return tempNodes;
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

   public PageNavigation getPageNavigation() throws Exception
   {
      if (pageNavigation == null)
      {
         initPageNavigation();
      }
      return pageNavigation;
   }

   /**
    * Remove node specified by nodeIndex and returns the node to switch to
    * @param nodeIndex
    * @return
    */
   public PageNode removePageNode(int nodeIndex)
   {
      try
      {
         List<PageNode> nodes = getSameSiblingsNode();
         PageNode tobeRemoved = nodes.get(nodeIndex);
         PageNode selectedNode = uiPortal.getSelectedNode();

         boolean isRemoved = true; // To check 
         PageNavigation updateNav =
            dataService.getPageNavigation(pageNavigation.getOwnerType(), pageNavigation.getOwnerId());
         for (PageNode pageNode : updateNav.getNodes())
         {
            if (pageNode.getUri().equals(tobeRemoved.getUri()))
            {
               isRemoved = false;
               break;
            }
         }

         if (nodes.size() >= 2)
         {
            // Remove node
            pageNavigation.getNodes().remove(tobeRemoved);
            //In WebOS we need to clone the nodes and filter the WebOS page. See getSameSiblingsNode method
            nodes.remove(tobeRemoved);
            
            // Choose selected Node
            if (tobeRemoved.getUri().equals(selectedNode.getUri()))
            {
               selectedNode = nodes.get(Math.max(0, nodeIndex - 1));

            }
            else if (!nodes.contains(selectedNode))
            {
               selectedNode = nodes.get(0);
            }

            // Update
            if (!isRemoved)
            {
               String pageRef = tobeRemoved.getPageReference();
               if (pageRef != null && pageRef.length() > 0)
               {
                  Page page = configService.getPage(pageRef);
                  if (page != null)
                     dataService.remove(page);
                  UIPortal uiPortal = Util.getUIPortal();
                  // Remove from cache
                  uiPortal.setUIPage(pageRef, null);
               }
               //uiPortal.setSelectedNode(selectedNode);
               dataService.save(pageNavigation);
            }
         }
         else
         {
            getAncestorOfType(UIApplication.class).addMessage(
               new ApplicationMessage("UIWebOSTabPaneDashboard.msg.cannotDeleteLastTab", null));
            return null;
         }

         return selectedNode;
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   public String createNewPageNode(String nodeLabel)
   {
      try
      {
         if (nodeLabel == null || nodeLabel.length() == 0)
         {
            nodeLabel = "Tab_" + getCurrentNumberOfTabs();
         }
         Page page =
            configService.createPageTemplate(UIWebOSTabPaneDashboard.PAGE_TEMPLATE, pageNavigation.getOwnerType(),
               pageNavigation.getOwnerId());
         page.setTitle(nodeLabel);

         List<PageNode> selectedPath = uiPortal.getSelectedPath();
         PageNode parentNode = null;
         if (selectedPath != null && selectedPath.size() > 1)
         {
            parentNode = selectedPath.get(selectedPath.size() - 2);
         }

         PageNode pageNode = new PageNode();
         pageNode.setLabel(nodeLabel);
         String uniqueNodeName = nodeLabel.toLowerCase().replace(' ', '_');
         if (nameExisted(uniqueNodeName))
         {
            uniqueNodeName = uniqueNodeName + "_" + System.currentTimeMillis();
         }

         String fullName = (parentNode != null) ? parentNode.getUri() + "/" + uniqueNodeName : uniqueNodeName;

         page.setName(uniqueNodeName + "_" + page.hashCode());
         pageNode.setName(uniqueNodeName);
         pageNode.setUri(fullName);
         pageNode.setPageReference(page.getPageId());

         if (parentNode == null)
         {
            pageNavigation.addNode(pageNode);
         }
         else if (parentNode.getChildren() != null)
         {
            parentNode.getChildren().add(pageNode);
         }

         //uiPortal.setSelectedNode(pageNode);

         dataService.create(page);
         dataService.save(pageNavigation);

         return fullName;
      }
      catch (Exception ex)
      {
         logger.info("Could not create page template", ex);
         return null;
      }
   }

   private boolean validateName(String label)
   {
      if (label == null || label.length() < 1)
      {
         return false;
      }
      label = label.trim();
      if (label.length() < 1 || Character.isDigit(label.charAt(0)) || label.charAt(0) == '-')
      {
         return false;
      }
      for (int i = 0; i < label.length(); i++)
      {
         char c = label.charAt(i);
         if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-' || Character.isSpaceChar(c))
         {
            continue;
         }
         return false;
      }
      return true;
   }

   private boolean nameExisted(String nodeName)
   {
      for (PageNode node : pageNavigation.getNodes())
      {
         if (node.getName().equals(nodeName))
         {
            return true;
         }
      }
      return false;
   }

   public String renamePageNode(int nodeIndex, String newNodeLabel)
   {
      try
      {
         List<PageNode> nodes = getSameSiblingsNode();
         PageNode renamedNode = nodes.get(nodeIndex);
         if (renamedNode == null || newNodeLabel.length() == 0)
         {
            return null;
         }

         renamedNode.setLabel(newNodeLabel);

         String newNodeName = newNodeLabel.toLowerCase().replace(' ', '_');
         if (nameExisted(newNodeName))
         {
            newNodeName = newNodeName + "_" + System.currentTimeMillis();
         }
         renamedNode.setName(newNodeName);

         List<PageNode> selectedPath = Util.getUIPortal().getSelectedPath();
         PageNode parentNode = null;
         if (selectedPath != null && selectedPath.size() > 1)
         {
            parentNode = selectedPath.get(selectedPath.size() - 2);
         }
         String newUri = (parentNode != null) ? parentNode.getUri() + "/" + newNodeName : newNodeName;

         renamedNode.setUri(newUri);

         Page page = configService.getPage(renamedNode.getPageReference());
         page.setTitle(newNodeLabel);
         if (page != null)
            dataService.save(page);
         
         dataService.save(pageNavigation);
         return newUri;
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   static public class DeleteTabActionListener extends EventListener<UIWebOSTabPaneDashboard>
   {
      public void execute(Event<UIWebOSTabPaneDashboard> event) throws Exception
      {
         UIWebOSTabPaneDashboard source = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         int removedNodeIndex = Integer.parseInt(context.getRequestParameter(UIComponent.OBJECTID));
         PageNode selectedNode = source.removePageNode(removedNodeIndex);

         //If the node is removed successfully, then redirect to the node specified by tab on the left
         if (selectedNode != null)
         {
            // set maximizedUIComponent of UIPageBody is null if it is maximized portlet of removed page
            UIPortal uiPortal = Util.getUIPortal();
            UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
            if (uiPageBody != null && uiPageBody.getMaximizedUIComponent() != null)
            {
               uiPageBody.setMaximizedUIComponent(null);
            }

            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.setResponseComplete(true);
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(selectedNode.getUri(), "UTF-8"));
         }
      }
   }

   static public class AddDashboardActionListener extends EventListener<UIWebOSTabPaneDashboard>
   {
      public void execute(Event<UIWebOSTabPaneDashboard> event) throws Exception
      {
         UIWebOSTabPaneDashboard tabPane = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         String newTabLabel = context.getRequestParameter(UIComponent.OBJECTID);
         if (!tabPane.validateName(newTabLabel))
         {
            //TODO nguyenanhkien2a@gmail.com
            //We should redirect to current node while adding new tab fails
            PageNode currentNode = tabPane.uiPortal.getSelectedNode();
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(currentNode.getUri(), "UTF-8"));
            
            Object[] args = {newTabLabel};
            context.getUIApplication().addMessage(new ApplicationMessage("UIWebOSTabPaneDashboard.msg.wrongTabName", args));
            return;
         }
         String uri = tabPane.createNewPageNode(newTabLabel);

         //If new node is created with success, then redirect to it
         if (uri != null)
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.setResponseComplete(true);
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(uri, "UTF-8"));
         }
      }
   }

   static public class RenameTabLabelActionListener extends EventListener<UIWebOSTabPaneDashboard>
   {

      final public static String RENAMED_TAB_LABEL_PARAMETER = "newTabLabel";

      public void execute(Event<UIWebOSTabPaneDashboard> event) throws Exception
      {
         UIWebOSTabPaneDashboard tabPane = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         int nodeIndex = Integer.parseInt(context.getRequestParameter(UIComponent.OBJECTID));
         String newTabLabel = context.getRequestParameter(RENAMED_TAB_LABEL_PARAMETER);
         if (!tabPane.validateName(newTabLabel))
         {
            //We should redirect to current node while renaming fails
            PageNode currentNode = tabPane.uiPortal.getSelectedNode();
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(currentNode.getUri(), "UTF-8"));
            
            Object[] args = {newTabLabel};
            context.getUIApplication().addMessage(new ApplicationMessage("UIWebOSTabPaneDashboard.msg.wrongTabName", args));
            return;
         }
         String newUri = tabPane.renamePageNode(nodeIndex, newTabLabel);

         //If page node is renamed with success, then redirect to new URL
         if (newUri != null)
         {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            prContext.getResponse().sendRedirect(prContext.getPortalURI() + URLEncoder.encode(newUri, "UTF-8"));
         }
      }
   }
}
