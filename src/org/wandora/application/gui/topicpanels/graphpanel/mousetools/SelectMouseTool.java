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
 * SelectMouseTool.java
 *
 * Created on 25. kesäkuuta 2007, 10:18
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.mousetools;

import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.gui.topicpanels.graphpanel.projections.Projection;
import java.awt.*;
import java.util.*;
import static org.wandora.utils.Tuples.*;


/**
 *
 * @author olli
 */
public class SelectMouseTool extends MouseTool {
    
    private boolean drawingSelection=false;
    private ArrayList<T2<Double,Double>> selectPath;
    private boolean clearSelection;
    
    private boolean selectNodes;
    private boolean selectEdges;
    
    
    /** Creates a new instance of SelectMouseTool */
    public SelectMouseTool(boolean selectNodes, boolean selectEdges, boolean clearSelection) {
        this.clearSelection=clearSelection;
        this.selectNodes=selectNodes;
        this.selectEdges=selectEdges;
    }
    
    
    public SelectMouseTool(boolean clearSelection) {
        this(true,true,clearSelection);
    }
    
    
    public SelectMouseTool() {
        this(true,true,true);
    }
    
    
    @Override
    public boolean mouseReleased(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(drawingSelection){
            panel.releaseMouseTool();
            drawingSelection=false;
            VModel model=panel.getModel();
            if(selectPath.size()>10){
                if(clearSelection) model.deselectAll();
                panel.selectNodesWithPath(selectPath);
            }
            return true;
        }
        else return false;
    }
    
    
    @Override
    public boolean mousePressed(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(panel.lockMouseTool(this)){
            drawingSelection=true;
            T2<Double,Double> m=panel.getMouseWorldCoordinates();
            selectPath=new ArrayList<T2<Double,Double>>();
            selectPath.add(t2(m.e1,m.e2));
            return true;
        }
        return false;
    }

    
    @Override
    public boolean mouseDragged(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(drawingSelection){
            selectPath.add(panel.getMouseWorldCoordinates());
            return true;
        }
        else return false;
    }

    
    @Override
    public boolean mouseClicked(TopicMapGraphPanel panel, int mousex,int mousey) {
        VNode mouseOverNode=panel.getMouseOverNode();
        VEdge mouseOverEdge=panel.getMouseOverEdge();
        VModel model=panel.getModel();
        if(selectNodes && mouseOverNode!=null){
            if(clearSelection) panel.getModel().deselectAll();
            if(mouseOverNode.isSelected()) model.deselectNode(mouseOverNode);
            else model.addSelection(mouseOverNode);
        }
        else if(selectEdges && mouseOverEdge!=null){
            if(clearSelection) panel.getModel().deselectAll();
            if(mouseOverEdge.isSelected()) model.deselectEdge(mouseOverEdge);
            else model.addSelection(mouseOverEdge);            
        }
        else{
            model.deselectAll();
        }
        return true;
    }

    
    @Override
    public void paint(Graphics2D g2,TopicMapGraphPanel panel){
        if(drawingSelection){
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1));
            Projection projection=panel.getProjection();
            for(int i=0;i<selectPath.size()-1;i++){
                T2<Double,Double> p1=selectPath.get(i);
                T2<Double,Double> p2=selectPath.get(i+1);
                p1=projection.worldToScreen(p1.e1,p1.e2);
                p2=projection.worldToScreen(p2.e1,p2.e2);
                g2.drawLine((int)p1.e1.doubleValue(),(int)p1.e2.doubleValue(),
                        (int)p2.e1.doubleValue(),(int)p2.e2.doubleValue());
            }
            
        }
    }
    
    
    @Override
    public Cursor getCursor(TopicMapGraphPanel panel, int mousex, int mousey){
        VNode mouseOverNode=panel.getMouseOverNode();
        if(mouseOverNode==null) return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        else return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    
}
