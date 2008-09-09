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
package org.constellation.observation;

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;

/**
 * The query to execute for a {@link SurveyProcedureTable}
 *
 * @author Guilhem Legal
 */
public class SurveyProcedureQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, operator, elevationDatum, elevationMethod, elevationAccuracy,
            geodeticDatum, positionMethod, positionAccuracy, projection, surveyTime;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;
      
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SurveyProcedureQuery(final Database database) {
        super(database, "SurveyProcedure");
        final QueryType[] usage = {SELECT, LIST};
        name              = addColumn   ("name",              usage);
        operator          = addColumn   ("operator",          usage);
        elevationDatum    = addColumn   ("elevationDatum",    usage);
        elevationMethod   = addColumn   ("elevationMethod",   usage);
        elevationAccuracy = addColumn   ("elevationAccuracy", usage);
        geodeticDatum     = addColumn   ("geodeticDatum",     usage);
        positionMethod    = addColumn   ("positionMethod",    usage);
        positionAccuracy  = addColumn   ("positionAccuracy",  usage);
        projection        = addColumn   ("projection",        usage);
        surveyTime        = addColumn   ("surveyTime",        usage);
        
        byName  = addParameter(name, SELECT);
    }
    
}
