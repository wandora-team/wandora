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
 * VEdge.java
 *
 * Created on 4.6.2007, 14:02
 */

package org.wandora.application.gui.topicpanels.graphpanel;



import org.wandora.application.gui.topicpanels.graphpanel.projections.Projection;
import static org.wandora.utils.Tuples.*;
import java.awt.*;


/**
 *
 * @author olli
 */
public class VEdge {
    
    private Edge edge;
    private VModel model;
    private T2<VNode,VNode> vnodes;
    private boolean swapped;
        
    boolean mouseOver = false;
    boolean labelEdges = true;
    boolean selected = false;
    
    double curvature = 0.0;
    
    private GraphStyle style = null;
    
    
    
    /** Creates a new instance of VEdge */
    public VEdge(Edge edge, VModel model) {
        this.edge=edge;
        this.model=model;
        T2<Node,Node> nodes = edge.getNodes();
        vnodes=t2(model.getNode(nodes.e1),model.getNode(nodes.e2));
        swapped=false;
        if(vnodes.e1.getID()>vnodes.e2.getID()) {
            vnodes=t2(vnodes.e2,vnodes.e1);
            swapped=true;
        }
        style = model.getGraphStyle();
    }
    
    
    public boolean isSelected(){
        return selected;
    }
    
    public T2<VNode,VNode> getNodes(){
        return vnodes;
    }
    
    public VModel getModel(){
        return model;
    }
    
    public TopicMapGraphPanel getPanel(){
        return model.getPanel();
    }
    
    public Edge getEdge(){
        return edge;
    }
    
    
    private void drawLabelRect(Graphics2D g2,Projection proj,String label,int posX,int posY,Color c) {
        drawLabelRect(g2, proj, label, posX, posY, c, false);
    }
    
    
    private void drawLabelRect(Graphics2D g2,Projection proj,String label,int posX,int posY,Color c, boolean scale){
        if(label!=null){
            g2.setColor(c);
            int labelWidth = 100;
            int labelHeight = 18;
            if(scale) {
                double s=proj.scale(posX,posY);
                g2.setFont(getLabelFont((int) (12*s)));
                labelWidth=g2.getFontMetrics().stringWidth(label);
                labelHeight=(int) (16*s);
            }
            else { 
                g2.setFont(getLabelFont(12));
                labelWidth=g2.getFontMetrics().stringWidth(label);
                labelHeight=16;
            }

            g2.fillRect(posX-labelWidth/2-3,posY-labelHeight/2,labelWidth+6,labelHeight);
            g2.setStroke(getLabelBorderStroke());
            g2.setColor(getLabelColor());
            g2.drawString(label,posX-labelWidth/2,posY+g2.getFont().getSize()/2-1);
            // g2.drawRect(posX-labelWidth/2,posY-labelHeight/2,labelWidth,labelHeight);
        }
    }
    
    

    private void drawDetailLabel(Graphics2D g2,Projection proj,String label,int posX,int posY,Color c){
        if(label!=null){
            double s=proj.scale(posX,posY);
            int fsize = Math.min(12, (int) (12*s));
            if(fsize > 2) {
                g2.setFont(getLabelFont(fsize));
                int labelWidth=g2.getFontMetrics().stringWidth(label);
                g2.setColor(c);
                g2.drawString(label,posX-labelWidth/2,posY+g2.getFont().getSize()/2);
            }
        }
    }
    
    
    public void draw(Graphics2D g2, Projection proj){
        
        Color c=getColor();
        if(selected){
            c=new Color(0,204,255);
        }
        g2.setColor(c);
        
        T2<Double,Double> p1=proj.worldToScreen(vnodes.e1.x,vnodes.e1.y);
        T2<Double,Double> p2=proj.worldToScreen(vnodes.e2.x,vnodes.e2.y);
        if(p1.e1 == Double.NaN || p1.e2 == Double.NaN) return;
        if(p2.e1 == Double.NaN || p2.e2 == Double.NaN) return;
        int width=(int)(getWidth()*proj.scale(vnodes.e1.x,vnodes.e1.y)+0.5);
        Stroke stroke=getStroke(width);
        g2.setStroke(stroke);
/*        if(curvature==0.0){
            g2.drawLine((int)p1.e1.doubleValue(),(int)p1.e2.doubleValue(),
                    (int)p2.e1.doubleValue(),(int)p2.e2.doubleValue());
        }
        else*/ {
            double sdx=p2.e1-p1.e1; // delta from p1 to p2, screen coordinates
            double sdy=p2.e2-p1.e2;
            double sd2=sdx*sdx+sdy*sdy;
            if(sd2<=400.0){
                g2.drawLine((int)p1.e1.doubleValue(),(int)p1.e2.doubleValue(),
                        (int)p2.e1.doubleValue(),(int)p2.e2.doubleValue());                
            }
            else{
                double sd=Math.sqrt(sd2); // delta length, screen coordinates
                int segments=(int)Math.ceil(sd/10.0); // number of segments

                double dx=vnodes.e2.x-vnodes.e1.x; // delta from node 1 to node 2, world coordinates
                double dy=vnodes.e2.y-vnodes.e1.y;
                double d=Math.sqrt(dx*dx+dy*dy); // delta length, world coordinates
                double ndx=dx/d; // normalized delta, world coordinates
                double ndy=dy/d;
                dx/=segments; // delta for each segment on a straight line
                dy/=segments;

//                T2<Double,Double> spp=p1; // previous point on a curved line, screen coordinates
                T2<Double,Double> snp; // next point on a curved line, screen coordinates
                double nx2,ny2; // next point on a curved line, world coordinates
                double nx=vnodes.e1.x+dx; // next point on a straight line
                double ny=vnodes.e1.y+dy;
                double dr=Math.PI/segments; // angle delta
                double nr=dr; // angle for next point
                double f=0.0; // curve function value, Math.sin(cr)

                int[] x=new int[segments+1]; // points for polyline
                int[] y=new int[segments+1];
                x[0]=(int)p1.e1.doubleValue();
                y[0]=(int)p1.e2.doubleValue();

                for(int i=0;i<segments;i++){
                    if(curvature!=0.0) f=Math.sin(nr)*curvature; 
                    nx2=nx+f*ndy;
                    ny2=ny-f*ndx;

                    snp=proj.worldToScreen(nx2,ny2);
                    if(snp.e1 == Double.NaN || snp.e2 == Double.NaN) return;
//                    g2.drawLine((int)spp.e1.doubleValue(),(int)spp.e2.doubleValue(),
//                            (int)snp.e1.doubleValue(),(int)snp.e2.doubleValue());
                    x[i+1]=(int)snp.e1.doubleValue();
                    y[i+1]=(int)snp.e2.doubleValue();
//                    spp=snp;
                    nr+=dr;
                    nx+=dx;
                    ny+=dy;
                }
                g2.drawPolyline(x,y,x.length);
            }
        }
        if(mouseOver) {
            String label=edge.getLabel();
            double dx=vnodes.e2.x-vnodes.e1.x;
            double dy=vnodes.e2.y-vnodes.e1.y;
            double d=Math.sqrt(dx*dx+dy*dy);
            double ndx=dx/d;
            double ndy=dy/d;

            if(label!=null) {
                T2<Double,Double> p=proj.worldToScreen(vnodes.e1.x+dx*0.5+Math.sin(0.5*Math.PI)*curvature*ndy,
                                                       vnodes.e1.y+dy*0.5-Math.sin(0.5*Math.PI)*curvature*ndx);
                if(p.e1 == Double.NaN || p.e2 == Double.NaN) return;
                drawLabelRect(g2, proj, label,(int)p.e1.doubleValue(),(int)p.e2.doubleValue(),getColor());
            }
            T2<String,String> nodeLabels=edge.getNodeLabels();
            if(nodeLabels!=null) {
                if(swapped) nodeLabels=t2(nodeLabels.e2,nodeLabels.e1);
                T2<Double,Double> p=proj.worldToScreen(vnodes.e1.x+dx*0.2+Math.sin(0.2*Math.PI)*curvature*ndy,
                                                       vnodes.e1.y+dy*0.2-Math.sin(0.2*Math.PI)*curvature*ndx);
                if(p.e1 == Double.NaN || p.e2 == Double.NaN) return;
                drawLabelRect(g2, proj, nodeLabels.e1,(int)p.e1.doubleValue(),(int)p.e2.doubleValue(),getColor());
                p=proj.worldToScreen(vnodes.e1.x+dx*0.8+Math.sin(0.8*Math.PI)*curvature*ndy,
                                                       vnodes.e1.y+dy*0.8-Math.sin(0.8*Math.PI)*curvature*ndx);
                drawLabelRect(g2, proj, nodeLabels.e2,(int)p.e1.doubleValue(),(int)p.e2.doubleValue(),getColor());
            }
        }
        else if(labelEdges) {
            String label=edge.getLabel();
            double dx=vnodes.e2.x-vnodes.e1.x;
            double dy=vnodes.e2.y-vnodes.e1.y;
            double d=Math.sqrt(dx*dx+dy*dy);
            double ndx=dx/d;
            double ndy=dy/d;

            if(label!=null) {
                T2<Double,Double> p=proj.worldToScreen(vnodes.e1.x+dx*0.5+Math.sin(0.5*Math.PI)*curvature*ndy,
                                                       vnodes.e1.y+dy*0.5-Math.sin(0.5*Math.PI)*curvature*ndx);
                if(p.e1 == Double.NaN || p.e2 == Double.NaN) return;
                drawDetailLabel(g2, proj, label,(int)p.e1.doubleValue(),(int)p.e2.doubleValue(),getColor());
            }
        }
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
       return style.getEdgeColor(this); 
    }
    
    public double getWidth() {
        return style.getEdgeWidth(this);
    }
    public int getLabelFontSize() {
        return style.getEdgeLabelFontSize(this);
    }
    public Font getLabelFont(int forSize) {
        return style.getEdgeLabelFont(this, forSize);
    }
    public Color getLabelColor() {
        return style.getEdgeLabelColor(this);
    }
    public Stroke getLabelBorderStroke() {
        return style.getEdgeLabelStroke(this);
    }
    public Stroke getStroke(int forWidth) {
        return style.getEdgeStroke(this, forWidth);
    }
}
