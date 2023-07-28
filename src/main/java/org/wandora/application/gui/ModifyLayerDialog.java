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
 * 
 * ModifyLayerDialog.java
 *
 * Created on 14. huhtikuuta 2008, 12:42
 */

package org.wandora.application.gui;




import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.topicmap.undowrapper.UndoTopicMap;
import org.wandora.utils.swing.GuiTools;

/**
 *
 * @author  olli
 */
public class ModifyLayerDialog extends javax.swing.JDialog {
	
	private static final long serialVersionUID = 1L;
	
    
    private TopicMapConfigurationPanel modifyPanel;
    private TopicMapType modifyType;
    private Layer modifyLayer;
    private Wandora wandora;
    
    /** Creates new form ModifyLayerDialog */
    public ModifyLayerDialog(Wandora wandora,Layer modifyLayer) {
        super(wandora, true);
        this.wandora = wandora;
        this.modifyLayer = modifyLayer;
        TopicMap tm = getWrappedTopicMap(modifyLayer.getTopicMap());
        modifyType = TopicMapTypeManager.getType(tm);
        modifyPanel = modifyType.getModifyConfigurationPanel(wandora,wandora.getOptions(),tm);
        initComponents();
        if(modifyPanel!=null) modifyContainerPanel.add(modifyPanel);
        modifyNameTextField.setText(modifyLayer.getName());
        if(modifyPanel==null) {
            emptyPanel.setSize(10,20);
            this.setSize(400, 150);
        }
        else this.setSize(400, 350);
        GuiTools.centerWindow(this, wandora);
    }
    
    
    
    
    private TopicMap getWrappedTopicMap(TopicMap tm) {
        if(tm != null) {
            if(tm.getClass().equals(UndoTopicMap.class)) {
                tm = ((UndoTopicMap) tm).getWrappedTopicMap();
            }
        }
        return tm;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        emptyPanel = new javax.swing.JPanel();
        modifyContainerPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        modifyOKButton = new org.wandora.application.gui.simple.SimpleButton();
        modifyCancelButton = new org.wandora.application.gui.simple.SimpleButton();
        nameLabel = new org.wandora.application.gui.simple.SimpleLabel();
        modifyNameTextField = new org.wandora.application.gui.simple.SimpleField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Configure layer");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        emptyPanel.setMinimumSize(new java.awt.Dimension(10, 2));
        emptyPanel.setPreferredSize(new java.awt.Dimension(10, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(emptyPanel, gridBagConstraints);

        modifyContainerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(modifyContainerPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        modifyOKButton.setText("OK");
        modifyOKButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        modifyOKButton.setMaximumSize(new java.awt.Dimension(70, 23));
        modifyOKButton.setMinimumSize(new java.awt.Dimension(70, 23));
        modifyOKButton.setPreferredSize(new java.awt.Dimension(70, 23));
        modifyOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyOKButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        buttonPanel.add(modifyOKButton, gridBagConstraints);

        modifyCancelButton.setText("Cancel");
        modifyCancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        modifyCancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        modifyCancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        modifyCancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        modifyCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyCancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 5);
        buttonPanel.add(modifyCancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(buttonPanel, gridBagConstraints);

        nameLabel.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        getContentPane().add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        getContentPane().add(modifyNameTextField, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void modifyOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyOKButtonActionPerformed
        Object params=null;
        if(modifyPanel!=null) params=modifyPanel.getParameters();
        ContainerTopicMap layerStack=modifyLayer.getContainer();
        try{
            TopicMap tm=modifyType.modifyTopicMap(getWrappedTopicMap(modifyLayer.getTopicMap()),params);
            
            String name=modifyNameTextField.getText().trim();
            if(name.length()==0) {
                WandoraOptionPane.showMessageDialog(wandora, "Enter name for the layer!");
                return;
            }
            for(Layer l : layerStack.getLayers()){
                if(l.getName().equals(name) && l!=modifyLayer) {
                    WandoraOptionPane.showMessageDialog(wandora, "Layer name is already in use!", null, WandoraOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            this.setVisible(false);
            
            if(tm==modifyLayer.getTopicMap()){
                modifyLayer.setName(name);
            }
            else{
                Layer l=new Layer(tm,name,layerStack);
                l.setVisible(modifyLayer.isVisible());
                l.setReadOnly(modifyLayer.isReadOnly());
                layerStack.setLayer(l,modifyLayer.getZPos());
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
        
    }//GEN-LAST:event_modifyOKButtonActionPerformed

    private void modifyCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyCancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_modifyCancelButtonActionPerformed
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JButton modifyCancelButton;
    private javax.swing.JPanel modifyContainerPanel;
    private javax.swing.JTextField modifyNameTextField;
    private javax.swing.JButton modifyOKButton;
    private javax.swing.JLabel nameLabel;
    // End of variables declaration//GEN-END:variables
    
}