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
 */

package org.wandora.application.tools.extractors.reddit;

import com.mashape.unirest.http.*;
import org.wandora.dep.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.wandora.topicmap.TopicMap;


import java.util.HashMap;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero
 */


public class RedditThingExtractor extends AbstractRedditExtractor{
    
    private boolean shouldCrawl;
    
    private HashMap<String,Boolean> crawlSettings;
    
    public void setShouldCrawl(boolean c){
        shouldCrawl = c;
    }
    public void setCrawling(HashMap<String,Boolean> crawls){
        crawlSettings = crawls;
    }
    
    
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
                
        log("handling url " + str);
        
        ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>() {
            @Override
            public void run(HttpResponse<JsonNode> response){
                try {
                    parse(response);
                } catch (JSONException | TopicMapException e) {
                    log(e.getMessage());
                }
            }
            
        };
        
        requester.doRequest(Unirest.get(str), callback);
        
        boolean shouldQuit = false;
        
        while(!shouldQuit){
           Thread.sleep(1000);
           shouldQuit = forceStop() || !requester.hasJobs();
        }
        
        requester.cancel();
        
        return true;
    }
    private void parse(HttpResponse<JsonNode> resp) 
            throws JSONException, TopicMapException{
        
        resetExtracted();
        
        TopicMap tm = getWandora().getTopicMap();
        JSONArray respArray = resp.getBody().getArray();
        final HashMap<String, Topic> thingTypes = getThingTypes(tm);
        for (int i = 0; i < respArray.length(); i++) {
            parseThing(respArray.getJSONObject(i),tm,thingTypes, crawlSettings);
        }
    }  
}
