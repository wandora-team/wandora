/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 * ClassIterator.java
 *
 * Created on 13. huhtikuuta 2006, 10:49
 *
 */

package org.wandora.application.contexts.iterators;

import java.util.Iterator;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.logger.Log4j2Logger;


/**
 *
 * @author akivela
 */
public class ClassIterator extends TopicIterator {

	private static final Log4j2Logger logger = Log4j2Logger.getLogger(ClassIterator.class);
	

    @Override
    public Iterator<Topic> solveIteratorForTopic(Topic topic, TopicMap topicmap, Iterator<Topic> oldIterator) {
        Iterator<Topic> it = oldIterator;
        if(topic != null) {
            try{
                collection = topic.getTypes();
            }
            catch(TopicMapException tme){
            	logger.error(tme);
            }
            if(collection != null) it = collection.iterator();
        }
        return it;
    }
    
}
