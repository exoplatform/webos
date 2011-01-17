/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.webos.services.desktop.exception;

public class ImageSizeException extends Exception
{
   private int sizeLimit;
   private String imageName;

   public ImageSizeException(int sizeLimit, String imageName)
   {
      this(sizeLimit, imageName, null);
   }

   public ImageSizeException(int sizeLimit, String imageName, String message)
   {
      super(message);
      this.sizeLimit = sizeLimit;
      this.imageName = imageName;
   }

   public String getImageName()
   {
      return imageName;
   }

   public int getSizeLimit()
   {
      return sizeLimit;
   }
}
