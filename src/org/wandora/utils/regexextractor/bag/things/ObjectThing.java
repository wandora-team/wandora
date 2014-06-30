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
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.*;


public class ObjectThing extends AbstractThing implements Thing {
    
    protected final static String SIMPLE = "GET_SIMPLE";
    
    protected Object content;
    
    // -------------------------------------------------------------------------
    
    
    public Object add(Object object, Object rule) { return add(object); }
    public boolean contains(Object object, Object rule) { return contains(rule); }
    public int size(Object rule) { return size(); }
    
    
    
    public Object add(Object object) {
        this.content = object;
        smudge(1);
        return object;
    }
    
    public boolean contains(Object object) {
        if (object != null && object.equals(content)) return true;
        else return false;
    }
    
    public int size() {
        if (content != null) return 1;
        else return 0;
    }
    
    // -------------------------------------------------------------------------
    
    public Object get(Object rule, Object[] params) { return get(rule); }
    public Object get(Object rule, Object param) { return get(rule); }
    public Object get(Object rule) { return get(); }
    
    public Object get() { return content; }
    
    
    // -------------------------------------------------------------------------
    
    
    public void reset(Object param) { reset(); }
    
    
    public void reset() {
        content = null;
        smudge(2);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public String toString() {
        return this.toString(null);
    }
    
    
    
    public String toString(Object rule) {
        if (content != null) {
            if (content instanceof String) return (String) content;
            if (content instanceof Integer) return content.toString();
            if (content instanceof Long) return content.toString();
            return "[unknown class: " + content.getClass() + "]";
        }
        else return "null";
    }
    
    
    // -------------------------------------------------------------------------
    
    public Hashtable serialize(Object rule) {
        return serialize();
    }
    
    
    public Hashtable serialize() {
        Hashtable serialized = new Hashtable();
        if (content != null) {
            String contentClassName = content.getClass().getName();
            serialized.put("content", content);
            serialized.put("content.class.name", content.getClass().getName());
            serialized.put(thingClassKey, this.getClass().getName());
        }
        return serialized;
    }
    
    
    public void deserialize(Hashtable serialized) {
        this.content = null;
        if (serialized != null) {
            this.content = serialized.get("content");
        }
    }
    
}
