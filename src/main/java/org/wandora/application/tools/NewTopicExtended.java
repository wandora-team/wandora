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
 * 
 * NewTopicExtended.java
 *
 * Created on 2013-04-28
 *
 */

package org.wandora.application.tools;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.NewTopicPanelExtended;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.topicpanels.GraphTopicPanel;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class NewTopicExtended extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of NewTopicExtended */
    public NewTopicExtended() {
    }
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        NewTopicPanelExtended newTopicPanel = new NewTopicPanelExtended(context);
        if(newTopicPanel.getAccepted()) {
            Topic newTopic = newTopicPanel.createTopic();
            postExecute(newTopic, context);
        }
    }
    
    
    
    private void postExecute(Topic newTopic, Context context) {
        try {
            if(newTopic != null) {
                Object contextSource = context.getContextSource();
                if(contextSource != null) {
                    if(contextSource instanceof TopicGrid) {
                        ((TopicGrid) contextSource).setCurrentTopic(newTopic);
                    }
                    else if(contextSource instanceof TopicMapGraphPanel) {
                        ((TopicMapGraphPanel) contextSource).setRootTopic(newTopic);
                    }
                    else if(contextSource instanceof GraphTopicPanel) {
                        ((GraphTopicPanel) contextSource).open(newTopic);
                    }
                }
            }
        }
        catch(Exception e) {
            // SKIP SILENTLY...
        }
    }
    
    
    
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/new_topic.png");
    }
    @Override
    public String getName() {
        return "New Topic";
    }

    @Override
    public String getDescription() {
        return "Creates new topic.";
    }
    

}
