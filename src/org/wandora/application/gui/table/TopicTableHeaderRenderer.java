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
 * TopicTableHeaderRenderer.java
 *
 * Created on 1. elokuuta 2006, 20:48
 *
 */

package org.wandora.application.gui.table;


import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import org.wandora.topicmap.*;


/**
 *
 * @author akivela
 */
public class TopicTableHeaderRenderer extends DefaultTableCellRenderer implements TableCellRenderer {


    private Color[] columnColors = null;
    private TableCellRenderer oldRenderer = null;
       
    
    
    /** Creates a new instance of TopicTableHeaderRenderer */
    public TopicTableHeaderRenderer(TableCellRenderer renderer, Color[] columnColors) {
        this.columnColors = columnColors;
        this.oldRenderer = renderer;
    }
    

    @Override
    public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus, int row, int column){
        Component c = oldRenderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        
        column=table.convertColumnIndexToModel(column);
        if(columnColors != null && columnColors[column]!=null) {
            c.setForeground(columnColors[column]);
        }
        return c;
    }

    
}
