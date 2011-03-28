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
package org.exoplatform.webos.services.desktop.impl;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.chromattic.ext.ntdef.NTFile;

@MixinType(name = "webos:page")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("webos")
public abstract class DesktopPageMetadata
{

   @ManyToOne(type = RelationshipType.PATH)
   @MappedBy("webos:background")
   public abstract NTFile getBackgroundImage();

   public abstract void setBackgroundImage(NTFile backgroundImage);

}
