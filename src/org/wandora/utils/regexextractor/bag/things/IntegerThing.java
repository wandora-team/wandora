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

import org.wandora.utils.regexextractor.LogWriter;
import java.io.*;
import java.util.*;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.*;



public class IntegerThing extends ObjectThing implements Thing {

    public Object get() {
        int reply = 0;
        if (content != null) {
            if (content instanceof String) {
                try {
                    reply = Integer.parseInt((String) content);
                } catch (Exception e) {
                    LogWriter.println("Unable to parse integer from a string within the integer thing! Illegal String!");
                }
            }
            else if (content instanceof Integer) {
                reply = ((Integer) content).intValue();
            }
            else if (content instanceof Long) {
                reply = ((Integer) content).intValue();
            }
            else if (content instanceof Boolean) {
                reply = ((Boolean) content).booleanValue() ? 1 : 0;
            }
        }
        return new Integer(reply);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void deserialize(Hashtable serialized) {
        if (serialized != null) {
            this.content = new Integer((String) serialized.get("content"));
        }
    }
    
}
