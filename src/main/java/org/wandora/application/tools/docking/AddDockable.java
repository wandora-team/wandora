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
 */

package org.wandora.application.tools.docking;



import java.util.Iterator;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.application.gui.topicpanels.TopicPanel;
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

	
	private static final long serialVersionUID = 1L;


    private Class dockableClass = null;
    private Icon dockableIcon = null;
    
    
    
    /** Creates a new instance of AddDockable */
    public AddDockable(Class dc) {
        dockableClass = dc;
    }
    
    
    
    public AddDockable(String className) {
        try {
            if(className != null) {
                dockableClass = Class.forName(className);
            }
            else {
                System.out.println("AddDockable can't use given class name 'null'.");
            }
        }
        catch(Exception e) {
            System.out.println("AddDockable can't use given class name '"+className+"'.");
        }
    }
    
    
    
    @Override
    public String getName() {
        if(dockableClass != null) {
            return "New dockable "+dockableClass.getName();
        }
        else {
            return "New dockable";
        }
    }
    
    
    @Override
    public Icon getIcon() {
        if(dockableClass == null) {
            return UIBox.getIcon("gui/icons/topic_panel_add.png");
        }
        else {
            if(dockableIcon == null) {
                try {
                    Object o = dockableClass.getDeclaredConstructor().newInstance();
                    if(o instanceof TopicPanel) {
                        TopicPanel tp = (TopicPanel) o;
                        dockableIcon = tp.getIcon();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(dockableIcon == null) {
                return UIBox.getIcon("gui/icons/topic_panel_add.png");
            }
            else {
                return dockableIcon;
            }
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
                dockingPanel.addDockable(dockableClass.getDeclaredConstructor().newInstance(), t);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("No valid dockable class registered in AddDockable. Can't create new dockable.");
        }
    }    
    
    
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }

}
