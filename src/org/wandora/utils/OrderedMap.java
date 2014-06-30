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
 * 
 *
 * OrderedMap.java
 *
 * Created on August 24, 2004, 3:20 PM
 */

package org.wandora.utils;
import java.util.*;
/**
 * <p>
 * A map that keeps the order of entries in entrySet. Entries are returned in the order they are added.
 * Note that contains*, get, keySet and values methods use linear time so this class isn't good for
 * lookups.</p>
 *
 * <p>Java now has a built in better implementation of same functionality. 
 * You should use java.util.LinkedHashMap instead of this class.
 * </p>
 *
 * @author  olli
 */
public class OrderedMap implements Map {
    
    private Set entrySet;
    
    /** Creates a new instance of OrderedMap */
    public OrderedMap() {
        entrySet=new OrderedSet();
    }
    
    public OrderedMap(Object[] objects){
        this();
        for(int i=0;i+1<objects.length;i+=2){
            put(objects[i],objects[i+1]);
        }
    }
    
    public void clear() {
        entrySet.clear();
    }
    
    public boolean containsKey(Object key) {
        Iterator iter=entrySet.iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            if(e.getKey()==key) return true;
        }
        return false;
    }
    
    public boolean containsValue(Object value) {
        Iterator iter=entrySet.iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            if(e.getValue()==value) return true;
        }
        return false;
    }
    
    public Set entrySet() {
        return entrySet;
    }
    
    public Object get(Object key) {
        Iterator iter=entrySet.iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            if(e.getKey()==key) return e.getValue();
        }
        return null;
    }
    
    public boolean isEmpty() {
        return entrySet.isEmpty();
    }
    
    public Set keySet() {
        OrderedSet set=new OrderedSet();
        Iterator iter=entrySet.iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            set.add(e.getKey());
        }
        return set;        
    }
    
    public Object put(Object key, Object value) {
        Iterator iter=entrySet.iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            if(e.getKey()==key) {
                Object v=e.getValue();
                e.setValue(value);
                return v;
            }
        }
        entrySet.add(new OrderedEntry(key,value));
        return null;
    }
    
    public void putAll(Map t) {
        Iterator iter=t.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            put(e.getKey(),e.getValue());
        }
    }
    
    public Object remove(Object key) {
        Iterator iter=entrySet.iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            if(e.getKey()==key) {
                Object v=e.getValue();
                iter.remove();
                return v;
            }
        }
        return null;
    }
    
    public int size() {
        return entrySet.size();
    }
    
    public Collection values() {
        OrderedSet set=new OrderedSet();
        Iterator iter=entrySet.iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            set.add(e.getValue());
        }
        return set;        
    }
    
    private class OrderedSet extends ArrayList implements Set {
        public OrderedSet(){
            super();
        }
    }
    
    private class OrderedEntry implements Map.Entry {
        private Object key,value;
        public OrderedEntry(Object key,Object value){
            this.key=key; this.value=value;
        }
        
        public Object getKey() {
            return key;
        }
        
        public Object getValue() {
            return value;
        }
        
        public Object setValue(Object value) {
            Object old=this.value;
            this.value=value;
            return old;
        }
        public boolean equals(Object o){
            if(!(o instanceof Map.Entry)) return false;
            Map.Entry e=(Map.Entry)o;
            return (e.getKey()==null?this.getKey()==null:e.getKey().equals(this.getKey()))
                    &&
                    (e.getValue()==null?this.getValue()==null:e.getValue().equals(this.getValue()));
        }
        public int hashCode(){
            return (getKey()==null?0:getKey().hashCode())^(getValue()==null?0:getValue().hashCode());
        }
    }
}
