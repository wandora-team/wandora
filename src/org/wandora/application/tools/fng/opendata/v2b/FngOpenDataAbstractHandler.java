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
 */
package org.wandora.application.tools.fng.opendata.v2b;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Tuples;

/**
 *
 * @author akivela
 */


public class FngOpenDataAbstractHandler {
    
    
    private String VALUE_KEY = "value";
    
    
    private String resourceURI = null;
    
    private Collection<Map<String,String>> titles = null;
    private Collection<Map<String,String>> types = null;
    private Collection<Map<String,String>> identifiers = null;
    private Collection<Map<String,String>> subjects = null;
    private Collection<Map<String,String>> creators = null;
    private Collection<Map<String,String>> dates = null;
    private Collection<Map<String,String>> formats = null;
    private Collection<Map<String,String>> rights = null;
    private Collection<Map<String,String>> publishers = null;
    private Collection<Map<String,String>> relations = null;
    private Collection<Map<String,String>> descriptions = null;
    
    
    
    public FngOpenDataAbstractHandler() {
        titles = new ArrayList<Map<String,String>>();
        identifiers = new ArrayList<Map<String,String>>();
        types = new ArrayList<Map<String,String>>();
        subjects = new ArrayList<Map<String,String>>();
        creators = new ArrayList<Map<String,String>>();
        dates = new ArrayList<Map<String,String>>();
        formats = new ArrayList<Map<String,String>>();
        rights = new ArrayList<Map<String,String>>();
        publishers = new ArrayList<Map<String,String>>();
        relations = new ArrayList<Map<String,String>>();
        descriptions = new ArrayList<Map<String,String>>();
    }
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void setResourceURI(String str) {
        str = str.replace(" ", "%20");
        resourceURI = str;
    }
    
    
    
    public String getResourceURI() {
        return resourceURI;
    }
    
    
    public String getResourceURIBase() {
        return "http://kokoelmat.fng.fi/app?si=";
    }
    

    
    // -------------------------------------------------------------------------
    
    
    public void addTitle(String str) {
        addTitle(str, null);
    }
    
    public void addTitle(String str, String lang) {
        if(str != null) {
            titles.add(makeLangMap(str, lang));
        }
    }
    
    public void addTypedTitle(String str, String type) {
        if(str != null) {
            titles.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getTitles() {
        return titles;
    }
    
    // -------------------------------------------------------------------------
    
    public void addType(String str) {
        addType(str, null);
    }
    
    public void addType(String str, String type) {
        if(str != null) {
            types.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getTypes() {
        return types;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addIdentifier(String str) {
        addIdentifier(str, null);
    }
    
    public void addIdentifier(String str, String type) {
        if(str != null) {
            identifiers.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getIdentifiers() {
        return identifiers;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addCreator(String str) {
        addCreator(str, (String) null);
    }
    
    public void addCreator(String str, String type) {
        if(str != null) {
            creators.add(makeTypeMap(str, type));
        }
    }
    
    public void addCreator(String str, String type, Map<String,String> additionalProperties) {
        if(str != null) {
        	Map<String,String> map = makeTypeMap(str, type);
            map.putAll(additionalProperties);
            creators.add(map);
        }
    }
    
    
    public void addCreator(String str, Map<String,String> additionalProperties) {
        if(str != null) {
        	Map<String,String> map = makeTypeMap(str, null);
            map.putAll(additionalProperties);
            creators.add(map);
        }
    }
    
    
    
    public Collection<Map<String,String>> getCreators() {
        return creators;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addDate(String str) {
        addDate(str, null);
    }
    
    public void addDate(String str, String type) {
        if(str != null) {
            dates.add(makeTypeMap(str, type));
        }
    }
    
    public void addDate(String str, String type, String description) {
        if(str != null) {
        	Map<String,String> map = makeTypeMap(str, type);
            map.put("description", description);
            dates.add(map);
        }
    }
    
    public void addDate(String str, String type, Map<String,String> additionalProperties) {
        if(str != null) {
        	Map<String,String> map = makeTypeMap(str, type);
            map.putAll(additionalProperties);
            dates.add(map);
        }
    }
    
    public Collection<Map<String,String>> getDates() {
        return dates;
    }
    
    // -------------------------------------------------------------------------
    
    public void addSubject(String str) {
        addSubject(str, null);
    }
    
    public void addSubject(String str, String type) {
        if(str != null) {
            subjects.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getSubjects() {
        return subjects;
    }
        
    // -------------------------------------------------------------------------
    
    
    public void addFormat(String str) {
        addFormat(str, null);
    }
    
    public void addFormat(String str, String type) {
        if(str != null) {
            formats.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getFormats() {
        return formats;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addRights(String str) {
        addRights(str, null);
    }
    
    public void addRights(String str, String type) {
        if(str != null) {
            rights.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getRights() {
        return rights;
    }
    
    // -------------------------------------------------------------------------
    
    
    public void addPublisher(String str) {
        addPublisher(str, null);
    }
    
    
    public void addPublisher(String str, String type) {
        if(str != null) {
            publishers.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getPublishers() {
        return publishers;
    }
    
    
    public String getDefaultPublisher() {
        return "Finnish National Gallery";
    }
    
    // -------------------------------------------------------------------------
    
    public void addRelation(String str) {
        addRelation(str, null);
    }
    
    public void addRelation(String str, String type) {
        if(str != null) {
            relations.add(makeTypeMap(str, type));
        }
    }
    
    public void addRelation(String str, String type, Map<String,String> additionalProperties) {
        if(str != null) {
        	Map<String,String> map = makeTypeMap(str, type);
            map.putAll(additionalProperties);
            relations.add(map);
        }
    }
    
    public Collection<Map<String,String>> getRelations() {
        return relations;
    }
    
    
    // -------------------------------------------------------------------------
    
    public void addDescription(String str) {
        addDescription(str, null);
    }
    
    public void addDescription(String str, String lang) {
        if(str != null) {
            descriptions.add(makeLangMap(str, lang));
        }
    }
    
    public void addTypedDescription(String str, String type) {
        if(str != null) {
            descriptions.add(makeTypeMap(str, type));
        }
    }
    
    public Collection<Map<String,String>> getDescriptions() {
        return descriptions;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    

    protected String getNameFor(Topic t) throws TopicMapException {
        return t.getDisplayName("fi");
    }
    
    protected String getNameFor(Topic t, String lang) throws TopicMapException {
        return t.getDisplayName(lang);
    }
    
    

    protected String getOccurrenceFor(Topic t, Topic type, String lang) throws TopicMapException {
        if(type != null) {
            String o = t.getData(type, lang);
            if(o != null) {
                o = o.replaceAll("\\<.+?\\>", "");
            }
            return o;
        }
        return null;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    protected void appendAsDCXMLStatement(String valueType, Collection<Map<String,String>> hashs, StringBuilder sb) {
        if(hashs != null && !hashs.isEmpty()) {
            for(Map<String,String> hash : hashs) {
                String value = encodeXMLValue(hash.get(VALUE_KEY));
                StringBuilder attributes = new StringBuilder("");
                for(String key : hash.keySet()) {
                    if(!key.equals(VALUE_KEY)) {
                        if(hash.get(key) != null) {
                            String val = encodeXMLAttribute(hash.get(key));
                            attributes.append(" "+key+"="+"\"" + val + "\"");
                        }
                    }
                }
                appendLine(sb, 2, "<dc:"+valueType+attributes.toString()+">"+value+"</dc:"+valueType+">");
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    protected void appendAsXMLStatement(String propertyUri, String valueType, Collection<Map<String,String>> hashs, StringBuilder sb) {
        if(hashs != null && !hashs.isEmpty()) {
            appendLine(sb, 2, "<dcds:statement dcds:propertyURI=\""+propertyUri +"\">");
            for(Map<String,String> hash : hashs) {
                String value = encodeXMLValue(hash.get(VALUE_KEY));
                StringBuilder attributes = new StringBuilder("");
                for(String key : hash.keySet()) {
                    if(!key.equals(VALUE_KEY)) {
                        if(hash.get(key) != null) {
                            String val = encodeXMLAttribute(hash.get(key));
                            attributes.append(" "+key+"="+"\"" + val + "\"");
                        }
                    }
                }
                appendLine(sb, 3, "<dcds:valueString" + attributes + ">" + value + "</dcds:valueString>");
            }
            appendLine(sb, 2, "</dcds:statement>");
        }
    }
    
    // -------------------------------------------------------------------------
    
    protected void appendAsTextStatement(String propertyUri, String valueType, Collection<Map<String,String>> hashs, StringBuilder sb) {
        if(hashs != null && !hashs.isEmpty()) {
            appendLine(sb, 2, "Statement (");
            appendLine(sb, 3, "PropertyURI ( dcterms:"+propertyUri+" )");
            for(Map<String,String> hash : hashs) {
                String str = encodeTextString(hash.get(VALUE_KEY));
                appendLine(sb, 3, "ValueString ( \""+str+"\" )" );
                
                for(String key : hash.keySet()) {
                    if(!key.equals(VALUE_KEY)) {
                        if(hash.get(key) != null) {
                            String val = encodeTextString(hash.get(key));
                            if(!"Language".equals(valueType)) { 
                                val = "\""+val+"\"";
                            }
                            appendLine(sb, 4, key+" ( "+val+" )" );
                        }
                    }
                }
            }
            appendLine(sb, 3, ")" );
            appendLine(sb, 2, ")");
        }
    }
    
    
    protected String encodeTextString(String str) {
        if(str != null) {
            str = str.replace("\\", "\\\\");
            str = str.replace("\"", "\\\"");
            str = str.replace("\n", "\\n");
            str = str.replace("\t", "\\t");
            str = str.replace("\r", "\\r");
        }
        return str;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    protected void appendAsJSONArray(String label, Collection<Map<String,String>> hashs, StringBuilder sb) {
        if(hashs != null && !hashs.isEmpty()) {
            appendLine(sb, 2, "\""+label+"\": [");
            for(Map<String,String> hash : hashs) {
                if(hash.size() <= 2) {
                    String val = encodeJSONString(hash.get(VALUE_KEY));
                    String key = label;
                    for(String hashKey : hash.keySet()) {
                        if(!hashKey.equals(VALUE_KEY) && hash.get(hashKey) != null) {
                            key = hash.get(hashKey);
                        }
                    }
                    appendLine(sb, 3, "{ \"" +key+"\": \""+val+"\" }," );
                }
                else {
                    appendLine(sb, 3, "{");
                    for(String key : hash.keySet()) {
                        if(hash.get(key) != null) {
                            String val = encodeJSONString(hash.get(key));
                            appendLine(sb, 4, "\"" +key+"\": \""+val+"\"," );
                        }
                    }
                    trimLastComma(sb);
                    appendLine(sb, 3, "},");
                }
            }
            trimLastComma(sb);
            appendLine(sb, 2, "],");
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected void trimLastComma(StringBuilder sb) {
        if(sb != null) {
            int l = sb.lastIndexOf(",");
            if(l > 0) {
                sb.deleteCharAt(l);
            }
        }
    }
    

    
    protected String encodeJSONString(String string) {
        if(string == null || string.length() == 0) {
             return "\"\"";
         }

         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         String       t;

         for(i = 0; i < len; i += 1) {
             c = string.charAt(i);
             switch (c) {
             case '\\':
             case '"':
                 sb.append('\\');
                 sb.append(c);
                 break;
             case '/':
 //                if (b == '<') {
                     sb.append('\\');
 //                }
                 sb.append(c);
                 break;
             case '\b':
                 sb.append("\\b");
                 break;
             case '\t':
                 sb.append("\\t");
                 break;
             case '\n':
                 sb.append("\\n");
                 break;
             case '\f':
                 sb.append("\\f");
                 break;
             case '\r':
                sb.append("\\r");
                break;
             default:
                 if(c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         return sb.toString();
    }
    
    
    
    protected String encodeJSONKey(String str) {
        if(str != null) {
            str = str.replaceAll("\\W", "_");
        }
        return str;
    }
    
    
    
    
    
    
    protected String encodeXMLValue(String str) {
        if(str != null) {
            str = str.replace("&", "&amp;");
            str = str.replace("<", "&lt;");
            str = str.replace(">", "&gt;");
        }
        return str;
    }
    
    protected String encodeXMLAttribute(String str) {
        if(str != null) {
            str = str.replace("\"", "\\\"");
            str = str.replace("\n", "");
            str = str.replace("\r", "");
            str = str.replace("\f", "");
            str = str.replace("\b", "");
        }
        return str;
    }
    
    
    
    
    
    protected String inject(String d, String str0, String str1) {
        if(str1 != null) {
            if(d != null) {
                return str1.replace("__1__", d);
            }
        }
        return str0;
    }
    
    
    
    protected String injectT2(Tuples.T2<String,String> d, String str1, String str2) {
        if(d != null && str1 != null && str2 != null) {
            if(d.e1 != null && d.e2 == null) {
                return str1.replace("__1__", d.e1);
            }
            if(d.e1 != null && d.e2 != null) {
                String str = str2.replace("__1__", d.e1);
                return str.replace("__2__", d.e2);
            }
        }
        return "";
    }
    
    
    protected void appendLine(StringBuilder sb, String str) {
        appendLine(sb, 0, str);
    }
    
    protected void appendLine(StringBuilder sb, int tabs, String str) {
        switch(tabs) {
            case 1: { sb.append("  "); break; }
            case 2: { sb.append("    "); break; }
            case 3: { sb.append("      "); break; }
            case 4: { sb.append("        "); break; }
            case 5: { sb.append("          "); break; }
        }
        sb.append(str);
        sb.append("\n");
    }
    
    
    protected String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {}
        return str;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
      protected Map<String,String> makeTypeMap(String value, String type) {
        Map<String,String> map = new LinkedHashMap<>();
        map.put(VALUE_KEY, value);
        if(type != null) map.put("type", type);
        return map;
    }  
    
    protected Map<String,String> makeLangMap(String value, String lang) {
    	Map<String,String> map = new LinkedHashMap<>();
        map.put(VALUE_KEY, value);
        if(lang != null) map.put("lang", lang);
        return map;
    }
    
    
    protected Map<String,String> makeMap(String value, String key, String val) {
        Map<String,String> map = new LinkedHashMap<>();
        map.put(VALUE_KEY, value);
        if(key != null && val != null) map.put(key, val);
        return map;
    }
    
    
    protected Map<String,String> makeMap(String value, String key1, String val1, String key2, String val2) {
        Map<String,String> map = new LinkedHashMap<>();
        map.put(VALUE_KEY, value);
        if(key1 != null && val1 != null) map.put(key1, val1);
        if(key2 != null && val2 != null) map.put(key2, val2);
        return map;
    }
    
    
    protected Map<String,String> makeMap(String value, String key1, String val1, String key2, String val2, String key3, String val3) {
        Map<String,String> map = new LinkedHashMap<>();
        map.put(VALUE_KEY, value);
        if(key1 != null && val1 != null) map.put(key1, val1);
        if(key2 != null && val2 != null) map.put(key2, val2);
        if(key3 != null && val3 != null) map.put(key3, val3);
        return map;
    }
    
}
