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
package org.constellation.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import org.geotoolkit.util.FileUtilities;
import org.apache.sis.util.logging.Logging;

/**
 * Temporary copy of static methods from the WebService class (in module web-base),
 * in order to retrieve the configuration directory of Constellation.
 *
 * TODO: this implementation should probably been handled by the server registry, so
 *       move it there.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public final class ConfigDirectory {

    private ConfigDirectory() {}
    
    /**
     * The default debugging logger.
     */
    private static final Logger LOGGER = Logging.getLogger("org.constellation.provider.configuration");

    /**
     * Specifies if the process is running on a Glassfish application server.
     */
    private static Boolean runningOnGlassfish = null;

    /**
     * The user directory where configuration files are stored on Unix platforms.
     * TODO: How does this relate to the directories used in deployment? This is
     *       in the home directory of the user running the container?
     */
    private static final String UNIX_DIRECTORY = ".constellation";

    /**
     * The user directory where configuration files are stored on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Constellation";

    /**
     * The user directory where configuration files are stored.
     * this variable is fill by the user in the JSF interface.
     */
    public static String USER_DIRECTORY = null;

    public static String DATA_DIRECTORY = null;

    /**
     * This should be a class loader from the main constellation application.
     */
    private static final ClassLoader baseClassLoader;

    //we try to load this variable at the start by reading a properties file
    static {
        baseClassLoader = Thread.currentThread().getContextClassLoader();
        final File webInfDirectory = getWebInfDiretory();
        File propertiesFile = new File(webInfDirectory, "constellation.properties");
        if (propertiesFile != null && propertiesFile.exists()) {
            try {
                Properties prop = FileUtilities.getPropertiesFromFile(propertiesFile);
                USER_DIRECTORY = prop.getProperty("configuration_directory");
                DATA_DIRECTORY = prop.getProperty("data_directory");
            } catch (IOException ex) {
                LOGGER.warning("IOException while reading the constellation properties file");
            }
        } 
    }

    private static File getWebInfDiretory() {
        final URL url = baseClassLoader.getResource("org/constellation/configuration/ConfigDirectory.class");
        String path = url.toString();
        path = path.substring(path.lastIndexOf(':') + 1); // we remove the file type
        final int separator = path.indexOf('!'); // we remove the path inside the jar
        if (separator != -1) {
            path = path.substring(0, separator);
        }
        File f = new File(path);
        f = f.getParentFile(); // lib
        f = f.getParentFile(); // WEB-INF
        return f;
    }

    /**
     * Give a data directory {@link java.io.File} defined on constellation.properties or
     * by default on .constellation-data from user home directory
     *
     * @return data directory as {@link java.io.File}
     */
    public static File getDataDirectory() {
        File constellationDataDirectory = null;

        if (DATA_DIRECTORY != null && !DATA_DIRECTORY.isEmpty()) {
            constellationDataDirectory = new File(DATA_DIRECTORY);
            if (!constellationDataDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", DATA_DIRECTORY);
            } else if (!constellationDataDirectory.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", DATA_DIRECTORY);
            }
        } else {
            constellationDataDirectory = new File(System.getProperty("user.home"), ".constellation-data");
            if(!constellationDataDirectory.exists()){
                constellationDataDirectory.mkdir();
            }
        }
        return constellationDataDirectory;
    }

    /**
     * Return the configuration directory.
     * 
     * priority is :
     *  1) packaged war file
     *  2) user defined directory
     *  3) .constellation in home directory
     */
    public static File getConfigDirectory() {
        File constellationDirectory;

        /*
         * 1) WAR packaged config located in WEB-INF
         */
         final File webInfDirectory = getWebInfDiretory();

         constellationDirectory = new File(webInfDirectory, "constellation");
         if (constellationDirectory != null && constellationDirectory.isDirectory()) {
             return constellationDirectory;
         }

        /*
         * 2) resource packaged config
         */
         constellationDirectory = FileUtilities.getDirectoryFromResource("constellation");
         if (constellationDirectory != null && constellationDirectory.isDirectory()) {
            return constellationDirectory;
         }

        /*
         * 3) user defined config
         */
        if (USER_DIRECTORY != null && !USER_DIRECTORY.isEmpty()) {
            constellationDirectory = new File(USER_DIRECTORY);
            if (!constellationDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", USER_DIRECTORY);
            } else if (!constellationDirectory.isDirectory()){
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", USER_DIRECTORY);
            }
            return constellationDirectory;
        }

        /*
         * 4) .constellation in home directory
         */
        constellationDirectory = getConstellationDirectory();
        
        return constellationDirectory;
    }

    /**
     * Return the ".constellation" configuration directory in the user home.
     * 
     * @return
     */
    private static File getConstellationDirectory() {
        File constellationDirectory;
        final String home = System.getProperty("user.home");

        if (System.getProperty("os.name", "").startsWith("Windows")) {
             constellationDirectory = new File(home, WINDOWS_DIRECTORY);
        } else {
             constellationDirectory = new File(home, UNIX_DIRECTORY);
        }
        return constellationDirectory;
    }

    /**
     * Return a folder named 'provider' at the root in the configuration directory.
     */
    public static File getProviderConfigDirectory() {
        final File constellationDirectory = getConfigDirectory();

        if(!constellationDirectory.exists()){
            constellationDirectory.mkdirs();
        }

        final File providerDirectory = new File(constellationDirectory, "provider");
        if (!providerDirectory.exists()) {
            providerDirectory.mkdirs();
        }

        return providerDirectory;
    }
    
    /**
     * Return a folder named 'auth' at the root in the configuration directory.
     */
     public static File getAuthConfigDirectory() {
        final File constellationDirectory = getConfigDirectory();

        if(!constellationDirectory.exists()){
            constellationDirectory.mkdirs();
        }

        final File authDirectory = new File(constellationDirectory, "auth");
        if (!authDirectory.exists()) {
            authDirectory.mkdirs();
        }

        return authDirectory;
     }
     
    /**
     * Return the properties file at the root in the auth directory.
     */
    public static File getAuthConfigFile() {
        final File authDirectory = getAuthConfigDirectory();
        return new File(authDirectory, "cstl-auth.properties");
    }

    /**
     * Return a file at the root in the provider directory.
     */
    public static File getProviderConfigFile(final String fileName) {
        final File providerDirectory = getProviderConfigDirectory();
        return new File(providerDirectory, fileName);
    }

    public static void setConfigDirectory(final File directory) {
        USER_DIRECTORY = null;
        if (directory != null && directory.isDirectory()) {
            if (!directory.getPath().equals(getConstellationDirectory().getPath())) {
                USER_DIRECTORY = directory.getPath();
            }
        }
        //store the configuration properties file
        final File webInfDirectory = getWebInfDiretory();
        final File propertiesFile = new File(webInfDirectory, "constellation.properties");
        try {
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            }
            final Properties prop = new Properties();
            final String pathValue;
            if (USER_DIRECTORY == null) {
                pathValue = "";
            } else {
                pathValue = USER_DIRECTORY;
            }
            prop.put("configuration_directory", pathValue);
            FileUtilities.storeProperties(prop, propertiesFile);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException while writing the constellation properties file", ex);
        }
        
    }

    /**
     * Get the value for a property defined in the JNDI context chosen.
     *
     * @param propGroup If you use Glassfish, you have to specify the name of the resource that
     *                  owns the property you wish to get. Otherwise you should specify {@code null}
     * @param propName  The name of the property to get.
     * @return The property value defines in the context, or {@code null} if no property of this name
     *         is defined in the resource given in parameter.
     * @throws NamingException if an error occurs while initializing the context, or if an empty value
     *                         for propGroup has been passed while using a Glassfish application server.
     */
    public static String getPropertyValue(final String propGroup, final String propName) throws NamingException {
        final InitialContext ctx = new InitialContext();
        if (runningOnGlassfish == null) {
            runningOnGlassfish = (System.getProperty("domain.name") != null) ? true : false;
        }
        if (runningOnGlassfish) {
            if (propGroup == null) {
                throw new NamingException("The coverage property group is not specified.");
            }
            final Reference props = (Reference) getContextProperty(propGroup, ctx);
            if (props == null) {
                throw new NamingException("The coverage property group specified does not exist.");
            }
            final RefAddr permissionAddr = (RefAddr) props.get(propName);
            if (permissionAddr != null) {
                return (String) permissionAddr.getContent();
            }
            return null;
        } else {
            final javax.naming.Context envContext = (javax.naming.Context) ctx.lookup("java:/comp/env");
            return (String) getContextProperty(propName, envContext);
        }
    }

    /**
     * Returns the context value for the key specified, or {@code null} if not found
     * in this context.
     *
     * @param key The key to search in the context.
     * @param context The context which to consider.
     */
    private static Object getContextProperty(final String key, final javax.naming.Context context) {
        Object value = null;
        try {
            value = context.lookup(key);
        } catch (NamingException n) {
            // Do nothing, the key is not found in the context and the value is still null.
        }

        return value;
    }

}
