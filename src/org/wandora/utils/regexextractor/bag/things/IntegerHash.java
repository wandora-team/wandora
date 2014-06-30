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



public class IntegerHash extends ObjectHash implements Thing {
    
    protected final static String SUM =                       "GET_SUM";
    protected final static String AVERAGE =                   "GET_AVERAGE";
    protected final static String BIGGEST_INTEGER =           "GET_BIGGEST_INTEGER";
    protected final static String SMALLEST_INTEGER =          "GET_SMALLEST_INTEGER";
    protected final static String BIGGEST_STRING_INTEGER =    "GET_BIGGEST_STRING_INTEGER";
    protected final static String SMALLEST_STRING_INTEGER =   "GET_SMALLEST_STRING_INTEGER";

    protected final static String CROPPED_SUM =                       "GET_CROPPED_SUM";
    protected final static String CROPPED_AVERAGE =                   "GET_CROPPED_AVERAGE";
    protected final static String CROPPED_BIGGEST_INTEGER =           "GET_CROPPED_BIGGEST_INTEGER";
    protected final static String CROPPED_SMALLEST_INTEGER =          "GET_CROPPED_SMALLEST_INTEGER";
    protected final static String CROPPED_BIGGEST_STRING_INTEGER =    "GET_CROPPED_BIGGEST_STRING_INTEGER";
    protected final static String CROPPED_SMALLEST_STRING_INTEGER =   "GET_CROPPED_SMALLEST_STRING_INTEGER";
    
    
    
    public synchronized Object get(Object rule, Object[] params) {
        try {
            Object reply = super.get(rule, params);
            if (reply != null) return reply;
            else {
                if (rule instanceof String) {
                    String criteria = (String) rule;
                    if (CROPPED_SUM.equals(criteria)) {
                        IntegerHash clone = (IntegerHash) this.clone();
                        clone.crop(params[0], params[1]);
                        return clone.get(SUM);
                    }
                    if (CROPPED_AVERAGE.equals(criteria)) {
                        IntegerHash clone = (IntegerHash) this.clone();
                        clone.crop(params[0], params[1]);
                        return clone.get(AVERAGE);                    
                    }
                }
            }
        }
        catch (Exception ex) {}
        return null;
    }
    
    
    
    
    public synchronized Object get(Object rule) {
        Object reply = super.get(rule);
        
        if (reply != null) { return reply; }
        else if (rule instanceof String) {
            String criteria = (String) rule;
            if (SUM.equals(criteria)) {
                int sum = 0;
                Object key;
                for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
                    key = keys.nextElement();
                    try { sum = sum + getInt(key); }
                    catch (Exception e0) {
                        try { sum = sum + parseInt(key); }
                        catch (Exception e1) {}
                    }
                }
                return new Integer(sum);                
            }            
            
            
            if (AVERAGE.equals(criteria)) {
                int sum = 0;
                int counter = 0;
                Object key;
                for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
                    key = keys.nextElement();
                    try {
                        sum = sum + getInt(key);
                        counter++;
                    }
                    catch (Exception e0) {
                        try {
                            sum = sum + parseInt(key);
                            counter++;
                        } catch (Exception e1) {}
                    }
                }
                if (counter > 0) return new Integer(sum / counter);
                else return new Integer(0);
            }
            
            
            if (BIGGEST_INTEGER.equals(criteria)) {
                int biggest = Integer.MIN_VALUE;
                Object biggestKey = null;
                Object key;
                int value;
                for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
                    key = keys.nextElement();
                    try {
                        value = getInt(key);
                        if (value > biggest) {
                            biggest = value;
                            biggestKey = key;
                        }
                    } catch (Exception e) {}
                }
                return new Integer(biggest);
            }

            
            if (SMALLEST_INTEGER.equals(criteria)) {
                int smallest = Integer.MAX_VALUE;
                Object smallestKey = null;
                Object key;
                int value;
                for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
                    key = keys.nextElement();
                    try {
                        value = getInt(key);
                        if (value < smallest) {
                            smallest = value;
                            smallestKey = key;
                        }
                    } catch (Exception e) {}
                }
                return new Integer(smallest);
            }

            
            if (BIGGEST_STRING_INTEGER.equals(criteria)) {
                int biggest = Integer.MIN_VALUE;
                Object biggestKey = null;
                Object key;
                int value;
                for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
                    key = keys.nextElement();
                    try {
                        value = parseInt(key);
                        if (value > biggest) {
                            biggest = value;
                            biggestKey = key;
                        }
                    } catch (Exception e) {}
                }
                return new Integer(biggest);
            }

            
            if (SMALLEST_STRING_INTEGER.equals(criteria)) {
                int smallest = Integer.MAX_VALUE;
                Object smallestKey = null;
                Object key;
                int value;
                for (Enumeration keys = content.keys(); keys.hasMoreElements();) {
                    key = keys.nextElement();
                    try {
                        value = parseInt(key);
                        if (value < smallest) {
                            smallest = value;
                            smallestKey = key;
                        }
                    } catch (Exception e) {}
                }
                return new Integer(smallest);
            }
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected synchronized int getInt(Object key) throws Exception {
        return ((Integer) content.get(key)).intValue();
    }
    
    
    protected synchronized int parseInt(Object key) throws Exception {
        return Integer.parseInt((String) content.get(key));
    }

    
    
    // -------------------------------------------------------------------------
    
    
    
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
                    newContent.put(index, new Integer(Integer.parseInt(value)));                   
                }
            }
            catch (Exception e) {}
        }
        this.content = newContent;
    }
    
}
