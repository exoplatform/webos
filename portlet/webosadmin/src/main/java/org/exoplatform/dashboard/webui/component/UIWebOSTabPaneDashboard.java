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
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

   private UIPortal uiPortal;
   
   private UserNode cachedParent;

   final public static String PAGE_TEMPLATE = "dashboard";

   final private UserNodeFilterConfig filterConfig;
   
   static final private Scope TAB_PANE_DASHBOARD_SCOPE = Scope.CHILDREN;

   public UIWebOSTabPaneDashboard() throws Exception
   {
      configService = getApplicationComponent(UserPortalConfigService.class);
      dataService = getApplicationComponent(DataStorage.class);
      uiPortal = Util.getUIPortal();

      UserNodeFilterConfig.Builder scopeBuilder = UserNodeFilterConfig.builder();
      scopeBuilder.withAuthorizationCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
      scopeBuilder.withTemporalCheck();
      filterConfig = scopeBuilder.build();
   }

   private int getCurrentNumberOfTabs() throws Exception
   {
      return getSameSiblingsNode().size();
   }

   public UserNode getParentTab() throws Exception
   {
      UserPortal userPortal = getUserPortal();
      UserNode selectedNode =  uiPortal.getSelectedUserNode();
      UserNode currParent = selectedNode.getParent();
      
      UserNode parent = this.cachedParent;    
      if (parent == null || (currParent != null && !currParent.getId().equals(parent.getId())))
      {         
         if ("".equals(currParent.getURI()))
         {
            this.cachedParent = userPortal.getNode(currParent.getNavigation(), TAB_PANE_DASHBOARD_SCOPE, filterConfig, null);
         }
         else
         {
            this.cachedParent = userPortal.resolvePath(currParent.getNavigation(), filterConfig, currParent.getURI());            
         }
         parent = this.cachedParent;
      }
            
      if (parent != null)
      {
         try
         {            
            userPortal.updateNode(parent, TAB_PANE_DASHBOARD_SCOPE, null);
         }
         catch (NavigationServiceException e)
         {
            parent = null;
         }
      }      
      this.cachedParent = parent;      
      return parent;
   }

   public Collection<UserNode> getSameSiblingsNode() throws Exception
   {                                                                 
      UserNode parentTab =  getParentTab();

      if (parentTab == null)
      {
         return Collections.emptyList();
      }
      return filterWebOSNode(parentTab.getChildren());
   }
   
   /**
    * Return the current node uri, if it's been deleted, return first sibling node uri
    * if there is no node remain, return default path 
    * @throws Exception
    */
   public String getFirstAvailableURI() throws Exception
   {           
      UserNode parentTab = getParentTab();      
      if (parentTab != null)
      {
         UserNode currNode = Util.getUIPortal().getSelectedUserNode();
         if (parentTab.getChildren().size() == 0 && parentTab.getURI() != null)
         {
            return parentTab.getURI();
         } 
         
         if (parentTab.getChild(currNode.getName()) != null)
         {
            return currNode.getURI();
         } 
         else 
         {
            return parentTab.getChild(0).getURI(); 
         }
      }   

      return getUserPortal().getDefaultPath(null).getURI();
   }

   private Collection<UserNode> filterWebOSNode(Collection<UserNode> pageNodes) throws Exception
   {
      if (pageNodes == null || pageNodes.size() == 0)
      {
         return pageNodes;
      }
      List<UserNode> tempNodes = new ArrayList<UserNode>(pageNodes);
      UserNode node;
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

   private boolean isWebOSNode(UserNode pageNode) throws Exception
   {
      if (pageNode == null)
      {
         return false;
      }
      String pageRef = pageNode.getPageRef();
      if (pageRef == null)
      {
         return false;
      }
      DataStorage ds = getApplicationComponent(DataStorage.class);
      Page page = ds.getPage(pageRef);
      return page == null || UIDesktopPage.DESKTOP_FACTORY_ID.equals(page.getFactoryId());
   }
   
   private UserNavigation getCurrentUserNavigation() throws Exception
   {
      UserPortal userPortal = getUserPortal();
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      return userPortal.getNavigation(SiteKey.user(rcontext.getRemoteUser()));
   }

   private UserPortal getUserPortal()
   {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      return uiApp.getUserPortalConfig().getUserPortal();
   }

   /**
    * Remove node specified by nodeName and returns the node to switch to
    * @param nodeName - name of the Node that will be remove
    * @return return the node that should be selected after remove node
    */
   private UserNode removePageNode(String nodeName)
   {
      try
      {         
         UserNode parentNode = getParentTab();
         if (parentNode == null || parentNode.getChild(nodeName) == null)
         {
            return null;
         }

         UserNode tobeRemoved = parentNode.getChild(nodeName);
         UserNode prevNode = null;

         if (parentNode.getChildrenCount() >= 2)
         {
            for (UserNode child : parentNode.getChildren())
            {
               if (child.getName().equals(nodeName))
               {
                  parentNode.removeChild(nodeName);
                  break;
               }
               prevNode = child;
            }

            String pageRef = tobeRemoved.getPageRef();
            if (pageRef != null && pageRef.length() > 0)
            {
               Page page = configService.getPage(pageRef);
               if (page != null)
                  dataService.remove(page);
               UIPortal uiPortal = Util.getUIPortal();
               // Remove from cache
               uiPortal.setUIPage(pageRef, null);
            }
            getUserPortal().saveNode(parentNode, null);
         }
         else
         {
            getAncestorOfType(UIApplication.class).addMessage(
               new ApplicationMessage("UIWebOSTabPaneDashboard.msg.cannotDeleteLastTab", null));
            return null;
         }

         UserNode selectedNode = uiPortal.getSelectedUserNode();
         if (nodeName.equals(selectedNode.getName()))
         {
            selectedNode = prevNode != null ? prevNode : parentNode.getChildren().iterator().next();
         }
         return selectedNode;
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   private String createNewPageNode(String nodeLabel)
   {
      try
      {
         if (nodeLabel == null || nodeLabel.length() == 0)
         {
            nodeLabel = "Tab_" + getCurrentNumberOfTabs();
         }

         UserNavigation userNav = getCurrentUserNavigation();
         UserNode parentNode = getParentTab();
         if (userNav == null || parentNode == null)
         {
            return null;
         }

         String uniqueNodeName = nodeLabel.toLowerCase().replace(' ', '_');

         SiteKey siteKey = userNav.getKey();
         Page page =
            configService.createPageTemplate(UIWebOSTabPaneDashboard.PAGE_TEMPLATE, siteKey.getTypeName(), siteKey.getName());
         page.setTitle(nodeLabel);
         page.setName(uniqueNodeName + page.hashCode());
         dataService.create(page);

         if (parentNode.getChild(uniqueNodeName) != null)
         {
            uniqueNodeName = uniqueNodeName + "_" + System.currentTimeMillis();
         }

         UserNode tabNode = parentNode.addChild(uniqueNodeName);
         tabNode.setLabel(nodeLabel);
         tabNode.setPageRef(page.getPageId());

         getUserPortal().saveNode(parentNode, null);

         return tabNode.getURI();
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

   private String renamePageNode(String nodeName, String newNodeLabel)
   {
      try
      {
         UserNode parentNode = getParentTab();
         if (parentNode == null || parentNode.getChild(nodeName) == null)
         {
            return null;
         }
         UserNode renamedNode = parentNode.getChild(nodeName);
         renamedNode.setLabel(newNodeLabel);

         String newNodeName = newNodeLabel.toLowerCase().replace(' ', '_');
         if (parentNode.getChild(newNodeName) != null)
         {
            newNodeName = newNodeName + "_" + System.currentTimeMillis();
         }
         renamedNode.setName(newNodeName);

         Page page = configService.getPage(renamedNode.getPageRef());
         if (page != null)
         {
            page.setTitle(newNodeLabel);
            dataService.save(page);
         }

         getUserPortal().saveNode(parentNode, null);
         return renamedNode.getURI();
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   private String encodeURI(String uri) throws UnsupportedEncodingException
   {
      if (uri == null || uri.isEmpty())
      {
         return "";
      }
      String[] path = uri.split("/");
      StringBuilder uriBuilder = new StringBuilder();
      for (String name : path)
      {
         uriBuilder.append("/").append(URLEncoder.encode(name, "UTF-8"));
      }
      if (uriBuilder.indexOf("/") == 0)
      {
         uriBuilder.deleteCharAt(0);
      }
      return uriBuilder.toString();
   }

   static public class DeleteTabActionListener extends EventListener<UIWebOSTabPaneDashboard>
   {
      public void execute(Event<UIWebOSTabPaneDashboard> event) throws Exception
      {
         UIWebOSTabPaneDashboard source = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         String nodeName = context.getRequestParameter(UIComponent.OBJECTID);
         String newUri = source.getFirstAvailableURI();
         UserNode selectedNode = source.removePageNode(nodeName);

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
            newUri = selectedNode.getURI();
         }

         PortalRequestContext prContext = Util.getPortalRequestContext();
         prContext.setResponseComplete(true);
         prContext.getResponse().sendRedirect(prContext.getPortalURI() + source.encodeURI(newUri));
      }
   }

   static public class AddDashboardActionListener extends EventListener<UIWebOSTabPaneDashboard>
   {
      public void execute(Event<UIWebOSTabPaneDashboard> event) throws Exception
      {
         UIWebOSTabPaneDashboard tabPane = event.getSource();
         WebuiRequestContext context = event.getRequestContext();
         String newTabLabel = context.getRequestParameter(UIComponent.OBJECTID);
         String newUri = tabPane.getFirstAvailableURI();
         if (!tabPane.validateName(newTabLabel))
         {            
            Object[] args = {newTabLabel};
            context.getUIApplication().addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", args));
         }
         else
         {
            String uri = tabPane.createNewPageNode(newTabLabel);
            if (uri != null)
            {
               newUri = uri;
            }
         }

         PortalRequestContext prContext = Util.getPortalRequestContext();
         prContext.setResponseComplete(true);
         prContext.getResponse().sendRedirect(prContext.getPortalURI() + tabPane.encodeURI(newUri));
      }
   }

   static public class RenameTabLabelActionListener extends EventListener<UIWebOSTabPaneDashboard>
   {

      final public static String RENAMED_TAB_LABEL_PARAMETER = "newTabLabel";

      public void execute(Event<UIWebOSTabPaneDashboard> event) throws Exception
      {
         UIWebOSTabPaneDashboard tabPane = event.getSource();         
         WebuiRequestContext context = event.getRequestContext();
         UIApplication rootUI = context.getUIApplication();
                  
         String newTabLabel = context.getRequestParameter(RENAMED_TAB_LABEL_PARAMETER);
         String newUri = tabPane.getFirstAvailableURI();
         if (!tabPane.validateName(newTabLabel))
         {            
            Object[] args = {newTabLabel};
            rootUI.addMessage(new ApplicationMessage("UITabPaneDashboard.msg.wrongTabName", args));
         }
         else
         {
            String nodeName = context.getRequestParameter(UIComponent.OBJECTID);
            String returnUri = tabPane.renamePageNode(nodeName, newTabLabel);            
            if (returnUri != null)
            {            
               newUri = returnUri;               
            }
         }
         PortalRequestContext prContext = Util.getPortalRequestContext();
         prContext.getResponse().sendRedirect(prContext.getPortalURI() + tabPane.encodeURI(newUri));
         prContext.setResponseComplete(true);
      }
   }
}
