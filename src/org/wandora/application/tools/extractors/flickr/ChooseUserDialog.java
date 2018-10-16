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
 * ChooseUserDialog.java
 *
 * Created on 24. huhtikuuta 2008, 12:40
 */

package org.wandora.application.tools.extractors.flickr;

import java.util.ArrayList;
import java.util.List;

import org.wandora.application.Wandora;

/**
 *
 * @author  anttirt
 */
public class ChooseUserDialog extends javax.swing.JDialog {
    

	private static final long serialVersionUID = 1L;


	/** Creates new form ChooseUserDialog */
    public ChooseUserDialog(java.awt.Frame parent, boolean modal, String text) {
        super(parent, modal);
        initComponents();
        cancelled = false;
        this.pack();
        this.setSize(450,200);
        if(parent instanceof Wandora) {
            Wandora w = (Wandora)parent;
            w.centerWindow(this);
        }
        if(text != null)
            wandoraLabel1.setText(text);
    }
    
    
    private boolean cancelled;
    
    
    public boolean wasCancelled() {
        return cancelled;
    }
    
    
    
    public String[] getUserList() {
        String users = this.userNameTextField.getText();
        List<String> processedUserList = new ArrayList<>();
        if(users != null) {
            if(users.indexOf(',') != -1) {
                String[] userList = users.split(",");
                for(int i=0; i<userList.length; i++) {
                    userList[i] = userList[i].trim();
                    if(userList[i].length() > 0) {
                        processedUserList.add(userList[i]);
                    }
                }
            }
            else {
                processedUserList.add(users);
            }
        }            
        return (String[]) processedUserList.toArray(new String[] {});
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        wandoraLabel1 = new org.wandora.application.gui.simple.SimpleLabel();
        userNameTextField = new javax.swing.JTextField();
        buttonPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnCancel = new org.wandora.application.gui.simple.SimpleButton();
        btnOK = new org.wandora.application.gui.simple.SimpleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Choose user dialog");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        wandoraLabel1.setText("<html><body><p>To convert Flickr user profile to a Topic Map you need to identify user profile with a username. Please write Flickr username below. To convert multiple profiles at once use comma (,) character between user names.</p></body></html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        jPanel1.add(wandoraLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 10);
        jPanel1.add(userNameTextField, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(jPanel2, gridBagConstraints);

        btnCancel.setText("Cancel");
        btnCancel.setMargin(new java.awt.Insets(1, 5, 1, 5));
        btnCancel.setMaximumSize(new java.awt.Dimension(70, 23));
        btnCancel.setMinimumSize(new java.awt.Dimension(70, 23));
        btnCancel.setPreferredSize(new java.awt.Dimension(70, 23));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        buttonPanel.add(btnCancel, gridBagConstraints);

        btnOK.setText("OK");
        btnOK.setMargin(new java.awt.Insets(1, 5, 1, 5));
        btnOK.setMaximumSize(new java.awt.Dimension(70, 23));
        btnOK.setMinimumSize(new java.awt.Dimension(70, 23));
        btnOK.setPreferredSize(new java.awt.Dimension(70, 23));
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        buttonPanel.add(btnOK, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
        jPanel1.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        cancelled = false;
        setVisible(false);
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        cancelled = true;
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ChooseUserDialog dialog = new ChooseUserDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
        */
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.wandora.application.gui.simple.SimpleButton btnCancel;
    private org.wandora.application.gui.simple.SimpleButton btnOK;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField userNameTextField;
    private org.wandora.application.gui.simple.SimpleLabel wandoraLabel1;
    // End of variables declaration//GEN-END:variables
    
}
