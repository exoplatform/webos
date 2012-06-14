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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.application.UIApplication;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageActionListener.DeleteGadgetActionListener;
import org.exoplatform.portal.webui.page.UIPageLifecycle;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * May 19, 2006
 */

@ComponentConfigs({
   @ComponentConfig(lifecycle = UIPageLifecycle.class, template = "system:/groovy/portal/webui/page/UIDesktopPage.gtmpl", events = {
      @EventConfig(listeners = DeleteGadgetActionListener.class),
      @EventConfig(name = "RemoveChild", listeners = UIDesktopPage.RemovePortletActionListener.class),
      @EventConfig(listeners = UIDesktopPage.SaveWindowPropertiesActionListener.class),
      @EventConfig(listeners = UIDesktopPage.ShowAddNewApplicationActionListener.class),
      @EventConfig(listeners = UIDesktopPage.ShowPortletActionListener.class),
      @EventConfig(name = "EditCurrentPage", listeners = UIDesktopPage.EditCurrentPageActionListener.class)}),
      
   @ComponentConfig(id = "UIDesktopContextMenu", type = UIRightClickPopupMenu.class, template = "system:/groovy/portal/webui/page/UIDesktopContextMenu.gtmpl", events = {
      @EventConfig(listeners = UIDesktopPage.ShowAddNewApplicationActionListener.class),
      @EventConfig(listeners = UIDesktopPage.RefreshPageActionListener.class),
      @EventConfig(listeners = UIDesktopPage.EditCurrentPageActionListener.class),
      @EventConfig(listeners = UIDesktopPage.ChangeBackgroundActionListener.class)})
})
public final class UIDesktopPage extends UIPage
{

   public static String DESKTOP_FACTORY_ID = "Desktop";
   public static final String PAGE_ID = "webos";
   public static final String PAGE_TITLE = "WebOS Page";
   public static final String NODE_NAME = "classicWebosPage";
   public static final String NODE_LABEL = "WebOS Page";
   
   private DesktopBackground currBackground;

   public UIDesktopPage() throws Exception
   {
      setChildren((List<UIComponent>)new CopyOnWriteArrayList<UIComponent>());      
   }

   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      UIRightClickPopupMenu rightClickPopup = getChild(UIRightClickPopupMenu.class);
      if (rightClickPopup == null)
      {
         addChild(UIRightClickPopupMenu.class, "UIDesktopContextMenu", null);
      }
      
      super.processRender(context);
   }

   public boolean isShowMaxWindow()
   {
      return true;
   }

   /**
    * Invoked whenever the portlet window display in the desktop page is changed as following reasons:
    * <p><ul>
    * <li>Change the window position by drag and drop
    * <li>Resize width, height of the portlet window
    * <li>Change state of the portlet window between NORMALIZE, MINIMIZE and MAXIMIZE
    * <li>The portlet windows is activated or inactivate
    *
    */
   static public class SaveWindowPropertiesActionListener extends EventListener<UIPage>
   {
      public void execute(Event<UIPage> event) throws Exception
      {
         UIPage uiPage = event.getSource();
         String objectId = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);

         UIApplication uiApp = uiPage.getChildById(objectId);
         if (uiApp == null)
            return;

         /*########################## Save Position ##########################*/
         String posX = event.getRequestContext().getRequestParameter("posX");
         String posY = event.getRequestContext().getRequestParameter("posY");
         if (posX != null)
            uiApp.getProperties().put(UIApplication.locationX, posX);
         if (posY != null)
            uiApp.getProperties().put(UIApplication.locationY, posY);

         /*########################## Save ZIndex ##########################*/
         String zIndex = event.getRequestContext().getRequestParameter(UIApplication.zIndex);
         if (zIndex != null)
            uiApp.getProperties().put(UIApplication.zIndex, zIndex);

         /*########################## Save Dimension ##########################*/
         String windowWidth = event.getRequestContext().getRequestParameter("windowWidth");
         String windowHeight = event.getRequestContext().getRequestParameter("windowHeight");

         if (windowWidth != null)
            uiApp.setWidth(windowWidth);
         if (windowHeight != null)
            uiApp.setHeight(windowHeight);

         /*########################## Save Window status (SHOW / HIDE) ##########################*/
         String appStatus = event.getRequestContext().getRequestParameter(UIApplication.appStatus);
         if (appStatus != null)
            uiApp.getProperties().put(UIApplication.appStatus, appStatus);
      }
   }

   static public abstract class BaseDesktopActionListener extends EventListener<UIComponent>
   {
      public void execute(Event<UIComponent> event) throws Exception
      {
         UIComponent source = event.getSource();
         UIPage uiPage;
         if (source instanceof UIPage)
         {
            uiPage = (UIPage)source;
         }
         else
         {
            uiPage = source.getAncestorOfType(UIPage.class);
         }
         doAction(event, uiPage);
      }

      protected abstract void doAction(Event<UIComponent> event, UIPage uiPage) throws Exception;
   }

   /**
    * Show the 'Add application' form
    * <p>Invoked if user click 'Add applications' icon on dockbar or select 'Add application' item in right click menu context 
    */
   static public class ShowAddNewApplicationActionListener extends BaseDesktopActionListener
   {
      protected void doAction(Event<UIComponent> event, UIPage uiPage) throws Exception
      {
         UIPortalApplication uiPortalApp = uiPage.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWorkspace = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

         UIAddNewApplication uiAddApplication = uiPage.createUIComponent(UIAddNewApplication.class, null, null);
         uiAddApplication.setInPage(true);
         uiAddApplication.setUiComponentParent(uiPage);

         uiMaskWorkspace.setWindowSize(700, 375);
         uiMaskWorkspace.setUIComponent(uiAddApplication);
         uiMaskWorkspace.setShow(true);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWorkspace);
      }
   }

   /**
    * Show the selected portlet by click on portlet icon in the dockbar
    */
   static public class ShowPortletActionListener extends EventListener<UIPage>
   {
      public void execute(Event<UIPage> event) throws Exception
      {
         UIPage uiPage = event.getSource();
         String portletId = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
         UIPortlet uiPortlet = uiPage.getChildById(portletId);
         uiPortlet.getProperties().setProperty("appStatus", "SHOW");
         event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      }
   }
   
   public void showEditBackgroundPopup(WebuiRequestContext context) throws Exception
   {
      PortalRequestContext pContext = (PortalRequestContext)context;
      
      UIPortalApplication uiPortalApp = (UIPortalApplication)pContext.getUIApplication();
      UIMaskWorkspace maskWorkspace = uiPortalApp.findComponentById(UIPortalApplication.UI_MASK_WS_ID);
      
      maskWorkspace.createUIComponent(UIBackgroundSelector.class, "backgroundSelector", "backgroundSelector");
      
      pContext.addUIComponentToUpdateByAjax(maskWorkspace);
   }

   public void setDesktopBackground(DesktopBackground desktopBackground)
   {
      this.currBackground = desktopBackground;
      showDesktopBackground(currBackground);
   }

   public void showDesktopBackground(DesktopBackground desktopBackground)
   {
      String backgroundURL = null;
      if (desktopBackground != null)
      {
         backgroundURL = "'" + desktopBackground.getImageURL() + "'";
      }
      JavascriptManager jsManager =WebuiRequestContext.getCurrentInstance().getJavascriptManager();
      jsManager.addOnLoadJavascript("eXo.desktop.UIDesktop.setDesktopBackground(" + backgroundURL + ")");
   }

   public String getCurrBackgroundLabel() throws Exception
   {
      initBackground();
      return currBackground.getImageLabel();
   }

   public String getCurrBackgroundURL() throws Exception
   {
      initBackground();
      return currBackground.getImageURL();
   }

   private void initBackground() throws Exception
   {
      if (currBackground == null)
      {
         DesktopBackgroundService service = getApplicationComponent(DesktopBackgroundService.class);
         currBackground = service.getCurrentDesktopBackground(getPageId());
         if (currBackground == null)
         {
            currBackground = new DesktopBackground();
         }
      }
   }
   
   /**
    * Return the path to a Dock icon corresponding to the portlet.
    * <p>
    * This is used only in template
    * @param window
    * @return
    */
   private String getApplicationIconImageLocation(UIPortlet window)
   {
  	String applicationId = window.getApplicationId();
  	 
		if (applicationId.indexOf("/") >= 0) {
			String imgLocation = applicationId.substring(0, applicationId
					.indexOf("/"))
					+ "/skin/DefaultSkin/portletIcons/"
					+ applicationId.substring(applicationId.indexOf("/") + 1,
							applicationId.length());
			return imgLocation;
		}
		else{
			//Currently hardcode
			return "";
		}
   }
   
   static public class RemovePortletActionListener extends EventListener<UIDesktopPage>
   {
  	 public void execute(Event<UIDesktopPage> event) throws Exception {
  		 UIDesktopPage desktopPage = event.getSource();
       String id = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
       PortalRequestContext pcontext = (PortalRequestContext)event.getRequestContext();
       desktopPage.removeChildById(id);
       Page page = (Page) PortalDataMapper.buildModelObject(desktopPage);
       if (page.getChildren() == null) {
				page.setChildren(new ArrayList<ModelObject>());
       }
       DataStorage dataService = desktopPage
					.getApplicationComponent(DataStorage.class);
       dataService.save(page);
       pcontext.setFullRender(false);
       pcontext.setResponseComplete(true);
       pcontext.getWriter().write(EventListener.RESULT_OK);
       
  	 }
   }

   @Override
   public void switchToEditMode() throws Exception
   {
      showPageForm(this);
   }

   @Override
   public void switchToEditMode(Page page) throws Exception
   {
      if (page == null)
      {
         return;
      }
      UIDesktopPage desktopPage = createUIComponent(UIDesktopPage.class, null, null);
      PortalDataMapper.toUIPage(desktopPage, page);
      showPageForm(desktopPage);
   }

   private void showPageForm(UIDesktopPage uiDesktopPage) throws Exception
   {
      UIPortalApplication portalApp = Util.getUIPortalApplication();

      UIDesktopPageForm pageForm = portalApp.createUIComponent(UIDesktopPageForm.class, null, null);
      pageForm.setValues(uiDesktopPage);

      UIMaskWorkspace maskWorkspace = portalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      maskWorkspace.setUIComponent(pageForm);
      Util.getPortalRequestContext().addUIComponentToUpdateByAjax(maskWorkspace);
   }
   
   public static class EditCurrentPageActionListener extends BaseDesktopActionListener
   {
      @Override
      protected void doAction(Event<UIComponent> event, UIPage uiPage) throws Exception
      {
         uiPage.switchToEditMode();
      }
   }

   public static class RefreshPageActionListener extends EventListener<UIRightClickPopupMenu>
   {
      @Override
      public void execute(Event<UIRightClickPopupMenu> event) throws Exception
      {
         PortalRequestContext context = Util.getPortalRequestContext();
         UIPortalApplication uiApp = Util.getUIPortalApplication();
         context.addUIComponentToUpdateByAjax(uiApp.<UIComponent>getChildById(UIPortalApplication.UI_WORKING_WS_ID));
         context.setFullRender(true);
      }
   }

   public static class ChangeBackgroundActionListener extends BaseDesktopActionListener
   {
      @Override
      protected void doAction(Event<UIComponent> event, UIPage uiPage) throws Exception
      {
         ((UIDesktopPage)uiPage).showEditBackgroundPopup(event.getRequestContext());
      }
   }
}