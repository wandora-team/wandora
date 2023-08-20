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
 * ToggleAntialiasTool.java
 *
 * Created on 11.6.2007, 16:33
 *
 */

package org.wandora.application.tools.graph;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.GraphNodeContext;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;



/**
 *
 * @author akivela
 */
public class ToggleLabelEdges extends AbstractGraphTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of ToggleLabelEdges */
    public ToggleLabelEdges(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    @Override
    public String getName(){
        return "Toggle draw egde labels";
    }
    
    @Override
    public String getDescription(){
        return "Change option for draw egde labels. If selected, "+
               "Wandora draws edge labels i.e. association types.";
    }
    
    
    public void executeSynchronized(Wandora wandora, Context context) {
        TopicMapGraphPanel graphPanel = this.solveGraphPanel(wandora, context);
        if(graphPanel != null) {
            graphPanel.setLabelEdges(!graphPanel.getLabelEdges());
        }
    }
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
}
