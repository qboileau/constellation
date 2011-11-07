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
package org.constellation.ws.embedded;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.ws.rs.core.UriBuilder;
import javax.xml.ws.Endpoint;

import org.geotoolkit.console.CommandLine;
import org.geotoolkit.console.Option;
import org.geotoolkit.util.logging.Logging;


import com.sun.jersey.api.container.grizzly2.servlet.GrizzlyWebContainerFactory;
import com.sun.net.httpserver.HttpContext;
import org.constellation.metadata.ws.soap.CSWService;
import org.constellation.ws.rs.CstlServletContainer;
import org.glassfish.grizzly.http.server.HttpServer;



/**
 * An Abstract class to run the web service in an embedded container.
 * <p>
 * For now, the services are run only using the Grizzly web server. In the 
 * future, we may look into working on the embedded Glassfish system.
 * </p>
 * <p>
 * Classes wishing to run an embedded service should extend this class. The 
 * responsibilities of each extending class is to:
 * <ol>
 *   <li>Call this constructor with the arguments of the main method.
 * <pre>
 *     super(args);
 * </pre>
 *   </li>
 *   <li>For JAX-RS, REST based services, add parameters to the configuration 
 *       {@code Map}.
 * <pre>
 *     map.put("com.sun.jersey.config.property.packages", "org.constellation.map.ws.rs");
 *     map.put("com.sun.jersey.config.property.packages", "org.constellation.coverage.ws.rs");
 *     map.put(TODO: get providers line from Cedric);
 * </pre>
 *   </li>
 *   <li>For JAX-WS, SOAP based services, set the reference of the Service 
 *       Endpoint Interface.
 * <pre>
 *     serviceInstanceSOAP = new org.constellation.sos.ws.soap.SOSService();
 * </pre>
 *   </li>
 *   <li>Add a simple main, such as:
 * <pre>
 *    public static void main(String[] args) {
 *	    
 *        EXTENDOR sei = new EXTENDOR(args);
 *        if (useFacadeREST){
 *            sei.runREST();
 *        } else {
 *            sei.runSOAP();
 *        }
 *        
 *        System.exit(0);
 *        
 *    }
 * </pre>
 *   </li>
 * </ol>
 * </p>
 * 
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public class CstlEmbeddedService extends CommandLine {

    /**
     * Logger for this service.
     */
    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.embedded");
    // THESE ARE INJECTED BY THE CommandLine CLASS
    //   TODO: these default values clobber main's args; fixed in Geotidy
    @Option
    protected Boolean useFacadeREST = true;       //Default value
    @Option
    protected String host = "localhost";//Default value
    @Option
    protected Integer port = 9090;       //Default value
    @Option
    protected Integer duration = 20 * 60 * 1000; //minutes*seconds*millseconds; set to <=0 to last until <enter>

    final URI uri;
    final DateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    /* ***********************************************************************
     *   CONCRETE CLASSES MUST: (see below)
     * ********************************************************************** */
    //FOR REST, COMPLETE THIS PARAMETER MAP
    //  Grizzly is used by the REST service. Extending classes need to add the
    //  package(s) containing the service(s) they desire to run and the
    //  providers on which those services depend, e.g. :
    //      map.put("com.sun.jersey.config.property.packages", "org.constellation.map.ws.rs");
    //      map.put("com.sun.jersey.config.property.packages", "org.constellation.coverage.ws.rs");
    //      map.put(TODO: get providers line from Cedric);
    //  below we add in one of the properties needed by all services.
    protected Map<String, String> grizzlyWebContainerProperties = new HashMap<String, String>();
    //FOR SOAP, DEFINE THIS REFERENCE:
    protected Object serviceInstanceSOAP = null;

    //INCLUDE THIS MAIN.
//	public static void main(String[] args) {
//		
//		EXTENDOR sei = new EXTENDOR(args);
//	    if (useFacadeREST){
//		    sei.runREST();
//      } else {
//          sei.runSOAP();
//      }
//		
//		System.exit(0);
//
//	}

    /* ***********************************************************************
     *   CONCRETE CLASSES MUST HAVE: (see above)
     * ********************************************************************** */
    /**
     * Constructor which passes the arguments for processing to the
     * CommandLine parent and sets the URI.
     * By default, only start the WMS and WCS services.
     * <p>
     * Extending classes using the REST facade should
     */
    protected CstlEmbeddedService(final String[] args) {
        this(args, new String[] {
            "org.constellation.map.ws.rs",
            "org.constellation.coverage.ws.rs",
            "org.constellation.wfs.ws.rs",
            "org.constellation.sos.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.metadata.ws.rs",
            "org.constellation.metadata.ws.rs.provider",
            "org.constellation.ws.rs.provider"
        });
    }

    /**
     * Constructor which passes the arguments for processing to the
     * CommandLine parent and sets the URI.
     * <p>
     * Extending classes using the REST facade should
     *
     * @param args The command line arguments.
     * @param providerPackages The packages for providers to start.
     */
    protected CstlEmbeddedService(final String[] args, final String[] providerPackages) {
        super(null, args);

        final StringBuilder sb = new StringBuilder();
        final int length = providerPackages.length;
        for (int i=0; i<length; i++) {
            final String pack = providerPackages[i];
            sb.append(pack);
            if (i != length - 1) {
                sb.append(';');
            }
        }
        grizzlyWebContainerProperties.put("com.sun.jersey.config.property.packages", sb.toString());

        final String base = "http://" + host + "/";
        uri = UriBuilder.fromUri(base).port(port).build();

    }

    /**
     * Should be called by the {@code main()} method for any web service wishing
     * to implement a JAX-RS REST facade.
     */
    protected void runREST() {

        grizzlyWebContainerProperties.put("com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.PackagesResourceConfig");

        LOGGER.log(Level.INFO, "Starting grizzly server at: {0}", f.format(new Date()));

        HttpServer threadSelector = null;
        try {
            if (grizzlyWebContainerProperties.isEmpty()) {
                threadSelector = GrizzlyWebContainerFactory.create(uri, CstlServletContainer.class);
            } else {
                threadSelector = GrizzlyWebContainerFactory.create(uri, CstlServletContainer.class, grizzlyWebContainerProperties);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        LOGGER.log(Level.INFO, "Started Grizzly application server for: {0}", uri);
        LOGGER.log(Level.INFO, "The service definition file can be found at: {0}application.wadl", uri);

        stayAlive();

        threadSelector.stop();
        LOGGER.log(Level.INFO, "*Stopped grizzly server at: {0}.", f.format(new Date()));
    }

    /**
     * Should be called by the {@code main()} method for any web service wishing
     * to implement a JAX-WS SOAP facade.
     */
    protected void runSOAP() {

        if (null == serviceInstanceSOAP) {
            LOGGER.info("The Service Endpoint Instance was never defined.");
            System.exit(0);
        }


        LOGGER.log(Level.INFO, "Starting jax-ws server at: {0}", f.format(new Date()));

        final String service = uri.toString() + "sos";
        Endpoint ep =  Endpoint.create(serviceInstanceSOAP);

        ep = Endpoint.publish(service, serviceInstanceSOAP);

        LOGGER.log(Level.INFO, "Started jax-ws application server for: {0}", service);
        LOGGER.log(Level.INFO, "The service definition file can be found at: {0}?wsdl", service);

        stayAlive();

        ep.stop();
        LOGGER.log(Level.INFO, "*Stopped jax-ws server at: {0}.", f.format(new Date()));
    }

    /**
     * Will keep either of the services alive either for the number of
     * milliseconds defined in the command-line parameter {@code duration} if
     * that value is greater than zero or until the user presses the
     * {@code <ENTER>} (or {@code <RETURN>}) key of the keyboard depending if
     * the duration time is greater than zero.
     *
     */
    protected void stayAlive() {

        if (duration > 0) {
            //Survive 'duration' milliseconds
            LOGGER.info("  Service will stop in " + duration / (60 * 1000) + " minutes.");
            try {
                Thread.sleep(duration);
            } catch (InterruptedException iex) {
                LOGGER.fine("The grizzly thread has received an interrupted request.");
                LOGGER.log(Level.INFO, "*Stopped grizzly server at: {0}.", f.format(new Date()));
            }
        } else {
            //Listen and wait for <ENTER>
            LOGGER.info("  Hit <ENTER> to stop the service.");
            try {
                System.in.read();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) {
        new CstlEmbeddedService(args).runSOAP();
    }
}
