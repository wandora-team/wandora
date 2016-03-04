/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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

package org.wandora.utils;


import java.util.*;
import java.io.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.*;








public class XMLbox {

    
    
    public static Document getDocument( String contents ) {
        return getDocument(contents, null);
    }

    public static Document getDocument( String contents, String encoding ) {
        try {
            org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
    	    try {
                parser.setFeature( "http://xml.org/sax/features/validation", false);
                parser.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", false ); // NOTE THIS ADDED, HOPEFULLY NOBODY WANTED THIS METHOD DEFERRED?
                parser.setFeature( "http://apache.org/xml/features/dom/include-ignorable-whitespace", false );
                parser.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                parser.setFeature( "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            } catch (SAXException e) {
                //LogWriter.println("WRN", "WRN parse(S): Couldn't set XML parser feature: "+e.getMessage());
            }
            InputSource source = new InputSource( new StringReader( contents ) );
            if(encoding != null) source.setEncoding(encoding);
            parser.parse(source);
    	    Document doc = parser.getDocument();
            return doc;
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    
    public static Hashtable getAsHashtable(String content) {
        return getAsHashtable(content, null);
    }
    
    
    public static Hashtable getAsHashtable(String content, String encoding) {
        Hashtable xmlHash = new Hashtable();
        try {
            Document doc = getDocument(content, encoding);
            xmlHash = xml2Hash(doc);
        }
        catch (Exception e) {
            //LogWriter.println("ERR", "Unable to parse XML from content!");
        }
        return xmlHash;
    }
    

    
    public static Hashtable xml2Hash(org.w3c.dom.Document doc) {
        Hashtable xmlHash = new Hashtable();
        parse2Hashtable(doc.getDocumentElement(), "", xmlHash);
        return xmlHash;
    }
    
    public static String cleanForAttribute(String value){
        value=value.replace("&","&amp;");
        value=value.replace("\"","&quot;");
        return value;
    }
    
    public static String cleanForXML(String value){
        value=value.replace("&","&amp;");
        value=value.replace("<","&lt;");
        return value;
    }

    
    private static void parse2Hashtable(Node node, String key, Hashtable xmlHash) {
        NodeList nodes = node.getChildNodes();
        int numOfNodes = nodes.getLength();
        for( int nnum=0; nnum<numOfNodes; nnum++ ) {
            Node n = nodes.item(nnum);
            if( n.getNodeType()==Node.ELEMENT_NODE ) {
                String value = textValue((Element)n);
                int i = -1;
                String currentKey = null;
                do {
                    i++;
                    currentKey = key + "." + n.getNodeName() + "["+i+"]";
                } while(xmlHash.get(currentKey) != null);
                if(null == value) {
                    xmlHash.put(currentKey, "");
                    parseXML( n, currentKey, xmlHash );
                }
                else {
                    //System.out.println("parsed: "+currentKey+" == "+value);
                    xmlHash.put(currentKey, value);
                }
            }
            else {
                // ignoring whitespace TEXT_NODEs
            }
        }
    }
    
    
    
    
    public static String wrapHash2XML(Hashtable h) {
        return hash2XML(wrapHash(h, "."));
    }
    
    
    
    
    public static String hash2XML(Hashtable hash) {
        String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator");
        return prefix + hash2XML(hash, 0);
    }

    
    private static String hash2XML(Hashtable hash, int depth) {
        String br = System.getProperty("line.separator");
        if(br == null) br = "";
        String s = "";
        String tab = "";
        for(int i=0; i<depth; i++) {
            tab = tab + "   ";
        }
        for(Enumeration keys = hash.keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = hash.get(key);
            String keyStr = key.toString();
            int index = keyStr.indexOf("[");
            if(index > 0) keyStr = keyStr.substring(0, index);
            if(value instanceof Hashtable) {
                s = s + tab + "<" + keyStr + ">" + br + hash2XML((Hashtable) value, depth+1) + tab + "</" + keyStr + ">" + br;
            }
            else {
                s = s + tab + "<" + keyStr + ">" + cleanForXML(value.toString()) + "</" + keyStr + ">" + br;
            }
        }
        return s;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    

    public static String wrapHashMap2XML(HashMap h) {
        return hashMap2XML(wrapHashMap(h, "."));
    }
    
    
    
    
    public static String hashMap2XML(HashMap hash) {
        String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator");
        return prefix + hashMap2XML(hash, 0);
    }

    
    private static String hashMap2XML(HashMap hash, int depth) {
        String br = System.getProperty("line.separator");
        if(br == null) br = "";
        String s = "";
        String tab = "";
        for(int i=0; i<depth; i++) {
            tab = tab + "   ";
        }
        Set keys = hash.keySet();
        for(Iterator i = keys.iterator(); i.hasNext(); ) {
            Object key = i.next();
            Object value = hash.get(key);
            String keyStr = key.toString();
            int index = keyStr.indexOf("[");
            if(index > 0) keyStr = keyStr.substring(0, index);
            if(value instanceof HashMap) {
                s = s + tab + "<" + keyStr + ">" + br + hashMap2XML((HashMap) value, depth+1) + tab + "</" + keyStr + ">" + br;
            }
            else {
                s = s + tab + "<" + keyStr + ">" + cleanForXML(value.toString()) + "</" + keyStr + ">" + br;
            }
        }
        return s;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static Hashtable wrapHash(Hashtable hash, String delimiters) {
        Hashtable wrapped = new Hashtable();
        
        for(Enumeration keys = hash.keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            if(key instanceof String) {
                StringTokenizer address = new StringTokenizer((String) key, delimiters);
                Hashtable subhash = wrapped;
                String path = null;
                while(address.hasMoreTokens()) {
                    path = address.nextToken();
                    if(address.hasMoreTokens()) {
                        if(subhash.get(path) == null || !(subhash.get(path) instanceof Hashtable)) {
                            subhash.put(path, new Hashtable());
                        }
                        subhash = (Hashtable) subhash.get(path);
                    }
                }
                if(hash.get(key) != null) {
                    subhash.put(path, hash.get(key));
                }
            }
        }
        return wrapped;
    }
    
    
    
    
    public static Hashtable getAsHashTree(String content) {
        return getAsHashTree(content, null);
    }    
    public static Hashtable getAsHashTree(String content, String encoding) {
        Hashtable xmlHash = new Hashtable();
        try {
            Document doc = getDocument(content, encoding);
            xmlHash = xml2HashTree(doc);
        }
        catch (Exception e) {
            //LogWriter.println("ERR", "Unable to parse XML from content!");
        }
        return xmlHash;
    }
    
    
    // -------------
    
    
    public static HashMap wrapHashMap(HashMap hash, String delimiters) {
        HashMap wrapped = new LinkedHashMap();
        
        Set keys = hash.keySet();
        for(Iterator i = keys.iterator(); i.hasNext(); ) {
            Object key = i.next();
            if(key instanceof String) {
                StringTokenizer address = new StringTokenizer((String) key, delimiters);
                HashMap subhash = wrapped;
                String path = null;
                while(address.hasMoreTokens()) {
                    path = address.nextToken();
                    if(address.hasMoreTokens()) {
                        if(subhash.get(path) == null || !(subhash.get(path) instanceof HashMap)) {
                            subhash.put(path, new LinkedHashMap());
                        }
                        subhash = (HashMap) subhash.get(path);
                    }
                }
                if(hash.get(key) != null) {
                    subhash.put(path, hash.get(key));
                }
            }
        }
        return wrapped;
    }
    
    public static LinkedHashMap getAsHashMapTree(String content) {
        return getAsHashMapTree(content, null);
    }    
    public static LinkedHashMap getAsHashMapTree(String content, String encoding) {
        LinkedHashMap xmlHash = new LinkedHashMap();
        try {
            Document doc = getDocument(content, encoding);
            xmlHash = xml2HashMapTree(doc);
        }
        catch (Exception e) {
            //LogWriter.println("ERR", "Unable to parse XML from content!");
        }
        return xmlHash;
    }
    
    
    
    
    public static Hashtable xml2HashTree(org.w3c.dom.Document doc) {
        Hashtable xmlHash = new Hashtable();
        Node rootNode = doc.getDocumentElement();
        parseXML(rootNode, rootNode.getNodeName()+"[0]", xmlHash);
        return xmlHash;
    }
    
    
    
    
    private static void parseXML(Node node, String key, Hashtable xmlHash) {
        NodeList nodes = node.getChildNodes();
        int numOfNodes = nodes.getLength();
        for( int nnum=0; nnum<numOfNodes; nnum++ ) {
            Node n = nodes.item(nnum);
            if( n.getNodeType()==Node.ELEMENT_NODE ) {
                String value = textValue((Element)n);
                int i = -1;
                String currentKey = null;
                do {
                    i++;
                    currentKey = key + "." + n.getNodeName() + "["+i+"]";
                } while(xmlHash.get(currentKey) != null);
                if(null == value) {
                    xmlHash.put(currentKey, "");
                    parseXML( n, currentKey, xmlHash );
                }
                else {
                    //System.out.println("parsed: "+currentKey+" == "+value);
                    xmlHash.put(currentKey, value);
                }
            }
            else {
                // ignoring whitespace TEXT_NODEs
            }
        }
    }
    
    
    
    
    public static LinkedHashMap xml2HashMapTree(org.w3c.dom.Document doc) {
        LinkedHashMap xmlHash = new LinkedHashMap();
        Node rootNode = doc.getDocumentElement();
        parseXML(rootNode, rootNode.getNodeName()+"[0]", xmlHash);
        return xmlHash;
    }
    
    
    
    
    private static void parseXML(Node node, String key, HashMap xmlHash) {
        NodeList nodes = node.getChildNodes();
        int numOfNodes = nodes.getLength();
        for( int nnum=0; nnum<numOfNodes; nnum++ ) {
            Node n = nodes.item(nnum);
            if( n.getNodeType()==Node.ELEMENT_NODE ) {
                String value = textValue((Element)n);
                int i = -1;
                String currentKey = null;
                do {
                    i++;
                    currentKey = key + "." + n.getNodeName() + "["+i+"]";
                } while(xmlHash.get(currentKey) != null);
                if(null == value) {
                    xmlHash.put(currentKey, "");
                    parseXML( n, currentKey, xmlHash );
                }
                else {
                    //System.out.println("parsed: "+currentKey+" == "+value);
                    xmlHash.put(currentKey, value);
                }
            }
            else {
                // ignoring whitespace TEXT_NODEs
            }
        }
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    public static String naiveGetAsText(String content) {
        String str = content;
        try {
            str = str.replaceAll("\\<script.*?\\>.*?\\<\\/script\\>", " ");
            str = str.replaceAll("\\<.+?\\>", " ");
            str = str.replaceAll("\\<\\\\.+?\\>", " ");
            str = str.replaceAll("\\<.+?\\\\\\>", " ");
            str = HTMLEntitiesCoder.decode(str);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
    
    
    public static String getAsText(String content, String encoding) {
        String str = content;
        try {
            Document doc = getDocument(content, encoding);
            str = xml2Text(doc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
    
    
    
    public static String xml2Text(org.w3c.dom.Document doc) {
        StringBuffer sb = new StringBuffer("");
        xml2Text(doc.getDocumentElement(), sb);
        return sb.toString().trim();
    }
    
    
    
    
    private static void xml2Text(Node node, StringBuffer sb) {
        NodeList nodes = node.getChildNodes();
        int numOfNodes = nodes.getLength();
        for( int nnum=0; nnum<numOfNodes; nnum++ ) {
            Node n = nodes.item(nnum);
            if( n.getNodeType()==Node.ELEMENT_NODE ) {
                sb.append(" ");
                xml2Text( n, sb );
            }
            else if( n.getNodeType()==Node.TEXT_NODE || n.getNodeType()==Node.CDATA_SECTION_NODE ) {
                String bit = n.getNodeValue().trim();
                sb.append( bit );
                if(!bit.endsWith("\n")) sb.append( " " );
            }
        }
    }
    
        
    private static String textValue( Element e ) {
        StringBuffer text = new StringBuffer("");
        NodeList nl = e.getChildNodes();
        for( int i=0;i<nl.getLength();i++ ) {
            Node n = nl.item(i);
            if( n.getNodeType()==Node.TEXT_NODE || n.getNodeType()==Node.CDATA_SECTION_NODE ) {
                String bit = n.getNodeValue().trim();
                text.append( bit );
            }
            else {
                return null;
            }
        }
        return text.toString();
    }
    
    
    
    
    public static String cleanUp( String xml ) {
        org.w3c.tidy.Tidy tidy = null;
        String tidyContent = null;

        try {
            xml = HTMLEntitiesCoder.decode(xml);
            Properties tidyProps = new Properties();
            tidyProps.put("trim-empty-elements", "no");

            tidy = new org.w3c.tidy.Tidy();
            tidy.setConfigurationFromProps(tidyProps);
            tidy.setXmlOut(true);
            tidy.setXmlPi(true);
            tidy.setTidyMark(false);
            tidy.setWraplen(0);

            ByteArrayOutputStream tidyOutput = null;
            tidyOutput = new ByteArrayOutputStream();       
            tidy.parse(new ByteArrayInputStream(xml.getBytes()), tidyOutput);
            tidyContent = tidyOutput.toString();
        }
        catch(Error er) {
            er.printStackTrace();
        }
        return tidyContent;
    }
    
    
}
