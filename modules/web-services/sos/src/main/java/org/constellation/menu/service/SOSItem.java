/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.menu.service;

import org.constellation.bean.AbstractMenuItem;


/**
 * Add a SOS service page.
 *
 * @author Johann Sorel (Geomatys)
 */
public class SOSItem extends AbstractMenuItem{

    public SOSItem() {
        super(
            new String[]{
                "/org/constellation/menu/service/sos.xhtml",
                "/org/constellation/menu/service/sosConfig.xhtml"},
            "org.constellation.menu.service.sos",
            new Path(SERVICES_PATH,"SOS", "/org/constellation/menu/service/sos.xhtml", null,500)
            );
    }

}
