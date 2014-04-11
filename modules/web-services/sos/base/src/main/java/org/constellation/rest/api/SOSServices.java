/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.rest.api;

import java.io.File;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.constellation.ServiceDef;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.dto.SimpleValue;
import org.constellation.sos.configuration.SOSConfigurer;
import static org.constellation.utils.RESTfulUtilities.ok;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Path("/1/SOS")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SOSServices {
    
    
    @PUT
    @Path("{id}/sensors")
    public Response importSensor(final @PathParam("id") String id, final @PathParam("fileName") String fileName, final File sensor) throws Exception {
        return ok(getConfigurer().importSensor(id, sensor, "xml"));
    }
    
    @DELETE
    @Path("{id}/sensor/{sensorID}")
    public Response removeSensor(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
        return ok(getConfigurer().removeSensor(id, sensorID));
    }

    @GET
    @Path("{id}/sensor/{sensorID}")
    public Response getSensor(final @PathParam("id") String id, final @PathParam("sensorID") String sensorID) throws Exception {
        return ok(getConfigurer().getSensor(id, sensorID));
    }
    
    @GET
    @Path("{id}/sensors/count")
    public Response getSensortCount(final @PathParam("id") String id) throws Exception {
        return ok(new SimpleValue(getConfigurer().getSensorCount(id)));
    }
    
    private static SOSConfigurer getConfigurer() throws NotRunningServiceException {
        return (SOSConfigurer) ServiceConfigurer.newInstance(ServiceDef.Specification.SOS);
    }
}
