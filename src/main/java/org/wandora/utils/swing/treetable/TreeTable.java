/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wandora.utils.swing.treetable;

import java.awt.Component;
import java.awt.Graphics;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author olli
 */
public class TreeTable extends JTable {
    protected TreeTableCellRenderer tree;
    
    public TreeTable(TreeTableModel treeTableModel){
        super();
        tree=new TreeTableCellRenderer(treeTableModel);
        super.setModel(new TreeTableModelAdapter(treeTableModel, tree));
        tree.setSelectionModel(new DefaultTreeSelectionModel() {
            {
                setSelectionModel(listSelectionModel);
            }
        });
        tree.setRowHeight(getRowHeight());
        setDefaultRenderer(TreeTableModel.class, tree);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
//        setShowGrid(false);
//        setIntercellSpacing(new Dimension(0,0));
    }
    
    public JTree getTree(){
        return tree;
    }
    
    public Object getValueForRow(int row){
        TreePath path=tree.getPathForRow(row);
        if(path==null) return null;
        return path.getLastPathComponent();
    }
    
    @Override
    public int getEditingRow(){
        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
    }
    
    public class TreeTableCellRenderer extends JTree implements TableCellRenderer {

	protected int visibleRow;
   
	public TreeTableCellRenderer(TreeModel model) { 
	    super(model); 
	}

        @Override
	public void setBounds(int x, int y, int w, int h) {
	    super.setBounds(x, 0, w, TreeTable.this.getHeight());
	}

        @Override
	public void paint(Graphics g) {
	    g.translate(0, -visibleRow * getRowHeight());
	    super.paint(g);
	}

	public Component getTableCellRendererComponent(JTable table,Object value,
                                boolean isSelected,boolean hasFocus,int row, int column) {
	    if(isSelected) setBackground(table.getSelectionBackground());
	    else setBackground(table.getBackground());
       
	    visibleRow = row;
	    return this;
	}
    }

    
    
    public class TreeTableCellEditor implements TableCellEditor {
        protected EventListenerList listeners;
        
        public TreeTableCellEditor(){
            listeners=new EventListenerList();
        }
        
        public Object getCellEditorValue() { return null; }
        public boolean isCellEditable(EventObject e) { return true; }
        public boolean shouldSelectCell(EventObject anEvent) { return false; }
        public boolean stopCellEditing() { return true; }
        public void cancelCellEditing() {}

        public void addCellEditorListener(CellEditorListener l) {
            listeners.add(CellEditorListener.class, l);
        }

        public void removeCellEditorListener(CellEditorListener l) {
            listeners.remove(CellEditorListener.class, l);
        }

        protected void fireEditingStopped() {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==CellEditorListener.class) {
                    ((CellEditorListener)listeners[i+1]).editingStopped(new ChangeEvent(this));
                }	       
            }
        }

        protected void fireEditingCanceled() {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==CellEditorListener.class) {
                    ((CellEditorListener)listeners[i+1]).editingCanceled(new ChangeEvent(this));
                }	       
            }
        }
        
	public Component getTableCellEditorComponent(JTable table, Object value,boolean isSelected, int r, int c) {
	    return tree;
	}
    }
    
}
