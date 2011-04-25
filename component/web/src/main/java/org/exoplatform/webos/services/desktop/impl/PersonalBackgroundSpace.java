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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Set;

import javax.servlet.ServletContext;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.common.IO;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.chromattic.ext.ntdef.NTFolder;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.services.jcr.impl.core.query.OnWorkspaceInconsistency;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.common.util.ParameterValidation;
import org.gatein.mop.core.api.workspace.SiteImpl;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

@MixinType(name = "webos:site")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("webos")
public abstract class PersonalBackgroundSpace
{
   
   @Create
   public abstract NTFolder createFolder();
   
   @OneToOne(type=RelationshipType.EMBEDDED)
   public abstract SiteImpl getSiteParent();
   
   public abstract void setSiteParent(SiteImpl site);
   
   private static final Log log = ExoLogger.getExoLogger(PersonalBackgroundSpace.class);
   
   @OneToOne
   @Owner
   @MappedBy("webos:files")
   public abstract NTFolder getBackgroundImageFolder();
   
   public abstract void setBackgroundImageFolder(NTFolder backgroundImageFolder);
   
   public boolean uploadBackgroundImage(String imageName, String mimeType, String encoding, InputStream binaryStream)
   {
	   try
	   {
		   NTFolder imageFolder = getBackgroundImageFolder();
		   byte[] content = IO.getBytes(binaryStream);
		   imageFolder.createFile(imageName, new Resource(mimeType, encoding, content));
		   binaryStream.close();
		   return true;
	   }
	   catch(Exception ex)
	   {
		   log.error(ex.getMessage(), ex);
		   return false;
	   }
   }
      
   /**
    * Help to upload default images. </br> 
    * The images are loaded from the images folder configed for each sites. </br> 
    * For example: group::/platform/administrators . The images should be in "/backgrounds/group/platform/administrators/" </br>
    * If the folder exists but is empty, that site will not have any default images   
    * If there is no folder that was configed for specific site.  Images that are in default folder will be loaded (/backgrounds/default) </br>   
    * @param path - path to folder that contains all image folders for all sites. By default, it is "/backgrounds"
    */
   protected void uploadDefaultBackgroundImage(String path)
   {
      if (ParameterValidation.isNullOrEmpty(path))
      {
         log.warn("path parameter is null or empty");
         return;
      }
      
      PortalContainer pcontainer = PortalContainer.getInstance();
      ServletContext mergedContext = pcontainer.getPortalContext();
            
      String resourcePath = buildPath(path, mergedContext);      
      Set<String> defaultImages = mergedContext.getResourcePaths(resourcePath);

      if(defaultImages == null)
      {
         log.debug("No images at path " + resourcePath);
         return;
      }

      for(String defaultImage : defaultImages)
      {
         String mimeType = mergedContext.getMimeType(defaultImage); 
         if(mimeType != null && mimeType.startsWith("image/"))
         {
            int indexOfLastSlash = defaultImage.lastIndexOf('/');
            String displayName = defaultImage.substring(indexOfLastSlash + 1, defaultImage.length());

            uploadBackgroundImage(displayName, mimeType, "UTF-8", mergedContext.getResourceAsStream(defaultImage));
         }
      }
   }

   /**
    * Return the path to images folder configed for specific site </br>
    * If it doesn't exist, return default folder "{path}/default"
    * @param path
    * @param mergedContext 
    */
   private String buildPath(String path, ServletContext mergedContext)
   {
      SiteImpl site = getSiteParent();
      String ownerType = Mapper.getOwnerType(site.getObjectType());
      String ownerID= site.getName();       
      
      StringBuilder pathBuilder = new StringBuilder(path);
      pathBuilder.append("/").append(ownerType);
      if (!ownerID.startsWith("/"))
      {
         pathBuilder.append("/");
      }
      pathBuilder.append(ownerID).append("/");

      String pathForSite = pathBuilder.toString();      
      int idx = pathBuilder.lastIndexOf("/", pathBuilder.length() - 2);
      Set<String> folders = mergedContext.getResourcePaths(pathBuilder.substring(0, idx));
      if (folders != null && folders.contains(pathForSite))
      {
         return pathForSite;
      }
      
      log.debug("Use default path. No images at path :" + pathForSite);      
      pathBuilder.delete(path.length(), pathBuilder.length());
      pathBuilder.append("/default/");      
      return pathBuilder.toString();
   }
}
