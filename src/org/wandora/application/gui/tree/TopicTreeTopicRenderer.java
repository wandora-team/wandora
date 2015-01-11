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
 * TopicTreeTopicRenderer.java
 *
 * Created on 27. joulukuuta 2005, 23:00
 *
 */

package org.wandora.application.gui.tree;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import org.wandora.application.Wandora;
import org.wandora.utils.GripCollections;
import org.wandora.application.gui.TopicGuiWrapper;




/**
 * @author akivela
 */
public class TopicTreeTopicRenderer extends DefaultTreeCellRenderer {

    private TopicTree topicTree;

    
    
    public Map<String,Icon> icons=GripCollections.addArrayToMap(new HashMap<String,Icon>(),new Object[]{
                                        });
    
    
    public TopicTreeTopicRenderer(TopicTree topicTree) {
        this.topicTree = topicTree;
    }

    
    
    
    
    
    
    @Override
    public java.awt.Component getTreeCellRendererComponent (
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        Component c = super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        
        try {
            if(topicTree.isBroken()) return c;
            String res=((TopicGuiWrapper)value).icon;
            setIcon(solveIcon(res));

            if(c instanceof JLabel) {
                JLabel label = (JLabel) c;
                TopicGuiWrapper topicWrapper = (TopicGuiWrapper) value;
                try {
                    String topicName = topicWrapper.toString();
                    label.setText(topicName);
                    Wandora wandora = Wandora.getWandora();
                    Color color = null;
                    if(wandora != null) wandora.topicHilights.getLayerColor(topicWrapper.topic);
                    if(color!=null) label.setForeground(color);
                    else label.setForeground(Color.BLACK);
                }
                catch(Exception ex) {}
            }
        }
        catch(Exception e) { e.printStackTrace(); }
        
        return c;
    }
    
    
    
    public Icon solveIcon(String res) {
        Icon icon=icons.get( res );
        if(res!=null && icon==null){
            try {
                URL url=this.getClass().getClassLoader().getResource(res);
                icon=new ImageIcon(url);
                icons.put(url.toExternalForm(),icon);
            }
            catch(Exception e){
                e.printStackTrace();
                icon=null;
            }
        }
        return icon;
    }
    
}    
    
