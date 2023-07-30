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
 */
package org.wandora.application.tools.git;

import javax.swing.JDialog;
import javax.swing.JPasswordField;

import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimplePasswordField;


/**
 *
 * @author akikivela
 */
public class PullUI extends javax.swing.JPanel {


	private static final long serialVersionUID = 1L;

	private JDialog dialog = null;
    private boolean wasAccepted = false;
    
    
    /**
     * Creates new form PullUI
     */
    public PullUI() {
        initComponents();
    }
    
    
    public void openInDialog() {
        if(dialog == null) {
            Wandora wandora = Wandora.getWandora();
            dialog = new JDialog(wandora, true);
            dialog.add(this);
            dialog.setSize(600,170);
            dialog.setTitle("Git pull options");
            wandora.centerWindow(dialog);
        }
        
        wasAccepted = false;
        dialog.setVisible(true);
    }
    
    
    
    
    // -------------------------------------------------------------------------
    

    
    public boolean wasAccepted() {
        return wasAccepted;
    }
    
    
    public String getRemoteUrl() {
        return remoteUrlTextField.getText();
    }
    
    
    public void setRemoteUrl(String username) {
        remoteUrlTextField.setText(username);
    }
    
    
    public String getUsername() {
        return usernameTextField.getText();
    }
    
    public void setUsername(String username) {
        usernameTextField.setText(username);
    }
    
    public String getPassword() {
        return String.valueOf(((JPasswordField) passwordTextField).getPassword());
    }
    
    public void setPassword(String password) {
        passwordTextField.setText(password);
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

        pullPanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        remoteUrl = new SimpleLabel();
        remoteUrlTextField = new SimpleField();
        usernameLabel = new SimpleLabel();
        usernameTextField = new SimpleField();
        passwordLabel = new SimpleLabel();
        passwordTextField = new SimplePasswordField();
        buttonPanel = new javax.swing.JPanel();
        panelFiller = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        pullPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setText("<html>Pull data from remote git repository and update current project.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 6, 4);
        pullPanel.add(infoLabel, gridBagConstraints);

        remoteUrl.setText("Remote URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        pullPanel.add(remoteUrl, gridBagConstraints);

        remoteUrlTextField.setEditable(false);
        remoteUrlTextField.setBackground(new java.awt.Color(238, 238, 238));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pullPanel.add(remoteUrlTextField, gridBagConstraints);

        usernameLabel.setText("Username");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        pullPanel.add(usernameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pullPanel.add(usernameTextField, gridBagConstraints);

        passwordLabel.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        pullPanel.add(passwordLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pullPanel.add(passwordTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(pullPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout panelFillerLayout = new javax.swing.GroupLayout(panelFiller);
        panelFiller.setLayout(panelFillerLayout);
        panelFillerLayout.setHorizontalGroup(
            panelFillerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelFillerLayout.setVerticalGroup(
            panelFillerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(panelFiller, gridBagConstraints);

        okButton.setText("Pull");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton, new java.awt.GridBagConstraints());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        wasAccepted = false;
        if(dialog != null) {
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        wasAccepted = true;
        if(dialog != null) {
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel panelFiller;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField passwordTextField;
    private javax.swing.JPanel pullPanel;
    private javax.swing.JLabel remoteUrl;
    private javax.swing.JTextField remoteUrlTextField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables
}
