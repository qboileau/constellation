/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.sos.webservice.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import net.seagis.ows.v110.ExceptionReport;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.Service;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SOServiceException", namespace="http://sos.geomatys.fr/")
public class SOServiceException extends Exception {
    
    /**
     * An OGC Web Service exception report
     */
    private ExceptionReport exception;
    
    SOServiceException() {
        super();
    }
            
    public SOServiceException(String message, String code, String v) {
        super(message);
        this.exception = new ExceptionReport(message, code, null, new ServiceVersion(Service.OWS, v));
        
        this.setStackTrace(new StackTraceElement[0]);
    }
    
    public ExceptionReport getException() {
        return exception;
    }
}
