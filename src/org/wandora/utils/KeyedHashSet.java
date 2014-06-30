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
 * KeyedHashSet.java
 *
 * Created on 18. lokakuuta 2005, 15:45
 */

package org.wandora.utils;
import java.util.*;

/**
 *
 * @author olli
 */
public class KeyedHashSet<E> implements Set<E> {
    
    protected KeyedHashMap<E,Object> map;
    protected Delegate<String,E> keyMaker;
    
    /** Creates a new instance of KeyedHashSet */
    public KeyedHashSet(Delegate<String,E> keyMaker) {
        this.keyMaker=keyMaker;
        map=new KeyedHashMap<E,Object>(keyMaker);
    }
    
    public boolean add(E o){
        return map.put(o,o)==null;
    }
    public boolean addAll(Collection<? extends E> c){
        boolean ret=false;
        for(E e : c){
            ret^=add(e);
        }
        return ret;
    }
    public void clear(){
        map=new KeyedHashMap<E,Object>(keyMaker);        
    }
    public boolean contains(Object o){
        return map.containsKey(o);
    }
    public boolean containsAll(Collection<?> c){
        for(Object o : c){
            if(!map.containsKey(o)) return false;
        }
        return true;
    }
    public boolean equals(Object o){
        if(o instanceof Set){
            if(((Set)o).size()!=this.size()) return false;
            return this.containsAll((Set)o);
        }
        else return false;
    }
    public int hashCode(){
        int hashCode=0;
        for(E e : map.keySet()){
            hashCode+=e.hashCode();
        }
        return hashCode;
    }
    public boolean isEmpty(){
        return map.isEmpty();
    }
    public Iterator<E> iterator(){
        return map.keySet().iterator();
    }
    public boolean remove(Object o){
        return map.remove(o)!=null;
    }
    public boolean removeAll(Collection<?> c){
        boolean changed=false;
        for(Object o : c ){
            changed^=(map.remove(o)!=null);
        }
        return changed;
    }
    public boolean retainAll(Collection<?> c){
        throw new UnsupportedOperationException();
    }
    public int size(){
        return map.size();
    }
    public Object[] toArray(){
        Object[] a=new Object[size()];
        int ptr=0;
        for(E e : map.keySet()){
            a[ptr++]=e;
        }
        return a;
    }
    public <T> T[] toArray(T[] a){
        throw new RuntimeException("Not implemented");
    }
    
}
