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

package org.wandora.application.tools.extractors.reddit;

import com.mashape.unirest.http.*;
import org.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.wandora.topicmap.TopicMap;


import java.util.HashMap;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen
 * @author akivela
 */


public class RedditThingExtractor extends AbstractRedditExtractor {
    

	private static final long serialVersionUID = 1L;

	private HashMap<String,Boolean> crawlSettings;
    
    

    public void setCrawlingSettings(HashMap<String,Boolean> crawls){
        crawlSettings = crawls;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        FileInputStream is = new FileInputStream(f);
        String fileContent = IOUtils.toString(is);
        return _extractTopicsFrom(fileContent, tm);
    }

    
    
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        if(u == null || tm == null) return false;
        
        ParseCallback<JsonNode> callback = new ParseCallback<JsonNode>() {
            @Override
            public void run(HttpResponse<JsonNode> response) {
                try {
                    TopicMap tm = getWandora().getTopicMap();
                    JSONArray respArray = response.getBody().getArray();
                    final HashMap<String, Topic> thingTypes = getThingTypes(tm);
                    for(int i=0; i<respArray.length(); i++) {
                        parseThing(respArray.getJSONObject(i),tm,thingTypes, crawlSettings);
                    }
                }
                catch (JSONException | TopicMapException e) {
                    log(e.getMessage());
                }
            }
            @Override
            protected void error(Exception e, String body) {
                log(e.getMessage());
                if(body != null){
                    log("Server responed with");
                    log(body);
                }
            }
        };
        
        resetExtracted();
        requester.addRequest(Unirest.get(u.toExternalForm()), callback);
        
        while(requester.hasRequests()) {
            if(forceStop()) {
                log("Aborting...");
                log("Deleting "+requester.size()+" requests in run queue.");
                requester.clear();
            }
            else {
                if(requester.getRunCounter() > 0) {
                    hlog("Queue contains "+requester.size()+" requests. Running next request. Already processed "+requester.getRunCounter()+" requests.");
                }
                else {
                    hlog("Queue contains "+requester.size()+" requests. Running request.");
                }
                requester.runNext();
            }
        }
        
        // Print some statistics of successful and failed requests.
        int failCounter = requester.getFailCounter();
        if(failCounter > 0) {
            log("Failed to handle "+requester.getFailCounter()+" requests.");
        }
        else {
            log("All API request were successful.");
        }
        int runCounter = requester.getRunCounter();
        log("Handled successfully "+runCounter+" Reddit API requests.");
        
        return true;
    }
    
    

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        if(str == null || tm == null) return false;
        
        // We assume the string contains URLs that are separated with a new line
        // character.
        String[] urls = str.split("\n");
        for(String urlString : urls) {
            urlString = urlString.trim();
            if(urlString.length() > 1) {
                try {
                    URL url = new URL(urlString);
                    _extractTopicsFrom(url, tm);
                }
                catch(MalformedURLException mfue) {
                    log("Found malformed URL '"+urlString+"' in text processed by RedditThingExtractor.");
                }
            }
        }
        
        return true;
    }
    
 
}
