/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.metadata;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.constellation.catalog.Entry;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.util.LocalName;
import org.opengis.util.NameSpace;
import org.opengis.util.ScopedName;

/**
 *
 * @author legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericName")
public class GenericNameEntry extends Entry implements GenericName {

    public NameSpace scope() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GenericName getScope() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int depth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<LocalName> getParsedNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LocalName asLocalName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LocalName name() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ScopedName asScopedName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GenericName toFullyQualifiedName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ScopedName push(GenericName arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InternationalString toInternationalString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int compareTo(GenericName o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LocalName head() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LocalName tip() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
