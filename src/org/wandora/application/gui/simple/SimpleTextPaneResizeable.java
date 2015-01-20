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
 * SimpleTextPaneResizeable.java
 *
 * Created on Jan 5th, 2015, 21:38
 */


package org.wandora.application.gui.simple;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;



/**
 * Based on owatkins's example at
 * http://blue-walrus.com/2011/02/expandable-text-area-in-swing/
 *
 * @author akivela
 */


public class SimpleTextPaneResizeable extends SimpleTextPane implements MouseMotionListener {
    
    
    protected boolean onlyVerticalResize = false;
    protected boolean mousePressedInTriangle = false;
    protected Point mousePressedPoint = null;
    protected Dimension sizeAtPress = null;
    protected Dimension newSize = null;
    
    
    // Reference to the underlying scrollpane
    protected JScrollPane scrollPane = null;
 
    // Height and width.. not hypotenuse
    protected int triangleSize = 15;
 
    // Is the mouse in the triangle
    protected boolean inTheTriangleZone = false;
    
    
    
    public SimpleTextPaneResizeable(JPanel parent) {
        super(parent);
        addMouseMotionListener(this);
    }
    public SimpleTextPaneResizeable() {
        this(null);
    }
    
    
    
    /**
     * Paint the text area
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D)g;
        if(inTheTriangleZone) {
            graphics.setColor(new Color(0.5f,0.5f,0.5f,0.75f));
        }
        else {
            graphics.setColor(new Color(0.5f,0.5f,0.5f,0.2f));
        }
        graphics.fillPolygon(getTriangle());
    }
    
    
    
    
    protected JScrollPane getScrollPane() {
 
        // Get scrollpane, if first time calling this method then add an addjustment listener
        // to the scroll pane
 
        if(this.getParent() instanceof JViewport) {
            if(scrollPane == null) {
                JViewport p = (JViewport)this.getParent();
                scrollPane = (JScrollPane)p.getParent();
                scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
                    @Override
                    public void adjustmentValueChanged(AdjustmentEvent e) {
                        //need to repaint the triangle when scroll bar moves
                        repaint();
                    }
                });
            }
        }
        return scrollPane;
    }



    
    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        Polygon polygon = getTriangle();
 
        if(polygon.contains(p)) {
            inTheTriangleZone = true;
            this.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
            this.repaint();
        }
        else {
            inTheTriangleZone = false;
            this.setCursor(new Cursor(Cursor.TEXT_CURSOR));
            this.repaint();
        }
    }
    
    
    
    @Override
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        if(mousePressedInTriangle) { 
            // Mouse was pressed in triangle so we can resize
            inTheTriangleZone = true;
            int xDiff = (mousePressedPoint.x - p.x);
            int yDiff = (mousePressedPoint.y - p.y);
            
            if(onlyVerticalResize) {
                newSize = new Dimension(sizeAtPress.width, sizeAtPress.height - yDiff);
            }
            else {
                newSize = new Dimension(sizeAtPress.width - xDiff, sizeAtPress.height - yDiff);
            }

            JScrollPane sp = getScrollPane();
            this.revalidate();
            this.repaint();

            if(sp != null) {
                sp.getViewport().setSize(newSize);
                sp.getViewport().setPreferredSize(newSize);
                sp.getViewport().setMinimumSize(newSize);

                sp.setSize(newSize);
                sp.setPreferredSize(newSize);
                sp.setMinimumSize(newSize);

                sp.getParent().revalidate();
                sp.revalidate();
                sp.repaint();
            }
        }
    }

    
    
    public void setHorizontallyResizeable(boolean rh) {
        onlyVerticalResize = !rh;
    }

    
    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        if(getTriangle().contains(p)) {
            mousePressedInTriangle = true;
            mousePressedPoint = p;
            sizeAtPress = getScrollPane().getSize();
        }
    }

    
    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressedInTriangle = false;
        mousePressedPoint = null;
    }

    
    @Override
    public void mouseExited(MouseEvent e) {
        inTheTriangleZone=false;
        repaint();
    }
    
    
    
    
    private Polygon getTriangle() {
        JViewport viewport = getScrollPane().getViewport();
 
        // Get bounds of viewport
        Rectangle bounds = viewport.getBounds();
 
        // Position of viewport relative to text area.
        Point viewportPosition = viewport.getViewPosition();
 
        int w = viewportPosition.x + bounds.width;
        int h = viewportPosition.y + bounds.height;
 
        int[] xs = {w,w,w-triangleSize};
        int[] ys = {h-triangleSize,h,h};
 
        Polygon polygon = new Polygon(xs, ys, 3);
        return polygon;
    }
    
}
