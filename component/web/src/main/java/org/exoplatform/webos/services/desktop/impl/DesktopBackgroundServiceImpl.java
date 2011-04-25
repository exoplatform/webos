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

import org.chromattic.ext.ntdef.NTFile;
import org.chromattic.ext.ntdef.NTFolder;
import org.chromattic.ext.ntdef.NTHierarchyNode;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webos.services.desktop.exception.ImageQuantityException;
import org.exoplatform.webos.services.desktop.exception.ImageSizeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

public class DesktopBackgroundServiceImpl implements DesktopBackgroundService
{
   private static final Log log = ExoLogger.getExoLogger("portal:DesktopBackgroundServiceImpl");

   private DataStorage dataStorage;

   //Path to folder that contains all image folders for all sites. By default, it is "/backgrounds"
   private String defaultImagePath;
   
   // 0 means unlimited
   private int quantityLimit;

   /**
    * 0 means unlimited<br>
    * This is applied for each image
    */
   private int sizeLimit;

   public DesktopBackgroundServiceImpl(DataStorage dataStorage,  InitParams params) throws Exception
   {
      this.dataStorage = dataStorage;

      if (params != null)
      {
         ValueParam quantityParam = params.getValueParam("image.limit.quantity");
         if (quantityParam != null)
         {
            quantityLimit = Integer.parseInt(quantityParam.getValue());
         }
         ValueParam sizeParam = params.getValueParam("image.limit.size");
         if (sizeParam != null)
         {
            sizeLimit = Integer.parseInt(sizeParam.getValue());
         }
         ValueParam pathParam = params.getValueParam("default.image.path");
         if (pathParam != null)
         {
            defaultImagePath = pathParam.getValue();
         }
      }
   }

   public int getSizeLimit()
   {
      return sizeLimit;
   }

   @Override
   public boolean removeBackgroundImage(PortalKey siteKey, String backgroundImageName) throws Exception
   {
      PersonalBackgroundSpace space = getSpace(siteKey, false);
      if (space == null)
      {
         //TODO: Throws an exception here
         return false;
      }

      if (backgroundImageName !=null)
      {
         if (space.getBackgroundImageFolder().getChild(backgroundImageName) == null)
         {
            throw new IllegalStateException("Image doesn't exists");
         }
      }
      space.getBackgroundImageFolder().getChildren().remove(backgroundImageName);
      return true;
   }

   private PersonalBackgroundSpace getSpace(PortalKey siteKey, boolean create) throws Exception
   {
      if (siteKey == null)
      {
         return null;
      }
      PortalConfig cfg = dataStorage.getPortalConfig(siteKey.getType(), siteKey.getId());
      PersonalBackgroundSpace space = dataStorage.adapt(cfg, PersonalBackgroundSpace.class, create);
      if (space != null)
      {
         NTFolder folder = space.getBackgroundImageFolder();
         if (folder == null && create) {
            folder = space.createFolder();
            space.setBackgroundImageFolder(folder);
            space.uploadDefaultBackgroundImage(defaultImagePath);
         }
      }
      return space;
   }

   @Override
   public boolean uploadBackgroundImage(PortalKey siteKey, String backgroundImageName, String mimeType, String encoding,
         InputStream binaryStream) throws Exception
   {
     if (siteKey == null || backgroundImageName == null || mimeType == null || encoding == null || binaryStream == null)
     {
        throw new IllegalArgumentException("One of the arguments is null");
     }

     //
     PersonalBackgroundSpace space = getSpace(siteKey, true);
     NTFolder folder = space.getBackgroundImageFolder();
     Map<String,NTHierarchyNode> children = folder.getChildren();
     if (quantityLimit != 0 && children.size() == quantityLimit)
     {
        log.debug("Each user can only have" + quantityLimit + " background images");
        throw new ImageQuantityException(quantityLimit);
     }
     if (sizeLimit != 0 && sizeLimit < binaryStream.available()/1024.0/1024)
     {
        log.debug("Can't upload, naximum image size is :" + sizeLimit);
        throw new ImageSizeException(sizeLimit, backgroundImageName);
     }

     backgroundImageName = processDuplicatedName(space, backgroundImageName);
     return space.uploadBackgroundImage(backgroundImageName, mimeType, encoding, binaryStream);
   }

   private String processDuplicatedName(PersonalBackgroundSpace space, String imgName)
   {
      int dotIndex = imgName.lastIndexOf(".");
      if (dotIndex == -1)
      {
         dotIndex = imgName.length();
      }
      StringBuilder nameBuilder = new StringBuilder(imgName).insert(dotIndex, "(0)");

      int idx = 0;
      while (space.getBackgroundImageFolder().getChild(imgName) != null)
      {
         nameBuilder.replace(dotIndex + 1, nameBuilder.indexOf(")", dotIndex), String.valueOf(idx++));
         imgName = nameBuilder.toString();
      }
      return imgName;
   }

   @Override
   public DesktopBackground getCurrentDesktopBackground(String pageID) throws Exception
   {
      if(pageID == null)
      {
         return null;
      }

      Page desktopPage = dataStorage.getPage(pageID);
      if (desktopPage == null)
      {
         throw new IllegalStateException("page : " + pageID + " doen't exists");
      }
      DesktopPageMetadata pageMetadata = dataStorage.adapt(desktopPage, DesktopPageMetadata.class);
      NTFile selectedBackground = pageMetadata.getBackgroundImage();
      if (selectedBackground != null)
      {
         return new DesktopBackground(makeImageURL(parsePageID(pageID), selectedBackground), selectedBackground.getName());
      }
      return null;
   }

   @Override
   public void setSelectedBackgroundImage(String pageID, String imageName) throws Exception
   {
      Page desktopPage = dataStorage.getPage(pageID);
      if (desktopPage == null)
      {
         throw new IllegalStateException("page : " + pageID + " doen't exists");
      }
      DesktopPageMetadata pageMetadata = dataStorage.adapt(desktopPage, DesktopPageMetadata.class);
      if (imageName != null)
      {
         PortalKey siteKey = parsePageID(pageID);
         PersonalBackgroundSpace space = getSpace(siteKey, true);
         NTHierarchyNode child = space.getBackgroundImageFolder().getChild(imageName);
         if (child == null)
         {
            throw new IllegalStateException("Image doesn't exists");
         }
         if (child instanceof NTFile)
         {
            NTFile image = (NTFile)child;
            pageMetadata.setBackgroundImage(image);
            dataStorage.save(desktopPage);
         }
         else
         {
            throw new IllegalStateException("Image doesn't exists");
         }
      }
      else
      {
         pageMetadata.setBackgroundImage(null);
         dataStorage.save(desktopPage);
      }
   }

   private PortalKey parsePageID(String pageID)
   {
      String[] idFrags = pageID.split("::");
      if (idFrags.length < 3)
      {
         throw new IllegalArgumentException("Can't parse pageID :" + pageID);
      }
      return new PortalKey(idFrags[0], idFrags[1]);
   }

   @Override
   public List<DesktopBackground> findDesktopBackgrounds(PortalKey siteKey) throws Exception
   {
      PersonalBackgroundSpace space = getSpace(siteKey, true);
      List<DesktopBackground> backgroundList = new ArrayList<DesktopBackground>();
      if (space != null)
      {
         NTFolder backgroundFolder = space.getBackgroundImageFolder();
         if (backgroundFolder != null)
         {
            for(NTHierarchyNode child : backgroundFolder.getChildren().values())
            {
               if (child instanceof NTFile)
               {
                  NTFile file = (NTFile)child;
                  backgroundList.add(new DesktopBackground(makeImageURL(siteKey, file), file.getName()));
               }
            }
         }
      }

      //
      return backgroundList;
   }

   @Override
   public DesktopBackground getDesktopBackground(PortalKey siteKey, String imageName) throws Exception
   {
      if (imageName == null)
      {
         return null;
      }
      
      PersonalBackgroundSpace space = getSpace(siteKey, true);
      if (space == null)
      {
         throw new IllegalStateException("Can't found PersonalBackgroundSpace for :" + siteKey);
      }
      NTFolder backgroundFolder = space.getBackgroundImageFolder();
      NTHierarchyNode child = backgroundFolder.getChildren().get(imageName);
      if (child instanceof NTFile)
      {
         NTFile file = (NTFile)child;
         return new DesktopBackground(makeImageURL(siteKey, file), file.getName());
      }
      return null;      
   }

   private String makeImageURL(PortalKey siteKey, NTFile file)
   {
      ServletContext sc = (ServletContext)PortalContainer.getInstance().getComponentInstance(ServletContext.class);

      // We do that because the org.exoplatform.test.mocks.servlet.MockServletContext
      // does not implement getContextPath which raise a java.lang.AbstractMethodError during unit tests
      // until this is fixed
      String contextPath = sc.getClass().getSimpleName().equals("MockServletContext") ? "/mock" : sc.getContextPath();

      //
      return contextPath + "/webos/" + siteKey.getType() + "/" + siteKey.getId().replace("/", "_") + "/" + file.getName();
   }

   @Override
   public void renderImage(HttpServletRequest req, HttpServletResponse resp, PortalKey siteKey, String imageName) throws IOException
   {
      try
      {
         PersonalBackgroundSpace space = getSpace(siteKey, true);

         //
         Resource res = null;
         if (space != null)
         {
            NTFolder folder = space.getBackgroundImageFolder();
            if (folder != null)
            {
               NTHierarchyNode child = folder.getChild(imageName);
               if (child instanceof NTFile)
               {
                  NTFile file = (NTFile)child;
                  res = file.getContentResource();
               }
            }
         }

         //
         if (res != null)
         {
            String mediaType = res.getMimeType();
            byte[] data = res.getData();

            // Send data
            resp.setContentType(mediaType);
            resp.setContentLength(data.length);
            OutputStream out = resp.getOutputStream();
            try
            {
               out.write(data);
            }
            finally
            {
               Safe.close(out);
            }
         }
         else
         {
            resp.sendError(404, "Could not find image for background (" + siteKey + "," + imageName + ")");
         }
      }
      catch (Exception e)
      {
         log.error("Could not render image for background (" + siteKey + "," + imageName + ")", e);
         resp.sendError(500, e.getMessage());
      }
   }
}
