/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
package net.sicade.coverage.model;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;


/**
 * The query to execute for a {@link DescriptorTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class DescriptorQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, symbol, layer, operation, region, band, distribution;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter bySymbol, byIdentifier;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public DescriptorQuery(final Database database) {
        super(database);
        final QueryType[] usage  = {SELECT, SELECT_BY_IDENTIFIER, LIST};
        identifier   = addColumn   ("Descriptors", "identifier",   usage);
        symbol       = addColumn   ("Descriptors", "symbol",       usage);
        layer        = addColumn   ("Descriptors", "layer",        usage);
        operation    = addColumn   ("Descriptors", "operation",    usage);
        region       = addColumn   ("Descriptors", "region",       usage);
        band         = addColumn   ("Descriptors", "band",         usage);
        distribution = addColumn   ("Descriptors", "distribution", usage);
        bySymbol     = addParameter(symbol,     SELECT);
        byIdentifier = addParameter(identifier, SELECT_BY_IDENTIFIER);
        identifier.setOrdering("ASC", LIST);
    }
}
