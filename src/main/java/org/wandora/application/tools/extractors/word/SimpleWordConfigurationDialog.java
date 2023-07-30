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
 */
package org.wandora.application.tools.extractors.word;

import javax.swing.JDialog;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;

/**
 *
 * @author Eero Lehtonen
 */
class SimpleWordConfigurationDialog extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;

	private Wandora wandora = null;
    private boolean wasAccepted = false;
    private JDialog myDialog = null;
    private SimpleWordConfiguration config;
    private SimpleWordConfiguration newConfig;

    protected boolean wasAccepted() {
        return wasAccepted;
    }

    public SimpleWordConfigurationDialog(Wandora w) {
        wandora = w;
        initComponents();

    }

    public void openDialog(SimpleWordConfiguration c) {
        
        wasAccepted = false;
        if (myDialog == null) {
            myDialog = new JDialog(wandora, true);
            myDialog.add(this);
            myDialog.setSize(320, 320);
            myDialog.setTitle("Simple Word Extractor Configuration");
            UIBox.centerWindow(myDialog, wandora);
        }

        newConfig = (c != null) ? c : new SimpleWordConfiguration();
        toggleRegex.setSelected(newConfig.getRegex());
        toggleCaseSensitive.setSelected(newConfig.getCaseSensitive());
        toggleMatchWords.setSelected(newConfig.getMatchWords());
        toggleBaseName.setSelected(newConfig.getBaseName());
        toggleVariantName.setSelected(newConfig.getVariantName());
        toggleInstanceData.setSelected(newConfig.getInstanceData());

        myDialog.setVisible(true);
    }

    private void saveConfiguration() {
        if (newConfig == null) {
            newConfig = new SimpleWordConfiguration();
        }

        newConfig.setRegex(toggleRegex.isSelected());
        newConfig.setCaseSensitive(toggleCaseSensitive.isSelected());
        newConfig.setMatchWords(toggleMatchWords.isSelected());
        newConfig.setBaseName(toggleBaseName.isSelected());
        newConfig.setVariantName(toggleVariantName.isSelected());
        newConfig.setInstanceData(toggleInstanceData.isSelected());

        config = newConfig;

    }

    protected SimpleWordConfiguration getConfiguration() {
        if(config == null) config = new SimpleWordConfiguration();
        return config;
    }

    ;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        container = new javax.swing.JPanel();
        toggleRegex = new SimpleCheckBox();
        toggleCaseSensitive = new SimpleCheckBox();
        toggleMatchWords = new SimpleCheckBox();
        toggleBaseName = new SimpleCheckBox();
        toggleVariantName = new SimpleCheckBox();
        toggleInstanceData = new SimpleCheckBox();
        submit = new SimpleButton();
        cancel = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        container.setLayout(new java.awt.GridBagLayout());

        toggleRegex.setText("Regex");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleRegex, gridBagConstraints);

        toggleCaseSensitive.setText("Case sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleCaseSensitive, gridBagConstraints);

        toggleMatchWords.setText("Match word");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleMatchWords, gridBagConstraints);

        toggleBaseName.setText("Base Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleBaseName, gridBagConstraints);

        toggleVariantName.setText("Variant Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleVariantName, gridBagConstraints);

        toggleInstanceData.setText("Instance Data");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleInstanceData, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(container, gridBagConstraints);

        submit.setText("OK");
        submit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        add(submit, gridBagConstraints);

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        add(cancel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        wasAccepted = false;
        if (myDialog != null) {
            myDialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelActionPerformed

    private void submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitActionPerformed
        wasAccepted = true;
        saveConfiguration();
        if (myDialog != null) {
            myDialog.setVisible(false);
        }
    }//GEN-LAST:event_submitActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JPanel container;
    private javax.swing.JButton submit;
    private javax.swing.JCheckBox toggleBaseName;
    private javax.swing.JCheckBox toggleCaseSensitive;
    private javax.swing.JCheckBox toggleInstanceData;
    private javax.swing.JCheckBox toggleMatchWords;
    private javax.swing.JCheckBox toggleRegex;
    private javax.swing.JCheckBox toggleVariantName;
    // End of variables declaration//GEN-END:variables
}
