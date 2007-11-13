/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.seagis.swe;

import java.util.Collection;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;

/**
 * Resultat d'une observation de type DataBlockDefinition.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataBlockDefinition", propOrder = {
    "components",
    "encoding"})
    public class DataBlockDefinitionEntry extends Entry implements DataBlockDefinition {
    
    /**
     * L'identifiant du resultat.
     */
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    private String id;
    
    /**
     * Liste de composant Data record.
     */
    private Collection<? extends AbstractDataComponentEntry> components;
    
    /**
     * Decrit l'encodage des données.
     */
    private AbstractEncodingEntry encoding;
    
    /**
     * constructeur utilisé par jaxB
     */
    protected DataBlockDefinitionEntry() {}
    
    /**
     * créé un nouveau resultat d'observation.
     * Liste de valeur decrite dans swe:DatablockDefinition de type simple,
     * pour valeur scalaire ou textuelle.
     *
     * @param id l'identifiant du resultat.
     * @param components liste de composant data record.
     * @param encoding encodage des données.
     */
    public DataBlockDefinitionEntry(final String id, final Collection<? extends AbstractDataComponentEntry> components,
            final AbstractEncodingEntry encoding) {
        super(id);
        this.id         = id;
        this.components = components;
        this.encoding   = encoding;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }
    
    /**
     * surcharge le getName() de Entry
     */
    @Override
    public String getName() {
        return this.name;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends AbstractDataComponentEntry> getComponents() {
        return components;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractEncoding getEncoding() {
        return encoding;
    }
    
    /**
     * Retourne un code représentant ce dataBlock.
     */
    @Override
    public final int hashCode() {
        return id.hashCode();
    }
    
    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        
        final DataBlockDefinitionEntry that = (DataBlockDefinitionEntry) object;
        if (this.components.size() != that.components.size())
            return false;
        
        Iterator<? extends AbstractDataComponentEntry> i = components.iterator();
        while (i.hasNext()) {
            if (!that.components.contains(i.next()))
                return false;
        }
        return Utilities.equals(this.id,         that.id) &&
               Utilities.equals(this.encoding,   that.encoding) ;
    }
    
    /**
     * Retourne une representation de l'objet (debug).
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        final String lineSeparator = System.getProperty("line.separator", "\n");
        buffer.append('[').append(this.getClass().getSimpleName()).append("]:").append(id).append(lineSeparator);
        buffer.append("encoding: ").append(this.encoding.toString()).append(lineSeparator);
        appendTo(buffer, "", lineSeparator);
        return buffer.toString();
    }
    
    /**
     * Ajoute la description des composants du dataBlock definition.
     */
    private void appendTo(final StringBuilder buffer, String margin, final String lineSeparator) {
        buffer.append("components: ").append(lineSeparator);
        margin += "  ";
        for (final AbstractDataComponentEntry a : components) {
            buffer.append(margin).append(a.toString()).append(lineSeparator);
        }
    }
    
}
