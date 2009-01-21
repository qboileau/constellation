/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.sos.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.sml.AbstractSensorML;
import org.constellation.ws.WebServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class FileSensorWriter extends SensorWriter {

    /**
     * A JAXB unmarshaller used to unmarshall the xml generated by the XMLWriter.
     */
    private Marshaller marshaller;

    private File dataDirectory;

    private List<File> uncommittedFiles;

    private String sensorIdBase;

    public FileSensorWriter(File dataDirectory, String sensorIdBase) throws WebServiceException {
        this.sensorIdBase = sensorIdBase;
        uncommittedFiles = new ArrayList<File>();
        this.dataDirectory = dataDirectory;
    }

    @Override
    public void writeSensor(String id, AbstractSensorML sensor) throws WebServiceException {
        try {
            File currentFile = new File(dataDirectory, id + ".xml");
            marshaller.marshal(sensor, currentFile);
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new WebServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                             NO_APPLICABLE_CODE);
        }
    }

    @Override
    public void startTransaction() throws WebServiceException {
        uncommittedFiles = new ArrayList<File>();
    }

    @Override
    public void abortTransaction() throws WebServiceException {
        for (File f: uncommittedFiles) {
            boolean delete = f.delete();
            if (!delete) {
                logger.severe("unable to delete the file:" + f.getName());
            }
        }
        uncommittedFiles = new ArrayList<File>();
    }

    @Override
    public void endTransaction() throws WebServiceException {
        uncommittedFiles = new ArrayList<File>();
    }

    @Override
    public int getNewSensorId() throws WebServiceException {
        int maxID = 0;
        for (File f : dataDirectory.listFiles()) {
            String id = f.getName();
            id = id.substring(0, id.indexOf(".xml"));
            id = id.substring(id.indexOf(sensorIdBase) + sensorIdBase.length());
            try {
                int curentID = Integer.parseInt(id);
                if (curentID > maxID) {
                    maxID = curentID;
                }
            } catch (NumberFormatException ex) {
                throw new WebServiceException("unable to parse the identifier:" + id, NO_APPLICABLE_CODE);
            }
        }
        return maxID + 1;
    }

    @Override
    public void destroy() {
        if (uncommittedFiles != null)
            uncommittedFiles.clear();
    }

}
