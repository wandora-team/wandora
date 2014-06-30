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
 * GripCollections.java
 *
 * Created on 26. toukokuuta 2005, 12:37
 */

package org.wandora.utils;
import java.util.*;
/**
 * This class provides some methods that make it easier to create and convert
 * between collections, arrays and maps.
 *
 * @author olli
 */
public class GripCollections {
    
    /** Creates a new instance of GripCollections */
    public GripCollections() {
    }
    
    /**
     * Creates a Vector<T> from the T[] array.
     */
    public static <T> Vector<T> arrayToCollection(T[] array){
        return arrayToCollection(new Vector<T>(),array);
    }
    
    /**
     * Fills the given Collection with the elements of the given array and returns
     * the Collection.
     */
    public static <T,C extends Collection<? super T>> C arrayToCollection(C c,T[] array){
        for(T t : array){
            c.add(t);
        }
        return c;
    }
    
    /**
     * Fills the given Collection with the other parameters and returns
     * the Collection.
     */
    public static <T,C extends Collection<? super T>> C newCollection(C c,T ... objs){
        return arrayToCollection(c,objs);
    }
    /**
     * Creates a new Vector<T>, fills it with the parameters and returns it.
     */
    public static <T> Vector<T> newVector(T ... objs){
        return newCollection(new Vector<T>(),objs);
    }
    
    /**
     * Creates a new HashSet<T>, fills it with the parameters and returns it.
     */
    public static <T> HashSet<T> newHashSet(T ... objs){
        return newCollection(new HashSet<T>(),objs);
    }
    
    /**
     * Creates a new T[] array where T is the same class as cls parameter and fills
     * it with elements of the given collection and then returns it.
     */
    public static <T> T[] collectionToArray(Collection<? extends T> c,Class<T> cls){
        T[] ts=(T[])java.lang.reflect.Array.newInstance(cls,c.size());
        int ptr=0;
        for(T t : c){
            ts[ptr++]=t;
        }
        return ts;
    }
    
    /**
     * Adds items from the specified array to the map. Items with even index
     * are used as keys of the items following them in the array.
     */
    public static <K,V> Map<K,V> addArrayToMap(Map<K,V> map,Object[] items){
        for(int i=0;i<items.length-1;i+=2){
            map.put((K)items[i],(V)items[i+1]);
        }
        return map;
    }
    
    /**
     * Checks if there is at least one common element in the two specified
     * collections.
     */
    public static boolean collectionsOverlap(Collection a,Collection b){
        if(a.size()>b.size()){
            Collection c=a;
            a=b;
            b=c;
        }
        for(Object o : a){
            if(b.contains(o)) return true;
        }
        return false;
    }
    
}
