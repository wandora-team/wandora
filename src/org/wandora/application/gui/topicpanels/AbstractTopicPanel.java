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
 * AbstractTopicPanel.java
 *
 * Created on 19. lokakuuta 2005, 16:32
 *
 */

package org.wandora.application.gui.topicpanels;



import org.wandora.application.gui.table.ClassTable;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.InstanceTable;
import java.util.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


import org.wandora.application.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.topicnames.AddScopeTopicToVariantName;
import org.wandora.application.tools.topicnames.DeleteScopeTopicInVariantName;
import org.wandora.application.tools.topicnames.DeleteVariantName;




/**
 *
 * @author akivela
 */
public abstract class AbstractTopicPanel extends JPanel implements Printable, MouseListener, TopicMapListener, RefreshListener  {

    public static final String VARIANT_GUITYPE_SCHEMA = "schema";
    public static final String VARIANT_GUITYPE_ALL = "all";


    public String variantGUIType = VARIANT_GUITYPE_SCHEMA;

    public static final String OPTIONS_PREFIX = "gui.traditionalTopicPanel.";
    public static final String OPTIONS_VIEW_PREFIX = OPTIONS_PREFIX + "view.";
    public static final String VARIANT_GUITYPE_OPTIONS_KEY = OPTIONS_PREFIX + "variant_gui";


    public int ASSOCIATIONS_WHERE_PLAYER = 1;
    public int ASSOCIATIONS_WHERE_TYPE = 2;


    protected OccurrenceTable occurrenceTable;
    protected HashMap<Set<Topic>,SimpleField> nameTable;
    protected HashMap<SimpleField,Set<Topic>> invNameTable;
    protected IteratedMap<Collection<Topic>,String> originalNameTable;

    // this is never cleared so in some cases may collect topics that aren't anymore visible,
    // this might result in some unnecessary refreshing
    protected HashSet<Locator> visibleTopics;
    protected HashSet<Locator> openTopic;

    protected boolean needsRefresh;




    /** Creates a new instance of AbstractTopicPanel */
    public AbstractTopicPanel() {
        visibleTopics=new HashSet<Locator>();
        openTopic=new HashSet<Locator>();
        needsRefresh=false;
    }


    public void toggleVisibility(String componentName) {}




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



    public void buildAssociationsPanel(JPanel associationPanel, Wandora parent, Topic topic) {
        buildAssociationsPanel(associationPanel, parent, topic, ASSOCIATIONS_WHERE_PLAYER);

    }

    public void buildAssociationsPanel(JPanel associationPanel, Wandora parent, Topic topic, int options) {
        java.awt.GridBagConstraints gbc;
        try {
            Map<Association,ArrayList<Topic>> selectedAssociationWithRoles = new HashMap<Association,ArrayList<Topic>>();
            Map<Topic,java.util.List<? extends RowSorter.SortKey>> sortKeys = new HashMap();
            Map<Topic,ArrayList<Integer>> columnProperties = new HashMap();
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
            Collection associations=null;

            associations = new ArrayList();
            if((options & ASSOCIATIONS_WHERE_PLAYER) != 0) associations.addAll(topic.getAssociations());
            if((options & ASSOCIATIONS_WHERE_TYPE) != 0) associations.addAll(topic.getTopicMap().getAssociationsOfType(topic));
            associations = TMBox.sortAssociations(associations, "en");
            associationPanel.setLayout(new java.awt.GridBagLayout());
            ArrayList sameTypes=new ArrayList();
            Topic lastType=null;
            Iterator iter=associations.iterator();
            int acounter=0;
            while(iter.hasNext()){
                Association a=(Association)iter.next();
                if(lastType==null || a.getType()==lastType) {
                    if(lastType==null) lastType=a.getType();
                    sameTypes.add(a);
                }
                else{
                    gbc=new java.awt.GridBagConstraints();
                    gbc.gridy=acounter++;
                    gbc.gridx=0;
                    gbc.anchor=GridBagConstraints.WEST;
                    gbc.weightx=1.0;
                    gbc.fill=GridBagConstraints.HORIZONTAL;
                    if(acounter!=1) gbc.insets=new java.awt.Insets(10,0,0,0);
                    AssociationTable table=new AssociationTable(sameTypes, parent, topic);
                    AssociationTypeLink label=new AssociationTypeLink(table, lastType, parent);
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
                    sameTypes=new ArrayList();
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
            if(!sameTypes.isEmpty()){
                gbc=new java.awt.GridBagConstraints();
                gbc.gridy=acounter++;
                gbc.gridx=0;
                gbc.anchor=GridBagConstraints.WEST;
                gbc.weightx=1.0;
                gbc.fill=GridBagConstraints.HORIZONTAL;
                if(acounter!=1) gbc.insets=new java.awt.Insets(10,0,0,0);
                AssociationTable table=new AssociationTable(sameTypes,parent,topic);
                AssociationTypeLink label=new AssociationTypeLink(table, lastType, parent);
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
            }

            // ----

            int n = associations.size();
            String s = "Associations";
            if((options & ASSOCIATIONS_WHERE_PLAYER) != 0) s = "Associations";
            if((options & ASSOCIATIONS_WHERE_TYPE) != 0) s = "Associations where type";
            setPanelTitle(associationPanel, s+" ("+n+")");
            
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
                            tcm.addColumn(sortedTableColumns[j]);
                        }
                    }
                    if(!selectedAssociationWithRoles.isEmpty()) {
                        associationTable.selectAssociations(selectedAssociationWithRoles);
                    }
                }
            }
        }
        catch(Exception e) {
            System.out.println("Failed to initialize associations!");
            e.printStackTrace();
        }
    }




    public void buildClassesPanel(JPanel classesPanel, Wandora parent, Topic topic) {
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
            ClassTable ct = new ClassTable(topic, parent);
            classesPanel.add(ct, gbc);

            int n = ct.getRowCount();
            setPanelTitle(classesPanel, "Classes ("+n+")");
           
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




    public void buildSubjectIdentifierPanel(JPanel subjectIdentifierPanel, Wandora parent, Topic topic) {
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
            SITable sit = new SITable(topic,parent);
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




    public void buildInstancesPanel(JPanel instancesPanel, Wandora parent, Topic topic) {
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
            InstanceTable it = new InstanceTable(topic,parent);
            instancesPanel.add(it,gbc);

            int n = it.getRowCount();
            setPanelTitle(instancesPanel, "Instances ("+n+")");

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





    public void buildOccurrencesPanel(JPanel dataPanel, Wandora parent, Topic topic) {
        try {
            GridBagConstraints gbc;
            dataPanel.removeAll();
            dataPanel.setLayout(new java.awt.GridBagLayout());
            occurrenceTable = null;
            int n = topic.getDataTypes().size();
            if(n > 0) {
                occurrenceTable=new OccurrenceTable(topic,parent);
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

            setPanelTitle(dataPanel, "Occurrences ("+n+")");
        }
        catch(Exception e) {
            System.out.println("Failed to initialize occurrences!");
            e.printStackTrace();
        }
    }





    public void buildNamePanel(JPanel variantPanel, Wandora parent, Topic topic) {
        buildHorizontalNamePanel(variantPanel, parent, topic);
    }







    public void buildHorizontalNamePanel(JPanel variantPanel, Wandora parent, Topic topic) {
        try {
            nameTable=new HashMap<Set<Topic>,SimpleField>();
            invNameTable=new HashMap<SimpleField,Set<Topic>>();
            originalNameTable=new IteratedMap<Collection<Topic>,String>();
            variantPanel.removeAll();

            Topic[] langTopics = null;
            Topic[] verTopics = null;

            TopicMap tm = parent.getTopicMap();
            
            String[] langs=TMBox.getLanguageSIs(tm);
            String[] vers=TMBox.getNameVersionSIs(tm);
            langTopics=tm.getTopics(langs);
            verTopics=tm.getTopics(vers);

            // variantPanel.setLayout(new java.awt.GridLayout(vers.length+1,langs.length+1));
            variantPanel.setLayout(new java.awt.GridBagLayout());
            // variantPanel.add(new javax.swing.JLabel(""));
            
            GridBagConstraints gbc=new java.awt.GridBagConstraints();
            gbc.gridx=GridBagConstraints.RELATIVE;
            gbc.gridy=0;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            gbc.weightx=1.0;
            
            int x = 1;
            for(int i=0; i<langTopics.length; i++) {
                if(langTopics[i] != null) {
                    variantPanel.add(new TopicLink(langTopics[i],parent),gbc);
                    x++;
                }
            }
            
            gbc=new java.awt.GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=GridBagConstraints.RELATIVE;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            gbc.weightx=0.0;
            gbc.insets = new Insets(0,0,0,10);
            
            int y = 1;
            for(int i=0; i<verTopics.length; i++) {
                if(verTopics[i] != null) {
                    variantPanel.add(new TopicLink(verTopics[i],parent),gbc);
                    x = 1;
                    for(int j=0; j<langTopics.length; j++) {
                        if(langTopics[j] != null) {
                            HashSet s=new HashSet(); 
                            s.add(verTopics[i]); 
                            s.add(langTopics[j]);
                            
                            String name=topic.getVariant(s);
                            SimpleField field = new SimpleField(name==null ? "" : name);
                            field.setCaretPosition(0);
                            // field.addKeyListener(this);
                            field.setPreferredSize(new java.awt.Dimension(80,19));

                            Color c=parent.topicHilights.getVariantColor(topic,s);
                            if(c != null) field.setForeground(c);

                            nameTable.put(s,field);
                            invNameTable.put(field,s);
                            originalNameTable.put(s,field.getText());
                            
                            GridBagConstraints gbc2=new java.awt.GridBagConstraints();
                            gbc2.gridx=x;
                            gbc2.gridy=y;
                            gbc2.fill=GridBagConstraints.HORIZONTAL;
                            gbc2.weightx=1.0;
                            gbc2.insets=new Insets(0,0,0,0);
                            variantPanel.add(field,gbc2);
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




    public void buildVerticalNamePanel(JPanel variantPanel, Wandora parent, Topic topic) {
        try {
            nameTable=new HashMap<Set<Topic>,SimpleField>();
            invNameTable=new HashMap<SimpleField,Set<Topic>>();
            originalNameTable=new IteratedMap<Collection<Topic>,String>();
            variantPanel.removeAll();
            
            TopicMap tm = parent.getTopicMap();

            String[] langs=TMBox.getLanguageSIs(tm);
            String[] vers=TMBox.getNameVersionSIs(tm);
            Topic[] langTopics=tm.getTopics(langs);
            Topic[] verTopics=tm.getTopics(vers);

            // variantPanel.setLayout(new java.awt.GridLayout(vers.length+1,langs.length+1));
            variantPanel.setLayout(new java.awt.GridBagLayout());
            // variantPanel.add(new javax.swing.JLabel(""));

            GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.gridx = GridBagConstraints.RELATIVE;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.0;
            int x = 1;
            for(int i=0;i<verTopics.length;i++) {
                if(verTopics[i] != null) {
                    variantPanel.add(new TopicLink(verTopics[i],parent),gbc);
                    x++;
                }
            }
            
            gbc = new java.awt.GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0,0,0,10);
            //gbc.fill=gbc.HORIZONTAL;
            //gbc.weightx=1.0;

            int y = 1;
            for(int j=0; j<langTopics.length; j++) {
                if(langTopics[j] != null) {
                    variantPanel.add(new TopicLink(langTopics[j],parent),gbc);
                    x = 1;
                    for(int i=0; i<verTopics.length; i++) {
                        if(verTopics[i]!=null) {
                            HashSet s=new HashSet(); 
                            s.add(verTopics[i]); 
                            s.add(langTopics[j]);
                            
                            String name=topic.getVariant(s);
                            SimpleField field = new SimpleField(name==null ? "" : name);
                            field.setCaretPosition(0);
                            // field.addKeyListener(this);
                            field.setPreferredSize(new java.awt.Dimension(80,19));

                            Color c=parent.topicHilights.getVariantColor(topic,s);
                            if(c != null) field.setForeground(c);

                            nameTable.put(s,field);
                            invNameTable.put(field,s);
                            originalNameTable.put(s,field.getText());
                            GridBagConstraints gbc2 = new GridBagConstraints();
                            gbc2.gridx=x;
                            gbc2.gridy=y;
                            gbc2.fill=GridBagConstraints.HORIZONTAL;
                            gbc2.weightx=1.0;
                            gbc2.insets = new Insets(0,0,0,0);
                            variantPanel.add(field,gbc2);
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



    // -------------------------------------------------------------------------
    // ------------------------------------------------------- Apply Changes ---
    // -------------------------------------------------------------------------



    public boolean applyChanges(final Topic topic, Wandora parent) throws TopicMapException {
        try {
            boolean changed = false;
            if(nameTable != null) {
                Iterator iter = nameTable.entrySet().iterator();
                String originalText = null;
                while(iter.hasNext()) {
                    Map.Entry e = (Map.Entry) iter.next();
                    final Set scope = (Set) e.getKey();
                    javax.swing.JTextField field = (javax.swing.JTextField) e.getValue();
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
                if(occurrenceTable.applyChanges(topic,parent)) {
                    changed = true;
                }
            }
            return changed;
        }
        catch(TopicMapReadOnlyException roe) {
            int button = parent.displayExceptionYesNo("Layer is read only. You can either discard changes and proceed with current operation or cancel.",null,"Discard changes","Cancel");
            if(button==0) return false;
            else {
                roe.fillInStackTrace();
                throw roe;
            }
        }
    }




    // -------------------------------------------------------------------------
    // -------------------------------------------------------------- Popups ---
    // -------------------------------------------------------------------------




    public JPopupMenu getNamesMenu() {
        return null;
    }


    public JPopupMenu getClassesMenu() {
         return null;
    }


    public JPopupMenu getInstancesMenu() {
         return null;
    }


    public JPopupMenu getSIMenu() {
         return null;
    }


    public JPopupMenu getTextDataMenu() {
         return null;
    }


    public JPopupMenu getSubjectMenu() {
         return null;
    }


    public JPopupMenu getAssociationsMenu() {
         return null;
    }


    public JPopupMenu getAssociationTypeMenu() {
         return null;
    }

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
    // ----------------------------------------------------- RefreshListener ---
    // -------------------------------------------------------------------------

    @Override
    public void doRefresh() throws TopicMapException {
        if(needsRefresh) {
            refresh();
            this.revalidate();
            this.repaint();
        }
    }





}
