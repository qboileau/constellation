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
package org.constellation.resources.i18n;

import java.io.File;
import org.geotools.resources.IndexedResourceCompiler;


/**
 * Resource compiler.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Compiler {
    /**
     * The base directory for {@code "java"} {@code "resources"} sub-directories.
     * The directory structure must be consistent with Maven conventions.
     */
    private static final File SOURCE_DIRECTORY = new File("postgrid/src/main");

    /**
     * The resources to process.
     */
    private static final Class[] RESOURCES_TO_PROCESS = {
        Resources.class
    };

    /**
     * Do not allows instantiation of this class.
     */
    private Compiler() {
    }

    /**
     * Run the resource compiler.
     */
    public static void main(final String[] args) {
        IndexedResourceCompiler.main(args, SOURCE_DIRECTORY, RESOURCES_TO_PROCESS);
    }
}
