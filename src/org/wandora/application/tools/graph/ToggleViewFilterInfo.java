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
 * ToggleViewFilterInfo.java
 *
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


public class ToggleViewFilterInfo extends AbstractGraphTool implements WandoraTool {
    
    
    /** Creates a new instance of ToggleViewFilterInfo */
    public ToggleViewFilterInfo(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    @Override
    public String getName(){
        return "Toggle visibility of node and edge filter info";
    }
    
    @Override
    public String getDescription(){
        return "Toggle visibility of node and edge filter info. If selected, "+
               "information about node and edge filters is viewed beside the graph.";
    }
    
    
    public void executeSynchronized(Wandora admin, Context context) {
        TopicMapGraphPanel graphPanel = this.solveGraphPanel(admin, context);
        if(graphPanel != null) {
            graphPanel.setViewFilterInfo(!graphPanel.getViewFilterInfo());
        }
    }
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
    
}
