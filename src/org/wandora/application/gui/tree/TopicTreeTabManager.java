

package org.wandora.application.gui.tree;


import org.wandora.application.*;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.WandoraOptionPane;


/**
 *
 * @author akivela
 */
public class TopicTreeTabManager {

    private JPopupMenu tabPopupMenu = null;

    // how many tabs before and after configurable topic trees
    private int fixedStartTabs;
    private int fixedEndTabs;


    private Wandora wandora = null;
    // this is to retain the ordering of tabs
    private ArrayList<String> tabTrees;
    //              tree  ,set of association names
    private HashMap<String,Set<String>> selectedTreeRelations;
    //              tree  ,root SI
    private HashMap<String,String> treeRoots;


    private HashMap<String,TopicTreePanel> treeTopicChoosers;


    JTabbedPane tabbedPane = null;
    Options options = null;

    public TopicTreeTabManager(Wandora w, JTabbedPane tPane) {
        wandora = w;
        tabbedPane = tPane;
        options = w.getOptions();
        initializePopupMenu();
    }



    public void initializePopupMenu() {
        tabPopupMenu = new javax.swing.JPopupMenu();
        tabPopupMenu.setFont(UIConstants.menuFont);

        JMenuItem createTabMenuItem = new SimpleMenuItem();
        JMenuItem deleteTabMenuItem = new SimpleMenuItem();
        JSeparator jSeparator2 = new javax.swing.JSeparator();
        JMenuItem configTabMenuItem = new SimpleMenuItem();
        JMenuItem configTypesMenuItem = new SimpleMenuItem();

        createTabMenuItem.setFont(UIConstants.menuFont);
        createTabMenuItem.setText("Create new tab");
        createTabMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createTabMenuItemActionPerformed(evt);
            }
        });
        tabPopupMenu.add(createTabMenuItem);

        deleteTabMenuItem.setFont(UIConstants.menuFont);
        deleteTabMenuItem.setText("Delete tab");
        deleteTabMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTabMenuItemActionPerformed(evt);
            }
        });
        tabPopupMenu.add(deleteTabMenuItem);
        tabPopupMenu.add(jSeparator2);

        configTabMenuItem.setFont(UIConstants.menuFont);
        configTabMenuItem.setText("Configure topic tree tab...");
        configTabMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configTopicTreeActionPerformed(evt);
            }
        });
        tabPopupMenu.add(configTabMenuItem);

        configTypesMenuItem.setFont(UIConstants.menuFont);
        configTypesMenuItem.setText("Configure types...");
        configTypesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configTopicTreeRelationsActionPerformed(evt);
            }
        });
        tabPopupMenu.add(configTypesMenuItem);
    }





    /**
     * Creates and initializes topic trees based on application options and adds them
     * in the tabbed pane along with the finder tab.
     */
    public void initializeTopicTrees() {
        fixedStartTabs = 0;
        fixedEndTabs = 1;
        try {
            if(treeTopicChoosers!=null){
                for(TopicTreePanel c : treeTopicChoosers.values()){
                    wandora.removeTopicMapListener(c);
                    wandora.removeRefreshListener(c);
                }
            }
            for(int i=0; i<=tabbedPane.getTabCount(); i++) {
                tabbedPane.removeTabAt(0);
            }
            readTopicTreeRelations();
            TopicTreeRelation[] associations=TopicTreeRelationsEditor.readRelationTypes(options);
            treeTopicChoosers=new LinkedHashMap<String,TopicTreePanel>();

            // tabbedPane.addTab("Layers", layersPanel);
            for(String tab : tabTrees) {
                TopicTreePanel treeTopicChooser = new TopicTreePanel(treeRoots.get(tab),wandora,selectedTreeRelations.get(tab),associations,tab);
                treeTopicChoosers.put(tab,treeTopicChooser);
                wandora.addTopicMapListener(treeTopicChooser);
                wandora.addRefreshListener(treeTopicChooser);
                // JPanel panel=new JPanel();
                // panel.add(treeTopicChooser);
                tabbedPane.addTab(tab,null,treeTopicChooser,null);
            }
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    

    /**
     * Gets all topic tree choosers. Returned map has tree names as keys and trees
     * themselves as values.
     */
    public HashMap<String,TopicTreePanel> getTrees() {
        return treeTopicChoosers;
    }




    /**
     * Creates new tree choosers based on application options and returns them as a collection.
     */
    public Collection<TopicTreePanel> getTreeChoosers() throws TopicMapException {
        ArrayList<TopicTreePanel> v=new ArrayList<TopicTreePanel>();
        TopicTreeRelation[] associations=TopicTreeRelationsEditor.readRelationTypes(options);
        for(String tab : selectedTreeRelations.keySet()){
            TopicTreePanel treeTopicChooser = new TopicTreePanel(treeRoots.get(tab),wandora,selectedTreeRelations.get(tab),associations,tab);
            v.add(treeTopicChooser);
        }
        return v;
    }





    /**
     * Initializes treeRoots, tabTrees and selectedTreeAssociations with application options.
     */
    public void readTopicTreeRelations() {
        treeRoots=new LinkedHashMap<String,String>();
        tabTrees=new ArrayList<String>();
        selectedTreeRelations=new LinkedHashMap<String,Set<String>>();
        int counter=0;
        while(true) {
            String name=options.get("trees.tree["+counter+"].name");
            if(name==null) break;
            String root=options.get("trees.tree["+counter+"].root");
            tabTrees.add(name);
            treeRoots.put(name,root);
            int counter2=0;
            HashSet<String> as=new LinkedHashSet<String>();
            while(true){
                String a=options.get("trees.tree["+counter+"].relation["+counter2+"]");
                if(a==null) break;
                as.add(a);
                counter2++;
            }
            selectedTreeRelations.put(name,as);
            counter++;
        }
    }



    /**
     * Writes the information in treeRoots, tabTrees and selectedTreeAssociations into
     * application options. Effectively saves information about topic tree choosers
     * and association types used in them.
     */
    public void writeTopicTreeRelations() {
        int counter=0;
        options.removeAll("trees");
        for(String tab : tabTrees) {
            //for(Map.Entry<String,Set<String>> e : selectedTreeAssociations.entrySet()){
            Set<String> as=selectedTreeRelations.get(tab);
            options.put("trees.tree["+counter+"].name",tab);
            options.put("trees.tree["+counter+"].root",treeRoots.get(tab));
            int counter2=0;
            for(String a : as) {
                options.put("trees.tree["+counter+"].relation["+counter2+"]",a);
                counter2++;
            }
            counter++;
        }
    }





    public void createTabMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            TopicTreeRelation[] associations=TopicTreeRelationsEditor.readRelationTypes(options);
            JDialog jd=new JDialog(wandora,true);
            jd.setTitle("New topic tree tab");

            TopicTreeConfigPanel tap=new TopicTreeConfigPanel(associations,new LinkedHashSet<String>(),null,"",jd,wandora);
            jd.add(tap);
            jd.setSize(400,300);
            wandora.centerWindow(jd);

            while(true){
                jd.setVisible(true);
                if(tap.wasCancelled()) return;
                String name=tap.getTabName();
                String rootSI = tap.getRoot();
                if(selectedTreeRelations.get(name) != null) {
                    WandoraOptionPane.showMessageDialog(wandora,"Tab '"+name+"' already exists! Please use another name for the tab.", null, WandoraOptionPane.WARNING_MESSAGE);
                }
                else if(name == null || name.trim().length() == 0) {
                    WandoraOptionPane.showMessageDialog(wandora,"Tab name is not valid! It is null or zero length or contains only space characters. Please use another name for the tab.", null, WandoraOptionPane.WARNING_MESSAGE);
                }
                else if(rootSI == null || rootSI.trim().length() == 0) {
                    WandoraOptionPane.showMessageDialog(wandora,"Root topic is not valid! Please select topic for the tree root.", null, WandoraOptionPane.WARNING_MESSAGE);
                }
                else break;
            }
            String name=tap.getTabName();
            selectedTreeRelations.put(name,tap.getSelected());
            treeRoots.put(name,tap.getRoot());
            tabTrees.add(name);

            TopicTreePanel treeTopicChooser = new TopicTreePanel(treeRoots.get(name),wandora,selectedTreeRelations.get(name),associations,name);
            treeTopicChoosers.put(name,treeTopicChooser);
            wandora.addTopicMapListener(treeTopicChooser);
            wandora.addRefreshListener(treeTopicChooser);
            tabbedPane.insertTab(name,null,treeTopicChooser,null,tabbedPane.getTabCount()-fixedEndTabs);

            writeTopicTreeRelations();
        }
        catch(TopicMapException tme){
            wandora.handleError(tme); // TODO EXCEPTION
        }
    }

    public void deleteTabMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        int tabIndex=tabbedPane.getSelectedIndex();
        if(tabIndex<=fixedStartTabs || tabIndex>=tabbedPane.getTabCount()-fixedEndTabs) {
            WandoraOptionPane.showMessageDialog(wandora, "You are not allowed to delete original topic tree tab.", "Topic tree tab deletion not allowed");
            return;
        }
        String name=tabbedPane.getTitleAt(tabIndex);
        int c=WandoraOptionPane.showConfirmDialog(wandora,"Do you really want to delete tab '"+name+"'?","Confirm delete",WandoraOptionPane.YES_NO_OPTION);
        if(c!=WandoraOptionPane.YES_OPTION) return;

        selectedTreeRelations.remove(name);
        treeRoots.remove(name);
        tabTrees.remove(name);

        tabbedPane.removeTabAt(tabIndex);

        writeTopicTreeRelations();
    }



    public void configTopicTreeActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int tabIndex = tabbedPane.getSelectedIndex();
            if(tabIndex<fixedStartTabs || tabIndex>=tabbedPane.getTabCount()-fixedEndTabs) return;
            String tab = tabbedPane.getTitleAt(tabIndex);
            Set<String> selected = selectedTreeRelations.get(tab);
            if(selected == null) selected = new LinkedHashSet<String>();
            JDialog jd=new JDialog(wandora,true);
            jd.setTitle("Configure topic tree");

            TopicTreeRelation[] associations = TopicTreeRelationsEditor.readRelationTypes(options);
            TopicTreeConfigPanel tap = new TopicTreeConfigPanel(associations,selected,treeRoots.get(tab),tab,jd,wandora);
            jd.add(tap);
            jd.setSize(400,300);
            wandora.centerWindow(jd);

            jd.setVisible(true);
            if(tap.wasCancelled()) return;

            selectedTreeRelations.put(tab,tap.getSelected());
            treeRoots.put(tab,tap.getRoot());
            if(!tap.getTabName().equals(tab)){
                String newname = tap.getTabName();
                String rootSI = tap.getRoot();
                if(selectedTreeRelations.get(newname) != null){
                    WandoraOptionPane.showMessageDialog(wandora,"Tab '"+newname+"' already exists! Please use another name for the tab.", null, WandoraOptionPane.WARNING_MESSAGE);
                }
                else if(newname == null || newname.trim().length() == 0) {
                    WandoraOptionPane.showMessageDialog(wandora,"Tab name is not valid! It is null or zero length or contains only space characters. Please use another name for the tab.", null, WandoraOptionPane.WARNING_MESSAGE);
                }
                else if(rootSI == null || rootSI.trim().length() == 0) {
                    WandoraOptionPane.showMessageDialog(wandora,"Root topic is not valid! Please select topic for the tree root.", null, WandoraOptionPane.WARNING_MESSAGE);
                }
                else {
                    selectedTreeRelations.put(newname,selectedTreeRelations.get(tab));
                    treeRoots.put(newname,treeRoots.get(tab));
                    treeTopicChoosers.put(newname,treeTopicChoosers.get(tab));
                    selectedTreeRelations.remove(tab);
                    treeRoots.remove(tab);
                    treeTopicChoosers.remove(tab);
                    // treeTopicChooser doesn't change, only it's name, so no need to update listeners
                    for(int i=fixedStartTabs; i<tabbedPane.getTabCount()-fixedEndTabs; i++){
                        if(tabbedPane.getTitleAt(i).equals(tab)) {
                            tabbedPane.setTitleAt(i,newname);
                            break;
                        }
                    }
                    for(int i=0; i<tabTrees.size(); i++) {
                        if(tabTrees.get(i).equals(tab)) {
                            tabTrees.set(i,newname);
                            break;
                        }
                    }
                    tab=newname;
                }
            }
            writeTopicTreeRelations();

            treeTopicChoosers.get(tab).setModel(treeRoots.get(tab),selectedTreeRelations.get(tab),associations);
        }
        catch(TopicMapException tme) {
            wandora.handleError(tme);  // TODO EXCEPTION
        }
    }





    public void configTopicTreeRelationsActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            JDialog jd=new JDialog(wandora,true);
            jd.setTitle("Configure topic tree relations");

            TopicTreeRelationsEditor editor=new TopicTreeRelationsEditor(TopicTreeRelationsEditor.readRelationTypes(options),jd,wandora);
            jd.add(editor);
            jd.setSize(750,300);
            wandora.centerWindow(jd);
            jd.setVisible(true);
            if(editor.wasCancelled()) return;

            TopicTreeRelationsEditor.writeAssociationTypes(options,editor.getRelationTypes());

            TopicTreeRelation[] associations=TopicTreeRelationsEditor.readRelationTypes(options);
            for(Map.Entry<String,TopicTreePanel> e : treeTopicChoosers.entrySet()){
                e.getValue().setModel(treeRoots.get(e.getKey()),selectedTreeRelations.get(e.getKey()),associations);
            }
        }
        catch(TopicMapException tme) {
            wandora.handleError(tme); // TODO EXCEPTION
        }

    }

    public void tabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {
        try {
            if(evt.getButton()==MouseEvent.BUTTON3) {
                int tabIndex=tabbedPane.getSelectedIndex();
                if(tabIndex<fixedStartTabs || tabIndex>=tabbedPane.getTabCount()-fixedEndTabs) return;
                tabPopupMenu.show(evt.getComponent(),evt.getX(),evt.getY());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }



}
