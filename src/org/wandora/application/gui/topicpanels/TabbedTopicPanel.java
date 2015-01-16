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
 * TabbedTopicPanel.java
 *
 * Created on August 9, 2004, 2:33 PM
 */

package org.wandora.application.gui.topicpanels;



import org.wandora.application.gui.topicpanels.traditional.AbstractTraditionalTopicPanel;
import org.wandora.application.tools.subjects.AddSubjectIdentifier;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.previews.*;
import org.wandora.application.tools.subjects.*;
import org.wandora.application.tools.subjects.PasteSIs;
import org.wandora.application.tools.topicnames.*;
import org.wandora.application.contexts.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.wandora.application.gui.UIBox;

import org.wandora.application.gui.topicstringify.TopicToString;




/**
 *
 * @author  olli, ak
 */


public class TabbedTopicPanel extends AbstractTraditionalTopicPanel implements ActionListener, ChangeListener, TopicPanel, ComponentListener {
    
    public static final boolean MAKE_LOCAL_SETTINGS_GLOBAL = false;
    
    public Topic topic;
    public String topicSI;
    
    private Wandora wandora;
    private Options options;
    
    private String originalBN;
    private String originalSL;
       

    // tabComponentName, tabName, menuName
    private Object[][] tabStruct;


    
    
    
    /** Creates new TabbedTopicPanel */
    public TabbedTopicPanel(Topic topic, Wandora wandora) {
        open(topic);
    }
    public TabbedTopicPanel() {}
    
    
    
    
    @Override
    public void init() {
        this.wandora = Wandora.getWandora();
        
        if(this.options == null) {
            // Notice, a copy of global options is created for local use.
            // Thus, local adjustments have no global effect.
            this.options = new Options(wandora.getOptions());
        }
 
    }
    
    
    
    
    
    @Override
    public void open(Topic topic) {
        
       this.removeAll();
       initComponents();
        
       tabStruct = new Object[][] {
            { subjectScrollPanel,      "Subject",          "View subject tab",       "subjectScrollPanel" },
            { variantScrollPanel,      "Names",            "View names tab",         "variantScrollPanel" },
            { dataScrollPanel,         "Occurrences",      "View occurrences tab",   "dataScrollPanel" },
            { classesScrollPanel,      "Classes",          "View classes tab",       "classesScrollPanel" },
            { instancesScrollPanel,    "Instances",        "View instances tab",     "instancesScrollPanel" },
            { associationScrollPanel,  "Associations",     "View associations tab",  "associationScrollPanel" },
        };
        
        associationPanelContainer.setTransferHandler(new AssociationTableTransferHandler(this));
        instancesPanelContainer.setTransferHandler(new InstancesPanelTransferHandler(this));
        classesPanelContainer.setTransferHandler(new ClassesPanelTransferHandler(this));
        this.setTransferHandler(new TopicPanelTransferHandler(this));
        
        this.addComponentListener(this);
        topicTabbedPane.addChangeListener(this);
        
        try {
            this.topic = topic;
            this.topicSI = topic.getOneSubjectIdentifier().toExternalForm();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        refresh();
        selectCurrentTab();
    }
   
    
    
    public void selectCurrentTab() {
        if(options == null) return;
        String currentTabPanelName = options.get(OPTIONS_PREFIX + "currentTab");
        if(currentTabPanelName != null) {
            try {
                boolean found = false;
                for(int i=0; i<topicTabbedPane.getComponentCount(); i++) {
                    Component tabComponent = topicTabbedPane.getComponent(i);
                    if(tabComponent != null) {
                        if(tabComponent.getName() != null) {
                            if(tabComponent.getName().equals(currentTabPanelName)) {
                                topicTabbedPane.setSelectedComponent(tabComponent);
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if(!found) {
                    Component component = topicTabbedPane.getSelectedComponent();
                    if(component != null) options.put(OPTIONS_PREFIX + "currentTab", component.getName());
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    

    @Override
    public void stateChanged(ChangeEvent e) {
        // System.out.println("TopicTabbedPane state changed!");
        if(e.getSource().equals(topicTabbedPane) && options != null) {
            Component component = topicTabbedPane.getSelectedComponent();
            if(component != null) {
                for(int i=0; i<tabStruct.length; i++) {
                    if(((JPanel) tabStruct[i][0]).equals(component)) {
                        options.put(OPTIONS_PREFIX + "currentTab", component.getName());
                        if(MAKE_LOCAL_SETTINGS_GLOBAL) {
                            Options globalOptions = wandora.getOptions();
                            if(globalOptions != null) {
                                globalOptions.put(OPTIONS_PREFIX + "currentTab", component.getName());
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "Tabbed";
    }
    
    @Override
    public String getTitle() {
        if(topic != null) return TopicToString.toString(topic);
        else return "";
    }
    
    
    @Override
    public int getOrder() {
        return 10;
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_tabbed.png");
    }
    
    @Override
    public boolean noScroll(){
        return false;
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
        for(int i=0; i<tabStruct.length; i++) {
            Object[] tabData = tabStruct[i];
            String tabPanelName = ((JPanel) tabData[0]).getName();
            menuVector.add( tabData[2] );
            menuVector.add( options.isFalse(OPTIONS_VIEW_PREFIX + tabPanelName) ? hideIcon : viewIcon );
            menuVector.add( this );
        }
        menuVector.add( "---" );
        menuVector.add( "View all" );
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
            applyChanges();
        }
        catch(Exception e) {
            return;
        }
        
        if("View All".equalsIgnoreCase(componentName)) {
            JPanel panel;
            for(int i=0; i<tabStruct.length; i++) {
                panel = (JPanel) tabStruct[i][0];
                setVisibitilityOption(tabStruct[i][3].toString(), true);
                panel.setVisible(true);
            }
            setVisibitilityOption("subjectLocatorIcons", true);
            refreshTabs();
            iDidIt = true;
        }
        
        if("Hide All".equalsIgnoreCase(componentName)) {
            JPanel panel;
            for(int i=0; i<tabStruct.length; i++) {
                panel = (JPanel) tabStruct[i][0];
                setVisibitilityOption(tabStruct[i][3].toString(), false);
                panel.setVisible(false);
            }
            setVisibitilityOption("subjectLocatorIcons", false);
            refreshTabs();
            iDidIt = true;
        }

        else {
            int panelIndex = solvePanelIndex(componentName);
            if(panelIndex >= 0) {
                String optionName = tabStruct[panelIndex][3].toString();
                if(optionName != null) {
                    boolean currentValue = options.isFalse(OPTIONS_VIEW_PREFIX + optionName);
                    setVisibitilityOption(optionName, currentValue);
                    topicTabbedPane.removeAll();
                    refreshTabs();
                    iDidIt = true;
                }
            }
        }
        
        if(iDidIt) {
            wandora.topicPanelsChanged();
        }
    }
    
    
    
    private int solvePanelIndex(String componentName) {
        if(componentName == null) return -1;
        for(int i=0; i<tabStruct.length; i++) {
            if(componentName.equals(tabStruct[i][2])) return i;
        }
        return -1;
    }
    
    
    
    
    private void setVisibitilityOption(String key, boolean value) {
        options.put(OPTIONS_VIEW_PREFIX + key, value ? "true" : "false");
        if(MAKE_LOCAL_SETTINGS_GLOBAL) {
            Options globalOptions = wandora.getOptions();
            if(globalOptions != null) {
                globalOptions.put(OPTIONS_VIEW_PREFIX + key, value ? "true" : "false");
            }
        }
    }
    

    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

      
    
    public void updatePreview() {
        try {
            boolean viewPreview = options.getBoolean(OPTIONS_VIEW_PREFIX+"subjectPanelContainer", false);
            if(viewPreview) {
                if(topic.getSubjectLocator() != null) {
                    ((PreviewWrapper) previewPanel).setURL(topic.getSubjectLocator());
                }
                else {
                    ((PreviewWrapper) previewPanel).setURL(null);
                }
            }
        }
        catch(Exception e) {}
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void refresh() {
        
        try {
            topic=wandora.getTopicMap().getTopic(topicSI);
            if(topic==null || topic.isRemoved()) {
                System.out.println("Topic is null or removed!");
                topicTabbedPane.setVisible(false);
                removedTopicMessage.setVisible(true);
                return;
            }
            else {
                topicTabbedPane.setVisible(true);
                removedTopicMessage.setVisible(false);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Topic is null or removed!");
            topicTabbedPane.setVisible(false);
            removedTopicMessage.setVisible(true);
            return;
        }
        
        super.refresh();
        try {
            updatePreview();
            
/*            if(topic.getBaseName()!=null) baseNameField.setText(topic.getBaseName());
            else baseNameField.setText("");
            if(topic.getSubjectLocator()!=null) subjectLocatorField.setText(topic.getSubjectLocator().toString());
            else subjectLocatorField.setText("");*/
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

        buildAssociationsPanel(associationPanel, topic, options, wandora);
        buildClassesPanel(classesPanel, topic, options, wandora);
        buildSubjectIdentifierPanel(subjectIdentifierPanel, topic, options, wandora);
        buildInstancesPanel(instancesPanel, topic, options, wandora);
        buildAllNamesPanel(variantPanel, topic, this, options, wandora);
        buildOccurrencesPanel(dataPanel, topic, options, wandora);

        refreshTabs();
        
        this.setComponentPopupMenu(this.getViewPopupMenu());
    }
    
    
    
    public void refreshTabs() {
        String currentTab = options.get(OPTIONS_PREFIX + "currentTab");
        topicTabbedPane.removeAll();
        for(int i=0; i<tabStruct.length; i++) {
            JPanel tabPanel = (JPanel) tabStruct[i][0];
            if(! options.isFalse(OPTIONS_VIEW_PREFIX + tabPanel.getName())) {
                topicTabbedPane.addTab((String) tabStruct[i][1], tabPanel);
            }
            tabPanel.addMouseListener(this);
        }
        if(currentTab != null) {
            options.put(OPTIONS_PREFIX + "currentTab", currentTab);
            if(MAKE_LOCAL_SETTINGS_GLOBAL) {
                Options globalOptions = wandora.getOptions();
                if(globalOptions != null) {
                    globalOptions.put(OPTIONS_PREFIX + "currentTab", currentTab);
                }
            }
        }
        selectCurrentTab();
    }
    
    
 
    
    
    @Override
    public boolean applyChanges() throws CancelledException,TopicMapException {
        boolean changed=false;
        
        String fieldTextSL = subjectLocatorField.getText().trim();
//        String origSL=null;
//        if(topic.getSubjectLocator()!=null) origSL=topic.getSubjectLocator().toString();
        String origSL=originalSL;
        
        // ------- basenames -------
        String baseNameFieldText = baseNameField.getText().trim();
//        String origBN=topic.getBaseName();
        String origBN=originalBN;
        if((origBN==null && baseNameFieldText.length()>0) || (origBN!=null && !origBN.equals(baseNameFieldText))){
            if(TMBox.checkBaseNameChange(wandora,topic,baseNameFieldText)!=ConfirmResult.yes) {
                baseNameField.setText(origBN);
                throw new CancelledException();
            }
            if(baseNameFieldText.length()>0) topic.setBaseName(baseNameFieldText);
            else topic.setBaseName(null);
            changed=true;
        }
        
        // ------ subject locator -------
        if((origSL==null && fieldTextSL.length()>0) || (origSL!=null && !origSL.equals(fieldTextSL))){
            if(TMBox.checkSubjectLocatorChange(wandora,topic,fieldTextSL)!=ConfirmResult.yes) {
                subjectLocatorField.setText(originalSL);
                throw new CancelledException();
            }
            if(origSL!=null && !origSL.equals(fieldTextSL)){
                topic.setSubjectLocator(null);
            }
            if(fieldTextSL.length()>0) topic.setSubjectLocator(topic.getTopicMap().createLocator(fieldTextSL));
            changed=true;
        }

        // ------ everything else -------
        changed = changed | super.applyChanges(topic, wandora);
        if(changed) wandora.doRefresh();
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

        subjectScrollPanel = new javax.swing.JPanel();
        subjectPanelContainer = new javax.swing.JPanel();
        subjectLocatorLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectLocatorField = new org.wandora.application.gui.simple.SimpleURIField();
        subjectIdentifierLabel = new org.wandora.application.gui.simple.SimpleLabel();
        subjectIdentifierRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        subjectIdentifierPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        previewPanel = PreviewWrapper.getPreviewWrapper(wandora);
        variantScrollPanel = new javax.swing.JPanel();
        variantPanelContainer = new javax.swing.JPanel();
        basenamePanel = new javax.swing.JPanel();
        basenameLabel = new org.wandora.application.gui.simple.SimpleLabel();
        baseNameField = new org.wandora.application.gui.simple.SimpleField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        variantPanel = new org.wandora.application.gui.simple.SimplePanel();
        jPanel3 = new javax.swing.JPanel();
        dataScrollPanel = new javax.swing.JPanel();
        dataPanelContainer = new javax.swing.JPanel();
        dataRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        jPanel5 = new javax.swing.JPanel();
        dataPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        associationScrollPanel = new javax.swing.JPanel();
        associationPanelContainer = new javax.swing.JPanel();
        associationRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        associationPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        classesScrollPanel = new javax.swing.JPanel();
        classesPanelContainer = new javax.swing.JPanel();
        classesRootPanel = new org.wandora.application.gui.simple.SimplePanel();
        jPanel4 = new javax.swing.JPanel();
        classesPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        instancesScrollPanel = new javax.swing.JPanel();
        instancesPanelContainer = new javax.swing.JPanel();
        instancesPanel = new org.wandora.application.gui.simple.SimplePanel();
        jPanel8 = new javax.swing.JPanel();
        topicTabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        removedTopicMessage = new javax.swing.JPanel();
        removedTopicMessageLabel = new javax.swing.JLabel();

        subjectScrollPanel.setComponentPopupMenu(getSubjectMenu());
        subjectScrollPanel.setName("subjectScrollPanel"); // NOI18N
        subjectScrollPanel.setLayout(new java.awt.BorderLayout());

        subjectPanelContainer.setComponentPopupMenu(getSubjectMenu());
        subjectPanelContainer.setName("subjectPanelContainer"); // NOI18N
        subjectPanelContainer.setLayout(new java.awt.GridBagLayout());

        subjectLocatorLabel.setText("Subject locator");
        subjectLocatorLabel.setComponentPopupMenu(getSLMenu());
        subjectLocatorLabel.addMouseListener(wandora);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        subjectPanelContainer.add(subjectLocatorLabel, gridBagConstraints);

        subjectLocatorField.setMinimumSize(new java.awt.Dimension(6, 21));
        subjectLocatorField.setPreferredSize(new java.awt.Dimension(6, 21));
        subjectLocatorField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                subjectLocatorFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        subjectPanelContainer.add(subjectLocatorField, gridBagConstraints);

        subjectIdentifierLabel.setText("Subject identifiers");
        subjectIdentifierLabel.setComponentPopupMenu(getSIMenu());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        subjectPanelContainer.add(subjectIdentifierLabel, gridBagConstraints);

        subjectIdentifierRootPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        subjectIdentifierRootPanel.setComponentPopupMenu(getSIMenu());
        subjectIdentifierRootPanel.setName("subjectIdentifierRootPanel"); // NOI18N
        subjectIdentifierRootPanel.setLayout(new java.awt.BorderLayout());

        subjectIdentifierPanel.setLayout(new java.awt.GridLayout(1, 0));
        subjectIdentifierRootPanel.add(subjectIdentifierPanel, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        subjectPanelContainer.add(subjectIdentifierRootPanel, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        previewPanel.setToolTipText("");
        previewPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        previewPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(previewPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        subjectPanelContainer.add(jPanel2, gridBagConstraints);

        subjectScrollPanel.add(subjectPanelContainer, java.awt.BorderLayout.CENTER);

        variantScrollPanel.setComponentPopupMenu(getNamesMenu());
        variantScrollPanel.setName("variantScrollPanel"); // NOI18N
        variantScrollPanel.setLayout(new java.awt.BorderLayout());

        variantPanelContainer.setComponentPopupMenu(getNamesMenu());
        variantPanelContainer.setName("variantPanelContainer"); // NOI18N
        variantPanelContainer.setLayout(new java.awt.GridBagLayout());

        basenamePanel.setLayout(new java.awt.GridBagLayout());

        basenameLabel.setText("Base name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        basenamePanel.add(basenameLabel, gridBagConstraints);

        baseNameField.setMinimumSize(new java.awt.Dimension(6, 21));
        baseNameField.setPreferredSize(new java.awt.Dimension(6, 21));
        baseNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                baseNameFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 20);
        basenamePanel.add(baseNameField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        variantPanelContainer.add(basenamePanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        variantPanelContainer.add(jSeparator1, gridBagConstraints);

        jLabel1.setText("Variant names");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        variantPanelContainer.add(jLabel1, gridBagConstraints);

        variantPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        variantPanel.setName("variantPanel"); // NOI18N
        variantPanel.setLayout(new java.awt.GridLayout(1, 0, 0, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        variantPanelContainer.add(variantPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.weighty = 1.0;
        variantPanelContainer.add(jPanel3, gridBagConstraints);

        variantScrollPanel.add(variantPanelContainer, java.awt.BorderLayout.CENTER);

        dataScrollPanel.setComponentPopupMenu(getOccurrencesMenu());
        dataScrollPanel.setName("dataScrollPanel"); // NOI18N
        dataScrollPanel.setLayout(new java.awt.BorderLayout());

        dataPanelContainer.setComponentPopupMenu(getOccurrencesMenu());
        dataPanelContainer.setName("dataPanelContainer"); // NOI18N
        dataPanelContainer.setLayout(new java.awt.GridBagLayout());

        dataRootPanel.setComponentPopupMenu(getOccurrencesMenu());
        dataRootPanel.setName("dataRootPanel"); // NOI18N
        dataRootPanel.addMouseListener(wandora);
        dataRootPanel.setLayout(new java.awt.BorderLayout(5, 3));

        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel5.add(dataPanel, java.awt.BorderLayout.CENTER);

        dataRootPanel.add(jPanel5, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        dataPanelContainer.add(dataRootPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        dataPanelContainer.add(jPanel7, gridBagConstraints);

        dataScrollPanel.add(dataPanelContainer, java.awt.BorderLayout.PAGE_START);

        associationScrollPanel.setComponentPopupMenu(getAssociationsMenu());
        associationScrollPanel.setName("associationScrollPanel"); // NOI18N
        associationScrollPanel.setLayout(new java.awt.BorderLayout());

        associationPanelContainer.setComponentPopupMenu(getAssociationsMenu());
        associationPanelContainer.setName("associationPanelContainer"); // NOI18N
        associationPanelContainer.setLayout(new java.awt.GridBagLayout());

        associationRootPanel.setComponentPopupMenu(getAssociationsMenu());
        associationRootPanel.setName("associationRootPanel"); // NOI18N
        associationRootPanel.addMouseListener(wandora);
        associationRootPanel.setLayout(new java.awt.BorderLayout(5, 3));
        associationRootPanel.add(associationPanel, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        associationPanelContainer.add(associationRootPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        associationPanelContainer.add(jPanel6, gridBagConstraints);

        associationScrollPanel.add(associationPanelContainer, java.awt.BorderLayout.PAGE_START);

        classesScrollPanel.setComponentPopupMenu(getClassesMenu());
        classesScrollPanel.setName("classesScrollPanel"); // NOI18N
        classesScrollPanel.setLayout(new java.awt.BorderLayout());

        classesPanelContainer.setComponentPopupMenu(getClassesMenu());
        classesPanelContainer.setName("classesPanelContainer"); // NOI18N
        classesPanelContainer.setLayout(new java.awt.GridBagLayout());

        classesRootPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        classesRootPanel.setComponentPopupMenu(getClassesMenu());
        classesRootPanel.setName("classesRootPanel"); // NOI18N
        classesRootPanel.addMouseListener(wandora);
        classesRootPanel.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel4.add(classesPanel, java.awt.BorderLayout.CENTER);

        classesRootPanel.add(jPanel4, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        classesPanelContainer.add(classesRootPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        classesPanelContainer.add(jPanel1, gridBagConstraints);

        classesScrollPanel.add(classesPanelContainer, java.awt.BorderLayout.PAGE_START);

        instancesScrollPanel.setComponentPopupMenu(getInstancesMenu());
        instancesScrollPanel.setName("instancesScrollPanel"); // NOI18N
        instancesScrollPanel.setLayout(new java.awt.BorderLayout());

        instancesPanelContainer.setComponentPopupMenu(getInstancesMenu());
        instancesPanelContainer.setName("instancesPanelContainer"); // NOI18N
        instancesPanelContainer.setLayout(new java.awt.GridBagLayout());

        instancesPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        instancesPanel.setComponentPopupMenu(getInstancesMenu());
        instancesPanel.setName("instancesPanel"); // NOI18N
        instancesPanel.addMouseListener(wandora);
        instancesPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        instancesPanelContainer.add(instancesPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        instancesPanelContainer.add(jPanel8, gridBagConstraints);

        instancesScrollPanel.add(instancesPanelContainer, java.awt.BorderLayout.PAGE_START);

        setLayout(new java.awt.GridBagLayout());

        topicTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        topicTabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClickedOnTab(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(topicTabbedPane, gridBagConstraints);

        removedTopicMessage.setBackground(new java.awt.Color(255, 255, 255));
        removedTopicMessage.setLayout(new java.awt.GridBagLayout());

        removedTopicMessageLabel.setText("Topic is either merged or removed and can not be viewed!");
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
                applyChanges();
            }
        }
        catch(Exception e) {}
    }//GEN-LAST:event_baseNameFieldKeyReleased

    private void mouseClickedOnTab(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClickedOnTab
        if(evt.getButton()==MouseEvent.BUTTON3){
            Component tabComponent=topicTabbedPane.getSelectedComponent();
            if(tabComponent == null) return;
            
            JPopupMenu tabPopupMenu = null;
            if(tabComponent.equals(this.variantScrollPanel)) {
                tabPopupMenu = getNamesMenu();
            }
            else if(tabComponent.equals(classesScrollPanel)) {
                tabPopupMenu = getClassesMenu();
            }
            else if(tabComponent.equals(dataScrollPanel)) {
                tabPopupMenu = getOccurrencesMenu();
            }
            else if(tabComponent.equals(associationScrollPanel)) {
                tabPopupMenu = getAssociationsMenu();
            }
            else if(tabComponent.equals(instancesScrollPanel)) {
                tabPopupMenu = getInstancesMenu();
            }
            else if(tabComponent.equals(subjectScrollPanel)) {
                tabPopupMenu = getSubjectMenu();
            }
            
            if(tabPopupMenu != null) {
                tabPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_mouseClickedOnTab

    private void subjectLocatorFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_subjectLocatorFieldKeyReleased
        try {
            if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
                applyChanges();
                updatePreview();
            }
        }
        catch(Exception e) {}
    }//GEN-LAST:event_subjectLocatorFieldKeyReleased

    
    
    
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
        Object[] menuStructure = new Object[] {
            "Add variant name", new AddVariantName(new ApplicationContext()),
            "---",
            "Copy all variant names", new TopicNameCopier(new ApplicationContext()),
            "Remove all empty variant names...", new AllEmptyVariantRemover(new ApplicationContext()),
        };
        return UIBox.makePopupMenu(menuStructure, wandora);
    }
    
    
    
    
    @Override
    public JPopupMenu getClassesMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getClassesTablePopupStruct(), wandora);
    }
    
    
    
    public JPopupMenu getSLMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getSubjectLocatorLabelPopupStruct(), wandora);
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
    
    
    @Override
    public JPopupMenu getSubjectMenu() {
        Object[] menuStructure = new Object[] {
            "Check subject locator...", new SubjectLocatorChecker(new ApplicationContext()),
            "Download subject locator...", new DownloadSubjectLocators(new ApplicationContext()),
            "Remove subject locator...", new SubjectLocatorRemover(new ApplicationContext()),
            "---",
            "Add subject identifier...", new AddSubjectIdentifier(new ApplicationContext()),
            "Copy subject identifiers", new CopySIs(new ApplicationContext()),
            "Paste subject identifiers", new PasteSIs(new ApplicationContext()),
            "Flatten identity...", new FlattenSIs(new ApplicationContext()),
        };
        return UIBox.makePopupMenu(menuStructure, wandora);
    }
    
    
    @Override
    public JPopupMenu getAssociationsMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getAssociationTableLabelPopupStruct(), wandora);
    }


    
    @Override
    public JPopupMenu getAssociationTypeMenu() {
        return UIBox.makePopupMenu(WandoraMenuManager.getAssociationTypeLabelPopupStruct(), wandora);
    }

    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        System.out.println("TabbedTopicPanel catched action command '" + c + "'.");
        toggleVisibility(c);    
    }
    
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel associationPanel;
    private javax.swing.JPanel associationPanelContainer;
    private javax.swing.JPanel associationRootPanel;
    private javax.swing.JPanel associationScrollPanel;
    private javax.swing.JTextField baseNameField;
    private javax.swing.JLabel basenameLabel;
    private javax.swing.JPanel basenamePanel;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel classesPanelContainer;
    private javax.swing.JPanel classesRootPanel;
    private javax.swing.JPanel classesScrollPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JPanel dataPanelContainer;
    private javax.swing.JPanel dataRootPanel;
    private javax.swing.JPanel dataScrollPanel;
    private javax.swing.JPanel instancesPanel;
    private javax.swing.JPanel instancesPanelContainer;
    private javax.swing.JPanel instancesScrollPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JPanel removedTopicMessage;
    private javax.swing.JLabel removedTopicMessageLabel;
    private javax.swing.JLabel subjectIdentifierLabel;
    private javax.swing.JPanel subjectIdentifierPanel;
    private javax.swing.JPanel subjectIdentifierRootPanel;
    private javax.swing.JTextField subjectLocatorField;
    private javax.swing.JLabel subjectLocatorLabel;
    private javax.swing.JPanel subjectPanelContainer;
    private javax.swing.JPanel subjectScrollPanel;
    private javax.swing.JTabbedPane topicTabbedPane;
    private javax.swing.JPanel variantPanel;
    private javax.swing.JPanel variantPanelContainer;
    private javax.swing.JPanel variantScrollPanel;
    // End of variables declaration//GEN-END:variables


    // -------------------------------------------------------------------------
    
    
    @Override
    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        handleComponentEvent(e);
    }
   
    
    private void handleComponentEvent(ComponentEvent e) {
        revalidate();
        repaint();
    }
    
    
    
    // -------------------------------------------------------------------------

    
}

