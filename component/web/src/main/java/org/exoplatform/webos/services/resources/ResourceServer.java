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
package org.exoplatform.webos.services.resources;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.webos.services.desktop.DesktopBackgroundService;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ResourceServer implements org.exoplatform.web.filter.Filter
{

   public void init(FilterConfig filterConfig) throws ServletException
   {
   }

   public void destroy()
   {
   }

   // Three parts
   // site type (for now "user")
   // site name
   // image name
   public static final Pattern PATTERN = Pattern.compile("/webos/([^/]+)/([^/]+)/([^/]+)$");

   public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
   {
      HttpServletRequest hreq = (HttpServletRequest)req;
      HttpServletResponse hresp = (HttpServletResponse)resp;
      if (!hreq.getMethod().equals("GET"))
      {
         hresp.sendError(405, "HTTP method " + hreq.getMethod() + " not supported");
      }
      else
      {
         //
         String path = hreq.getServletPath();

         //
         Matcher matcher = PATTERN.matcher(path);

         //
         if (matcher.matches())
         {

            String siteType = matcher.group(1);
            String siteName = matcher.group(2).replace("_", "/");
            String image = matcher.group(3);

            //
            PortalContainer container = PortalContainer.getInstance();

            //
            DesktopBackgroundService service = (DesktopBackgroundService)container.getComponentInstanceOfType(DesktopBackgroundService.class);

            //
            RequestLifeCycle.begin(container);
            try
            {
               service.renderImage(hreq, hresp, new PortalKey(siteType, siteName), image);
            }
            finally
            {
               RequestLifeCycle.end();
            }
         }
         else
         {
            hresp.sendError(404, "Not found " + path);
         }
      }
   }
}
