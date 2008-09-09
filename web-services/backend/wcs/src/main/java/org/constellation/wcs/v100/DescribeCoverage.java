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
package org.constellation.wcs.v100;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.wcs.AbstractDescribeCoverage;


/**
 * <p>An xml binding classe for a DescribeCoverage request.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Coverage" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="WCS" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.0.0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"coverage"})
@XmlRootElement(name = "DescribeCoverage")
public class DescribeCoverage extends AbstractDescribeCoverage {

    @XmlAttribute(required = true)
    private String service;
    @XmlAttribute(required = true)
    private String version;
    
    /*
     * WCS 1.0.0
     */
    @XmlElement(name = "Coverage")
    private List<String> coverage;
    
    /**
     * Empty constructor used by JAXB
     */
    DescribeCoverage(){
    }
    
    /**
     * Build a new DescribeCoverage request.
     * 
     * @param version The version of the service.
     * @param listOfCoverage a string containing many coverage name separated by a colon.
     */
    public DescribeCoverage(String listOfCoverage){
        this.service = "WCS";
        this.version = "1.0.0";
        coverage = new ArrayList<String>();
        final StringTokenizer tokens = new StringTokenizer(listOfCoverage, ",;");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            coverage.add(token);
        }
    }
    
    /**
     * Build a new DescribeCoverage request.
     * 
     * @param version The version of the service.
     * @param coverages A list  of coverage name.
     */
    public DescribeCoverage(List<String> coverages){
        this.service = "WCS";
        this.version = "1.0.0";
        this.coverage = coverages;
    }
    
    /**
     * Return the list of requested coverage name.
     * (unmodifiable)
     */
    public List<String> getCoverage() {
        if (coverage == null) {
            coverage = new ArrayList<String>();
        }
        return Collections.unmodifiableList(coverage);
    }
    
    /**
     * return the service type here always WCS.
     */
    public String getService() {
        return "WCS";
    }

    /**
     * Return the version of the service.
     */
    public String getVersion() {
        return version;
    }
}
