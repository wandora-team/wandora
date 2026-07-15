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
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author olli
 */
public class TreeTableModelAdapter extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    
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
