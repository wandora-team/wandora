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
 */

package org.wandora.application.tools.extractors.freebase;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;


/**
 *
 * @author
 * Eero Lehtonen
 */


public class FreebaseMQLExtractor  extends AbstractFreebaseExtractor {

	private static final long serialVersionUID = 1L;
	
	private static String currentURL = null;
    
    @Override
    public String getName() {
        return "Freebase MQL API extractor";
    }

    @Override
    public String getDescription(){
        return "Extractor performs an content query using Freebase MQL API and "+
                "transforms results to topics and associations.";
    }

    // -------------------------------------------------------------------------
    
    private int extractCount = 0;
    private int maxExtractCount;
    private boolean toggleLimit = FreebaseExtractorUI.toggleLimit;
    private int progress = 0;
    public ArrayList<String> extractedIDs = new ArrayList<String>();
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        currentURL = null;
        String in = IObox.loadFile(f);
        JSONObject json = new JSONObject(in);
        parse(json, tm);
        return true;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        
        currentURL = null;
        JSONObject json = new JSONObject(str);
        if (json.has("response")){
            JSONObject response = json.getJSONObject("response");
            parse(response, tm);
            
        }
        return true;
    }
    
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        setProgressMax(10);
        setProgress(0);
        String id = getIdFromUrl(u);
        return _extractTopicsFrom(u, tm, id);
        
    }
    
    public boolean _extractTopicsFrom(URL u, TopicMap tm, String id) throws Exception {
        extractedIDs = new ArrayList<String>();
        extractedIDs.add(id);
        maxExtractCount = FreebaseExtractorUI.maxExtractCount;
        
        setProgressMax(10);
        setProgress(0);
        return _extractTopicsFrom(u, tm, 0, id);
        
    }
    
    public boolean _extractTopicsFrom(URL u, TopicMap tm, int depth, String id) throws Exception {
        if (depth >= FreebaseExtractorUI.maxDepth){
            return true;
        }
        
        progress = (progress == 10) ? 0 : progress + 1;
        setProgress(progress);
        currentURL = u.toExternalForm();

        log("MQL query extraction with id: "+id);
        String in = "";
        try{
            in = IObox.doUrl(u);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String printIn = (in.length() > 2000) ? in.substring(0, 1999) +  "..." : in;
        
        System.out.println("Freebase API returned-------------------------\n"
                           +printIn
                           +"\n----------------------------------------------------");

        JSONObject json = new JSONObject(in);

        ArrayList<String> nextIDs = new ArrayList<String>();
        try{
            String s = " current depth: " + depth + ", extractCount: " + extractCount 
                     + ", maxExtractCount: " + maxExtractCount
                     + ", toggleLimit: " + toggleLimit;
            nextIDs = parse(json, tm);
            System.out.println("NextIDs.size(): " + nextIDs.size() + s);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        // Don't request already extracted topics
        nextIDs.removeAll(extractedIDs);

        for(String cid : nextIDs) {
            if(extractCount >= maxExtractCount || forceStop()) {
                break;
            }
            extractedIDs.add(cid);
            String query = getQuery(cid);
            URL newURL = new URL(FreebaseExtractorUI.FREEBASE_MQL_API_BASE + "?query=" + query);
            try{
                _extractTopicsFrom(newURL, tm, depth + 1, cid); 
            } catch (Exception e) {
                e.printStackTrace();
            }   
        }
        return true;
    }
    
    // -------------------------------------------------------------------------
    
    public ArrayList<String> parse(JSONObject json, TopicMap tm) throws TopicMapException, JSONException {
        if(!json.has("result")){
            throw new TopicMapException("Query returned no results!");
        }
        if(json.get("result") instanceof JSONObject) { 
           try {
                JSONObject result = json.getJSONObject("result");
                return parseResult(result, tm);
            } 
            catch (Exception ex) {
                ex.printStackTrace();
                log(ex);
            }
        }
        return new ArrayList<String>();
    }
    
    public ArrayList<String> parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {
        Topic objectTopic = createFreebaseTopic(tm, result);
        extractCount++;
        if (result.get("type") instanceof JSONArray && (extractCount < maxExtractCount || toggleLimit) ) { // Freebase type -> Wandora class
            JSONArray types = result.getJSONArray("type");
            return parseTypes(types, objectTopic, tm);
        }
        return new ArrayList<String>();
    }
    
    public ArrayList<String> parseTypes(JSONArray types, Topic objectTopic, TopicMap tm) throws JSONException, TopicMapException {
        ArrayList<String> typeTargetIDs = new ArrayList<String>();
        for ( int i = 0; i < types.length(); i++){
            if(extractCount >= maxExtractCount && !toggleLimit) break;
            JSONObject type = types.getJSONObject(i);
            Topic typeTopic = createType(tm, type);
            extractCount++;
            objectTopic.addType(typeTopic);
            
            if(type.get("properties") instanceof JSONArray){ // Freebase property -> Wandora association
                JSONArray typeProperties = type.getJSONArray(("properties"));
                typeTargetIDs.addAll(parseProperty(typeProperties, objectTopic, typeTopic, tm));
            }
        }
        return typeTargetIDs;
    }
    
    public ArrayList<String> parseProperty(JSONArray properties, Topic objectTopic, Topic typeTopic, TopicMap tm ) throws JSONException,TopicMapException  {
        ArrayList<String> propertyTargetIDs = new ArrayList<String>();
        for ( int i = 0; i < properties.length(); i++){
            if(extractCount >= maxExtractCount && !toggleLimit) break;
            JSONObject property = properties.getJSONObject(i);
            if(property.getJSONArray("links").length() != 0) {
                JSONObject expectedType = property.getJSONObject("expected_type");
                Topic propertyTypeTopic = createLinkType(tm, property);
                extractCount++;
                Topic targetTypeTopic = createType(tm, expectedType);
                
                JSONArray links = property.getJSONArray(("links"));
                for (int j = 0; j < links.length(); j++){
                    JSONObject link = links.getJSONObject(j);
                    if(link.get("target_value") instanceof String) {
                        String targetValue = link.getString("target_value");
                        objectTopic.setData(propertyTypeTopic, getLangTopic(tm), targetValue);
                    }
                    else  if(link.get("target") instanceof JSONObject && extractCount < maxExtractCount && !toggleLimit) {
                        JSONObject target = link.getJSONObject("target");
                        propertyTargetIDs.add(target.getString("id"));
                        Topic targetTopic = createFreebaseTopic(tm, target);
                        extractCount++;
                        targetTopic.addType(targetTypeTopic);
                        Association a = tm.createAssociation(propertyTypeTopic);
                        a.addPlayer(objectTopic, getSourceType(tm));
                        a.addPlayer(targetTopic, getTargetType(tm));
                    } 
                }
            }
            if (property.get("master_property") instanceof JSONObject){
                JSONObject mProperty = property.getJSONObject("master_property");
                if(mProperty.has("links")){
                    JSONArray mlinks = mProperty.getJSONArray("links");
                    for (int j = 0; j < mlinks.length(); j++) {
                        if(extractCount >= maxExtractCount && !toggleLimit) break;
                        JSONObject mExpectedType = property.getJSONObject("expected_type");
                        Topic mPropertyTypeTopic = createLinkType(tm, mProperty);
                        Topic mSourceTypeTopic = createType(tm, mExpectedType);
                        JSONObject link = mlinks.getJSONObject(j);
                        if(link.get("source") instanceof JSONObject) {
                            JSONObject source = link.getJSONObject("source");
                            propertyTargetIDs.add(source.getString("id"));
                            Topic mSourceTopic = createFreebaseTopic(tm, source);
                            extractCount++;
                            mSourceTopic.addType(mSourceTypeTopic);
                            Association a = tm.createAssociation(mPropertyTypeTopic);
                            a.addPlayer(objectTopic, getTargetType(tm));
                            a.addPlayer(mSourceTopic, getSourceType(tm));
                        }
                    }
                }
            }
        }
        return propertyTargetIDs;
    }
    
    private String getIdFromUrl(URL u){
        try{
            String us = u.toString();
            int beginID = us.indexOf("%22id%22%3A+%22%2F") + 18;
            int endID = us.indexOf("%22", beginID);
            String id = URLDecoder.decode(us.substring(beginID, endID),"UTF-8");
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}