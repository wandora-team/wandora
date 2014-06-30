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
 * DefaultGraphStyle.java
 *
 * Created on 12. heinäkuuta 2007, 11:40
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;


import java.awt.*;
import java.util.*;
import org.wandora.topicmap.*;


/**
 *
 * @author akivela
 */
public class DefaultGraphStyle implements GraphStyle {
    
    private static Color defaultNodeColor = Color.GRAY;
    private static Color defaultEdgeColor = Color.GRAY;
    
    private static Color selectedNodeColor = new Color(200,221,242);
    private static Color currentNodeColor = new Color(255,255,255);
    
    
    private static Stroke defaultNodeBorderStroke = new BasicStroke(1);
    private static Stroke defaultPinnedNodeBorderStroke = new BasicStroke(2);
    private static HashMap<Integer,Font> nodeFonts=new HashMap<Integer,Font>();
    private static HashMap<Integer,Font> edgeFonts=new HashMap<Integer,Font>();
    
    private static Stroke defaultEdgeLabelStroke = new BasicStroke(1);
    
    private static HashMap<Integer,Stroke> edgeStrokes=new HashMap<Integer,Stroke>();
    private static HashMap<Integer,Stroke> occurrenceEdgeStrokes=new HashMap<Integer,Stroke>();
    
    private static Color[] colors;
    static {
        int step=64;
        ArrayList<Color> cs=new ArrayList<Color>();
        int counter=0;
        for(int r=0;r<=256;r+=step){
            for(int g=0;g<=256;g+=step){
                for(int b=0;b<=256;b+=step){
                    double s=Math.sqrt(r*r+g*g+b*b);
                    if(s<300 || s>367) continue; // 0... 368
                    cs.add(new Color(r==256?255:r,g==256?255:g,b==256?255:b));
                }
            }
        }
        colors=cs.toArray(new Color[cs.size()]);
    }
    
    private HashMap<Topic,Color> topicColors = new HashMap<Topic,Color>();
    
    
    
    /** Creates a new instance of DefaultGraphStyle */
    public DefaultGraphStyle() {

    }
    
    
    
    // -------------------------------------------------------------------------
    /*
     *            
     public Color getNodeColor(){
        if(nodeColor==null){
            try{
                Collection<Topic> types=topic.getTypes();
                if(types.size()==0) nodeColor=Color.GRAY;
                else nodeColor=model.getTypeColorForTopic(types.iterator().next());
            }catch(TopicMapException tme){tme.printStackTrace();}
        }
        return nodeColor;
    }
     **/
    
    
    @Override
    public Color getNodeColor(VNode vn) {
        Color c = defaultNodeColor;
        if(vn.selected) c = this.selectedNodeColor;
        else {
            c = getNodeColor(vn.getNode());
        }
        if(vn.mouseOver) {
            c = c.brighter();
        }
        return c;
    }
    public Color getNodeColor(Node n) {
        if(n instanceof TopicNode) {
            return getNodeColor((TopicNode) n);
        }
        else if(n instanceof OccurrenceNode) {
            return getNodeColor((OccurrenceNode) n);
        }
        else {
            return defaultNodeColor;
        }
    }
    
    public Color getNodeColor(TopicNode n) {
        Topic t = n.getTopic();
        Color c=topicColors.get(t);
        if(c==null){
            try {
                c = getTypeTopicColor(t);
            }
            catch(TopicMapException tme) {
                tme.printStackTrace(); 
                return Color.GRAY;
            }
        }
        return c;
    }
    
    public Color getNodeColor(OccurrenceNode n) {
        try {
            Topic t = n.getScope();
            return getTopicColor(t);
        }
        catch(Exception tme) {
            tme.printStackTrace(); 
            
        }
        return Color.GRAY;
    }
    
    
    
    private Color getTypeTopicColor(Topic t) throws TopicMapException {
        Collection<Topic> types=t.getTypes();
        if(types.isEmpty()) return Color.GRAY;
        Topic type=types.iterator().next();
        return getTopicColor(type);
    }
    
    private Color getTopicColor(Topic t) throws TopicMapException {
        if(t==null) return Color.GRAY;
        int hash=Math.abs(t.hashCode());
        Color c=colors[hash%colors.length];
        topicColors.put(t, c);
        return c;
    }
    
    
    @Override
    public Color getNodeTextColor(VNode vn) {
        return getNodeTextColor(vn.getNode());
    }
    public Color getNodeTextColor(Node n) {
        return Color.BLACK;
    }
    
    
    @Override
    public NodeShape getNodeShape(VNode vn) {
        return getNodeShape(vn.getNode());
    }
    public NodeShape getNodeShape(Node n) {
        return NodeShape.rectangle;
    }
    public NodeShape getNodeShape(AssociationNode n) {
        return NodeShape.invisible;
    }
    
    @Override
    public double getNodeWidth(VNode vn) {
        double w = getNodeWidth(vn.getNode());
        if(vn.mouseOver) w *= 1.5;
        return w;
    }
    public double getNodeWidth(Node n) {
        return 80.0;
    }
    
    @Override
    public double getNodeHeight(VNode vn) {
        return getNodeHeight(vn.getNode());
    }
    public double getNodeHeight(Node n) {
        return 20.0;
    }
    
    @Override
    public Color getNodeBorderColor(VNode vn) {
        if(vn.isSelected()) return Color.BLACK;
        if(vn.isPinned()) return Color.BLACK; 
        return getNodeBorderColor(vn.getNode());
    }
    public Color getNodeBorderColor(Node n) {
        return Color.GRAY;
    }
    
    @Override
    public Stroke getNodeBorderStroke(VNode vn) {
        if(vn.isPinned()) return defaultPinnedNodeBorderStroke;
        return getNodeBorderStroke(vn.getNode());
    }
    public Stroke getNodeBorderStroke(Node n) {
        return defaultNodeBorderStroke;
    }
    
    @Override
    public int getNodeFontSize(VNode vn) {
        return getNodeFontSize(vn.getNode());
    }
    public int getNodeFontSize(Node n) {
        if(n instanceof OccurrenceNode) {
            return 10;
        }
        return 12;
    }
    
    @Override
    public Font getNodeFont(VNode vn, int forSize) {
        return getNodeFont(vn.getNode(), forSize);
    }
    public Font getNodeFont(Node n, int forSize) {
        Font f=nodeFonts.get(forSize);
        if(f==null){
            f=new Font(Font.SANS_SERIF,Font.PLAIN,forSize);
            nodeFonts.put(forSize,f);
        }
        return f;
    }
    
    
    
    
    
    @Override
    public Color getEdgeColor(VEdge ve) {
        return getEdgeColor(ve.getEdge());
    }
    public Color getEdgeColor(Edge e) {
        if(e instanceof AssociationEdge) {
            return getEdgeColor((AssociationEdge) e);
        }
        else if(e instanceof OccurrenceEdge) {
            return getEdgeColor((OccurrenceEdge) e);
        }
        else {
            return defaultEdgeColor;
        }
    }
    
    public Color getEdgeColor(OccurrenceEdge e) {
        Color c = defaultEdgeColor;
        try {
            Topic t = e.getType();
            c = getTopicColor(t);
            c = c.darker();
            c = c.darker();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return c;
    }
  
    public Color getEdgeColor(AssociationEdge e) {
        Color c = defaultEdgeColor;
        try {
            Association a = e.getAssociation();
            Topic t = a.getType();
            c = getTopicColor(t);
            c = c.darker();
            c = c.darker();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return c;
    }
    
    
    @Override
    public double getEdgeWidth(VEdge ve) {
        return getEdgeWidth(ve.getEdge());
    }
    public double getEdgeWidth(Edge e) {
        if(e instanceof OccurrenceEdge) {
            return 1.5;
        }
        return 2.0;
    }
    
    
    @Override
    public int getEdgeLabelFontSize(VEdge ve) {
        return getEdgeLabelFontSize(ve.getEdge());
    }
    public int getEdgeLabelFontSize(Edge n) {
        return 12;
    }
    
    @Override
    public Font getEdgeLabelFont(VEdge ve, int forSize) {
        return getEdgeLabelFont(ve.getEdge(), forSize);
    }
    public Font getEdgeLabelFont(Edge n, int forSize) {
        Font f=edgeFonts.get(forSize);
        if(f==null){
            f=new Font(Font.SANS_SERIF,Font.PLAIN,forSize);
            edgeFonts.put(forSize,f);
        }
        return f;
    }
    @Override
    public Color getEdgeLabelColor(VEdge ve) {
        return Color.BLACK;
    }
    @Override
    public Stroke getEdgeLabelStroke(VEdge ve) {
        return defaultEdgeLabelStroke;
    }
    
    @Override
    public Stroke getEdgeStroke(VEdge ve, int forWidth) {
        if(forWidth==0) forWidth=1;
        if(ve.mouseOver) forWidth*=2;
        Edge e = ve.getEdge();
        
        if(e instanceof OccurrenceEdge) {
            Stroke stroke = occurrenceEdgeStrokes.get(forWidth);
            if(stroke == null) {
                stroke=new BasicStroke(forWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 2f, new float[] { 2f,2f }, 0f );
                occurrenceEdgeStrokes.put(forWidth, stroke);
            }
            return stroke;
        }
        else {
            Stroke stroke=edgeStrokes.get(forWidth);
            if(stroke==null){
                stroke=new BasicStroke(forWidth);
                edgeStrokes.put(forWidth,stroke);
            }
            return stroke;
        }
    }
}
