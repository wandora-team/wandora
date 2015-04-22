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
 */



package org.wandora.application.tools.extractors.facebook;


import org.wandora.topicmap.*;
import org.wandora.utils.Tuples.*;
import org.wandora.utils.*;


import java.util.*;
import java.io.*;
import java.net.*;
import org.json.*;
import org.wandora.application.tools.extractors.ExtractHelper;

/**
 *
 * @author akivela
 */
public class FacebookGraphParser {

    private static LinkedHashSet<URL> history = null;

    public static final String FACEBOOK_BASE_SI = "https://graph.facebook.com/";
    public static final String GENERAL_SI = "http://wandora.org/si/facebook/";

    private TopicMap tm = null;
    private TopicMapLogger logger = null;
    private int connectionDepth = 1;

    private static String accessToken = null;
    private static int progress = 1;
    private static boolean pagingConsumesDepth = false;
    private static boolean processPaging = false;
    
    private static long waitBetweenURLRequests = 100;




    public FacebookGraphParser(TopicMap tm, TopicMapLogger logger, int depth, JSONObject inputJSON) {
        this(tm, logger, depth);
        try {
            parse(inputJSON);
        }
        catch(Exception e) {
            log(e);
        }
    }




    public FacebookGraphParser(TopicMap tm, TopicMapLogger logger, int depth) {
        this(tm, logger);
        connectionDepth = depth;
    }



   

    public FacebookGraphParser(TopicMap tm, TopicMapLogger logger) {
        this.tm = tm;
        this.logger = logger;

        if(history == null) {
            clearHistory();
        }
    }



    // ------------------------------------------------------------- HISTORY ---


    
    public void clearHistory() {
        history = new LinkedHashSet<URL>();
    }

    public boolean inHistory(URL u) {
        if(history != null) {
            if(history.contains(u)) return true;
        }
        return false;
    }

    public void addToHistory(URL u) {
        if(history == null) clearHistory();
        history.add(u);
    }

    public int getHistorySize() {
        return (history == null ? 0 : history.size());
    }



    // -------------------------------------------------------------------------


    public void setConnectionDepth(int d) {
        connectionDepth = d;
    }

    public void setAccessToken(String at) {
        //System.out.println("accessToken set to "+at);
        this.accessToken = at;
    }


    // -------------------------------------------------------------------------


    public boolean errorDetected(JSONObject inputJSON) {
        try {
            if(inputJSON.has("error")) {
                JSONObject error = inputJSON.getJSONObject("error");
                if(error != null) {
                    log(error.getString("message")+" ("+error.getString("type")+")");
                    return true;
                }
            }
        }
        catch(Exception e) {
            // NOTHING HERE
        }
        return false;
    }


    public void takeNap(long napTime) {
        try {
            Thread.currentThread().sleep(napTime);
        }
        catch(Exception e) {}
    }


    // --------------------------------------------------------------- PARSE ---



    public ArrayList<Topic> parse(String id) throws Exception {
        if(id != null) {
            String url = FACEBOOK_BASE_SI+id+"?metadata=1";
            if(accessToken != null) url = url + "&access_token="+accessToken;
            return parse(new URL(url));
        }
        return null;
    }



    public ArrayList<Topic> parse(URL url) throws Exception {
        if(waitBetweenURLRequests > 0) {
            takeNap(waitBetweenURLRequests);
        }
        if(url != null && connectionDepth >= 0 && !logger.forceStop()) {
            String urlStr = url.toExternalForm();
            if(urlStr.indexOf("access_token=") == -1) {
                if(urlStr.indexOf("?") == -1) {
                    if(accessToken != null) {
                        url = new URL( urlStr + "?access_token="+accessToken+"&metadata=1");
                    }
                }
                else {
                    if(accessToken != null) {
                        url = new URL( urlStr + "&access_token="+accessToken);
                    }
                }
            }
            if(!inHistory(url)) {
                addToHistory(url);
                System.out.println("Parsing URL "+url);
                String in = IObox.doUrl(url);
                System.out.println("GOT "+in);
                JSONObject inputJSON = new JSONObject(in);
                return parse(inputJSON);
            }
            else {
                System.out.println("Rejecting already parsed URL "+url);
            }
        }
        return null;
    }




    public ArrayList<Topic> parse(JSONObject inputJSON) throws Exception {
        if(logger != null) {
            logger.setProgress(progress++);
        }
        if(tm == null) {
            log("Warning: Facebook graph parser has no Topic Map object for topics and associations. Aborting.");
            return null;
        }
        if(errorDetected(inputJSON)) return null;
        ArrayList<Topic> fbTopics = new ArrayList<Topic>();

        String type = robustJSONGet(inputJSON, "type");
        String id = robustJSONGet(inputJSON, "id");
        if(id != null) {
            Topic topic = getFBTopic(id, type);
            Topic typeTopic = getTypeTopic("node");
            topic.addType(typeTopic);
            typeTopic.addType(createFBTypeTopic());
            if(type != null) {
                typeTopic = getTypeTopic(type);
                topic.addType(typeTopic);
                typeTopic.addType(createFBTypeTopic());
            }
            Iterator keys = inputJSON.keys();
            Object key = null;
            Object value = null;
            boolean success = true;
            while(keys.hasNext() && !logger.forceStop()) {
                key = keys.next();
                if(key != null) {
                    String keyStr = key.toString();
                    value = inputJSON.get(keyStr);
                    if(logger != null) logger.hlog("Parsing '"+key+"' of '"+id+"'");
                    success = parseProperty(keyStr, value, topic, id, type);
                    if(!success) {
                        log("Warning: Unrecognized key '"+key+"' found in JSON ("+type+"). Using default conversion schema.");
                        setOccurrence(topic, keyStr, value);
                    }
                }
            }
            fbTopics.add(topic);
        }
        else {
            if(inputJSON.has("data")) {
                JSONArray data = inputJSON.getJSONArray("data");
                for(int i=0; i<data.length() && !logger.forceStop(); i++) {
                    JSONObject dataItem = data.optJSONObject(i);
                    boolean doSimpleParse = false;
                    if(!dataItem.has("type") && dataItem.has("id") && connectionDepth > 0) {
                        try {
                            FacebookGraphParser fbgp = new FacebookGraphParser(tm, logger, connectionDepth-1);
                            ArrayList<Topic> subTopics = fbgp.parse(dataItem.getString("id"));
                            if(subTopics != null) fbTopics.addAll(subTopics);
                        }
                        catch(Exception e) {
                            doSimpleParse = true;
                        }
                    }
                    else {
                        doSimpleParse = true;
                    }
                    if(doSimpleParse) {
                        ArrayList<Topic> subTopics = parse(dataItem);
                        if(subTopics != null) fbTopics.addAll(subTopics);
                    }
                }
                if(inputJSON.has("paging")) {
                    if(processPaging) {
                        JSONObject pagingObj = inputJSON.getJSONObject("paging");
                        if(pagingObj.has("previous")) {
                            String previous = pagingObj.getString("previous");
                            if(previous != null) {
                                if(pagingConsumesDepth) {
                                    FacebookGraphParser fbgp = new FacebookGraphParser(tm, logger, connectionDepth-1);
                                    ArrayList<Topic> previousTopics = fbgp.parse(new URL(previous));
                                    if(previousTopics != null) fbTopics.addAll(previousTopics);
                                }
                                else {
                                    ArrayList<Topic> previousTopics = parse(new URL(previous));
                                    if(previousTopics != null) fbTopics.addAll(previousTopics);
                                }
                            }
                        }
                        if(pagingObj.has("next")) {
                            String next = pagingObj.getString("next");
                            if(next != null) {
                                if(pagingConsumesDepth) {
                                    FacebookGraphParser fbgp = new FacebookGraphParser(tm, logger, connectionDepth-1);
                                    ArrayList<Topic> nextTopics = fbgp.parse(new URL(next));
                                    if(nextTopics != null) fbTopics.addAll(nextTopics);
                                }
                                else {
                                    ArrayList<Topic> nextTopics = parse(new URL(next));
                                    if(nextTopics != null) fbTopics.addAll(nextTopics);
                                }
                            }
                        }
                    }
                }
            }
            
        }
        return fbTopics;
    }









    protected boolean parseProperty(String key, Object value, Topic t, String id, String type) throws Exception {
        if(areEqual("id", key)) {
            // PASS. ALREADY PROCESSED
        }
        else if(areEqual("name", key)) {
            String name = value.toString();
            if(name != null && name.length() > 0) {
                t.setBaseName(name+" ("+id+")");
                t = tm.getTopicWithBaseName(name+" ("+id+")"); // Ensuring we still have a valid topic
                t.setDisplayName("en", name);
            }
        }
        else if(areEqual("icon", key)) {
            associate(key, t, value);
        }
        else if(areEqual("link", key)) {
            associate(key, t, value);
        }
        else if(areEqual("type", key)) {
            String ty = value.toString();
            if(ty != null && ty.length() > 0) {
                if(type != null && !type.equalsIgnoreCase(ty)) {
                    log("Warning: creating user topic for a '"+ty+"'");
                }
                Topic typeTopic = getTypeTopic(ty);
                t.addType(typeTopic);
                typeTopic.addType(createFBTypeTopic());
            }
        }
        else if(areEqual("metadata", key)) {
            if(value != null && value instanceof JSONObject) {
                JSONObject metadataJSON = (JSONObject) value;
                if(metadataJSON.has("connections")) {
                    JSONObject connectionsJSON = metadataJSON.getJSONObject("connections");
                    if(connectionsJSON != null) {
                        parseConnections(connectionsJSON, t);
                    }
                }
            }
        }
        else if(areEqual("properties", key)) {
            if(value != null && value instanceof JSONArray) {
                JSONArray propertyArray = (JSONArray) value;
                for(int i=0; i<propertyArray.length(); i++) {
                    Object property = propertyArray.get(i);
                    String propertyId = ""+System.currentTimeMillis();
                    Topic propertyTopic = getTopic(propertyId);
                    if(property instanceof JSONObject) {
                        JSONObject jsonProperty = (JSONObject) property;
                        Iterator ks = jsonProperty.keys();
                        Object k = null;
                        Object v = null;
                        while(ks.hasNext() && !logger.forceStop()) {
                            k = ks.next();
                            if(k != null) {
                                String kStr = k.toString();
                                v = jsonProperty.get(kStr);
                                setOccurrence(propertyTopic, kStr, v);
                            }
                        }
                    }
                }
            }
        }
        else if(areEqual("actions", key)) {
            if(value != null && value instanceof JSONArray) {
                JSONArray actionsArray = (JSONArray) value;
                for(int i=0; i<actionsArray.length(); i++) {
                    Object action = actionsArray.get(i);
                    if(action instanceof JSONObject) {
                        JSONObject actionJSON = (JSONObject) action;
                        if(actionJSON.has("name") && actionJSON.has("link")) {
                            Object actionName = actionJSON.get("name");
                            Object actionLink = actionJSON.get("link");
                            setOccurrence(t, actionName+" action", actionLink);
                        }
                    }
                }
            }
        }
        else if(areEqual("location", key)) {
            associate(key, t, value);
        }
        else if(areEqual("images", key)) {
            associate(key, t, value);
        }

        else if(areEqual("first_name", key)) {
            associate(key, t, value);
        }
        else if(areEqual("last_name", key)) {
            associate(key, t, value);
        }
        else if(areEqual("middle_name", key)) {
            associate(key, t, value);
        }
        else if(areEqual("gender", key)) {
            associate(key, t, value);
        }
        else if(areEqual("locale", key)) {
            associate(key, t, value);
        }

        else if(areEqual("timezone", key)) {
            associate(key, t, value);
        }
        else if(areEqual("verified", key)) {
            associate(key, t, value);
        }

        else if(areEqual("from", key)) {
            associate(key, t, value);
        }
        else if(areEqual("to", key)) {
            associate(key, t, value);
        }
        else if(areEqual("message", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("picture", key)) {
            associate(key, t, value);
        }
        else if(areEqual("caption", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("description", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("source", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("attributes", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("attribution", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("about", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("bio", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("application", key)) {
            associate(key, t, value);
        }
        else if(areEqual("likes", key)) {
            setOccurrence(t, key, value);
            if(value instanceof JSONObject) {
                JSONObject jsonValue = (JSONObject) value;
                if(jsonValue.has("count")) {
                    setOccurrence(t, "likes-count", jsonValue.get("count"));
                }
            }
        }
        else if(areEqual("created_time", key)) {
            associate(key, t, value);
        }
        else if(areEqual("updated_time", key)) {
            associate(key, t, value);
        }
        else if(areEqual("message", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("length", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("tags", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("source", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("height", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("width", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("subject", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("owner", key)) {
            associate(key, t, value);
        }
        else if(areEqual("venue", key)) {
            associate(key, t, value);
        }
        else if(areEqual("privacy", key)) {
            associate(key, t, value);
        }
        else if(areEqual("start_time", key)) {
            associate(key, t, value);
        }
        else if(areEqual("end_time", key)) {
            associate(key, t, value);
        }
        else if(areEqual("location_time", key)) {
            associate(key, t, value);
        }

        else if(areEqual("category", key)) {
            associate(key, t, value);
        }
        else if(areEqual("founded", key)) {
            associate(key, t, value);
        }
        else if(areEqual("username", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("company_overview", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("mission", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("products", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("fan_count", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("comments", key)) {
            associate(key, t, value);
            if(value instanceof JSONObject) {
                JSONObject jsonValue = (JSONObject) value;
                if(jsonValue.has("count")) {
                    setOccurrence(t, "comments-count", jsonValue.get("count"));
                }
            }
        }
        else if(areEqual("talking_about_count", key)) {
            setOccurrence(t, key, value);
        }
        else if(areEqual("location", key)) {
            associate(key, t, value);
        }
        else if(areEqual("count", key)) {
            associate(key, t, value);
        }
        else if(areEqual("category", key)) {
            associate(key, t, value);
        }
        else if(areEqual("cover_photo", key)) {
            associate(key, t, value);
        }
        else if(areEqual("can_upload", key)) {
            associate(key, t, value);
        }
        else {
            System.out.println("Found unrecognized key '"+key+"' in graph serialization. Creating occurrence.");
            setOccurrence(t, key, value);
        }
        
        return true;
    }




    // -------------------------------------------------------------------------




    protected void parseConnections(JSONObject connectionsJSON, Topic targetTopic) throws Exception {
        if(connectionDepth <= 1) return;

        Iterator<String> keys = connectionsJSON.keys();
        String key = null;
        Object value = null;
        while(keys.hasNext() && !logger.forceStop()) {
            try {
                key = keys.next();
                if(key != null) {
                    value = connectionsJSON.get(key);
                    if(areEqual("picture", key)) {
                        associate(key, targetTopic, value);
                    }
                    else if(areEqual("inbox", key)) {
                       // DO NOT PROCESS. NO PRIVILEGES.
                    }
                    else if(areEqual("outbox", key)) {
                       // DO NOT PROCESS. NO PRIVILEGES.
                    }
                    else if(areEqual("updates", key)) {
                       // DO NOT PROCESS. NO PRIVILEGES.
                    }
                    else {
                        if(logger != null) logger.hlog("Parsing '"+key+"' of '"+targetTopic+"'");
                        FacebookGraphParser fbgp = new FacebookGraphParser(tm, logger, connectionDepth-1);
                        ArrayList<Topic> fbTopics = fbgp.parse(new URL(value.toString()));
                        associate(key, targetTopic, fbTopics);
                    }
                }
            }
            catch(Exception e) {
                logger.log("Exception '"+e.getMessage()+"' occurred while parsing connection '"+key+"'.");
            }
        }
    }






    // -------------------------------------------------------------------------


    public String robustJSONGet(JSONObject json, String key) {
        try {
            if(json.has(key)) {
                return (String) json.get(key);
            }
        }
        catch(Exception e) {
            // NOTHING HERE
        }
        return null;
    }


    public boolean areEqual(String key, Object o) {
        if(key == null || o == null) return false;
        return key.equalsIgnoreCase(o.toString());
    }



    

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------- TOPICS ---
    // -------------------------------------------------------------------------


    
    private static final String ROLE1_SUFFIX = " (subject)";
    private static final String ROLE2_SUFFIX = " (object)";



    public void associate(String key, Topic t1, Object t2something) throws Exception {
        if(t2something != null) {
            if(t2something instanceof JSONObject) {
                ArrayList<Topic> t2s = parse((JSONObject) t2something);
                associate(key, t1, key+ROLE1_SUFFIX, t2s, key+ROLE2_SUFFIX);
            }
            else if(t2something instanceof JSONArray) {
                JSONArray jsona = (JSONArray) t2something;
                for(int i=0; i<jsona.length(); i++) {
                    associate(key, t1, jsona.get(i));
                }
            }
            else if(t2something instanceof String) {
                String t2str = (String) t2something;
                if(t2str.startsWith(FACEBOOK_BASE_SI) && !"picture".equals(key)) {
                    ArrayList<Topic> t2s = parse(new URL(t2str));
                    associate(key, t1, t2s);
                }
                else if(t2str.startsWith("http://") || t2str.startsWith("https://")) {
                    t2str = t2str.trim();
                    Topic t2 = getOrCreateTopic(tm, t2str);
                    if(t2 != null) {
                        t2.setSubjectLocator(new Locator(t2str));
                        associate(key, t1, t2);
                    }
                }
                else {
                    associate(key, t1, key+ROLE1_SUFFIX, (String) t2something, key+ROLE2_SUFFIX);
                }
            }
            else if(t2something instanceof Integer || t2something instanceof Long) {
                associate(key, t1, key+ROLE1_SUFFIX, t2something.toString(), key+ROLE2_SUFFIX);
            }
            else if(t2something instanceof Boolean) {
                associate(key, t1, key+ROLE1_SUFFIX, t2something.toString(), key+ROLE2_SUFFIX);
            }
            else {
                System.out.println("Warning: Skipping object '"+t2something+"' while associating.");
            }
        }
    }


    public void associate(String key, Topic t1, ArrayList<Topic> t2s) throws Exception {
        if(t2s != null && t2s.size() > 0) {
            for( Topic t2 : t2s ) {
                associate(key, t1, t2);
            }
        }
    }

    public void associate(String key, Topic t1, Topic t2) throws Exception {
        associate(key, t1, key+ROLE1_SUFFIX, t2, key+ROLE2_SUFFIX);
    }

    public void associate(String key, Topic t1, String t2str) throws Exception {
        associate(key, t1, key+ROLE1_SUFFIX, t2str, key+ROLE2_SUFFIX);
    }

    public void associate(String typestr, Topic t1, String role1str, String t2str, String role2str) throws Exception {
        associate(typestr, null, t1, role1str, t2str, role2str);
    }
    
    public void associate(String typestr, String supertypestr, Topic t1, String role1str, String t2str, String role2str) throws Exception {
        if(typestr != null && typestr.length() > 0 && t1 != null && role1str != null && role1str.length() > 0 && t2str != null && t2str.length() > 0 && role2str != null && role2str.length() > 0) {
            Topic atype = ( supertypestr == null ? getTypeTopic(typestr) : getTypeTopic(typestr, supertypestr) );
            Topic role1 = getTypeTopic(role1str);
            Topic role2 = getTypeTopic(role2str);
            Topic t2 = getTopic(t2str);
            if(atype != null && role1 != null && role2 != null && t2 != null) {
                Association a = tm.createAssociation(atype);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
            }
        }
    }



    public void associate(String typestr, Topic t1, String role1str, ArrayList<Topic> t2s, String role2str) throws Exception {
        if(t2s != null && t2s.size() > 0) {
            for( Topic t2 : t2s ) {
                associate(typestr, null, t1, role1str, t2, role2str);
            }
        }
    }



    public void associate(String typestr, Topic t1, String role1str, Topic t2, String role2str) throws Exception {
        associate(typestr, null, t1, role1str, t2, role2str);
    }

    
    public void associate(String typestr, String supertypestr, Topic t1, String role1str, Topic t2, String role2str) throws Exception {
        if(typestr != null && typestr.length() > 0 && t1 != null && role1str != null && role1str.length() > 0 && t2 != null && role2str != null && role2str.length() > 0) {
            Topic atype = ( supertypestr == null ? getTypeTopic(typestr) : getTypeTopic(typestr, supertypestr) );
            Topic role1 = getTypeTopic(role1str);
            Topic role2 = getTypeTopic(role2str);
            if(atype != null && role1 != null && role2 != null) {
                Association a = tm.createAssociation(atype);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
            }
        }
    }



    public void setOccurrence(Topic t, String ot, String o) throws Exception {
        if(t != null && ot != null && ot.length() > 0 && o != null && o.length() > 0) {
            Topic otype = getTypeTopic(ot);
            Topic lang = TMBox.getLangTopic(otype, "en");
            t.setData(otype, lang, o);
        }
    }


    public void setOccurrence(Topic t, String ot, Object o) throws Exception {
        if(o instanceof String) {
            setOccurrence(t, ot, (String) o);
        }
        else if(o instanceof Integer) {
            setOccurrence(t, ot, ((Integer) o).toString());
        }
        else if(o instanceof Long) {
            setOccurrence(t, ot, ((Long) o).toString());
        }
        else if(o instanceof Float) {
            setOccurrence(t, ot, ((Float) o).toString());
        }
        else if(o instanceof Double) {
            setOccurrence(t, ot, ((Double) o).toString());
        }
        else if(o instanceof Boolean) {
            setOccurrence(t, ot, ((Boolean) o).toString());
        }
        else {
            associate(ot, t, o);
        }
    }

    // ------


    public Topic getTypeTopic(String type) throws Exception {
        Topic t = getOrCreateTopic(tm, GENERAL_SI+type);
        t.setBaseName(type);
        return t;
    }


    public Topic getTypeTopic(String type, String supertype) throws Exception {
        Topic t = getTypeTopic(type);
        if(supertype != null && supertype.length() > 0) {
            Topic st = getTypeTopic(supertype);
            Topic fbt = createFBTypeTopic();
            st.addType(fbt);
            t.addType(st);
        }
        return t;
    }


    // ------

    public Topic getTopic(String str) throws Exception {
        Topic t = getOrCreateTopic(tm, GENERAL_SI+str);
        t.setBaseName(str);
        return t;
    }

    public Topic getTopic(String str, String type) throws Exception {
        Topic t = getTopic(str);
        Topic ty = getTypeTopic(type);
        t.addType(ty);
        return t;
    }

    public Topic getTopic(String str, String type, String supertype) throws Exception {
        Topic t = getTopic(str);
        Topic ty = getTypeTopic(type, supertype);
        t.addType(ty);
        return t;
    }


    // ------



    public Topic getFBTopic(String id) throws Exception {
        Topic t = getOrCreateTopic(tm, FACEBOOK_BASE_SI+id);
        return t;
    }

    public Topic getFBTopic(String id, String type) throws Exception {
        Topic t = getOrCreateTopic(tm, FACEBOOK_BASE_SI+id);
        if(type != null && type.length() > 0) {
            Topic ty = getTypeTopic(type);
            t.addType(ty);
            t.setBaseName(type+" ("+id+")");
        }
        return t;
    }

    public Topic getFBTopic(String id, String type, String supertype) throws Exception {
        Topic t = getFBTopic(id);
        if(type != null && type.length() > 0) {
            Topic ty = getTypeTopic(type, supertype);
            t.addType(ty);
        }
        return t;
    }


    // --------



    
    public Topic createFBTypeTopic() throws Exception {
        Topic t=tm.getTopic(FACEBOOK_BASE_SI);
        if(t==null){
            t=tm.createTopic();
            t.addSubjectIdentifier(tm.createLocator(FACEBOOK_BASE_SI));
            t.setBaseName("Facebook");
            Topic wandoraClass=getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
            t.addType(wandoraClass);
        }
        return t;
    }


    // --------



    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }





    // ---------------------------------------------------------------- LOGS ---



    private int maxLogs = 1000;
    private int logCount = 0;

    private void log(String str) {
        if(logCount<maxLogs) {
            logCount++;
            logger.log(str);
            if(logCount>=maxLogs) {
                logger.log("Silently passing rest logs...");
            }
        }
    }

    private void log(Exception ex) {
        logger.log(ex);
    }

}
