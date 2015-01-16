/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */


package org.wandora.application.gui.topicpanels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.search.QueryPanel;
import org.wandora.application.gui.search.SearchPanel;
import org.wandora.application.gui.search.SimilarityPanel;
import org.wandora.application.gui.search.TMQLPanel;
import org.wandora.application.gui.simple.SimpleComboBox;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.exceptions.OpenTopicNotSupportedException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;

/**
 *
 * @author akivela
 */


public class SearchTopicPanel extends javax.swing.JPanel implements ActionListener, TopicPanel {

    private Options options = null;
    
    private SearchPanel searchPanel = null;
    private SimilarityPanel similarityPanel = null;
    private QueryPanel queryPanel = null;
    private TMQLPanel tmqlPanel = null;
    
    private Component currentContainerPanel = null;
    
    
    
    
    /**
     * Creates new form SearchTopicPanel
     */
    public SearchTopicPanel() {
    }
    
    
    @Override
    public void init() {
        searchPanel = new SearchPanel();
        similarityPanel = new SimilarityPanel();
        queryPanel = new QueryPanel();
        tmqlPanel = new TMQLPanel();
        
        Wandora wandora = Wandora.getWandora();
        this.options = new Options(wandora.getOptions());
        initComponents();
        try {
            tabbedPane.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    // System.out.println("Tab: " + tabbedPane.getSelectedIndex());
                    setCurrentPanel(tabbedPane.getSelectedComponent());
                }
            });
            setCurrentPanel(searchContainerPanel);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void setCurrentPanel(Component p) {
        if(p != null) {
            if(!p.equals(currentContainerPanel)) {
                
                /*
                    Why remove content from other container panels?
                    Tabbed pane's height is inherited from the heighest
                    component within. Removing content from other container
                    panels ensures the tabbed pane inherits it's height from
                    the active tab.
                */
                
                if(p.equals(searchContainerPanel)) {
                    searchContainerPanel.add(searchPanel, BorderLayout.CENTER);
                    similarityContainerPanel.removeAll();
                    queryContainerPanel.removeAll();
                    tmqlContainerPanel.removeAll();
                }
                else if(p.equals(similarityContainerPanel)) {
                    similarityContainerPanel.add(similarityPanel, BorderLayout.CENTER);
                    searchContainerPanel.removeAll();
                    queryContainerPanel.removeAll();
                    tmqlContainerPanel.removeAll();
                }
                else if(p.equals(queryContainerPanel)) {
                    queryContainerPanel.add(queryPanel, BorderLayout.CENTER);
                    searchContainerPanel.removeAll();
                    similarityContainerPanel.removeAll();
                    tmqlContainerPanel.removeAll();
                }
                else if(p.equals(tmqlContainerPanel)) {
                    tmqlContainerPanel.add(tmqlPanel, BorderLayout.CENTER);
                    searchContainerPanel.removeAll();
                    queryContainerPanel.removeAll();
                    similarityContainerPanel.removeAll();
                }
                currentContainerPanel = p;
                this.revalidate();
            }
        }
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new SimpleTabbedPane();
        searchContainerPanel = new javax.swing.JPanel();
        similarityContainerPanel = new javax.swing.JPanel();
        queryContainerPanel = new javax.swing.JPanel();
        tmqlContainerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        searchContainerPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Finder", searchContainerPanel);

        similarityContainerPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Similar", similarityContainerPanel);

        queryContainerPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Query", queryContainerPanel);

        tmqlContainerPanel.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("TMQL", tmqlContainerPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel queryContainerPanel;
    private javax.swing.JPanel searchContainerPanel;
    private javax.swing.JPanel similarityContainerPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel tmqlContainerPanel;
    // End of variables declaration//GEN-END:variables

    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
    }

    
    @Override
    public boolean supportsOpenTopic() {
        return false;
    }
    
    
    @Override
    public void open(Topic topic) throws TopicMapException, OpenTopicNotSupportedException {
        throw new OpenTopicNotSupportedException();
    }

    
    
    @Override
    public void stop() {
        
    }

    @Override
    public void refresh() throws TopicMapException {
        searchPanel.refresh();
        similarityPanel.refresh();
        queryPanel.refresh();
        tmqlPanel.refresh();
        
        revalidate();
    }

    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        return true;
    }

    @Override
    public JPanel getGui() {
        return this;
    }

    @Override
    public Topic getTopic() throws TopicMapException {
        return null;
    }
    
    @Override
    public String getName(){
        return "Search";
    }
    
    @Override
    public String getTitle() {
        return "Search and query";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_search.png");
    }

    @Override
    public boolean noScroll(){
        return false;
    }
    
    @Override
    public int getOrder() {
        return 9995;
    }

    @Override
    public Object[] getViewMenuStruct() {
        return new Object[] {
            "[Nothing to configure]"
        };
    }

    @Override
    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }

    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }

    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }

    
    
    
    // -------------------------------------------------------------------------
    
    

    
    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        refresh();
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        refresh();
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        refresh();
    }
}
