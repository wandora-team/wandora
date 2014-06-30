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


import org.wandora.utils.regexextractor.bag.*;
import java.util.*;
import java.lang.*;



public class ObjectHash extends AbstractThing implements Thing {
    public final static String FIRST =             "GET_FIRST";
    public final static String LAST =              "GET_LAST";
    public final static String ALL =               "GET_ALL";
    public final static String CROPPED_FIRST =     "GET_CROPPED_FIRST";
    public final static String CROPPED_LAST =      "GET_CROPPED_LAST";

    
    protected Bag bag;
    protected Hashtable content;    
    protected long lastIndex;
    protected long firstIndex;
    
    
    public synchronized void init(Bag bag) {
        content = new Hashtable();
        firstIndex = 0;
        lastIndex = -1;
        this.bag = bag;
    }
    
    
    // -------------------------------------------------------------------------
    
    public synchronized Object add(Object object, Object rule) {
        return add(object);
    }
    
    
    public synchronized Object add(Object object) {
        if (content != null) {
            content.put(new Long(++lastIndex), object);
            smudge(1);
            return object;
        }
        else return null;
    }
    
    
    public synchronized boolean contains(Object object, Object rule) {
        return contains(object);
    }
    
    
    public synchronized boolean contains(Object object) {
        if (content != null) return content.contains(object);
        else return false;
    }
    
    
    public synchronized int size(Object rule) {
        return size();
    }

    
    public synchronized int size() {
        if (content != null) return content.size();
        else return 0;
    }
    
    // -------------------------------------------------------------------------
    
    public synchronized Object get(Object rule, Object param) {
        return get(rule);
    }
    
    
    public synchronized Object get(Object rule, Object params[]) {
        try {
            if (rule instanceof String) {
                String criteria = (String) rule;
                if (CROPPED_FIRST.equals(criteria)) {
                    ObjectHash clone = (ObjectHash) this.clone();
                    clone.crop(params[0], params[1]);
                    return clone.get(FIRST);
                }
                if (CROPPED_LAST.equals(criteria)) {
                    ObjectHash clone = (ObjectHash) this.clone();
                    clone.crop(params[0], params[1]);
                    return clone.get(LAST);                    
                }
            }
        } 
        catch (Exception ex) {}
        return null;
    }
    
    
    public synchronized Object get(Object rule) {
        if (content != null && rule != null && content.size() > 0) {
            if (rule instanceof Long) {
                long index = ((Long) rule).longValue();
                return get(index);
            }
            if (rule instanceof Integer) {
                long index = (long) ((Integer) rule).intValue();
                return get(index);
            }
            if (rule instanceof String) {
                String criteria = (String) rule;
                if (FIRST.equals(criteria)) return get(firstIndex);
                if (LAST.equals(criteria)) return get(lastIndex);
                if (ALL.equals(criteria)) return asVector();
            }
        }
        return null;
    }
    
    
    public synchronized Object get() {
        return get(lastIndex);
    }

    
        
    
    // -------------------------------------------------------------------------
    
    
    public synchronized void reset() {
        content = new Hashtable();
        smudge(2);
        this.init(bag);
    }
    
    
    public synchronized void reset(Object rule) {
        if (content != null) {
            content.remove(rule);
            smudge(3);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected synchronized Vector asVector() {
        Object key;
        Object currentKey = null;
        long smallIndex = Long.MIN_VALUE;
        long currentIndex = Long.MAX_VALUE;
        long index;
        Vector vector = new Vector();
        
        for (Enumeration keys0 = content.keys(); keys0.hasMoreElements();) {
            currentIndex = Long.MAX_VALUE;
            for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
                key = keys.nextElement();
                try {
                    index = ((Long) key).longValue();
                    if (index < currentIndex && index > smallIndex) {
                        currentIndex = index;
                        currentKey = key;
                    }
                } catch (Exception e2) {}
            }
            smallIndex = currentIndex;
            vector.add(content.get(currentKey));
            keys0.nextElement();
        }
        return vector;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    protected synchronized Object get(int i) {
        return get((long) i);
    }
    
    
    protected synchronized Object get(long i) {
        if (i >= firstIndex && i <= lastIndex) {
            try {
                return content.get(new Long(i));
            } catch (Exception e0) {}
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    protected void crop(Object startObject, Object endObject) {
        try {
            long startIndex = ((Long) startObject).longValue();
            long endIndex = ((Long) endObject).longValue();
            this.crop(startIndex, endIndex);
        } catch (Exception e) {}
    }
    
    
    protected synchronized void crop(long startIndex, long endIndex) {
        long index;
        Vector croppedKeys = new Vector();
        Object key;
        long lastIndex = Long.MIN_VALUE;
        long firstIndex = Long.MAX_VALUE;
        
        for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
            key = keys.nextElement();
            try {
                index = ((Long) key).longValue();
                if (index < startIndex || index > endIndex) {
                    croppedKeys.add(key);
                }
                else {
                    if (index < firstIndex) firstIndex = index;
                    if (index > lastIndex) lastIndex = index;
                }
            } catch (Exception e2) {}
        }
        
        for (int i=0; i<croppedKeys.size(); i++) {
            content.remove(croppedKeys.elementAt(i));
        }
        smudge(4);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public String toString() {
        return toString(null);
    }
    
    
    
    public String toString(Object rule) {
        StringBuffer buffer = new StringBuffer(1000);
        String valueString = null;
        int counter = 0;
        
        if (content != null) {
            for (Enumeration keys = content.keys(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                Object value = content.get(key);

                if (value != null) { valueString = value.toString(); }
                else valueString = "null";

                buffer.append(key.toString() + "=" + valueString);
                if (++counter < content.size()) buffer.append(",");
            }
        }
        return buffer.toString();
    }
    
    
    // -------------------------------------------------------------------------
    
    public Hashtable serialize(Object rule) {
        return serialize();
    }
    
    
    public Hashtable serialize() {
        Hashtable serialized = new Hashtable();
        for(Enumeration keys=content.keys(); keys.hasMoreElements(); ) {
            try {
                Long index = (Long) keys.nextElement();
                String value = content.get(index).toString();
                serialized.put("index." + index, value);
            }
            catch (Exception e) {
            }
        }
        
        serialized.put("first.index", "" + firstIndex);
        serialized.put("last.index", "" + lastIndex);
        serialized.put(thingClassKey, this.getClass().getName());
        
        return serialized;
    }
    
    
    // ---
    
    
    public void deserialize(Hashtable serialized) {
        Hashtable newContent = new Hashtable();
        for(Enumeration keys=serialized.keys(); keys.hasMoreElements(); ) {
            try {
                String key = (String) keys.nextElement();
                String value = (String) serialized.get(key);
                if ("first.index".equals(key)) { firstIndex = Long.parseLong(value); }
                if ("last.index".equals(key)) { lastIndex = Long.parseLong(value); }
                if (key.startsWith("index.")) {
                    Long index = new Long(Long.parseLong(key.substring(6)));
                    newContent.put(index, value);                   
                }
            }
            catch (Exception e) {}
        }
        this.content = newContent;
    }
    
    
    
    
}
