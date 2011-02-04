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

package org.exoplatform.webos.services.dockbar;

import org.exoplatform.portal.application.Preference;
import org.jibx.runtime.IAliasable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;

public class PreferencesHandler implements IMarshaller, IUnmarshaller, IAliasable
{

   private String m_uri;

   private String m_name;

   public PreferencesHandler()
   {
   }

   public PreferencesHandler(String m_uri, int m_index, String m_name)
   {
      this.m_uri = m_uri;
      this.m_name = m_name;
   }

   // IMarshaller implementation

   public boolean isExtension(String s)
   {
      throw new UnsupportedOperationException();
   }

   public void marshal(Object o, IMarshallingContext iMarshallingContext) throws JiBXException
   {
      throw new UnsupportedOperationException();
   }

   // IUnmarshaller implementation

   public boolean isPresent(IUnmarshallingContext ctx) throws JiBXException
   {
      return ctx.isAt(m_uri, m_name);
   }

   public Object unmarshal(Object obj, IUnmarshallingContext ictx) throws JiBXException
   {
      UnmarshallingContext ctx = (UnmarshallingContext)ictx;
      if (!ctx.isAt(m_uri, m_name))
      {
         ctx.throwStartTagNameError(m_uri, m_name);
      }

      if (obj != null)
      {
         throw new AssertionError("That should not happen");
      }

      ctx.parsePastStartTag(m_uri, m_name);

      Preference pref = new Preference();

      pref.setName(ctx.parseElementText(m_uri, "name"));

      if (ctx.isAt(m_uri, "values"))
      {
         ctx.parsePastStartTag(m_uri, "values");
         while (ctx.isAt(m_uri, "value"))
         {
            pref.addValue(ctx.parseElementText(m_uri, "value"));
         }
         ctx.parsePastEndTag(m_uri, "values");
      }
      else
      {
         pref.addValue(ctx.parseElementText(m_uri, "value"));
      }
      nextOptionalBooleanTag(ctx, "read-only", false);

      ctx.parsePastEndTag(m_uri, m_name);

      return pref;
   }

   private boolean nextOptionalBooleanTag(UnmarshallingContext ctx, String tagName, boolean defaultValue)
      throws JiBXException
   {
      Boolean value = defaultValue;
      if (ctx.isAt(m_uri, tagName))
      {
         value = ctx.parseElementBoolean(m_uri, tagName);
      }
      return value;
   }
}
