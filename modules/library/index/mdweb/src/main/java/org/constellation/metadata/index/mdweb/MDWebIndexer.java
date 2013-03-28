/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.metadata.index.mdweb;

// J2SE dependencies
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;
import javax.sql.DataSource;

// Apache Lucene dependencies
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.index.AbstractCSWIndexer;

import static org.constellation.metadata.CSWQueryable.*;

// geotoolkit dependencies
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.temporal.object.TemporalUtilities;

// MDweb dependencies
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.CodeList;
import org.mdweb.model.schemas.CodeListElement;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.RecordSet;
import org.mdweb.model.storage.FullRecord;
import org.mdweb.model.storage.TextValue;
import org.mdweb.model.storage.Value;
import org.mdweb.io.Reader;
import org.mdweb.io.MD_IOException;
import org.mdweb.io.MD_IOFactory;
import org.mdweb.model.storage.RecordSet.EXPOSURE;

/**
 * A Lucene index handler for an MDWeb Database.
 *
 * @author Guilhem Legal
 */
public class MDWebIndexer extends AbstractCSWIndexer<FullRecord> {

    /**
     * The Reader of this lucene index (MDWeb DB mode).
     */
    private Reader mdWebReader;

    /**
     * main ebrim 3.0 classes
     */
    private Classe identifiable;

    /**
     * main ebrim 2.5 classes
     */
    private Classe registryObject;

    private final boolean indexOnlyPusblishedMetadata;

    private final boolean indexInternalRecordset;

    private final boolean indexExternalRecordset;

    public MDWebIndexer(final Automatic configuration, final String serviceID) throws IndexingException {
        this(configuration, serviceID, INSPIRE_QUERYABLE);
    }

    /**
     * Creates a new CSW indexer for a MDWeb database.
     *
     * @param configuration A configuration object containing the database informations. Must not be null.
     * @param serviceID The identifier, if there is one, of the index/service.
     */
    public MDWebIndexer(final Automatic configuration, final String serviceID, final Map<String, List<String>> additionalQueryable) throws IndexingException {
        super(serviceID, configuration.getConfigurationDirectory(), additionalQueryable);

        this.indexOnlyPusblishedMetadata = configuration.getIndexOnlyPublishedMetadata();
        this.indexInternalRecordset      = configuration.getIndexInternalRecordset();
        this.indexExternalRecordset      = configuration.getIndexExternalRecordset();

        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new IndexingException("The configuration file does not contains a BDD object");
        }
        try {
            final DataSource dataSource = db.getDataSource();
            final boolean isPostgres    = db.getClassName().equals("org.postgresql.Driver");
            MD_IOFactory factory = null;
            final Iterator<MD_IOFactory> ite = ServiceRegistry.lookupProviders(MD_IOFactory.class);
            while (ite.hasNext()) {
                MD_IOFactory currentFactory = ite.next();
                if (currentFactory.matchImplementationType(dataSource, isPostgres)) {
                    factory = currentFactory;
                }
            }
            if (factory != null) {
                mdWebReader                 = factory.getReaderInstance(dataSource, isPostgres);
                mdWebReader.setProperty("readProfile", false);
                initEbrimClasses();
                if (create) {
                    createIndex();
                }
            } else {
                throw new IndexingException("Unable to find a MD_IO factory");
            }
        } catch (SQLException ex) {
            throw new IndexingException("SQL Exception while creating mdweb indexer: " + ex.getMessage());
        } catch (MD_IOException ex) {
            throw new IndexingException("MD_IO Exception while creating mdweb indexer(during Ebrim classes reading): " + ex.getMessage());
        }
    }

    /**
     * Load the ebrim classes from the MDWeb database.
     *
     * @throws MD_IOException
     */
    private void initEbrimClasses() throws MD_IOException {
        identifiable   = mdWebReader.getClasse("Identifiable", Standard.EBRIM_V3);
        registryObject = mdWebReader.getClasse("RegistryObject", Standard.EBRIM_V2_5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getAllIdentifiers() throws IndexingException {
        try {
            // getting the objects list and index avery item in the IndexWriter.
            final List<RecordSet> cats = mdWebReader.getRecordSets();
            final List<RecordSet> catToIndex = new ArrayList<RecordSet>();
            for (RecordSet r : cats) {
                if (indexInternalRecordset) {
                    if (r.getExposure() == EXPOSURE.INTERNAL) {
                        catToIndex.add(r);
                    } else {
                        LOGGER.log(logLevel, "RecordSet:{0} is internal we exclude it.", r.getCode());
                    }
                }
                if (indexExternalRecordset) {
                    if (r.getExposure() == EXPOSURE.EXTERNAL) {
                        catToIndex.add(r);
                    } else {
                        LOGGER.log(logLevel, "RecordSet:{0} is external we exclude it.", r.getCode());
                    }
                }
            }
            return mdWebReader.getAllIdentifiers(catToIndex, indexOnlyPusblishedMetadata);
        } catch (MD_IOException ex) {
            throw new IndexingException("MD_IOException while reading all identifiers", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FullRecord getEntry(final String identifier) throws IndexingException {
        try {
            return mdWebReader.getRecord(identifier);
        } catch (MD_IOException ex) {
            throw new IndexingException("MD_IOException while reading entry for:" + identifier, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIdentifier(FullRecord obj) {
        return obj.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexSpecialField(final FullRecord metadata, final Document doc) throws IndexingException {
        if (metadata.getRoot() == null) {
            throw new IndexingException("unable to index record:" + metadata.getId() + " top value is null");

        } else if (metadata.getRoot().getType() == null) {
            throw new IndexingException("unable to index record:" + metadata.getId() + " top value type is null");
        }

        final String identifier = metadata.getIdentifier();
        doc.add(new Field("id",        identifier,           Field.Store.YES, Field.Index.NOT_ANALYZED));
        //doc.add(new Field("Title",     metadata.getTitle(),  Field.Store.YES, Field.Index.ANALYZED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isISO19139(FullRecord record) {
       return record.getRoot().getType().getName().equals("MD_Metadata") ||
              record.getRoot().getType().getName().equals("MI_Metadata");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isDublinCore(FullRecord record) {
        return record.getRoot().getType().getName().equals("Record");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim25(FullRecord record) {
        return record.getRoot().getType().isSubClassOf(registryObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim30(FullRecord record) {
        return record.getRoot().getType().isSubClassOf(identifiable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFeatureCatalogue(FullRecord record) {
        return record.getRoot().getType().getName().equals("FC_FeatureCatalogue");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getType(FullRecord f) {
        return f.getRoot().getType().getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexQueryableSet(final Document doc, final FullRecord record, Map<String, List<String>> queryableSet, final StringBuilder anyText) throws IndexingException {
        for (Entry<String,List<String>> entry :queryableSet.entrySet()) {
            final List<Object> values = getValuesList(record, entry.getValue());
            indexFields(values, entry.getKey(), anyText, doc);
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    @Deprecated
    protected String getValues(final FullRecord record, final List<String> paths) throws IndexingException {
        final StringBuilder response  = new StringBuilder();
        if (paths != null) {
            for (String fullPathID: paths) {
                try {
                    final List<Value> values = getValuesFromPathID(fullPathID, record);
                    for (final Value v: values) {
                        //only handle textvalue
                        if (!(v instanceof TextValue)) {continue;}

                        final TextValue tv = (TextValue) v;
                        try  {
                            response.append(getTextValueDescription(tv)).append(',');
                        } catch (NumberFormatException ex) {
                             LOGGER.warning("Unable to parse value:" + tv.getValue() + "\ncause:" + ex.getMessage());
                        }
                    }
                } catch (MD_IOException ex) {
                    throw new IndexingException("MD_IO exception while get values from path", ex);
                }
            }
        }
        if (response.length() == 0) {
            response.append("null");
        } else {
            // we remove the last ','
            response.deleteCharAt(response.length()-1);
        }
        return response.toString();
    }

    protected List<Object> getValuesList(final FullRecord record, final List<String> paths) throws IndexingException {
        final List<Object> response  = new ArrayList<Object>();
        if (paths != null) {
            for (String fullPathID: paths) {
                try {
                    final List<Value> values = getValuesFromPathID(fullPathID, record);
                    for (final Value v: values) {
                        //only handle textvalue
                        if (!(v instanceof TextValue)) {continue;}

                        final TextValue tv = (TextValue) v;
                        try {
                            response.add(getTextValueDescription(tv));
                        } catch (NumberFormatException ex) {
                            LOGGER.warning("Unable to parse value:" + tv.getValue() + "\ncause:" + ex.getMessage());
                        }
                    }
                } catch (MD_IOException ex) {
                    throw new IndexingException("MD_IO exception while get values from path", ex);
                }
            }
        }
        if (response.isEmpty()) {
            response.add("null");
        }
        return response;

    }

    /**
     * Return the String description of An MDWeb textValue :
     * For almost all the value it return TextValue.getValue()
     * but for the value with codeList type it return the label of the codelist element
     * instead of the code.
     *
     * @param tv A TextValue
     *
     * @return S String label
     */
    private Object getTextValueDescription(final TextValue tv) throws NumberFormatException {
        final Object value;
        final Classe valueType   = tv.getType();
        final String stringValue = tv.getValue();
        if (valueType instanceof CodeList) {
            value = getCodeListValue(tv);
        } else if (valueType.getName().equals("Date") && !stringValue.isEmpty()) {
            value = toLuceneDateSyntax(stringValue);
        } else if (valueType.getName().equals("Integer")  && !stringValue.isEmpty()) {
            value = Integer.parseInt(stringValue);
        } else if ((valueType.getName().equals("Real") || valueType.getName().equals("Decimal")) && !stringValue.isEmpty()) {
            value = Double.parseDouble(stringValue);
        } else {
            value = stringValue;
        }
        return value;
    }

    /**
     *  Return a List of MDWeb Value from the specified path.
     *
     * @param fullPathID
     * @param record
     * @return A list of Values.
     *
     * @throws MD_IOException
     */
    public static List<Value> getValuesFromPathID(String fullPathID, final FullRecord record) throws MD_IOException {
        String pathID;
        String conditionalPathID = null;
        String conditionalValue  = null;
        int ordinal              = -1;

        // if the path ID contains a # we have a conditional value (codeList element) next to the searched value.
        final int separator = fullPathID.indexOf('#');
        if (separator != -1) {
            pathID            = fullPathID.substring(0, separator);
            conditionalPathID = fullPathID.substring(0, fullPathID.indexOf('='));
            conditionalPathID = conditionalPathID.replace('#', ':');
            final String temp = fullPathID.substring(fullPathID.indexOf('=') + 1);
            conditionalValue  = temp.substring(0, temp.indexOf(':'));
            pathID            = pathID + temp.substring(temp.indexOf(':'));

            LOGGER.finer("pathID           : " + pathID + '\n'
                       + "conditionalPathID: " + conditionalPathID + '\n'
                       + "conditionalValue : " + conditionalValue);
        } else {
            if (fullPathID.indexOf('[') != -1) {
                final String stringOrdinal = fullPathID.substring(fullPathID.indexOf('[') + 1, fullPathID.indexOf(']'));
                try {
                    ordinal = Integer.parseInt(stringOrdinal);
                    // mdweb ordinal start at 1
                    ordinal++;
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "unable to parse the ordinal:{0}", stringOrdinal);
                    ordinal = -1;
                }
                fullPathID = fullPathID.substring(0, fullPathID.indexOf('['));
            }
            pathID = fullPathID;
        }

        final List<Value> values;
        if (conditionalPathID == null) {
            values = record.getValueFromPath(pathID);
            if (ordinal != -1) {
                final List<Value> toRemove = new ArrayList<Value>();
                for (Value v : values) {
                    if (v.getOrdinal() != ordinal) {
                        toRemove.add(v);
                    }
                }
                values.removeAll(toRemove);
            }

        } else {
            final Value v = record.getConditionalValueFromPath(pathID, conditionalPathID, conditionalValue);
            if (v != null) {
                values = Collections.singletonList(v);
            } else {
                values = new ArrayList<Value>();
            }
        }
        return values;
    }

    /**
     * Return a Date representation in ISO syntax into Lucene date format
     *
     * @param value
     * @return
     */
    private String toLuceneDateSyntax(String value) {
        if (value != null) {
            final Date d = TemporalUtilities.parseDateSafe(value, true);
            synchronized (LUCENE_DATE_FORMAT) {
                value = LUCENE_DATE_FORMAT.format(d);
            }
        }
        return value;
    }

    /**
     * Return the text associated with a codeList textValue.
     *
     * @param tv A TextValue with a type instance of CodeList.
     *
     * @return A text description of the codeList element.
     */
    private String getCodeListValue(TextValue tv) {
        //for a codelist value we don't write the code but the codelistElement value.
        final CodeList cl = (CodeList) tv.getType();
        final String result;

        // we look if the codelist contains locale element.
        final boolean locale = cl.isLocale();

        if (locale) {
            result = tv.getValue();

        } else {
            int code = 1;
            try {
                code = Integer.parseInt(tv.getValue());
            } catch (NumberFormatException ex) {
                // don't log for empty values
                if (!tv.getValue().isEmpty()) {
                    LOGGER.log(Level.WARNING, "NumberFormat Exception while parsing a codelist code: {0}", tv.getValue());
                }
                // return null ?
            }
            final CodeListElement element = cl.getElementByCode(code);

            if (element != null) {
                result = element.getName();
            } else {
                LOGGER.warning("Unable to find a codelistElement for the code: " + code + " in the codelist: " + cl.getName());
                return null;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        try {
            mdWebReader.close();
        } catch (MD_IOException ex) {
            LOGGER.log(Level.WARNING, "MD IO Exception during destroying index while closing MDW reader:{0}", ex.getMessage());
        }
    }
}
