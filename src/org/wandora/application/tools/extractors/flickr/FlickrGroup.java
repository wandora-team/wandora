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


package org.wandora.application.tools.extractors.flickr;

import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author anttirt
 */
public class FlickrGroup {
    public String Name;
    public String ID;
    
    public FlickrGroup(JSONObject obj) throws JSONException {
        Name = obj.getString("name");
        ID = obj.getString("nsid");
    }
    
    public FlickrGroup() {}
    
    public Topic makeTopic(FlickrExtractor extractor) throws TopicMapException {
        Topic ret = FlickrUtils.createRaw(extractor.getCurrentMap(), "http://www.flickr.com/groups/" + ID + "/", " (flickr group)", Name, extractor.getTopic(FlickrTopic.Group));
        ret.setData(extractor.getOccurrence(FlickrOccur.NSID), extractor.getLanguage(null), ID);
        TreeMap<String, String> args = new TreeMap();
        args.put("group_id", ID);
        try {
            JSONObject obj = extractor.getFlickrState().unauthorizedCall("flickr.urls.getGroup", args);
            ret.addSubjectIdentifier(new Locator(FlickrUtils.searchString(obj, "group.url")));
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        catch(FlickrExtractor.RequestFailure e) {
            e.printStackTrace();
        }
        return ret;
    }
}
