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
package org.constellation.cat.csw.v202;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.constellation.cat.csw.AbstractRecord;


/**
 * <p>Java class for AbstractRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractRecordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractRecordType")
@XmlSeeAlso({
    DCMIRecordType.class,
    SummaryRecordType.class,
    BriefRecordType.class
})
public abstract class AbstractRecordType implements AbstractRecord {
    
    @XmlTransient
    protected static org.constellation.ows.v100.ObjectFactory owsFactory = new org.constellation.ows.v100.ObjectFactory();
    
    @XmlTransient
    protected static org.constellation.dublincore.v2.elements.ObjectFactory dublinFactory = new org.constellation.dublincore.v2.elements.ObjectFactory();
    
    @XmlTransient
    protected static org.constellation.dublincore.v2.terms.ObjectFactory dublinTermFactory = new org.constellation.dublincore.v2.terms.ObjectFactory();

}
