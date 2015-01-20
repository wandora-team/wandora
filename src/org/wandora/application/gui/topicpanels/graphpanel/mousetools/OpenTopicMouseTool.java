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
 * OpenTopicMouseTool.java
 *
 * Created on 25. kesäkuuta 2007, 10:14
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.mousetools;

import org.wandora.application.gui.topicpanels.graphpanel.*;
import javax.swing.*;
import java.awt.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */
public class OpenTopicMouseTool extends MouseTool {
    
    /** Creates a new instance of OpenTopicMouseTool */
    public OpenTopicMouseTool() {
    }

    @Override
    public boolean mouseClicked(TopicMapGraphPanel panel, int mousex,int mousey) {
        VNode mouseOverNode = panel.getMouseOverNode();
        if(mouseOverNode==null) return false;
        Node n = mouseOverNode.getNode();
        Wandora wandora=panel.getWandora();
        if(n instanceof TopicNode && wandora != null) {
            Topic t = ((TopicNode) n).getTopic();
            wandora.applyChangesAndOpen(t);    
            return true;
        }
        return false;
    }

    
    @Override
    public Cursor getCursor(TopicMapGraphPanel panel, int mousex, int mousey){
        VNode mouseOverNode=panel.getMouseOverNode();
        if(mouseOverNode!=null) return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        else return null;
    }
    
}
