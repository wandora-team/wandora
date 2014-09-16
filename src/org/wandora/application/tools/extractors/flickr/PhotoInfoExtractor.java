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


package org.wandora.application.tools.extractors.flickr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


import org.wandora.application.tools.extractors.geonames.*;


/**
 *
 * @author anttirt
 */
public class PhotoInfoExtractor extends FlickrExtractor {

    public boolean MAKE_GPS_OCCURRENCES = true;
    

    private FlickrPhoto photo;
    private Topic photoTopic;
    private Wandora currentAdmin;
    
    
    
    @Override
    public String getDescription() {
        return "Reads Flickr photo info and converts it to a topic map.";
    }
    
    
    @Override
    public String getName() {
        return "Flickr photo info extractor";
    }
    
    
    @Override
    protected boolean extract(Wandora admin, Context context) throws ExtractionFailure {
        currentAdmin = admin;
        Collection<Topic> photoTopics = null;
        Topic photoT = null;
        try {
            photoT = getTopic(FlickrTopic.Photo);
        }
        catch(TopicMapException e) {
            throw new ExtractionFailure(e);
        }
        
        photoTopics = getWithType(context, photoT);
        
        if(photoTopics.isEmpty()) {
            log("Found no photo topics to look up!");
            log("To extract information about photos you need to select Flickr photo topics first!");
            log("Flickr photo topic has an occurrence with Flickr photo id.");
        }
        else {
            log("Found a total of " + photoTopics.size() + " photo topics to look up!");
        }
        
        for(Topic t : photoTopics) {
            try {
                photoTopic = t;
                String photoID = photoTopic.getData(getOccurrence(FlickrOccur.PhotoID), getLanguage(null));
                log("Extracting information for photo " + photoTopic.getDisplayName());
                TreeMap<String, String> args = new TreeMap();
                args.put("photo_id", photoID);
                JSONObject response = getFlickrState().unauthorizedCall("flickr.photos.getInfo", args);
                throwOnAPIError(response);
                photo = FlickrPhoto.makeFromPhotoInfo(response.getJSONObject("photo"));

                try { photos_getExif(); } catch(RequestFailure e) { log(e); } catch(JSONException e) { log(e); } catch(TopicMapException e) { log(e); }
                try { photos_geo_getLocation(); } catch(RequestFailure e) { log(e); } catch(JSONException e) { log(e); } catch(TopicMapException e) { log(e); }
                photo.makeTopic(this);
            }
            catch(RequestFailure e) {
                log(e);
            }
            catch(JSONException e) {
                log(e);
            }
            catch(TopicMapException e) {
                log(e);
            }
            catch(UserCancellation e) {
                log("User cancelled");
            }
        }
        
        log("Ok.");
        return photoTopics.size() > 0;
    }
    
    
    
    private void photos_geo_getLocation() throws RequestFailure, JSONException, TopicMapException {
        TreeMap<String, String> args = new TreeMap();
        args.put("photo_id", photo.ID);
        JSONObject response = getFlickrState().unauthorizedCall("flickr.photos.geo.getLocation", args);
        if(response.getString("stat").equals("ok")) {
            photo.Latitude = FlickrUtils.searchDouble(response, "photo.location.latitude");
            photo.Longitude = FlickrUtils.searchDouble(response, "photo.location.longitude");

            if(MAKE_GPS_OCCURRENCES) {
                Topic flickrTopic = getFlickrClass();
                Topic latT = FlickrUtils.createTopic(currentMap, "latitude", flickrTopic);
                Topic lonT = FlickrUtils.createTopic(currentMap, "longitude", flickrTopic);

                photoTopic.setData(latT, getLanguage(null), String.valueOf(photo.Latitude));
                photoTopic.setData(lonT, getLanguage(null), String.valueOf(photo.Longitude));
            }
            else {
                AbstractGeoNamesExtractor.makeLatLong(String.valueOf(photo.Latitude), String.valueOf(photo.Longitude), photoTopic, photoTopic.getTopicMap());
            }
        }
    }
    
    
    private void photos_getExif() throws RequestFailure, JSONException, TopicMapException, UserCancellation {
        TreeMap<String, String> args = new TreeMap();
        args.put("photo_id", photo.ID);
        JSONObject exifResponse = getFlickrState().authorizedCall("flickr.photos.getExif", args, FlickrState.PermRead, currentAdmin);
        throwOnAPIError(exifResponse);
        JSONArray exifArray = FlickrUtils.searchJSONArray(exifResponse, "photo.exif");
        
        Topic flickrTopic = getFlickrClass();
        
        Topic xResT = FlickrUtils.createTopic(currentMap, "x-resolution", flickrTopic);
        Topic yResT = FlickrUtils.createTopic(currentMap, "y-resolution", flickrTopic);
        
        Topic xDimT = FlickrUtils.createTopic(currentMap, "x-dimension", flickrTopic);
        Topic yDimT = FlickrUtils.createTopic(currentMap, "y-dimension", flickrTopic);
        
        Topic resUnitT = FlickrUtils.createTopic(currentMap, "resolutionUnit", flickrTopic);
        Topic dateTimeT = FlickrUtils.createTopic(currentMap, "dateTime", flickrTopic);
        Topic exposureT = FlickrUtils.createTopic(currentMap, "exposure", flickrTopic);
        Topic apertureT = FlickrUtils.createTopic(currentMap, "aperture", flickrTopic);
        
        Topic focalLenT = FlickrUtils.createTopic(currentMap, "focalLength", flickrTopic);
        Topic colorSpaceT = FlickrUtils.createTopic(currentMap, "colorSpace", flickrTopic);
        Topic spaceAssocT = FlickrUtils.createTopic(currentMap, "isInSpace", flickrTopic);
        
        HashMap<String, JSONObject> exifTable = new HashMap<String, JSONObject>();
        
        for(int i = 0; i < exifArray.length(); ++i) {
            try {
                JSONObject exifObj = exifArray.getJSONObject(i);
                exifTable.put(exifObj.getString("label"), exifObj);
            }
            catch(JSONException e) {
                log(e);
                continue;
            }
        }

        try {
            if(exifTable.get("Make") != null && exifTable.get("Model") != null) {
                Topic makeT = FlickrUtils.createTopic(currentMap, "cameraMaker", flickrTopic);
                Topic cameraT = FlickrUtils.createTopic(currentMap, "camera", flickrTopic);
                Topic makerAssoc = FlickrUtils.createTopic(currentMap, "madeBy", flickrTopic);
                Topic takenWithAssoc = FlickrUtils.createTopic(currentMap, "takenWith", flickrTopic);
                
                String cameraMakerName = FlickrUtils.searchString(exifTable.get("Make"), "raw._content");
                String cameraModelName = FlickrUtils.searchString(exifTable.get("Model"), "raw._content");
                Topic curCameraTopic = FlickrUtils.createTopic(currentMap, url(cameraModelName), " (camera)", cameraModelName, cameraT);
                Topic curCameraMaker = FlickrUtils.createTopic(currentMap, url(cameraMakerName), " (camera maker)", cameraMakerName, makeT);
                FlickrUtils.createAssociation(currentMap, makerAssoc, new Topic[] { curCameraMaker, curCameraTopic });
                FlickrUtils.createAssociation(currentMap, takenWithAssoc, new Topic[] { curCameraTopic, photoTopic });
            }
        }
        catch(JSONException e) {
            log(e);
        }
        
        //setDataIfNotNull(exifTable.get("Make"), "raw._content", makeT);
        //setDataIfNotNull(exifTable.get("Model"), "raw._content", modelT);
        setDataIfNotNull(exifTable.get("X-Resolution"), "raw._content", xResT);
        setDataIfNotNull(exifTable.get("Y-Resolution"), "raw._content", yResT);
        setDataIfNotNull(exifTable.get("Pixel X-Dimension"), "raw._content", xDimT);
        setDataIfNotNull(exifTable.get("Pixel Y-Dimension"), "raw._content", yDimT);
        setDataIfNotNull(exifTable.get("Resolution Unit"), "raw._content", resUnitT);
        setDataIfNotNull(exifTable.get("Exposure"), "raw._content", exposureT);
        setDataIfNotNull(exifTable.get("Aperture"), "raw._content", apertureT);
        setDataIfNotNull(exifTable.get("Focal Length"), "raw._content", focalLenT);
        
        JSONObject colorSpaceObj = exifTable.get("Color Space");
        if(colorSpaceObj != null) {
            try {
                String colorSpaceName = FlickrUtils.searchString(colorSpaceObj, "clean._content");
                Topic spaceT = FlickrUtils.createTopic(currentMap, url(colorSpaceName), " (color space)", colorSpaceName, colorSpaceT);
                FlickrUtils.createAssociation(currentMap, spaceAssocT, new Topic[] { spaceT, photoTopic });
            }
            catch(JSONException e) {
            }
        }
    }
    
    
    
    private void setDataIfNotNull(JSONObject exifObj, String path, Topic type) {
        if(exifObj == null) return;
        
        try {
            photoTopic.setData(type, getLanguage(null), FlickrUtils.searchString(exifObj, path));
        }
        catch(TopicMapException e) {
            log(e);
        }
        catch(JSONException e) {
            log(e);
        }
    }
}
