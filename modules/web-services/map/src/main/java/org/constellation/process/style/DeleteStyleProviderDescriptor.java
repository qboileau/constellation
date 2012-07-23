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
package org.constellation.process.style;

import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.util.SimpleInternationalString;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 * Remove a style from an existing style provider.
 *
 * @author Quentin Boileau (Geomatys).
 */
public class DeleteStyleProviderDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "style_provider.delete";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Remove a style from an existing style provider.");

    /*
     * Provider identifier
     */
    public static final String PROVIDER_ID_NAME = "provider_id";
    private static final String PROVIDER_ID_REMARKS = "Identifier of the provider to remove style.";
    public static final ParameterDescriptor<String> PROVIDER_ID =
            new DefaultParameterDescriptor(PROVIDER_ID_NAME, PROVIDER_ID_REMARKS, String.class, null, true);

    /*
     * Style name.
     */
    public static final String STYLE_ID_NAME = "style_id";
    private static final String STYLE_ID_REMARKS = "Name/Identifier of the style to remove.";
    public static final ParameterDescriptor<String> STYLE_ID =
            new DefaultParameterDescriptor(STYLE_ID_NAME, STYLE_ID_REMARKS, String.class, null, true);

    /**
     * Input parameters
     */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters", new GeneralParameterDescriptor[]{PROVIDER_ID, STYLE_ID});
    /**
     * Output parameters
     */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public DeleteStyleProviderDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new DeleteStyleProvider(this, input);
    }
}