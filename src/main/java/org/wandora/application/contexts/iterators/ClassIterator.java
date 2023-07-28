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
 * ClassIterator.java
 *
 * Created on 13. huhtikuuta 2006, 10:49
 *
 */

package org.wandora.application.contexts.iterators;

import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class ClassIterator extends TopicIterator {


    @Override
    public Iterator solveIteratorForTopic(Topic topic, TopicMap topicmap, Iterator oldIterator) {
        Iterator it = oldIterator;
        if(topic != null) {
            try{
                collection = topic.getTypes();
            }
            catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION
            }
            if(collection != null) it = collection.iterator();
        }
        return it;
    }
    
}
