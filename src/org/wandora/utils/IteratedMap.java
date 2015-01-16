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
 */
package org.wandora.utils;
import java.util.*;
import static org.wandora.utils.Tuples.*;

/**
 *
 * A Map implementation that always iterates over the entire set to find requested
 * items. While this is inefficient compared to a hash map, it has the advantage that
 * keys can be modified after they have been inserted in the map and still be found
 * later. In a hash map, the hash code of the key must not change after it has been
 * entered in the map.
 * 
 * Note that while the map does not allow two entries with equal keys to be inserted
 * in the map, it is possible to modify one key so that it becomes equal to another
 * key in the map after both have been inserted in the map. The behaviour of the
 * map will become somewhat strange if this is done.
 * 
 * @author olli
 */
public class IteratedMap<K,V> extends AbstractMap<K,V> {

    private ArrayList<T2<K,V>> data;
    
    public IteratedMap(){
        data=new ArrayList<T2<K,V>>();
    }
    
    public IteratedMap(Map<? extends K,? extends V> m){
        this();
        putAll(m);
    }
    
    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public V put(K key, V value) {
        for(int i=0;i<data.size();i++){
            T2<K,V> d = data.get(i);
            if(d.e1.equals(key)) {
                V old=d.e2;
                data.set(i, t2(key,value));
                return old;
            }
        }
        data.add(t2(key,value));
        return null;
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }
        @Override
        public int size() {
            return data.size();
        }
    }
    
    private class EntryIterator implements Iterator<Map.Entry<K,V>> {
        private int nextPos=0;
        public boolean hasNext() {
            return nextPos<data.size();
        }
        public Map.Entry<K, V> next() {
            return new Entry(nextPos++);
        }
        public void remove() {
            data.remove(--nextPos);
        }
    }
    private class Entry implements Map.Entry<K,V> {
        private int pos;
        public Entry(int pos){
            this.pos=pos;
        }
        public K getKey() {
            return data.get(pos).e1;
        }
        public V getValue() {
            return data.get(pos).e2;
        }
        public V setValue(V value) {
            T2<K,V> d=data.get(pos);
            data.set(pos, t2(d.e1,value));
            return d.e2;
        }
    }
}
