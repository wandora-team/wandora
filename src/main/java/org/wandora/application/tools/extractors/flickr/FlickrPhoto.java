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
 * 
 */


package org.wandora.application.tools.extractors.flickr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author anttirt
 */
public class FlickrPhoto {
    public String Title;
    public String OwnerName;
    public String OwnerID;
    public String ID;
    public ArrayList<String> Tags;
    
    public String Description;
    
    public String LastUpdate;
    public String DateTaken;
    public String DateUpload;
    
    public String Secret;
    public String Media;
    
    public double Latitude, Longitude;
    
    public Integer FarmID, ServerID;
    
    public Integer License;
    
    private static SimpleDateFormat ISO8601Format;
    
    public FlickrPhoto() {}


    static FlickrPhoto makeFromPhotoInfo(JSONObject obj) throws JSONException {
        FlickrPhoto ret = new FlickrPhoto();
        if(ISO8601Format == null) {
            ISO8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        
        ret.ID = obj.getString("id");
        ret.Title = FlickrUtils.searchString(obj, "title._content");
        ret.OwnerName = FlickrUtils.searchString(obj, "owner.username");
        ret.OwnerID = FlickrUtils.searchString(obj, "owner.nsid");
        ret.Tags = new ArrayList<>();
        JSONArray tagsArray = FlickrUtils.searchJSONArray(obj, "tags.tag");
        for(int i = 0; i < tagsArray.length(); ++i) {
            JSONObject tagObj = tagsArray.getJSONObject(i);
            ret.Tags.add(tagObj.getString("_content"));
        }
        
        ret.DateTaken = FlickrUtils.searchString(obj, "dates.taken");
        ret.DateUpload = FlickrUtils.searchString(obj, "dates.posted");
        ret.LastUpdate = FlickrUtils.searchString(obj, "dates.lastupdate");
        
        ret.FarmID = obj.getInt("farm");
        ret.ServerID = obj.getInt("server");
        
        ret.Secret = obj.optString("secret");
        
        ret.License = obj.getInt("license");
        
        JSONObject descObj = obj.optJSONObject("description");
        if(descObj != null) {
            ret.Description = descObj.getString("_content");
        }
        
        return ret;
    }



    static FlickrPhoto makeFromPublicPhotoList(JSONObject obj) throws JSONException {
        FlickrPhoto ret = new FlickrPhoto();
        if(ISO8601Format == null) {
            ISO8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        
        ret.Title = obj.getString("title");
        ret.OwnerName = obj.getString("ownername");
        ret.OwnerID = obj.getString("owner");
        ret.ID = obj.getString("id");
        ret.Tags = new ArrayList<String>(Arrays.asList(obj.getString("tags").split(" ")));
        
        ret.LastUpdate = ISO8601Format.format(new Date(obj.getLong("lastupdate")));
        ret.DateTaken = obj.getString("datetaken");
        ret.DateUpload = ISO8601Format.format(new Date(obj.getLong("dateupload")));
        
        ret.Secret = obj.getString("secret");
        ret.Media = obj.getString("media");
        
        ret.Latitude = obj.getLong("latitude");
        ret.Longitude = obj.getLong("longitude");
        
        ret.FarmID = obj.getInt("farm");
        ret.ServerID = obj.getInt("server");
        
        ret.Secret = obj.optString("secret");
        ret.License = obj.getInt("license");
        
        return ret;
    }



    Topic makeTopic(FlickrExtractor extractor) throws TopicMapException {
        if("".equals(Title))
            Title = "(unnamed)";
        
        String baseStr = "http://www.flickr.com/photos/" + OwnerID + "/" + ID + "/";
        Topic photoTopic = FlickrUtils.createRaw(extractor.getCurrentMap(), baseStr, " (flickr photo " + ID + ")", Title, extractor.getTopic(FlickrTopic.Photo));
        photoTopic.setData(extractor.getOccurrence(FlickrOccur.PhotoID), extractor.getLanguage(null), ID);
        
        if(FarmID != null && ServerID != null && Secret != null) {
            StringBuilder bldr = new StringBuilder("http://farm");
            bldr.append(FarmID);
            bldr.append(".static.flickr.com/");
            bldr.append(ServerID);
            bldr.append('/');
            bldr.append(ID);
            bldr.append('_');
            bldr.append(Secret);
            bldr.append("_d.jpg");
            
            photoTopic.setSubjectLocator(new Locator(bldr.toString()));
        }
        
        if(Description != null) {
            photoTopic.setData(extractor.getOccurrence(FlickrOccur.Description), extractor.getLanguage(null), Description);
        }
        
        if(License != null) {
            Topic licenseT = extractor.getLicenseTopic(License);
            if(licenseT != null)
                FlickrUtils.createAssociation(extractor.getCurrentMap(), extractor.getAssociation(FlickrAssoc.License), new Topic[] { licenseT, photoTopic });
        }
        
        for(String tag : Tags) {
            if("".equals(tag))
                continue;

            String tagBaseStr = "http://www.flickr.com/photos/tags/" + tag + "/";
            Topic tagTopic = FlickrUtils.createRaw(extractor.getCurrentMap(), tagBaseStr, " (flickr tag)", tag, extractor.getTopic(FlickrTopic.Tag));
            if(photoTopic == null)
                throw new TopicMapException("Null photoTopic");
            if(tagTopic == null)
                throw new TopicMapException("Null tagTopic");
            FlickrUtils.createAssociation(extractor.getCurrentMap(), extractor.getAssociation(FlickrAssoc.Description), new Topic[] { photoTopic, tagTopic });
        }
        
        if(OwnerID != null) {
            FlickrPerson p = new FlickrPerson();
            p.UserName = OwnerName;
            p.ID = OwnerID;
            Topic personTopic = p.makeTopic(extractor);
            
            if(photoTopic == null)
                throw new TopicMapException("Null photoTopic");
            if(personTopic == null)
                throw new TopicMapException("Null personTopic");
            
            FlickrUtils.createAssociation(extractor.getCurrentMap(), extractor.getAssociation(FlickrAssoc.Ownership), new Topic[] {photoTopic, personTopic});
        }
        return photoTopic;
    }
}
