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
 * JTMParser.java
 *
 * Created on May 18, 2009, 10:38 AM
 */



package org.wandora.topicmap.parser;


import org.wandora.topicmap.*;
import org.wandora.utils.Tuples.*;

import java.util.*;
import java.io.*;
import org.json.*;



/**
 * This class implements a JSON Topic Maps 1.0 (JTM) parser. Class reads given 
 * input stream or file and converts serialization to topics and associations.
 * As Wandora's Topic Maps model is limited, some parts of JTM serialization
 * are skipped.
 * 
 * About JTM see: http://www.cerny-online.com/jtm/1.0/
 *
 * @author akivela
 */


public class JTMParser {

    private static String STATIC_SI_BODY = "http://wandora.org/si/jtm-parser/generated/"; 
    
    
    private static boolean ASSOCIATION_TO_TYPE = true;
    
    private TopicMap topicMap = null;
    private TopicMapLogger logger = null;
    
    
    
    public JTMParser(TopicMap tm, TopicMapLogger logger) {
        this.topicMap = tm;
        if(logger != null) {
            this.logger = logger;
        }
        else {
            this.logger = tm;
        }
    }
    
    

    public void parse(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            parse(fis, "UTF-8");
            fis.close();
        }
        catch(Exception e) {
            logger.log(e);
        }
    }
    
    
    public void parse(InputStream ins) {
        parse(ins, "UTF-8");
    }
    
    
    public void parse(InputStream ins, String enc) {
        if(topicMap == null) {
            log("Warning: JTM parser has no Topic Map object for topics and associations. Aborting.");
            return;
        }
        try {
            StringBuilder input = new StringBuilder("");
            InputStreamReader insr = new InputStreamReader(ins, enc);
            BufferedReader in = new BufferedReader(insr);

            String line = null;
            do {
                line = in.readLine();
                if(line != null) {
                    input.append(line);
                }
            } while(line != null);

            in.close();
            parse(new JSONObject(input.toString()));
            postProcess();
        }
        catch(Exception e) {
            logger.log(e);
        }
    }

    
    
    public boolean areEqual(String key, Object o) {
        if(key == null || o == null) return false;
        return key.equalsIgnoreCase(o.toString());
    }
    


    public void postProcess() throws Exception {
        Iterator<Topic> topics = topicMap.getTopics();
        Topic t;
        HashMap<Topic,Locator> toBeRemoved = new HashMap();
        while(topics.hasNext()) {
            t = topics.next();
            if(t != null && !t.isRemoved()) {
                Collection<Locator> subjectIdentifiers = t.getSubjectIdentifiers();
                for(Locator subjectIdentifier : t.getSubjectIdentifiers()) {
                    if(subjectIdentifier.toExternalForm().startsWith(STATIC_SI_BODY)) {
                        if(subjectIdentifiers.size() > 1) {
                            toBeRemoved.put(t, subjectIdentifier);
                        }
                        else {
                            log("Warning: Can't remove temporary subject identifier. Probably the topic had no subject identifier in jtm.");
                        }
                    }
                }
            }
        }
        for(Topic topic : toBeRemoved.keySet()) {
            topic.removeSubjectIdentifier(toBeRemoved.get(topic));
        }
    }
    
    
    
    public void parse(JSONObject inputJSON) throws Exception {
        if(topicMap == null) {
            log("Warning: JTM parser has no Topic Map object for topics and associations. Aborting.");
            return;
        }
        Iterator keys = inputJSON.keys();
        Object key = null;
        Object value = null;
        logCount = 0;
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = inputJSON.get(key.toString());
                if("version".equals(key)) {
                    if(!"1.0".equals(value)) {
                        log("Warning: JTM version number is not equal to 1.0! Trying anyway...");
                    }
                }
                else if("item_type".equals(key)) {
                    if(areEqual("topicmap", value)) {
                        // PASS SILENTY
                    }
                    else if(areEqual("topic", value)) {
                        parseTopic(inputJSON);
                    }
                    else if(areEqual("association", value)) {
                        parseAssociation(inputJSON);
                    }
                    else {
                        log("Warning: Wandora's JTM parser does not support item_type '"+value.toString()+"'. Skipping.");
                    }
                }
                else if("topics".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray topicJSONArray = (JSONArray) value;
                        int length = topicJSONArray.length();
                        Object topicJSON = null;
                        for(int i=0; i<length; i++) {
                            topicJSON = topicJSONArray.get(i);
                            if(topicJSON != null && topicJSON instanceof JSONObject) {
                                parseTopic((JSONObject) topicJSON);
                            }
                        }
                    }
                }
                else if("associations".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray associationJSONArray = (JSONArray) value;
                        int length = associationJSONArray.length();
                        Object associationJSON = null;
                        for(int i=0; i<length; i++) {
                            associationJSON = associationJSONArray.get(i);
                            if(associationJSON != null && associationJSON instanceof JSONObject) {
                                parseAssociation((JSONObject) associationJSON);
                            }
                        }
                    }
                }
            }
        }
    }
    

    

    public void parseTopic(JSONObject topicJSON) throws Exception {
        Iterator keys = topicJSON.keys();
        Object key = null;
        Object value = null;
        Topic t = topicMap.createTopic();
        t.addSubjectIdentifier(new Locator(STATIC_SI_BODY+System.currentTimeMillis()+"-"+Math.floor(Math.random()*9999999)));
        
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = topicJSON.get(key.toString());
                if("subject_identifiers".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray siArray = (JSONArray) value;
                        Object si = null;
                        int length = siArray.length();
                        for(int i=0; i<length; i++) {
                            si = siArray.get(i);
                            if(si != null) {
                                String sis = si.toString();
                                if(sis != null && sis.length() > 0) {
                                    t.addSubjectIdentifier(new Locator(sis));
                                }
                            }
                        }
                    }
                }
                else if("subject_locators".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray slArray = (JSONArray) value;
                        Object sl = null;
                        int length = slArray.length();
                        for(int i=0; i<length; i++) {
                            sl = slArray.get(i);
                            if(sl != null) {
                                String sls = sl.toString();
                                if(sls != null && sls.length() > 0) {
                                    if(i<1) {
                                        t.setSubjectLocator(new Locator(sls));
                                    }
                                    else {
                                        log("Warning: Wandora supports only one subject locator in topic. Skipping subject locator '"+sl+"'.");
                                    }
                                }
                            }
                        }
                    }
                }
                else if("item_identifiers".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray iiArray = (JSONArray) value;
                        Object ii = null;
                        int length = iiArray.length();
                        for(int i=0; i<length; i++) {
                            ii = iiArray.get(i);
                            if(ii != null) {
                                String iis = ii.toString();
                                if(iis != null && iis.length() > 0) {
                                    log("Warning: Wandora doesn't support item identifiers. Making subject identifier out of item identifier '"+ii+"'.");
                                    t.addSubjectIdentifier(new Locator(iis));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        keys = topicJSON.keys();
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = topicJSON.get(key.toString());
                if("names".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray nameArray = (JSONArray) value;
                        Object name = null;
                        int length = nameArray.length();
                        for(int i=0; i<length; i++) {
                            name = nameArray.get(i);
                            if(name!= null && name instanceof JSONObject) {
                                parseTopicName((JSONObject) name, t);
                            }
                        }
                    }
                }
                else if("occurrences".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray occurrenceArray = (JSONArray) value;
                        Object occurrence = null;
                        int length = occurrenceArray.length();
                        for(int i=0; i<length; i++) {
                            occurrence = occurrenceArray.get(i);
                            if(occurrence!= null && occurrence instanceof JSONObject) {
                                parseOccurrence((JSONObject) occurrence, t);
                            }
                        }
                    }
                }
            }
        }
    }




    public void parseTopicName(JSONObject topicNameJSON, Topic t) throws Exception {
        Iterator keys = topicNameJSON.keys();
        Object key = null;
        Object value = null;
        boolean hasSetBasename = false; 
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = topicNameJSON.get(key.toString());
                if("value".equals(key)) {
                    if(hasSetBasename) {
                        log("Warning: Wandora supports only one base name. Skipping name '"+value+"'.");
                    }
                    else {
                        String bn = value.toString();
                        if(bn != null && bn.length() > 0) {
                            t.setBaseName(fixString(bn));
                            hasSetBasename = true;
                        }
                    }
                }
                else if("variants".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray variantArray = (JSONArray) value;
                        Object variant = null;
                        int length = variantArray.length();
                        for(int i=0; i<length; i++) {
                            variant = variantArray.get(i);
                            if(variant!= null && variant instanceof JSONObject) {
                                parseVariant((JSONObject) variant, t);
                            }
                        }
                    }
                }
                else if("type".equals(key)) {
                    log("Warning: Wandora doesn't support typed base names. Skipping.");
                }
                else if("scope".equals(key)) {
                    log("Warning: Wandora doesn't support scopes in base names. Skipping.");
                }
                else if("reifier".equals(key)) {
                    log("Warning: Wandora doesn't support reifiers in topic names. Skipping.");
                }
                else if("item_identifiers".equals(key)) {
                    log("Warning: Wandora doesn't support item identifiers in topic names. Skipping.");
                }
            }
        }
    }



    public void parseVariant(JSONObject variantJSON, Topic t) throws Exception {
        String variantName = null;
        HashSet<Topic> scope = new LinkedHashSet();
        Iterator keys = variantJSON.keys();
        Object key = null;
        Object value = null;
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = variantJSON.get(key.toString());
                if("value".equals(key)) {
                    variantName = fixString(value.toString());
                }
                else if("scope".equals(key)) {
                    if( value instanceof JSONArray ) {
                        JSONArray scopeArray = (JSONArray) value;
                        int length = scopeArray.length();
                        for(int i=0; i<length; i++) {
                            scope.add(getOrCreateTopic(scopeArray.get(i).toString()));
                        }
                    }
                    else {
                        scope.add(getOrCreateTopic(value.toString()));
                    }
                }
                else if("datatype".equals(key)) {
                    log("Warning: Wandora doesn't support datatypes in variant names. Skipping.");
                }
                else if("reifier".equals(key)) {
                    log("Warning: Wandora doesn't support reifiers in variant names. Skipping.");
                }
                else if("item_identifiers".equals(key)) {
                    log("Warning: Wandora doesn't support item_identifiers in variant names. Skipping.");
                }
            }
        }
        if(variantName != null && variantName.length() > 0 && scope.size() > 0) {
            if(t == null) {
                t = topicMap.createTopic();
                Locator l = TopicTools.createDefaultLocator();
                log("Warning: Adding topic default subject identifier '"+l.toExternalForm()+"'.");
                t.addSubjectIdentifier(l);
            }
            t.setVariant(scope, variantName);
        }
    }




    public void parseOccurrence(JSONObject occurrenceJSON, Topic t) throws Exception {
        String occurrenceText = null;
        Topic scope = null;
        Topic type = null;
        Iterator keys = occurrenceJSON.keys();
        Object key = null;
        Object value = null;
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = occurrenceJSON.get(key.toString());
                if("value".equals(key)) {
                    occurrenceText = fixString(value.toString());
                }
                else if("type".equals(key)) {
                    type = getOrCreateTopic(value.toString());
                }
                else if("scope".equals(key)) {
                    if( value instanceof JSONArray ) {
                        JSONArray scopeArray = (JSONArray) value;
                        int length = scopeArray.length();
                        for(int i=0; i<length; i++) {
                            if(scope != null) {
                                log("Warning: Wandora supports only one scope topic in occurrences. Skipping scope '"+scopeArray.getString(i));
                            }
                            else {
                                scope = getOrCreateTopic(scopeArray.get(i).toString());
                            }
                        }
                    }
                    else {
                        scope = getOrCreateTopic(value.toString());
                    }
                }
                else if("datatype".equals(key)) {
                    log("Warning: Wandora doesn't support datatypes in occurrences. Skipping.");
                }
                else if("reifier".equals(key)) {
                    log("Warning: Wandora doesn't support reifiers in occurrences. Skipping.");
                }
                else if("item_identifiers".equals(key)) {
                    log("Warning: Wandora doesn't support item_identifiers in occurrences. Skipping.");
                }
            }
        }
        if(occurrenceText != null && occurrenceText.length() > 0 && type != null && scope != null) {
            if(t == null) {
                t = topicMap.createTopic();
                Locator l = TopicTools.createDefaultLocator();
                log("Warning: Adding topic default subject identifier '"+l.toExternalForm()+"'.");
                t.addSubjectIdentifier(l);
            }
            t.setData(type, scope, occurrenceText);
        }
    }


    

    // -------------------------------------------------------- ASSOCIATIONS ---



    public void parseAssociation(JSONObject topicJSON) throws Exception {
        Association a = null;
        ArrayList<T2<String,String>> roles = new ArrayList<T2<String,String>>();
        String type = null;
        Iterator keys = topicJSON.keys();
        Object key = null;
        Object value = null;
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = topicJSON.get(key.toString());
                if("type".equals(key)) {
                    type = value.toString();
                }
                else if("roles".equals(key)) {
                    if(value != null && value instanceof JSONArray) {
                        JSONArray roleArray = (JSONArray) value;
                        T2<String,String> role = null;
                        Object roleObject = null;
                        int length = roleArray.length();
                        for(int i=0; i<length; i++) {
                            roleObject = roleArray.get(i);
                            if(roleObject != null && roleObject instanceof JSONObject) {
                                role = parseRoles((JSONObject) roleObject);
                                if(role != null) {
                                    roles.add(role);
                                }
                            }
                        }
                    }
                }
                else if("scope".equals(key)) {
                    log("Warning: Wandora doesn't support scopes in associations. Skipping.");
                }
                else if("reifier".equals(key)) {
                    log("Warning: Wandora doesn't support reifiers in associations. Skipping.");
                }
                else if("item_identifiers".equals(key)) {
                    log("Warning: Wandora doesn't support item_identifiers in associations. Skipping.");
                }
            }
        }
        if(type != null && roles.size() > 0) {
            if(ASSOCIATION_TO_TYPE && roles.size() == 2 && 
                    (type.endsWith("http://psi.topicmaps.org/iso13250/model/type-instance") || 
                     type.endsWith("http://www.topicmaps.org/xtm/1.0/core.xtm#class-instance"))) {
                Topic instanceTopic = null;
                Topic typeTopic = null;
                String player1 = roles.get(0).e1;
                String roleType1 = roles.get(0).e2;
                String player2 = roles.get(1).e1;
                String roleType2 = roles.get(1).e2;
                if(roleType1 != null &&
                        (roleType1.endsWith("http://psi.topicmaps.org/iso13250/model/instance") ||
                         roleType1.endsWith("http://www.topicmaps.org/xtm/1.0/core.xtm#instance"))) {
                    instanceTopic = getOrCreateTopic(player1);
                    typeTopic = getOrCreateTopic(player2);
                    instanceTopic.addType(typeTopic);
                }
                else {
                    instanceTopic = getOrCreateTopic(player2);
                    typeTopic = getOrCreateTopic(player1);
                    instanceTopic.addType(typeTopic);
                }
            }
            else {
                Topic typeTopic = getOrCreateTopic(type);
                Topic roleType = null;
                Topic rolePlayer = null;
                a = topicMap.createAssociation(typeTopic);
                for( T2<String,String> role : roles ) {
                    roleType = getOrCreateTopic(role.e2);
                    rolePlayer = getOrCreateTopic(role.e1);
                    a.addPlayer(rolePlayer, roleType);
                }
            }
        }
    }



    public T2<String,String> parseRoles(JSONObject rolesJSON) throws Exception {
        Iterator keys = rolesJSON.keys();
        String type = null;
        String player = null;
        Object key = null;
        Object value = null;
        while(keys.hasNext()) {
            key = keys.next();
            if(key != null) {
                value = rolesJSON.get(key.toString());
                if("type".equals(key)) {
                    type = value.toString();
                }
                else if("player".equals(key)) {
                    player = value.toString();
                }
                else if("reifier".equals(key)) {
                    log("Warning: Wandora doesn't support reifiers in roles. Skipping.");
                }
                else if("item_identifiers".equals(key)) {
                    log("Warning: Wandora doesn't support item_identifiers in roles. Skipping.");
                }
            }
        }
        if(type != null && player != null) {
            return new T2(player, type);
        }
        else {
            return null;
        }
    }



    // -------------------------------------------------------------- TOPICS ---



    public Topic getOrCreateTopic(String i) {
        Topic t = null;
        try {
            if(i.startsWith("si:")) {
                String si = i.substring(3);
                t = topicMap.getTopic(si);
                if(t == null) {
                    t = topicMap.createTopic();
                    if(t != null) {
                        t.addSubjectIdentifier(new Locator(si));
                    }
                }
            }
            else if(i.startsWith("sl:")) {
                String sl = i.substring(3);
                t = topicMap.getTopicBySubjectLocator(sl);
                if(t == null) {
                    t = topicMap.createTopic();
                    if(t != null) {
                        t.setSubjectLocator(new Locator(sl));
                    }
                }
            }
            else if(i.startsWith("ii:")) {
                String ii = i.substring(3);
                t = topicMap.getTopic(ii);
                if(t == null) {
                    t = topicMap.createTopic();
                    if(t != null) {
                        t.addSubjectIdentifier(new Locator(ii));
                    }
                }
            }
            else {
                t = topicMap.getTopic(i);
                if(t == null) {
                    t = topicMap.createTopic();
                    if(t != null) {
                        t.addSubjectIdentifier(new Locator(i));
                    }
                }
            }
        }
        catch(Exception e) {
            logger.log(e);
        }
        return t;
    }
    
    
    
    
    public String fixString(String str) {
        return str;
        /*
        
         // LOOKS LIKE JAVA HANDLES STRING FIXING AUTOMATICALLY. THUS COMMENTED!
        
        if(str == null || str.length()==0) return str;
        else {
            System.out.println("Fixing "+str);
            StringBuffer sb = new StringBuffer("");
            int length = str.length();
            int c0 = -1;
            int c1 = -1;
            for(int i=0; i<length; i++) {
                c0 = str.charAt(i);
                if(c0=='\\') {
                    if(i+1<length) {
                        c1 = str.charAt(i+1);
                        if(c1=='n') sb.append('\n');
                        else if(c1=='f') sb.append('\f');
                        else if(c1=='r') sb.append('\r');
                        else if(c1=='t') sb.append('\t');
                        else if(c1=='b') sb.append('\b');
                        else if(c1=='"') sb.append('"');
                        else if(c1=='\\') sb.append('\\');
                        else if(c1=='/') sb.append('/');
                        else if(c1=='u') {
                            if(i+5<length) {
                                try {
                                    String hexstr = str.substring(i+2,i+6);
                                    System.out.println("Found 'u"+hexstr+"'");
                                    int hex = -1;
                                    hex = Integer.parseInt(hexstr, 16);
                                    sb.append((char) hex);
                                }
                                catch(Exception e) {
                                    logger.log(e);
                                }
                                i+=4;
                            }
                        }
                        else {
                            sb.append((char) c1);
                        }
                        i++;
                    }
                }
                else {
                    sb.append((char) c0);
                }
                
            }
            return sb.toString();
        }
        */
    }
    
    
    
    
    // ---------------------------------------------------------------- LOGS ---

    
    private int maxLogs = 1000;
    private int logCount = 0;
    
    private void log(String str) {
        if(logCount<maxLogs) {
            logCount++;
            logger.log(str);
            if(logCount>=maxLogs) {
                logger.log("Silently ignoring rest of the logs...");
            }
        }
    }
    
    
    
}
