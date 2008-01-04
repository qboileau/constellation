//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.12.11 at 08:02:10 PM CET 
//


package net.seagis.gml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Description of a spatial and/or temporal reference system used by a dataset.
 * 
 * <p>Java class for AbstractReferenceSystemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractReferenceSystemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractReferenceSystemBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}srsID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}remarks" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}validArea" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}scope" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractReferenceSystemType", propOrder = {
    "srsID",
    "remarks",
    "validArea",
    "scope"
})
// GeographicCRSType.class,VerticalCRSType.class,GeocentricCRSType.class,EngineeringCRSType.class, TemporalCRSType.class, AbstractGeneralDerivedCRSType.class, CompoundCRSType.class
@XmlSeeAlso({
    ImageCRSType.class
})
public abstract class AbstractReferenceSystemType extends AbstractReferenceSystemBaseType {

    private List<IdentifierType> srsID = new ArrayList<IdentifierType>();
    private StringOrRefType remarks;
    private ExtentType validArea;
    private String scope;

    /**
     * Empty constructor used by JAXB
     */
    AbstractReferenceSystemType() {
    }
    
    /**
     * build an abstract reference system.
     */
    public AbstractReferenceSystemType(List<IdentifierType> srsID, StringOrRefType remarks,
            ExtentType validArea, String scope) {
        
        this.remarks   = remarks;
        this.scope     = scope;
        this.validArea = validArea;
        this.srsID     = srsID;
    }
    
    /**
     * Set of alterative identifications of this reference system. The first srsID, if any, is normally the primary identification code, and any others are aliases.Gets the value of the srsID property.
     */
    public List<IdentifierType> getSrsID() {
        return Collections.unmodifiableList(srsID);
    }

    /**
     * Comments on or information about this reference system, including source information.
     */
    @Override
    public String getRemarks() {
        return super.getRemarks();
    }

    /**
     * Gets the value of the validArea property.
     */
    public ExtentType getValidArea() {
        return validArea;
    }

    /**
     * Gets the value of the scope property.
     */
    public String getScope() {
        return scope;
    }
}
