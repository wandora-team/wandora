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


package org.wandora.utils.regexextractor.bag.things;


import java.io.*;
import java.util.*;
import java.lang.*;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.*;



public class TimeHash extends ObjectHash implements Thing {
    protected final static String YOUNGEST =  "GET_YOUNGEST";
    protected final static String OLDEST =    "GET_OLDEST";
    
    protected final static String CROPPED_YOUNGEST =  "GET_CROPPED_YOUNGEST";
    protected final static String CROPPED_OLDEST =    "GET_CROPPED_OLDEST";
    
    
    protected long initTime;
    

    
    public synchronized void init(Bag bag) {
        super.init(bag);
        this.initTime = System.currentTimeMillis();
    }
    
    
    
    // -------------------------------------------------------------------------

    
    public synchronized Object add(Object object) {
        if (content != null) {
            lastIndex = System.currentTimeMillis();
            if (firstIndex == 0) firstIndex = lastIndex;
            smudge(6);
            content.put(new Long(lastIndex), object);
            return object;
        }
        else return null;
    }
    

    
    // -------------------------------------------------------------------------
   
    
    
    public synchronized Object get(Object rule, Object[] params) {
        try {
            Object reply = super.get(rule, params);
            if (reply != null) return reply;
            else {
                if (rule instanceof String) {
                    String criteria = (String) rule;
                    if (CROPPED_YOUNGEST.equals(criteria)) return get(CROPPED_FIRST, params);
                    if (CROPPED_OLDEST.equals(criteria)) return get(CROPPED_LAST, params);                    
                }
            }
        }
        catch (Exception ex) {}
        return null;
    }

    
    
    public synchronized Object get(Object rule) {
        Object reply = super.get(rule);
        if (reply != null) { return reply; }
        else {  
            if (content != null && rule != null && content.size() > 0) {
                if (rule instanceof Long) {
                    return content.get(rule);
                }
                if (rule instanceof String) {
                    String criteria = (String) rule;
                    if (YOUNGEST.equals(criteria)) { return super.get(FIRST); }
                    if (OLDEST.equals(criteria)) {  return super.get(LAST); }
                }
            }
        }
        return null;
    }
    

        public Hashtable serialize() {
        Hashtable serialized = super.serialize();
        
        serialized.put("init.time", "" + initTime);
        serialized.put(thingClassKey, this.getClass().getName());
        
        return serialized;
    }
    
    
    // ---
    
    
    public void deserialize(Hashtable serialized) {
        super.deserialize(serialized);
        for(Enumeration keys=serialized.keys(); keys.hasMoreElements(); ) {
            try {
                String key = (String) keys.nextElement();
                String value = (String) serialized.get(key);
                if ("init.time".equals(key)) { initTime = Long.parseLong(value); }
            }
            catch (Exception e) {}
        }
    }
    
    
}
