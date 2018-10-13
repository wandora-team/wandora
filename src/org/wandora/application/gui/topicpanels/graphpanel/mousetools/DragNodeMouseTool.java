/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * DragNodeMouseTool.java
 *
 * Created on 25. kes�kuuta 2007, 12:12
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.mousetools;


import org.wandora.application.gui.topicpanels.graphpanel.*;
import java.awt.*;
import static org.wandora.utils.Tuples.*;

/**
 *
 * @author olli
 */
public class DragNodeMouseTool extends MouseTool {
    
    private boolean pinned=false;
    private VNode draggingNode=null;
    private double dragOffsX,
				    dragOffsY,
				    dragOffsX2,
				    dragOffsY2;
    
    
    /** Creates a new instance of DragNodeMouseTool */
    public DragNodeMouseTool() {
    }
 
    
    @Override
    public boolean mouseReleased(TopicMapGraphPanel panel, int mousex, int mousey) {
        if(draggingNode!=null) {
            draggingNode.setPinned(pinned);
            panel.releaseMouseTool();
            draggingNode=null;
            return true;
        }
        else return false;
    }
    
    
    @Override
    public boolean mousePressed(TopicMapGraphPanel panel, int mousex, int mousey) {
        VNode mouseOverNode=panel.getMouseOverNode();
        if(mouseOverNode!=null && panel.lockMouseTool(this)) {
            T2<Double,Double> m=panel.getMouseWorldCoordinates();
            draggingNode=mouseOverNode;
            pinned=mouseOverNode.isPinned();
            mouseOverNode.setPinned(true);
            dragOffsX=mouseOverNode.getX()-m.e1;
            dragOffsY=mouseOverNode.getY()-m.e2;
            return true;
        }
        return false;
    }

    
    @Override
    public boolean mouseDragged(TopicMapGraphPanel panel, int mousex, int mousey) {
        if(draggingNode!=null) {
            panel.setMouseFollowNode(null);
            T2<Double,Double> m=panel.getMouseWorldCoordinates();
            draggingNode.setX(m.e1);
            draggingNode.setY(m.e2);
            return true;
        }
        return false;
    }
    
    
    @Override
    public Cursor getCursor(TopicMapGraphPanel panel, int mousex, int mousey){
        VNode mouseOverNode=panel.getMouseOverNode();
        if(mouseOverNode!=null) return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        else return null;
    }
    
}
