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
 * BigHugeThesaurusSelector.java
 *
 * Created on 26.12.2009, 12:45:42
 */

package org.wandora.application.tools.extractors.bighugethesaurus;


import org.wandora.application.contexts.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.text.*;


/**
 *
 * @author akivela
 */
public class BigHugeThesaurusSelector extends javax.swing.JDialog {
    public static String defaultLanguage = "en";

    public static String apiURL = "http://words.bighugelabs.com/api/2/__2__/__1__/xml";


    private Wandora wandora = null;
    private Context context = null;
    private boolean accepted = false;



    /** Creates new form BigHugeThesaurusSelector */
    public BigHugeThesaurusSelector(Wandora wandora) {
        super(wandora, true);
        initComponents();

        setTitle("Big Huge Thesaurus extractor");
        setSize(500,200);
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
        Component component = tabbedPane.getSelectedComponent();
        WandoraTool wt = null;


        // ***** WORDS *****
        if(wordPanel.equals(component)) {
            String wordsAll = wordTextField.getText();
            String[] words = urlEncode(commaSplitter(wordsAll));
            String apikey = solveAPIKey();
            if(apikey != null && apikey.length() > 0) {
                apikey = apikey.trim();
                String[] wordUrls = completeString(apiURL, words, apikey);
                XMLBigHugeThesaurusExtractor ex = new XMLBigHugeThesaurusExtractor();
                ex.setForceUrls( wordUrls );
                wt = ex;
            }
            else {
                parentTool.log("Invalid apikey given. Aborting...");
            }
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



    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------




    private static String apikey = null;
    private String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(wandora, "Please give valid apikey for Big Huge Thesaurus. You can register apikey at http://words.bighugelabs.com/api.php", apikey, "Big Huge Thesaurus apikey", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return apikey;
    }




    public void forgetAuthorization() {
        apikey = null;
    }


    // -------------------------------------------------------------------------



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabContainerPanel = new javax.swing.JPanel();
        tabbedPane = new SimpleTabbedPane();
        wordPanel = new javax.swing.JPanel();
        wordLabel = new SimpleLabel();
        wordTextField = new SimpleField();
        getContextButton = new SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        extractButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setTitle("Big Huge Thesaurus extractor");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        tabContainerPanel.setLayout(new java.awt.GridBagLayout());

        wordPanel.setLayout(new java.awt.GridBagLayout());

        wordLabel.setText("<html>Big Huge Thesaurus extractor associates related words for given word(s). Please, write words to the field below.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        wordPanel.add(wordLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        wordPanel.add(wordTextField, gridBagConstraints);

        getContextButton.setText("Get context");
        getContextButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        getContextButton.setMaximumSize(new java.awt.Dimension(100, 21));
        getContextButton.setMinimumSize(new java.awt.Dimension(100, 21));
        getContextButton.setPreferredSize(new java.awt.Dimension(100, 21));
        getContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getContextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        wordPanel.add(getContextButton, gridBagConstraints);

        tabbedPane.addTab("Thesaurus", wordPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tabContainerPanel.add(tabbedPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        getContentPane().add(tabContainerPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonFillerPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout buttonFillerPanelLayout = new javax.swing.GroupLayout(buttonFillerPanel);
        buttonFillerPanel.setLayout(buttonFillerPanelLayout);
        buttonFillerPanelLayout.setHorizontalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 255, Short.MAX_VALUE)
        );
        buttonFillerPanelLayout.setVerticalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        extractButton.setText("Extract");
        extractButton.setMaximumSize(new java.awt.Dimension(75, 21));
        extractButton.setMinimumSize(new java.awt.Dimension(75, 21));
        extractButton.setPreferredSize(new java.awt.Dimension(75, 21));
        extractButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(extractButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new java.awt.Dimension(75, 21));
        cancelButton.setMinimumSize(new java.awt.Dimension(75, 21));
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 21));
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
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.accepted = false;
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void extractButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractButtonActionPerformed
        this.accepted = true;
        this.setVisible(false);
    }//GEN-LAST:event_extractButtonActionPerformed

    private void getContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getContextButtonActionPerformed
        String context = getContextAsString();
        wordTextField.setText(context);
    }//GEN-LAST:event_getContextButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton extractButton;
    private javax.swing.JButton getContextButton;
    private javax.swing.JPanel tabContainerPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel wordLabel;
    private javax.swing.JPanel wordPanel;
    private javax.swing.JTextField wordTextField;
    // End of variables declaration//GEN-END:variables

}
