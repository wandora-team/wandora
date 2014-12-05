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
    
    public void repaint(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);
        
        Point f=getAnchorCoordinates(from);
        Point t=getAnchorCoordinates(to);
        g2.drawLine(f.x, f.y, t.x, t.y);
    }
    
}
