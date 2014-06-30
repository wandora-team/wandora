/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * OperationTableModel.java
 *
 */


package org.wandora.application.gui.table;

import javax.swing.table.DefaultTableModel;
import org.wandora.topicmap.undowrapper.UndoOperation;

/**
 *
 * @author akivela
 */


public class OperationTableModel extends DefaultTableModel {
    private UndoOperation[] ops = null;
    
    
    public OperationTableModel(UndoOperation[] operations) {
        this.ops = operations;
    }
    

    @Override
    public int getColumnCount() {
        return 6;
    }

    


    @Override
    public int getRowCount() {
        if(ops != null) return ops.length;
        return 0;
    }

    
   
    


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if(ops != null && rowIndex >= 0 && rowIndex < getRowCount()) {
                switch(columnIndex) {
                    case 0: {
                        String i = ""+ops[rowIndex].getOperationNumber();
                        while(i.length()<5) i="0"+i;
                        return i;
                    }
                    case 1: {
                        return ops[rowIndex].getUndoLabel();
                    }
                    case 2: {
                        return ops[rowIndex].getRedoLabel();
                    }
                    case 3: {
                        return ops[rowIndex].getDescription();
                    }
                    case 4: {
                        return ""+ops[rowIndex].getClass().toString();
                    }
                    case 5: {
                        return ""+ops[rowIndex].isMarker();
                    }
                }
            }
        }
        catch (Exception e) {}
        return "[ERROR]";
    }


    
    
    @Override
    public String getColumnName(int columnIndex){
        try {
            switch(columnIndex) {
                case 0: {
                    return "index";
                }
                case 1: {
                    return "undo label";
                }
                case 2: {
                    return "redo label";
                }
                case 3: {
                    return "description";
                }
                case 4: {
                    return "class";
                }
                case 5: {
                    return "is marker";
                }
            }
            return "";
        }
        catch (Exception e) {}
        return "[ERROR]";
    }



    @Override
    public boolean isCellEditable(int row, int col){
        return false;
    }

    
    public boolean isMarker(int row) {
        if(ops != null && row >= 0 && row < getRowCount()) {
            return ops[row].isMarker();
        }
        return false;
    }

    
}
