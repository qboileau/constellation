/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.apache.sis.util.Static;
import org.apache.sis.util.logging.Logging;
import org.constellation.dto.BandDescription;
import org.constellation.dto.CoverageDataDescription;
import org.constellation.dto.DataDescription;
import org.constellation.dto.FeatureDataDescription;
import org.constellation.dto.PropertyDescription;
import org.constellation.gui.binding.*;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.gui.util.StyleFactories.FF;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class StyleUtilities extends Static {

    private static final Logger LOGGER = Logging.getLogger(StyleUtilities.class);

    public static Expression expression(final String label) {
        if (label.startsWith("{") && label.endsWith("}")) {
            return FF.property(label.substring(1, label.length() - 1));
        }
        return FF.literal(label);
    }

    public static Expression opacity(final double opacity) {
        return (opacity >= 0 && opacity <= 1.0) ? FF.literal(opacity) : Expression.NIL;
    }

    public static Expression literal(final Object value) {
        return value != null ? FF.literal(value) : Expression.NIL;
    }

    public static <T> T type(final StyleElement<T> elt) {
        return elt != null ? elt.toType() : null;
    }

    public static <T> List<T> singletonType(final StyleElement<T> elt) {
        return elt != null ? Collections.singletonList(elt.toType()) : new ArrayList<T>(0);
    }

    public static <T> List<T> listType(final List<? extends StyleElement<T>> elts) {
        final List<T> list = new ArrayList<T>();
        if (elts == null) {
            return list;
        }
        for (final StyleElement<T> elt : elts) {
            list.add(elt.toType());
        }
        return list;
    }

    public static Filter filter(final String filter) {
        if (filter == null) {
            return null;
        }
        try {
            return CQL.parseFilter(filter);
        } catch (CQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred during filter parsing.", ex);
        }
        return null;
    }

    public static String writeJson(final Object object) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public static <T> T readJson(final String json, final Class<T> clazz) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, clazz);
    }

    public static String toHex(final Color color) {
        return String.format("#%06X", (0xFFFFFF & color.getRGB()));
    }

    public static final Symbolizer DEFAULT_POINT_SYMBOLIZER        = new PointSymbolizer();
    public static final Symbolizer DEFAULT_LINE_SYMBOLIZER         = new LineSymbolizer();
    public static final Symbolizer DEFAULT_POLYGON_SYMBOLIZER      = new PolygonSymbolizer();
    public static final RasterSymbolizer DEFAULT_GREY_RASTER_SYMBOLIZER  = new RasterSymbolizer();
    public static final RasterSymbolizer DEFAULT_RGB_RASTER_SYMBOLIZER   = new RasterSymbolizer();
    static {
        final ChannelSelection rgbSelection = new ChannelSelection();
        final SelectedChannelType red   = new SelectedChannelType();
        final SelectedChannelType green = new SelectedChannelType();
        final SelectedChannelType blue  = new SelectedChannelType();
        red.setName("0");
        green.setName("1");
        blue.setName("2");
        rgbSelection.setRgbChannels(new SelectedChannelType[]{red, green, blue});
        DEFAULT_RGB_RASTER_SYMBOLIZER.setChannelSelection(rgbSelection);

        final ChannelSelection greySelection = new ChannelSelection();
        final SelectedChannelType grey = new SelectedChannelType();
        grey.setName("0");
        greySelection.setGreyChannel(grey);
        DEFAULT_GREY_RASTER_SYMBOLIZER.setChannelSelection(greySelection);
    }

    public static Style createDefaultStyle(final DataDescription dataDescription) {
        // Create a default rule.
        final Rule rule = new Rule();
        rule.setName("Default");

        // Create a default style;
        final Style style = new Style();
        style.getRules().add(rule);

        // Feature data.
        if (dataDescription instanceof FeatureDataDescription) {
            final PropertyDescription geometryProp = ((FeatureDataDescription) dataDescription).getGeometryProperty();

            final Symbolizer symbolizer;
            if (geometryProp != null) {
                if (isAssignableToPolygon(geometryProp.getType())) {
                    symbolizer = DEFAULT_POLYGON_SYMBOLIZER;
                } else if (isAssignableToLine(geometryProp.getType())) {
                    symbolizer = DEFAULT_LINE_SYMBOLIZER;
                } else {
                    symbolizer = DEFAULT_POINT_SYMBOLIZER;
                }
            } else {
                symbolizer = DEFAULT_POINT_SYMBOLIZER;
            }
            rule.getSymbolizers().add(symbolizer);
        }

        // Coverage data.
        else if (dataDescription instanceof CoverageDataDescription) {
            final List<BandDescription> bands = ((CoverageDataDescription) dataDescription).getBands();

            final Symbolizer symbolizer;
            if (bands.size() >= 2) {
                symbolizer = DEFAULT_RGB_RASTER_SYMBOLIZER;
            } else {
                symbolizer = DEFAULT_GREY_RASTER_SYMBOLIZER;
            }
            rule.getSymbolizers().add(symbolizer);
        }

        // Undefined
        else {
            rule.getSymbolizers().add(DEFAULT_GREY_RASTER_SYMBOLIZER);
            rule.getSymbolizers().add(DEFAULT_POINT_SYMBOLIZER);
        }

        // Return generated style.
        return style;
    }

    /**
     * Determines if the {@link Class} passed in arguments is assignable
     * to a JTS {@link Geometry}.
     *
     * @param clazz the {@link Class}
     * @return {@code true} if the class is assignable to the expected type
     */
    public static boolean isAssignableToGeometry(final Class<?> clazz) {
        return Geometry.class.isAssignableFrom(clazz);
    }

    /**
     * Determines if the {@link Class} passed in arguments is assignable
     * to a JTS polygon {@link Geometry}.
     *
     * @param clazz the {@link Class} to test
     * @return {@code true} if the class is assignable to the expected type
     */
    public static boolean isAssignableToPolygon(final Class<?> clazz) {
        return Polygon.class.isAssignableFrom(clazz) || MultiPolygon.class.isAssignableFrom(clazz);
    }

    /**
     * Determines if the {@link Class} passed in arguments is assignable
     * to a JTS line {@link Geometry}.
     *
     * @param clazz the {@link Class}
     * @return {@code true} if the class is assignable to the expected type
     */
    public static boolean isAssignableToLine(final Class<?> clazz) {
        return LineString.class.isAssignableFrom(clazz) || MultiLineString.class.isAssignableFrom(clazz)
                || LinearRing.class.isAssignableFrom(clazz);
    }

    /**
     * Determines if the {@link Class} passed in arguments is assignable
     * to a JTS point {@link Geometry}.
     *
     * @param clazz the {@link Class}
     * @return {@code true} if the class is assignable to the expected type
     */
    public static boolean isAssignableToPoint(final Class<?> clazz) {
        return Point.class.isAssignableFrom(clazz) || MultiPoint.class.isAssignableFrom(clazz);
    }
}
