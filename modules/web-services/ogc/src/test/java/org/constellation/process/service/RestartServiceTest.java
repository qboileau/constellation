/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.service;

import org.constellation.process.ConstellationProcessFactory;
import org.constellation.ws.WSEngine;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class RestartServiceTest extends ServiceProcessTest {

    public RestartServiceTest(final String serviceName, final Class workerClass) {
        super(RestartServiceDescriptor.NAME, serviceName, workerClass);
    }


    @Test
    public void testRestartOneNoClose() throws NoSuchIdentifierException, ProcessException {

        createInstance("restartInstance1");
        startInstance("restartInstance1");

        final int initSize = WSEngine.getInstanceSize(serviceName);
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("restartInstance1");
        in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(false);
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize(serviceName) == initSize);
        assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance1"));

        deleteInstance("restartInstance1");
    }

    @Test
    public void testRestartOneClose() throws NoSuchIdentifierException, ProcessException {

        createInstance("restartInstance2");
        startInstance("restartInstance2");

        final int initSize = WSEngine.getInstanceSize(serviceName);
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("restartInstance2");
        in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(true);
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize(serviceName) == initSize);
        assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance2"));

        deleteInstance("restartInstance2");
    }

    @Test
    public void testRestartAllNoClose() throws NoSuchIdentifierException, ProcessException {

        createInstance("restartInstance3");
        createInstance("restartInstance4");
        startInstance("restartInstance3");
        startInstance("restartInstance4");

        final int initSize = WSEngine.getInstanceSize(serviceName);
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(null);
        in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(false);
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize(serviceName) == initSize);
        assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance3"));
        assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance4"));

        deleteInstance("restartInstance3");
        deleteInstance("restartInstance4");

    }

    @Test
    public void testRestartAllClose() throws NoSuchIdentifierException, ProcessException {

        createInstance("restartInstance5");
        createInstance("restartInstance6");
        startInstance("restartInstance5");
        startInstance("restartInstance6");

        final int initSize = WSEngine.getInstanceSize(serviceName);
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(null);
        in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(true);

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(WSEngine.getInstanceSize(serviceName) == initSize);
        assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance5"));
        assertTrue(WSEngine.serviceInstanceExist(serviceName, "restartInstance6"));

          deleteInstance("restartInstance5");
        deleteInstance("restartInstance6");
    }

    /**
     * Try to restart an instance that exist but no started.
     * @throws NoSuchIdentifierException
     * @throws ProcessException
     */
    @Test
    public void testFailRestart1() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("resartInstance4");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    /**
     * Try to restart an instance that doesn't exist.
     * @throws NoSuchIdentifierException
     * @throws ProcessException
     */
    @Test
    public void testFailRestart2() throws NoSuchIdentifierException, ProcessException {
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
        in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue("restartInstance5");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

}