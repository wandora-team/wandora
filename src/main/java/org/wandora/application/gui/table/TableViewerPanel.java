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



package org.wandora.application.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JDialog;

import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;


/**
 *
 * @author akivela
 */


public class TableViewerPanel extends javax.swing.JPanel {

    private Component component = null;
    private JDialog dialog = null;
    
    /**
     * Creates new form TableViewerPanel
     */
    public TableViewerPanel() {
        initComponents();
    }

    
    public void setTable(Component table) {
        if(table != null) {
            component = table;
            tableContainer.add(table, BorderLayout.CENTER);
        }
    }
    
    
    public void openInDialog(Component table, String title) {
        setTable(table);
        Wandora wandora = Wandora.getWandora();
        
        dialog = new JDialog(wandora, true);
        dialog.setTitle(title);
        dialog.add(this);
        dialog.setSize(700, 600);
        wandora.centerWindow(dialog);
        dialog.setVisible(true);
        
        // WAIT TILL CLOSED
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

        tablePanelContainer = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        tableContainer = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        closeButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        tablePanelContainer.setLayout(new java.awt.GridBagLayout());

        tableContainer.setLayout(new java.awt.BorderLayout());
        tableScrollPane.setViewportView(tableContainer);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 77;
        gridBagConstraints.ipady = 77;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablePanelContainer.add(tableScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tablePanelContainer, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout buttonFillerPanelLayout = new javax.swing.GroupLayout(buttonFillerPanel);
        buttonFillerPanel.setLayout(buttonFillerPanelLayout);
        buttonFillerPanelLayout.setHorizontalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        buttonFillerPanelLayout.setVerticalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        buttonPanel.add(closeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        
        if(dialog != null) {
            dialog.setVisible(false);
        }
        
    }//GEN-LAST:event_closeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel tableContainer;
    private javax.swing.JPanel tablePanelContainer;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables
}
