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
 * TopicHashMap.java
 *
 * Created on 30. toukokuuta 2007, 13:55
 *
 */

package org.wandora.topicmap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author olli
 */
public class TopicHashMap<K> implements Map<Topic,K> {
    
    public Map<Locator,Topic> siMap;
    public Map<String,Topic> bnMap;
    public Map<Locator,Topic> slMap;
    public Map<Topic,K> valueMap;
    
    
    /** Creates a new instance of TopicHashMap */
    public TopicHashMap() {
        clear();
    }

    public Collection<K> values() {
        return valueMap.values();
    }

    public int size() {
        return valueMap.size();
    }

    public Set<Topic> keySet() {
        return valueMap.keySet();
    }

    public boolean isEmpty() {
        return valueMap.isEmpty();
    }

    public boolean containsValue(Object value) {
        return valueMap.containsValue(value);
    }
    
    private Topic findInternalKey(Object t){
        if(t instanceof Topic) return findInternalKey((Topic)t);
        else return null;
    }
    private Topic findInternalKey(Topic t){
        Topic r=null;
        try{
            if(t.getBaseName()!=null) {
                r=bnMap.get(t.getBaseName());
                if(r!=null) return r;
            }
            for(Locator l : t.getSubjectIdentifiers()){
                r=siMap.get(l);
                if(r!=null) return r;
            }
            if(t.getSubjectLocator()!=null) {
                r=slMap.get(t.getSubjectLocator());
                if(r!=null) return r;
            }        
        }catch(TopicMapException tme){tme.printStackTrace();}
        return null;
    }

    public boolean containsKey(Object key) {
        if(findInternalKey(key)!=null) return true;
        else return false;
    }

    public void putAll(Map<? extends Topic, ? extends K> m) {
        for(Map.Entry<? extends Topic, ? extends K> e : m.entrySet()){
            put(e.getKey(),e.getValue());
        }
    }

    public K put(Topic t, K value) {
        Topic old=findInternalKey(t);
        K oldValue=null;
        if(old!=null) {
            if(old==t){
                return valueMap.put(t,value);
            }
            else {
                oldValue=remove(old);
            }
        }
        
        try{
            if(t.getBaseName()!=null) bnMap.put(t.getBaseName(),t);
            for(Locator l : t.getSubjectIdentifiers()){
                siMap.put(l,t);
            }
            if(t.getSubjectLocator()!=null) slMap.put(t.getSubjectLocator(),t);
            valueMap.put(t,value);
            return oldValue;
        }catch(TopicMapException tme){tme.printStackTrace();}
        return null;
    }

    public Set<Map.Entry<Topic, K>> entrySet() {
        return valueMap.entrySet();
    }

    public void clear() {
        siMap=new LinkedHashMap<Locator,Topic>();
        bnMap=new LinkedHashMap<String,Topic>();
        slMap=new LinkedHashMap<Locator,Topic>();
        valueMap=new LinkedHashMap<Topic,K>();        
    }

    public K remove(Object key) {
        Topic t=findInternalKey(key);
        if(t==null) return null;
        else {
            try{
                if(t.getBaseName()!=null) bnMap.remove(t.getBaseName());
                for(Locator l : t.getSubjectIdentifiers()){
                    siMap.remove(l);
                }
                if(t.getSubjectLocator()!=null) slMap.remove(t.getSubjectLocator());
            }catch(TopicMapException tme){tme.printStackTrace();}
            
            return valueMap.remove(t);
        }
    }

    public K get(Object key) {
        Topic t=findInternalKey(key);
        if(t!=null) return valueMap.get(t);
        else return null;
    }
    
}
