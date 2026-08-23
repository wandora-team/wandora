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
 * TopicIteratorForCurrentLayer.java
 *
 * Created on 7.6.2006, 18:16
 *
 */

package org.wandora.application.contexts.iterators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.layered.LayeredTopic;
import org.wandora.utils.logger.Log4j2Logger;

/**
 *
 * @author akivela
 */
public class TopicIteratorForCurrentLayer extends TopicIterator {
    

	private static final Log4j2Logger logger = Log4j2Logger.getLogger(TopicIteratorForCurrentLayer.class);
    

    @Override
    public Iterator<Topic> solveIteratorForTopic(Topic topic, TopicMap topicmap, Iterator<Topic> oldIterator) {
        Iterator<Topic> it = oldIterator;
        if(topic != null && topicmap != null) {
            try {
                if(topic instanceof LayeredTopic layeredTopic) {
                    collection = layeredTopic.getTopicsForSelectedLayer();
                }
                else {
                    List<Topic> list = new ArrayList<>();
                    list.add(topic);
                    collection = list;
                    //collection = null;
                }
            }
            catch(Exception e) {
            	logger.error(e);
                collection=null;
            }
            if(collection != null) {
                it = collection.iterator();
            }
        }
        return it;
    }
}
