/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.ws.rest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.DataProviderFactory;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 * RestFull API for provider management/operations.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Path("/1/provider")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class Provider {

    private static final Logger LOGGER = Logging.getLogger(Provider.class);

    /**
     * Create a new provider from the given configuration.
     */
    @PUT
    @Path("{id}")
    public Response create(final @PathParam("id") String id, final ProviderConfiguration config) {
        final String type = config.getType();
        final String subType = config.getSubType();
        final Map<String,String> inParams = config.getParameters();

        final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
        final ParameterDescriptorGroup serviceDesc = providerService.getServiceDescriptor();
        final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup) serviceDesc.descriptor("source");
        final ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(id);
        sources.parameter("providerType").setValue(type);
        String folderPath;

        switch (type) {
            case "sld":
                final String sldPath = inParams.get("path");
                folderPath = sldPath.substring(0, sldPath.lastIndexOf('/'));
                sources.groups("sldFolder").get(0).parameter("path").setValue(folderPath);
                break;
            case "feature-store":
                switch (subType) {
                    case "shapefile":
                        try {
                            final String shpPath = inParams.get("path");
                            final URL url = new URL("file:" + shpPath);
                            final ParameterValueGroup shpFolderParams = sources.groups("choice").get(0).addGroup("ShapeFileParametersFolder");
                            shpFolderParams.parameter("url").setValue(url);
                            shpFolderParams.parameter("namespace").setValue("no namespace");
                        } catch (MalformedURLException e) {
                            LOGGER.log(Level.WARNING, "unable to create url from path", e);
                        }
                        break;
                    default:
                        final ParameterValueGroup pgParams = sources.groups("choice").get(0).addGroup("PostgresParameters");
                        final int port = Integer.parseInt(inParams.get("port"));
                        pgParams.parameter("identifier").setValue("postgresql");
                        pgParams.parameter("host").setValue(inParams.get("host"));
                        pgParams.parameter("port").setValue(port);
                        pgParams.parameter("user").setValue(inParams.get("user"));
                        pgParams.parameter("password").setValue(inParams.get("password"));
                        pgParams.parameter("database").setValue(inParams.get("database"));
                        pgParams.parameter("simple types").setValue(true);
                }
                break;
            case "coverage-store":
                URL fileUrl = null;

                switch (subType) {
                    case "coverage-xml-pyramid":
                        try {
                            final String pyramidPath = inParams.get("path");
                            fileUrl = URI.create(pyramidPath).toURL();
                        } catch (MalformedURLException e) {
                            LOGGER.log(Level.WARNING, "unable to create url from path", e);
                        }
                        final ParameterValueGroup xmlCovParams = sources.groups("choice").get(0).addGroup("XMLCoverageStoreParameters");
                        xmlCovParams.parameter("identifier").setValue("coverage-xml-pyramid");
                        xmlCovParams.parameter("path").setValue(fileUrl);
                        xmlCovParams.parameter("type").setValue("AUTO");
                        break;
                    case "coverage-file":
                        try {
                            final String covPath = inParams.get("path");
                            fileUrl = URI.create("file:"+covPath).toURL();
                        } catch (MalformedURLException e) {
                            LOGGER.log(Level.WARNING, "unable to create url from path", e);
                        }

                        final ParameterValueGroup fileCovParams = sources.groups("choice").get(0).addGroup("FileCoverageStoreParameters");
                        fileCovParams.parameter("identifier").setValue("coverage-file");
                        fileCovParams.parameter("path").setValue(fileUrl);
                        fileCovParams.parameter("type").setValue("AUTO");
                        fileCovParams.parameter("namespace").setValue("no namespace");
                        break;
                    case "pgraster":
                        final ParameterValueGroup pgRasterParams = sources.groups("choice").get(0).addGroup("PGRasterParameters");
                        final int port = Integer.parseInt(inParams.get("port"));
                        pgRasterParams.parameter("identifier").setValue("postgresql");
                        pgRasterParams.parameter("host").setValue(inParams.get("host"));
                        pgRasterParams.parameter("port").setValue(port);
                        pgRasterParams.parameter("user").setValue(inParams.get("user"));
                        pgRasterParams.parameter("password").setValue(inParams.get("password"));
                        pgRasterParams.parameter("database").setValue(inParams.get("database"));
                        pgRasterParams.parameter("simple types").setValue(true);
                        break;
                    default:
                        LOGGER.log(Level.WARNING, "error on subtype definition");
                }
                break;
            default:
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Provider type not known");
                }
        }

        final DataProvider old = DataProviders.getInstance().getProvider(id);
        if (old != null) {
            // Provider already exists, update config
            old.updateSource(sources);
        } else {
            DataProviders.getInstance().createProvider(providerService, sources);
        }
        return Response.ok().build();
    }

    /**
     * Delete a provider with the given id.
     */
    @DELETE
    @Path("{id}")
    public Response delete(final @PathParam("id") String id) {
        DataProviders.getInstance().remove(new DefaultName(id));
        return Response.status(200).build();
    }

    /**
     * @see LayerProviders#getDataDescription(String, String)
     */
    @GET
    @Path("{id}/{layerName}/dataDescription")
    public Response dataDescription(final @PathParam("id") String id,
                                    final @PathParam("layerName") String layerName) {
        try {
            return Response.ok(LayerProviders.getDataDescription(id, layerName)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    /**
     * @see LayerProviders#getPropertyValues(String, String, String)
     */
    @GET
    @Path("{id}/{layerName}/{property}/propertyValues")
    public Response propertyValues(final @PathParam("id") String id,
                                   final @PathParam("layerName") String layerName,
                                   final @PathParam("property") String property) {
        try {
            return Response.ok(LayerProviders.getPropertyValues(id, layerName, property)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    /**
     * @see LayerProviders#getBandValues(String, String, int)
     */
    @GET
    @Path("{id}/{layerName}/{bandIndex}/bandValues")
    public Response bandValues(final @PathParam("id") String id,
                               final @PathParam("layerName") String layerName,
                               final @PathParam("bandIndex") int bandIndex) {
        try {
            return Response.ok(LayerProviders.getBandValues(id, layerName, bandIndex)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    @GET
    @Path("metadata/{providerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetadata(final @PathParam("providerId") String providerId) throws SQLException, NotRunningServiceException, CoverageStoreException, NoSuchIdentifierException, ProcessException, JAXBException {
        final DefaultMetadata metadata = ConfigurationEngine.loadProviderMetadata(providerId, CSWMarshallerPool.getInstance());
        metadata.prune();
        return Response.ok(metadata).build();
    }

    @POST
    @Path("metadata/{providerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setMetadata(final @PathParam("providerId") String providerId, final DefaultMetadata metadata) throws SQLException, NotRunningServiceException, CoverageStoreException, NoSuchIdentifierException, ProcessException, JAXBException {
        ConfigurationEngine.saveMetaData(metadata, providerId, CSWMarshallerPool.getInstance());
        return Response.status(200).build();
    }
}
