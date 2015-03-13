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
 * EraserTool.java
 *
 * Created on 16. heinäkuuta 2007, 16:55
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.mousetools;


import org.wandora.application.gui.topicpanels.graphpanel.*;
import java.awt.*;


/**
 *
 * @author akivela
 */
public class EraserTool extends MouseTool {
    
    
    /** Creates a new instance of EraserTool */
    public EraserTool() {
    }
    

    @Override
    public boolean mouseClicked(TopicMapGraphPanel panel, int mousex,int mousey) {
        VNode mouseOverNode=panel.getMouseOverNode();
        VModel model=panel.getModel();
        if(mouseOverNode != null && model != null) {
            model.openNode(mouseOverNode);
            return true;
        }
        else return false;
    }
    
    
    @Override
    public Cursor getCursor(TopicMapGraphPanel panel, int mousex, int mousey){
        VNode mouseOverNode=panel.getMouseOverNode();
        if(mouseOverNode!=null) return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        else return null;
    }
}
