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
 * TopicTableCellRenderer.java
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

import org.wandora.application.Wandora;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.Topic;





/**
 *
 * @author akivela
 */
public class TopicGridCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    
    private Wandora wandora = null;
    private Topic topic;
    private TopicGrid topicGrid;

    
    public TopicGridCellRenderer(TopicGrid grid) {
        topicGrid = grid;
        wandora = Wandora.getWandora();
    }
    
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        try {
            if(value == null || value instanceof Topic) {
                topic = (Topic) value;
            }
            else if(value instanceof TopicGuiWrapper) {
                TopicGuiWrapper topicWrapper = (TopicGuiWrapper) value;
                topic = topicWrapper.topic;
            }

            if(!isSelected && !hasFocus) {
                Color hilight = wandora.topicHilights.get(topic);
                if(hilight != null) c.setForeground(hilight);
                else {
                    Color layerColor = wandora.topicHilights.getLayerColor(topic);
                    if(layerColor != null) c.setForeground(layerColor);
                    else c.setForeground(Color.BLACK);
                }
            }
            else if(topic != null && topic.isRemoved()) {
                c.setForeground(Color.RED);
            }

            if(c instanceof JLabel) {
                JLabel label = (JLabel) c;
                label.setBorder(UIConstants.defaultTableCellLabelBorder);
                if(topic != null) {
                    String topicName = TopicToString.toString(topic);
                    label.setText(topicName);
                }
                else {
                    label.setText("");
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        return c;
    }

    


    

}
