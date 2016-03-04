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
 * 
 */

package org.wandora.application.tools.navigate;

import java.awt.Component;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.*;
import java.util.*;
import javax.swing.*;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.gui.topicpanels.TraditionalTopicPanel;

/**
 *
 * @author  akivela
 */
public class OpenTopicInNew extends AbstractWandoraTool implements WandoraTool {
    public final static int ASK_USER = 100;
    public final static int SOLVE_USING_CONTEXT = 102;
    
    public int options = SOLVE_USING_CONTEXT;
    
    private Class<Component> dockableClass = null;

    
    /** Creates a new instance of OpenTopicAt */
    public OpenTopicInNew(Class<Component> c) {
        dockableClass = c;
    }
    public OpenTopicInNew(Class<Component> c, Context preferredContext) {
        dockableClass = c;
        setContext(preferredContext);
    }
    public OpenTopicInNew(Class<Component> c, int preferredOptions) {
        dockableClass = c;
        this.options = preferredOptions;
    }
            
            
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        try {
            DockingFramePanel dockingPanel = (DockingFramePanel) wandora.topicPanelManager.getTopicPanel();
            Iterator contextTopics = context.getContextObjects();
            if(options == SOLVE_USING_CONTEXT && contextTopics != null && contextTopics.hasNext()) {
                int count = 0;
                while(contextTopics.hasNext()) {
                    count++;
                    boolean doIt = true;
                    if(count > 20) {
                        int a = WandoraOptionPane.showConfirmDialog(wandora, "You have already opened 20 topic panels. You sure want to open yet another?", "Many topic panels already open", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                        if(a != WandoraOptionPane.CANCEL_OPTION) break;
                        else if(a != WandoraOptionPane.YES_OPTION) doIt = false;
                    }
                    if(doIt) {
                        Topic t = (Topic) contextTopics.next();
                        if(t != null) {
                            wandora.addToHistory(t);
                            if(dockableClass != null) {
                                dockingPanel.addDockable(dockableClass.newInstance(), t);
                            }
                            else {
                                dockingPanel.addDockable((Component) new TraditionalTopicPanel(), t);
                            }
                        }
                        else {
                            WandoraOptionPane.showMessageDialog(wandora, "Can't open a topic. Select a valid topic first.", "Can't open a topic", WandoraOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
            else {
                if(dockableClass != null) {
                    Topic t = wandora.showTopicFinder();                
                    if(t != null) {
                        dockingPanel.addDockable(dockableClass.newInstance(), t);
                    }
                }
                else {
                    Topic t = wandora.showTopicFinder();                
                    if(t != null) {
                        dockingPanel.addDockable((Component) new TraditionalTopicPanel(), t);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Open topic in new";
    }

    @Override
    public String getDescription() {
        return "Open selected topic in new topic panel.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_open.png");
    }
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
    @Override
    public boolean runInOwnThread() {
        return false;
    }
}
