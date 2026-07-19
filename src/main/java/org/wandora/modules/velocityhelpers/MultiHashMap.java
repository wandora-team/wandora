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
 * 
 *
 * SetHashMap.java
 *
 * Created on July 21, 2004, 9:24 AM
 */

package org.wandora.modules.velocityhelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author  olli
 */

/*
 Do not implement Map interface or extend classes implementing it. This class is not a map!
 In Map specification it is specifically said that "each key can map to at most one value."
 */
public class MultiHashMap<K, V> {
    
    private Map<K,Collection<V>> hm;
    
    /** Creates a new instance of MultiHashMap */
    public MultiHashMap() {
        hm=new HashMap<>();
    }
    
    public Collection<?> get(K key){
        Collection<V> c=hm.get(key);
        if(c==null) return new LinkedHashSet<>();
        else return c;
    }
    
    public boolean add(K key,V value){
        Collection<V> c=hm.get(key);
        if(c==null){
            c=new LinkedHashSet<V>();
            hm.put(key,c);
        }
        return c.add(value);
    }
    
    public boolean isKeyEmpty(K key){
        return hm.containsKey(key);
    }
    
    public boolean containsAt(K key,V value){
        Collection<V> c=hm.get(key);
        if(c==null) return false;
        else return c.contains(value);
    }
    
    public boolean clearKey(Object key){
        return (hm.remove(key)!=null);
    }
    
    public boolean remove(K key,V value){
        Collection<V> c=hm.get(key);
        if(c==null) return false;
        boolean ret=c.remove(value);
        if(c.isEmpty()) hm.remove(c);
        return ret;
    }
    
    public Set<Map.Entry<K,Collection<V>>> entrySet(){
        return hm.entrySet();
    }
    
    public Set<K> keySet(){
        return hm.keySet();
    }
    
}
