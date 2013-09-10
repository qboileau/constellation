/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.configuration;

import org.geotoolkit.process.ProcessException;

/**
 * {@link ConfigurationException} to be thrown a configuration process has reported
 * an error.
 *
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class ConfigProcessException extends ConfigurationException {

    public ConfigProcessException(final String message, final ProcessException cause) {
        super(message, cause);
    }
}
