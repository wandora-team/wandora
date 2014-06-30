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
 * AssociationTypeIterator.java
 *
 * Created on 13. huhtikuuta 2006, 12:01
 *
 */

package org.wandora.application.contexts.iterators;

import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class AssociationTypeIterator extends TopicIterator {




    @Override
    public Iterator solveIteratorForTopic(Topic topic, TopicMap topicmap, Iterator oldIterator) {
        Collection associationTypeTopics = new ArrayList();
        Collection associations = null;
        Association association = null;
        Iterator associationIterator = null;
        
        if(topic != null) {
            try{
                associations = topic.getAssociations();
                if(associations != null) {
                    associationIterator = associations.iterator();
                    while(associationIterator.hasNext()) {
                        association = (Association) associationIterator.next();
                        if(association == null) continue;
                        associationTypeTopics.add(association.getType());
                    }
                }
            }catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION
            }
        }
        return associationTypeTopics.iterator();
    }

}
