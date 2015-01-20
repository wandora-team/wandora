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
 */


package org.wandora.utils.regexextractor.bag.things;



import org.wandora.utils.regexextractor.bag.*;
import java.io.*;
import java.util.Hashtable;




public interface Thing extends Serializable, Comparable {
    public static final String thingClassKey = "thing.class.name";
    
    public void init(Bag bag);
//    public void init(Bag bag, Hashtable data);
    
    public boolean isDirty();
    public void clearDirty();
    
    public Object add(Object object);
    public Object add(Object object, Object rule);
    public boolean contains(Object object);
    public boolean contains(Object object, Object rule);
    public int size();
    public int size(Object rule);
   
    public Object get(Object rule, Object[] params);
    public Object get(Object rule, Object param);
    public Object get(Object rule);
    public Object get();
    
    public boolean supportsRule(Object rule);
    public Object[] supportedRules();
    
    public void reset();
    public void reset(Object rule);
    
    public String toString();
    public String toString(Object rule);
    
    public Hashtable serialize();
    public Hashtable serialize(Object rule);
    public void deserialize(Hashtable table);
    
}

