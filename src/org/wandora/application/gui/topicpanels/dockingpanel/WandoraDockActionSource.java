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
 */


package org.wandora.application.gui.topicpanels.dockingpanel;

import bibliothek.gui.DockController;
import bibliothek.gui.dock.action.MultiDockActionSource;
import bibliothek.gui.dock.facile.action.CloseAction;
import bibliothek.gui.dock.facile.mode.ExternalizedMode;
import bibliothek.gui.dock.facile.mode.MinimizedMode;
import bibliothek.gui.dock.facile.mode.action.ExternalizedModeAction;
import bibliothek.gui.dock.facile.mode.action.MinimizedModeAction;
import javax.swing.JMenu;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.gui.topicpanels.dockingpanel.actions.CloseDockableAction;
import org.wandora.application.gui.topicpanels.dockingpanel.actions.MaximizeDockableAction;
import org.wandora.application.gui.topicpanels.dockingpanel.actions.TopicPanelMenuAction;
import org.wandora.application.gui.topicpanels.dockingpanel.actions.WandoraToolWrapperAction;
import org.wandora.application.tools.AboutWandora;
import org.wandora.application.tools.navigate.OpenTopic;

/**
 *
 * @author akivela
 */


public class WandoraDockActionSource extends MultiDockActionSource {
    private TopicPanel topicPanel = null;
    
    
    
    public WandoraDockActionSource(TopicPanel tp, DockingFramePanel dfp, DockController control) {
        topicPanel = tp;

        //this.add(new WandoraToolWrapperAction(new OpenTopic(OpenTopic.ASK_USER)));
        //this.add(new TopicPanelMenuAction(tp));
        this.add(new MaximizeDockableAction(dfp));
        this.add(new CloseDockableAction(dfp));
    }

}
