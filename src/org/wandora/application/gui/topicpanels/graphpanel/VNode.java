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
 * VNode.java
 *
 * Created on 4. kesäkuuta 2007, 14:01
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;




import org.wandora.application.gui.topicpanels.graphpanel.projections.Projection;
import java.awt.*;
import static org.wandora.utils.Tuples.*;
import java.util.*;



/**
 *
 * @author olli
 */
public class VNode {
    
    private int id;
    
    private Node node;
    private VModel model;
    
    double x,y;
    double newx,newy;
    
    boolean mouseOver=false;
    
    boolean pinned=false;
    
    boolean selected=false;
    
    int cluster=0;
    
    int edgeCount=0;
    
    private HashSet<VEdge> edges;
    
    private GraphStyle style = null;
    
    
    
    /** Creates a new instance of VNode */
    public VNode(Node node,VModel model,int id) {
        this.id = id;
        this.node = node;
        this.model = model;
        this.edges = new HashSet<VEdge>();
        this.style = model.getGraphStyle();
    }
 
    public int getID(){
        return id;
    }
    
    public VModel getModel(){
        return model;
    }
    
    public Node getNode() {
        return node;
    }
    
    public TopicMapGraphPanel getPanel(){
        return model.getPanel();
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    void addEdge(VEdge edge){
        edges.add(edge);
        edgeCount=edges.size();
    }
    void removeEdge(VEdge edge){
        edges.remove(edge);
        edgeCount=edges.size();
    }
    
    public boolean isPinned() {
        return pinned;
    }
    public void setPinned(boolean b) {
        pinned=b;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public Collection<VEdge> getEdges(){
        return edges;
    }
    
    private boolean getCropNodeBoxes() {
        if(model != null) {
            TopicMapGraphPanel p = model.getPanel();
            if(p != null) {
                return p.getCropNodeBoxes();
            }
        }
        return true;
    }
    

    
    public void draw(Graphics2D g2, Projection proj){
        if(getShape()==NodeShape.invisible && !mouseOver) return;
        T2<Double,Double> pos=proj.worldToScreen(x,y);
        if(pos.e1 == Double.NaN || pos.e2 == Double.NaN) return;

        if(node instanceof AssociationNode) {
            int posX=(int)pos.e1.doubleValue();
            int posY=(int)pos.e2.doubleValue();
            double scale=proj.scale(x,y);
            int w=(int) (12*scale);
            int h=(int) (12*scale);
            if(w>0 && h>0){
                Color c=getColor();
                g2.setColor(c);
                g2.fillOval(posX-w/2,posY-h/2,w,h);

                if(h>2 && w>2 && getBorderColor()!=null){
                    g2.setColor(getBorderColor());
                    g2.setStroke(getBorderStroke());
                    g2.drawOval(posX-w/2,posY-h/2,w,h);
                }
            }
        }
        else {
            int posX=(int)pos.e1.doubleValue();
            int posY=(int)pos.e2.doubleValue();
            double scale=proj.scale(x,y);
            int w=(int)(getWidth()*scale+0.5);
            int h=(int)(getHeight()*scale+0.5);

            int labelWidth=-1;
            String label=node.getLabel();

            int fontSize=(int)(getFontSize()*scale);
            if(label!=null){
                if(fontSize<12 && mouseOver) fontSize=12;

                if(fontSize>2){
                    Font f=getFont(fontSize);
                    g2.setFont(f);
                    labelWidth=g2.getFontMetrics().stringWidth(label);
                    if((labelWidth>w && mouseOver) || !getCropNodeBoxes()) w=labelWidth;
                    if((int)(fontSize*1.5)>h && mouseOver) h=(int)(fontSize*1.5);
                }
            }

            if(w>0 && h>0){
                Color c=getColor();
                g2.setColor(c);
                g2.fillRect(posX-w/2-4,posY-h/2,w+8,h);

                if(h>2 && w>2 && getBorderColor()!=null){
                    g2.setColor(getBorderColor());
                    g2.setStroke(getBorderStroke());
                    g2.drawRect(posX-w/2-4,posY-h/2,w+8,h);
                }
            }

            if(label!=null && fontSize>2) {
                if(labelWidth>w){
                    Shape oldClip=g2.getClip();
                    Rectangle oldClipRect = oldClip.getBounds();
                    int newClipX = Math.max( oldClipRect.x, posX-w/2+1);
                    int newClipY = Math.max( oldClipRect.y, posY-h/2+1);
                    int newClipWidth = Math.min(oldClipRect.width, posX+w/2+1) - newClipX;
                    int newClipHeight = Math.min(oldClipRect.height, posY+h/2+1) - newClipY;
                    g2.setClip(newClipX-4, newClipY, newClipWidth+8, newClipHeight);
                    g2.setColor(getTextColor());
                    g2.drawString(label,posX-w/2+1,posY+g2.getFont().getSize()/2);
                    g2.setClip(oldClip);
                }
                else{
                    g2.setColor(getTextColor());
                    g2.drawString(label,posX-labelWidth/2,posY+g2.getFont().getSize()/2);
                }
            }
        }
    }
    
    public boolean pointInside(double px,double py){
        double ix=x-getWidth()/2;
        double iy=y-getHeight()/2;
        double ax=ix+getWidth();
        double ay=iy+getHeight();
        return (ix<px && px<ax && iy<py && py<ay);        
    }
    
    
    
    // --------------------------------------------------------- GRAPH STYLE ---
    
    public void setGraphStyle(GraphStyle newStyle) {
        if(newStyle == null) {
            this.style = model.getGraphStyle();
        }
        else {
            this.style = newStyle;
        }
    }
    
    
    public Color getColor() {
        return style.getNodeColor(this);
    }
    public Color getTextColor() {
        return style.getNodeTextColor(this);
    }
    public Color getBorderColor() {
        return style.getNodeBorderColor(this);
    }
    public Stroke getBorderStroke() {
        return style.getNodeBorderStroke(this);
    }
    public double getWidth() {
        return style.getNodeWidth(this);
    }
    public double getHeight() {
        return style.getNodeHeight(this);
    }
    public NodeShape getShape() {
        return style.getNodeShape(this);
    }
    public int getFontSize() {
        return style.getNodeFontSize(this);
    }
    public Font getFont(int forSize) {
        return style.getNodeFont(this, forSize);
    }
    
}
