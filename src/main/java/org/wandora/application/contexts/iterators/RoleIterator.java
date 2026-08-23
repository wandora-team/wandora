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
 * RoleIterator.java
 *
 * Created on 13. huhtikuuta 2006, 11:03
 *
 */

package org.wandora.application.contexts.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.logger.Log4j2Logger;

/**
 *
 * @author akivela
 */
public class RoleIterator extends TopicIterator {


	private static final Log4j2Logger logger = Log4j2Logger.getLogger(RoleIterator.class);



    @Override
    public Iterator<Topic> solveIteratorForTopic(Topic topic, TopicMap topicmap, Iterator<Topic> oldIterator) {
        Collection<Topic> allRoleTopics = new ArrayList<>();
        Collection<Association> associations = null;
        Association association = null;
        Iterator<Association> associationIterator = null;
        Collection<Topic> roleTopics = null;
        Topic roleTopic = null;

        if(topic != null) {
            try{
                associations = topic.getAssociations();
                if(associations != null) {
                    associationIterator = associations.iterator();
                    while(associationIterator.hasNext()) {
                        association = associationIterator.next();
                        if(association == null) continue;
                        roleTopics = association.getRoles();
                        if(roleTopics != null && roleTopics.size() > 0) {
                            for(Iterator<Topic> roleIterator = roleTopics.iterator(); roleIterator.hasNext(); ) {
                                roleTopic = (Topic) roleIterator.next();
                                if(roleTopic != null) {
                                    allRoleTopics.add(roleTopic);
                                }
                            }
                        }
                    }
                }
            }
            catch(TopicMapException tme) {
            	logger.error(tme);
            }
        }
        return allRoleTopics.iterator();
    }

    
}
