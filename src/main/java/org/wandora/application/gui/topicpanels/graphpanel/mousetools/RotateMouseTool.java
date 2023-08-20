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
 * RotateMouseTool.java
 *
 * Created on 12.7.2007, 12:02
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.mousetools;

import java.awt.Cursor;
import java.util.Set;

import org.wandora.application.gui.topicpanels.graphpanel.MouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.application.gui.topicpanels.graphpanel.VModel;
import org.wandora.application.gui.topicpanels.graphpanel.VNode;
import org.wandora.application.gui.topicpanels.graphpanel.projections.Projection;
import org.wandora.utils.Tuples.T2;


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
        if(draggingView) {
            panel.releaseMouseTool();
            draggingView=false;
            return true;
        }
        else return false;
    }
    
    
    @Override
    public boolean mousePressed(TopicMapGraphPanel panel, int mousex,int mousey) {
       if(panel.lockMouseTool(this)) {
            draggingView=true;
            T2<Double,Double> center = getRotationCenter(panel);
            T2<Double,Double> m = panel.getMouseWorldCoordinates();
            dragAngle=Math.atan2(m.e2-center.e2, m.e1-center.e1);
            return true;
        }
        return false;
    }

    
    @Override
    public boolean mouseDragged(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(draggingView && panel != null) {
            VModel model = panel.getModel();
            if(model != null) {
                panel.setMouseFollowNode(null);
                T2<Double,Double> center = getRotationCenter(panel);
                T2<Double,Double> m = panel.getMouseWorldCoordinates();
                double angle=Math.atan2(m.e2-center.e2, m.e1-center.e1);
                double d=angle-dragAngle;

                for(VNode vnode : model.getNodes()) {
                    double dx=vnode.getX()-center.e1;
                    double dy=vnode.getY()-center.e2;
                    double l=Math.sqrt(dx*dx+dy*dy);
                    double a=Math.atan2(dy,dx);
                    vnode.setX(center.e1+l*Math.cos(a+d));
                    vnode.setY(center.e2+l*Math.sin(a+d));
                }
                dragAngle=angle;
            }
            return true;
        }
        return false;
    }

    
    @Override
    public Cursor getCursor(TopicMapGraphPanel panel, int mousex, int mousey){
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
    
    
    private T2<Double,Double> getRotationCenter(TopicMapGraphPanel panel) {
        double centerX = 0;
        double centerY = 0;

        VNode rootNode = panel.getRootNode();
        if(rootNode != null) {
            centerX = rootNode.getX();
            centerY = rootNode.getY();
        }
        else {
            Set<VNode> selectedNodes = panel.getSelectedNodes();
            if(selectedNodes != null && !selectedNodes.isEmpty()) {
                VNode selectedNode = selectedNodes.iterator().next();
                if(selectedNode != null) {
                    centerX = selectedNode.getX();
                    centerY = selectedNode.getY();
                }
            }
            else {
                Projection projection = panel.getProjection();
                if(projection != null) {
                    T2<Double,Double> leftUpper = projection.screenToWorld(0, 0);
                    T2<Double,Double> rightLower = projection.screenToWorld(panel.getWidth(), panel.getHeight());
                    centerX = leftUpper.e1 + ((rightLower.e1 - leftUpper.e1) / 2);
                    centerY = leftUpper.e2 + ((rightLower.e2 - leftUpper.e2) / 2);
                }
            }
        }
        return new T2(centerX, centerY);
    }
    
    
}
