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
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
public class RekognitionExtractorUI extends javax.swing.JPanel {

    private Wandora wandora = null;
    private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;
    
    private String[] forceUrls = null;
    
    private HashMap<String,JCheckBox> faceJobs;
    
    private static final String API_ROOT = AbstractRekognitionExtractor.API_ROOT;
    
    /**
     * No URLs given - get them from the URL field.
     */
    public RekognitionExtractorUI() {

        this(null);
        
    }
    
    /**
     * 
     * The default constructor for the UI.
     * 
     * @param urls the URLs to use for extraction
     */
    public RekognitionExtractorUI(String[] urls) {
        initComponents();
        
        //Hook up checkboxes
        faceJobs = new HashMap<>();
        faceJobs.put("age", faceJobCheckAge);
        faceJobs.put("aggressive",faceJobCheckAggressive);
        faceJobs.put("beauty",faceJobCheckBeauty);
        faceJobs.put("emotion",faceJobCheckEmotion);
        faceJobs.put("eye_closed",faceJobCheckEyeClosed);
        faceJobs.put("gender",faceJobCheckGender);
        faceJobs.put("glass",faceJobCheckGlass);
        faceJobs.put("mouth_open_wide",faceJobCheckMouthOpenWide);
        faceJobs.put("part",faceJobCheckPart);
        faceJobs.put("part_detail",faceJobCheckPartDetail);
        faceJobs.put("race",faceJobCheckRace);
        faceJobs.put("celebrity",faceJobCheckCelebrity);
        
        
        if(urls != null){
            this.forceUrls = urls;
            this.urlField.setVisible(false);
            this.getContextButton.setVisible(false);
        } else {
            this.urlField.setVisible(true);
            this.getContextButton.setVisible(true);
        }
        
        if(auth == null){
            this.forgetButton.setEnabled(false);
        }
        
        
    }
    
    public boolean wasAccepted(){
        return this.accepted;
    }
    
    public void setAccpeted(boolean b){
        this.accepted = b;
    }
    
    public void open(Wandora w, Context c){
        context = c;
        wandora = w;
        accepted = false;
        dialog = new JDialog(w, true);
        dialog.setSize(800, 500);
        dialog.add(this);
        dialog.setTitle("ReKognition API extractor");
        UIBox.centerWindow(dialog, w);

        faceCelebrityDetails.setVisible(faceJobCheckCelebrity.isSelected());
        
        dialog.setVisible(true); 
    }
    
    public WandoraTool[] getExtractors(RekognitionExtractor tool) throws TopicMapException, NumberFormatException{
        
        ArrayList<WandoraTool> wts = new ArrayList();
        
        //TODO: Handle user input and return an extractor to execute
        
        this.solveAPIKeys();
        
        Component selectedTab = rekognitionTabs.getSelectedComponent();
        
        String apiKey = RekognitionExtractorUI.auth.get(RekognitionAuthenticationDialog.KEY_KEY);
        String apiSecret = RekognitionExtractorUI.auth.get(RekognitionAuthenticationDialog.SECRET_KEY);
        
        if (selectedTab.equals(faceDetectorTab)){
            
            String[] imageUrls;
            if(forceUrls == null){
                imageUrls = new String[]{urlField.getText()};
            } else {
                imageUrls = forceUrls;
            }
            
                
            for (String imageUrl : imageUrls) {
                RekognitionFaceDetector faceDetector = getFaceDetector(imageUrl, apiKey, apiSecret);
                if(faceDetector != null) wts.add(faceDetector);
                
            }
            
        }
        
        return wts.toArray(new WandoraTool[]{});

        
    }
    
    private RekognitionFaceDetector getFaceDetector(String imageUrl, String apiKey, String apiSecret) throws NumberFormatException{
        
        String jobs = "face" + getFaceJobs();

        RekognitionFaceDetector faceDetector = new RekognitionFaceDetector();
        String extractUrl = API_ROOT + 
                "?api_key="    + apiKey +
                "&api_secret=" + apiSecret +
                "&jobs="       + jobs +
                "&urls="       + imageUrl;

        faceDetector.setForceUrls(new String[]{extractUrl});
        try {
            String treshold = faceCelebrityTreshold.getText();
            faceDetector.setCelebrityNaming(faceAssociateCelebrity.isSelected(), Double.parseDouble(treshold));

        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid celebrity association treshold");
        }
        
        return faceDetector;
    }
    
    private String getFaceJobs() {
        StringBuilder sb = new StringBuilder();
        
        for (String jobName : faceJobs.keySet()) {
            if (faceJobs.get(jobName).isSelected()) {
                sb.append("_");
                sb.append(jobName);
            }
        }
        
        return sb.toString();
        
    }
    
    private static HashMap<String,String> auth = null;

    private void solveAPIKeys() {

        if(auth == null){
            
            RekognitionAuthenticationDialog authDialog = new RekognitionAuthenticationDialog();
            authDialog.open(wandora);
            
            RekognitionExtractorUI.auth = authDialog.getAuth();
            
            
        }
        forgetButton.setEnabled(true);
    }
    
    public void forgetAuthorization() {
        auth = null;
        forgetButton.setEnabled(false);
    }

    private void getContext() {
        Topic openedTopic = this.wandora.getOpenTopic();

        Locator l;
        //Prefer SL over SI
        try {
            l = openedTopic.getSubjectLocator();    
            l = (l != null) ? l : openedTopic.getOneSubjectIdentifier();
        } catch (TopicMapException tme) {
            l = null;
        }
        
        if(l != null){
            setImageURL(l.toString());
        }        
    }
    
    private void setImageURL(String url){
        this.urlField.setText(url);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rekognitionTabs = new javax.swing.JTabbedPane();
        faceDetectorTab = new javax.swing.JPanel();
        urlField = new SimpleField();
        getContextButton = new SimpleButton();
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
        faceCelebrityTresholdLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();
        forgetButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        faceDetectorTab.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        faceDetectorTab.add(urlField, gridBagConstraints);

        getContextButton.setText("Get Context Locator");
        getContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        faceDetectorTab.add(getContextButton, gridBagConstraints);

        faceDetectorJobsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Jobs"));
        faceDetectorJobsPanel.setLayout(new java.awt.GridBagLayout());

        faceJobCheckAggressive.setText("Aggressive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckAggressive, gridBagConstraints);

        faceJobCheckPart.setText("Part Positions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckPart, gridBagConstraints);

        faceJobCheckPartDetail.setText("Part Details");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckPartDetail, gridBagConstraints);

        faceJobCheckGender.setText("Gender");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckGender, gridBagConstraints);

        faceJobCheckEmotion.setText("Emotion");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckEmotion, gridBagConstraints);

        faceJobCheckRace.setText("Race");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckRace, gridBagConstraints);

        faceJobCheckAge.setText("Age");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckAge, gridBagConstraints);

        faceJobCheckGlass.setText("Glasses");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckGlass, gridBagConstraints);

        faceJobCheckMouthOpenWide.setText("Mouth Open");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckMouthOpenWide, gridBagConstraints);

        faceJobCheckEyeClosed.setText("Eyes Closed");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckEyeClosed, gridBagConstraints);

        faceJobCheckBeauty.setText("Beauty");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckBeauty, gridBagConstraints);

        faceJobCheckCelebrity.setText("Celebrity");
        faceJobCheckCelebrity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                faceJobCheckCelebrityActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        faceDetectorJobsPanel.add(faceJobCheckCelebrity, gridBagConstraints);

        faceCelebrityDetails.setLayout(new java.awt.GridBagLayout());

        faceAssociateCelebrity.setText("Attempt Celebrity Naming");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        faceCelebrityDetails.add(faceAssociateCelebrity, gridBagConstraints);

        faceCelebrityTreshold.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("##0.##"))));
        faceCelebrityTreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                faceCelebrityTresholdActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        faceCelebrityDetails.add(faceCelebrityTreshold, gridBagConstraints);

        faceCelebrityTresholdLabel.setText("Treshold");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 3, 0, 3);
        faceCelebrityDetails.add(faceCelebrityTresholdLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        faceDetectorJobsPanel.add(faceCelebrityDetails, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        faceDetectorTab.add(faceDetectorJobsPanel, gridBagConstraints);

        rekognitionTabs.addTab("Face Detector", faceDetectorTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(rekognitionTabs, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonFillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        okButton.setText("Extract");
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

        forgetButton.setText("Forget api-key");
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

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        accepted = true;
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        accepted = false;
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void forgetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forgetButtonActionPerformed
        auth = null;
        forgetButton.setEnabled(false);
        
    }//GEN-LAST:event_forgetButtonActionPerformed

    private void getContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getContextButtonActionPerformed
        getContext();
    }//GEN-LAST:event_getContextButtonActionPerformed

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
    private javax.swing.JButton getContextButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTabbedPane rekognitionTabs;
    private javax.swing.JTextField urlField;
    // End of variables declaration//GEN-END:variables

    

}
