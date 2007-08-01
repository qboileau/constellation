/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.coverage.io;

import java.util.Date;
import java.util.HashMap;

import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;


/**
 * Description of PostGrid DataBase format.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class PostGridFormat extends AbstractGridFormat {    
    /**
     *
     */
    private static final DefaultParameterDescriptor TIME = new DefaultParameterDescriptor(
            "TIME", Date.class, null, null);

    /**
     *
     */
    private static final DefaultParameterDescriptor ELEVATION = new DefaultParameterDescriptor(
            "ELEVATION", Integer.TYPE, null, null);

    /**
     * Creates a new instance of PostGridFormat.
     * Contains the main information about the PostGrid DataBase format.
     */
    public PostGridFormat() {        
        writeParameters = null;
        mInfo = new HashMap();
        mInfo.put("name", "PostGrid");
        mInfo.put("description", "PostGrid"); 
        mInfo.put("vendor", "Geomatys");
        mInfo.put("docURL", "http://www.geomatys.fr/");
        mInfo.put("version", "1.0");
        readParameters = new ParameterGroup(
                new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D, TIME, ELEVATION }));
    }

    /**
     * Gets a reader for the raster chosen in the DataBase.
     * 
     * @param input The input object.
     * @return A reader on the grid coverage chosen.
     */
    public GridCoverageReader getReader(final Object input) {
        return getReader(input, null);
    }

    /**
     * Gets a reader for the raster, specifying some {@code hints}.
     * 
     * @param input The input object.
     * @param hints Hints value for the data.
     * @return A reader on the grid coverage chosen.
     */
    public GridCoverageReader getReader(final Object input, final Hints hints) {
        return new PostGridReader(this, input, null);
    }

    /**
     * Gets a writer for the raster.
     * Not used in our implementation.
     *
     * @param object The source in which we will write.
     */
    public GridCoverageWriter getWriter(Object object) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Specifies if the source is a valid raster, and by the way is available.
     *
     * @param object The source to test.
     *
     * @todo Not yet implemented (previous implementation was useless).
     */
    public boolean accepts(Object object) {
        return true;
    }

    /**
     * Not used in our implementation.
     */
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        return null;
    }
}
