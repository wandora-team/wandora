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
 * AbstractTable.java
 *
 * Created on 30. marraskuuta 2004, 17:40
 */

package org.wandora.application.tools.sqlconsole.data;


import java.awt.Point;
import java.util.StringTokenizer;
import java.util.Vector;

import org.wandora.utils.Tuples.T2;


/**
 *
 * @author  akivela
 */
public class RowTable implements TableView {
       
    
    
    private Row[] rows;
    private String[] columns;
    private boolean[] columnsEditable;
    private TableViewUpdater updater;
    
    
    /** Creates a new instance of AbstractTable */
    public RowTable(String[] c) {
        columns = c;
        columnsEditable=new boolean[columns.length];
        for(int i=0;i<columnsEditable.length;i++) columnsEditable[i]=false;
        rows = null;
    }
    
    public RowTable(String[] columns,java.util.List<Object[]> rows){
        this.columns=columns;
        this.rows=new Row[rows.size()];
        int ptr=0;
        columnsEditable=new boolean[columns.length];
        for(int i=0;i<columnsEditable.length;i++) columnsEditable[i]=false;
        for(Object[] r : rows){
            Row row=new Row(r,columns.length);
            this.rows[ptr++]=row;
        }
    }
    /** @param columns Column name and editable flag. */
    public RowTable(T2<String,Boolean>[] columns,java.util.List<Object[]> rows){
        this.columns=new String[columns.length];
        columnsEditable=new boolean[columns.length];
        for(int i=0;i<columns.length;i++){
            this.columns[i]=columns[i].e1;
            this.columnsEditable[i]=columns[i].e2;
        }
        this.rows=new Row[rows.size()];
        int ptr=0;
        for(Object[] r : rows){
            Row row=new Row(r,columns.length);
            this.rows[ptr++]=row;
        }
    }

    public TableViewUpdater getUpdater(){
        return updater;
    }
    public void setUpdater(TableViewUpdater updater){
        this.updater=updater;
    }
    
    
    public void resetView() {
        // DO NOTHING!
    }
    
    
    
    
    public String getAt(Point p) {
        return getAt(p.x, p.y);
    }
    public String getAt(int r, int c) {
        Row row = rows[r];
        return row.getColumn(c);
    }
    
    public Object[] getHiddenData(int r){
        return rows[r].getHiddenData();
    }
    
    
    public void setAt(Point p, String val) {
        setAt(p.x, p.y, val);
    }
    public void setAt(int r, int c, String val) {
        Row row = rows[r];
        row.setColumn(c, val);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public int getRowCount() {
        return rows.length;
    }
    
    public int getColumnCount() {
        return columns.length;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
        
    public void insertRows(int pos, int number) {
        Row[] newRows = new Row[rows.length + number];
        int endpos = pos + number;
        for(int i=0; i<newRows.length; i++) {
            if(i<pos) newRows[i] = rows[i];
            else if(i>=pos && i<endpos) newRows[i] = new Row(columns.length);
            else newRows[i] = rows[i-number];
        }
        rows = newRows;
    }
    
    
    public void deleteRows(int[] rowsToDelete) {
        int p = 0;
        Row[] newRows = new Row[rows.length - rowsToDelete.length];
        for(int i=0; i<rows.length; i++) {
            if(p < rowsToDelete.length && i == rowsToDelete[p]) {
                p++;
            }
            else {
                //Logger.println(" index " + (i-p) + " == " + i);
                newRows[i-p] = rows[i];                
            }
        }
        rows = newRows;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    public String[][] getView() {
        String[][] table = new String[rows.length][columns.length];
        String[] row;
        for(int i=0; i<rows.length; i++) {
            row = rows[i].getColumns();
            for(int j=0; j<row.length; j++) {
                table[i][j] = row[j];
            }
        }
        return table;
    }
    
    
    
    public String[][] getRows(int[] rs) {
        String[][] subtable = new String[rs.length][columns.length];
        String[] row;
        for(int i=0; i<rs.length; i++) {
            row = rows[rs[i]].getColumns();
            for(int j=0; j<row.length; j++) {
                subtable[i][j] = row[j];
            }            
        }
        return subtable;
    }
    
    
    
    public String[] getRow(int r) {
        Row row = rows[r];
        return row.getColumns();
    }
    
    
        
    public String[] getColumn(int c) {
        int size = rows.length;
        String[] column = new String[rows.length];
        for(int i=0; i<rows.length; i++) {
            column[i] = rows[i].getColumn(c);
        }
        return column;
    }
    
    
    
    
    public String[][] getColumns(int[] cs) {
        String[][] subtable = new String[rows.length][cs.length];
        String[] row;
        for(int i=0; i<rows.length; i++) {
            row = rows[i].getColumns();
            for(int j=0; j<cs.length; j++) {
                subtable[i][cs[j]] = row[j];
            }            
        }
        return subtable;
    }
    
 
    
    
    public String[] getColumnNames() {
        return columns;
    }
    
    
    
    
    
    public Row findRowWithColumn(int c, String colunmContent) {
        Row row;
        for(int i=0; i<rows.length; i++) {
            row = rows[i];
            if(colunmContent.equals(row.getColumn(c))) return row;
        }
        return null;
    }
    
    
    
    public boolean isColumnEditable(int col){
        return columnsEditable[col];
    }
    public void setColumnEditable(int col,boolean editable){
        columnsEditable[col]=editable;
    }
    

    
    // -------------------------------------------------------------------------
    
    
    
    public void importFromFile(String resourceName, String importOrder) {
        try {
            Vector<Row> rowVector = new Vector<>();
            if(importOrder == null || importOrder.length() < 1) importOrder="0123456789";
            if(resourceName != null) {
                String s = ""; // IObox.loadResource(resourceName);
                StringTokenizer lines = new StringTokenizer(s, "\n");
                while(lines.hasMoreTokens()) {
                    String line = lines.nextToken();
                    StringTokenizer parts = new StringTokenizer(line, "\t");
                    int currentPart = 0;
                    Row row = new Row(columns.length);
                    while(parts.hasMoreTokens()) {
                        String part = parts.nextToken();
                        if(part != null && part.length() > 0) {
                            int p = currentPart;
                            try { p = Integer.parseInt(importOrder.substring(currentPart, currentPart+1)); }
                            catch(Exception e) { e.printStackTrace(); }
                            //Logger.println("column " + p + " part " + part);
                            row.setColumn(p, part);
                        }
                        currentPart++;
                    }
                    if(currentPart > 0) {
                        rowVector.add(row);
                    }
                    else {
                        //Logger.println("Rejecting line!");
                    }
                }
                rows = (Row[]) rowVector.toArray(new Row[rowVector.size()]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    
    
}
