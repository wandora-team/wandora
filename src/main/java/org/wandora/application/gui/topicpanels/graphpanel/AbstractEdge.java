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
 * AbstractEdge.java
 *
 * Created on 6.6.2007, 11:45
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;


import org.wandora.utils.Tuples.T2;
/**
 *
 * @author olli
 */
public abstract class AbstractEdge implements Edge {
    public static double defaultEdgeStiffness = 0.1;
    public static double defaultEdgeLength = 50.0;
    public static double defaultEdgeWidth = 2.0;
    
    
    @Override
    public String getLabel(){
        return null;
    }
    
    
    @Override
    public T2<String,String> getNodeLabels(){
        return null;
    }
    
    
    @Override
    public double getStiffness() {
        return defaultEdgeStiffness;
    }

    
    @Override
    public double getLength() {
        return defaultEdgeLength;
    }

    
    public double getEdgeWidth() {
        return defaultEdgeWidth;
    }

}
