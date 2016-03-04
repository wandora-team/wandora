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
 * 
 *
 * MultiHashMap.java
 *
 * Created on March 8, 2002, 6:31 PM
 */

package org.wandora.utils;
import java.util.*;
/**
 * 09.05.2014 AK: Commented method boolean remove(K key, V value)
 * 20.08.2011 AK: Commented method Collection<V> get(K key)
 * 11.01.2006 OL: Removed some synchronization and new TreeSet from keySet 
 *                no need for those according to specs but don't know if something
 *                depended on them.
 * 06.10.2003 PH: added remove() 
 * 
 * @author  olli
 */
public class MultiHashMap<K,V> extends HashMap<K,Collection<V>> {

    /** Creates new MultiHashMap */
    public MultiHashMap() {
        super();
    }

    public void addUniq(K key, V value) {
        if (!containsAt(key, value)) {
            add(key, value);
        }
    }
    
    public void add(K key, V value){
        Collection<V> c = super.get(key);
        if(c==null){
            c=new ArrayList();
            c.add(value);
            super.put(key,c);
        }
        else{
            c.add(value);
        }
    }
    
    

    /*
    public boolean remove(K key, V value) {
        boolean rval = false;
        if (containsAt(key, value)) {
            Collection<V> c = get(key);
            rval = c.remove(value);
            if (c.isEmpty()) super.remove(key);
        }
        return rval;
    }
    */
    

    /*
    public Collection<V> get(K key){
        return super.get(key);
    }
    */
    
    public void reset() {
        super.clear();
    }
    
    public int totalSize(){
        int count=0;
        for(K key : keySet()){
            Collection<V> c=super.get(key);
            if(c!=null) count+=c.size();
        }
        return count;
    }
    
    public boolean containsAt(K key,V value){  
        Collection<V> c=get(key);
        if(c==null) return false;
        else return c.contains(value);
    }
    
    @Override
    public java.util.Set<K> keySet() {
        return super.keySet();
    }
    
}
