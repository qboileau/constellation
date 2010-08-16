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
package org.constellation.ws;

import java.util.ArrayList;
import java.util.List;
import org.opengis.util.CodeList;


/**
 * Describes the type of an exception.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 *
 * @todo Rename as {@code ExceptionCode} and move to {@link org.opengis.webservice}.
 */
public final class ExceptionCode extends CodeList<ExceptionCode> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7234996844680200818L;

    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List<ExceptionCode> VALUES = new ArrayList<ExceptionCode>(16);

    /**
     * Invalid format.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_FORMAT = new ExceptionCode("INVALID_FORMAT");
    
    /**
     * Invalid request.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_REQUEST = new ExceptionCode("INVALID_REQUEST");

    /**
     * Current update sequence.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode CURRENT_UPDATE_SEQUENCE = new ExceptionCode("CURRENT_UPDATE_SEQUENCE");

    /**
     * Invalid update sequence.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_UPDATE_SEQUENCE = new ExceptionCode("INVALID_UPDATE_SEQUENCE");

    /**
     * Missing parameter value.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode MISSING_PARAMETER_VALUE = new ExceptionCode("MISSING_PARAMETER_VALUE");

    /**
     * Invalid parameter value.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_PARAMETER_VALUE = new ExceptionCode("INVALID_PARAMETER_VALUE");

    /**
     * Operation not supported.
     * Used by WMS.
     */
    public static final ExceptionCode OPERATION_NOT_SUPPORTED = new ExceptionCode("OPERATION_NOT_SUPPORTED");

    /**
     * Version negotiation failed.
     * Used by WMS.
     */
    public static final ExceptionCode VERSION_NEGOTIATION_FAILED = new ExceptionCode("VERSION_NEGOTIATION_FAILED");

    /**
     * No applicable code.
     * Used by WMS.
     */
    public static final ExceptionCode NO_APPLICABLE_CODE = new ExceptionCode("NO_APPLICABLE_CODE");

    /**
     * Invalid CRS.
     * Used by WMS.
     */
    public static final ExceptionCode INVALID_CRS = new ExceptionCode("INVALID_CRS");

    /**
     * Invalid CRS.
     * Used by WMS 1.1.1
     */
    public static final ExceptionCode INVALID_SRS = new ExceptionCode("InvalidSRS");

    /**
     * Layer not defined.
     * Used by WMS.
     */
    public static final ExceptionCode LAYER_NOT_DEFINED = new ExceptionCode("LAYER_NOT_DEFINED");

    /**
     * Style not defined.
     * Used by WMS.
     */
    public static final ExceptionCode STYLE_NOT_DEFINED = new ExceptionCode("STYLE_NOT_DEFINED");

    /**
     * Layer not queryable.
     * Used by WMS.
     */
    public static final ExceptionCode LAYER_NOT_QUERYABLE = new ExceptionCode("LAYER_NOT_QUERYABLE");

    /**
     * Invalid point.
     * Used by WMS.
     */
    public static final ExceptionCode INVALID_POINT = new ExceptionCode("INVALID_POINT");

    /**
     * Missing dimension value.
     * Used by WMS.
     */
    public static final ExceptionCode MISSING_DIMENSION_VALUE = new ExceptionCode("MISSING_DIMENSION_VALUE");

    /**
     * Invalid dimension value.
     * Used by WMS.
     */
    public static final ExceptionCode INVALID_DIMENSION_VALUE = new ExceptionCode("INVALID_DIMENSION_VALUE");

    /**
     * Constructs an enum with the given name. The new enum is
     * automatically added to the list returned by {@link #values}.
     *
     * @param name The enum name. This name must not be in use by an other enum of this type.
     */
    private ExceptionCode(final String name) {
        super(name, VALUES);
    }

    /**
     * Returns the list of exception codes.
     *
     * @return The list of codes declared in the current JVM.
     */
    public static ExceptionCode[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(new ExceptionCode[VALUES.size()]);
        }
    }

    /**
     * Returns the list of exception codes.
     */
    @Override
    public ExceptionCode[] family() {
        return values();
    }

    /**
     * Returns the exception code that matches the given string, or returns a
     * new one if none match it.
     *
     * @param code The name of the code to fetch or to create.
     * @return A code matching the given name.
     */
    public static ExceptionCode valueOf(String code) {
        return valueOf(ExceptionCode.class, code);
    }
}
