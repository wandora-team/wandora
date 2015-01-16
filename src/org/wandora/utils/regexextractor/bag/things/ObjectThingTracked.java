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

import org.wandora.utils.regexextractor.LogWriter;
import java.io.*;
import java.util.*;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.*;


public class ObjectThingTracked extends ObjectThing implements Thing {

    protected final static String ACCESS_TIME = "GET_ACCESS_TIME";
    protected final static String BIRTH_TIME = "GET_BIRTH_TIME";
    
    private long accessTime = System.currentTimeMillis(); 
    private long birthTime = System.currentTimeMillis();
    
    
    public void init(Bag bag) {
        super.init(bag);
        trackAccess();
    }
    
      
    // -------------------------------------------------------------------------
    
   
    public Object add(Object object) {
        trackAccess();
        return super.add(object);
    }
    
    public boolean contains(Object object) {
        trackAccess();
        return super.contains(object);
    }
    
    
    public int size() {
        trackAccess();
        return super.size();
    }
  
    // -------------------------------------------------------------------------

    
    public Object get(Object object, Object[] params) { trackAccess(); return super.get(object, params); } 
    
    
    public Object get(Object object) {
        try {
            if (object instanceof String) {
                String criteria = (String) object;
                if (ACCESS_TIME.equals(criteria)) {
                    return new Long(accessTime);
                }
                if (BIRTH_TIME.equals(criteria)) {
                    return new Long(birthTime);
                }
            }
        } 
        catch (Exception e) {}
        trackAccess();
        return super.get(object);
    }
    

    public Object get() { trackAccess(); return super.get(); }

    
    // -------------------------------------------------------------------------
    
    
    
    public void reset() {
        trackAccess();
        super.reset();
    }
    
    public void reset(Object object) {
        trackAccess();
        super.reset(object);
    }
    
    
    // -------------------------------------------------------------------------
    
    private void trackAccess() {
        accessTime = System.currentTimeMillis();
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public Hashtable serialize(Object rule) {
        return serialize();
    }
    
    
    public Hashtable serialize() {
        Hashtable serialized = super.serialize();
        serialized.put("access.time", "" + accessTime);
        serialized.put("birth.time", "" + birthTime);
        serialized.put(thingClassKey, this.getClass().getName());
        return serialized;
    }
   
    
    public void deserialize(Hashtable serialized) {
        if (serialized != null) {
            super.deserialize(serialized);
            try { this.accessTime = Long.parseLong((String) serialized.get("access.time")); }
            catch (Exception e) {
                LogWriter.println("Unable to deSerialize access time in ObjectThingTracked!");
            }
            
            try { this.birthTime = Long.parseLong((String) serialized.get("birth.time")); }
            catch (Exception e) {
                LogWriter.println("Unable to deSerialize birth time in ObjectThingTracked!");
            }
        }
    }
    
}
