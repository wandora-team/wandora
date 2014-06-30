/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * TreeAssociationTypesEditor.java
 *
 * Created on 13. helmikuuta 2006, 13:13
 */

package org.wandora.application.gui;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.utils.Options;
import org.wandora.utils.GripCollections;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.*;
import org.wandora.application.*;
import org.wandora.utils.*;
import org.wandora.application.gui.simple.*;
/**
 *
 * @author  olli
 */
public class TreeAssociationTypesEditor extends javax.swing.JPanel {
    
    private TopicTree.TreeAssociation[] associations;
    private boolean cancelled=true;
    
    private Component parent;
    private Wandora admin;
    
    /** Creates new form TreeAssociationTypesEditor */
    public TreeAssociationTypesEditor(TopicTree.TreeAssociation[] allAssociations,Component parent,Wandora admin)  throws TopicMapException {
        this.associations=allAssociations;
        this.parent=parent;
        this.admin=admin;
        initComponents();
        updateAssociationsPanel();
    }




    public static TopicTree.TreeAssociation[] readAssociationTypes(Options options){
        int counter=0;
        Vector<TopicTree.TreeAssociation> v=new Vector<TopicTree.TreeAssociation>();
        while(true){
            String name=options.get("topictreetypes.type["+counter+"].name");
            if(name==null) break;
            String subSI=options.get("topictreetypes.type["+counter+"].subsi");
            String assocSI=options.get("topictreetypes.type["+counter+"].assocsi");
            String superSI=options.get("topictreetypes.type["+counter+"].supersi");
            String icon=options.get("topictreetypes.type["+counter+"].icon");
            v.add(new TopicTree.TreeAssociation(name,subSI,assocSI,superSI,icon));
            counter++;
        }
        return GripCollections.collectionToArray(v,TopicTree.TreeAssociation.class);
    }




    public static void writeAssociationTypes(Options options, TopicTree.TreeAssociation[] associations){
        for(int i=0;i<associations.length;i++){
            options.put("topictreetypes.type["+i+"].name",associations[i].name);
            options.put("topictreetypes.type["+i+"].subsi",associations[i].subSI);
            options.put("topictreetypes.type["+i+"].assocsi",associations[i].assocSI);
            options.put("topictreetypes.type["+i+"].supersi",associations[i].superSI);
            options.put("topictreetypes.type["+i+"].icon",associations[i].icon);
        }
        int counter=associations.length;
        while(true){
            String name=options.get("topictreetypes.type["+counter+"].name");
            if(name==null) break;
            options.put("topictreetypes.type["+counter+"].name",null);
            options.put("topictreetypes.type["+counter+"].subsi",null);
            options.put("topictreetypes.type["+counter+"].assocsi",null);
            options.put("topictreetypes.type["+counter+"].supersi",null);
            options.put("topictreetypes.type["+counter+"].icon",null);
            counter++;
        }
    }



    
    private void updateAssociationsPanel() throws TopicMapException {
        associationsPanel.removeAll();
        for(int i=0;i<associations.length;i++){
            GridBagConstraints gbc=new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.weightx=1.0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            TreeAssociationTypeEditorPanel tatep=new TreeAssociationTypeEditorPanel(associations[i].name,
                    associations[i].subSI,associations[i].assocSI,associations[i].superSI,associations[i].icon,this,admin);
            associationsPanel.add(tatep,gbc);
        }        
        addFillerPanel();
    }



    private void addFillerPanel(){
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=associationsPanel.getComponentCount();
        gbc.fill=GridBagConstraints.VERTICAL;
        gbc.weighty=1.0;
        JPanel panel=new JPanel();
        associationsPanel.add(panel,gbc);        
    }




    public void deleted(TreeAssociationTypeEditorPanel editor){
        TreeAssociationTypeEditorPanel[] panels=new TreeAssociationTypeEditorPanel[associationsPanel.getComponentCount()-2];
        int counter=0;
        TreeAssociationTypeEditorPanel panel = null;
        GridBagConstraints gbc = null;
        for(int i=0;i<associationsPanel.getComponentCount()-1;i++){
            panel=(TreeAssociationTypeEditorPanel)associationsPanel.getComponent(i);
            if(panel!=editor) panels[counter++]=panel;
        }
        associationsPanel.removeAll();
        for(int i=0;i<panels.length;i++){
            gbc=new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.weightx=1.0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            associationsPanel.add(panels[i],gbc);
        }
        addFillerPanel();
        this.revalidate();
        this.repaint();
    }



    public TopicTree.TreeAssociation[] getAssociationTypes() throws TopicMapException {
        Vector<TopicTree.TreeAssociation> v=new Vector<TopicTree.TreeAssociation>();
        for(int i=0;i<associationsPanel.getComponentCount()-1;i++){
            TreeAssociationTypeEditorPanel panel=(TreeAssociationTypeEditorPanel)associationsPanel.getComponent(i);
            v.add(panel.getAssociation());
        }
        return GripCollections.collectionToArray(v,TopicTree.TreeAssociation.class);
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
        associationsPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        addButton = new SimpleButton();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        associationsPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(associationsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        addButton.setText("Add");
        addButton.setMaximumSize(new java.awt.Dimension(70, 23));
        addButton.setMinimumSize(new java.awt.Dimension(70, 23));
        addButton.setPreferredSize(new java.awt.Dimension(70, 23));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 26);
        jPanel1.add(addButton, gridBagConstraints);

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
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        jPanel1.add(okButton, gridBagConstraints);

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
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        jPanel1.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelled=true;
        parent.setVisible(false);

    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        cancelled=false;
        parent.setVisible(false);

    }//GEN-LAST:event_okButtonActionPerformed

    public boolean wasCancelled(){
        return cancelled;
    }
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        associationsPanel.remove(associationsPanel.getComponentCount()-1);
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=associationsPanel.getComponentCount();
        gbc.weightx=1.0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        try{
            TreeAssociationTypeEditorPanel tatep=new TreeAssociationTypeEditorPanel("","","","","",this,admin);
            associationsPanel.add(tatep,gbc);
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
        addFillerPanel();
        this.revalidate();
        this.repaint();
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel associationsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
    
}
