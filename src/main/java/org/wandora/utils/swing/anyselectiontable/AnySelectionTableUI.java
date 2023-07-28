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



import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.event.MouseInputListener;

/**
  * This class doesn't change the L&F of the JTable but
  * listens to mouse clicks and updates the TableSelectionModel.
  * 
  * @author Jan-Friedrich Mutter (jmutter@bigfoot.de)
  * @author Aki Kivela / Wandora Team
  */
public class AnySelectionTableUI extends BasicTableUI {
    
    private int lastTableSelectionRow = -1;
    private int lastTableSelectionColumn = -1;
    
    
    public static ComponentUI createUI(JComponent c) {
        return new AnySelectionTableUI();
    }

    
    
    @Override
    protected MouseInputListener createMouseInputListener() {
        return new AnySelectionMouseInputHandler();
    }

    
    
    /**
    * to get access to the table from the inner class MyMouseInputHandler
    */
    protected JTable getTable() {
        return table;
    }

    
    
    /**
    * updates the TableSelectionModel.
    */
    protected void updateTableSelectionModel(int row, int column, boolean ctrlDown, boolean shiftDown, boolean isDrag) {
        AnySelectionTable t = (AnySelectionTable)getTable();
        column = t.convertColumnIndexToModel(column);
        TableSelectionModel tsm = t.getTableSelectionModel();

        if(ctrlDown) {
            if (tsm.isSelected(row, column)) {
                lastTableSelectionRow = -1;
                lastTableSelectionColumn = -1;
                tsm.removeSelection(row, column);
            } 
            else {
                lastTableSelectionRow = row;
                lastTableSelectionColumn = column;
                tsm.addSelection(row, column);
            }
        } 
        else if(shiftDown && lastTableSelectionRow != -1 && lastTableSelectionColumn != -1) {
            t.selectArea(lastTableSelectionColumn, lastTableSelectionRow, column, row);
        } 
        else if(!isDrag) {
            lastTableSelectionRow = row;
            lastTableSelectionColumn = column;
            t.clearSelection();
            tsm.setSelection(row, column);
        }
    }


  
  
  
  public class AnySelectionMouseInputHandler extends MouseInputHandler {

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            
            if(!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            JTable t = getTable();
            Point p = e.getPoint();
            int row = t.rowAtPoint(p);
            int column = t.columnAtPoint(p);
            int rowCount = t.getRowCount();
            int columnCount = t.getColumnCount();

            if(column < 0 || row < 0 || column >= columnCount || row >= rowCount ) {
                return;
            }

            TableCellEditor tce = t.getCellEditor();
            if((tce==null) || (tce.shouldSelectCell(e))) {
                t.requestFocus();
                updateTableSelectionModel(row, column, e.isControlDown(), e.isShiftDown(), false);
                t.repaint();
            }
        }
        
        
        
        @Override
        public void mouseDragged(MouseEvent e) {           
            super.mouseDragged(e);

            if(!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            
            JTable t = getTable();
            Point p = e.getPoint();
            int row = t.rowAtPoint(p);
            int column = t.columnAtPoint(p);
            int rowCount = t.getRowCount();
            int columnCount = t.getColumnCount();

            if(column < 0 || row < 0 || column >= columnCount || row >= rowCount ) {
                return;
            }

            TableCellEditor tce = t.getCellEditor();
            if(tce==null) {
                t.requestFocus();
                updateTableSelectionModel(row, column, e.isControlDown(), !e.isShiftDown(), true);
                t.repaint();
            }
        }
    }
}
