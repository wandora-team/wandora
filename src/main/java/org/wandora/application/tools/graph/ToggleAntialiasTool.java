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

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;



/**
 *
 * @author olli
 */
public class ToggleAntialiasTool extends AbstractGraphTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	/** Creates a new instance of ToggleAntialiasTool */
    public ToggleAntialiasTool(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    @Override
    public String getName(){
        return "Toggle antialiasing";
    }
    
    @Override
    public String getDescription(){
        return "Change option for antialising. If selected, "+
               "graph is draw smooth. Drawing smooth graphics requires more computing power.";
    }
    
    public void executeSynchronized(Wandora wandora, Context context) {
        TopicMapGraphPanel graphPanel = this.solveGraphPanel(wandora, context);
        //System.out.println("GRAPHPANEL: "+graphPanel);
        if(graphPanel != null) {
            graphPanel.setAntialized(!graphPanel.getAntialized());
        }

    }    
    
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }
}
