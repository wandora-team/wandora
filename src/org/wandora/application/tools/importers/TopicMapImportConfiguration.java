/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * TopicMapImportConfiguration.java
 *
 * Created on 6.10.2011, 17:26:29
 */

package org.wandora.application.tools.importers;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.topicmap.parser.LTMParser;
import org.wandora.topicmap.parser.XTMParser2;
import org.wandora.utils.Options;

/**
 *
 * @author akivela
 */


public class TopicMapImportConfiguration extends javax.swing.JPanel {

    private Wandora wandora = null;
    private boolean wasAccepted = false;
    private JDialog myDialog = null;
    
    
    
    
    /** Creates new form TopicMapImportConfiguration */
    public TopicMapImportConfiguration(Wandora w) {
        wandora = w;
        initComponents();
    }
    
    
    
    public boolean wasAccepted() {
        return wasAccepted;
    }
    
    
    public void openDialog() {
        wasAccepted = false;
        if(myDialog == null) {
            myDialog = new JDialog(wandora, true);
            myDialog.add(this);
            myDialog.setSize(400,400);
            myDialog.setTitle("Topic map import configuration");
            UIBox.centerWindow(myDialog, wandora);
        }
        loadConfiguration();
        myDialog.setVisible(true);
    }
    
    
    public void loadConfiguration() {
        Options o = wandora.getOptions();
        
        // XTM2
        xtm2OccurrenceCheckBox.setSelected(o.getBoolean(XTMParser2.OCCURRENCE_RESOURCE_REF_KEY, false));
        
        // LTM
        ltmAllowSpecialCharsInQNamesCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_ALLOW_SPECIAL_CHARS_IN_QNAMES, LTMParser.ALLOW_SPECIAL_CHARS_IN_QNAMES));
        ltmNewOccurrenceForEachScopeTopicCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_NEW_OCCURRENCE_FOR_EACH_SCOPE, LTMParser.NEW_OCCURRENCE_FOR_EACH_SCOPE));
        ltmRejectTypelessAssociationPlayersCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_REJECT_ROLELESS_MEMBERS, LTMParser.REJECT_ROLELESS_MEMBERS));
        ltmPreferClassAsRoleCheckBox.setSelected( o.getBoolean(LTMParser.OPTIONS_KEY_PREFER_CLASS_AS_ROLE, LTMParser.PREFER_CLASS_AS_ROLE));
        ltmForceUniqueBaseNamesCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_FORCE_UNIQUE_BASENAMES, LTMParser.FORCE_UNIQUE_BASENAMES));
        ltmTrimBasenamesCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_TRIM_BASENAMES, LTMParser.TRIM_BASENAMES));
        ltmOverwriteVariantNamesCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_OVERWRITE_VARIANTS, LTMParser.OVERWRITE_VARIANTS));
        ltmOverwriteBasenamesCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_OVERWRITE_BASENAME, LTMParser.OVERWRITE_BASENAME));
        ltmDebugCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_DEBUG, LTMParser.debug));
        ltmMakeSIfromIDCheckBox.setSelected(o.getBoolean(LTMParser.OPTIONS_KEY_MAKE_SUBJECT_IDENTIFIER_FROM_ID, LTMParser.MAKE_SUBJECT_IDENTIFIER_FROM_ID));
    }

    
    public void saveConfiguration() {
        Options o = wandora.getOptions();
        
        // XTM2
        o.put(XTMParser2.OCCURRENCE_RESOURCE_REF_KEY, boxVal(xtm2OccurrenceCheckBox));
        
        // LTM
        o.put(LTMParser.OPTIONS_KEY_ALLOW_SPECIAL_CHARS_IN_QNAMES, boxVal(ltmAllowSpecialCharsInQNamesCheckBox));
        o.put(LTMParser.OPTIONS_KEY_NEW_OCCURRENCE_FOR_EACH_SCOPE, boxVal(ltmNewOccurrenceForEachScopeTopicCheckBox));
        o.put(LTMParser.OPTIONS_KEY_REJECT_ROLELESS_MEMBERS, boxVal(ltmRejectTypelessAssociationPlayersCheckBox));
        o.put(LTMParser.OPTIONS_KEY_PREFER_CLASS_AS_ROLE, boxVal(ltmPreferClassAsRoleCheckBox));
        o.put(LTMParser.OPTIONS_KEY_FORCE_UNIQUE_BASENAMES, boxVal(ltmForceUniqueBaseNamesCheckBox));
        o.put(LTMParser.OPTIONS_KEY_TRIM_BASENAMES, boxVal(ltmTrimBasenamesCheckBox));
        o.put(LTMParser.OPTIONS_KEY_OVERWRITE_VARIANTS, boxVal(ltmOverwriteVariantNamesCheckBox));
        o.put(LTMParser.OPTIONS_KEY_OVERWRITE_BASENAME, boxVal(ltmOverwriteBasenamesCheckBox));
        o.put(LTMParser.OPTIONS_KEY_DEBUG, boxVal(ltmDebugCheckBox));
        o.put(LTMParser.OPTIONS_KEY_MAKE_SUBJECT_IDENTIFIER_FROM_ID, boxVal(ltmMakeSIfromIDCheckBox));
        
    }
    
    
    private String boxVal(JCheckBox cb) {
        return cb.isSelected() ? "true" : "false";
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

        formatTabbedPane = new SimpleTabbedPane();
        xtm1Panel = new javax.swing.JPanel();
        xtm1PanelInner = new javax.swing.JPanel();
        xtm1Label = new SimpleLabel();
        xtm2Panel = new javax.swing.JPanel();
        xtm2PanelInner = new javax.swing.JPanel();
        xtm2Label = new SimpleLabel();
        xtm2OccurrenceCheckBox = new SimpleCheckBox();
        ltmPanel = new javax.swing.JPanel();
        ltmPanelInner = new javax.swing.JPanel();
        ltmLabel = new SimpleLabel();
        ltmPreferClassAsRoleCheckBox = new SimpleCheckBox();
        ltmForceUniqueBaseNamesCheckBox = new SimpleCheckBox();
        ltmRejectTypelessAssociationPlayersCheckBox = new SimpleCheckBox();
        ltmNewOccurrenceForEachScopeTopicCheckBox = new SimpleCheckBox();
        ltmOverwriteVariantNamesCheckBox = new SimpleCheckBox();
        ltmOverwriteBasenamesCheckBox = new SimpleCheckBox();
        ltmAllowSpecialCharsInQNamesCheckBox = new SimpleCheckBox();
        ltmTrimBasenamesCheckBox = new SimpleCheckBox();
        ltmDebugCheckBox = new SimpleCheckBox();
        ltmMakeSIfromIDCheckBox = new SimpleCheckBox();
        jtmPanel = new javax.swing.JPanel();
        jtmPanelInner = new javax.swing.JPanel();
        jtmLabel = new SimpleLabel();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        xtm1Panel.setLayout(new java.awt.GridBagLayout());

        xtm1PanelInner.setLayout(new java.awt.GridBagLayout());

        xtm1Label.setText("<html>XTM 1.0 format has no configuration options.</html>");
        xtm1PanelInner.add(xtm1Label, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        xtm1Panel.add(xtm1PanelInner, gridBagConstraints);

        formatTabbedPane.addTab("XTM1", xtm1Panel);

        xtm2Panel.setLayout(new java.awt.GridBagLayout());

        xtm2PanelInner.setLayout(new java.awt.GridBagLayout());

        xtm2Label.setText("<html>XTM 2.0 format configuration options</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        xtm2PanelInner.add(xtm2Label, gridBagConstraints);

        xtm2OccurrenceCheckBox.setText("<html>Convert resource reference occurrences to resource data occurrences. If unchecked Wandora converts resource reference occurrences to topics and associations.</html>");
        xtm2OccurrenceCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        xtm2PanelInner.add(xtm2OccurrenceCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        xtm2Panel.add(xtm2PanelInner, gridBagConstraints);

        formatTabbedPane.addTab("XTM2", xtm2Panel);

        ltmPanel.setLayout(new java.awt.GridBagLayout());

        ltmPanelInner.setLayout(new java.awt.GridBagLayout());

        ltmLabel.setText("<html>LTM import options are</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        ltmPanelInner.add(ltmLabel, gridBagConstraints);

        ltmPreferClassAsRoleCheckBox.setText("Prefer class (type) as role.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmPreferClassAsRoleCheckBox, gridBagConstraints);

        ltmForceUniqueBaseNamesCheckBox.setText("Force unique base names.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmForceUniqueBaseNamesCheckBox, gridBagConstraints);

        ltmRejectTypelessAssociationPlayersCheckBox.setText("Reject typeless association players.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmRejectTypelessAssociationPlayersCheckBox, gridBagConstraints);

        ltmNewOccurrenceForEachScopeTopicCheckBox.setText("New occurrence for each scope topic.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmNewOccurrenceForEachScopeTopicCheckBox, gridBagConstraints);

        ltmOverwriteVariantNamesCheckBox.setText("Overwrite existing variant names.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmOverwriteVariantNamesCheckBox, gridBagConstraints);

        ltmOverwriteBasenamesCheckBox.setText("Overwrite existing base names.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmOverwriteBasenamesCheckBox, gridBagConstraints);

        ltmAllowSpecialCharsInQNamesCheckBox.setText("Allow special characters in QNames.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmAllowSpecialCharsInQNamesCheckBox, gridBagConstraints);

        ltmTrimBasenamesCheckBox.setText("Trim base names.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmTrimBasenamesCheckBox, gridBagConstraints);

        ltmDebugCheckBox.setText("Output additional debugging logs.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmDebugCheckBox, gridBagConstraints);

        ltmMakeSIfromIDCheckBox.setText("Make SI from ID");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        ltmPanelInner.add(ltmMakeSIfromIDCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        ltmPanel.add(ltmPanelInner, gridBagConstraints);

        formatTabbedPane.addTab("LTM", ltmPanel);

        jtmPanel.setLayout(new java.awt.GridBagLayout());

        jtmPanelInner.setLayout(new java.awt.GridBagLayout());

        jtmLabel.setText("<html>JTM format has no configuration options.</html>");
        jtmPanelInner.add(jtmLabel, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jtmPanel.add(jtmPanelInner, gridBagConstraints);

        formatTabbedPane.addTab("JTM", jtmPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(formatTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonFillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        okButton.setText("OK");
        okButton.setPreferredSize(new java.awt.Dimension(75, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 23));
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
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    wasAccepted = false;
    if(myDialog != null) {
        myDialog.setVisible(false);
    }
}//GEN-LAST:event_cancelButtonMouseReleased

private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
    wasAccepted = false;
    if(myDialog != null) {
        myDialog.setVisible(false);
    }
}//GEN-LAST:event_okButtonMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTabbedPane formatTabbedPane;
    private javax.swing.JLabel jtmLabel;
    private javax.swing.JPanel jtmPanel;
    private javax.swing.JPanel jtmPanelInner;
    private javax.swing.JCheckBox ltmAllowSpecialCharsInQNamesCheckBox;
    private javax.swing.JCheckBox ltmDebugCheckBox;
    private javax.swing.JCheckBox ltmForceUniqueBaseNamesCheckBox;
    private javax.swing.JLabel ltmLabel;
    private javax.swing.JCheckBox ltmMakeSIfromIDCheckBox;
    private javax.swing.JCheckBox ltmNewOccurrenceForEachScopeTopicCheckBox;
    private javax.swing.JCheckBox ltmOverwriteBasenamesCheckBox;
    private javax.swing.JCheckBox ltmOverwriteVariantNamesCheckBox;
    private javax.swing.JPanel ltmPanel;
    private javax.swing.JPanel ltmPanelInner;
    private javax.swing.JCheckBox ltmPreferClassAsRoleCheckBox;
    private javax.swing.JCheckBox ltmRejectTypelessAssociationPlayersCheckBox;
    private javax.swing.JCheckBox ltmTrimBasenamesCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel xtm1Label;
    private javax.swing.JPanel xtm1Panel;
    private javax.swing.JPanel xtm1PanelInner;
    private javax.swing.JLabel xtm2Label;
    private javax.swing.JCheckBox xtm2OccurrenceCheckBox;
    private javax.swing.JPanel xtm2Panel;
    private javax.swing.JPanel xtm2PanelInner;
    // End of variables declaration//GEN-END:variables
}
