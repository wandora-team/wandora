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
 * OviExtractorSelector.java
 *
 * Created on 20. marraskuuta 2008, 13:32
 */

package org.wandora.application.tools.extractors.ovi;


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
public class OviExtractorSelector extends JDialog {
    public static String defaultLanguage = "en";
    private String defaultEncoding = "ISO-8859-1";
    
    private static final String oviURL = "http://share.ovi.com/feeds/rss/search/media/";
    private static final String oviTaggedURL=oviURL+"tag:__1__?sort=&page=1&count=__2__";
    
    private static final String oviLocatedURL=oviURL+"location:__1__?sort=&page=1&count=__2__";
    private static final String oviCityURL=oviURL+"city:__1__?sort=&page=1&count=__2__";
    private static final String oviCountryURL=oviURL+"country:__1__?sort=&page=1&count=__2__";
    private static final String oviStateURL=oviURL+"stateOrProvince:__1__?sort=&page=1&count=__2__";
    private static final String oviPostalURL=oviURL+"postalCode:__1__?sort=&page=1&count=__2__";
    
    private static final String oviOwnerURL=oviURL+"owner:__1__?sort=&page=1&count=__2__";
    private static final String oviAuthorURL=oviURL+"author:__1__?sort=&page=1&count=__2__";
    
    private static final String oviTitleURL=oviURL+"title:__1__?sort=&page=1&count=__2__";
    private static final String oviDescriptionURL=oviURL+"description:__1__?sort=&page=1&count=__2__";
    
    private static final String oviChannelURL=oviURL+"channel/__1__?page=1&count=__2__";
    
    private static final String oviDateURL=oviURL+"dateAdded:__1__?sort=&page=1&count=__2__";
    
    
    private Wandora admin = null;
    private Context context = null;
    private boolean accepted = false;
    
    
    /** Creates new form OviExtractorSelector */
    public OviExtractorSelector(Wandora admin) {
        super(admin, true);
        initComponents();
        
        ownerComboBox.setEditable(false);
        locationComboBox.setEditable(false);
        titleComboBox.setEditable(false);
        
        setTitle("Ovi extractor");
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
        Component component = oviTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        String entryCountStr = entriesNumberTextField.getText();
        int entryCount = 9;
        try {
            entryCount = Integer.parseInt(entryCountStr);
        }
        catch(Exception e) {
            // Illegal number of entry count. Using default!
        }
        
        // ***** TAGGED *****
        if(tagTab.equals(component)) {
            String tagAll = tagField.getText();
            String[] tags = urlEncode(commaSplitter(tagAll));
            String[] tagUrls = completeString(oviTaggedURL, tags, ""+entryCount);
            
            OviMediaExtractor ex = new OviMediaExtractor();
            ex.setForceUrls( tagUrls );
            wt = ex;
        }
        
        // ***** LOCATED *****
        else if(locationTab.equals(component)) {
            String locationAll = locationField.getText();
            String[] locations = urlEncode(commaSplitter(locationAll));
            String locationType = locationComboBox.getSelectedItem().toString();
            
            String urlTemplate = oviLocatedURL;
            if("city".equalsIgnoreCase(locationType)) { urlTemplate = oviCityURL; }
            else if("country".equalsIgnoreCase(locationType)) { urlTemplate = oviCountryURL; }
            else if("state or province".equalsIgnoreCase(locationType)) { urlTemplate = oviStateURL; }
            else if("postal code".equalsIgnoreCase(locationType)) { urlTemplate = oviPostalURL; }
            
            String[] locationUrls = completeString(urlTemplate, locations, ""+entryCount);
            OviMediaExtractor ex = new OviMediaExtractor();
            ex.setForceUrls( locationUrls );
            wt = ex;
        }
        
        // ***** OWNED & AUTHOR *****
        else if(ownerTab.equals(component)) {
            String ownerAll = ownerField.getText();
            String[] owners = urlEncode(commaSplitter(ownerAll));
            String ownerType = ownerComboBox.getSelectedItem().toString();
            String urlTemplate = oviOwnerURL;
            if("author".equalsIgnoreCase(ownerType)) urlTemplate = oviAuthorURL;
            
            String[] ownerUrls = completeString(urlTemplate, owners, ""+entryCount);
            
            OviMediaExtractor ex = new OviMediaExtractor();
            ex.setForceUrls( ownerUrls );
            wt = ex;
        }
        
        // ***** TITLED *****
        else if(titleTab.equals(component)) {
            String titleAll = titleField.getText();
            String[] titles = urlEncode(new String[] { titleAll } );
            String titleType = titleComboBox.getSelectedItem().toString();
            String urlTemplate = oviTitleURL;
            if("description".equalsIgnoreCase(titleType)) urlTemplate = oviDescriptionURL;
            
            String[] titleUrls = completeString(urlTemplate, titles, ""+entryCount);
            
            OviMediaExtractor ex = new OviMediaExtractor();
            ex.setForceUrls( titleUrls );
            wt = ex;
        }
        
        // ***** CHANNEL *****
        else if(channelTab.equals(component)) {
            String channelAll = channelField.getText();
            String[] channels = urlEncode(commaSplitter(channelAll));
            String[] channelUrls = completeString(oviChannelURL, channels, ""+entryCount);
            
            OviMediaExtractor ex = new OviMediaExtractor();
            ex.setForceUrls( channelUrls );
            wt = ex;
        }
        
        // ***** DATE *****
        else if(dateTab.equals(component)) {
            String dateAll = dateField.getText();
            String[] dates = urlEncode(commaSplitter(dateAll));
            String[] dateUrls = completeString(oviDateURL, dates, ""+entryCount);
            
            OviMediaExtractor ex = new OviMediaExtractor();
            ex.setForceUrls( dateUrls );
            wt = ex;
        }
        
        // ***** URL *****
        else if(urlTab.equals(component)) {
            String url = urlField.getText();
            OviMediaExtractor ex = new OviMediaExtractor();
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
    
    

    public String getContextAsURL() {
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
                        Locator l = t.getSubjectLocator();
                        if(l != null) {
                            str = l.toExternalForm().trim();
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
    

    
    
    public String getContextAsDates() {
        StringBuffer sb = new StringBuffer("");
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                String str = null;
                Object o = null;
                TopicMap tm = admin.getTopicMap();
                Topic dateType = tm.getTopic(OviMediaExtractor.OVI_DATE_SI);
                if(dateType != null) {
                    while(contextObjects.hasNext()) {
                        str = null;
                        o = contextObjects.next();
                        if(o instanceof Topic) {
                            Topic t = (Topic) o;
                            if(t.isOfType(dateType)) {
                                str = t.getBaseName();
                                if(str != null) {
                                    if(str.endsWith(" (ovi date)")) {
                                        str = str.substring(0, str.length()-" (ovi date)".length());
                                    }
                                    try {
                                        DateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
                                        Date date = (Date)formatter.parse(str);
                                        DateFormat printFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                        str = printFormatter.format(date);
                                    }
                                    catch(Exception edate) {}
                                }
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

        oviTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        tagTab = new javax.swing.JPanel();
        tagInnerPanel = new javax.swing.JPanel();
        tagLabel = new org.wandora.application.gui.simple.SimpleLabel();
        tagField = new org.wandora.application.gui.simple.SimpleField();
        tagGetButton = new org.wandora.application.gui.simple.SimpleButton();
        locationTab = new javax.swing.JPanel();
        locationInnerPanel = new javax.swing.JPanel();
        locationLabel = new org.wandora.application.gui.simple.SimpleLabel();
        locationComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        locationField = new org.wandora.application.gui.simple.SimpleField();
        locationGetButton = new org.wandora.application.gui.simple.SimpleButton();
        ownerTab = new javax.swing.JPanel();
        ownerInnerPanel = new javax.swing.JPanel();
        ownerLabel = new org.wandora.application.gui.simple.SimpleLabel();
        ownerComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        ownerField = new org.wandora.application.gui.simple.SimpleField();
        ownerGetButton = new org.wandora.application.gui.simple.SimpleButton();
        titleTab = new javax.swing.JPanel();
        titleInnerPanel = new javax.swing.JPanel();
        titleLabel = new org.wandora.application.gui.simple.SimpleLabel();
        titleComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        titleField = new org.wandora.application.gui.simple.SimpleField();
        titleGetButton = new org.wandora.application.gui.simple.SimpleButton();
        channelTab = new javax.swing.JPanel();
        channelInnerPanel = new javax.swing.JPanel();
        channelLabel = new org.wandora.application.gui.simple.SimpleLabel();
        channelField = new org.wandora.application.gui.simple.SimpleField();
        channelGetButton = new org.wandora.application.gui.simple.SimpleButton();
        dateTab = new javax.swing.JPanel();
        dateInnerPanel = new javax.swing.JPanel();
        dateLabel = new org.wandora.application.gui.simple.SimpleLabel();
        dateField = new org.wandora.application.gui.simple.SimpleField();
        dateGetButton = new org.wandora.application.gui.simple.SimpleButton();
        urlTab = new javax.swing.JPanel();
        urlInnerPanel = new javax.swing.JPanel();
        urlLabel = new org.wandora.application.gui.simple.SimpleLabel();
        urlField = new org.wandora.application.gui.simple.SimpleField();
        urlGetButton = new org.wandora.application.gui.simple.SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        getLabel = new org.wandora.application.gui.simple.SimpleLabel();
        entriesNumberTextField = new org.wandora.application.gui.simple.SimpleField();
        entriesLabel = new org.wandora.application.gui.simple.SimpleLabel();
        emptyPanel = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        tagTab.setLayout(new java.awt.GridBagLayout());

        tagInnerPanel.setLayout(new java.awt.GridBagLayout());

        tagLabel.setText("<html>Fetch media with given tag. Use comma (,) character to separate different tag terms.</html>");
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
        tagGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
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

        oviTabbedPane.addTab("Tagged", tagTab);

        locationTab.setLayout(new java.awt.GridBagLayout());

        locationInnerPanel.setLayout(new java.awt.GridBagLayout());

        locationLabel.setText("<html>Fetch media located into a given location. Please write location names below or get the context. Use comma (,) character to separate different locations.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        locationInnerPanel.add(locationLabel, gridBagConstraints);

        locationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Location", "City", "Country", "State or province", "Postal code" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        locationInnerPanel.add(locationComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        locationInnerPanel.add(locationField, gridBagConstraints);

        locationGetButton.setLabel("Get context");
        locationGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        locationGetButton.setPreferredSize(new java.awt.Dimension(80, 20));
        locationGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                locationGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        locationInnerPanel.add(locationGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        locationTab.add(locationInnerPanel, gridBagConstraints);

        oviTabbedPane.addTab("Located", locationTab);

        ownerTab.setLayout(new java.awt.GridBagLayout());

        ownerInnerPanel.setLayout(new java.awt.GridBagLayout());

        ownerLabel.setText("<html>Fetch media for given Ovi owners or authors. Please write user names below or get the context. Use comma (,) character to separate different names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        ownerInnerPanel.add(ownerLabel, gridBagConstraints);

        ownerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Owner", "Author" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        ownerInnerPanel.add(ownerComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        ownerInnerPanel.add(ownerField, gridBagConstraints);

        ownerGetButton.setLabel("Get context");
        ownerGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        ownerGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ownerGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        ownerInnerPanel.add(ownerGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        ownerTab.add(ownerInnerPanel, gridBagConstraints);

        oviTabbedPane.addTab("Owned", ownerTab);

        titleTab.setLayout(new java.awt.GridBagLayout());

        titleInnerPanel.setLayout(new java.awt.GridBagLayout());

        titleLabel.setText("<html>Fetch media with given text in media title or description.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        titleInnerPanel.add(titleLabel, gridBagConstraints);

        titleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Title", "Description" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        titleInnerPanel.add(titleComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        titleInnerPanel.add(titleField, gridBagConstraints);

        titleGetButton.setLabel("Get context");
        titleGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        titleGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                titleGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        titleInnerPanel.add(titleGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        titleTab.add(titleInnerPanel, gridBagConstraints);

        oviTabbedPane.addTab("Titled", titleTab);

        channelTab.setLayout(new java.awt.GridBagLayout());

        channelInnerPanel.setLayout(new java.awt.GridBagLayout());

        channelLabel.setText("<html>Read named media channel on Ovi. Please write channel name below. Use comma (,) character to separate different channel names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        channelInnerPanel.add(channelLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        channelInnerPanel.add(channelField, gridBagConstraints);

        channelGetButton.setLabel("Get context");
        channelGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        channelGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                channelGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        channelInnerPanel.add(channelGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        channelTab.add(channelInnerPanel, gridBagConstraints);

        oviTabbedPane.addTab("Channel", channelTab);

        dateTab.setLayout(new java.awt.GridBagLayout());

        dateInnerPanel.setLayout(new java.awt.GridBagLayout());

        dateLabel.setText("<html>Fetch media added at given time. Please write date below in dd/mm/yyyy format. Separate different dates with comma character.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        dateInnerPanel.add(dateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        dateInnerPanel.add(dateField, gridBagConstraints);

        dateGetButton.setLabel("Get context");
        dateGetButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        dateGetButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dateGetButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
        dateInnerPanel.add(dateGetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        dateTab.add(dateInnerPanel, gridBagConstraints);

        oviTabbedPane.addTab("Dated", dateTab);

        urlTab.setLayout(new java.awt.GridBagLayout());

        urlInnerPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText("<html>Fetch media entries with given URL address. URL should resolve exactly Ovi media feed.</html>");
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

        oviTabbedPane.addTab("URL", urlTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(oviTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        getLabel.setText("Get");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(getLabel, gridBagConstraints);

        entriesNumberTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        entriesNumberTextField.setText("50");
        entriesNumberTextField.setMinimumSize(new java.awt.Dimension(30, 23));
        entriesNumberTextField.setPreferredSize(new java.awt.Dimension(30, 23));
        buttonPanel.add(entriesNumberTextField, new java.awt.GridBagConstraints());

        entriesLabel.setText("entries");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        buttonPanel.add(entriesLabel, gridBagConstraints);

        emptyPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout emptyPanelLayout = new javax.swing.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 167, Short.MAX_VALUE)
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

private void locationGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_locationGetButtonMouseReleased
    locationField.setText(getContextAsString());
}//GEN-LAST:event_locationGetButtonMouseReleased

private void ownerGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ownerGetButtonMouseReleased
    ownerField.setText(getContextAsString());
}//GEN-LAST:event_ownerGetButtonMouseReleased

private void titleGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleGetButtonMouseReleased
    titleField.setText(getContextAsString());
}//GEN-LAST:event_titleGetButtonMouseReleased

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

private void channelGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_channelGetButtonMouseReleased
// TODO add your handling code here:
}//GEN-LAST:event_channelGetButtonMouseReleased

private void dateGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dateGetButtonMouseReleased
    urlField.setText(getContextAsDates());
}//GEN-LAST:event_dateGetButtonMouseReleased

private void urlGetButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlGetButtonMouseReleased
    urlField.setText(getContextAsURL());
}//GEN-LAST:event_urlGetButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField channelField;
    private javax.swing.JButton channelGetButton;
    private javax.swing.JPanel channelInnerPanel;
    private javax.swing.JLabel channelLabel;
    private javax.swing.JPanel channelTab;
    private javax.swing.JTextField dateField;
    private javax.swing.JButton dateGetButton;
    private javax.swing.JPanel dateInnerPanel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JPanel dateTab;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JLabel entriesLabel;
    private javax.swing.JTextField entriesNumberTextField;
    private javax.swing.JLabel getLabel;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JTextField locationField;
    private javax.swing.JButton locationGetButton;
    private javax.swing.JPanel locationInnerPanel;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JPanel locationTab;
    private javax.swing.JButton okButton;
    private javax.swing.JTabbedPane oviTabbedPane;
    private javax.swing.JComboBox ownerComboBox;
    private javax.swing.JTextField ownerField;
    private javax.swing.JButton ownerGetButton;
    private javax.swing.JPanel ownerInnerPanel;
    private javax.swing.JLabel ownerLabel;
    private javax.swing.JPanel ownerTab;
    private javax.swing.JTextField tagField;
    private javax.swing.JButton tagGetButton;
    private javax.swing.JPanel tagInnerPanel;
    private javax.swing.JLabel tagLabel;
    private javax.swing.JPanel tagTab;
    private javax.swing.JComboBox titleComboBox;
    private javax.swing.JTextField titleField;
    private javax.swing.JButton titleGetButton;
    private javax.swing.JPanel titleInnerPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel titleTab;
    private javax.swing.JTextField urlField;
    private javax.swing.JButton urlGetButton;
    private javax.swing.JPanel urlInnerPanel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JPanel urlTab;
    // End of variables declaration//GEN-END:variables

}
