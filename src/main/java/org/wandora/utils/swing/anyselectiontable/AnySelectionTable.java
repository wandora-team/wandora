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
 */



package org.wandora.utils.swing.anyselectiontable;



import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

/**
  * A JTable which allows any selection of cells.
  * @author Jan-Friedrich Mutter (jmutter@bigfoot.de)
  * @author Aki Kivela / Wandora Team
  */
public class AnySelectionTable extends JTable {
    
    private static final long serialVersionUID = 1L;
    
    private TableSelectionModel tableSelectionModel;

    /**
    * Doing almost the same as its parent except
    * creating its own SelectionModel and UI
    */
    public AnySelectionTable() {
        super();
        createDefaultTableSelectionModel();
        setUI(new AnySelectionTableUI());
    }

    /**
    * Doing almost the same as its parent except
    * creating its own SelectionModel and UI
    */
    public AnySelectionTable(TableModel dm) {
        super(dm);
        createDefaultTableSelectionModel();
        setUI(new AnySelectionTableUI());
    }

    /**
    * refers to its TableSelectionModel.
    */
    @Override
    public boolean isCellSelected(int row, int column) {
        return tableSelectionModel.isSelected(row, convertColumnIndexToModel(column));
    }

    /**
    * Creates a default TableSelectionModel.
    */
    public void createDefaultTableSelectionModel() {
        TableSelectionModel tsm = new TableSelectionModel();
        setTableSelectionModel(tsm);
    }
    


    /**
    * same intention as setSelectionModel(ListSelectionModel newModel)
    */
    public void setTableSelectionModel(TableSelectionModel newModel) {
        //the TableSelectionModel shouldn't be null
        if (newModel == null) {
            throw new IllegalArgumentException("Can't set a null TableSelectionModel");
        }

        //save the old Model
        TableSelectionModel oldModel = this.tableSelectionModel;
        //set the new Model
        this.tableSelectionModel = newModel;
        //The model needs to know how many columns are there
        newModel.setColumns(getColumnModel().getColumnCount());
        getModel().addTableModelListener(newModel);

        if (oldModel != null) {
            removePropertyChangeListener(oldModel);
        }
        addPropertyChangeListener(newModel);
        firePropertyChange("tableSelectionModel", oldModel, newModel);
    }

    /**
    * @return the current TableSelectionModel.
    */
    public TableSelectionModel getTableSelectionModel() {
        return tableSelectionModel;
    }
  
  
    @Override
    public void clearSelection() {
        if(tableSelectionModel != null) {
            tableSelectionModel.clearSelection();
        }
        super.getSelectionModel().clearSelection();
        super.getColumnModel().getSelectionModel().clearSelection();
        this.repaint();
    }
    

    @Override
    public void selectAll() {
        if(tableSelectionModel != null) {
            int colCount = this.getColumnCount();
            int rowCount = this.getRowCount();
            for(int c=0; c<colCount; c++) {
                ListSelectionModel columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                if(columnSelectionModel != null) {
                    columnSelectionModel.addSelectionInterval(0, rowCount);
                }
            }
        }
        this.repaint();
    }
    
    
    
    public void invertSelection() {
        if(tableSelectionModel != null) {
            int colCount = this.getColumnCount();
            int rowCount = this.getRowCount();
            for(int c=0; c<colCount; c++) {
                ListSelectionModel columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                if(columnSelectionModel != null) {
                    for(int r=0; r<rowCount; r++) {
                        if(columnSelectionModel.isSelectedIndex(r)) {
                            columnSelectionModel.removeSelectionInterval(r, r);
                        }
                        else {
                            columnSelectionModel.addSelectionInterval(r, r);
                        }
                    }
                }
            }
        }
        this.repaint();
    }
    
    
    public void selectColumn(int column) {
        int rowCount = this.getRowCount();
        int colCount = this.getColumnCount();
        if(tableSelectionModel != null && column < colCount) {
            ListSelectionModel columnSelectionModel = tableSelectionModel.getListSelectionModelAt(column);
            if(columnSelectionModel != null) {
                columnSelectionModel.addSelectionInterval(0, rowCount-1);
            }
        }
        this.repaint();
    }
    
    
    public void deselectColumn(int column) {
        int rowCount = this.getRowCount();
        int colCount = this.getColumnCount();
        if(tableSelectionModel != null && column < colCount) {
            ListSelectionModel columnSelectionModel = tableSelectionModel.getListSelectionModelAt(column);
            if(columnSelectionModel != null) {
                columnSelectionModel.removeSelectionInterval(0, rowCount-1);
            }
        }
        this.repaint();
    }
    
    
    
    
    public void selectColumns() {
        if(tableSelectionModel != null) {
            int colCount = this.getColumnCount();
            int rowCount = this.getRowCount();
            for(int c=0; c<colCount; c++) {
                ListSelectionModel columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                if(columnSelectionModel != null) {
                    if(!columnSelectionModel.isSelectionEmpty()) {
                        columnSelectionModel.addSelectionInterval(0, rowCount-1);
                    }
                }
            }
        }
        this.repaint();
    }
    
    
    public void selectRow(int row) {
        int colCount = this.getColumnCount();
        int rowCount = this.getRowCount();
        if(tableSelectionModel != null && row < rowCount) {
            ListSelectionModel columnSelectionModel = null;
            for(int c=0; c<colCount; c++) {
                columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                if(columnSelectionModel != null) {
                    columnSelectionModel.addSelectionInterval(row, row);
                }
            }
        }
        this.repaint();
    }
    
    
    public void deselectRow(int row) {
        int colCount = this.getColumnCount();
        int rowCount = this.getRowCount();
        if(tableSelectionModel != null && row < rowCount) {
            ListSelectionModel columnSelectionModel = null;
            for(int c=0; c<colCount; c++) {
                columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                if(columnSelectionModel != null) {
                    columnSelectionModel.removeSelectionInterval(row, row);
                }
            }
        }
        this.repaint();
    }
    
    
    
    public void selectRows() {
        if(tableSelectionModel != null) {
            int colCount = this.getColumnCount();
            int rowCount = this.getRowCount();
            ListSelectionModel columnSelectionModel = null;
            for(int r=0; r<rowCount; r++) {
                boolean selectRow = false;
                for(int c=0; c<colCount; c++) {
                    columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                    if(columnSelectionModel != null) {
                        if(columnSelectionModel.isSelectedIndex(r)) {
                            selectRow = true;
                            break;
                        }
                    }
                }
                if(selectRow) {
                    for(int c=0; c<colCount; c++) {
                        columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                        if(columnSelectionModel != null) {
                            columnSelectionModel.addSelectionInterval(r, r);
                        }
                    }
                }
            }
        }
        this.repaint();
    }
    
    
    
    public void selectCells(List<int[]> cells) {
        if(cells != null && !cells.isEmpty()) {
            for(int[] cell : cells) {
                if(cell != null && cell.length == 2) {
                    selectCell(cell[1], cell[0]);
                }
            }
        }
    }
    
    
    public void selectCell(int c, int r) {
        //System.out.println("Selecting c,r == "+c+","+r);
        if(tableSelectionModel != null) {
            int colCount = this.getColumnCount();
            int rowCount = this.getRowCount();
            if(c >= 0 && c < colCount && r >= 0 && r < rowCount) {
                ListSelectionModel columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                if(columnSelectionModel != null) {
                    //System.out.println("Selecting c,r == "+c+","+r);
                    columnSelectionModel.addSelectionInterval(r, r);
                }
            }
        }
        this.repaint();
    }
    
    
    
    public void selectArea(int c1, int r1, int c2, int r2) {
        if(tableSelectionModel != null) {
            int colCount = this.getColumnCount();
            int rowCount = this.getRowCount();
            c1 = Math.min(c1, colCount);
            c2 = Math.min(c2, colCount);
            r1 = Math.min(r1, rowCount);
            r2 = Math.min(r2, rowCount);
            
            int cs = c1;
            int ce = c2;
            if(c2 < c1) {
                cs = c2;
                ce = c1;
            }
            int rs = r1;
            int re = r2;
            if(r2 < r1) {
                rs = r2;
                re = r1;
            }
            
            for(int c=cs; c<=ce; c++) {
                ListSelectionModel columnSelectionModel = tableSelectionModel.getListSelectionModelAt(c);
                if(columnSelectionModel != null) {
                    columnSelectionModel.addSelectionInterval(rs, re);
                }
            }
        }
        this.repaint();
    }
    
    
}
