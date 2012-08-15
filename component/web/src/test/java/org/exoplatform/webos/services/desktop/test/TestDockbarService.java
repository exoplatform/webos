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
package org.exoplatform.webos.services.desktop.test;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.webos.services.dockbar.DockbarService;

import java.util.Arrays;

public class TestDockbarService extends AbstractWebOSTest
{
   private ChromatticManager chromatticManager;
   private DockbarService dockbarService;
   private DataStorage dataStorage;
   private Page rootPage;
   private Page demoPage;
   private ConversationState rootState;
   private ConversationState demoState;

   @Override
   protected void setUp() throws Exception
   {
      PortalContainer portalContainer = getContainer();
      chromatticManager = (ChromatticManager)portalContainer.getComponentInstanceOfType(ChromatticManager.class);
      dockbarService = (DockbarService)portalContainer.getComponentInstanceOfType(DockbarService.class);      
      dataStorage = (DataStorage)portalContainer.getComponentInstanceOfType(DataStorage.class);

      MembershipEntry managerMem = new MembershipEntry("/platform/administrators", "manager");
      MembershipEntry userMem = new MembershipEntry("/platform/users", "member");
      rootState = new ConversationState(new Identity("root", Arrays.asList(managerMem, userMem)));
      demoState = new ConversationState(new Identity("demo", Arrays.asList(userMem)));

      begin();      
      createPages();
   }

   @Override
   protected void tearDown() throws Exception
   {
      chromatticManager.getSynchronization().setSaveOnClose(false);
      end();
   }

   public void testInjectDockbarApp() throws Exception
   {
      rootPage = dataStorage.getPage("user::root::webos");
      demoPage = dataStorage.getPage("user::demo::webos");      
      assertEquals(0, rootPage.getChildren().size());
      assertEquals(0, demoPage.getChildren().size());

      //Permission : manager:/platform/administrators
      ConversationState.setCurrent(rootState);
      dockbarService.injectDockbarApps(rootPage);
      rootPage = dataStorage.getPage("user::root::webos");
      assertEquals(2, rootPage.getChildren().size());

      Application appReg = (Application)rootPage.getChildren().get(1);
      assertEquals("Application Registry", appReg.getTitle());
      assertEquals("SimpleSkin:SimpleWebOSTheme", appReg.getTheme());

      //Permission : Everyone
      ConversationState.setCurrent(demoState);
      dockbarService.injectDockbarApps(demoPage);
      demoPage = dataStorage.getPage("user::demo::webos");
      assertEquals(1, demoPage.getChildren().size());

      Application homePortlet = (Application)demoPage.getChildren().get(0);
      assertEquals("Home Page portlet", homePortlet.getTitle());
      assertEquals("Default:WebosTheme", homePortlet.getTheme());      
   }

   private void createPages() throws Exception
   {
      rootPage = new Page();
      rootPage.setOwnerId("root");
      rootPage.setOwnerType("user");
      rootPage.setName("webos");

      demoPage = new Page();
      demoPage.setOwnerId("demo");
      demoPage.setOwnerType("user");
      demoPage.setName("webos");

      dataStorage.create(rootPage);
      dataStorage.create(demoPage);
   }
}
