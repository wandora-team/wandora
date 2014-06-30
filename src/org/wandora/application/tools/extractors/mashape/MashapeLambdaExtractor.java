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
 */

package org.wandora.application.tools.extractors.mashape;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import com.mashape.unirest.http.*;
import com.mashape.unirest.http.Unirest;
import org.wandora.dep.json.*;

import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen
 */


public class MashapeLambdaExtractor extends AbstractMashapeExtractor{
    
    private static final String endpoint
        = "https://lambda-face-detection-and-recognition.p.mashape.com/detect";
    
    
    private static final String API_SI 
        = "https://lambda-face-detection-and-recognition.p.mashape.com";
    
    private static final String API_NAME = "Lambda API";    
    private static final String SI_ROOT = "http://wandora.org/si/mashape/lambda";
    
    private static final String TAG_SI          = SI_ROOT + "/tag";
    private static final String FACE_SI         = SI_ROOT + "/face";
    private static final String PHOTO_SI        = SI_ROOT + "/photo";
    private static final String SMILE_SI        = SI_ROOT + "/smiling";
    private static final String WIDTH_SI        = SI_ROOT + "/width";
    private static final String HEIGHT_SI       = SI_ROOT + "/height";
    private static final String CONFIDENCE_SI   = SI_ROOT + "/confidence";

    private static final  String[][] typeStrings = {
        {"width",       WIDTH_SI,      "Width"},
        {"height",      HEIGHT_SI,     "Height"},
        {"photo",       PHOTO_SI,      "Photo"},
        {"tagged",      TAG_SI,        "Tagged"},
        {"face",        FACE_SI,       "Face"},
        {"smiling",     SMILE_SI,      "Smiling"},
        {"condifence",  CONFIDENCE_SI, "Confidence"}
    };
    
    private String api_key;
   
    public void setApiKey(String key){
        this.api_key = key;
    }
    
     // -------------------------------------------------------------------------
   
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        FileInputStream is = new FileInputStream(f);
        String query = IOUtils.toString(is);
        _extractTopicsFrom(query, tm);
        return true;
    
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        String currentURL = u.toExternalForm();
        extractTopicsFromText(currentURL, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {        
        log("Querying: " +  str);
        
        HttpResponse<JsonNode> resp = Unirest.get(str)
            .header("X-Mashape-Authorization", api_key)
            .asJson();
        
        System.out.println(IOUtils.toString(resp.getRawBody()));
        
        parse(resp, tm);
        return true;
    }
    
    // -------------------------------------------------------------------------
    
    private void parse(HttpResponse<JsonNode> json, TopicMap tm){
        try {
            
            JsonNode jsonBody = json.getBody();
            JSONObject body = jsonBody.getObject();
            
            if(!body.has("status") && body.has("message")){
                log(body.getString("message"));
            }
            
            String status = body.getString("status");
            if(!status.equals("success")){
                throw new Exception("Query failed: no result");
            }
            
            Topic lang = getLangTopic(tm);
            Topic api = getAPIClass(tm, API_SI, API_NAME);
            HashMap<String, Topic> types = getTypes(tm,typeStrings, api);
            
            JSONArray photoJSON = body.getJSONArray("photos");
            
            log("found " + photoJSON.length() + " photoJSON");
            
            for (int i = 0; i < photoJSON.length(); i++) {
                this.parsePhoto(photoJSON.getJSONObject(i), tm, types,lang);
            }
            
        } catch(JSONException jse){
            log(jse.getMessage());
        } catch (org.wandora.dep.apache.http.NoHttpResponseException httpe){
            log("No response from the server");
        } catch (IOException ioe) {
            log(ioe.getMessage());
            log(ioe);
        } catch (Exception e){
            e.printStackTrace();
            log(e.getMessage());
        } 
    }
    
    private void parsePhoto(JSONObject photoJSON, TopicMap tm, 
                            HashMap<String,Topic> types, Topic lang){
        try {
            
            String url = photoJSON.getString("url");
            Topic photo = getTopic(tm, types.get("photo"), url, null);
            photo.setSubjectLocator(new Locator(url));
            
            photo.setData(types.get("height"), lang, 
                          photoJSON.getString("height"));
            
            photo.setData(types.get("width"), lang,
                          photoJSON.getString("width"));
            
            JSONArray tagJSON = photoJSON.getJSONArray("tags");
            
            log("found "+ tagJSON.length() + " faces");
            
            for (int i = 0; i < tagJSON.length(); i++) {
                parseFace(tagJSON.getJSONObject(i), tm, types, photo, lang);
            }
            
        } catch (JSONException jse) {
            log(jse.getMessage());
        } catch (TopicMapException tme){
            log(tme.getMessage());
        }
    }
    
    private void parseFace(JSONObject tagJSON, TopicMap tm, 
                          HashMap<String,Topic> types, Topic photo, Topic lang)
    throws JSONException, TopicMapException{

        String id  = UUID.randomUUID().toString();
        Topic face = getTopic(tm, types.get("face"), FACE_SI + "/" + id, "Face [" + id + "]");
        Association a = tm.createAssociation(types.get("tagged"));
        a.addPlayer(face, types.get("face"));
        a.addPlayer(photo, types.get("photo"));
        
        JSONArray attrs = tagJSON.getJSONArray("attributes");
        for (int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            if(attr.has("smiling")){
                String smile = Boolean.toString(attr.getBoolean("smiling"));
                face.setData(types.get("smiling"), lang, smile);
            }
        }
        
    }
    
}
