/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2023 Wandora Team
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


package org.wandora.application.tools.extractors.yahoo.termextraction;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.IObox;
import org.wandora.utils.XMLbox;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * THE YAHOO API THE EXTRACTOR USES IS SHUT DOWN DECEMBER 2010.
 * USE THE YQL TERM EXTRACTOR (SearchTermExtract) INSTEAD!
 *
 * @author akivela
 */


public class YahooTermExtractor extends AbstractYahooTermExtractor {


	private static final long serialVersionUID = 1L;



	@Override
    public String getName() {
        return "Yahoo! Term Extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts terms out of given text using Yahoo's Term Extractor web service. Read more at http://developer.yahoo.com/search/content/V1/termExtraction.html. "
                + "NOTICE: THE YAHOO API IS SHUT DOWN DECEMBER 2010. USE THE YQL TERM EXTRACTOR INSTEAD!";
    }



    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }


    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String data = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(data, topicMap);
    }


    public boolean _extractTopicsFrom(String data, TopicMap topicMap) throws Exception {
        String appid = solveAppId();
        if(data != null && data.length() > 0) {
            if(appid != null) appid = appid.trim();
            if(appid.length() > 0) {
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

                String extractURL = WEB_SERVICE_URL+"?appid="+appid+"&output=xml&context="+URLEncoder.encode(content, "utf-8");
                String result = IObox.doUrl(new URL(extractURL));

                System.out.println("Yahoo returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                YahooTermExtractorParser parserHandler = new YahooTermExtractorParser(getMasterSubject(), content, topicMap,this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                log("Total " + parserHandler.progress + " terms extracted by Yahoo!");
            }
            else {
                log("No valid application identifier given! Aborting!");
            }
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }



    // -------------------------------------------------------------------------






    public class YahooTermExtractorParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public YahooTermExtractorParser(String term, String data, TopicMap tm, YahooTermExtractor parent){
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
        private YahooTermExtractor parent;

        public static final String TAG_RESULTSET="ResultSet";
        public static final String TAG_RESULT="Result";

        public static final String TAG_ERROR="Error";
        public static final String TAG_DESCRIPTION="Description";
        public static final String TAG_DETAIL="Detail";
        public static final String TAG_MESSAGE="Message";

        private static final int STATE_START=0;
        private static final int STATE_RESULTSET=1;
        private static final int STATE_RESULTSET_RESULT=11;

        private static final int STATE_ERROR=2;
        private static final int STATE_ERROR_DESCRIPTION=21;
        private static final int STATE_ERROR_DETAIL=22;
        private static final int STATE_ERROR_MESSAGE=23;
        
        private int state=STATE_START;


        private String data_error_detail = "";
        private String data_error_description = "";
        private String data_error_message = "";

        private String data_result = "";
        


        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(qName.equals(TAG_RESULTSET)) {
                        state = STATE_RESULTSET;
                    }
                    else if(qName.equals(TAG_ERROR)) {
                        data_error_detail = "";
                        data_error_description = "";
                        state = STATE_ERROR;
                    }
                    break;
                case STATE_RESULTSET:
                    if(qName.equals(TAG_RESULT)) {
                        state = STATE_RESULTSET_RESULT;
                        data_result = "";
                    }
                    break;

                case STATE_ERROR: {
                    if(qName.equals(TAG_DESCRIPTION)) {
                        data_error_description = "";
                        state = STATE_ERROR_DESCRIPTION;
                    }
                    else if(qName.equals(TAG_DETAIL)) {
                        data_error_detail = "";
                        state = STATE_ERROR_DETAIL;
                    }
                    else if(qName.equals(TAG_MESSAGE)) {
                        data_error_message = "";
                        state = STATE_ERROR_MESSAGE;
                    }
                }
            }
        }







        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_RESULTSET_RESULT:
                    if(qName.equals(TAG_RESULT)) {

                        //parent.log("Found keyword '"+data_keyword_text+"'");

                        parent.setProgress( progress++ );
                        if(data_result != null && data_result.length() > 0) {
                            try {
                                if(parent.getCurrentLogger() != null) parent.log("Yahoo Term Extractor found term '"+data_result+"'.");
                                Topic termTopic = parent.getTermTopic(data_result, tm);
                                if(masterTopic != null && termTopic != null) {
                                    Topic termType = parent.getTermType(tm);
                                    Association a = tm.createAssociation(termType);
                                    a.addPlayer(masterTopic, parent.getTopicType(tm));
                                    a.addPlayer(termTopic, termType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        else {
                            parent.log("Zero length term text found! Rejecting!");
                        }

                        state = STATE_RESULTSET;
                    }
                    break;


                // ******** ERROR *******
                case STATE_ERROR_DESCRIPTION:
                    if(qName.equals(TAG_DESCRIPTION)) {
                        parent.log("Yahoo! says: "+data_error_description);
                        state = STATE_ERROR;
                    }
                    break;

                case STATE_ERROR_DETAIL:
                    if(qName.equals(TAG_DETAIL)) {
                        parent.log("Yahoo! says: "+data_error_detail);
                        state = STATE_ERROR;
                    }
                    break;
                case STATE_ERROR_MESSAGE:
                    if(qName.equals(TAG_MESSAGE)) {
                        parent.log("Yahoo! says: "+data_error_message);
                        state = STATE_ERROR;
                    }
                    break;


                    
                case STATE_ERROR:
                    if(qName.equals(TAG_ERROR)) {
                        state = STATE_START;
                    }
                    break;


                case STATE_RESULTSET:
                    if(qName.equals(TAG_RESULTSET)) {
                        state = STATE_START;
                    }
                    break;
                
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_RESULTSET_RESULT:
                    data_result+=new String(ch,start,length);
                    break;
                case STATE_ERROR_DESCRIPTION:
                    data_error_description+=new String(ch,start,length);
                    break;
                case STATE_ERROR_DETAIL:
                    data_error_detail+=new String(ch,start,length);
                    break;
                case STATE_ERROR_MESSAGE:
                    data_error_message+=new String(ch,start,length);
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


        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
        public void processingInstruction(String target, String data) throws SAXException {}
        public void startPrefixMapping(String prefix, String uri) throws SAXException {}
        public void endPrefixMapping(String prefix) throws SAXException {}
        public void setDocumentLocator(org.xml.sax.Locator locator) {}
        public void skippedEntity(String name) throws SAXException {}

    }


}

