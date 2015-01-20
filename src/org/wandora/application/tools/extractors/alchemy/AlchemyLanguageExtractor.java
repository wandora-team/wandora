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
 *
 * AlchemyLanguageExtractor.java
 *
 *
 */
package org.wandora.application.tools.extractors.alchemy;


import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
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
public class AlchemyLanguageExtractor extends AbstractAlchemyExtractor {


    @Override
    public String getName() {
        return "Alchemy language extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts language information out of given text using AlchemyAPI. Read more at http://www.alchemyapi.com/";
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
        if(data != null && data.length() > 0) {
            if(apikey != null) apikey = apikey.trim();
            if(apikey != null && apikey.length() > 0) {
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

                String alchemyURL = ALCHEMY_URL+"calls/text/TextGetLanguage"; // ?apikey="+apikey+"&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String alchemyData = "apikey="+URLEncoder.encode(apikey, "utf-8")+"&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String result = sendRequest(new URL(alchemyURL), alchemyData, "application/x-www-form-urlencoded", "POST");

                System.out.println("Alchemy returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemyLanguageParser parserHandler = new AlchemyLanguageParser(getMasterSubject(), content, topicMap,this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                log("Alchemy language extraction done!");
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






    public class AlchemyLanguageParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public AlchemyLanguageParser(String term, String data, TopicMap tm, AbstractAlchemyExtractor parent){
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
        public static final String TAG_ISO6391="iso-639-1";
        public static final String TAG_ISO6392="iso-639-2";
        public static final String TAG_ISO6393="iso-639-3";
        public static final String TAG_ETHNILOGUE="ethnologue";
        public static final String TAG_SPEAKERS="native-speakers";
        public static final String TAG_WIKIPEDIA="wikipedia";

        private static final int STATE_START=0;
        private static final int STATE_RESULTS=1;
        private static final int STATE_RESULTS_STATUS=11;
        private static final int STATE_RESULTS_URL=13;
        private static final int STATE_RESULTS_LANGUAGE=14;
        private static final int STATE_RESULTS_ISO6391=15;
        private static final int STATE_RESULTS_ISO6392=16;
        private static final int STATE_RESULTS_ISO6393=17;
        private static final int STATE_RESULTS_ETHNILOGUE=18;
        private static final int STATE_RESULTS_SPEAKERS=19;
        private static final int STATE_RESULTS_WIKIPEDIA=20;

        private int state=STATE_START;


        private String data_status = "";
        private String data_url = "";
        private String data_language = "";
        private String data_iso6391 = "";
        private String data_iso6392 = "";
        private String data_iso6393 = "";
        private String data_ethnilogue = "";
        private String data_speakers = "";
        private String data_wikipedia = "";



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
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS_URL;
                        data_url = "";
                    }
                    else if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_RESULTS_LANGUAGE;
                        data_language = "";
                    }
                    else if(qName.equals(TAG_ISO6391)) {
                        state = STATE_RESULTS_ISO6391;
                        data_iso6391 = "";
                    }
                    else if(qName.equals(TAG_ISO6392)) {
                        state = STATE_RESULTS_ISO6392;
                        data_iso6392 = "";
                    }
                    else if(qName.equals(TAG_ISO6393)) {
                        state = STATE_RESULTS_ISO6393;
                        data_iso6393 = "";
                    }
                    else if(qName.equals(TAG_ETHNILOGUE)) {
                        state = STATE_RESULTS_ETHNILOGUE;
                        data_ethnilogue = "";
                    }
                    else if(qName.equals(TAG_SPEAKERS)) {
                        state = STATE_RESULTS_SPEAKERS;
                        data_speakers = "";
                    }
                    else if(qName.equals(TAG_WIKIPEDIA)) {
                        state = STATE_RESULTS_WIKIPEDIA;
                        data_wikipedia = "";
                    }
                    break;
            }
        }







        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_RESULTS:
                    if(qName.equals(TAG_RESULTS)) {

                        //parent.log("Found keyword '"+data_keyword_text+"'");

                        parent.setProgress( progress++ );
                        if(data_language != null && data_language.length() > 0) {
                            try {
                                Topic languageTopic = parent.getLanguageTopic(data_language, tm);
                                if(masterTopic != null && languageTopic != null) {
                                    if(parent.getCurrentLogger() != null) parent.log("Alchemy found language '"+data_language+"'.");
                                    Topic languageType = parent.getLanguageType(tm);

                                    Association a = tm.createAssociation(languageType);
                                    a.addPlayer(masterTopic, parent.getTopicType(tm));
                                    a.addPlayer(languageTopic, languageType);

                                    if(data_ethnilogue != null && data_ethnilogue.length() > 0) {
                                        try { languageTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_ethnilogue)); }
                                        catch(Exception e) { e.printStackTrace(); }
                                    }

                                    if(data_wikipedia != null && data_wikipedia.length() > 0) {
                                        try { languageTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_wikipedia)); }
                                        catch(Exception e) { e.printStackTrace(); }
                                    }

                                    if(data_iso6391 != null && data_iso6391.length() > 0) {
                                        try {
                                            languageTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(XTMPSI.LANG_PREFIX+data_iso6391));
                                            languageTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator("http://psi.oasis-open.org/iso/639/#"+data_iso6391));
                                        }
                                        catch(Exception e) { e.printStackTrace(); }
                                    }

                                    if(data_iso6392 != null && data_iso6392.length() > 0) {
                                        try { languageTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator("http://psi.oasis-open.org/iso/639/#"+data_iso6392)); }
                                        catch(Exception e) { e.printStackTrace(); }
                                    }
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        else {
                            parent.log("Zero length category text found! Rejecting!");
                        }

                        state = STATE_START;
                    }
                    break;



                case STATE_RESULTS_LANGUAGE:
                    if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_ISO6391:
                    if(qName.equals(TAG_ISO6391)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_ISO6392:
                    if(qName.equals(TAG_ISO6392)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_ISO6393:
                    if(qName.equals(TAG_ISO6393)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_ETHNILOGUE:
                    if(qName.equals(TAG_ETHNILOGUE)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_SPEAKERS:
                    if(qName.equals(TAG_SPEAKERS)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_WIKIPEDIA:
                    if(qName.equals(TAG_WIKIPEDIA)) {
                        state = STATE_RESULTS;
                    }
                    break;



                case STATE_RESULTS_URL:
                    if(qName.equals(TAG_URL)) {
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



        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_RESULTS_STATUS:
                    data_status+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_URL:
                    data_url+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_LANGUAGE:
                    data_language+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_ISO6391:
                    data_iso6391+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_ISO6392:
                    data_iso6392+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_ISO6393:
                    data_iso6393+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_ETHNILOGUE:
                    data_ethnilogue+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_SPEAKERS:
                    data_speakers+=new String(ch,start,length);
                    break;
               case STATE_RESULTS_WIKIPEDIA:
                    data_wikipedia+=new String(ch,start,length);
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

