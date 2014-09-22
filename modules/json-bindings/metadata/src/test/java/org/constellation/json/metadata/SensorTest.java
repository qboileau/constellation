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
package org.constellation.json.metadata;

import java.io.IOException;
import java.net.URI;
import org.geotoolkit.sml.xml.v101.*;
import org.junit.Test;

import static org.junit.Assert.*;
import static java.util.Collections.singletonList;
import static org.constellation.json.metadata.TemplateTest.readAllLines;
import static org.constellation.json.metadata.TemplateTest.assertJsonEquals;


/**
 * Tests the {@link Template} class with a Sensor ML.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
public final strictfp class SensorTest {
    /**
     * Creates the metadata object corresponding to the test JSon string.
     */
    private static SensorML createSensorML() {
        final SystemType process = new SystemType();
        process.setIdentification(singletonList(
            new Identification(
                new IdentifierList(null, singletonList(
                    new Identifier("My identifier",                     // member.process.identification.identifierList.identifier.name
                    new Term("My identifier term", (URI) null)))))));   // member.process.identification.identifierList.identifier.term.value

        final SensorML sensor = new SensorML();
        sensor.getMember().add(new SensorML.Member(process));

        return sensor;
    }

    /**
     * Test writing of a simple metadata while pruning the empty nodes.
     *
     * @throws IOException if an error occurred while applying the template.
     */
    @Test
    public void testWritePrune() throws IOException {
        final SensorML metadata = createSensorML();
        final StringBuilder buffer = new StringBuilder(10000);
        Template.getInstance("profile_sensorml_system").write(metadata, buffer, true);
        assertJsonEquals("sensor_prune.json", buffer);
    }

    /**
     * Tests {@link Template#read(Iterable, Object)} when storing in an initially empty {@link SensorML}.
     *
     * @throws IOException if an error occurred while reading the test JSON file.
     */
    @Test
    public void testRead() throws IOException {
        final SensorML expected = createSensorML();
        final SensorML metadata = new SensorML();
        Template.getInstance("profile_sensorml_system").read(readAllLines("sensor_prune.json"), metadata, true);
        assertEquals(expected, metadata);
    }
}