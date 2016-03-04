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

import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author anttirt
 */
public class FlickrPerson {
        String ID;
        String UserName;
        String RealName;
        String Location;
        String PhotosURL;
        String ProfileURL;
        int PhotoCount;

        public FlickrPerson() { }

        public FlickrPerson(JSONObject obj) throws JSONException {
            ID = FlickrUtils.searchString(obj, "nsid");
            UserName = FlickrUtils.searchString(obj, "username._content");
            RealName = FlickrUtils.searchString(obj, "realname._content");
            Location = FlickrUtils.searchString(obj, "location._content");
            PhotosURL = FlickrUtils.searchString(obj, "photosurl._content");
            ProfileURL = FlickrUtils.searchString(obj, "profileurl._content");
            PhotoCount = FlickrUtils.searchInt(obj, "photos.count._content");
        }

        public Topic makeTopic(FlickrExtractor extractor) throws TopicMapException {
            if(ProfileURL == null || ProfileURL.equals("")) {
                ProfileURL = "http://www.flickr.com/people/" + ID + "/";
            }
            
            Topic ret = FlickrUtils.createRaw(
                    extractor.getCurrentMap(), ProfileURL, 
                    " (flickr profile)", UserName,
                    extractor.getTopic(FlickrTopic.Profile));
            ret.setData(extractor.getOccurrence(FlickrOccur.NSID), extractor.getLanguage(null), ID);
            return ret;
        }
}
