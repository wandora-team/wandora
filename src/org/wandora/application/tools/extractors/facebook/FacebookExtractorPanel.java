/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://www.wandora.org/
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
 * FacebookExtractorPanel.java
 *
 * Created on 11.8.2010, 16:24:35
 */

package org.wandora.application.tools.extractors.facebook;


import org.wandora.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.utils.*;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;

import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.*;



/**
 *
 * @author akivela
 */
public class FacebookExtractorPanel extends javax.swing.JPanel {

    public static final String defaultAccessTokenText = "click here to paste your access token...";

    public static final int CONFIRM_TOKEN = 0;
    public static final int INTRO = 1;
    public static final int ACCESS_TOKEN = 2;
    public static final int EXTRACT_URLS = 3;


    private static final String accessTokenRequest = "http://www.wandora.org/wandora/fb/";
    private boolean wasCancelled = true;
    private boolean continueWithoutAuthorization = false;

    private static String accessToken = null;
    private JDialog myWindow = null;
    Wandora application = null;
    Context context = null;




    /** Creates new form FacebookExtractorPanel */
    public FacebookExtractorPanel(Wandora app) {
        application = app;
        initComponents();
    }


    // -------------------------------------------------------------------------


    public String getAccessToken() {
        return accessToken;
    }


    public boolean wasCancelled() {
        return wasCancelled;
    }


    public boolean continueWithoutAuthorization() {
        return continueWithoutAuthorization;
    }


    public int getDepth() {
        int depth = 1;
        String dstr = depthTextField.getText();
        try { depth = Integer.parseInt(dstr); }
        catch(Exception e) { /* NOTHING HERE */ }
        return depth;
    }


    // -------------------------------------------------------------------------


    
    public void open(Wandora app, WandoraTool tool, Context con) {
        context = con;
        wasCancelled = true;
        searchComboBox.setEditable(false);
        myWindow = new JDialog(app, "Facebook Graph extractor", true);

        myWindow.add(this);
        myWindow.setSize(700, 300);
        app.centerWindow(myWindow);
        if(accessToken == null)
            changePagePanel(INTRO);
        else
            changePagePanel(CONFIRM_TOKEN);
        myWindow.setVisible(true);
        
        // WAIT TILL CLOSED!
    }



    

    public void changePagePanel(int pn) {
        this.removeAll();
        switch(pn) {
            case CONFIRM_TOKEN: {
                this.add(panel0, BorderLayout.CENTER);
                myWindow.validate();
                break;
            }
            case INTRO: {
                this.add(panel1, BorderLayout.CENTER);
                myWindow.validate();
                break;
            }
            case ACCESS_TOKEN: {
                if(defaultAccessTokenText.equalsIgnoreCase(accessTokenTextField.getText())) {
                    accessTokenTextField.setForeground(Color.LIGHT_GRAY);
                }
                this.add(panel2, BorderLayout.CENTER);
                myWindow.validate();
                break;
            }
            case EXTRACT_URLS: {
                this.add(panel3, BorderLayout.CENTER);
                myWindow.validate();
                break;
            }
        }
    }



    public ArrayList<String> getExtractUrls() {
        ArrayList<String> urls = new ArrayList<String>();
        if(mePanel.equals(panel3TabbedPane.getSelectedComponent())) {
            urls.add( "https://graph.facebook.com/me" );
        }
        else if(urlPanel.equals(panel3TabbedPane.getSelectedComponent())) {
            String in = urlsTextPane.getText();
            String[] ins = in.split("\n");
            for(int i=0; i<ins.length; i++) {
                String ini = ins[i];
                if(ini != null) {
                    ini = ini.trim();
                    if(ini.length() > 0) {
                        urls.add(ini);
                    }
                }
            }
        }
        else if(searchPanel.equals(panel3TabbedPane.getSelectedComponent())) {
            String q = searchTextField.getText();
            String type = searchComboBox.getSelectedItem().toString();
            String offset = offsetTextField.getText();
            String limit = limitTextField.getText();
            String since = sinceTextField.getText();
            String until = untilTextField.getText();

            if(q != null && q.length() > 0) {
                String u = "https://graph.facebook.com/search?q="+encode(q);
                if(!"anything".equalsIgnoreCase(type) && type != null && type.length() > 0) {
                    u = u + "&type="+type;
                }
                try {
                    if(limit != null && limit.length() > 0) {
                        int l = Integer.parseInt(limit);
                        u = u + "&limit="+l;
                    }
                    if(offset != null && offset.length() > 0) {
                        int o = Integer.parseInt(offset);
                        u = u + "&offset="+o;
                    }
                }
                catch(Exception e) { }
                try {
                    if(since != null && since.length() > 0) {
                        u = u + "&since="+encode(since);
                    }
                    if(until != null && until.length() > 0) {
                        u = u + "&until="+encode(until);
                    }
                }
                catch(Exception e) { }
                System.out.println(u);
                urls.add(u);
            }
        }
        else if(idsPanel.equals(panel3TabbedPane.getSelectedComponent())) {
            String in = idsTextPane.getText();
            String[] ins = in.split("\n");
            for(int i=0; i<ins.length; i++) {
                String ini = ins[i];
                if(ini != null) {
                    ini = ini.trim();
                    if(ini.length() > 0) {
                        urls.add("https://graph.facebook.com/"+ini);
                    }
                }
            }
        }
        if(accessToken != null) {
            urls = addParameter(urls, "access_token", accessToken);
        }
        urls = addParameter(urls, "metadata", "1");
        return urls;
    }

    
    
    private String encode(String str) {
        try { 
            return URLEncoder.encode(str, "utf-8"); 
        }
        catch(Exception e) { 
            return URLEncoder.encode(str); 
        }
    }


    public ArrayList<String> addParameter(ArrayList<String> urlList, String param, String value) {
        ArrayList<String> newUrls = new ArrayList<String>();
        for( String u : urlList ) {
            if(u != null) {
                u = u.trim();
                if(u.length() > 0) {
                    if(u.indexOf("?") == -1) u = u + "?";
                    else u = u + "&";
                    u = u + param + "=" + value;
                    newUrls.add(u);
                }
            }
        }
        return newUrls;
    }





    public void getSIsFromContext() {
        String delim = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("");
        if(urlsTextPane.getText().length() > 0) {
            sb.append(urlsTextPane.getText()).append(delim);
        }
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
                                sb.append(str).append(delim);
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        urlsTextPane.setText( sb.toString() );
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

        panel1 = new javax.swing.JPanel();
        infoLabel = new SimpleLabel();
        fillerPanel1 = new javax.swing.JPanel();
        panel1ContinueButton = new SimpleButton();
        panel1ExtractButton = new SimpleButton();
        panel1CancelButton = new SimpleButton();
        panel2 = new javax.swing.JPanel();
        jLabel1 = new SimpleLabel();
        accessTokenTextField = new SimpleField();
        jPanel6 = new javax.swing.JPanel();
        panel2FillerPanel = new javax.swing.JPanel();
        panel2ContinueButton = new SimpleButton();
        panel2CancelButton = new SimpleButton();
        panel3 = new javax.swing.JPanel();
        panel3TabbedPane = new SimpleTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new SimpleLabel();
        searchTextField = new SimpleField();
        searchGetPanel = new javax.swing.JPanel();
        typeLabel = new SimpleLabel();
        searchComboBox = new SimpleComboBox();
        offsetLabel = new SimpleLabel();
        offsetTextField = new SimpleField();
        limitLabel = new SimpleLabel();
        limitTextField = new SimpleField();
        untilLabel = new SimpleLabel();
        untilTextField = new SimpleField();
        sinceLabel = new SimpleLabel();
        sinceTextField = new SimpleField();
        urlPanel = new javax.swing.JPanel();
        urlInfo = new SimpleLabel();
        jScrollPane1 = new SimpleScrollPane();
        urlsTextPane = new SimpleTextPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        getSIButton = new SimpleButton();
        idsPanel = new javax.swing.JPanel();
        idsLabel = new SimpleLabel();
        idsScrollPane = new SimpleScrollPane();
        idsTextPane = new SimpleTextPane();
        mePanel = new javax.swing.JPanel();
        meInfo = new SimpleLabel();
        panel3Buttons = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        depthLabel = new SimpleLabel();
        depthTextField = new SimpleField();
        jPanel2 = new javax.swing.JPanel();
        extractButton = new SimpleButton();
        cancelButton = new SimpleButton();
        panel0 = new javax.swing.JPanel();
        panel0Label = new SimpleLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        panel0UseButton = new SimpleButton();
        panel0RequestButton = new SimpleButton();
        panel0CancelButton = new SimpleButton();

        panel1.setLayout(new java.awt.GridBagLayout());

        infoLabel.setText("<html>This is Wandora's Facebook Graph extractor. Extractor converts Facebook Graph data to a topic map. In order to make a successful graph transformation, user has to authorize Wandora by giving an access token. Requesting an access token is carried out in WWW browser. Next steps are required for authorization:\n<br><br>\n 1. User has to log in Facebook.<br>\n 2. User has to authorize Wandora Facebook application to access her profile data.<br>\n 3. Wandora Facebook application gives you an access token.<br>\n 4. User has to copy the access token from WWW page to the Wandora application.\n<br><br>\nYou can read more about Facebook authorization at http://developers.facebook.com/docs/authentication/<br>\nTo continue press Open Browser and Request Access Token button. If you have already a valid Facebook Graph JSON feed, press Extract. To close this dialog, press Cancel.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 16, 12);
        panel1.add(infoLabel, gridBagConstraints);

        javax.swing.GroupLayout fillerPanel1Layout = new javax.swing.GroupLayout(fillerPanel1);
        fillerPanel1.setLayout(fillerPanel1Layout);
        fillerPanel1Layout.setHorizontalGroup(
            fillerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        fillerPanel1Layout.setVerticalGroup(
            fillerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panel1.add(fillerPanel1, gridBagConstraints);

        panel1ContinueButton.setText("Open browser and request access token");
        panel1ContinueButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel1ContinueButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        panel1.add(panel1ContinueButton, gridBagConstraints);

        panel1ExtractButton.setText("Extract");
        panel1ExtractButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel1ExtractButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        panel1.add(panel1ExtractButton, gridBagConstraints);

        panel1CancelButton.setText("Cancel");
        panel1CancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel1CancelButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        panel1.add(panel1CancelButton, gridBagConstraints);

        panel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("<html>Wandora has now started a WWW browser. If you are not logged in Facebook, or have not authorized Wandora Facebook application, you are first redirected to the Facebook.\n<br>\n<br>\nEventually browser opens a Wandora Access Token page with a text field. Text field contains an access token. Please copy the token to the text field below and press Continue button. If Wandora application is not able to start external WWW browser for some reason, you can request access token manually at http://www.wandora.org/wandora/fb/</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 14, 12);
        panel2.add(jLabel1, gridBagConstraints);

        accessTokenTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        accessTokenTextField.setText("click here to paste your access token...");
        accessTokenTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                accessTokenTextFieldFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 16, 8);
        panel2.add(accessTokenTextField, gridBagConstraints);

        jPanel6.setPreferredSize(new java.awt.Dimension(20, 20));
        jPanel6.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel2.add(jPanel6, gridBagConstraints);

        javax.swing.GroupLayout panel2FillerPanelLayout = new javax.swing.GroupLayout(panel2FillerPanel);
        panel2FillerPanel.setLayout(panel2FillerPanelLayout);
        panel2FillerPanelLayout.setHorizontalGroup(
            panel2FillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panel2FillerPanelLayout.setVerticalGroup(
            panel2FillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panel2.add(panel2FillerPanel, gridBagConstraints);

        panel2ContinueButton.setText("Continue to extract");
        panel2ContinueButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel2ContinueButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        panel2.add(panel2ContinueButton, gridBagConstraints);

        panel2CancelButton.setText("Cancel");
        panel2CancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel2CancelButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        panel2.add(panel2CancelButton, gridBagConstraints);

        panel3.setLayout(new java.awt.GridBagLayout());

        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("<html>Search Facebook with a query</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        searchPanel.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 16, 6);
        searchPanel.add(searchTextField, gridBagConstraints);

        searchGetPanel.setLayout(new java.awt.GridBagLayout());

        typeLabel.setText("Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 5);
        searchGetPanel.add(typeLabel, gridBagConstraints);

        searchComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "anything", "achievement", "album", "application", "checkin", "comment", "domain", "event", "friendlist", "group", "insights", "link", "location", "message", "note", "offer", "order", "page", "photo", "place", "post", "question", "question_option", "review", "status", "subscription", "thread", "user", "video", " " }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        searchGetPanel.add(searchComboBox, gridBagConstraints);

        offsetLabel.setText("Offset");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 5);
        searchGetPanel.add(offsetLabel, gridBagConstraints);

        offsetTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        offsetTextField.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        searchGetPanel.add(offsetTextField, gridBagConstraints);

        limitLabel.setText("Limit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 5);
        searchGetPanel.add(limitLabel, gridBagConstraints);

        limitTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        limitTextField.setText("25");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        searchGetPanel.add(limitTextField, gridBagConstraints);

        untilLabel.setText("Until (a unix timestamp or any date accepted by strtotime)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 5);
        searchGetPanel.add(untilLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        searchGetPanel.add(untilTextField, gridBagConstraints);

        sinceLabel.setText("Since (a unix timestamp or any date accepted by strtotime)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 5);
        searchGetPanel.add(sinceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        searchGetPanel.add(sinceTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        searchPanel.add(searchGetPanel, gridBagConstraints);

        panel3TabbedPane.addTab("Search", searchPanel);

        urlPanel.setLayout(new java.awt.GridBagLayout());

        urlInfo.setText("<html>Please write Facebook Graph URLs to the text field below. In order to make successful extraction, the access token given earlier must have appropriate rights. Extractor adds automatically URLs a parameter <i>metadata</i> with a value <i>1</i>.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        urlPanel.add(urlInfo, gridBagConstraints);

        jScrollPane1.setViewportView(urlsTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        urlPanel.add(jScrollPane1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel4, new java.awt.GridBagConstraints());

        getSIButton.setText("Get Subject Identifier");
        getSIButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getSIButtonMouseReleased(evt);
            }
        });
        jPanel3.add(getSIButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        urlPanel.add(jPanel3, gridBagConstraints);

        panel3TabbedPane.addTab("URLs", urlPanel);

        idsPanel.setLayout(new java.awt.GridBagLayout());

        idsLabel.setText("<html>Please write Facebook Graph IDs to the field below. In order to make a successful conversion, your access token must have appropriate privileges.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        idsPanel.add(idsLabel, gridBagConstraints);

        idsScrollPane.setViewportView(idsTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        idsPanel.add(idsScrollPane, gridBagConstraints);

        panel3TabbedPane.addTab("Ids", idsPanel);

        mePanel.setLayout(new java.awt.GridBagLayout());

        meInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        meInfo.setText("<html><center>Select this tab to extract your own Facebook profile data using URL address<br>\nhttps://graph.facebook.com/me?metadata=1</center></html>");
        meInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        mePanel.add(meInfo, gridBagConstraints);

        panel3TabbedPane.addTab("Me", mePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel3.add(panel3TabbedPane, gridBagConstraints);

        panel3Buttons.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        depthLabel.setText("depth");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(depthLabel, gridBagConstraints);

        depthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        depthTextField.setText("1");
        depthTextField.setMinimumSize(new java.awt.Dimension(30, 23));
        depthTextField.setPreferredSize(new java.awt.Dimension(30, 23));
        jPanel1.add(depthTextField, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panel3Buttons.add(jPanel1, gridBagConstraints);

        extractButton.setText("Extract");
        extractButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                extractButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        panel3Buttons.add(extractButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        panel3Buttons.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        panel3.add(panel3Buttons, gridBagConstraints);

        panel0.setLayout(new java.awt.GridBagLayout());

        panel0Label.setText("<html>\nIt looks like you have already given an access token to Wandora application. Would you like to use old access token or request new one? Press Cancel to abort extractor.\n</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 8, 20);
        panel0.add(panel0Label, gridBagConstraints);

        jPanel7.setPreferredSize(new java.awt.Dimension(20, 20));
        jPanel7.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel0.add(jPanel7, gridBagConstraints);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panel0.add(jPanel5, gridBagConstraints);

        panel0UseButton.setText("Use old token");
        panel0UseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel0UseButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        panel0.add(panel0UseButton, gridBagConstraints);

        panel0RequestButton.setText("Request new token");
        panel0RequestButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel0RequestButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        panel0.add(panel0RequestButton, gridBagConstraints);

        panel0CancelButton.setText("Cancel");
        panel0CancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                panel0CancelButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        panel0.add(panel0CancelButton, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void accessTokenTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_accessTokenTextFieldFocusGained
        if(defaultAccessTokenText.equalsIgnoreCase(accessTokenTextField.getText())) {
            accessTokenTextField.setText("");
            accessTokenTextField.setForeground(Color.BLACK);
            accessTokenTextField.setText(ClipboardBox.getClipboard());
        }
    }//GEN-LAST:event_accessTokenTextFieldFocusGained

    private void panel1CancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel1CancelButtonMouseReleased
        wasCancelled = true;
        myWindow.setVisible(false);
    }//GEN-LAST:event_panel1CancelButtonMouseReleased

    private void panel1ExtractButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel1ExtractButtonMouseReleased
        continueWithoutAuthorization = true;
        wasCancelled = false;
        myWindow.setVisible(false);
    }//GEN-LAST:event_panel1ExtractButtonMouseReleased

    private void panel1ContinueButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel1ContinueButtonMouseReleased
        try {
            changePagePanel(ACCESS_TOKEN);
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(accessTokenRequest));
        }
        catch(Exception e) {

        }
    }//GEN-LAST:event_panel1ContinueButtonMouseReleased



    


    private void panel2ContinueButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel2ContinueButtonMouseReleased
        wasCancelled = false;
        accessToken = accessTokenTextField.getText();
        accessToken = accessToken.trim();
        changePagePanel(EXTRACT_URLS);
    }//GEN-LAST:event_panel2ContinueButtonMouseReleased

    private void panel2CancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel2CancelButtonMouseReleased
        wasCancelled = true;
        myWindow.setVisible(false);
    }//GEN-LAST:event_panel2CancelButtonMouseReleased

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        wasCancelled = true;
        myWindow.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    private void extractButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extractButtonMouseReleased
        wasCancelled = false;
        myWindow.setVisible(false);
    }//GEN-LAST:event_extractButtonMouseReleased

    private void getSIButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getSIButtonMouseReleased
        getSIsFromContext();
    }//GEN-LAST:event_getSIButtonMouseReleased

    private void panel0CancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel0CancelButtonMouseReleased
        wasCancelled = true;
        myWindow.setVisible(false);
    }//GEN-LAST:event_panel0CancelButtonMouseReleased

    private void panel0RequestButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel0RequestButtonMouseReleased
        try {
            changePagePanel(ACCESS_TOKEN);
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(accessTokenRequest));
        }
        catch(Exception e) {

        }
    }//GEN-LAST:event_panel0RequestButtonMouseReleased

    private void panel0UseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel0UseButtonMouseReleased
        changePagePanel(EXTRACT_URLS);
    }//GEN-LAST:event_panel0UseButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField accessTokenTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel depthLabel;
    private javax.swing.JTextField depthTextField;
    private javax.swing.JButton extractButton;
    private javax.swing.JPanel fillerPanel1;
    private javax.swing.JButton getSIButton;
    private javax.swing.JLabel idsLabel;
    private javax.swing.JPanel idsPanel;
    private javax.swing.JScrollPane idsScrollPane;
    private javax.swing.JTextPane idsTextPane;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel limitLabel;
    private javax.swing.JTextField limitTextField;
    private javax.swing.JLabel meInfo;
    private javax.swing.JPanel mePanel;
    private javax.swing.JLabel offsetLabel;
    private javax.swing.JTextField offsetTextField;
    private javax.swing.JPanel panel0;
    private javax.swing.JButton panel0CancelButton;
    private javax.swing.JLabel panel0Label;
    private javax.swing.JButton panel0RequestButton;
    private javax.swing.JButton panel0UseButton;
    private javax.swing.JPanel panel1;
    private javax.swing.JButton panel1CancelButton;
    private javax.swing.JButton panel1ContinueButton;
    private javax.swing.JButton panel1ExtractButton;
    private javax.swing.JPanel panel2;
    private javax.swing.JButton panel2CancelButton;
    private javax.swing.JButton panel2ContinueButton;
    private javax.swing.JPanel panel2FillerPanel;
    private javax.swing.JPanel panel3;
    private javax.swing.JPanel panel3Buttons;
    private javax.swing.JTabbedPane panel3TabbedPane;
    private javax.swing.JComboBox searchComboBox;
    private javax.swing.JPanel searchGetPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JLabel sinceLabel;
    private javax.swing.JTextField sinceTextField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel untilLabel;
    private javax.swing.JTextField untilTextField;
    private javax.swing.JLabel urlInfo;
    private javax.swing.JPanel urlPanel;
    private javax.swing.JTextPane urlsTextPane;
    // End of variables declaration//GEN-END:variables

}
