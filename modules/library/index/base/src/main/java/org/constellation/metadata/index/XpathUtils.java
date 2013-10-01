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

package org.constellation.metadata.index;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class XpathUtils {

    public static List<String> xpathToMDPath(final List<String> xpaths) {
        final List<String> results = new ArrayList<>();
        for (String xpath : xpaths) {
            // remove the first slash
            xpath = xpath.substring(1);

            // extract conditional attribute if there is one
            String condPart = null;
            String condAtt  = null;
            final int condIndex = xpath.indexOf('#');
            if (condIndex != -1) {
                condPart = xpath.substring(condIndex + 1);
                final int eqIndex  = condPart.indexOf('=');
                int endCondIndex = condPart.indexOf('/', eqIndex);
                if (endCondIndex != -1) {
                    condPart = condPart.substring(0, endCondIndex);
                }
                condAtt = condPart.substring(eqIndex);
                xpath = xpath.replace("#" + condPart, "#");
                condPart = condPart.substring(0, eqIndex);
            }

            // extract standard:type for root
            final int index       = xpath.indexOf('/');
            final String root     = xpath.substring(0, index);
            final int separator   = root.indexOf(':');
            final String prefix   = root.substring(0, separator);
            final String type     = root.substring(separator + 1);
            final String rootPath = getStandardFromPrefix(prefix) + ':' + type;
            xpath = xpath.substring(index + 1);

            //ebrim path do not remove typeNode
            final boolean content = prefix.equals("csw");
            final boolean rmTypeNode = prefix.startsWith("eb") || content;

            xpath = rootPath + toMDPath(xpath, content, rmTypeNode);
            if (condPart != null) {
                condPart = toMDPath(condPart, content, rmTypeNode) + condAtt;
                condPart = condPart.substring(1);
                xpath = xpath.replace("#", "#" + condPart);
            }
            if (!results.contains(xpath)) {
                results.add(xpath);
            }
        }
        return results;
    }



    private static String toMDPath(String xpath, final boolean content, final boolean rmTypeNode) {
        final StringBuilder result = new StringBuilder();
        final String[] parts = xpath.split("/");
        
        for (int i = 0; i < parts.length; i++) {
            int separator = parts[i].indexOf(':');
            final String propName = parts[i].substring(separator + 1);
            //do not add type node
            if (rmTypeNode || i % 2 == 0) {
                //special case
                if (i < parts.length - 1 && parts[i + 1].equals("gmd:EX_GeographicDescription")) {
                    result.append(':').append(propName).append("3");
                } else if (i < parts.length - 1 && parts[i + 1].equals("gmd:EX_GeographicBoundingBox")) {
                    result.append(':').append(propName).append("2");
                } else if (propName.equals("@codeListValue")) {
                    // skip this property
                } else if (propName.startsWith("@")) {
                    result.append(':').append(propName.substring(1));
                } else {
                    result.append(':').append(propName);
                }
            } else if ("Anchor".equals(propName)) {
                result.append(":value");
            } else if (propName.contains("#")) {
                result.append('#');
            }
        }
        if (content && !parts[0].equals("ows:BoundingBox")) {
            result.append(":content");
        }
        return result.toString();
    }

    private static String getStandardFromPrefix(final String prefix) {
        switch (prefix) {
            case "gfc": return "ISO 19110";
            case "gmd": return "ISO 19115";
            case "gmi": return "ISO 19115-2";
            case "csw": return "Catalog Web Service";
            case "eb3": return "Ebrim v3.0";
            case "eb2": return "Ebrim v2.5";
            case "wrs": return "Web Registry Service v1.0";
            case "wr" : return "Web Registry Service v0.9";
            default: throw new IllegalArgumentException("Unexpected prefix: " + prefix);

        }
    }
}
