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
 * NewTopicPanelExtended.java
 *
 * Created on 29. joulukuuta 2005, 22:04
 */

package org.wandora.application.gui;


import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;
import javax.swing.*;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.SchemaBox;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 * @author  akivela
 */
public class NewTopicPanelExtended extends javax.swing.JPanel {
    
    private boolean accepted = false;
    private JDialog newTopicDialog = null;
    private Context currentContext = null;
    private Wandora wandora = null;
    private TopicMap topicmap = null;
    
    
    
    /** Creates new form NewTopicPanelExtended */
    public NewTopicPanelExtended(Context context) {
        this.wandora = Wandora.getWandora();
        this.topicmap = wandora.getTopicMap();
        this.currentContext = context;
        initComponents();
        selectCurrentTopicAsTypeActionPerformed(null);
        
        newTopicDialog = new JDialog(wandora, "Create new topic", true);
        newTopicDialog.add(this);
        newTopicDialog.pack();
        newTopicDialog.setSize(600, 250);
        wandora.centerWindow(newTopicDialog);
        
        basenameTextField.requestFocusInWindow();
        newTopicDialog.setVisible(true);
    }
    
    
    public boolean getAccepted() {
        return accepted;
    }
    
    
    
    
    
    public Topic createTopic() throws TopicMapException {
        Topic newTopic = null;
        
        String basename = getBasename();
        
        
        if(basename != null && basename.length() > 0) {
            newTopic=topicmap.createTopic();

            if(TMBox.checkBaseNameChange(wandora,newTopic,basename)!=ConfirmResult.yes){
                newTopic.remove();
                return null;
            }
            
            newTopic.setBaseName(basename);
        }
        else {
            WandoraOptionPane.showMessageDialog(wandora, "No basename was given. No topic created!", "No topic created", WandoraOptionPane.WARNING_MESSAGE);
            if(newTopic != null) newTopic.remove();
            return null;
        }
        
        String si = getSI();
        if(si != null && si.length() > 0) {
            if(TMBox.checkSubjectIdentifierChange(wandora,newTopic,topicmap.createLocator(si),true)!=ConfirmResult.yes){
                newTopic.remove();
                return null;
            }
            newTopic.addSubjectIdentifier(new Locator(si));
        }
        else {
            WandoraOptionPane.showMessageDialog(wandora, "No subject identifier was given. No topic created!", "No topic created", WandoraOptionPane.WARNING_MESSAGE);
            newTopic.remove();
            return null;
        }
        
        String sl = getSL();
        if(sl != null && sl.length() > 0) {
            if(TMBox.checkSubjectLocatorChange(wandora,newTopic,sl,true)!=ConfirmResult.yes){
                newTopic.remove();
                return null;
            }
            newTopic.setSubjectLocator(new Locator(sl));
        }

        // --- Types ---
        try {
            Topic typeTopic = ((GetTopicButton) typeTopicButton).getTopic();
            if(typeTopic != null) {
                newTopic.addType(typeTopic);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        try {
            Topic superclassTopic = ((GetTopicButton) superclassButton).getTopic();
            if(superclassTopic != null) {
                SchemaBox.setSuperClass(newTopic, superclassTopic);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        
        return newTopic;
    }
    
    
    
    
    private String getBasename() {
        return basenameTextField.getText().trim();
    }
    
    
    
    
    
    private String getSI() {
        String si = SITextField.getText().trim();
        if(si.length() == 0) {
            si = topicmap.makeSubjectIndicator();
            WandoraOptionPane.showMessageDialog(wandora, "Valid subject identifier was not available. Topic will be given a default subject identifier '"+si+"'.", "Default SI given to new topic", WandoraOptionPane.INFORMATION_MESSAGE);
        }
        try {
            URL siUrl = new URL(si);
        }
        catch(Exception e) {
            si = topicmap.makeSubjectIndicator();
            WandoraOptionPane.showMessageDialog(wandora, "Valid subject identifier was not available. Topic will be given a default subject identifier '"+si+"'.", "Default SI given to new topic", WandoraOptionPane.INFORMATION_MESSAGE);
        }
        return si;
    }
    

    private String getSL() {
        String sl = SLTextField.getText().trim();
        if(sl.length() == 0) return null;
        try {
            URL slUrl = new URL(sl);
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(wandora, "Given subject locator was invalid. Rejecting subject locator.", "Rejecting subject locator", WandoraOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return sl;
    }
    
    
    
    private GetTopicButton getTopicButton() {
        try {
            return new GetTopicButton();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        newTopicTabbedPane = new SimpleTabbedPane();
        identityPanel = new javax.swing.JPanel();
        identityInfoLabel = new SimpleLabel();
        basenameLabel = new org.wandora.application.gui.simple.SimpleLabel();
        basenameTextField = new org.wandora.application.gui.simple.SimpleField();
        SILabel = new org.wandora.application.gui.simple.SimpleLabel();
        SITextField = new org.wandora.application.gui.simple.SimpleField();
        SLLabel = new org.wandora.application.gui.simple.SimpleLabel();
        pickSIFile = new SimpleButton();
        SLTextField = new org.wandora.application.gui.simple.SimpleField();
        pickSLFile = new SimpleButton();
        classPanel = new javax.swing.JPanel();
        jLabel1 = new SimpleLabel();
        typeLabel = new org.wandora.application.gui.simple.SimpleLabel();
        typePanel = new javax.swing.JPanel();
        typeTopicButton = getTopicButton();
        selectCurrentTopicAsType = new SimpleButton();
        clearTypeButton = new SimpleButton();
        superclassLabel1 = new org.wandora.application.gui.simple.SimpleLabel();
        superclassPanel = new javax.swing.JPanel();
        superclassButton = getTopicButton();
        selectCurrentTopicAsSuperclas = new SimpleButton();
        clearSuperclassButton = new SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        dummyPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        identityPanel.setLayout(new java.awt.GridBagLayout());

        identityInfoLabel.setText("<html>To create a topic you need to enter topic's base name at least. If valid subject identifier is not available, a default subject identifier will be added to the topic.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 5);
        identityPanel.add(identityInfoLabel, gridBagConstraints);

        basenameLabel.setText("Base name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        identityPanel.add(basenameLabel, gridBagConstraints);

        basenameTextField.setMinimumSize(new java.awt.Dimension(6, 21));
        basenameTextField.setPreferredSize(new java.awt.Dimension(6, 21));
        basenameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                basenameTextFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        identityPanel.add(basenameTextField, gridBagConstraints);

        SILabel.setText("Subject identifier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        identityPanel.add(SILabel, gridBagConstraints);

        SITextField.setMinimumSize(new java.awt.Dimension(6, 21));
        SITextField.setPreferredSize(new java.awt.Dimension(6, 21));
        SITextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                SITextFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        identityPanel.add(SITextField, gridBagConstraints);

        SLLabel.setText("Subject locator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        identityPanel.add(SLLabel, gridBagConstraints);

        pickSIFile.setText("pick file");
        pickSIFile.setMargin(new java.awt.Insets(0, 8, 0, 8));
        pickSIFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pickSIFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        identityPanel.add(pickSIFile, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        identityPanel.add(SLTextField, gridBagConstraints);

        pickSLFile.setText("pick file");
        pickSLFile.setMargin(new java.awt.Insets(0, 8, 0, 8));
        pickSLFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pickSLFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        identityPanel.add(pickSLFile, gridBagConstraints);

        newTopicTabbedPane.addTab("Identity", identityPanel);

        classPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("<html>By default topic's class is derived from action's context. To remove default class topic click clear button. By default topic has no superclass. Pick context button chooses a topic in context.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 5);
        classPanel.add(jLabel1, gridBagConstraints);

        typeLabel.setText("Class topic");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        classPanel.add(typeLabel, gridBagConstraints);

        typePanel.setLayout(new java.awt.GridBagLayout());

        typeTopicButton.setText("<No topic>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        typePanel.add(typeTopicButton, gridBagConstraints);

        selectCurrentTopicAsType.setText("pick context");
        selectCurrentTopicAsType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectCurrentTopicAsTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        typePanel.add(selectCurrentTopicAsType, gridBagConstraints);

        clearTypeButton.setText("clear");
        clearTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearTypeButtonActionPerformed(evt);
            }
        });
        typePanel.add(clearTypeButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        classPanel.add(typePanel, gridBagConstraints);

        superclassLabel1.setText("Superclass topic");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        classPanel.add(superclassLabel1, gridBagConstraints);

        superclassPanel.setLayout(new java.awt.GridBagLayout());

        superclassButton.setText("<No topic>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        superclassPanel.add(superclassButton, gridBagConstraints);

        selectCurrentTopicAsSuperclas.setText("pick context");
        selectCurrentTopicAsSuperclas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectCurrentTopicAsSuperclasActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        superclassPanel.add(selectCurrentTopicAsSuperclas, gridBagConstraints);

        clearSuperclassButton.setText("clear");
        clearSuperclassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSuperclassButtonActionPerformed(evt);
            }
        });
        superclassPanel.add(clearSuperclassButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        classPanel.add(superclassPanel, gridBagConstraints);

        newTopicTabbedPane.addTab("Class", classPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(newTopicTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(dummyPanel, gridBagConstraints);

        okButton.setText("Create");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accepted(evt);
            }
        });
        okButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                okButtonKeyReleased(evt);
            }
        });
        buttonPanel.add(okButton, new java.awt.GridBagConstraints());

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelled(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    private void accepted(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accepted
        accepted = true;
        if(newTopicDialog != null) newTopicDialog.setVisible(false);
    }//GEN-LAST:event_accepted

    private void cancelled(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelled
        accepted = false;
        if(newTopicDialog != null) newTopicDialog.setVisible(false);
    }//GEN-LAST:event_cancelled

    private void basenameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_basenameTextFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            accepted(null);
        }
    }//GEN-LAST:event_basenameTextFieldKeyReleased

    private void SITextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SITextFieldKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            accepted(null);
        }
    }//GEN-LAST:event_SITextFieldKeyReleased

    private void okButtonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_okButtonKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            accepted(null);
        }
    }//GEN-LAST:event_okButtonKeyReleased

    private void selectCurrentTopicAsTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectCurrentTopicAsTypeActionPerformed
        if(currentContext != null) {
            Iterator i = currentContext.getContextObjects();
            if(i.hasNext()) {
                Object o = i.next();
                if(o instanceof Topic) {
                    Topic t = (Topic) o;
                    try {
                        ((GetTopicButton) typeTopicButton).setTopic(t.getOneSubjectIdentifier().toExternalForm());
                    }
                    catch(Exception e) {
                    }
                }
            }
        }
    }//GEN-LAST:event_selectCurrentTopicAsTypeActionPerformed

    private void clearSuperclassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSuperclassButtonActionPerformed
        try {
            ((GetTopicButton) superclassButton).setTopic((Topic) null);
        }
            catch(Exception e) {
        }
    }//GEN-LAST:event_clearSuperclassButtonActionPerformed

    private void clearTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearTypeButtonActionPerformed
        try {
            ((GetTopicButton) typeTopicButton).setTopic((Topic) null);
        }
            catch(Exception e) {
        }
    }//GEN-LAST:event_clearTypeButtonActionPerformed

    private void selectCurrentTopicAsSuperclasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectCurrentTopicAsSuperclasActionPerformed
        if(currentContext != null) {
            Iterator i = currentContext.getContextObjects();
            if(i.hasNext()) {
                Object o = i.next();
                if(o instanceof Topic) {
                    Topic t = (Topic) o;
                    try {
                        ((GetTopicButton) superclassButton).setTopic(t.getOneSubjectIdentifier().toExternalForm());
                    }
                    catch(Exception e) {
                    }
                }
            }
        }
    }//GEN-LAST:event_selectCurrentTopicAsSuperclasActionPerformed

    private void pickSLFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pickSLFileActionPerformed
        SimpleFileChooser chooser = UIConstants.getFileChooser();
        if(chooser != null) {
            if(chooser.open(wandora, "Select")==SimpleFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if(f != null) {
                    String subject = f.toURI().toString();
                    SLTextField.setText(subject);
                }
            }
        }
    }//GEN-LAST:event_pickSLFileActionPerformed

    private void pickSIFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pickSIFileActionPerformed
        SimpleFileChooser chooser = UIConstants.getFileChooser();
        if(chooser != null) {
            if(chooser.open(wandora, "Select")==SimpleFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if(f != null) {
                    String subject = f.toURI().toString();
                    SITextField.setText(subject);
                }
            }
        }
    }//GEN-LAST:event_pickSIFileActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel SILabel;
    private javax.swing.JTextField SITextField;
    private javax.swing.JLabel SLLabel;
    private javax.swing.JTextField SLTextField;
    private javax.swing.JLabel basenameLabel;
    private javax.swing.JTextField basenameTextField;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel classPanel;
    private javax.swing.JButton clearSuperclassButton;
    private javax.swing.JButton clearTypeButton;
    private javax.swing.JPanel dummyPanel;
    private javax.swing.JLabel identityInfoLabel;
    private javax.swing.JPanel identityPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTabbedPane newTopicTabbedPane;
    private javax.swing.JButton okButton;
    private javax.swing.JButton pickSIFile;
    private javax.swing.JButton pickSLFile;
    private javax.swing.JButton selectCurrentTopicAsSuperclas;
    private javax.swing.JButton selectCurrentTopicAsType;
    private javax.swing.JButton superclassButton;
    private javax.swing.JLabel superclassLabel1;
    private javax.swing.JPanel superclassPanel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JPanel typePanel;
    private javax.swing.JButton typeTopicButton;
    // End of variables declaration//GEN-END:variables
    
}
