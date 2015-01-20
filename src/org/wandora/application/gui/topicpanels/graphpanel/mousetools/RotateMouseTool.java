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
 * RotateMouseTool.java
 *
 * Created on 12. heinäkuuta 2007, 12:02
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.mousetools;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import javax.swing.*;
import java.awt.*;
import static org.wandora.utils.Tuples.*;

/**
 *
 * @author olli
 */
public class RotateMouseTool extends MouseTool {
    private boolean draggingView=false;
    private double dragAngle;

    public RotateMouseTool() {
    }
 
    @Override
    public boolean mouseReleased(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(draggingView){
            panel.releaseMouseTool();
            draggingView=false;
            return true;
        }
        else return false;
    }
    
    @Override
    public boolean mousePressed(TopicMapGraphPanel panel, int mousex,int mousey) {
       if(panel.lockMouseTool(this)){
            draggingView=true;
            VNode center=panel.getModel().getNode(panel.getTopicMapModel().getNodeFor(panel.getRootTopic()));
            T2<Double,Double> m=panel.getMouseWorldCoordinates();
            dragAngle=Math.atan2(m.e2-center.getY(),m.e1-center.getX());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(draggingView){
            panel.setMouseFollowNode(null);
            VNode center=panel.getModel().getNode(panel.getTopicMapModel().getNodeFor(panel.getRootTopic()));
            T2<Double,Double> m=panel.getMouseWorldCoordinates();
            double angle=Math.atan2(m.e2-center.getY(),m.e1-center.getX());
            double d=angle-dragAngle;
            
            for(VNode vnode : panel.getModel().getNodes()){
                double dx=vnode.getX()-center.getX();
                double dy=vnode.getY()-center.getY();
                double l=Math.sqrt(dx*dx+dy*dy);
                double a=Math.atan2(dy,dx);
                vnode.setX(center.getX()+l*Math.cos(a+d));
                vnode.setY(center.getY()+l*Math.sin(a+d));
            }
            
            dragAngle=angle;
            return true;
        }
        return false;
    }

    
    @Override
    public Cursor getCursor(TopicMapGraphPanel panel, int mousex, int mousey){
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
    
}
