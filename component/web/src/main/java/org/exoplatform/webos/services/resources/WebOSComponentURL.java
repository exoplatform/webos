/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.URLContext;
import org.exoplatform.webui.url.ComponentURL;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 *
 */
public class WebOSComponentURL extends ComponentURL
{

   public static final QualifiedName WINDOW_ID = QualifiedName.create("gtn", "wid");
   
   private static Set<QualifiedName> paramNames = null;
   
   public WebOSComponentURL(URLContext context) throws NullPointerException
   {
      super(context);
   }

   @Override
   public Set<QualifiedName> getParameterNames()
   {      
      PortalRequestContext context = Util.getPortalRequestContext();      
      if (context != null && SiteType.USER.equals(context.getSiteType())) 
      {
         if (paramNames == null)
         {
            Set<QualifiedName> tmp = new HashSet<QualifiedName>(super.getParameterNames());
            tmp.add(WINDOW_ID);
            paramNames = Collections.unmodifiableSet(tmp);
         }
         return paramNames;         
      }
      else
      {
         return super.getParameterNames();
      }
   }

   @Override
   public String getParameterValue(QualifiedName parameterName)
   {
      if (WINDOW_ID.equals(parameterName))
      {
         return "";
      }
      else
      {
         return super.getParameterValue(parameterName);
      }
   }            
}
