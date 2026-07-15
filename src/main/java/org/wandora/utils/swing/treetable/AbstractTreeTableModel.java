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
 */

package org.wandora.utils.swing.treetable;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

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
