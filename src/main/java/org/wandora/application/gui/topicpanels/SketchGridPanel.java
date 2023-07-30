/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * 
 * SketchGridPanel.java
 *
 * Created on 2013-05-10
 */


package org.wandora.application.gui.topicpanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.table.JTableHeader;

import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;

/**
 * 
 * @author akivela
 */


public class SketchGridPanel extends javax.swing.JPanel implements TopicMapListener, RefreshListener, TopicPanel, ActionListener, ComponentListener {

    private int gridWidth = 10;
    private int gridHeight = 200;
    private TopicGrid topicGrid;
    private boolean needsRefresh = false;



    /**
     * Creates new form SketchGridPanel
     */
    public SketchGridPanel() {
    }
    
    
    
    @Override
    public void init() {
        initComponents();
        this.addComponentListener(this);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        topicGrid = new TopicGrid(Wandora.getWandora());
        topicGrid.initialize(gridWidth, gridHeight);
        contentPanel.add(topicGrid, BorderLayout.CENTER);
        JTableHeader tableHeader = topicGrid.getTableHeader();
        headerPanel.add(tableHeader, BorderLayout.CENTER);
    }
    
    

    @Override
    public void doRefresh() throws TopicMapException {
        
    }

    
    @Override
    public boolean supportsOpenTopic() {
        return true;
    }
    
    

    @Override
    public void open(Topic topic) throws TopicMapException {
        List<int[]> cells = topicGrid.getSelectedCells();
        if(cells != null && !cells.isEmpty()) {
            topicGrid.setCurrentTopic(topic);
        }
        else {
            topicGrid.setTopicAt(topic, 0, 0);
        }
    }

    @Override
    public void stop() {
        
    }

    @Override
    public void refresh() throws TopicMapException {
        
    }

    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        return false;
    }

    @Override
    public JPanel getGui() {
        return this;
    }

    @Override
    public Topic getTopic() throws TopicMapException {
        Topic[] cts = topicGrid.getCurrentTopics();
        if(cts != null && cts.length > 0) {
            return cts[0];
        }
        return null;
    }

    @Override
    public String getName() {
        return "Sketch grid";
    }
    
    @Override
    public String getTitle() {
        return getName();
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_grid.png");
    }

    @Override
    public int getOrder() {
        return 300;
    }

    @Override
    public boolean noScroll(){
        return false;
    }
    
    @Override
    public Object[] getViewMenuStruct() {
        return new Object[] {};
    }

    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }
    @Override
    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }
    
    

    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }


    
    @Override
    public void actionPerformed(ActionEvent e) {
        
    }

    
    
    // -------------------------------------------------------------------------
    
    @Override
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        handleComponentEvent(e);
    }
    
    private void handleComponentEvent(ComponentEvent e) {
        this.setPreferredSize(new Dimension(50, 50));
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        needsRefresh=true;
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        needsRefresh=true;
    }
    



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        headerPanel.setLayout(new java.awt.BorderLayout());
        add(headerPanel, java.awt.BorderLayout.NORTH);

        contentPanel.setLayout(new java.awt.BorderLayout());
        scrollPane.setViewportView(contentPanel);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
