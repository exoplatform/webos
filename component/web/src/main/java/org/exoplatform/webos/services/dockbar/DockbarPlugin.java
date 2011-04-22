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
package org.exoplatform.webos.services.dockbar;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.impl.UnmarshallingContext;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin of DockbarService, which enables external configuration of applications appearing on dockbar
 *
 */
public class DockbarPlugin extends BaseComponentPlugin
{
   public static final String CONFIG = "dockbarConfigLocation";
   private List<Application> applications;
   private Log log = ExoLogger.getExoLogger(Dockbar.class.getName());
   private ConfigurationManager configurationManager;

   public DockbarPlugin(ConfigurationManager configurationManager, InitParams initParams) throws Exception
   {
      ValueParam configLocation = initParams.getValueParam(CONFIG);
      if (configLocation == null)
      {
         throw new IllegalStateException("Missing dockbarConfigLocation param");
      }
      this.configurationManager = configurationManager;

      parseDockbarConfig(configLocation.getValue());
   }

   public void setApplications(List<Application> applications)
   {
      this.applications = applications;
   }

   public List<Application> getApplications()
   {
      return applications;
   }

   protected void parseDockbarConfig(String configLocation) throws Exception
   {
      String content = IOUtil.getStreamContentAsString(configurationManager.getInputStream(configLocation));

      ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
      IBindingFactory bfact = BindingDirectory.getFactory(Dockbar.class);
      UnmarshallingContext uctx = (UnmarshallingContext)bfact.createUnmarshallingContext();
      uctx.setDocument(is, null, "UTF-8", false);

      Dockbar dockbar = Dockbar.class.cast(uctx.unmarshalElement());
      if (dockbar.getApplications() == null)
      {
         applications = new ArrayList<Application>();
         log.warn("No application config's found");
         return;
      }
      applications = dockbar.getApplications();
   }
}
