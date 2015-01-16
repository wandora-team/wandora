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
 * Options.java
 *
 * Created on November 1, 2004, 3:26 PM
 */

package org.wandora.utils;


import java.util.*;
import java.io.*;
import java.net.*;




/**
 * Options is a LinkedHashMap wrapper class. Options is used to store all
 * important settings in Wandora between use sessions. Options support XML
 * import and export. Import and export features use methods in XMLbox utility
 * class.
 * 
 * Options converts XML paths to simple dot notation strings.
 *
 * @author  akivela
 */
public class Options {
    
    private LinkedHashMap<String, String> options;
    private String resource;
    
    
    /**
     * Used to pick up all available options.
     * 
     * @return Map with all options.
     */
    public Map<String, String> asMap() {
        return options;
    }
    
    /** Creates a new instance of Options */
    public Options(String optionsResource) {
        resource = optionsResource;
        options = new LinkedHashMap();
        String optionsString = null;
        if(optionsResource.startsWith("http")) {
            //System.out.println("Reading options from URL '" + resource + "'.");
            try { optionsString = IObox.doUrl(new URL(resource)); }
            catch (Exception e) { e.printStackTrace(); }
        }
        else if(optionsResource.startsWith("file")) {
            try {
                String filename = resource.substring(7);
                //System.out.println("Reading options from file '" + filename + "'.");
                optionsString = IObox.loadFile(filename);
                //System.out.println("optionsString==" +optionsString);
            }
            catch (Exception e) { e.printStackTrace();  }
        }
        else {
            //System.out.println("Reading options from resource '" + resource + "'.");
            optionsString = IObox.loadResource(resource);
        }
        try { parseOptions(optionsString); }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    
    
    public Options() {
        options=new LinkedHashMap<String,String>();
    }
    
    
    public Options(Options opts) {
        options=new LinkedHashMap<String,String>();
        options.putAll(opts.asMap());
    }
    
    
    /**
     * Private method used to add indexes into an options key.
     * 
     * @param key
     * @param fixAlsoLast Should method add explicit index to the last path part also
     * @return Fixed key containing indexes.
     */
    private String fixIndexes(String key, boolean fixAlsoLast) {
        String[] parts = ((String) key).split("\\.");
        int endIndex = parts.length;
        if(parts != null && endIndex > 0) {
            key = "";
            String part = null;
            for(int i=0; i<endIndex; i++) {
                part = parts[i];
                if(fixAlsoLast || i < endIndex-1) {
                    if(part.lastIndexOf("[") == -1) {
                        part = part + "[0]";
                    }
                }
                if(i < endIndex-1) part = part + ".";
                key = key + part;
            }
        }
        return key;
    }
    
    private String fixIndexes(String key) {
        return fixIndexes(key, true);
    }
    
    

    /**
     * Returns value for the given key or defaultValue if key resolves no value.
     * 
     * @param key Object (
     * @param defaultValue
     * @return
     */
    public String get(String key, String defaultValue) {
        String v=get(key);
        if(v==null) return defaultValue;
        else return v;
    }
    
    /**
     * Returns value for the given key. If key has no prefix "options." it is
     * added to the key. Also, key is modified by fixIndexes method.
     * 
     * @param key String path for the returned value
     * @return String value stored in options with key or null if key resolves no value.
     */
    public String get(String key) {
        if(key != null) {
            try {
                if(!key.startsWith("options.")) key = "options." + key; 
                key = fixIndexes(key);
                //System.out.println("option request: "+key+" == "+options.get(key));
                return options.get(key);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * Shortcut method that returns options value as integer or 0 (zero) if
     * value can not be parsed to an integer.
     * 
     * @param key String representing key path of the returned integer value.
     * @return Integer number stored to options with key or 0.
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }
    
    /**
     * Shortcut method that returns options value as integer or defaultValue if
     * value can not be converted to an integer.
     * 
     * @param key String representing key path of the returned integer value.
     * @param defaultValue Integer number returned if key resolved value can not be
     * converted to an integer.
     * @return Integer number stored to options with key or defaultValue.
     */
    public int getInt(String key, int defaultValue) {
        String sint = get(key);
        if(sint != null) {
            try { int val = Integer.parseInt(sint); return val; }
            catch (Exception e) {}
        }
        return defaultValue;
    }
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }
    public double getDouble(String key, double defaultValue) {
        String sd = get(key);
        if(sd != null) {
            try { double val = Double.parseDouble(sd); return val; }
            catch (Exception e) {}
        }
        return defaultValue;
    }
    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }
    public float getFloat(String key, float defaultValue) {
        String sd = get(key);
        if(sd != null) {
            try { float val = Float.parseFloat(sd); return val; }
            catch (Exception e) {}
        }
        return defaultValue;
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        String s = get(key);
        if(s != null) {
            try { boolean val = Boolean.parseBoolean(s); return val; }
            catch (Exception e) {
                if("1".equals(s)) return true;
                if("0".equals(s)) return false;
            }
        }
        return defaultValue;
    }
    
    
    /**
     * Shortcut method to store integer numbers to options.
     * @param key
     * @param value Integer number that will be stored to options with key.
     */
    public void put(String key, int value) {
        put(key, "" + value);
    }
    
    /**
     * Shortcut method to store double numbers to options.
     * @param key
     * @param value Double number to be stored to options.
     */
    public void put(String key, double value) {
        put(key, "" + value);
    }
    
    /**
     * Shortcut method to store float numbers to options.
     * 
     * @param key
     * @param value Float number to be stored to options.
     */
    public void put(String key, float value) {
        put(key, "" + value);
    }
    
    /**
     * This is the actual put method every other put method uses. If key has no
     * "options." prefix, it is added to the key. If value is null then method
     * removes given key in options.
     *  
     * @param key String key
     * @param value String value of the key
     */
    public void put(String key, String value) {
        if(key != null) {
            if(!key.startsWith("options.")) key = "options." + key;
            key = fixIndexes(key);
            if(value == null) {
                options.remove(key);
            }
            else {
                options.put(key, value);
            }
        }
    }
    
    
    /**
     * Method iterates all values in options and returns first key i.e.
     * path that contains the key. If options contains no value string,
     * method returns null.
     * 
     * @param value String representing options value. 
     * @return String representing options path or null
     */
    public String findKeyFor(String value) {
        if(value != null && options.containsValue(value)) {
            Set<String> keys = options.keySet();
            String key = null;
            for(Iterator<String> i=keys.iterator();i.hasNext();) {
                key=i.next();
                if(value.equalsIgnoreCase(get(key))) return key;
            }
        }
        return null;
    }
    
    
    
    /**
     * Shortcut to discover boolean value of given options key.
     * 
     * @param key String representing options path
     * @return true if value is "true" or "a". Returns false otherwise.
     */
    public boolean isTrue(String key) {
        String val = get(key);
        if(val != null) {
            if("true".equalsIgnoreCase(val)) return true;
            if("1".equalsIgnoreCase(val)) return true;
        }
        return false;
    }
    
    
    /**
     * Shortcut method to discover boolean value of given options key.
     * 
     * @param key String representing options path
     * @return true if value is not null, neither "false" or "0". Returns false otherwise.
     */
    public boolean isFalse(String key) {
        String val = get(key);
        if(val != null) {
            if("false".equalsIgnoreCase(val)) return true;
            if("0".equalsIgnoreCase(val)) return true;
        }
        return false;
    }
    
    
    /**
     * Removes all key-value pairs that start with given key path string.
     * Method is used to clean up options.
     * 
     * @param path String representing options path.
     */
    public void removeAll(String path) {
        ArrayList<String> toBeDeletedKeys = new ArrayList<String>();
        if(!path.startsWith("options.")) path = "options."+path;
        path = this.fixIndexes(path, false);
        Set<String> keys = options.keySet();
        for(Iterator<String> i=keys.iterator(); i.hasNext(); ) {
            String key=i.next();
            if(key instanceof String) {
                String stringKey = (String) key;
                if(stringKey.startsWith(path)) {
                    toBeDeletedKeys.add(key);
                }
            }
        }
        for( String key : toBeDeletedKeys ) {
            options.remove(key);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------------------ IO ---
    // -------------------------------------------------------------------------
    
    /**
     * Parses given XML string and sets options to parsed content. This method
     * passes the parsing to parseOptions(String content, String encoding)
     * 
     * @param content String containing valid XML document.
     */
    public synchronized void parseOptions(String content) {
        parseOptions(content, null);
    }
    
    /**
     * Parses given XML string and sets options to parsed content.
     * @param content String containing valid XML document.
     * @param encoding String representing content encoding.
     */
    public synchronized void parseOptions(String content, String encoding) {
        options = XMLbox.getAsHashMapTree(content, encoding);
    }
    
    public synchronized void parseOptions(BufferedReader reader) throws IOException {
        String line=null;
        StringBuilder sb=new StringBuilder();
        while( (line=reader.readLine())!=null ){
            sb.append(line).append("\n");
        }
        parseOptions(sb.toString());
    }
   

    public void print() {
        Object key;
        Object value;
        if(options.size() == 0) {
            System.out.println("  no options available (size == 0)!");
        }
        Set keys = options.keySet();
        for(Iterator i=keys.iterator();i.hasNext();) {
            key = i.next();
            value = options.get(key.toString());
            System.out.println("  " + key + " == " + value);
        }
    }

    public void save(Writer out) throws IOException{
        String optionsXML = XMLbox.wrapHashMap2XML(options);
        out.write(optionsXML);
        out.flush();
    }

    public void save() {
        if(resource.startsWith("http:")) { return; }
        else if(resource.startsWith("file:")) { 
            try {
                String filename=IObox.getFileFromURL(resource);
//                String filename = resource.substring(7);
                try { IObox.moveFile(filename, filename + ".bak"); } catch (Exception e) { e.printStackTrace(); }
                String optionsXML = XMLbox.wrapHashMap2XML(options);
                IObox.saveFile(filename, optionsXML);
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        else {
            String path = "./resources/";
            String resourcePath = path + resource;
            String optionsXML = XMLbox.wrapHashMap2XML(options);
            try { IObox.moveFile(resourcePath, resourcePath + ".bak"); } catch (Exception e) { e.printStackTrace(); }
            try { IObox.saveFile(resourcePath, optionsXML); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public Collection<String> keySet(){
        ArrayList<String> copy=new ArrayList<String>();
        copy.addAll(options.keySet());
        return copy;
    }
    
    
}
