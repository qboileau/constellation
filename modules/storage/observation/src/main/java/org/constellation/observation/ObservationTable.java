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
package org.constellation.observation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


// Constellation dependencies
import java.sql.Timestamp;
import java.util.logging.Logger;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.constellation.sampling.SamplingFeatureTable;
import org.constellation.sampling.SamplingPointTable;
import org.constellation.swe.v101.AnyResultTable;
import org.constellation.swe.v101.CompositePhenomenonTable;

// OpenGis dependencies
import org.constellation.swe.v101.PhenomenonTable;
import org.geotoolkit.gml.xml.v311.AbstractTimeGeometricPrimitiveType;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.observation.xml.v100.ProcessEntry;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sampling.xml.v100.SamplingPointEntry;
import org.geotoolkit.swe.xml.v101.AnyResultEntry;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonEntry;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.Observation;

// geotools dependencies
import org.geotoolkit.util.Utilities;


/**
 * Classe de base des connections vers la table des {@linkplain Observation observation}.
 * La requête SQL donné au constructeur doit répondre aux conditions suivantes:
 * <p>
 * <ul>
 *   <li>Les deux premiers arguments doivent être la {@linkplain Station station} et
 *       l'{@linkplain Observable observable} recherchés, dans cet ordre.</li>
 *   <li>Les deux premières colonnes retournées doivent aussi être les identifiants de la
 *       {@linkplain Station station} et de l'{@linkplain Observable observable}.</li>
 * </ul>
 * <p>
 * Exemple:
 *
 * <blockquote><pre>
 * SELECT station, observable FROM Observations WHERE (station = ?) AND (observable = ?)
 * </pre></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 * @author Guilhem Legal
 */
public class ObservationTable<EntryType extends Observation> extends SingletonTable<Observation> {
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code ObservationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingFeatureTable stations;
    
    /**
     * Connexion vers la table des stations.
     * <p>
     * <strong>NOTE:</strong> {@link StationTable} garde elle-même une référence vers cette instance
     * de {@code ObservationTable}, mais seule {@link StationEntry} l'utilise. L'ordre d'acquisition
     * des verrous devrait toujours être {@code ObservationTable} d'abord, et {@code StationTable}
     * ensuite.
     */
    protected SamplingPointTable stationPoints;
    
    /**
     * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected PhenomenonTable phenomenons;
    
    /**
     * Connexion vers la table des {@linkplain CompositePhenomenon phénomènes composés}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected CompositePhenomenonTable compositePhenomenons;
    
    /**
     * Connexion vers la table des {@linkplain Procedure procedures}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected ProcessTable procedures;
    
    /**
     * Connexion vers la table des {@linkplain AnyResult result}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected AnyResultTable results;
    
    /**
     * La station pour laquelle on veut des observations, ou {@code null} pour récupérer les
     * observations de toutes les stations.
     */
    protected SamplingFeature featureOfInterest;
    
    
    /**
     * Construit une nouvelle connexion vers la table des observations. Voyez la javadoc de
     * cette classe pour les conditions que doivent remplir la requête donnée en argument.
     *
     * @param  database Connexion vers la base de données des observations.
     */
    public ObservationTable(final Database database) {
        this(new ObservationQuery(database));
    }
    
    /**
     * Super constructeur qui est appelé par les classe specialisant ObservationTable.
     *
     * @param  database Connexion vers la base de données des observations.
     */
    protected ObservationTable(ObservationQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Retourne la station pour laquelle on recherche des observations.
     */
    public final SamplingFeature getFeatureOfInterest() {
        return featureOfInterest;
    }
    
    /**
     * Définit la station pour laquelle on recherche des observations.
     * La valeur {@code null} recherche toutes les stations.
     */
    public synchronized void setStation(final SamplingFeature station) {
        if (!Utilities.equals(station, this.featureOfInterest)) {
            this.featureOfInterest = station;
            fireStateChanged("Station");
        }
    }
    
    /**
     * Configure la requête SQL spécifiée en fonction de la station et de l'observable recherchés
     * par cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     *
     * @Override
     * protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
     * super.configure(type, statement);
     * if (featureOfInterest != null) {
     * statement.setInt(STATION, featureOfInterest.getNumericIdentifier());
     * } else {
     * throw new UnsupportedOperationException("La recherche sur toutes les stations n'est pas encore impléméntée.");
     * }
     * if (observable != null) {
     * statement.setInt(OBSERVABLE, observable.getNumericIdentifier());
     * } else {
     * throw new UnsupportedOperationException("La recherche sur tous les observables n'est pas encore impléméntée.");
     * }
     * }
     */
    
    /**
     * Retourne {@code true} s'il existe au moins une entrée pour la station et l'observable
     * courant.
     *
     * public synchronized boolean exists() throws SQLException {
     * final PreparedStatement statement = getStatement(getProperty(select));
     * final ResultSet result = statement.executeQuery();
     * final boolean exists = result.next();
     * result.close();
     * return exists;
     * }
     *
     */
    
    /**
     * Retourne les observations pour la station et l'observable courants. Cette méthode
     * ne retourne jamais {@code null}, mais peut retourner un ensemble vide. L'ensemble
     * retourné ne contiendra jamais plus d'un élément si une station et un observable
     * non-nuls ont été spécifiés à cette table.
     *
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     *
     * public synchronized List<EntryType> getEntries() throws CatalogException, SQLException {
     * final List<EntryType> list = new ArrayList<EntryType>();
     * final PreparedStatement statement = getStatement(getProperty(select));
     * final ResultSet result = statement.executeQuery();
     * while (result.next()) {
     * list.add(createEntry(result));
     * }
     * result.close();
     * return list;
     * }*/
    
    /**
     * Retourne une seule observation pour la station et l'observable courants, ou {@code null}
     * s'il n'y en a pas. Cette méthode risque d'échouer si la station et l'observable n'ont pas
     * été spécifiés tous les deux à cette table avec une valeur non-nulle.
     *
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     *
     * public synchronized EntryType getEntry() throws CatalogException, SQLException {
     * final PreparedStatement statement = getStatement(getProperty(select));
     * final ResultSet results = statement.executeQuery();
     * EntryType observation = null;
     * while (results.next()) {
     * final EntryType candidate = createEntry(results);
     * if (observation == null) {
     * observation = candidate;
     * } else if (!observation.equals(candidate)) {
     * throw new DuplicatedRecordException(results, 1, String.valueOf(observation));
     * }
     * }
     * results.close();
     * return observation;
     * }
     */
    
    /**
     * Construit une observation pour l'enregistrement courant.
     */
    @Override
    public Observation createEntry(final ResultSet result) throws CatalogException, SQLException {
        final ObservationQuery query = (ObservationQuery) super.query;
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        PhenomenonEntry pheno = (PhenomenonEntry)phenomenons.getEntry(result.getString(indexOf(query.observedProperty)));
        
        if (compositePhenomenons == null) {
            compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
        }
        final CompositePhenomenonEntry compoPheno = compositePhenomenons.getEntry(result.getString(indexOf(query.observedPropertyComposite)));
        
        if (stations == null) {
            stations = getDatabase().getTable(SamplingFeatureTable.class);
        }
        SamplingFeatureEntry station = stations.getEntry(result.getString(indexOf(query.featureOfInterest)));
        
        if (stationPoints == null) {
            stationPoints = getDatabase().getTable(SamplingPointTable.class);
        }
        final SamplingPointEntry stationPoint = stationPoints.getEntry(result.getString(indexOf(query.featureOfInterestPoint)));
        
        if (procedures == null) {
            procedures = getDatabase().getTable(ProcessTable.class);
        }
        final ProcessEntry procedure = procedures.getEntry(result.getString(indexOf(query.procedure)));
        
        if (results == null) {
            results = getDatabase().getTable(AnyResultTable.class);
        }
        final AnyResultEntry any = results.getEntry(result.getInt(indexOf(query.result)));
        Object resultat = null;
        if (any != null) {
            if (any.getReference() == null && any.getArray() != null) {
                resultat = any.getPropertyArray();
            } else if (any.getReference() != null && any.getArray() == null)  {
                resultat = any.getReference();
            }
        }
        
        if(pheno == null) pheno     = compoPheno;
        if(station == null) station =  stationPoint;

        final String name                               = result.getString(indexOf(query.name));
        final Timestamp begin                           = result.getTimestamp(indexOf(query.samplingTimeBegin));
        final Timestamp end                             = result.getTimestamp(indexOf(query.samplingTimeEnd));
        AbstractTimeGeometricPrimitiveType samplingTime = null;
        TimePositionType beginPosition                  = null;
        TimePositionType endPosition                    = null;
        if (begin != null) {
            final String normalizedTime = begin.toString().replace(' ', 'T');
            beginPosition = new TimePositionType(normalizedTime);
        }
        if (end != null) {
            final String normalizedTime = end.toString().replace(' ', 'T');
            endPosition = new TimePositionType(normalizedTime);
        }
        
        if (beginPosition != null && endPosition != null) {
            samplingTime = new TimePeriodType(beginPosition, endPosition);
        
        } else if (begin != null && end == null) {
            samplingTime =  new TimeInstantType(beginPosition);
        
        //this case will normally never append
        } else if (begin == null && end != null) {
            samplingTime =  new TimeInstantType(endPosition);
        }
        if(samplingTime != null) {
            samplingTime.setId("samplingTime-" + name);
        }
        
        return new ObservationEntry(name,
                                    result.getString(indexOf(query.description)),
                                    station,
                                    pheno,
                                    procedure,
                                    //manque quality
                                    resultat,
                                    samplingTime);
        
        
    }
    
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de l'observation passée en parametre si non-null)
     * et enregistre la nouvelle observation dans la base de donnée.
     *
     * @param l'observation a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final Observation obs) throws SQLException, CatalogException {
        final ObservationQuery query = (ObservationQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (obs.getName() != null) {
                final PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.name), obs.getName());
                final ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return obs.getName();
                } else {
                    id = obs.getName();
                }
                result.close();
            } else {
                id = searchFreeIdentifier("urn:object:observation:BRGM");
            }
            final PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.name),         id);
            statement.setString(indexOf(query.description),  obs.getDefinition());
        
            // regler le probleme avec la distribution
            statement.setString(indexOf(query.distribution), "normale");
        
        
            // on insere la station qui a effectué cette observation
            if (obs.getFeatureOfInterest() instanceof SamplingPointEntry){
                final SamplingPointEntry station = (SamplingPointEntry)obs.getFeatureOfInterest();
                if (stationPoints == null) {
                    stationPoints = getDatabase().getTable(SamplingPointTable.class);
                }
                statement.setString(indexOf(query.featureOfInterestPoint),stationPoints.getIdentifier(station));
                statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
            
            } else if (obs.getFeatureOfInterest() instanceof SamplingFeatureEntry){
                final SamplingFeatureEntry station = (SamplingFeatureEntry)obs.getFeatureOfInterest();
                if (stations == null) {
                    stations = getDatabase().getTable(SamplingFeatureTable.class);
                }
                statement.setString(indexOf(query.featureOfInterest),stations.getIdentifier(station));
                statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
            } else {
                statement.setNull(indexOf(query.featureOfInterest),    java.sql.Types.VARCHAR);
                statement.setNull(indexOf(query.featureOfInterestPoint),    java.sql.Types.VARCHAR);
            }
        
            // on insere le phenomene observé
            if(obs.getObservedProperty() instanceof CompositePhenomenonEntry){
                final CompositePhenomenonEntry pheno = (CompositePhenomenonEntry)obs.getObservedProperty();
                if (compositePhenomenons == null) {
                    compositePhenomenons = getDatabase().getTable(CompositePhenomenonTable.class);
                }
                statement.setString(indexOf(query.observedPropertyComposite), compositePhenomenons.getIdentifier(pheno));
                statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
        
            } else if(obs.getObservedProperty() instanceof PhenomenonEntry){
                final PhenomenonEntry pheno = (PhenomenonEntry)obs.getObservedProperty();
                if (phenomenons == null) {
                    phenomenons = getDatabase().getTable(PhenomenonTable.class);
                }
                statement.setString(indexOf(query.observedProperty), phenomenons.getIdentifier(pheno));
                statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);
       
            } else {
                statement.setNull(indexOf(query.observedProperty), java.sql.Types.VARCHAR);
                statement.setNull(indexOf(query.observedPropertyComposite), java.sql.Types.VARCHAR);
            }
        
            //on insere le capteur
            if (obs.getProcedure() != null) {
                final ProcessEntry process = (ProcessEntry)obs.getProcedure();
                if (procedures == null) {
                    procedures = getDatabase().getTable(ProcessTable.class);
                }
                statement.setString(indexOf(query.procedure), procedures.getIdentifier(process));
            } else {
                statement.setNull(indexOf(query.procedure), java.sql.Types.VARCHAR);
            }
        
            // on insere le resultat
            if (obs.getResult() instanceof ReferenceEntry || obs.getResult() instanceof AnyResultEntry || obs.getResult() instanceof DataArrayPropertyType){
                if (results == null) {
                    results = getDatabase().getTable(AnyResultTable.class);
                }
                final String rid = results.getIdentifier(obs.getResult());
                Integer prid = null;
                boolean parsed = true;
                try {
                    prid = Integer.parseInt(rid);
                } catch (NumberFormatException ex) {
                    Logger.getAnonymousLogger().severe("unable to parse an integer identifier for result:" + rid);
                    parsed = false;
                }
                if (parsed)
                    statement.setInt(indexOf(query.result), prid);
                else
                    statement.setNull(indexOf(query.result), java.sql.Types.INTEGER);

            } else {
                statement.setNull(indexOf(query.result), java.sql.Types.INTEGER);
            }
        
            // on insere le "samplingTime""
            if (obs.getSamplingTime() != null){
                if (obs.getSamplingTime() instanceof TimePeriodType) {
                    
                    final TimePeriodType sampTime = (TimePeriodType)obs.getSamplingTime();
                    if (sampTime.getBeginPosition()!= null) {
                        String s       = sampTime.getBeginPosition().getValue();
                        s = s.replace("T", " ");
                        final Timestamp date = Timestamp.valueOf(s);
                        statement.setTimestamp(indexOf(query.samplingTimeBegin), date);
                    } else {
                        statement.setNull(indexOf(query.samplingTimeBegin), java.sql.Types.TIMESTAMP);
                    }
                    
                    if (sampTime.getEndPosition() != null) {
                       
                        String s = sampTime.getEndPosition().getValue();
                        s = s.replace("T", " ");
                        final Timestamp date = Timestamp.valueOf(s);
                        statement.setTimestamp(indexOf(query.samplingTimeEnd),  date);
                   
                    } else {
                        statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
                    }
                    
                } else if (obs.getSamplingTime() instanceof TimeInstantType) {
                    final TimeInstantType sampTime = (TimeInstantType)obs.getSamplingTime();
                    if (sampTime.getTimePosition() !=null) {
                        final String s       = sampTime.getTimePosition().getValue();
                        final Timestamp date = Timestamp.valueOf(s);
                        statement.setTimestamp(indexOf(query.samplingTimeBegin),  date);
                        statement.setNull(indexOf(query.samplingTimeEnd), java.sql.Types.DATE);
                    } else {
                        statement.setNull(indexOf(query.samplingTimeBegin), java.sql.Types.DATE);
                        statement.setNull(indexOf(query.samplingTimeEnd), java.sql.Types.DATE);
                    }
                    
                } else {
                    throw new IllegalArgumentException("type allowed for sampling time: TimePeriod or TimeInstant");
                }
            } else {
                statement.setNull(indexOf(query.samplingTimeBegin), java.sql.Types.DATE);
                statement.setNull(indexOf(query.samplingTimeEnd),   java.sql.Types.DATE);
            }
        
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
    
}
