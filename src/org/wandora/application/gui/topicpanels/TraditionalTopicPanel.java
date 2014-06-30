/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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



import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.previews.*;
import org.wandora.application.tools.topicnames.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.wandora.application.gui.UIBox;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleURIField;
import org.wandora.application.gui.simple.TopicLink;
import org.wandora.application.gui.topicstringify.TopicToString;


/**
 *
 * @author  olli, ak
 */


public class TraditionalTopicPanel extends AbstractTopicPanel implements ActionListener, TopicPanel {

    public static final boolean MAKE_LOCAL_SETTINGS_GLOBAL = false;

    public Topic topic;
    public String topicSI;
    
    private Wandora wandora;
    protected Options options;
    private Object[][] panelStruct;
    
    private String originalBN;
    private String originalSL;
    
    protected final TraditionalTopicPanel finalThis;
    
    
    
    /** Creates new form TraditionalTopicPanel */
    public TraditionalTopicPanel(Topic topic, Wandora wandora) {
        finalThis = this;
        open(topic);
        this.setTransferHandler(new TopicPanelTransferHandler());
    }
    public TraditionalTopicPanel() {
        finalThis = this;
        this.options = new Options(Wandora.getWandora().getOptions());
        this.setTransferHandler(new TopicPanelTransferHandler());
    }
    
    
    
    
    
    @Override
    public void open(Topic topic) {
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

        try {
            this.topic = topic;
            this.topicSI = topic.getOneSubjectIdentifier().toExternalForm();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        this.removeAll();
        initComponents();
        
        panelStruct = new Object[][] {
            { variantRootPanel,             "View names",           "variantRootPanel"  },
            { classesRootPanel,             "View classes",         "classesRootPanel" },
            { occurrencesRootPanel,         "View occurrences",         "occurrencesRootPanel"},
            { associationRootPanel,         "View associations",    "associationRootPanel" },
            { typedAssociationsRootPanel,   "View associations where type", "typedAssociationsRootPanel" },
            { instancesRootPanel,           "View instances",       "instancesRootPanel"  },
            { previewPanel,                 "View subject locators", "previewPanel" },
        };
        
        associationRootPanel.setTransferHandler(new AssociationRootTableTransferHandler());
        instancesRootPanel.setTransferHandler(new InstancesPanelTransferHandler());
        classesRootPanel.setTransferHandler(new ClassesPanelTransferHandler());
        occurrencesRootPanel.setTransferHandler(new OccurrencesPanelTransferHandler());

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
            if(topic == null || topic.isRemoved()) {
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
        Icon viewIcon = UIBox.getIcon("gui/icons/view.png");
        Icon hideIcon = UIBox.getIcon("gui/icons/view_no.png");

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
            for(int i=0; i<panelStruct.length; i++) {
                panel = (JPanel) panelStruct[i][0];
                setVisibitilityOption(panelStruct[i][2].toString(), true);
                //panel.setVisible(true);
            }
            setVisibitilityOption("subjectLocatorIcons", true);
            iDidIt = true;
        }
        
        if("Hide All".equalsIgnoreCase(componentName)) {
            JPanel panel;
            for(int i=0; i<panelStruct.length; i++) {
                panel = (JPanel) panelStruct[i][0];
                setVisibitilityOption(panelStruct[i][2].toString(), false);
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
            refresh();
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
            //component.setVisible(currentValue);
            refresh();
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
    
    
    
    
    public void updatePreview() {
        try {
            boolean viewPreview = options.getBoolean(OPTIONS_VIEW_PREFIX+"previewPanel", false);
            if(viewPreview) {
                if(topic.getSubjectLocator() != null) {
                    ((PreviewWrapper) previewPanel).setURL(topic.getSubjectLocator());
                }
                else {
                    ((PreviewWrapper) previewPanel).setURL(null);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void refresh() {
        //Thread.dumpStack();

        try {
            topic=wandora.getTopicMap().getTopic(topicSI);
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

        updatePreview();
        if(associationRootPanel.isVisible())        buildAssociationsPanel(associationPanel, wandora, topic, ASSOCIATIONS_WHERE_PLAYER);
        if(typedAssociationsRootPanel.isVisible())  buildAssociationsPanel(typedAssociationsPanel, wandora, topic, ASSOCIATIONS_WHERE_TYPE);
        if(classesRootPanel.isVisible())            buildClassesPanel(classesPanel, wandora, topic);
        if(subjectIdentifierRootPanel.isVisible())  buildSubjectIdentifierPanel(subjectIdentifierPanel, wandora, topic);
        if(instancesRootPanel.isVisible())              buildInstancesPanel(instancesRootPanel, wandora, topic);
         

        variantGUIType = options.get(VARIANT_GUITYPE_OPTIONS_KEY);
        if(variantGUIType == null || variantGUIType.length() == 0) variantGUIType = VARIANT_GUITYPE_SCHEMA;
        if(VARIANT_GUITYPE_SCHEMA.equalsIgnoreCase(variantGUIType)) {
            if("vertical".equals(options.get(OPTIONS_PREFIX + "namePanelOrientation"))) {
                buildVerticalNamePanel(variantRootPanel, wandora, topic);
            }
            else {
                buildHorizontalNamePanel(variantRootPanel, wandora, topic);
            }
        }
        else {
            buildAllNamesPanel(variantRootPanel, wandora, topic);
        }
        
        
        // Always create occurrences panel as data in the panel is updated to topic map
        // in apply changes. See applyChanges below.
        buildOccurrencesPanel(dataPanel, wandora, topic);
       
      
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
        if(previewPanel != null && previewPanel instanceof PreviewWrapper) {
            ((PreviewWrapper) previewPanel).stop();
        }
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
        previewPanel = PreviewWrapper.getPreviewWrapper(wandora);
        idPanel = new javax.swing.JPanel();
        baseNameLabel = new org.wandora.application.gui.simple.SimpleLabel();
        baseNameField = new SimpleField();
        subjectLocatorLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectLocatorField = new SimpleURIField();
        subjectIdentifierLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectIdentifierRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        subjectIdentifierPanel = new javax.swing.JPanel();
        variantRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        occurrencesRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        dataPanel = new javax.swing.JPanel();
        classesRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        classesPanel = new javax.swing.JPanel();
        associationRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        associationPanel = new javax.swing.JPanel();
        typedAssociationsRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        typedAssociationsPanel = new javax.swing.JPanel();
        instancesRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        removedTopicMessage = new javax.swing.JPanel();
        removedTopicMessageLabel = new SimpleLabel();

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
        gridBagConstraints.insets = new java.awt.Insets(10, 15, 5, 15);
        containerPanel.add(previewContainerPanel, gridBagConstraints);

        idPanel.setLayout(new java.awt.GridBagLayout());

        baseNameLabel.setForeground(new java.awt.Color(51, 51, 51));
        baseNameLabel.setText("Base name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        idPanel.add(baseNameLabel, gridBagConstraints);

        baseNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                baseNameFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
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

        subjectLocatorField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                subjectLocatorFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        idPanel.add(subjectLocatorField, gridBagConstraints);

        subjectIdentifierLabel.setForeground(new java.awt.Color(51, 51, 51));
        subjectIdentifierLabel.setText("Subject identifiers");
        subjectIdentifierLabel.setComponentPopupMenu(getSIMenu());
        subjectLocatorLabel.addMouseListener(wandora);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        idPanel.add(subjectIdentifierLabel, gridBagConstraints);

        subjectIdentifierRootPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        subjectIdentifierRootPanel.setName("subjectIdentifierRootPanel"); // NOI18N
        subjectIdentifierRootPanel.setLayout(new java.awt.BorderLayout());

        subjectIdentifierPanel.setLayout(new java.awt.GridLayout(1, 0));
        subjectIdentifierRootPanel.add(subjectIdentifierPanel, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        idPanel.add(subjectIdentifierRootPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 15, 7, 15);
        containerPanel.add(idPanel, gridBagConstraints);

        variantRootPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Variant names", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, javax.swing.UIManager.getDefaults().getColor("activeCaptionBorder")));
        variantRootPanel.setComponentPopupMenu(getNamesMenu());
        variantRootPanel.setName("variantRootPanel"); // NOI18N
        variantRootPanel.addMouseListener(wandora);
        variantRootPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
        containerPanel.add(variantRootPanel, gridBagConstraints);

        occurrencesRootPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Occurrences", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, javax.swing.UIManager.getDefaults().getColor("activeCaptionBorder")));
        occurrencesRootPanel.setComponentPopupMenu(getTextDataMenu());
        occurrencesRootPanel.setName("occurrencesRootPanel"); // NOI18N
        occurrencesRootPanel.addMouseListener(wandora);
        occurrencesRootPanel.setLayout(new java.awt.BorderLayout(0, 3));
        occurrencesRootPanel.add(dataPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
        containerPanel.add(occurrencesRootPanel, gridBagConstraints);

        classesRootPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Classes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, javax.swing.UIManager.getDefaults().getColor("activeCaptionBorder")));
        classesRootPanel.setComponentPopupMenu(getClassesMenu());
        classesRootPanel.setName("classesRootPanel"); // NOI18N
        classesRootPanel.addMouseListener(wandora);
        classesRootPanel.setLayout(new java.awt.BorderLayout(0, 3));
        classesRootPanel.add(classesPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
        containerPanel.add(classesRootPanel, gridBagConstraints);

        associationRootPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Associations", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, javax.swing.UIManager.getDefaults().getColor("activeCaptionBorder")));
        associationRootPanel.setComponentPopupMenu(getAssociationsMenu());
        associationRootPanel.setName("associationRootPanel"); // NOI18N
        associationRootPanel.addMouseListener(wandora);
        associationRootPanel.setLayout(new java.awt.BorderLayout(0, 3));
        associationRootPanel.add(associationPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
        containerPanel.add(associationRootPanel, gridBagConstraints);

        typedAssociationsRootPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Associations where type", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, javax.swing.UIManager.getDefaults().getColor("activeCaptionBorder")));
        typedAssociationsRootPanel.setName("typedAssociationsRootPanel"); // NOI18N
        typedAssociationsRootPanel.setLayout(new java.awt.BorderLayout());
        typedAssociationsRootPanel.add(typedAssociationsPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
        containerPanel.add(typedAssociationsRootPanel, gridBagConstraints);

        instancesRootPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Instances", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, javax.swing.UIManager.getDefaults().getColor("activeCaptionBorder")));
        instancesRootPanel.setComponentPopupMenu(getInstancesMenu());
        instancesRootPanel.setName("instancesRootPanel"); // NOI18N
        instancesRootPanel.addMouseListener(wandora);
        instancesRootPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 7, 10);
        containerPanel.add(instancesRootPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(containerPanel, gridBagConstraints);

        removedTopicMessage.setBackground(new java.awt.Color(255, 255, 255));
        removedTopicMessage.setLayout(new java.awt.GridBagLayout());

        removedTopicMessageLabel.setText("<html>Topic is either merged or removed and can not be viewed!<html>");
        removedTopicMessage.add(removedTopicMessageLabel, new java.awt.GridBagConstraints());

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

    
    
    
    protected void variantNameFieldKeyReleased(java.awt.event.KeyEvent evt) {                                                
        try {
            if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
                applyChanges();
            }
        }
        catch(Exception e) {}
    }                                               

    
    
    
    
    @Override
    public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int param) throws java.awt.print.PrinterException {
        if (param > 0) {
            return(NO_SUCH_PAGE);
        } else {
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
        return UIBox.makePopupMenu(WandoraMenuManager.getVariantsLabelPopupStruct(options, variantGUIType), wandora);
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
    public JPopupMenu getTextDataMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getOccurrencesLabelPopupStruct(), wandora);
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
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        System.out.println("TraditionalTopicPanel catched action command '" + c + "'.");
        toggleVisibility(c);       
    }
    
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel associationPanel;
    private javax.swing.JPanel associationRootPanel;
    private javax.swing.JTextField baseNameField;
    private javax.swing.JLabel baseNameLabel;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel classesRootPanel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JPanel idPanel;
    private javax.swing.JPanel instancesRootPanel;
    private javax.swing.JPanel occurrencesRootPanel;
    private javax.swing.JPanel previewContainerPanel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JPanel removedTopicMessage;
    private javax.swing.JLabel removedTopicMessageLabel;
    private javax.swing.JLabel subjectIdentifierLabel;
    private javax.swing.JPanel subjectIdentifierPanel;
    private javax.swing.JPanel subjectIdentifierRootPanel;
    private javax.swing.JTextField subjectLocatorField;
    private javax.swing.JLabel subjectLocatorLabel;
    private javax.swing.JPanel typedAssociationsPanel;
    private javax.swing.JPanel typedAssociationsRootPanel;
    private javax.swing.JPanel variantRootPanel;
    // End of variables declaration//GEN-END:variables






    private class AssociationRootTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            //System.out.println("Dropped "+support);
            if(!support.isDrop()) return false;
            try {
                TopicMap tm = wandora.getTopicMap();
                ArrayList<Topic> sourceTopics=DnDHelper.getTopicList(support, tm, true);
                if(sourceTopics==null || sourceTopics.isEmpty()) return false;

                Topic schemaContentTypeTopic = tm.getTopic(SchemaBox.CONTENTTYPE_SI);
                Topic schemaAssociationTypeTopic = tm.getTopic(SchemaBox.ASSOCIATIONTYPE_SI);
                Topic schemaRoleTypeTopic = tm.getTopic(SchemaBox.ROLE_SI);

                if(schemaContentTypeTopic == null ||
                        schemaAssociationTypeTopic == null ||
                        schemaRoleTypeTopic == null) {
                            return false;
                }
                
                Topic targetTopic = topic;
                if(targetTopic != null && !targetTopic.isRemoved()) {
                    boolean associationAdded = false;
                    Collection<Topic> targetTopicTypes = topic.getTypes();
                    for(Topic targetTopicType : targetTopicTypes) {
                        if(targetTopicType.isOfType(schemaContentTypeTopic)) {
                            Collection<Association> associationTypes = targetTopicType.getAssociations(schemaAssociationTypeTopic);
                            for(Association associationTypeAssociation : associationTypes) {
                                Topic associationType = associationTypeAssociation.getPlayer(schemaAssociationTypeTopic);
                                if(associationType != null) {
                                    Collection<Association> roleAssociations = associationType.getAssociations(schemaRoleTypeTopic);
                                    Collection<Association> roleAssociations2 = associationType.getAssociations(schemaRoleTypeTopic);
                                    if(roleAssociations.size() == 2) {
                                        Topic sourceRole = null;
                                        Topic targetRole = null;
                                        Topic proposedSourceRole = null;
                                        Topic proposedTargetRole = null;
                                        Topic selectedSourceTopic = null;

                                        for(Association roleAssociation : roleAssociations) {
                                            proposedTargetRole = roleAssociation.getPlayer(schemaRoleTypeTopic);
                                            if(proposedTargetRole != null && proposedTargetRole.mergesWithTopic(targetTopicType)) {
                                                targetRole = proposedTargetRole;

                                                for(Association roleAssociation2 : roleAssociations2) {
                                                    proposedSourceRole = roleAssociation2.getPlayer(schemaRoleTypeTopic);
                                                    for(Topic sourceTopic : sourceTopics) {
                                                        if(sourceTopic != null && !sourceTopic.isRemoved()) {
                                                            Collection<Topic> sourceTopicTypes = sourceTopic.getTypes();
                                                            for(Topic sourceTopicType : sourceTopicTypes) {
                                                                if(sourceTopicType != null && !sourceTopicType.isRemoved()) {
                                                                    if(proposedSourceRole.mergesWithTopic(sourceTopicType)) {
                                                                        sourceRole = proposedSourceRole;
                                                                        selectedSourceTopic = sourceTopic;

                                                                        if(targetRole != null && sourceRole != null && selectedSourceTopic != null) {
                                                                            Association a=tm.createAssociation(associationType);
                                                                            a.addPlayer(selectedSourceTopic, sourceRole);
                                                                            a.addPlayer(targetTopic, targetRole);
                                                                            associationAdded = true;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(!associationAdded) {
                        Topic associationType = tm.getTopic(SchemaBox.DEFAULT_ASSOCIATION_SI);
                        Topic role1 = tm.getTopic(SchemaBox.DEFAULT_ROLE_1_SI);
                        Topic role2 = tm.getTopic(SchemaBox.DEFAULT_ROLE_2_SI);
                        if(associationType != null && role1 != null && role2 != null) {
                            for(Topic sourceTopic : sourceTopics) {
                                if(sourceTopic != null && !sourceTopic.isRemoved()) {
                                    Association a=tm.createAssociation(associationType);
                                    a.addPlayer(sourceTopic, role1);
                                    a.addPlayer(targetTopic, role2);
                                    associationAdded = true;
                                }
                            }
                        }
                    }
                }
                wandora.doRefresh();
                return true;
            }
            catch(Exception ce){
                Wandora.getWandora().handleError(ce);
            }
            return false;
        }

    }

    
    
    
    
    private class InstancesPanelTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
//            return DnDHelper.makeTopicTableTransferable(data,getSelectedRows(),getSelectedColumns());
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try{
                TopicMap tm=wandora.getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                Topic base=topic;
                if(base==null) return false;

                for(Topic t : topics) {
                    if(t != null && !t.isRemoved()) {
                        t.addType(base);
                    }
                }
                wandora.doRefresh();
                return true;
            }
            catch(Exception ce){
                Wandora.getWandora().handleError(ce);
            }
            return false;
        }

    }
    
    

    
    private class ClassesPanelTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
//            return DnDHelper.makeTopicTableTransferable(data,getSelectedRows(),getSelectedColumns());
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try{
                TopicMap tm=wandora.getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                Topic base=topic;
                if(base==null) return false;

                for(Topic t : topics) {
                    if(t != null && !t.isRemoved()) {
                        base.addType(t);
                    }
                }
                wandora.doRefresh();
                return true;
            }
            catch(Exception ce){
                Wandora.getWandora().handleError(ce);
            }
            return false;
        }

    }
    

    
    
    private class OccurrencesPanelTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
//            return DnDHelper.makeTopicTableTransferable(data,getSelectedRows(),getSelectedColumns());
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try {
                Transferable transferable = support.getTransferable();
                boolean ready = false;
                String data = null;
                
                if(support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    data = transferable.getTransferData(DataFlavor.stringFlavor).toString();
                }

                if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        java.util.List<File> fileList = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if(fileList != null && fileList.size() > 0) {
                            for( File occurrenceFile : fileList ) {

                                Reader inputReader = null;
                                String content = "";

                                String filename = occurrenceFile.getPath().toLowerCase();
                                String extension = filename.substring(Math.max(filename.lastIndexOf(".")+1, 0));

                                // --- handle rtf files ---
                                if("rtf".equals(extension)) {
                                    content=Textbox.RTF2PlainText(new FileInputStream(occurrenceFile));
                                    inputReader = new StringReader(content);
                                }

                                // --- handle pdf files ---
                                if("pdf".equals(extension)) {
                                    try {
                                        PDDocument doc = PDDocument.load(occurrenceFile);
                                        PDFTextStripper stripper = new PDFTextStripper();
                                        content = stripper.getText(doc);
                                        doc.close();
                                        inputReader = new StringReader(content);
                                    }
                                    catch(Exception e) {
                                        System.out.println("No PDF support!");
                                    }
                                }

                                // --- handle MS office files ---
                                if("doc".equals(extension) ||
                                   "docx".equals(extension) ||
                                   "ppt".equals(extension) ||
                                   "xsl".equals(extension) ||
                                   "vsd".equals(extension) 
                                   ) {
                                        content = MSOfficeBox.getText(occurrenceFile);
                                        if(content != null) {
                                            inputReader = new StringReader(content);
                                        }
                                }


                                // --- handle everything else ---
                                if(inputReader == null) {
                                    inputReader = new FileReader(occurrenceFile);
                                }

                                data = IObox.loadFile(inputReader);
                            }
                        }
                    }
                    catch(Exception ce){
                        Wandora.getWandora().handleError(ce);
                    }
                }
                
                // IF THE OCCURRENCE TEXT (=DATA) IS AVAILABLE, THEN...
                if(data != null) {
                    Topic base=topic;
                    if(base==null) return false;
                    TopicMap tm=wandora.getTopicMap();
                    Topic type = tm.getTopic(SchemaBox.DEFAULT_OCCURRENCE_SI);
                    if(type != null) {
                        Collection<Topic> langs = tm.getTopicsOfType(XTMPSI.LANGUAGE);
                        langs = TMBox.sortTopics(langs, "en");
                        for(Topic lang : langs) {
                            if(ready) break;
                            if(lang != null && !lang.isRemoved()) {
                                if(base.getData(type, lang) == null) {
                                    base.setData(type, lang, data);
                                    ready = true;
                                }
                            }
                        }
                    }
                }
                if(ready) {
                    wandora.doRefresh();
                }
                return true;
            }
            catch(Exception ce){
                Wandora.getWandora().handleError(ce);
            }
            return false;
        }

    }

    
    
    
    private class TopicPanelTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            return false;
            //if(!support.isDrop()) return false;
            //return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
            //       support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
//            return DnDHelper.makeTopicTableTransferable(data,getSelectedRows(),getSelectedColumns());
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try {
                /*
                TopicMap tm=parent.getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                Topic base=topic;
                if(base==null) return false;

                for(Topic t : topics) {
                    if(t != null && !t.isRemoved()) {
                        base.addType(t);
                    }
                }
                */ 
                wandora.doRefresh();
            }
            catch(Exception ce){
                Wandora.getWandora().handleError(ce);
            }
            return false;
        }

    }
    
    
    // -------------------------------------------------------------------------
    
    


    @Override
    public void buildHorizontalNamePanel(JPanel variantPanel, Wandora parent, Topic topic) {
        try {
            GridBagConstraints gbc;
            nameTable=new HashMap<Set<Topic>,SimpleField>();
            invNameTable=new HashMap<SimpleField,Set<Topic>>();
            originalNameTable=new IteratedMap<Collection<Topic>,String>();
            variantPanel.removeAll();

            Topic[] langTopics = null;
            Topic[] verTopics = null;

            String[] langs=TMBox.getLanguageSIs(parent.getTopicMap());
            String[] vers=TMBox.getNameVersionSIs(parent.getTopicMap());
            langTopics=parent.getTopicMap().getTopics(langs);
            verTopics=parent.getTopicMap().getTopics(vers);

            // variantPanel.setLayout(new java.awt.GridLayout(vers.length+1,langs.length+1));
            variantPanel.setLayout(new java.awt.GridBagLayout());
            // variantPanel.add(new javax.swing.JLabel(""));
            int x = 1;
            for(int i=0; i<langTopics.length; i++) {
                if(langTopics[i] != null) {
                    // variantPanel.add(new javax.swing.JLabel(langTopics[i].getName(nameScope)));
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridx=x;
                    gbc.gridy=0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=1.0;
                    variantPanel.add(new TopicLink(langTopics[i],parent),gbc);
                    x++;
                }
            }
            int y = 1;
            for(int i=0;i<verTopics.length;i++) {
                if(verTopics[i] != null) {
                    // variantPanel.add(new javax.swing.JLabel(verTopics[i].getName(nameScope)));
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridx=0;
                    gbc.gridy=y;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=0.0;
                    gbc.insets = new Insets(0,0,0,10);
                    variantPanel.add(new TopicLink(verTopics[i],parent),gbc);
                    x = 1;
                    for(int j=0;j<langTopics.length;j++) {
                        if(langTopics[j] != null) {
                            HashSet s=new HashSet(); s.add(verTopics[i]); s.add(langTopics[j]);
                            String name=topic.getVariant(s);
                            SimpleField field = new SimpleField(name==null?"":name);
                            field.addKeyListener( new KeyAdapter() {
                                @Override
                                public void keyReleased(java.awt.event.KeyEvent evt) {
                                    finalThis.variantNameFieldKeyReleased(evt);
                                }
                            });
                            field.setCaretPosition(0);
                            // field.addKeyListener(this);
                            field.setPreferredSize(new java.awt.Dimension(130,19));

                            Color c=parent.topicHilights.getVariantColor(topic,s);
                            if(c!=null) field.setForeground(c);

                            nameTable.put(s,field);
                            invNameTable.put(field,s);
                            originalNameTable.put(s,field.getText());
                            gbc=new java.awt.GridBagConstraints();
                            gbc.gridx=x;
                            gbc.gridy=y;
                            gbc.fill=GridBagConstraints.HORIZONTAL;
                            gbc.weightx=1.0;
                            gbc.insets=new Insets(0,0,0,0);
                            variantPanel.add(field,gbc);
                            x++;
                        }
                    }
                    y++;
                }
            }
            int n = topic.getVariantScopes().size();
            setPanelTitle(variantPanel, "Variant names ("+n+")");
        }
        catch(Exception e) {
            System.out.println("Failed to initialize names!");
            e.printStackTrace();
        }
    }




    @Override
    public void buildVerticalNamePanel(JPanel variantPanel, Wandora parent, Topic topic) {
        try {
            GridBagConstraints gbc;
            nameTable=new HashMap<Set<Topic>,SimpleField>();
            invNameTable=new HashMap<SimpleField,Set<Topic>>();
            originalNameTable=new IteratedMap<Collection<Topic>,String>();
            variantPanel.removeAll();

            Topic[] langTopics = null;
            Topic[] verTopics = null;

            String[] langs=TMBox.getLanguageSIs(parent.getTopicMap());
            String[] vers=TMBox.getNameVersionSIs(parent.getTopicMap());
            langTopics=parent.getTopicMap().getTopics(langs);
            verTopics=parent.getTopicMap().getTopics(vers);

            // variantPanel.setLayout(new java.awt.GridLayout(vers.length+1,langs.length+1));
            variantPanel.setLayout(new java.awt.GridBagLayout());
            // variantPanel.add(new javax.swing.JLabel(""));

            int x = 1;
            for(int i=0;i<verTopics.length;i++) {
                if(verTopics[i] != null) {
                    // for(int i=0;i<langs.length;i++){
                    //   variantPanel.add(new javax.swing.JLabel(langTopics[i].getName(nameScope)));
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridx=x;
                    gbc.gridy=0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=0.0;
                    variantPanel.add(new TopicLink(verTopics[i],parent),gbc);
                    x++;
                }
            }
            int y = 1;
            for(int j=0;j<langTopics.length;j++) {
                if(langTopics[j] != null) {
                    //for(int i=0;i<vers.length;i++) {
                    // variantPanel.add(new javax.swing.JLabel(verTopics[i].getName(nameScope)));
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridx=0;
                    gbc.gridy=y;
                    gbc.anchor=GridBagConstraints.WEST;
                    gbc.insets = new Insets(0,0,0,10);
                    //gbc.fill=gbc.HORIZONTAL;
                    //gbc.weightx=1.0;
                    variantPanel.add(new TopicLink(langTopics[j],parent),gbc);
                    x = 1;
                    for(int i=0;i<verTopics.length;i++) {
                        if(verTopics[i]!=null) {
                            //for(int j=0;j<langs.length;j++) {
                            HashSet s=new HashSet(); s.add(verTopics[i]); s.add(langTopics[j]);
                            String name=topic.getVariant(s);
                            SimpleField field = new SimpleField(name==null?"":name);
                            field.addKeyListener( new KeyAdapter() {
                                @Override
                                public void keyReleased(java.awt.event.KeyEvent evt) {
                                    finalThis.variantNameFieldKeyReleased(evt);
                                }
                            });
                            field.setCaretPosition(0);
                            // field.addKeyListener(this);
                            field.setPreferredSize(new java.awt.Dimension(130,19));

                            Color c=parent.topicHilights.getVariantColor(topic,s);
                            if(c!=null) field.setForeground(c);

                            nameTable.put(s,field);
                            invNameTable.put(field,s);
                            originalNameTable.put(s,field.getText());
                            gbc=new java.awt.GridBagConstraints();
                            gbc.gridx=x;
                            gbc.gridy=y;
                            gbc.fill=GridBagConstraints.HORIZONTAL;
                            gbc.weightx=1.0;
                            gbc.insets = new Insets(0,0,0,0);
                            variantPanel.add(field,gbc);
                            x++;
                        }
                    }
                    y++;
                }
            }

            int n = topic.getVariantScopes().size();
            setPanelTitle(variantPanel, "Variant names ("+n+")");
        }
        catch(Exception e) {
            System.out.println("Failed to initialize names!");
            e.printStackTrace();
        }
    }





    @Override
    public void buildAllNamesPanel(JPanel variantPanel, final Wandora parent, final Topic topic) {
        if(topic != null) {
            try {
                nameTable=new HashMap<Set<Topic>,SimpleField>();
                invNameTable=new HashMap<SimpleField,Set<Topic>>();
                originalNameTable=new IteratedMap<Collection<Topic>,String>();
                variantPanel.removeAll();

                JPanel myVariantPanel = variantPanel;
                myVariantPanel.setLayout(new GridBagLayout());
                Set<Set<Topic>> scopes = topic.getVariantScopes();
                int i = 0;
                for(Set<Topic> scope : scopes) {
                    JPanel scopeNamePanel = new JPanel();
                    scopeNamePanel.setLayout(new GridBagLayout());
                    java.awt.GridBagConstraints gbcs=new java.awt.GridBagConstraints();
                    gbcs.gridx=0;
                    gbcs.gridy=0;
                    gbcs.fill=GridBagConstraints.HORIZONTAL;
                    gbcs.weightx=1.0;
                    gbcs.insets = new Insets(0,10,0,0);
                    SimpleField nf = new SimpleField();
                    nf.addKeyListener( new KeyAdapter() {
                        @Override
                        public void keyReleased(java.awt.event.KeyEvent evt) {
                            finalThis.variantNameFieldKeyReleased(evt);
                        }
                    });
                    nf.setPreferredSize(new Dimension(100,23));
                    nf.setMinimumSize(new Dimension(100,23));
                    String variant = topic.getVariant(scope);
                    nf.setText(variant);
                    scopeNamePanel.add(nf, gbcs);

                    JPanel buttonPanel = new JPanel();
                    buttonPanel.setPreferredSize(new Dimension(30, 23));
                    buttonPanel.setMinimumSize(new Dimension(30, 23));
                    buttonPanel.setMaximumSize(new Dimension(30, 23));
                    buttonPanel.setLayout(new FlowLayout(0));
                    SimpleButton deleteVariantButton = new SimpleButton(UIBox.getIcon("resources/gui/icons/delete_variant.png"));
                    deleteVariantButton.setPreferredSize(new Dimension(16, 16));
                    deleteVariantButton.setBackground(UIConstants.buttonBarBackgroundColor);
                    deleteVariantButton.setForeground(UIConstants.buttonBarLabelColor);
                    deleteVariantButton.setOpaque(true);
                    deleteVariantButton.setBorderPainted(false);
                    deleteVariantButton.setToolTipText("Delete variant name.");

                    final DeleteVariantName tool=new DeleteVariantName(topic, scope);
                    deleteVariantButton.addActionListener(new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            try {
                                tool.execute(parent);
                            }
                            catch(TopicMapException tme) {
                                tme.printStackTrace();
                            } // TODO EXCEPTION
                        }
                    }
                    );
                    buttonPanel.add(deleteVariantButton);
                    buttonPanel.setSize(16, 16);

                    gbcs.gridx=1;
                    gbcs.fill=GridBagConstraints.NONE;
                    gbcs.weightx=0.0;
                    gbcs.insets = new Insets(0,0,0,0);
                    scopeNamePanel.add(buttonPanel, gbcs);


                    originalNameTable.put(scope, variant);
                    nameTable.put(scope, nf);
                    invNameTable.put(nf, scope);

                    JPanel scopePanel = new JPanel();
                    scopePanel.setLayout(new GridBagLayout());
                    java.awt.GridBagConstraints gbcst=new java.awt.GridBagConstraints();
                    int j=1;
                    for(Topic scopeTopic : scope) {
                        visibleTopics.addAll(scopeTopic.getSubjectIdentifiers());

                        gbcst.gridx=0;
                        gbcst.gridy=j;
                        gbcst.fill=GridBagConstraints.HORIZONTAL;
                        gbcst.anchor=GridBagConstraints.EAST;
                        gbcst.weightx=1.0;
                        //gbcst.insets = new Insets(0,20,0,0);

                        JPanel fillerPanel = new JPanel();
                        fillerPanel.setPreferredSize(new Dimension(30, 16));
                        fillerPanel.setMinimumSize(new Dimension(30, 16));
                        fillerPanel.setMaximumSize(new Dimension(30, 16));
                        scopePanel.add(fillerPanel, gbcst);

                        TopicLink scopeTopicLabel = new TopicLink(scopeTopic, parent);
                        scopeTopicLabel.setLimitLength(false);
                        scopeTopicLabel.setText(scopeTopic);
                        scopeTopicLabel.setToolTipText(scopeTopic.getOneSubjectIdentifier().toExternalForm());

                        ArrayList addScopeSubmenu = new ArrayList();
                        addScopeSubmenu.add("Add scope topic...");
                        addScopeSubmenu.add(new AddScopeTopicToVariantName(topic, scope));
                        addScopeSubmenu.add("---");
                        String[] vers=TMBox.getNameVersionSIs(parent.getTopicMap());
                        for(int k=0; k<vers.length; k++) {
                            Topic lt = parent.getTopicMap().getTopic(vers[k]);
                            String ltName = parent.getTopicGUIName(lt);
                            addScopeSubmenu.add("Add scope topic '"+ltName+"'");
                            addScopeSubmenu.add(new AddScopeTopicToVariantName(topic, scope, lt));
                        }
                        addScopeSubmenu.add("---");
                        String[] langs=TMBox.getLanguageSIs(parent.getTopicMap());
                        for(int k=0; k<langs.length; k++) {
                            Topic lt = parent.getTopicMap().getTopic(langs[k]);
                            String ltName = parent.getTopicGUIName(lt);
                            addScopeSubmenu.add("Add scope topic '"+ltName+"'");
                            addScopeSubmenu.add(new AddScopeTopicToVariantName(topic, scope, lt));
                        }

                        JPopupMenu scopeTopicPopup = UIBox.makePopupMenu(
                                new Object[] {
                                    "Add scope topic", addScopeSubmenu.toArray( new Object[] {} ),
                                    "Remove scope topic", new DeleteScopeTopicInVariantName(topic, scope, scopeTopic),
                                    "---",
                                    "Remove variant name", new DeleteVariantName(topic, scope)
                                },
                                parent
                        );
                        scopeTopicLabel.setComponentPopupMenu(scopeTopicPopup);

                        gbcst.gridx=1;
                        gbcst.gridy=j;
                        gbcst.fill=GridBagConstraints.NONE;
                        gbcst.anchor=GridBagConstraints.EAST;
                        //gbcst.weightx=1.0;
                        //gbcst.insets = new Insets(0,0,0,20);
                        scopePanel.add(scopeTopicLabel, gbcst);
                        j++;
                    }
                    gbcs.gridx=0;
                    gbcs.gridy=1;
                    gbcs.gridwidth=1;
                    gbcs.fill=GridBagConstraints.HORIZONTAL;
                    gbcs.weightx=1.0;
                    scopeNamePanel.add(scopePanel, gbcs);

                    java.awt.GridBagConstraints gbc=new java.awt.GridBagConstraints();
                    gbc.gridx=0;
                    gbc.gridy=i;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=1.0;
                    gbc.insets = new Insets(5,0,7,0);
                    myVariantPanel.add(scopeNamePanel, gbc);
                    i++;
                }
                GridBagConstraints pgbc = new java.awt.GridBagConstraints();
                pgbc.fill=GridBagConstraints.HORIZONTAL;
                pgbc.weightx=1.0;
                //variantPanel.add(myVariantPanel, pgbc);


                int n = topic.getVariantScopes().size();
                setPanelTitle(variantPanel, "Variant names ("+n+")");
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        }

    }
    
    
    

    
}

