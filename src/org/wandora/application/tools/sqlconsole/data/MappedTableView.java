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
 * 
 * TableView.java
 *
 * Created on 3. joulukuuta 2004, 14:18
 */

package org.wandora.application.tools.sqlconsole.data;



import org.wandora.application.tools.sqlconsole.*;
import java.util.*;
import java.awt.*;

/**
 *
 * @author  akivela
 */
public class MappedTableView implements TableView {
    
    
    protected TableView table;
    protected int[] rowMap;
    protected int[] columnMap;
    
    
    /** Creates a new instance of TableView */
    public MappedTableView(TableView t) {
        table = t;
        rowMap = new int[table.getRowCount()];
        columnMap = new int[table.getColumnCount()];
    }
    
    
    
    public void setRowMap(int[] map) {
        rowMap = map;
    }
    
    
    public void setColunmMap(int[] map) {
        columnMap = map;
    } 
    
    
    public void resetView() {
        rowMap = new int[table.getRowCount()];
        for(int i=0; i<rowMap.length; i++) rowMap[i] = i;
        columnMap = new int[table.getColumnCount()];
        for(int i=0; i<columnMap.length; i++) columnMap[i] = i;
    }
    
    
    // -------------------------------------------------------------------------
    
    public String[][] getView() {
        String[][] view = new String[rowMap.length][columnMap.length];
        //Logger.println("filtered rows = " + rowMap.length + ", filtered columns " + columnMap.length);
        for(int j=0; j<rowMap.length; j++) {
            for(int i=0; i<columnMap.length; i++) {
                view[j][i] = getAt(j,i);
            }
        }
        return view;
    }
    
    
    
    public String[] getColumnNames() {
        String[] colunmNames = table.getColumnNames();
        String[] view = new String[columnMap.length];
        for(int i=0; i<columnMap.length; i++) {
            view[i] = colunmNames[columnMap[i]];
        }
        return colunmNames;
    }
    
    public boolean isColumnEditable(int col){
        return table.isColumnEditable(convertColunmIndex(col));
    }
    public void setColumnEditable(int col,boolean editable){
        table.setColumnEditable(convertColunmIndex(col),editable);
    }
    

        
    
    public String getAt(Point p) {
        return getAt(p.x, p.y);
    }    
    
    public Object[] getHiddenData(int r){
        return table.getHiddenData(convertRowIndex(r));
    }
    
    public String getAt(int r, int c) {
        return table.getAt(convertRowIndex(r), convertColunmIndex(c));
    }    
    
    public void setAt(Point p, String val) {
        setAt(p.x, p.y, val);
    }
    
    public void setAt(int r, int c, String val) {
        table.setAt(convertRowIndex(r), convertColunmIndex(c), val);
    }
    
    public String[] getRow(int r) {
        return table.getRow(convertRowIndex(r));
    }    
    
    public String[][] getRows(int[] rs) {
        int[] crs = new int[rs.length];
        for(int i=0; i<rs.length; i++) {
            crs[i] = convertRowIndex(rs[i]);
        }
        return table.getRows(crs);
    }
    
    
    public String[] getColumn(int c) {
        return table.getColumn(convertColunmIndex(c));
    }
    
    public String[][] getColumns(int[] cs) {
        int[] ccs = new int[cs.length];
        for(int i=0; i<cs.length; i++) {
            ccs[i] = convertColunmIndex(cs[i]);
        }
        return table.getColumns(ccs);
    }    
    
    
    public int getColumnCount() {
        return columnMap.length;
    }    
    
    public int getRowCount() {
        return rowMap.length;
    }    
    
    
    // -------------------------------------------------------------------------
    
    
    private int convertRowIndex(int i) {
        return rowMap[i];
    }
    
    private int[] convertRowIndexes(int[] indexes) {
        int[] mappedIndexes = new int[indexes.length];
        for(int i=0; i<indexes.length; i++) {
            mappedIndexes[i] = convertRowIndex(indexes[i]);
        }
        return mappedIndexes;
    }
    
    
    private int convertColunmIndex(int i) {
        return columnMap[i];
    }
    
    
    public void deleteRows(int[] rowsToDelete) {
        int[] mappedRows = convertRowIndexes(rowsToDelete);
        table.deleteRows(mappedRows);
        resetView();
    }

    
    
    
    public void hideRows(int [] rowsToHide) {
        int[] newRowMap = new int[rowMap.length - rowsToHide.length];
        int p=0;
        for(int i=0; i<rowMap.length; i++) {
            if(p < rowsToHide.length && i == rowsToHide[p]) {
                p++;
            }
            else {
                newRowMap[i-p] = rowMap[i];                
            }
        }
        rowMap = newRowMap;
    }
    
    
    
    
    public void insertRows(int pos, int number) {
        table.insertRows(convertRowIndex(pos), number);
        resetView();
    }
 
    
    
    // -------------------------------------------------------------------------
    
    
    
}
