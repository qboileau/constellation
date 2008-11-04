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
package org.constellation.coverage.web;

import org.constellation.catalog.CatalogException;
import org.opengis.util.CodeList;


/**
 * Reports a failure in {@link WebService}.
 *
 * @version $Id$
 * @author Guihlem Legal
 * @author Cédric Briançon
 */
public class WebServiceException extends CatalogException {
    /**
     * The exceptioni code.
     */
    private final CodeList exceptionCode;

    /**
     * The service version.
     */
    private final ServiceVersion version;

    /**
     * The reason of the exception.
     */
    private final String locator;

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     * @param exceptionCode The exception code.
     * @param version The service version.
     */
    public WebServiceException(final String message, final CodeList exceptionCode,
                               final ServiceVersion version)
    {
        super(message);
        this.exceptionCode = exceptionCode;
        this.version = version;
        this.locator = null;
    }

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     * @param exceptionCode The exception code.
     * @param version The service version.
     */
    public WebServiceException(final String message, final CodeList exceptionCode,
                               final ServiceVersion version, final String locator)
    {
        super(message);
        this.exceptionCode = exceptionCode;
        this.version = version;
        this.locator = locator;
    }

    /**
     * Creates an exception with the specified details message and cause.
     *
     * @param message The detail message.
     * @param exceptionCode The exception code.
     * @param version The service version.
     */
    public WebServiceException(final Exception cause, final CodeList exceptionCode,
            final ServiceVersion version)
    {
        super(cause);
        this.exceptionCode = exceptionCode;
        this.version = version;
        locator = null;
    }

    /**
     * Creates an exception with the specified details message and cause.
     *
     * @param message The detail message.
     * @param cause The cause for this exception.
     * @param exceptionCode The exception code.
     * @param version The service version.
     */
    public WebServiceException(final String message, final Exception cause,
                               final CodeList exceptionCode,  final ServiceVersion version)
    {
        super(message, cause);
        this.exceptionCode = exceptionCode;
        this.version = version;
        this.locator = null;
    }

    public CodeList getExceptionCode() {
        return exceptionCode;
    }

    public ServiceVersion getVersion() {
        return version;
    }

    public String getLocator() {
        return locator;
    }
}
