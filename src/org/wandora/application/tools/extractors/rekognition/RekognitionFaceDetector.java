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
        return "ReKognition face detector";
    }

    @Override
    public String getDescription() {
        return "Detects face out of given image using ReKognition API. "+
               "Creates topics and associations out of ReKognition face detection. "+
               "Read more about ReKognition at http://rekognition.com/";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_rekognition.png");
    }
    
    @Override
    protected HashMap<String,ValueHandler> createHandlerMap() {
        
        HashMap<String,ValueHandler> handlerMap = new HashMap<>();
        
        handlerMap.put("matches", new MatchHandler(SI_ROOT + "celeb_match", "Celebrity Match"));

        handlerMap.put("boundingbox", new BoundingBoxHandler(FEATURE_SI_ROOT + "boundingbox/", "Boundingbox"));
        handlerMap.put("pose",        new PoseHandler(FEATURE_SI_ROOT + "pose/", "Pose"));
        handlerMap.put("confidence",  new NumericValueHandler(FEATURE_SI_ROOT + "confidence/", "Confidence"));
        handlerMap.put("emotion",     new NumericValuesHandler(FEATURE_SI_ROOT + "emotion/", "Emotion"));
        handlerMap.put("eye_left",    new CoordinateHandler(FEATURE_SI_ROOT + "eye_left/", "Left Eye"));
        handlerMap.put("eye_right",   new CoordinateHandler(FEATURE_SI_ROOT + "eye_right/", "Right Eye"));
        handlerMap.put("nose",        new CoordinateHandler(FEATURE_SI_ROOT + "nose/", "Nose"));
        handlerMap.put("mouth_l",     new CoordinateHandler(FEATURE_SI_ROOT + "mouth_left", "Mouth (left)"));
        handlerMap.put("mouth_r",     new CoordinateHandler(FEATURE_SI_ROOT + "mouth_right", "Mouth (right)"));
        
        handlerMap.put("b_ll", new CoordinateHandler(FEATURE_SI_ROOT+ "brow_left_left/", "Left Brow (left)"));
        handlerMap.put("b_lm", new CoordinateHandler(FEATURE_SI_ROOT+ "brow_left_middle/", "Left Brow (middle)"));
        handlerMap.put("b_lr", new CoordinateHandler(FEATURE_SI_ROOT+ "brow_left_right/", "Left Bro (right)"));
        handlerMap.put("b_rl", new CoordinateHandler(FEATURE_SI_ROOT+ "brow_right_left/", "Right Brow (left)"));
        handlerMap.put("b_rm", new CoordinateHandler(FEATURE_SI_ROOT+ "brow_right_middle/","Right Brow (middle)"));
        handlerMap.put("b_rr", new CoordinateHandler(FEATURE_SI_ROOT+ "brow_right_right/", "Right Bro (right)"));
        handlerMap.put("e_ld", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_left_down/", "Left Eye (down)"));
        handlerMap.put("e_ll", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_left_left/", "Left Eye (left)"));
        handlerMap.put("e_lu", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_left_up/", "Left Eye (up)"));
        handlerMap.put("e_lr", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_left_right/", "Left Eye (right)"));
        handlerMap.put("e_rd", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_right_down/", "Right Eye (down)"));
        handlerMap.put("e_rl", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_right_left/", "Right Eye (left)"));
        handlerMap.put("e_ru", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_right_up/", "Right Eye (up)"));
        handlerMap.put("e_rr", new CoordinateHandler(FEATURE_SI_ROOT+ "eye_right_right/", "Right Eye (right)"));
        handlerMap.put("m_d",  new CoordinateHandler(FEATURE_SI_ROOT+ "mouth_down/", "Mouth (down)"));
        handlerMap.put("m_u",  new CoordinateHandler(FEATURE_SI_ROOT+ "mouth_up/", "Mouth (up)"));
        handlerMap.put("n_l",  new CoordinateHandler(FEATURE_SI_ROOT+ "nose_left/", "Nose (left)"));
        handlerMap.put("n_r",  new CoordinateHandler(FEATURE_SI_ROOT+ "nose_right/", "Nose (right)"));
        handlerMap.put("tl",   new CoordinateHandler(FEATURE_SI_ROOT+ "top_left/", "Nose (left)"));
        
        return handlerMap;
        
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
     * @return true if the extraction was successful, false otherwise
     * @throws Exception if the supplied URL is invalid or Topic Map
     * manipulation fails
     */
    
    @Override
    public boolean _extractTopicsFrom(String imageUrl, TopicMap tm) throws Exception {
        
        WandoraToolLogger logger = getCurrentLogger();
        
        if(imageUrl == null)
            throw new Exception("No valid Image URL found.");

        /**
         * Prompt for authentication if we're still lacking it. Return if we still
         * didn't get it
         */
        if(!conf.hasAuth()){
            if(!conf.askForAuth()) return false;
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
                
                if(conf.celebrityNaming) {
                    try {
                        String bestMatch = getBestMatch(detectionJSON, conf.celebrityTreshold);
                        detectionTopic.setBaseName(bestMatch);
                    } 
                    catch (JSONException | TopicMapException e) {
                        logger.log("Failed to match name for detection");
                    }
                }
                
                associateImageWithDetection(tm, imageTopic, detectionTopic);
                
                for(String featureKey : handlerMap.keySet()) {
                    if (detectionJSON.has(featureKey)) {
                        Object feature = detectionJSON.get(featureKey);
                        handlerMap.get(featureKey).handleValue(tm, detectionTopic, feature);
                    }
                }
            }
        }
        catch (JSONException e) {
            logger.log("Failed to parse response. (" + e.getMessage() + ")");
            return false;
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
    
    
    // ----------------------------------------------------------- CONFIGURE ---
    
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        RekognitionConfigurationUI configurationUI = new RekognitionConfigurationUI("face");
        configurationUI.open(wandora,600);
        configurationUI.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora wandora,org.wandora.utils.Options options,String prefix){
    }
    
    

    
}
