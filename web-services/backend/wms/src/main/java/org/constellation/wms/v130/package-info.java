/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
@javax.xml.bind.annotation.XmlSchema(namespace = "http://www.opengis.net/wms", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
xmlns = { @javax.xml.bind.annotation.XmlNs(prefix = "wms", namespaceURI= "http://www.opengis.net/wms"),
          @javax.xml.bind.annotation.XmlNs(prefix = "sld", namespaceURI= "http://www.opengis.net/sld"),
          @javax.xml.bind.annotation.XmlNs(prefix = "se", namespaceURI= "http://www.opengis.net/se")})
package org.constellation.wms.v130;
