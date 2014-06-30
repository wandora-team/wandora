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
 * EasyHash.java
 *
 * Created on April 26, 2001, 11:57 AM
 */

package org.wandora.utils;

import java.util.HashMap;

/**
 * EasyHash is an extended HashMap with one added constructor and one
 * initialization method.
 *
 * The new constructor makes it possible to more easily create a new HashMap
 * instance. For example, this associates letters with numbers:
 *      <code>EasyHash eh=new EasyHash(new Object[] {
 *          "a",new Integer(1),
 *          "b",new Integer(2),
 *          "c",new Integer(3),
 *          "d",new Integer(4)
 *      });</code>
 *
 * @author  Olli Lyytinen
 * @see java.util.HashMap
 */

/*
 * EasyHash is an extended HashMap with one added costructor and one
 * initialization method.
 *
 * The new constructor makes it possible to more easily create a new HashMap
 * instance. For example, this associates letters with numbers:
 *      EasyHash eh=new EasyHash(new Object[] {
 *          "a",new Integer(1),
 *          "b",new Integer(2),
 *          "c",new Integer(3),
 *          "d",new Integer(4)
 *      });
 *
 * The setArray can be used to add values to this EasyHash in a similar way.
 * In both the constructor and setArray, the given array must contain an even
 * number of Object. If it doesen't, the last Object will be ignored.
 */

public class EasyHash<K,V> extends HashMap<K,V> {

    /**
     * Creates new EasyHash from an array of objects.
     *
     * @param a The objects to be fed to <code>setArray</code>.
     *          <code>a[n]</code> will be associated with <code>a[n+1]</code>.
     *          The number of elements in <code>a</code> should therefore be
     *          even. If it isn't, the last element will be ignored.
     */
    public EasyHash(Object ... a) {
        setArray(a);
    }
    
    /**
     * Adds the given objects to this EasyHash.
     *
     * @param a The objects to be added.
     *          <code>a[n]</code> will be associated with <code>a[n+1]</code>.
     *          The number of elements in <code>a</code> should therefore be
     *          even. If it isn't, the last element will be ignored.
     */
    public void setArray(Object ... a){
        for(int i=0;i+1<a.length;i+=2){
            this.put((K)a[i],(V)a[i+1]);
        }
    }

}
