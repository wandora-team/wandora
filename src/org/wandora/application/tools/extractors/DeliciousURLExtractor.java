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
 * 
 */

package org.wandora.application.tools.extractors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.extractors.flickr.FlickrUtils;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.IObox;
import org.wandora.application.gui.*;
        
import javax.swing.*;



/**
 *
 * @author anttirt
 */
public final class DeliciousURLExtractor extends AbstractWandoraTool {
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    @Override
    public String getName() {
        return "Delicious URL info extractor";
    }
    @Override
    public String getDescription() {
        return "Tries to find a post in Delicious matching the URL in the SI or SL of a topic and if found, extracts information.";
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
    private static Iterable<Topic> inContext(final Context context) {
        return new Iterable<Topic>() { public Iterator<Topic> iterator() { return context.getContextObjects(); }};
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_delicious.png");
    }


    public void execute(Wandora admin, final Context context) {
        currentMap = admin.getTopicMap();
        
        setDefaultLogger();
        
        if(!context.getContextObjects().hasNext())
        {
            log("No topics found in context! Make sure that you have selected one or more topics.");
        }
        
        for(Topic t : inContext(context))
        {
            try {
                log("Extracting data from topic " + t.getBaseName());
                extract(t);
            }
            catch(ExtractionFailure e) { log(e); }
            catch(TopicMapException e) { log(e); }
        }
        
        setState(WAIT);
    }
    
    private TopicMap currentMap;
    
    private String createHash(String target) throws NoSuchAlgorithmException
    {
        MessageDigest md = null;
        md = MessageDigest.getInstance("MD5");
        Charset ASCII = Charset.forName("ISO-8859-1");
        md.update(target.getBytes(ASCII));
        StringBuilder bldr = new StringBuilder();
        byte[] hash = md.digest();
        for(int i = 0; i < hash.length; ++i)
        {
            bldr.append(String.format("%02x", hash[i]));
        }
        return bldr.toString();
    }
    
    private JSONArray doRequest(Collection<? extends String> targets) throws RequestFailure
    {
        if(targets.isEmpty())
            throw new RequestFailure("No targets specified");
        
        try {
            StringBuilder result = new StringBuilder("http://badges.del.icio.us/feeds/json/url/data");
            char introducer = '?';
            for(String t : targets)
            {
                result.append(introducer + "hash=" + createHash(t));
                introducer = '&';
            }
            return new JSONArray(IObox.doUrl(new URL(result.toString())));
        }
        catch(MalformedURLException e) {
            throw new RequestFailure(e);
        }
        catch(JSONException e) {
            throw new RequestFailure(e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new RequestFailure(e);
        }
        catch(IOException e) {
            throw new RequestFailure(e);
        }
    }



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
    
    private boolean extract(final Topic topic) throws ExtractionFailure, TopicMapException
    {
        ArrayList<String> urlList = new ArrayList<String>();
        
        for(Locator l : topic.getSubjectIdentifiers())
        {
            urlList.add(l.toString());
        }
        
        if(topic.getSubjectLocator() != null)
            urlList.add(topic.getSubjectLocator().toString());
        log("Found " + urlList.size() + " url(s) to check.");
        final JSONArray result = doRequest(urlList);
        
        if(result.length() == 0)
        {
            log("No results available.");
            return false;
        }
        
        Topic deliciousT = getDeliciousClass(currentMap);
        Topic tagT = FlickrUtils.createTopic(currentMap, "delicious tag", deliciousT);
        Topic tagAssoc = FlickrUtils.createTopic(currentMap, "describes", deliciousT);
        Topic tagTargetT = FlickrUtils.createTopic(currentMap, "descriptee", deliciousT);
        
        boolean success = false;
        
        for(int i = 0; i < result.length(); ++i)
        {
            final JSONObject resultObj = result.optJSONObject(i);
            if(resultObj == null)
                throw new RequestFailure("Unable to get inner object");

            final JSONObject tagsObj = resultObj.optJSONObject("top_tags");
            if(tagsObj == null)
            {
                log("No results for url[" + i + "]");
                continue;
            }

            log("Extracting tags");
            for(String s : new Iterable<String>() { public Iterator<String> iterator() { return tagsObj.keys(); }})
            {
                log("Extracting tag " + s);
                Topic tag = FlickrUtils.createRaw(currentMap, "http://delicious.com/tag/" + url(s), " (delicious tag)", s, tagT);
                FlickrUtils.createAssociation(currentMap, tagAssoc, new Topic[] { tag, topic }, new Topic[]{tagT, tagTargetT});
            }
            success = true;
        }
        
        return success;
    }
}