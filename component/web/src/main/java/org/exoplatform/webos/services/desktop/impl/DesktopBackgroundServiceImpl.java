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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticSession;
import org.chromattic.ext.ntdef.NTFolder;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webos.services.desktop.exception.ImageQuantityException;
import org.exoplatform.webos.services.desktop.exception.ImageSizeException;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

public class DesktopBackgroundServiceImpl implements DesktopBackgroundService
{
   private static final Log log = ExoLogger.getExoLogger("portal:DesktopBackgroundServiceImpl");

   private ChromatticManager chromatticManager;

   private ChromatticLifeCycle chromatticLifecycle;

   // 0 means unlimited
   private int quantityLimit;

   // 0 means unlimited
   //This is applied for each image
   private int sizeLimit;

   public DesktopBackgroundServiceImpl(ChromatticManager manager, InitParams params) throws Exception
   {
      chromatticManager = manager;
      chromatticLifecycle = manager.getLifeCycle("webos");

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
      }
   }

   public DesktopBackgroundRegistry initBackgroundRegistry()
   {
      DesktopBackgroundRegistry backgroundRegistry;
      Chromattic chromattic = chromatticLifecycle.getChromattic();
      ChromatticSession session = chromattic.openSession();

      backgroundRegistry = session.findByPath(DesktopBackgroundRegistry.class, "webos:desktopBackgroundRegistry");
      if (backgroundRegistry == null)
      {
         backgroundRegistry = session.insert(DesktopBackgroundRegistry.class, "webos:desktopBackgroundRegistry");
         session.save();
      }
      
      return backgroundRegistry; 
   }

   public ChromatticLifeCycle getChromatticLifecycle()
   {
      return this.chromatticLifecycle;
   }

   @Override
   public int getSizeLimit()
   {
      return sizeLimit;
   }

   @Override
   public boolean removeBackgroundImage(String userName, String backgroundImageName)
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      PersonalBackgroundSpace space = backgroundRegistry.getPersonalBackgroundSpace(userName);
      if (space == null)
      {
         //TODO: Throws an exception here
         return false;
      }

      if (backgroundImageName !=null)
      {
         if (backgroundImageName.equals(space.getCurrentBackground()))
         {
            space.setCurrentBackground(null);
         }
         if (space.getBackgroundImageFolder().getChild(backgroundImageName) == null)
         {
            throw new IllegalStateException("Image doesn't exists");
         }
      }
      space.getBackgroundImageFolder().getChildren().remove(backgroundImageName);
      return true;
   }

   @Override
   public boolean uploadBackgroundImage(String userName, String backgroundImageName, String mimeType, String encoding,
         InputStream binaryStream) throws Exception
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      PersonalBackgroundSpace space = backgroundRegistry .getPersonalBackgroundSpace(userName, true);
      if (quantityLimit != 0 && space.getBackgroundImageFolder().getChildren().size() == quantityLimit)
      {
         log.debug("Each user can only have" + quantityLimit + " background images");
         throw new ImageQuantityException(quantityLimit);
      }
      if (sizeLimit != 0 && sizeLimit < binaryStream.available()/1024.0/1024)
      {
         log.debug("Can't upload, naximum image size is :" + sizeLimit);
         throw new ImageSizeException(sizeLimit, backgroundImageName);
      }
      return space.uploadBackgroundImage(backgroundImageName, mimeType, encoding, binaryStream);
   }

   @Override
   public DesktopBackground getCurrentDesktopBackground(String userName)
   {
	  if(userName == null)
	  {
		  return null;
	  }
	  //TODO: Replace this method with a mixin in UIDesktopPage
	  DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      PersonalBackgroundSpace space = backgroundRegistry.getPersonalBackgroundSpace(userName, true);
      String selectedBackground = space.getCurrentBackground();
      
		if (selectedBackground == null) {
			return null;
		} else {
			return new DesktopBackground(makeImageURL(userName, selectedBackground), selectedBackground);
		}
   }

   public void setSelectedBackgroundImage(String userName, String imageName)
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
	   PersonalBackgroundSpace space = backgroundRegistry.getPersonalBackgroundSpace(userName, true);
      if (imageName !=null && space.getBackgroundImageFolder().getChild(imageName) == null)
      {
         space.setCurrentBackground(null);
         throw new IllegalStateException("Image doesn't exists");
      }
	   space.setCurrentBackground(imageName);
   }
   
   @Override
   public List<DesktopBackground> getUserDesktopBackgrounds(String userName)
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      PersonalBackgroundSpace space = backgroundRegistry.getPersonalBackgroundSpace(userName, true);
	  
      NTFolder backgroundFolder = space.getBackgroundImageFolder();
      Set<String> availableBackgrounds = backgroundFolder.getChildren().keySet();

      List<DesktopBackground> backgroundList = new ArrayList<DesktopBackground>();
      for(String background : availableBackgrounds)
      {
         backgroundList.add(new DesktopBackground(makeImageURL(userName, background), background));
      }
      return backgroundList;
   }

   @Override
   public DesktopBackground getUserDesktopBackground(String userName, String imageName)
   {
      if (imageName == null)
      {
         return null;
      }
      
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      PersonalBackgroundSpace space = backgroundRegistry.getPersonalBackgroundSpace(userName, true);
      if (space == null)
      {
         throw new IllegalStateException("Can't found PersonalBackgroundSpace for :" + userName);
      }
      NTFolder backgroundFolder = space.getBackgroundImageFolder();
      if (backgroundFolder.getChildren().containsKey(imageName))
      {
         return new DesktopBackground(makeImageURL(userName, imageName), imageName);
      }
      return null;      
   }

   private String makeImageURL(String userName, String imageLabel)
   {
      StringBuilder urlBuilder = new StringBuilder("/");
      urlBuilder.append(PortalContainer.getCurrentPortalContainerName()).append("/rest/jcr/");
      urlBuilder.append(chromatticLifecycle.getRepositoryName()).append("/");
      urlBuilder.append(chromatticLifecycle.getWorkspaceName()).append("/webos:desktopBackgroundRegistry/webos:");
      urlBuilder.append(userName).append("/webos:personalBackgroundFolder/").append(imageLabel);

      return urlBuilder.toString();
   }

   @Override
   public void removeUserBackground(String userName)
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      backgroundRegistry.removePersonalBackgroundSpace(userName);
   }
}
