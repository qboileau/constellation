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

package org.constellation.sos.ws;

// JUnit dependencies

import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Ignore
@RunWith(SpringTestRunner.class)
public class GenericPostgridSOSWorkerTest extends SOSWorkerTest {

    private static DefaultDataSource ds = null;

    @Inject
    private IServiceBusiness serviceBusiness;
    
    private static String url;
    
    @Before
    public void setUpClass() throws Exception {
        url = "jdbc:derby:memory:GPGTest1;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));


        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        //we write the configuration file
        Automatic SMLConfiguration = new Automatic();
        SMLConfiguration.setFormat("nosml");

        Automatic OMConfiguration = (Automatic) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/generic-config.xml"));
        pool.recycle(unmarshaller);

        OMConfiguration.getBdd().setConnectURL(url);


        SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
        configuration.setObservationReaderType(DataSourceType.GENERIC);
        configuration.setObservationWriterType(DataSourceType.NONE);
        configuration.setSMLType(DataSourceType.NONE);
        configuration.setObservationFilterType(DataSourceType.GENERIC);
        configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
        configuration.setProfile("discovery");
        configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
        configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
        configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
        configuration.getParameters().put("transactionSecurized", "false");


        serviceBusiness.create("sos", "default", configuration, null, null);

        init();
        worker = new SOSworker("default");
        worker.setServiceUrl(URL);
        worker.setLogLevel(Level.FINER);
    }
    
    @Override
    public void initWorker() {
        worker = new SOSworker("default");
        worker.setServiceUrl(URL);
        worker.setLogLevel(Level.FINER);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (worker != null) {
            worker.destroy();
        }
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
        File mappingFile = new File("mapping.properties");
        if (mappingFile.exists()) {
            mappingFile.delete();
        }
        if (ds != null) {
            ds.shutdown();
        }
        ConfigDirectory.shutdownTestEnvironement("GPGSOSWorkerTest");
    }



    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=1)
    public void getCapabilitiesErrorTest() throws Exception {
        super.getCapabilitiesErrorTest();

    }


    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=2)
    public void getCapabilitiesTest() throws Exception {
        super.getCapabilitiesTest();

    }


    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=3)
    public void GetObservationErrorTest() throws Exception {
        super.GetObservationErrorTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=4)
    public void GetObservationTest() throws Exception {
        super.GetObservationTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=5)
    public void GetObservationSamplingCurveTest() throws Exception {
        super.GetObservationSamplingCurveTest();
    }

    @Test
    @Override
    @Order(order=6)
    public void GetObservationMeasurementTest() throws Exception {
        super.GetObservationMeasurementTest();
    }
    
    /**
     * Tests the GetObservationById method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=7)
    public void GetObservationByIdTest() throws Exception {
        super.GetObservationByIdTest();
    }
    
    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=8)
    public void GetResultErrorTest() throws Exception {
        super.GetResultErrorTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=9)
    public void GetResultTest() throws Exception {
        super.GetResultTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=10)
    public void GetFeatureOfInterestErrorTest() throws Exception {
        super.GetFeatureOfInterestErrorTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=11)
    public void GetFeatureOfInterestTest() throws Exception {
        super.GetFeatureOfInterestTest();
    }


    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=12)
    public void GetFeatureOfInterestTimeTest() throws Exception {
        super.GetFeatureOfInterestTimeTest();
    }
    
    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=13)
    public void destroyTest() throws Exception {
        super.destroyTest();
    }
}
