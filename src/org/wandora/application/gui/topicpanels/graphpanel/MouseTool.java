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
 * MouseTool.java
 *
 * Created on 25. kesäkuuta 2007, 9:34
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;



import java.awt.*;


/**
 *
 * @author olli
 */


public abstract class MouseTool {
    
    public boolean mouseMoved(TopicMapGraphPanel panel, int mousex,int mousey){
        return false;
    }
    public boolean mouseClicked(TopicMapGraphPanel panel, int mousex,int mousey){
        return false;
    }
    public boolean mouseDragged(TopicMapGraphPanel panel, int mousex,int mousey){
        return false;
    }
    public boolean mouseReleased(TopicMapGraphPanel panel, int mousex,int mousey){
        return false;
    }
    public boolean mousePressed(TopicMapGraphPanel panel, int mousex,int mousey){
        return false;
    }
    
    public void paint(Graphics2D g2,TopicMapGraphPanel panel){
    }
    
    public Cursor getCursor(TopicMapGraphPanel panel,int mousex, int mousey){
        return null;
    }
}
