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

package org.exoplatform.webos.services.dockbar.impl;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.webos.services.dockbar.DockbarPlugin;
import org.exoplatform.webos.services.dockbar.DockbarService;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Minh Hoang TO - hoang281283@gmail.com
 *
 * Nov 11, 2010
 */
public class DockbarServiceImpl implements DockbarService
{
   public static final String DEFAULT_WIDOW_THEME = SkinService.DEFAULT_SKIN + ":WebosTheme";
   private UserACL userACL;
   private DataStorage dataStorage;
   private Map<String, Application> dockbarApps;
   
	public DockbarServiceImpl(UserACL _userACL, DataStorage dataStorage) throws Exception
   {
		this.userACL = _userACL;
      this.dataStorage = dataStorage;
      dockbarApps = new HashMap<String, Application>();
	}

   @Override
   public void injectDockbarApps(Page page) throws Exception
   {
      if (page == null)
      {
         throw new IllegalArgumentException("Can't inject apps to a null page");
      }

      int lastChildSize = page.getChildren().size();
      for (Application app : dockbarApps.values())
      {
         for (String permission : app.getAccessPermissions())
         {
            if (userACL.hasPermission(permission))
            {
               page.getChildren().add(app);
               break;
            }
         }
      }

      if (lastChildSize != page.getChildren().size())
      {
         dataStorage.save(page);
      }
   }
   
   public void addDockbarConfig(ComponentPlugin plugin)
   {
      if (plugin instanceof DockbarPlugin)
      {
         for(Application app :  ((DockbarPlugin)plugin).getApplications())
         {                                                                       
            if (app.getTheme() == null) 
            {
               app.setTheme(DEFAULT_WIDOW_THEME);
            }
            dockbarApps.put(((TransientApplicationState)app.getState()).getContentId(), app);  
         }
      }
   }
}
