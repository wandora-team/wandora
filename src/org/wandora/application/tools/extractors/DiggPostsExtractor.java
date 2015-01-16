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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.wandora.application.tools.extractors.flickr.ChooseUserDialog;
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
public class DiggPostsExtractor extends AbstractWandoraTool {
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    @Override
    public String getName() {
        return "Digg profile posts extractor";
    }
    
    @Override
    public String getDescription() {
        return "Creates topics for posts made by one or more users in Digg. The subject identifiers of the topics are set to the URLs and will be merged with any topics in the map that already have the same SI.";
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
    
    public static class JSONArrayIterable<T> implements Iterable<T> {
        public class Iter implements Iterator<T> {
            private int index;

            public boolean hasNext() {
                return !atEnd();
            }

            public T next() {
                Object obj = array.opt(index);
                ++index;
                return (T)obj;
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            private boolean atEnd() {
                // find next object that can be returned
                for(; index < array.length(); ++index)
                {
                    Object obj = array.opt(index);
                    
                    if(obj == null)
                        continue;
                    
                    if(!obj.getClass().equals(elemClass))
                        continue;
                    
                    break;
                }
                
                if(index == array.length())
                    return true;
                else
                    return false;
            }
            
            public Iter() { index = 0; }
        }

        public Iterator<T> iterator() {
            return new Iter();
        }
        
        private JSONArray array;
        private Class elemClass;
        
        public JSONArrayIterable(JSONArray cont, Class req)
        {
            array = cont;
            elemClass = req;
        }
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
    public void execute(Wandora admin, Context context) {
        currentMap = admin.getTopicMap();
        
        ChooseUserDialog dlg = new ChooseUserDialog(admin, true, "<html><body><p>Please enter a comma-delimited list of Digg usernames to fetch information for.</p></body></html>");
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
            log("Extracting data for user " + user);
            try { extract(user); }
            catch(ExtractionFailure e) { log(e); }
            catch(TopicMapException e) { log(e); }
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
    private TopicMap currentMap;

    private void extract(String user) throws ExtractionFailure, TopicMapException {
        try {
            JSONObject obj =
                new JSONObject(IObox.doUrl(new URL(
                    "http://digg.com/tools/services?endPoint=/user/" + user + "/submissions" +
                    "&appkey=" + DiggAppKey() +
                    "&type=json")));
            
            JSONArray storyArray = obj.optJSONArray("stories");
            if(storyArray == null)
            {
                log("No posts found for user " + user);
                return;
            }
            
            log("Checking " + storyArray.length() + " stories");
            
            Topic diggT = getDIGGClass();
            Topic titleOccur = FlickrUtils.createTopic(currentMap, "diggTitle", diggT);
            Topic descOccur = FlickrUtils.createTopic(currentMap, "diggDesc", diggT);
            Topic idOccur = FlickrUtils.createTopic(currentMap, "diggID", diggT);
            Topic containerT = FlickrUtils.createTopic(currentMap, "diggContainer", diggT);
            Topic contAssoc = FlickrUtils.createTopic(currentMap, "contains", diggT);
            Topic contTargetT = FlickrUtils.createTopic(currentMap, "diggPost", diggT);

            Topic lanT = getLanguage(null);
            
            for(JSONObject story : new JSONArrayIterable<JSONObject>(storyArray, JSONObject.class))
            {
                log("Checking story " + story.getString("title"));
                
                Topic t = FlickrUtils.createRaw(currentMap, story.getString("href"), " (digg post)", story.getString("title"), contTargetT);
                    
                t.setData(titleOccur, lanT, story.getString("title"));
                t.setData(descOccur, lanT, story.getString("description"));
                t.setData(idOccur, lanT, story.getString("id"));
                t.addSubjectIdentifier(currentMap.createLocator(story.getString("link")));
                
                Topic cont = FlickrUtils.createRaw(
                        currentMap,
                        "http://digg.com/" + FlickrUtils.searchString(story, "container.short_name"),
                        " (digg container)",
                        FlickrUtils.searchString(story, "container.name"),
                        containerT);
                FlickrUtils.createAssociation(currentMap, contAssoc, new Topic[]{ t, cont }, new Topic[]{ contTargetT, containerT });
            }
        }
        catch(MalformedURLException e) {}
        catch(JSONException e) {}
        catch(ExtractionFailure e) {}
        catch(IOException e) {}
    }
}
