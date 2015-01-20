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
 * GraphStyle.java
 *
 * Created on 12. heinäkuuta 2007, 11:35
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import java.awt.*;


/**
 *
 * @author akivela
 */
public interface GraphStyle {
    public Color getNodeColor(VNode vn);
        
    public Color getNodeTextColor(VNode vn);
    
    public NodeShape getNodeShape(VNode vn);
    
    public double getNodeWidth(VNode vn);
    public double getNodeHeight(VNode vn);
    public Color getNodeBorderColor(VNode vn);
    public Stroke getNodeBorderStroke(VNode vn);
    public int getNodeFontSize(VNode vn);
    public Font getNodeFont(VNode vn, int forSize);
    
    
    public double getEdgeWidth(VEdge ve);
    public Color getEdgeColor(VEdge ve);
    public int getEdgeLabelFontSize(VEdge ve);
    public Font getEdgeLabelFont(VEdge ve, int forSize);
    public Color getEdgeLabelColor(VEdge ve);
    public Stroke getEdgeLabelStroke(VEdge ve);
    public Stroke getEdgeStroke(VEdge ve, int forWidth);
}
