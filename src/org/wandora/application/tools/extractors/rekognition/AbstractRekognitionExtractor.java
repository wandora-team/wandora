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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.dep.json.JSONArray;
import org.wandora.dep.json.JSONException;
import org.wandora.dep.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
abstract class AbstractRekognitionExtractor extends AbstractExtractor{
    
    protected static final String API_ROOT = "http://rekognition.com/func/api/";
    
    protected enum JSON {KEY, NAME, VALUE, ERROR};
    
    protected static final String FLATTENING_DELIMETER = ".";
    
    //Face detection data is language independent
    private static final String LANG_SI = "http://wandora.org/si/core/lang-independent";
    
    private static final String SI_ROOT = "http://wandora.org/si/rekognition/";
    
    private static final String IMAGE_SI = SI_ROOT + "image/";
    private static final String FACE_SI_ROOT = SI_ROOT + "face/";
    private static final String DETECTION_SI = FACE_SI_ROOT + "detection/";
    private static final String FEATURE_SI = FACE_SI_ROOT + "feature/";
    
    private static final String[][] KEY_MAP = {
        {"b_ll", "brow_left_left"},
        {"b_lm", "brow_left_middle"},
        {"b_lr", "brow_left_right"},
        {"b_rl", "brow_right_left"},
        {"b_rm", "brow_right_middle"},
        {"b_rr", "brow_right_right"},
        {"e_ld", "eye_left_down"},
        {"e_ll", "eye_left_left"},
        {"e_lu", "eye_left_up"},
        {"e_lr", "eye_left_right"},
        {"e_rd", "eye_right_down"},
        {"e_rl", "eye_right_left"},
        {"e_ru", "eye_right_up"},
        {"e_rr", "eye_right_right"},
        {"m_d",  "mouth_down"},
        {"m_u",  "mouth_up"},
        {"n_l",  "nose_left"},
        {"n_r",  "nose_right"}
    };
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other ReKognition extractors. It doesn't perform extraction itself.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other ReKognition extractors. It doesn't perform extraction itself.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other ReKognition extractors. It doesn't perform extraction itself.");
    }
    

    private static RekognitionConfiguration conf = new RekognitionConfiguration();
    protected static void setConfiguration(RekognitionConfiguration c){
        conf = c;
    }
    protected static RekognitionConfiguration getConfiguration(){
        return conf;
    }
    
    
    // -------------------------------------------------------------------------

    
    protected static Topic getImageTopic(TopicMap tm, String url) throws TopicMapException{
        Topic image = getOrCreateTopic(tm, url);
        Topic imageClass = getImageClass(tm);
        
        image.addType(imageClass);
        
        return image;
        
    }
    
    protected static Topic getDetectionTopic(TopicMap tm) throws TopicMapException{
        
        String id  = UUID.randomUUID().toString();
        
        Topic detection = getOrCreateTopic(tm, DETECTION_SI + id);
        Topic detectionClass = getDetectionClass(tm);

        detection.addType(detectionClass);
        
        return detection;
        
    }
    
    protected static void associateImageWithDetection(TopicMap tm, Topic image, Topic detection) throws TopicMapException{
        
        Topic imageClass = getImageClass(tm);
        Topic detectionClass = getDetectionClass(tm);
        
        Association a = tm.createAssociation(detectionClass);
        
        a.addPlayer(image, imageClass);
        a.addPlayer(detection, detectionClass);
        
    }
    
    protected static void addFeatureToDetection(TopicMap tm, Topic Detection, String featureType, String featureData) throws TopicMapException{
        
        Topic langTopic = getLangTopic(tm);
        Topic featureTypeTopic = getFeatureTypeTopic(tm, featureType);
        
        Detection.setData(featureTypeTopic, langTopic, featureData);
        
        
    }
    
    protected static Topic getFeatureTypeTopic(TopicMap tm, String featureType) throws TopicMapException {
        
        Topic featureTypeTopic = getOrCreateTopic(tm, FEATURE_SI + featureType, featureType);
        Topic featureClass = getFeatureClass(tm);
        
        makeSubclassOf(tm, featureTypeTopic, featureClass);
        
        return featureTypeTopic;
    }
    
    protected static Topic getImageClass(TopicMap tm) throws TopicMapException{
        
        Topic imageClass = getOrCreateTopic(tm, IMAGE_SI, "Image");
        makeSubclassOf(tm, imageClass, getRekognitionClass(tm));
        
        return imageClass;
        
    }
    
    protected static Topic getDetectionClass(TopicMap tm) throws TopicMapException{
        
        Topic detectionClass = getOrCreateTopic(tm, DETECTION_SI, "Face Detection");
        makeSubclassOf(tm, detectionClass, getRekognitionClass(tm));
        
        return detectionClass;
        
    }
    
    protected static Topic getFeatureClass(TopicMap tm) throws TopicMapException{
        
        Topic featureClass = getOrCreateTopic(tm, FEATURE_SI, "Face Detection Feature");
        makeSubclassOf(tm, featureClass, getRekognitionClass(tm));
        
        return featureClass;
        
    }
    
    // ------------------------------------------------------ HELPERS ----------
    protected static Topic getRekognitionClass(TopicMap tm) throws TopicMapException {
        Topic rekognition = getOrCreateTopic(tm, SI_ROOT, "ReKognition");
        makeSubclassOf(tm, rekognition, getWandoraClassTopic(tm));
        return rekognition;
    }

    protected static Topic getWandoraClassTopic(TopicMap tm)
            throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si)
            throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn)
            throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass)
            throws TopicMapException {

        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, LANG_SI);
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * Recursively flatten a JSON object structure to an array of key-value-pairs.
     * The key-value-pairs are represented as a hash where JSON.KEY corresponds to
     * a flattened key and JSON.VALUE to the corresponding value. For JSON
     * primitives (Integer, Double, String) we simply create corresponding hashes.
     * For JSON objects we create a flattened key based on the JSON structure. For
     * arrays we use the array index as a key. The flattened keys are concatenation
     * of the original key structure separated by FLATTENING_DELIMITER.
     * 
     * Example:
     * {
     *     "foo":42,
     *     "bar":{
     *         "baz":43
     *     },
     *     "foos":[1,4,9]
     * }
     * 
     * translates to
     * 
     * [
     *     "foo":42,
     *     "bar.baz":43,
     *     "foos.0":1,
     *     "foos.1":4,
     *     "foos.2":9
     * ]
     * 
     * @param   obj A JSONObject to flatten
     * @return      A flattened list of key-value-pairs represented by an 
     *              ArrayList of Hashes
     */
    protected static ArrayList<HashMap<JSON,String>> flattenJSONObject(JSONObject obj){
        
        ArrayList<HashMap<JSON,String>> flattenedArray = new ArrayList<>();
        Iterator keys = obj.keys();
        
        while(keys.hasNext()){
            
            String key = (String)keys.next();
            
            try {
                
                Object element = obj.get(key);
                
                //JSONObjects require further flattening. Branch into recursion
                if(element instanceof JSONObject){
                    
                    JSONObject subObj = (JSONObject)element;
                    ArrayList<HashMap<JSON,String>> flattenedSubObj = flattenJSONObject(subObj);
                    
                    for(HashMap<JSON,String> subObjectProperty: flattenedSubObj){
                        
                        flattenedArray.add(prefixKey(translateKey(key), subObjectProperty));
                        
                    }
                
                //As do JSONArrays
                } else if(element instanceof JSONArray){
                    
                    JSONArray array = (JSONArray)element;
                    ArrayList<HashMap<JSON,String>> flattenedSubArray = flattenJSONArray(array);
                    for(HashMap<JSON,String> subArrayElement: flattenedSubArray){
                        
                        flattenedArray.add(prefixKey(translateKey(key), subArrayElement));
                        
                    }
                    
                //Simple case: We should have an element that's representable as a primitive  
                } else {
                    
                    
                    flattenedArray.add(flattenPrimitive(key,element));

                }
                
            } catch (JSONException e) {
                HashMap<JSON,String> flattenedItem = new HashMap<>();
                flattenedItem.put(JSON.KEY, translateKey(key));
                flattenedItem.put(JSON.ERROR, e.getMessage());
                flattenedArray.add(flattenedItem);
            }
            
            
        }
        
        return flattenedArray;
        
    }
    
    private static ArrayList<HashMap<JSON,String>> flattenJSONArray(JSONArray array) throws JSONException{
    
        ArrayList<HashMap<JSON,String>> flattenedArray = new ArrayList<>();
        
        for (int i = 0; i < array.length(); i++) {
            
            Object subObj = array.get(i);
            if(subObj instanceof JSONObject){
                ArrayList<HashMap<JSON,String>> flattenedSubObject = flattenJSONObject((JSONObject)subObj);
                for(HashMap<JSON,String> subObjectProperty: flattenedSubObject){
                    
                    HashMap<JSON,String> flattenedProperty = prefixKey(Integer.toString(i), subObjectProperty);
                    
                    flattenedArray.add(flattenedProperty);
                }
                
            } else if(subObj instanceof JSONArray){
                ArrayList<HashMap<JSON,String>> flattenedSubObjects = flattenJSONArray((JSONArray)subObj);
                for(HashMap<JSON,String> subObject: flattenedSubObjects){
                    
                    HashMap<JSON,String> flattenedProperty = prefixKey(Integer.toString(i), subObject);
                    
                    flattenedArray.add(flattenedProperty);
                }
            } else {
                HashMap<JSON,String> flattenedPrimitive = flattenPrimitive(Integer.toString(i), subObj);
                flattenedArray.add(prefixKey(Integer.toString(i), flattenedPrimitive));
            }
        }
        
        return flattenedArray;
        
    }
    
    private static HashMap<JSON,String> flattenPrimitive(String key, Object primitive) throws JSONException{
        HashMap<JSON,String> flattenedItem = new HashMap<>();
        flattenedItem.put(JSON.KEY, translateKey(key));

        if(primitive instanceof Integer){

            int value = (int)primitive;
            flattenedItem.put(JSON.VALUE,Integer.toString(value));

        } else if(primitive instanceof Double){

            double value = (double)primitive;
            flattenedItem.put(JSON.VALUE, Double.toString(value));

        } else if(primitive instanceof String) {

            String value = (String)primitive;
            flattenedItem.put(JSON.VALUE, value);

        } else {
            throw new JSONException("Invalid JSON element for key " + key);
        }
        
        return flattenedItem;
    }
    
    private static HashMap<JSON,String> prefixKey(String key, HashMap<JSON,String> hash){
        HashMap<JSON,String> prefixed = new HashMap<>();
        
        String hashKey = hash.get(JSON.KEY);
        
        prefixed.put(JSON.KEY, key + FLATTENING_DELIMETER + hashKey);
        
        if(hash.containsKey(JSON.ERROR)){
            prefixed.put(JSON.ERROR, hash.get(JSON.ERROR));
        } else {
            prefixed.put(JSON.VALUE, hash.get(JSON.VALUE));
        }
        
        return prefixed;
        
    }
    
    private static String translateKey(String key){
        String translated = key;
        for (String[] KEY_MAPPING : KEY_MAP) {
            if (KEY_MAPPING[0].equals(key)) {
                translated = KEY_MAPPING[1];
            }
        }
        return translated;
    }
    
    protected String getBestMatch(JSONObject obj, double treshold) throws JSONException{
        JSONArray matches = obj.getJSONArray("matches");
        
        double best = treshold;
        String matchedName = null;
        
        for (int i = 0; i < matches.length(); i++) {
            JSONObject match = matches.getJSONObject(i);
            try {
                double score = Double.parseDouble(match.getString("score"));
                if(score > best){
                    String tag = match.getString("tag");
                    matchedName = tag.replace("_", " ");
                    best = score;
                }
            } catch (NumberFormatException | JSONException e) {
            }
        }
        
        if(matchedName == null) throw new JSONException("Failed to match name");
        return matchedName;
        
    }
     
}
