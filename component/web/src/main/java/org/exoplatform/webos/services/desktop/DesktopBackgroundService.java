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
 * The interface defines API to manage background image of a desktop page
 *
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

public interface DesktopBackgroundService
{
   /**
    * Upload an image to user's folder in jcr, using name provided in parameter backgroundImageName. If there is already
    * an image having such name in JCR folder, a suffix is inserted to avoid naming conflict.
    *
    * @param siteKey
    * @param backgroundImageName
    * @param mimeType
    * @param encoding
    * @throws org.exoplatform.webos.services.desktop.exception.ImageQuantityException by default user can have 10 images
    * @throws org.exoplatform.webos.services.desktop.exception.ImageSizeException by default image's size limit is 2mb  
    */
   public boolean uploadBackgroundImage(PortalKey siteKey, String backgroundImageName, String mimeType, String encoding, InputStream binaryStream) throws Exception;

   /**
    * Remove user background image
    *
    * @param siteKey
    * @param backgroundImageName - the name of the image file
    * @throws IllegalStateException if image doesn't exists
    */
   public boolean removeBackgroundImage(PortalKey siteKey, String backgroundImageName) throws Exception;

   /**
    * Get @link{org.exoplatform.webos.services.desktop.DesktopBackground} object associated to the page specified
    * by pageID
    *
    * @param pageID
    * @return @link{org.exoplatform.webos.services.desktop.DesktopBackground}
    * @throws Exception
    */
   public DesktopBackground getCurrentDesktopBackground(String pageID) throws Exception;

   /**
    * Get the list of backgrounds applicable to the site identified by siteKey
    *
    * @param siteKey
    * @return
    * @throws Exception
    */
   public List<DesktopBackground> findDesktopBackgrounds(PortalKey siteKey) throws Exception;

   /**
    * Return the DesktopBackground object associated to the site siteKey, and having name imageName.
    *
    * If the imageName is null or if there is no such named desktop background, the method returns null
    *
    * @param siteKey - the key of portal site
    * @param imageName - the name of the image file
    */
   public DesktopBackground getDesktopBackground(PortalKey siteKey, String imageName) throws Exception;

   /**
    * Set current user background image
    * If image is null, or doesn't exists current background will be reset
    *
    * @param pageID
    * @param imageName - the name of the image file
    * @throws IllegalStateException if image doesn't exists
    */
   public void setSelectedBackgroundImage(String pageID, String imageName) throws Exception;

   /**
    * return size limit of an image uploaded
    */
   public int getSizeLimit();

   /**
    * Render the desktop background with an image stored in a site
    * 
    * @param req
    * @param resp
    * @param siteKey site contains the image which want to set as desktop background
    * @param imageName
    * @throws IOException
    */
   public void renderImage(HttpServletRequest req, HttpServletResponse resp, PortalKey siteKey, String imageName) throws IOException;

}
