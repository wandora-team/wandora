/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class AbstractDockingTool extends AbstractWandoraTool implements WandoraTool {

    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        System.out.println("Warning: Docking tool is not overriding execute method.");
    }
    
    

    public DockingFramePanel solveDockingFramePanel(Wandora wandora, Context context) {
        if(context != null) {
            Object contextSource = context.getContextSource();
            if(contextSource != null && contextSource instanceof DockingFramePanel) {
                return ((DockingFramePanel)contextSource);
            }
        }
        if(wandora != null) {
            TopicPanel topicPanel = wandora.getTopicPanel();
            if(topicPanel != null && topicPanel instanceof DockingFramePanel) {
                return ((DockingFramePanel)topicPanel);
            }
        }
        return null;
    }
    
}
