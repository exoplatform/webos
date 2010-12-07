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
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Sep 14, 2010
 */

public class DesktopBackgroundServiceImpl implements DesktopBackgroundService
{

   private ChromatticManager chromatticManager;

   private ChromatticLifeCycle chromatticLifecycle;

   public DesktopBackgroundServiceImpl(ChromatticManager manager, InitParams params) throws Exception
   {
      chromatticManager = manager;
      chromatticLifecycle = (WebOSChromatticLifecycle) manager.getLifeCycle("webos");
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
   public boolean removeBackgroundImage(String userName, String backgroundImageName)
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      PersonalBackgroundSpace space = backgroundRegistry .getPersonalBackgroundSpace(userName);
      if (space == null)
      {
         //TODO: Throws an exception here
         return false;
      }

      space.getBackgroundImageFolder().addChild(backgroundImageName, null);
      return true;
   }

   @Override
   public boolean uploadBackgroundImage(String userName, String backgroundImageName, String mimeType, String encoding,
         InputStream binaryStream)
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
      PersonalBackgroundSpace space = backgroundRegistry .getPersonalBackgroundSpace(userName, true);
      return space.uploadBackgroundImage(backgroundImageName, mimeType, encoding, binaryStream);
   }

   @Override
   public String getCurrentBackgroundImageURL(String userName)
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
			return "/rest/private/jcr/"
					+ chromatticLifecycle.getRepositoryName() + "/"
					+ chromatticLifecycle.getWorkspaceName()
					+ "/webos:desktopBackgroundRegistry/webos:" + userName
					+ "/webos:personalBackgroundFolder/" + selectedBackground;

		}
   }
   
   public void setSelectedBackgroundImage(String userName, String imageName)
   {
      DesktopBackgroundRegistry backgroundRegistry = initBackgroundRegistry();
	   PersonalBackgroundSpace space = backgroundRegistry.getPersonalBackgroundSpace(userName, true);
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
         backgroundList.add(new DesktopBackground("/rest/private/jcr/" + chromatticLifecycle.getRepositoryName() + "/" + chromatticLifecycle.getWorkspaceName()
                 + "/webos:desktopBackgroundRegistry/webos:" + userName + "/webos:personalBackgroundFolder/" + background, background));
      }
      return backgroundList;
   }
}
