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
 * 
 * Subj3ctSelector.java
 *
 * Created on 20. marraskuuta 2008, 13:32
 */

package org.wandora.application.tools.extractors.subj3ct;


import org.wandora.application.tools.extractors.ovi.*;
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
 * Unfortunately it looks like the Subj3ct web service has been closed down.
 * Thus, this tool is deprecated and you probably can't make it working.
 * However, the tool is still kept in Wandora's source code for the time being.
 *
 * @author  akivela
 */
public class Subj3ctSelector extends JDialog {
    public static String defaultLanguage = "en";
    
    private static final String apiURL = "http://api.subj3ct.com/subjects";

    private static final String recordsByIdentifier = apiURL+"?identifier=__1__&format=xml";
    private static final String recordsByIdentifierWithProvenance = apiURL+"?identifier=__1__&provenance=__2__&format=xml";

    private static final String recordsByResource = apiURL+"/webaddresses?uri=__1__&skip=__2__&take=__3__&format=xml";
    private static final String recordsByURI = apiURL+"/identifiers?uri=__1__&skip=__2__&take=__3__&format=xml";

    //private static final String searchRecords = apiURL+"/search?query=__1__&skip=__2__&take=__3__&format=xml";
    private static final String searchRecords = apiURL+"/search?query=__1__&format=xml";
    
    private Wandora wandora = null;
    private Context context = null;
    private boolean accepted = false;
    
    
    /** Creates new form Subj3ctSelector */
    public Subj3ctSelector(Wandora wandora) {
        super(wandora, true);
        initComponents();
               
        setTitle("Subj3ct identity record extractor");
        setSize(500,400);
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
        Component component = subj3ctTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        
        
        // ***** BY IDENTIFIER *****
        if(byIdentifierTab.equals(component)) {
            String identifiersAll = identifiersTextPane.getText();
            String[] identifiers = urlEncode(newlineSplitter(identifiersAll));
            String provenance = provenanceTextField.getText();
            String[] identifierUrls = null;
            if(provenance != null && provenance.length() > 0) {
                identifierUrls = completeString(recordsByIdentifierWithProvenance, identifiers, provenance);
            }
            else {
                identifierUrls = completeString(recordsByIdentifier, identifiers);
            }

            XMLSubj3ctRecordExtractor ex = new XMLSubj3ctRecordExtractor();
            ex.setForceUrls( identifierUrls );
            wt = ex;
        }
        
        // ***** BY RESOURCE *****
        else if(byResourceTab.equals(component)) {
            String resourcesAll = resourcesTextPane.getText();
            String[] resources = urlEncode(newlineSplitter(resourcesAll));
            String skipStr = resourcesSkipTextField.getText();
            int skip = 0;
            try {
                skip = Integer.parseInt(skipStr);
            }
            catch(Exception e) {
                skip = 0;
            }
            String takeStr = resourcesTakeTextField.getText();
            int take = 50;
            try {
                take = Integer.parseInt(takeStr);
            }
            catch(Exception e) {
                take = 50;
            }
            String[] resourceUrls = completeString(recordsByResource, resources, ""+skip, ""+take);
            
            XMLSubj3ctRecordExtractor ex = new XMLSubj3ctRecordExtractor();
            ex.setForceUrls( resourceUrls );
            wt = ex;
        }
        
        // ***** BY URI *****
        else if(byURITab.equals(component)) {
            String urisAll = uriTextPane.getText();
            String[] uris = urlEncode(newlineSplitter(urisAll));
            String skipStr = urisSkipTextField.getText();
            int skip = 0;
            try {
                skip = Integer.parseInt(skipStr);
            }
            catch(Exception e) {
                skip = 0;
            }
            String takeStr = urisTakeTextField.getText();
            int take = 50;
            try {
                take = Integer.parseInt(takeStr);
            }
            catch(Exception e) {
                take = 50;
            }
            String[] resourceUrls = completeString(recordsByURI, uris, ""+skip, ""+take);

            XMLSubj3ctRecordExtractor ex = new XMLSubj3ctRecordExtractor();
            ex.setForceUrls( resourceUrls );
            wt = ex;
        }
        
        // ***** SEARCH *****
        else if(searchTab.equals(component)) {
            String query = urlEncode(searchField.getText());
            String queryUrl = searchRecords.replace("__1__", query);
            
            XMLSubj3ctRecordExtractor ex = new XMLSubj3ctRecordExtractor();
            ex.setForceUrls( new String[] { queryUrl } );
            wt = ex;
        }
        
        // ***** EXACT URL *****

        else if(urlTab.equals(component)) {
            String url = urlField.getText();
            XMLSubj3ctRecordExtractor ex = new XMLSubj3ctRecordExtractor();
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
                                sb.append(str + delim);
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

        subj3ctTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        byIdentifierTab = new javax.swing.JPanel();
        byIdentifierInnerPanel = new javax.swing.JPanel();
        byIdentifierLabel = new SimpleLabel();
        byIdentifierPanel = new javax.swing.JPanel();
        provenanceLabel = new SimpleLabel();
        provenanceTextField = new SimpleField();
        identifiersLabel = new SimpleLabel();
        identifiersScrollPane = new javax.swing.JScrollPane();
        identifiersTextPane = new SimpleTextPane();
        byIdentifierButtonPanel = new javax.swing.JPanel();
        getContextButton2Identifiers = new SimpleButton();
        getContextButton2Provenance = new SimpleButton();
        byResourceTab = new javax.swing.JPanel();
        byResourceInnerPanel = new javax.swing.JPanel();
        byResourceLabel = new SimpleLabel();
        byResourcePanel = new javax.swing.JPanel();
        resourcesLabel = new SimpleLabel();
        resourcesScrollPane = new javax.swing.JScrollPane();
        resourcesTextPane = new SimpleTextPane();
        byResourceButtonPanel = new javax.swing.JPanel();
        getContextButton2Resources = new SimpleButton();
        takeLabel = new SimpleLabel();
        resourcesSkipTextField = new SimpleField();
        skipLabel = new SimpleLabel();
        resourcesTakeTextField = new SimpleField();
        byURITab = new javax.swing.JPanel();
        byURIInnerPanel = new javax.swing.JPanel();
        byURILabel = new SimpleLabel();
        byURIPanel = new javax.swing.JPanel();
        uriLabel = new SimpleLabel();
        uriScrollPane = new javax.swing.JScrollPane();
        uriTextPane = new SimpleTextPane();
        byURIButtonPanel = new javax.swing.JPanel();
        getContextButton2URIs = new SimpleButton();
        takeLabel1 = new SimpleLabel();
        urisSkipTextField = new SimpleField();
        skipLabel1 = new SimpleLabel();
        urisTakeTextField = new SimpleField();
        searchTab = new javax.swing.JPanel();
        searchInnerPanel = new javax.swing.JPanel();
        searchLabel = new org.wandora.application.gui.simple.SimpleLabel();
        searchField = new org.wandora.application.gui.simple.SimpleField();
        searchGetButton = new org.wandora.application.gui.simple.SimpleButton();
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

        byIdentifierTab.setLayout(new java.awt.GridBagLayout());

        byIdentifierInnerPanel.setLayout(new java.awt.GridBagLayout());

        byIdentifierLabel.setText("<html>Get subject identity records with subject identifiers. Write subject identifiers below. Use newline character to separate different identifiers. You may also set the required provenance for identity records.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        byIdentifierInnerPanel.add(byIdentifierLabel, gridBagConstraints);

        byIdentifierPanel.setLayout(new java.awt.GridBagLayout());

        provenanceLabel.setText("Provenance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 5);
        byIdentifierPanel.add(provenanceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        byIdentifierPanel.add(provenanceTextField, gridBagConstraints);

        identifiersLabel.setText("Identifiers");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byIdentifierPanel.add(identifiersLabel, gridBagConstraints);

        identifiersScrollPane.setViewportView(identifiersTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        byIdentifierPanel.add(identifiersScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        byIdentifierInnerPanel.add(byIdentifierPanel, gridBagConstraints);

        byIdentifierButtonPanel.setLayout(new java.awt.GridBagLayout());

        getContextButton2Identifiers.setText("Context to identifiers");
        getContextButton2Identifiers.setMargin(new java.awt.Insets(2, 6, 2, 6));
        getContextButton2Identifiers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getContextButton2IdentifiersMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        byIdentifierButtonPanel.add(getContextButton2Identifiers, gridBagConstraints);

        getContextButton2Provenance.setText("Context to provenance");
        getContextButton2Provenance.setMargin(new java.awt.Insets(2, 6, 2, 6));
        getContextButton2Provenance.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getContextButton2ProvenanceMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        byIdentifierButtonPanel.add(getContextButton2Provenance, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        byIdentifierInnerPanel.add(byIdentifierButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        byIdentifierTab.add(byIdentifierInnerPanel, gridBagConstraints);

        subj3ctTabbedPane.addTab("By identifiers", byIdentifierTab);

        byResourceTab.setLayout(new java.awt.GridBagLayout());

        byResourceInnerPanel.setLayout(new java.awt.GridBagLayout());

        byResourceLabel.setText("<html>Get subject identity records by web resources. Subjects are included in the result if they have a corresponding web resource whose URI matches the URI specified. Use newline character to separate multiple URLs.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        byResourceInnerPanel.add(byResourceLabel, gridBagConstraints);

        byResourcePanel.setLayout(new java.awt.GridBagLayout());

        resourcesLabel.setText("Resources");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byResourcePanel.add(resourcesLabel, gridBagConstraints);

        resourcesScrollPane.setViewportView(resourcesTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        byResourcePanel.add(resourcesScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        byResourceInnerPanel.add(byResourcePanel, gridBagConstraints);

        byResourceButtonPanel.setLayout(new java.awt.GridBagLayout());

        getContextButton2Resources.setText("Context to resources");
        getContextButton2Resources.setMargin(new java.awt.Insets(2, 6, 2, 6));
        getContextButton2Resources.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getContextButton2ResourcesMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
        byResourceButtonPanel.add(getContextButton2Resources, gridBagConstraints);

        takeLabel.setText("skip");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byResourceButtonPanel.add(takeLabel, gridBagConstraints);

        resourcesSkipTextField.setMinimumSize(new java.awt.Dimension(40, 23));
        resourcesSkipTextField.setPreferredSize(new java.awt.Dimension(50, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byResourceButtonPanel.add(resourcesSkipTextField, gridBagConstraints);

        skipLabel.setText("take");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byResourceButtonPanel.add(skipLabel, gridBagConstraints);

        resourcesTakeTextField.setMinimumSize(new java.awt.Dimension(40, 23));
        resourcesTakeTextField.setPreferredSize(new java.awt.Dimension(50, 23));
        byResourceButtonPanel.add(resourcesTakeTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        byResourceInnerPanel.add(byResourceButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        byResourceTab.add(byResourceInnerPanel, gridBagConstraints);

        subj3ctTabbedPane.addTab("By resources", byResourceTab);

        byURITab.setLayout(new java.awt.GridBagLayout());

        byURIInnerPanel.setLayout(new java.awt.GridBagLayout());

        byURILabel.setText("<html>Get subject identity records by URIs. Subjects are included in the result if the start of their subject identifier matches URIs provided. Use newline character to separate multiple URIs.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        byURIInnerPanel.add(byURILabel, gridBagConstraints);

        byURIPanel.setLayout(new java.awt.GridBagLayout());

        uriLabel.setText("URIs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byURIPanel.add(uriLabel, gridBagConstraints);

        uriScrollPane.setViewportView(uriTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        byURIPanel.add(uriScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        byURIInnerPanel.add(byURIPanel, gridBagConstraints);

        byURIButtonPanel.setLayout(new java.awt.GridBagLayout());

        getContextButton2URIs.setText("Context to resources");
        getContextButton2URIs.setMargin(new java.awt.Insets(2, 6, 2, 6));
        getContextButton2URIs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getContextButton2URIsMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
        byURIButtonPanel.add(getContextButton2URIs, gridBagConstraints);

        takeLabel1.setText("skip");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byURIButtonPanel.add(takeLabel1, gridBagConstraints);

        urisSkipTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        urisSkipTextField.setPreferredSize(new java.awt.Dimension(50, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byURIButtonPanel.add(urisSkipTextField, gridBagConstraints);

        skipLabel1.setText("take");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        byURIButtonPanel.add(skipLabel1, gridBagConstraints);

        urisTakeTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        urisTakeTextField.setPreferredSize(new java.awt.Dimension(50, 23));
        byURIButtonPanel.add(urisTakeTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        byURIInnerPanel.add(byURIButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        byURITab.add(byURIInnerPanel, gridBagConstraints);

        subj3ctTabbedPane.addTab("By URIs", byURITab);

        searchTab.setLayout(new java.awt.GridBagLayout());

        searchInnerPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("<html>Search for subject identity records by text query. Please, write query below.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        searchInnerPanel.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchInnerPanel.add(searchField, gridBagConstraints);

        searchGetButton.setLabel("Get context");
        searchGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        searchGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                searchGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        searchInnerPanel.add(searchGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        searchTab.add(searchInnerPanel, gridBagConstraints);

        subj3ctTabbedPane.addTab("Search", searchTab);

        urlTab.setLayout(new java.awt.GridBagLayout());

        urlInnerPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText("<html>Fetch identity records in URL resource. URL should resolve Subj3ct XML feed.</html>");
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
        urlGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
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

        subj3ctTabbedPane.addTab("URL", urlTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(subj3ctTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        emptyPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout emptyPanelLayout = new javax.swing.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 297, Short.MAX_VALUE)
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

private void getContextButton2IdentifiersMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getContextButton2IdentifiersMouseReleased
    identifiersTextPane.setText(getContextAsSIs("\n"));
}//GEN-LAST:event_getContextButton2IdentifiersMouseReleased

private void getContextButton2ResourcesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getContextButton2ResourcesMouseReleased
    resourcesTextPane.setText(getContextAsSIs("\n"));
}//GEN-LAST:event_getContextButton2ResourcesMouseReleased

private void getContextButton2URIsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getContextButton2URIsMouseReleased
    uriTextPane.setText(getContextAsSIs("\n"));
}//GEN-LAST:event_getContextButton2URIsMouseReleased

private void searchGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchGetButtonMouseReleased
    searchField.setText(getContextAsString());
}//GEN-LAST:event_searchGetButtonMouseReleased

private void getContextButton2ProvenanceMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getContextButton2ProvenanceMouseReleased
    provenanceTextField.setText(getContextAsSI());
}//GEN-LAST:event_getContextButton2ProvenanceMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel byIdentifierButtonPanel;
    private javax.swing.JPanel byIdentifierInnerPanel;
    private javax.swing.JLabel byIdentifierLabel;
    private javax.swing.JPanel byIdentifierPanel;
    private javax.swing.JPanel byIdentifierTab;
    private javax.swing.JPanel byResourceButtonPanel;
    private javax.swing.JPanel byResourceInnerPanel;
    private javax.swing.JLabel byResourceLabel;
    private javax.swing.JPanel byResourcePanel;
    private javax.swing.JPanel byResourceTab;
    private javax.swing.JPanel byURIButtonPanel;
    private javax.swing.JPanel byURIInnerPanel;
    private javax.swing.JLabel byURILabel;
    private javax.swing.JPanel byURIPanel;
    private javax.swing.JPanel byURITab;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JButton getContextButton2Identifiers;
    private javax.swing.JButton getContextButton2Provenance;
    private javax.swing.JButton getContextButton2Resources;
    private javax.swing.JButton getContextButton2URIs;
    private javax.swing.JLabel identifiersLabel;
    private javax.swing.JScrollPane identifiersScrollPane;
    private javax.swing.JTextPane identifiersTextPane;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel provenanceLabel;
    private javax.swing.JTextField provenanceTextField;
    private javax.swing.JLabel resourcesLabel;
    private javax.swing.JScrollPane resourcesScrollPane;
    private javax.swing.JTextField resourcesSkipTextField;
    private javax.swing.JTextField resourcesTakeTextField;
    private javax.swing.JTextPane resourcesTextPane;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton searchGetButton;
    private javax.swing.JPanel searchInnerPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchTab;
    private javax.swing.JLabel skipLabel;
    private javax.swing.JLabel skipLabel1;
    private javax.swing.JTabbedPane subj3ctTabbedPane;
    private javax.swing.JLabel takeLabel;
    private javax.swing.JLabel takeLabel1;
    private javax.swing.JLabel uriLabel;
    private javax.swing.JScrollPane uriScrollPane;
    private javax.swing.JTextPane uriTextPane;
    private javax.swing.JTextField urisSkipTextField;
    private javax.swing.JTextField urisTakeTextField;
    private javax.swing.JTextField urlField;
    private javax.swing.JButton urlGetButton;
    private javax.swing.JPanel urlInnerPanel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JPanel urlTab;
    // End of variables declaration//GEN-END:variables

}
