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
 * MergeMatrixCellRenderer.java
 *
 */

package org.wandora.application.tools.statistics;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author
 * Eero
 */


public class MergeMatrixCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer{
    
	private static final long serialVersionUID = 1L;

	public MergeMatrixCellRenderer() {
        
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
            Color diagonalColor = new Color(230, 230, 230);
            Color layerColor = new Color(192,192,192);
            Color defaultColor = new Color(255,255,255);
            if(row == 0 || column == 0){
                this.setBackground(layerColor);
            }
            else if(row == column){
                this.setBackground(diagonalColor);
            } else {
                this.setBackground(defaultColor); 
            }
            
            this.setValue(value);
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION;
            // uriLabel.setText("*** Exception occurred while initializing locator table label!");
        }
        
        return this;
    }
}
