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
 * LocatorTableCellRenderer.java
 *
 * Created on 16. lokakuuta 2005, 22:05
 *
 */

package org.wandora.application.gui.table;




import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.wandora.application.gui.UIConstants;
import org.wandora.topicmap.Locator;
import org.wandora.utils.DataURL;




/**
 *
 * @author akivela
 */
public class LocatorTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    
    private LocatorTable locatorTable;

    
    
    public LocatorTableCellRenderer(LocatorTable table) {
        this.locatorTable = table;
    }

    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {          
            Color foregroundColor = locatorTable.getColorFor(row, column);
            if(foregroundColor != null) {
                c.setForeground(foregroundColor);
            }
            
            if(c instanceof JLabel) {
                JLabel label = (JLabel) c;
                label.setBorder(UIConstants.defaultTableCellLabelBorder);
                Locator l = (Locator) value;
                String locatorString = l.toExternalForm();
                if(DataURL.isDataURL(locatorString)) {
                    String locatorFragment = locatorString.substring(0, Math.min(locatorString.length(), 64)) + "... ("+locatorString.length()+")";
                    label.setText(locatorFragment);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION
        }
        return c;
    }
}
