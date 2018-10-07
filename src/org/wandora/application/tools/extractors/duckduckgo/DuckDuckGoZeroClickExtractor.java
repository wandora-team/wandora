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
 */
package org.wandora.application.tools.extractors.duckduckgo;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author Eero
 */


public class DuckDuckGoZeroClickExtractor  extends AbstractDuckDuckGoExtractor {
    
    private static final String API_SI 
        = "http://api.duckduckgo.com";
    
    private static final String API_NAME = "DuckDuckGo API";    
    private static final String SI_ROOT  = "http://wandora.org/si/duckduckgo";
    
    private static final String DUCK_T_SI       = SI_ROOT + "/topic";
    private static final String DEFINITON_SI    = SI_ROOT + "/defintion";
    private static final String HEADING_SI      = SI_ROOT + "/heading";
    private static final String IMAGE_SI        = SI_ROOT + "/image";
    private static final String ABSTRACT_SI     = SI_ROOT + "/abstract";
    private static final String ABST_SOURCE_SI  = SI_ROOT + "/abstract_source";
    private static final String ANSWER_SI       = SI_ROOT + "/answer";
    private static final String ANS_TYPE_SI     = SI_ROOT + "/answer_type";
    private static final String RELATED_SI      = SI_ROOT + "/related";
    private static final String RELATED_TO_SI   = SI_ROOT + "/related_to";
    private static final String RELATED_FROM_SI = SI_ROOT + "/related_from";
    
    //  {field in JSON,     Type Topic SI,   Base name}  
    private static final  String[][] typeStrings = {
        {"Topic",           DUCK_T_SI,       "Topic"},
        {"Definition",      DEFINITON_SI,    "Definition"},
        {"Heading",         HEADING_SI,      "Heading"},
        {"Image",           IMAGE_SI,        "Image"},
        {"Abstract",        ABSTRACT_SI,     "Abstract"},
        {"AbstractSource",  ABST_SOURCE_SI,  "Abstract Source"},
        {"Answer",          ANSWER_SI,       "Answer"},
        {"AnswerType",      ANS_TYPE_SI,     "Answered Type"},
        {"Related",         RELATED_SI,      "Related"},
        {"RelatedTo",       RELATED_TO_SI,   "Related To"},
        {"RelatedFrom",     RELATED_FROM_SI, "Related From"}
    };
    
    // A list of types that should be used as occurrence types
    // Should be a subset of the keys in typeStrings.
    private static final  String[] occStrings = {
        "Definition",
        "Heading",
        "Image",
        "Abstract",
        "AbstractSource",
        "Answer",
        "AnswerType"
    };
    
    
     // -------------------------------------------------------------------------
   
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        FileInputStream is = new FileInputStream(f);
        String query = IOUtils.toString(is);
        _extractTopicsFrom(query, tm);
        return true;
        
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        String currentURL = u.toExternalForm();
        extractTopicsFromText(currentURL, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        
        HttpResponse<JsonNode> resp =
            Unirest.get(str)
            .asJson();
                
        parse(resp, tm);
        return true;
    }
    
    // -------------------------------------------------------------------------
    
    private void parse(HttpResponse<JsonNode> json, TopicMap tm){
        try {
            
            JsonNode jsonBody = json.getBody();
            JSONObject body = jsonBody.getObject();
            
            if(body.has("message")){
                log(body.getString("message"));
                throw new Exception("Query failed: no result");
            }
                        
            Topic lang = getLangTopic(tm);
            Topic api = getAPIClass(tm, API_SI, API_NAME);
            HashMap<String, Topic> types = getTypes(tm,typeStrings, api);
            
            String bn = body.getString("Heading");
            
            String absUrl = body.getString("AbstractURL");
            Topic res = getTopic(tm, types.get("Topic"), absUrl ,bn);
            
            for (int i = 0; i < occStrings.length; i++) {
                String key = occStrings[i];
                String val = body.getString(key);
                Topic type = types.get(key);
                if(val.length() > 0){
                   res.setData(type, lang , val); 
                }
            }
            
            JSONArray relTopics = body.getJSONArray("RelatedTopics");
            for (int i = 0; i < relTopics.length(); i++) {
                
                JSONObject relJson = relTopics.getJSONObject(i);
                String relUrl = relJson.getString("FirstURL");
                String relDesc = relJson.getString("Text");
                Topic rel = getTopic(tm, types.get("Topic"), relUrl, relDesc);
                
                Association a = tm.createAssociation(types.get("Related"));
                a.addPlayer(res, types.get("RelatedFrom"));
                a.addPlayer(rel, types.get("RelatedTo"));
            }
            
        } catch(JSONException jse){
            log(jse.getMessage());
        } catch (Exception e){
            log(e.getMessage());
        } 
    }
    
}
