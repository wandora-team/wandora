/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * TopicIterator.java
 *
 * Created on 13. huhtikuuta 2006, 10:05
 *
 */

package org.wandora.application.contexts.iterators;


import org.wandora.application.*;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public abstract class TopicIterator implements Iterator {

    Iterator source = null;
    TopicMap topicmap = null;
    Topic topic = null;       
    Object next = null;
    Iterator iterator = null;
    Collection collection = null;
    Collection cache = null;

    boolean removeDuplicates = true;
            


    public TopicIterator() {
    }

    
    public void initialize(Iterator source, Wandora admin) {
        this.source = source;
        this.topicmap = admin.getTopicMap();

        if(removeDuplicates) {
            cache = new ArrayList();
            next = solveNextUncached();
        }
        else {
            next = solveNext();
        }
    }
    
    
    
    
    
    @Override
    public boolean hasNext() {
        if(next != null) return true;
        else return false;
    }

    @Override
    public Object next() {
        Object current = next;
        next = removeDuplicates ? solveNextUncached() : solveNext();
        return current;
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();            
    }

    
    
    
    public void removeDuplicates(boolean should) {
        this.removeDuplicates = should;
    }
    


    // -------------------------------------------------------------------------
    // ----------------------------------------------- HIDDEN IMPLEMENTATION ---
    // -------------------------------------------------------------------------




    private Object solveNextUncached() {
        Object nextUncached = null;
        do {
            nextUncached = solveNext();
        }
        while(cache.contains(nextUncached) && nextUncached != null);
        if(nextUncached != null) cache.add(nextUncached);
        return nextUncached;
    }


    private Object solveNext() {
        Iterator iterator = solveIterator();
        if(iterator != null && iterator.hasNext()) return iterator.next();
        else return null;
    }



    private Iterator solveIterator() {
        while(iterator == null || !iterator.hasNext()) {
            if(source != null && source.hasNext()) {
                topic = (Topic) source.next();
                while(topic == null && source.hasNext()) topic = (Topic) source.next();
                iterator = solveIteratorForTopic(topic, topicmap, iterator);
            }
            else {
                break;
            }
        }
        return iterator;
    }



    // -------------------------------------------------------------------------
    // ----------- Overwrite next method in your own topic element iterator! ---
    // -------------------------------------------------------------------------
    
    
    public Iterator solveIteratorForTopic(Topic topic, TopicMap topicmap, Iterator oldIterator) {
        Iterator it = oldIterator;
        return it;
    }


}

    
    

