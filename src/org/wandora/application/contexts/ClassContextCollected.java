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
 * 
 * ClassContextCollected.java
 *
 * Created on 8. huhtikuuta 2006, 20:04
 *
 */

package org.wandora.application.contexts;


import org.wandora.topicmap.*;
import java.util.*;



/**
 *
 * @author akivela
 */
public class ClassContextCollected extends LayeredTopicContext implements Context {
    
    public boolean removeDuplicates = true;  
    
    
    /**
     * Creates a new instance of ClassContextCollected
     */
    public ClassContextCollected() {
    }
    
    
    
    @Override
    public Iterator getContextObjects() {
        return getClassesOf( super.getContextObjects() );
    }
    
    
    
    public Iterator getClassesOf(Iterator topics) {
        if(topics == null) return null;
        ArrayList contextTopics = new ArrayList();
        Collection<Topic> classTopics = null;
        Topic topic = null;
        Topic classTopic = null;

        while(topics.hasNext()) {
            try {
                topic = (Topic) topics.next();
                if(topic == null) continue;
                if(removeDuplicates) {
                    classTopics = topic.getTypes();
                    for(Iterator<Topic> classIterator = classTopics.iterator(); classIterator.hasNext(); ) {
                        classTopic = classIterator.next();
                        if(classTopic != null && !contextTopics.contains(classTopic)) {
                            contextTopics.add(classTopic);
                        }
                    }
                }
                else {
                    contextTopics.addAll( topic.getTypes() );
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        return contextTopics.iterator();
    }
    
}
