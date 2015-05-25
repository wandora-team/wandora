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

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.Page;
import com.restfb.types.User;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


abstract class AbstractFBGraphExtractor extends AbstractExtractor{
    

    protected static final com.restfb.Version API_VERSION = Version.VERSION_2_3;
    protected static final String URL_ROOT = "https://graph.facebook.com/v2.3/";

    protected static final String AUTH_PAGE = "http://wandora.org/fbv2";
    
    protected static final String NAME = "Facebook Open Graph Extractor";
    
    protected static final String SI_BASE = "http://wandora.org/si/facebook/v2/";
    
    
    protected static String accessToken = null;
    protected static FacebookClient facebookClient = null;
    
    protected static final String[] PERMISSIONS = {
        "public_profile",
        "user_friends",
        "email",
        "user_about_me",
        "user_actions.books",
        "user_actions.fitness",
        "user_actions.music",
        "user_actions.news",
        "user_actions.video",
        "user_birthday",
        "user_education_history",
        "user_events",
        "user_games_activity",
        "user_groups",
        "user_hometown",
        "user_likes",
        "user_location",
        "user_managed_groups",
        "user_photos",
        "user_posts",
        "user_relationships",
        "user_relationship_details",
        "user_religion_politics",
        "user_status",
        "user_tagged_places",
        "user_videos",
        "user_website",
        "user_work_history",
        "read_custom_friendlists",
        "read_insights",
        "read_mailbox",
        "read_page_mailboxes",
        "read_stream",
        "manage_notifications",
        "manage_pages",
        "publish_pages",
        "publish_actions",
        "rsvp_event"
    };
    
    private static final String USER_FIELDS = "id,picture,about,address,age_range,bio,"
            + "birthday,currency,education,email,favorite_athletes,favorite_teams,"
            + "gender,hometown,inspirational_people,interested_in,languages,link,"
            + "location,locale,meeting_for,name,political,relationship_status,"
            + "religion,significant_other,sports,quotes,website,work";
    
    protected static void openBrowser(List<String> perms){
        StringBuilder sb = new StringBuilder(AUTH_PAGE);
        
        if(!perms.isEmpty()){
            sb.append("?perms=");
            sb.append(perms.remove(0));
        }
        for(String perm : perms){
            sb.append(',').append(perm);
        }
        
        Desktop desktop = Desktop.getDesktop();
        
        try {
            desktop.browse(new URI(sb.toString()));
        } catch (URISyntaxException | IOException e) {
            showException(e);
        }
        
    }
    
    protected static void setAccessToken(String token){
        accessToken = token;
    }
    
    protected static void setupClient(){
        if(accessToken == null){
            showException(new Exception("Access token not set"));
            return;
        }
        facebookClient = new DefaultFacebookClient(accessToken, API_VERSION);
    }
    
    protected static void showException(Exception e){
        WandoraOptionPane.showMessageDialog(Wandora.getWandora(),
            e.getMessage(),
            "Encountered an exception",
            WandoraOptionPane.INFORMATION_MESSAGE);

    }

    static List<String> getScopes() {
        
        ArrayList<String> scopes = new ArrayList();
        
        JsonObject permResponse = facebookClient.fetchObject("me/permissions", JsonObject.class);
        JsonArray permsArray = permResponse.getJsonArray("data");
        for (int i = 0; i < permsArray.length(); i++) {
            
            JsonObject perm = permsArray.getJsonObject(i);
            if(perm.getString("status").equals("granted")){
                scopes.add(perm.getString("permission"));
            }
        }
        
        return scopes;
    }
    
    static <T> void extractObject(TopicMap tm, String id, Class<T> type) throws TopicMapException {

        T object = facebookClient.fetchObject(id, type);
        AbstractFBTypeWrapper wrapper = AbstractFBTypeWrapper.create(type, object);

        wrapper.mapToTopicMap(tm);
        
    }

}
