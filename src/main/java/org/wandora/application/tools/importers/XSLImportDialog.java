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
 * XSLImportDialog.java
 *
 * Created on October 4, 2004, 3:22 PM
 */

package org.wandora.application.tools.importers;



import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;

import java.io.*;


/**
 *
 * @author  olli
 */
public class XSLImportDialog extends javax.swing.JDialog {
    

	private static final long serialVersionUID = 1L;

	private Wandora wandora;
    public boolean accept = false;
    
    /**
     * Creates new form XSLImportDialog
     */
    public XSLImportDialog(Wandora w, boolean modal) {
        super(w, modal);
        this.wandora=w;
        initComponents();
        
        Options options = w.getOptions();
        String previousXML = options.get("XSLImport.xml");
        if(previousXML != null) {
            xmlTextField.setText(previousXML);
        }

        String previousXSL = options.get("XSLImport.xsl");
        if(previousXSL != null) {
            xslTextField.setText(previousXSL);
        }

        wandora.centerWindow(this);
        setVisible(true);
    }
    
    
    
    public String getXML() {
        return xmlTextField.getText();
    }
    public String getXSL() {
        return xslTextField.getText();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new SimpleLabel();
        xmlTextField = new org.wandora.application.gui.simple.SimpleField();
        xmlBrowseButton = new SimpleButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new SimpleLabel();
        xslTextField = new org.wandora.application.gui.simple.SimpleField();
        xslBrowseButton = new SimpleButton();
        jPanel1 = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Transform and merge XML");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("XML file or URL");
        jLabel1.setMaximumSize(new java.awt.Dimension(140, 14));
        jLabel1.setMinimumSize(new java.awt.Dimension(140, 14));
        jLabel1.setPreferredSize(new java.awt.Dimension(140, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel2.add(jLabel1, gridBagConstraints);

        xmlTextField.setMinimumSize(new java.awt.Dimension(450, 25));
        xmlTextField.setPreferredSize(new java.awt.Dimension(450, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(xmlTextField, gridBagConstraints);

        xmlBrowseButton.setFont(org.wandora.application.gui.UIConstants.buttonLabelFont);
        xmlBrowseButton.setText("Browse");
        xmlBrowseButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        xmlBrowseButton.setMinimumSize(new java.awt.Dimension(70, 25));
        xmlBrowseButton.setPreferredSize(new java.awt.Dimension(70, 25));
        xmlBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xmlBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel2.add(xmlBrowseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Stylesheet file or URL");
        jLabel2.setMaximumSize(new java.awt.Dimension(140, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(140, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(140, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        xslTextField.setMinimumSize(new java.awt.Dimension(450, 25));
        xslTextField.setPreferredSize(new java.awt.Dimension(450, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(xslTextField, gridBagConstraints);

        xslBrowseButton.setFont(org.wandora.application.gui.UIConstants.buttonLabelFont);
        xslBrowseButton.setText("Browse");
        xslBrowseButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        xslBrowseButton.setMinimumSize(new java.awt.Dimension(70, 25));
        xslBrowseButton.setPreferredSize(new java.awt.Dimension(70, 25));
        xslBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xslBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel3.add(xslBrowseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 10);
        getContentPane().add(jPanel3, gridBagConstraints);

        jPanel1.setMinimumSize(new java.awt.Dimension(165, 27));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setPreferredSize(new java.awt.Dimension(75, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        jPanel1.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(8, 5, 0, 5);
        getContentPane().add(jPanel1, gridBagConstraints);

        setSize(new java.awt.Dimension(631, 165));
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        String xmlInString = getXML();
        if(!(xmlInString.startsWith("http:/") || xmlInString.startsWith("https:/") || xmlInString.startsWith("ftp:/") || xmlInString.startsWith("ftps:/"))) {
            File xmlIn=new File(xmlInString);
            if(!xmlIn.exists()) {
                WandoraOptionPane.showMessageDialog(wandora, "XML source not found.", WandoraOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String xslInString = getXSL();
        if(!(xmlInString.startsWith("http:/") || xmlInString.startsWith("https:/") || xmlInString.startsWith("ftp:/") || xmlInString.startsWith("ftps:/"))) {
            File xslIn=new File(xslInString);
            if(!xslIn.exists()) {
                WandoraOptionPane.showMessageDialog(wandora,"Stylesheet source not found.", WandoraOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try {
            wandora.getOptions().put("XSLImport.xml", getXML());
            wandora.getOptions().put("XSLImport.xsl", getXSL());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        accept = true;
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        accept = false;
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void xslBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xslBrowseButtonActionPerformed
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        if(chooser.open(wandora)==SimpleFileChooser.APPROVE_OPTION){
            xslTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_xslBrowseButtonActionPerformed

    private void xmlBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xmlBrowseButtonActionPerformed
        SimpleFileChooser chooser=UIConstants.getFileChooser();
        if(chooser.open(wandora)==SimpleFileChooser.APPROVE_OPTION){
            xmlTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_xmlBrowseButtonActionPerformed
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton okButton;
    private javax.swing.JButton xmlBrowseButton;
    private javax.swing.JTextField xmlTextField;
    private javax.swing.JButton xslBrowseButton;
    private javax.swing.JTextField xslTextField;
    // End of variables declaration//GEN-END:variables
    
}