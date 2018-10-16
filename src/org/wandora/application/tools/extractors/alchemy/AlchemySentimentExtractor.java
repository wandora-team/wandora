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
 *
 */



package org.wandora.application.tools.extractors.alchemy;

import java.net.*;
import java.io.*;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

/**
 * Send any text to AlchemyAPI web service and transform results to topics and
 * associations.
 * 
 * @author akivela
 */
public class AlchemySentimentExtractor extends AbstractAlchemyExtractor {


	private static final long serialVersionUID = 1L;



	@Override
    public String getName() {
        return "Alchemy sentiment extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts sentiment out of given text using AlchemyAPI. Read more at http://www.alchemyapi.com/";
    }



    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(ExtractHelper.getContent(url),topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String data = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(data, topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(String data, TopicMap topicMap) throws Exception {
        String apikey = solveAPIKey();
        if(apikey != null && data != null && data.length() > 0) {
            apikey = apikey.trim();
            if(apikey.length() > 0) {
                String content = null;
                try {
                    content = XMLbox.cleanUp(data);
                    if(content == null || content.length() < 1) {
                        // Tidy failed to fix the file...
                        content = data;
                        //contentType = "text/html";
                    }
                    else {
                        // Ok, Tidy fixed the html/xml document
                        content = XMLbox.getAsText(content, defaultEncoding);
                        //System.out.println("content after getAsText: "+content);
                        //contentType = "text/txt";
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    content = data;
                    //contentType = "text/raw";
                }

                String alchemyURL = ALCHEMY_URL+"calls/text/TextGetTextSentiment"; // ?apikey="+apikey+"&maxRetrieve=100&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String alchemyData = "apikey="+URLEncoder.encode(apikey, "utf-8")+"&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String result = sendRequest(new URL(alchemyURL), alchemyData, "application/x-www-form-urlencoded", "POST");

                System.out.println("Alchemy returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemySentimentParser parserHandler = new AlchemySentimentParser(getMasterSubject(), content, topicMap,this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                //log("OK!");
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



    // -------------------------------------------------------------------------






    public class AlchemySentimentParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public AlchemySentimentParser(String term, String data, TopicMap tm, AbstractAlchemyExtractor parent){
            this.tm=tm;
            this.parent=parent;

            try {
                if(term != null) {
                    masterTopic = tm.getTopicWithBaseName(term);
                    if(masterTopic == null) masterTopic = tm.getTopic(term);
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            if(masterTopic == null && data != null && data.length() > 0) {
                try {
                    masterTopic = tm.createTopic();
                    masterTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                    parent.fillDocumentTopic(masterTopic, tm, data);
                }
                catch(Exception e) {
                    parent.log(e);
                }
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
        public static final String TAG_LANGUAGE="language";
        public static final String TAG_TEXT="text";
        public static final String TAG_DOCSENTIMENT="docSentiment";
        public static final String TAG_TYPE="type";
        public static final String TAG_SCORE="score";

        private static final int STATE_START=0;
        private static final int STATE_RESULTS=1;
        private static final int STATE_RESULTS_STATUS=11;
        private static final int STATE_RESULTS_LANGUAGE=12;
        private static final int STATE_RESULTS_URL=13;
        private static final int STATE_RESULTS_TEXT=14;
        private static final int STATE_RESULTS_DOCSENTIMENT=15;
        private static final int STATE_RESULTS_DOCSENTIMENT_TYPE=151;
        private static final int STATE_RESULTS_DOCSENTIMENT_SCORE=152;

        private int state=STATE_START;


        private String data_status = "";
        private String data_language = "";
        private String data_url = "";
        private String data_text = "";
        private String data_sentiment_type = "";
        private String data_sentiment_score = "";


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
                    else if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_RESULTS_LANGUAGE;
                        data_language = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS_URL;
                        data_url = "";
                    }
                    else if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_TEXT;
                        data_text = "";
                    }
                    else if(qName.equals(TAG_DOCSENTIMENT)) {
                        state = STATE_RESULTS_DOCSENTIMENT;
                    }
                    break;
                case STATE_RESULTS_DOCSENTIMENT:
                    if(qName.equals(TAG_TYPE)) {
                        state = STATE_RESULTS_DOCSENTIMENT_TYPE;
                        data_sentiment_type = "";
                    }
                    else if(qName.equals(TAG_SCORE))  {
                        state = STATE_RESULTS_DOCSENTIMENT_SCORE;
                        data_sentiment_score = "";
                    }
                    break;


            }
        }




        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_RESULTS_DOCSENTIMENT:
                    if(qName.equals(TAG_DOCSENTIMENT)) {
                        parent.setProgress( progress++ );
                        if(data_sentiment_type != null && data_sentiment_type.length() > 0) {
                            try {
                                if(parent.getCurrentLogger() != null) parent.log("Alchemy found sentiment type '"+data_sentiment_type+"'.");
                                Topic sentimentTypeTopic = parent.getSentimentTypeTopic(data_sentiment_type, tm);
                                if(masterTopic != null && sentimentTypeTopic != null) {
                                    Topic sentimentType = parent.getSentimentTypeType(tm);
                                    Association a = tm.createAssociation(sentimentType);
                                    a.addPlayer(masterTopic, parent.getTopicType(tm));
                                    a.addPlayer(sentimentTypeTopic, sentimentType);
                                    if(data_sentiment_score != null && data_sentiment_score.length() > 0) {
                                        Topic sentimentScoreTopic = parent.getSentimentScoreTopic(data_sentiment_score, tm);
                                        a.addPlayer(sentimentScoreTopic, parent.getSentimentScoreType(tm));
                                    }
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        else {
                            parent.log("Zero length sentiment type found! Rejecting!");
                        }

                        state = STATE_RESULTS;
                    }
                    break;

                case STATE_RESULTS_DOCSENTIMENT_TYPE:
                    if(qName.equals(TAG_TYPE)) {
                        state = STATE_RESULTS_DOCSENTIMENT;
                    }
                    break;
                case STATE_RESULTS_DOCSENTIMENT_SCORE:
                    if(qName.equals(TAG_SCORE)) {
                        state = STATE_RESULTS_DOCSENTIMENT;
                    }
                    break;

                case STATE_RESULTS:
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_START;
                    }
                    break;


                case STATE_RESULTS_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_URL:
                    if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_LANGUAGE:
                    if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_STATUS:
                    if(qName.equals(TAG_STATUS)) {
                        state = STATE_RESULTS;
                        if(!"OK".equalsIgnoreCase(data_status)) {
                            parent.log("Warning: Alchemy request status was '"+data_status+"'");
                        }
                    }
                    break;
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_RESULTS_STATUS:
                    data_status+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_LANGUAGE:
                    data_language+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_URL:
                    data_url+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_TEXT:
                    data_text+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_DOCSENTIMENT_TYPE:
                    data_sentiment_type+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_DOCSENTIMENT_SCORE:
                    data_sentiment_score+=new String(ch,start,length);
                    break;

            }
        }

        public void warning(SAXParseException exception) throws SAXException {
        }

        public void error(SAXParseException exception) throws SAXException {
            parent.log("Error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
        }

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
