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
package net.seagis.observation.fishery;

// OpenGis dependencies
import org.opengis.observation.Observation;


/**
 * Une {@linkplain Species espèces} observée à un certain {@linkplain Stage stade de développement}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Category extends Observation {
    /**
     * Retourne l'espèce observée.
     */
    Species getObservedProperty();
    /**
     * Retourne le stade de développement de l'espèce observé.
     */
    Stage getStage();

    /**
     * Retourne la méthode par laquelle les individus sont capturés.
     */
    FisheryType getProcedure();
}
