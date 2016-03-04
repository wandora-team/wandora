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
 * 
 */

package org.wandora.application.tools.extractors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Icon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.extractors.flickr.FlickrUtils;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.IObox;

/**
 *
 * @author anttirt
 */
public class DiggURLExtractor extends AbstractWandoraTool {
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    @Override
    public String getName() {
        return "Digg url extractor";
    }
    
    @Override
    public String getDescription() {
        return "Tries to find a post in Digg matching the URL in the SI or SL of a topic and if found, extracts information.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_digg.png");
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
    private static String mDiggAppKey = null;
    protected static String DiggAppKey() throws ExtractionFailure {
        if(mDiggAppKey == null)
            mDiggAppKey = url("http://wandora.org/");
        return mDiggAppKey;
    }
    
    protected static String url(String str) throws ExtractionFailure {
        if(str == null)
            return "";
        try {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch(Exception e) {
            throw new ExtractionFailure(e);
        }
    }
    
    public void execute(Wandora admin, final Context context) throws TopicMapException {
        currentMap = admin.getTopicMap();
        
        setDefaultLogger();
        
        boolean foundTopics = false;
        
        for(Topic t : new Iterable<Topic>() { public Iterator<Topic> iterator() { return context.getContextObjects(); }})
        {
            foundTopics = true;
            try {
                log("Extracting data from topic " + t.getBaseName());
                extract(t);
            }
            catch(ExtractionFailure e) { log(e); }
        }
        
        if(!foundTopics)
        {
            log("Found no topics in context! Make sure you have selected one or more topics in Wandora.");
        }
        
        setState(WAIT);
    }


    public Topic getDIGGClass() throws TopicMapException {
        Locator loc = new Locator("http://www.digg.com");
        Topic diggClass = currentMap.getTopic(loc);
        if(diggClass == null)
        {
            diggClass = currentMap.createTopic();
            diggClass.addSubjectIdentifier(loc);
            diggClass.setBaseName("digg");
            diggClass.addType(getWandoraClass());
        }
        return diggClass;
    }




    public Topic getWandoraClass() throws TopicMapException {
        Locator loc = new Locator(TMBox.WANDORACLASS_SI);
        Topic wandoraClass = currentMap.getTopic(loc);
        if(wandoraClass == null)
        {
            wandoraClass = currentMap.createTopic();
            wandoraClass.addSubjectIdentifier(new Locator(TMBox.WANDORACLASS_SI));
            wandoraClass.setBaseName("Wandora class");
        }
        return wandoraClass;
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
    private void extract(Topic t) throws ExtractionFailure
    {
        String topicName = "";
        
        try {
            ArrayList<String> urls = new ArrayList();
            for(Locator l : t.getSubjectIdentifiers()) urls.add(l.toString());
            if(t.getSubjectLocator() != null) urls.add(t.getSubjectLocator().toString());
            
            if(urls.size() == 0)
            {
                log("No urls found for topic " + t.getBaseName());
            }
            
            for(String currentURL : urls) {
                try {
                    JSONObject obj =
                            new JSONObject(IObox.doUrl(new URL(
                            "http://digg.com/tools/services?endPoint=/stories" +
                            "&link=" + url(currentURL) +
                            "&appkey=" + DiggAppKey() +
                            "&type=json")));
                    
                    JSONArray stories = obj.optJSONArray("stories");
                    if(stories == null || stories.length() == 0) {
                        log("No story found for url " + currentURL);
                        continue;
                    }
                    
                    log("Story found for url " + currentURL);

                    JSONObject story = stories.getJSONObject(0);
                    
                    Topic diggT = getDIGGClass();
                    Topic titleOccur = FlickrUtils.createTopic(currentMap, "diggTitle", diggT);
                    Topic descOccur = FlickrUtils.createTopic(currentMap, "diggDesc", diggT);
                    Topic idOccur = FlickrUtils.createTopic(currentMap, "diggID", diggT);
                    Topic containerT = FlickrUtils.createTopic(currentMap, "diggContainer", diggT);
                    Topic contAssoc = FlickrUtils.createTopic(currentMap, "contains", diggT);
                    Topic contTargetT = FlickrUtils.createTopic(currentMap, "diggPost", diggT);
                    
                    Topic lanT = getLanguage(null);
                    
                    t.setData(titleOccur, lanT, story.getString("title"));
                    t.setData(descOccur, lanT, story.getString("description"));
                    t.setData(idOccur, lanT, story.getString("id"));
                    t.addSubjectIdentifier(currentMap.createLocator(story.getString("href")));
                    Topic cont = FlickrUtils.createRaw(
                            currentMap,
                            "http://digg.com/" + FlickrUtils.searchString(story, "container.short_name"),
                            " (digg container)",
                            FlickrUtils.searchString(story, "container.name"),
                            containerT);
                    FlickrUtils.createAssociation(currentMap, contAssoc, new Topic[]{ t, cont }, new Topic[]{ contTargetT, containerT });
                    
                    // story.title -> occur "digg title"
                    // story.description -> occur "digg description"
                    // story.id -> occur "digg id"
                    // story.container.short_name -> topic "digg container", assoc "contains"
                    
                    return;
                }
                catch(MalformedURLException e) { log(e); }
                catch(IOException e) { log(e); }
            }
            
            topicName = t.getBaseName();
        }
        catch(TopicMapException e) { log(e); }
        catch(JSONException e) { log(e); }
        
        log("No stories found for topic " + topicName);
    }
    
    TopicMap currentMap;
}
