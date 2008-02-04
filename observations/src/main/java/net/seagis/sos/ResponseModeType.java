/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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


package net.seagis.sos;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for responseModeType.
 * 
 */
@XmlType(name = "responseModeType")
@XmlEnum
public enum ResponseModeType {

    @XmlEnumValue("inline")
    INLINE("inline"),
    @XmlEnumValue("attached")
    ATTACHED("attached"),
    @XmlEnumValue("out-of-band")
    OUT_OF_BAND("out-of-band"),
    @XmlEnumValue("resultTemplate")
    RESULT_TEMPLATE("resultTemplate");
    private final String value;

    ResponseModeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResponseModeType fromValue(String v) {
        for (ResponseModeType c: ResponseModeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
