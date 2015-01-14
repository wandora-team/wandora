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

import bibliothek.gui.Dockable;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.topicpanels.DockingFramePanel;

/**
 *
 * @author akivela
 */


public class DeleteDockable extends AbstractDockingTool {
    private Dockable dockable = null;

    /** Creates a new instance of DeleteDockable */
    public DeleteDockable(Dockable d) {
        dockable = d;
    }
    
    
    @Override
    public String getName() {
        if(dockable != null) {
            return "Close dockable "+dockable.getTitleText();
        }
        else {
            return "Close dockable";
        }
    }
    
    
    @Override
    public void execute(Wandora w, Context context) {
        DockingFramePanel dockingPanel = this.solveDockingFramePanel(w, context);
        if(dockingPanel != null && dockable != null) {
            try {
                dockingPanel.deleteDockable(dockable);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }    
    
}
