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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A custom servlet context, used to load initial images in JUnit test
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 3/23/11
 */
public class ImageLoader implements ServletContext
{

   private final Set<String> pathToImages;

   private final String name;

   private final ClassLoader classLoaderOfRunningUnitTest;

   public ImageLoader(String _name, Set<String> _pathToImages, ClassLoader _classLoaderOfRunningUnitTest)
   {
      this.name = _name;
      this.pathToImages = _pathToImages;
      this.classLoaderOfRunningUnitTest = _classLoaderOfRunningUnitTest;
   }

   public String getContextPath()
   {
      return null;
   }

   public ServletContext getContext(String uripath)
   {
      return null;
   }

   public int getMajorVersion()
   {
      return 0;
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public String getMimeType(String file)
   {
      return null;
   }

   public Set getResourcePaths(String path)
   {
      return this.pathToImages;
   }

   public URL getResource(String path) throws MalformedURLException
   {
      return null;
   }

   public InputStream getResourceAsStream(String path)
   {
      return classLoaderOfRunningUnitTest.getResourceAsStream(path);
   }

   public RequestDispatcher getRequestDispatcher(String path)
   {
      return null;
   }

   public RequestDispatcher getNamedDispatcher(String name)
   {
      return null;
   }

   public Enumeration getServlets()
   {
      return null;
   }

   public Enumeration getServletNames()
   {
      return null;
   }

   public void log(String msg)
   {
   }

   public void log(String message, Throwable throwable)
   {
   }

   public String getRealPath(String path)
   {
      return null;
   }

   public String getServerInfo()
   {
      return null;
   }

   public String getInitParameter(String name)
   {
      return null;
   }

   public Enumeration getInitParameterNames()
   {
      return null;
   }

   public Object getAttribute(String name)
   {
      return null;
   }

   public Enumeration getAttributeNames()
   {
      return null;
   }

   public void setAttribute(String name, Object object)
   {
   }

   public void removeAttribute(String name)
   {
   }

   public String getServletContextName()
   {
      return name;
   }

   public Servlet getServlet(String name) throws ServletException
   {
      return null;
   }

   public void log(Exception exception, String msg)
   {
   }
}
