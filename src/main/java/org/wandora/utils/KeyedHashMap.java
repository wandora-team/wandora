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
 * TopicMapMap.java
 *
 * Created on 18. lokakuuta 2005, 14:14
 */

package org.wandora.utils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A hash map where the key specified in the put and get methods is not really used as
 * the key but instead another object is derived from it which is then used as the key.
 * In some cases it would be useful to have the equals and hashCode methods behave
 * differently depending on the situation. For example you may
 * want to have the original behavior of equals method (that is the == check) but have
 * it check the actual contents of the object instead of object equality when you use it
 * as a key in a HashMap.</p>
 * <p>
 * To use this class you need to specify in constructor the function that transforms the key into
 * another object that has the new implementation for equals and hashCode methods. Typically
 * this object will wrap the original key and use some information in there.
 * Later you use get and put methods normally. The function transforming keys is
 * called automatically. 
 * </p>
 * 
 *
 * @author olli
 */
public class KeyedHashMap<K,V> implements Map<K,V> {    
    
    protected HashMap<Wrapper<K>,V> map;
    
    protected Delegate<? extends Object,K> keyMaker;
    
    /** Creates a new instance of TopicMapMap */
    public KeyedHashMap(Delegate<? extends Object,K> keyMaker) {
        this.keyMaker=keyMaker;
        map=new HashMap<Wrapper<K>,V>();
    }
    
    
    public Wrapper getWrapper(K k){
        return new Wrapper(k,keyMaker.invoke(k));
    }
    
    public void clear(){
        map.clear();
    }
    public boolean containsKey(Object key){
        return map.containsKey(getWrapper((K)key));
    }
    public boolean containsValue(Object value){
        return map.containsValue(value);
    }
    public Set<Map.Entry<K,V>> entrySet(){
        HashSet<Map.Entry<K,V>> ret=new HashSet<Map.Entry<K,V>>();
        for(Map.Entry<Wrapper<K>,V> e : map.entrySet()){
            ret.add(new MapEntry<K,V>(e.getKey().wrapped,e.getValue()));
        }
        return ret;
    }
    public boolean equals(Object o){
        if(o instanceof Map){
            return entrySet().equals(((Map)o).entrySet());
        }
        else return false;
    }
    public V get(Object key){
        return map.get(getWrapper((K)key));
    }
    public int hashCode(){
        int code=0;
        for(Map.Entry<K,V> e : entrySet()){
            code+=e.hashCode();
        }
        return code;
    }
    public boolean isEmpty(){
        return map.isEmpty();
    }
    public Set<K> keySet(){
        HashSet<K> ret=new HashSet<K>();
        for(Map.Entry<Wrapper<K>,V> e : map.entrySet()){
            ret.add(e.getKey().wrapped);
        }
        return ret;        
    }
    public V put(K key,V value){
        Wrapper<K> wrapper=getWrapper(key);
        if(wrapper.key==null) throw new NullPointerException("KeyedHashMap wrapper key is null");
        return (V)map.put(wrapper,value);
    }
    public void putAll(Map<? extends K,? extends V> t){
        for(Map.Entry e : t.entrySet()){
            put((K)e.getKey(),(V)e.getValue());
        }
    }
    public V remove(Object key){
        return map.remove(getWrapper((K)key));
    }
    public int size(){
        return map.size();
    }
    public Collection<V> values(){
        return map.values();
    }
}

class Wrapper<K> {
    public K wrapped;
    public Object key;
    public Wrapper(K wrapped,Object key){
        this.wrapped=wrapped;
        this.key=key;
    }
    public int hashCode(){
        if(key==null) return 0;
        return key.hashCode();
    }
    public boolean equals(Object o){
        if(o != null && o instanceof Wrapper && key != null) {
            return key.equals(((Wrapper)o).key);
        }
        else return false;
    }
}

