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
package org.constellation.provider.coveragestore;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.DefaultCoverageStoreLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.postgresql.PGCoverageStore;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageStoreProvider extends AbstractLayerProvider{

    private CoverageStore store;
    private Set<Name> names;

    public CoverageStoreProvider(ProviderService service, ParameterValueGroup param){
        super(service,param);
    }

    @Override
    public synchronized void reload() {
        super.reload();
        dispose();

        //pameter is a choice of different types
        //extract the first one
        ParameterValueGroup param = getSource();
        param = param.groups("choice").get(0);
        ParameterValueGroup factoryconfig = null;
        for(GeneralParameterValue val : param.values()){
            if(val instanceof ParameterValueGroup){
                factoryconfig = (ParameterValueGroup) val;
                break;
            }
        }

        if(factoryconfig == null){
            getLogger().log(Level.WARNING, "No configuration for coverage store source.");
            names = Collections.EMPTY_SET;
            return;
        }
        try {
            //create the store
            store = CoverageStoreFinder.open(factoryconfig);
            if(store == null){
                throw new DataStoreException("Could not create coverage store for parameters : "+factoryconfig);
            }
            names = store.getNames();
        } catch (DataStoreException ex) {
            names = Collections.EMPTY_SET;
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }

    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if(store != null){
            store.dispose();
            store = null;
            names = null;
        }
    }

    @Override
    public Set<Name> getKeys() {
        if(names == null){
            reload();
        }
        return names;
    }

    @Override
    public LayerDetails get(Name key) {
        key = fullyQualified(key);
        if(!contains(key)){
            return null;
        }
        try {
            return new DefaultCoverageStoreLayerDetails(key, store.getCoverageReference(key));
        } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public void remove(Name key) {
        if (store == null) {
            reload();
        }

        try {
            store.delete(key);
            reload();
       } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    @Override
    public void removeAll() {
        try {
            for (Name name : names) {
                store.delete(name);
            }
            reload();

            if (store instanceof PGCoverageStore) {
                final PGCoverageStore pgStore = (PGCoverageStore)store;
                final String dbSchema = pgStore.getDatabaseSchema();
                if (dbSchema != null && !dbSchema.isEmpty()) {
                    pgStore.dropPostgresSchema(dbSchema);
                }
            }
        } catch (DataStoreException e) {
            getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }
}
