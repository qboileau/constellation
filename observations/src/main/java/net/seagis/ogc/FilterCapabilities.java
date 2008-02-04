//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.09.13 at 04:28:10 PM CEST 
//


package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="Spatial_Capabilities" type="{http://www.opengis.net/ogc}Spatial_CapabilitiesType"/>
 *         &lt;element name="Temporal_Capabilities" type="{http://www.opengis.net/ogc}Temporal_CapabilitiesType"/>
 *         &lt;element name="Existence_Capabilities" type="{http://www.opengis.net/ogc}Existence_CapabilitiesType"/>
 *         &lt;element name="Classification_Capabilities" type="{http://www.opengis.net/ogc}Classification_CapabilitiesType"/>
 *         &lt;element name="Scalar_Capabilities" type="{http://www.opengis.net/ogc}Scalar_CapabilitiesType"/>
 *         &lt;element name="Id_Capabilities" type="{http://www.opengis.net/ogc}Id_CapabilitiesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "spatialCapabilities",
    "temporalCapabilities",
    "existenceCapabilities",
    "classificationCapabilities",
    "scalarCapabilities",
    "idCapabilities"
})
@XmlRootElement(name = "Filter_Capabilities")
public class FilterCapabilities {

    @XmlElement(name = "Spatial_Capabilities", required = true)
    protected SpatialCapabilitiesType spatialCapabilities;
    @XmlElement(name = "Temporal_Capabilities", required = true)
    protected TemporalCapabilitiesType temporalCapabilities;
    @XmlElement(name = "Existence_Capabilities", required = true)
    protected ExistenceCapabilitiesType existenceCapabilities;
    @XmlElement(name = "Classification_Capabilities", required = true)
    protected ClassificationCapabilitiesType classificationCapabilities;
    @XmlElement(name = "Scalar_Capabilities", required = true)
    protected ScalarCapabilitiesType scalarCapabilities;
    @XmlElement(name = "Id_Capabilities", required = true)
    protected IdCapabilitiesType idCapabilities;

    /**
     * Gets the value of the spatialCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link SpatialCapabilitiesType }
     *     
     */
    public SpatialCapabilitiesType getSpatialCapabilities() {
        return spatialCapabilities;
    }

    /**
     * Sets the value of the spatialCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link SpatialCapabilitiesType }
     *     
     */
    public void setSpatialCapabilities(SpatialCapabilitiesType value) {
        this.spatialCapabilities = value;
    }

    /**
     * Gets the value of the temporalCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link TemporalCapabilitiesType }
     *     
     */
    public TemporalCapabilitiesType getTemporalCapabilities() {
        return temporalCapabilities;
    }

    /**
     * Sets the value of the temporalCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link TemporalCapabilitiesType }
     *     
     */
    public void setTemporalCapabilities(TemporalCapabilitiesType value) {
        this.temporalCapabilities = value;
    }

    /**
     * Gets the value of the existenceCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link ExistenceCapabilitiesType }
     *     
     */
    public ExistenceCapabilitiesType getExistenceCapabilities() {
        return existenceCapabilities;
    }

    /**
     * Sets the value of the existenceCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExistenceCapabilitiesType }
     *     
     */
    public void setExistenceCapabilities(ExistenceCapabilitiesType value) {
        this.existenceCapabilities = value;
    }

    /**
     * Gets the value of the classificationCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link ClassificationCapabilitiesType }
     *     
     */
    public ClassificationCapabilitiesType getClassificationCapabilities() {
        return classificationCapabilities;
    }

    /**
     * Sets the value of the classificationCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassificationCapabilitiesType }
     *     
     */
    public void setClassificationCapabilities(ClassificationCapabilitiesType value) {
        this.classificationCapabilities = value;
    }

    /**
     * Gets the value of the scalarCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link ScalarCapabilitiesType }
     *     
     */
    public ScalarCapabilitiesType getScalarCapabilities() {
        return scalarCapabilities;
    }

    /**
     * Sets the value of the scalarCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScalarCapabilitiesType }
     *     
     */
    public void setScalarCapabilities(ScalarCapabilitiesType value) {
        this.scalarCapabilities = value;
    }

    /**
     * Gets the value of the idCapabilities property.
     * 
     * @return
     *     possible object is
     *     {@link IdCapabilitiesType }
     *     
     */
    public IdCapabilitiesType getIdCapabilities() {
        return idCapabilities;
    }

    /**
     * Sets the value of the idCapabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdCapabilitiesType }
     *     
     */
    public void setIdCapabilities(IdCapabilitiesType value) {
        this.idCapabilities = value;
    }

}
