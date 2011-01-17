/*
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

import java.util.Map;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.ext.ntdef.NTFolder;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

@PrimaryType(name = "webos:desktopBackgroundRegistry")
@FormattedBy(WebOSChromatticFormatter.class)
public abstract class DesktopBackgroundRegistry
{

   @OneToMany
   public abstract Map<String, PersonalBackgroundSpace> getPersonalBackgroundSpaces();
   
   @Create
   public abstract PersonalBackgroundSpace createPersonalBackgroundSpace();
   
   public PersonalBackgroundSpace getPersonalBackgroundSpace(String title)
   {
      return getPersonalBackgroundSpaces().get(title);
   }
   
   public PersonalBackgroundSpace getPersonalBackgroundSpace(String title, boolean autoCreated)
   {
      if (title == null)
      {
         return null;
      }
	   PersonalBackgroundSpace space = getPersonalBackgroundSpaces().get(title);
	   if(space == null && autoCreated)
	   {
		   space = addPersonalBackgroundSpace(title);
		   NTFolder folder = space.createBackgroundImageFolder();
		   space.setBackgroundImageFolder(folder);
		   space.uploadDefaultBackgroundImage();
	   }
	   
	   return space;
   }
   
   public PersonalBackgroundSpace addPersonalBackgroundSpace(String title)
   {
      PersonalBackgroundSpace space = createPersonalBackgroundSpace();
      getPersonalBackgroundSpaces().put(title, space);
      return space;
   }
   
   public void removePersonalBackgroundSpace(String title)
   {
      getPersonalBackgroundSpaces().put(title, null);
   }
}
