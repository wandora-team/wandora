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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import javax.swing.JComponent;

/**
 *
 * @author olli
 */


public class Connector {
    protected ConnectorAnchor from;
    protected ConnectorAnchor to;
    protected JComponent root;
    
    public Connector(JComponent root,ConnectorAnchor from,ConnectorAnchor to){
        this.root=root;
        this.from=from;
        this.to=to;
    }
    
    protected Point getRootCoordinates(JComponent c,int x,int y){
        Container co=c;
        Point d=new Point(x,y);
        while(co!=null && co!=root){
            Rectangle rect=co.getBounds();
            d.setLocation(d.getX()+rect.x, d.getY()+rect.y);
            co=co.getParent();
        }
        return d;
    }
    
    protected Point getAnchorCoordinates(ConnectorAnchor anchor){
        Point p=anchor.getAnchorPoint();
        return getRootCoordinates(anchor.getComponent(),p.x,p.y);
    }
    
    public Rectangle getBoundingBox(){
        Point f=getAnchorCoordinates(from);
        Point t=getAnchorCoordinates(to);
        int x=Math.min(f.x, t.x);
        int y=Math.min(f.y, t.y);
        int w=Math.abs(f.x-t.x);
        int h=Math.abs(f.y-t.y);
        return new Rectangle(x, y, w, h);
    }
    
    protected LineSegment[] rotateIcon(LineSegment[] icon,double angle){
        double ca=Math.cos(angle);
        double sa=Math.sin(angle);
        
        LineSegment[] ret=new LineSegment[icon.length];
        for(int i=0;i<icon.length;i++){
            LineSegment line=icon[i];
            Point p1=new Point((int)Math.round(line.p1.x*ca-line.p1.y*sa),
                               (int)Math.round(line.p1.x*sa+line.p1.y*ca));
            Point p2=new Point((int)Math.round(line.p2.x*ca-line.p2.y*sa),
                               (int)Math.round(line.p2.x*sa+line.p2.y*ca));
            ret[i]=new LineSegment(p1,p2);
        }
        return ret;
    }
    
    protected void drawIcon(Graphics2D g2,LineSegment[] icon,Point offset){
        for(LineSegment line : icon){
            g2.drawLine(line.p1.x+offset.x, line.p1.y+offset.y,
                        line.p2.x+offset.x, line.p2.y+offset.y);
        }
    }
    
    public static class LineSegment {
        public Point p1;
        public Point p2;
        public LineSegment(){}
        public LineSegment(Point p1,Point p2){this.p1=p1; this.p2=p2;}
    }
    
    public static final LineSegment[] arrowIcon=new LineSegment[]{
        new LineSegment(new Point(-10,-10),new Point(0,0)),
        new LineSegment(new Point(-10, 10),new Point(0,0))
    };
    
    protected Point paintExit(Graphics2D g2,ConnectorAnchor anchor,boolean in){
        Point p=getAnchorCoordinates(anchor);
        Point exitPoint=null;
        switch(anchor.getExitDirection()){
            case LEFT:
                exitPoint=new Point(p.x-20,p.y);
                if(in) drawIcon(g2,arrowIcon,p);
                break;
            case RIGHT:
                exitPoint=new Point(p.x+20,p.y);
                if(in) drawIcon(g2,rotateIcon(arrowIcon,Math.PI),p);
                break;
            case UP:
                exitPoint=new Point(p.x,p.y-20);
                if(in) drawIcon(g2,rotateIcon(arrowIcon,-Math.PI/2),p);
                break;
            case DOWN:
                exitPoint=new Point(p.x,p.y+20);
                if(in) drawIcon(g2,rotateIcon(arrowIcon,Math.PI/2),p);
                break;
        }
        g2.drawLine(p.x,p.y,exitPoint.x,exitPoint.y);
        
        return exitPoint;
    }
    
    public void repaint(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);
        
        Point f=paintExit(g2,from,false);
        Point t=paintExit(g2,to,true);
        
//        g2.drawLine(f.x, f.y, t.x, t.y);
        
        Point f0=getAnchorCoordinates(from);
        Point t0=getAnchorCoordinates(to);
        
        
        Path2D.Double curve=new Path2D.Double();
        curve.moveTo(f.x, f.y);
        curve.curveTo(f.x+(f.x-f0.x)*5, f.y+(f.y-f0.y)*5,
                      t.x+(t.x-t0.x)*5, t.y+(t.y-t0.y)*5,
                      t.x, t.y);
        g2.draw(curve);
        
    }
    
}
