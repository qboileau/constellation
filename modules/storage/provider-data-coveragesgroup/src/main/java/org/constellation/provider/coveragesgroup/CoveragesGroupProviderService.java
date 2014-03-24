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
package org.constellation.provider.coveragesgroup;

import java.net.URL;
import java.util.logging.Level;
import org.constellation.provider.AbstractProviderFactory;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.provider.coveragesgroup.CoveragesGroupProvider.*;


/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class CoveragesGroupProviderService extends AbstractProviderFactory
        <Name,Data,DataProvider> implements DataProviderFactory {

    public static final String NAME = "coverages-group";

    public static final ParameterDescriptor<URL> URL =
            new DefaultParameterDescriptor<URL>(KEY_PATH, "Map context path", URL.class, null, true);

    public static final ParameterDescriptorGroup SOURCE_CONFIG_DESCRIPTOR =
            new DefaultParameterDescriptorGroup("coveragesgroup", URL);

    public static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            createDescriptor(SOURCE_CONFIG_DESCRIPTOR);

    public CoveragesGroupProviderService(){
        super(NAME);
    }

    @Override
    public ParameterDescriptorGroup getProviderDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public GeneralParameterDescriptor getStoreDescriptor() {
        return SOURCE_CONFIG_DESCRIPTOR;
    }

    @Override
    public DataProvider createProvider(String providerId, ParameterValueGroup ps) {
        if (!canProcess(ps)) {
            return null;
        }

        final CoveragesGroupProvider provider = new CoveragesGroupProvider(providerId, this, ps);
        ps = getOrCreate(SOURCE_CONFIG_DESCRIPTOR, ps);
        getLogger().log(Level.INFO, "[PROVIDER]> Coverages group provider created : {0}",
                value(URL, ps));
        return provider;
    }

}
