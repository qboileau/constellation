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

package org.constellation.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Instance;
import org.constellation.generic.database.Automatic;
import static org.constellation.swing.JServiceEditionPane.LOGGER;
import org.openide.util.Exceptions;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JServiceCswEditPane extends JServiceEditionPane {

    private ConstellationServer server;
    private ConstellationClient serverV2;
    private Instance serviceInstance;
    private Automatic configuration;
    private JServiceEditionPane specificPane;
    
    /**
     * Creates new form JServiceCswEditPane
     */
    public JServiceCswEditPane() {
        initComponents();
    }

    /**
     * Creates new form JServiceMapEditPane
     *
     * @param server
     * @param serverV2
     * @param serviceInstance
     * @param configuration
     */
    public JServiceCswEditPane(final ConstellationServer server, final ConstellationClient serverV2, final Instance serviceInstance, final Object configuration) {
        this.server = server;
        this.serverV2 = serverV2;
        this.serviceInstance = serviceInstance;
        this.configuration = (configuration instanceof Automatic) ? (Automatic) configuration : null;
        initComponents();
        if (this.configuration != null) {
            if (this.configuration.getFormat().equals("mdweb")) {
                specificPane = new JCswMdwEditPane(this.configuration);
                specificPane.setSize(562, 298);
                centerPane.add(BorderLayout.CENTER, specificPane);
            } else if (this.configuration.getFormat().equals("filesystem")) {
                specificPane = new JCswFsEditPane(this.configuration);
                specificPane.setSize(450, 86);
                centerPane.add(BorderLayout.CENTER, specificPane);
            } else if (this.configuration.getFormat().equals("internal")) {
                specificPane = new JCswInternalEditPane(this.configuration);
                specificPane.setSize(450, 86);
                centerPane.add(BorderLayout.CENTER, specificPane);
            } else {
                LOGGER.log(Level.WARNING, "Unexpected CSW format:{0}", this.configuration.getFormat());
            }
            guiDataSourceCombo.setSelectedItem(this.configuration.getFormat());
            if (this.configuration.getLogLevel() != null) {
                this.logLevelCombo.setSelectedItem(this.configuration.getLogLevel().getName());
            }
        }
        repaint();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        guiDataSourceCombo = new JComboBox();
        centerPane = new JPanel();
        refreshIndexButton = new JButton();
        jLabel2 = new JLabel();
        logLevelCombo = new JComboBox();
        purgeDbButton = new JButton();

        setPreferredSize(new Dimension(642, 395));

        ResourceBundle bundle = ResourceBundle.getBundle("org/constellation/swing/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("sourceType")); // NOI18N

        guiDataSourceCombo.setModel(new DefaultComboBoxModel(new String[] { "mdweb", "filesystem", "internal" }));
        guiDataSourceCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                guiDataSourceComboItemStateChanged(evt);
            }
        });

        centerPane.setPreferredSize(new Dimension(236, 256));

        GroupLayout centerPaneLayout = new GroupLayout(centerPane);
        centerPane.setLayout(centerPaneLayout);
        centerPaneLayout.setHorizontalGroup(
            centerPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        centerPaneLayout.setVerticalGroup(
            centerPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 271, Short.MAX_VALUE)
        );

        refreshIndexButton.setText(bundle.getString("refreshIndex")); // NOI18N
        refreshIndexButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshIndexButtonActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle.getString("logLevel")); // NOI18N

        logLevelCombo.setModel(new DefaultComboBoxModel(new String[] { "INFO", "FINE", "FINER" }));

        purgeDbButton.setText(bundle.getString("purgeDb")); // NOI18N
        purgeDbButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                purgeDbButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(centerPane, GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logLevelCombo, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(purgeDbButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshIndexButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiDataSourceCombo, 0, 491, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(guiDataSourceCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(centerPane, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshIndexButton)
                    .addComponent(jLabel2)
                    .addComponent(logLevelCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(purgeDbButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void guiDataSourceComboItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_guiDataSourceComboItemStateChanged
        centerPane.removeAll();
        if (guiDataSourceCombo.getSelectedItem().equals("mdweb")) {
            specificPane = new JCswMdwEditPane(this.configuration);
            specificPane.setSize(562, 278);
            centerPane.add(BorderLayout.CENTER, specificPane);
        } else if (guiDataSourceCombo.getSelectedItem().equals("filesystem")) {
            specificPane = new JCswFsEditPane(this.configuration);
            specificPane.setSize(450, 86);
            centerPane.add(BorderLayout.CENTER, specificPane);
        } else if (guiDataSourceCombo.getSelectedItem().equals("internal")) {
            specificPane = new JCswInternalEditPane(this.configuration);
            specificPane.setSize(450, 86);
            centerPane.add(BorderLayout.CENTER, specificPane);
        } else {
            LOGGER.log(Level.WARNING, "Unexpected CSW format:{0}", this.configuration.getFormat());
        }
        validate();
        repaint();
    }//GEN-LAST:event_guiDataSourceComboItemStateChanged

    private void refreshIndexButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_refreshIndexButtonActionPerformed
        try {
            serverV2.csw.refreshIndex(serviceInstance.getIdentifier(), false, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_refreshIndexButtonActionPerformed

    private void purgeDbButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_purgeDbButtonActionPerformed
        final ResourceBundle bundle = ResourceBundle.getBundle("org/constellation/swing/Bundle");
        final String message = bundle.getString("purgeWarning");
        final String title = bundle.getString("purgeWarningTitle");
        final int result = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.OK_CANCEL_OPTION);
        try {
            switch (result) {
                case JOptionPane.OK_OPTION:
                    serverV2.csw.deleteAllMetadata(serviceInstance.getIdentifier());
                    serverV2.csw.refreshIndex(serviceInstance.getIdentifier(), false, false);
                    break;
                case JOptionPane.CANCEL_OPTION:
                    break;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_purgeDbButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel centerPane;
    private JComboBox guiDataSourceCombo;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JComboBox logLevelCombo;
    private JButton purgeDbButton;
    private JButton refreshIndexButton;
    // End of variables declaration//GEN-END:variables

    private void updateConfiguration() {
        if (specificPane != null) {
            this.configuration = (Automatic) specificPane.getConfiguration();
            this.configuration.setLogLevel((String)logLevelCombo.getSelectedItem());
        }
        
    }
    
    @Override
    public Object getConfiguration() {
        updateConfiguration();
        return configuration;
    }

    @Override
    public DataSourceType getDatasourceType() {
        throw new UnsupportedOperationException("Not supported on this panel.");
    }
}
