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
 * NYTExtractorUI.java
 *
 * Created on Apr 14, 2012, 9:43:48 PM
 */
package org.wandora.application.tools.extractors.nyt;

import java.awt.Component;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.ListModel;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleList;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author
 * akivela
 */
public class NYTExtractorUI extends javax.swing.JPanel {

    private Wandora wandora = null;
    private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;
    private static final String NYT_API_BASE = "http://api.nytimes.com/svc/search/v1/";
    private static final String NYT_API_EVENT_BASE = "http://api.nytimes.com/svc/events/v2/listings.json";

    /**
     * Creates
     * new
     * form
     * NYTExtractorUI
     */
    public NYTExtractorUI() {
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
        accepted = false;
        dialog = new JDialog(w, true);
        dialog.setSize(550, 500);
        dialog.add(this);
        dialog.setTitle("New York Times API extractor");
        UIBox.centerWindow(dialog, w);
        if(eventapikey != null || articleapikey != null){
            forgetButton.setEnabled(true);
        } else {
            forgetButton.setEnabled(false);
        }
        dialog.setVisible(true);
    }

    public WandoraTool[] getExtractors(NYTExtractor tool) throws TopicMapException {
        Component component = nytTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        ArrayList<WandoraTool> wts = new ArrayList();

        // ***** SEARCH ARTICLES *****
        if (articleSearchPanel.equals(component)) {
            String query = searchQueryTextField.getText();
            String key = solveAPIKey();
            
            if(key == null){
                return null;
            }
            
            String extractUrl = NYT_API_BASE + "article?query=" + urlEncode(query) + "&api-key=" + key;

            String facets = facetsTextField.getText().trim();
            if (facets != null && facets.length() > 0) {
                extractUrl += "&facets=" + urlEncode(facets);
            }

            int[] fieldIndexes = fieldsList.getSelectedIndices();
            ListModel listModel = fieldsList.getModel();
            StringBuilder fieldValues = new StringBuilder("");
            for (int i = 0; i < fieldIndexes.length; i++) {
                if (i > 0) {
                    fieldValues.append(",");
                }
                fieldValues.append(listModel.getElementAt(fieldIndexes[i]));
            }
            if (fieldValues.length() > 0) {
                extractUrl += "&fields=" + urlEncode("url," + fieldValues.toString());
            }

            String beginDate = beginDateTextField.getText().trim();
            if (beginDate != null && beginDate.length() > 0) {
                extractUrl += "&begin_date=" + urlEncode(beginDate);
            }
            String endDate = endDateTextField.getText().trim();
            if (endDate != null && endDate.length() > 0) {
                extractUrl += "&end_date=" + urlEncode(endDate);
            }
            String offset = offsetTextField.getText().trim();
            if (offset == null || offset.length() == 0) {
                offset = "0";
            }
            extractUrl += "&offset=" + urlEncode(offset);

            String rank = rankComboBox.getSelectedItem().toString();
            if (rank != null && rank.length() > 0) {
                extractUrl += "&rank=" + urlEncode(rank);
            }

            System.out.println("URL: " + extractUrl);

            NYTArticleSearchExtractor ex = new NYTArticleSearchExtractor();
            ex.setForceUrls(new String[]{extractUrl});
            wt = ex;
            wts.add(wt);
        } // ***** SEARCH EVENTS *****
        else if (eventSearchPanel.equals(component)) {
            String eventKey = solveAPIKey();
            
            if(eventKey == null){
                throw new TopicMapException("Invalid key.");
            }
            
            String extractUrl = NYT_API_EVENT_BASE + "?";

            String query = urlEncode(eventQueryTextField.getText());

            if (!query.isEmpty()) {
                extractUrl += "&query=" + query;
            }

            Component subComponent = eventLocationTabs.getSelectedComponent();

            if (eventCircleTab.equals(subComponent)) {
                if(circlCtrLatTextField.getText().matches("^-?\\d*[.,]\\d*") && circlCtrLongTextField.getText().matches("^-?\\d*[.,]\\d*")){
                    extractUrl += "&ll=" + urlEncode(circlCtrLatTextField.getText() + "," + circlCtrLongTextField.getText());
                } else throw new TopicMapException("Invalid circle coordinates.");
                if(circlRadTextField.getText().matches("\\d*")){
                    extractUrl += "&radius=" + urlEncode(circlRadTextField.getText());
                } else throw new TopicMapException("Invalid radius.");
                
            } else if (eventBoxTab.equals(subComponent)) {
                if(boxNELatTextField.getText().matches("^-?\\d*[,.]\\d*") && boxNELongTextField.getText().matches("^-?\\d*[,.]\\d*")) {
                    extractUrl += "&ne=" + urlEncode(boxNELatTextField.getText() + "," + boxNELongTextField.getText());
                } else throw new TopicMapException("Invalid box coordinates!");
                
                if(boxSWLatTextField.getText().matches("^-?\\d*[,.]\\d*") && boxSWLongTextField.getText().matches("^-?\\d*[,.]\\d*")) {
                    extractUrl += "&ne=" + urlEncode(boxSWLatTextField.getText() + "," + boxSWLongTextField.getText());
                } else throw new TopicMapException("Invalid box coordinates!");
                
            } else if (eventFilterTab.equals(subComponent)) {

                String filCat = evtFilterCatTextField.getText();
                String filSubCat = evtFilterSubCatTextField.getText();
                String filBor = evtFilterBoroughTextField.getText();
                String filNghbrhd = evtFilterNeighborhoodTextField.getText();


                String filter = "&filters=";

                if (!filCat.isEmpty()) {
                    filter += "category:(" + filCat + "),";
                } 
                if (!filSubCat.isEmpty()) {
                    filter += "subcategory:(" + filSubCat + "),";
                }
                if (!filBor.isEmpty()) {
                    filter += "borough:(" + filBor + "),";
                }
                if (!filNghbrhd.isEmpty()) {
                    filter += "neighborhood:(" + filNghbrhd + "),";
                }
                if (evtFilterTimesPick.isSelected()) {
                    filter += "times_pick:true,";
                }
                if (evtFilterFree.isSelected()) {
                    filter += "free:true,";
                }
                if (evtFilterKidFriendly.isSelected()) {
                    filter += "kid_friendly:true,";
                }
                if (evtFilterLastChance.isSelected()) {
                    filter += "last_chance:true,";
                }
                if (evtFilterFestival.isSelected()) {
                    filter += "festival:true,";
                }
                if (evtFilterLongRunningShow.isSelected()) {
                    filter += "long_running_show:true,";
                }
                if (evtFilterPreviewsAndOpenings.isSelected()) {
                    filter += "previews_and_openings:true,";
                }

                extractUrl += filter;
            }


            String dateRange = (!evtDateStartTextField.getText().isEmpty() && !evtDateEndTextField.getText().isEmpty()) ? urlEncode(evtDateStartTextField.getText() + ":" + evtDateEndTextField.getText()) : null;

            if (dateRange != null) {
                if(dateRange.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d%3A\\d\\d\\d\\d-\\d\\d-\\d\\d")){
                    extractUrl += "&date_range=" + urlEncode(dateRange);
                } else throw new TopicMapException("Invalid date range!");
            }

            String offset = evtOffsetTextField.getText();

            if (!offset.isEmpty()) {
                if(offset.matches("\\d*")){
                    extractUrl += "&offset=" + urlEncode(offset);
                } else throw new TopicMapException("Invalid offset value!");
            }
            
            String limit = evtLimitTextField.getText();
            if(limit.matches("\\d*")){
                extractUrl += "&limit=" + limit;
            } else throw new TopicMapException("Invalid limit value!");;
            
            extractUrl += "&api-key=" + eventKey;

            System.out.println(extractUrl);

            NYTEventSearchExtractor ex = new NYTEventSearchExtractor();
            ex.setForceUrls(new String[]{extractUrl});
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
    // ------------------------------------------------------------ API-KEY ----
    private static String articleapikey = null;
    private static String eventapikey = null;

    public String solveAPIKey(Wandora wandora) {
        return solveAPIKey();
    }
    
    public String solveAPIKey() {
        if (eventSearchPanel.equals(nytTabbedPane.getSelectedComponent())){
            if(eventapikey == null){
                 eventapikey = "";
                 eventapikey = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Please give an api-key for NYT event search. You can register your api-key at http://developer.nytimes.com/docs/reference/keys", eventapikey, "NYT Event api-key", WandoraOptionPane.QUESTION_MESSAGE);
                if(eventapikey != null) eventapikey = eventapikey.trim();
            }
            forgetButton.setEnabled(true);
            return eventapikey;
        } else if (articleSearchPanel.equals(nytTabbedPane.getSelectedComponent())){
            if(articleapikey == null) {
                articleapikey = "";
                articleapikey = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Please give an api-key for NYT article search. You can register your api-key at http://developer.nytimes.com/docs/reference/keys", articleapikey, "NYT Article api-key", WandoraOptionPane.QUESTION_MESSAGE);
                if(articleapikey != null) articleapikey = articleapikey.trim();
                }
            forgetButton.setEnabled(true);
            return articleapikey;
        }
        return null;
    }
    
    public void forgetAuthorization() {
        if (articleSearchPanel.equals(nytTabbedPane.getSelectedComponent())){
            articleapikey = null;
            forgetButton.setEnabled(false);
        }
        else if(eventSearchPanel.equals(nytTabbedPane.getSelectedComponent())) {
            eventapikey = null;
            forgetButton.setEnabled(false);
        }
    }

    // -------------------------------------------------------------------------
    /**
     * This
     * method
     * is
     * called
     * from
     * within
     * the
     * constructor
     * to
     * initialize
     * the
     * form.
     * WARNING:
     * Do
     * NOT
     * modify
     * this
     * code.
     * The
     * content
     * of
     * this
     * method
     * is
     * always
     * regenerated
     * by
     * the
     * Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        facetsLabel = new SimpleLabel();
        facetsTextField = new SimpleField();
        nytTabbedPane = new SimpleTabbedPane();
        articleSearchPanel = new javax.swing.JPanel();
        articleSearchInnerPanel = new javax.swing.JPanel();
        searchLabel = new SimpleLabel();
        searchQueryTextField = new SimpleField();
        optionalArticleSearchFieldsPanel = new javax.swing.JPanel();
        fieldsLabel = new SimpleLabel();
        fieldsListScrollPane = new javax.swing.JScrollPane();
        fieldsList = new SimpleList();
        beginDateLabel = new SimpleLabel();
        beginDateTextField = new SimpleField();
        endDateLabel = new SimpleLabel();
        endDateTextField = new SimpleField();
        offsetLabel = new SimpleLabel();
        offsetTextField = new SimpleField();
        rankLabel = new SimpleLabel();
        rankComboBox = new javax.swing.JComboBox();
        eventSearchPanel = new javax.swing.JPanel();
        eventSearchInnerPanel = new javax.swing.JPanel();
        eventQueryLabel = new SimpleLabel();
        eventQueryTextField = new SimpleField();
        eventLocationTabs = new SimpleTabbedPane();
        eventBoxTab = new javax.swing.JPanel();
        boxLabel = new javax.swing.JLabel();
        boxSWpanel = new javax.swing.JPanel();
        boxSWLatLabel = new SimpleLabel();
        boxSWLongLabel = new SimpleLabel();
        boxSWLatTextField = new SimpleField();
        boxSWLongTextField = new SimpleField();
        boxNEpanel = new javax.swing.JPanel();
        boxNELatLabel = new SimpleLabel();
        boxNELongLabel = new SimpleLabel();
        boxNELatTextField = new SimpleField();
        boxNELongTextField = new SimpleField();
        eventCircleTab = new javax.swing.JPanel();
        circleLabel = new javax.swing.JLabel();
        circlCenterPanel = new javax.swing.JPanel();
        circlCtrLongTextField = new SimpleField();
        circlCtrLatTextField = new SimpleField();
        circlCtrLongLabel = new SimpleLabel();
        circlCtrLatLabel = new SimpleLabel();
        circlRadiusPanel = new javax.swing.JPanel();
        circlRadTextField = new SimpleField();
        eventFilterTab = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        evtFilterCatLabel = new SimpleLabel();
        evtFilterCatTextField = new SimpleField();
        evtFilterSubCatLabel = new SimpleLabel();
        evtFilterSubCatTextField = new SimpleField();
        evtFilterBoroughLabel = new SimpleLabel();
        evtFilterBoroughTextField = new SimpleField();
        evtFilterNeighborhoodLabel = new SimpleLabel();
        evtFilterNeighborhoodTextField = new SimpleField();
        evtFilterTimesPick = new SimpleCheckBox();
        evtFilterFree = new SimpleCheckBox();
        evtFilterKidFriendly = new SimpleCheckBox();
        evtFilterLastChance = new SimpleCheckBox();
        evtFilterFestival = new SimpleCheckBox();
        evtFilterLongRunningShow = new SimpleCheckBox();
        evtFilterPreviewsAndOpenings = new SimpleCheckBox();
        eventLimitPanel = new javax.swing.JPanel();
        eventDateRangePanel = new javax.swing.JPanel();
        evtDateStartLabel = new SimpleLabel();
        evtDateEndLabel = new SimpleLabel();
        evtDateStartTextField = new SimpleField();
        evtDateEndTextField = new SimpleField();
        eventOffsetPanel = new javax.swing.JPanel();
        evtOffset = new SimpleLabel();
        evtOffsetTextField = new SimpleField();
        evtLimitLabel = new SimpleLabel();
        evtLimitTextField = new SimpleField();
        jPanel1 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        forgetButton = new SimpleButton();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        facetsLabel.setText("Facets");
        facetsLabel.setToolTipText("Comma-delimited list of up to 5 facets.");

        setLayout(new java.awt.GridBagLayout());

        nytTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                nytTabbedPaneStateChanged(evt);
            }
        });

        articleSearchPanel.setLayout(new java.awt.GridBagLayout());

        articleSearchInnerPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("Search query");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        articleSearchInnerPanel.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        articleSearchInnerPanel.add(searchQueryTextField, gridBagConstraints);

        optionalArticleSearchFieldsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Optional params"));
        optionalArticleSearchFieldsPanel.setLayout(new java.awt.GridBagLayout());

        fieldsLabel.setText("Fields");
        fieldsLabel.setToolTipText("Comma-delimited list of fields (no limit).");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 4);
        optionalArticleSearchFieldsPanel.add(fieldsLabel, gridBagConstraints);

        fieldsListScrollPane.setMinimumSize(new java.awt.Dimension(23, 150));
        fieldsListScrollPane.setPreferredSize(new java.awt.Dimension(125, 150));

        fieldsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "abstract", "author", "body", "byline", "classifiers_facet", "column_facet", "date", "dbpedia_resource_url", "des_facet", "geo_facet", "lead_paragraph", "material_type_facet", "org_facet", "per_facet", "source_facet", "title" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fieldsListScrollPane.setViewportView(fieldsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        optionalArticleSearchFieldsPanel.add(fieldsListScrollPane, gridBagConstraints);

        beginDateLabel.setText("Begin date (YYYYMMDD)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        optionalArticleSearchFieldsPanel.add(beginDateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        optionalArticleSearchFieldsPanel.add(beginDateTextField, gridBagConstraints);

        endDateLabel.setText("End date (YYYYMMDD)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        optionalArticleSearchFieldsPanel.add(endDateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        optionalArticleSearchFieldsPanel.add(endDateTextField, gridBagConstraints);

        offsetLabel.setText("Offset");
        offsetLabel.setToolTipText("The value of offset corresponds to a set of 10 results.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        optionalArticleSearchFieldsPanel.add(offsetLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        optionalArticleSearchFieldsPanel.add(offsetTextField, gridBagConstraints);

        rankLabel.setText("Rank");
        rankLabel.setToolTipText("Use the rank parameter to set the order of the results. The default rank is newest.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        optionalArticleSearchFieldsPanel.add(rankLabel, gridBagConstraints);

        rankComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "newest", "oldest", "closest" }));
        rankComboBox.setMinimumSize(new java.awt.Dimension(120, 20));
        rankComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        optionalArticleSearchFieldsPanel.add(rankComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        articleSearchInnerPanel.add(optionalArticleSearchFieldsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        articleSearchPanel.add(articleSearchInnerPanel, gridBagConstraints);

        nytTabbedPane.addTab("Article search", articleSearchPanel);

        eventSearchPanel.setLayout(new java.awt.GridBagLayout());

        eventSearchInnerPanel.setLayout(new java.awt.GridBagLayout());

        eventQueryLabel.setText("Search query");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        eventSearchInnerPanel.add(eventQueryLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        eventSearchInnerPanel.add(eventQueryTextField, gridBagConstraints);

        eventBoxTab.setLayout(new java.awt.GridBagLayout());

        boxLabel.setText("<html>Filter events by their location by specifying the Southwestern and Northeastern corners of a box.The default positions are Liberty State Park as the Southwestern corner and Jeromes Park as the Northeastern corner.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 12, 4);
        eventBoxTab.add(boxLabel, gridBagConstraints);

        boxSWpanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Southwest corner"));
        boxSWpanel.setLayout(new java.awt.GridBagLayout());

        boxSWLatLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        boxSWLatLabel.setText("Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        boxSWpanel.add(boxSWLatLabel, gridBagConstraints);

        boxSWLongLabel.setText("Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        boxSWpanel.add(boxSWLongLabel, gridBagConstraints);

        boxSWLatTextField.setText("40.702635");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        boxSWpanel.add(boxSWLatTextField, gridBagConstraints);

        boxSWLongTextField.setText("-74.054976");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        boxSWpanel.add(boxSWLongTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        eventBoxTab.add(boxSWpanel, gridBagConstraints);

        boxNEpanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Northeast corner"));
        boxNEpanel.setLayout(new java.awt.GridBagLayout());

        boxNELatLabel.setText("Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        boxNEpanel.add(boxNELatLabel, gridBagConstraints);

        boxNELongLabel.setText("Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        boxNEpanel.add(boxNELongLabel, gridBagConstraints);

        boxNELatTextField.setText("40.877829");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        boxNEpanel.add(boxNELatTextField, gridBagConstraints);

        boxNELongTextField.setText("-73.895845");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        boxNEpanel.add(boxNELongTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        eventBoxTab.add(boxNEpanel, gridBagConstraints);

        eventLocationTabs.addTab("Box", eventBoxTab);

        eventCircleTab.setLayout(new java.awt.GridBagLayout());

        circleLabel.setText("<html>Filter events by their location by specifying the center coordinates and radius of a circle. By default the center of the circle is the New York Times building on Times Square and the radius of the circle is 1000 meters.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 12, 4);
        eventCircleTab.add(circleLabel, gridBagConstraints);

        circlCenterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Center coordinates"));
        circlCenterPanel.setLayout(new java.awt.GridBagLayout());

        circlCtrLongTextField.setText("-73.99021");
        circlCtrLongTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                circlCtrLongTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        circlCenterPanel.add(circlCtrLongTextField, gridBagConstraints);

        circlCtrLatTextField.setText("40.756146");
        circlCtrLatTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                circlCtrLatTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        circlCenterPanel.add(circlCtrLatTextField, gridBagConstraints);

        circlCtrLongLabel.setText("Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        circlCenterPanel.add(circlCtrLongLabel, gridBagConstraints);

        circlCtrLatLabel.setText("Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        circlCenterPanel.add(circlCtrLatLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        eventCircleTab.add(circlCenterPanel, gridBagConstraints);

        circlRadiusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Radius"));
        circlRadiusPanel.setLayout(new java.awt.GridBagLayout());

        circlRadTextField.setText("1000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        circlRadiusPanel.add(circlRadTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.7;
        eventCircleTab.add(circlRadiusPanel, gridBagConstraints);

        eventLocationTabs.addTab("Circle", eventCircleTab);

        eventFilterTab.setLayout(new java.awt.GridBagLayout());

        filterLabel.setText("<html>Filter events by facets. Each text field and checkbox can be used independently of each other. '+' functions as a 'OR' operator and '-' as a 'NOT' operator. For example \"Theater+Dance-Movies\" in the Category field shows theater and dance events but not movies.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 12, 4);
        eventFilterTab.add(filterLabel, gridBagConstraints);

        evtFilterCatLabel.setText("Category");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        eventFilterTab.add(evtFilterCatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        eventFilterTab.add(evtFilterCatTextField, gridBagConstraints);

        evtFilterSubCatLabel.setText("Subcategory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        eventFilterTab.add(evtFilterSubCatLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        eventFilterTab.add(evtFilterSubCatTextField, gridBagConstraints);

        evtFilterBoroughLabel.setText("Borough");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        eventFilterTab.add(evtFilterBoroughLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        eventFilterTab.add(evtFilterBoroughTextField, gridBagConstraints);

        evtFilterNeighborhoodLabel.setText("Neighborhood");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        eventFilterTab.add(evtFilterNeighborhoodLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        eventFilterTab.add(evtFilterNeighborhoodTextField, gridBagConstraints);

        evtFilterTimesPick.setText("Times pick");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        eventFilterTab.add(evtFilterTimesPick, gridBagConstraints);

        evtFilterFree.setText("Free");
        evtFilterFree.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        eventFilterTab.add(evtFilterFree, gridBagConstraints);

        evtFilterKidFriendly.setText("Kid friendly");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        eventFilterTab.add(evtFilterKidFriendly, gridBagConstraints);

        evtFilterLastChance.setText("Last chance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        eventFilterTab.add(evtFilterLastChance, gridBagConstraints);

        evtFilterFestival.setText("Festival");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        eventFilterTab.add(evtFilterFestival, gridBagConstraints);

        evtFilterLongRunningShow.setText("Long running show");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        eventFilterTab.add(evtFilterLongRunningShow, gridBagConstraints);

        evtFilterPreviewsAndOpenings.setText("Previews and Openings");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        eventFilterTab.add(evtFilterPreviewsAndOpenings, gridBagConstraints);

        eventLocationTabs.addTab("Faceted", eventFilterTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        eventSearchInnerPanel.add(eventLocationTabs, gridBagConstraints);

        eventLimitPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        eventSearchInnerPanel.add(eventLimitPanel, gridBagConstraints);

        eventDateRangePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Time range"));
        eventDateRangePanel.setLayout(new java.awt.GridBagLayout());

        evtDateStartLabel.setText("Start (YYYY-MM-DD)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        eventDateRangePanel.add(evtDateStartLabel, gridBagConstraints);

        evtDateEndLabel.setText("End (YYYY-MM-DD)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        eventDateRangePanel.add(evtDateEndLabel, gridBagConstraints);

        evtDateStartTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evtDateStartTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        eventDateRangePanel.add(evtDateStartTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        eventDateRangePanel.add(evtDateEndTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        eventSearchInnerPanel.add(eventDateRangePanel, gridBagConstraints);

        eventOffsetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Offset"));
        eventOffsetPanel.setLayout(new java.awt.GridBagLayout());

        evtOffset.setText("Offset");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        eventOffsetPanel.add(evtOffset, gridBagConstraints);

        evtOffsetTextField.setText("0");
        evtOffsetTextField.setMinimumSize(new java.awt.Dimension(50, 20));
        evtOffsetTextField.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        eventOffsetPanel.add(evtOffsetTextField, gridBagConstraints);

        evtLimitLabel.setText("Limit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        eventOffsetPanel.add(evtLimitLabel, gridBagConstraints);

        evtLimitTextField.setText("20");
        evtLimitTextField.setMinimumSize(new java.awt.Dimension(50, 20));
        evtLimitTextField.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        eventOffsetPanel.add(evtLimitTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        eventSearchInnerPanel.add(eventOffsetPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        eventSearchInnerPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        eventSearchPanel.add(eventSearchInnerPanel, gridBagConstraints);

        nytTabbedPane.addTab("Event search", eventSearchPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(nytTabbedPane, gridBagConstraints);

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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        accepted = false;
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        accepted = true;
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void forgetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forgetButtonActionPerformed
        if (articleSearchPanel.equals(nytTabbedPane.getSelectedComponent())){
            articleapikey = null;
            forgetButton.setEnabled(false);
        }
        else if(eventSearchPanel.equals(nytTabbedPane.getSelectedComponent())){
            eventapikey = null;
            forgetButton.setEnabled(false);
        }
    }//GEN-LAST:event_forgetButtonActionPerformed

    private void nytTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nytTabbedPaneStateChanged
        if(articleSearchPanel.equals(nytTabbedPane.getSelectedComponent())){
            if(articleapikey != null) forgetButton.setEnabled(true);
            else forgetButton.setEnabled(false);
        } else if(eventSearchPanel.equals(nytTabbedPane.getSelectedComponent())){
            if(eventapikey != null) forgetButton.setEnabled(true);
            else forgetButton.setEnabled(false);
        }
    }//GEN-LAST:event_nytTabbedPaneStateChanged
	
	private void circlCtrLongTextFieldActionPerformed(java.awt.event.ActionEvent evt){
	}
	private void circlCtrLatTextFieldActionPerformed(java.awt.event.ActionEvent evt){
	}
	private void evtDateStartTextFieldActionPerformed(java.awt.event.ActionEvent evt){
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel articleSearchInnerPanel;
    private javax.swing.JPanel articleSearchPanel;
    private javax.swing.JLabel beginDateLabel;
    private javax.swing.JTextField beginDateTextField;
    private javax.swing.JLabel boxLabel;
    private javax.swing.JLabel boxNELatLabel;
    private javax.swing.JTextField boxNELatTextField;
    private javax.swing.JLabel boxNELongLabel;
    private javax.swing.JTextField boxNELongTextField;
    private javax.swing.JPanel boxNEpanel;
    private javax.swing.JLabel boxSWLatLabel;
    private javax.swing.JTextField boxSWLatTextField;
    private javax.swing.JLabel boxSWLongLabel;
    private javax.swing.JTextField boxSWLongTextField;
    private javax.swing.JPanel boxSWpanel;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel circlCenterPanel;
    private javax.swing.JLabel circlCtrLatLabel;
    private javax.swing.JTextField circlCtrLatTextField;
    private javax.swing.JLabel circlCtrLongLabel;
    private javax.swing.JTextField circlCtrLongTextField;
    private javax.swing.JTextField circlRadTextField;
    private javax.swing.JPanel circlRadiusPanel;
    private javax.swing.JLabel circleLabel;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JTextField endDateTextField;
    private javax.swing.JPanel eventBoxTab;
    private javax.swing.JPanel eventCircleTab;
    private javax.swing.JPanel eventDateRangePanel;
    private javax.swing.JPanel eventFilterTab;
    private javax.swing.JPanel eventLimitPanel;
    private javax.swing.JTabbedPane eventLocationTabs;
    private javax.swing.JPanel eventOffsetPanel;
    private javax.swing.JLabel eventQueryLabel;
    private javax.swing.JTextField eventQueryTextField;
    private javax.swing.JPanel eventSearchInnerPanel;
    private javax.swing.JPanel eventSearchPanel;
    private javax.swing.JLabel evtDateEndLabel;
    private javax.swing.JTextField evtDateEndTextField;
    private javax.swing.JLabel evtDateStartLabel;
    private javax.swing.JTextField evtDateStartTextField;
    private javax.swing.JLabel evtFilterBoroughLabel;
    private javax.swing.JTextField evtFilterBoroughTextField;
    private javax.swing.JLabel evtFilterCatLabel;
    private javax.swing.JTextField evtFilterCatTextField;
    private javax.swing.JCheckBox evtFilterFestival;
    private javax.swing.JCheckBox evtFilterFree;
    private javax.swing.JCheckBox evtFilterKidFriendly;
    private javax.swing.JCheckBox evtFilterLastChance;
    private javax.swing.JCheckBox evtFilterLongRunningShow;
    private javax.swing.JLabel evtFilterNeighborhoodLabel;
    private javax.swing.JTextField evtFilterNeighborhoodTextField;
    private javax.swing.JCheckBox evtFilterPreviewsAndOpenings;
    private javax.swing.JLabel evtFilterSubCatLabel;
    private javax.swing.JTextField evtFilterSubCatTextField;
    private javax.swing.JCheckBox evtFilterTimesPick;
    private javax.swing.JLabel evtLimitLabel;
    private javax.swing.JTextField evtLimitTextField;
    private javax.swing.JLabel evtOffset;
    private javax.swing.JTextField evtOffsetTextField;
    private javax.swing.JLabel facetsLabel;
    private javax.swing.JTextField facetsTextField;
    private javax.swing.JLabel fieldsLabel;
    private javax.swing.JList fieldsList;
    private javax.swing.JScrollPane fieldsListScrollPane;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JButton forgetButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane nytTabbedPane;
    private javax.swing.JLabel offsetLabel;
    private javax.swing.JTextField offsetTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel optionalArticleSearchFieldsPanel;
    private javax.swing.JComboBox rankComboBox;
    private javax.swing.JLabel rankLabel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchQueryTextField;
    // End of variables declaration//GEN-END:variables
}
