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
 * TableView.java
 *
 * Created on 3. joulukuuta 2004, 15:59
 */

package org.wandora.application.tools.sqlconsole.data;


import java.awt.Point;

/**
 *
 * @author  akivela
 */
public interface TableView {
    
    void resetView();
    
    
    String[] getColumnNames();
    String[] getRow(int r);
    String[][] getRows(int[] r);
    String[] getColumn(int c);
    String[][] getColumns(int[] c);
    
    String[][] getView();
    
    public Object[] getHiddenData(int r);
       
    public String getAt(Point p);
    public String getAt(int r, int c);
       
    public void setAt(Point p, String val);
    public void setAt(int r, int c, String val);

    public int getRowCount();
    public int getColumnCount();
    
    public void insertRows(int pos, int number);
    public void deleteRows(int[] rowsToDelete);
    
    public boolean isColumnEditable(int col);
    public void setColumnEditable(int col,boolean editable);
    
    //public TableViewUpdater getUpdater();
    //public void setUpdater(TableViewUpdater updater);
}
