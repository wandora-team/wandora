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
 */

package org.wandora.application.tools.extractors.mashape;

/**
 *
 * @author Eero
 */

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleList;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

public class MashapeExtractorUI extends javax.swing.JPanel {

    private Wandora wandora = null;
    private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;
    
    private static final String ddgEndpoint
        = "https://duckduckgo-duckduckgo-zero-click-info.p.mashape.com/";
    
     private static final String lambdaEndpoint
        = "https://lambda-face-detection-and-recognition.p.mashape.com/detect";
    
    /**
     * Creates new form MashapiExtractorUI
     */
    public MashapeExtractorUI() {
        initComponents();
    }

    public boolean wasAccepted() {
        return accepted;
    }

    public void setAccepted(boolean b) {
        accepted = b;
    }

    public void open(Wandora w, Context c) {
        context = c;
        wandora = w;
        accepted = false;
        dialog = new JDialog(w, true);
        dialog.setSize(550, 500);
        dialog.add(this);
        dialog.setTitle("Mashape API extractor");
        UIBox.centerWindow(dialog, w);
        if(apikey != null){
            forgetButton.setEnabled(true);
        } else {
            forgetButton.setEnabled(false);
        }
        dialog.setVisible(true);
    }

    public WandoraTool[] getExtractors(MashapeExtractor tool) throws TopicMapException {
        
        WandoraTool wt;
        ArrayList<WandoraTool> wts = new ArrayList();
        String key = solveAPIKey();
        
        if(key == null){
            return null;
        }
        
        if(MashapiTabs.getSelectedComponent().equals(lambdaPanel)){
           
            String query = lambdaSearchField.getText().replaceAll(key, ddgEndpoint);
            query = query.replace("\n", "").replace("\r", "");
            String extractUrl = lambdaEndpoint + "?images=" + query;

            MashapeLambdaExtractor ex = new MashapeLambdaExtractor();
            ex.setForceUrls(new String[]{extractUrl});
            ex.setApiKey(key); 
            wt = ex;
            wts.add(wt);
        } else if(MashapiTabs.getSelectedComponent().equals(duckDuckGoPanel)){
            
            String query = ddgInput.getText();
            String extractUrl = ddgEndpoint + "?q=" + query + "&format=json";
            MashapeDuckDuckGoExtractor ex = new MashapeDuckDuckGoExtractor();
            ex.setForceUrls(new String[]{extractUrl});
            ex.setApiKey(key); 
            wt = ex;
            wts.add(wt);
        }
       
        return wts.toArray(new WandoraTool[]{});
    }

    protected static String urlEncode(String str) {
        try {
            str = URLEncoder.encode(str, "utf-8");
        } catch (Exception e) {
        }
        return str;
    }
    
    private static String apikey = null;

    public String solveAPIKey(Wandora wandora) {
        return solveAPIKey();
    }
    
    public String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Please enter your Mashape api-key. You can register your api-key at https://www.mashape.com/", apikey, "Mashape api-key", WandoraOptionPane.QUESTION_MESSAGE);
            if(apikey != null) {
                apikey = apikey.trim();
            }
        }
        forgetButton.setEnabled(true);
        return apikey;
    }
    
    public void forgetAuthorization() {
        apikey = null;
        forgetButton.setEnabled(false);
    }
    
    private void selectContextSLs() {
        Iterator iter = context.getContextObjects();
        Object o;
        Topic t;
        Locator locator;
        StringBuilder sb = new StringBuilder("");
        while(iter.hasNext()) {
            try {
                o = iter.next();
                if(o == null) continue;
                if(o instanceof Topic) {
                    t = (Topic) o;
                    if(!t.isRemoved()) {
                        locator = t.getSubjectLocator();
                        if(locator != null) {
                            String locatorStr = locator.toExternalForm();
                            sb.append(locatorStr).append(",\n");
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        String s = lambdaSearchField.getText();
        if(s == null || s.length() == 0) {
            s = sb.toString();
        }
        else {
            s = s + ",\n" + sb.toString();
        }
        lambdaSearchField.setText(s);
    }
    
    private void selectContextSIs() {
        Iterator iter = context.getContextObjects();
        Object o;
        Topic t;
        StringBuilder sb = new StringBuilder("");
        while(iter.hasNext()) {
            try {
                o = iter.next();
                if(o == null) continue;
                if(o instanceof Topic) {
                    t = (Topic) o;
                    if(!t.isRemoved()) {
                        HashSet<Locator> sis = (HashSet) t.getSubjectIdentifiers();
                        for(Locator si : sis){
                            String siString = si.toExternalForm();
                            sb.append(siString).append(",\n");
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(sb.toString());
        String s = lambdaSearchField.getText();
        if(s == null || s.length() == 0) {
            s = sb.toString();
        }
        else {
            s = s + ",\n" + sb.toString();
        }
        lambdaSearchField.setText(s);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        MashapiTabs = new SimpleTabbedPane();
        lambdaPanel = new javax.swing.JPanel();
        lambdaDesc = new SimpleLabel();
        lamdaSearchPanel = new javax.swing.JScrollPane();
        lambdaSearchField = new javax.swing.JTextArea();
        lambdaGetContextSLs = new SimpleButton();
        lambdaGetContextSIs = new SimpleButton();
        duckDuckGoPanel = new javax.swing.JPanel();
        ddgLabel = new SimpleLabel();
        ddgInput = new SimpleField();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        buttonPanel = new javax.swing.JPanel();
        forgetButton = new SimpleButton();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        lambdaPanel.setLayout(new java.awt.GridBagLayout());

        lambdaDesc.setText("Add a comma separated list of URLs to process");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        lambdaPanel.add(lambdaDesc, gridBagConstraints);

        lambdaSearchField.setColumns(20);
        lambdaSearchField.setRows(5);
        lamdaSearchPanel.setViewportView(lambdaSearchField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        lambdaPanel.add(lamdaSearchPanel, gridBagConstraints);

        lambdaGetContextSLs.setText("Add context SLs");
        lambdaGetContextSLs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lambdaGetContextSLsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(1, 4, 0, 4);
        lambdaPanel.add(lambdaGetContextSLs, gridBagConstraints);

        lambdaGetContextSIs.setText("Add context SIs");
        lambdaGetContextSIs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lambdaGetContextSIsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        lambdaPanel.add(lambdaGetContextSIs, gridBagConstraints);

        MashapiTabs.addTab("Lambda", lambdaPanel);

        duckDuckGoPanel.setLayout(new java.awt.GridBagLayout());

        ddgLabel.setText("Search query:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        duckDuckGoPanel.add(ddgLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        duckDuckGoPanel.add(ddgInput, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        duckDuckGoPanel.add(filler1, gridBagConstraints);

        MashapiTabs.addTab("DuckDuckGo", duckDuckGoPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(MashapiTabs, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        forgetButton.setText("Forget api-key");
        forgetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forgetButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(forgetButton, new java.awt.GridBagConstraints());

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void forgetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forgetButtonActionPerformed
        apikey = null;
        forgetButton.setEnabled(false);
    }//GEN-LAST:event_forgetButtonActionPerformed

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

    private void lambdaGetContextSLsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lambdaGetContextSLsActionPerformed
        this.selectContextSLs();
    }//GEN-LAST:event_lambdaGetContextSLsActionPerformed

    private void lambdaGetContextSIsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lambdaGetContextSIsActionPerformed
        this.selectContextSIs();
    }//GEN-LAST:event_lambdaGetContextSIsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane MashapiTabs;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField ddgInput;
    private javax.swing.JLabel ddgLabel;
    private javax.swing.JPanel duckDuckGoPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JButton forgetButton;
    private javax.swing.JLabel lambdaDesc;
    private javax.swing.JButton lambdaGetContextSIs;
    private javax.swing.JButton lambdaGetContextSLs;
    private javax.swing.JPanel lambdaPanel;
    private javax.swing.JTextArea lambdaSearchField;
    private javax.swing.JScrollPane lamdaSearchPanel;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

}
