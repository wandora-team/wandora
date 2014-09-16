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
 */
package org.wandora.application.tools.extractors.rekognition;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.tools.extractors.rekognition.RekognitionConfiguration.AUTH_KEY;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
class RekognitionConfigurationUI extends javax.swing.JPanel {

    private Wandora wandora = null;
    private JDialog dialog = null;
    
    private RekognitionConfiguration configuration;
    
    private final HashMap<String,JCheckBox> faceJobs;

    /**
     * Constructs a configuration UI, with the active tab identified by tab. A 
     * null value hides the tab pane.
     * @param tab 
     */
    public RekognitionConfigurationUI(String tab){
        initComponents();
        
        if(tab == null){
            rekognitionTabs.setVisible(false);
        } else {
            switch(tab){
                case "face":
                    rekognitionTabs.setSelectedComponent(faceDetectorTab);
            }
        }
        
        faceCelebrityTresholdLabel.setFont(UIConstants.buttonLabelFont);
        UIConstants.setFancyFont(faceCelebrityTresholdLabel);
                
        //Hook up checkboxes
        faceJobs = new HashMap<>();
        faceJobs.put("age",            faceJobCheckAge);
        faceJobs.put("aggressive",     faceJobCheckAggressive);
        faceJobs.put("beauty",         faceJobCheckBeauty);
        faceJobs.put("emotion",        faceJobCheckEmotion);
        faceJobs.put("eye_closed",     faceJobCheckEyeClosed);
        faceJobs.put("gender",         faceJobCheckGender);
        faceJobs.put("glass",          faceJobCheckGlass);
        faceJobs.put("mouth_open_wide",faceJobCheckMouthOpenWide);
        faceJobs.put("part",           faceJobCheckPart);
        faceJobs.put("part_detail",    faceJobCheckPartDetail);
        faceJobs.put("race",           faceJobCheckRace);
        faceJobs.put("celebrity",      faceJobCheckCelebrity);
        
        configuration = AbstractRekognitionExtractor.getConfiguration();
        
        if(configuration.auth == null){
            this.forgetButton.setEnabled(false);
        }
        
        for(String key: faceJobs.keySet()){
            if(configuration.jobs.contains(key))
                faceJobs.get(key).setSelected(true);
            else
                faceJobs.get(key).setSelected(false);
        }
        
        faceAssociateCelebrity.setSelected(configuration.celebrityNaming);
        faceCelebrityTreshold.setValue(new Double(configuration.celebrityTreshold));
    }
    
    public void open(Wandora w, int height){
        wandora = w;
        dialog = new JDialog(w, true);
        dialog.setSize(400, height);
        dialog.add(this);
        dialog.setTitle("ReKognition API extractor configuration");
        UIBox.centerWindow(dialog, w);

        faceCelebrityDetails.setVisible(faceJobCheckCelebrity.isSelected());
        
        dialog.setVisible(true); 
    }
    
    private void writeConfiguration(){
        
        RekognitionConfiguration c = null;

        HashMap<AUTH_KEY,String> auth = (configuration.auth == null) ?
                this.getAPIKeys() : configuration.auth;
        
        Component selectedTab = rekognitionTabs.getSelectedComponent();
        if (selectedTab.equals(faceDetectorTab)){
            
            ArrayList<String> jobs = this.getFaceJobs();
            boolean celebrityNaming;
            double celebrityTreshold;
            
            try {
                celebrityNaming = faceAssociateCelebrity.isSelected();
                celebrityTreshold = ((Number)faceCelebrityTreshold.getValue()).floatValue();
            } catch (NumberFormatException e) {
                celebrityNaming = false;
                celebrityTreshold = 0;
            }
            
            c = new RekognitionConfiguration(jobs, celebrityNaming, celebrityTreshold, auth);
            
        }
        
        AbstractRekognitionExtractor.setConfiguration(c);
        
    }
    
    private ArrayList<String> getFaceJobs() {
        
        ArrayList<String> jobs = new ArrayList<>();
        
        for (String jobName : faceJobs.keySet()) {
            if (faceJobs.get(jobName).isSelected()) {
                jobs.add(jobName);
            }
        }
        
        return jobs;
        
    }
    
    private HashMap<AUTH_KEY,String> getAPIKeys() {

            
        RekognitionAuthenticationDialog authDialog = new RekognitionAuthenticationDialog();
        authDialog.open(wandora);
        forgetButton.setEnabled(true);

        return authDialog.getAuth();
                
    }
    
    public void forgetAuthorization() {
        
        configuration.auth = null;
        AbstractRekognitionExtractor.setConfiguration(configuration);
        
        forgetButton.setEnabled(false);
    }
    
    void hideTabs() {
        rekognitionTabs.setVisible(false);
        dialog.setSize(400, 100);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rekognitionTabs = new SimpleTabbedPane();
        faceDetectorTab = new javax.swing.JPanel();
        faceDetectorJobsPanel = new javax.swing.JPanel();
        faceJobCheckAggressive = new SimpleCheckBox();
        faceJobCheckPart = new SimpleCheckBox();
        faceJobCheckPartDetail = new SimpleCheckBox();
        faceJobCheckGender = new SimpleCheckBox();
        faceJobCheckEmotion = new SimpleCheckBox();
        faceJobCheckRace = new SimpleCheckBox();
        faceJobCheckAge = new SimpleCheckBox();
        faceJobCheckGlass = new SimpleCheckBox();
        faceJobCheckMouthOpenWide = new SimpleCheckBox();
        faceJobCheckEyeClosed = new SimpleCheckBox();
        faceJobCheckBeauty = new SimpleCheckBox();
        faceJobCheckCelebrity = new SimpleCheckBox();
        faceCelebrityDetails = new javax.swing.JPanel();
        faceAssociateCelebrity = new SimpleCheckBox();
        faceCelebrityTreshold = new javax.swing.JFormattedTextField();
        faceCelebrityTresholdLabel = new SimpleLabel();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();
        forgetButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        faceDetectorTab.setLayout(new java.awt.GridBagLayout());

        faceDetectorJobsPanel.setLayout(new java.awt.GridBagLayout());

        faceJobCheckAggressive.setText("Aggressive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckAggressive, gridBagConstraints);

        faceJobCheckPart.setText("Part Positions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckPart, gridBagConstraints);

        faceJobCheckPartDetail.setText("Part Details");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckPartDetail, gridBagConstraints);

        faceJobCheckGender.setText("Gender");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckGender, gridBagConstraints);

        faceJobCheckEmotion.setSelected(true);
        faceJobCheckEmotion.setText("Emotion");
        faceJobCheckEmotion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                faceJobCheckEmotionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckEmotion, gridBagConstraints);

        faceJobCheckRace.setText("Race");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckRace, gridBagConstraints);

        faceJobCheckAge.setText("Age");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckAge, gridBagConstraints);

        faceJobCheckGlass.setText("Glasses");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckGlass, gridBagConstraints);

        faceJobCheckMouthOpenWide.setText("Mouth Open");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckMouthOpenWide, gridBagConstraints);

        faceJobCheckEyeClosed.setText("Eyes Closed");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckEyeClosed, gridBagConstraints);

        faceJobCheckBeauty.setText("Beauty");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckBeauty, gridBagConstraints);

        faceJobCheckCelebrity.setSelected(true);
        faceJobCheckCelebrity.setText("Celebrity");
        faceJobCheckCelebrity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                faceJobCheckCelebrityActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckCelebrity, gridBagConstraints);

        faceCelebrityDetails.setLayout(new java.awt.GridBagLayout());

        faceAssociateCelebrity.setSelected(true);
        faceAssociateCelebrity.setText("Attempt Celebrity Naming");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        faceCelebrityDetails.add(faceAssociateCelebrity, gridBagConstraints);

        faceCelebrityTreshold.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        faceCelebrityTreshold.setText("0.50");
        faceCelebrityTreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                faceCelebrityTresholdActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        faceCelebrityDetails.add(faceCelebrityTreshold, gridBagConstraints);

        faceCelebrityTresholdLabel.setText("Treshold");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        faceCelebrityDetails.add(faceCelebrityTresholdLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 0, 0);
        faceDetectorJobsPanel.add(faceCelebrityDetails, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        faceDetectorTab.add(faceDetectorJobsPanel, gridBagConstraints);

        rekognitionTabs.addTab("Face Detector", faceDetectorTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(rekognitionTabs, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonFillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        okButton.setText("Save");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        forgetButton.setText("Forget API credentials");
        forgetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forgetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        buttonPanel.add(forgetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void forgetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forgetButtonActionPerformed
        forgetAuthorization();
        forgetButton.setEnabled(false);        
    }//GEN-LAST:event_forgetButtonActionPerformed

    private void faceJobCheckCelebrityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_faceJobCheckCelebrityActionPerformed
        boolean b = faceJobCheckCelebrity.isSelected();
        faceCelebrityDetails.setVisible(b);
        if(!b){
            faceAssociateCelebrity.setSelected(false);
        }
    }//GEN-LAST:event_faceJobCheckCelebrityActionPerformed

    private void faceCelebrityTresholdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_faceCelebrityTresholdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_faceCelebrityTresholdActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        this.writeConfiguration();
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void faceJobCheckEmotionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_faceJobCheckEmotionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_faceJobCheckEmotionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox faceAssociateCelebrity;
    private javax.swing.JPanel faceCelebrityDetails;
    private javax.swing.JFormattedTextField faceCelebrityTreshold;
    private javax.swing.JLabel faceCelebrityTresholdLabel;
    private javax.swing.JPanel faceDetectorJobsPanel;
    private javax.swing.JPanel faceDetectorTab;
    private javax.swing.JCheckBox faceJobCheckAge;
    private javax.swing.JCheckBox faceJobCheckAggressive;
    private javax.swing.JCheckBox faceJobCheckBeauty;
    private javax.swing.JCheckBox faceJobCheckCelebrity;
    private javax.swing.JCheckBox faceJobCheckEmotion;
    private javax.swing.JCheckBox faceJobCheckEyeClosed;
    private javax.swing.JCheckBox faceJobCheckGender;
    private javax.swing.JCheckBox faceJobCheckGlass;
    private javax.swing.JCheckBox faceJobCheckMouthOpenWide;
    private javax.swing.JCheckBox faceJobCheckPart;
    private javax.swing.JCheckBox faceJobCheckPartDetail;
    private javax.swing.JCheckBox faceJobCheckRace;
    private javax.swing.JButton forgetButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTabbedPane rekognitionTabs;
    // End of variables declaration//GEN-END:variables


}
