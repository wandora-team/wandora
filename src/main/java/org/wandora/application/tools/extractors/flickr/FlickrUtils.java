/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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

package org.wandora.application.tools.extractors.flickr;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author anttirt
 */
public class FlickrUtils {
    
    public static Locator buildSI(String siend) {
        if(siend == null) siend = "" + System.currentTimeMillis() + Math.random();
        return new Locator("http://wandora.org/si/flickr/" + siend);
    }
    
    public static Topic createTopic(TopicMap topicMap, String baseString) throws TopicMapException {
        return createTopic(topicMap, baseString, "", baseString, new Topic[] { });
    }
    
    public static <T> Iterable<T> each(final Iterator<T> iter) {
        return new Iterable<T>() { public Iterator<T> iterator() { return iter; } };
    }
    
    public static class EnumToIter<T> implements Iterator<T> {
        private Enumeration<T> data;

        public boolean hasNext() {
            return data.hasMoreElements();
        }

        public T next() {
            return data.nextElement();
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported.");
        }
        
        public EnumToIter(Enumeration<T> e) {
            if(e == null) throw new NullPointerException();
            data = e;
        }
    }
    
    public static <T> Iterable<T> each(final Enumeration<T> e) {
        return new Iterable<T>() { public Iterator<T> iterator() { return new EnumToIter<T>(e); } };
    }

    public static Topic createTopic(TopicMap topicMap, String siString, String baseString) throws TopicMapException {
        return createTopic(topicMap, siString, "", baseString, new Topic[] { });
    }

    public static Topic createTopic(TopicMap topicMap, String siString, String baseString, Topic type) throws TopicMapException {
        return createTopic(topicMap, siString, "", baseString, new Topic[] { type });
    }

    public static Topic createTopic(TopicMap topicMap, String baseString, Topic type)  throws TopicMapException {
        return createTopic(topicMap, baseString, "", baseString, new Topic[] { type });
    }

    public static Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString)  throws TopicMapException {
        return createTopic(topicMap, siString, baseNameString, baseString, new Topic[] { });
    }


    public static Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic type)  throws TopicMapException {
        return createTopic(topicMap, siString, baseNameString, baseString, new Topic[] { type });
    }

    public static Topic createRaw(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic type) throws TopicMapException {
        return createRaw(topicMap, siString, baseNameString, baseString, new Topic[] { type });
    }
    
    public static Topic createTopic(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic[] types)  throws TopicMapException {
        if(baseString == null)
            throw new java.lang.IllegalArgumentException("Null baseString passed to createTopic (siString=\"" + siString + "\", baseNameString=\"" + baseNameString + "\"");
        if(baseString.length() == 0)
            throw new java.lang.IllegalArgumentException("Empty baseString passed to createTopic (siString=\"" + siString + "\", baseNameString=\"" + baseNameString + "\"");

        Locator si = buildSI(siString);
        Topic t = topicMap.getTopic(si);
        if(t == null) {
            t = topicMap.getTopicWithBaseName(baseString + baseNameString);
            if(t == null) {
                t = topicMap.createTopic();
                t.setBaseName(baseString + baseNameString);
            }
            t.addSubjectIdentifier(si);
        }

        setDisplayName(t, "en", baseString);
        for(int i=0; i<types.length; i++) {
            Topic typeTopic = types[i];
            if(typeTopic != null) {
                t.addType(typeTopic);
            }
        }
        return t;
    }
    
    
    public static Topic createRaw(TopicMap topicMap, String siString, String baseNameString, String baseString, Topic[] types)  throws TopicMapException {
        if(baseString == null)
            throw new java.lang.IllegalArgumentException("Null baseString passed to createRaw (siString=\"" + siString + "\", baseNameString=\"" + baseNameString + "\"");
        if(baseString.length() == 0)
            throw new java.lang.IllegalArgumentException("Empty baseString passed to createRaw (siString=\"" + siString + "\", baseNameString=\"" + baseNameString + "\"");

        Locator si = new Locator(siString);
        Topic t = topicMap.getTopic(si);
        if(t == null) {
            t = topicMap.getTopicWithBaseName(baseString + baseNameString);
            if(t == null) {
                t = topicMap.createTopic();
                t.setBaseName(baseString + baseNameString);
            }
            t.addSubjectIdentifier(si);
        }

        setDisplayName(t, "en", baseString);
        for(int i=0; i<types.length; i++) {
            Topic typeTopic = types[i];
            if(typeTopic != null) {
                t.addType(typeTopic);
            }
        }
        return t;
    }
    
    
    public static void setDisplayName(Topic t, String lang, String name)  throws TopicMapException {
        if(t != null & lang != null && name != null) {
            String langsi=XTMPSI.getLang(lang);
            Topic langT=t.getTopicMap().getTopic(langsi);
            if(langT == null) {
                langT = t.getTopicMap().createTopic();
                langT.addSubjectIdentifier(new Locator(langsi));
                try {
                    langT.setBaseName("Language " + lang.toUpperCase());
                }
                catch (Exception e) {
                    langT.setBaseName("Language " + langsi);
                }
            }
            String dispsi=XTMPSI.DISPLAY;
            Topic dispT=t.getTopicMap().getTopic(dispsi);
            if(dispT == null) {
                dispT = t.getTopicMap().createTopic();
                dispT.addSubjectIdentifier(new Locator(dispsi));
                dispT.setBaseName("Scope Display");
            }
            HashSet scope=new HashSet();
            if(langT!=null) scope.add(langT);
            if(dispT!=null) scope.add(dispT);
            t.setVariant(scope, name);
        }
    }


    public static void setData(Topic t, Topic type, String lang, String text) throws TopicMapException {
        if(t != null & type != null && lang != null && text != null) {
            String langsi=XTMPSI.getLang(lang);
            Topic langT=t.getTopicMap().getTopic(langsi);
            if(langT == null) {
                langT = t.getTopicMap().createTopic();
                langT.addSubjectIdentifier(new Locator(langsi));
                try {
                    langT.setBaseName("Language " + lang.toUpperCase());
                }
                catch (Exception e) {
                    langT.setBaseName("Language " + langsi);
                }
            }
            t.setData(type, langT, text);
        }
    }

    
    public static Association createAssociation(TopicMap topicMap, Topic aType, Topic[] players)  throws TopicMapException {
        Association a = topicMap.createAssociation(aType);
        Topic player;
        Topic role;
        Collection playerTypes;
        for(int i=0; i<players.length; i++) {
            player = players[i];
            playerTypes = player.getTypes();
            if(playerTypes.size() > 0) {
                role = (Topic) playerTypes.iterator().next();
                a.addPlayer(player, role);
            }
        }
        return a;
    }


    public static Association createAssociation(TopicMap topicMap, Topic aType, Topic[] players, Topic[] roles)  throws TopicMapException {
        Association a = topicMap.createAssociation(aType);
        Topic player;
        Topic role;
        for(int i=0; i<players.length; i++) {
            player = players[i];
            role = roles[i];
            a.addPlayer(player, role);
        }
        return a;
    }
    
    private static Pattern objOrArray = Pattern.compile("[^\\.\\[]+");
    private static Pattern arrayIndex = Pattern.compile("[0-9]+");
    private static enum curEnum { obj, arr }
    private static class SearchResult { public curEnum type; public JSONObject obj; public JSONArray arr; public String subName; public int subIdx; }
    private static SearchResult jsonSearch(JSONObject obj, String path) throws JSONException {
        Matcher m = objOrArray.matcher(path);
        SearchResult ret = new SearchResult();
        //ret.type = curEnum.obj;
        //ret.obj = obj;
        
        while(m.find()) {
            if(ret.obj == null) {
                ret.obj = obj;
            }
            else {
                if(ret.type == curEnum.obj)
                    ret.obj = ret.obj.getJSONObject(ret.subName);
                else
                    ret.obj = ret.arr.getJSONObject(ret.subIdx);
            }
            
            ret.subName = m.group();
            ret.type = curEnum.obj;
            
            if(m.hitEnd()) {
                break;
            }
            
            if(path.charAt(m.end()) == '.') {
                continue;
            }
            else {
                Matcher n = arrayIndex.matcher(path.subSequence(m.end() + 1, path.length()));
                if(!n.find())
                    throw new JSONException(path);

                if(ret.type == curEnum.obj)
                    ret.arr = ret.obj.getJSONArray(ret.subName);
                else
                    ret.arr = ret.arr.getJSONArray(ret.subIdx);
                
                ret.subIdx = Integer.parseInt(n.group());
                ret.type = curEnum.arr;
                
                m.region(m.end() + 1 + n.end() + 1, path.length());
            }
        }
        return ret;
    }
    
    
    public static int searchInt(JSONObject obj, String path) throws JSONException {
        SearchResult res = jsonSearch(obj, path);
        if(res.type == curEnum.arr)
            return res.arr.getInt(res.subIdx);
        else
            return res.obj.getInt(res.subName);
    }
    
    
    public static long searchLong(JSONObject obj, String path) throws JSONException {
        SearchResult res = jsonSearch(obj, path);
        if(res.type == curEnum.arr)
            return res.arr.getLong(res.subIdx);
        else
            return res.obj.getLong(res.subName);
    }
    
    
    public static double searchDouble(JSONObject obj, String path) throws JSONException {
        SearchResult res = jsonSearch(obj, path);
        if(res.type == curEnum.arr)
            return res.arr.getDouble(res.subIdx);
        else
            return res.obj.getDouble(res.subName);
    }
    
    
    
    public static String searchString(JSONObject obj, String path) throws JSONException {
        SearchResult res = jsonSearch(obj, path);
        if(res.type == curEnum.arr)
            return res.arr.getString(res.subIdx);
        else
            return res.obj.getString(res.subName);
    }
    
    
    public static JSONObject searchJSONObject(JSONObject obj, String path) throws JSONException {
        SearchResult res = jsonSearch(obj, path);
        if(res.type == curEnum.arr)
            return res.arr.getJSONObject(res.subIdx);
        else
            return res.obj.getJSONObject(res.subName);
    }
    
    
    public static JSONArray searchJSONArray(JSONObject obj, String path) throws JSONException {
        SearchResult res = jsonSearch(obj, path);
        if(res.type == curEnum.arr)
            return res.arr.getJSONArray(res.subIdx);
        else
            return res.obj.getJSONArray(res.subName);
    }
}
