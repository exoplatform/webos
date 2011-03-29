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

package org.exoplatform.webos.webui.page;

import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageFactory;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * This factory is used to create UIDesktopPage component
 * 
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class UIDesktopPageFactory extends UIPageFactory
{
   public static String DESKTOP_FACTORY_ID = "Desktop";
   
   @Override
   public String getType()
   {
      return DESKTOP_FACTORY_ID;
   }

   @Override
   public UIPage createUIPage(WebuiRequestContext context) throws Exception
   {
      if (context == null)
      {
         context = WebuiRequestContext.getCurrentInstance();
      }
      WebuiApplication app = (WebuiApplication)context.getApplication();
      UIDesktopPage uiPage = app.createUIComponent(UIDesktopPage.class, null, null, context);
      return uiPage;
   }
}