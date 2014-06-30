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
 * ToolTree.java
 *
 * Created on 19.2.2008, 10:47
 *
 */

package org.wandora.application.gui;

import java.io.*;
import org.wandora.*;
import org.wandora.application.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;
import org.wandora.application.tools.navigate.OpenTopic;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import static org.wandora.utils.Tuples.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;


/**
 *
 * @author akivela
 */
public class WandoraToolTree extends SimpleTree implements MouseListener, TreeModelListener, TreeSelectionListener, ActionListener /*, DragSourceListener, DragGestureListener*/ {
    
    
    private Wandora wandora;
    private MouseEvent mouseEvent;
    private WandoraToolSet toolSet = null;
    protected TreeModel treeModel = null;
    
    private Object[] toolTreeMenuStruct = new Object[] {
        "Add tool...",
        "Add group...",
        "---",
        "Rename...",
        "Delete...",
        "---",
        "Execute...",
        "Configure...",
        "Release tool locks...",
        "Kill threads...",
        "---",
        "Info...",
    };
    private JPopupMenu toolTreeMenu = null;
    
    
    public WandoraToolTree(Wandora parent) {
        this.wandora = parent;
    }
    
    public void initialize(WandoraToolSet toolSet) {
        this.toolSet = toolSet;
        this.setRootVisible(false);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.addTreeSelectionListener(this);
        this.putClientProperty("JTree.lineStyle", "None");
        this.setCellRenderer(new ToolTreeRenderer());
        this.setEditable(false);
        this.setDragEnabled(true);
        this.setDropMode(DropMode.ON);
        this.setTransferHandler(new ToolTreeTransferHandler());
        ToolTipManager.sharedInstance().registerComponent(this);
        this.addMouseListener(this);
        this.toolTreeMenu = UIBox.makePopupMenu(toolTreeMenuStruct, this);
        this.setComponentPopupMenu(toolTreeMenu);
        
        ToolTreeNode top = new ToolTreeNode(toolSet);
        createNodes(top, toolSet);
        treeModel = new DefaultTreeModel(top);
        treeModel.addTreeModelListener(this);
        this.setModel(treeModel);
    }
    
    
    
    public void createNodes(ToolTreeNode node, WandoraToolSet toolSet) {
        Object[] ts = toolSet.asArray();
        Object to = null;
        ToolTreeNode subnode = null;
        WandoraToolSet subset = null;
        WandoraToolSet.ToolItem toolWrapper = null;
        for(int i=0; i<ts.length; i++) {
            to = ts[i];
            if(to != null) {
                if(to instanceof WandoraToolSet) {
                    subset = (WandoraToolSet) to;
                    //System.out.println("adding subtree "+subset+" to tool tree");
                    subnode = new ToolTreeNode(subset);
                    createNodes(subnode, subset);
                    subnode.setParentNode(node);
                    node.add(subnode);
                }
                else if(to instanceof WandoraToolSet.ToolItem) {
                    toolWrapper = (WandoraToolSet.ToolItem) to;
                    //System.out.println("adding tool "+toolWrapper+" to tool tree");
                    subnode = new ToolTreeNode(toolWrapper);
                    subnode.setParentNode(node);
                    node.add(subnode);
                }
            }
        }
    }

    
    
    public Object addNode(WandoraToolSet set) {
        return addNode((Object) set);
    }
    
    public Object addNode(WandoraToolSet.ToolItem tool) {
        return addNode((Object) tool);
    }
    
    private Object addNode(Object n) {
        TreePath parentPath = this.getSelectionPath();
        ToolTreeNode parentNode = null;
        if(parentPath == null) {
            //There is no selection. Default to the root node.
            parentNode = (ToolTreeNode) this.getModel().getRoot();
        }
        else {
            parentNode = (ToolTreeNode) (parentPath.getLastPathComponent());
            while(!parentNode.isSet()) {
                parentNode = parentNode.getParentNode();
            }
        }
        
        ToolTreeNode childNode = null;
        if(n instanceof WandoraToolSet.ToolItem) {
            childNode = new ToolTreeNode((WandoraToolSet.ToolItem) n);
        }
        if(n instanceof WandoraToolSet) {
            childNode = new ToolTreeNode((WandoraToolSet) n);
        }
        if(childNode != null) {
            childNode.setParentNode(parentNode);
            ((DefaultTreeModel) treeModel).insertNodeInto(
                    childNode,
                    parentNode,
                    parentNode.getChildCount()
            );
            scrollPathToVisible(new TreePath(childNode.getPath()));
            
            WandoraToolSet set = (WandoraToolSet) parentNode.getUserObject();
            set.add(n);
            
            return parentNode.getUserObject();
        }
        return null;
    }

    
    
    public Object renameCurrentNode(String newName) {
        TreePath parentPath = this.getSelectionPath();
        if(parentPath != null) {
            ToolTreeNode node = (ToolTreeNode) (parentPath.getLastPathComponent());
            Object o = node.getUserObject();
            if(o instanceof WandoraToolSet) {
                ((WandoraToolSet) o).setName(newName);
            }
            else if(o instanceof WandoraToolSet.ToolItem) {
                ((WandoraToolSet.ToolItem) o).setName(newName);
            }
            return o; 
        }
        return null;
    }
    
    
    public Object removeCurrentNode() {
        TreePath selectionPath = this.getSelectionPath();
        if(selectionPath != null) {
            ToolTreeNode currentNode = (ToolTreeNode) (selectionPath.getLastPathComponent());
            if(currentNode != null) {
                ((DefaultTreeModel) treeModel).removeNodeFromParent(currentNode);
                
                ToolTreeNode parentNode = currentNode.getParentNode();
                WandoraToolSet set = (WandoraToolSet) parentNode.getUserObject();
                set.remove(currentNode.getUserObject());
                return currentNode.getUserObject();
            }
        }
        return null;
    }

    
    
    
    public ToolTreeNode solveNode(int nodeHash) {
        ToolTreeNode root = (ToolTreeNode) this.getModel().getRoot();
        return solveNode(nodeHash, root);
    }
    public ToolTreeNode solveNode(int hash, ToolTreeNode node) {
        if(hash == node.hashCode()) return node;
        else {
            ToolTreeNode child = null;
            for(Enumeration children = node.children() ; children.hasMoreElements(); ) {
                child = (ToolTreeNode) children.nextElement();
                ToolTreeNode childnode = solveNode(hash, child);
                if(childnode != null) return childnode;
            }
        }
        return null;
    }
    
            
    
    public void refresh() {
        this.setModel(treeModel);
        this.validate();
        this.repaint();
        System.out.println("refresh acquired");
    }
    
    public WandoraToolSet getToolSet() {
        return toolSet;
    }
    
    
    
    
    public WandoraToolSet.ToolItem getSelectedTool() {
        TreePath parentPath = this.getSelectionPath();
        if(parentPath != null) {
            ToolTreeNode node = (ToolTreeNode) (parentPath.getLastPathComponent());
            Object o = node.getUserObject();
            if(o instanceof WandoraToolSet.ToolItem) {
                return (WandoraToolSet.ToolItem) o;
            }
        }
        return null;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String c = e.getActionCommand();
        if(c == null) return;
        c = c.toLowerCase();
        // System.out.println("actionPerformed @ ToolTree "+e);
        
        // ***** EXECUTE *****
        if(c.startsWith("execute")) {
            try {
                WandoraToolSet.ToolItem toolItem = getSelectedTool();
                if(toolItem != null) {
                    WandoraTool t = toolItem.getTool();
                    t.execute(wandora);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        // ***** CONFIGURE *****
        else if(c.startsWith("configure")) {
            try {
                WandoraToolSet.ToolItem toolItem = getSelectedTool();
                if(toolItem != null) {
                    WandoraTool t = toolItem.getTool();
                    if(t.isConfigurable()) {
                        t.configure(wandora, wandora.getOptions(), wandora.getToolManager().getOptionsPrefix(t));
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(wandora, "Tool is not configurable!", "Not configurable");
                    }
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        // ***** RENAME *****
        else if(c.startsWith("rename")) {
            if(getSelectionPath() != null) {
                String newName = WandoraOptionPane.showInputDialog(wandora, "Give new name for selected node?", "", "Rename node");
                if(newName != null) {
                    renameCurrentNode(newName);
                }
            }
        }
        
        // ***** DELETE *****
        else if(c.startsWith("delete")) {
            if(getSelectionPath() != null) {
                int a = WandoraOptionPane.showConfirmDialog(wandora, "Remove selected tool or folder?", "Confirm remove");
                if(a == WandoraOptionPane.YES_OPTION) {
                    removeCurrentNode();
                }
            }
        }
        
        // ***** ADD TOOL *****
        else if(c.startsWith("add tool")) {
            WandoraToolSelector toolSelector = new WandoraToolSelector(wandora);
            if(toolSelector.selectToolAccepted()) {              
                WandoraTool addedTool = toolSelector.getSelectedTool();
                if(addedTool != null) {
                    // toolSet.new ToolWrapper(toolName, (WandoraTool) n)
                    String toolName = addedTool.getName();
                    WandoraToolSet rootSet = getRootSet();
                    WandoraToolSet.ToolItem toolWrapper = rootSet.new ToolItem(toolName, addedTool);
                    addNode(toolWrapper);
                }
            }
        }
        
        // ***** ADD GROUP *****
        else if(c.startsWith("add group")) {
            String name = WandoraOptionPane.showInputDialog(wandora, "Give name for new group?", "", "Group name");
            if(name != null) {
                if(name.length() > 0) {
                    WandoraToolSet group = new WandoraToolSet(name, wandora);
                    addNode(group);
                }
                else {
                    WandoraOptionPane.showMessageDialog(wandora, "Given name is zero length. Cancelling folder creation.", "Too short folder name");
                }
            }
        }
        
        // ***** RELEASE TOOL LOCKS *****
        else if(c.startsWith("release tool locks")) {
            try {
                WandoraToolSet.ToolItem toolItem = getSelectedTool();
                if(toolItem != null) {
                    WandoraTool t = toolItem.getTool();
                    String className = t.getClass().getSimpleName();
                    if(t instanceof AbstractWandoraTool && !((AbstractWandoraTool) t).allowMultipleInvocations()) {
                        int a = WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to release tool lock for class '"+className+"'", "Release tool lock");
                        if(a == WandoraOptionPane.YES_OPTION) {
                            boolean released = ((AbstractWandoraTool) t).clearToolLock();
                            if(!released) WandoraOptionPane.showMessageDialog(wandora, "Tool class '"+className+"' was not locked. Couldn't release tool locks.");
                        }
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(wandora, "Tool class '"+className+"' doesn't support tool locks. Can't release tool locks.");
                    }
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        // ***** KILL TOOL THREADS *****
        else if(c.startsWith("kill threads")) {
            try {
                WandoraToolSet.ToolItem toolItem = getSelectedTool();
                if(toolItem != null) {
                    WandoraTool t = toolItem.getTool();
                    String className = t.getClass().getSimpleName();
                    if(t instanceof AbstractWandoraTool && ((AbstractWandoraTool) t).runInOwnThread()) {
                        int a = WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to interrupt tool threads for class '"+className+"'", "Interrupt tool threads");
                        if(a == WandoraOptionPane.YES_OPTION) {
                            ((AbstractWandoraTool) t).interruptThreads();
                        }
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(wandora, "Tool class '"+className+"' doesn't support interrups. Can't kill tool threads.");
                    }
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        // ***** TOOL INFO *****
        else if(c.startsWith("info")) {
            try {
                WandoraToolSet.ToolItem wrappedTool = getSelectedTool();
                if(wrappedTool != null) {
                    WandoraToolManager2 toolManager=wandora.getToolManager();
                    WandoraToolManager2.ToolInfo info=null;
                    WandoraTool tool=getSelectedTool().getTool();
                    if(toolManager!=null) info=toolManager.getToolInfo(tool);
                    new WandoraToolInfoDialog(wandora, tool,info);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // ****** FINALLY REFRESH TOOL TREE
        this.refresh();
    }
    
    
    
    public WandoraToolSet getRootSet() {
        ToolTreeNode rootNode = (ToolTreeNode) this.getModel().getRoot();
        return (WandoraToolSet) rootNode.getUserObject();
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        //System.out.println("valueChanged @ tooltree");
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        DefaultMutableTreeNode node;
        node = (DefaultMutableTreeNode)
                 (e.getTreePath().getLastPathComponent());

        /*
         * If the event lists children, then the changed
         * node is the child of the node we have already
         * gotten.  Otherwise, the changed node and the
         * specified node are the same.
         */
        try {
            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode)
                   (node.getChildAt(index));
        } catch (NullPointerException exc) {}

        System.out.println("The user has finished editing the node.");
        System.out.println("New value: " + node.getUserObject());
    }
    @Override
    public void treeNodesInserted(TreeModelEvent e) {
    }
    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
    }
    @Override
    public void treeStructureChanged(TreeModelEvent e) {
    }

    
    // -------------------------------------------------------------------------
    
      
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        if(mouseEvent.getClickCount()>=2) {
            try {
                WandoraToolSet.ToolItem wrappedTool = getSelectedTool();
                if(wrappedTool != null) {
                    WandoraToolManager2 toolManager=wandora.getToolManager();
                    WandoraToolManager2.ToolInfo info=null;
                    WandoraTool tool=getSelectedTool().getTool();
                    if(toolManager!=null) info=toolManager.getToolInfo(tool);
                    new WandoraToolInfoDialog(wandora, tool,info);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public class ToolTreeNode extends DefaultMutableTreeNode {
        private WandoraToolSet toolSet = null;
        private WandoraToolSet.ToolItem toolItem = null;
        private ToolTreeNode parentNode = null;
        
        public ToolTreeNode(WandoraToolSet set) {
            super();
            this.toolSet = set;
        }
        
        public ToolTreeNode(WandoraToolSet.ToolItem wrapper) {
            super();
            this.toolItem = wrapper;
        }
        
        
        @Override
        public Object getUserObject() {
            return (toolSet != null ? toolSet : toolItem);
        }
        
        public void setParentNode(ToolTreeNode node) {
            this.parentNode = node;
        }
        
        public ToolTreeNode getParentNode() {
            return parentNode;
        }
        
        public boolean isSet() {
            return (toolSet != null);
        }
        
        @Override
        public String toString() {
            Object o = getUserObject();
            return (o != null ? o.toString() : "null");
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public class ToolTreeRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
        
        private Icon toolSetIcon = null;
        
        
        public ToolTreeRenderer() {
            toolSetIcon = UIBox.getIcon("gui/icons/tool_set.png");
        }
        
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                       Object value,
                                       boolean selected,
                                       boolean expanded,
                                       boolean leaf,
                                       int row,
                                       boolean hasFocus) {
            
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            try {
                if(c instanceof JLabel) {
                    JLabel l = (JLabel) c;
                    
                    Object userValueContent = ((DefaultMutableTreeNode) value).getUserObject();
                    //System.out.println("userValue == " +userValueContent+" --- " +userValueContent.getClass());
                    
                    if(userValueContent instanceof WandoraToolSet.ToolItem) {
                        WandoraToolSet.ToolItem tool = (WandoraToolSet.ToolItem) userValueContent;
                        l.setText(tool.getName());
                        l.setIcon(tool.getTool().getIcon());
                        setToolTipText(Textbox.makeHTMLParagraph(tool.getTool().getDescription(), 40));
                    }
                    else if(userValueContent instanceof WandoraToolSet) {
                        if(toolSetIcon != null) {
                            l.setIcon(toolSetIcon);
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            
            return c;
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    public static class ToolTreeTransferable extends DnDHelper.WandoraTransferable {
        public static final DataFlavor toolTreeNodeFlavor=new DataFlavor(ToolTreeNode.class,"ToolTreeNode");
        
        protected Object transferable;
        
        public ToolTreeTransferable(ToolTreeNode node) {
            this.transferable=new Integer(node.hashCode());
        }
        
        @Override
        public void updateFlavors() {
            super.updateFlavors();
            if(this.transferable==null) return;
            DataFlavor[] old=supportedFlavors;
            supportedFlavors=new DataFlavor[old.length+1];
            supportedFlavors[0]=toolTreeNodeFlavor;
            System.arraycopy(old, 0, supportedFlavors, 1, old.length);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(transferable!=null && (flavor.equals(toolTreeNodeFlavor))) return transferable;
            else return super.getTransferData(flavor);
        }
    }
    
    
    
    
    public class ToolTreeTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(ToolTreeTransferable.toolTreeNodeFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {

            TreePath path=getSelectionPath();
            if(path==null) return new ToolTreeTransferable(null);
            else {
                ToolTreeNode toolNode = (ToolTreeNode) path.getLastPathComponent();
                return new ToolTreeTransferable(toolNode);
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try{
                if(support.isDataFlavorSupported(ToolTreeTransferable.toolTreeNodeFlavor)) {
                    Object movedNodeHash = (Object)support.getTransferable().getTransferData(ToolTreeTransferable.toolTreeNodeFlavor);                  
                    ToolTreeNode movedNode = solveNode(((Integer)movedNodeHash).intValue());

                    int action=support.getDropAction();
                    
                    JTree.DropLocation location=(JTree.DropLocation)support.getDropLocation();
                    TreePath dropPath=location.getPath();
                    if(dropPath==null){
                        //WandoraOptionPane.showMessageDialog(TopicTree.this.parent,"Invalid drop location, drop cancelled.");
                        return false;
                    }
                    ToolTreeNode targetNode = (ToolTreeNode)dropPath.getLastPathComponent();
                    ToolTreeNode targetSetNode = targetNode;
                    while(!targetSetNode.isSet()) {
                        targetSetNode = targetSetNode.getParentNode();
                    }
                    int index = targetSetNode.getIndex(targetNode);
                    index = Math.max(0, Math.min(index, targetSetNode.getChildCount()-1));
                    
                    try {
                        ((DefaultTreeModel) treeModel).removeNodeFromParent(movedNode);
                        WandoraToolSet sourceSet = (WandoraToolSet) movedNode.getParentNode().getUserObject();
                        sourceSet.remove(movedNode.getUserObject());
                        
                        ((DefaultTreeModel) treeModel).insertNodeInto(movedNode, targetSetNode, index);
                        WandoraToolSet targetSet = (WandoraToolSet) targetSetNode.getUserObject();
                        Object o = movedNode.getUserObject();
                        targetSet.add(o, index);
                        scrollPathToVisible(new TreePath(movedNode.getPath()));
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        return false;
                    }
                    
                    doRefresh();
                    return true;
                }
            }
            //catch(UnsupportedFlavorException ufe){ufe.printStackTrace();}
            catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        public void doRefresh(){
            WandoraToolTree.this.refresh();
        }
    }



}
