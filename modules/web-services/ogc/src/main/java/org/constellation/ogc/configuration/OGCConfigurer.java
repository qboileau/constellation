/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.ogc.configuration;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Instance;
import org.constellation.configuration.NoSuchInstanceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.configuration.ServiceStatus;
import org.constellation.dto.Service;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.CreateServiceDescriptor;
import org.constellation.process.service.DeleteServiceDescriptor;
import org.constellation.process.service.GetConfigServiceDescriptor;
import org.constellation.process.service.RenameServiceDescriptor;
import org.constellation.process.service.RestartServiceDescriptor;
import org.constellation.process.service.SetConfigServiceDescriptor;
import org.constellation.process.service.StartServiceDescriptor;
import org.constellation.process.service.StopServiceDescriptor;
import org.constellation.utils.MetadataUtilities;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describe methods which need to be specify by an implementation to manage
 * service (create, set configuration, etc...).
 *
 * @author Benjamin Garcia (Geomatys).
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public abstract class OGCConfigurer extends ServiceConfigurer {

    /**
     * Create a new {@link OGCConfigurer} instance.
     *
     * @param specification  the target service specification
     * @param configClass    the target service config class
     * @param configFileName the target service config file name
     */
    protected OGCConfigurer(final Specification specification, final Class configClass, final String configFileName) {
        super(specification, configClass, configFileName);
    }

    /**
     * Creates a new service instance.
     *
     * @param metadata      the service metadata (can be null)
     * @param configuration the service configuration (can be null)
     * @throws ProcessException if the process used to perform action has failed
     */
    public void createInstance(final String identifier, final Service metadata, final Object configuration) throws ProcessException {
        final ProcessDescriptor desc = getProcessDescriptor(CreateServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        inputs.parameter(CreateServiceDescriptor.CONFIG_NAME).setValue(configuration);
        inputs.parameter(CreateServiceDescriptor.SERVICE_METADATA_NAME).setValue(metadata);
        inputs.parameter(CreateServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(configClass);
        inputs.parameter(CreateServiceDescriptor.FILENAME_NAME).setValue(configFileName);
        desc.createProcess(inputs).call();
    }

    /**
     * Starts a service instance.
     *
     * @param identifier the service identifier
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void startInstance(final String identifier) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        final ProcessDescriptor desc = getProcessDescriptor(StartServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        inputs.parameter(StartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory());
        desc.createProcess(inputs).call();
    }

    /**
     * Stops a service instance.
     *
     * @param identifier the service identifier
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void stopInstance(final String identifier) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        final ProcessDescriptor desc = getProcessDescriptor(StopServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(StopServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(StopServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        desc.createProcess(inputs).call();
    }

    /**
     * Restarts a service instance.
     *
     * @param identifier the service identifier
     * @param closeFirst indicates if the service should be closed before trying to restart it
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void restartInstance(final String identifier, final boolean closeFirst) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        final ProcessDescriptor desc = getProcessDescriptor(RestartServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        inputs.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(closeFirst);
        inputs.parameter(RestartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory());
        desc.createProcess(inputs).call();
    }

    /**
     * Renames a service instance.
     *
     * @param identifier    the current service identifier
     * @param newIdentifier the new service identifier
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void renameInstance(final String identifier, final String newIdentifier) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        final ProcessDescriptor desc = getProcessDescriptor(RenameServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(RenameServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(RenameServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        inputs.parameter(RenameServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory());
        inputs.parameter(RenameServiceDescriptor.NEW_NAME_NAME).setValue(newIdentifier);
        desc.createProcess(inputs).call();
    }

    /**
     * Deletes a service instance.
     *
     * @param identifier the service identifier
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void deleteInstance(final String identifier) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        final ProcessDescriptor desc = getProcessDescriptor(DeleteServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(DeleteServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(DeleteServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        inputs.parameter(DeleteServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory());
        desc.createProcess(inputs).call();
    }

    /**
     * Configures a service instance.
     *
     * @param identifier    the service identifier
     * @param configuration the service configuration (depending on implementation)
     * @param metadata      the service metadata
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void configureInstance(final String identifier, final Service metadata, final Object configuration) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        final ProcessDescriptor desc = getProcessDescriptor(SetConfigServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(SetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(SetConfigServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        inputs.parameter(SetConfigServiceDescriptor.CONFIG_NAME).setValue(configuration);
        inputs.parameter(SetConfigServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(getInstanceDirectory(identifier));
        inputs.parameter(SetConfigServiceDescriptor.SERVICE_METADATA_NAME).setValue(metadata);
        inputs.parameter(SetConfigServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(configClass);
        inputs.parameter(SetConfigServiceDescriptor.FILENAME_NAME).setValue(configFileName);
        desc.createProcess(inputs).call();

        if (metadata != null && !identifier.equals(metadata.getIdentifier())) { // rename if necessary
            renameInstance(identifier, metadata.getIdentifier());
        }
    }

    /**
     * Returns the configuration object of a service instance.
     *
     * @param identifier the service
     * @return a configuration {@link Object} (depending on implementation)
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public Object getInstanceConfiguration(final String identifier) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        final ProcessDescriptor desc = getProcessDescriptor(GetConfigServiceDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(GetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(specification.name());
        inputs.parameter(GetConfigServiceDescriptor.IDENTIFIER_NAME).setValue(identifier);
        inputs.parameter(GetConfigServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(getInstanceDirectory(identifier));
        inputs.parameter(SetConfigServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(configClass);
        inputs.parameter(GetConfigServiceDescriptor.FILENAME_NAME).setValue(configFileName);

        final ParameterValueGroup outputs = desc.createProcess(inputs).call();
        return outputs.parameter(GetConfigServiceDescriptor.CONFIG_NAME).getValue();
    }

    /**
     * Updates a service instance configuration object.
     *
     * @param identifier    the service identifier
     * @param configuration the service configuration
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void setInstanceConfiguration(final String identifier, final Object configuration) throws NoSuchInstanceException, ProcessException, IOException {
        this.ensureExistingInstance(identifier);
        this.configureInstance(identifier, getInstanceMetadata(identifier), configuration);
    }

    /**
     * Returns a service instance metadata.
     *
     * TODO: use a process and remove IOException
     *
     * @param identifier the service identifier
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws IOException if failed to read the metadata file
     */
    public Service getInstanceMetadata(final String identifier) throws NoSuchInstanceException, IOException {
        this.ensureExistingInstance(identifier);
        return MetadataUtilities.readMetadata(getInstanceDirectory(identifier));
    }

    /**
     * Updates a service instance metadata.
     *
     * @param identifier the service identifier
     * @param metadata   the service metadata
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void setInstanceMetadata(final String identifier, final Service metadata) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);
        this.configureInstance(identifier, metadata, getInstanceConfiguration(identifier));
    }

    /**
     * Find and returns a service {@link Instance}.
     *
     * @param identifier the service identifier
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @return an {@link Instance} instance
     */
    public Instance getInstance(final String identifier) throws NoSuchInstanceException {
        final Instance instance = new Instance(identifier, specification.name(), getInstanceStatus(identifier));
        Service metadata = null;
        try {
            metadata = getInstanceMetadata(identifier);
        } catch (IOException ignore) {
        }
        if (metadata != null) {
            instance.set_abstract(metadata.getDescription());
            instance.setVersions(metadata.getVersions());
        } else {
            instance.set_abstract("");
            instance.setVersions(new ArrayList<String>());
        }
        return instance;
    }

    /**
     * Returns a service instance status.
     *
     * @param identifier the service identifier
     * @return a {@link ServiceStatus} status
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     */
    public ServiceStatus getInstanceStatus(final String identifier) throws NoSuchInstanceException {
        this.ensureExistingInstance(identifier);
        for (Map.Entry<String, Boolean> entry : WSEngine.getEntriesStatus(specification.name())) {
            if (entry.getKey().equals(identifier)) {
                return entry.getValue() ? ServiceStatus.WORKING : ServiceStatus.ERROR;
            }
        }
        return ServiceStatus.NOT_STARTED;
    }

    /**
     * Returns all service instances (for current specification) status.
     *
     * @return a {@link Map} of {@link ServiceStatus} status
     */
    public Map<String, ServiceStatus> getInstancesStatus() {
        final Map<String, ServiceStatus> status = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : WSEngine.getEntriesStatus(specification.name())) {
            status.put(entry.getKey(), entry.getValue() ? ServiceStatus.WORKING : ServiceStatus.ERROR);
        }
        final File[] files = getServiceDirectory().listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isDirectory() && !file.getName().startsWith(".")
                        && !WSEngine.serviceInstanceExist(specification.name(), file.getName());
            }
        });
        if (files != null) {
            for (final File directory : files) {
                status.put(directory.getName(), ServiceStatus.NOT_STARTED);
            }
        }
        return status;
    }

    /**
     * Returns list of service {@link Instance}(s) related to the {@link OGCConfigurer}
     * implementation.
     *
     * @return the {@link Instance} list
     */
    public List<Instance> getInstances() {
        final List<Instance> instances = new ArrayList<>();
        final Map<String, ServiceStatus> statusMap = getInstancesStatus();
        for (final Map.Entry<String, ServiceStatus> entry : statusMap.entrySet()) {
            final Instance instance = new Instance(entry.getKey(), specification.name(), entry.getValue());
            Service metadata = null;
            try {
                metadata = getInstanceMetadata(entry.getKey());
            } catch (NoSuchInstanceException | IOException ignore) {
            }
            if (metadata != null) {
                instance.set_abstract(metadata.getDescription());
                instance.setVersions(metadata.getVersions());
            } else {
                instance.set_abstract("");
                instance.setVersions(new ArrayList<String>());
            }
            instances.add(instance);
        }
        return instances;
    }

    /**
     * Returns a Constellation {@link ProcessDescriptor} from its name.
     *
     * @param name the process descriptor name
     * @return a {@link ProcessDescriptor} instance
     */
    protected ProcessDescriptor getProcessDescriptor(final String name) {
        try {
            return ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, name);
        } catch (NoSuchIdentifierException ex) { // unexpected
            throw new IllegalStateException("Unexpected error has occurred", ex);
        }
    }
}