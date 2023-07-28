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
 * AbstractTraditionalTopicPanel.java
 *
 * Created on 19. lokakuuta 2005, 16:32
 *
 */

package org.wandora.application.gui.topicpanels.traditional;



import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.gui.table.ClassTable;
import org.wandora.application.gui.table.InstanceTable;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.topicnames.AddScopeTopicToVariantName;
import org.wandora.application.tools.topicnames.DeleteScopeTopicInVariantName;
import org.wandora.application.tools.topicnames.DeleteVariantName;
import org.wandora.topicmap.*;
import org.wandora.utils.*;




/**
 *
 * @author akivela
 */
public abstract class AbstractTraditionalTopicPanel extends JPanel implements Printable, MouseListener, TopicMapListener, RefreshListener  {


	private static final long serialVersionUID = 1L;
	
	
	public static final String VARIANT_GUITYPE_SCHEMA = "schema";
    public static final String VARIANT_GUITYPE_USED = "used";


    public String variantGUIType = VARIANT_GUITYPE_SCHEMA;

    public static final String OPTIONS_PREFIX = "gui.traditionalTopicPanel.";
    public static final String OPTIONS_VIEW_PREFIX = OPTIONS_PREFIX + "view.";
    public static final String VARIANT_GUITYPE_OPTIONS_KEY = OPTIONS_PREFIX + "nameUI";

    public static final Color tableBorderColor = new Color(153,153,153);
    public static final Border leftTableBorder = BorderFactory.createMatteBorder(0, 1, 0, 0, tableBorderColor);
    public static final Border leftTopTableBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, tableBorderColor);

    public int ASSOCIATIONS_WHERE_PLAYER = 1;
    public int ASSOCIATIONS_WHERE_TYPE = 2;


    protected OccurrenceTableAll occurrenceTable;
    protected Collection<OccurrenceTableSingleType> occurrenceTables;
    protected HashMap<Set<Topic>,SimpleField> nameTable;
    protected HashMap<SimpleField,Set<Topic>> invNameTable;
    protected IteratedMap<Collection<Topic>,String> originalNameTable;

    // this is never cleared so in some cases may collect topics that aren't anymore visible,
    // this might result in some unnecessary refreshing
    protected HashSet<Locator> visibleTopics;
    protected HashSet<Locator> openTopic;

    protected boolean needsRefresh;




    /** Creates a new instance of AbstractTraditionalTopicPanel **/
    public AbstractTraditionalTopicPanel() {
        visibleTopics=new HashSet<Locator>();
        openTopic=new HashSet<Locator>();
        needsRefresh=false;
    }


    public void toggleVisibility(String componentName) {}


    public boolean supportsOpenTopic() {
        return true;
    }
    


    // -------------------------------------------------------------------------
    // --------------------------------------------------------- Mouse Event ---
    // -------------------------------------------------------------------------




    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        mouseEvent.consume();
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        mouseEvent.consume();
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        mouseEvent.consume();
    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        mouseEvent.consume();
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        mouseEvent.consume();
    }





    // -------------------------------------------------------------------------
    // ----------------------------------------------------------- Build GUI ---
    // -------------------------------------------------------------------------



    public void buildAssociationsPanel(JPanel associationPanel, JLabel numberLabel, Topic topic, Options options, Wandora wandora) {
        buildAssociationsPanel(associationPanel, numberLabel, topic, ASSOCIATIONS_WHERE_PLAYER, options, wandora);

    }

    public void buildAssociationsPanel(JPanel associationPanel, JLabel numberLabel, Topic topic, int option, Options options, Wandora wandora) {
        java.awt.GridBagConstraints gbc;
        try {
            Map<Association,ArrayList<Topic>> selectedAssociationWithRoles = new HashMap<Association,ArrayList<Topic>>();
            Map<Topic,java.util.List<? extends RowSorter.SortKey>> sortKeys = new HashMap<>();
            Map<Topic,ArrayList<Integer>> columnProperties = new HashMap<>();
            if(associationPanel.getComponentCount() > 0) {
                for(int i=0; i<associationPanel.getComponentCount(); i++) {
                    Component associationTableComponent = associationPanel.getComponent(i);
                    if(associationTableComponent != null && associationTableComponent instanceof AssociationTable) {
                        AssociationTable associationTable = (AssociationTable) associationTableComponent;
                        selectedAssociationWithRoles.putAll(associationTable.getSelectedAssociationsWithSelectedRoles());
                        Topic associationTypeTopic = associationTable.getAssociationTypeTopic();
                        sortKeys.put(associationTypeTopic, associationTable.getRowSorter().getSortKeys());
                        ArrayList<Integer> columnProps = new ArrayList<Integer>();
                        TableColumnModel tcm = associationTable.getColumnModel();
                        for(int j=0; j<associationTable.getColumnCount(); j++) {
                            TableColumn tc = tcm.getColumn(j);
                            columnProps.add(tc.getModelIndex());
                        }
                        columnProperties.put(associationTypeTopic, columnProps);
                    }
                }
            }
            
            associationPanel.removeAll();
            Collection<Association> associations=null;

            associations = new ArrayList<>();
            if((option & ASSOCIATIONS_WHERE_PLAYER) != 0) associations.addAll(topic.getAssociations());
            if((option & ASSOCIATIONS_WHERE_TYPE) != 0) associations.addAll(topic.getTopicMap().getAssociationsOfType(topic));
            associations = TMBox.sortAssociations(associations, "en");
            associationPanel.setLayout(new java.awt.GridBagLayout());
            List<Association> sameTypes=new ArrayList<>();
            Topic lastType=null;
            Iterator<Association> iter=associations.iterator();
            int acounter=0;
            while(iter.hasNext()){
                Association a=(Association)iter.next();
                if(lastType==null || a.getType()==lastType) {
                    if(lastType==null) lastType=a.getType();
                    sameTypes.add(a);
                }
                else {
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridy=acounter++;
                    gbc.gridx=0;
                    gbc.anchor=GridBagConstraints.WEST;
                    gbc.weightx=1.0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    if(acounter!=1) gbc.insets=new java.awt.Insets(10,0,0,0);
                    AssociationTable table=new AssociationTable(sameTypes, wandora, topic);
                    
                    table.setBorder(leftTableBorder);
                    table.getTableHeader().setBorder(leftTopTableBorder);
                    
                    AssociationTypeLink label=new AssociationTypeLink(table, lastType, wandora);
                    label.setFont(UIConstants.h3Font);
                    label.setLimitLength(false);
                    JPopupMenu popup = this.getAssociationTypeMenu();
                    label.setComponentPopupMenu(popup);
                    associationPanel.add(label,gbc);
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridy=acounter++;
                    gbc.gridx=0;
                    gbc.weightx=1.0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    associationPanel.add(table.getTableHeader(),gbc);
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridy=acounter++;
                    gbc.gridx=0;
                    gbc.weightx=1.0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    associationPanel.add(table,gbc);
                    lastType=a.getType();
                    sameTypes=new ArrayList<>();
                    sameTypes.add(a);
                }
                {
                    visibleTopics.addAll(a.getType().getSubjectIdentifiers());
                    for(Topic role : a.getRoles()){
                        visibleTopics.addAll(role.getSubjectIdentifiers());
                        visibleTopics.addAll(a.getPlayer(role).getSubjectIdentifiers());
                    }
                }
            }
            if(!sameTypes.isEmpty()) {
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=acounter++;
                gbc.gridx=0;
                gbc.anchor=GridBagConstraints.WEST;
                gbc.weightx=1.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                if(acounter!=1) gbc.insets=new java.awt.Insets(10,0,0,0);
                AssociationTable table=new AssociationTable(sameTypes, wandora, topic);
                AssociationTypeLink label=new AssociationTypeLink(table, lastType, wandora);
                label.setFont(UIConstants.h3Font);
                label.setLimitLength(false);
                JPopupMenu popup = this.getAssociationTypeMenu();
                label.setComponentPopupMenu(popup);
                associationPanel.add(label,gbc);
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=acounter++;
                gbc.gridx=0;
                gbc.weightx=1.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                
                table.getTableHeader().setBorder(leftTopTableBorder);
                table.setBorder(leftTableBorder);
                
                associationPanel.add(table.getTableHeader(),gbc);
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=acounter++;
                gbc.gridx=0;
                gbc.weightx=1.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                associationPanel.add(table,gbc);
            }

            // ----

            int n = associations.size();
            if(numberLabel != null) {
                numberLabel.setText(""+n);
            }
            setParentVisibility(associationPanel, n);
            
            // Finally select all previously selected association.
            for(int i=0; i<associationPanel.getComponentCount(); i++) {
                Component associationTableComponent = associationPanel.getComponent(i);
                if(associationTableComponent != null && associationTableComponent instanceof AssociationTable) {
                    AssociationTable associationTable = (AssociationTable) associationTableComponent;
                    Topic associationTypeTopic = associationTable.getAssociationTypeTopic();
                    java.util.List<? extends RowSorter.SortKey> sortKeysForAssociationType = sortKeys.get(associationTypeTopic);
                    if(sortKeysForAssociationType != null) {
                        associationTable.getRowSorter().setSortKeys(sortKeysForAssociationType);
                    }
                    TableColumnModel tcm = associationTable.getColumnModel();
                    ArrayList<Integer> columnProps = columnProperties.get(associationTypeTopic);
                    if(columnProps != null && !columnProps.isEmpty()) {
                        TableColumn[] sortedTableColumns = new TableColumn[columnProps.size()];
                        for(int j=0; j<columnProps.size(); j++) {
                            if(j < tcm.getColumnCount()) {
                                Integer index = columnProps.get(j);
                                TableColumn tc = tcm.getColumn(j);
                                sortedTableColumns[index] = tc;
                            }
                        }
                        while (tcm.getColumnCount() > 0) {
                            tcm.removeColumn(tcm.getColumn(0));
                        }
                        for(int j=0; j<columnProps.size(); j++) {
                            if(sortedTableColumns[j] != null) {
                                tcm.addColumn(sortedTableColumns[j]);
                            }
                        }
                    }
                    if(!selectedAssociationWithRoles.isEmpty()) {
                        associationTable.selectAssociations(selectedAssociationWithRoles);
                    }
                }
            }
        }
        catch(Exception e) {
            System.out.println("Failed to initialize associationss!");
            e.printStackTrace();
        }
    }




    public void buildClassesPanel(JPanel classesPanel, JLabel numberLabel, Topic topic, Options options, Wandora wandora) {
        GridBagConstraints gbc;
        try {
            Topic[] selectedClasses = null;
            if(classesPanel.getComponentCount() > 0) {
                Component oldClassTableComponent = classesPanel.getComponent(0);
                if(oldClassTableComponent != null && oldClassTableComponent instanceof ClassTable) {
                    ClassTable oldClassTable = (ClassTable) oldClassTableComponent;
                    selectedClasses = oldClassTable.getSelectedTopics();
                }
            }
            classesPanel.removeAll();
            classesPanel.setLayout(new java.awt.GridBagLayout());
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            gbc.weightx=1.0;
            ClassTable ct = new ClassTable(topic, wandora);
            ct.setBorder(leftTopTableBorder);
            
            classesPanel.add(ct, gbc);

            int n = ct.getRowCount();
            if(numberLabel != null) {
                numberLabel.setText(""+n);
            }
            setParentVisibility(classesPanel, n);
            
            {
                for(Topic t : topic.getTypes()){
                    visibleTopics.addAll(t.getSubjectIdentifiers());
                }
            }
            
            if(selectedClasses != null && selectedClasses.length > 0) {
                ct.selectTopics(selectedClasses);
            }
        }
        catch(Exception e) {
            System.out.println("Failed to classes instances!");
            e.printStackTrace();
        }
    }




    public void buildSubjectIdentifierPanel(JPanel subjectIdentifierPanel, Topic topic, Options options, Wandora wandora) {
        try {
            Locator[] selectedSIs = null;
            if(subjectIdentifierPanel.getComponentCount() > 0) {
                Component oldSITableComponent = subjectIdentifierPanel.getComponent(0);
                if(oldSITableComponent != null && oldSITableComponent instanceof SITable) {
                    SITable oldSITable = (SITable) oldSITableComponent;
                    selectedSIs = oldSITable.getSelectedLocators();
                }
            }
            GridBagConstraints gbc;
            subjectIdentifierPanel.removeAll();
            subjectIdentifierPanel.setLayout(new java.awt.GridBagLayout());
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            gbc.weightx=1.0;
            SITable sit = new SITable(topic, wandora);
            sit.setBorder(leftTopTableBorder);
            subjectIdentifierPanel.add(sit,gbc);

            visibleTopics.addAll(topic.getSubjectIdentifiers());
            openTopic.addAll(topic.getSubjectIdentifiers());
            
            if(selectedSIs != null && selectedSIs.length > 0) {
                sit.selectLocators(selectedSIs);
            }
        }
        catch(Exception e) {
            System.out.println("Failed to initialize sis!");
            e.printStackTrace();
        }
    }




    public void buildInstancesPanel(JPanel instancesPanel, JLabel numberLabel, Topic topic, Options options, Wandora wandora) {
        try {
            Topic[] selectedInstances = null;
            if(instancesPanel.getComponentCount() > 0) {
                Component oldInstanceTableComponent = instancesPanel.getComponent(0);
                if(oldInstanceTableComponent != null && oldInstanceTableComponent instanceof InstanceTable) {
                    InstanceTable oldInstanceTable = (InstanceTable) oldInstanceTableComponent;
                    selectedInstances = oldInstanceTable.getSelectedTopics();
                }
            }
            GridBagConstraints gbc;
            instancesPanel.removeAll();
            instancesPanel.setLayout(new java.awt.GridBagLayout());
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            gbc.weightx=1.0;
            InstanceTable it = new InstanceTable(topic, wandora);
            it.setBorder(leftTopTableBorder);
            instancesPanel.add(it,gbc);

            int n = it.getRowCount();
            if(numberLabel != null) {
                numberLabel.setText(""+n);
            }
            setParentVisibility(instancesPanel, n);
            
            {
                for(Topic t : topic.getTopicMap().getTopicsOfType(topic)){
                    visibleTopics.addAll(t.getSubjectIdentifiers());
                }
            }
            if(selectedInstances != null && selectedInstances.length > 0) {
                it.selectTopics(selectedInstances);
            }
        }
        catch(Exception e) {
            System.out.println("Failed to initialize instances!");
            e.printStackTrace();
        }
    }



    
    public void buildOccurrencesPanel(JPanel dataPanel, JLabel numberLabel, Topic topic, Options options, Wandora wandora) {
        try {
            GridBagConstraints gbc;
            dataPanel.removeAll();
            dataPanel.setLayout(new java.awt.GridBagLayout());
            occurrenceTables = new ArrayList<>();
            int n=0;
            int y=0;
            gbc=new java.awt.GridBagConstraints();
            for(Topic occurrenceType : topic.getDataTypes()) {
                OccurrenceTableSingleType occurrenceTableSingle = new OccurrenceTableSingleType(topic, occurrenceType, options, wandora);
                occurrenceTableSingle.setBorder(leftTableBorder);
                occurrenceTableSingle.getTableHeader().setBorder(leftTopTableBorder);
                if(occurrenceTableSingle.getRowCount() > 0) {
                    OccurrenceTypeLink label = new OccurrenceTypeLink(occurrenceTableSingle, occurrenceType, wandora);
                    label.setFont(UIConstants.h3Font);
                    label.setLimitLength(false);
                    JPopupMenu popup = this.getOccurrenceTypeMenu(occurrenceType);
                    label.setComponentPopupMenu(popup);

                    gbc.gridx=0;
                    gbc.gridy=y++;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=1.0;

                    if(y > 1) {
                        gbc.insets=new java.awt.Insets(10,0,0,0);
                    }
                    else {
                        gbc.insets=new java.awt.Insets(0,0,0,0);
                    }

                    dataPanel.add(label,gbc);
                    gbc.gridy=y++;
                    gbc.insets=new java.awt.Insets(0,0,0,0);
                    dataPanel.add(occurrenceTableSingle.getTableHeader(),gbc);
                    gbc.gridy=y++;
                    dataPanel.add(occurrenceTableSingle,gbc);
                }
                {
                    visibleTopics.addAll(occurrenceType.getSubjectIdentifiers());
                    for(Topic version : topic.getData(occurrenceType).keySet()) {
                        visibleTopics.addAll(version.getSubjectIdentifiers());
                        n++;
                    }
                }
            }

            if(numberLabel != null) {
                numberLabel.setText(""+n);
            }
            setParentVisibility(dataPanel, n);
        }
        catch(Exception e) {
            System.out.println("Failed to initialize occurrences!");
            e.printStackTrace();
        }
    }



    public void buildOccurrencesPanelOld(JPanel dataPanel, JLabel numberLabel, Topic topic, Options options, Wandora wandora) {
        try {
            GridBagConstraints gbc;
            dataPanel.removeAll();
            dataPanel.setLayout(new java.awt.GridBagLayout());
            occurrenceTable = null;
            int n = topic.getDataTypes().size();
            if(n > 0) {
                occurrenceTable=new OccurrenceTableAll(topic, options, wandora);
                gbc=new java.awt.GridBagConstraints();
                gbc.gridx=0;
                gbc.gridy=0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                gbc.weightx=1.0;
                dataPanel.add(occurrenceTable.getTableHeader(),gbc);
                gbc=new java.awt.GridBagConstraints();
                gbc.gridx=0;
                gbc.gridy=1;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                gbc.weightx=1.0;
                dataPanel.add(occurrenceTable,gbc);
                {
                    for(Topic type : topic.getDataTypes()) {
                        visibleTopics.addAll(type.getSubjectIdentifiers());
                        for(Topic version : topic.getData(type).keySet()) {
                            visibleTopics.addAll(version.getSubjectIdentifiers());
                        }
                    }
                }
            }
            else occurrenceTable=null;
            
            if(numberLabel != null) {
                numberLabel.setText(""+n);
            }
            setParentVisibility(dataPanel, n);
        }
        catch(Exception e) {
            System.out.println("Failed to initialize occurrences!");
            e.printStackTrace();
        }
    }





    public void buildNamePanel(JPanel variantPanel, JLabel numberLabel, Topic topic, TopicPanel topicPanel, Options options, Wandora wandora) {
        buildAllNamesPanel(variantPanel, numberLabel, topic, topicPanel, options, wandora);
        //buildHorizontalNamePanel(variantPanel, parent, topic);
    }




    public void buildHorizontalNamePanel(JPanel variantPanel, JLabel numberLabel, Topic topic, final TopicPanel topicPanel, Options options, Wandora wandora) {
        try {
            TopicMap tm = wandora.getTopicMap();
            GridBagConstraints gbc = new java.awt.GridBagConstraints();
            nameTable=new LinkedHashMap<Set<Topic>,SimpleField>();
            invNameTable=new LinkedHashMap<SimpleField,Set<Topic>>();
            originalNameTable=new IteratedMap<Collection<Topic>,String>();
            variantPanel.removeAll();

            Topic[] langTopics = null;
            Topic[] verTopics = null;

            String[] langs = TMBox.getLanguageSIs(tm);
            String[] vers = TMBox.getNameVersionSIs(tm);
            langTopics = tm.getTopics(langs);
            verTopics = tm.getTopics(vers);

            variantPanel.setLayout(new java.awt.GridBagLayout());

            int x = 1;
            for(Topic langTopic : langTopics) {
                if(langTopic != null) {
                    gbc.gridx=x;
                    gbc.gridy=0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=1.0;
                    variantPanel.add(new TopicLink(langTopic, wandora), gbc);
                    x++;
                }
            }
            int y = 1;
            for(Topic verTopic : verTopics) {
                if(verTopic != null) {
                    gbc.gridx=0;
                    gbc.gridy=y;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=0.0;
                    gbc.insets = new Insets(0,0,0,10);
                    variantPanel.add(new TopicLink(verTopic, wandora), gbc);
                    x = 1;
                    for (Topic langTopic : langTopics) {
                        if (langTopic != null) {
                            Set<Topic> s=new LinkedHashSet<>();
                            s.add(verTopic);
                            s.add(langTopic);
                            String name=topic.getVariant(s);
                            SimpleField field = new SimpleField(name==null ? "" : name);
                            field.setCaretPosition(0);
                            //field.setPreferredSize(new java.awt.Dimension(130,21));
                            field.addKeyListener( new KeyAdapter() {
                                @Override
                                public void keyReleased(java.awt.event.KeyEvent evt) {
                                    try {
                                        if(topicPanel != null && evt.getKeyCode()==KeyEvent.VK_ENTER) {
                                            topicPanel.applyChanges();
                                        }
                                    }
                                    catch(Exception e) {}
                                }
                            });
                            field.addFocusListener( new FocusAdapter() {
                                private String originalText = null;
                                
                                @Override
                                public void focusGained(FocusEvent e) {
                                    originalText = ((SimpleField) e.getComponent()).getText();
                                    e.getComponent().setBackground(UIConstants.editableBackgroundColor);
                                }
                                @Override
                                public void focusLost(FocusEvent e) {
                                    try {
                                        String modifiedText = ((SimpleField) e.getComponent()).getText();
                                        if(!originalText.equals(modifiedText) && topicPanel != null) {
                                            topicPanel.applyChanges();
                                            e.getComponent().setBackground(UIConstants.editableBackgroundColor);
                                        }
                                        else {
                                            if(originalText.length() == 0) {
                                                e.getComponent().setBackground(UIConstants.noContentBackgroundColor);
                                            }
                                        }
                                    }
                                    catch(Exception ex) {
                                        
                                    }
                                }
                            });
                            if(name == null) {
                                field.setBackground(UIConstants.noContentBackgroundColor);
                            }
                            Color c = wandora.topicHilights.getVariantColor(topic,s);
                            if(c!=null) field.setForeground(c);
                            nameTable.put(s,field);
                            invNameTable.put(field,s);
                            originalNameTable.put(s,field.getText());
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
            if(numberLabel != null) {
                numberLabel.setText(""+n);
            }
            setParentVisibility(variantPanel, n);
        }
        catch(Exception e) {
            System.out.println("Failed to initialize names!");
            e.printStackTrace();
        }
    }





    public void buildVerticalNamePanel(JPanel variantPanel, JLabel numberLabel, Topic topic, final TopicPanel topicPanel, Options options, Wandora wandora) {
        if(wandora == null) return;
        if(variantPanel == null) return;
        if(topic == null) return;
        
        try {
            TopicMap tm = wandora.getTopicMap();
            GridBagConstraints gbc = new java.awt.GridBagConstraints();
            Insets rightGapInsets = new Insets(0,0,0,10);
            Insets noGapsInsets = new Insets(0,0,0,0);
            Dimension defaultFieldSize = new java.awt.Dimension(100,27);
            
            nameTable = new HashMap<Set<Topic>,SimpleField>();
            invNameTable = new HashMap<SimpleField,Set<Topic>>();
            originalNameTable = new IteratedMap<Collection<Topic>,String>();
            variantPanel.removeAll();

            Topic[] langTopics = null;
            Topic[] verTopics = null;

            String[] langs = TMBox.getLanguageSIs(tm);
            String[] vers = TMBox.getNameVersionSIs(tm);
            langTopics = tm.getTopics(langs);
            verTopics = tm.getTopics(vers);
            variantPanel.setLayout(new java.awt.GridBagLayout());

            int x = 1;
            for(Topic verTopic : verTopics) {
                if(verTopic != null) {
                    gbc.gridx=x;
                    gbc.gridy=0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    gbc.weightx=0.0;
                    variantPanel.add(new TopicLink(verTopic, wandora), gbc);
                    x++;
                }
            }
            int y = 1;
            for(Topic langTopic : langTopics) {
                if(langTopic != null) {
                    gbc.gridx=0;
                    gbc.gridy=y;
                    gbc.fill=GridBagConstraints.NONE;
                    gbc.weightx=0.0;
                    gbc.anchor=GridBagConstraints.WEST;
                    gbc.insets = rightGapInsets;
                    variantPanel.add(new TopicLink(langTopic, wandora), gbc);
                    x = 1;
                    for(Topic verTopic : verTopics) {
                        if(verTopic != null) {
                            Set<Topic> s = new LinkedHashSet<>();
                            s.add(verTopic);
                            s.add(langTopic);
                            String name = topic.getVariant(s);
                            SimpleField field = new SimpleField(name==null ? "" : name);
                            field.setCaretPosition(0);
                            field.setPreferredSize(defaultFieldSize);
                            
                            field.addKeyListener( new KeyAdapter() {
                                @Override
                                public void keyReleased(java.awt.event.KeyEvent evt) {
                                    try {
                                        if(topicPanel != null && evt.getKeyCode()==KeyEvent.VK_ENTER) {
                                            topicPanel.applyChanges();
                                        }
                                    }
                                    catch(Exception e) {}
                                }
                            });
                            field.addFocusListener( new FocusAdapter() {
                                private String originalText = null;
                                
                                @Override
                                public void focusGained(FocusEvent e) {
                                    originalText = ((SimpleField) e.getComponent()).getText();
                                    e.getComponent().setBackground(UIConstants.editableBackgroundColor);
                                }
                                @Override
                                public void focusLost(FocusEvent e) {
                                    try {
                                        String modifiedText = ((SimpleField) e.getComponent()).getText();
                                        if(!originalText.equals(modifiedText) && topicPanel != null) {
                                            topicPanel.applyChanges();
                                            e.getComponent().setBackground(UIConstants.editableBackgroundColor);
                                        }
                                        else {
                                            if(originalText.length() == 0) {
                                                e.getComponent().setBackground(UIConstants.noContentBackgroundColor);
                                            }
                                        }
                                    }
                                    catch(Exception ex) {}
                                }
                            });
                            if(name == null) {
                                field.setBackground(UIConstants.noContentBackgroundColor);
                            }
                            Color c = wandora.topicHilights.getVariantColor(topic,s);
                            if(c != null) field.setForeground(c);
                            nameTable.put(s,field);
                            invNameTable.put(field,s);
                            originalNameTable.put(s,field.getText());

                            gbc.gridx=x;
                            gbc.gridy=y;
                            gbc.fill=GridBagConstraints.HORIZONTAL;
                            gbc.weightx=1.0;
                            gbc.insets = noGapsInsets;
                            variantPanel.add(field,gbc);
                            x++;
                        }
                    }
                    y++;
                }
            }

            int n = topic.getVariantScopes().size();
            if(numberLabel != null) {
                numberLabel.setText(""+n);
            }
            setParentVisibility(variantPanel, n);
        }
        catch(Exception e) {
            System.out.println("Failed to initialize names!");
            e.printStackTrace();
        }
    }







    public void buildAllNamesPanel(JPanel variantPanel, JLabel numberLabel, final Topic topic, final TopicPanel topicPanel, Options options, final Wandora wandora) {
        if(topic != null) {
            try {
                nameTable=new LinkedHashMap<Set<Topic>,SimpleField>();
                invNameTable=new LinkedHashMap<SimpleField,Set<Topic>>();
                originalNameTable=new IteratedMap<Collection<Topic>,String>();
                variantPanel.removeAll();

                JPanel myVariantPanel = variantPanel;
                myVariantPanel.setLayout(new GridBagLayout());
                Set<Set<Topic>> scopes = topic.getVariantScopes();
                int i = 0;
                for(Set<Topic> scope : scopes) {
                    JPanel scopeNamePanel = new JPanel();
                    scopeNamePanel.setLayout(new GridBagLayout());
                    java.awt.GridBagConstraints gbcs = new java.awt.GridBagConstraints();
                    gbcs.gridx=0;
                    gbcs.gridy=0;
                    gbcs.fill=GridBagConstraints.HORIZONTAL;
                    gbcs.weightx=1.0;
                    gbcs.insets = new Insets(0,10,0,0);
                    SimpleField nf = new SimpleField();
                    nf.addKeyListener( new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent evt) {
                            try {
                                if(topicPanel != null && evt.getKeyCode()==KeyEvent.VK_ENTER) {
                                    topicPanel.applyChanges();
                                }
                            }
                            catch(Exception e) {}
                        }
                    });
                    nf.setPreferredSize(new Dimension(100,27));
                    nf.setMinimumSize(new Dimension(100,27));
                    String variant = topic.getVariant(scope);
                    nf.setText(variant);
                    scopeNamePanel.add(nf, gbcs);

                    JPanel buttonPanel = new JPanel();
                    buttonPanel.setPreferredSize(new Dimension(30, 23));
                    buttonPanel.setMinimumSize(new Dimension(30, 23));
                    buttonPanel.setMaximumSize(new Dimension(30, 23));
                    buttonPanel.setLayout(new FlowLayout(0));
                    SimpleButton deleteVariantButton = new SimpleButton(UIBox.getIcon("gui/icons/delete_variant.png"));
                    deleteVariantButton.setPreferredSize(new Dimension(16, 16));
                    deleteVariantButton.setBackground(UIConstants.buttonBarBackgroundColor);
                    deleteVariantButton.setForeground(UIConstants.buttonBarLabelColor);
                    deleteVariantButton.setOpaque(true);
                    deleteVariantButton.setBorderPainted(false);
                    deleteVariantButton.setToolTipText("Delete variant name.");

                    final DeleteVariantName tool = new DeleteVariantName(topic, scope);
                    deleteVariantButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            try {
                                tool.execute(wandora);
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

                        TopicLink scopeTopicLabel = new TopicLink(scopeTopic, wandora);
                        scopeTopicLabel.setLimitLength(false);
                        scopeTopicLabel.setText(scopeTopic);
                        scopeTopicLabel.setToolTipText(scopeTopic.getOneSubjectIdentifier().toExternalForm());

                        ArrayList<Object> addScopeSubmenu = new ArrayList<>();
                        addScopeSubmenu.add("Add scope topic...");
                        addScopeSubmenu.add(new AddScopeTopicToVariantName(topic, scope));
                        addScopeSubmenu.add("---");
                        String[] vers=TMBox.getNameVersionSIs(wandora.getTopicMap());
                        for(int k=0; k<vers.length; k++) {
                            Topic lt = wandora.getTopicMap().getTopic(vers[k]);
                            String ltName = TopicToString.toString(lt);
                            addScopeSubmenu.add("Add scope topic '"+ltName+"'");
                            addScopeSubmenu.add(new AddScopeTopicToVariantName(topic, scope, lt));
                        }
                        addScopeSubmenu.add("---");
                        String[] langs=TMBox.getLanguageSIs(wandora.getTopicMap());
                        for(int k=0; k<langs.length; k++) {
                            Topic lt = wandora.getTopicMap().getTopic(langs[k]);
                            String ltName = TopicToString.toString(lt);
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
                                wandora
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
                if(numberLabel != null) {
                    numberLabel.setText(""+n);
                }
                setParentVisibility(variantPanel, n);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        }

    }
    
    


    protected void setPanelTitle(JPanel panel, String title) {
        try {
            if(panel != null) {
                Border border = panel.getBorder();
                if(border == null) {
                    panel = (JPanel) panel.getParent();
                    if(panel != null) border = panel.getBorder();
                }
                if(border != null && border instanceof TitledBorder) {
                    ((TitledBorder) border).setTitle(title);
                    ((TitledBorder) border).setTitleFont(UIConstants.h2Font.deriveFont(Font.BOLD));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }





    public void refresh(){
        needsRefresh=false;
        visibleTopics.clear();
        openTopic.clear();
    }

    
    
    @Override
    public void doRefresh() throws TopicMapException {
        if(needsRefresh) {
            refresh();
            this.revalidate();
            this.repaint();
        }
    }



    // -------------------------------------------------------------------------
    // ------------------------------------------------------- Apply Changes ---
    // -------------------------------------------------------------------------



    public boolean applyChanges(final Topic topic, Wandora wandora) throws TopicMapException {
        try {
            boolean changed = false;
            if(nameTable != null) {
                Iterator<Map.Entry<Set<Topic>,SimpleField>> iter = nameTable.entrySet().iterator();
                String originalText = null;
                while(iter.hasNext()) {
                    Map.Entry<Set<Topic>,SimpleField> e = iter.next();
                    final Set<Topic> scope = e.getKey();
                    SimpleField field = e.getValue();
                    final String text = field.getText().trim();
                    originalText = (String) originalNameTable.get(scope);
                    if(originalText != null && originalText.equals(text)){
                        continue;
                    }
                    if(text.length() > 0) {
                        topic.setVariant(scope, text);
                    }
                    else if(originalText!=null) {
                        topic.removeVariant(scope);
                    }
                    changed = true;
                }
            }
            if(occurrenceTable != null) {
                if(occurrenceTable.applyChanges(topic,wandora)) {
                    changed = true;
                }
            }
            return changed;
        }
        catch(TopicMapReadOnlyException roe) {
            int button = wandora.displayExceptionYesNo("The layer is read only. You can either discard changes and proceed with current operation or cancel.",null,"Discard changes","Cancel");
            if(button==0) return false;
            else {
                roe.fillInStackTrace();
                throw roe;
            }
        }
    }

    
    
    private void setParentVisibility(JComponent component, int n) {
        Container parentComponent = component.getParent();
        if(parentComponent != null) {
            if(n == 0) {
                parentComponent.setVisible(false);
            }
            else {
                parentComponent.setVisible(true);
            }
        }
    }



    // -------------------------------------------------------------------------
    // -------------------------------------------------------------- Popups ---
    // -------------------------------------------------------------------------


    public abstract JPopupMenu getNamesMenu();
    public abstract JPopupMenu getClassesMenu();
    public abstract JPopupMenu getInstancesMenu();
    public abstract JPopupMenu getSIMenu();
    public abstract JPopupMenu getOccurrencesMenu();
    public abstract JPopupMenu getOccurrenceTypeMenu(Topic occurrenceType);
    public abstract JPopupMenu getSubjectMenu();
    public abstract JPopupMenu getAssociationsMenu();
    public abstract JPopupMenu getAssociationTypeMenu();

    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------- TopicMapListener ---
    // -------------------------------------------------------------------------

    
    
    private <T> boolean collectionsOverlap(Collection<T> a,Collection<T> b){
        if(a.size()>b.size()){
            Collection<T> c=a;
            a=b;
            b=c;
        }
        for(T o : a){
            if(b.contains(o)) return true;
        }
        return false;
    }



    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        if(needsRefresh) return;
        for(Topic role : a.getRoles()){
            if(collectionsOverlap(role.getSubjectIdentifiers(),visibleTopics)) needsRefresh=true;
            Topic player=a.getPlayer(role);
            if(collectionsOverlap(player.getSubjectIdentifiers(),visibleTopics)) needsRefresh=true;
        }
    }



    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        if(needsRefresh) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
    }



    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        if(needsRefresh) return;
        if(t==null) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),visibleTopics)) needsRefresh=true;
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        if(needsRefresh) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),visibleTopics)) needsRefresh=true;
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        if(needsRefresh) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        if(needsRefresh) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),visibleTopics)) needsRefresh=true;
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(needsRefresh) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),visibleTopics)) needsRefresh=true;
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        if(needsRefresh) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),visibleTopics)) needsRefresh=true;
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        if(needsRefresh) return;
        for(Topic role : a.getRoles()){
            if(collectionsOverlap(role.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
            Topic player=a.getPlayer(role);
            if(collectionsOverlap(player.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
        }
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        if(a==null) return;
        if(needsRefresh) return;
        if(newPlayer!=null && collectionsOverlap(newPlayer.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
        if(oldPlayer!=null && collectionsOverlap(oldPlayer.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
        if(needsRefresh) return;
        for(Topic r : a.getRoles()){
            if(collectionsOverlap(r.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
            Topic player=a.getPlayer(r);
            if(collectionsOverlap(player.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
        }
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        if(needsRefresh) return;
        needsRefresh=true;
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        if(needsRefresh) return;
        if(collectionsOverlap(t.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
        if(added!=null && collectionsOverlap(added.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
        if(removed!=null && collectionsOverlap(removed.getSubjectIdentifiers(),openTopic)) needsRefresh=true;
    }

    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------- TransferHandlers ---
    // -------------------------------------------------------------------------
    
    

    protected class AssociationTableTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;
		private TopicPanel topicPanel = null;
        private Wandora wandora = null;
        
        
        
        public AssociationTableTransferHandler(TopicPanel topicPanel) {
            this.topicPanel = topicPanel;
            this.wandora = Wandora.getWandora();
        }
        
        
        
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
                
                Topic targetTopic = topicPanel.getTopic();
                if(targetTopic != null && !targetTopic.isRemoved()) {
                    boolean associationAdded = false;
                    Collection<Topic> targetTopicTypes = targetTopic.getTypes();
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
                wandora.handleError(ce);
            }
            return false;
        }

    }

    
    
    
    
    protected class InstancesPanelTransferHandler extends TransferHandler {


		private static final long serialVersionUID = 1L;
		private TopicPanel topicPanel = null;
        private Wandora wandora = null;
        
        
        
        public InstancesPanelTransferHandler(TopicPanel topicPanel) {
            this.topicPanel = topicPanel;
            this.wandora = Wandora.getWandora();
        }
        
        
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
                TopicMap tm = wandora.getTopicMap();
                ArrayList<Topic> topics = DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                Topic base = topicPanel.getTopic();
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
                wandora.handleError(ce);
            }
            return false;
        }

    }
    
    

    
    protected class ClassesPanelTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;
		private TopicPanel topicPanel = null;
        private Wandora wandora = null;
        
        
        
        public ClassesPanelTransferHandler(TopicPanel topicPanel) {
            this.topicPanel = topicPanel;
            this.wandora = Wandora.getWandora();
        }
        
        
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
                TopicMap tm = wandora.getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;
                Topic base = topicPanel.getTopic();
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
                wandora.handleError(ce);
            }
            return false;
        }

    }
    

    
    
    protected class OccurrencesPanelTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;
		private TopicPanel topicPanel = null;
        private Wandora wandora = null;
        
        
        
        public OccurrencesPanelTransferHandler(TopicPanel topicPanel) {
            this.topicPanel = topicPanel;
            this.wandora = Wandora.getWandora();
        }
        
        
        
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
                    Topic base = topicPanel.getTopic();
                    if(base==null) return false;
                    TopicMap tm = wandora.getTopicMap();
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
                wandora.handleError(ce);
            }
            return false;
        }

    }

    
    
    
    protected class TopicPanelTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;
		private TopicPanel topicPanel = null;
        private Wandora wandora = null;
        
        
        
        public TopicPanelTransferHandler(TopicPanel topicPanel) {
            this.topicPanel = topicPanel;
            this.wandora = Wandora.getWandora();
        }
        
        
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
                wandora.handleError(ce);
            }
            return false;
        }

    }
    




}
