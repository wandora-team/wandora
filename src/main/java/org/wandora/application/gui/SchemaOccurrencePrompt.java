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
 * SchemaOccurrencePrompt.java
 *
 * Created on August 16, 2004, 10:39 AM
 */

package org.wandora.application.gui;


import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleComboBox;
import org.wandora.topicmap.SchemaBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


/**
 * @deprecated 
 *
 * @author  olli
 */
public class SchemaOccurrencePrompt extends javax.swing.JDialog {
    
    private Topic topic;
    private Wandora parent;
    private boolean cancelled;
    private ResourceEditor editor;
    
    /** Creates new form SchemaOccurrencePrompt */
    public SchemaOccurrencePrompt(Wandora parent, boolean modal,Topic topic) {
        super(parent, modal);
        this.parent=parent;
        this.topic=topic;
        initComponents();
        typeComboBox.setEditable(false);

        try{
            Iterator iter=SchemaBox.getOccurrenceTypesFor(topic).iterator();
            while(iter.hasNext()){
                Topic t=(Topic)iter.next();
                typeComboBox.addItem(new ComboBoxTopicWrapper(t));
            }
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
        
        this.cancelled=true;
        
        parent.centerWindow(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        typeComboBox = new SimpleComboBox();
        dataPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Occurrence Editor");
        typeComboBox.setMinimumSize(new java.awt.Dimension(50, 20));
        typeComboBox.setPreferredSize(new java.awt.Dimension(200, 20));
        typeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(typeComboBox, gridBagConstraints);

        dataPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 9);
        getContentPane().add(dataPanel, gridBagConstraints);

        okButton.setText("OK");
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 5);
        getContentPane().add(buttonPanel, gridBagConstraints);

        setBounds(0, 0, 603, 227);
    }// </editor-fold>//GEN-END:initComponents

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        try{
            makeDataPanel();
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        cancelled=false;
        try{
            makeOccurrence();
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
        this.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelled=true;
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void makeOccurrence() throws TopicMapException {
        if(editor!=null) { editor.applyChanges(topic, parent); }
        else cancelled=true;
    }
    
    public boolean wasCancelled(){
        return cancelled;
    }
        
    private void makeDataPanel() throws TopicMapException {
        Topic t=((ComboBoxTopicWrapper)typeComboBox.getSelectedItem()).topic;
        editor=new OccurrencePanel();
        editor.initializeOccurrence(topic,t,parent);
        dataPanel.removeAll();
        dataPanel.add(editor,java.awt.BorderLayout.CENTER);
        dataPanel.validate();
        this.repaint();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox typeComboBox;
    // End of variables declaration//GEN-END:variables
    
}
