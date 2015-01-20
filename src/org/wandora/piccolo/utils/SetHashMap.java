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
 * 
 *
 * SetHashMap.java
 *
 * Created on July 21, 2004, 9:24 AM
 */

package org.wandora.piccolo.utils;
import java.util.*;
/**
 *
 * @author  olli
 */

/*
 Do not implement Map interface or extend classes implementing it. This class is not a map!
 In Map specification it is specifically said that "each key can map to at most one value."
 */
public class SetHashMap {
    
    private HashMap hm;
    
    /** Creates a new instance of MultiHashMap */
    public SetHashMap() {
        hm=new HashMap();
    }
    
    public Collection get(Object key){
        Collection c=(Collection)hm.get(key);
        if(c==null) return new ArrayList();
        else return c;
    }
    
    public boolean add(Object key,Object value){
        Collection c=(Collection)hm.get(key);
        if(c==null){
            c=new HashSet();
            hm.put(key,c);
        }
        return c.add(value);
    }
    
    public boolean isKeyEmpty(Object key){
        return hm.containsKey(key);
    }
    
    public boolean containsAt(Object key,Object value){
        Collection c=(Collection)hm.get(key);
        if(c==null) return false;
        else return c.contains(value);
    }
    
    public boolean clearKey(Object key){
        return (hm.remove(key)!=null);
    }
    
    public boolean remove(Object key,Object value){
        Collection c=(Collection)hm.get(key);
        if(c==null) return false;
        boolean ret=c.remove(value);
        if(c.isEmpty()) hm.remove(c);
        return ret;
    }
    
    public Set entrySet(){
        return hm.entrySet();
    }
    
    public Set keySet(){
        return hm.keySet();
    }
    
}
