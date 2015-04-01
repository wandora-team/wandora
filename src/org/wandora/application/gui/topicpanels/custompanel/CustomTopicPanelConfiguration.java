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
 * 
 * CustomTopicPanelConfiguration.java
 *
 * Created on 11. tammikuuta 2008, 10:28
 */

package org.wandora.application.gui.topicpanels.custompanel;
import org.wandora.application.gui.texteditor.TextEditor;
import org.wandora.application.gui.topicpanels.CustomTopicPanel;
import org.wandora.application.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.*;
import org.wandora.utils.swing.DragJTree;
import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.awt.dnd.DnDConstants;

import static org.wandora.application.gui.topicpanels.CustomTopicPanel.QueryGroupInfo;
import static org.wandora.application.gui.topicpanels.CustomTopicPanel.QueryInfo;


import static java.awt.event.KeyEvent.*;
import static java.awt.event.InputEvent.*;
import jsyntaxpane.DefaultSyntaxKit;
/**
 *
 * @author  olli
 */
public class CustomTopicPanelConfiguration extends javax.swing.JPanel {
    
    private ArrayList<QueryGroupInfo> groups;
    private Object rootNode;
    
    private CustomTreeModel treeModel;
    
    private Wandora admin;
    
    /** Creates new form CustomTopicPanelConfiguration */
    public CustomTopicPanelConfiguration(Wandora admin) {
        this.admin=admin;
        initComponents();
        groupsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultTreeCellRenderer cellRenderer=new DefaultTreeCellRenderer();
        cellRenderer.setLeafIcon(UIBox.getIcon("gui/icons/topic_panel_custom_script.png"));
        cellRenderer.setClosedIcon(UIBox.getIcon("gui/icons/topic_panel_custom_group.png"));
        cellRenderer.setOpenIcon(UIBox.getIcon("gui/icons/topic_panel_custom_group.png"));
        groupsTree.setCellRenderer(cellRenderer);
        groupsTree.addTreeWillExpandListener(new TreeWillExpandListener(){
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                throw new ExpandVetoException(event);
            }
            public void treeWillExpand(TreeExpansionEvent event){
            }            
        });
    }
    
    public ArrayList<QueryGroupInfo> getQueryGroups(){
        return groups;
    }
    
    public void readQueryGroups(ArrayList<QueryGroupInfo> oldGroups){
        rootNode="Custom panel";
        this.groups=new ArrayList<QueryGroupInfo>();
        for(QueryGroupInfo g : oldGroups){
            this.groups.add(g.deepCopy());
        }
        
        treeModel=new CustomTreeModel();
        groupsTree.setModel(treeModel);
        openGroups();
        updateButtonStates();
    }
    
    public void updateButtonStates(){
        TreePath selection=groupsTree.getSelectionPath();
        boolean groupSelected=false;
        boolean querySelected=false;
        if(selection!=null){
            Object last=selection.getLastPathComponent();
            if(last instanceof QueryGroupInfo) groupSelected=true;
            else if(last instanceof QueryInfo) querySelected=true;
        }
        addGroupButton.setEnabled(true);
        addQueryButton.setEnabled(groupSelected|querySelected);
        duplicateQueryButton.setEnabled(querySelected);
        editButton.setEnabled(querySelected);
        removeGroupButton.setEnabled(groupSelected);
        removeQueryButton.setEnabled(querySelected);        
    }
    
    public void openGroups(){
        for(QueryGroupInfo g : groups){
            groupsTree.expandPath(new TreePath(new Object[]{rootNode,g}));
        }        
    }
    
    public void addGroup(){
        QueryGroupInfo g=new QueryGroupInfo();
        g.name="New Group";
        groups.add(g);
        treeModel.fireNodesInserted(new TreeModelEvent(this,new Object[]{rootNode},new int[]{groups.size()-1},new Object[]{g}));
        openGroups();
    }
    
    public void removeGroup(TreePath selection){
        if(selection==null) return;
        Object[] path=selection.getPath();
        if(path.length!=2) return;
        QueryGroupInfo g = (QueryGroupInfo)path[1];
        int idx=groups.indexOf(g);
        if(idx==-1) return;
        groups.remove(g);
        treeModel.fireNodesRemoved(new TreeModelEvent(this,new Object[]{rootNode},new int[]{idx},new Object[]{g}));
    }
    
    public void addQuery(TreePath selection){
        if(selection==null) return;
        Object[] path=selection.getPath();
        if(path.length<2) return;
        QueryGroupInfo g = (QueryGroupInfo)path[1];
        QueryInfo q=new QueryInfo();
        q.name="New Query";
        q.scriptEngine=WandoraScriptManager.getDefaultScriptEngine();
        g.queries.add(q);
        treeModel.fireNodesInserted(new TreeModelEvent(this,new Object[]{rootNode,g},new int[]{g.queries.size()-1},new Object[]{q}));
        groupsTree.expandPath(new TreePath(new Object[]{rootNode,g}));
    }
    
    public void duplicateQuery(TreePath selection){
        if(selection==null) return;
        Object[] path=selection.getPath();
        if(path.length<3) return;
        QueryGroupInfo g = (QueryGroupInfo)path[1];
        QueryInfo sourceq = (QueryInfo)path[2];
        QueryInfo q=sourceq.copy();
        q.name="Copy of "+q.name;
        g.queries.add(q);
        treeModel.fireNodesInserted(new TreeModelEvent(this,new Object[]{rootNode,g},new int[]{g.queries.size()-1},new Object[]{q}));
        groupsTree.expandPath(new TreePath(new Object[]{rootNode,g}));
    }
    
    public void removeQuery(TreePath selection){
        if(selection==null) return;
        Object[] path=selection.getPath();
        if(path.length!=3) return;
        QueryGroupInfo g=(QueryGroupInfo)path[1];
        QueryInfo q=(QueryInfo)path[2];
        int idx=g.queries.indexOf(q);
        if(idx==-1) return;
        g.queries.remove(idx);
        treeModel.fireNodesRemoved(new TreeModelEvent(this,new Object[]{rootNode,g},new int[]{idx},new Object[]{q}));
    }
    
    public void editQuery(TreePath selection){
        if(selection==null) return;
        Object[] path=selection.getPath();
        if(path.length!=3) return;
        QueryGroupInfo g=(QueryGroupInfo)path[1];
        QueryInfo q=(QueryInfo)path[2];
        ScriptEditor editor=new ScriptEditor(q);
        editor.setVisible(true);
        if(editor.acceptChanges()){
            q.script=editor.getText();
            q.scriptEngine=engineComboBox.getSelectedItem().toString();
            q.name=nameField.getText();
            treeModel.fireNodesChanged(new TreeModelEvent(this,new Object[]{rootNode,g},new int[]{g.queries.indexOf(q)},new Object[]{q}));            
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

        scriptToolPanel = new javax.swing.JPanel();
        checkButton = new SimpleButton();
        jSeparator1 = new javax.swing.JSeparator();
        groupsPanel = new javax.swing.JPanel();
        editorToolPanel = new javax.swing.JPanel();
        nameLabel = new SimpleLabel();
        nameField = new SimpleField();
        engineLabel = new SimpleLabel();
        engineComboBox = new SimpleComboBox();
        engineComboBox.setEditable(false);
        addGroupButton = new SimpleButton();
        removeGroupButton = new SimpleButton();
        editButton = new SimpleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        groupsTree = new DraggableTree();
        addQueryButton = new SimpleButton();
        removeQueryButton = new SimpleButton();
        duplicateQueryButton = new SimpleButton();

        scriptToolPanel.setLayout(new java.awt.GridBagLayout());

        checkButton.setText("Check script");
        checkButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        checkButton.setPreferredSize(new java.awt.Dimension(79, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        scriptToolPanel.add(checkButton, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 0);
        scriptToolPanel.add(jSeparator1, gridBagConstraints);

        groupsPanel.setLayout(new java.awt.GridBagLayout());

        editorToolPanel.setLayout(new java.awt.GridBagLayout());

        nameLabel.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        editorToolPanel.add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        editorToolPanel.add(nameField, gridBagConstraints);

        engineLabel.setText("Script engine");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        editorToolPanel.add(engineLabel, gridBagConstraints);

        engineComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        engineComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        engineComboBox.setPreferredSize(new java.awt.Dimension(57, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        editorToolPanel.add(engineComboBox, gridBagConstraints);

        setLayout(new java.awt.GridBagLayout());

        addGroupButton.setText("Add group");
        addGroupButton.setMaximumSize(new java.awt.Dimension(105, 23));
        addGroupButton.setPreferredSize(new java.awt.Dimension(105, 23));
        addGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 0);
        add(addGroupButton, gridBagConstraints);

        removeGroupButton.setText("Remove group");
        removeGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGroupButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 7, 0);
        add(removeGroupButton, gridBagConstraints);

        editButton.setText("Modify query");
        editButton.setMaximumSize(new java.awt.Dimension(105, 23));
        editButton.setPreferredSize(new java.awt.Dimension(105, 23));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 7, 0);
        add(editButton, gridBagConstraints);

        groupsTree.setEditable(true);
        groupsTree.setRootVisible(false);
        groupsTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                groupsTreeMouseClicked(evt);
            }
        });
        groupsTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                groupsTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(groupsTree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        add(jScrollPane1, gridBagConstraints);

        addQueryButton.setText("Add query");
        addQueryButton.setMaximumSize(new java.awt.Dimension(105, 23));
        addQueryButton.setPreferredSize(new java.awt.Dimension(105, 23));
        addQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addQueryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 0);
        add(addQueryButton, gridBagConstraints);

        removeQueryButton.setText("Remove query");
        removeQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeQueryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 7, 0);
        add(removeQueryButton, gridBagConstraints);

        duplicateQueryButton.setText("Copy query");
        duplicateQueryButton.setMaximumSize(new java.awt.Dimension(105, 23));
        duplicateQueryButton.setPreferredSize(new java.awt.Dimension(105, 23));
        duplicateQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateQueryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 0);
        add(duplicateQueryButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void groupsTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_groupsTreeValueChanged
        updateButtonStates();
    }//GEN-LAST:event_groupsTreeValueChanged

    private void duplicateQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateQueryButtonActionPerformed
        duplicateQuery(groupsTree.getSelectionPath());
    }//GEN-LAST:event_duplicateQueryButtonActionPerformed

    private void groupsTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_groupsTreeMouseClicked
        if( evt.getClickCount() == 2){
            editQuery(groupsTree.getSelectionPath());
        }
    }//GEN-LAST:event_groupsTreeMouseClicked

    private void removeQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeQueryButtonActionPerformed
        removeQuery(groupsTree.getSelectionPath());
    }//GEN-LAST:event_removeQueryButtonActionPerformed

    private void addQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addQueryButtonActionPerformed
        addQuery(groupsTree.getSelectionPath());
    }//GEN-LAST:event_addQueryButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        editQuery(groupsTree.getSelectionPath());
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGroupButtonActionPerformed
        removeGroup(groupsTree.getSelectionPath());
    }//GEN-LAST:event_removeGroupButtonActionPerformed

    private void addGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGroupButtonActionPerformed
        addGroup();
    }//GEN-LAST:event_addGroupButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addGroupButton;
    private javax.swing.JButton addQueryButton;
    private javax.swing.JButton checkButton;
    private javax.swing.JButton duplicateQueryButton;
    private javax.swing.JButton editButton;
    private javax.swing.JPanel editorToolPanel;
    private javax.swing.JComboBox engineComboBox;
    private javax.swing.JLabel engineLabel;
    private javax.swing.JPanel groupsPanel;
    private javax.swing.JTree groupsTree;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton removeGroupButton;
    private javax.swing.JButton removeQueryButton;
    private javax.swing.JPanel scriptToolPanel;
    // End of variables declaration//GEN-END:variables

    public class CustomTreeModel implements TreeModel {
        
        public ArrayList<TreeModelListener> listeners;
        public CustomTreeModel(){
            listeners=new ArrayList<TreeModelListener>();
        }
                
        public Object getRoot() {
            return rootNode;
        }
        
        public void fireNodesChanged(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeNodesChanged(e);
        }
        public void fireNodesInserted(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeNodesInserted(e);
        }
        public void fireNodesRemoved(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeNodesRemoved(e);
        }
        public void fireStructureChanged(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeStructureChanged(e);
        }
        
        public int getIndexOfChild(Object parent, Object child) {
            if(parent==rootNode) return groups.indexOf(child);
            else if(child instanceof QueryGroupInfo) return ((QueryGroupInfo)child).queries.indexOf(child);
            else return -1;
        }

        public Object getChild(Object parent, int index) {
            if(parent==rootNode){
                return groups.get(index);
            }
            if(parent instanceof QueryGroupInfo){
                return ((QueryGroupInfo)parent).queries.get(index);
            }
            return null;
        }

        public boolean isLeaf(Object node) {
            if(node == rootNode) return false;
            else if(node instanceof QueryGroupInfo) return false;
            else return true;
        }

        public int getChildCount(Object parent) {
            if(parent==rootNode) return groups.size();
            else if(parent instanceof QueryGroupInfo) return ((QueryGroupInfo)parent).queries.size();
            else return 0;
        }

        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
            Object o=path.getLastPathComponent();
            if(o instanceof QueryGroupInfo) {
                ((QueryGroupInfo)o).name=newValue.toString();
                fireNodesChanged(new TreeModelEvent(this,new Object[]{rootNode},new int[]{groups.indexOf(o)},new Object[]{o}));
            }
            else if(o instanceof QueryInfo) {
                ((QueryInfo)o).name=newValue.toString();
                Object[] p=path.getPath();
                QueryGroupInfo g=(QueryGroupInfo)p[1];
                fireNodesChanged(new TreeModelEvent(this,new Object[]{rootNode,g},new int[]{g.queries.indexOf(o)},new Object[]{o}));
            }
        }
        
    }
    
    public class DraggableTree extends DragJTree {
        public int allowDrop(TreePath destinationParent, TreePath destinationPosition, TreePath source) {
            Object[] s=source.getPath();
            if(destinationParent==null) return DnDConstants.ACTION_NONE;
            Object[] d=destinationParent.getPath();
            if(s.length==3){
                if(d.length==2) return DnDConstants.ACTION_MOVE;
                else return DnDConstants.ACTION_NONE;
            }
            else if(s.length==2){
                if(d.length==1) return DnDConstants.ACTION_MOVE;
                else return DnDConstants.ACTION_NONE;
            }
            else return DnDConstants.ACTION_NONE;
        }

        public void doDrop(TreePath destinationParent, TreePath destinationPosition, TreePath source, int action) {
            Object[] s=source.getPath();
            Object[] d=destinationParent.getPath();
            Object[] p=null;
            if(destinationPosition!=null) p=destinationPosition.getPath();
            if(s[s.length-1] instanceof QueryInfo){
                if(d[d.length-1] instanceof QueryGroupInfo){
                    QueryInfo q=(QueryInfo)s[s.length-1];
                    QueryGroupInfo g=(QueryGroupInfo)d[d.length-1];
                    QueryGroupInfo oldg=(QueryGroupInfo)s[s.length-2];
                    int idx=oldg.queries.indexOf(q);
                    oldg.queries.remove(q);
                    CustomTopicPanelConfiguration.this.treeModel.fireNodesRemoved(new TreeModelEvent(this,new Object[]{rootNode,oldg},new int[]{idx},new Object[]{q}));
                    if(p!=null){
                        QueryInfo pq=(QueryInfo)p[p.length-1];
                        int idx2=g.queries.indexOf(pq);
                        if(idx2!=-1) idx=idx2+1;
                    }
                    else idx=0;
                    g.queries.add(idx,q);
                    CustomTopicPanelConfiguration.this.treeModel.fireNodesInserted(new TreeModelEvent(this,new Object[]{rootNode,g},new int[]{idx},new Object[]{q}));
                }
            }
            else if(s[s.length-1] instanceof QueryGroupInfo){
                if(d.length==1){
                    QueryGroupInfo g=(QueryGroupInfo)s[s.length-1];
                    int idx=groups.indexOf(g);
                    groups.remove(g);
                    CustomTopicPanelConfiguration.this.treeModel.fireNodesRemoved(new TreeModelEvent(this,new Object[]{rootNode},new int[]{idx},new Object[]{g}));
                    if(p!=null){
                        QueryGroupInfo pg=(QueryGroupInfo)p[p.length-1];
                        int idx2=groups.indexOf(pg);
                        if(idx2!=-1) idx=idx2+1;
                    }
                    else idx=0;
                    groups.add(idx,g);
                    CustomTopicPanelConfiguration.this.treeModel.fireNodesInserted(new TreeModelEvent(this,new Object[]{rootNode},new int[]{idx},new Object[]{g}));
                    openGroups();
                }
            }
        }
        
    }
    
    
    
    
    
    private class ScriptEditor extends TextEditor {
        public static final String optionPrefix = "scriptTextEditor.";
        protected JMenu scriptMenu;
        
        public ScriptEditor(QueryInfo queryInfo){
            super(CustomTopicPanelConfiguration.this.admin,true,queryInfo.script);
            this.setTitle("Edit query script");
            this.wrapLines(false);
            ArrayList<String> engines=WandoraScriptManager.getAvailableEngines();
            engineComboBox.removeAllItems();
            for(int i=0;i<engines.size();i++){
                String e=engines.get(i);
                engineComboBox.addItem(e);
            }   
            engineComboBox.setSelectedItem(queryInfo.scriptEngine);
            nameField.setText(queryInfo.name);
            
            centerPanel.add(editorToolPanel,BorderLayout.NORTH);
            ActionListener[] ls=checkButton.getActionListeners();
            for(int i=0;i<ls.length;i++) checkButton.removeActionListener(ls[i]);
            checkButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    checkScript();
                }
            });
            setCustomButtons(scriptToolPanel);
        }
        public void checkScript(){
            String message=CustomTopicPanel.checkScript(admin,engineComboBox.getSelectedItem().toString(),textPane.getText());
            if(message!=null){
                WandoraOptionPane.showMessageDialog(admin, message, "Check syntax", WandoraOptionPane.ERROR_MESSAGE);        
            }
            else{
                WandoraOptionPane.showMessageDialog(admin, "No errors", "Check syntax", WandoraOptionPane.INFORMATION_MESSAGE);                    
            }        
        }
        public String getOptionsPrefix(){
            return optionPrefix;
        }
        public void exitTextEditor(boolean acceptingChanges) {
            if(acceptingChanges){
                String message=CustomTopicPanel.checkScript(admin,engineComboBox.getSelectedItem().toString(),textPane.getText());
                if(message!=null){
                    int c=WandoraOptionPane.showConfirmDialog(admin, "Unabled to evaluate script. Do you want continue?<br><br>"+message,"Check syntax.");
                    if(c!=WandoraOptionPane.YES_OPTION) return;
                }
            }
            super.exitTextEditor(acceptingChanges);
        }
        
        public JMenu getScriptMenu(){
            scriptMenu = new SimpleMenu("Script", (Icon) null);

            Object[] menuStructure = new Object[] {
                "Check script", UIBox.getIcon("gui/icons/check.png"), KeyStroke.getKeyStroke(VK_F5, 0),
            };
            scriptMenu.removeAll();
            UIBox.attachMenu( scriptMenu, menuStructure, this );
            return scriptMenu;
        }
        
        public void initMenuBar(){
            super.initMenuBar();
            menuBar.add(getScriptMenu());        
        }

        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            String c = actionEvent.getActionCommand();
            if("check script".equalsIgnoreCase(c)){
                checkScript();
            }
            else super.actionPerformed(actionEvent);
        }
    }
}
