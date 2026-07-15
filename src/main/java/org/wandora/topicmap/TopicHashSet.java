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
 * 
 *
 * TopicHashSet.java
 *
 * Created on 30. toukokuuta 2007, 14:15
 *
 */

package org.wandora.topicmap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
/**
 *
 * @author olli
 */
public class TopicHashSet implements Set<Topic> {
    
    private TopicHashMap<TopicHashSet> map;
    
    /** Creates a new instance of TopicHashSet */
    public TopicHashSet() {
        map=new TopicHashMap<>();
    }

    public TopicHashSet(Collection<? extends Topic> c){
        this();
        addAll(c);
    }

    public void clear() {
        map.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Iterator<Topic> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }

    public Object[] toArray() {
        return map.keySet().toArray();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean remove(Object o) {
        return (map.remove(o)!=null);
    }

    public <T> T[] toArray(T[] a) {
        return (T[])map.keySet().toArray(a);
    }

    public boolean addAll(Collection<? extends Topic> c) {
        boolean ret=false;
        for(Topic t : c){
            if(map.put(t,this)==null) ret=true;
        }
        return ret;
    }

    public boolean containsAll(Collection<?> c) {
        for(Object t : c){
            if(!map.containsKey(t)) return false;
        }
        return true;
    }

    public boolean removeAll(Collection<?> c) {
        boolean ret=false;
        for(Object t : c){
            if(map.remove(t)!=null) ret=true;
        }
        return ret;
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean add(Topic e) {
        return (map.put(e,this)==null);
    }
    
}
