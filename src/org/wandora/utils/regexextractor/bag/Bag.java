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
 */

package org.wandora.utils.regexextractor.bag;


import java.io.*;
import java.util.*;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.things.*;
import org.w3c.dom.*;






public interface Bag extends Serializable {
    public String INITIAL_THING_CLASS = "org.wandora.utils.regexextractor.bag.things.ObjectThing";
    
    
    public void init(); // must be called always after instantiation
    public void init( Object obj ); // can be anytime
    public boolean isDirty();
    public void clearDirty();
    
    
    public void merge(Object object) throws Exception;
    
    // -------------------------------------------------------------------------   
    
    public Class getDefaultThingClass();
    public void setDefaultThingClass(String thingClassName);
    
    // -------------------------------------------------------------------------
    
    public int size(Object key);
    public int size();
    
    public boolean keyContains(Object key, Object value);
    public Object whichKeyContains(Object value);
    
    public Vector machingKeys(String key);
    // corrected misspelled method name. TODO Should phase out machingKeys, when
    // all apps have replaced it with matchingKeys...
    public Vector matchingKeys(String key);
    // -------------------------------------------------------------------------
    
    public Object put(Object key, int value, Object rule); 
    public Object put(Object key, long value, Object rule);
    public Object put(Object key, Vector vector, Object rule);
    public Object put(Object key, Object value, Object rule);
    
    public Object put(Class thingClass, Object key, int value, Object rule);
    public Object put(Class thingClass, Object key, long value, Object rule);
    public Object put(Class thingClass, Object key, Vector vector, Object rule);
    
    public Object put(Class thingClass, Object key, Object value, Object rule);
    
    // -------------------------------------------------------------------------
    
    public Object put(Object key, int value); 
    public Object put(Object key, long value);
    public Object put(Object key, Vector vector);
    public Object put(Object key, Object value);
    public Object put(Object thingArray[][]);
    
    public Object put(Class thingClass, Object key, int value);
    public Object put(Class thingClass, Object key, long value);
    public Object put(Class thingClass, Object key, Vector vector);
    
    public Object put(Class thingClass, Object key, Object value);
    
    // -------------------------------------------------------------------------
    
    public Object add(Object key, Object value, Object rule);
    public Object add(Object key, int value, Object rule);
    public Object add(Object key, long value, Object rule);
    public Object add(Object key, Object value);
    public Object add(Object key, int value);
    public Object add(Object key, long value);
    
    // -------------------------------------------------------------------------
    
    public Enumeration keys();
    
    // values() removed for the same reason as get() below
//    public Collection values();
    
    // get() removed so that Things cannot be accessed directly, which complicates dirty flagging
//    public Object get(Object key);
    
    public Object getIn(Object key, Object rule);
    public Object getIn(Object key);
    
    public int getIntIn(Object key) throws Exception;
    public int getIntIn(Object key, Object rule) throws Exception;

    public long getLongIn(Object key) throws Exception;
    public long getLongIn(Object key, Object rule) throws Exception;
    
    public String getStringIn(Object key) throws Exception;
    public String getStringIn(Object key, Object rule) throws Exception;
    
    public boolean getBooleanIn(Object key) throws Exception;
    public boolean getBooleanIn(Object key, Object rule) throws Exception;

    // added for convenience - get common things and return given defaults, if not found in Bag.
    public boolean getBooleanDefault(Object key, boolean def);     
    public String getStringDefault(Object key, String def); 
    public int getIntDefault(Object key, int def); 
    public long getLongDefault(Object key, long def);
    
    // -------------------------------------------------------------------------
    
    public Class getThingClass(Object key);
    public boolean supportsRule(Object key, Object rule);
    public Object[] supportedRules(Object key);
    
    // -------------------------------------------------------------------------
    
    public void reset();
    public boolean reset(Object key, Object rule);
    public void reset(Object key);
    
    public Object remove(Object key); // @return The removed Object

    // -------------------------------------------------------------------------
    
    public void load(String inputFile) throws IOException;
    public void load(InputStreamReader input) throws IOException;
    public void load(Document initArgs) throws IOException;

    // -------------------------------------------------------------------------
    
    public void print();
    public String toString();
    public String toString(Object key);
    public String toString(Vector chosenKeys);
    
    
    // -------------------------------------------------------------------------
    
    public Hashtable serialize(Object rule);
    public Hashtable serialize();
    public void deserialize(Hashtable serialized,Object rule);
    public void deserialize(Hashtable serialized);
}

