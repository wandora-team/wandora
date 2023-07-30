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
 */




package org.wandora.application.gui.topicpanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.gui.DropExtractPanel;
import org.wandora.application.gui.UIBox;
import org.wandora.exceptions.OpenTopicNotSupportedException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class DropExtractor extends javax.swing.JPanel implements TopicPanel, ActionListener {

    private DropExtractPanel dropExtractPanel = null;
    
    
    /**
     * Creates new form DropExtractor
     */
    public DropExtractor() {
    }

    
    @Override
    public void init() {
        initComponents();
        dropExtractPanel = new DropExtractPanel();
        dropExtractPanelContainer.add(dropExtractPanel);
    }
    
    
    @Override
    public boolean supportsOpenTopic() {
        return false;
    }
    
    
    @Override
    public void open(Topic topic) throws TopicMapException, OpenTopicNotSupportedException {

    }

    
    @Override
    public void stop() {

    }


    @Override
    public void refresh() throws TopicMapException {
        if(dropExtractPanel != null) {
            dropExtractPanel.updateMenu();
        }
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
        return "Drop extractor";
    }
    
    
    @Override
    public String getTitle() {
        return "Drop extractor";
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_drop_extractor.png");
    }

    
    @Override
    public boolean noScroll(){
        return false;
    }
    
    
    @Override
    public int getOrder() {
        return 9997;
    }

    
    @Override
    public Object[] getViewMenuStruct() {
        return new Object[] {
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

    
    @Override
    public void actionPerformed(ActionEvent e) {

    }
    
    
    
    
    // ---------------------------------------------------- TopicMapListener ---
    
    // Topic map changes doesn't affect DropExtractor any way.

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
    }

    
    
    // -------------------------------------------------------------------------
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dropExtractPanelContainer = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());

        dropExtractPanelContainer.setBackground(new java.awt.Color(255, 255, 255));
        dropExtractPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(dropExtractPanelContainer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dropExtractPanelContainer;
    // End of variables declaration//GEN-END:variables


}
