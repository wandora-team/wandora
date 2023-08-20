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
 * SearchTable.java
 *
 * Created on 27. joulukuuta 2005, 20:57
 *
 */

package org.wandora.application.gui.search;


import java.awt.Component;
import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.wandora.application.Wandora;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.swing.TableSorter;



/**
 * @author akivela
 */
public class SearchTable extends JTable {
    
    private Object[][] data;
    private TableSorter sorter;
    private Wandora wandora;
    private String[] res;
    private JDialog dialog;
    
    
    
    /** Creates a new instance of SearchTable */
    public SearchTable(String[] res, Wandora parent, JDialog dialog) {
        this.wandora=parent;
        this.res=res;
        this.dialog=dialog;
        data=new Object[res.length/3][2];
        
        for(int i=0;i<res.length/3;i++){
            try { data[i][0]=Double.valueOf(res[i*3]); }
            catch (Exception e) { data[i][0]=Double.valueOf( 0 ); }
            data[i][1]=res[i*3+2];
        }
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(false);
        sorter=new TableSorter(new SearchTableModel());
        this.setAutoCreateColumnsFromModel(false);
        this.setModel(sorter);
        TableColumn column=new TableColumn(0,80,null,null);
        column.setMaxWidth(80);
        this.addColumn(column);
        this.addColumn(new TableColumn(1,80,null,new TopicCellEditor()));
        sorter.setTableHeader(this.getTableHeader());

    }
    
    @Override
    public String getToolTipText(java.awt.event.MouseEvent e){
        java.awt.Point p=e.getPoint();
        int row=rowAtPoint(p);
        int col=columnAtPoint(p);
        int realCol=convertColumnIndexToModel(col);
        if(realCol==0) return null;
        else return sorter.getValueAt(row,realCol).toString();
    }
        
    private class TopicCellEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.MouseListener {        
        private int topic;
        private JLabel label;
        
        public TopicCellEditor(){
            label= new JLabel();
            Font f=label.getFont();
            label.setFont(new Font(f.getName(),Font.PLAIN,f.getSize()));
            label.addMouseListener(this);
        }
        
        @Override
        public Object getCellEditorValue() {
            return res[topic*3+2];
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            topic=sorter.modelIndex(row);
            label.setText(res[topic*3+2]);
            return label;
        }
        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            fireEditingStopped();
            if(label.contains(e.getPoint())){
                try {
                    Topic t=wandora.getTopicMap().getTopic(res[topic*3+1]);
                    dialog.setVisible(false);
                    if(t!=null) wandora.openTopic(t);
                } 
                catch(TopicMapException tme){
                    tme.printStackTrace(); // TODO EXCEPTION
                    dialog.setVisible(false);
                }
            }
        }
        
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {}
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {}
        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {}
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {}
        
    }
    
    private class SearchTableModel extends AbstractTableModel {
        
        
        @Override
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public int getRowCount() {
            return res.length/3;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }
        
        @Override
        public String getColumnName(int columnIndex){
            if(columnIndex==0) return "Score";
            else return "Topic";
        }
        
        
        @Override
        public boolean isCellEditable(int row,int col){
            if(col==1) return true;
            else return false;
        }
        
    }
    
}
