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
 */


package org.wandora.application.tools.docking;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.topicpanels.DockingFramePanel;

/**
 *
 * @author akivela
 */


public class DeleteAllDockables extends AbstractDockingTool {
    

    /** Creates a new instance of DeleteAllDockables */
    public DeleteAllDockables() {
    }
    
    
    @Override
    public String getName() {
        return "Delete all dockables";
    }
    
    
    @Override
    public void execute(Wandora w, Context context) {
        DockingFramePanel dockingPanel = this.solveDockingFramePanel(w, context);
        if(dockingPanel != null) {
            try {
                dockingPanel.deleteAllDockables();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }    
    

}
