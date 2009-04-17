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
package org.constellation.coverage.catalog;

import org.opengis.coverage.SampleDimension;
import org.geotoolkit.coverage.Category;


/**
 * Indique la profondeur de l'arborescence attendue de {@link LayerTree#getTree}. Les arborescences
 * peuvent contenir des chemins de la forme "{@linkplain Thematic thématique}//{@linkplain Layer
 * couche}/{@linkplain Format format}/{@linkplain SampleDimension bande}/{@linkplain Category catégorie}".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum TreeDepth {
    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Thematic thèmes}.
     */
    THEMATIC(LayerTree.THEMATIC),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Layer couches}.
     */
    LAYER(LayerTree.LAYER),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les séries (après les couches).
     */
    SERIES(LayerTree.SERIES),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Format formats}.
     */
    FORMAT(LayerTree.FORMAT),

    /**
     * Indique que l'arborescence doit aller jusqu'aux catégories (après les formats).
     */
    CATEGORY(LayerTree.FORMAT + 2);

    /**
     * La profondeur de l'arborescence, comptée comme le nombre de noeuds à traverser jusqu'à la
     * feuille (feuille inclue).
     */
    final int rank;

    /**
     * Construit une nouvelle énumération avec la profondeur d'arborescence spécifiée. Cette
     * profondeur est comptée comme le nombre de noeuds à traverser jusqu'à la feuille
     * (feuille inclue).
     */
    private TreeDepth(final int rank) {
        this.rank = rank;
    }
}
