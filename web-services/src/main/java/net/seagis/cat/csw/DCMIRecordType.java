/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.cat.csw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import net.seagis.dublincore.elements.SimpleLiteral;


/**
 * 
 * This type encapsulates all of the standard DCMI metadata terms,
 * including the Dublin Core refinements; these terms may be mapped
 * to the profile-specific information model.
 *          
 * 
 * <p>Java class for DCMIRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DCMIRecordType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}AbstractRecordType">
 *       &lt;sequence>
 *         &lt;group ref="{http://purl.org/dc/terms/}DCMI-terms"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DCMIRecordType", propOrder = {
    "dcElement"
})
@XmlSeeAlso({
    RecordType.class
})
public class DCMIRecordType extends AbstractRecordType {

    @XmlElementRef(name = "DC-element", namespace = "http://purl.org/dc/elements/1.1/", type = JAXBElement.class)
    private List<JAXBElement<SimpleLiteral>> dcElement;

    /**
     * An empty constructor used by JAXB
     */
    DCMIRecordType() {
        this.dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
    }
        
    
    public DCMIRecordType(SimpleLiteral identifier, SimpleLiteral title, SimpleLiteral type, 
            List<SimpleLiteral> subjects, SimpleLiteral format, SimpleLiteral modified, SimpleLiteral _abstract,
            SimpleLiteral creator, SimpleLiteral distributor, SimpleLiteral language) {
        
        this.dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
        this.dcElement.add(dublinFactory.createIdentifier(identifier));
        
        this.dcElement.add(dublinFactory.createTitle(title));
              
        this.dcElement.add(dublinFactory.createType(type));
        
        for (SimpleLiteral subject: subjects) {
            this.dcElement.add(dublinFactory.createSubject(subject));
        }
        
        this.dcElement.add(dublinFactory.createFormat(format));
        
        this.dcElement.add(dublinTermFactory.createModified(modified));
        
        this.dcElement.add(dublinTermFactory.createAbstract(_abstract));
        
        this.dcElement.add(dublinFactory.createCreator(creator));
        
        this.dcElement.add(dublinFactory.createPublisher(distributor));
        
        this.dcElement.add(dublinFactory.createLanguage(language));
        
    }
    
    /**
     * Gets the value of the dcElement property.
     * (unModifiable)
     */
    public List<JAXBElement<SimpleLiteral>> getDCElement() {
        if (dcElement == null) {
            dcElement = new ArrayList<JAXBElement<SimpleLiteral>>();
        }
        return Collections.unmodifiableList(dcElement);
    }
    
    public void setIdentifier(SimpleLiteral identifier) {
        this.dcElement.add(dublinFactory.createIdentifier(identifier));
    }
    
    public String getIdentifier() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("identifier")) {
                if (jb.getValue() != null && jb.getValue().getContent()!= null 
                    && jb.getValue().getContent().size() != 0) {
                    return jb.getValue().getContent().get(0);
                } else {
                    return null;
                }
            }
            
        }
        return null;
    }
    
    public void setTitle(SimpleLiteral title) {
        this.dcElement.add(dublinFactory.createTitle(title));
    }
    
    public String getTitle() {
        for (JAXBElement<SimpleLiteral> jb: dcElement) {
            if (jb.getName().getLocalPart().equals("title")) {
                if (jb.getValue() != null && jb.getValue().getContent()!= null 
                    && jb.getValue().getContent().size() != 0) {
                    return jb.getValue().getContent().get(0);
                } else {
                    return null;
                }
            }
            
        }
        return null;
    }
    
    public void setType(SimpleLiteral type) {
        this.dcElement.add(dublinFactory.createType(type));
    }
    
    public void setSubject(SimpleLiteral subject) {
        this.dcElement.add(dublinFactory.createSubject(subject));
    }
    
    public void setFormat(SimpleLiteral format) {
        this.dcElement.add(dublinFactory.createFormat(format));
    }
    
    public void setModified(SimpleLiteral modified) {
        this.dcElement.add(dublinTermFactory.createModified(modified));
    }
    
    public void setAbstract(SimpleLiteral _abstract) {
        this.dcElement.add(dublinTermFactory.createAbstract(_abstract));
    }
    
    public void setCreator(SimpleLiteral creator) {
        this.dcElement.add(dublinFactory.createCreator(creator));
    }
    
    public void setDistributor(SimpleLiteral distributor) {
        this.dcElement.add(dublinFactory.createPublisher(distributor));
    }
    
    public void setLanguage(SimpleLiteral language) {
        this.dcElement.add(dublinFactory.createLanguage(language));
    }
    
    public void setRelation(SimpleLiteral relation) {
        this.dcElement.add(dublinFactory.createRelation(relation));
    }
    
    public void setSource(SimpleLiteral source) {
        this.dcElement.add(dublinFactory.createSource(source));
    }
    
    public void setCoverage(SimpleLiteral coverage) {
        this.dcElement.add(dublinFactory.createCoverage(coverage));
    }
    
    public void setDate(SimpleLiteral date) {
        this.dcElement.add(dublinFactory.createDate(date));
    }
    
    public void setRights(SimpleLiteral rights) {
        this.dcElement.add(dublinFactory.createRights(rights));
    }
    
    public void setSpatial(SimpleLiteral spatial) {
        this.dcElement.add(dublinTermFactory.createSpatial(spatial));
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (dcElement != null) {
            for (JAXBElement<SimpleLiteral> jb: dcElement) {
                s.append("name=").append(jb.getName()).append(" value=").append(jb.getValue().toString()).append('\n');
            }
        }
        return s.toString();
    }
}
