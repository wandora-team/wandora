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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.rekognition.RekognitionConfiguration.AUTH_KEY;
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
    
    @Override
    public int getExtractorType() {
        return URL_EXTRACTOR | RAW_EXTRACTOR;
    }
    
    private static final String[] contentTypes=new String[] { 
        "text/plain", "text/json", "application/json" 
    };
    
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public String getName() {
        return "ReKognition API Extractor";
    }

    @Override
    public String getDescription() {
        return "Extracts topics and associations from the ReKognition API. ";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_rekognition.png");
    }
    
    /**
     * Parse the URL as a string and pass it to _extractTopicsFrom(String ...)
     * @param u the image URL to extract
     * @param tm the current TopicMap
     * @return whether the extraction was a success.
     * @throws Exception propagated from _extracTopicsFrom(String ...)
     */
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        String currentURL = u.toExternalForm();
        return _extractTopicsFrom(currentURL, tm);
    }
    
    /**
     * 
     * The method used for actual extraction.
     * 
     * @param imageUrl the image URL for extraction
     * @param tm the current TopicMap
     * @return ???
     * @throws Exception if the supplied URL is invalid or Topic Map
     * manipulation fails
     */
    
    @Override
    public boolean _extractTopicsFrom(String imageUrl, TopicMap tm) throws Exception {
        
        WandoraToolLogger logger = getCurrentLogger();
        
        if(imageUrl == null)
            throw new Exception("No valid Image URL found.");

        
        /**
         * Fetch the configuration.
         */
        RekognitionConfiguration conf = getConfiguration();
        
        /**
         * Prompt for authentication if we're still lacking it.
         */
        if(conf.auth == null){
            RekognitionAuthenticationDialog authDialog = new RekognitionAuthenticationDialog();
            authDialog.open(getWandora());
            conf.auth = authDialog.getAuth();
            setConfiguration(conf);
        }
        
        /**
         * Construct the extraction URL based on the configuration and image URL
         */
        String extractUrl = API_ROOT +
                "?api_key=" + conf.auth.get(AUTH_KEY.KEY) + 
                "&api_secret=" + conf.auth.get(AUTH_KEY.SECRET) + 
                "&jobs=" + "face_" + getJobsString(conf.jobs) +
                "&urls=" + imageUrl;
        
        logger.log("GETting \"" + extractUrl + "\"");
        
        HttpResponse<JsonNode> resp = Unirest.get(extractUrl).asJson();
        JSONObject respNode = resp.getBody().getObject();
        
        System.out.println(respNode.toString());
        
        HashMap<String,ValueHandler> handlerMap = createHandlerMap();
        try {
            
            logUsage(respNode);
            
            
            
            String imageURL = respNode.getString("url");
            Topic imageTopic = getImageTopic(tm, imageURL);
            
            JSONArray detections = respNode.getJSONArray("face_detection");
            
            logger.log("Detected " + detections.length() + " faces");
            
            for(int i = 0; i < detections.length(); i++){
                
                logger.log("Parsing detection #" + (i+1));
                
                JSONObject detectionJSON = detections.getJSONObject(i);
                Topic detectionTopic = getDetectionTopic(tm);
                
                if(conf.celebrityNaming){
                    try {
                        String bestMatch = getBestMatch(detectionJSON,conf.celebrityTreshold);
                        detectionTopic.setBaseName(bestMatch);
                    } catch (JSONException | TopicMapException e) {
                        logger.log("Failed to match name for detection");
                    }
                }
                
                associateImageWithDetection(tm, imageTopic, detectionTopic);
                
                for(String featureKey: handlerMap.keySet()){
                    
                    if (detectionJSON.has(featureKey)) {
                        Object feature = detectionJSON.get(featureKey);
                        handlerMap.get(featureKey).handleValue(tm, detectionTopic, feature);
                    }
                }
                
            }
            
        } catch (JSONException e) {
        
            logger.log("Failed to parse response. (" + e.getMessage() + ")");
        
        }

        
        return true;
    }
    
    private String getJobsString(ArrayList<String> jobs){
        if(jobs == null) return "";

        StringBuilder sb = new StringBuilder();
        
        for(String job: jobs){
            sb.append('_');
            sb.append(job);
        }
        
        return sb.toString();
    }

    private void logUsage(JSONObject respNode) throws JSONException{
        
        JSONObject usage = respNode.getJSONObject("usage");
        
        log("Response status: " + usage.getString("status"));
        log("Quota status: " + Integer.toString(usage.getInt("quota")));
        
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        RekognitionConfigurationUI configurationUI = new RekognitionConfigurationUI(this);
        configurationUI.open(wandora);
        configurationUI.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora wandora,org.wandora.utils.Options options,String prefix){
    }
    
    

    
}
