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
 *
 */


package org.wandora.application.tools.extractors.yahoo.boss;

import java.net.*;
import java.io.*;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

/**
 *
 * @author akivela
 */
public class BossSearchResultExtractor extends AbstractBossExtractor {
    private String searchStr = null;




    @Override
    public String getName() {
        return "Yahoo! BOSS result extractor";
    }

    @Override
    public String getDescription(){
        return "Search Yahoo! BOSS and convert query and the result set to Topic Maps. Read more at http://developer.yahoo.com/search/boss.";
    }


    public void setSearchString(String searchStr) {
        this.searchStr = searchStr;
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



    public boolean _extractTopicsFrom(String result, TopicMap topicMap) throws Exception {
        if(result != null && result.length() > 0) {
            //System.out.println("Yahoo! BOSS returned == "+result);

            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            BossExtractorParser parserHandler = new BossExtractorParser(getMasterSubject(), searchStr, topicMap, this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            try {
                reader.parse(new InputSource(new StringReader(result)));
            }
            catch(Exception e){
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }
            log("Yahoo! BOSS search extraction finished!");
        }
        else {
            log("No valid data given! Aborting!");
        }
        searchStr = null;
        return true;
    }



    // -------------------------------------------------------------------------






    private class BossExtractorParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {


        public int progress=0;
        private TopicMap tm;
        private BossSearchResultExtractor parent;
        private Topic queryTopic = null;
        private Topic masterTopic = null;


        
        public BossExtractorParser(String term, String data, TopicMap tm, BossSearchResultExtractor parent){
            this.tm=tm;
            this.parent=parent;

            try {
                if(data != null && data.length() > 0) {
                    queryTopic = tm.createTopic();
                    queryTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                    parent.fillQueryTopic(queryTopic, tm, data);
                }
            }
            catch(Exception e) {
                parent.log(e);
            }

            try {
                if(term != null) {
                    masterTopic = tm.getTopicWithBaseName(term);
                    if(masterTopic == null) masterTopic = tm.getTopic(term);
                }
            }
            catch(Exception e) {
                parent.log(e);
            }

            if(masterTopic != null && queryTopic != null) {
                try {
                    Association a = tm.createAssociation(getQueryType(tm));
                    a.addPlayer(queryTopic, getQueryType(tm));
                    a.addPlayer(masterTopic, getSourceType(tm));
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
        }


        public static final String TAG_YSEARCHRESPONSE = "ysearchresponse";
        public static final String TAG_NEXTPAGE = "nextpage";

        public static final String TAG_RESULTSETWEB = "resultset_web";
        public static final String TAG_RESULTSETNEWS = "resultset_news";
        public static final String TAG_RESULTSETIMAGES = "resultset_images";

        public static final String TAG_RESULT = "result";
        public static final String TAG_ABSTRACT = "abstract";
        public static final String TAG_DATE = "date";
        public static final String TAG_DISPURL = "dispurl";
        public static final String TAG_CLICKURL = "clickurl";
        public static final String TAG_SIZE = "size";
        public static final String TAG_TITLE = "title";
        public static final String TAG_URL = "url";


        private static final int STATE_START=0;
        private static final int STATE_YSEARCHRESPONSE=1;
        private static final int STATE_YSEARCHRESPONSE_NEXTPAGE=11;
        
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB=12;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETIMAGES=13;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETNEWS=14;

        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT=121;
        
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_ABSTRACT=1211;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DATE=1212;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DISPURL=1213;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_CLICKURL=1214;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_SIZE=1215;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_TITLE=1216;
        private static final int STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_URL=1217;

        private int state=STATE_START;


        private String data_result_abstract = "";
        private String data_result_date = "";
        private String data_result_dispurl = "";
        private String data_result_clickurl = "";
        private String data_result_size = "";
        private String data_result_title = "";
        private String data_result_url = "";
        


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
                    if(qName.equals(TAG_YSEARCHRESPONSE)) {
                        state = STATE_YSEARCHRESPONSE;
                    }
                    break;
                case STATE_YSEARCHRESPONSE:
                    if(qName.equals(TAG_NEXTPAGE)) {
                        state = STATE_YSEARCHRESPONSE_NEXTPAGE;
                    }
                    else if(qName.equals(TAG_RESULTSETWEB))  {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB;
                    }
                    else if(qName.equals(TAG_RESULTSETIMAGES))  {
                        state = STATE_YSEARCHRESPONSE_RESULTSETIMAGES;
                    }
                    else if(qName.equals(TAG_RESULTSETNEWS))  {
                        state = STATE_YSEARCHRESPONSE_RESULTSETNEWS;
                    }
                    break;

                    
                case STATE_YSEARCHRESPONSE_RESULTSETNEWS:
                case STATE_YSEARCHRESPONSE_RESULTSETIMAGES:
                case STATE_YSEARCHRESPONSE_RESULTSETWEB: {
                    if(qName.equals(TAG_RESULT)) {
                        data_result_abstract = "";
                        data_result_date = "";
                        data_result_dispurl = "";
                        data_result_clickurl = "";
                        data_result_size = "";
                        data_result_title = "";
                        data_result_url = "";
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;
                }

                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT: {
                    if(qName.equals(TAG_ABSTRACT)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_ABSTRACT;
                    }
                    else if(qName.equals(TAG_DATE)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DATE;
                    }
                    else if(qName.equals(TAG_DISPURL)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DISPURL;
                    }
                    else if(qName.equals(TAG_CLICKURL)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_CLICKURL;
                    }
                    else if(qName.equals(TAG_SIZE)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_SIZE;
                    }
                    else if(qName.equals(TAG_TITLE)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_TITLE;
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_URL;
                    }
                    break;
                }
            }
        }







        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT:
                    if(qName.equals(TAG_RESULT)) {
                        parent.log("Found Yahoo! BOSS search result '"+data_result_title+"'");
                        parent.setProgress( progress++ );                      
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB;

                        try {
                            try {
                                if(data_result_title != null) data_result_title = new String(data_result_title.getBytes(defaultEncoding), "UTF-8");
                                if(data_result_abstract != null) data_result_abstract = new String(data_result_abstract.getBytes(defaultEncoding), "UTF-8");
                                if(data_result_url != null) data_result_url = new String(data_result_url.getBytes(defaultEncoding), "UTF-8");
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }

                            Topic searchResultTopic = parent.getWebSearchResultTopic( tm, data_result_title, data_result_abstract, data_result_url, data_result_date );
                            if(searchResultTopic != null && queryTopic != null) {
                                Topic searchResultType = parent.getWebSearchResultType(tm);
                                Association a = tm.createAssociation(searchResultType);
                                a.addPlayer(queryTopic, parent.getQueryType(tm));
                                a.addPlayer(searchResultTopic, searchResultType);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                    break;

                case STATE_YSEARCHRESPONSE_RESULTSETWEB:
                    if(qName.equals(TAG_RESULTSETWEB))
                        state = STATE_YSEARCHRESPONSE;
                    break;

                case STATE_YSEARCHRESPONSE_NEXTPAGE:
                    if(qName.equals(TAG_NEXTPAGE))
                        state = STATE_YSEARCHRESPONSE;
                    break;

                case STATE_YSEARCHRESPONSE:
                    if(qName.equals(TAG_YSEARCHRESPONSE))
                        state = STATE_START;
                    break;


                // ******** INSIDE RESULT *******
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_ABSTRACT:
                    if(qName.equals(TAG_ABSTRACT)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;

                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DATE:
                    if(qName.equals(TAG_DATE)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;

                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DISPURL:
                    if(qName.equals(TAG_DISPURL)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;

                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_CLICKURL:
                    if(qName.equals(TAG_CLICKURL)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;

                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_SIZE:
                    if(qName.equals(TAG_SIZE)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;

                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_TITLE:
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;

                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_URL:
                    if(qName.equals(TAG_URL)) {
                        state = STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT;
                    }
                    break;
                
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_ABSTRACT:
                    data_result_abstract+=new String(ch,start,length);
                    break;
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DATE:
                    data_result_date+=new String(ch,start,length);
                    break;
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_DISPURL:
                    data_result_dispurl+=new String(ch,start,length);
                    break;
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_CLICKURL:
                    data_result_clickurl+=new String(ch,start,length);
                    break;
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_SIZE:
                    data_result_size+=new String(ch,start,length);
                    break;
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_TITLE:
                    data_result_title+=new String(ch,start,length);
                    break;
                case STATE_YSEARCHRESPONSE_RESULTSETWEB_RESULT_URL:
                    data_result_url+=new String(ch,start,length);
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

