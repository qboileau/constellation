/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.admin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.index.IndexEngine;
import org.constellation.business.IDatasetBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.utils.ISOMarshallerPool;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;

/**
 *
 * Business facade for dataset.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@Component
@Primary
public class DatasetBusiness implements IDatasetBusiness {

    /**
     * w3c document builder factory.
     */
    protected final DocumentBuilderFactory dbf;

    /**
     * Injected dataset repository.
     */
    @Inject
    private DatasetRepository datasetRepository;
    /**
     * Injected data repository.
     */
    @Inject
    private DataRepository dataRepository;
    /**
     * Injected provider repository.
     */
    @Inject
    private ProviderRepository providerRepository;
    /**
     * Injected lucene index engine.
     */
    @Inject
    private IndexEngine indexEngine;

    /**
     * Creates a new instance of {@link DatasetBusiness}.
     */
    public DatasetBusiness() {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }

    /**
     * Get all dataset from dataset table.
     * @return list of {@link Dataset}.
     */
    @Override
    public List<Dataset> getAllDataset() {
        return datasetRepository.findAll();
    }

    /**
     * Get dataset for given identifier and domain id.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId domain id.
     * @return {@link Dataset}.
     */
    @Override
    public Dataset getDataset(final String datasetIdentifier, final int domainId) {
        return datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
    }

    /**
     * Get dataset for given identifier.
     *
     * @param identifier dataset identifier.
     * @return {@link Dataset}.
     */
    @Override
    public Dataset getDataset(final String identifier) {
        return datasetRepository.findByIdentifier(identifier);
    }

    /**
     * Create and insert then returns a new dataset for given parameters.
     * @param identifier dataset identifier.
     * @param metadataId metadata identifier.
     * @param metadataXml metadata content as xml string.
     * @param owner
     * @return {@link Dataset}.
     */
    @Override
    public Dataset createDataset(final String identifier, final String metadataId, final String metadataXml, final Integer owner) {
        final Dataset ds = new Dataset(identifier, metadataId, metadataXml, owner, System.currentTimeMillis(), null);
        return datasetRepository.insert(ds);
    }

    /**
     * Get metadata for given dataset identifier and domain id.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId given domain id.
     * @return {@link org.apache.sis.metadata.iso.DefaultMetadata}.
     * @throws ConfigurationException for JAXBException
     */
    @Override
    public DefaultMetadata getMetadata(final String datasetIdentifier, final int domainId) throws ConfigurationException {
        final Dataset dataset = getDataset(datasetIdentifier, domainId);
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        try {
            final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
            final String metadataStr = dataset.getMetadataIso();
            if(metadataStr == null){
                throw new ConfigurationException("Unable to get metadata for dataset identifier "+datasetIdentifier);
            }
            final byte[] byteArray = metadataStr.getBytes();
            final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            final DefaultMetadata metadata = (DefaultMetadata) unmarshaller.unmarshal(bais);
            pool.recycle(unmarshaller);
            metadata.prune();
            return metadata;
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to unmarshall the dataset metadata", ex);
        }
    }

    /**
     * Returns {@link Node} that represents the metadata document of dataset.
     * @param datasetIdentifier the given dataset identifier.
     * @param domainId domain id.
     * @return {@link Node}
     * @throws ConfigurationException
     */
    @Override
    public Node getMetadataNode(final String datasetIdentifier, int domainId) throws ConfigurationException {
        final Dataset dataset = getDataset(datasetIdentifier, domainId);
        try {
            return getNodeFromString(dataset.getMetadataIso());
        } catch (MetadataIoException ex) {
            throw new ConfigurationException("Unable to get w3c node for metadata of dataset!", ex);
        }
    }

    /**
     * Convert iso metadata string xml to w3c document.
     *
     * @param metadataStr the given metadata xml as string.
     * @return {@link Node} that represents the metadata in w3c document format.
     * @throws MetadataIoException
     */
    protected Node getNodeFromString(final String metadataStr) throws MetadataIoException {
        if(metadataStr == null) {
            return null;
        }
        try {
            final InputSource source = new InputSource(new StringReader(metadataStr));
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.parse(source);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new MetadataIoException("unable to parse the metadata", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Proceed to update metadata for given dataset identifier.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId given domain id.
     * @param metadata metadata as {@link org.apache.sis.metadata.iso.DefaultMetadata} to update.
     * @throws ConfigurationException
     */
    @Override
    public void updateMetadata(final String datasetIdentifier, final Integer domainId,
                               final DefaultMetadata metadata) throws ConfigurationException {
        String metadataString = null;
        try {
            final MarshallerPool pool = ISOMarshallerPool.getInstance();
            final Marshaller marshaller = pool.acquireMarshaller();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            marshaller.marshal(metadata, outputStream);
            metadataString = outputStream.toString();
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to marshall the dataset metadata", ex);
        }
        
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
        if (dataset != null) {
            dataset.setMetadataIso(metadataString);
            dataset.setMetadataId(metadata.getFileIdentifier());
            datasetRepository.update(dataset);
            indexEngine.addMetadataToIndexForDataset(metadata, dataset.getId());
        } else {
            throw new TargetNotFoundException("Dataset :" + datasetIdentifier + " not found");
        }
    }

    /**
     * Proceed to update metadata for given dataset identifier.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId domain id.
     * @param metaId metadata identifier.
     * @param metadataXml metadata as xml string content.
     * @throws ConfigurationException
     */
    public void updateMetadata(final String datasetIdentifier, final Integer domainId,
                               final String metaId, final String metadataXml) throws ConfigurationException {
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
        if (dataset != null) {
            dataset.setMetadataIso(metadataXml);
            dataset.setMetadataId(metaId);
            datasetRepository.update(dataset);
        } else {
            throw new TargetNotFoundException("Dataset :" + datasetIdentifier + " not found");
        }
    }

    /**
     * Proceed to link data to dataset.
     *
     * @param ds given dataset.
     * @param datas given data to link.
     */
    @Override
    public void linkDataTodataset(final Dataset ds, final List<Data> datas) {
        for (final Data data : datas) {
            data.setDatasetId(ds.getId());
            dataRepository.update(data);
        }
    }

    /**
     * Search and returns result as list of {@link Dataset} for given query string.
     * @param queryString the lucene query.
     * @return list of {@link Dataset}
     * @throws org.constellation.admin.exception.ConstellationException
     * @throws IOException
     */
    @Override
    public List<Dataset> searchOnMetadata(final String queryString) throws IOException, ConstellationException {
        final List<Dataset> result = new ArrayList<>();
        final Set<Integer> ids;
        try {
            ids = indexEngine.searchOnMetadata(queryString, "datasetId");
        } catch( ParseException ex) {
            throw new ConstellationException(ex);
        }
        for (final Integer datasetId : ids){
            final Dataset d = datasetRepository.findById(datasetId);
            if(d!=null){
                result.add(d);
            }
        }
        return result;
    }

    @Override
    public void addProviderDataToDataset(final String datasetId, final String providerId) throws ConfigurationException {
        final Dataset ds = datasetRepository.findByIdentifier(datasetId);
        if (ds != null) {
            final Provider p = providerRepository.findByIdentifier(providerId);
            if (p != null) {
                final List<Data> datas = dataRepository.findByProviderId(p.getId());
                for (Data data : datas) {
                    data.setDatasetId(ds.getId());
                    dataRepository.update(data);
                }
            } else {
                throw new TargetNotFoundException("Unable to find a profile: " + providerId);
            }
        } else {
            throw new TargetNotFoundException("Unable to find a dataset: " + datasetId);
        }
    }
}
