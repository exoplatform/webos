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

package org.exoplatform.webos.services.dockbar;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

/**
 *
 * The DockbarInjectionListener is designed as a plugin of Kernel's ListenerService component. It handles the
 * event PAGE_CREATED, which is fired as a Page object is created via DataStorage component.
 *
 */
public class DockbarInjectionListener extends Listener<DataStorage, Page>
{
   public static final String DESKTOP_ID = "Desktop";
   private DockbarService dockbarService;


   public DockbarInjectionListener(DockbarService dockbarService)
   {
      this.dockbarService = dockbarService;
   }

   @Override
   public void onEvent(Event<DataStorage, Page> event) throws Exception
   {
      Page page = event.getData();
      if (!DESKTOP_ID.equals(page.getFactoryId()))
         return;

      dockbarService.injectDockbarApps(page);
   }

}
