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

package org.constellation.sos.factory;

import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.geotoolkit.util.FileUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.spi.ServiceRegistry;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSFactoryTest {

    private static SMLFactory smlFactory;

    private static final File configurationDirectory = new File("LuceneSOSTest");
    
    private static final File dataDirectory = new File(configurationDirectory, "data");
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        
        final Iterator<SMLFactory> ite = ServiceRegistry.lookupProviders(SMLFactory.class);
        while (ite.hasNext()) {
            SMLFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(DataSourceType.FILESYSTEM)) {
                smlFactory = currentFactory;
            }
        }
        if (!configurationDirectory.exists()) {
            configurationDirectory.mkdir();
            dataDirectory.mkdir();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (configurationDirectory.exists()) {
            FileUtilities.deleteDirectory(configurationDirectory);
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    /**
     * Tests the initialisation of the SOS worker with different configuration mistake
     *
     * @throws java.lang.Exception
     */
    @Test
    public void defaultTypeTest() throws Exception {

        final BDD bdd            = new BDD("org.postgresql.driver", "SomeUrl", "boby", "gary");
        final Automatic config   = new Automatic("postgrid", bdd);
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(OMFactory.OBSERVATION_ID_BASE, "idbase");
        parameters.put(OMFactory.OBSERVATION_TEMPLATE_ID_BASE, "templateIdBase");
        parameters.put(OMFactory.SENSOR_ID_BASE, "sensorBase");

        boolean exLaunched = false;
        try  {
            SensorReader sr = smlFactory.getSensorReader(DataSourceType.MDWEB, config, parameters);
        } catch (MetadataIoException ex) {
            exLaunched = true;
            assertEquals("The sensor data directory is null", ex.getMessage());
        }
        assertTrue(exLaunched);
        
        try  {
            SensorWriter sw = smlFactory.getSensorWriter(DataSourceType.MDWEB, config, parameters);
        } catch (MetadataIoException ex) {
            exLaunched = true;
            assertEquals("The sensor data directory is null", ex.getMessage());
        }
        assertTrue(exLaunched);
        
        config.setConfigurationDirectory(configurationDirectory);
        config.setDataDirectory(dataDirectory.getPath());

        SensorReader sr = smlFactory.getSensorReader(DataSourceType.MDWEB, config, parameters);
        SensorWriter sw = smlFactory.getSensorWriter(DataSourceType.MDWEB, config, parameters);
        
    }

}
