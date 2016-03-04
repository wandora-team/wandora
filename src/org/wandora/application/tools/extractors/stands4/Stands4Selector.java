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
 * 
 * Stands4Selector.java
 *
 * Created on 25.3.2010
 */

package org.wandora.application.tools.extractors.stands4;


import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.text.*;

/**
 *
 * @author  akivela
 */
public class Stands4Selector extends JDialog {
    public static String defaultLanguage = "en";
    
    private static final String acronymsAPIURL = "http://www.abbreviations.com/services/v1/abbr.aspx?tokenid=__2__&term=__1__";
    private static final String synonymsAPIURL = "http://www.abbreviations.com/services/v1/syno.aspx?tokenid=__2__&word=__1__";
    private static final String definitionsAPIURL = "http://www.abbreviations.com/services/v1/defs.aspx?tokenid=__2__&word=__1__";
    
    private Wandora wandora = null;
    private Context context = null;
    private boolean accepted = false;
    
    
    /** Creates new form Stands4Selector */
    public Stands4Selector(Wandora wandora) {
        super(wandora, true);
        initComponents();
               
        setTitle("Stands4 extractor");
        setSize(550,310);
        wandora.centerWindow(this);
        this.wandora = wandora;
        accepted = false;
    }
    
    


    public void setWandora(Wandora wandora) {
        this.wandora = wandora;
    }
    
    public void setContext(Context context) {
        this.context = context;
    }
    

    
    public boolean wasAccepted() {
        return accepted;
    }
    public void setAccepted(boolean b) {
        accepted = b;
    }
    
    
    
    public WandoraTool getWandoraTool(WandoraTool parentTool) {
        Component component = stands4TabbedPane.getSelectedComponent();
        WandoraTool wt = null;

        // ***** DESCRIBE *****
        if(defineTab.equals(component)) {
            String wordsAll = wordTextField.getText();
            String[] words = urlEncode(newlineSplitter(wordsAll));
            String apibase = null;
            AbstractStands4Extractor ex = null;

            if(acronymsCheckBox.isSelected()) {
                apibase = acronymsAPIURL;
                AbbreviationExtractor abex = new AbbreviationExtractor();
                ex = abex;
                parentTool.log("Selecting Abbreviation Extractor");
            }
            else {
                parentTool.log("Selecting Synonym Extractor");
                apibase = synonymsAPIURL;
                SynonymExtractor syex = new SynonymExtractor();
                if(!synonymsCheckBox.isSelected()) syex.excludeSynonyms(true);
                if(!antonymsCheckBox.isSelected()) syex.excludeAntonyms(true);
                if(!partOfSpeechCheckBox.isSelected()) syex.excludePartOfSpeech(true);
                if(!definitionCheckBox.isSelected()) syex.excludeDefinition(true);
                ex = syex;
            }

            if(apibase != null && ex != null) {
                String apikey = ex.solveAPIKey(wandora);
                if(apikey != null && apikey.length() > 0) {
                    String[] wordsUrls = completeString(apibase, words, apikey);
                    ex.setForceUrls( wordsUrls );
                    wt = ex;
                }
                else {
                    parentTool.log("Invalid api key. Aborting...");
                }
            }
        }
        
        
        // ***** EXACT URL *****
        else if(urlTab.equals(component)) {
            String url = urlField.getText();
            AbstractStands4Extractor ex = null;
            if(url.indexOf("abbr.aspx") != -1) {
                ex = new AbbreviationExtractor();
            }
            else if(url.indexOf("syno.aspx") != -1) {
                ex = new SynonymExtractor();
            }
            else if(url.indexOf("defs.aspx") != -1) {
                ex = new DefinitionExtractor();
            }
            else {
                ex = new AbbreviationExtractor();
            }
            ex.setForceUrls( new String[] { url } );
            wt = ex;
        }
        
        return wt;
    }
    



    public String[] newlineSplitter(String str) {
        if(str.indexOf('\n') != -1) {
            String[] strs = str.split("\n");
            ArrayList<String> strList = new ArrayList<String>();
            String s = null;
            for(int i=0; i<strs.length; i++) {
                s = strs[i];
                s = s.trim();
                if(s.length() > 0) {
                    strList.add(s);
                }
            }
            return strList.toArray( new String[] {} );
        }
        else {
            return new String[] { str };
        }
    }


    public String[] commaSplitter(String str) {
        if(str.indexOf(',') != -1) {
            String[] strs = str.split(",");
            ArrayList<String> strList = new ArrayList<String>();
            String s = null;
            for(int i=0; i<strs.length; i++) {
                s = strs[i];
                s = s.trim();
                if(s.length() > 0) {
                    strList.add(s);
                }
            }
            return strList.toArray( new String[] {} );
        }
        else {
            return new String[] { str };
        }
        
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
    
    
    public String[] completeString(String template, String[] strs1, String strs2) {
        if(strs1 == null || strs2 == null || template == null) return null;
        
        String[] completed = new String[strs1.length];
        for(int i=0; i<strs1.length; i++) {
            completed[i] = template.replaceAll("__1__", strs1[i]);
            completed[i] = completed[i].replaceAll("__2__", strs2);
        }
        return completed;
    }
    
    public String[] completeString(String template, String[] strs1, String strs2, String strs3) {
        if(strs1 == null || strs2 == null || template == null) return null;

        String[] completed = new String[strs1.length];
        for(int i=0; i<strs1.length; i++) {
            completed[i] = template.replaceAll("__1__", strs1[i]);
            completed[i] = completed[i].replaceAll("__2__", strs2);
            completed[i] = completed[i].replaceAll("__3__", strs3);
        }
        return completed;
    }
    
    public String[] urlEncode(String[] urls) {
        if(urls == null) return null;
        String[] cleanUrls = new String[urls.length];
        for(int i=0; i<urls.length; i++) {
            cleanUrls[i] = urlEncode(urls[i]);
        }
        return cleanUrls;
    }
    
    
    
    public String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        }
        catch(Exception e) {
            return url;
        }
    }
    
    
    
    
    public String getContextAsString() {
        StringBuilder sb = new StringBuilder("");
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
                        //str = t.getBaseName();
                        str = t.getDisplayName("en");
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
    
    

    

    public String getContextAsSIs(String delim) {
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
                        Collection<Locator> identifiers = t.getSubjectIdentifiers();
                        for( Iterator<Locator> iter = identifiers.iterator() ; iter.hasNext(); ) {
                            Locator identifier = iter.next();
                            if(identifier != null) {
                                str = identifier.toExternalForm().trim();
                                sb.append(str);
                                if(iter.hasNext()) sb.append(delim);
                            }
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

    
    public String getContextAsSI() {
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
                        Collection<Locator> identifiers = t.getSubjectIdentifiers();
                        for( Iterator<Locator> iter = identifiers.iterator() ; iter.hasNext(); ) {
                            Locator identifier = iter.next();
                            if(identifier != null) {
                                str = identifier.toExternalForm().trim();
                                return str;
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return "";
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

        stands4TabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        defineTab = new javax.swing.JPanel();
        defineInnerPanel = new javax.swing.JPanel();
        defineLabel = new SimpleLabel();
        definePanel = new javax.swing.JPanel();
        wordLabel = new SimpleLabel();
        wordTextField = new SimpleField();
        acronymsCheckBox = new SimpleCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        definitionCheckBox = new SimpleCheckBox();
        synonymsCheckBox = new SimpleCheckBox();
        antonymsCheckBox = new SimpleCheckBox();
        partOfSpeechCheckBox = new SimpleCheckBox();
        jPanel1 = new javax.swing.JPanel();
        defineButtonPanel = new javax.swing.JPanel();
        getContextButton2Identifiers = new SimpleButton();
        urlTab = new javax.swing.JPanel();
        urlInnerPanel = new javax.swing.JPanel();
        urlLabel = new org.wandora.application.gui.simple.SimpleLabel();
        urlField = new org.wandora.application.gui.simple.SimpleField();
        urlGetButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        emptyPanel = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        defineTab.setLayout(new java.awt.GridBagLayout());

        defineInnerPanel.setLayout(new java.awt.GridBagLayout());

        defineLabel.setText("<html>Describe given words using Stands4 web services.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        defineInnerPanel.add(defineLabel, gridBagConstraints);

        definePanel.setLayout(new java.awt.GridBagLayout());

        wordLabel.setText("Word");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 5);
        definePanel.add(wordLabel, gridBagConstraints);

        wordTextField.setMinimumSize(new java.awt.Dimension(6, 21));
        wordTextField.setPreferredSize(new java.awt.Dimension(6, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        definePanel.add(wordTextField, gridBagConstraints);

        acronymsCheckBox.setText("Word is an acronym, expand it. Selecting this option excludes other options.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        definePanel.add(acronymsCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        definePanel.add(jSeparator1, gridBagConstraints);

        definitionCheckBox.setSelected(true);
        definitionCheckBox.setText("Get definition for the word.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        definePanel.add(definitionCheckBox, gridBagConstraints);

        synonymsCheckBox.setSelected(true);
        synonymsCheckBox.setText("Get synonyms for the word.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        definePanel.add(synonymsCheckBox, gridBagConstraints);

        antonymsCheckBox.setSelected(true);
        antonymsCheckBox.setText("Get antonyms for the word.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        definePanel.add(antonymsCheckBox, gridBagConstraints);

        partOfSpeechCheckBox.setSelected(true);
        partOfSpeechCheckBox.setText("Get part-of-speech for the word.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        definePanel.add(partOfSpeechCheckBox, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        definePanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        defineInnerPanel.add(definePanel, gridBagConstraints);

        defineButtonPanel.setLayout(new java.awt.GridBagLayout());

        getContextButton2Identifiers.setText("Get context");
        getContextButton2Identifiers.setMargin(new java.awt.Insets(0, 6, 1, 6));
        getContextButton2Identifiers.setMaximumSize(new java.awt.Dimension(90, 20));
        getContextButton2Identifiers.setMinimumSize(new java.awt.Dimension(90, 20));
        getContextButton2Identifiers.setPreferredSize(new java.awt.Dimension(90, 20));
        getContextButton2Identifiers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getContextButton2MouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        defineButtonPanel.add(getContextButton2Identifiers, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        defineInnerPanel.add(defineButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        defineTab.add(defineInnerPanel, gridBagConstraints);

        stands4TabbedPane.addTab("Describe", defineTab);

        urlTab.setLayout(new java.awt.GridBagLayout());

        urlInnerPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText("<html>Read given Stands4 web service feed. Given URL should resolve the feed.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        urlInnerPanel.add(urlLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        urlInnerPanel.add(urlField, gridBagConstraints);

        urlGetButton.setLabel("Get context");
        urlGetButton.setMargin(new java.awt.Insets(0, 2, 1, 2));
        urlGetButton.setMaximumSize(new java.awt.Dimension(90, 20));
        urlGetButton.setMinimumSize(new java.awt.Dimension(90, 20));
        urlGetButton.setPreferredSize(new java.awt.Dimension(90, 20));
        urlGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                urlGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        urlInnerPanel.add(urlGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        urlTab.add(urlInnerPanel, gridBagConstraints);

        stands4TabbedPane.addTab("URL", urlTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(stands4TabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        emptyPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout emptyPanelLayout = new javax.swing.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 405, Short.MAX_VALUE)
        );
        emptyPanelLayout.setVerticalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(emptyPanel, gridBagConstraints);

        okButton.setText("Extract");
        okButton.setPreferredSize(new java.awt.Dimension(75, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
            }
        });
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
    accepted = true;
    setVisible(false);
}//GEN-LAST:event_okButtonMouseReleased

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    accepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonMouseReleased

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    accepted = true;
    setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    accepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void urlGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlGetButtonMouseReleased
    urlField.setText(getContextAsSIs(", "));
}//GEN-LAST:event_urlGetButtonMouseReleased

private void getContextButton2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getContextButton2MouseReleased
    wordTextField.setText(getContextAsString());
}//GEN-LAST:event_getContextButton2MouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox acronymsCheckBox;
    private javax.swing.JCheckBox antonymsCheckBox;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel defineButtonPanel;
    private javax.swing.JPanel defineInnerPanel;
    private javax.swing.JLabel defineLabel;
    private javax.swing.JPanel definePanel;
    private javax.swing.JPanel defineTab;
    private javax.swing.JCheckBox definitionCheckBox;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JButton getContextButton2Identifiers;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox partOfSpeechCheckBox;
    private javax.swing.JTabbedPane stands4TabbedPane;
    private javax.swing.JCheckBox synonymsCheckBox;
    private javax.swing.JTextField urlField;
    private javax.swing.JButton urlGetButton;
    private javax.swing.JPanel urlInnerPanel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JPanel urlTab;
    private javax.swing.JLabel wordLabel;
    private javax.swing.JTextField wordTextField;
    // End of variables declaration//GEN-END:variables

}
