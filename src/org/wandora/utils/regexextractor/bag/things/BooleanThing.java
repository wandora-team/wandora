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


import java.io.*;
import java.util.*;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.*;



public class BooleanThing extends ObjectThing implements Thing {

    public Object get(Object rule, Object[] params) { return get(); }    
    public Object get(Object rule) { return get(); }
    public Object get() {
        boolean reply = false;
        if (content != null) {
            if (content instanceof String) {
                if ("yes".compareToIgnoreCase((String) content) == 0) reply = true;
                else if ("true".compareToIgnoreCase((String) content) == 0) reply = true;
                else {
                    try {
                        if (Integer.parseInt((String) content) != 0) reply = true;
                    } catch (Exception e) {}
                }
            }
            else if (content instanceof Integer) {
                if (((Integer) content).intValue() != 0) reply = true;
            }
            else if (content instanceof Long) {
                if (((Long) content).longValue() != 0) reply = true;
            }
            else if (content instanceof Boolean) {
                reply = ((Boolean) content).booleanValue();
            }
        }
        return new Boolean(reply);
    }
    
    // -------------------------------------------------------------------------

    
    public void deserialize(Hashtable serialized) {
        if (serialized != null) {
            this.content = new Boolean((String) serialized.get("content"));
        }
    }
    
    
}
