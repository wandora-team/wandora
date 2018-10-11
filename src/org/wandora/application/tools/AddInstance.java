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
 * AddInstance.java
 *
 * Created on October 1, 2004, 1:12 PM
 */

package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;


/**
 * Tool is used to add instance to the context topics.
 *
 * @author  akivela
 */
public class AddInstance extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;

	
	private boolean shouldRefresh = false;
    
    public AddInstance() {
    }
    public AddInstance(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Add instance";
    }

    @Override
    public String getDescription() {
        return "Add instance topic to selected topics.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        shouldRefresh = false;
        Iterator topics = context.getContextObjects();
        if(topics != null && topics.hasNext()) {
            Topic instanceTopic=wandora.showTopicFinder("Select instance topic...");
            Topic topic = null;
            if(instanceTopic != null) {
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            shouldRefresh = true;
                            instanceTopic.addType(topic);
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
    }
    
    
    @Override
    public boolean requiresRefresh() {
        return shouldRefresh;
    }
    
}
