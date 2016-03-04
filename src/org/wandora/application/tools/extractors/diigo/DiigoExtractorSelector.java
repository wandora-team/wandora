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
 * DiigoExtractorSelector.java
 *
 * Created on 5.2.2009, 13:32
 */

package org.wandora.application.tools.extractors.diigo;


import org.wandora.application.tools.extractors.ovi.*;
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
public class DiigoExtractorSelector extends JDialog {
    public static String defaultLanguage = "en";
    private String defaultEncoding = "UTF-8";
    
    private static final String diigoURL = "http://api2.diigo.com/";
    private static final String diigoTaggedURL=diigoURL+"bookmarks?tags=__1__&rows=__2__&start=__3__";
    private static final String diigoUserURL=diigoURL+"bookmarks?users=__1__&rows=__2__&start=__3__";
    private static final String diigoSearchURL=diigoURL+"bookmarks?ft=__1__&rows=__2__&start=__3__";
    private static final String diigoSiteURL=diigoURL+"bookmarks?site=__1__&rows=__2__&start=__3__";

    private Wandora admin = null;
    private Context context = null;
    private boolean accepted = false;
    
    
    /** Creates new form DiigoExtractorSelector */
    public DiigoExtractorSelector(Wandora admin) {
        super(admin, true);
        initComponents();
        
        setTitle("Diigo extractor");
        setSize(400,300);
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
    

    
    public boolean wasAccepted() {
        return accepted;
    }
    public void setAccepted(boolean b) {
        accepted = b;
    }
    
    
    
    public WandoraTool getWandoraTool(WandoraTool parentTool) {
        Component component = diiguTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        int startRow = 0;
        String rowCountStr = rowNumberTextField.getText();
        int rowCount = 100;
        try {
            rowCount = Integer.parseInt(rowCountStr);
        }
        catch(Exception e) {
            // Illegal number of entry count. Using default!
        }
        
        // ***** SEARCH *****
        if(searchTab.equals(component)) {
            String searchWord = searchField.getText();
            String[] words = urlEncode( new String[] { searchWord } );
            String[] searchUrls = makeUrlStrings(diigoSearchURL, words, startRow, rowCount);
            
            DiigoBookmarkExtractor ex = new DiigoBookmarkExtractor();
            ex.setForceUrls( searchUrls );
            wt = ex;
        }
        // ***** TAGGED *****
        else if(tagTab.equals(component)) {
            String tagAll = tagField.getText();
            String[] tags = urlEncode(commaSplitter(tagAll));
            String[] tagUrls = makeUrlStrings(diigoTaggedURL, tags, startRow, rowCount);
            
            DiigoBookmarkExtractor ex = new DiigoBookmarkExtractor();
            ex.setForceUrls( tagUrls );
            wt = ex;
        }
        // ***** USER *****
        else if(userTab.equals(component)) {
            String userAll = userField.getText();
            String[] users = urlEncode(commaSplitter(userAll));
            String[] userUrls = makeUrlStrings(diigoUserURL, users, startRow, rowCount);
            
            DiigoBookmarkExtractor ex = new DiigoBookmarkExtractor();
            ex.setForceUrls( userUrls );
            wt = ex;
        }
        // ***** SITE *****
        else if(siteTab.equals(component)) {
            String siteAll = siteField.getText();
            String[] sites = urlEncode(commaSplitter(siteAll));
            String[] siteUrls = makeUrlStrings(diigoSiteURL, sites, startRow, rowCount);
            
            DiigoBookmarkExtractor ex = new DiigoBookmarkExtractor();
            ex.setForceUrls( siteUrls );
            wt = ex;
        }
        // ***** URL *****
        else if(urlTab.equals(component)) {
            String url = urlField.getText();
            DiigoBookmarkExtractor ex = new DiigoBookmarkExtractor();
            ex.setForceUrls( new String[] { url } );
            wt = ex;
        }
        
        return wt;
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
    
    
    public String[] makeUrlStrings(String template, String[] str1, int startRow, int rowCount ) {
        ArrayList<String> urls = new ArrayList<String>();
        String[] urlsStr = null;
        int end = 0;
        for(int i=startRow; i<rowCount; i=i+100) {
            end = Math.min(rowCount, i+100)-i;
            urlsStr = completeString(template, str1, ""+end, ""+i);
            for(int k=0; k<urlsStr.length; k++) {
                urls.add(urlsStr[k]);
            }
        }
        return urls.toArray( new String[] {} );
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
                        str = t.getBaseName();
                        if(str != null) {
                            int pindex = str.indexOf("(");
                            if(pindex > 0) {
                                str = str.substring(0, pindex);
                            }
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

        siteTab = new javax.swing.JPanel();
        siteInnerPanel = new javax.swing.JPanel();
        siteLabel = new org.wandora.application.gui.simple.SimpleLabel();
        siteField = new org.wandora.application.gui.simple.SimpleField();
        siteGetButton = new org.wandora.application.gui.simple.SimpleButton();
        diiguTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        searchTab = new javax.swing.JPanel();
        searchInnerPanel = new javax.swing.JPanel();
        searchLabel = new org.wandora.application.gui.simple.SimpleLabel();
        searchField = new org.wandora.application.gui.simple.SimpleField();
        searchGetButton = new org.wandora.application.gui.simple.SimpleButton();
        tagTab = new javax.swing.JPanel();
        tagInnerPanel = new javax.swing.JPanel();
        tagLabel = new org.wandora.application.gui.simple.SimpleLabel();
        tagField = new org.wandora.application.gui.simple.SimpleField();
        tagGetButton = new org.wandora.application.gui.simple.SimpleButton();
        userTab = new javax.swing.JPanel();
        userInnerPanel = new javax.swing.JPanel();
        userLabel = new org.wandora.application.gui.simple.SimpleLabel();
        userField = new org.wandora.application.gui.simple.SimpleField();
        userGetButton = new org.wandora.application.gui.simple.SimpleButton();
        urlTab = new javax.swing.JPanel();
        urlInnerPanel = new javax.swing.JPanel();
        urlLabel = new org.wandora.application.gui.simple.SimpleLabel();
        urlField = new org.wandora.application.gui.simple.SimpleField();
        urlGetButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        getLabel = new org.wandora.application.gui.simple.SimpleLabel();
        rowNumberTextField = new org.wandora.application.gui.simple.SimpleField();
        entriesLabel = new org.wandora.application.gui.simple.SimpleLabel();
        emptyPanel = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        siteTab.setLayout(new java.awt.GridBagLayout());

        siteInnerPanel.setLayout(new java.awt.GridBagLayout());

        siteLabel.setText("<html>Read bookmarks related to given site. Use comma (,) character to separate different channel names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        siteInnerPanel.add(siteLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        siteInnerPanel.add(siteField, gridBagConstraints);

        siteGetButton.setLabel("Get context");
        siteGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        siteGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        siteGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        siteGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        siteGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                siteGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        siteInnerPanel.add(siteGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        siteTab.add(siteInnerPanel, gridBagConstraints);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        searchTab.setLayout(new java.awt.GridBagLayout());

        searchInnerPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("<html>Search for Diigo bookmarks with given search words.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        searchInnerPanel.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchInnerPanel.add(searchField, gridBagConstraints);

        searchGetButton.setLabel("Get context");
        searchGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        searchGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        searchGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        searchGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        searchGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                searchGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        searchInnerPanel.add(searchGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        searchTab.add(searchInnerPanel, gridBagConstraints);

        diiguTabbedPane.addTab("Search", searchTab);

        tagTab.setLayout(new java.awt.GridBagLayout());

        tagInnerPanel.setLayout(new java.awt.GridBagLayout());

        tagLabel.setText("<html>Fetch bookmarks with given tag. Use comma (,) character to separate different tag terms.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        tagInnerPanel.add(tagLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        tagInnerPanel.add(tagField, gridBagConstraints);

        tagGetButton.setLabel("Get context");
        tagGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        tagGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        tagGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        tagGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        tagGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tagGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        tagInnerPanel.add(tagGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        tagTab.add(tagInnerPanel, gridBagConstraints);

        diiguTabbedPane.addTab("Tagged", tagTab);

        userTab.setLayout(new java.awt.GridBagLayout());

        userInnerPanel.setLayout(new java.awt.GridBagLayout());

        userLabel.setText("<html>Fetch bookmarks of Diigo user. Please write user names below or get the context. Use comma (,) character to separate different names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        userInnerPanel.add(userLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        userInnerPanel.add(userField, gridBagConstraints);

        userGetButton.setLabel("Get context");
        userGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        userGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        userGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        userGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        userGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                userGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        userInnerPanel.add(userGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        userTab.add(userInnerPanel, gridBagConstraints);

        diiguTabbedPane.addTab("User", userTab);

        urlTab.setLayout(new java.awt.GridBagLayout());

        urlInnerPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText("<html>Fetch and parse Diigo API URL address. URL should resolve exactly Diigo API feed.</html>");
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
        urlGetButton.setMargin(new java.awt.Insets(0, 6, 1, 6));
        urlGetButton.setMaximumSize(new java.awt.Dimension(75, 20));
        urlGetButton.setMinimumSize(new java.awt.Dimension(75, 20));
        urlGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
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

        diiguTabbedPane.addTab("URL", urlTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(diiguTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        getLabel.setText("Get");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(getLabel, gridBagConstraints);

        rowNumberTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rowNumberTextField.setText("50");
        rowNumberTextField.setMinimumSize(new java.awt.Dimension(30, 23));
        rowNumberTextField.setPreferredSize(new java.awt.Dimension(30, 23));
        buttonPanel.add(rowNumberTextField, new java.awt.GridBagConstraints());

        entriesLabel.setText("bookmarks");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        buttonPanel.add(entriesLabel, gridBagConstraints);

        emptyPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout emptyPanelLayout = new javax.swing.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
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

private void tagGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tagGetButtonMouseReleased
    tagField.setText(getContextAsString());
}//GEN-LAST:event_tagGetButtonMouseReleased

private void userGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userGetButtonMouseReleased
    userField.setText(getContextAsString());
}//GEN-LAST:event_userGetButtonMouseReleased

private void searchGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGetButtonMouseReleased
    searchField.setText(getContextAsString());
}//GEN-LAST:event_searchGetButtonMouseReleased

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

private void siteGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_siteGetButtonMouseReleased
    siteField.setText(getContextAsString());
}//GEN-LAST:event_siteGetButtonMouseReleased

private void urlGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlGetButtonMouseReleased
    urlField.setText(getContextAsString());
}//GEN-LAST:event_urlGetButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTabbedPane diiguTabbedPane;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JLabel entriesLabel;
    private javax.swing.JLabel getLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField rowNumberTextField;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton searchGetButton;
    private javax.swing.JPanel searchInnerPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchTab;
    private javax.swing.JTextField siteField;
    private javax.swing.JButton siteGetButton;
    private javax.swing.JPanel siteInnerPanel;
    private javax.swing.JLabel siteLabel;
    private javax.swing.JPanel siteTab;
    private javax.swing.JTextField tagField;
    private javax.swing.JButton tagGetButton;
    private javax.swing.JPanel tagInnerPanel;
    private javax.swing.JLabel tagLabel;
    private javax.swing.JPanel tagTab;
    private javax.swing.JTextField urlField;
    private javax.swing.JButton urlGetButton;
    private javax.swing.JPanel urlInnerPanel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JPanel urlTab;
    private javax.swing.JTextField userField;
    private javax.swing.JButton userGetButton;
    private javax.swing.JPanel userInnerPanel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JPanel userTab;
    // End of variables declaration//GEN-END:variables

}
