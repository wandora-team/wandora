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
 * MoveViewMouseTool.java
 *
 * Created on 25.6.2007, 10:41
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;



import java.awt.*;
import static org.wandora.utils.Tuples.*;


/**
 *
 * @author olli
 */
public class MoveViewMouseTool extends MouseTool {
    
    private boolean draggingView=false;
    private double dragOffsX,dragOffsY,dragOffsX2,dragOffsY2;
    /** Creates a new instance of MoveViewMouseTool */
    public MoveViewMouseTool() {
    }
 
    public boolean mouseReleased(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(draggingView){
            panel.releaseMouseTool();
            draggingView=false;
            return true;
        }
        else return false;
    }
    
    public boolean mousePressed(TopicMapGraphPanel panel, int mousex,int mousey) {
       if(panel.lockMouseTool(this)){
            draggingView=true;
            T2<Double,Double> m=panel.getMouseWorldCoordinates();
            T2<Double,Double> v=panel.getViewCoordinates();
            dragOffsX=v.e1;
            dragOffsY=v.e2;
            dragOffsX2=m.e1;
            dragOffsY2=m.e2;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(draggingView){
            panel.setMouseFollowNode(null);
            T2<Double,Double> m=panel.getMouseWorldCoordinates();
            double viewX=dragOffsX+(dragOffsX2-m.e1);
            double viewY=dragOffsY+(dragOffsY2-m.e2);
            panel.setViewCoordinates(viewX,viewY);

            panel.updateMouseWorldCoordinates(mousex,mousey);
            m=panel.getMouseWorldCoordinates();
            dragOffsX=viewX;
            dragOffsY=viewY;
            dragOffsX2=m.e1;
            dragOffsY2=m.e2;                        
            return true;
        }
        return false;
    }
    public Cursor getCursor(TopicMapGraphPanel panel, int mousex, int mousey){
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
    
}
