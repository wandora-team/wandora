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


package org.wandora.application.tools.extractors.stanfordner;


import java.io.File;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.utils.Options;

/**
 *
 * @author akivela
 */
public class StanfordNERConfiguration extends javax.swing.JDialog {

	private static final long serialVersionUID = 1L;

	private boolean accepted = false;
    private Wandora wandora = null;
    private Options options = null;
    private StanfordNERClassifier parent = null;
    


    /** Creates new form StanfordNERConfiguration */
    public StanfordNERConfiguration(Wandora w, Options o, StanfordNERClassifier p ) {
        super(w, true);
        wandora = w;
        options = o;
        parent = p;
        initComponents();
        if(o != null && p != null) {
            fileTextField.setText(o.get(p.optionsPath));
        }
        setSize(600,170);
        if(wandora != null) {
            wandora.centerWindow(this);
        }
    }



    public String getSuggestedClassifier() {
        return fileTextField.getText();
    }
    

    public boolean wasAccepted() {
        return accepted;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        configurationPanel = new javax.swing.JPanel();
        infoLabel = new SimpleLabel();
        fieldsPanel = new javax.swing.JPanel();
        fileTextField = new SimpleField();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        browseButton = new SimpleButton();
        restoreButton = new SimpleButton();
        jSeparator1 = new javax.swing.JSeparator();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setTitle("Stanford NER configuration");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        configurationPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setText("<html>Stanford Named Entity Recognizer requires an external file containing sequence classifier. Wandora uses the classifier file addressed below.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        configurationPanel.add(infoLabel, gridBagConstraints);

        fieldsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        fieldsPanel.add(fileTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        configurationPanel.add(fieldsPanel, gridBagConstraints);

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

        browseButton.setText("Browse");
        browseButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        browseButton.setPreferredSize(new java.awt.Dimension(70, 23));
        browseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                browseButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        buttonPanel.add(browseButton, gridBagConstraints);

        restoreButton.setText("Default");
        restoreButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        restoreButton.setPreferredSize(new java.awt.Dimension(70, 23));
        restoreButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                restoreButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(restoreButton, new java.awt.GridBagConstraints());

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        buttonPanel.add(jSeparator1, gridBagConstraints);

        okButton.setText("OK");
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        configurationPanel.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(configurationPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
        accepted = true;
        this.setVisible(false);
    }//GEN-LAST:event_okButtonMouseReleased

    private void restoreButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_restoreButtonMouseReleased
        if(parent != null) {
            fileTextField.setText(parent.defaultClassifier);
        }
    }//GEN-LAST:event_restoreButtonMouseReleased

    private void browseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_browseButtonMouseReleased
        SimpleFileChooser fileChooser = UIConstants.getFileChooser();
        if(fileChooser != null) {
            String current = fileTextField.getText();
            File currentF = new File(current);
            File currentP = currentF;
            if(!currentP.exists() || !currentP.isDirectory()) {
                currentP = currentF.getParentFile();
            }
            if(currentP.exists()) {
                fileChooser.setCurrentDirectory(currentP);
            }
            if( fileChooser.open(this, SimpleFileChooser.OPEN_DIALOG, "Select") == SimpleFileChooser.APPROVE_OPTION ) {
                File file = fileChooser.getSelectedFile();
                String fileName = file.getAbsolutePath();
                fileTextField.setText(fileName);
            }
        }
    }//GEN-LAST:event_browseButtonMouseReleased

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        accepted = false;
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JPanel fieldsPanel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton okButton;
    private javax.swing.JButton restoreButton;
    // End of variables declaration//GEN-END:variables

}
