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
package org.wandora.application.tools.extractors.rekognition;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.wandora.dep.json.JSONArray;
import org.wandora.dep.json.JSONException;
import org.wandora.dep.json.JSONObject;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
public class RekognitionFaceDetector extends AbstractRekognitionExtractor{
    
    private boolean celebrityNaming = false;
    private double  celebrityTreshold = 0;
    protected void setCelebrityNaming(boolean b){
    
        setCelebrityNaming(b, 0);
    
    }
    
    protected void setCelebrityNaming(boolean b, double treshold){
        
        this.celebrityNaming = b;
        this.celebrityTreshold = treshold;
    }
    
    private static final String[] contentTypes=new String[] { 
        "text/plain", "text/json", "application/json" 
    };
        
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
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
        
        
        HttpResponse<JsonNode> resp = Unirest.get(str).asJson();

        //DEBUG
        StringWriter sw = new StringWriter();
        IOUtils.copy(resp.getRawBody(), sw);
        System.out.println(sw.toString());
        
        JSONObject respNode = resp.getBody().getObject();
        try {
            
            logUsage(respNode);
            
            String imageURL = respNode.getString("url");
            Topic imageTopic = getImageTopic(tm, imageURL);
            
            JSONArray detections = respNode.getJSONArray("face_detection");
            
            log("Detected " + detections.length() + " faces");
            
            for(int i = 0; i < detections.length(); i++){
                
                log("Parsing detection #" + (i+1));
                
                JSONObject detectionJSON = detections.getJSONObject(i);
                Topic detectionTopic = getDetectionTopic(tm);
                
                if(celebrityNaming){
                    try {
                        String bestMatch = getBestMatch(detectionJSON,this.celebrityTreshold);
                        detectionTopic.setBaseName(bestMatch);
                    } catch (JSONException | TopicMapException e) {
                        log("Failed to match name for detection");
                    }
                }
                
                associateImageWithDetection(tm, imageTopic, detectionTopic);
                
                ArrayList<HashMap<JSON,String>> featuresArray = flattenJSONObject(detectionJSON);
                
                for(HashMap<JSON,String> featureData: featuresArray){
                    
                    if(featureData.containsKey(JSON.ERROR) || !featureData.containsKey(JSON.VALUE)){
                        log("Failed to parse detection data for attribute \"" + featureData.get(JSON.KEY) + "\"");
                        continue;
                    }
                    
                    String featureKey = featureData.get(JSON.KEY);
                    String featureValue = featureData.get(JSON.VALUE);
                    
                    addFeatureToDetection(tm, detectionTopic, featureKey, featureValue);
                    
                }
                
            }
            
        } catch (JSONException e) {
        
            log("Failed to parse response. (" + e.getMessage() + ")");
        
        }

        
        return true;
    }

    private void logUsage(JSONObject respNode) throws JSONException{
        
        JSONObject usage = respNode.getJSONObject("usage");
        
        log("Response status: " + usage.getString("status"));
        log("Quota status: " + Integer.toString(usage.getInt("quota")));
        
    }

    
}
