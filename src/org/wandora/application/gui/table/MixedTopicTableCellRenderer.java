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
 * MixedTopicTableCellRenderer.java
 *
 *
 */

package org.wandora.application.gui.table;




import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.MixedTopicGuiWrapper;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;





/**
 *
 * @author akivela
 */
public class MixedTopicTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
    public Object content;
    private Topic topic;
    private MixedTopicTable topicTable;
    private int topicRenders;
    private static MixedTopicGuiWrapper defaultWrapper = null;


    public MixedTopicTableCellRenderer(MixedTopicTable table) {
        topicTable = table;
        topicRenders = Wandora.getWandora().options.getInt(MixedTopicGuiWrapper.TOPIC_RENDERS_OPTION_KEY);
        defaultWrapper = new MixedTopicGuiWrapper(null);
    }


    public Object getTableCellRendererValue() {
        if(content == null) return "--";
        else if(topic!=null){
            try {
                return topic.getBaseName();
            }
            catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION;
                return "Exception retrieving name";
            }
        }
        else return content.toString();
    }

    
    
    

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
            MixedTopicGuiWrapper wrapper = null;
            if(value != null && value instanceof MixedTopicGuiWrapper) {
                wrapper = (MixedTopicGuiWrapper) value;
            }
            else {
                defaultWrapper.content = value;
                wrapper = defaultWrapper;
            }
            
            content = wrapper.getContent();
            topic = null;
            if(content instanceof Topic) {
                topic = (Topic) content;
            }

            if(!isSelected && !hasFocus) {
                Color hilight=null;
                Color bg=null;
                if(content==null){
                    bg = new Color(0xffcece);
                }
                else if(topic==null){
                    bg = new Color(0xfff1ce);
                }
                else{
                    hilight = Wandora.getWandora().topicHilights.get(topic);
                }
                if(hilight != null) c.setForeground(hilight);
                else {
                    Color layerColor = Wandora.getWandora().topicHilights.getLayerColor(topic);
                    if(layerColor != null) c.setForeground(layerColor);
                    else c.setForeground(Color.BLACK);
                }
                c.setBackground(bg); // note null is valid value 
            }


            if(c instanceof JLabel) {
                JLabel label = (JLabel) c;
                try {
                    String topicName = wrapper.toString(topicRenders);
                    label.setText(topicName);
                    label.setBorder(UIConstants.defaultTableCellLabelBorder);

                    if(MixedTopicGuiWrapper.TOPIC_RENDERS_BASENAME_WITH_SL_ICON == topicRenders) {
                        if(topic!=null && topic.getSubjectLocator() != null) {
                            String iconUrl = topic.getSubjectLocator().toExternalForm();
                            label.setIcon(UIBox.getCachedIconThumbForLocator(iconUrl, 24, 24));
                        }
                        else {
                            label.setIcon(null);
                        }
                    }
                }
                catch(TopicMapException tme){
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
