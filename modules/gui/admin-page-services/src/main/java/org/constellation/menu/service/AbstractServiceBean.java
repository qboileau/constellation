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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.MenuBean;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.ServiceStatus;
import org.constellation.configuration.Source;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.logging.Logging;
import org.mapfaces.event.CloseEvent;
import org.mapfaces.i18n.I18NBean;
import org.mapfaces.model.UploadedFile;
import org.mapfaces.renderkit.html.outline.OutlineCellStyler;
import org.mapfaces.renderkit.html.outline.OutlineDataModel;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;

/**
 * Abstract JSF Bean for service administration interface.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class AbstractServiceBean extends I18NBean {

    public static final OutlineRowStyler ROW_STYLER = new OutlineRowStyler() {

        @Override
        public String getRowStyle(TreeNode node) {
            final DefaultMutableTreeNode mn = (DefaultMutableTreeNode) node;
            final Object obj = mn.getUserObject();
            if(obj instanceof Source){
                return "height:34px;";
            }else if(obj instanceof SourceElement){
                int index = mn.getParent().getIndex(node);
                if(index % 2 == 0){
                    return "background-color:#DDEEFF";
                }else{
                    return "";
                }
            }else{
                return "";
            }
        }

        @Override
        public String getRowClass(TreeNode node) {
            return "";
        }
    };
    
    public static final OutlineCellStyler CELL_STYLER = new OutlineCellStyler() {

        @Override
        public String getCellStyle(TreeNode node, int rowIndex, int columnIndex) {
            if(columnIndex > 0){
                return "border-left: 1px solid #A2ACB6;";
            }else{
                return "";
            }
        }

        @Override
        public String getCellClass(TreeNode node, int rowIndex, int columnIndex) {
            return "";
        }
    };
    
    
    /**
     * When user is log in, a ServiceAdministrator object is added in the session map.
     */
    public static final String SERVICE_ADMIN_KEY = "serviceAdmin";
    
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.bean");

    private final Specification specification;
    private final String configPage;
    private final String mainPage;
    private ServiceInstance configuredInstance = null;
    protected Object configurationObject = null;
    private LayerContextTreeModel treemodel = null;
    private String selectedPotentialSource = null;
    private UploadedFile uploadedCapabilities;
    
    public static boolean ACTION_VISIBLE = true;
    
    private boolean creatingFlag = false;

    public AbstractServiceBean(final Specification specification, final String mainPage, final String configPage) {
        ArgumentChecks.ensureNonNull("specification", specification);
        ArgumentChecks.ensureNonNull("main page", mainPage);
        this.specification = specification;
        this.mainPage      = mainPage;
        this.configPage    = configPage;
        addBundle("service.service");
    }

    public final String getSpecificationName(){
        return specification.name();
    }

    protected ConstellationServer getServer(){
        return (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(SERVICE_ADMIN_KEY);
    }
    
    /**
     * @return List of all service instance of this specification.
     *      This list include both started and stopped instances.
     */
    public final TreeModel getInstances(){
        final ConstellationServer server = getServer();
        final List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        if (server != null) {
            final InstanceReport report = server.services.listInstance(getSpecificationName());
            if (report != null) {
                for (Instance instance : report.getInstances()) {
                    instances.add(new ServiceInstance(instance));
                }
            }
            Collections.sort(instances);
        }
        return new ListTreeModel(instances);
    }

    /**
     * Subclass may override this method to extend the ServiceInstance object.
     */
    protected ServiceInstance toServiceInstance(Instance inst){
        return new ServiceInstance(inst);
    }
       
    public OutlineCellStyler getCellStyler(){
        return CELL_STYLER;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // CREATING NEW INSTANCE ///////////////////////////////////////////////////

    /**
     * Create a new instance of this service.
     */
    public void createInstance(){
        final ConstellationServer server = getServer();
        if (server != null) {
            final InstanceReport report = server.services.listInstance(getSpecificationName());
            // Look for a free Instance name
            String newInstanceName = "default";
            int i = 1;
            boolean freeName = false;
            while (!freeName) {
                freeName = true;
                for (Instance instance : report.getInstances()) {
                    if (newInstanceName.equals(instance.getName())) {
                        //an instance with this already exist
                        freeName = false;
                        newInstanceName = "default" + i;
                        i++;
                        break;
                    }
                }
            }
            server.services.newInstance(getSpecificationName(), newInstanceName);
            
            configuredInstance  = new ServiceInstance(new Instance(newInstanceName, ServiceStatus.NOT_STARTED));
            configurationObject = server.services.getInstanceconfiguration(getSpecificationName(), newInstanceName);
           

            if (configurationObject instanceof LayerContext) {
                treemodel = new LayerContextTreeModel((LayerContext)configurationObject, server);
            } else {
                treemodel = null;
            }
            
            if (configPage != null) {
                 creatingFlag = true;
                final MenuBean bean = getMenuBean();
                if (bean != null) {
                    bean.addToNavigationStack(configuredInstance.getName());
                }
                FacesContext.getCurrentInstance().getViewRoot().setViewId(configPage);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ACTION TO ADD NEW SOURCE IN LAYER CONTEXT ///////////////////////////////
    
    public List<SelectItem> getPotentialSources(){
        final List<SelectItem> items = new ArrayList<SelectItem>();
        final ConstellationServer server = getServer();
        if (server != null) {
            final ProvidersReport pr = server.providers.listProviders();
            for(ProviderServiceReport provider : pr.getProviderServices()){
                if (!provider.isStyleService()) {
                    for (ProviderReport p : provider.getProviders()) {
                        final String name = p.getId();
                        items.add(new SelectItem(name, name));
                    }
                }
            }
        }
        return items;
    }
    
    public String getSelectedPotentialSource(){
        return selectedPotentialSource;
    }
    
    public void setSelectedPotentialSource(String selected){
        selectedPotentialSource = selected;
    }
    
    public boolean getPartialTree() {
        return MenuBean.PARTIAL_TREE;
    }
    
    public boolean getActionVisible() {
        return AbstractServiceBean.ACTION_VISIBLE;
    }
    
    public void addSource(){
        if(!(configurationObject instanceof LayerContext) ){
            return;
        }
        
        final LayerContext ctx = (LayerContext) configurationObject;
        final Source src = new Source();
        src.setId((selectedPotentialSource == null) ? "" : selectedPotentialSource );
        ctx.getLayers().add(src);
        final ConstellationServer server = getServer();
        if (configurationObject instanceof LayerContext && server != null) {
            treemodel = new LayerContextTreeModel((LayerContext)configurationObject, server);
        } else {
            treemodel = null;
        }
        
    }
    
    public void removeSource(){
        if(!(configurationObject instanceof LayerContext) ){
            return;
        }
                
        final FacesContext context = FacesContext.getCurrentInstance();
        final ExternalContext ext = context.getExternalContext();
        final Integer index = Integer.valueOf(ext.getRequestParameterMap().get("NodeId"));
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                OutlineDataModel.getNode((TreeNode)treemodel.getRoot(), index);
        
        treemodel.removeProperty(new TreePath(OutlineDataModel.getTreePath(node)));
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // CONFIGURE CURRENT INSTANCE //////////////////////////////////////////////

    /**
     * @return the main service page.
     * this is used to return from the configuration page.
     * @Deprecated use goMainPage() (portlet issue).
     */
    public String getMainPage(){
        return mainPage;
    }
    
    public void goMainPage(){
        if (creatingFlag && configuredInstance != null) {
            creatingFlag = false;
            final ConstellationServer server = getServer();
            if (server != null) {
                server.services.deleteInstance(getSpecificationName(), configuredInstance.getName());
            }
        }
        if (mainPage != null) {
            final MenuBean bean = getMenuBean();
            if (bean != null) {
                bean.backNavigationStack();
            }
            FacesContext.getCurrentInstance().getViewRoot().setViewId(mainPage);
        }
    }
    
    public void goToSourceAdmin(){
        if (mainPage != null) {
            final MenuBean bean = getMenuBean();
            if (bean != null) {
                bean.updateNavigationStack("providers");
            }
            FacesContext.getCurrentInstance().getViewRoot().setViewId("/base/providers.xhtml");
        }
    }

    /**
     * @return the currently configured instance.
     */
    public ServiceInstance getConfiguredInstance(){
        return configuredInstance;
    }

    /**
     * @return the configuration object of the edited instance.
     * Subclass should override this method to ensure this object is never null.
     */
    public Object getConfigurationObject(){
        return configurationObject;
    }

    /**
     * Called when the configuration dialog is closed.
     */
    public void configurationClosed(final CloseEvent event){
        //reset configured instance
        LOGGER.info("configuration closed");
        configuredInstance = null;
        configurationObject = null;
    }
    
    public LayerContextTreeModel getLayerModel(){
        return treemodel;
    }
    
    
    /**
     * Save the currently edited instance.
     * Subclass should override this method to make the proper save.
     */
    public void saveConfiguration() {
        final ConstellationServer server = getServer();
        if(configuredInstance != null && server != null){
            server.services.configureInstance(getSpecificationName(), configuredInstance.getName(), configurationObject);
            configuredInstance.restart();
        }
        creatingFlag = false;
        goMainPage();
    }
    
    public void updateCapabilities() {
         if (uploadedCapabilities != null) {
            final String contentType = uploadedCapabilities.getContentType();
            if ("application/xml".equals(contentType)
             || "text/xml".equals(contentType)
             || "application/x-httpd-php".equals(contentType)){
               
                final String instanceId = getConfiguredInstance().getName();
                try {
                    final File tmp = File.createTempFile("cstl", null);
                    final File importedfile = FileUtilities.buildFileFromStream(uploadedCapabilities.getInputStream(), tmp);
                    final ConstellationServer server = getServer();
                    if (server != null) {
                        server.services.updateCapabilities(getSpecificationName(), instanceId, importedfile, uploadedCapabilities.getFileName());
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "IO exception while reading imported file", ex);
                }
                
            } else {
                LOGGER.log(Level.WARNING, "This content type can not be read : {0}", contentType);
            }
        } else {
            LOGGER.log(Level.WARNING, "imported file is null");
        }
    }

    /**
     * @return the uploadedCapabilities
     */
    public UploadedFile getUploadedCapabilities() {
        return uploadedCapabilities;
    }

    /**
     * @param uploadedCapabilities the uploadedCapabilities to set
     */
    public void setUploadedCapabilities(UploadedFile uploadedCapabilities) {
        this.uploadedCapabilities = uploadedCapabilities;
    }

    public MenuBean getMenuBean() {
        final FacesContext context = FacesContext.getCurrentInstance();
        return (MenuBean) context.getApplication().evaluateExpressionGet(context, "#{menuBean}", MenuBean.class);
    }
    
    public class ServiceInstance implements Comparable<ServiceInstance>{

        protected Instance instance;

        public ServiceInstance(final Instance instance) {
            this.instance = instance;
        }

        public String getName(){
            return instance.getName();
        }

        public void setName(final String newName){
            if (!newName.equals(instance.getName())) {
                final ConstellationServer server = getServer();
                if (server != null) {
                    server.services.renameInstance(getSpecificationName(), instance.getName(), newName);
                    instance.setName(newName);
                    final MenuBean menuBean = getMenuBean();
                    if (menuBean != null) {
                        menuBean.backNavigationStack();
                        menuBean.addToNavigationStack(newName);
                    }
                }
            }
        }

        /**
         * @return URL path to the running service.
         */
        public String getPath(){
            final ConstellationServer server = getServer();
            if (server != null) {
                return server.services.getInstanceURL(getSpecificationName(), instance.getName());
            }
            return null;
        }

        public String getStatusIcon(){
            switch(instance.getStatus()){
                case WORKING:   return "provider.smallgreen.png.mfRes";
                case ERROR:     return "provider.smallred.png.mfRes";
                default:        return "provider.smallgray.png.mfRes";
            }
        }
        
        public boolean isWorking(){
            return ServiceStatus.WORKING.equals(instance.getStatus());
        }

        /**
         * Set this instance as the currently configured one in for the property dialog.
         */
        public void config(){
            configuredInstance = this;
            final ConstellationServer server = getServer();
            if (server != null) {
                configurationObject = server.services.getInstanceconfiguration(getSpecificationName(), instance.getName());
            
                if (configurationObject instanceof LayerContext) {
                    treemodel = new LayerContextTreeModel((LayerContext)configurationObject, server);
                }
            } else {
                treemodel = null;
            }
            
            if (configPage != null) {
                final MenuBean bean = getMenuBean();
                if (bean != null) {
                    bean.addToNavigationStack(configuredInstance.getName());
                }
            
                //the session is not logged, redirect him to the authentication page
                FacesContext.getCurrentInstance().getViewRoot().setViewId(configPage);
            }
        }

        public void start(){
            final ConstellationServer server = getServer();
            if (server != null) {
                server.services.startInstance(getSpecificationName(), instance.getName());
            }
            refresh();
        }
        public void stop(){
            final ConstellationServer server = getServer();
            if (server != null) {
                server.services.stopInstance(getSpecificationName(), instance.getName());
            }
            refresh();
        }

        public void delete(){
            final ConstellationServer server = getServer();
            if (server != null) {
                server.services.deleteInstance(getSpecificationName(), instance.getName());
            }
            refresh();
            goMainPage();
        }

        public void restart(){
            final ConstellationServer server = getServer();
            if (server != null) {
                server.services.restartInstance(getSpecificationName(), instance.getName());
            }
            refresh();
        }

        /**
         * Refresh this instance.
         */
        private void refresh(){
            final ConstellationServer server = getServer();
            if (server != null) {
                final InstanceReport report = server.services.listInstance(getSpecificationName());
                for (final Instance inst : report.getInstances()) {
                    if (instance.getName().equals(inst.getName())) {
                        instance = inst;
                        return;
                    }
                }
            }
        }

        @Override
        public int compareTo(final ServiceInstance other) {
            return getName().compareTo(other.getName());
        }

    }

}
