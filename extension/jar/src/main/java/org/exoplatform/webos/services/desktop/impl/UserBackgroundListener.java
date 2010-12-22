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
package org.exoplatform.webos.services.desktop.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;

/**
 * 
 * @author <a href="mailto:ndkhoi168@gmail.com">Nguyen Duc Khoi</a>
 */
public class UserBackgroundListener extends UserEventListener
{
   @Override
   public void preDelete(User user)
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      
      DesktopBackgroundService backgroundService = (DesktopBackgroundService) pcontainer.getComponentInstance(DesktopBackgroundService.class);
      backgroundService.removeUserBackground(user.getUserName());
   }
}
