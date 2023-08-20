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
 * TopicTreeRelationsEditor.java
 *
 * Created on 13. helmikuuta 2006, 13:13
 */

package org.wandora.application.gui.tree;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.wandora.application.Wandora;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.GripCollections;
import org.wandora.utils.Options;



/**
 *
 * @author  olli
 */
public class TopicTreeRelationsEditor extends javax.swing.JPanel {
    

	private static final long serialVersionUID = 1L;
	
	private TopicTreeRelation[] relations;
    private boolean cancelled=true;
    
    private Component parent;
    private Wandora wandora;
    
    
    
    /** Creates new form TreeAssociationTypesEditor */
    public TopicTreeRelationsEditor() {
        initComponents();
    }



    public void open(Wandora wandora) {
        this.wandora = wandora;
        relations = readRelationTypes();
        updateRelationsPanel();
        
        JDialog jd=new JDialog(wandora,true);
        jd.setTitle("Configure topic tree relations");
        jd.add(this);
        jd.setSize(800,400);
        parent = jd;
        wandora.centerWindow(jd);
        jd.setVisible(true);
        
        // Blocks till closed.
    }
    
    
    

    public static TopicTreeRelation[] readRelationTypes() {
        int counter = 0;
        Options options = Wandora.getWandora().getOptions();
        List<TopicTreeRelation> v = new ArrayList<>();
        if(options != null) {
            while(true) {
                String name=options.get("trees.type["+counter+"].name");
                if(name==null) break;
                String subSI=options.get("trees.type["+counter+"].subsi");
                String assocSI=options.get("trees.type["+counter+"].assocsi");
                String superSI=options.get("trees.type["+counter+"].supersi");
                String icon=options.get("trees.type["+counter+"].icon");
                v.add(new TopicTreeRelation(name,subSI,assocSI,superSI,icon));
                counter++;
            }
        }
        return GripCollections.collectionToArray(v,TopicTreeRelation.class);
    }




    public static void writeAssociationTypes(TopicTreeRelation[] associations) {
        Options options = Wandora.getWandora().getOptions();
        System.out.println("Writing tree associations: "+associations.length);
        if(options != null) {
            for(int i=0;i<associations.length;i++){
                options.put("trees.type["+i+"].name",associations[i].name);
                options.put("trees.type["+i+"].subsi",associations[i].subSI);
                options.put("trees.type["+i+"].assocsi",associations[i].assocSI);
                options.put("trees.type["+i+"].supersi",associations[i].superSI);
                options.put("trees.type["+i+"].icon",associations[i].icon);
            }
            int counter=associations.length;
            while(true){
                String name=options.get("trees.type["+counter+"].name");
                if(name==null) break;
                options.put("trees.type["+counter+"].name",null);
                options.put("trees.type["+counter+"].subsi",null);
                options.put("trees.type["+counter+"].assocsi",null);
                options.put("trees.type["+counter+"].supersi",null);
                options.put("trees.type["+counter+"].icon",null);
                counter++;
            }
        }
    }



    
    private void updateRelationsPanel() {
        relationsPanel.removeAll();
        for(int i=0; i<relations.length; i++) {
            try {
                GridBagConstraints gbc=new GridBagConstraints();
                gbc.gridx=0;
                gbc.gridy=i;
                gbc.weightx=1.0;
                gbc.weighty=0.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                TopicTreeRelationEditorPanel tatep = 
                    new TopicTreeRelationEditorPanel(
                        relations[i].name,
                        relations[i].subSI,
                        relations[i].assocSI,
                        relations[i].superSI,
                        relations[i].icon,
                        this,
                        wandora
                    );
                relationsPanel.add(tatep,gbc);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        addFillerPanel();
        
        relationsPanel.revalidate();
        relationsPanel.repaint();
    }



    private void addFillerPanel(){
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=relationsPanel.getComponentCount();
        gbc.fill=GridBagConstraints.VERTICAL;
        gbc.weighty=1.0;
        JPanel panel=new JPanel();
        relationsPanel.add(panel,gbc);        
    }




    public void deleted(TopicTreeRelationEditorPanel editor){
        TopicTreeRelationEditorPanel[] panels=new TopicTreeRelationEditorPanel[relationsPanel.getComponentCount()-2];
        int counter=0;
        TopicTreeRelationEditorPanel panel = null;
        GridBagConstraints gbc = null;
        for(int i=0; i<relationsPanel.getComponentCount()-1; i++){
            panel=(TopicTreeRelationEditorPanel)relationsPanel.getComponent(i);
            if(panel!=editor) panels[counter++]=panel;
        }
        relationsPanel.removeAll();
        for(int i=0; i<panels.length; i++){
            gbc=new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.weightx=1.0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            relationsPanel.add(panels[i],gbc);
        }
        addFillerPanel();
        this.revalidate();
        this.repaint();
    }



    public TopicTreeRelation[] getRelationTypes() {
        List<TopicTreeRelation> v = new ArrayList<>();
        for(int i=0; i<relationsPanel.getComponentCount()-1; i++){
            try {
                TopicTreeRelationEditorPanel panel = (TopicTreeRelationEditorPanel)relationsPanel.getComponent(i);
                v.add(panel.getRelation());
            }
            catch(TopicMapException e) {
                e.printStackTrace();
            }
        }
        return GripCollections.collectionToArray(v, TopicTreeRelation.class);
    }


    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        relationsScrollPane = new javax.swing.JScrollPane();
        relationsPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        addButton = new org.wandora.application.gui.simple.SimpleButton();
        fillerPanel = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        relationsPanel.setLayout(new java.awt.GridBagLayout());
        relationsScrollPane.setViewportView(relationsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(relationsScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 26);
        buttonPanel.add(addButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

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
        buttonPanel.add(okButton, gridBagConstraints);

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
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelled=true;
        parent.setVisible(false);

    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        cancelled=false;
        writeAssociationTypes(getRelationTypes());
        parent.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    public boolean wasCancelled(){
        return cancelled;
    }
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        relationsPanel.remove(relationsPanel.getComponentCount()-1);
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=relationsPanel.getComponentCount();
        gbc.weightx=1.0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        try {
            TopicTreeRelationEditorPanel tatep=new TopicTreeRelationEditorPanel("","","","","",this,wandora);
            relationsPanel.add(tatep,gbc);
        }
        catch(TopicMapException tme) {
            tme.printStackTrace(); // TODO EXCEPTION
        }
        addFillerPanel();
        this.revalidate();
        this.repaint();
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel relationsPanel;
    private javax.swing.JScrollPane relationsScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
