/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import org.exoplatform.test.mocks.servlet.MockServletContext;

/**
 * A custom servlet context, used to load initial images in JUnit test
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 3/23/11
 */
public class ImageLoader extends MockServletContext
{

   public ImageLoader(String name,String contextPath)
   {
      super(name, contextPath);
   }

   public String getMimeType(String path)
   {
      File file = new File(getRealPath(path));
      if (file.exists() || file.isFile()) 
      {
         MimetypesFileTypeMap mapper = new MimetypesFileTypeMap();
         return mapper.getContentType(file);
      }
      return null;      
   }

   @SuppressWarnings("rawtypes")
   public Set getResourcePaths(String s) 
   {
      Set<String> paths = new HashSet<String>();
      String realPath = getRealPath(s);
      
      File file = new File(realPath);
      if (file.exists() && file.isDirectory())
      {
         for (File child : file.listFiles())
         {           
            String path = child.isDirectory() ? child.getName() + "/" : child.getName();  
            paths.add(s + "/" +  path);
         }
      }    
      
      return paths;
   }    
}
