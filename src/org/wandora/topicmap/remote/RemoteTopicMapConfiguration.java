/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 *
 * RemoteTopicMapConfiguration.java
 *
 * Created on 22. helmikuuta 2006, 17:06
 */

package org.wandora.topicmap.remote;


import org.wandora.utils.Options;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;
import java.util.*;

import static org.wandora.utils.Tuples.*;
import javax.swing.*;


/**
 *
 * @author  akivela
 */
public class RemoteTopicMapConfiguration extends TopicMapConfigurationPanel {
    public final static String OPTIONS_PREFIX = "options.remoteconnections.connection";
    
    private Options options;
    private Wandora admin = null;
    protected DefaultListModel listModel;
    protected JDialog newConDialog;
    protected int editingIndex = -1;
    
    
    /** Creates new form RemoteTopicMapConfiguration */
    public RemoteTopicMapConfiguration(Wandora admin, Options options) {
        this.admin = admin;
        listModel=new DefaultListModel();
        this.options=options;
        initialize(options);
        initComponents();
    }
    
    
    public void initialize(Options options){
        Collection<RemoteTopicMapConfiguration.StoredConnection> connections=
                new Vector<RemoteTopicMapConfiguration.StoredConnection>();
        int counter=0;
        while(true){
            String name=options.get(OPTIONS_PREFIX+"["+counter+"].name");
            if(name==null) break;
            String user=options.get(OPTIONS_PREFIX+"["+counter+"].user");
            String pass=options.get(OPTIONS_PREFIX+"["+counter+"].pass");
            String host=options.get(OPTIONS_PREFIX+"["+counter+"].host");
            String port=options.get(OPTIONS_PREFIX+"["+counter+"].port");
            String map=options.get(OPTIONS_PREFIX+"["+counter+"].map");
            connections.add(RemoteTopicMapConfiguration.StoredConnection.known(name,host,port,map,user,pass));
            counter++;
        }
        setConnections(connections);
    }
    
    
    public void setConnections(Collection<StoredConnection> cs){
        listModel.clear();
        for(StoredConnection c : cs){
            listModel.addElement(c);
        }
    }
    public StoredConnection getSelectedConnection(){
        StoredConnection sc=(StoredConnection)connectionsList.getSelectedValue();
        return sc; // can be null if nothing selected
    }
    public Collection<StoredConnection> getAllConnections(){
        Vector<StoredConnection> ret=new Vector<StoredConnection>();
        for(int i=0;i<listModel.getSize();i++){
            ret.add((StoredConnection)listModel.getElementAt(i));
        }
        return ret;
    }
    
    public Object getParameters(){
        writeOptions(options);
        StoredConnection sc=getSelectedConnection();
        if(sc==null) return null;
        return sc;
    }
    
    public void writeOptions(Options options){
        int counter=0;
        while(true){
            String name=options.get(OPTIONS_PREFIX+"["+counter+"].name");
            if(name==null) break;
            options.put(OPTIONS_PREFIX+"["+counter+"].name",null);
            options.put(OPTIONS_PREFIX+"["+counter+"].host",null);
            options.put(OPTIONS_PREFIX+"["+counter+"].port",null);
            options.put(OPTIONS_PREFIX+"["+counter+"].map",null);
            options.put(OPTIONS_PREFIX+"["+counter+"].user",null);
            options.put(OPTIONS_PREFIX+"["+counter+"].pass",null);
            counter++;
        }
        Collection<RemoteTopicMapConfiguration.StoredConnection> connections=getAllConnections();
        counter=0;
        for(RemoteTopicMapConfiguration.StoredConnection sc : connections){
            options.put(OPTIONS_PREFIX+"["+counter+"].name",sc.name);
            options.put(OPTIONS_PREFIX+"["+counter+"].user",sc.user);
            options.put(OPTIONS_PREFIX+"["+counter+"].pass",sc.pass);
            options.put(OPTIONS_PREFIX+"["+counter+"].host",sc.host);
            options.put(OPTIONS_PREFIX+"["+counter+"].port",sc.port);    
            options.put(OPTIONS_PREFIX+"["+counter+"].map",sc.map); 
            counter++;
        }
    }
    
    
    protected void clearNewConPanel(){
        nameTextField.setText("");
        hostTextField.setText("");
        portTextField.setText("");
        mapTextField.setText("");
        userTextField.setText("");
        rememberCheckBox.setSelected(false);
        passTextField.setEnabled(false);
    }
    
  
    protected void openNewConPanel(){
        java.awt.Container parent=this.getParent();
        while( parent!=null && !(parent instanceof java.awt.Frame) && !(parent instanceof java.awt.Dialog) ){
            parent=parent.getParent();
        }
        if(parent==null) newConDialog=new JDialog();
        else if(parent instanceof java.awt.Frame) newConDialog=new JDialog((java.awt.Frame)parent,true);
        else newConDialog=new JDialog((java.awt.Dialog)parent,true);
        newConDialog.getContentPane().add(newConPanel);
        newConDialog.setSize(400,280);
        if(admin != null) admin.centerWindow(newConDialog, 0, +12);
        newConDialog.setTitle("Edit remote layer configuration");
        newConDialog.setVisible(true);
    }

    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        newConPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        nameLabel = new org.wandora.application.gui.simple.SimpleLabel();
        nameTextField = new org.wandora.application.gui.simple.SimpleField();
        jSeparator1 = new javax.swing.JSeparator();
        hostLabel = new org.wandora.application.gui.simple.SimpleLabel();
        hostTextField = new org.wandora.application.gui.simple.SimpleField();
        portLabel = new org.wandora.application.gui.simple.SimpleLabel();
        portTextField = new org.wandora.application.gui.simple.SimpleField();
        mapLabel = new org.wandora.application.gui.simple.SimpleLabel();
        mapTextField = new org.wandora.application.gui.simple.SimpleField();
        userLabel = new org.wandora.application.gui.simple.SimpleLabel();
        userTextField = new org.wandora.application.gui.simple.SimpleField();
        rememberLabel = new org.wandora.application.gui.simple.SimpleLabel();
        rememberCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        passLabel = new org.wandora.application.gui.simple.SimpleLabel();
        passTextField = new org.wandora.application.gui.simple.SimpleField();
        newConButtonsPanel = new javax.swing.JPanel();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        connectionsList = new JList(listModel);
        buttonPanel = new javax.swing.JPanel();
        newButton = new org.wandora.application.gui.simple.SimpleButton();
        editButton = new org.wandora.application.gui.simple.SimpleButton();
        deleteButton = new org.wandora.application.gui.simple.SimpleButton();

        newConPanel.setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        nameLabel.setText("Remote name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 5);
        jPanel2.add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 7);
        jPanel2.add(nameTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        newConPanel.add(jPanel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 7, 5, 7);
        newConPanel.add(jSeparator1, gridBagConstraints);

        hostLabel.setText("Host");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 5);
        newConPanel.add(hostLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 7);
        newConPanel.add(hostTextField, gridBagConstraints);

        portLabel.setText("Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 5);
        newConPanel.add(portLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 7);
        newConPanel.add(portTextField, gridBagConstraints);

        mapLabel.setText("Map");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 5);
        newConPanel.add(mapLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 7);
        newConPanel.add(mapTextField, gridBagConstraints);

        userLabel.setText("User");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 5);
        newConPanel.add(userLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 7);
        newConPanel.add(userTextField, gridBagConstraints);

        rememberLabel.setText("Remember password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 5);
        newConPanel.add(rememberLabel, gridBagConstraints);

        rememberCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rememberCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        newConPanel.add(rememberCheckBox, gridBagConstraints);

        passLabel.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 2, 5);
        newConPanel.add(passLabel, gridBagConstraints);

        passTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 7);
        newConPanel.add(passTextField, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        cancelButton.setMaximumSize(new java.awt.Dimension(65, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(65, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(65, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newConCancelButtonActionPerformed(evt);
            }
        });
        newConButtonsPanel.add(cancelButton);

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        okButton.setMinimumSize(new java.awt.Dimension(65, 23));
        okButton.setPreferredSize(new java.awt.Dimension(65, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newConOkButtonActionPerformed(evt);
            }
        });
        newConButtonsPanel.add(okButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(8, 2, 7, 2);
        newConPanel.add(newConButtonsPanel, gridBagConstraints);

        setLayout(new java.awt.GridBagLayout());

        connectionsList.setFont(UIConstants.plainFont);
        connectionsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                proposeLayerName(evt);
            }
        });
        jScrollPane1.setViewportView(connectionsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        add(jScrollPane1, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridLayout(3, 1, 0, 5));

        newButton.setText("New");
        newButton.setMinimumSize(new java.awt.Dimension(57, 21));
        newButton.setPreferredSize(new java.awt.Dimension(65, 21));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(newButton);

        editButton.setText("Edit");
        editButton.setPreferredSize(new java.awt.Dimension(65, 21));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(editButton);

        deleteButton.setText("Delete");
        deleteButton.setPreferredSize(new java.awt.Dimension(65, 21));
        buttonPanel.add(deleteButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void proposeLayerName(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_proposeLayerName
        try {
            NewTopicMapPanel ntp = (NewTopicMapPanel) UIBox.getParentComponentByClass("org.wandora.application.gui.NewTopicMapPanel", this, 5);
            if(ntp != null) {
                ntp.proposeName(connectionsList.getSelectedValue().toString());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_proposeLayerName

    public TopicMapConfigurationPanel getEditConfigurationPanel(Object params){
        newConButtonsPanel.setVisible(false);
        final StoredConnection sc=(StoredConnection)params;
        initNewConPanel(sc);
        
        return new TopicMapConfigurationPanel(){
            {
                this.setLayout(new java.awt.BorderLayout());
                this.add(newConPanel);
                this.revalidate();
            }
            
            public Object getParameters(){
                StoredConnection sc=makeNewStoredConnection();
                return sc;
            }
        };
    }
    
    private void initNewConPanel(StoredConnection sc){
        clearNewConPanel();
        nameTextField.setText(sc.name);
        hostTextField.setText(sc.host);
        portTextField.setText(sc.port);
        mapTextField.setText(sc.map);
        userTextField.setText(sc.user);
        if(sc.pass.length()>0){
            rememberCheckBox.setSelected(true);
            passTextField.setText(sc.pass);
            passTextField.setEnabled(true);
        }
        else {
            rememberCheckBox.setSelected(false);
            passTextField.setText("");
            passTextField.setEnabled(false);                
        }        
    }
    
    
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        StoredConnection sc=(StoredConnection)connectionsList.getSelectedValue();
        if(sc==null){WandoraOptionPane.showMessageDialog(this,"Select connection first");return;}
        initNewConPanel(sc);
        editingIndex=connectionsList.getSelectedIndex();
        openNewConPanel();
    }//GEN-LAST:event_editButtonActionPerformed

    private StoredConnection makeNewStoredConnection(){
        StoredConnection sc=null;
        String name=nameTextField.getText().trim();
        String host=hostTextField.getText().trim();
        String port=portTextField.getText().trim();
        String map=mapTextField.getText().trim();
        String user=userTextField.getText().trim();
        String pass="";
        if(rememberCheckBox.isSelected()) pass=passTextField.getText().trim();
        if(name.length()==0) {WandoraOptionPane.showMessageDialog(newConDialog,"Enter connection name"); return null;}
        if(host.length()==0) {WandoraOptionPane.showMessageDialog(newConDialog,"Enter host address"); return null;}
        if(port.length()==0) {WandoraOptionPane.showMessageDialog(newConDialog,"Enter port number"); return null;}
        if(map.length()==0) {WandoraOptionPane.showMessageDialog(newConDialog,"Enter map name"); return null;}
        if(user.length()==0) {WandoraOptionPane.showMessageDialog(newConDialog,"Enter user name"); return null;}
        sc=StoredConnection.known(name, host, port, map, user, pass);
        return sc;
    }
    
    private void newConOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newConOkButtonActionPerformed
        StoredConnection sc=makeNewStoredConnection();
        if(sc==null) return;
        if(editingIndex!=-1) listModel.setElementAt(sc, editingIndex);
        else listModel.addElement(sc);
        newConDialog.setVisible(false);
    }//GEN-LAST:event_newConOkButtonActionPerformed

    private void newConCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newConCancelButtonActionPerformed
        newConDialog.setVisible(false);
    }//GEN-LAST:event_newConCancelButtonActionPerformed

    private void rememberCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rememberCheckBoxActionPerformed
        passTextField.setEnabled(rememberCheckBox.isSelected());
    }//GEN-LAST:event_rememberCheckBoxActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        clearNewConPanel();
        editingIndex=-1;
        openNewConPanel();
    }//GEN-LAST:event_newButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList connectionsList;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel mapLabel;
    private javax.swing.JTextField mapTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton newButton;
    private javax.swing.JPanel newConButtonsPanel;
    private javax.swing.JPanel newConPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel passLabel;
    private javax.swing.JTextField passTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JCheckBox rememberCheckBox;
    private javax.swing.JLabel rememberLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables
    
    
    

    public static class StoredConnection {
        public String name;
        public String host;
        public String port;
        public String map;
        public String user;
        public String pass;
        public StoredConnection(){

        }
        public static StoredConnection known(String name,String host,String port,String map,String user,String password){
            StoredConnection c=new StoredConnection();
            c.name=name;
            c.host=host;
            c.port=port;
            c.map=map;
            c.user=user;
            c.pass=password;
            return c;
        }

        public String toString(){return name;}
    }

}
