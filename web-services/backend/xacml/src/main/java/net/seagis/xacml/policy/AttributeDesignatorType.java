/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008 Geomatys
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

package net.seagis.xacml.policy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for AttributeDesignatorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeDesignatorType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}ExpressionType">
 *       &lt;attribute name="AttributeId" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="DataType" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Issuer" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="MustBePresent" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttributeDesignatorType")
public class AttributeDesignatorType extends ExpressionType {

   @XmlAttribute(name = "AttributeId", required = true)
   @XmlSchemaType(name = "anyURI")
   private String attributeId;

   @XmlAttribute(name = "DataType", required = true)
   @XmlSchemaType(name = "anyURI")
   private String dataType;

   @XmlAttribute(name = "Issuer")
   private String issuer;

   @XmlAttribute(name = "MustBePresent")
   private Boolean mustBePresent;

   /**
    * Gets the value of the attributeId property.
    * 
    */
   public String getAttributeId() {
      return attributeId;
   }

   /**
    * Sets the value of the attributeId property.
    */
   public void setAttributeId(String value) {
      this.attributeId = value;
   }

   /**
    * Gets the value of the dataType property.
    */
   public String getDataType() {
      return dataType;
   }

   /**
    * Sets the value of the dataType property.
    */
   public void setDataType(String value) {
      this.dataType = value;
   }

   /**
    * Gets the value of the issuer property.
    */
   public String getIssuer() {
      return issuer;
   }

   /**
    * Sets the value of the issuer property.
    */
   public void setIssuer(String value) {
      this.issuer = value;
   }

   /**
    * Gets the value of the mustBePresent property.
    */
   public boolean isMustBePresent() {
      if (mustBePresent == null) {
         return false;
      } else {
         return mustBePresent;
      }
   }

   /**
    * Sets the value of the mustBePresent property.
    */
   public void setMustBePresent(Boolean value) {
      this.mustBePresent = value;
   }

}
