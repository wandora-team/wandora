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
 * 
 */


package org.wandora.application.gui.table;

import java.awt.Color;
import javax.swing.table.DefaultTableModel;
import org.wandora.topicmap.Locator;



/**
 *
 * @author akivela
 */


public class LocatorTableModel extends DefaultTableModel {
        
    
    
    private String[] cols;
    private Locator[][] data;
    private Color[][] colors;

    
    
    public LocatorTableModel(Locator[][] tableData, String[] columnData, Color[][] colorData) {
        data = tableData;
        cols = columnData;
        colors = colorData;
    }
    
    
    
    @Override
    public int getColumnCount() {
        if(cols != null) return cols.length;
        return 0;
    }

    
    @Override
    public Class getColumnClass(int col) {
        return Locator.class;
    }
    


    @Override
    public int getRowCount() {
        if(data != null) return data.length;
        return 0;
    }

    
    
    public Color getColorAt(int rowIndex, int columnIndex) {
        try {
            if(data != null && rowIndex >= 0 && columnIndex >= 0) {
                return colors[rowIndex][columnIndex];
            }
        }
        catch (Exception e) {}
        return null;
    }
    
    


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if(data != null && rowIndex >= 0 && columnIndex >= 0 && columnIndex < getColumnCount() && rowIndex < getRowCount()) {
                return data[rowIndex][columnIndex];
            }
        }
        catch (Exception e) {}
        return "[ERROR]";
    }


    
    
    @Override
    public String getColumnName(int columnIndex){
        try {
            if(cols != null && columnIndex >= 0 && cols.length > columnIndex && cols[columnIndex] != null) {
                return cols[columnIndex];
            }
            return "";
        }
        catch (Exception e) {}
        return "[ERROR]";
    }



    @Override
    public boolean isCellEditable(int row,int col){
        return false;
    }


}
