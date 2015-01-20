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
 *
 * Rexbox.java
 *
 * Created on July 23, 2001, 5:42 PM
 */

package org.wandora.utils;


import gnu.regexp.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;



/**
 *
 * @author  akivela
 */
public class Rexbox extends Object {

    /** Creates new Rexbox */
    public Rexbox() {
    }

    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static String[] replaceInAll(String[] strings, String regularExpression, String replacement) {
        String[] results = null;
        if (strings.length > 0) {
            results = new String[strings.length];
            for (int i=0; i<strings.length; i++) {
                results[i] = replace(strings[i], regularExpression, replacement);
            }
        }
        return results;
    }
    
        
    
    public static String replacer(String[] words) {
        String string = null;
        
        if (words.length > 0) {
            string = words[0];
            for (int i=1; i<words.length; i++) {
                string = replace(string, "\\%" + i + "\\%", words[i]);
            }
        }
        return string;
    }
    
    
    
    public static String replace(String string, String regularExpression, String replacement) {
        StringBuffer sb = new StringBuffer();
        
        // LogWriter.println("Replace: " + string + ", " + regularExpression + ", " + replacement);
        try {
            RE re = new RE(regularExpression);
            REMatchEnumeration ren = re.getMatchEnumeration(string);
            if (ren != null) {
                int beginIndex = 0;
                try {
                    REMatch rm = null;
                    while((rm = ren.nextMatch()) != null) {
                        sb.append(string.substring(beginIndex, rm.getStartIndex()));
                        sb.append(replacement);
                        // LogWriter.println("Replace: " + sb.toString()); }
                        beginIndex =  rm.getEndIndex();
                    }
                    sb.append(string.substring(beginIndex));
                } catch (NoSuchElementException e1) { sb.append(string.substring(beginIndex)); }
            }
            else return string;
        }
        catch (Exception e) { return string; }
        return sb.toString();
    }
    
    
   // ---------------------------------------------------------------------------
    public static boolean anyMatch(String word, Vector regularExpressions) {
        if (whichMatch(word, regularExpressions) >= 0) return true;
        else return false;
    }
    
    
    public static int whichMatch(String word, Vector regularExpressions) {
        if (word != null && regularExpressions != null) {
            for (int i=0; i<regularExpressions.size(); i++) {
                // LogWriter.println("Matching: " + word + " == " + (String) regularExpressions.elementAt(i));
                try {
                    if (match((String) regularExpressions.elementAt(i), word)) return i;
                } catch (Exception e) {}
            }
        }
        return -1;
    }
    
    
    public static boolean match(String regularExpression, String string) {
        try {
            RE re = new RE(regularExpression, RE.REG_ICASE);
            if (re.isMatch(string)) {
                // LogWriter.print("  " + string + " = " + regularExpression + " is ");
                // LogWriter.println("---------TRUE");
                return true;     
            }
        } catch (Exception e) { }
        return false;
    }
    
    public static String matchString(String regularExpression, String string) {
      try {
            RE re = new RE(regularExpression, RE.REG_ICASE);
            REMatch rm = re.getMatch(string); 
            return rm.substituteInto("$0");
        } catch (Exception e) {
            return null;
        }
    }
    

    
}
