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
 */
package org.wandora.application.tools.extractors.twitter;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */



public class TwitterJSONExtractor extends AbstractTwitterExtractor {
    
    private static final String BASE_SI = "http://wandora.org/si/twitter/twurl";
    private static final String BASE_NAME = "twurl";
    
    @Override
    public String getName() {
        return "Twitter JSON extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "The extractor converts tweets to topics and associations.";
    }
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        List<String> lines = Files.readAllLines(f.toPath(), Charset.forName("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for(String line: lines){
            sb.append(line);
        }
        return handleStatuses(sb.toString(), t);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        String str = IOUtils.toString(u.openStream());
        return _extractTopicsFrom(str, t);
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        return handleStatuses(str, t);
    }

    private boolean handleStatuses(String str, TopicMap tm) {
        
        JSONArray statuses;
        Topic baseTopic;
        
        try {
            JSONObject json = new JSONObject(str);
            statuses = json.getJSONArray("statuses");
            
        } catch (JSONException jse) {
            try {
                statuses = new JSONArray(str);
            } catch (JSONException jsee) {
                log("Couldn't parse a status set due to malformed data. The error "
                        + "was:\n");
                log(jsee.getMessage());
                return false;
            }
        }
        
        try {
           
            baseTopic = ExtractHelper.getOrCreateTopic(BASE_SI, BASE_NAME, tm);

            for (int i = 0; i < statuses.length(); i++) {
                try {
                    JSONObject status = statuses.getJSONObject(i);
                    handleStatus(status, baseTopic, tm);

                } catch (JSONException jse) {
                    log("Malformed status. The error was:\n");
                    log(jse.getMessage());
                }
            } 
        } catch (TopicMapException tme) {
            log("Couldn't create twurl base topic. The error was: \n");
            log(tme.getMessage());
            return false;
        }
        
        
        return true;
    }

    private void handleStatus(JSONObject json, Topic baseTopic, TopicMap tm) {
        Status s;
        try {
            s = TwitterObjectFactory.createStatus(json.toString());
        } catch (TwitterException te) {
            log("Couldn't parse status JSON. The error was:\n");
            log(te.getErrorMessage());
            return;
        }
        
        Topic t = reifyTweet(s, tm);
        try {
            t.addType(baseTopic);
        } catch (TopicMapException tme) {
            log("Couldn't associate status with base topic. The error was:\n");
            log(tme.getMessage());
        }
        
    }
}
