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
 * SetTopicStringifier.java
 *
 */


package org.wandora.application.tools;


import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicstringify.TopicStringifier;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class SetTopicStringifier extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	
	private TopicStringifier topicStringifier = null;
    
    
    public SetTopicStringifier(TopicStringifier r) {
        topicStringifier = r;
    }

    

    @Override
    public String getName() {
        return "Topic renders";
    }

    @Override
    public String getDescription() {
        if(topicStringifier == null) {
            return "Topic renders";
        }
        else {
            return topicStringifier.getDescription();
        }
    }
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/view_topic_as.png");
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        boolean success = topicStringifier.initialize(wandora, context);
        if(success) {
            TopicToString.setStringifier(topicStringifier);
        }
    }
  
}
