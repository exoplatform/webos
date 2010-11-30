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


/**
 * @author Minh Hoang TO - hoang281283@gmail.com
 *
 * Nov 4, 2010
 */
public class DockbarIcon {

	private /*final*/ String iconName;
	
	private String accessPermission;
		
	private String i18nLabel;
	
	//Use this non-arg constructor before figuring out JIBX mapping problem in
	//configuration file. Don't ask!
	public DockbarIcon()
	{
		iconName = "";
		accessPermission = "";
		i18nLabel = "";
	}
	
	/*
	 * 
	public DockbarIcon(String _iconName)
	{
		if(_iconName == null)
		{
			throw new IllegalArgumentException();
		}
		
		iconName = _iconName;
		accessPermission = "";
	}
	
	*/
	/*
	public DockbarIcon(String _iconName, String _accessPermission)
	{
		this(_iconName);
		accessPermission = _accessPermission;
	}
	*/
	
	
	public void setAccessPermission(String _accessPermission)
	{
		accessPermission = _accessPermission;
	}
	
	public String getAccessPermission()
	{
		return accessPermission;
	}
	
	public String getIconName()
	{
		return iconName;
	}
	
	public void setIconName(String _iconName)
	{
		iconName = _iconName;
	}
	public void setI18nLabel(String i18nExpression)
	{
		this.i18nLabel = i18nExpression;
	}
	
	public String getI18nLabel()
	{
		return i18nLabel;
	}

}
