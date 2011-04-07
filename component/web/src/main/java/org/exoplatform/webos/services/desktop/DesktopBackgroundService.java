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

import org.exoplatform.portal.pom.data.PortalKey;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

public interface DesktopBackgroundService
{

   /**
    * Upload image to user's folder in jcr, if imageName is duplicated, name's postfix will be add automatically
    * @throws org.exoplatform.webos.services.desktop.exception.ImageQuantityException by default user can have 10 images
    * @throws org.exoplatform.webos.services.desktop.exception.ImageSizeException by default image's size limit is 2mb  
    */
   public boolean uploadBackgroundImage(PortalKey siteKey, String backgroundImageName, String mimeType, String encoding, InputStream binaryStream) throws Exception;

   /**
    * Remove user background image, If it is current background, user background will be reset
    * @param userName - user that will remove this desktop background image
    * @param backgroundImageName - the name of the image file
    * @throws IllegalStateException if image doesn't exists
    */
   public boolean removeBackgroundImage(PortalKey siteKey, String backgroundImageName) throws Exception;
   
   public DesktopBackground getCurrentDesktopBackground(String pageID) throws Exception;
   
   public List<DesktopBackground> findDesktopBackgrounds(PortalKey siteKey) throws Exception;

   /**
    * if image doen't exists or imageName is null, return null
    * @param userName - user that will use this desktop background image
    * @param imageName - the name of the image file
    */
   public DesktopBackground getDesktopBackground(PortalKey siteKey, String imageName) throws Exception;

   /**
    * Set current user background image
    * If image is null, or doesn't exists current background will be reset
    * @param pageID
    * @param imageName - the name of the image file
    * @throws IllegalStateException if image doesn't exists
    */
   public void setSelectedBackgroundImage(String pageID, String imageName) throws Exception;

   public int getSizeLimit();

   public void renderImage(HttpServletRequest req, HttpServletResponse resp, PortalKey siteKey, String imageName) throws IOException;

}
