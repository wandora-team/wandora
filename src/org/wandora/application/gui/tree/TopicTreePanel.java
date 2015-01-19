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
 * TopicTreePanel.java
 *
 * Created on 14. heinäkuuta 2005, 14:45
 */

package org.wandora.application.gui.tree;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import java.awt.*;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.application.gui.TopicSelector;
import org.wandora.application.gui.WandoraOptionPane;



/**
 * @author  olli
 */
public class TopicTreePanel extends JPanel implements TopicSelector,TopicMapListener,RefreshListener {

    private String rootTopic;
    // private TopicTreeModel model;
    private Wandora wandora;
    
    private String name;
    
    private Set<String> initialSelectedRelations;
    private TopicTreeRelation[] initialAllRelations;
    private boolean treeEnabled;
    
    
    /** Creates new form TopicTreePanel */
    public TopicTreePanel(String rootTopic, Wandora parent) throws TopicMapException {
        this(rootTopic,parent,null,null);
    }

    public TopicTreePanel(String rootTopic, Wandora parent,Set<String> selectedAssociations, TopicTreeRelation[] associations)  throws TopicMapException {
        this(rootTopic,parent,selectedAssociations,associations,"Topic tree");
    }

    /**
     * @param rootTopic Subject identifier of the topic that is used as root for the tree
     * @param selectedRelations Names of the associations in associations array that
     *        are used in this topic tree chooser.
     * @param allRelations A list of tree association types. Not all of them are
     *        necessarily used in this topic tree chooser. selectedAssociations
     *        contains the names of the used association types.
     */
    public TopicTreePanel(String rootTopic, Wandora wandora, Set<String> selectedRelations, TopicTreeRelation[] allRelations,String name) throws TopicMapException {
        this.rootTopic=rootTopic;
        this.wandora=wandora;
        this.initialSelectedRelations=selectedRelations;
        this.initialAllRelations=allRelations;
        this.name=name;

        treeEnabled=true;
        
        jTree = initialSelectedRelations==null ?
            new TopicTree(rootTopic, wandora, this) :
            new TopicTree(rootTopic, wandora, initialSelectedRelations, initialAllRelations, this);
        
        initComponents();

        setTreeEnabled(treeEnabled);

        jTreeTreeExpanded(null);
    }


    /**
     * @param rootSI Subject identifier of the topic that is used as root for the tree
     * @param selectedRelations Names of the associations in associations array that
     *        are used in this topic tree chooser.
     * @param allRelations A list of tree association types. Not all of them are
     *        necessarily used in this topic tree chooser. selectedAssociations
     *        contains the names of the used association types.
     */
    public void setModel(String rootSI, Set<String> selectedRelations, TopicTreeRelation[] allRelations ) throws TopicMapException {
        ((TopicTree)jTree).updateModel(rootSI, selectedRelations, allRelations);
    }
    
    @Override
    public String getSelectorName(){
        return name;
    }
    
    public void setTreeEnabled(boolean b) {
        treeEnabled=b;
        if(jTree!=null){
            if(b==true && this.getComponent(0)!=scrollPane) {
                this.removeAll();
                this.add(scrollPane);
                this.revalidate();
            }
            else if(b==false && this.getComponent(0)!=newRootPanel) {
                this.removeAll();
                this.add(newRootPanel);
                this.revalidate();
            }
        }
    }

    public String getRootSI(){
        return rootTopic;
    }



    
    public void updateSelectedAssociation(String oldRelation, String newRelation) {
        if(initialSelectedRelations.contains(oldRelation)) {
            initialSelectedRelations.remove(oldRelation);
            initialSelectedRelations.add(newRelation);
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

        buttonPanel = new javax.swing.JPanel();
        newInstanceButton = new org.wandora.application.gui.simple.SimpleButton();
        newSubclassButton = new org.wandora.application.gui.simple.SimpleButton();
        jPanel1 = new javax.swing.JPanel();
        newRootPanel = new javax.swing.JPanel();
        jLabel1 = new org.wandora.application.gui.simple.SimpleLabel();
        newRootButton = new org.wandora.application.gui.simple.SimpleButton();
        scrollPane = new javax.swing.JScrollPane();
        jTree = jTree;

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        newInstanceButton.setText("New Instance");
        newInstanceButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        newInstanceButton.setMinimumSize(new java.awt.Dimension(79, 21));
        newInstanceButton.setPreferredSize(new java.awt.Dimension(85, 21));
        newInstanceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newInstanceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        buttonPanel.add(newInstanceButton, gridBagConstraints);

        newSubclassButton.setText("New Subclass");
        newSubclassButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        newSubclassButton.setMinimumSize(new java.awt.Dimension(77, 21));
        newSubclassButton.setPreferredSize(new java.awt.Dimension(85, 21));
        newSubclassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSubclassButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        buttonPanel.add(newSubclassButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.BorderLayout());

        newRootPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Topic tree root not found.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        newRootPanel.add(jLabel1, gridBagConstraints);

        newRootButton.setText("Create root topic");
        newRootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newRootButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        newRootPanel.add(newRootButton, gridBagConstraints);

        setLayout(new java.awt.BorderLayout(2, 2));

        scrollPane.setBorder(null);
        scrollPane.setMinimumSize(new java.awt.Dimension(0, 0));

        jTree.setMaximumSize(new java.awt.Dimension(32768, 999999));
        jTree.setMinimumSize(new java.awt.Dimension(10, 10));
        jTree.setPreferredSize(new java.awt.Dimension(10, 10));
        jTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                jTreeTreeExpanded(evt);
            }
        });
        scrollPane.setViewportView(jTree);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void newRootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRootButtonActionPerformed
        if(rootTopic == null) {
            WandoraOptionPane.showMessageDialog(wandora, "Can't find root topic. Update the root topic in configuration panel.");
            return;
        }
        String bn=WandoraOptionPane.showInputDialog(wandora,"Enter topic base name","Wandora class","Create root topic");
        if(bn==null) return;
        try {
            Topic t=wandora.getTopicMap().createTopic();
            if(TMBox.checkBaseNameChange(wandora,t,bn)!=ConfirmResult.yes){
                t.remove();
                return;
            }
            t.setBaseName(bn);
            t.addSubjectIdentifier(t.getTopicMap().createLocator(rootTopic));
            wandora.doRefresh();
            wandora.refreshTopicTrees();
        }
        catch(TopicMapException tme) {
            tme.printStackTrace();
            wandora.displayException("Unable to create root topic.", tme);
        }
    }//GEN-LAST:event_newRootButtonActionPerformed

    private void newSubclassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSubclassButtonActionPerformed
        //((TopicTree) jTree).createNewSubclass();
    }//GEN-LAST:event_newSubclassButtonActionPerformed

    private void newInstanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newInstanceButtonActionPerformed
        //((TopicTree) jTree).createNewInstance();
    }//GEN-LAST:event_newInstanceButtonActionPerformed

    private void jTreeTreeExpanded(javax.swing.event.TreeExpansionEvent evt) {//GEN-FIRST:event_jTreeTreeExpanded
        java.awt.Rectangle rect=jTree.getRowBounds(jTree.getRowCount()-1);
        java.awt.Dimension dim=new java.awt.Dimension(rect.x+rect.width,rect.y+rect.height);
        // note that JTree.getPreferredScrollableViewpartSize doesn't seem to work
        jTree.setMinimumSize(dim);
        jTree.setPreferredSize(dim);
        //this.validateTree(); // TRIGGERS EXCEPTION IN JAVA 1.7
    }//GEN-LAST:event_jTreeTreeExpanded

    
    
    public Topic getSelection() {
        TreePath path=jTree.getSelectionPath();
        if(path==null) return null;
        return ((TopicGuiWrapper)path.getLastPathComponent()).topic;
    }

    public boolean needsRefresh() {
        return ((TopicTree)jTree).getNeedsRefresh();
    }
    
    public void refresh() throws TopicMapException {
        ((TopicTree) jTree).refresh();
    }

    @Override
    public Topic[] getSelectedTopics() {
        Topic t=getSelection();
        if(t==null) return new Topic[0];
        else return new Topic[]{t};
    }

    @Override
    public Topic getSelectedTopic() {
        return getSelection();
    }
    
    @Override
    public void init() {
        wandora.addTopicMapListener(this);
        wandora.addRefreshListener(this);
    }
    
    @Override
    public void cleanup() {
        wandora.removeTopicMapListener(this);
        wandora.removeRefreshListener(this);
    }

    @Override
    public Component getPanel() {
        ((TopicTree)jTree).setOpenWithDoubleClick(false);
        return this;
    }
    
    
    // ---------------------------------------------------- TopicMapListener ---
    
    @Override
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException {
        ((TopicTree)jTree).topicSubjectIdentifierChanged(t,added,removed);
    }
    @Override
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException {
        ((TopicTree)jTree).topicBaseNameChanged(t,newName,oldName);
    }
    @Override
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        ((TopicTree)jTree).topicTypeChanged(t,added,removed);
    }
    @Override
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        ((TopicTree)jTree).topicVariantChanged(t,scope,newName,oldName);
    }
    @Override
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        ((TopicTree)jTree).topicDataChanged(t,type,version,newValue,oldValue);
    }
    @Override
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        ((TopicTree)jTree).topicSubjectLocatorChanged(t,newLocator,oldLocator);
    }
    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        ((TopicTree)jTree).topicRemoved(t);
    }
    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        ((TopicTree)jTree).topicChanged(t);
    }
    @Override
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        ((TopicTree)jTree).associationTypeChanged(a,newType,oldType);
    }
    @Override
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        ((TopicTree)jTree).associationPlayerChanged(a,role,newPlayer,oldPlayer);
    }
    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        ((TopicTree)jTree).associationRemoved(a);
    }
    @Override
    public void associationChanged(Association a) throws TopicMapException {
        ((TopicTree)jTree).associationChanged(a);
    }

    // --------------------------------------------------- /TopicMapListener ---
    
    
    
    @Override
    public void doRefresh() throws TopicMapException {
        //if(needsRefresh()){
            refresh();
        //}
    }
    
    public TopicTree getTopicTree() {
        return (TopicTree)jTree;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTree jTree;
    private javax.swing.JButton newInstanceButton;
    private javax.swing.JButton newRootButton;
    private javax.swing.JPanel newRootPanel;
    private javax.swing.JButton newSubclassButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables


    

}
