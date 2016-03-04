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
 * YouTubeVideoFeedSelector.java
 *
 * Created on 9. toukokuuta 2008, 13:25
 */

package org.wandora.application.tools.extractors.youtube;


import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.util.regex.*;

import com.google.gdata.client.youtube.*;
import com.google.gdata.data.youtube.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import com.google.gdata.client.*;
import com.google.gdata.data.*;

import com.google.gdata.client.youtube.*;
import com.google.gdata.data.youtube.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import org.wandora.application.gui.*;



/**
 *
 * @author  akivela
 */
public class YouTubeVideoFeedSelector extends javax.swing.JDialog {

    public static final String YOUTUBE_VIDEO_URL = "http://gdata.youtube.com/feeds/api/videos";
    public static final String YOUTUBE_BASE_URL = "http://gdata.youtube.com/feeds/api/";
    protected String USER_URL_PREFIX = "http://gdata.youtube.com/feeds/api/users/";
    protected String USER_URL_POSTFIX = "/uploads";
    
    private boolean accepted = false;
    
    private static final Pattern youtubeUrlPattern=Pattern.compile("^http://www.youtube.com/watch\\?(?:.*&)?v=([^&]+)(?:&.*)?$");
    
    /** Creates new form YouTubeVideoFeedSelector */
    public YouTubeVideoFeedSelector(Wandora admin, WandoraTool tool) {
        super(admin, true);
        setSize(450,220);
        setTitle("YouTube video feed selector");
        admin.centerWindow(this);
        initComponents();
        initDefaultFeeds(admin, tool);
        feedComboBox.setEditable(false);
        searchSizeComboBox.setEditable(false);
        searchSizeComboBox.setSelectedIndex(3);
        defaultTimeComboBox.setEditable(false);
    }
    
    
    
    public void initDefaultFeeds(Wandora admin, WandoraTool tool) {
        for(int i=0; i<YouTubeExtractor.standardVideoFeeds.length; i += 2) {
            try {
                feedComboBox.addItem(YouTubeExtractor.standardVideoFeeds[i]);
            }
            catch(Exception e) {
                tool.log(e);
            }
        }
    }

    public int processVideoFeedURL(Wandora admin, TopicMap topicMap, YouTubeService service, YouTubeVideoFeedExtractor tool,String url) throws java.io.IOException, ServiceException, TopicMapException {
        int counter=0;
        Matcher m=youtubeUrlPattern.matcher(url);
        URL feedUrl = new URL(url);
        if(m.matches()){
            String videoid=m.group(1);
            VideoEntry videoEntry=service.getEntry(new URL(YOUTUBE_VIDEO_URL+"/"+videoid), VideoEntry.class);
            tool.extract(videoEntry, null , feedUrl, admin, topicMap);
            counter+=1;
        }
        else{
            tool.log("Reading YouTube video feed from URL '"+feedUrl.toExternalForm()+"'.");
            int resultSize = getSearchSize();
            for(int r=0; r<resultSize; r+=50) {
                YouTubeQuery query = new YouTubeQuery(feedUrl);
                if(resultSize>50) query.setStartIndex(r+1);
                query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                VideoFeed videoFeed = service.getFeed(query, VideoFeed.class);
                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
            }
        }
        return counter;

    }
    
    
    public int processVideoFeeds(Wandora admin, Context context, YouTubeService service, YouTubeVideoFeedExtractor tool) {
        int counter = 0;
        String feed = null;
        URL feedUrl = null;
        VideoFeed videoFeed = null;

        TopicMap topicMap = admin.getTopicMap();

        Component component = tabbedPane.getSelectedComponent();
        try {
            // ***** Default video feeds *****
            if(defaultFeeds.equals(component)) {
                String selectedDefaultFeed = (String) feedComboBox.getItemAt(feedComboBox.getSelectedIndex());
                if(selectedDefaultFeed != null) {
                    for(int i=0; i<YouTubeExtractor.standardVideoFeeds.length; i += 2) {
                        if(selectedDefaultFeed.equals(YouTubeExtractor.standardVideoFeeds[i])) {

                            feed = YouTubeExtractor.standardVideoFeeds[i+1];
                            feedUrl = new URL(feed);
                            int resultSize = getSearchSize();
                            tool.log("Reading YouTube video feed '"+selectedDefaultFeed+"'.");
                            for(int r=0; r<resultSize; r+=50) {
                                YouTubeQuery query = new YouTubeQuery(feedUrl);
                                if(!feed.endsWith("recently_featured") && !feed.endsWith("watch_on_mobile")) {
                                    query.setTime(getFeedTime());
                                }
                                if(resultSize>50) query.setStartIndex(r+1);
                                query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                                videoFeed = service.getFeed(query, VideoFeed.class);
                                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                            }

                        }
                    }
                }
            }
            
            // ***** Search for videos *****
            else if(searchPanel.equals(component)) {
                String queryStr = searchTextField.getText();
                if(queryStr != null && queryStr.length() > 0) {
                    int resultSize = getSearchSize();
                    for(int r=0; r<resultSize; r+=50) {
                        YouTubeQuery query = new YouTubeQuery(new URL(YOUTUBE_VIDEO_URL));
                        query.setFullTextQuery(queryStr);
                        query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
                        if(resultSize>50) query.setStartIndex(r+1);
                        query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                        videoFeed = service.query(query, VideoFeed.class);
                        counter += tool.extract(videoFeed, feedUrl, admin, topicMap);

                    }
                }
            }
            
            // ***** Extract with username *****
            else if(userFeed.equals(component)) {
                String u = userTextField.getText();
                String[] us = null;
                if(u.indexOf(',') != -1) {
                    us = u.split(",");
                }
                else {
                    us = new String[] { u };
                }
                int resultSize = getSearchSize();

                for(int i=0; i<us.length; i++) {
                    if(us[i] != null) {
                        String usi = us[i].trim();
                        if(usi.length() > 0) {
                            feed = USER_URL_PREFIX+usi+USER_URL_POSTFIX;
                            tool.log("Reading YouTube user's '"+usi+"' video feed.");
                            for(int r=0; r<resultSize; r+=50) {
                                YouTubeQuery query = new YouTubeQuery(new URL(feed));
                                if(resultSize>50) query.setStartIndex(r+1);
                                query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                                videoFeed = service.getFeed(query, VideoFeed.class);
                                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                            }
                        }
                    }
                }
            }
            
            // ***** Extract with URL *****
            else if(urlFeed.equals(component)) {
                feed = urlTextField.getText();
                counter+=processVideoFeedURL(admin, topicMap, service, tool, feed);
            }

            
            // ***** Extract with context *****
            else if(contextPanel.equals(component)) {
                Iterator os = context.getContextObjects();
                while(os.hasNext() && !tool.forceStop()) {
                    try {
                        Object o = os.next();
                        if(o instanceof Topic) {
                            Topic t = (Topic) o;
                            if(t != null && !t.isRemoved()) {
                                Locator l = null;
                                String sx = null;
                                
                                if(SLCheckBox.isSelected()) {
                                    l = t.getSubjectLocator();
                                    if(l != null) {
                                        sx = l.toExternalForm();
                                        if(sx.startsWith(YOUTUBE_BASE_URL)) {
                                            feedUrl = new URL(sx);
                                            int resultSize = getSearchSize();
                                            tool.log("Reading YouTube video feed from subject locator '"+feedUrl.toExternalForm()+"'.");
                                            for(int r=0; r<resultSize; r+=50) {
                                                YouTubeQuery query = new YouTubeQuery(feedUrl);
                                                if(resultSize>50) query.setStartIndex(r+1);
                                                query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                                                videoFeed = service.getFeed(query, VideoFeed.class);
                                                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                                            }
                                        }
                                    }
                                }
                                if(SICheckBox.isSelected()) {
                                    for(Iterator iter=t.getSubjectIdentifiers().iterator(); iter.hasNext(); ) {
                                        l = (Locator) iter.next();
                                        sx = l.toExternalForm();
                                        if(sx.startsWith(YOUTUBE_BASE_URL)) {
                                            feedUrl = new URL(sx);
                                            int resultSize = getSearchSize();
                                            tool.log("Reading YouTube video feed from subject identifier '"+feedUrl.toExternalForm()+"'.");
                                            for(int r=0; r<resultSize; r+=50) {
                                                YouTubeQuery query = new YouTubeQuery(feedUrl);
                                                if(resultSize>50) query.setStartIndex(r+1);
                                                query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                                                videoFeed = service.getFeed(query, VideoFeed.class);
                                                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                                            }
                                        }
                                    }
                                }
                                if(typeCheckBox.isSelected()) {
                                    System.out.println("typeCheckBox is selected!");
                                    int resultSize = getSearchSize();
                                    Topic userType = tool.getTopicForPersonType(topicMap);
                                    Topic categoryType = tool.getTopicForCategoryType(topicMap);
                                    Topic keywordType = tool.getTopicForKeywordType(topicMap);
                                    Topic videoType = tool.getTopicForVideoFeedType(topicMap);

                                    if(userType != null && t.isOfType(userType)) {
                                        String username = t.getBaseName();
                                        sx = USER_URL_PREFIX + username + USER_URL_POSTFIX;
                                        feedUrl = new URL(sx);
                                        tool.log("Reading user's '"+username+"' videos.");
                                        for(int r=0; r<resultSize; r+=50) {
                                            YouTubeQuery query = new YouTubeQuery(feedUrl);
                                            if(resultSize>50) query.setStartIndex(r+1);
                                            query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                                            videoFeed = service.getFeed(query, VideoFeed.class);
                                            counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                                        }
                                    }
                                    else if(categoryType != null && t.isOfType(categoryType)) {
                                        System.out.println("   Yes, category found");
                                        String categoryName = t.getDisplayName(tool.LANG);
                                        if(categoryName != null && categoryName.length() > 0) {
                                            tool.log("Reading category '"+categoryName+"' related videos.");
                                            for(int r=0; r<resultSize; r+=50) {
                                                YouTubeQuery query = new YouTubeQuery(new URL(YOUTUBE_VIDEO_URL));
                                                Query.CategoryFilter categoryFilter = new Query.CategoryFilter();
                                                categoryFilter.addCategory(new Category(YouTubeNamespace.CATEGORY_SCHEME, categoryName));
                                                query.addCategoryFilter(categoryFilter);
                                                if(resultSize>50) query.setStartIndex(r+1);
                                                query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                                                videoFeed = service.query(query, VideoFeed.class);
                                                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                                            }
                                        }
                                    }
                                    else if(keywordType != null && t.isOfType(keywordType)) {
                                        String keywordName = t.getDisplayName(tool.LANG);
                                        if(keywordName != null && keywordName.length() > 0) {
                                            tool.log("Reading keyword '"+keywordName+"' related videos.");
                                            for(int r=0; r<resultSize; r+=50) {
                                                YouTubeQuery query = new YouTubeQuery(new URL(YOUTUBE_VIDEO_URL));
                                                Query.CategoryFilter categoryFilter = new Query.CategoryFilter();
                                                categoryFilter.addCategory(new Category(YouTubeNamespace.KEYWORD_SCHEME, keywordName));
                                                query.addCategoryFilter(categoryFilter);
                                                if(resultSize>50) query.setStartIndex(r+1);
                                                query.setMaxResults(resultSize > 50 ? 50 : resultSize);
                                                videoFeed = service.query(query, VideoFeed.class);

                                                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                                            }
                                        }
                                    }
                                    else if(videoType != null && t.isOfType(videoType)) {
                                        try {
                                            Collection<Locator> sis = t.getSubjectIdentifiers();
                                            String videoEntryUrl = null;
                                            for(Locator si : sis) {
                                                if(si != null) {
                                                    if(si.toExternalForm().startsWith(YOUTUBE_VIDEO_URL)) {
                                                        videoEntryUrl = si.toExternalForm();
                                                        break;
                                                    }
                                                }
                                            }
                                            if(videoEntryUrl != null) {
                                                VideoEntry videoEntry = service.getEntry(new URL(videoEntryUrl), VideoEntry.class);
                                                String relatedFeedUrl = videoEntry.getRelatedVideosLink().getHref();
                                                videoFeed = service.getFeed(new URL(relatedFeedUrl), VideoFeed.class);
                                                counter += tool.extract(videoFeed, feedUrl, admin, topicMap);
                                            }
                                            else {
                                                tool.log("Extractor couldn't find proper video entry url.");
                                            }
                                        }
                                        catch(Exception e) {
                                            tool.log(e);
                                        }
                                    }
                                    else{
                                        Locator sl=t.getSubjectLocator();
                                        if(sl!=null){
                                            Locator si=t.getOneSubjectIdentifier();
                                            String s=sl.toExternalForm();
                                            Matcher m=youtubeUrlPattern.matcher(s);
                                            if(m.matches()){
                                                String videoid=m.group(1);
                                                VideoEntry videoEntry=service.getEntry(new URL(YOUTUBE_VIDEO_URL+"/"+videoid), VideoEntry.class);
                                                Topic newTopic=tool.extract(videoEntry, null , feedUrl, admin, topicMap);
                                                if(si!=null) newTopic.addSubjectIdentifier(si);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    catch(Exception e) {
                        tool.log(e);
                    }
                }
            }
        }
        catch(MalformedURLException me) {
            tool.log("Video feed url '"+feed+"' was invalid!");
            tool.log("Cancelling video feed extraction.");
        }
        catch(Exception e) {
            tool.log(e);
        }
        return counter;
    }

    
    
    
    
    public int getSearchSize() {
        String sizeStr = searchSizeComboBox.getSelectedItem().toString();
        int size = 25;
        try {
            size = Integer.parseInt(sizeStr);
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("Using default search size "+size);
        }
        return size;
    }
    
    
    
    public YouTubeQuery.Time getFeedTime() {
        String time = defaultTimeComboBox.getSelectedItem().toString();

        if("Today".equals(time)) return YouTubeQuery.Time.TODAY;
        else if("This month".equals(time)) return YouTubeQuery.Time.THIS_MONTH;
        else if("This week".equals(time)) return YouTubeQuery.Time.THIS_WEEK;
        else return YouTubeQuery.Time.ALL_TIME;
    }
    
    
    
    public boolean isAccepted() {
        return accepted;
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

        tabPanel = new javax.swing.JPanel();
        tabbedPane = new SimpleTabbedPane();
        defaultFeeds = new javax.swing.JPanel();
        defaultDescription = new SimpleLabel();
        feedComboBox = new SimpleComboBox();
        defaultTimePanel = new javax.swing.JPanel();
        defaultTimeLabel = new SimpleLabel();
        defaultTimeComboBox = new SimpleComboBox();
        contextPanel = new javax.swing.JPanel();
        contextLabel = new SimpleLabel();
        SLCheckBox = new SimpleCheckBox();
        SICheckBox = new SimpleCheckBox();
        typeCheckBox = new SimpleCheckBox();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new SimpleLabel();
        searchTextField = new SimpleField();
        searchSizePanel = new javax.swing.JPanel();
        userFeed = new javax.swing.JPanel();
        userDescription = new SimpleLabel();
        userTextField = new SimpleField();
        urlFeed = new javax.swing.JPanel();
        jLabel1 = new SimpleLabel();
        urlTextField = new SimpleField();
        buttonPanel = new javax.swing.JPanel();
        searchSizePanel1 = new javax.swing.JPanel();
        searchSizeLabel = new SimpleLabel();
        searchSizeComboBox = new SimpleComboBox();
        fillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        tabPanel.setLayout(new java.awt.GridBagLayout());

        defaultFeeds.setLayout(new java.awt.GridBagLayout());

        defaultDescription.setText("Select here the default video feed Wandora should extract");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        defaultFeeds.add(defaultDescription, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        defaultFeeds.add(feedComboBox, gridBagConstraints);

        defaultTimePanel.setLayout(new java.awt.GridBagLayout());

        defaultTimeLabel.setText("Time period to limit standard feed results to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        defaultTimePanel.add(defaultTimeLabel, gridBagConstraints);

        defaultTimeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All time", "This month", "This week", "Today" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        defaultTimePanel.add(defaultTimeComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        defaultFeeds.add(defaultTimePanel, gridBagConstraints);

        tabbedPane.addTab("Default", defaultFeeds);

        contextPanel.setLayout(new java.awt.GridBagLayout());

        contextLabel.setText("Use topic context to get the videos.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        contextPanel.add(contextLabel, gridBagConstraints);

        SLCheckBox.setText("Extract using subject locators");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        contextPanel.add(SLCheckBox, gridBagConstraints);

        SICheckBox.setText("Extract using subject identifiers");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        contextPanel.add(SICheckBox, gridBagConstraints);

        typeCheckBox.setSelected(true);
        typeCheckBox.setText("Recognize topic type and extract using type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        contextPanel.add(typeCheckBox, gridBagConstraints);

        tabbedPane.addTab("Context", contextPanel);

        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("Fetch videos with YouTube query. Query is based on given search word:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        searchPanel.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        searchPanel.add(searchTextField, gridBagConstraints);

        searchSizePanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        searchPanel.add(searchSizePanel, gridBagConstraints);

        tabbedPane.addTab("Search", searchPanel);

        userFeed.setLayout(new java.awt.GridBagLayout());

        userDescription.setText("Extract video feed data using YouTube username.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        userFeed.add(userDescription, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        userFeed.add(userTextField, gridBagConstraints);

        tabbedPane.addTab("User", userFeed);

        urlFeed.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("<html>Use given URL address to extract a video feed. You can either use the standard video URLs starting with \"http://www.youtube.com/watch?v=\" or the YouTube API URLs starting with \"http://gdata.youtube.com/feeds/api/\".</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        urlFeed.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        urlFeed.add(urlTextField, gridBagConstraints);

        tabbedPane.addTab("URL", urlFeed);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tabPanel.add(tabbedPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tabPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        searchSizePanel1.setLayout(new java.awt.GridBagLayout());

        searchSizeLabel.setText("Max of entries to return");
        searchSizePanel1.add(searchSizeLabel, new java.awt.GridBagConstraints());

        searchSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "10", "25", "50", "100", "250", "500" }));
        searchSizeComboBox.setPreferredSize(new java.awt.Dimension(50, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        searchSizePanel1.add(searchSizeComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 5, 10);
        buttonPanel.add(searchSizePanel1, gridBagConstraints);

        javax.swing.GroupLayout fillerPanelLayout = new javax.swing.GroupLayout(fillerPanel);
        fillerPanel.setLayout(fillerPanelLayout);
        fillerPanelLayout.setHorizontalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        fillerPanelLayout.setVerticalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    accepted = false;
    this.setVisible(false);
}//GEN-LAST:event_cancelButtonMouseReleased

private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
    accepted = true;
    this.setVisible(false);
}//GEN-LAST:event_okButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox SICheckBox;
    private javax.swing.JCheckBox SLCheckBox;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel contextLabel;
    private javax.swing.JPanel contextPanel;
    private javax.swing.JLabel defaultDescription;
    private javax.swing.JPanel defaultFeeds;
    private javax.swing.JComboBox defaultTimeComboBox;
    private javax.swing.JLabel defaultTimeLabel;
    private javax.swing.JPanel defaultTimePanel;
    private javax.swing.JComboBox feedComboBox;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JComboBox searchSizeComboBox;
    private javax.swing.JLabel searchSizeLabel;
    private javax.swing.JPanel searchSizePanel;
    private javax.swing.JPanel searchSizePanel1;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JPanel tabPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox typeCheckBox;
    private javax.swing.JPanel urlFeed;
    private javax.swing.JTextField urlTextField;
    private javax.swing.JLabel userDescription;
    private javax.swing.JPanel userFeed;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

}
