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
 */

package org.wandora.application.tools.navigate;

import java.util.Iterator;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.exceptions.OpenTopicNotSupportedException;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author  akivela
 */
public class OpenTopicIn extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	public final static int ASK_USER = 100;
    public final static int SOLVE_USING_CONTEXT = 102;
    
    public int options = SOLVE_USING_CONTEXT;
    
    private TopicPanel topicPanel = null;
    

    
    /** Creates a new instance of OpenTopicIn */
    public OpenTopicIn(TopicPanel tp) {
        topicPanel = tp;
    }
    public OpenTopicIn(TopicPanel tp, Context preferredContext) {
        topicPanel = tp;
        setContext(preferredContext);
    }
    public OpenTopicIn(TopicPanel tp, int preferredOptions) {
        topicPanel = tp;
        this.options = preferredOptions;
    }
            
            
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        Iterator contextTopics = context.getContextObjects();
        if(options == SOLVE_USING_CONTEXT && contextTopics != null && contextTopics.hasNext()) {
            Topic t = (Topic) contextTopics.next();
            if(t != null) {
                if(topicPanel != null) {
                    try {
                        topicPanel.open(t);
                        wandora.addToHistory(t);
                    } 
                    catch(OpenTopicNotSupportedException ex) {
                        wandora.handleError(ex);
                    }
                }
                else {
                    wandora.openTopic(t);
                }
            }
            else {
                WandoraOptionPane.showMessageDialog(wandora, "Can't open a topic. Select a valid topic first.", "Can't open a topic", WandoraOptionPane.WARNING_MESSAGE);
            }
        }
        else {
            if(topicPanel != null) {
                Topic t = wandora.showTopicFinder();
                if(t != null) {
                    try {
                        topicPanel.open(t);
                        wandora.addToHistory(t);
                    }
                    catch (OpenTopicNotSupportedException ex) {
                        wandora.handleError(ex);
                    }
                }
            }
            else {
                Topic t = wandora.showTopicFinder();
                if(t != null) {
                    wandora.openTopic(t);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Open topic in";
    }

    @Override
    public String getDescription() {
        return "Open selected topic in given topic panel.";
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
