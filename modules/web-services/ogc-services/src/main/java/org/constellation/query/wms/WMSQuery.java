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
package org.constellation.query.wms;

import org.constellation.query.Query;
import org.constellation.query.QueryService;
import org.constellation.ws.ServiceVersion;


/**
 * WMS Query in java objects
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public abstract class WMSQuery extends Query {
    /**
     * Request parameters.
     */
    public static final String GETMAP = "GetMap";
    public static final String GETFEATUREINFO = "GetFeatureInfo";
    public static final String GETCAPABILITIES = "GetCapabilities";
    public static final String DESCRIBELAYER = "DescribeLayer";
    public static final String GETLEGENDGRAPHIC = "GetLegendGraphic";
    public static final String GETORIGFILE = "GetOrigFile";

    /** Parameter used in getMap, getLegendGraphic, getCapabilities */
    public static final String KEY_FORMAT = "FORMAT";
    /** Parameter used in getMap, describeLayer */
    public static final String KEY_LAYERS = "LAYERS";
    /** Parameter used in getOrigFile, getLegendGraphic */
    public static final String KEY_LAYER = "LAYER";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_QUERY_LAYERS = "QUERY_LAYERS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_v111 = "SRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_v130 = "CRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_BBOX = "BBOX";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_ELEVATION = "ELEVATION";
    /** Parameter used in getMap, getOrigFile, getFeatureInfo */
    public static final String KEY_TIME = "TIME";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_WIDTH = "WIDTH";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getMap */
    public static final String KEY_BGCOLOR = "BGCOLOR";
    /** Parameter used in getMap */
    public static final String KEY_TRANSPARENT = "TRANSPARENT";
    /** Parameter used in getMap */
    public static final String KEY_STYLES = "STYLES";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_STYLE = "STYLE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_SLD = "SLD";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_FEATURETYPE = "FEATURETYPE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_COVERAGE = "COVERAGE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_RULE = "RULE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SCALE = "SCALE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SLD_BODY = "SLD_BODY";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_TYPE = "REMOTE_OWS_TYPE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_URL = "REMOTE_OWS_URL";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_v130 = "I";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_v130 = "J";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_v110 = "X";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_v110 = "Y";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_INFO_FORMAT= "INFO_FORMAT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_FEATURE_COUNT = "FEATURE_COUNT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_GETMETADATA = "GetMetadata";
    /** Parameter used in getMap */
    public static final String KEY_DIM_RANGE = "DIM_RANGE";
    /** Parameter used in getMap */
    public static final String KEY_AZIMUTH = "AZIMUTH";

    protected final ServiceVersion version;

    /**
     * {@inheritDoc}
     */
    public final QueryService getService() {
        return new QueryService.WMS();
    }

    protected WMSQuery(final ServiceVersion version) {
        if (version == null) {
            throw new NullPointerException("Version should not be null !");
        }
        this.version = version;
    }

    public final ServiceVersion getVersion() {
        return version;
    }
}
