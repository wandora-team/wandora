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



public class IntegerTimeHash extends IntegerHash implements Thing {
   
    protected final static String BIGGEST_INTEGER_DURING =    "GET_BIGGEST_INTEGER_DURING";
    protected final static String SMALLEST_INTEGER_DURING =   "GET_SMALLEST_INTEGER_DURING";
    protected final static String BIGGEST_STRING_INTEGER_DURING =     "GET_BIGGEST_STRING_INTEGER_DURING";
    protected final static String SMALLEST_STRING_INTEGER_DURING =    "GET_SMALLEST_STRING_INTEGER_DURING";
    protected final static String AVERAGE_DURING =                    "GET_AVERAGE_DURING";
    protected final static String SUM_DURING =                        "GET_SUM_DURING";
 
    
    
    public Object get(Object rule, Object[] params) {
        try {
            Object reply = super.get(rule, params);
            if (reply != null) return reply;
            else {
                if (rule instanceof String) {
                    String criteria = (String) rule;
                    if (BIGGEST_INTEGER_DURING.equals(criteria)) return get(CROPPED_BIGGEST_INTEGER, params);
                    if (SMALLEST_INTEGER_DURING.equals(criteria)) return get(CROPPED_SMALLEST_INTEGER, params);
                    if (BIGGEST_STRING_INTEGER_DURING.equals(criteria)) return get(CROPPED_BIGGEST_STRING_INTEGER, params);
                    if (SMALLEST_STRING_INTEGER_DURING.equals(criteria)) return get(CROPPED_SMALLEST_STRING_INTEGER, params);
                    if (AVERAGE_DURING.equals(criteria)) return get(CROPPED_AVERAGE, params);
                    if (SUM_DURING.equals(criteria)) return get(CROPPED_SUM, params);
                }
            }
        }
        catch (Exception ex) {}
        return null;
    }
    
    
    
    
    public Object get(Object rule) {
        Object reply = super.get(rule);
        
        if (reply != null) { return reply; }
        else if (rule instanceof String) {
            String criteria = (String) rule;
        }
        return null;
    }


}
