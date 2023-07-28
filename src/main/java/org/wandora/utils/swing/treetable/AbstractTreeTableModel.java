/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wandora.utils.swing.treetable;

import javax.swing.event.*;
import javax.swing.tree.*;

/**
 *
 * @author olli
 */
public abstract class AbstractTreeTableModel implements TreeTableModel {

    protected EventListenerList listeners;
    protected Object root;
    
//    public int getColumnCount();
//    public String getColumnName();
//    public Object getValueAt(Object node, int column);
//    public Object getChild(Object parent, int index);
//    public int getChildCount(Object parent);
    
    public AbstractTreeTableModel(Object root){
        this.root=root;
        listeners=new EventListenerList();
    }

    public Class getColumnClass(int cloumn){
        return Object.class;
    }
    
    public boolean isCellEditable(Object node, int column) {
        return getColumnClass(column) == TreeTableModel.class;
    }

    public void setValueAt(Object aValue, Object node, int column) {
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(TreeModelListener.class, l);
    }

    public int getIndexOfChild(Object parent, Object child) {
        for (int i = 0; i < getChildCount(parent); i++) {
	    if (getChild(parent, i).equals(child)) { 
	        return i; 
	    }
        }
	return -1; 
    }

    public Object getRoot() {
        return root;
    }

    public boolean isLeaf(Object node) {
        return getChildCount(node)==0;
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(TreeModelListener.class, l);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }
    
    public void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] ls = listeners.getListenerList();
        TreeModelEvent e = null;
        for (int i = ls.length-2; i>=0; i-=2) {
            if (ls[i]==TreeModelListener.class) {
                if (e == null) e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)ls[i+1]).treeNodesChanged(e);
            }          
        }
    }

    public void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] ls = listeners.getListenerList();
        TreeModelEvent e = null;
        for (int i = ls.length-2; i>=0; i-=2) {
            if (ls[i]==TreeModelListener.class) {
                if (e == null) e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)ls[i+1]).treeNodesInserted(e);
            }          
        }
    }

    public void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] ls = listeners.getListenerList();
        TreeModelEvent e = null;
        for (int i = ls.length-2; i>=0; i-=2) {
            if (ls[i]==TreeModelListener.class) {
                if (e == null) e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)ls[i+1]).treeNodesRemoved(e);
            }          
        }
    }

    public void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] ls = listeners.getListenerList();
        TreeModelEvent e = null;
        for (int i = ls.length-2; i>=0; i-=2) {
            if (ls[i]==TreeModelListener.class) {
                if (e == null) e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener)ls[i+1]).treeStructureChanged(e);
            }          
        }
    }

    

}
