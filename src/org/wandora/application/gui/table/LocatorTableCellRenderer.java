/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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




import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import org.wandora.application.gui.simple.SimpleURILabel;
import org.wandora.topicmap.Locator;




/**
 *
 * @author akivela
 */
public class LocatorTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    private SimpleURILabel uriLabel = new SimpleURILabel();
    private LocatorTable locatorTable;

    
    public LocatorTableCellRenderer(LocatorTable table) {
        this.locatorTable = table;
        uriLabel.setEnabled(true);
        uriLabel.setOpaque(true);
    }

    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
            uriLabel.setBackground(c.getBackground());
                        
            Color foregroundColor = locatorTable.getColorFor(row, column);
            if(foregroundColor != null) uriLabel.setForeground(foregroundColor);
            else uriLabel.setForeground(c.getForeground());
            
            Locator l = (Locator) value;
            String locatorString = l.toExternalForm();
            uriLabel.setText(locatorString);
        }
        catch(Exception e){
            e.printStackTrace(); // TODO EXCEPTION;
            // uriLabel.setText("*** Exception occurred while initializing locator table label!");
        }
        
        return uriLabel;
    }
   

}
