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

package org.constellation.menu.provider;

import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.sld.SLDProvider;

/**
 * SLD configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public class SLDBean extends AbstractDataStoreServiceBean{

    private static StyleProviderService getService(){
        for(StyleProviderService service : StyleProviderProxy.getInstance().getServices()){
            if(service.getName().equals("sld")){
                return service;
            }
        }
        return null;
    }

    public SLDBean(){
        super(getService(),"/org/constellation/menu/provider/sld.xhtml",
              "/org/constellation/menu/provider/sldConfig.xhtml");
        addBundle("org.constellation.menu.provider.sld");
    }

    @Override
    protected Class getProviderClass() {
        return SLDProvider.class;
    }

}
