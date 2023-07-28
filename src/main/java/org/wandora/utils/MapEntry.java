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
 * MapEntry.java
 *
 * Created on 18. lokakuuta 2005, 14:45
 */

package org.wandora.utils;
import java.util.*;

/**
 * A simple class that implements java.util.Map.Entry. Needed to make own
 * Map implementations for entrySet method.
 * @author olli
 */
public class MapEntry<K,V> implements Map.Entry<K,V> {
    
    private K key;
    private V value;
    
    /** Creates a new instance of MapEntry */
    public MapEntry(K key,V value) {
        this.key=key;
        this.value=value;
    }
    
    public K getKey(){return key;}
    public V getValue(){return value;}
    public int hashCode(){
        return (key==null   ? 0 : key.hashCode()) ^
               (value==null ? 0 : value.hashCode()) ; // by api definition
    }
    public boolean equals(Object o){
        if(o instanceof Map.Entry){
            Map.Entry e=(Map.Entry)o;
            return (getKey()==null ?
                    e.getKey()==null : getKey().equals(e.getKey()))  &&
                   (getValue()==null ?
                    e.getValue()==null : getValue().equals(e.getValue()));
        }
        else return false;
    }
    public V setValue(V value){
        V old=this.value;
        this.value=value;
        return old;
    }
    
}
