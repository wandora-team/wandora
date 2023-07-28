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
 * DrawAssociationMouseTool.java
 *
 * Created on 6.7.2007, 12:38
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.mousetools;

import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.gui.topicpanels.graphpanel.projections.Projection;
import java.awt.*;
import org.wandora.topicmap.*;
import java.util.*;
import static org.wandora.utils.Tuples.*;
import org.wandora.application.gui.*;


/**
 *
 * @author olli
 */
public class DrawAssociationMouseTool extends MouseTool {
    private VNode startNode=null;
    private VNode endNode=null;
    
    
    /** Creates a new instance of DrawAssociationMouseTool */
    public DrawAssociationMouseTool() {
    }
    
    
    @Override
    public boolean mouseReleased(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(startNode!=null){
            panel.releaseMouseTool();
            if(endNode!=null){
                Node sn=startNode.getNode();
                Node en=endNode.getNode();
                if(sn instanceof TopicNode && en instanceof TopicNode){
                    Vector<Topic> v=new Vector<Topic>();
                    v.add(((TopicNode)sn).getTopic());
                    v.add(((TopicNode)en).getTopic());
                    try{
                        FreeAssociationPrompt d=new FreeAssociationPrompt(panel.getWandora(),v);
                        d.prefill(false);
                        d.setVisible(true);
                        Association a=d.getCreatedAssociation();
                        if(a!=null) {
                            TopicMapModel tmModel=panel.getTopicMapModel();
                            VModel vModel=panel.getModel();
                            Edge e=tmModel.getEdgeFor(a);
                            if(e!=null) vModel.addEdge(e);
                            else {
                                Node n=tmModel.getNodeFor(a);
                                if(n!=null){
                                    VNode vn=vModel.addNode(n);
                                    vModel.openNode(vn);
                                }
                            }
                        }
                    }
                    catch(TopicMapException tme){
                        tme.printStackTrace();
                    }
                }
            }
            startNode=null;
            endNode=null;
            return true;
        }
        else return false;
    }
    
    
    @Override
    public boolean mousePressed(TopicMapGraphPanel panel, int mousex,int mousey) {
        VNode mouseOverNode=panel.getMouseOverNode();
        if(mouseOverNode!=null && panel.lockMouseTool(this)){
            startNode=mouseOverNode;
            endNode=null;
            return true;
        }
        return false;
    }

    
    @Override
    public boolean mouseDragged(TopicMapGraphPanel panel, int mousex,int mousey) {
        if(startNode!=null){
            VNode mouseOverNode=panel.getMouseOverNode();
            if(mouseOverNode!=null && mouseOverNode!=startNode) endNode=mouseOverNode;
            else endNode=null;
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
    
    
    @Override
    public void paint(Graphics2D g2,TopicMapGraphPanel panel){
        if(startNode!=null){
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(2));
            Projection proj=panel.getProjection();
            T2<Double,Double> sp=proj.worldToScreen(startNode.getX(),startNode.getY());
            if(endNode!=null){
                T2<Double,Double> ep=proj.worldToScreen(endNode.getX(),endNode.getY());
                g2.drawLine((int)sp.e1.doubleValue(),(int)sp.e2.doubleValue(),
                        (int)ep.e1.doubleValue(),(int)ep.e2.doubleValue());
            }
            else{
                T2<Double,Double> m=panel.getMouseWorldCoordinates();
                T2<Double,Double> ep=proj.worldToScreen(m.e1,m.e2);
                g2.drawLine((int)sp.e1.doubleValue(),(int)sp.e2.doubleValue(),
                        (int)ep.e1.doubleValue(),(int)ep.e2.doubleValue());                
            }
        }
    }
    
}
