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
 * TopicTableCellRenderer.java
 *
 * Created on 16. lokakuuta 2005, 22:05
 *
 */

package org.wandora.application.gui.table;




import org.wandora.application.gui.topicstringify.TopicToString;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.TopicGuiWrapper;





/**
 *
 * @author akivela
 */
public class TopicTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    
    
    private Wandora wandora = null;
    private Topic topic;
    private TopicTable topicTable;

    
    
    public TopicTableCellRenderer(TopicTable table) {
        topicTable = table;
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
            if(topic == null) {
                c.setForeground(Color.LIGHT_GRAY);
            }
            else if(topic.isRemoved()) {
                c.setForeground(Color.RED);
            }


            if(c instanceof JLabel) {
                JLabel label = (JLabel) c;
                try {
                    String topicName = TopicToString.toString(topic);
                    label.setText(topicName);

                    /*
                    if(TopicToString.isStringType(TopicToString.TOPIC_RENDERS_BASENAME_WITH_SL_ICON)) {
                        if(topic != null && topic.getSubjectLocator() != null) {
                            String iconUrl = topic.getSubjectLocator().toExternalForm();
                            label.setIcon(UIBox.getCachedIconThumbForLocator(iconUrl, 24, 24));
                        }
                        else {
                            label.setIcon(null);
                        }
                    }
                     */
                }
                catch(Exception tme){
                    tme.printStackTrace(); // TODO EXCEPTION;
                    label.setText("Exception retrieving name");
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        return c;
    }

    


    

}
