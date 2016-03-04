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
 * CloseCurrentTopicPanel.java
 *
 * Created on 22. huhtikuuta 2006, 10:15
 *
 */

package org.wandora.application.tools.navigate;



import org.wandora.application.tools.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import javax.swing.*;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.application.gui.topicpanels.TopicPanel;

/**
 *
 * @author akivela
 */
public class CloseCurrentTopicPanel extends AbstractWandoraTool implements WandoraTool {
    
    /** Creates a new instance of CloseCurrentTopicPanel */
    public CloseCurrentTopicPanel() {
    }

    @Override
    public String getName() {
        return "Close current topic panels";
    }

    @Override
    public String getDescription() {
        return "Close current topic panel.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        TopicPanel tp = wandora.getTopicPanel();
        if(tp != null && tp instanceof DockingFramePanel) {
            DockingFramePanel dfp = (DockingFramePanel) tp;
            dfp.deleteCurrentDockable();
        }
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_close.png");
    }

}
