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
 */

package org.wandora.application.tools.extractors.guardian;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
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
 * Eero
 */


public class GuardianExtractorUI extends javax.swing.JPanel {
  
    private Wandora wandora = null;
    private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;
    private static final String GUARDIAN_CONTENT_API_BASE = "http://content.guardianapis.com/search?format=json";
    private static final String GUARDIAN_TAG_API_BASE = "http://content.guardianapis.com/tags?format=json";

  /*
   * Creates new form GuardianExtractorUI
   */
  public GuardianExtractorUI() {
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
        dialog.setTitle("The Guardian API extractor");
        UIBox.centerWindow(dialog, w);
        if(apikey!= null){
            forgetButton.setEnabled(true);
        } else {
            forgetButton.setEnabled(false);
        }
        dialog.setVisible(true);
    }
    
    public WandoraTool[] getExtractors(GuardianExtractor tool) throws TopicMapException {
        Component component = guardianTabbedPane.getSelectedComponent();
        WandoraTool wt = null;
        ArrayList<WandoraTool> wts = new ArrayList();

        if (contentSearchPanel.equals(component)){
            String query = searchQueryTextField.getText();
            String key = solveAPIKey();

            if (key == null) {
            return null;
            }

            String extractUrl = GUARDIAN_CONTENT_API_BASE + "&q=" + urlEncode(query);
            if(allFieldsCheckBox.isSelected()){
                extractUrl += "&show-fields=all";
            } else {
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
                    extractUrl += "&show-fields=" + urlEncode(fieldValues.toString());
            }
            }

            if(allTagsCheckBox.isSelected()){
                extractUrl += "&show-tags=all"; 
            } else {
                int[] tagIndexes = tagsList.getSelectedIndices();
                ListModel tagListModel = tagsList.getModel();
                StringBuilder tagValues = new StringBuilder("");
                for (int i = 0; i < tagIndexes.length; i++) {
                    if (i > 0) {
                        tagValues.append(",");
                    }
                    tagValues.append(tagListModel.getElementAt(tagIndexes[i]));
                }
                if (tagValues.length() > 0) {
                    extractUrl += "&show-tags=" + urlEncode(tagValues.toString());
                }
            }

            String beginDate = beginDateTextField.getText().trim();
            if (beginDate != null && beginDate.length() > 0) {
                extractUrl += "&from-date=" + urlEncode(beginDate);
            }
            String endDate = endDateTextField.getText().trim();
            if (endDate != null && endDate.length() > 0) {
                extractUrl += "&to-date=" + urlEncode(endDate);
            }

            String tags = tagQueryTextField.getText().trim();
            if( tags != null && tags.length() > 0) {
                extractUrl += "&tags=" + urlEncode(tags);
            }

            String sections = sectionQueryTextField.getText().trim();
            if( sections != null && sections.length() > 0) {
                extractUrl += "&section" + urlEncode(sections);
            }

            String order = rankComboBox.getSelectedItem().toString();
            if( order != null && order.length() > 0) {
                extractUrl += "&order-by=" + urlEncode(order);
            } 
            
            extractUrl += "&page=1&api-key=" + key;
            
            System.out.println("URL: " + extractUrl);

            GuardianContentSearchExtractor ex = new GuardianContentSearchExtractor();
            ex.setForceUrls(new String[]{extractUrl});
            wt = ex;
            wts.add(wt);
        } else if (tagSearchPanel.equals(component)){
            String query = tagSearchQueryTextField.getText();
            String key = solveAPIKey();

            if (key == null) {
            return null;
            }

            String extractUrl = GUARDIAN_TAG_API_BASE + "&q=" + urlEncode(query);
            if(allTagsCheckBox.isSelected()){
                extractUrl += "&type=all";
            } else {
                int[] typeIndexes = typesList.getSelectedIndices();
                ListModel listModel = typesList.getModel();
                StringBuilder typeValues = new StringBuilder("");
                for (int i = 0; i < typeIndexes.length; i++) {
                    if (i > 0) {
                        typeValues.append(",");
                    }
                    typeValues.append(listModel.getElementAt(typeIndexes[i]));
                }
                if (typeValues.length() > 0) {
                    extractUrl += "&type=" + urlEncode(typeValues.toString());
                }
                
                extractUrl += "&page=1&api-key=" + key;
                System.out.println(extractUrl);
                GuardianTagSearchExtractor ex = new GuardianTagSearchExtractor();
                ex.setForceUrls(new String[]{extractUrl});
                wt = ex;
                wts.add(wt);
            }
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
    private static String apikey = null;

    public String solveAPIKey(Wandora wandora) {
        return solveAPIKey();
    }
    
    
    public String solveAPIKey() {
        if(apikey == null){
                apikey = "";
                apikey = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Please give an api-key for the Guardian API. You can register your api-key at http://www.guardian.co.uk/open-platform", apikey, "the Guardian api-key", WandoraOptionPane.QUESTION_MESSAGE);
            if(apikey != null) apikey = apikey.trim();
        }
        forgetButton.setEnabled(true);
        return apikey;
    }
    
    public void forgetAuthorization() {
        apikey = null;
        forgetButton.setEnabled(false);
    }
  
  /*
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always regenerated
   * by the Form Editor.
   */
  
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        guardianTabbedPane = new SimpleTabbedPane();
        contentSearchPanel = new javax.swing.JPanel();
        contentSearchInnerPanel = new javax.swing.JPanel();
        searchLabel = new SimpleLabel();
        searchQueryTextField = new SimpleField();
        optionalSearchFieldsPanel = new javax.swing.JPanel();
        tagLabel = new SimpleLabel();
        tagQueryTextField = new SimpleField();
        sectionLabel = new SimpleLabel();
        sectionQueryTextField = new SimpleField();
        beginDateLabel = new SimpleLabel();
        beginDateTextField = new SimpleField();
        endDateLabel = new SimpleLabel();
        endDateTextField = new SimpleField();
        orderByLabel = new SimpleLabel();
        rankComboBox = new javax.swing.JComboBox();
        fieldToReturnPanel = new javax.swing.JPanel();
        FieldsToReturnLabel = new SimpleLabel();
        fieldsScrollPanel = new javax.swing.JScrollPane();
        fieldsList = new SimpleList();
        tagsToReturnLabel = new SimpleLabel();
        tagsScrollPanel = new javax.swing.JScrollPane();
        tagsList = new SimpleList();
        allFieldsCheckBox = new javax.swing.JCheckBox();
        allTagsCheckBox = new javax.swing.JCheckBox();
        tagSearchPanel = new javax.swing.JPanel();
        tagSearchInnerPanel = new javax.swing.JPanel();
        tagSearchLabel = new SimpleLabel();
        tagSearchQueryTextField = new SimpleField();
        optionaTagSearchFieldsPanel = new javax.swing.JPanel();
        TypesList = new SimpleLabel();
        typesScrollPanel = new javax.swing.JScrollPane();
        typesList = new SimpleList();
        allTypesCheckBox = new javax.swing.JCheckBox();
        buttonPanel = new javax.swing.JPanel();
        forgetButton = new SimpleButton();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        guardianTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                guardianTabbedPaneStateChanged(evt);
            }
        });

        contentSearchPanel.setLayout(new java.awt.GridBagLayout());

        contentSearchInnerPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("Search query");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        contentSearchInnerPanel.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        contentSearchInnerPanel.add(searchQueryTextField, gridBagConstraints);

        optionalSearchFieldsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Optional params"));
        optionalSearchFieldsPanel.setLayout(new java.awt.GridBagLayout());

        tagLabel.setText("Tags (comma separated list)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        optionalSearchFieldsPanel.add(tagLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionalSearchFieldsPanel.add(tagQueryTextField, gridBagConstraints);

        sectionLabel.setText("Sections (comma separated list)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        optionalSearchFieldsPanel.add(sectionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionalSearchFieldsPanel.add(sectionQueryTextField, gridBagConstraints);

        beginDateLabel.setText("Begin date (YYYY-MM-DD)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionalSearchFieldsPanel.add(beginDateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 6, 4);
        optionalSearchFieldsPanel.add(beginDateTextField, gridBagConstraints);

        endDateLabel.setText("End date (YYYY-MM-DD)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionalSearchFieldsPanel.add(endDateLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 6, 4);
        optionalSearchFieldsPanel.add(endDateTextField, gridBagConstraints);

        orderByLabel.setText("Order by");
        orderByLabel.setToolTipText("Use the rank parameter to set the order of the results. The default rank is newest.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        optionalSearchFieldsPanel.add(orderByLabel, gridBagConstraints);

        rankComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "newest", "oldest", "relevance" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionalSearchFieldsPanel.add(rankComboBox, gridBagConstraints);

        fieldToReturnPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Additional properties to return"));
        fieldToReturnPanel.setLayout(new java.awt.GridBagLayout());

        FieldsToReturnLabel.setText("Fields");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        fieldToReturnPanel.add(FieldsToReturnLabel, gridBagConstraints);

        fieldsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "headline", "byline", "body", "standfirst", "strap", "short-url", "thumbnail", "publication" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fieldsScrollPanel.setViewportView(fieldsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 4, 4);
        fieldToReturnPanel.add(fieldsScrollPanel, gridBagConstraints);

        tagsToReturnLabel.setText("Tags");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        fieldToReturnPanel.add(tagsToReturnLabel, gridBagConstraints);

        tagsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "keyword", "contributor", "tone", "type" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        tagsScrollPanel.setViewportView(tagsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 4, 4);
        fieldToReturnPanel.add(tagsScrollPanel, gridBagConstraints);

        allFieldsCheckBox.setText("all");
        allFieldsCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        allFieldsCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                allFieldsCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        fieldToReturnPanel.add(allFieldsCheckBox, gridBagConstraints);

        allTagsCheckBox.setText("all");
        allTagsCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        allTagsCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                allTagsCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        fieldToReturnPanel.add(allTagsCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 5.0;
        gridBagConstraints.weighty = 1.0;
        optionalSearchFieldsPanel.add(fieldToReturnPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        contentSearchInnerPanel.add(optionalSearchFieldsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        contentSearchPanel.add(contentSearchInnerPanel, gridBagConstraints);

        guardianTabbedPane.addTab("Content search", contentSearchPanel);

        tagSearchPanel.setLayout(new java.awt.GridBagLayout());

        tagSearchInnerPanel.setLayout(new java.awt.GridBagLayout());

        tagSearchLabel.setText("Search query");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        tagSearchInnerPanel.add(tagSearchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        tagSearchInnerPanel.add(tagSearchQueryTextField, gridBagConstraints);

        optionaTagSearchFieldsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Optional params"));
        optionaTagSearchFieldsPanel.setLayout(new java.awt.GridBagLayout());

        TypesList.setText("Filter types");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        optionaTagSearchFieldsPanel.add(TypesList, gridBagConstraints);

        typesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "keyword", "contributor", "tone", "series" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        typesScrollPanel.setViewportView(typesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionaTagSearchFieldsPanel.add(typesScrollPanel, gridBagConstraints);

        allTypesCheckBox.setText("all");
        allTypesCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        allTypesCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                allTypesCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        optionaTagSearchFieldsPanel.add(allTypesCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        tagSearchInnerPanel.add(optionaTagSearchFieldsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        tagSearchPanel.add(tagSearchInnerPanel, gridBagConstraints);

        guardianTabbedPane.addTab("Tag search", tagSearchPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(guardianTabbedPane, gridBagConstraints);

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
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

  private void forgetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forgetButtonActionPerformed
            apikey = null;
            forgetButton.setEnabled(false);
  }//GEN-LAST:event_forgetButtonActionPerformed

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

    private void allFieldsCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_allFieldsCheckBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED) fieldsList.setEnabled(false);
        else fieldsList.setEnabled(true);
    }//GEN-LAST:event_allFieldsCheckBoxItemStateChanged

    private void allTagsCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_allTagsCheckBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED) tagsList.setEnabled(false);
        else tagsList.setEnabled(true);
    }//GEN-LAST:event_allTagsCheckBoxItemStateChanged

    private void allTypesCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_allTypesCheckBoxItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_allTypesCheckBoxItemStateChanged

    private void guardianTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_guardianTabbedPaneStateChanged

    }//GEN-LAST:event_guardianTabbedPaneStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel FieldsToReturnLabel;
    private javax.swing.JLabel TypesList;
    private javax.swing.JCheckBox allFieldsCheckBox;
    private javax.swing.JCheckBox allTagsCheckBox;
    private javax.swing.JCheckBox allTypesCheckBox;
    private javax.swing.JLabel beginDateLabel;
    private javax.swing.JTextField beginDateTextField;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel contentSearchInnerPanel;
    private javax.swing.JPanel contentSearchPanel;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JTextField endDateTextField;
    private javax.swing.JPanel fieldToReturnPanel;
    private javax.swing.JList fieldsList;
    private javax.swing.JScrollPane fieldsScrollPanel;
    private javax.swing.JButton forgetButton;
    private javax.swing.JTabbedPane guardianTabbedPane;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel optionaTagSearchFieldsPanel;
    private javax.swing.JPanel optionalSearchFieldsPanel;
    private javax.swing.JLabel orderByLabel;
    private javax.swing.JComboBox rankComboBox;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchQueryTextField;
    private javax.swing.JLabel sectionLabel;
    private javax.swing.JTextField sectionQueryTextField;
    private javax.swing.JLabel tagLabel;
    private javax.swing.JTextField tagQueryTextField;
    private javax.swing.JPanel tagSearchInnerPanel;
    private javax.swing.JLabel tagSearchLabel;
    private javax.swing.JPanel tagSearchPanel;
    private javax.swing.JTextField tagSearchQueryTextField;
    private javax.swing.JList tagsList;
    private javax.swing.JScrollPane tagsScrollPanel;
    private javax.swing.JLabel tagsToReturnLabel;
    private javax.swing.JList typesList;
    private javax.swing.JScrollPane typesScrollPanel;
    // End of variables declaration//GEN-END:variables
}
