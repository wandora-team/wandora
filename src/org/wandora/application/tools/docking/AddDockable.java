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


import java.awt.Component;
import java.util.Iterator;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;




/**
 * Add a topic panel into the <code>DockingFramePanel</code>. Added topic panel
 * is specified with a class argument passed to the constructor. A single 
 * <code>AddDockable</code> object can add only one type of topic panels.
 * Whenever a topic panel is added a context topic is passed to the topic panel
 * as an argument.
 *
 * @author akivela
 */


public class AddDockable extends AbstractDockingTool {
    
    private Class<Component> dockableClass = null;

    
    /** Creates a new instance of AddDockable */
    public AddDockable(Class dc) {
        dockableClass = dc;
    }
    
    
    @Override
    public String getName() {
        if(dockableClass != null) {
            return "Add dockable "+dockableClass.getName();
        }
        else {
            return "Add dockable";
        }
    }
    
    
    @Override
    public void execute(Wandora w, Context context) {
        DockingFramePanel dockingPanel = this.solveDockingFramePanel(w, context);
        if(dockableClass != null && dockingPanel != null) {
            try {
                Topic t = null;
                Iterator i = context.getContextObjects();
                if(i.hasNext()) {
                    try {
                        t = (Topic) i.next();
                    }
                    catch(Exception e) {}
                }
                if(t == null) {
                    t = w.getOpenTopic();
                }
                if(t == null) {
                    t = w.getTopicMap().getTopic(TMBox.WANDORACLASS_SI);
                }
                dockingPanel.addDockable(dockableClass.newInstance(), t);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }    
    
    
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }

}
