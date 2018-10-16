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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wandora.application.tools.extractors.alchemy;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 *
 * @author akivela
 */


public class AlchemyFaceDetection extends AbstractAlchemyExtractor {


	private static final long serialVersionUID = 1L;
	
	private static int tempCounter = 0;
    public static int scaleDownWidth = 800;
    public static boolean transformScores = true;
    private int faceDetectionCounter = 0;
    

    
    
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }
    
    @Override
    public String getName() {
        return "Alchemy face detection extractor";
    }

    @Override
    public String getDescription(){
        return "Detects faces out of given images using AlchemyAPI service. Read more at http://www.alchemyapi.com/.";
    }


    
    
    // -------------------------------------------------------------------------

    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null || topicMap == null) return false;
        return _extractTopicsFrom(ImageIO.read(url), url.toExternalForm(), topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null || topicMap == null) return false;
        if(!file.canRead() || !file.exists()) return false;
        
        return _extractTopicsFrom(ImageIO.read(file), file.toURI().toURL().toExternalForm(), topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        if(in == null || topicMap == null) return false;
        
        return _extractTopicsFrom(ImageIO.read(in), null, topicMap);
    }
    

    @Override
    public boolean _extractTopicsFrom(String data, TopicMap topicMap) throws Exception {
        return false;
    }
    
    

    public boolean _extractTopicsFrom(BufferedImage image, String locator, TopicMap topicMap) throws Exception {
        String apikey = solveAPIKey();
        if(image != null && apikey != null) {
            apikey = apikey.trim();
            if(apikey.length() > 0) {
                
                // Scale down the image 
                if(image.getWidth() > scaleDownWidth) {
                    BufferedImage smallImage = resizeImage(image, scaleDownWidth);
                    image = smallImage;
                }
                
                // Save the image to the temp file
                tempCounter++;
                String tempFileName = "temp/temp"+(tempCounter % 10)+".jpg";
                ImageIO.write(image, "jpg", new File(tempFileName));
                byte[] imageData = readFile(new File(tempFileName));
                IObox.deleteFile(tempFileName);
                
                // Build url for the request
                String alchemyURL = ALCHEMY_URL+"calls/image/ImageGetRankedImageFaceTags";
                alchemyURL += "?apikey="+URLEncoder.encode(apikey, "utf-8");
                alchemyURL += "&imagePostMode=raw";
                alchemyURL += "&outputMode=xml";
                alchemyURL += "&knowledgeGraph=1";
                
                URL url = new URL(alchemyURL);
                
                // Write the image to the http stream
                HttpURLConnection handle = (HttpURLConnection) url.openConnection();
                handle.setDoOutput(true);
                handle.addRequestProperty("Content-Length", Integer.toString(imageData.length));
                DataOutputStream ostream = new DataOutputStream(handle.getOutputStream());
                ostream.write(imageData);
                ostream.close();

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(handle.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                
                String s;
                while ((s = in.readLine()) != null) {
                    responseBuilder.append(s);
                }
                in.close();
                String response = responseBuilder.toString();

                System.out.println("AlchemyAPI says: "+response);

                // Parse the response
                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemyFaceDetectionParser parserHandler = new AlchemyFaceDetectionParser(locator, image, topicMap, this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                faceDetectionCounter = 0;
                try {
                    reader.parse(new InputSource(new StringReader(response)));
                }
                catch(Exception e) {
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                if(faceDetectionCounter > 0) {
                    log("AlchemyAPI found total " + faceDetectionCounter + " face image.");
                }
                else {
                    log("AlchemyAPI didn't found any faces in the image.");
                }
            }
            else {
                log("No valid API key given! Aborting!");
            }
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }

    
    
    

    private byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } 
        finally {
            f.close();
        }
    }
    
    
    
    
    private BufferedImage resizeImage(BufferedImage originalImage, int newWidth) {
        int type = originalImage.getType();

        int newHeight = originalImage.getHeight() * newWidth / originalImage.getWidth();
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, type);
        Graphics2D g = resizedImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }

    
    
    
    // -------------------------------------------------------------------------






    public class AlchemyFaceDetectionParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public AlchemyFaceDetectionParser(String masterSubject, BufferedImage image, TopicMap tm, AbstractAlchemyExtractor parent) {
            this.tm=tm;
            this.parent=parent;

            try {
                if(masterSubject != null) {
                    masterTopic = tm.getTopic(masterSubject);
                    if(masterTopic == null && masterSubject != null) {
                        masterTopic = tm.getTopicBySubjectLocator(new Locator(masterSubject));
                    }
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            if(masterTopic == null && image != null) {
                try {
                    masterTopic = tm.createTopic();
                    if(masterSubject != null) {
                        masterTopic.addSubjectIdentifier(new Locator(masterSubject));
                        masterTopic.setSubjectLocator(new Locator(masterSubject));
                    }
                    else {
                        masterTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                    }
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
            if(masterTopic != null) {
                parent.fillImageTopic(masterTopic, tm, image);
            }
            
            this.tm=tm;
            this.parent=parent;
        }


        Topic masterTopic = null;
        public int progress=0;
        private TopicMap tm;
        private AbstractAlchemyExtractor parent;

        public static final String TAG_RESULTS="results";
        public static final String TAG_STATUS="status";
        public static final String TAG_STATUSINFO="statusInfo";
        public static final String TAG_URL="url";
        public static final String TAG_TOTALTRANSACTIONS="totalTransactions";
        public static final String TAG_IMAGEFACES="imageFaces";
        public static final String TAG_FACE="face";
        public static final String TAG_POSITIONX="positionX";
        public static final String TAG_POSITIONY="positionY";
        public static final String TAG_WIDTH="width";
        public static final String TAG_HEIGHT="height";
        public static final String TAG_GENDER="gender";
        public static final String TAG_SCORE="score";
        public static final String TAG_AGE="age";
        public static final String TAG_AGERANGE="ageRange";
        public static final String TAG_IDENTITY="identity";
        public static final String TAG_NAME="name";
        public static final String TAG_KNOWLEDGEGRAPH="knowledgeGraph";
        public static final String TAG_TYPEHIERARCHY="typeHierarchy";
        public static final String TAG_DISAMBIGUATED="disambiguated";
        public static final String TAG_SUBTYPE="subType";
        public static final String TAG_WEBSITE="website";
        public static final String TAG_DBPEDIA="dbpedia";
        public static final String TAG_FREEBASE="freebase";
        public static final String TAG_OPENCYC="opencyc";
        public static final String TAG_YAGO="yago";
        
        private static final int STATE_START=0;
        private static final int STATE_RESULTS=1;
        private static final int STATE_RESULTS_STATUS=11;
        private static final int STATE_RESULTS_STATUSINFO=111;
        private static final int STATE_RESULTS_URL=12;
        private static final int STATE_RESULTS_TOTALTRANSACTIONS=13;
        private static final int STATE_RESULTS_IMAGEFACES=14;
        private static final int STATE_RESULTS_IMAGEFACES_FACE=141;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_POSITIONX=1411;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_POSITIONY=1412;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_WIDTH=1413;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_HEIGHT=1414;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_GENDER=1415;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_GENDER_GENDER=14151;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_GENDER_SCORE=14152;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_AGE=1416;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_AGE_AGERANGE=14161;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_AGE_SCORE=14162;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY=1417;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_NAME=14171;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_SCORE=14172;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH=14173;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH_TYPEHIERARCHY=141731;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED=14174;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_NAME=141741;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_SUBTYPE=141742;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_WEBSITE=141743;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_DBPEDIA=141744;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_FREEBASE=141745;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_OPENCYC=141746;
        private static final int STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_YAGO=141747;
        
        private int state=STATE_START;


        private String data_status = "";
        private String data_status_info = "";
        private String data_url = "";
        private String data_total_transactions = "";
        private String data_face_position_x = "";
        private String data_face_position_y = "";
        private String data_face_width = "";
        private String data_face_height = "";
        private String data_face_gender = "";
        private String data_face_gender_score = "";
        private String data_face_agerange = "";
        private String data_face_agerange_score = "";
        private String data_face_identity_name = "";
        private String data_face_identity_score = "";
        private String data_face_identity_typehierarchy = "";
        private String data_face_identity_disambiguated_name = "";
        private String data_face_identity_disambiguated_subtype = "";
        private String data_face_identity_disambiguated_website = "";
        private String data_face_identity_disambiguated_dbpedia = "";
        private String data_face_identity_disambiguated_freebase = "";
        private String data_face_identity_disambiguated_opencyc = "";
        private String data_face_identity_disambiguated_yago = "";

        private ArrayList<String> data_face_identity_disambiguated_subtypes = new ArrayList();
        
        
        @Override
        public void startDocument() throws SAXException {
        }
        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_RESULTS;
                    }
                    break;
                    
                case STATE_RESULTS:
                    if(qName.equals(TAG_STATUS)) {
                        state = STATE_RESULTS_STATUS;
                        data_status = "";
                    }
                    else if(qName.equals(TAG_STATUSINFO)) {
                        state = STATE_RESULTS_STATUSINFO;
                        data_status_info = "";
                    }
                    else if(qName.equals(TAG_TOTALTRANSACTIONS)) {
                        state = STATE_RESULTS_TOTALTRANSACTIONS;
                        data_total_transactions = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS_URL;
                        data_url = "";
                    }
                    else if(qName.equals(TAG_IMAGEFACES)) {
                        state = STATE_RESULTS_IMAGEFACES;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES:
                    if(qName.equals(TAG_FACE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                        data_face_position_x = "";
                        data_face_position_y = "";
                        data_face_width = "";
                        data_face_height = "";
                        data_face_gender = "";
                        data_face_gender_score = "";
                        data_face_agerange = "";
                        data_face_agerange_score = "";
                        data_face_identity_name = "";
                        data_face_identity_score = "";
                        data_face_identity_typehierarchy = "";
                        data_face_identity_disambiguated_name = "";
                        data_face_identity_disambiguated_subtype = "";
                        data_face_identity_disambiguated_website = "";
                        data_face_identity_disambiguated_dbpedia = "";
                        data_face_identity_disambiguated_freebase = "";
                        data_face_identity_disambiguated_opencyc = "";
                        data_face_identity_disambiguated_yago = "";
                    }
                    break;

                case STATE_RESULTS_IMAGEFACES_FACE:
                    if(qName.equals(TAG_POSITIONX)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_POSITIONX;
                        data_face_position_x = "";
                    }
                    else if(qName.equals(TAG_POSITIONY)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_POSITIONY;
                        data_face_position_y = "";
                    }
                    else if(qName.equals(TAG_WIDTH)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_WIDTH;
                        data_face_width = "";
                    }
                    else if(qName.equals(TAG_HEIGHT)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_HEIGHT;
                        data_face_height = "";
                    }
                    else if(qName.equals(TAG_GENDER)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_GENDER;
                        data_face_gender = "";
                        data_face_gender_score = "";
                    }
                    else if(qName.equals(TAG_AGE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_AGE;
                        data_face_agerange = "";
                        data_face_agerange_score = "";
                    }
                    else if(qName.equals(TAG_IDENTITY)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY;
                        data_face_identity_name = "";
                        data_face_identity_score = "";
                        data_face_identity_typehierarchy = "";
                        data_face_identity_disambiguated_name = "";
                        data_face_identity_disambiguated_subtype = "";
                        data_face_identity_disambiguated_website = "";
                        data_face_identity_disambiguated_dbpedia = "";
                        data_face_identity_disambiguated_freebase = "";
                        data_face_identity_disambiguated_opencyc = "";
                        data_face_identity_disambiguated_yago = "";
                    }
                    break;

                case STATE_RESULTS_IMAGEFACES_FACE_GENDER:
                    if(qName.equals(TAG_GENDER)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_GENDER_GENDER;
                        data_face_gender = "";
                    }
                    else if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_GENDER_SCORE;
                        data_face_gender_score = "";
                    }
                    break;
                
                case STATE_RESULTS_IMAGEFACES_FACE_AGE:
                    if(qName.equals(TAG_AGERANGE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_AGE_AGERANGE;
                        data_face_agerange = "";
                    }
                    else if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_AGE_SCORE;
                        data_face_agerange_score = "";
                    }
                    break;
                
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_NAME;
                        data_face_identity_name = "";
                    }
                    else if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_SCORE;
                        data_face_identity_score = "";
                    }
                    else if(qName.equals(TAG_KNOWLEDGEGRAPH)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH;
                        data_face_identity_typehierarchy = "";
                    }
                    else if(qName.equals(TAG_DISAMBIGUATED)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                        data_face_identity_disambiguated_name = "";
                        data_face_identity_disambiguated_subtype = "";
                        data_face_identity_disambiguated_website = "";
                        data_face_identity_disambiguated_dbpedia = "";
                        data_face_identity_disambiguated_freebase = "";
                        data_face_identity_disambiguated_opencyc = "";
                        data_face_identity_disambiguated_yago = "";
                        data_face_identity_disambiguated_subtypes = new ArrayList();
                    }
                    break;
                
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH:
                    if(qName.equals(TAG_TYPEHIERARCHY)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH_TYPEHIERARCHY;
                        data_face_identity_typehierarchy = "";
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_NAME;
                        data_face_identity_disambiguated_name = "";
                    }
                    else if(qName.equals(TAG_SUBTYPE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_SUBTYPE;
                        data_face_identity_disambiguated_subtype = "";
                    }
                    else if(qName.equals(TAG_WEBSITE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_WEBSITE;
                        data_face_identity_disambiguated_website = "";
                    }
                    else if(qName.equals(TAG_DBPEDIA)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_DBPEDIA;
                        data_face_identity_disambiguated_dbpedia = "";
                    }
                    else if(qName.equals(TAG_FREEBASE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_FREEBASE;
                        data_face_identity_disambiguated_freebase = "";
                    }
                    else if(qName.equals(TAG_OPENCYC)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_OPENCYC;
                        data_face_identity_disambiguated_opencyc = "";
                    }
                    else if(qName.equals(TAG_YAGO)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_YAGO;
                        data_face_identity_disambiguated_yago = "";
                    }
                    break;
            }
        }







        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            System.out.println("   "+state);
            switch(state) {
                case STATE_RESULTS_IMAGEFACES_FACE:
                    if(qName.equals(TAG_FACE)) {
                        parent.log("Detected face.");
                        faceDetectionCounter++;
                        try {
                            Topic faceDetectionTopic = parent.getFaceDetectionTopic(tm, masterTopic);
                            if(faceDetectionTopic != null) {
                                if(isValid(data_face_position_x) && isValid(data_face_position_y)) {
                                    String data_face_position = data_face_position_x + "," + data_face_position_y;
                                    Topic faceDetectionPositionType = parent.getFaceDetectionPositionType(tm);
                                    Topic langTopic = parent.getOrCreateTopic(tm, XTMPSI.getLang(LANG));
                                    if(faceDetectionPositionType != null && langTopic != null) {
                                        faceDetectionTopic.setData(faceDetectionPositionType, langTopic, data_face_position);
                                    }
                                }
                                if(isValid(data_face_width) && isValid(data_face_height)) {
                                    String data_face_size = data_face_width + "," + data_face_height;
                                    Topic faceDetectionSizeType = parent.getFaceDetectionSizeType(tm);
                                    Topic langTopic = parent.getOrCreateTopic(tm, XTMPSI.getLang(LANG));
                                    if(faceDetectionSizeType != null && langTopic != null) {
                                        faceDetectionTopic.setData(faceDetectionSizeType, langTopic, data_face_size);
                                    }
                                }
                                if(isValid(data_face_gender)) {
                                    Topic faceDetectionGenderType = parent.getFaceDetectionGenderType(tm);
                                    Topic faceDetectionGenderTopic = parent.getFaceDetectionGenderTopic(data_face_gender, tm);
                                    
                                    if(faceDetectionGenderType != null && faceDetectionGenderTopic != null) {
                                        Association a = tm.createAssociation(faceDetectionGenderType);
                                        a.addPlayer(faceDetectionTopic, getFaceDetectionType(tm));
                                        a.addPlayer(faceDetectionGenderTopic, faceDetectionGenderType);

                                        if(isValid(data_face_gender_score)) {
                                            Topic faceDetectionGenderScoreTopic = parent.getScoreTopic(data_face_gender_score, tm);
                                            a.addPlayer(faceDetectionGenderScoreTopic, getScoreType(tm));
                                        }
                                    }
                                }
                                if(isValid(data_face_agerange)) {
                                    Topic faceDetectionAgeType = parent.getFaceDetectionAgeType(tm);
                                    Topic faceDetectionAgeTopic = parent.getFaceDetectionAgeTopic(data_face_agerange, tm);
                                    
                                    if(faceDetectionAgeType != null && faceDetectionAgeTopic != null) {
                                        Association a = tm.createAssociation(faceDetectionAgeType);
                                        a.addPlayer(faceDetectionTopic, getFaceDetectionType(tm));
                                        a.addPlayer(faceDetectionAgeTopic, faceDetectionAgeType);

                                        if(isValid(data_face_agerange_score)) {
                                            Topic faceDetectionAgeScoreTopic = parent.getScoreTopic(data_face_agerange_score, tm);
                                            a.addPlayer(faceDetectionAgeScoreTopic, getScoreType(tm));
                                        }
                                    }
                                }
                                if(isValid(data_face_identity_name)) {
                                    Topic faceDetectionIdentityType = parent.getFaceDetectionIdentityType(tm);
                                    Topic faceDetectionIdentityTopic = parent.getFaceDetectionIdentityTopic(data_face_identity_name, tm);
                                    
                                    if(faceDetectionIdentityType != null && faceDetectionIdentityTopic != null) {
                                        Association a = tm.createAssociation(faceDetectionIdentityType);
                                        a.addPlayer(faceDetectionTopic, getFaceDetectionType(tm));
                                        a.addPlayer(faceDetectionIdentityTopic, faceDetectionIdentityType);
                                        if(isValid(data_face_identity_score)) {
                                            Topic faceDetectionIdentityScoreTopic = parent.getScoreTopic(data_face_identity_score, tm);
                                            a.addPlayer(faceDetectionIdentityScoreTopic, getScoreType(tm));
                                        }
                                        
                                        if(isValid(data_face_identity_typehierarchy)) {
                                            Topic faceDetectionTypeHierarchyType = parent.getFaceDetectionTypeHierarchyType(tm);
                                            Topic langTopic = parent.getOrCreateTopic(tm, XTMPSI.getLang(LANG));
                                            if(faceDetectionTypeHierarchyType != null && langTopic != null) {
                                                faceDetectionIdentityTopic.setData(faceDetectionTypeHierarchyType, langTopic, data_face_identity_typehierarchy);
                                            }
                                            
                                            String[] typehierarchy = data_face_identity_typehierarchy.split("/");
                                            Topic parentTypeTopic = null;
                                            Topic typeTopic = null;
                                            for(String type : typehierarchy) {
                                                if(isValid(type)) {
                                                    parentTypeTopic = typeTopic;
                                                    typeTopic = parent.getFaceDetectionTypeHierarchyTopic(type, tm);
                                                    if(parentTypeTopic != null && typeTopic != null) {
                                                        Association a3 = tm.createAssociation(faceDetectionTypeHierarchyType);
                                                        a3.addPlayer(parentTypeTopic, parent.getFaceDetectionTypeHierarchyParent(tm));
                                                        a3.addPlayer(typeTopic, parent.getFaceDetectionTypeHierarchyChild(tm));
                                                    }
                                                }
                                            }
                                            if(typeTopic != null) {
                                                Topic sameAsType = getSameAsType(tm);
                                                Association saa = tm.createAssociation(sameAsType);
                                                saa.addPlayer(faceDetectionIdentityTopic, faceDetectionIdentityType);
                                                saa.addPlayer(typeTopic, sameAsType);
                                            }
                                        }
                                        if(isValid(data_face_identity_disambiguated_name)) {
                                            // TODO
                                        }
                                        if(isValid(data_face_identity_disambiguated_dbpedia)) {
                                            createSameAsAssociationWith(data_face_identity_disambiguated_dbpedia, faceDetectionIdentityTopic, faceDetectionIdentityType, tm);
                                        }
                                        if(isValid(data_face_identity_disambiguated_website)) {
                                            createSameAsAssociationWith(data_face_identity_disambiguated_website, faceDetectionIdentityTopic, faceDetectionIdentityType, tm);
                                        }
                                        if(isValid(data_face_identity_disambiguated_freebase)) {
                                            createSameAsAssociationWith(data_face_identity_disambiguated_freebase, faceDetectionIdentityTopic, faceDetectionIdentityType, tm);
                                        }
                                        if(isValid(data_face_identity_disambiguated_opencyc)) {
                                            createSameAsAssociationWith(data_face_identity_disambiguated_opencyc, faceDetectionIdentityTopic, faceDetectionIdentityType, tm);
                                        }
                                        if(isValid(data_face_identity_disambiguated_yago)) {
                                            createSameAsAssociationWith(data_face_identity_disambiguated_yago, faceDetectionIdentityTopic, faceDetectionIdentityType, tm);
                                        }
                                        if(!data_face_identity_disambiguated_subtypes.isEmpty()) {
                                            for(String subtype : data_face_identity_disambiguated_subtypes) {
                                                Topic faceDetectionIdentitySubtypeTopic = getFaceDetectionIdentitySubtypeTopic(subtype, tm);
                                                Topic faceDetectionIdentitySubtypeType = getFaceDetectionIdentitySubtypeType(tm);
                                                
                                                if(faceDetectionIdentitySubtypeTopic != null && faceDetectionIdentitySubtypeType != null) {
                                                    Association a2 = tm.createAssociation(faceDetectionIdentitySubtypeType);
                                                    a2.addPlayer(faceDetectionIdentitySubtypeTopic, faceDetectionIdentitySubtypeType);
                                                    a2.addPlayer(faceDetectionIdentityTopic, faceDetectionIdentityType);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        state = STATE_RESULTS_IMAGEFACES;
                    }
                    break;

                case STATE_RESULTS_IMAGEFACES:
                    if(qName.equals(TAG_IMAGEFACES)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS:
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_START;
                    }
                    break;


                case STATE_RESULTS_IMAGEFACES_FACE_POSITIONX:
                    if(qName.equals(TAG_POSITIONX)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_POSITIONY:
                    if(qName.equals(TAG_POSITIONY)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_WIDTH:
                    if(qName.equals(TAG_WIDTH)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_HEIGHT:
                    if(qName.equals(TAG_HEIGHT)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_GENDER:
                    if(qName.equals(TAG_GENDER)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_AGE:
                    if(qName.equals(TAG_AGE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY:
                    if(qName.equals(TAG_IDENTITY)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_GENDER_GENDER:
                    if(qName.equals(TAG_GENDER)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_GENDER;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_GENDER_SCORE:
                    if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_GENDER;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_AGE_AGERANGE:
                    if(qName.equals(TAG_AGERANGE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_AGE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_AGE_SCORE:
                    if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_AGE;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_NAME:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_SCORE:
                    if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH:
                    if(qName.equals(TAG_KNOWLEDGEGRAPH)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED:
                    if(qName.equals(TAG_DISAMBIGUATED)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH_TYPEHIERARCHY:
                    if(qName.equals(TAG_TYPEHIERARCHY)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_NAME:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_SUBTYPE:
                    if(qName.equals(TAG_SUBTYPE)) {
                        data_face_identity_disambiguated_subtypes.add(data_face_identity_disambiguated_subtype);
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_WEBSITE:
                    if(qName.equals(TAG_WEBSITE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_DBPEDIA:
                    if(qName.equals(TAG_DBPEDIA)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_FREEBASE:
                    if(qName.equals(TAG_FREEBASE)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_OPENCYC:
                    if(qName.equals(TAG_OPENCYC)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_YAGO:
                    if(qName.equals(TAG_YAGO)) {
                        state = STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED;
                    }
                    break;
                    
                    
                case STATE_RESULTS_URL:
                    if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS;
                    }
                    break;
                    
                case STATE_RESULTS_TOTALTRANSACTIONS:
                    if(qName.equals(TAG_TOTALTRANSACTIONS)) {
                        state = STATE_RESULTS;
                    }
                    break;
                    
                case STATE_RESULTS_STATUS:
                    if(qName.equals(TAG_STATUS)) {
                        state = STATE_RESULTS;
                        if(!"OK".equalsIgnoreCase(data_status)) {
                            parent.log("Warning: AlchemyAPI's response status was '"+data_status+"'");
                        }
                    }
                    break;
                case STATE_RESULTS_STATUSINFO:
                    if(qName.equals(TAG_STATUSINFO)) {
                        state = STATE_RESULTS;
                        if(!"OK".equalsIgnoreCase(data_status)) {
                            parent.log("Warning: AlchemyAPI's status info '"+data_status_info+"'");
                        }
                    }
                    break;
            }
        }






        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_RESULTS_STATUS:
                    data_status+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_STATUSINFO:
                    data_status_info+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_URL:
                    data_url+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_TOTALTRANSACTIONS:
                    data_total_transactions+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_POSITIONX:
                    data_face_position_x += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_POSITIONY:
                    data_face_position_y += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_WIDTH:
                    data_face_width += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_HEIGHT:
                    data_face_height += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_GENDER_GENDER:
                    data_face_gender += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_GENDER_SCORE:
                    data_face_gender_score += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_AGE_AGERANGE:
                    data_face_agerange += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_AGE_SCORE:
                    data_face_agerange_score += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_NAME:
                    data_face_identity_name += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_SCORE:
                    data_face_identity_score += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_KNOWLEDGEGRAPH_TYPEHIERARCHY:
                    data_face_identity_typehierarchy += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_NAME:
                    data_face_identity_disambiguated_name += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_SUBTYPE:
                    data_face_identity_disambiguated_subtype += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_WEBSITE:
                    data_face_identity_disambiguated_website += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_DBPEDIA:
                    data_face_identity_disambiguated_dbpedia += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_FREEBASE:
                    data_face_identity_disambiguated_freebase += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_OPENCYC:
                    data_face_identity_disambiguated_opencyc += new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEFACES_FACE_IDENTITY_DISAMBIGUATED_YAGO:
                    data_face_identity_disambiguated_yago += new String(ch,start,length);
                    break;
            }
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            parent.log("Error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            parent.log("Fatal error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
        }


        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
        @Override
        public void processingInstruction(String target, String data) throws SAXException {}
        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {}
        @Override
        public void endPrefixMapping(String prefix) throws SAXException {}
        @Override
        public void setDocumentLocator(org.xml.sax.Locator locator) {}
        @Override
        public void skippedEntity(String name) throws SAXException {}

    }


}