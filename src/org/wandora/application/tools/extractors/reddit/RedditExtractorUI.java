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
 */
package org.wandora.application.tools.extractors.reddit;

import com.mashape.unirest.http.*;
import com.mashape.unirest.http.async.Callback;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.text.DateFormat;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.*;
import static org.wandora.application.tools.extractors.reddit.AbstractRedditExtractor.statusToPhrase;

import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;

import org.wandora.dep.json.JSONArray;
import org.wandora.dep.json.JSONObject;
import org.wandora.dep.json.JSONException;

/**
 *
 * @author Eero
 */
public class RedditExtractorUI extends javax.swing.JPanel {

    private Wandora wandora = null;
    private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance();
    private String apiRoot = AbstractRedditExtractor.apiRoot;
    private DefaultListModel linkModel;
    private JSONArray threadResults = new JSONArray();
    private JSONArray subredditResults = new JSONArray();

    /**
     * Creates new form RedditExtractorUI
     */
    public RedditExtractorUI() {
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
        dialog.setSize(800, 500);
        dialog.add(this);
        threadSearchSubmit.setText("Search");
        threadSearchSubmit.setEnabled(true);
        dialog.setTitle("Reddit API extractor");
        UIBox.centerWindow(dialog, w);

        dialog.setVisible(true);
    }

    public WandoraTool[] getExtractors(RedditExtractor tool) throws TopicMapException {

        WandoraTool wt;
        ArrayList<WandoraTool> wts = new ArrayList();

        String id = null;
        String query = "";
        String extractUrl = null;

        Component selectedTab = redditTabs.getSelectedComponent();

        RedditThingExtractor ex = new RedditThingExtractor();

        HashMap<String, Boolean> crawling = new HashMap<String, Boolean>();
        crawling.put("more", crawlToggle.isSelected());
        crawling.put("linkSubreddit", crawlLinkSR.isSelected());
        crawling.put("linkComment", crawlLinkComment.isSelected());
        crawling.put("linkUser", crawlLinkUser.isSelected());
        crawling.put("commentLink", crawlCommentLink.isSelected());
        crawling.put("commentUser", crawlCommentUser.isSelected());
        crawling.put("userLink", crawlUserLink.isSelected());
        crawling.put("userComment", crawlUserComment.isSelected());
        crawling.put("subredditLink", crawlSRLink.isSelected());

        ex.setCrawling(crawling);

        try {
            if (selectedTab.equals(commentSearchTab)) {
                int selectedIndex = threadResList.getSelectedIndex();
                JSONObject selected = threadResults.getJSONObject(selectedIndex);
                id = selected.getJSONObject("data").getString("id");
                extractUrl = apiRoot + "comments/" + id;
                ex.setForceUrls(new String[]{extractUrl});

            } else if (selectedTab.equals(accountSearchTab)) {
                id = accountSearchField.getText();
                extractUrl = apiRoot + "user/" + id + "/about.json";
                ex.setForceUrls(new String[]{extractUrl});

            } else if (selectedTab.equals(subredditSearchTab)) {
                int selectedIndex = subredditResList.getSelectedIndex();
                JSONObject selected = subredditResults.getJSONObject(selectedIndex);
                id = selected.getJSONObject("data").getString("display_name");
                extractUrl = apiRoot +"r/"+ id + "/about.json";
                ex.setForceUrls(new String[]{extractUrl});

            } else if (selectedTab.equals(linkSearchTab)) {

                id = linkField.getText();
                ArrayList<String> urls = new ArrayList<String>();

                for (int i = 0; i < linkModel.getSize(); i++) {
                    urls.add(apiRoot + "api/info?url=" + linkModel.get(i));
                }

                String[] urlArray = new String[urls.size()];
                urlArray = urls.toArray(urlArray);

                ex.setForceUrls(urlArray);
            }

            System.out.println(extractUrl);

            wts.add(ex);
        } catch (JSONException jse) {

            jse.printStackTrace();
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

    private void threadPopulationCallback(HttpResponse<JsonNode> response) {
        DefaultListModel model = new DefaultListModel();;
        try {
            
            JSONObject resJson = response.getBody()
                    .getObject();
                    
            if(resJson.has("error")){
              Object error = resJson.get("error");
              throw new JSONException("API error: " + statusToPhrase((int)error));
            }
            
            threadResults = resJson
                    .getJSONObject("data")
                    .getJSONArray("children");;
            JSONObject r;

            model = new DefaultListModel();
            for (int i = 0; i < threadResults.length(); i++) {
                r = threadResults.getJSONObject(i).getJSONObject("data");

                StringBuilder titleBuilder = new StringBuilder();
                titleBuilder
                        .append("[r/").append(r.getString("subreddit"))
                        .append("] ").append(r.getString("title"))
                        .append(" - ").append(r.getString("score"));
                model.add(i, titleBuilder.toString());
            }
            //threadResList.setModel(model);

        } catch (Exception e) {
            model.add(0, e.getMessage());

        } finally {
            threadResList.setModel(model);
            threadSearchSubmit.setText("Search");
            threadSearchSubmit.setEnabled(true);
        }
    }

    private void populateThreadSearch() {

        threadSearchSubmit.setText("Searching...");
        threadSearchSubmit.setEnabled(false);

        String q = threadSearchField.getText();

        Callback<JsonNode> callback = new Callback<JsonNode>() {
            @Override
            public void failed(Exception e) {
            }

            @Override
            public void cancelled() {
            }

            @Override
            public void completed(HttpResponse<JsonNode> response) {
                threadPopulationCallback(response);
            }
        };

        AbstractRedditExtractor.getSubmissions(q, callback);

    }

    private void subredditPopulationCallback(HttpResponse<JsonNode> response) {
        DefaultListModel model = new DefaultListModel();;
        try {
            JSONObject resJson = response.getBody()
                    .getObject();
                    
            if(resJson.has("error")){
              Object error = resJson.get("error");
              throw new JSONException("API error: " + statusToPhrase((int)error));
            }
            
            threadResults = resJson
                    .getJSONObject("data")
                    .getJSONArray("children");;
            JSONObject r;

            model = new DefaultListModel();
            for (int i = 0; i < threadResults.length(); i++) {

                r = threadResults.getJSONObject(i).getJSONObject("data");
               
                StringBuilder titleBuilder = new StringBuilder();
                titleBuilder
                        .append("r/").append(r.getString("display_name"))
                        .append(": ").append(r.getString("title"));
                model.add(i, titleBuilder.toString());
            }

        } catch (Exception e) {
            model.add(0, e.getMessage());

        } finally {
            subredditResList.setModel(model);
            subredditSearchSubmit.setText("Search");
            subredditSearchSubmit.setEnabled(true);
        }
    }

    private void populateSubredditSearch() {

        subredditSearchSubmit.setText("Searching...");
        subredditSearchSubmit.setEnabled(false);

        String q = subredditSearchField.getText();

        Callback<JsonNode> callback = new Callback<JsonNode>() {
            @Override
            public void failed(Exception e) {
            }

            @Override
            public void cancelled() {
            }

            @Override
            public void completed(HttpResponse<JsonNode> response) {
                subredditPopulationCallback(response);
            }
        };

        AbstractRedditExtractor.getSubreddits(q, callback);

    }

    private void focusThread() {
        int i = threadResList.getSelectedIndex();
        try {
            JSONObject r = threadResults.getJSONObject(i).getJSONObject("data");
            StringBuilder sb = new StringBuilder();
            Date d = new Date(1000 * r.getLong("created"));
            String ds = dateFormat.format(d);
            sb.append("<html><body style='width=180'>posted ").append(ds).append("<br/>")
                    .append("score: ").append(r.getInt("score")).append(" (")
                    .append(r.getInt("ups")).append("|").append(r.getInt("downs"))
                    .append(")<br/>").append(r.getInt("num_comments")).append(" comments")
                    .append("<body></html>");

            threadSearchDetails.setText(sb.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void focusSubreddit() {
        int i = subredditResList.getSelectedIndex();
        try {
            JSONObject r = subredditResults.getJSONObject(i).getJSONObject("data");
            subredditTitleLabel.setText(r.getInt("subscribers") + " subscribers");
            subredditDetailTextArea.setText(r.getString("public_description"));
            subredditDetailTextArea.setLineWrap(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void addContextSLs() {
        Iterator iter = context.getContextObjects();
        Object o;
        Topic t;
        Locator locator;
        while (iter.hasNext()) {
            try {
                o = iter.next();
                if (o == null) {
                    continue;
                }
                if (o instanceof Topic) {
                    t = (Topic) o;
                    if (!t.isRemoved()) {
                        locator = t.getSubjectLocator();
                        if (locator != null) {
                            String locatorStr = locator.toExternalForm();
                            linkModel.addElement(locatorStr);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        redditTabs = new SimpleTabbedPane();
        commentSearchTab = new javax.swing.JPanel();
        threadSearchField = new SimpleField();
        threadSearchSubmit = new SimpleButton();
        threadResScrollPane = new javax.swing.JScrollPane();
        threadResList = new SimpleList();
        threadSearchDetails = new javax.swing.JLabel();
        subredditSearchTab = new javax.swing.JPanel();
        subredditSearchField = new javax.swing.JTextField();
        subredditSearchSubmit = new SimpleButton();
        subredditResScrollPane = new javax.swing.JScrollPane();
        subredditResList = new SimpleList();
        subredditDetailTextArea = new javax.swing.JTextArea();
        subredditTitleLabel = new javax.swing.JLabel();
        linkSearchTab = new javax.swing.JPanel();
        linkField = new javax.swing.JTextField();
        linkLabel = new SimpleLabel();
        linkScrollpane = new javax.swing.JScrollPane();
        linkSearchList = new javax.swing.JList();
        linkAddUrlButton = new SimpleButton();
        linkAddSLsButton = new SimpleButton();
        linkClearButton = new SimpleButton();
        accountSearchTab = new javax.swing.JPanel();
        accountSearchField = new javax.swing.JTextField();
        jLabel3 = new SimpleLabel();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();
        crawlOptions = new javax.swing.JPanel();
        crawlLinkSR = new javax.swing.JCheckBox();
        crawlLinkUser = new javax.swing.JCheckBox();
        crawlLinkComment = new javax.swing.JCheckBox();
        crawlUserComment = new javax.swing.JCheckBox();
        crawlUserLink = new javax.swing.JCheckBox();
        crawlSRLink = new javax.swing.JCheckBox();
        crawlCommentUser = new javax.swing.JCheckBox();
        crawlCommentLink = new javax.swing.JCheckBox();
        crawlToggle = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        commentSearchTab.setLayout(new java.awt.GridBagLayout());

        threadSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                threadSearchFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 4, 0);
        commentSearchTab.add(threadSearchField, gridBagConstraints);

        threadSearchSubmit.setText("Search");
        threadSearchSubmit.setMaximumSize(new java.awt.Dimension(180, 23));
        threadSearchSubmit.setMinimumSize(new java.awt.Dimension(180, 23));
        threadSearchSubmit.setPreferredSize(new java.awt.Dimension(180, 23));
        threadSearchSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                threadSearchSubmitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        commentSearchTab.add(threadSearchSubmit, gridBagConstraints);

        threadResList.setMinimumSize(new java.awt.Dimension(200, 200));
        threadResList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                threadResListValueChanged(evt);
            }
        });
        threadResScrollPane.setViewportView(threadResList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        commentSearchTab.add(threadResScrollPane, gridBagConstraints);

        threadSearchDetails.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        threadSearchDetails.setBorder(javax.swing.BorderFactory.createTitledBorder("Details"));
        threadSearchDetails.setMaximumSize(new java.awt.Dimension(180, 23));
        threadSearchDetails.setMinimumSize(new java.awt.Dimension(180, 23));
        threadSearchDetails.setPreferredSize(new java.awt.Dimension(180, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        commentSearchTab.add(threadSearchDetails, gridBagConstraints);

        redditTabs.addTab("Submission Search", commentSearchTab);

        subredditSearchTab.setLayout(new java.awt.GridBagLayout());

        subredditSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                subredditSearchFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 4, 0);
        subredditSearchTab.add(subredditSearchField, gridBagConstraints);

        subredditSearchSubmit.setText("Search");
        subredditSearchSubmit.setMaximumSize(new java.awt.Dimension(180, 23));
        subredditSearchSubmit.setMinimumSize(new java.awt.Dimension(180, 23));
        subredditSearchSubmit.setPreferredSize(new java.awt.Dimension(180, 23));
        subredditSearchSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subredditSearchSubmitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 3, 3);
        subredditSearchTab.add(subredditSearchSubmit, gridBagConstraints);

        subredditResList.setMinimumSize(new java.awt.Dimension(200, 200));
        subredditResList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                subredditResListValueChanged(evt);
            }
        });
        subredditResScrollPane.setViewportView(subredditResList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        subredditSearchTab.add(subredditResScrollPane, gridBagConstraints);

        subredditDetailTextArea.setEditable(false);
        subredditDetailTextArea.setColumns(2);
        subredditDetailTextArea.setRows(5);
        subredditDetailTextArea.setTabSize(4);
        subredditDetailTextArea.setWrapStyleWord(true);
        subredditDetailTextArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        subredditDetailTextArea.setMaximumSize(new java.awt.Dimension(180, 180));
        subredditDetailTextArea.setMinimumSize(new java.awt.Dimension(180, 180));
        subredditDetailTextArea.setPreferredSize(new java.awt.Dimension(180, 180));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        subredditSearchTab.add(subredditDetailTextArea, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        subredditSearchTab.add(subredditTitleLabel, gridBagConstraints);

        redditTabs.addTab("Subreddit search", subredditSearchTab);

        linkSearchTab.setLayout(new java.awt.GridBagLayout());

        linkField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                linkFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 4, 4);
        linkSearchTab.add(linkField, gridBagConstraints);

        linkLabel.setText("Link URL:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        linkSearchTab.add(linkLabel, gridBagConstraints);

        linkModel = new DefaultListModel();
        linkSearchList.setModel(linkModel);
        linkScrollpane.setViewportView(linkSearchList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        linkSearchTab.add(linkScrollpane, gridBagConstraints);

        linkAddUrlButton.setText("Add URL");
        linkAddUrlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkAddUrlButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        linkSearchTab.add(linkAddUrlButton, gridBagConstraints);

        linkAddSLsButton.setText("Add Context SLs");
        linkAddSLsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkAddSLsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        linkSearchTab.add(linkAddSLsButton, gridBagConstraints);

        linkClearButton.setText("Clear All");
        linkClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkClearButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        linkSearchTab.add(linkClearButton, gridBagConstraints);

        redditTabs.addTab("Link search", linkSearchTab);

        accountSearchTab.setLayout(new java.awt.GridBagLayout());

        accountSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                accountSearchFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 4, 4);
        accountSearchTab.add(accountSearchField, gridBagConstraints);

        jLabel3.setText("Account name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 5, 8, 0);
        accountSearchTab.add(jLabel3, gridBagConstraints);

        redditTabs.addTab("Account search", accountSearchTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.3;
        add(redditTabs, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(buttonPanel, gridBagConstraints);

        crawlOptions.setBorder(javax.swing.BorderFactory.createTitledBorder("Crawling options"));
        crawlOptions.setLayout(new java.awt.GridBagLayout());

        crawlLinkSR.setText("Crawl Link Subreddit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlLinkSR, gridBagConstraints);

        crawlLinkUser.setText("Crawl Link Account");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlLinkUser, gridBagConstraints);

        crawlLinkComment.setText("Crawl Link Comments");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlLinkComment, gridBagConstraints);

        crawlUserComment.setText("Crawl Account Comments");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlUserComment, gridBagConstraints);

        crawlUserLink.setText("Crawl Account Links");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlUserLink, gridBagConstraints);

        crawlSRLink.setText("Crawl Subreddit Links");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlSRLink, gridBagConstraints);

        crawlCommentUser.setText("Crawl Comment Account");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlCommentUser, gridBagConstraints);

        crawlCommentLink.setText("Crawl Comment Link");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlCommentLink, gridBagConstraints);

        crawlToggle.setText("Crawl Comment Tree");
        crawlToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crawlToggleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        crawlOptions.add(crawlToggle, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        add(crawlOptions, gridBagConstraints);
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

    private void threadSearchSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_threadSearchSubmitActionPerformed
        this.populateThreadSearch();
    }//GEN-LAST:event_threadSearchSubmitActionPerformed

    private void threadResListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_threadResListValueChanged
        this.focusThread();
    }//GEN-LAST:event_threadResListValueChanged

    private void threadSearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_threadSearchFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.populateThreadSearch();
        }
    }//GEN-LAST:event_threadSearchFieldKeyPressed

    private void accountSearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_accountSearchFieldKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_accountSearchFieldKeyPressed

    private void crawlToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crawlToggleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_crawlToggleActionPerformed

    private void subredditSearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_subredditSearchFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.populateSubredditSearch();
        }
    }//GEN-LAST:event_subredditSearchFieldKeyPressed

    private void linkFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_linkFieldKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_linkFieldKeyPressed

    private void linkAddUrlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkAddUrlButtonActionPerformed
        String u = linkField.getText();
        linkModel.addElement(u);
    }//GEN-LAST:event_linkAddUrlButtonActionPerformed

    private void linkClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkClearButtonActionPerformed
        linkModel.clear();
    }//GEN-LAST:event_linkClearButtonActionPerformed

    private void linkAddSLsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkAddSLsButtonActionPerformed
        addContextSLs();
    }//GEN-LAST:event_linkAddSLsButtonActionPerformed

    private void subredditSearchSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subredditSearchSubmitActionPerformed
        this.populateSubredditSearch();
    }//GEN-LAST:event_subredditSearchSubmitActionPerformed

    private void subredditResListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_subredditResListValueChanged
        this.focusSubreddit();
    }//GEN-LAST:event_subredditResListValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField accountSearchField;
    private javax.swing.JPanel accountSearchTab;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel commentSearchTab;
    private javax.swing.JCheckBox crawlCommentLink;
    private javax.swing.JCheckBox crawlCommentUser;
    private javax.swing.JCheckBox crawlLinkComment;
    private javax.swing.JCheckBox crawlLinkSR;
    private javax.swing.JCheckBox crawlLinkUser;
    private javax.swing.JPanel crawlOptions;
    private javax.swing.JCheckBox crawlSRLink;
    private javax.swing.JCheckBox crawlToggle;
    private javax.swing.JCheckBox crawlUserComment;
    private javax.swing.JCheckBox crawlUserLink;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton linkAddSLsButton;
    private javax.swing.JButton linkAddUrlButton;
    private javax.swing.JButton linkClearButton;
    private javax.swing.JTextField linkField;
    private javax.swing.JLabel linkLabel;
    private javax.swing.JScrollPane linkScrollpane;
    private javax.swing.JList linkSearchList;
    private javax.swing.JPanel linkSearchTab;
    private javax.swing.JButton okButton;
    private javax.swing.JTabbedPane redditTabs;
    private javax.swing.JTextArea subredditDetailTextArea;
    private javax.swing.JList subredditResList;
    private javax.swing.JScrollPane subredditResScrollPane;
    private javax.swing.JTextField subredditSearchField;
    private javax.swing.JButton subredditSearchSubmit;
    private javax.swing.JPanel subredditSearchTab;
    private javax.swing.JLabel subredditTitleLabel;
    private javax.swing.JList threadResList;
    private javax.swing.JScrollPane threadResScrollPane;
    private javax.swing.JLabel threadSearchDetails;
    private javax.swing.JTextField threadSearchField;
    private javax.swing.JButton threadSearchSubmit;
    // End of variables declaration//GEN-END:variables
}
