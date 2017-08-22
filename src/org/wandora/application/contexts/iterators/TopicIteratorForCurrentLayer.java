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
 * TopicIteratorForCurrentLayer.java
 *
 * Created on 7. kesäkuuta 2006, 18:16
 *
 */

package org.wandora.application.contexts.iterators;

import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class TopicIteratorForCurrentLayer extends TopicIterator {
    

    

    @Override
    public Iterator solveIteratorForTopic(Topic topic, TopicMap topicmap, Iterator oldIterator) {
        Iterator it = oldIterator;
        if(topic != null && topicmap != null) {
            try {
                if(topic instanceof LayeredTopic) {
                    collection = ((LayeredTopic) topic).getTopicsForSelectedLayer();
                }
                else {
                    //System.out.println("topic:" + topic.getClass().getName());
                    
                    ArrayList list = new ArrayList();
                    list.add(topic);
                    collection = list;
                    //collection = null;
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                collection=null;
            }
            if(collection != null) {
                it = collection.iterator();
            }
        }
        return it;
    }
}
