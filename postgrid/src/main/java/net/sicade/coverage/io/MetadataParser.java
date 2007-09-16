/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
package net.sicade.coverage.io;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Date;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.units.Unit;
import javax.units.NonSI;
import javax.units.Converter;


import org.geotools.image.io.GeographicImageReader;
import org.geotools.image.io.metadata.Axis;
import org.geotools.image.io.metadata.ImageGeometry;
import org.geotools.image.io.metadata.ImageReferencing;
import org.geotools.image.io.metadata.GeographicMetadata;
import org.geotools.image.io.metadata.GeographicMetadataFormat;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.util.MeasurementRange;
import org.geotools.util.NumberRange;

import net.sicade.util.Ranks;
import net.sicade.util.DateRange;


/**
 * Builds objects from coverage metadata.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MetadataParser {
    /**
     * Small number for rounding errors.
     */
    private static final double EPS = 1E-5;

    /**
     * The metadata to parse.
     */
    private final GeographicMetadata metadata;

    /**
     * Gets the geographic metadata from the specified reader.
     *
     * @param  reader     The reader where to fetch metadata from.
     * @param  imageIndex The index of the image to be read.
     * @throws IOException if an error occured during metadata reading, or if no metadata
     *         were found.
     */
    public MetadataParser(final ImageReader reader, final int imageIndex) throws IOException {
        if (reader instanceof GeographicImageReader) {
            metadata = ((GeographicImageReader) reader).getGeographicMetadata(imageIndex);
        } else {
            final IIOMetadata candidate = reader.getImageMetadata(imageIndex);
            if (candidate instanceof GeographicMetadata) {
                metadata = (GeographicMetadata) candidate;
            } else if (candidate != null) {
                metadata = new GeographicMetadata(reader);
                metadata.mergeTree(candidate);
            } else {
                throw new IIOException("Aucune métadonnée n'a été trouvée"); // TODO: localize
            }
        }
    }

    /**
     * Returns the units for the specified axis, or {@code null} if none.
     */
    private static Unit getUnits(final Axis axis) {
        final String symbol = axis.getUnits();
        if (symbol == null) {
            return null;
        }
        if (symbol.equalsIgnoreCase("days")) {
            return NonSI.DAY;
        }
        return Unit.valueOf(symbol);
    }

    /**
     * Returns the cell coordinates along the specified dimension, or {@code null} if none. If
     * {@linplain ImageGeometry#getCoordinateValues coordinate values} are explicitly defined,
     * they will be returned unchanged. Otherwise if a {@linkplain ImageGeometry#getCoordinateRange
     * coordinate range} is defined, then a suite of coordinate values will be infered from it.
     * <p>
     * This method do not take cell position in account ("center", "upper left corner", etc.).
     */
    private double[] getCoordinateValues(final int dimension) {
        final ImageGeometry geometry = metadata.getGeometry();
        double[] values = geometry.getCoordinateValues(dimension);
        if (values == null) {
            final NumberRange range = geometry.getCoordinateRange(dimension);
            if (range != null) {
                final NumberRange gridRange = geometry.getGridRange(dimension);
                if (gridRange != null) {
                    final double  minimum       = range.getMinimum(); // May be negative infinity.
                    final double  maximum       = range.getMaximum(); // May be positive infinity.
                    final boolean isMinIncluded = range.isMinIncluded();
                    final boolean isMaxIncluded = range.isMaxIncluded();
                    final double  numCells = gridRange.getMaximum(isMaxIncluded) -
                                             gridRange.getMinimum(isMinIncluded);
                    final double step;
                    if (numCells == 0 && minimum == maximum && isMinIncluded && isMaxIncluded) {
                        step = 0; // We have a single value.
                    } else {
                        step = (maximum - minimum) / numCells;
                    }
                    if (!Double.isInfinite(step) && !Double.isNaN(step)) {
                        final int lower  = isMinIncluded ? 0 : 1;
                        final int length = (int) Math.round(numCells) + (isMaxIncluded ? 1 : 0) - lower;
                        values = new double[length];
                        for (int i=0; i<length; i++) {
                            values[i] = minimum + step * (lower + i);
                        }
                    }
                }
            }
        }
        return values;
    }

    /**
     * Returns the range of values for the given axis. This method usually returns a singleton,
     * but more than one range could be returned if the image reader contains data at many
     * coordinates.
     *
     * @param  dimension The CRS dimension for which we want time ranges.
     * @return The coordinate ranges for the given metadata, or {@code null} if none.
     */
    private MeasurementRange[] getCoordinateRanges(final int dimension) {
        final double[] values = getCoordinateValues(dimension);
        if (values == null) {
            return null;
        }
        final Unit units = getUnits(metadata.getReferencing().getAxis(dimension));
        final MeasurementRange[] ranges = new MeasurementRange[values.length];
        switch (ranges.length) {
            case 0: {
                break;
            }
            case 1: {
                final double value = values[0];
                ranges[0] = new MeasurementRange(value, value, units);
                break;
            }
            default: {
                final double[] sorted = new double[values.length];
                final int[] ranks = Ranks.ranks(values, sorted);
                for (int i=0; i<sorted.length; i++) {
                    final int    before = Math.max(i-1, 0);
                    final int    after  = Math.min(i+1, sorted.length - 1);
                    final double value  = sorted[i];
                    ranges[ranks[i]] = new MeasurementRange(
                            value - 0.5*(sorted[before+1] - sorted[before ]), true,
                            value + 0.5*(sorted[after   ] - sorted[after-1]), false, units);
                }
            }
        }
        return ranges;
    }

    /**
     * Returns the date range for the given image metadata. This method usually returns a singleton,
     * but more than one time range could be returned if the image reader contains data at many times.
     *
     * @return The date range for the given metadata, or {@code null} if none.
     */
    public DateRange[] getDateRanges() {
        final ImageReferencing referencing = metadata.getReferencing();
        for (int i=referencing.getDimension(); --i>=0;) {
            final Axis axis = referencing.getAxis(i);
            final Date origin = axis.getTimeOrigin();
            if (origin != null) {
                final MeasurementRange[] ranges = getCoordinateRanges(i);
                if (ranges != null) {
                    final DateRange[] dates = new DateRange[ranges.length];
                    for (int j=0; j<dates.length; j++) {
                        dates[j] = new DateRange(ranges[j], origin);
                    }
                    return dates;
                }
            }
        }
        return null;
    }

    /**
     * Returns the <cite>grid to CRS</cite> transform for the specified axis. The returned
     * transform maps always the {@linkplain PixelOrientation#UPPER_LEFT upper left} corner.
     *
     * @param  xAxis The <var>x</var> axis (usually 0).
     * @param  yAxis The <var>y</var> axis (usually 1).
     * @return The affine transform from grid to CRS, or {@code null} if it can't be computed.
     */
    public AffineTransform getGridToCRS(final int xAxis, final int yAxis) {
        final double[] flatmatrix = new double[6];
        final ImageGeometry geometry = metadata.getGeometry();
        if (!computeAffineCoefficients(geometry, xAxis, xAxis, flatmatrix, false) ||
            !computeAffineCoefficients(geometry, yAxis, yAxis, flatmatrix, true ))
        {
            return null;
        }
        final AffineTransform at = new AffineTransform(flatmatrix);
        final String p = geometry.getPixelOrientation();
        if (p != null) {
            final Point2D offset = GridGeometry2D.getPixelTranslation(p);
            at.translate(-0.5 - offset.getX(), -0.5 - offset.getY());
        }
        return at;
    }

    /**
     * Computes the affine transform cooefficients for the specified dimension.
     *
     * @param geometry   The geometry where to fetch information from.
     * @param sourceDim  The source dimension in the grid geometry.
     * @param targetDim  The target dimension in the envelope. Usually identical to {@code sourceDim}.
     * @param flatmatrix The flat matrix to setup. Coefficients will be written in this array on output.
     * @param asY        {@code false} for setting <var>x</var> coefficients,
     *                   {@code true} for setting <var>y</var>.
     */
    private static boolean computeAffineCoefficients(final ImageGeometry geometry,
            final int sourceDim, final int targetDim, final double[] flatmatrix, final boolean asY)
    {
        final NumberRange sourceRange = geometry.getGridRange(sourceDim);
        if (sourceRange == null) {
            return false;
        }
        final NumberRange targetRange = geometry.getCoordinateRange(targetDim);
        if (targetRange == null) {
            return false;
        }
        /*
         * The 'isMin/MaxIncluded' flags are determined from the target ranges because they are
         * typically floating point values, while source ranges are typically integer values. It
         * is easier and more accurate to keep floating point values "as is" and adjust integer
         * values than the converse.
         */
        final boolean isMinIncluded = targetRange.isMinIncluded();
        final boolean isMaxIncluded = targetRange.isMaxIncluded();
        final double  minimum = targetRange.getMinimum();
        final double  maximum = targetRange.getMaximum();
        final double  lower   = sourceRange.getMinimum(isMinIncluded);
        final double  upper   = sourceRange.getMaximum(isMaxIncluded);
        double scale  = (maximum - minimum) / (upper - lower);
        if (Double.isNaN(scale)) {
            /*
             * We probably have a single value (minimum == maximum && lower == upper). Set the
             * scale to 0. This is not equivalent to NaN, but is not completly unsafe since it
             * will cause the transform to be non-invertible. Users who attempt to invert this
             * transform will be clearly notified by a NonInvertibleTransformException.
             *
             * Setting the scale to 0 allows the transform to produce 'minimum' on very attempt
             * to transform a coordinates, which is probably the expected value. Note that both
             * 'isMin/MaxIncluded' flags are expected to be true, otherwise we probably have an
             * invalid range. Note also that we accept infinite values; they are mathematically
             * okay.
             */
            if (!isMinIncluded || !isMaxIncluded) {
                return false;
            }
            scale = 0;
        }
        // Note: No need for a special processing of "excluded" case in formula below.
        final double offset = minimum - scale * lower;
        if (asY) {
            flatmatrix[3] = scale;
            flatmatrix[5] = offset;
        } else {
            flatmatrix[0] = scale;
            flatmatrix[4] = offset;
        }
        return true;
    }

    /**
     * Returns the CRS identifier, or {@code 0} if unknown.
     *
     * @todo Current implementation uses hard-coded values from EPSG code space.
     *       We need to do something more generic.
     */
    public int getSRID() {
        final ImageReferencing referencing = metadata.getReferencing();
        String type = referencing.getCoordinateReferenceSystem().type;
        if (GeographicMetadataFormat.PROJECTED.equalsIgnoreCase(type)) {
            return 3395; // World Mercator
        }
        if (GeographicMetadataFormat.GEOGRAPHIC.equalsIgnoreCase(type)) {
            return 4326; // WGS 84
        }
        if (GeographicMetadataFormat.GEOGRAPHIC_3D.equalsIgnoreCase(type)) {
            return 4327; // WGS 84
        }
        type = referencing.getCoordinateSystem().type;
        if (GeographicMetadataFormat.CARTESIAN.equalsIgnoreCase(type)) {
            return 3395; // World Mercator
        }
        if (GeographicMetadataFormat.ELLIPSOIDAL.equalsIgnoreCase(type)) {
            return referencing.getDimension() <= 2 ? 4326 : 4327; // WGS 84
        }
        return 0;
    }

    /**
     * Returns the horizontal CRS identifier, or {@code 0} if unknown.
     *
     * @todo Current implementation uses hard-coded values from EPSG code space.
     *       We need to do something more generic.
     */
    public int getHorizontalSRID() {
        int id = getSRID();
        switch (id) {
            case 4327: id = 4326; break;
        }
        return id;
    }

    /**
     * Returns the vertical CRS identifier, or {@code 0} if unknown.
     *
     * @todo Current implementation uses hard-coded values from EPSG code space.
     *       We need to do something more generic.
     */
    public int getVerticalSRID() {
        // Following rule is not exact since the third dimension could be time. This is okay as a
        // patch for now since this vertical SRID will be ignored by WritableGridCoverageTable if
        // getVerticalValues(SI.METER) returns null.
        return metadata.getReferencing().getDimension() > 2 ? 5714 : 0;
    }

    /**
     * Returns the vertical coordinate values in the specified units, or {@code null} if none.
     *
     * @todo Current implementation uses hard-coded values.
     *       We need to do something more generic.
     */
    public double[] getVerticalValues(final Unit units) {
        final ImageReferencing referencing = metadata.getReferencing();
        for (int i=referencing.getDimension(); --i>=0;) {
            final Axis axis = referencing.getAxis(i);
            final String direction = axis.getDirection();
            if (direction != null) {
                final int sign;
                if (direction.equalsIgnoreCase("up")) {
                    sign = +1;
                } else if (direction.equalsIgnoreCase("down")) {
                    sign = -1;
                } else {
                    continue;
                }
                final Unit axisUnits = getUnits(axis);
                final boolean convert = (units != null && axisUnits != null);
                if (convert && !units.isCompatible(axisUnits)) {
                    continue;
                }
                final double values[] = getCoordinateValues(i);
                if (values == null) {
                    continue;
                }
                if (sign != 1) {
                    for (int j=0; j<values.length; j++) {
                        values[j] *= sign;
                    }
                }
                if (convert) {
                    // TODO: Should convert the whole array in one method call (JSR-275)
                    final Converter converter = axisUnits.getConverterTo(units);
                    for (int j=0; j<values.length; j++) {
                        values[j] = converter.convert(values[j]);
                    }
                }
                return values;
            }
        }
        return null;
    }
}
