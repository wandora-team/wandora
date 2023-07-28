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
 * OperationTableRenderer.java
 *
 * Created on 2013-04-28.
 *
 */

package org.wandora.application.gui.table;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.wandora.application.gui.UIConstants;


/**
 *
 * @author akivela
 */


public class OperationTableRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    private OperationTable table;
    private OperationTableModel model;
    
    public OperationTableRenderer(OperationTable t) {
        this.table = t;
        this.model = table.getOperationDataModel();
    }

    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
            if(model.isMarker(row)) {
                c.setBackground(UIConstants.defaultActiveBackground);
            }
            else {
                c.setBackground(Color.WHITE);
            }
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION;
            // uriLabel.setText("*** Exception occurred while initializing locator table label!");
        }
        
        return c;
    }
   
    
}
