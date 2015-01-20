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
 * SQLTable.java
 *
 * Created on 1. joulukuuta 2004, 19:25
 */

package org.wandora.application.tools.sqlconsole.gui;


import org.wandora.utils.Textbox;
import org.wandora.utils.swing.TableSorter;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import org.wandora.utils.*;
import org.wandora.utils.swing.*;


/**
 *
 * @author  akivela
 */
public class SQLTable extends JTable implements TableCellRenderer {
    
    boolean[] columnEditable;
    String[][] data;
    String[] columnNames;
    Color[] columnBackground;
    Color[] columnForeground;
    boolean tableChanged;
    TableCellRenderer cellRenderer;
    TableSorter sorter;
    
    private KirjavaTableModel kirjavaModel;
    
    public SQLTable(String[][] data, String[] columnNames) {
        super(data, columnNames);
        this.data = data;
        this.columnNames = columnNames;
        this.tableChanged = false;
        this.columnBackground = new Color[columnNames.length];
        this.columnForeground = new Color[columnNames.length];

        columnEditable=new boolean[columnNames.length];
        for(int i=0;i<columnEditable.length;i++) columnEditable[i]=true;
        /*
        for(int i=0; i<columnNames.length;i++) {
            TableColumn column = getColumnModel().getColumn(i);
            //column.setCellEditor(new DefaultCellEditor());
            column.setCellRenderer(new DefaultTableCellRenderer());
        }
         **/
        
        kirjavaModel=new KirjavaTableModel();
        this.sorter = new TableSorter(kirjavaModel);
        setModel(sorter);
//      setPreferredSize(new java.awt.Dimension(640, getRowCount()*getRowHeight()));
//      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JTableHeader header = getTableHeader(); 
        sorter.setTableHeader(header);

    }

    
    
    // -------------------------------------------------------------------------
    
    public void setColumnEditable(int c,boolean editable){
        columnEditable[c]=editable;
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
        return this;
    }
    
    public int convertRowIndexToModel(int row){
        return sorter.modelIndex(row);
    }
    public int convertRowIndexToView(int row){
        return 0;
        //return sorter.viewIndex(row);
    }
    
    public Component getTableCellRendererComponent(javax.swing.JTable table, Object color, boolean isSelected, boolean hasFocus,int row, int column) {
        TableCellRenderer defaultRenderer = super.getCellRenderer(row, column);
        Component c = defaultRenderer.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, column);
        
        if(!isSelected && !hasFocus) {
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);
            int rc = convertColumnIndexToModel(column);
            if(columnBackground[rc] != null) {
                c.setBackground(columnBackground[rc]);
            }
            if(columnForeground[rc] != null) {
                c.setForeground(columnForeground[rc]);
            }
        }
        return c;
    }
    
    
    // -------------------------------------------------------------------------
    
        
    public void setColumnBackground(int column, Color color) {
        columnBackground[column] = color;
    }
    public void setColumnForeground(int column, Color color) {
        columnForeground[column] = color;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public String getToolTipText(java.awt.event.MouseEvent e){
        try {
            java.awt.Point p=getTablePoint(e);
            return (String) getModel().getValueAt(p.x, p.y);
        }
        catch(Exception ex) {
            return null;
        }
    }
    
    
    
    public Point getTablePoint(java.awt.event.MouseEvent e) {
        try {
            java.awt.Point p=e.getPoint();
            int row=rowAtPoint(p);
            int col=columnAtPoint(p);
            //int realCol=convertColumnIndexToModel(col);
            return new Point(row, col);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public String[] getRow(int r){
        return data[r];
    }
    
    public Collection<Integer> getEditedRows(){
        return kirjavaModel.getEditedRows();
    }
    
    public boolean isCellEditable(int row,int col){
        return kirjavaModel.isCellEditable(row,col);
    }
    

    
    // -------------------------------------------------------------------------
    
    
     
   private class KirjavaTableModel extends AbstractTableModel {
        private HashSet<Integer> editedRows=new HashSet<Integer>();
        private Collection<Integer> getEditedRows(){
            return editedRows;
        }
       
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int columnIndex){
            return columnNames[columnIndex];
        }
        
        public boolean isCellEditable(int row,int col){
            return columnEditable[col];
        }
        
        // ----
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }
        
        
        
        public void setValueAt(Object value, int row, int col) {
            try {
                String oldValue = data[row][col];
                String newValue = Textbox.trimExtraSpaces((String) value);
                if(oldValue == null || !oldValue.equals(newValue)) {
                    data[row][col] = newValue;
                    fireTableCellUpdated(row, col);
                    editedRows.add(row);
                    tableChanged = true;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

   
    
    
}
