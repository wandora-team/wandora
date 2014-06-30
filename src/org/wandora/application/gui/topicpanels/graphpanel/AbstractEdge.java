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
 * AbstractEdge.java
 *
 * Created on 6. kesäkuuta 2007, 11:45
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;


import static org.wandora.utils.Tuples.*;
/**
 *
 * @author olli
 */
public abstract class AbstractEdge implements Edge {
    
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
        return 0.1;
    }

    @Override
    public double getLength() {
        return 50.0;
    }

    public double getEdgeWidth() {
        return 2.0;
    }

    

    
}
