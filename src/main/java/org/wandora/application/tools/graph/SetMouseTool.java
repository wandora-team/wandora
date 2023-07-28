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
 * SetMouseTool.java
 *
 * Created on 16.7.2007, 14:52
 *
 */

package org.wandora.application.tools.graph;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;


/**
 *
 * @author akivela
 */
public class SetMouseTool extends AbstractGraphTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private int myMouseTool = -1;


    
    /** Creates a new instance of ToggleAntialiasTool */
    public SetMouseTool(TopicMapGraphPanel gp, int predefinedMouseTool) {
        super(gp);
        this.setContext(new GraphNodeContext());
        this.myMouseTool = predefinedMouseTool;
    }
    
    
    
    
    public int getMouseTool(){return myMouseTool;}
    
    @Override
    public String getName() {
        if(myMouseTool != -1) {
            return TopicMapGraphPanel.getToolName(myMouseTool);
        }
        return "Set mouse tool for graph panel";
    }
    
    
    @Override
    public String getDescription() {
        if(myMouseTool != -1) {
            return TopicMapGraphPanel.getToolDescription(myMouseTool);
        }
        return super.getDescription();
    }
    
    
    public void executeSynchronized(Wandora wandora, Context context) {
        TopicMapGraphPanel graphPanel = this.solveGraphPanel(wandora, context);
        if(graphPanel != null) {
            graphPanel.setMouseTool(myMouseTool);
        }
    }    
}

