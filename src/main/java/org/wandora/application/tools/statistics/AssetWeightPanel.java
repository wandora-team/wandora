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
 * AssetWeightPanel.java
 *
 * Created on 10.11.2010, 10:51:45
 */





package org.wandora.application.tools.statistics;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleToggleButton;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.ClipboardBox;





/**
 *
 * @author akivela
 */
public class AssetWeightPanel extends javax.swing.JPanel {


	private static final long serialVersionUID = 1L;

	private JDialog assetWeightDialog = null;
    private Wandora app = null;

    private Collection<Topic> topics = null;
    private TopicMap tm = null;
    

    private TopicWeightTable followWeightTypesTable = null;
    private TopicWeightTable partialWeightTypesTable = null;
    private TopicWeightTable resultsTable = null;

    private HashMap<Topic,Double> followWeightTypes = new LinkedHashMap<Topic,Double>();
    private HashMap<Topic,Double> partialWeightTypes = new LinkedHashMap<Topic,Double>();

    private HashMap<Topic, Double> partialWeights = new LinkedHashMap<Topic, Double>();
    private HashMap<Topic, Double> neighborhoodWeights = new LinkedHashMap<Topic, Double>();

    private HashMap<Topic, Double> totalTopicWeights = new LinkedHashMap<Topic, Double>();
    private HashMap<Topic, Double> normalizedTotalTopicWeights = new LinkedHashMap<Topic, Double>();




    /** Creates new form AssetWeightPanel */
    public AssetWeightPanel(Wandora application, Collection<Topic> topics) {
        this.app = application;
        this.topics = topics;
        this.tm = app.getTopicMap();

        initComponents();

        followWeightTypesTable = new TopicWeightTable(followWeightTypes);
        followWeightTypesTableScrollPane.setViewportView(followWeightTypesTable);
        followWeightTypesTablePanel.add(followWeightTypesTable.getTableHeader(), BorderLayout.NORTH);

        partialWeightTypesTable = new TopicWeightTable(partialWeightTypes);
        partialWeightTypesTableScrollPane.setViewportView(partialWeightTypesTable);
        partialWeightTypesTablePanel.add(partialWeightTypesTable.getTableHeader(), BorderLayout.NORTH);

        resultsTable = new TopicWeightTable(totalTopicWeights);
        resultsTableScrollPane.setViewportView(resultsTable);
        resultsTablePanel.add(resultsTable.getTableHeader(), BorderLayout.NORTH);
        resultsTable.setColumnNames("Topic", "Asset weight");
    }





    public void open(String title, TopicMap topicMap) {
        tm = topicMap;
        assetWeightDialog = new JDialog(app, true);
        if(title == null) title = "";
        else title = " - "+title;
        assetWeightDialog.setTitle("Topic asset weights" + title);
        assetWeightDialog.add(this);
        assetWeightDialog.setSize(800,350);
        app.centerWindow(assetWeightDialog);
        assetWeightDialog.setVisible(true);
        resultsInfoLabel.setText("");

        calculateAndUpdate();
    }



    public void close() {
        if(assetWeightDialog != null) {
            assetWeightDialog.setVisible(false);
        }
    }


    public void setTopics(Collection<Topic> topics) {
        this.topics = topics;
    }



    private void updateResultsInfo(double average) {
        resultsInfoLabel.setText("Average weight: "+average);
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

        assetWeightTabbedPane = new SimpleTabbedPane();
        introductionPanel = new javax.swing.JPanel();
        introductionPanelInner = new javax.swing.JPanel();
        introductionLabel = new SimpleLabel();
        partialWeightOptionsPanel = new javax.swing.JPanel();
        partialWeightOptionsPanelInner = new javax.swing.JPanel();
        partialWeightOptionsLabel = new SimpleLabel();
        jPanel1 = new javax.swing.JPanel();
        initialPartialWeightPanel = new javax.swing.JPanel();
        initialPartialWeightLabel = new SimpleLabel();
        initialPartialWeightTextField = new SimpleField();
        includeOccurrencesPanel = new javax.swing.JPanel();
        includeOccurrencesCheckBox = new SimpleCheckBox();
        defaultPartialWeightOfOccurrenceTextField = new SimpleField();
        includeAssociationsPanel = new javax.swing.JPanel();
        includeAssociationsCheckBox = new SimpleCheckBox();
        defaultPartialWeightOfAssociationTextField = new SimpleField();
        includeClassesPanel = new javax.swing.JPanel();
        includeClassesCheckBox = new SimpleCheckBox();
        partialWeightOfClassTextField = new SimpleField();
        includeInstancesPanel = new javax.swing.JPanel();
        includeInstancesCheckBox = new SimpleCheckBox();
        partialWeightOfinstanceTextField = new SimpleField();
        partialWeightsPanel = new javax.swing.JPanel();
        partialWeightTypesPanelInner = new javax.swing.JPanel();
        partialWeightTypesTablePanel = new javax.swing.JPanel();
        partialWeightTypesTableScrollPane = new javax.swing.JScrollPane();
        partialWeightTypesButtonPanel = new javax.swing.JPanel();
        addPartialWeightTypeButton = new SimpleButton();
        removePartialWeightTypeButton = new SimpleButton();
        followOptionsPanel = new javax.swing.JPanel();
        followOptionsPanelPanelInner = new javax.swing.JPanel();
        followOptionsLabel = new SimpleLabel();
        jPanel2 = new javax.swing.JPanel();
        followAssociationsPanel = new javax.swing.JPanel();
        followAllAssociationsCheckBox = new SimpleCheckBox();
        defaultFollowWeightOfAssociationTextField = new SimpleField();
        followNaryAssociationsCheckBox = new SimpleCheckBox();
        followClassesPanel = new javax.swing.JPanel();
        followClassesCheckBox = new SimpleCheckBox();
        followWeightOfClassTextField = new SimpleField();
        followInstancesPanel = new javax.swing.JPanel();
        followInstancesCheckBox = new SimpleCheckBox();
        followWeightOfInstanceTextField = new SimpleField();
        coefficientPanel = new javax.swing.JPanel();
        coefficientLabel = new SimpleLabel();
        coefficientTextField = new SimpleField();
        followWeightsPanel = new javax.swing.JPanel();
        followWeightsPanelInner = new javax.swing.JPanel();
        followWeightTypesTablePanel = new javax.swing.JPanel();
        followWeightTypesTableScrollPane = new SimpleScrollPane();
        followWeightsButtonPanel = new javax.swing.JPanel();
        addFollowWeightTypeButton = new SimpleButton();
        removeFollowWeightTypeButton = new SimpleButton();
        resultsPanel = new javax.swing.JPanel();
        resultsPanelInner = new javax.swing.JPanel();
        resultsTablePanel = new javax.swing.JPanel();
        resultsTableScrollPane = new javax.swing.JScrollPane();
        resultsButtonPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        resultsInfoLabel = new javax.swing.JLabel();
        copyResultsButton = new SimpleButton();
        normalizeButton = new SimpleToggleButton();
        jPanel3 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        calculateButton = new SimpleButton();
        closeButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        introductionPanel.setLayout(new java.awt.GridBagLayout());

        introductionPanelInner.setLayout(new java.awt.GridBagLayout());

        introductionLabel.setText("<html>Asset weight is a measure of a topic. The asset weight is a numeric value proportional to the number of occurrences and associations of a topic and it's neighbors. \nThe asset weight measure has been developed by Petra Haluzova and is described in detail in her TMRA'10 conference paper 'Evaluation of Instances Asset in a Topic Maps-Based Ontology'. This dialog is used to calculate asset weight values for selected topics.<br><br>To continue, select which assets you want to include asset weight calculations. The asset selection is created in 'Partial weight options' tab. You can specify type specific asset weights in 'Partial weights' tab. \nThen select which associations specify a neighborhood for a topic, and give follow weights. Finally, press Calculate button to create a result table.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        introductionPanelInner.add(introductionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 20, 20);
        introductionPanel.add(introductionPanelInner, gridBagConstraints);

        assetWeightTabbedPane.addTab("Introduction", introductionPanel);

        partialWeightOptionsPanel.setLayout(new java.awt.GridBagLayout());

        partialWeightOptionsPanelInner.setLayout(new java.awt.GridBagLayout());

        partialWeightOptionsLabel.setText("<html>In this tab you can specify which topic elements are used to calculate partial weights. Partial weight is a topic specific value proportional to the number of topic's occurrences and associations.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        partialWeightOptionsPanelInner.add(partialWeightOptionsLabel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        initialPartialWeightPanel.setLayout(new java.awt.GridBagLayout());

        initialPartialWeightLabel.setFont(org.wandora.application.gui.UIConstants.buttonLabelFont);
        initialPartialWeightLabel.setText("Initial partial weight of a topic");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        initialPartialWeightPanel.add(initialPartialWeightLabel, gridBagConstraints);

        initialPartialWeightTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        initialPartialWeightTextField.setText("1");
        initialPartialWeightTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        initialPartialWeightTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        initialPartialWeightPanel.add(initialPartialWeightTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(initialPartialWeightPanel, gridBagConstraints);

        includeOccurrencesPanel.setLayout(new java.awt.GridBagLayout());

        includeOccurrencesCheckBox.setSelected(true);
        includeOccurrencesCheckBox.setText("Include ALL occurrences. If unselected, includes only occurrences specified in Partial weights tab. Default occurrence weight is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        includeOccurrencesPanel.add(includeOccurrencesCheckBox, gridBagConstraints);

        defaultPartialWeightOfOccurrenceTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        defaultPartialWeightOfOccurrenceTextField.setText("1");
        defaultPartialWeightOfOccurrenceTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        defaultPartialWeightOfOccurrenceTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        includeOccurrencesPanel.add(defaultPartialWeightOfOccurrenceTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(includeOccurrencesPanel, gridBagConstraints);

        includeAssociationsPanel.setLayout(new java.awt.GridBagLayout());

        includeAssociationsCheckBox.setText("Include ALL associations. If unselected, includes only associations specified in Partial weights tab. Default association weight is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        includeAssociationsPanel.add(includeAssociationsCheckBox, gridBagConstraints);

        defaultPartialWeightOfAssociationTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        defaultPartialWeightOfAssociationTextField.setText("1");
        defaultPartialWeightOfAssociationTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        defaultPartialWeightOfAssociationTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        includeAssociationsPanel.add(defaultPartialWeightOfAssociationTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(includeAssociationsPanel, gridBagConstraints);

        includeClassesPanel.setLayout(new java.awt.GridBagLayout());

        includeClassesCheckBox.setText("Include classes. Class asset weight is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        includeClassesPanel.add(includeClassesCheckBox, gridBagConstraints);

        partialWeightOfClassTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        partialWeightOfClassTextField.setText("1");
        partialWeightOfClassTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        partialWeightOfClassTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        includeClassesPanel.add(partialWeightOfClassTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(includeClassesPanel, gridBagConstraints);

        includeInstancesPanel.setLayout(new java.awt.GridBagLayout());

        includeInstancesCheckBox.setText("Include instances. Instance asset weight is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        includeInstancesPanel.add(includeInstancesCheckBox, gridBagConstraints);

        partialWeightOfinstanceTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        partialWeightOfinstanceTextField.setText("1");
        partialWeightOfinstanceTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        partialWeightOfinstanceTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        includeInstancesPanel.add(partialWeightOfinstanceTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(includeInstancesPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        partialWeightOptionsPanelInner.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        partialWeightOptionsPanel.add(partialWeightOptionsPanelInner, gridBagConstraints);

        assetWeightTabbedPane.addTab("Partial weight options", partialWeightOptionsPanel);

        partialWeightsPanel.setLayout(new java.awt.GridBagLayout());

        partialWeightTypesPanelInner.setLayout(new java.awt.GridBagLayout());

        partialWeightTypesTablePanel.setLayout(new java.awt.BorderLayout());
        partialWeightTypesTablePanel.add(partialWeightTypesTableScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        partialWeightTypesPanelInner.add(partialWeightTypesTablePanel, gridBagConstraints);

        partialWeightTypesButtonPanel.setLayout(new java.awt.GridBagLayout());

        addPartialWeightTypeButton.setText("Add type");
        addPartialWeightTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addPartialWeightTypeButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        partialWeightTypesButtonPanel.add(addPartialWeightTypeButton, gridBagConstraints);

        removePartialWeightTypeButton.setText("Remove type");
        removePartialWeightTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                removePartialWeightTypeButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        partialWeightTypesButtonPanel.add(removePartialWeightTypeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        partialWeightTypesPanelInner.add(partialWeightTypesButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        partialWeightsPanel.add(partialWeightTypesPanelInner, gridBagConstraints);

        assetWeightTabbedPane.addTab("Partial weights", partialWeightsPanel);

        followOptionsPanel.setLayout(new java.awt.GridBagLayout());

        followOptionsPanelPanelInner.setLayout(new java.awt.GridBagLayout());

        followOptionsLabel.setText("<html>Here you can specify what is topic's neighborhood. You can follow all associations or just some.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        followOptionsPanelPanelInner.add(followOptionsLabel, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        followAssociationsPanel.setLayout(new java.awt.GridBagLayout());

        followAllAssociationsCheckBox.setSelected(true);
        followAllAssociationsCheckBox.setText("Follow ALL associations. If unselected, follows only associations specified in Follow weights tab. Default follow weight is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        followAssociationsPanel.add(followAllAssociationsCheckBox, gridBagConstraints);

        defaultFollowWeightOfAssociationTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        defaultFollowWeightOfAssociationTextField.setText("1");
        defaultFollowWeightOfAssociationTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        defaultFollowWeightOfAssociationTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        followAssociationsPanel.add(defaultFollowWeightOfAssociationTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(followAssociationsPanel, gridBagConstraints);

        followNaryAssociationsCheckBox.setSelected(true);
        followNaryAssociationsCheckBox.setText("Follow N-ary associations.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(followNaryAssociationsCheckBox, gridBagConstraints);

        followClassesPanel.setLayout(new java.awt.GridBagLayout());

        followClassesCheckBox.setSelected(true);
        followClassesCheckBox.setText("Follow classes. Class asset follow weight is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        followClassesPanel.add(followClassesCheckBox, gridBagConstraints);

        followWeightOfClassTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        followWeightOfClassTextField.setText("1");
        followWeightOfClassTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        followWeightOfClassTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        followClassesPanel.add(followWeightOfClassTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(followClassesPanel, gridBagConstraints);

        followInstancesPanel.setLayout(new java.awt.GridBagLayout());

        followInstancesCheckBox.setSelected(true);
        followInstancesCheckBox.setText("Follow instances. Instance asset follow weight is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        followInstancesPanel.add(followInstancesCheckBox, gridBagConstraints);

        followWeightOfInstanceTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        followWeightOfInstanceTextField.setText("1");
        followWeightOfInstanceTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        followWeightOfInstanceTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        followInstancesPanel.add(followWeightOfInstanceTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(followInstancesPanel, gridBagConstraints);

        coefficientPanel.setLayout(new java.awt.GridBagLayout());

        coefficientLabel.setFont(org.wandora.application.gui.UIConstants.buttonLabelFont);
        coefficientLabel.setText("Follow coefficient is");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        coefficientPanel.add(coefficientLabel, gridBagConstraints);

        coefficientTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        coefficientTextField.setText("0.5");
        coefficientTextField.setMinimumSize(new java.awt.Dimension(40, 20));
        coefficientTextField.setPreferredSize(new java.awt.Dimension(40, 20));
        coefficientPanel.add(coefficientTextField, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(coefficientPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        followOptionsPanelPanelInner.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        followOptionsPanel.add(followOptionsPanelPanelInner, gridBagConstraints);

        assetWeightTabbedPane.addTab("Follow options", followOptionsPanel);

        followWeightsPanel.setLayout(new java.awt.GridBagLayout());

        followWeightsPanelInner.setLayout(new java.awt.GridBagLayout());

        followWeightTypesTablePanel.setLayout(new java.awt.BorderLayout());
        followWeightTypesTablePanel.add(followWeightTypesTableScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        followWeightsPanelInner.add(followWeightTypesTablePanel, gridBagConstraints);

        followWeightsButtonPanel.setLayout(new java.awt.GridBagLayout());

        addFollowWeightTypeButton.setText("Add type");
        addFollowWeightTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                addFollowWeightTypeButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        followWeightsButtonPanel.add(addFollowWeightTypeButton, gridBagConstraints);

        removeFollowWeightTypeButton.setText("Remove type");
        removeFollowWeightTypeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                removeFollowWeightTypeButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        followWeightsButtonPanel.add(removeFollowWeightTypeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        followWeightsPanelInner.add(followWeightsButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        followWeightsPanel.add(followWeightsPanelInner, gridBagConstraints);

        assetWeightTabbedPane.addTab("Follow weights", followWeightsPanel);

        resultsPanel.setLayout(new java.awt.GridBagLayout());

        resultsPanelInner.setLayout(new java.awt.GridBagLayout());

        resultsTablePanel.setLayout(new java.awt.BorderLayout());
        resultsTablePanel.add(resultsTableScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        resultsPanelInner.add(resultsTablePanel, gridBagConstraints);

        resultsButtonPanel.setLayout(new java.awt.GridBagLayout());

        jPanel4.setPreferredSize(new java.awt.Dimension(100, 20));
        jPanel4.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(resultsInfoLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        resultsButtonPanel.add(jPanel4, gridBagConstraints);

        copyResultsButton.setText("Copy");
        copyResultsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                copyResultsButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        resultsButtonPanel.add(copyResultsButton, gridBagConstraints);

        normalizeButton.setFont(org.wandora.application.gui.UIConstants.buttonLabelFont);
        normalizeButton.setText("Normalize");
        normalizeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                normalizeButtonMouseReleased(evt);
            }
        });
        resultsButtonPanel.add(normalizeButton, new java.awt.GridBagConstraints());

        jPanel3.setPreferredSize(new java.awt.Dimension(100, 5));
        jPanel3.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        resultsButtonPanel.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        resultsPanelInner.add(resultsButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        resultsPanel.add(resultsPanelInner, gridBagConstraints);

        assetWeightTabbedPane.addTab("Results", resultsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(assetWeightTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonFillerPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout buttonFillerPanelLayout = new javax.swing.GroupLayout(buttonFillerPanel);
        buttonFillerPanel.setLayout(buttonFillerPanelLayout);
        buttonFillerPanelLayout.setHorizontalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 597, Short.MAX_VALUE)
        );
        buttonFillerPanelLayout.setVerticalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        calculateButton.setText("Calculate");
        calculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                calculateButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(calculateButton, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                closeButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(closeButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addFollowWeightTypeButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addFollowWeightTypeButtonMouseReleased
        try {
            Topic t = app.showTopicFinder(app, "Select follow type topic");
            if(t != null) {
                followWeightTypesTable.addTopicWeight(t, 1);
            }
        }
        catch(Exception e) {
            app.handleError(e);
            e.printStackTrace();
        }
    }//GEN-LAST:event_addFollowWeightTypeButtonMouseReleased



    private void addPartialWeightTypeButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addPartialWeightTypeButtonMouseReleased
        try {
            Topic t = app.showTopicFinder(app, "Select partial type topic");
            if(t != null) {
                partialWeightTypesTable.addTopicWeight(t, 1);
            }
        }
        catch(Exception e) {
            app.handleError(e);
            e.printStackTrace();
        }
    }//GEN-LAST:event_addPartialWeightTypeButtonMouseReleased

    private void closeButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeButtonMouseReleased
        this.close();
    }//GEN-LAST:event_closeButtonMouseReleased

    private void calculateButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calculateButtonMouseReleased
        try {
            calculateAndUpdate();
            this.assetWeightTabbedPane.setSelectedComponent(resultsPanel);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_calculateButtonMouseReleased

    private void normalizeButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_normalizeButtonMouseReleased
        try {
            if(normalizeButton.isSelected()) {
                resultsTable.refreshModel(normalizedTotalTopicWeights);
                updateResultsInfo(averageNormalizedWeight);
            }
            else {
                resultsTable.refreshModel(totalTopicWeights);
                updateResultsInfo(averageWeight);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_normalizeButtonMouseReleased




    
    private void copyResultsButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_copyResultsButtonMouseReleased
        String tableContent = resultsTable.copySelectedOrAllRows();
        ClipboardBox.setClipboard(tableContent);

        /*
        HashMap<Topic, Double> hash = null;
        if(normalizeButton.isSelected()) {
            hash = normalizedTotalTopicWeights;
        }
        else {
            hash = totalTopicWeights;
        }
        StringBuilder sb = new StringBuilder("");
        if(hash != null && !hash.isEmpty()) {
            for(Topic t : hash.keySet()) {
                try {
                    String n = t.getBaseName();
                    if(n == null) n = t.getOneSubjectIdentifier().toExternalForm();
                    String w = hash.get(t).toString();
                    sb.append(n);
                    sb.append("\t");
                    sb.append(w);
                    sb.append("\n");
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            sb.append("Asset weight result set is empty!");
        }
        ClipboardBox.setClipboard(sb.toString());
         */
    }//GEN-LAST:event_copyResultsButtonMouseReleased

    private void removePartialWeightTypeButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removePartialWeightTypeButtonMouseReleased
        partialWeightTypesTable.deleteSelectedRows();
    }//GEN-LAST:event_removePartialWeightTypeButtonMouseReleased

    private void removeFollowWeightTypeButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeFollowWeightTypeButtonMouseReleased
        followWeightTypesTable.deleteSelectedRows();
    }//GEN-LAST:event_removeFollowWeightTypeButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFollowWeightTypeButton;
    private javax.swing.JButton addPartialWeightTypeButton;
    private javax.swing.JTabbedPane assetWeightTabbedPane;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton calculateButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel coefficientLabel;
    private javax.swing.JPanel coefficientPanel;
    private javax.swing.JTextField coefficientTextField;
    private javax.swing.JButton copyResultsButton;
    private javax.swing.JTextField defaultFollowWeightOfAssociationTextField;
    private javax.swing.JTextField defaultPartialWeightOfAssociationTextField;
    private javax.swing.JTextField defaultPartialWeightOfOccurrenceTextField;
    private javax.swing.JCheckBox followAllAssociationsCheckBox;
    private javax.swing.JPanel followAssociationsPanel;
    private javax.swing.JCheckBox followClassesCheckBox;
    private javax.swing.JPanel followClassesPanel;
    private javax.swing.JCheckBox followInstancesCheckBox;
    private javax.swing.JPanel followInstancesPanel;
    private javax.swing.JCheckBox followNaryAssociationsCheckBox;
    private javax.swing.JLabel followOptionsLabel;
    private javax.swing.JPanel followOptionsPanel;
    private javax.swing.JPanel followOptionsPanelPanelInner;
    private javax.swing.JTextField followWeightOfClassTextField;
    private javax.swing.JTextField followWeightOfInstanceTextField;
    private javax.swing.JPanel followWeightTypesTablePanel;
    private javax.swing.JScrollPane followWeightTypesTableScrollPane;
    private javax.swing.JPanel followWeightsButtonPanel;
    private javax.swing.JPanel followWeightsPanel;
    private javax.swing.JPanel followWeightsPanelInner;
    private javax.swing.JCheckBox includeAssociationsCheckBox;
    private javax.swing.JPanel includeAssociationsPanel;
    private javax.swing.JCheckBox includeClassesCheckBox;
    private javax.swing.JPanel includeClassesPanel;
    private javax.swing.JCheckBox includeInstancesCheckBox;
    private javax.swing.JPanel includeInstancesPanel;
    private javax.swing.JCheckBox includeOccurrencesCheckBox;
    private javax.swing.JPanel includeOccurrencesPanel;
    private javax.swing.JLabel initialPartialWeightLabel;
    private javax.swing.JPanel initialPartialWeightPanel;
    private javax.swing.JTextField initialPartialWeightTextField;
    private javax.swing.JLabel introductionLabel;
    private javax.swing.JPanel introductionPanel;
    private javax.swing.JPanel introductionPanelInner;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JToggleButton normalizeButton;
    private javax.swing.JTextField partialWeightOfClassTextField;
    private javax.swing.JTextField partialWeightOfinstanceTextField;
    private javax.swing.JLabel partialWeightOptionsLabel;
    private javax.swing.JPanel partialWeightOptionsPanel;
    private javax.swing.JPanel partialWeightOptionsPanelInner;
    private javax.swing.JPanel partialWeightTypesButtonPanel;
    private javax.swing.JPanel partialWeightTypesPanelInner;
    private javax.swing.JPanel partialWeightTypesTablePanel;
    private javax.swing.JScrollPane partialWeightTypesTableScrollPane;
    private javax.swing.JPanel partialWeightsPanel;
    private javax.swing.JButton removeFollowWeightTypeButton;
    private javax.swing.JButton removePartialWeightTypeButton;
    private javax.swing.JPanel resultsButtonPanel;
    private javax.swing.JLabel resultsInfoLabel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JPanel resultsPanelInner;
    private javax.swing.JPanel resultsTablePanel;
    private javax.swing.JScrollPane resultsTableScrollPane;
    // End of variables declaration//GEN-END:variables





    public void calculateAndUpdate() {
        try {
            this.calculate();
            if(normalizeButton.isSelected()) {
                resultsTable.refreshModel(normalizedTotalTopicWeights);
                updateResultsInfo(averageNormalizedWeight);
            }
            else {
                resultsTable.refreshModel(totalTopicWeights);
                updateResultsInfo(averageWeight);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }



    // -------------------------------------------------------------------------



    private double initialPartialWeightOfATopic = 0;

    private double defaultPartialWeightOfAssociation = 1;
    private double defaultPartialWeightOfOccurrence = 1;
    private double partialWeightOfClass = 1;
    private double partialWeightOfInstance = 1;

    private boolean includeAllOccurrences = true;
    private boolean includeAllAssociations = true;
    private boolean includeClasses = true;
    private boolean includeInstances = true;

    private double defaultFollowWeightOfAssociation = 1;
    private double followWeightOfClass = 1;
    private double followWeightOfInstance = 1;
    private double coefficient = 0.5;

    private boolean followAllAssociations = true;
    private boolean followClasses = true;
    private boolean followInstances = true;
    private boolean followNaryAssociations = true;

    private double averageWeight = 0;
    private double averageNormalizedWeight = 0;
    

    public void calculate() throws TopicMapException {

        initialPartialWeightOfATopic = getInitialPartialWeight();

        includeAllOccurrences = includeOccurrencesCheckBox.isSelected();
        includeAllAssociations = includeAssociationsCheckBox.isSelected();
        includeInstances = includeInstancesCheckBox.isSelected();
        includeClasses = includeClassesCheckBox.isSelected();

        partialWeightOfClass = getPartialWeightOfClass();
        partialWeightOfInstance = getPartialWeightOfInstance();
        defaultPartialWeightOfOccurrence = getDefaultPartialWeightOfOccurrence();
        defaultPartialWeightOfAssociation = getDefaultPartialWeightOfAssociation();

        
        followAllAssociations = followAllAssociationsCheckBox.isSelected();
        followInstances = followInstancesCheckBox.isSelected();
        followClasses = followClassesCheckBox.isSelected();
        followNaryAssociations = followNaryAssociationsCheckBox.isSelected();

        defaultFollowWeightOfAssociation = getDefaultFollowWeightOfAssociation();
        followWeightOfClass = getFollowWeightOfClass();
        followWeightOfInstance = getFollowWeightOfInstance();
        coefficient = getCoefficient();

        partialWeights = new LinkedHashMap<Topic, Double>();
        neighborhoodWeights = new LinkedHashMap<Topic, Double>();
        totalTopicWeights = new LinkedHashMap<Topic, Double>();
        normalizedTotalTopicWeights = new LinkedHashMap<Topic, Double>();

        double biggestWeight = -1;
        double weight = 0;
        double partialWeight = 0;
        double normalizedWeight = 0;
        double neighborhoodWeight = 0;
        double totalWeight = 0;
        double sumWeight = 0;
        int numberOfWeights = 0;

        // ****** CALCULATE WEIGHTS ******
        for( Topic topic : topics ) {
            partialWeight = getPartialWeight(topic);
            neighborhoodWeight = getNeighborhoodWeight(topic, tm);
            totalWeight = partialWeight + coefficient * neighborhoodWeight;
            if(biggestWeight < totalWeight) biggestWeight = totalWeight;
            totalTopicWeights.put(topic, totalWeight);
            sumWeight = sumWeight + totalWeight;
            numberOfWeights++;
        }
        averageWeight = sumWeight / numberOfWeights;

        // ****** NORMALIZE ******
        weight = 0;
        sumWeight = 0;
        normalizedWeight = 0;
        for(Topic t : totalTopicWeights.keySet()) {
            weight = totalTopicWeights.get(t);
            normalizedWeight = weight / biggestWeight;
            normalizedTotalTopicWeights.put(t, normalizedWeight);
            sumWeight = sumWeight + normalizedWeight;
        }
        averageNormalizedWeight = sumWeight / numberOfWeights;
    }



    // ------------------------------------------------ NEIGHBORHOOD WEIGHT ----




    private double getNeighborhoodWeight(Topic topic, TopicMap tm) throws TopicMapException {
        if(neighborhoodWeights.containsKey(topic)) {
            return neighborhoodWeights.get(topic);
        }
        else {
            double weight = calculateNeighborhoodWeight(topic, tm);
            neighborhoodWeights.put(topic, weight);
            return weight;
        }
    }

    

    private double calculateNeighborhoodWeight(Topic topic, TopicMap tm) throws TopicMapException {
        double neighbourhoodWeight = 0;
        double weight = 0;

        Collection<Association> associations = topic.getAssociations();
        for(Association a : associations) {
            Topic associationType = a.getType();
            Collection<Topic> roles = a.getRoles();
            if(followNaryAssociations || roles.size() == 2) {
                boolean topicFound = false;
                for(Topic role : roles) {
                    Topic player = a.getPlayer(role);
                    if(player.mergesWithTopic(topic) && !topicFound) {
                        topicFound = true;
                        continue;
                    }
                    weight = getFollowWeightForType(associationType) * getPartialWeight(player);
                    neighbourhoodWeight = neighbourhoodWeight + weight;
                }
            }
        }

        if(followClasses) {
            for(Topic type : topic.getTypes()) {
                weight = followWeightOfClass * getPartialWeight(type);
                neighbourhoodWeight = neighbourhoodWeight + weight;
            }
        }
        if(followInstances) {
            for(Topic instance : tm.getTopicsOfType(topic)) {
                weight = followWeightOfInstance * getPartialWeight(instance);
                neighbourhoodWeight = neighbourhoodWeight + weight;
            }
        }

        return neighbourhoodWeight;
    }




    private double getFollowWeightForType(Topic type) {
        if(followWeightTypes != null) {
            if(followWeightTypes.containsKey(type)) {
                return followWeightTypes.get(type);
            }
        }
        if(followAllAssociations) {
            return defaultFollowWeightOfAssociation;
        }
        else {
            return 0;
        }
    }




    // ----------------------------------------------------- PARTIAL WEIGHT ----

    

    private double getPartialWeight(Topic topic) throws TopicMapException {
        double partialWeight = 0;
        if(partialWeights.containsKey(topic)) {
            partialWeight = partialWeights.get(topic);
        }
        else {
            partialWeight = calculatePartialWeight(topic);
            partialWeights.put(topic, partialWeight);
        }
        return partialWeight;
    }





    private double calculatePartialWeight(Topic topic) throws TopicMapException {
        double weight = initialPartialWeightOfATopic;
        double associationWeight = 0;
        double occurrenceWeight = 0;

        Topic associationType;
        Collection<Association> associations = topic.getAssociations();
        for(Association association : associations) {
            associationType = association.getType();
            associationWeight = getPartialWeightForAssociationType(associationType);
            weight = weight + associationWeight;
        }

        Collection<Topic> occurrenceTypes = topic.getDataTypes();
        for(Topic occurrenceType : occurrenceTypes) {
            occurrenceWeight = getPartialWeightForOccurrenceType(occurrenceType);
            weight = weight + occurrenceWeight;
        }

        if(includeClasses) {
            Collection<Topic> classes = topic.getTypes();
            if(classes != null && !classes.isEmpty()) {
                weight = weight + (partialWeightOfClass * classes.size());
            }
        }

        if(includeInstances) {
            Collection<Topic> instances = tm.getTopicsOfType(topic);
            if(instances != null && !instances.isEmpty()) {
                weight = weight + (partialWeightOfInstance * instances.size());
            }
        }

        return weight;
    }




    
    private double getPartialWeightForAssociationType(Topic type) {
        if(partialWeightTypes != null) {
            if(partialWeightTypes.containsKey(type)) {
                return partialWeightTypes.get(type);
            }
        }
        if(includeAllAssociations) {
            return defaultPartialWeightOfAssociation;
        }
        else {
            return 0;
        }
    }



    private double getPartialWeightForOccurrenceType(Topic type) {
        if(partialWeightTypes != null) {
            if(partialWeightTypes.containsKey(type)) {
                return partialWeightTypes.get(type);
            }
        }
        if(includeAllOccurrences) {
            return defaultPartialWeightOfOccurrence;
        }
        else {
            return 0;
        }
    }




    // ---- GET DATA OUT OF TEXT FIELDS ----




    public double getInitialPartialWeight() {
        try {
            return Double.parseDouble(initialPartialWeightTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return initialPartialWeightOfATopic;
    }



    public double getPartialWeightOfClass() {
        try {
            return Double.parseDouble(partialWeightOfClassTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return partialWeightOfClass;
    }


    public double getPartialWeightOfInstance() {
        try {
            return Double.parseDouble(partialWeightOfinstanceTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return partialWeightOfInstance;
    }


    public double getDefaultPartialWeightOfAssociation() {
        try {
            return Double.parseDouble(defaultPartialWeightOfAssociationTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return defaultPartialWeightOfAssociation;
    }



    public double getDefaultPartialWeightOfOccurrence() {
        try {
            return Double.parseDouble(defaultPartialWeightOfOccurrenceTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return defaultPartialWeightOfOccurrence;
    }




    // ----


    public double getDefaultFollowWeightOfAssociation() {
        try {
            return Double.parseDouble(defaultFollowWeightOfAssociationTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return defaultFollowWeightOfAssociation;
    }



    public double getFollowWeightOfClass() {
        try {
            return Double.parseDouble(followWeightOfClassTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return followWeightOfClass;
    }


    public double getFollowWeightOfInstance() {
        try {
            return Double.parseDouble(followWeightOfInstanceTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return followWeightOfInstance;
    }



    public double getCoefficient() {
        try {
            return Double.parseDouble(coefficientTextField.getText());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return coefficient;
    }






    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    class TopicWeightTable extends JTable {

		private static final long serialVersionUID = 1L;

		private HashMap<Topic,Double> modelData = null;
        private TopicWeightTableModel dm = null;
        private String name1 = "Type";
        private String name2 = "Weight";

        

        public TopicWeightTable(HashMap<Topic,Double> typeWeights) {
            modelData = typeWeights;
            dm = new TopicWeightTableModel(modelData);
            this.setModel(dm);
            this.setRowSorter(new TopicWeightTableRowSorter(dm));
        }



        public void addTopicWeight(Topic t, double weight) {
            modelData.put(t, weight);
            refreshModel();
        }


        public void removeTopicWeight(Topic t) {
            modelData.remove(t);
            refreshModel();
        }

        public void refreshModel(HashMap<Topic,Double> weights) {
            modelData = weights;
            dm = new TopicWeightTableModel(modelData);
            dm.setColumnNames(name1, name2);
            this.setModel(dm);
            this.setRowSorter(new TopicWeightTableRowSorter(dm));
        }

        public void refreshModel() {
            dm = new TopicWeightTableModel(modelData);
            dm.setColumnNames(name1, name2);
            this.setModel(dm);
            this.setRowSorter(new TopicWeightTableRowSorter(dm));
        }

        public void setColumnNames(String n1, String n2) {
            name1 = n1;
            name2 = n2;
            dm.setColumnNames(n1, n2);
        }

        public void deleteSelectedRows() {
            int[] selectedRows = getSelectedRows();
            Topic t = null;
            for(int i=0; i<selectedRows.length; i++) {
                int row = convertRowIndexToModel(selectedRows[i]);
                t = dm.getTopicAt(row);
                if(t != null) {
                    modelData.remove(t);
                }
            }
            refreshModel();
        }




        public String copySelectedOrAllRows() {
            StringBuilder sb = new StringBuilder("");
            int[] selectedRows = getSelectedRows();
            if(selectedRows == null || selectedRows.length == 0) {
                selectedRows = new int[this.getRowCount()];
                for(int i=0; i<this.getRowCount(); i++)
                selectedRows[i] = i;
            }
            Topic t = null;
            for(int i=0; i<selectedRows.length; i++) {
                int row = convertRowIndexToModel(selectedRows[i]);
                t = dm.getTopicAt(row);
                if(t != null) {
                    try {
                        String n = t.getBaseName();
                        if(n == null) n = t.getOneSubjectIdentifier().toExternalForm();
                        sb.append(n);
                        sb.append("\t");
                        sb.append(dm.getWeightAt(row));
                        sb.append("\n");
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }
    }




    class TopicWeightTableModel extends DefaultTableModel {
        private ArrayList<Topic> topics = null;
        private ArrayList<Double> weights = null;
        private HashMap<Topic,Double> topicWeights = null;

        private String columnName0 = "Type";
        private String columnName1 = "Weight";



        public TopicWeightTableModel(HashMap<Topic,Double> typeWeights) {
            topics = new ArrayList<Topic>();
            weights = new ArrayList<Double>();
            topicWeights = typeWeights;
            for(Topic topic : typeWeights.keySet()) {
                topics.add(topic);
                weights.add(typeWeights.get(topic));
            }
        }

        public Topic getTopicAt(int row) {
            if(row >= 0 && row < topics.size())
                return topics.get(row);
            else
                return null;
        }
        public double getWeightAt(int row) {
            if(row >= 0 && row < weights.size())
                return weights.get(row);
            else
                return -1;
        }



        @Override
        public void setValueAt(Object obj, int rowIndex, int columnIndex) {
            try {
                if(obj == null) return;
                String data = obj.toString();
                data = data.trim();
                if(rowIndex >= 0 && rowIndex < topics.size()) {
                    switch(columnIndex) {
                        case 0: {
                            // CANT EDIT
                            break;
                        }
                        case 1: {
                            double weight = Double.parseDouble(data);
                            weights.set(rowIndex, weight);
                            topicWeights.put(topics.get(rowIndex), weight);
                            break;
                        }
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }



        @Override
        public int getColumnCount() {
            return 2;
        }



        @Override
        public int getRowCount() {
            if(topics == null) return 0;
            return topics.size();
        }



        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                if(rowIndex >= 0 && rowIndex < topics.size()) {
                    switch(columnIndex) {
                        case 0: {
                            Topic t = topics.get(rowIndex);
                            String n = t.getBaseName();
                            if(n == null) n = t.getOneSubjectIdentifier().toExternalForm();
                            return n;
                        }
                        case 1: {
                            Double weight = weights.get(rowIndex);
                            return (weight == null ? "[not a number]" : weight);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }




        public void setColumnNames(String n1, String n2) {
            columnName0 = n1;
            columnName1 = n2;
        }



        @Override
        public String getColumnName(int columnIndex){
            try {
                switch(columnIndex) {
                    case 0: {
                        return columnName0;
                    }
                    case 1: {
                        return columnName1;
                    }
                }
                return "";
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return "ERROR";
        }




        @Override
        public boolean isCellEditable(int row, int col) {
            if(col == 0) return false;
            return true;
        }
    }





    class TopicWeightTableRowSorter extends TableRowSorter {


        public TopicWeightTableRowSorter(TopicWeightTableModel dm) {
            super(dm);
        }

        @Override
        public Comparator<?> getComparator(int column) {
            if(column == 1) {
                return new Comparator() {
                    public int compare(Object o1, Object o2) {
                        if(o1 == null || o2 == null) return 0;
                        if(o1 instanceof Double && o2 instanceof Double) {
                            return ((Double) o1).compareTo(((Double) o2));
                        }
                        else if(o1 instanceof String && o2 instanceof String) {
                            try {
                                double d1 = Double.parseDouble((String)o1);
                                double d2 = Double.parseDouble((String)o2);
                                if(d1 > d2) return 1;
                                if(d1 < d2) return -1;
                            }
                            catch(Exception e) {}
                            return 0;
                        }
                        else {
                            return 0;
                        }
                    }
                };
            }
            else  {
                return super.getComparator(column);
            }
        }

    }




}
