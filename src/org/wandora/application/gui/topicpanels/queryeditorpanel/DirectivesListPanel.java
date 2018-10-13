/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveManager;
import org.wandora.query2.DirectiveUIHints;

/**
 *
 * @author olli
 */


public class DirectivesListPanel extends javax.swing.JPanel {


	private static final long serialVersionUID = 1L;
	
	
	/**
     * Creates new form DirectivesListPanel
     */
    public DirectivesListPanel() {
        initComponents();
        populateDirectiveList();
        
        directivesTree.setTransferHandler(null);
        DnDTools.setDragSourceHandler(directivesTree, "directiveHints", DnDTools.directiveHintsDataFlavor, 
                new DnDTools.DragSourceCallback<DirectiveUIHints>() {
            @Override
            public DirectiveUIHints callback(JComponent component) {
                JTree tree=(JTree)component;
                TreePath path=tree.getSelectionPath();
                if(path==null) return null;
                DefaultMutableTreeNode n=(DefaultMutableTreeNode)path.getLastPathComponent();
                Object o=n.getUserObject();
                if(o instanceof DirectiveUIHints){
                    return (DirectiveUIHints)o;
                }
                else return null;
            }
        });
        
    }
    
    protected void populateDirectiveList(){
        DefaultTreeModel treeModel=(DefaultTreeModel)directivesTree.getModel();
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("Directives");
        DefaultMutableTreeNode other=new DefaultMutableTreeNode("Other");
        
        DirectiveManager directiveManager=DirectiveManager.getDirectiveManager();
        List<Class<? extends Directive>> directives=directiveManager.getDirectives();
        List<DirectiveUIHints> hints=new ArrayList<>();
        
        for(Class<? extends Directive> dir : directives){
            DirectiveUIHints h=DirectiveUIHints.getDirectiveUIHints(dir);
            hints.add(h);
        }
        
        Collections.sort(hints,new Comparator<DirectiveUIHints>(){
            @Override
            public int compare(DirectiveUIHints o1, DirectiveUIHints o2) {
                String l1=o1.getLabel();
                String l2=o2.getLabel();
                if(l1==null && l2!=null) return -1;
                else if(l1!=null && l2==null) return 1;
                else if(l1==null && l2==null) return 0;
                else return l1.compareTo(l2);
            }
        });
        
        ArrayList<DefaultMutableTreeNode> categoryNodes=new ArrayList<>();
        categoryNodes.add(other);
                
        for(DirectiveUIHints h: hints){
            boolean added=false;
            String categoryAll=h.getCategory();
            if(categoryAll==null) categoryAll="";
            String[] categories=categoryAll.split(";");
            for(String category : categories){
                DefaultMutableTreeNode parent=null;
                if(category!=null && category.trim().length()>0) {
                    category=category.trim();
                    if(!category.equalsIgnoreCase("Other")) {
                        for(DefaultMutableTreeNode n : categoryNodes ){
                            String name=n.toString();
                            if(name!=null && name.equalsIgnoreCase(category)) {
                                parent=n;
                                break;
                            }
                        }
                        
                        if(parent==null){
                            parent=new DefaultMutableTreeNode(category);
                            categoryNodes.add(parent);
                        }
                    }
                }
                
                if(parent!=null){
                    DefaultMutableTreeNode node=new DefaultMutableTreeNode(h);
                    parent.add(node);
                    added=true;
                }
            }
            
            if(!added){
                DefaultMutableTreeNode node=new DefaultMutableTreeNode(h);
                other.add(node);                
            }
        }
        
        Collections.sort(categoryNodes,new Comparator<DefaultMutableTreeNode>(){
            @Override
            public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        
        for(DefaultMutableTreeNode n: categoryNodes) { root.add(n); }
        
        treeModel.setRoot(root);
        
        
        
    }    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        directivesTree = new javax.swing.JTree();

        setLayout(new java.awt.BorderLayout());

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Directives");
        directivesTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane2.setViewportView(directivesTree);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree directivesTree;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
