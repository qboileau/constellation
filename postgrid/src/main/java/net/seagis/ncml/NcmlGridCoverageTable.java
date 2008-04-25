/*
 * (C) 2008, Geomatys
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
package net.seagis.ncml;

import java.io.IOException;
import javax.imageio.ImageReader;
import net.seagis.catalog.Database;
import net.seagis.coverage.catalog.WritableGridCoverageEntry;
import net.seagis.coverage.catalog.WritableGridCoverageTable;


/**
 * Adds to the {@link WritableGridCoverageTable} some specifics parameters designed
 * for handling time specified in an NcML file.
 *
 * @see net.seagis.coverage.catalog.WritableGridCoverageTable
 * @source $URL$
 * @author Cédric Briançon
 */
public class NcmlGridCoverageTable extends WritableGridCoverageTable {
    /**
     * The increment step between two consecutive dates in the list.
     */
    private long increment = 0L;

    /**
     * The starting time for the current element.
     */
    private long startTime = 0L;

    /**
     * The number of points.
     */
    private int npts = 0;

    /**
     * The starting time for the next item in the list.
     */
    private long nextItemStart = 0L;

    /**
     * Constructs a new {@code WritableGridCoverageTable}.
     *
     * @param connection The connection to the database.
     * @see net.seagis.coverage.catalog.WritableGridCoverageTable
     */
    public NcmlGridCoverageTable(final Database database) {
        super(database);
    }

    /**
     * Creates an entry for the {@code GridCoverages} table. It assumes that #setIncrement(Long),
     * #setStartTime(Long), #setNpts(Integer) and #setNextItemStart(Long) have been called previously.
     * Otherwise the default value for these variables is 0.
     *
     * @param reader
     * @param imageIndex
     * @return
     * @throws java.io.IOException
     */
    @Override
    protected WritableGridCoverageEntry createEntry(final ImageReader reader, final int imageIndex)
            throws IOException
    {
        return new NcmlGridCoverageEntry(reader, imageIndex, startTime, increment, npts, nextItemStart);
    }

    /**
     * Returns the increment step of time between two consecutive elements defined in the
     * NcML file.
     */
    public long getIncrement() {
        return increment;
    }

    /**
     * Sets the increment value between two dates in the list.
     */
    public void setIncrement(final long increment) {
        this.increment = increment;
    }

    /**
     * Returns the next item start time.
     */
    public long getNextItemStart() {
        return nextItemStart;
    }

    /**
     * Sets the next item starting time value, if there is several items in the list of dates
     * to add.
     * This will be used as the end time for the current element, instead of the increment parameter.
     */
    public void setNextItemStart(final long nextItemStart) {
        this.nextItemStart = nextItemStart;
    }

    /**
     * Returns the number of points chosen in the NcML file.
     */
    public long getNpts() {
        return npts;
    }

    /**
     * Sets the number of points value.
     */
    public void setNpts(final int npts) {
        this.npts = npts;
    }

    /**
     * Returns the starting time of this element.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the starting time value.
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }
}
