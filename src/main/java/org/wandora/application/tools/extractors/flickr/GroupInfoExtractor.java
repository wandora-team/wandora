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

import java.util.Collection;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author anttirt
 */
public class GroupInfoExtractor extends FlickrExtractor {


	private static final long serialVersionUID = 1L;
	
	private FlickrGroup curGroup;
    private Topic curGroupTopic;
        
    @Override
    public String getDescription() {
        return "Extracts information from a flickr group. Gets the list of photos in a group's pool.";
    }
    @Override
    public String getName() {
        return "Flickr group info extractor";
    }
    
    
    
    
    
    @Override
    protected boolean extract(Wandora wandora, Context context) throws ExtractionFailure {
        Collection<Topic> groupTopics = null;
        Topic groupT = null;
        
        try {
            groupT = getTopic(FlickrTopic.Group);
        }
        catch(TopicMapException e) {
            throw new ExtractionFailure(e);
        }
        
        groupTopics = getWithType(context, groupT);

        if(groupTopics.isEmpty()) {
            log("Unable to find any groups in context.");
            GroupSearchDialog dlg = new GroupSearchDialog(wandora, true, getFlickrState(), this);
            setState(INVISIBLE);
            dlg.setVisible(true);
            setState(VISIBLE);
            if(dlg.wasCancelled()) {
                log("User cancelled.");
                return false;
            }
            
            groupTopics = dlg.selection();
        }
        else {
            log("Looking up info for " + groupTopics.size() + " groups.");
        }
        
        for(Topic t : groupTopics) {
            try {
                curGroup = new FlickrGroup();
                curGroup.ID = t.getData(getOccurrence(FlickrOccur.NSID), getLanguage(null));
                curGroup.Name = t.getDisplayName();
                curGroupTopic = t;
                log("Getting info for group " + curGroup.Name);
                getPhotoList(wandora, "flickr.groups.pools.getPhotos", FlickrAssoc.InGroupPool, "in the photo pool of ");
            }
            catch(JSONException e) {
                log(e);
            }
            catch(RequestFailure e) {
                log(e);
            }
            catch(TopicMapException e) {
                log(e);
            }
            catch(UserCancellation e) {
                log("User cancelled.");
            }
        }
        return !groupTopics.isEmpty();
    }
    
    
    
    private static final int photosPerPage = 250;
    
    private void getPhotoList(Wandora currentAdmin, String jsonAPI, FlickrAssoc association, String relationship) throws JSONException, TopicMapException, RequestFailure, ExtractionFailure, UserCancellation {
        int totalPhotos = 0;
        int photosReceived = 0;
  
        TreeMap<String, String> args = new TreeMap();
        args.put("group_id", curGroup.ID);
        args.put("extras", "date_taken,date_upload,o_dims,geo,last_update,license,media,owner_name,tags,views");
        args.put("per_page", "" + photosPerPage);

        JSONObject result = getFlickrState().authorizedCall(jsonAPI, args, FlickrState.PermRead, currentAdmin);

        totalPhotos = FlickrUtils.searchInt(result, "photos.total");
        final int pageCount = 1 + totalPhotos / photosPerPage;
        getCurrentLogger().setProgressMax(totalPhotos);
        log("-- Getting info for " + totalPhotos + " photos in " + curGroup.Name + "'s pool.");
        for(int nextPageIndex = 2; nextPageIndex <= (pageCount + 1); ++nextPageIndex) {
            getCurrentLogger().setProgress(photosReceived);
            JSONArray photosArray = FlickrUtils.searchJSONArray(result, "photos.photo");
            int received = photosArray.length();
            log("-- -- Getting info for photos " + (photosReceived + 1) + " - " + (photosReceived + received) + " out of " + totalPhotos);

            for(int i = 0; i < received; ++i) {
                FlickrPhoto p = FlickrPhoto.makeFromPublicPhotoList(photosArray.getJSONObject(i));
                Topic photoTopic = p.makeTopic(this);
                FlickrUtils.createAssociation(currentMap, getAssociation(association), new Topic[] { photoTopic, curGroupTopic });
            }

            photosReceived += received;
            /*
            if(photosReceived >= totalPhotos || received <= perPage)
            {
                break;
            }
            */
            if(forceStop()) {
                log("-- -- Cancellation requested; finished getting info for " + photosReceived + " out of " + totalPhotos + " photos.");
                break;
            }
            
            args.clear();
            args.put("group_id", curGroup.ID);
            args.put("extras", "date_taken,date_upload,o_dims,geo,last_update,license,media,owner_name,tags,views");
            args.put("per_page", "" + photosPerPage);
            args.put("page", "" + nextPageIndex);
            result = getFlickrState().authorizedCall(jsonAPI, args, FlickrState.PermRead, currentAdmin);
        }
        
        if(photosReceived < totalPhotos) {
            log("" + (totalPhotos - photosReceived) + " photos not sent by flickr");
        }
    }
}
