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
 * OperationTable.java
 *
 */

package org.wandora.application.gui.table;

import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.wandora.application.gui.simple.SimpleTable;
import org.wandora.topicmap.undowrapper.UndoOperation;

/**
 *
 * @author akivela
 */


public class OperationTable extends SimpleTable /*, DropTargetListener, DragGestureListener*/ {
    
    private static final long serialVersionUID = 1L;
    
    private OperationTableModel opsModel = null;
    

    public void initialize(UndoOperation[] tableOperations) {
        opsModel = new OperationTableModel(tableOperations);
        initialize();
    }
    
    public void initialize(List<UndoOperation> tableOperations) {
        opsModel = new OperationTableModel(tableOperations.toArray(new UndoOperation[] {}));
        initialize();
    }
    
    
    private void initialize() {
        setDefaultRenderer(Object.class, new OperationTableRenderer(this));
        
        this.setModel(opsModel);
        setRowSorter(new TableRowSorter(opsModel));
        
        this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.setRowSelectionAllowed(false);
        
        TableColumnModel cm = this.getColumnModel();
        cm.getColumn(0).setPreferredWidth(40);
        cm.getColumn(1).setPreferredWidth(100);
        cm.getColumn(2).setPreferredWidth(100);
        cm.getColumn(3).setPreferredWidth(200);
        cm.getColumn(4).setPreferredWidth(200);
        cm.getColumn(5).setPreferredWidth(40);
    }
    
    
    public OperationTableModel getOperationDataModel() {
        return opsModel;
    }
    
}
