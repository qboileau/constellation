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
package org.constellation.process.layer.delete;

import java.net.MalformedURLException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.layer.AbstractMapLayerTest;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.Provider;
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
public class DeleteMapLayerTest extends AbstractMapLayerTest {
    
    public DeleteMapLayerTest() {
        super(DeleteMapLayerDescriptor.NAME);
    }
    
    @Test
    public void testDeleteLayer() throws ProcessException, NoSuchIdentifierException, MalformedURLException {
        
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider2", true, EMPTY_CSV, "provider2Layer"));
        
        ParameterValueGroup emptyProvider = buildCSVProvider(DATASTORE_SERVICE, "provider2", true, EMPTY_CSV, null);
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapLayerDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapLayerDescriptor.PROVIDER_ID_NAME).setValue("provider2");
        in.parameter(DeleteMapLayerDescriptor.LAYER_NAME_NAME).setValue( "provider2Layer");

        desc.createProcess(in).call();

        Provider provider = null;
        for (LayerProvider p : LayerProviderProxy.getInstance().getProviders()) {
            if ("provider2".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        assertEquals(emptyProvider, provider.getSource());
        assertTrue(provider.getSource().groups("Layer").isEmpty());
        
        removeProvider("provider2");
    }
    
    
    /**
     * Layer does'nt exist.
     */
    @Test
    public void testFailDeleteLayer1() throws ProcessException, NoSuchIdentifierException, MalformedURLException {
        
        addProvider(buildCSVProvider(DATASTORE_SERVICE, "provider2", true, EMPTY_CSV, "provider2Layer"));
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapLayerDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapLayerDescriptor.PROVIDER_ID_NAME).setValue("provider2");
        in.parameter(DeleteMapLayerDescriptor.LAYER_NAME_NAME).setValue("layer22");

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }

        removeProvider("provider2");
    }
    
    
    /**
     * Provider does'nt exist.
     */
    @Test
    public void testFailDeleteLayer2() throws ProcessException, NoSuchIdentifierException, MalformedURLException {
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapLayerDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapLayerDescriptor.PROVIDER_ID_NAME).setValue("provider22");
        in.parameter(DeleteMapLayerDescriptor.LAYER_NAME_NAME).setValue("layer2");

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
    
    
    /**
     * Empty provider identifier.
     */
    @Test
    public void testFailDeleteLayer3() throws ProcessException, NoSuchIdentifierException, MalformedURLException {
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapLayerDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapLayerDescriptor.PROVIDER_ID_NAME).setValue("");
        in.parameter(DeleteMapLayerDescriptor.LAYER_NAME_NAME).setValue("layer2");

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
    
     /**
     * Empty layer name.
     */
    @Test
    public void testFailDeleteLayer4() throws ProcessException, NoSuchIdentifierException, MalformedURLException {
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapLayerDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapLayerDescriptor.PROVIDER_ID_NAME).setValue("provider2");
        in.parameter(DeleteMapLayerDescriptor.LAYER_NAME_NAME).setValue("");

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
}
