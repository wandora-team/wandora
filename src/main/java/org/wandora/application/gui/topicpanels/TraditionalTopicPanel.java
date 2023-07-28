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
 * TraditionalTopicPanel.java
 *
 * Created on August 9, 2004, 2:33 PM
 */



package org.wandora.application.gui.topicpanels;



import org.wandora.application.gui.topicpanels.traditional.AbstractTraditionalTopicPanel;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.previews.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleToggleButton;
import org.wandora.application.gui.simple.SimpleURIField;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.navigate.OpenTopic;


/**
 *
 * @author  olli, akivela
 */


public class TraditionalTopicPanel extends AbstractTraditionalTopicPanel implements ActionListener, TopicPanel {

    public static final boolean MAKE_LOCAL_SETTINGS_GLOBAL = false;

    public Topic topic;
    public String topicSI;
    
    private Wandora wandora;
    protected Options options;
    private Object[][] panelStruct;
    
    private String originalBN;
    private String originalSL;
    
    private JComponent buttonContainer = null;
    private PreviewWrapper previewWrapper = null;

    
    
    /** Creates new form TraditionalTopicPanel */
    public TraditionalTopicPanel(Topic topic, Wandora wandora) {
        open(topic);
        this.setTransferHandler(new TopicPanelTransferHandler(this));
    }
    
    
    public TraditionalTopicPanel() {
        this.options = new Options(Wandora.getWandora().getOptions());
        this.setTransferHandler(new TopicPanelTransferHandler(this));
    }
    
    
    @Override
    public void init() {
        this.wandora = Wandora.getWandora();
        if(this.options == null) {
            // Notice, a copy of global options is created for local use.
            // Thus, local adjustments have no global effect.
            this.options = new Options(wandora.getOptions());
        }
        try {
            if(options != null) {
                variantGUIType = options.get(VARIANT_GUITYPE_OPTIONS_KEY);
                if(variantGUIType == null || variantGUIType.length() == 0) {
                    variantGUIType = VARIANT_GUITYPE_SCHEMA;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Object[] buttonStruct = {
            "Open topic",
            new OpenTopic(OpenTopic.ASK_USER),
            
            "New topic",
            UIBox.getIcon("gui/icons/new_topic.png"),
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NewTopicPanelExtended newTopicPanel = new NewTopicPanelExtended(null); // TODO: Should pass a context to the constructor.
                    if(newTopicPanel.getAccepted()) {
                        try {
                            Topic newTopic = newTopicPanel.createTopic();
                            if(newTopic != null) {
                                Wandora.getWandora().openTopic(newTopic);
                            }
                        }
                        catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            },
        };
        buttonContainer = UIBox.makeButtonContainer(buttonStruct, Wandora.getWandora());
    }
    
    
    @Override
    public void open(Topic topic) {       
        this.topic = topic;
        this.topicSI = null;
        
        try {
            if(topic != null && !topic.isRemoved()) {
                this.topicSI = topic.getOneSubjectIdentifier().toExternalForm();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        this.removeAll();
        initComponents();
        previewWrapper = (PreviewWrapper) previewPanel;
        buttonWrapperPanel.add(buttonContainer);
        
        panelStruct = new Object[][] {
            { variantRootPanel,             "View names",           "variantRootPanel"  },
            { classesRootPanel,             "View classes",         "classesRootPanel" },
            { occurrencesRootPanel,         "View occurrences",         "occurrencesRootPanel"},
            { associationRootPanel,         "View associations",    "associationRootPanel" },
            { typedAssociationsRootPanel,   "View associations where type", "typedAssociationsRootPanel" },
            { instancesRootPanel,           "View instances",       "instancesRootPanel"  },
            { previewPanel,                 "View subject locators", "previewPanel" },
        };

        associationRootPanel.setTransferHandler(new AssociationTableTransferHandler(this));
        instancesRootPanel.setTransferHandler(new InstancesPanelTransferHandler(this));
        classesRootPanel.setTransferHandler(new ClassesPanelTransferHandler(this));
        occurrencesRootPanel.setTransferHandler(new OccurrencesPanelTransferHandler(this));

        refresh();
    }
   
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_traditional.png");
    }
    
    
    @Override
    public String getName() {
        return "Traditional";
    }

    @Override
    public String getTitle() {
        if(topic != null) return TopicToString.toString(topic);
        else return "";
    }
    
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    
    @Override
    public Topic getTopic() {
        try {
            if((topic == null || topic.isRemoved()) && topicSI != null) {
                topic = wandora.getTopicMap().getTopic(topicSI);
            }
            return topic;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }
    
    
    
    @Override
    public JPanel getGui() {
        return this;
    }

    @Override
    public boolean noScroll(){
        return false;
    }
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------- VIEW MENU ---
    // -------------------------------------------------------------------------
    
    

    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }
    @Override
    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }
    
    
    
    @Override
    public Object[] getViewMenuStruct() {
        Icon viewIcon = UIBox.getIcon("gui/icons/view2.png");
        Icon hideIcon = UIBox.getIcon("gui/icons/view2_no.png");

        ArrayList menuVector = new ArrayList();
        for(int i=0; i<panelStruct.length; i++) {
            Object[] panelData = panelStruct[i];
            String panelName = ((JPanel) panelData[0]).getName();
            menuVector.add( panelData[1] );
            menuVector.add( options.isFalse(OPTIONS_VIEW_PREFIX + panelName) ? hideIcon : viewIcon );
            menuVector.add( this );
        }
        menuVector.add( "---" );
        menuVector.add( "View all" );
        menuVector.add( this );
        menuVector.add( "Hide all" );
        menuVector.add( this );
               
        return menuVector.toArray();
    }
    

    // -------------------------------------------------------------------------
    // --------------------------------------------------- TOGGLE VISIBILITY ---
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void toggleVisibility(String componentName) {
        boolean iDidIt = false;
        try {
            refresh();
        }
        catch(Exception e) {
            return;
        }
        
        if("View All".equalsIgnoreCase(componentName)) {
            JPanel panel;
            for (Object[] panelStruct1 : panelStruct) {
                panel = (JPanel) panelStruct1[0];
                setVisibitilityOption(panelStruct1[2].toString(), true);
                //panel.setVisible(true);
            }
            setVisibitilityOption("subjectLocatorIcons", true);
            iDidIt = true;
        }
        
        if("Hide All".equalsIgnoreCase(componentName)) {
            JPanel panel;
            for (Object[] panelStruct1 : panelStruct) {
                panel = (JPanel) panelStruct1[0];
                setVisibitilityOption(panelStruct1[2].toString(), false);
                //panel.setVisible(false);
            }
            setVisibitilityOption("subjectLocatorIcons", false);
            iDidIt = true;
        }

        else {
            int panelIndex = solvePanelIndex(componentName);
            if(panelIndex >= 0) {
                String optionName = panelStruct[panelIndex][2].toString();
                JPanel panel = (JPanel) panelStruct[panelIndex][0];
                toggleVisibility(panel, optionName);
                iDidIt = true;
            }
        }
        
        if(iDidIt) {
            // refresh();
            wandora.topicPanelsChanged();
        }
    }
    
    
    
    private int solvePanelIndex(String componentName) {
        if(componentName == null) return -1;
        for(int i=0; i<panelStruct.length; i++) {
            if(componentName.equals(panelStruct[i][1])) return i;
        }
        return -1;
    }
    
    
    private void toggleVisibility(Component component, String optionName) {
        boolean currentValue = options.isFalse(OPTIONS_VIEW_PREFIX + optionName);
        //System.out.println("currentValue==" + currentValue);
        setVisibitilityOption(optionName, currentValue);
        if(component != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        refresh();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
    
    private void setVisibitilityOption(String key, boolean value) {
        options.put(OPTIONS_VIEW_PREFIX + key, value ? "true" : "false");
        if(MAKE_LOCAL_SETTINGS_GLOBAL) {
            Options wandoraOptions = wandora.getOptions();
            if(wandoraOptions != null) {
                wandoraOptions.put(OPTIONS_VIEW_PREFIX + key, value ? "true" : "false");
            }
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    

    
    
    @Override
    public void refresh() {
        //Thread.dumpStack();

        try {
            if(topicSI != null) {
                topic = wandora.getTopicMap().getTopic(topicSI);
            }
            if(topic==null || topic.isRemoved()) {
                //System.out.println("Topic is null or removed!");
                containerPanel.setVisible(false);
                removedTopicMessage.setVisible(true);
                return;
            }
            else {
                containerPanel.setVisible(true);
                removedTopicMessage.setVisible(false);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            //System.out.println("Topic is null or removed!");
            containerPanel.setVisible(false);
            removedTopicMessage.setVisible(true);
            return;
        }
        
        //System.out.println("refresh");
        super.refresh();
        
        // First update visibility of panels!!
        for(int i=0; i<panelStruct.length; i++) {
            JPanel panel = (JPanel) panelStruct[i][0];
            String panelOptionName = (String) panelStruct[i][2];
            panel.setVisible( !options.isFalse(OPTIONS_VIEW_PREFIX + panelOptionName) );
        }

        try {
            boolean viewPreview = options.getBoolean(OPTIONS_VIEW_PREFIX+"previewPanel", false);
            subjectLocatorViewButton.setSelected(viewPreview);
            if(viewPreview && previewWrapper != null) {
                previewWrapper.setURL(topic.getSubjectLocator());
            }
            else {
                previewWrapper.stop();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
            
        if(associationRootPanel.isVisible())        buildAssociationsPanel(associationPanel, associationsNumber, topic, ASSOCIATIONS_WHERE_PLAYER, options, wandora);
        if(typedAssociationsRootPanel.isVisible())  buildAssociationsPanel(typedAssociationsPanel, typedAssociationNumber, topic, ASSOCIATIONS_WHERE_TYPE, options, wandora);
        if(classesRootPanel.isVisible())            buildClassesPanel(classesPanel, classesNumber, topic, options, wandora);
        if(subjectIdentifierRootPanel.isVisible())  buildSubjectIdentifierPanel(subjectIdentifierPanel, topic, options, wandora);
        if(instancesRootPanel.isVisible())          buildInstancesPanel(instancesPanel, instancesNumber, topic, options, wandora);
         

        variantGUIType = options.get(VARIANT_GUITYPE_OPTIONS_KEY);
        if(variantGUIType == null || variantGUIType.length() == 0) variantGUIType = VARIANT_GUITYPE_SCHEMA;
        if(VARIANT_GUITYPE_SCHEMA.equalsIgnoreCase(variantGUIType)) {
            if("vertical".equals(options.get(OPTIONS_PREFIX + "namePanelOrientation"))) {
                buildVerticalNamePanel(variantPanel, variantNumber, topic, this, options, wandora);
            }
            else {
                buildHorizontalNamePanel(variantPanel, variantNumber, topic, this, options, wandora);
            }
        }
        else {
            buildAllNamesPanel(variantPanel, variantNumber, topic, this, options, wandora);
        }
        
        
        // Always create occurrences panel as data in the panel is updated to topic map
        // in apply changes. See applyChanges below.
        buildOccurrencesPanel(dataPanel, dataNumber, topic, options, wandora);

        try {
            if(topic.getBaseName()!=null) {
                baseNameField.setText(topic.getBaseName());
                baseNameField.setCaretPosition(0);
                Color c=wandora.topicHilights.getBaseNameColor(topic);
                if(c!=null) baseNameField.setForeground(c);
                else baseNameField.setForeground(Color.BLACK);
            }
            else {
                baseNameField.setText("");
                baseNameField.setForeground(Color.BLACK);
            }
            originalBN=topic.getBaseName();
            if(topic.getSubjectLocator()!=null) {
                subjectLocatorField.setText(topic.getSubjectLocator().toString());
                subjectLocatorField.setCaretPosition(0);
                Color c=wandora.topicHilights.getSubjectLocatorColor(topic);
                if(c!=null) subjectLocatorField.setForeground(c);
                else subjectLocatorField.setForeground(Color.BLACK);
                originalSL=topic.getSubjectLocator().toString();
            }
            else {
                subjectLocatorField.setText("");
                subjectLocatorField.setForeground(Color.BLACK);
                originalSL=null;
            }
        }
        catch(Exception e) {
            System.out.println("Failed to initialize base or/and sl!");
            e.printStackTrace();
        }
        this.setComponentPopupMenu(getViewPopupMenu());
        variantRootPanel.setComponentPopupMenu(getNamesMenu());
        occurrencesRootPanel.setComponentPopupMenu(getOccurrencesMenu());
        
        this.revalidate();
        this.repaint();
        //System.out.println("refresh-end");
    }
    
    
    
    
    
    
    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        //System.out.println("applyChanges");
        
        // First check if user has somehow removed current topic. 
        // Then there is no need for apply changes!
        try {
            if(topic==null || topic.isRemoved()) {
                return false;
            }
        }
        catch(Exception e){
            return false;
        }
        
        // Ok, topic is still around and we have to check if it has changed.
        
        boolean changed=false;
        
        String fieldTextSL = subjectLocatorField.getText().trim();
        String origSL = originalSL;
        
        // ------- basenames -------
        final String fieldTextBN = baseNameField.getText().trim();
        String origBN = originalBN;
        if((origBN==null && fieldTextBN.length()>0) || (origBN!=null && !origBN.equals(fieldTextBN))) {
            if(TMBox.checkBaseNameChange(wandora, topic, fieldTextBN) != ConfirmResult.yes) {
                baseNameField.setText(origBN);
                throw new CancelledException();
            }
            if(fieldTextBN.length() > 0) {
                System.out.println("set basename to "+fieldTextBN);
                topic.setBaseName(fieldTextBN);
            }
            else {
                topic.setBaseName(null);
            }
            changed=true;
        }
        
        // ------ subject locator -------
        if((origSL==null && fieldTextSL.length()>0) || (origSL!=null && !origSL.equals(fieldTextSL))) {
            if(TMBox.checkSubjectLocatorChange(wandora,topic,fieldTextSL)!=ConfirmResult.yes) {
                subjectLocatorField.setText(originalSL);
                throw new CancelledException();
            }
            if(origSL!=null && !origSL.equals(fieldTextSL)) {
                topic.setSubjectLocator(null);
            }
            if(fieldTextSL.length()>0) {
                topic.setSubjectLocator(topic.getTopicMap().createLocator(fieldTextSL));
            }
            changed=true;
        }

        // ------ everything else -------
        changed = super.applyChanges(topic, wandora) | changed;


        if(changed) {
            wandora.doRefresh();
        }
        //System.out.println("applyChanges-end");
        return changed;
    }
    
    
    
    
    @Override
    public void stop() {
        if(previewWrapper != null) {
            previewWrapper.stop();
        }
        PreviewWrapper.removePreviewWrapper(this);
    }
        
    
    
    
    
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        containerPanel = new javax.swing.JPanel();
        previewContainerPanel = new javax.swing.JPanel();
        previewPanel = PreviewWrapper.getPreviewWrapper(this);
        idPanel = new javax.swing.JPanel();
        baseNameLabel = new org.wandora.application.gui.simple.SimpleLabel();
        baseNameField = new SimpleField();
        subjectLocatorLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectLocatorViewButton = new SimpleToggleButton("gui/icons/view2.png","gui/icons/view2_no.png",false);
        subjectLocatorField = new SimpleURIField();
        subjectIdentifierLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectIdentifierRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        subjectIdentifierPanel = new javax.swing.JPanel();
        variantRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        variantWrapperPanel = new javax.swing.JPanel();
        variantTitlePanel = new javax.swing.JPanel();
        variantTitle = new javax.swing.JLabel();
        variantNumber = new javax.swing.JLabel();
        variantTitleFiller = new javax.swing.JPanel();
        variantPanelMargin = new javax.swing.JPanel();
        variantPanel = new javax.swing.JPanel();
        occurrencesRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        occurrencesWrapperPanel = new javax.swing.JPanel();
        dataTitlePanel = new javax.swing.JPanel();
        dataTitle = new javax.swing.JLabel();
        dataNumber = new javax.swing.JLabel();
        dataTitleFiller = new javax.swing.JPanel();
        dataPanelMargin = new javax.swing.JPanel();
        dataPanel = new javax.swing.JPanel();
        classesRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        classesWrapperPanel = new javax.swing.JPanel();
        classesTitlePanel = new javax.swing.JPanel();
        classesTitle = new javax.swing.JLabel();
        classesNumber = new javax.swing.JLabel();
        classesTitleFiller = new javax.swing.JPanel();
        classesPanelMargin = new javax.swing.JPanel();
        classesPanel = new javax.swing.JPanel();
        associationRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        associationWrapperPanel = new javax.swing.JPanel();
        associationsTitlePanel = new javax.swing.JPanel();
        associationsTitle = new javax.swing.JLabel();
        associationsNumber = new javax.swing.JLabel();
        associationsTitleFiller = new javax.swing.JPanel();
        associationPanelMargin = new javax.swing.JPanel();
        associationPanel = new javax.swing.JPanel();
        typedAssociationsRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        typedAssociationsWrapperPanel = new javax.swing.JPanel();
        typedAssociationsTitlePanel = new javax.swing.JPanel();
        typedAssociationTitle = new javax.swing.JLabel();
        typedAssociationNumber = new javax.swing.JLabel();
        typedAssociationTitleFiller = new javax.swing.JPanel();
        typesAssociationPanelMargin = new javax.swing.JPanel();
        typedAssociationsPanel = new javax.swing.JPanel();
        instancesRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        instancesWrapperPanel = new javax.swing.JPanel();
        instancesTitlePanel = new javax.swing.JPanel();
        instancesTitle = new javax.swing.JLabel();
        instancesNumber = new javax.swing.JLabel();
        instancesTitleFiller = new javax.swing.JPanel();
        instancesPanelMargin = new javax.swing.JPanel();
        instancesPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        removedTopicMessage = new javax.swing.JPanel();
        removedTopicMessageLabel = new SimpleLabel();
        buttonWrapperPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        containerPanel.setLayout(new java.awt.GridBagLayout());

        previewContainerPanel.setLayout(new java.awt.GridBagLayout());

        previewPanel.setToolTipText("");
        previewPanel.setName("previewPanel"); // NOI18N
        previewPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        previewContainerPanel.add(previewPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        containerPanel.add(previewContainerPanel, gridBagConstraints);

        idPanel.setLayout(new java.awt.GridBagLayout());

        baseNameLabel.setForeground(new java.awt.Color(51, 51, 51));
        baseNameLabel.setText("Base name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        idPanel.add(baseNameLabel, gridBagConstraints);

        baseNameField.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        baseNameField.setForeground(new java.awt.Color(33, 33, 33));
        baseNameField.setMargin(new java.awt.Insets(0, 0, 0, 0));
        baseNameField.setPreferredSize(new java.awt.Dimension(6, 29));
        baseNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                baseNameFieldFocusLost(evt);
            }
        });
        baseNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                baseNameFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        idPanel.add(baseNameField, gridBagConstraints);

        subjectLocatorLabel.setForeground(new java.awt.Color(51, 51, 51));
        subjectLocatorLabel.setText("Subject locator");
        subjectLocatorLabel.setComponentPopupMenu(getSLMenu());
        subjectLocatorLabel.addMouseListener(wandora);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        idPanel.add(subjectLocatorLabel, gridBagConstraints);

        subjectLocatorViewButton.setToolTipText("View subject locator resource");
        subjectLocatorViewButton.setBorderPainted(false);
        subjectLocatorViewButton.setContentAreaFilled(false);
        subjectLocatorViewButton.setMargin(new java.awt.Insets(2, 6, 2, 2));
        subjectLocatorViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subjectLocatorViewButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        idPanel.add(subjectLocatorViewButton, gridBagConstraints);

        subjectLocatorField.setFont(new java.awt.Font("SansSerif", 0, 18)); // NOI18N
        subjectLocatorField.setForeground(new java.awt.Color(33, 33, 33));
        subjectLocatorField.setMargin(new java.awt.Insets(0, 0, 0, 0));
        subjectLocatorField.setPreferredSize(new java.awt.Dimension(6, 29));
        subjectLocatorField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                subjectLocatorFieldFocusLost(evt);
            }
        });
        subjectLocatorField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                subjectLocatorFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        idPanel.add(subjectLocatorField, gridBagConstraints);

        subjectIdentifierLabel.setForeground(new java.awt.Color(51, 51, 51));
        subjectIdentifierLabel.setText("Subject identifiers");
        subjectIdentifierLabel.setComponentPopupMenu(getSIMenu());
        subjectLocatorLabel.addMouseListener(wandora);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        idPanel.add(subjectIdentifierLabel, gridBagConstraints);

        subjectIdentifierRootPanel.setName("subjectIdentifierRootPanel"); // NOI18N
        subjectIdentifierRootPanel.setLayout(new java.awt.BorderLayout());

        subjectIdentifierPanel.setLayout(new java.awt.GridLayout(1, 0));
        subjectIdentifierRootPanel.add(subjectIdentifierPanel, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 1);
        idPanel.add(subjectIdentifierRootPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
        containerPanel.add(idPanel, gridBagConstraints);

        variantRootPanel.setComponentPopupMenu(getNamesMenu());
        variantRootPanel.setName("variantRootPanel"); // NOI18N
        variantRootPanel.addMouseListener(wandora);
        variantRootPanel.setLayout(new java.awt.BorderLayout());

        variantWrapperPanel.setLayout(new java.awt.GridBagLayout());

        variantTitlePanel.setLayout(new java.awt.GridBagLayout());

        variantTitle.setFont(UIConstants.h2Font);
        variantTitle.setText("Variant names");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        variantTitlePanel.add(variantTitle, gridBagConstraints);

        variantNumber.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        variantNumber.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        variantTitlePanel.add(variantNumber, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        variantTitlePanel.add(variantTitleFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        variantWrapperPanel.add(variantTitlePanel, gridBagConstraints);

        variantPanelMargin.setPreferredSize(new java.awt.Dimension(30, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        variantWrapperPanel.add(variantPanelMargin, gridBagConstraints);

        variantPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        variantWrapperPanel.add(variantPanel, gridBagConstraints);

        variantRootPanel.add(variantWrapperPanel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        containerPanel.add(variantRootPanel, gridBagConstraints);

        occurrencesRootPanel.setComponentPopupMenu(getOccurrencesMenu());
        occurrencesRootPanel.setName("occurrencesRootPanel"); // NOI18N
        occurrencesRootPanel.addMouseListener(wandora);
        occurrencesRootPanel.setLayout(new java.awt.BorderLayout());

        occurrencesWrapperPanel.setLayout(new java.awt.GridBagLayout());

        dataTitlePanel.setLayout(new java.awt.GridBagLayout());

        dataTitle.setFont(UIConstants.h2Font);
        dataTitle.setText("Occurrences");
        dataTitlePanel.add(dataTitle, new java.awt.GridBagConstraints());

        dataNumber.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        dataNumber.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        dataTitlePanel.add(dataNumber, gridBagConstraints);

        dataTitleFiller.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        dataTitlePanel.add(dataTitleFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        occurrencesWrapperPanel.add(dataTitlePanel, gridBagConstraints);

        dataPanelMargin.setPreferredSize(new java.awt.Dimension(30, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        occurrencesWrapperPanel.add(dataPanelMargin, gridBagConstraints);

        dataPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        occurrencesWrapperPanel.add(dataPanel, gridBagConstraints);

        occurrencesRootPanel.add(occurrencesWrapperPanel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        containerPanel.add(occurrencesRootPanel, gridBagConstraints);

        classesRootPanel.setComponentPopupMenu(getClassesMenu());
        classesRootPanel.setName("classesRootPanel"); // NOI18N
        classesRootPanel.addMouseListener(wandora);
        classesRootPanel.setLayout(new java.awt.BorderLayout());

        classesWrapperPanel.setLayout(new java.awt.GridBagLayout());

        classesTitlePanel.setLayout(new java.awt.GridBagLayout());

        classesTitle.setFont(UIConstants.h2Font);
        classesTitle.setText("Classes");
        classesTitlePanel.add(classesTitle, new java.awt.GridBagConstraints());

        classesNumber.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        classesNumber.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        classesTitlePanel.add(classesNumber, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        classesTitlePanel.add(classesTitleFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        classesWrapperPanel.add(classesTitlePanel, gridBagConstraints);

        classesPanelMargin.setPreferredSize(new java.awt.Dimension(30, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        classesWrapperPanel.add(classesPanelMargin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        classesWrapperPanel.add(classesPanel, gridBagConstraints);

        classesRootPanel.add(classesWrapperPanel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        containerPanel.add(classesRootPanel, gridBagConstraints);

        associationRootPanel.setComponentPopupMenu(getAssociationsMenu());
        associationRootPanel.setName("associationRootPanel"); // NOI18N
        associationRootPanel.addMouseListener(wandora);
        associationRootPanel.setLayout(new java.awt.BorderLayout());

        associationWrapperPanel.setLayout(new java.awt.GridBagLayout());

        associationsTitlePanel.setLayout(new java.awt.GridBagLayout());

        associationsTitle.setFont(UIConstants.h2Font);
        associationsTitle.setText("Associations");
        associationsTitlePanel.add(associationsTitle, new java.awt.GridBagConstraints());

        associationsNumber.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        associationsNumber.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        associationsTitlePanel.add(associationsNumber, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        associationsTitlePanel.add(associationsTitleFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        associationWrapperPanel.add(associationsTitlePanel, gridBagConstraints);

        associationPanelMargin.setPreferredSize(new java.awt.Dimension(30, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        associationWrapperPanel.add(associationPanelMargin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        associationWrapperPanel.add(associationPanel, gridBagConstraints);

        associationRootPanel.add(associationWrapperPanel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        containerPanel.add(associationRootPanel, gridBagConstraints);

        typedAssociationsRootPanel.setName("typedAssociationsRootPanel"); // NOI18N
        typedAssociationsRootPanel.setLayout(new java.awt.BorderLayout());

        typedAssociationsWrapperPanel.setLayout(new java.awt.GridBagLayout());

        typedAssociationsTitlePanel.setLayout(new java.awt.GridBagLayout());

        typedAssociationTitle.setFont(UIConstants.h2Font);
        typedAssociationTitle.setText("Associations where type");
        typedAssociationsTitlePanel.add(typedAssociationTitle, new java.awt.GridBagConstraints());

        typedAssociationNumber.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        typedAssociationNumber.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        typedAssociationsTitlePanel.add(typedAssociationNumber, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        typedAssociationsTitlePanel.add(typedAssociationTitleFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        typedAssociationsWrapperPanel.add(typedAssociationsTitlePanel, gridBagConstraints);

        typesAssociationPanelMargin.setPreferredSize(new java.awt.Dimension(30, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        typedAssociationsWrapperPanel.add(typesAssociationPanelMargin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        typedAssociationsWrapperPanel.add(typedAssociationsPanel, gridBagConstraints);

        typedAssociationsRootPanel.add(typedAssociationsWrapperPanel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        containerPanel.add(typedAssociationsRootPanel, gridBagConstraints);

        instancesRootPanel.setComponentPopupMenu(getInstancesMenu());
        instancesRootPanel.setName("instancesRootPanel"); // NOI18N
        instancesRootPanel.addMouseListener(wandora);
        instancesRootPanel.setLayout(new java.awt.BorderLayout());

        instancesWrapperPanel.setLayout(new java.awt.GridBagLayout());

        instancesTitlePanel.setLayout(new java.awt.GridBagLayout());

        instancesTitle.setFont(UIConstants.h2Font);
        instancesTitle.setText("Instances");
        instancesTitlePanel.add(instancesTitle, new java.awt.GridBagConstraints());

        instancesNumber.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        instancesNumber.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        instancesTitlePanel.add(instancesNumber, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        instancesTitlePanel.add(instancesTitleFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        instancesWrapperPanel.add(instancesTitlePanel, gridBagConstraints);

        instancesPanelMargin.setPreferredSize(new java.awt.Dimension(30, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        instancesWrapperPanel.add(instancesPanelMargin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        instancesWrapperPanel.add(instancesPanel, gridBagConstraints);

        instancesRootPanel.add(instancesWrapperPanel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        containerPanel.add(instancesRootPanel, gridBagConstraints);

        fillerPanel.setPreferredSize(new java.awt.Dimension(10, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        containerPanel.add(fillerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(containerPanel, gridBagConstraints);

        removedTopicMessage.setBackground(new java.awt.Color(255, 255, 255));
        removedTopicMessage.setLayout(new java.awt.GridBagLayout());

        removedTopicMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        removedTopicMessageLabel.setText("<html><p align=\"center\">No topic to view.<br>Maybe the topic has been merged or removed.<br>Please, open another topic.</p><html>");
        removedTopicMessage.add(removedTopicMessageLabel, new java.awt.GridBagConstraints());

        buttonWrapperPanel.setBackground(new java.awt.Color(255, 255, 255));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        removedTopicMessage.add(buttonWrapperPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(removedTopicMessage, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void baseNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_baseNameFieldKeyReleased
        try {
            if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
                System.out.println("Applying changes called at keyReleased");
                applyChanges();
            }
        }
        catch(Exception e) {}
    }//GEN-LAST:event_baseNameFieldKeyReleased

    private void subjectLocatorFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_subjectLocatorFieldKeyReleased
        try {
            if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
                applyChanges();
            }
        }
        catch(Exception e) {}
    }//GEN-LAST:event_subjectLocatorFieldKeyReleased

    private void subjectLocatorFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_subjectLocatorFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    applyChanges();
                }
                catch(Exception e) {}
            }
        });
    }//GEN-LAST:event_subjectLocatorFieldFocusLost

    private void baseNameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_baseNameFieldFocusLost
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    applyChanges();
                }
                catch(Exception e) {}
            }
        });
    }//GEN-LAST:event_baseNameFieldFocusLost

    private void subjectLocatorViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subjectLocatorViewButtonActionPerformed
        toggleVisibility("View subject locators");
    }//GEN-LAST:event_subjectLocatorViewButtonActionPerformed

    
    

    
    
    
    
    @Override
    public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int param) throws java.awt.print.PrinterException {
        if (param > 0) {
            return(NO_SUCH_PAGE);
        } 
        else {
            Graphics2D g2d = (Graphics2D)graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            // Turn off double buffering
            this.paint(g2d);
            // Turn double buffering back on
            return(PAGE_EXISTS);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public JPopupMenu getNamesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getVariantsLabelPopupStruct(options), wandora);
    }
    

    @Override
    public JPopupMenu getClassesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getClassesTablePopupStruct(), wandora);
    }
    
    
    @Override
    public JPopupMenu getInstancesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getInstancesTablePopupStruct(), wandora);
    }
    
    
    @Override
    public JPopupMenu getSIMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getSubjectIdentifierLabelPopupStruct(), wandora);
    }

    
    @Override
    public JPopupMenu getOccurrencesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getOccurrencesLabelPopupStruct(options), wandora);
    }
    
    
    @Override
    public JPopupMenu getOccurrenceTypeMenu(Topic occurrenceType) {
         return UIBox.makePopupMenu(WandoraMenuManager.getOccurrenceTypeLabelPopupStruct(occurrenceType, topic), wandora);
    }
    
    
    public JPopupMenu getSLMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getSubjectLocatorLabelPopupStruct(), wandora);
    }
    
    
    @Override
    public JPopupMenu getAssociationsMenu() {
         return UIBox.makePopupMenu(WandoraMenuManager.getAssociationTableLabelPopupStruct(), wandora);
    }

    
    @Override
    public JPopupMenu getAssociationTypeMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getAssociationTypeLabelPopupStruct(), wandora);
    }

    
    @Override
    public JPopupMenu getSubjectMenu() {
        return null;
    }
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        System.out.println("TraditionalTopicPanel catched action command '" + c + "'.");
        toggleVisibility(c);       
    }
    
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel associationPanel;
    private javax.swing.JPanel associationPanelMargin;
    private javax.swing.JPanel associationRootPanel;
    private javax.swing.JPanel associationWrapperPanel;
    private javax.swing.JLabel associationsNumber;
    private javax.swing.JLabel associationsTitle;
    private javax.swing.JPanel associationsTitleFiller;
    private javax.swing.JPanel associationsTitlePanel;
    private javax.swing.JTextField baseNameField;
    private javax.swing.JLabel baseNameLabel;
    private javax.swing.JPanel buttonWrapperPanel;
    private javax.swing.JLabel classesNumber;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel classesPanelMargin;
    private javax.swing.JPanel classesRootPanel;
    private javax.swing.JLabel classesTitle;
    private javax.swing.JPanel classesTitleFiller;
    private javax.swing.JPanel classesTitlePanel;
    private javax.swing.JPanel classesWrapperPanel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel dataNumber;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JPanel dataPanelMargin;
    private javax.swing.JLabel dataTitle;
    private javax.swing.JPanel dataTitleFiller;
    private javax.swing.JPanel dataTitlePanel;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel idPanel;
    private javax.swing.JLabel instancesNumber;
    private javax.swing.JPanel instancesPanel;
    private javax.swing.JPanel instancesPanelMargin;
    private javax.swing.JPanel instancesRootPanel;
    private javax.swing.JLabel instancesTitle;
    private javax.swing.JPanel instancesTitleFiller;
    private javax.swing.JPanel instancesTitlePanel;
    private javax.swing.JPanel instancesWrapperPanel;
    private javax.swing.JPanel occurrencesRootPanel;
    private javax.swing.JPanel occurrencesWrapperPanel;
    private javax.swing.JPanel previewContainerPanel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JPanel removedTopicMessage;
    private javax.swing.JLabel removedTopicMessageLabel;
    private javax.swing.JLabel subjectIdentifierLabel;
    private javax.swing.JPanel subjectIdentifierPanel;
    private javax.swing.JPanel subjectIdentifierRootPanel;
    private javax.swing.JTextField subjectLocatorField;
    private javax.swing.JLabel subjectLocatorLabel;
    private javax.swing.JToggleButton subjectLocatorViewButton;
    private javax.swing.JLabel typedAssociationNumber;
    private javax.swing.JLabel typedAssociationTitle;
    private javax.swing.JPanel typedAssociationTitleFiller;
    private javax.swing.JPanel typedAssociationsPanel;
    private javax.swing.JPanel typedAssociationsRootPanel;
    private javax.swing.JPanel typedAssociationsTitlePanel;
    private javax.swing.JPanel typedAssociationsWrapperPanel;
    private javax.swing.JPanel typesAssociationPanelMargin;
    private javax.swing.JLabel variantNumber;
    private javax.swing.JPanel variantPanel;
    private javax.swing.JPanel variantPanelMargin;
    private javax.swing.JPanel variantRootPanel;
    private javax.swing.JLabel variantTitle;
    private javax.swing.JPanel variantTitleFiller;
    private javax.swing.JPanel variantTitlePanel;
    private javax.swing.JPanel variantWrapperPanel;
    // End of variables declaration//GEN-END:variables



    
}

