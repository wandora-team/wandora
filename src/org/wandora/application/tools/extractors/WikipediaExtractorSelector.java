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
 * WikipediaExtractorSelector.java
 *
 * Created on 16. heinäkuuta 2008, 21:26
 */

package org.wandora.application.tools.extractors;


import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;




/**
 *
 * @author  akivela
 */
public class WikipediaExtractorSelector extends javax.swing.JDialog {
    private boolean accepted = false;
    private Wandora admin = null;
    private Context context = null;
    
    
    
    /** Creates new form WikipediaExtractorSelector */
    public WikipediaExtractorSelector(Wandora admin) {
        super(admin, true);
        initComponents();
        setSize(450,300);
        setTitle("Wikipedia extractor");
        admin.centerWindow(this);
        this.admin = admin;
        accepted = false;
    }

    
    
    public void setWandora(Wandora wandora) {
        this.admin = wandora;
    }
    
    public void setContext(Context context) {
        this.context = context;
    }
    
    public void selectAll() {
        termsTextPane.selectAll();
    }
    
    
    public boolean wasAccepted() {
        return accepted;
    }
    
    public WandoraTool getWandoraTool(WandoraTool parentTool) {
        Component component = tabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        
        // ***** TERMS *******
        if(termsPanel.equals(component)) {
            String termText = termsTextPane.getText();
            
            String lang = langField.getText();
            if(lang != null) {
                lang = lang.trim();
                if(lang.length() == 0) lang = "en";
            }
            String[] terms = encode(termSplitter(termText));
            String base = "http://"+lang+".wikipedia.org/wiki/Special:Export/__1__";
            String[] termUrls = completeString(base, terms);
            
            MediaWikiExtractor ex = new MediaWikiExtractor();
            ex.setForceUrls(termUrls);
            ex.setWikiBaseURL(base);
            ex.setFollowRedirects(redirectsCheckBox.isSelected());
            wt = ex;
        }
        
        return wt;
    }


    public String[] termSplitter(String str) {
        String[] strs=str.split(",|\n");
        ArrayList<String> ret=new ArrayList<String>();
        for(int i=0;i<strs.length;i++){
            strs[i]=strs[i].trim();
            if(strs[i].length()>0) ret.add(strs[i]);
        }
        return ret.toArray(new String[ret.size()]);
/*
        if(str.indexOf(',') != -1) {
            String[] strs = str.split(",");
            ArrayList<String> strList = new ArrayList<String>();
            String s = null;
            for(int i=0; i<strs.length; i++) {
                s = strs[i];
                s = s.trim();
                if(s.indexOf('\n') != -1) {
                    String[] strs2 = s.split("\n");
                    for(int j=0; j<strs2.length; j++) {
                        s = strs[j];
                        s = s.trim();
                        if(s.length() > 0) {
                            strList.add(s);
                        }
                    }
                }
                else {
                    if(s.length() > 0) {
                        strList.add(s);
                    }
                }
            }
            return strList.toArray( new String[] {} );
        }
        else {
            return new String[] { str };
        }
        */
    }
    
    
    
    public String[] completeString(String template, String[] strs) {
        if(strs == null || template == null) return null;
        String[] completed = new String[strs.length];
        for(int i=0; i<strs.length; i++) {
            completed[i] = template.replaceAll("__1__", strs[i]);
        }
        return completed;
    }
    
    
    public String[] completeString(String template, String[] strs1, String[] strs2) {
        if(strs1 == null || strs2 == null || template == null) return null;
        if(strs1.length != strs2.length) return null;
        
        String[] completed = new String[strs1.length];
        for(int i=0; i<strs1.length; i++) {
            completed[i] = template.replaceAll("__1__", strs1[i]);
            completed[i] = completed[i].replaceAll("__2__", strs2[i]);
        }
        return completed;
    }
    
    
    public String[] encode(String[] urls) {
        if(urls == null) return null;
        String[] cleanUrls = new String[urls.length];
        for(int i=0; i<urls.length; i++) {
            cleanUrls[i] = urlEncode(urls[i]);
        }
        return cleanUrls;
    }
    
    
    
    public String urlEncode(String url) {
        try {
            if(url != null) url = url.replaceAll(" ", "_"); // Wikipedia terms contains underscores instead of spaces!
            return URLEncoder.encode(url, "UTF-8");
        }
        catch(Exception e) {
            return url;
        }
    }
    
    public String getContextAsString() {
        StringBuffer sb = new StringBuffer("");
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                String str = null;
                Object o = null;
                while(contextObjects.hasNext()) {
                    str = null;
                    o = contextObjects.next();
                    if(o instanceof Topic) {
                        Topic t = (Topic) o;
                        String lang = langField.getText();
                        if(lang == null || lang.trim().length() == 0) lang = "en";
                        str = t.getDisplayName(lang);
                        if(str != null) {
                            str = str.trim();
                        }
                    }
                    
                    if(str != null && str.length() > 0) {
                        sb.append(str);
                        if(contextObjects.hasNext()) {
                            sb.append(", ");
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
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

        contextPanel = new javax.swing.JPanel();
        contextPanelInner = new javax.swing.JPanel();
        contextLabel = new org.wandora.application.gui.simple.SimpleLabel();
        contextCheckBox1 = new org.wandora.application.gui.simple.SimpleCheckBox();
        contextCheckBox2 = new org.wandora.application.gui.simple.SimpleCheckBox();
        tabContainerPanel = new javax.swing.JPanel();
        tabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        termsPanel = new javax.swing.JPanel();
        termsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        termsTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        getButtonPanel = new javax.swing.JPanel();
        getTermNameButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        languagePanel = new javax.swing.JPanel();
        langLabel = new org.wandora.application.gui.simple.SimpleLabel();
        langField = new org.wandora.application.gui.simple.SimpleField();
        redirectsCheckBox = new javax.swing.JCheckBox();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        contextPanel.setOpaque(false);
        contextPanel.setVisible(false);
        contextPanel.setLayout(new java.awt.GridBagLayout());

        contextPanelInner.setLayout(new java.awt.GridBagLayout());

        contextLabel.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        contextPanelInner.add(contextLabel, gridBagConstraints);

        contextCheckBox1.setText("jCheckBox1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        contextPanelInner.add(contextCheckBox1, gridBagConstraints);

        contextCheckBox2.setText("jCheckBox2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        contextPanelInner.add(contextCheckBox2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        contextPanel.add(contextPanelInner, gridBagConstraints);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        tabContainerPanel.setLayout(new java.awt.BorderLayout());

        termsPanel.setLayout(new java.awt.GridBagLayout());

        termsLabel.setText("<html>This tab is used to specify Wikipedia terms to be extracted. Write terms into the text field below. Or use pick up buttons to get context terms. Use comma (,) or newline character to separate different terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        termsPanel.add(termsLabel, gridBagConstraints);

        jScrollPane1.setViewportView(termsTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        termsPanel.add(jScrollPane1, gridBagConstraints);

        getButtonPanel.setLayout(new java.awt.GridBagLayout());

        getTermNameButton.setText("Get context names");
        getTermNameButton.setToolTipText("Copies names of selected topics in Wandora to the text area above.");
        getTermNameButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        getTermNameButton.setMaximumSize(new java.awt.Dimension(130, 23));
        getTermNameButton.setMinimumSize(new java.awt.Dimension(130, 23));
        getTermNameButton.setPreferredSize(new java.awt.Dimension(130, 21));
        getTermNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getTermNameButtonActionPerformed(evt);
            }
        });
        getButtonPanel.add(getTermNameButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        termsPanel.add(getButtonPanel, gridBagConstraints);

        tabbedPane.addTab("Terms", termsPanel);

        tabContainerPanel.add(tabbedPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tabContainerPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        languagePanel.setLayout(new java.awt.GridBagLayout());

        langLabel.setText("Language");
        langLabel.setToolTipText("Language assigns the Wikipedia version used to extract.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        languagePanel.add(langLabel, gridBagConstraints);

        langField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        langField.setText("en");
        langField.setPreferredSize(new java.awt.Dimension(40, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        languagePanel.add(langField, gridBagConstraints);

        buttonPanel.add(languagePanel, new java.awt.GridBagConstraints());

        redirectsCheckBox.setSelected(true);
        redirectsCheckBox.setText("Follow redirects");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        buttonPanel.add(redirectsCheckBox, gridBagConstraints);

        okButton.setText("Extract");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    accepted = true;
    setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    accepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void getTermNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getTermNameButtonActionPerformed
    termsTextPane.setText(getContextAsString());
}//GEN-LAST:event_getTermNameButtonActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox contextCheckBox1;
    private javax.swing.JCheckBox contextCheckBox2;
    private javax.swing.JLabel contextLabel;
    private javax.swing.JPanel contextPanel;
    private javax.swing.JPanel contextPanelInner;
    private javax.swing.JPanel getButtonPanel;
    private javax.swing.JButton getTermNameButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField langField;
    private javax.swing.JLabel langLabel;
    private javax.swing.JPanel languagePanel;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox redirectsCheckBox;
    private javax.swing.JPanel tabContainerPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel termsLabel;
    private javax.swing.JPanel termsPanel;
    private javax.swing.JTextPane termsTextPane;
    // End of variables declaration//GEN-END:variables

}
