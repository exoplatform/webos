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

import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.webos.services.dockbar.BaseDockbarService;
import org.exoplatform.webos.services.dockbar.DockbarIcon;

/**
 * @author Minh Hoang TO - hoang281283@gmail.com
 *
 * Nov 11, 2010
 */
public class DockbarServiceImpl extends BaseDockbarService {

	private UserACL userACL;
	
	public DockbarServiceImpl(UserACL _userACL, InitParams params) throws Exception{
		super();
		
		userACL = _userACL;
		List<DockbarIcon> configuredDockbarIcons = params.getObjectParamValues(DockbarIcon.class);
		this.utilIcons.addAll(configuredDockbarIcons);
	}
	
	@Override
	public boolean hasPermission(String remoteUser, DockbarIcon icon) {
		if(remoteUser == null)
		{
			return UserACL.EVERYONE.equals(icon.getAccessPermission());
		}
		else
		{
			return userACL.hasPermission(icon.getAccessPermission());
		}
	}
}
