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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Tuples.T2;

/**
 *
 * @author anttirt
 */
public class PersonInfoExtractor extends FlickrExtractor {
    private static final int photosPerPage = 250;

    private FlickrPerson curPerson;
    private Topic curPersonTopic;



    private Collection<T2<FlickrPerson, Topic>> promptForUsers(Wandora admin) throws ExtractionFailure {
        ArrayList<T2<FlickrPerson, Topic>> people = new ArrayList();
        
        ChooseUserDialog dlg = new ChooseUserDialog(admin, true, null);
        setState(INVISIBLE);
        dlg.setVisible(true);
        if(dlg.wasCancelled()) {
            return null;
        }
        setState(VISIBLE);

        for(String username : dlg.getUserList()) {
            try {
                SortedMap<String, String> userInfoArgs = new TreeMap();
                userInfoArgs.put("username", username);
                JSONObject obj = getFlickrState().unauthorizedCall("flickr.people.findByUsername", userInfoArgs);

                if(!"ok".equals(obj.getString("stat")))
                    continue;

                FlickrPerson p = new FlickrPerson();
                p.UserName = FlickrUtils.searchString(obj, "user.username._content");
                p.ID = FlickrUtils.searchString(obj, "user.nsid");
                people.add(new T2<FlickrPerson, Topic>(p, p.makeTopic(this)));
            }
            catch(JSONException e) {
                throw new ExtractionFailure(e);
            }
            catch(RequestFailure e) {
                throw new ExtractionFailure(e);
            }
            catch(TopicMapException e) {
                throw new ExtractionFailure(e);
            }
        }
        
        return people;
    }



    @Override
    protected boolean extract(Wandora admin, Context context) throws ExtractionFailure {
        Collection<T2<FlickrPerson, Topic>> people = null;
        Topic profileT = null;
        Topic nsidT = null;
        Topic langT = null;

        try {
            profileT = getTopic(FlickrTopic.Profile);
            nsidT = getOccurrence(FlickrOccur.NSID);
            langT = getLanguage(null);
        }
        catch(TopicMapException e) {
            throw new ExtractionFailure(e);
        }
        
        Collection<Topic> contextUserTopics = getWithType(context, profileT);
        
        if(contextUserTopics.isEmpty()) {
            people = promptForUsers(admin);
            if(people == null) return false; // user cancelled
        }
        else {
            people = new ArrayList<T2<FlickrPerson, Topic>>();
            
            for(Topic t : contextUserTopics) {
                try {
                    FlickrPerson p = new FlickrPerson();
                    p.UserName = t.getDisplayName();
                    p.ID = t.getData(nsidT, langT);
                    people.add(new T2<FlickrPerson, Topic>(p, t));
                }
                catch(TopicMapException e) {
                    log(e);
                }
            }
        }
        if(people.isEmpty()) {
            log("Found no profiles to look up!");
        }
        else {
            log("Found a total of " + people.size() + " profiles to look up");
        }
        
        for(T2<FlickrPerson, Topic> p : people) {
            try {
                curPerson = p.e1;
                curPersonTopic = p.e2;
                
                log("Getting profile info for " + p.e1.UserName);
                people_getInfo(admin);
                if(forceStop()) return true;
                log("Getting public photo list for " + p.e1.UserName);
                getPhotoList(admin, "flickr.people.getPublicPhotos", FlickrAssoc.Ownership, "owned");
                if(forceStop()) return true;
                log("Getting favorite photo list for " + p.e1.UserName);
                getPhotoList(admin, "flickr.favorites.getList", FlickrAssoc.Favorite, "marked as favorite");
                if(forceStop()) return true;
                log("Getting public group list for " + p.e1.UserName);
                people_getPublicGroups(admin);
                if(forceStop()) return true;
            }
            catch(TopicMapException e) {
                log(e);
            }
            catch(RequestFailure e) {
                log(e);
            }
            catch(JSONException e) {
                log(e);
            }
            catch(UserCancellation e) {
                log("User cancelled");
            }
        }
        
        log("Ok.");
        return people.size() > 0;
    }
    
    
    
    private void getPhotoList(Wandora currentAdmin, String jsonAPI, FlickrAssoc association, String relationship) throws JSONException, TopicMapException, RequestFailure, ExtractionFailure, UserCancellation {
        int totalPhotos = 0;
        int photosReceived = 0;

        TreeMap<String, String> args = new TreeMap();
        args.put("user_id", curPerson.ID);
        args.put("extras", "date_taken,date_upload,o_dims,geo,last_update,license,media,owner_name,tags,views");
        args.put("per_page", "" + photosPerPage);

        JSONObject result = getFlickrState().authorizedCall(jsonAPI, args, FlickrState.PermRead, currentAdmin);

        totalPhotos = FlickrUtils.searchInt(result, "photos.total");
        final int pageCount = 1 + totalPhotos / photosPerPage;
        
        getCurrentLogger().setProgressMax(totalPhotos);
        log("-- Getting info for " + totalPhotos + " photos " + relationship + " by " + curPerson.UserName + ".");
        for(int nextPageIndex = 2; nextPageIndex <= (pageCount + 1); ++nextPageIndex) {
            getCurrentLogger().setProgress(photosReceived);
            JSONArray photosArray = FlickrUtils.searchJSONArray(result, "photos.photo");
            int received = photosArray.length();
            log("-- -- Getting info for photos " + (photosReceived + 1) + " - " + (photosReceived + received) + " out of " + totalPhotos);
            photosReceived += received;

            for(int i = 0; i < received; ++i) {
                FlickrPhoto p = FlickrPhoto.makeFromPublicPhotoList(photosArray.getJSONObject(i));
                Topic photoTopic = p.makeTopic(this);
                FlickrUtils.createAssociation(currentMap, getAssociation(association), new Topic[] { photoTopic, curPersonTopic });
            }
            
            if(forceStop()) {
                log("-- -- Cancellation requested; finished getting info for " + photosReceived + " out of " + totalPhotos + " photos.");
                break;
            }
            
            args.clear();
            args.put("user_id", curPerson.ID);
            args.put("extras", "date_taken,date_upload,o_dims,geo,last_update,license,media,owner_name,tags,views");
            args.put("per_page", "" + photosPerPage);
            args.put("page", "" + nextPageIndex);
            result = getFlickrState().authorizedCall(jsonAPI, args, FlickrState.PermRead, currentAdmin);

        }
        if(photosReceived < totalPhotos) {
            log("" + (totalPhotos - photosReceived) + " photos not sent by flickr");
        }
    }
    
    
    private void people_getPublicGroups(Wandora wandora) throws JSONException, TopicMapException, RequestFailure, ExtractionFailure {
        TreeMap<String, String> args = new TreeMap();
        args.put("user_id", curPerson.ID);

        JSONObject result = getFlickrState().unauthorizedCall("flickr.people.getPublicGroups", args);

        {
            JSONArray groupArray = FlickrUtils.searchJSONArray(result, "groups.group");
            log("-- Getting info for " + groupArray.length() + " groups.");
            getCurrentLogger().setProgressMax(groupArray.length());
            for(int i = 0; i < groupArray.length() && !forceStop(); ++i) {
                getCurrentLogger().setProgress(i);
                FlickrGroup g = new FlickrGroup(groupArray.getJSONObject(i));
                Topic groupTopic = g.makeTopic(this);
                FlickrUtils.createAssociation(currentMap, getAssociation(FlickrAssoc.Membership), new Topic[] { groupTopic, curPersonTopic });
            }
            getCurrentLogger().setProgress(groupArray.length());
        }
    }
    
    
    private void people_getInfo(Wandora wandora) throws JSONException, TopicMapException, RequestFailure, ExtractionFailure {
        TreeMap<String, String> args = new TreeMap();
        args.put("user_id", curPerson.ID);
        JSONObject response = getFlickrState().unauthorizedCall("flickr.people.getInfo", args);
        throwOnAPIError(response);

        try {
            curPersonTopic.setData(
                    getOccurrence(FlickrOccur.Location),
                    getLanguage(null),
                    FlickrUtils.searchString(response, "person.location._content"));
        }
        catch(JSONException e) { }
    }
    
    
    
    @Override
    public String getDescription() {
        return "Flickr person info extractor reads Flickr user profile and converts it to a topic map.";
    }
    
    
    @Override
    public String getName() {
        return "Flickr person info extractor";
    }
    
}
