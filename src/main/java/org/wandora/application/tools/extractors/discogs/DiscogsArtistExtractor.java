/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2013 Wandora Team
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

package org.wandora.application.tools.extractors.discogs;

import java.io.File;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;

/**
 *
 * @author nlaitine
 */


public class DiscogsArtistExtractor extends AbstractDiscogsExtractor {
    

	private static final long serialVersionUID = 1L;
	
	
	private static String defaultLang = "en";
    private static String currentURL = null;
    
    public DiscogsArtistExtractor () {
    }
    
    @Override
    public String getName() {
        return "Discogs API Artist extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor fetches artist data from Discogs API.";
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        currentURL = null;
        String in = IObox.loadFile(f);
        JSONObject json = new JSONObject(in);
        parseArtist(json, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        try {
            currentURL = u.toExternalForm();

            log("Release search extraction with " + currentURL);

            String in = DiscogsSearchExtractor.doUrl(u);

            System.out.println("---------------Discogs API returned------------\n"+in+
                               "\n-----------------------------------------------");

            JSONObject json = new JSONObject(in);
            parseArtist(json, tm);
   
        } catch (Exception e){
           e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        currentURL = null;
        JSONObject json = new JSONObject(str);
        parseArtist(json, tm);
        return true;
    }
    
    
    // ------------------------- PARSING ---------------------------------------
    
    
    public void parseArtist(JSONObject json, TopicMap tm) throws TopicMapException {
        if(json.has("results")) {
            try {
                JSONArray resultsArray = json.getJSONArray("results");
                if (resultsArray.length() > 0) {
                    int count = 0;
                    for(int i=0; i<resultsArray.length(); i++) {
                        JSONObject result = resultsArray.getJSONObject(i);
                        parseResult(result, tm);
                        count++;
                    }
                    log("Search returned " + count + " artists.");
                } else {
                    log("API returned no results.");
                }
            }
            catch (JSONException ex) {
                log(ex);
            }
        } else {
            log("API returned no results.");
        }
    }
    
    
    
    public void parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {
        
        if(result.has("uri") && result.has("id")) {
            String id = result.getString("id");
            String subjectId = DISCOGS_SI + result.getString("uri");
            Topic itemTopic = tm.createTopic();
            itemTopic.addSubjectIdentifier(new Locator(subjectId));
            itemTopic.addType(getArtistTypeTopic(tm));
                        
            if(result.has("title")) {
                String value = result.getString("title");
                if(value != null && value.length() > 0) {
                    Topic titleTypeTopic = getTitleTypeTopic(tm);
                    itemTopic.setDisplayName(defaultLang, value);
                    itemTopic.setBaseName(value);
                    Topic langTopic = getLangTopic(tm);
                    itemTopic.setData(titleTypeTopic, langTopic, value);
                }
            }
            
            if(result.has("thumb")) {
                String value = result.getString("thumb");
                if(value != null && value.length() > 0) {
                    Topic imageTypeTopic = getImageTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    itemTopic.setData(imageTypeTopic, langTopic, value);
                }
            }
            
        }
    }
    
}