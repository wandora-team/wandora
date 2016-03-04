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
import javax.imageio.ImageIO;
import static org.wandora.application.tools.extractors.alchemy.AbstractAlchemyExtractor.ALCHEMY_URL;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
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


public class AlchemyImageKeywordExtractor extends AbstractAlchemyExtractor {

    private static int tempCounter = 0;
    public static int scaleDownWidth = 800;
    public static boolean transformScores = true;
    private int keywordCounter = 0;
    

    
    
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }
    
    @Override
    public String getName() {
        return "Alchemy image keywords extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts keywords out of given images using AlchemyAPI service. Read more at http://www.alchemyapi.com/.";
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
                String alchemyURL = ALCHEMY_URL+"calls/image/ImageGetRankedImageKeywords";
                alchemyURL += "?apikey="+URLEncoder.encode(apikey, "utf-8");
                alchemyURL += "&imagePostMode=raw";
                alchemyURL += "&outputMode=xml";
                
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
                    if(!(s.endsWith("\n") || s.endsWith("\r"))) responseBuilder.append("\n");
                }
                in.close();
                String response = responseBuilder.toString();

                // System.out.println("AlchemyAPI says: "+response);

                // Parse the response
                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemyImageKeywordParser parserHandler = new AlchemyImageKeywordParser(locator, image, topicMap, this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                keywordCounter = 0;
                try {
                    reader.parse(new InputSource(new StringReader(response)));
                }
                catch(Exception e) {
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                if(keywordCounter > 0) {
                    log("AlchemyAPI found total " + keywordCounter + " keywords for the image.");
                }
                else {
                    log("AlchemyAPI didn't found any keywords for the image.");
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






    public class AlchemyImageKeywordParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public AlchemyImageKeywordParser(String masterSubject, BufferedImage image, TopicMap tm, AbstractAlchemyExtractor parent) {
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
        public static final String TAG_URL="url";
        public static final String TAG_TOTALTRANSACTIONS="totalTransactions";
        public static final String TAG_IMAGEKEYWORDS="imageKeywords";
        public static final String TAG_KEYWORD="keyword";
        public static final String TAG_TEXT="text";
        public static final String TAG_SCORE="score";
        
        private static final int STATE_START=0;
        private static final int STATE_RESULTS=1;
        private static final int STATE_RESULTS_STATUS=11;
        private static final int STATE_RESULTS_URL=12;
        private static final int STATE_RESULTS_TOTALTRANSACTIONS=13;
        private static final int STATE_RESULTS_IMAGEKEYWORDS=14;
        private static final int STATE_RESULTS_IMAGEKEYWORDS_KEYWORD=141;
        private static final int STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_TEXT=1411;
        private static final int STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_SCORE=1412;
        
        private int state=STATE_START;


        private String data_status = "";
        private String data_url = "";
        private String data_total_transactions = "";
        private String data_text = "";
        private String data_score = "";
        


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
                    else if(qName.equals(TAG_TOTALTRANSACTIONS)) {
                        state = STATE_RESULTS_TOTALTRANSACTIONS;
                        data_total_transactions = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS_URL;
                        data_url = "";
                    }
                    else if(qName.equals(TAG_IMAGEKEYWORDS)) {
                        state = STATE_RESULTS_IMAGEKEYWORDS;
                    }
                    break;
                case STATE_RESULTS_IMAGEKEYWORDS:
                    if(qName.equals(TAG_KEYWORD)) {
                        state = STATE_RESULTS_IMAGEKEYWORDS_KEYWORD;
                        data_score = "";
                        data_text = "";
                    }
                    break;

                case STATE_RESULTS_IMAGEKEYWORDS_KEYWORD:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_TEXT;
                        data_text = "";
                    }
                    else if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_SCORE;
                        data_score = "";
                    }
                    break;

            }
        }







        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_RESULTS_IMAGEKEYWORDS_KEYWORD:
                    if(qName.equals(TAG_KEYWORD)) {

                        //parent.log("Found keyword '"+data_keyword_text+"'");

                        if(data_text != null && data_text.length() > 0) {
                            try {
                                keywordCounter++;
                                if(parent.getCurrentLogger() != null) parent.log("Alchemy found image keyword '"+data_text+"'.");
                                Topic keywordTopic = parent.getKeywordTopic(data_text, tm);
                                if(masterTopic != null && keywordTopic != null) {
                                    Topic keywordType = parent.getImageKeywordType(tm);
                                    Association a = tm.createAssociation(keywordType);
                                    a.addPlayer(masterTopic, parent.getImageType(tm));
                                    a.addPlayer(keywordTopic, keywordType);
                                    if(transformScores) {
                                        if(data_score != null && data_score.length() > 0) {
                                            Topic scoreTopic = parent.getScoreTopic(data_score, tm);
                                            Topic scoreType = parent.getScoreType(tm);
                                            a.addPlayer(scoreTopic, scoreType);
                                        }
                                    }
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        else {
                            parent.log("Zero length keyword text found. Rejecting.");
                        }

                        state = STATE_RESULTS_IMAGEKEYWORDS;
                    }
                    break;

                case STATE_RESULTS_IMAGEKEYWORDS:
                    if(qName.equals(TAG_IMAGEKEYWORDS)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS:
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_START;
                    }
                    break;


                case STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_IMAGEKEYWORDS_KEYWORD;
                    }
                    break;
                    
                case STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_SCORE:
                    if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_IMAGEKEYWORDS_KEYWORD;
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
            }
        }






        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_RESULTS_STATUS:
                    data_status+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_URL:
                    data_url+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_TOTALTRANSACTIONS:
                    data_total_transactions+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_TEXT:
                    data_text+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_IMAGEKEYWORDS_KEYWORD_SCORE:
                    data_score+=new String(ch,start,length);
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