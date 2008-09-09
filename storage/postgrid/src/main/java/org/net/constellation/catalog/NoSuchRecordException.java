/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.catalog;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.constellation.resources.i18n.ResourceKeys;
import org.constellation.resources.i18n.Resources;


/**
 * Throws when a record was not found for the specified key.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NoSuchRecordException extends CatalogException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3105861955682823122L;

    /**
     * Creates an exception with the given message.
     */
    public NoSuchRecordException(final String message) {
        super (message);
    }

    /**
     * Creates an exception from the specified result set. The table and column names are
     * obtained from the {@code results} argument if non-null. <strong>Note that the result
     * set will be closed</strong>, because this exception is always thrown when an error
     * occured while reading this result set.
     *
     * @param table   The table that produced the result set, or {@code null} if unknown.
     * @param results The result set used in order to look for a record.
     * @param column  The column index of the primary key (first column index is 1).
     * @param key     The key value for the record that was not found.
     * @throws SQLException if the metadata can't be read from the result set.
     */
    public NoSuchRecordException(final Table table, final ResultSet results, final int column, final String key)
            throws SQLException
    {
        setMetadata(table, results, column, key);
    }

    /**
     * Returns a localized message created from the information provided at construction time.
     */
    @Override
    public String getLocalizedMessage() {
        // Do not invoke super.getLocalizedMessage() because
        // the super-class implementation never returns null.
        String message = getMessage();
        if (message == null) {
            message = Resources.format(ResourceKeys.ERROR_KEY_NOT_FOUND_$2, getTableName(), getPrimaryKey());
        }
        return message;
    }
}
