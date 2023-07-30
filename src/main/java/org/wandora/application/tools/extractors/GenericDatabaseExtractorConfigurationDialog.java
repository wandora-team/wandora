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
 * GenericDatabaseExtractorConfigurationDialog.java
 *
 * Created on 14. helmikuuta 2007, 11:23
 */

package org.wandora.application.tools.extractors;


import java.awt.GridBagConstraints;
import java.util.Vector;

import javax.swing.JPanel;

import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.utils.swing.GuiTools;

/**
 *
 * @author  olli
 */
public class GenericDatabaseExtractorConfigurationDialog extends javax.swing.JDialog {
    

	private static final long serialVersionUID = 1L;
	
	private Wandora wandora;
    private boolean wasCancelled=true;
    private Vector<GenericDatabaseExtractorConfigurationPanel> panels;
    
    /** Creates new form GenericDatabaseExtractorConfigurationDialog */
    public GenericDatabaseExtractorConfigurationDialog(Wandora wandora,boolean modal,GenericDatabaseExtractor.DatabaseSchema schema) {
        super(wandora, modal);
        
        this.wandora=wandora;
        
        initComponents();
        
        this.setTitle("Configure extraction");
        
        setTables(schema);
        
        GuiTools.centerWindow(this,wandora);
    }
    
    private void setTables(GenericDatabaseExtractor.DatabaseSchema schema){
        panels=new Vector<GenericDatabaseExtractorConfigurationPanel>();
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill=gbc.BOTH;
        gbc.weightx=1.0;
        gbc.weighty=0.0;
        for(String table : schema.tables){
            GenericDatabaseExtractorConfigurationPanel panel=new GenericDatabaseExtractorConfigurationPanel(schema,table);            
            tableContainer.add(panel,gbc);
            panels.add(panel);
            gbc.gridy++;
        }
        gbc.weighty=1.0;
        tableContainer.add(new JPanel(),gbc);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        tableContainer = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cancelButton = new SimpleButton();
        okButton = new SimpleButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        tableContainer.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(tableContainer);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(okButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jPanel2, gridBagConstraints);

        setBounds(0, 0, 521, 510);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.wasCancelled=true;
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        this.wasCancelled=false;
        this.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed
    
    public boolean wasCancelled(){
        return wasCancelled;
    }
    
    public void updateSchema(GenericDatabaseExtractor.DatabaseSchema schema){
        for(GenericDatabaseExtractorConfigurationPanel panel : panels){
            panel.updateSchema(schema);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel tableContainer;
    // End of variables declaration//GEN-END:variables
    
}
