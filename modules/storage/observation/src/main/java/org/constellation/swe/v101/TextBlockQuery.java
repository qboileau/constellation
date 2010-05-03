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
package org.constellation.swe.v101;

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import static org.geotoolkit.internal.sql.table.QueryType.*;

/**
 * The query to execute for a {@link TextBlockTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class TextBlockQuery extends Query{
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, tokenSeparator, blockSeparator, decimalSeparator;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byId, byTokenSeparator, byBlockSeparator, byDecimalSeparator;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public TextBlockQuery(final Database database) {
        super(database, "text_block_encodings", "observation");
        final QueryType[] si   = {SELECT, INSERT};
        final QueryType[] sief = {SELECT, INSERT, EXISTS,  LIST};
        id               = addMandatoryColumn("id_encoding",       sief);
        tokenSeparator   = addMandatoryColumn("token_separator",   si);
        blockSeparator   = addMandatoryColumn("block_separator",   si);
        decimalSeparator = addMandatoryColumn("decimal_separator", si);

        byId               = addParameter(id, SELECT, EXISTS);
        byBlockSeparator   = addParameter(blockSeparator,   LIST);
        byDecimalSeparator = addParameter(decimalSeparator, LIST);
        byTokenSeparator   = addParameter(tokenSeparator,   LIST);
    }
    
}
