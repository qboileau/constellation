//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.09.13 at 04:28:10 PM CEST 
//


package net.seagis.ogc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExistenceOperatorsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExistenceOperatorsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element name="ExistenceOperator" type="{http://www.opengis.net/ogc}ExistenceOperatorType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExistenceOperatorsType", propOrder = {
    "existenceOperator"
})
public class ExistenceOperatorsType {

    @XmlElement(name = "ExistenceOperator", required = true)
    protected List<ExistenceOperatorType> existenceOperator;

    /**
     * Gets the value of the existenceOperator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the existenceOperator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExistenceOperator().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExistenceOperatorType }
     * 
     * 
     */
    public List<ExistenceOperatorType> getExistenceOperator() {
        if (existenceOperator == null) {
            existenceOperator = new ArrayList<ExistenceOperatorType>();
        }
        return this.existenceOperator;
    }

}
