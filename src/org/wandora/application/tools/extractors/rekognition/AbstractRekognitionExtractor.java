/*
* WANDORA
* Knowledge Extraction, Management, and Publishing Application
* http://wandora.org
*
* Copyright (C) 2004-2016 Wandora Team
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/



package org.wandora.application.tools.extractors.rekognition;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
*
* @author Eero Lehtonen
*/
abstract class AbstractRekognitionExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;

	private static boolean MAKE_DISTINCT_CONFIDENCE_TOPICS = false;
    
    protected static final String API_ROOT = "http://rekognition.com/func/api/";
    
    protected enum JSON {KEY, NAME, VALUE, ERROR};
    
    protected static final String FLATTENING_DELIMETER = ".";
    
    //Face detection data is language independent
    private static final String LANG_SI = "http://wandora.org/si/core/lang-independent";
    
    protected static final String SI_ROOT = "http://wandora.org/si/rekognition/";
    
    protected static final String IMAGE_SI = SI_ROOT + "image/";
    protected static final String DETECTION_SI = SI_ROOT + "detection/";
    
    protected static final String FACE_SI_ROOT = SI_ROOT + "face/";
    protected static final String SCENE_SI_ROOT = SI_ROOT + "scene/";
    protected static final String FEATURE_SI_ROOT = FACE_SI_ROOT + "feature/";

    protected abstract HashMap<String,ValueHandler> createHandlerMap();
    
    // -------------------------------------------------------------------------
    
    private static final String EXTRACT_ERROR =
            "This extractor is a frontend for other ReKognition extractors. "
            + "It doesn't perform extraction itself.";
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException(EXTRACT_ERROR);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException(EXTRACT_ERROR);
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException(EXTRACT_ERROR);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    protected static RekognitionConfiguration conf = new RekognitionConfiguration();
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
    
    protected static Topic getDetectionTopic(TopicMap tm) throws TopicMapException {
        String id = UUID.randomUUID().toString();
        
        Topic detection = getOrCreateTopic(tm, DETECTION_SI + id);
        Topic detectionClass = getDetectionClass(tm);

        detection.addType(detectionClass);
        
        return detection;
    }
    
    
    protected static void associateImageWithDetection(TopicMap tm, Topic image, Topic detection) throws TopicMapException {
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
        Topic featureTypeTopic = getOrCreateTopic(tm, FEATURE_SI_ROOT + featureType, featureType);
        Topic featureClass = getFeatureClass(tm);
        
        makeSubclassOf(tm, featureTypeTopic, featureClass);
        
        return featureTypeTopic;
    }
    
    
    
    protected static Topic getImageClass(TopicMap tm) throws TopicMapException {
        Topic imageClass = getOrCreateTopic(tm, IMAGE_SI, "Image");
        makeSubclassOf(tm, imageClass, getRekognitionClass(tm));
        
        return imageClass;
    }
    
    
    
    protected static Topic getDetectionClass(TopicMap tm) throws TopicMapException {
        Topic detectionClass = getOrCreateTopic(tm, DETECTION_SI, "Rekognition Detection");
        makeSubclassOf(tm, detectionClass, getRekognitionClass(tm));
        
        return detectionClass;
    }
        
    protected static Topic getFeatureClass(TopicMap tm) throws TopicMapException{
        Topic featureClass = getOrCreateTopic(tm, FEATURE_SI_ROOT, "Face Detection Feature");
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
    
    
    /**
    * A value handler is used to create and associate topics from face detection
    * data return as JSON. Implementations use constructors to initialize
    * feature types etc. handleValue is used to create the individual feature
    * Topics.
    */
    interface ValueHandler{
        public void handleValue(TopicMap tm, Topic detection, Object value) throws Exception;
    }
    
    
    
    /**
    * AbstractValueHandler implements common functionality for extending
    * ValueHandlers.
    */
    abstract class AbstractValueHandler{
        protected static final String COORDINATE_SI = SI_ROOT + "coordinate/";
        protected static final String COORDINATE_NAME = "Coordinate";

        protected static final String CONFIDENCE_SI = FEATURE_SI_ROOT + "confidence/";
        protected static final String CONFIDENCE_NAME = "Confidence";
        
        protected String TYPE_SI;
        protected String TYPE_NAME;
        
        protected Topic getTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, TYPE_SI, TYPE_NAME);
            return typeTopic;
        }
        
        protected Topic getCoordinateTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, COORDINATE_SI, COORDINATE_NAME);
            return typeTopic;
        }
        
        protected Topic getConfidenceTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, CONFIDENCE_SI, CONFIDENCE_NAME);
            return typeTopic;
        }
        
        protected Topic[] getCoordinateTypeTopics(TopicMap tm) throws TopicMapException{
            Topic coordinateType = getCoordinateTypeTopic(tm);
            Topic x = getOrCreateTopic(tm, COORDINATE_SI + "x", "x");
            Topic y = getOrCreateTopic(tm, COORDINATE_SI + "y", "y");
            x.addType(coordinateType);
            y.addType(coordinateType);
            
            return new Topic[]{x,y};
        }
    }
    
    
    
    
    /**
    * NumericValueHandler creates simple occurrence data representing numeric
    * (double) values. siBase and name are used to differentiate numeric values.
    * Valid value for handleValue should be able to be casted to integer or double.
    */
    class NumericValueHandler extends AbstractValueHandler implements ValueHandler{

        NumericValueHandler(String si, String name){
            this.TYPE_SI = si;
            this.TYPE_NAME = name;
        }
        
        @Override
        public void handleValue(TopicMap tm, Topic detection, Object value) throws Exception {
            Topic typeTopic = this.getTypeTopic(tm);
            Topic langTopic = getLangTopic(tm);
            detection.setData(typeTopic, langTopic, String.valueOf(value));

        }
    }
    
    /**
    * NumericValuesHandler creates a series of Topics from a JSONObject where
    * the keys are used as Topics to be associated with the detection and
    * a numeric value describing the confidence of the association. For example
    * the JSONObject with a key "emotion"
    *
    * {
    * "happy" : 0.98,
    * "surprised" : 0.05,
    * "calm" : 0.02
    * }
    *
    * will create three associations with the detection, emotion and respective
    * confidence as players.
    */
    class NumericValuesHandler extends AbstractValueHandler implements ValueHandler{

        NumericValuesHandler(String si, String name){
            this.TYPE_SI = si;
            this.TYPE_NAME = name;
        }
        
        @Override
        public void handleValue(TopicMap tm, Topic detection, Object values) throws Exception {
            
            if(!(values instanceof JSONObject)){
                throw new IllegalArgumentException("Argument supplied to "
                        + "NumericValuesHandler is not a JSONObject.");
            }
            
            Topic typeTopic = this.getTypeTopic(tm);
            Topic confidenceTypeTopic = getConfidenceTypeTopic(tm);
            Topic detectionTypeTopic = getDetectionClass(tm);
            
            JSONObject numericValues = (JSONObject)values;
            Iterator keys = numericValues.keys();
            
            while(keys.hasNext()) {
                String key = (String)keys.next();
                Topic keyTopic = getOrCreateTopic(tm, TYPE_SI + "/" + key, key);
                keyTopic.addType(typeTopic);
                
                Object value = numericValues.get(key);
                
                Topic confidenceTopic = null;
                if(MAKE_DISTINCT_CONFIDENCE_TOPICS) {
                    String confidenceID = UUID.randomUUID().toString();
                    confidenceTopic = getOrCreateTopic(tm, CONFIDENCE_SI + confidenceID);
                    confidenceTopic.setData(confidenceTypeTopic, getLangTopic(tm), String.valueOf(value));
                }
                else {
                    confidenceTopic = getOrCreateTopic(tm, CONFIDENCE_SI + String.valueOf(value));
                    confidenceTopic.setBaseName(String.valueOf(value));
                }
                confidenceTopic.addType(confidenceTypeTopic);
                
                Association a = tm.createAssociation(typeTopic);
                a.addPlayer(keyTopic, typeTopic);
                a.addPlayer(confidenceTopic, confidenceTypeTopic);
                a.addPlayer(detection, detectionTypeTopic);
            }
        }
    }
    
    
    
    
    
    
    /**
    * CoordinateHandler creates a coordinate topic with simple occurrences of
    * x and y from a JSONObject looking like:
    *
    * {
    * "x":<double>
    * "y":<double>
    * }
    */
    class CoordinateHandler extends AbstractValueHandler implements ValueHandler{

        CoordinateHandler(String si, String name){
            this.TYPE_SI = si;
            this.TYPE_NAME = name;
        }
        
        @Override
        public void handleValue(TopicMap tm, Topic detection, Object value) throws Exception {
            
            if(!(value instanceof JSONObject)){
                throw new IllegalArgumentException("Argument supplied to "
                        + "CoordinateHandler is not a JSONObject.");
            }
            
            JSONObject coordinates = (JSONObject)value;

            if(!(coordinates.has("x") && coordinates.has("y"))){
                throw new IllegalArgumentException("Argument supplied to "
                        + "CoordinateHandler doesn't have required keys.");
            }
            
            Topic[] coordinateTypes = getCoordinateTypeTopics(tm);
            Topic typeTopic = this.getTypeTopic(tm);
            Topic langTopic = getLangTopic(tm);
            Topic detectionTypeTopic = getDetectionClass(tm);
            
            String coordinatesID = UUID.randomUUID().toString();

            Topic coordinatesTopic = getOrCreateTopic(tm, TYPE_SI + coordinatesID);
            coordinatesTopic.addType(typeTopic);
            coordinatesTopic.setData(coordinateTypes[0], langTopic, String.valueOf(coordinates.get("x")));
            coordinatesTopic.setData(coordinateTypes[1], langTopic, String.valueOf(coordinates.get("y")));
            
            Association a = tm.createAssociation(typeTopic);
            a.addPlayer(detection, detectionTypeTopic);
            a.addPlayer(coordinatesTopic, typeTopic);
        }
    }
    
    
    
    /**
    * BoundingBoxHandler creates a Topic structure representing a "boundingbox"
    * structure from the response data. The expected response structure is like:
    *
    * {
    * "tl":{
    * "x":<double>,
    * "y":<double>
    * },
    * "size":{
    * "width":<double>,
    * "height":<double>
    * }
    * }
    *
    * Here "tl" is interpreted as a coordinate Topic and "size" as a size
    * Topic.
    */
    class BoundingBoxHandler extends AbstractValueHandler implements ValueHandler{
        
        
        private static final String SIZE_SI = SI_ROOT + "size/";
        private static final String SIZE_NAME = "Size";
        
        private static final String TL_SI = SI_ROOT + "tl/";
        private static final String TL_NAME = "Top Left";

        private Topic getSizeTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, SIZE_SI, SIZE_NAME);
            return typeTopic;
        }
        
        private Topic getTLTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, TL_SI, TL_NAME);
            return typeTopic;
        }
        
        protected Topic[] getSizeTypeTopics(TopicMap tm) throws TopicMapException{
            Topic sizeType = getSizeTypeTopic(tm);
            Topic width = getOrCreateTopic(tm, SIZE_SI + "width", "Width");
            Topic height = getOrCreateTopic(tm, SIZE_SI + "height", "Height");
            width.addType(sizeType);
            height.addType(sizeType);
            
            return new Topic[]{width,height};
        }
        
        BoundingBoxHandler(String si, String name){
            this.TYPE_SI = si;
            this.TYPE_NAME = name;
        }
        
        private void handleCoordinates(TopicMap tm, Topic boundingBox, JSONObject tl) throws Exception{
            Topic[] coordinateTypes = getCoordinateTypeTopics(tm);
            Topic typeTopic = this.getCoordinateTypeTopic(tm);
            Topic tlTypeTopic = this.getTLTypeTopic(tm);
            Topic langTopic = getLangTopic(tm);
            Topic boundingboxTypeTopic = this.getTypeTopic(tm);
            
            String coordinatesID = UUID.randomUUID().toString();

            Topic coordinatesTopic = getOrCreateTopic(tm, COORDINATE_SI + coordinatesID);
            coordinatesTopic.addType(typeTopic);
            coordinatesTopic.setData(coordinateTypes[0], langTopic, String.valueOf(tl.get("x")));
            coordinatesTopic.setData(coordinateTypes[1], langTopic, String.valueOf(tl.get("y")));
            
            Association a = tm.createAssociation(tlTypeTopic);
            a.addPlayer(boundingBox, boundingboxTypeTopic);
            a.addPlayer(coordinatesTopic, typeTopic);
        }
        
        private void handleSize(TopicMap tm, Topic boundingBox, JSONObject size) throws Exception{
            Topic[] sizeTypes = getSizeTypeTopics(tm);
            Topic typeTopic = this.getSizeTypeTopic(tm);
            Topic langTopic = getLangTopic(tm);
            Topic boundingboxTypeTopic = this.getTypeTopic(tm);
            
            String sizeID = UUID.randomUUID().toString();

            Topic sizeTopic = getOrCreateTopic(tm, SIZE_SI + sizeID);
            sizeTopic.addType(typeTopic);
            sizeTopic.setData(sizeTypes[0], langTopic, String.valueOf(size.get("width")));
            sizeTopic.setData(sizeTypes[1], langTopic, String.valueOf(size.get("height")));
            
            Association a = tm.createAssociation(typeTopic);
            a.addPlayer(boundingBox, boundingboxTypeTopic);
            a.addPlayer(sizeTopic, typeTopic);
        }
        
        @Override
        public void handleValue(TopicMap tm, Topic detection, Object value) throws Exception {
            
            if(!(value instanceof JSONObject)){
                throw new IllegalArgumentException("Argument supplied to "
                        + "BoundingBoxHandler is not a JSONObject.");
            }
            
            JSONObject boundingbox = (JSONObject)value;
            
            if(!(boundingbox.has("tl") && boundingbox.has("size"))){
                throw new IllegalArgumentException("Argument supplied to "
                        + "BoundingBoxHandler doesn't have required keys.");
            }
            
            JSONObject tl = boundingbox.getJSONObject("tl");
            JSONObject size = boundingbox.getJSONObject("size");
            
            Topic typeTopic = this.getTypeTopic(tm);
            
            String boundingboxID = UUID.randomUUID().toString();
            Topic boundingboxTopic = getOrCreateTopic(tm, TYPE_SI + boundingboxID);
            
            boundingboxTopic.addType(typeTopic);
            
            this.handleCoordinates(tm, boundingboxTopic, tl);
            this.handleSize(tm, boundingboxTopic, size);
            
            Association a = tm.createAssociation(typeTopic);
            a.addPlayer(boundingboxTopic, typeTopic);
            a.addPlayer(detection, getDetectionClass(tm));
        }
    }
    
    
    
    /**
    * PoseHandler creates a Topic representing a "pose" structure from the
    * response data. The expected response structure is like:
    *
    * {
    * "roll":<double>,
    * "pitch":<double>,
    * "yaw":<double>
    * }
    *
    */
    class PoseHandler extends AbstractValueHandler implements ValueHandler{
        
        private static final String ROLL_SI = FEATURE_SI_ROOT + "roll/";
        private static final String ROLL_NAME = "Roll";
        
        private static final String YAW_SI = FEATURE_SI_ROOT + "yaw/";
        private static final String YAW_NAME = "Yaw";
        
        private static final String PITCH_SI = FEATURE_SI_ROOT + "pitch/";
        private static final String PITCH_NAME = "Pitch";
        
        private Topic getRollTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, ROLL_SI, ROLL_NAME);
            return typeTopic;
        }
        
        private Topic getYawTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, YAW_SI, YAW_NAME);
            return typeTopic;
        }
        
        private Topic getPitchTypeTopic(TopicMap tm) throws TopicMapException{
            Topic typeTopic = getOrCreateTopic(tm, PITCH_SI, PITCH_NAME);
            return typeTopic;
        }
        
        PoseHandler(String si, String name){
            this.TYPE_SI = si;
            this.TYPE_NAME = name;
        }
        
        @Override
        public void handleValue(TopicMap tm, Topic detection, Object value) throws Exception {
            
            if(!(value instanceof JSONObject)){
                throw new IllegalArgumentException("Argument supplied to "
                        + "PoseHandler is not a JSONObject.");
            }
            
            JSONObject pose = (JSONObject)value;
            
            if(!(pose.has("roll") && pose.has("yaw") && pose.has("pitch"))){
                throw new IllegalArgumentException("Argument supplied to "
                        + "PoseHandler doesn't have required keys.");
            }
            
            String poseID = UUID.randomUUID().toString();
            Topic typeTopic = this.getTypeTopic(tm);
            Topic poseTopic = getOrCreateTopic(tm, TYPE_SI + poseID);
            poseTopic.addType(typeTopic);
            
            Topic langTopic = getLangTopic(tm);
            
            poseTopic.setData(getRollTypeTopic(tm), langTopic, String.valueOf(pose.get("roll")));
            poseTopic.setData(getYawTypeTopic(tm), langTopic, String.valueOf(pose.get("yaw")));
            poseTopic.setData(getPitchTypeTopic(tm), langTopic, String.valueOf(pose.get("pitch")));
            
            Topic detectionTypeTopic = getDetectionClass(tm);
            Association a = tm.createAssociation(typeTopic);
            a.addPlayer(poseTopic, typeTopic);
            a.addPlayer(detection,detectionTypeTopic);
        }
    }
    
    
    
    
    /**
    * MatchHandler creates a Topic representing a celebrity match from the
    * response data. The expected response structure is like:
    *
    * [{
    * "tag":<String>,
    * "score":<double>
    * },...]
    *
    * Here tag has snake case and is converted to a nicer format.
    * Example: "Gwyneth_Paltrow" -> "Gwyneth Paltrow"
    */
    
    protected class MatchHandler extends AbstractValueHandler implements ValueHandler{

        MatchHandler(String si, String name){
            this.TYPE_SI = si;
            this.TYPE_NAME = name;
        }
        
        @Override
        public void handleValue(TopicMap tm, Topic detection, Object values) throws Exception {
            if(!(values instanceof JSONArray)){
                throw new IllegalArgumentException("Argument supplied to "
                        + "MatchHandler is not a JSONArray.");
            }
            
            Topic typeTopic = this.getTypeTopic(tm);
            Topic confidenceTypeTopic = getConfidenceTypeTopic(tm);
            Topic detectionTypeTopic = getDetectionClass(tm);
            
            JSONArray matches = (JSONArray)values;
            
            for(int i=0;i<matches.length();i++){
                JSONObject match = matches.getJSONObject(i);
                String matchName = match.getString("tag");
                Topic matchTopic = getOrCreateTopic(tm, TYPE_SI + "/" + matchName, matchName.replace("_", " "));
                
                Double confidenceValue = match.getDouble("score");
                String confidenceID = UUID.randomUUID().toString();
                Topic confidenceTopic = getOrCreateTopic(tm, CONFIDENCE_SI + confidenceID, confidenceValue.toString());
                
                confidenceTopic.setData(confidenceTypeTopic, getLangTopic(tm), String.valueOf(confidenceValue));
                confidenceTopic.addType(confidenceTypeTopic);
                
                Association a = tm.createAssociation(typeTopic);
                a.addPlayer(matchTopic, typeTopic);
                a.addPlayer(confidenceTopic,confidenceTypeTopic);
                a.addPlayer(detection,detectionTypeTopic);
            }
        }
    }
    
}
