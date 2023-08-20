/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wandora.utils.swing.treetable;
import javax.swing.tree.TreeModel;
/**
 *
 * @author olli
 */
public interface TreeTableModel extends TreeModel {
    public int getColumnCount();
    public String getColumnName(int column);
    public Class getColumnClass(int cloumn);
    public Object getValueAt(Object node,int column);
    public boolean isCellEditable(Object node,int column);
    public void setValueAt(Object aValue,Object node,int column);
}
