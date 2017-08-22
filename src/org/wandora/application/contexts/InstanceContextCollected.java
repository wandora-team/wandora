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
 * InstanceContextCollected.java
 *
 * Created on 13. huhtikuuta 2006, 9:45
 *
 */

package org.wandora.application.contexts;


import org.wandora.topicmap.*;
import java.util.*;



/**
 *
 * @author akivela
 */
public class InstanceContextCollected extends LayeredTopicContext implements Context {

    public static final int GATHER_TOPICS_FROM_LAYERSTACK = 1;
    public static final int GATHER_TOPICS_FROM_OWNER_TOPICMAP = 2;

    
    public int gatherStyle = GATHER_TOPICS_FROM_LAYERSTACK;
    public boolean removeDuplicates = true;
    
    
    
    @Override
    public Iterator getContextObjects() {
        return collectInstancesOf( super.getContextObjects() );
    }
    
    
    
    
    
    public Iterator collectInstancesOf(Iterator topics) {
        if(topics == null) return null;
        List contextTopics = new ArrayList();
        Collection<Topic> instanceTopics = null;
        Topic topic = null;
        Topic instance = null;
        TopicMap topicmap = null;
        if(gatherStyle == GATHER_TOPICS_FROM_LAYERSTACK) {
            topicmap = this.wandora.getTopicMap();
        }
        while(topics.hasNext()) {
            try {
                topic = (Topic) topics.next();
                if(topic == null) continue;
                if(gatherStyle == GATHER_TOPICS_FROM_OWNER_TOPICMAP) {
                    topicmap = topic.getTopicMap();
                }
                if(removeDuplicates) {
                    instanceTopics = topicmap.getTopicsOfType(topic);
                    for(Iterator<Topic> instanceIterator = instanceTopics.iterator(); instanceIterator.hasNext(); ) {
                        instance = instanceIterator.next();
                        if(instance != null && !contextTopics.contains(instance)) {
                            contextTopics.add(instance);
                        }
                    }
                }
                else {
                    contextTopics.addAll( topicmap.getTopicsOfType(topic) );
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        return contextTopics.iterator();
    }

        
}
