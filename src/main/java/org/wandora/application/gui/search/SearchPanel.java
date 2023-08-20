/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * SearchPanel.java
 *
 * Created on 29. joulukuuta 2005, 16:34
 */

package org.wandora.application.gui.search;


import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;

import javax.swing.ComboBoxEditor;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.wandora.application.Wandora;
import org.wandora.application.gui.TopicSelector;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleComboBox;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapSearchOptions;
import org.wandora.utils.Textbox;


/**
 *
 * @author  akivela
 */
public class SearchPanel extends javax.swing.JPanel implements TopicSelector {
       
    
    private static final long serialVersionUID = 1L;

    public static final int HISTORYMAXSIZE = 40;
    
    private Wandora wandora;
    private TopicTable foundTable = null;
    private Collection<Topic> foundTopics = null;
    private Topic[] foundTopicsArray = null;
    private boolean allowMultiSelection = true;



    
    /** Creates new form SearchPanel */
    public SearchPanel() {
        this.wandora = Wandora.getWandora();
        initComponents();
        searchWords.getEditor().getEditorComponent().addKeyListener(
            new java.awt.event.KeyAdapter() {
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt){
                    if(evt.getKeyChar()==KeyEvent.VK_ENTER) {
                        doSearch();
                    }
                }
            }
        );
        toggleSearchOptionsVisibility(null);
    }
    
    public SearchPanel(boolean allowMultiSelection) {
        this();
        this.allowMultiSelection = allowMultiSelection;
    }

    
    public void requestSearchFieldFocus() {
        searchWords.requestFocus();
        ComboBoxEditor ed = searchWords.getEditor();
        if(ed != null) {
            ed.selectAll();
        }
    }

    
    public void removeResultScrollPanesMouseListeners() {
        MouseWheelListener[] mouseWheelListeners = resultPanelScroller.getMouseWheelListeners();
        for(MouseWheelListener listener : mouseWheelListeners) {
            resultPanelScroller.removeMouseWheelListener(listener);
        }
    }
    
    
    public void doSearch() {
        try {
            String query = (String) searchWords.getSelectedItem();
            query = Textbox.trimExtraSpaces(query);
            resultPanel.removeAll();
            foundTopics = null;
            if(query != null && query.length() > 0) {
                resultPanel.add(messagePanel, BorderLayout.CENTER);
                messageField.setText("Executing query...");
                messageField.setIcon(UIBox.getIcon("gui/icons/wait.png"));
                refresh();
                
                TopicMap topicMap = wandora.getTopicMap();
                //this.searchWords.setSelectedIndex(0);
                searchWords.addItem(query);
                if(searchWords.getItemCount() > HISTORYMAXSIZE) searchWords.removeItemAt(1);
                
                TopicMapSearchOptions searchOptions;
                if(searchAllCheckBox.isSelected()) {
                    searchOptions = new TopicMapSearchOptions();
                }
                else {
                    searchOptions = new TopicMapSearchOptions(
                        searchBasenamesCheckBox.isSelected(),
                        searchVariantnamesCheckBox.isSelected(),
                        searchTextdatasCheckBox.isSelected(),
                        searchSIsCheckBox.isSelected(),
                        searchSLsCheckBox.isSelected());
                }
                foundTopics = topicMap.search(query, searchOptions);
                foundTopicsArray = foundTopics.toArray(new Topic[] {});
                if(foundTopics != null && foundTopicsArray.length > 0) {
                    foundTable = new TopicTable(wandora);
                    foundTable.initialize(foundTopicsArray, null);
                    foundTable.toggleSortOrder(0);
                    if(!allowMultiSelection) {
                        foundTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    }
                    
                    resultPanel.removeAll();
                    resultPanel.add(foundTable, BorderLayout.CENTER);
                }
                else {
                    //resultPanel.add(messagePanel, BorderLayout.CENTER);
                    messageField.setText("Found no topics!");
                    messageField.setIcon(UIBox.getIcon("gui/icons/warn.png"));
                }
            }
            else {
                resultPanel.add(messagePanel, BorderLayout.CENTER);
                messageField.setText("Query was empty!");
                messageField.setIcon(UIBox.getIcon("gui/icons/warn.png"));
            }
            refresh();
        }
        catch (Exception e) {
            messageField.setText("Error!");
            messageField.setIcon(UIBox.getIcon("gui/icons/warn.png"));
            wandora.handleError(e);
        }
    }
    
    
    
    public void refresh() {
        if(foundTable != null) {
            ((DefaultTableModel) foundTable.getModel()).fireTableDataChanged();
        }
        resultPanel.revalidate();
        resultPanelContainer.revalidate();
        revalidate();
    }




    // -------------------------------------------------------------------------


    
    
    @Override
    public Topic getSelectedTopic() {
        if(foundTable != null) {
            Topic[] topics = foundTable.getSelectedTopics();
            if(topics != null && topics.length > 0) {
                return topics[0];
            }
        }
        return null;
    }


    @Override
    public Topic[] getSelectedTopics() {
        if(foundTable != null) {
            foundTable.getSelectedTopics();
        }
        return null;
    }


    @Override
    public java.awt.Component getPanel() {
        return this;
    }

    @Override
    public String getSelectorName() {
        return "Finder";
    }

    @Override
    public void init() {
        searchWords.setSelectedItem("");
    }


    @Override
    public void cleanup() {

    }



    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        messagePanel = new javax.swing.JPanel();
        messageField = new org.wandora.application.gui.simple.SimpleLabel();
        searchFieldPanel = new javax.swing.JPanel();
        searchWords = new SimpleComboBox();
        startSearchButton = new org.wandora.application.gui.simple.SimpleButton();
        searchOptionsPanel = new javax.swing.JPanel();
        searchAllCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchBasenamesCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchVariantnamesCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchTextdatasCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchSLsCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        searchSIsCheckBox = new org.wandora.application.gui.simple.SimpleCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        resultPanelContainer = new javax.swing.JPanel();
        resultPanelScroller = new org.wandora.application.gui.simple.SimpleScrollPane();
        resultPanel = new javax.swing.JPanel();

        messagePanel.setBackground(new java.awt.Color(255, 255, 255));
        messagePanel.setLayout(new java.awt.GridBagLayout());

        messageField.setBackground(new java.awt.Color(255, 255, 255));
        messageField.setForeground(new java.awt.Color(53, 56, 87));
        messageField.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        messageField.setText("No topics found!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        messagePanel.add(messageField, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        searchFieldPanel.setLayout(new java.awt.GridBagLayout());

        searchWords.setEditable(true);
        searchWords.setPreferredSize(new java.awt.Dimension(29, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        searchFieldPanel.add(searchWords, gridBagConstraints);

        startSearchButton.setFont(org.wandora.application.gui.UIConstants.smallButtonLabelFont);
        startSearchButton.setText("Search");
        startSearchButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        startSearchButton.setMaximumSize(new java.awt.Dimension(60, 25));
        startSearchButton.setMinimumSize(new java.awt.Dimension(60, 25));
        startSearchButton.setPreferredSize(new java.awt.Dimension(60, 25));
        startSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doSearch(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 5);
        searchFieldPanel.add(startSearchButton, gridBagConstraints);

        searchOptionsPanel.setLayout(new java.awt.GridBagLayout());

        searchAllCheckBox.setSelected(true);
        searchAllCheckBox.setText("Search in all topic elements");
        searchAllCheckBox.setMinimumSize(new java.awt.Dimension(155, 20));
        searchAllCheckBox.setPreferredSize(new java.awt.Dimension(155, 20));
        searchAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleSearchOptionsVisibility(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        searchOptionsPanel.add(searchAllCheckBox, gridBagConstraints);

        searchBasenamesCheckBox.setText("Search in base names");
        searchBasenamesCheckBox.setMinimumSize(new java.awt.Dimension(127, 20));
        searchBasenamesCheckBox.setPreferredSize(new java.awt.Dimension(127, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        searchOptionsPanel.add(searchBasenamesCheckBox, gridBagConstraints);

        searchVariantnamesCheckBox.setText("Search in variant names");
        searchVariantnamesCheckBox.setMinimumSize(new java.awt.Dimension(141, 20));
        searchVariantnamesCheckBox.setPreferredSize(new java.awt.Dimension(141, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        searchOptionsPanel.add(searchVariantnamesCheckBox, gridBagConstraints);

        searchTextdatasCheckBox.setText("Search in occurrences");
        searchTextdatasCheckBox.setMinimumSize(new java.awt.Dimension(123, 20));
        searchTextdatasCheckBox.setPreferredSize(new java.awt.Dimension(81, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        searchOptionsPanel.add(searchTextdatasCheckBox, gridBagConstraints);

        searchSLsCheckBox.setText("Search in subject locators");
        searchSLsCheckBox.setMinimumSize(new java.awt.Dimension(149, 20));
        searchSLsCheckBox.setPreferredSize(new java.awt.Dimension(81, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        searchOptionsPanel.add(searchSLsCheckBox, gridBagConstraints);

        searchSIsCheckBox.setText("Search in subject identifiers");
        searchSIsCheckBox.setMinimumSize(new java.awt.Dimension(157, 20));
        searchSIsCheckBox.setPreferredSize(new java.awt.Dimension(81, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        searchOptionsPanel.add(searchSIsCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        searchFieldPanel.add(searchOptionsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        searchFieldPanel.add(jSeparator1, gridBagConstraints);

        add(searchFieldPanel, java.awt.BorderLayout.NORTH);

        resultPanelContainer.setLayout(new java.awt.BorderLayout());

        resultPanelScroller.setBorder(null);

        resultPanel.setLayout(new java.awt.BorderLayout());
        resultPanelScroller.setViewportView(resultPanel);

        resultPanelContainer.add(resultPanelScroller, java.awt.BorderLayout.CENTER);

        add(resultPanelContainer, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void toggleSearchOptionsVisibility(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleSearchOptionsVisibility
        boolean flag = false;
        if(!searchAllCheckBox.isSelected()) {
            flag = true;
        }
        searchBasenamesCheckBox.setVisible(flag);
        searchVariantnamesCheckBox.setVisible(flag);
        searchTextdatasCheckBox.setVisible(flag);
        searchSLsCheckBox.setVisible(flag);
        searchSIsCheckBox.setVisible(flag);
    }//GEN-LAST:event_toggleSearchOptionsVisibility

    private void doSearch(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doSearch
        startSearchButton.setEnabled(false);
        Thread searchThread = new Thread() {
            public void run() {
                doSearch();
                startSearchButton.setEnabled(true);
            };
        };
        searchThread.start();
    }//GEN-LAST:event_doSearch
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel messageField;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JPanel resultPanelContainer;
    private javax.swing.JScrollPane resultPanelScroller;
    private javax.swing.JCheckBox searchAllCheckBox;
    private javax.swing.JCheckBox searchBasenamesCheckBox;
    private javax.swing.JPanel searchFieldPanel;
    private javax.swing.JPanel searchOptionsPanel;
    private javax.swing.JCheckBox searchSIsCheckBox;
    private javax.swing.JCheckBox searchSLsCheckBox;
    private javax.swing.JCheckBox searchTextdatasCheckBox;
    private javax.swing.JCheckBox searchVariantnamesCheckBox;
    private javax.swing.JComboBox searchWords;
    private javax.swing.JButton startSearchButton;
    // End of variables declaration//GEN-END:variables
    
}
