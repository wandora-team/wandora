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
 * ToggleFilterWindow.java
 *
 * Created on 17. heinäkuuta 2007, 11:26
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
public class ToggleFilterWindow extends AbstractGraphTool implements WandoraTool {
    


    
    /** Creates a new instance of ToggleFilterWindow */
    public ToggleFilterWindow(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    
    @Override
    public String getName(){
        return "Toggle filter window visibility";
    }
    
    
    public void executeSynchronized(Wandora admin, Context context) {
        TopicMapGraphPanel graphPanel = this.solveGraphPanel(admin, context);
        if(graphPanel != null) {
            graphPanel.openFilterManager();
        }
    }    
    
}
