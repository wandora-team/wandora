 /*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
import com.mashape.unirest.http.async.Callback;
import org.wandora.dep.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import org.wandora.topicmap.TopicMap;
import java.util.List;


import java.util.HashMap;
import java.util.concurrent.Future;
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
        
        Callback<JsonNode> callback = new Callback<JsonNode>() {
            @Override
            public void failed(Exception e){}
            @Override
            public void cancelled(){}
            @Override
            public void completed(HttpResponse<JsonNode> response){
                try {
                    File f = new File("resp_" + System.currentTimeMillis() + ".json");
                    FileOutputStream fos = new FileOutputStream(f);
                    IOUtils.write(IOUtils.toString(response.getRawBody()), fos);
                    fos.close();
                    parse(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        };
        
        //List<Future<HttpResponse<JsonNode>>> futures = 
        //        new ArrayList<Future<HttpResponse<JsonNode>>>();
        
        //Future<HttpResponse<JsonNode>> f = doAsyncRequest(str, callback);
        
        //futures.add(f);
        //We don't want to return before stuff's done
        //for(Future fut : futures){
        //    fut.get();
        //}
        
        HttpResponse<JsonNode> resp = doRequest(str);
        parse(resp);
        
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
