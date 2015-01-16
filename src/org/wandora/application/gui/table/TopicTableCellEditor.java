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
 * TopicTableCellEditor.java
 *
 * Created on 14. lokakuuta 2005, 14:50
 */

package org.wandora.application.gui.table;


import org.wandora.application.gui.topicstringify.TopicToString;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import org.wandora.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.utils.swing.*;
import org.wandora.application.gui.simple.SimpleLabel;


/**
 *
 * @author olli,akivela
 */
public class TopicTableCellEditor extends AbstractCellEditor implements TableCellEditor {        
    private Topic topic;
    private SimpleLabel label;
    private TopicTable table;
            
    
    public TopicTableCellEditor(TopicTable table) {
        this.table = table;
        label = new SimpleLabel();
        Font f = label.getFont();
        label.setFont(new Font(f.getName(),Font.PLAIN,f.getSize()));
    }

    public Object getCellEditorValue() {
        return TopicToString.toString(topic);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if(value instanceof TopicGuiWrapper) {
            topic=((TopicGuiWrapper) value).topic;
        }
        label.setText(TopicToString.toString(topic));
        return label;
    }


}

