/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wandora.utils.swing.treetable;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author olli
 */
public class TreeTableModelAdapter extends AbstractTableModel {

    protected JTree tree;
    protected TreeTableModel treeTableModel;
    
    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree){
        this.treeTableModel=treeTableModel;
        this.tree=tree;
        tree.addTreeExpansionListener(new TreeExpansionListener(){
            public void treeExpanded(TreeExpansionEvent event){
                fireTableDataChanged();
            }
            public void treeCollapsed(TreeExpansionEvent event){
                fireTableDataChanged();
            }
        });
    }
    
    @Override
    public String getColumnName(int column){
        return treeTableModel.getColumnName(column);
    }
    
    public int getColumnCount() {
        return treeTableModel.getColumnCount();
    }

    public int getRowCount() {
        return tree.getRowCount();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return treeTableModel.getValueAt(tree.getPathForRow(rowIndex).getLastPathComponent(), columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return treeTableModel.isCellEditable(tree.getPathForRow(rowIndex).getLastPathComponent(), columnIndex);        
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        treeTableModel.setValueAt(aValue, tree.getPathForRow(rowIndex).getLastPathComponent(), columnIndex);        
    }
    
    @Override
    public Class getColumnClass(int column){
        return treeTableModel.getColumnClass(column);
    }
    

}
