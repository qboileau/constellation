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
package org.constellation.sld.v110;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.se.OnlineResourceType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/sld}Service"/>
 *         &lt;element ref="{http://www.opengis.net/se}OnlineResource"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "service",
    "onlineResource"
})
@XmlRootElement(name = "RemoteOWS")
public class RemoteOWS {

    @XmlElement(name = "Service", required = true)
    private String service;
    @XmlElement(name = "OnlineResource", namespace = "http://www.opengis.net/se", required = true)
    private OnlineResourceType onlineResource;

    /**
     * Empty Constructor used by JAXB.
     */
    RemoteOWS() {
        
    }
    
    /**
     * Build a new remote OGC Web Service.
     */
    public RemoteOWS(String service, OnlineResourceType onlineResource) {
        this.service = service;
        this.onlineResource = onlineResource;
    }
    
    /**
     * Gets the value of the service property.
     * 
     */
    public String getService() {
        return service;
    }

    /**
     * Gets the value of the onlineResource property.
     */
    public OnlineResourceType getOnlineResource() {
        return onlineResource;
    }
}
