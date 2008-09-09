/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.sie.window.coverages;

// J2SE dependencies
import java.util.Date;
import java.io.Serializable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.ListSelectionListener;

// OpenIDE dependencies
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

// Sicade dependencies
import org.constellation.observation.coverage.CoverageReference;
import org.constellation.observation.coverage.CoverageTableModel;


/**
 * Fenêtre qui affichera la liste des images d'une série. Cette fenêtre peut être affichée
 * par {@link ViewAction}, une action qui sera proposée dans le menu "Window".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CoveragesWindow extends TopComponent {
    /**
     * Pour compatibilité avec différentes versions de cette classe.
     */
    private static final long serialVersionUID = -2749736476976179738L;

    /**
     * Une instance unique créée par {@link #getDefault} la première fois où cette dernière
     * sera appelée. Cette instance n'est utilisée que lors de la construction de la fenêtre
     * à partir d'un flot binaire.
     */
    private static CoveragesWindow instance;

    /**
     * Chemin vers l'icône utilisée pour cette fenêtre ainsi que pour l'action
     * {@link ViewAction} qui l'ouvrira.
     */
    static final String ICON_PATH = "net/seagis/sie/window/coverages/Icon.gif";

    /**
     * Une chaîne de caractères qui représentera cette fenêtre au sein du {@linkplain WindowManager
     * gestionnaire des fenêtres}. Cet ID sert à obtenir une instance unique de cette fenêtre par
     * un appel à la méthode {@link #findInstance}.
     */
    private static final String PREFERRED_ID = "CoveragesWindow";

    /**
     * Un modèle vide, utilisé lorsque aucune mosaïque d'images n'est active.
     */
    private final CoverageTableModel emptyModel = new CoverageTableModel();

    /**
     * Le modèle qui représentera les séries dans un tableau.
     */
    private CoverageTableModel model = emptyModel;

    /**
     * Objet à informer des changements de la sélection des images. Ces listeners correspondent
     * à des instances de {@link org.constellation.sie.window.mosaic.MosaicWindow}, et il ne devrait y
     * avoir qu'un seul listener actif à chaque instant.
     */
    private ListSelectionListener listener;

    /**
     * Construit une fenêtre contenant une liste initialement vide.
     *
     * @todo Ajouter {@code UndoManager}. Voir la javadoc de {@link CoverageTableModel}.
     */
    private CoveragesWindow() {
        initComponents();
        setName       (PREFERRED_ID);
        setDisplayName(NbBundle.getMessage(CoveragesWindow.class, "WindowTitle"));
        setToolTipText(NbBundle.getMessage(CoveragesWindow.class, "WindowHint"));
        setIcon       (Utilities.loadImage(ICON_PATH, true));
        final TableCellRenderer renderer = new CoverageTableModel.CellRenderer();
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(  Date.class, renderer);
        table.setModel(model);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JScrollPane scrollPane;

        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        scrollPane.setViewportView(table);

        add(scrollPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    /**
     * Retourne une instance par défaut. <strong>N'appellez pas cette méthode directement!</strong>
     * Cette méthode est public pour les besoins de la plateforme Netbeans, mais réservée à un usage
     * interne par les fichiers {@code *.settings}, c'est-à-dire durant les lectures à partir d'un flot
     * binaire. Pour obtenir un singleton dans les tous les autres cas, utilisez {@link #findInstance}.
     */
    public static synchronized CoveragesWindow getDefault() {
        if (instance == null) {
            instance = new CoveragesWindow();
        }
        return instance;
    }

    /**
     * Obtient une instance unique d'une fenêtre de cette classe. Utilisez cette méthode
     * plutôt que {@link #getDefault}.
     */
    public static synchronized CoveragesWindow findInstance() {
        final TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        final String message;
        if (win == null) {
            message = "Aucune composante de type \"" + PREFERRED_ID + "\". " +
                      "La fenêtre ne sera pas positionnée correctement.";
        } else if (win instanceof CoveragesWindow) {
            return (CoveragesWindow) win;
        } else {
            message = "Il semble y avoir plusieurs composantes de type \"" + PREFERRED_ID + "\". " +
                      "L'application peut ne pas fonctionner correctement.";
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, message);
        return getDefault();
    }

    /**
     * Change les données affichées par cette table. Cette méthode est appelée chaque fois qu'une
     * nouvelle mosaïque d'images devient la fenêtre active.
     *
     * @param model Le modèle de table à utiliser.
     * @maram listener L'objet à informer des changements de la sélection d'images.
     */
    public void setCoverageTableModel(CoverageTableModel model, final ListSelectionListener listener) {
        if (model == null) {
            model = emptyModel;
        }
        if (model != this.model) {
            table.setModel(model);
            this.model = model;
        }
        final ListSelectionModel select = table.getSelectionModel();
        select.removeListSelectionListener(this.listener);
        select.addListSelectionListener(this.listener = listener);
    }

    /**
     * Retourne les références vers les images sélectionnées par l'utilisateur.
     * Le tableau retourné peut avoir une longueur de 0, mais ne sera jamais {@code null}.
     */
    public CoverageReference[] getSelectedEntries() {
        final int[] rows = table.getSelectedRows();
        final CoverageReference[] selection = new CoverageReference[rows.length];
        for (int i=0; i<rows.length; i++) {
            selection[i] = model.getCoverageReferenceAt(rows[i]);
        }
        return selection;
    }

    /**
     * Retourne l'identifiant des fenêtres de type {@code CoveragesWindow} dans le
     * gestionnaire des fenêtres.
     */
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * Spécifie de manière explicite que le type de persistence doit être
     * {@link #PERSISTENCE_ALWAYS PERSISTENCE_ALWAYS}. La surcharge de cette
     * méthode est nécessaire pour éviter un avertissement au moment de l'exécution.
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    /**
     * Lors de l'écriture en binaire de cette fenêtre, écrit une classe sentinelle
     * à la place de la totalité de {@code CoveragesWindow}.
     */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    /**
     * Les classes qui seront enregistrées en binaire à la place de {@link CoveragesWindow}.
     * Lors de la lecture, cette classe appelera {@link CoveragesWindow#getDefault} afin de
     * reconstruire une fenêtre qui apparaîtra dans l'application de l'utilisateur.
     *
     * @author Martin Desruisseaux
     * @version $Id$
     */
    final static class ResolvableHelper implements Serializable {
        /**
         * Pour compatibilité avec différentes versions de cette classe.
         */
        private static final long serialVersionUID = 1698356701408203107L;

        /**
         * Lors de la lecture binaire, remplace cet objet par une instance de la fenêtre
         * {@link CoveragesWindow}.
         */
        public Object readResolve() {
            return CoveragesWindow.getDefault();
        }
    }
}
