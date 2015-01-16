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

package org.wandora.application.tools.extractors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.extractors.flickr.ChooseUserDialog;
import org.wandora.application.tools.extractors.flickr.FlickrUtils;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;
import org.wandora.application.gui.*;
        
import javax.swing.*;
import java.util.*;


/**
 *
 * @author anttirt
 */
public class DeliciousPostsExtractor extends AbstractWandoraTool {
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    @Override
    public String getName() {
        return "Delicious profile posts extractor";
    }
    
    @Override
    public String getDescription() {
        return "Creates topics for posts made by one or more users in Delicious. The subject identifiers of the topics are set to the URLs and will be merged with any topics in the map that already have the same SI.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_delicious.png");
    }
    
    
    public static class ExtractionFailure extends Exception {
        public ExtractionFailure(String message) { super(message); }
        public ExtractionFailure(Throwable cause) { super(cause); }
        public ExtractionFailure(String message, Throwable cause) { super(message, cause); }
    }
    public static class RequestFailure extends ExtractionFailure {
        public RequestFailure(String message) { super(message); }
        public RequestFailure(Throwable cause) { super(cause); }
        public RequestFailure(String message, Throwable cause) { super(message, cause); }
    }
    public void execute(Wandora admin, final Context context) {
        currentMap = admin.getTopicMap();
        
        ChooseUserDialog dlg = new ChooseUserDialog(admin, true, "<html><body><p>Please enter a comma-delimited list of Delicious usernames to fetch information for.</p></body></html>");
        dlg.setVisible(true);
        setDefaultLogger();
        if(dlg.wasCancelled())
        {
            log("User cancelled.");
            setState(WAIT);
            return;
        }
        
        String[] users = dlg.getUserList();
        
        if(users.length == 0)
        {
            log("No users specified! Please specify a comma-delimited list of users.");
        }
        
        for(String user : users)
        {
            try
            {
                extract(user);
            }
            catch(ExtractionFailure e)
            {
                log(e);
            }
            catch(TopicMapException e)
            {
                log(e);
            }
        }
        
        setState(WAIT);
    }



    public Topic getLanguage(String id) throws TopicMapException {
        Topic lanT = currentMap.getTopic(XTMPSI.getLang(id));
        if(lanT == null)
        {
            lanT = currentMap.createTopic();
            lanT.addSubjectIdentifier(new Locator(XTMPSI.getLang(id)));
            lanT.setBaseName("");
        }
        return lanT;
    }




    private void extract(final String username) throws ExtractionFailure, TopicMapException {
        final String reqURL = "http://badges.del.icio.us/feeds/json/" + url(username) + "?raw&count=100";
        log("Getting posts for " + username);
        try {
            JSONArray result = new JSONArray(IObox.doUrl(new URL(reqURL)));
            Topic dT = getDeliciousClass(currentMap);
            Topic tagT = FlickrUtils.createTopic(currentMap, "delicious tag", dT);
            Topic tagAssoc = FlickrUtils.createTopic(currentMap, "describes", dT);
            Topic tagTargetT = FlickrUtils.createTopic(currentMap, "descriptee", dT);
            
            Topic postT = FlickrUtils.createTopic(currentMap, "delicious post", dT);
            Topic descOccur = FlickrUtils.createTopic(currentMap, "delicious description", dT);
            
            
            for(int i = 0; i < result.length(); ++i)
            {
                JSONObject postObj = result.getJSONObject(i);
                Topic post = FlickrUtils.createRaw(currentMap, postObj.getString("u"), " (delicious post)", postObj.getString("u"), postT);
                try { post.setData(descOccur, getLanguage(null), postObj.getString("d")); } catch(JSONException e) {}
                try { JSONArray tagsArray = postObj.getJSONArray("t");
                for(int j = 0; j < tagsArray.length(); ++j)
                {
                    Topic tag = FlickrUtils.createRaw(currentMap, "http://delicious.com/tag/" + tagsArray.getString(j), " (delicious tag)", tagsArray.getString(j), tagT);
                    FlickrUtils.createAssociation(currentMap, tagAssoc, new Topic[] { tag, post }, new Topic[]{tagT, tagTargetT});
                }  } catch(JSONException e) {}
            }
        }
        catch(MalformedURLException e) { log(e); }
        catch(IOException e) { log(e); }
        catch(JSONException e) { log(e); }
        catch(IllegalArgumentException e) { log(e); }
    }
    
    private TopicMap currentMap;





    public static Topic getDeliciousClass(TopicMap currentMap) throws TopicMapException {
        Locator loc = new Locator("http://www.delicious.com");
        Topic deliciousClass = currentMap.getTopic(loc);
        if(deliciousClass == null)
        {
            deliciousClass = currentMap.createTopic();
            deliciousClass.addSubjectIdentifier(loc);
            deliciousClass.setBaseName("Delicious");
            deliciousClass.addType(getWandoraClass(currentMap));
        }
        return deliciousClass;
    }



    public static Topic getWandoraClass(TopicMap currentMap) throws TopicMapException {
        Locator loc = new Locator(TMBox.WANDORACLASS_SI);
        Topic wandoraClass = currentMap.getTopic(loc);
        if(wandoraClass == null)
        {
            wandoraClass = currentMap.createTopic();
            wandoraClass.addSubjectIdentifier(
                new Locator(TMBox.WANDORACLASS_SI));
            wandoraClass.setBaseName("Wandora class");
        }
        return wandoraClass;
    }





    private String url(String str) throws ExtractionFailure
    {
        try
        {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ExtractionFailure(e);
        }
        
    }
}
