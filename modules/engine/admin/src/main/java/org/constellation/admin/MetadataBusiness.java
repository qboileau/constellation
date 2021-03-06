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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.XML;

import org.constellation.business.IMetadataBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Metadata;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.MetadataRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.json.metadata.v2.Template;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business facade for metadata.
 *
 * @author guilhem
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@Component("cstlMetadataBusiness")
@Primary
public class MetadataBusiness implements IMetadataBusiness {

    protected static final Logger LOGGER = Logging.getLogger(MetadataBusiness.class);
    
    /**
     * Injected data repository.
     */
    @Inject
    private DataRepository dataRepository;
    /**
     * Injected dataset repository.
     */
    @Inject
    private DatasetRepository datasetRepository;
    /**
     * Injected service repository.
     */
    @Inject
    private ServiceRepository serviceRepository;
    /**
     * Injected metadata repository.
     */
    @Inject
    protected MetadataRepository metadataRepository;
    
    /**
     * Returns the xml as string representation of metadata for given metadata identifier.
     *
     * @param metadataId given metadata identifier
     * @param includeService flag that indicates if service repository will be requested.
     * @return String representation of metadata in xml.
     */
    @Override
    public String searchMetadata(final String metadataId, final boolean includeService)  {
        final Metadata metadata = metadataRepository.findByMetadataId(metadataId);
        if (metadata != null) {
            if (!includeService && metadata.getServiceId() != null) {
                return null;
            }
            return metadata.getMetadataIso();
        }
        return null;
    }
    
    @Override
    @Transactional
    public boolean updateMetadata(final String metadataId, final String xml)  {
        final Metadata metadata = metadataRepository.findByMetadataId(metadataId);
        if (metadata != null) {
            metadata.setMetadataId(metadataId);
            metadata.setMetadataIso(xml);
            
            // calculate completion rating
            Integer completion = null;
            if (metadata.getDatasetId() != null) {
                final Dataset dataset = datasetRepository.findById(metadata.getDatasetId()); // calculate completion rating
                try {
                    List<Data> datas = dataRepository.findByDatasetId(dataset.getId());
                    if (!datas.isEmpty()) {
                        final String type = datas.get(0).getType();
                        final Template template = Template.getInstance(getDatasetTemplate(dataset.getIdentifier(), type));
                        completion = template.calculateMDCompletion(unmarshallMetadata(xml));
                    }
                } catch (IOException | ConfigurationException | JAXBException ex) {
                    LOGGER.log(Level.WARNING, "Error while calculating metadata completion", ex);
                }
            }
            if (metadata.getDataId() != null) {
                final Data data = dataRepository.findById(metadata.getDataId());
                try {
                    final Template template = Template.getInstance(getDataTemplate(data.getName(), data.getNamespace(), data.getType()));
                    completion = template.calculateMDCompletion(unmarshallMetadata(xml));
                } catch (IOException | ConfigurationException | JAXBException ex) {
                    LOGGER.log(Level.WARNING, "Error while calculating metadata completion", ex);
                }
            }
            metadata.setMdCompletion(completion);
            metadataRepository.update(metadata);
            return true;
        }

        
        // if the metadata is not yet present look for empty metadata object
        final Dataset dataset = datasetRepository.findByIdentifierWithEmptyMetadata(metadataId);
        if (dataset != null) {
            final Metadata metadata2 = new Metadata(metadataId, xml, null, dataset.getId(), null, null);
            Integer completion = null;
            try {
                List<Data> datas = dataRepository.findByDatasetId(dataset.getId());
                if (!datas.isEmpty()) {
                    final String type = datas.get(0).getType();
                    final Template template = Template.getInstance(getDatasetTemplate(dataset.getIdentifier(), type));
                    completion = template.calculateMDCompletion(unmarshallMetadata(xml));
                }
            } catch (IOException | ConfigurationException | JAXBException ex) {
                LOGGER.log(Level.WARNING, "Error while calculating metadata completion", ex);
            }
            metadata2.setMdCompletion(completion);
            metadataRepository.create(metadata2);
            return true;
        }
        // unsafe but no better way for now
        final Data data = dataRepository.findByIdentifierWithEmptyMetadata(metadataId);
        if (data != null) {
            final Metadata metadata2 = new Metadata(metadataId, xml, data.getId(), null, null, null);
            // calculate completion rating
            Integer completion = null;
            try {
                final Template template = Template.getInstance(getDataTemplate(data.getName(), data.getNamespace(), data.getType()));
                completion = template.calculateMDCompletion(unmarshallMetadata(xml));
            } catch (IOException | ConfigurationException | JAXBException ex) {
                LOGGER.log(Level.WARNING, "Error while calculating metadata completion", ex);
            }
            metadata2.setMdCompletion(completion);
            metadataRepository.create(metadata2);
            return true;
        }
        
        // save a new metadata (unliked to any data/dataset/service)
        final Metadata metadata2 = new Metadata(metadataId, xml, null, null, null, null);
        metadataRepository.create(metadata2);
        return true;
    }

    /**
     * Returns {@code true} if the xml metadata exists for given metadata identifier.
     *
     * @param metadataID given metadata identifier.
     * @param includeService flag that indicates if service repository will be requested.
     * @return boolean to indicates if metadata is present or not.
     */
    @Override
    public boolean existInternalMetadata(final String metadataID, final boolean includeService) {
        return searchMetadata(metadataID, includeService) != null;
    }

    /**
     * Returns a list of all metadata identifiers.
     *
     * @param includeService flag that indicates if service repository will be requested.
     * @return List of string identifiers.
     */
    @Override
    public List<String> getInternalMetadataIds(final boolean includeService) {
        final List<String> results = new ArrayList<>();
        final List<Metadata> metadatas = metadataRepository.findAll();
        for (final Metadata record : metadatas) {
            if (record.getServiceId() != null) {
                if (includeService) {
                    results.add(record.getMetadataId());
                }
            } else {
                results.add(record.getMetadataId());
            }
        }
        return results;
    }

    /**
     * Returns all metadata stored in database.
     *
     * @param includeService given flag to include service's metadata
     * @return List of all metadata as string xml stored in database.
     */
    @Override
    public List<String> getAllMetadata(final boolean includeService) {
        final List<String> results = new ArrayList<>();
        final List<Metadata> metadatas = metadataRepository.findAll();
        for (final Metadata record : metadatas) {
            if (record.getServiceId() != null) {
                if (includeService) {
                    results.add(record.getMetadataIso());
                }
            } else {
                results.add(record.getMetadataIso());
            }
        }
        return results;
    }
    
    @Override
    public List<String> getLinkedMetadataIDs(final String cswIdentifier) {
        final List<String> results = new ArrayList<>();
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            List<Metadata> metas = metadataRepository.findByCswId(service.getId());
            for (Metadata meta : metas) {
                results.add(meta.getMetadataId());
            }
        }
        return results;
    }
    
    @Override
    @Transactional
    public void linkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            metadataRepository.addMetadataToCSW(metadataId, service.getId());
        }
    }
    
    @Override
    @Transactional
    public void unlinkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            metadataRepository.removeDataFromCSW(metadataId, service.getId());
        }
    }
    
    protected Object unmarshallMetadata(final String metadata) throws JAXBException {
        return XML.unmarshal(metadata);
    }
    
    protected String getDataTemplate(final String dataName, final String dataNamespace, final String dataType) throws ConfigurationException {
        final String templateName;
        if ("vector".equalsIgnoreCase(dataType)) {
            //vector template
            templateName = "profile_default_vector";
        } else if ("raster".equalsIgnoreCase(dataType)) {
            //raster template
            templateName = "profile_default_raster";
        } else {
            //default template is import
            templateName = "profile_import";
        }
        return templateName;
    }
    
    protected String getDatasetTemplate(final String datasetId, final String dataType) throws ConfigurationException {
        //get template name
        final String templateName;
        if ("vector".equalsIgnoreCase(dataType)) {
            //vector template
            templateName = "profile_default_vector";
        } else if ("raster".equalsIgnoreCase(dataType)) {
            //raster template
            templateName = "profile_default_raster";
        } else {
            //default template is import
            templateName = "profile_import";
        }
        return templateName;
    }
}
