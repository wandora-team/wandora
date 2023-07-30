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


package org.wandora.application.gui.search;

import java.awt.BorderLayout;

import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.topicpanels.SearchTopicPanel;

/**
 *
 * @author akivela
 */


public class SearchTopicsFrame extends javax.swing.JDialog {

    private SearchTopicPanel searchPanels = null;
    
    /**
     * Creates new form SearchTopicsFrame
     */
    public SearchTopicsFrame() {
        super(Wandora.getWandora(), false);
        Wandora wandora = Wandora.getWandora();
        setAlwaysOnTop(false);
        setTitle("Search and query");
        
        initComponents();
        
        searchPanels = new SearchTopicPanel();
        searchPanels.setUseResultScrollPanes(true);
        searchPanels.init();
        containerPanel.add(searchPanels.getGui(), BorderLayout.CENTER);
        
        setSize(850, 600);

        if(wandora != null) {
            setIconImages(wandora.wandoraIcons);
            wandora.centerWindow(this);
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

        buttonPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        closeButton = new SimpleButton();
        containerScrollPane = new SimpleScrollPane();
        containerPanel = new javax.swing.JPanel();

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        fillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        buttonPanel.add(closeButton, gridBagConstraints);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        containerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(containerPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.setVisible(false);
        
    }//GEN-LAST:event_closeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JScrollPane containerScrollPane;
    private javax.swing.JPanel fillerPanel;
    // End of variables declaration//GEN-END:variables
}
