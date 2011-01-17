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

package org.exoplatform.webos.services.desktop.test;

import org.chromattic.api.Chromattic;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webos.services.desktop.DesktopBackground;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;
import org.exoplatform.webos.services.desktop.exception.ImageQuantityException;
import org.exoplatform.webos.services.desktop.exception.ImageSizeException;
import org.exoplatform.webos.services.desktop.impl.PersonalBackgroundSpace;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@ConfiguredBy({
	@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/webos-desktop-service-configuration.xml"),
	@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/webos-jcr-configuration.xml"),
	@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml")
})
public class TestDesktopBackgroundService extends AbstractKernelTest
{
   private ChromatticManager chromatticManager;
   private DesktopBackgroundService desktopBackgroundService;
   private String userName;
   private String imageName;
   private String mimeType;
   private String encoding;
   private InputStream imgStream;

   @Override
   protected void setUp() throws Exception
   {
      userName = "testUserName";
      imageName = "testImageName";
      mimeType = "image/jpeg";
      encoding = "UTF-8";
      imgStream = new ByteArrayInputStream(new byte[] {0, 1});

      PortalContainer portalContainer = getContainer();
      chromatticManager = (ChromatticManager)portalContainer.getComponentInstanceOfType(ChromatticManager.class);
      desktopBackgroundService = (DesktopBackgroundService)portalContainer.getComponentInstanceOfType(DesktopBackgroundService.class);
      begin();
   }

   @Override
   protected void tearDown() throws Exception
   {
      chromatticManager.getSynchronization().setSaveOnClose(false);
      end();
   }

   public void testGetUserDesktopBackgrounds()
   {
      List<DesktopBackground> bgs = desktopBackgroundService.getUserDesktopBackgrounds(userName);
      assertEquals(8, bgs.size());
   }

   public void testUploadBackgroundImage() throws Exception
   {
      //Test normal flow
      assertTrue(desktopBackgroundService.uploadBackgroundImage(userName, imageName, mimeType, encoding, imgStream));
      DesktopBackground background = desktopBackgroundService.getUserDesktopBackground(userName, imageName);
      assertNotNull(background);
      assertEquals(imageName, background.getImageLabel());
   }

   public void testLimitQuantity() throws Exception
   {
      //Test quantity limit (9 images, config in : webos-desktop-service-configuration.xml)
      List<DesktopBackground> bgs = desktopBackgroundService.getUserDesktopBackgrounds(userName);
      assertEquals(8, bgs.size());
      try
      {
         for (int i = 0; i < 2; i++)
         {
            desktopBackgroundService.uploadBackgroundImage(userName, imageName + i, mimeType, encoding, imgStream);
         }
         fail("Should throw ImageQuantityException here");
      }
      catch (ImageQuantityException ex)
      {
         assertEquals(9, ex.getQuantity());
      }
   }

   public void testLimitImageSize() throws Exception
   {
      //Test size limit (2 MB)
      imgStream = new ByteArrayInputStream(new byte[1024 *1024*2 + 1]);
      try
      {
         desktopBackgroundService.uploadBackgroundImage(userName, imageName, mimeType, encoding, imgStream);
         fail("Should throw ImageSizeException here");
      }
      catch (ImageSizeException ex)
      {
         assertEquals(imageName, ex.getImageName());
         assertEquals(2, ex.getSizeLimit());
      }
   }

   public void testGetUserDesktopBackground() throws Exception
   {
      desktopBackgroundService.uploadBackgroundImage(userName, imageName, mimeType, encoding, imgStream);

      //Normal flow
      DesktopBackground background = desktopBackgroundService.getUserDesktopBackground(userName, imageName);
      assertNotNull(background);
      assertEquals(imageName, background.getImageLabel());

      //get background that doesn't exists
      background = desktopBackgroundService.getUserDesktopBackground(userName, imageName + new Date().getTime());
      assertNull(background);

      //get background image with null imageName
      background = desktopBackgroundService.getUserDesktopBackground(userName, null);
      assertNull(background);
   }

   public void testCurrentDesktopBackground() throws Exception
   {
      uploadImage();

      desktopBackgroundService.setSelectedBackgroundImage(userName, imageName);
      DesktopBackground currBackground = desktopBackgroundService.getCurrentDesktopBackground(userName);
      assertNotNull(currBackground);
      assertEquals(imageName, currBackground.getImageLabel());
   }

   public void testRemoveBackgroundImage() throws Exception
   {
      uploadImage();

      //Normal flow
      assertTrue(desktopBackgroundService.removeBackgroundImage(userName, imageName));
      DesktopBackground background = desktopBackgroundService.getUserDesktopBackground(userName, imageName);
      assertNull(background);

      //Always return false if remove image with null userName
      assertFalse(desktopBackgroundService.removeBackgroundImage(null, null));
      assertFalse(desktopBackgroundService.removeBackgroundImage(null, imageName));

      //Remove image that doesn't exists
      try
      {
         desktopBackgroundService.removeBackgroundImage(userName, imageName + new Date().getTime());
         fail("Should throw IllegaStateException here");
      }
      catch (IllegalStateException ex)
      {
      }

      //Remove curent selected background image
      uploadImage();
      desktopBackgroundService.setSelectedBackgroundImage(userName, imageName);
      DesktopBackground currBackground = desktopBackgroundService.getCurrentDesktopBackground(userName);
      assertEquals(imageName, currBackground.getImageLabel());

      desktopBackgroundService.removeBackgroundImage(userName, imageName);
      currBackground = desktopBackgroundService.getCurrentDesktopBackground(userName);
      assertNull(currBackground);
   }

   public void testRemoveUserBackground()
   {
      //Test remove PersonalBackgroundSpace

      //Create default personal desktop backgrounds
      desktopBackgroundService.getUserDesktopBackgrounds(userName);

      Chromattic chromattic = chromatticManager.getLifeCycle("webos").getChromattic();
      PersonalBackgroundSpace space = chromattic.openSession().findByPath(PersonalBackgroundSpace.class,
         "/webos:desktopBackgroundRegistry/webos:" + userName, true);
      assertNotNull(space);

      desktopBackgroundService.removeUserBackground(userName);

      space = chromattic.openSession().findByPath(PersonalBackgroundSpace.class,
         "/webos:desktopBackgroundRegistry/webos:" + userName, true);
      assertNull(space);
   }

   private void uploadImage() throws Exception
   {
      assertTrue(desktopBackgroundService.uploadBackgroundImage(userName, imageName, mimeType, encoding, imgStream));
      DesktopBackground background = desktopBackgroundService.getUserDesktopBackground(userName, imageName);
      assertNotNull(background);
   }
}