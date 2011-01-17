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
package org.exoplatform.webos.services.desktop;

import java.io.InputStream;
import java.util.List;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

public interface DesktopBackgroundService
{

   public boolean uploadBackgroundImage(String userName, String backgroundImageName, String mimeType, String encoding, InputStream binaryStream) throws Exception;

   /**
    * Remove user background image, If it is current background, user background will be reset
    * @param userName - user that will remove this desktop background image
    * @param backgroundImageName - the name of the image file
    * @throws IllegalStateException if image doesn't exists
    */
   public boolean removeBackgroundImage(String userName, String backgroundImageName); 
   
   /**
    * Remove all background of an user
    * 
    * @param userName
    * @return
    */
   public void removeUserBackground(String userName);
   
   public DesktopBackground getCurrentDesktopBackground(String userName);
   
   public List<DesktopBackground> getUserDesktopBackgrounds(String userName);

   /**
    * if image doen't exists or imageName is null, return null
    * @param userName - user that will use this desktop background image
    * @param imageName - the name of the image file
    */
   public DesktopBackground getUserDesktopBackground(String userName, String imageName);

   /**
    * Set current user background image
    * If image is null, or doesn't exists current background will be reset
    * @param userName - user that will use this desktop background image
    * @param imageName - the name of the image file
    * @throws IllegalStateException if image doesn't exists
    */
   public void setSelectedBackgroundImage(String userName, String imageName);

   public int getSizeLimit();
}
