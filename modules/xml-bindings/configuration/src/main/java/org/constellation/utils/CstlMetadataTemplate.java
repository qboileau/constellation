/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.utils;

import org.apache.sis.metadata.iso.DefaultMetadata;

/**
 * Enumeration of the different types of Metadata used in the application.
 *
 * @author Guilhem Legal (Geomatys)
 */
public enum CstlMetadataTemplate {
    DATA("data"),
    SERVICE("serv");

    private final String prefix;

    private CstlMetadataTemplate(final String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
    
    /**
     * Returns the {@linkplain CstlMetadataTemplate enum value} with the given identifier,
     * or {@code null} if none found.
     *
     * @param metadataId
     * @return A template matching the prefix.
     */
    public static CstlMetadataTemplate valueForPrefix(final String metadataId) {
        if (metadataId.startsWith(DATA.getPrefix())) {
            return DATA;
        }
        if (metadataId.startsWith(SERVICE.getPrefix())) {
            return SERVICE;
        }
        return null;
    }

    /**
     * Determines the {@linkplain DcnsMetadataTemplate metadata template} for a given
     * {@linkplain DefaultMetadata metadata}.
     *
     * @param metadata The metadata for which to retrieve its template.
     * @return A {@linkplain DcnsMetadataTemplate template, or {@code null} if none found
     *         for this metadata.
     */
    public static CstlMetadataTemplate findTemplateForMetadata(final DefaultMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        final MetadataFeeder feeder =new MetadataFeeder(metadata);
        final String ident = feeder.getIdentifier();
        if (ident == null || ident.isEmpty()) {
            return null;
        }

        return valueForPrefix(ident);
    }
}