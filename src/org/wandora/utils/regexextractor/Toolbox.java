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
 * Toolbox.java
 *
 * Created on February 21, 2001, 2:35 PM
 */

package org.wandora.utils.regexextractor;

import org.wandora.utils.regexextractor.LogWriter;
import org.wandora.utils.Textbox;
import org.wandora.utils.Rexbox;
import gnu.regexp.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import org.wandora.utils.*;


/**
 * @author  Aki Kivel� / akivela@gripstudios.com
 * @version 1.1 - 10.4.2001
 */




public class Toolbox extends Object {
 
        
    /** Creates new Toolbox */
    public Toolbox() {
    }




    
        
    
    // ----------------------------------------------------------- HASHTABLE ---
    
        
    public static void printHashtable(Hashtable hash) {
        String key = null;
        String value = null;
        for(Enumeration keys = hash.keys(); keys.hasMoreElements(); ) {
            key = (String) keys.nextElement();
            value = (String) hash.get(key);
            LogWriter.println("Hash: " + key + " = " + value);
        }
    }
    
    
    public static Hashtable cropHashtable(Hashtable hash, String cropString) {
        Hashtable croppedHash = new Hashtable();
        String croppedKey = null;
        String hashKey = null;
        for(Enumeration hashKeys=hash.keys(); hashKeys.hasMoreElements(); ) {
            try {
                hashKey = (String) hashKeys.nextElement();
                if(hashKey.startsWith(cropString)) {
                    croppedKey = hashKey.substring(cropString.length());
                    if(croppedKey != null && croppedKey.length() > 0) {
                        croppedHash.put(croppedKey, hash.get(hashKey));
                    }
                }
            }
            catch (Exception e) {
                LogWriter.println("Unable to modify key " + hashKey + " in hashtable!");
            }
        }
        return croppedHash;
    }
    
    
    
    public static Hashtable insertIntoKeys(Hashtable hash, String insertString) {
        Hashtable newHash = new Hashtable();
        String hashKey = null;
        for(Enumeration hashKeys=hash.keys(); hashKeys.hasMoreElements(); ) {
            try {
                hashKey = (String) hashKeys.nextElement();
                newHash.put(insertString + hashKey, hash.get(hashKey));
            }
            catch (Exception e) {
                LogWriter.println("Unable to modify key " + hashKey + " in hashtable!");
            }
        }
        return newHash;
    }
    
    
    
    
    
    public static Hashtable deserializeHash(Hashtable serialized, String delimiter) {
        Hashtable deserialized = new Hashtable();
        Hashtable innerHash = null;
        String hashKey = null;
        String firstKey = null;
        String secondKey = null;
        Object value = null;
        
        for(Enumeration hashKeys = serialized.keys(); hashKeys.hasMoreElements(); ) {
            try {
                hashKey = (String) hashKeys.nextElement();
                value = serialized.get(hashKey);
                firstKey = hashKey.substring(0, hashKey.indexOf(delimiter));
                secondKey = hashKey.substring(hashKey.indexOf(delimiter)+1);
                innerHash = (Hashtable) deserialized.get(firstKey);
                
                LogWriter.println("dbg","  deserializing [" + firstKey + "][" + secondKey + "]=[" + value + "]");
                
                if (innerHash == null) innerHash = new Hashtable();
                innerHash.put(secondKey, value);
                deserialized.put(firstKey, innerHash);
            }
            catch (Exception e) {
                LogWriter.println("ERR","Unable to deserialize hash key [" + hashKey + "].");
            }
        }
        return deserialized;       
    }
    

    
    
    
    public static String[][] hash2StringTable(Hashtable hash) {
        return hash2StringTable(hash, false);
    }
    
    
    
    public static String[][] hash2StringTable(Hashtable hash, boolean arrayOrder) {
        String [][] stringTable = null;
        if (hash != null) {
            if (arrayOrder) stringTable = new String[hash.size()][2];
            else stringTable = new String[2][hash.size()];
            String key = "";
            int counter = 0;
            for(Enumeration keys = hash.keys(); keys.hasMoreElements(); ) {
                try {
                    key = (String) keys.nextElement();
                    if (arrayOrder) {
                        stringTable[counter][0] = key;
                        stringTable[counter][1] = (String) hash.get(key);
                    }
                    else {
                        stringTable[0][counter] = key;
                        stringTable[1][counter] = (String) hash.get(key);
                    }
                }
                catch (Exception e) {
                    if (arrayOrder) {
                        stringTable[counter][0] = "";
                        stringTable[counter][1] = "";
                    }
                    else {
                        stringTable[0][counter] = "";
                        stringTable[1][counter] = "";
                    }
                }
                counter++;
            }
        }
        return stringTable;
    }
    
        
    
    
    public static boolean meaningless(String string) {
        return Textbox.meaningless(string);
    }


    
    
    
    
    
    
    
    
    
    
    
    
    public static String extractMoves(Vector words) {
        String moves = null;
        int index = Rexbox.whichMatch("(l|L|r|R)+", words);
        if (index >= 0) {
            moves = (String) words.elementAt(index);
            words.removeElementAt(index);
        }
        return moves;
    }
  
    
    public static String pickFirstLetters(Vector words) {
        StringBuffer letters = new StringBuffer();
        for (int i=0; i<words.size(); i++) {
            try {
                letters.append(((String) words.elementAt(i)).charAt(0));
            } catch (Exception e) {}
        }
        return letters.toString();
    }
  
      
  
  
    public static String getFirstInt(String[] s) {
        int index = getFirstIntIndex(s);
        if (index >= 0) return s[index];
        else return null;
    }
    public static int getFirstIntIndex(String[] s) {
        for (int i=0; i<s.length; i++) {
            try {
                if (Integer.parseInt(s[i]) > 0) { return i; }
            } catch (Exception e) { }
        }
        return -1;
    }
    
      
    
    public static boolean equalAsStrings(Object o1, Object o2) {
        if (o1 != null && o2 != null) {
            if (((String) o1).compareToIgnoreCase((String) o2) == 0) return true;
        }
        return false;
    }
    
       
    public static String getGameCode(String[] words) {
        String code = null;
        try { 
            int index = getFirstIntIndex(words);
            code = words[index];
            words[index] = null;
        } catch (Exception e) {}
        return code;
    }
    
    
    public static String getRandomGameCode() {
        return (new Long(System.currentTimeMillis())).toString();
    }


    
    
    //---------------------------------------------------------------------------- 
    
    
    
    public static String[] hashtableKeysToStringArray(Hashtable table) {
        String[] strings = null;
        if(table != null) {
            if(table.size() > 0) {
                strings = new String[table.size()];
                int i = 0;
                for(Enumeration keys=table.keys(); keys.hasMoreElements(); ) {
                    try {
                        strings[i] = (String) keys.nextElement();
                    }
                    catch (Exception e) {
                        LogWriter.println("Object not a String in hashtableKeysToStringArray. Skipping!");
                        strings[i] = "";
                    }
                    i++;
                }
            }
            else {
                strings = new String[0];
            }
        }
        return strings;
    }
    
    
    

    

    //----------------------------------------------------------------------------   
    
    /**
     * Method checks is given vector contains a given string. Method does not change
     * the vertor nor string given as parameters.
     * @param v The object container that is expected to contain the given string.
     * @param s The string method is seeking for.
     * @return true if the string was found in vector. Method returns false is the string
     * was not found.
     */
    public static boolean vectorContainsString(Vector v, String s) {
        if (v != null && s != null) {
            for (int i=0; i<v.size(); i++) {
                if (equalAsStrings(v.elementAt(i), s)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    
    /**
     * Method checks if given vector contains given string. If string is found
     * the string is removed from the vector. Note that vectors inner indexing
     * changes if string is found and object removed from vector. You should
     * not rely on indexes of vector!
     *
     * @param v Vector containing a list of strings used as a lookup table.
     * @param s The string we are seeking for.
     * @return true if string was found. Method return false if string was not
     * found. 
     */
    public static boolean vectorContainedString(Vector v, String s) {
        if (v != null && s != null) {
            for (int i=0; i<v.size(); i++) {
                if (equalAsStrings(v.elementAt(i), s)) {
                    v.removeElementAt(i);
                    return true;
                }
            }
        }
        return false;
    }

    
    
        
    /**
     * Method generates vector out of given object array.
     * @param a The given object array converted to a vector.
     * @return A vector that contains all the elements in object array. If array was null or contained no elements vector is empty. 
     */
    public static Vector arrayToVector(Object[] a) {
        Vector v = new Vector();
        if (a != null && a.length > 0) {
            for (int i=0; i<a.length; i++) {
                v.add(a[i]);
            }
        }
        return v;
    }
    
    
    
    
    /**
     * Method creates a string array from objects in given vector.
     * @param v The vector containing strings to be attached to the generated string.
     * @return Method returns an array containing string picked out of given vector. If vector contains no strings null is returned.
     */
    public static String[] vectorToArray(Vector v) {
        String[] a = null;
        if (v != null && v.size() > 0) {
            a = new String[v.size()];
            for (int i=0; i<v.size(); i++) {
                try {
                    a[i] = (String) v.elementAt(i);
                } catch (Exception e) { LogWriter.println("Object in vector is not string as expected (Toolbox.vectorToArray)"); }
            }
        }
        return a;
    }
    
    
    
    
    /**
     * Method creates an array from objects in given vector.
     * @param v The vector containing strings to be attached to the generated string.
     * @return Method returns an array containing string picked out of given vector.
     * If vector contains no strings null is returned.
     */
    public static Object[] vectorToUnspecifiedArray(Vector v) {
        Object[] a = null;
        if (v != null && v.size() > 0) {
            a = new Object[v.size()];
            for (int i=0; i<v.size(); i++) {
                a[i] = v.elementAt(i);
            }
        }
        return a;
    }
    

    
/**
     * Method creates a single string from objects in given vector.
     * A space character is put between each vector object.
     *
     * @param v The vector containing strings to be attached to the generated string.
     * @return Method returns a single string generated out of objects in vector.  
     */

    public static String vectorToString(Vector v) { return vectorToString(v, ", "); }
    public static String vectorToString(Vector v, String delimiter) { return vectorToString(v, delimiter, false); }
    public static String vectorToString(Vector v, String delimiter, boolean toUpper) {
        StringBuffer s = new StringBuffer("");
        if (v != null && v.size() > 0) {
            for (int i=0; i<v.size(); i++) {
                try {
                    if (toUpper) s.append(toUpper((String) v.elementAt(i)) + delimiter);
                    else s.append(((String) v.elementAt(i)) + delimiter);
                } catch (Exception e) { LogWriter.println("Object in vector is not string as expected (Toolbox.vectorToString)"); }
            }
            return s.toString().substring(0, s.length()-delimiter.length());
        }
        return s.toString();
    }
   

    // -------------------------------------------------------------------------
    
    
    public static Vector stringToVector(String string) { return stringToVector(string, " ,"); }
    public static Vector stringToVector(String string, String delimiters) {
        if (string != null && delimiters != null && string.length() > 0 && delimiters.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(string, delimiters);
            Vector stringVector = new Vector();
        
            while(tokenizer.hasMoreElements()) {
                stringVector.add(tokenizer.nextToken());
            }
            return stringVector;
        }
        return null;
    }
    
    
    
    
    
    public static Vector removeDuplicates(Vector v) {
        Vector newVector = new Vector();
        Object currentElement = null;
        if(v != null && v.size() > 0) {
            for(int i=0; i<v.size(); i++) {
                currentElement = v.elementAt(i);
                if(!newVector.contains(currentElement)) {
                    newVector.add(currentElement);
                }
            }
        }
        return newVector;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public static String toUpper(String word) {
        String newWord = null;
        if (word != null) {
            newWord = "";
            if (word.length() > 0) newWord = newWord + Character.toUpperCase(word.charAt(0));
            if (word.length() > 1) newWord = newWord + word.substring(1);
        }
        return newWord;
    }
    
    
    /**
     * Method returns a random number equal or bigger than zero but smaller than
     * given max number.
     *
     * @param max The maximun limit for generated random number.
     * @return Returns a random number generated by system.
     */
    public static int random(int max) {
        return (int) Math.round(Math.random() * max);
    }

    
    

    
    
    // -------------------------------------------------------------------------

    // --------------------------------------------------------------- DEBUG ---
      
  
    public static void main( String[] args ) {

    }
    




}
