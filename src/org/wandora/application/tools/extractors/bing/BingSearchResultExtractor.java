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
 *
 * BingSearchExtractor.java
 *
 *
 */


package org.wandora.application.tools.extractors.bing;


import com.mashape.unirest.http.Unirest;
import java.net.*;
import java.io.*;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;
/**
 *
 * @author akivela
 */
public class BingSearchResultExtractor extends AbstractBingExtractor {

    private String searchStr = null;





    @Override
    public String getName() {
        return "Bing search result extractor";
    }

    @Override
    public String getDescription(){
        return "Converts Microsoft Bing search engine result attribute based XML feed to a topic map. Input should be a valid Bing search result feed.";
    }


    public void setSearchString(String searchStr) {
        this.searchStr = searchStr;
    }
    


    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String u = url.toExternalForm();
        
        String result = Unirest.get(u).asString().getBody();
        
        return _extractTopicsFrom(result,topicMap);
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
            //System.out.println("Bing returned == "+result);

            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            AtomParser parserHandler = new AtomParser(getMasterSubject(), searchStr, topicMap,this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            try {
                reader.parse(new InputSource(new StringReader(result)));
            }
            catch(Exception e){
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }
            log("Bing search extraction finished!");
        }
        else {
            log("No valid data given! Aborting!");
        }
        searchStr = null;
        return true;
    }



    // -------------------------------------------------------------------------
    // -------------------------------------------------- BING RESULT PARSER ---
    // -------------------------------------------------------------------------





    protected class BingParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public int progress=0;
        private TopicMap tm;
        private AbstractBingExtractor parent;
        
        private Topic queryTopic = null;
        private Topic masterTopic = null;

        public BingParser(String term, String data, TopicMap tm, AbstractBingExtractor parent){
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

        public static final String TAG_SEARCHRESPONSE="SearchResponse";
        public static final String TAG_QUERY="Query";

        public static final String TAG_SPELL="spl:Spell";
        public static final String TAG_SPELLRESULTS="spl:Results";
        public static final String TAG_SPELLRESULT="spl:SpellResult";

        public static final String TAG_WEB="web:Web";
        public static final String TAG_WEBRESULTS="web:Results";
        public static final String TAG_WEBRESULT="web:WebResult";

        public static final String TAG_IMAGE="mms:Image";
        public static final String TAG_IMAGERESULTS="mms:Results";
        public static final String TAG_IMAGERESULT="mms:ImageResult";
        public static final String TAG_THUMBNAIL="mms:Thumbnail";

        public static final String TAG_ERRORS="Errors";
        public static final String TAG_ERROR="Error";



        private static final int STATE_START=0;
        private static final int STATE_SEARCHRESPONSE=1;
        private static final int STATE_SEARCHRESPONSE_QUERY=11;

        private static final int STATE_SEARCHRESPONSE_SPELL=12;
        private static final int STATE_SEARCHRESPONSE_SPELL_RESULTS=121;
        private static final int STATE_SEARCHRESPONSE_SPELL_RESULTS_SPELLRESULT=1211;

        private static final int STATE_SEARCHRESPONSE_WEB=13;
        private static final int STATE_SEARCHRESPONSE_WEB_RESULTS=131;
        private static final int STATE_SEARCHRESPONSE_WEB_RESULTS_WEBRESULT=1311;

        private static final int STATE_SEARCHRESPONSE_IMAGE=14;
        private static final int STATE_SEARCHRESPONSE_IMAGE_RESULTS=141;
        private static final int STATE_SEARCHRESPONSE_IMAGE_RESULTS_IMAGERESULT=1411;
        private static final int STATE_SEARCHRESPONSE_IMAGE_RESULTS_IMAGERESULT_THUMBNAIL=14111;

        private static final int STATE_SEARCHRESPONSE_ERRORS=20;
        private static final int STATE_SEARCHRESPONSE_ERRORS_ERROR=201;

        private int state=STATE_START;




        @Override
        public void startDocument() throws SAXException {
        }
        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            // System.out.println("qname: "+qName);
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(qName.equals(TAG_SEARCHRESPONSE)) {
                        state = STATE_SEARCHRESPONSE;
                    }
                    break;
                case STATE_SEARCHRESPONSE:
                    if(qName.equals(TAG_QUERY)) {
                        state = STATE_SEARCHRESPONSE_QUERY;
                        // System.out.println("Query-SearchTerms: "+atts.getValue("SearchTerms"));
                        // System.out.println("Query-AlteredQuery: "+atts.getValue("AlteredQuery"));
                    }
                    else if(qName.equals(TAG_SPELL)) {
                        state = STATE_SEARCHRESPONSE_SPELL;
                    }
                    else if(qName.equals(TAG_WEB)) {
                        state = STATE_SEARCHRESPONSE_WEB;
                    }
                    else if(qName.equals(TAG_IMAGE)) {
                        state = STATE_SEARCHRESPONSE_IMAGE;
                    }
                    else if(qName.equals(TAG_ERRORS)) {
                        state = STATE_SEARCHRESPONSE_ERRORS;
                    }
                    break;
                case STATE_SEARCHRESPONSE_QUERY:
                    break;

                case STATE_SEARCHRESPONSE_SPELL:
                    if(qName.equals(TAG_SPELLRESULTS)) {
                        state = STATE_SEARCHRESPONSE_SPELL_RESULTS;
                    }
                    break;
                case STATE_SEARCHRESPONSE_WEB:
                    if(qName.equals(TAG_WEBRESULTS)) {
                        state = STATE_SEARCHRESPONSE_WEB_RESULTS;
                    }
                    break;
                case STATE_SEARCHRESPONSE_IMAGE:
                    if(qName.equals(TAG_IMAGERESULTS)) {
                        state = STATE_SEARCHRESPONSE_IMAGE_RESULTS;
                    }
                    break;
                case STATE_SEARCHRESPONSE_SPELL_RESULTS:
                    if(qName.equals(TAG_SPELLRESULT)) {
                        state = STATE_SEARCHRESPONSE_SPELL_RESULTS_SPELLRESULT;
                        // System.out.println("SpellResult-Value: "+atts.getValue("Value"));
                        // TODO
                    }
                    break;
                case STATE_SEARCHRESPONSE_WEB_RESULTS:
                    if(qName.equals(TAG_WEBRESULT)) {
                        parent.setProgress( progress++ );
                        state = STATE_SEARCHRESPONSE_WEB_RESULTS_WEBRESULT;

                        // System.out.println("WebResult-Title: "+atts.getValue("Title"));
                        // System.out.println("WebResult-Description: "+atts.getValue("Description"));
                        // System.out.println("WebResult-Url: "+atts.getValue("Url"));
                        // System.out.println("WebResult-Datetime: "+atts.getValue("DateTime"));
                        
                        try {
                            String title = atts.getValue("Title");
                            String description = atts.getValue("Description");
                            String url = atts.getValue("Url");
                            String datetime = atts.getValue("DateTime");

                            
                            try {
                                if(title != null) title = new String(title.getBytes(defaultEncoding), "UTF-8");
                                if(description != null) description = new String(description.getBytes(defaultEncoding), "UTF-8");
                                if(url != null) url = new String(url.getBytes(defaultEncoding), "UTF-8");
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                            
                            
                            Topic searchResultTopic = parent.getWebSearchResultTopic( tm, title, description, url, datetime );
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

                case STATE_SEARCHRESPONSE_IMAGE_RESULTS:
                    if(qName.equals(TAG_IMAGERESULT)) {
                        state = STATE_SEARCHRESPONSE_IMAGE_RESULTS_IMAGERESULT;

                        System.out.println("ImageResult-Title: "+atts.getValue("Title"));
                        System.out.println("ImageResult-MediaUrl: "+atts.getValue("MediaUrl"));
                        System.out.println("ImageResult-Url: "+atts.getValue("Url"));
                        System.out.println("ImageResult-Width: "+atts.getValue("Width"));
                        System.out.println("ImageResult-Height: "+atts.getValue("Height"));
                        System.out.println("ImageResult-ContentType: "+atts.getValue("ContentType"));
                        System.out.println("ImageResult-FileSize: "+atts.getValue("FileSize"));

                        try {
                            String title = atts.getValue("Title");
                            String url = atts.getValue("MediaUrl");
                            String width = atts.getValue("Width");
                            String height = atts.getValue("Height");
                            String contentType = atts.getValue("ContentType");
                            String fileSize = atts.getValue("FileSize");

                            try {
                                if(title != null) title = new String(title.getBytes(defaultEncoding), "UTF-8");
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }


                            Topic searchResultTopic = parent.getImageSearchResultTopic( tm, title, url, width, height, contentType, fileSize );
                            if(searchResultTopic != null && queryTopic != null) {
                                Topic imageSearchResultType = parent.getImageSearchResultType(tm);
                                Association a = tm.createAssociation(imageSearchResultType);
                                a.addPlayer(queryTopic, parent.getQueryType(tm));
                                a.addPlayer(searchResultTopic, imageSearchResultType);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                    break;

                case STATE_SEARCHRESPONSE_ERRORS:
                    if(qName.equals(TAG_ERROR)) {
                        state = STATE_SEARCHRESPONSE_ERRORS_ERROR;
                        parent.log("Error occurred while accessing Bing.");
                        parent.log("  Code: "+atts.getValue("Code"));
                        parent.log("  Message: "+atts.getValue("Message"));
                        parent.log("  Parameter: "+atts.getValue("Parameter"));
                        parent.log("  Value: "+atts.getValue("Value"));
                        parent.log("  HelpUrl: "+atts.getValue("HelpUrl"));
                    }
                    break;
            }
        }







        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_SEARCHRESPONSE:
                    if(qName.equals(TAG_SEARCHRESPONSE)) {
                        state = STATE_START;
                    }
                    break;



                case STATE_SEARCHRESPONSE_QUERY:
                    if(qName.equals(TAG_QUERY)) {
                        state = STATE_SEARCHRESPONSE;
                    }
                    break;
                case STATE_SEARCHRESPONSE_SPELL:
                    if(qName.equals(TAG_SPELL)) {
                        state = STATE_SEARCHRESPONSE;
                    }
                    break;
                case STATE_SEARCHRESPONSE_WEB:
                    if(qName.equals(TAG_WEB)) {
                        state = STATE_SEARCHRESPONSE;
                    }
                    break;
                case STATE_SEARCHRESPONSE_IMAGE:
                    if(qName.equals(TAG_IMAGE)) {
                        state = STATE_SEARCHRESPONSE;
                    }
                    break;
                case STATE_SEARCHRESPONSE_SPELL_RESULTS:
                    if(qName.equals(TAG_SPELLRESULTS)) {
                        state = STATE_SEARCHRESPONSE_SPELL;
                    }
                    break;
                case STATE_SEARCHRESPONSE_WEB_RESULTS:
                    if(qName.equals(TAG_WEBRESULTS)) {
                        state = STATE_SEARCHRESPONSE_WEB;
                    }
                    break;
                case STATE_SEARCHRESPONSE_IMAGE_RESULTS:
                    if(qName.equals(TAG_IMAGERESULTS)) {
                        state = STATE_SEARCHRESPONSE_IMAGE;
                    }
                    break;
                case STATE_SEARCHRESPONSE_SPELL_RESULTS_SPELLRESULT:
                    if(qName.equals(TAG_SPELLRESULT)) {
                        state = STATE_SEARCHRESPONSE_SPELL_RESULTS;
                    }
                    break;
                case STATE_SEARCHRESPONSE_WEB_RESULTS_WEBRESULT:
                    if(qName.equals(TAG_WEBRESULT)) {
                        state = STATE_SEARCHRESPONSE_WEB_RESULTS;
                    }
                    break;
                case STATE_SEARCHRESPONSE_IMAGE_RESULTS_IMAGERESULT:
                    if(qName.equals(TAG_IMAGERESULT)) {
                        state = STATE_SEARCHRESPONSE_IMAGE_RESULTS;
                    }
                    break;


                    
                case STATE_SEARCHRESPONSE_ERRORS_ERROR:
                    if(qName.equals(TAG_ERROR)) {
                        state = STATE_SEARCHRESPONSE_ERRORS;
                    }
                    break;
                case STATE_SEARCHRESPONSE_ERRORS:
                    if(qName.equals(TAG_ERRORS)) {
                        state = STATE_SEARCHRESPONSE;
                    }
                    break;
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state) {

                // NOTHING HERE AS ALL DATA IS IN ATTRIBUTES

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
    
    public static final String DEFAULT_LANG = "en";
    private String baseUrl = null;
    
    private class AtomParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public boolean MAKE_LINK_SUBJECT_LOCATOR = false;
        public boolean MAKE_SUBCLASS_OF_WANDORA_CLASS = true;
        
        private Topic queryTopic = null;
        private Topic masterTopic = null;
        
        public AtomParser(TopicMap tm, AbstractBingExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public AtomParser(String term, String data, TopicMap tm, AbstractBingExtractor parent){
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
        
        public int progress=0;
        public int entryCount = 0;
        public int feedCount = 0;
        
        private TopicMap tm;
        private AbstractBingExtractor parent;
        
        public static final String TAG_FEED="feed";
        public static final String TAG_TITLE="d:Title";
        public static final String TAG_LINK="d:Url";
        public static final String TAG_UPDATED="updated";
        public static final String TAG_AUTHOR="author";
        public static final String TAG_NAME="name";
        public static final String TAG_EMAIL="email";
        public static final String TAG_URI="id";
        public static final String TAG_ID="d:ID";
        
        public static final String TAG_ENTRY="entry";
        public static final String TAG_SUMMARY="summary";
        
        public static final String TAG_CATEGORY="category";
        public static final String TAG_CONTRIBUTOR="contributor";
        
        public static final String TAG_GENERATOR="generator";
        public static final String TAG_ICON="icon";
        public static final String TAG_LOGO="logo";
        public static final String TAG_RIGHTS="rights";
        public static final String TAG_SUBTITLE="subtitle";
        
        public static final String TAG_CONTENT="d:Description";
        public static final String TAG_PUBLISHED="published";
        
        public static final String TAG_SOURCE="source";


       
        
        private static final int STATE_START=0;
        private static final int STATE_FEED=2;
        private static final int STATE_FEED_TITLE=4;
        private static final int STATE_FEED_LINK=5;
        private static final int STATE_FEED_UPDATED=6;
        private static final int STATE_FEED_AUTHOR=7;
        private static final int STATE_FEED_AUTHOR_NAME=71;
        private static final int STATE_FEED_AUTHOR_URI=72;
        private static final int STATE_FEED_AUTHOR_EMAIL=73;
        
        private static final int STATE_FEED_ID=8;
        private static final int STATE_FEED_CATEGORY=9;
        private static final int STATE_FEED_GENERATOR=15;
        private static final int STATE_FEED_CONTRIBUTOR=10;
        private static final int STATE_FEED_CONTRIBUTOR_NAME=101;
        private static final int STATE_FEED_CONTRIBUTOR_EMAIL=102;
        private static final int STATE_FEED_CONTRIBUTOR_URI=103;
        
        private static final int STATE_FEED_ICON=11;
        private static final int STATE_FEED_LOGO=12;
        private static final int STATE_FEED_RIGHTS=13;
        private static final int STATE_FEED_SUBTITLE=14;
        
        private static final int STATE_FEED_ENTRY = 1000;
        private static final int STATE_FEED_ENTRY_ID = 1001;
        private static final int STATE_FEED_ENTRY_TITLE = 1002;
        private static final int STATE_FEED_ENTRY_UPDATED = 1003;
        private static final int STATE_FEED_ENTRY_AUTHOR = 1004;
        private static final int STATE_FEED_ENTRY_AUTHOR_NAME = 10041;
        private static final int STATE_FEED_ENTRY_AUTHOR_EMAIL = 10042;
        private static final int STATE_FEED_ENTRY_AUTHOR_URI = 10043;
        
        private static final int STATE_FEED_ENTRY_CONTENT = 1005;
        private static final int STATE_FEED_ENTRY_LINK = 1006;
        private static final int STATE_FEED_ENTRY_SUMMARY = 1007;
        
        private static final int STATE_FEED_ENTRY_CATEGORY=1008;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR=1009;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR_NAME=10091;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL=10092;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR_URI=10093;
        
        private static final int STATE_FEED_ENTRY_PUBLISHED = 1010;
        
        private static final int STATE_FEED_ENTRY_SOURCE = 1011;
        private static final int STATE_FEED_ENTRY_SOURCE_ID = 10111;
        private static final int STATE_FEED_ENTRY_SOURCE_TITLE = 10112;
        private static final int STATE_FEED_ENTRY_SOURCE_UPDATED = 10113;
        private static final int STATE_FEED_ENTRY_SOURCE_RIGHTS = 10114;
        
        private static final int STATE_FEED_ENTRY_RIGHTS = 1012;
        
        
        
        
        private int state=STATE_START;
        
        public String BINGSI = "http://www.bing.com";

        public String SIPREFIX="http://wandora.org/si/bing/";
        public String FEED_ID_SI=SIPREFIX+"id";
        public String FEED_SI=SIPREFIX+"channel";
        public String FEED_UPDATED_SI=SIPREFIX+"updated";
        public String FEED_AUTHOR_SI=SIPREFIX+"author";
        public String FEED_CONTRIBUTOR_SI=SIPREFIX+"contributor";
        public String FEED_CATEGORY_SI=SIPREFIX+"category";
        public String FEED_SUBTITLE_SI=SIPREFIX+"subtitle";
        
        public String EMAIL_ADDRESS_SI=SIPREFIX+"email";
        public String SCHEME_SI=SIPREFIX+"scheme";
        
        
        
        
        
        public String FEED_LINK_SI=FEED_SI+"/link";
        public String FEED_GENERATOR_SI=FEED_SI+"/generator";
        public String FEED_GENERATOR_URI_SI=FEED_SI+"/generator-uri";
        public String FEED_GENERATOR_VERSION_SI=FEED_SI+"/generator-version";
        
        public String FEED_RIGHTS_SI=FEED_SI+"/rights";
        public String FEED_ICON_SI=FEED_SI+"/icon";
        public String FEED_LOGO_SI=FEED_SI+"/logo";
        
        
        public String FEED_ENTRY_SI=FEED_SI+"/entry";
        public String FEED_ENTRY_ID_SI=FEED_ENTRY_SI+"/id";
        public String FEED_ENTRY_TITLE_SI=FEED_ENTRY_SI+"/title";
        public String FEED_ENTRY_LINK_SI=FEED_ENTRY_SI+"/link";
        public String FEED_ENTRY_CONTRIBUTOR_SI=FEED_ENTRY_SI+"/contributor";
        public String FEED_ENTRY_SUMMARY_SI=FEED_ENTRY_SI+"/summary";
        public String FEED_ENTRY_CONTENT_SI=FEED_ENTRY_SI+"/content";
        
        public String FEED_ENTRY_PUBLISHED_SI=FEED_ENTRY_SI+"/published";
        public String FEED_ENTRY_UPDATED_SI=FEED_ENTRY_SI+"/updated";
        public String FEED_ENTRY_CATEGORY_SI=FEED_ENTRY_SI+"/category";
        public String FEED_ENTRY_AUTHOR_SI=FEED_ENTRY_SI+"/author";
        public String FEED_ENTRY_RIGHTS_SI=FEED_ENTRY_SI+"/rights";
        
        public String FEED_ENTRY_SOURCE_SI=FEED_ENTRY_SI+"/source";
        
        public String LINK_HREF_SI=SIPREFIX+"link/href"; 
        public String LINK_REL_SI=SIPREFIX+"link/rel";  
        public String LINK_TYPE_SI=SIPREFIX+"link/type";
        public String LINK_HREF_LANG_SI=SIPREFIX+"link/href-lang"; 
        
        public String DATE_SI="http://wandora.org/si/date"; 
        public String RIGHTS_SI="http://wandora.org/si/rights"; 
        public String LINK_SI="http://wandora.org/si/link";
        
        private String data_feed_id;
        private String data_feed_title;
        private String data_feed_title_type;
        private Link data_feed_link;
        
        private String data_feed_updated;
        
        private String data_feed_author_name;
        private String data_feed_author_uri;
        private String data_feed_author_email;
        private Category data_feed_category;
        
        private String data_feed_contributor_name;
        private String data_feed_contributor_uri;
        private String data_feed_contributor_email;
                
        private String data_feed_generator;
        private String data_feed_generator_uri;
        private String data_feed_generator_version;
        
        private String data_feed_icon;
        private String data_feed_logo;
        private String data_feed_rights;
        private String data_feed_rights_type;
        private String data_feed_subtitle;
        private String data_feed_subtitle_type;
        
        private String data_entry_id;
        private String data_entry_title;
        private String data_entry_title_type;
                
        private String data_entry_updated;
        private String data_entry_published;
        
        private String data_entry_author_name;
        private String data_entry_author_uri;
        private String data_entry_author_email;
        private String data_entry_content;
        private String data_entry_content_type;
        private String data_entry_content_src;
        
        private Link data_entry_link;
        
        private String data_entry_summary;
        private String data_entry_summary_src;
        private String data_entry_summary_type;
        
        private Category data_entry_category;
        
        private String data_entry_contributor_name;
        private String data_entry_contributor_uri;
        private String data_entry_contributor_email;
        
        private String data_entry_source_id;
        private String data_entry_source_title;
        private String data_entry_source_updated;
        private String data_entry_source_rights;
        private String data_entry_source_rights_type;
        
        private String data_entry_rights;
        private String data_entry_rights_type;
        
        private Topic theFeed;
        private Topic theEntry;
        
        private org.wandora.topicmap.Locator theFeedSI = null;
        private org.wandora.topicmap.Locator theEntrySI = null;

        
        
        private Topic getOrCreateTopic(String si) throws TopicMapException {
            return getOrCreateTopic(si,null);
        }
        private Topic getOrCreateTopic(String si,String bn) throws TopicMapException {
            if(si!=null){
                si = TopicTools.cleanDirtyLocator(si);
                Topic t=tm.getTopic(si);
                if(t==null){
                    t=tm.createTopic();
                    t.addSubjectIdentifier(tm.createLocator(si));
                    if(bn!=null) t.setBaseName(bn);
                }
                return t;
            }
            else{
                Topic t=tm.getTopicWithBaseName(bn);
                if(t==null){
                    t=tm.createTopic();
                    t.setBaseName(bn);
                    if(si!=null) t.addSubjectIdentifier(tm.createLocator(si));
                    else t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                }
                return t;
            }
        }
        
        
        private Topic getFeedType() {
            try {
                return getOrCreateTopic(FEED_SI, "Bing Feed");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getEntryType() {
            try {
                return getOrCreateTopic(FEED_ENTRY_SI, "Bing Entry");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getDateType() {
            try {
                return getOrCreateTopic(DATE_SI, "Date");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getDateTopic(String d) {
            Topic dateTopic = null;
            try {
                dateTopic = tm.getTopic(DATE_SI+d);
                if(dateTopic == null) {
                    dateTopic = tm.createTopic();
                    dateTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(DATE_SI+d));
                    dateTopic.setBaseName(d);
                    dateTopic.setDisplayName(DEFAULT_LANG, d);
                    dateTopic.addType(getDateType());
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            return dateTopic;
        }
        
        

        
        private Topic getRightsType() {
            try {
                return getOrCreateTopic(RIGHTS_SI, "Rights");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getRightsTopic(String r) {
            Topic rightsTopic = null;
            try {
                rightsTopic = tm.getTopic(RIGHTS_SI+r);
                if(rightsTopic == null) {
                    rightsTopic = tm.createTopic();
                    rightsTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(RIGHTS_SI+r));
                    rightsTopic.setBaseName(r);
                    rightsTopic.setDisplayName(DEFAULT_LANG, r);
                    rightsTopic.addType(getRightsType());
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            return rightsTopic;
        }
        
        
        
        
        private Topic getLinkType() {
            try {
                return getOrCreateTopic(LINK_SI, "Bing Link");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        
        private String makeUrl(String url) {
            if(url != null) {
                if( !url.matches("[a-zA-Z0-9]+\\:\\/\\/.*?") ) {
                    if(parent.BING_ROOT != null) {
                        if(!url.startsWith("/")) url =  "/" + url;
                        url = parent.BING_ROOT + url;
                    }
                }
            }
            return url;
        }

        
        private void createLinkStruct(Link link, Topic player, Topic role) throws TopicMapException  {
            String href = link.getHref();
            if(link != null && isValid(href)) {
                Topic linkType = getLinkType();
                Topic hrefTopic = getOrCreateTopic(makeUrl(href));
                Topic hrefType = getOrCreateTopic(LINK_HREF_SI, "Bing Link Href");
                if(isValid(link.getTitle())) {
                    hrefTopic.setBaseName(link.getTitle());
                    hrefTopic.setDisplayName(DEFAULT_LANG, link.getTitle());
                }
                Association linka = tm.createAssociation(linkType);
                linka.addPlayer(player, role);
                linka.addPlayer(hrefTopic, hrefType);
                
                if(isValid(link.getRel())) {
                    Topic relTopic = getOrCreateTopic(link.getRel());
                    Topic relType = getOrCreateTopic(LINK_REL_SI, "Bing Link Rel");
                    linka.addPlayer(relTopic, relType);
                }
                if(isValid(link.getType())) {
                    Topic hrefTypeTopic = getOrCreateTopic(LINK_TYPE_SI+"/"+link.getType(), link.getType());
                    hrefTopic.addType(hrefTypeTopic);
                }
                if(isValid(link.getHrefLang())) {
                    Topic hrefLangType = getOrCreateTopic(LINK_HREF_LANG_SI, "Bing Link Href Lang");
                    Topic hrefLangTopic = getOrCreateTopic(LINK_HREF_LANG_SI+"/"+link.getHrefLang(), link.getHrefLang());
                    Association la = tm.createAssociation(hrefLangType);
                    la.addPlayer(hrefLangTopic, hrefLangType);
                    la.addPlayer(hrefTopic, hrefType);
                }
            }
        }
        
        
        
        
        private String postProcessFeedText(String txt, String type) {
            if(txt != null && type!=null && type.length()>0) {
                if("text".equals(type)) {}
                else if("html".equals(type)) { txt = HTMLEntitiesCoder.decode(txt); }
                else if("xhtml".equals(type)) {
                    txt = txt.replaceAll("\\&lt\\;", "<");
                    txt = txt.replaceAll("\\&gt\\;", ">");
                    txt = txt.replaceAll("\\&amp\\;", "&");
                }
            }
            return txt;
        }
        
        
        
        private boolean isValid(String str) {
            if(str == null || str.trim().length() == 0) return false;
            return true;
        }
        
        
        
        
        // ---------------------------------------------------------------------
        
        
        
        public void startDocument() throws SAXException {
            feedCount = 0;
            entryCount = 0;
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            //parent.log("START" + state +" --- " + qName);
            
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(qName.equals(TAG_FEED)) {
                        try {
                            Topic feedType=getFeedType();
                            theFeed = tm.createTopic();
                            theFeedSI = TopicTools.createDefaultLocator();
                            theFeed.addSubjectIdentifier(theFeedSI);
                            theFeed.addType(feedType);
                            feedCount++;
                            
                            Topic atomClass = getOrCreateTopic(BINGSI, "Bing");
                            Topic superClass = getOrCreateTopic(XTMPSI.SUPERCLASS);
                            Topic subClass = getOrCreateTopic(XTMPSI.SUBCLASS);
                            Topic supersubClass = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS);

                            Association supersubClassAssociation = tm.createAssociation(supersubClass);
                            supersubClassAssociation.addPlayer(atomClass, superClass);
                            supersubClassAssociation.addPlayer(feedType, subClass);

                            if(MAKE_SUBCLASS_OF_WANDORA_CLASS) {
                                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                           
                                supersubClassAssociation = tm.createAssociation(supersubClass);
                                supersubClassAssociation.addPlayer(wandoraClass, superClass);
                                supersubClassAssociation.addPlayer(atomClass, subClass);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        
                        data_feed_id = "";
                        data_feed_title = "";
                        data_feed_title_type = "";
                        data_feed_updated = "";
                        data_feed_category = null;
                        data_feed_link = null;
                        data_feed_generator = "";
                        data_feed_icon = "";
                        data_feed_logo = "";
                        data_feed_rights = "";
                        data_feed_subtitle = "";
                        data_feed_subtitle_type = "";
                        state = STATE_FEED;
                    }
                    break;
                case STATE_FEED:
                    if(qName.equals(TAG_TITLE)) {
                        data_feed_title_type = atts.getValue("type");
                        data_feed_title = "";
                        state = STATE_FEED_TITLE;
                    }
                    else if(qName.equals(TAG_LINK)) {
                        String href = atts.getValue("id");
                        String rel = atts.getValue("rel");
                        String type = atts.getValue("type");
                        String hreflang = atts.getValue("hreflang");
                        String title = atts.getValue("id");
                        String length = atts.getValue("length");
                        data_feed_link = new Link(href, rel, type, hreflang, title, length);
                        state = STATE_FEED_LINK;
                    }
                    else if(qName.equals(TAG_UPDATED)) {
                        data_feed_updated = "";
                        state = STATE_FEED_UPDATED;
                    }
                    else if(qName.equals(TAG_AUTHOR)) {
                        data_feed_author_name = "";
                        data_feed_author_uri = "";
                        data_feed_author_email = "";
                        state = STATE_FEED_AUTHOR;
                    }
                    else if(qName.equals(TAG_ID)) {
                        data_feed_id = "";
                        state = STATE_FEED_ID;
                    }
                    else if(qName.equals(TAG_CATEGORY)) {
                        String term = atts.getValue("term");
                        String scheme = atts.getValue("scheme");
                        String label = atts.getValue("label");
                        data_feed_category = new Category(term, scheme, label);
                        state = STATE_FEED_CATEGORY;
                    }
                    else if(qName.equals(TAG_CONTRIBUTOR)) {
                        data_feed_contributor_name = "";
                        data_feed_contributor_uri = "";
                        data_feed_contributor_email = "";
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    else if(qName.equals(TAG_GENERATOR)) {
                        data_feed_generator = "";
                        data_feed_generator_uri = atts.getValue("uri");
                        data_feed_generator_version = atts.getValue("version");
                        state = STATE_FEED_GENERATOR;
                    }
                    else if(qName.equals(TAG_ICON)) {
                        data_feed_icon = "";
                        state = STATE_FEED_ICON;
                    }
                    else if(qName.equals(TAG_LOGO)) {
                        data_feed_logo = "";
                        state = STATE_FEED_LOGO;
                    }
                    else if(qName.equals(TAG_RIGHTS)) {
                        data_feed_rights = "";
                        data_feed_rights_type = atts.getValue("type");
                        state = STATE_FEED_RIGHTS;
                    }
                    else if(qName.equals(TAG_SUBTITLE)) {
                        data_feed_subtitle_type = atts.getValue("type");
                        data_feed_subtitle = "";
                        state = STATE_FEED_SUBTITLE;
                    }
                    else if(qName.equals(TAG_ENTRY)) {
                        try {
                            Topic entryType=getEntryType();
                            theEntry = tm.createTopic();
                            theEntrySI = TopicTools.createDefaultLocator();
                            theEntry.addSubjectIdentifier(theEntrySI);
                            theEntry.addType(entryType);
                            entryCount++;
                            parent.setProgress(entryCount);
                            
                            Topic atomClass = getOrCreateTopic(BINGSI, "Bing");
                            Topic superClass = getOrCreateTopic(XTMPSI.SUPERCLASS);
                            Topic subClass = getOrCreateTopic(XTMPSI.SUBCLASS);
                            Topic supersubClass = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS);

                            Association supersubClassAssociation = tm.createAssociation(supersubClass);
                            supersubClassAssociation.addPlayer(atomClass, superClass);
                            supersubClassAssociation.addPlayer(entryType, subClass);

                            if(MAKE_SUBCLASS_OF_WANDORA_CLASS) {
                                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                                supersubClassAssociation = tm.createAssociation(supersubClass);
                                supersubClassAssociation.addPlayer(wandoraClass, superClass);
                                supersubClassAssociation.addPlayer(atomClass, subClass);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        
                        data_entry_id = "";
                        data_entry_title = "";
                        data_entry_title_type = "";
                        
                        data_entry_updated = "";
                        data_entry_published = "";
                        
                        data_entry_author_name = "";
                        data_entry_author_uri = "";
                        data_entry_author_email = "";
                        
                        data_entry_content = "";
                        data_entry_content_type = "";
                        data_entry_content_src = "";

                        data_entry_link = null;

                        data_entry_summary = "";
                        data_entry_summary_src = "";
                        data_entry_summary_type = "";

                        data_entry_category = null;

                        data_entry_contributor_name = "";
                        data_entry_contributor_uri = "";
                        data_entry_contributor_email = "";

                        data_entry_source_id = "";
                        data_entry_source_title = "";
                        data_entry_source_updated = "";
                        data_entry_source_rights = "";
                        data_entry_source_rights_type = "";

                        data_entry_rights = "";
                        data_entry_rights_type = "";
                        
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                    
                    
                case STATE_FEED_AUTHOR:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_AUTHOR_NAME;
                        data_feed_author_name = "";
                    }
                    else if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_AUTHOR_URI;
                        data_feed_author_uri = "";
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_AUTHOR_EMAIL;
                        data_feed_author_email = "";
                    }
                    break;
                
                case STATE_FEED_CONTRIBUTOR:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_CONTRIBUTOR_NAME;
                        data_feed_contributor_name = "";
                    }
                    else if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_CONTRIBUTOR_URI;
                        data_feed_contributor_uri = "";
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_CONTRIBUTOR_EMAIL;
                        data_feed_contributor_email = "";
                    }
                    break;
                    
                    
                case STATE_FEED_ENTRY:
                    if(qName.equals(TAG_ID)) {
                        state = STATE_FEED_ENTRY_ID;
                        data_entry_id = "";
                    }
                    else if(qName.equals(TAG_TITLE)) {
                        state = STATE_FEED_ENTRY_TITLE;
                        data_entry_title = "";
                        data_entry_title_type = atts.getValue("type");
                    }
                    else if(qName.equals(TAG_UPDATED)) {
                        state = STATE_FEED_ENTRY_UPDATED;
                        data_entry_updated = "";
                    }
                    else if(qName.equals(TAG_AUTHOR)) {
                        data_entry_author_name = "";
                        data_entry_author_uri = "";
                        data_entry_author_email = "";
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    else if(qName.equals(TAG_CONTENT)) {
                        state = STATE_FEED_ENTRY_CONTENT;
                        data_entry_content = "";
                        data_entry_content_src = atts.getValue("src");
                        data_entry_content_type = atts.getValue("type");
                    }
                    else if(qName.equals(TAG_LINK)) {
                        state = STATE_FEED_ENTRY_LINK;
                        String href = atts.getValue("href");
                        String rel = atts.getValue("rel");
                        String type = atts.getValue("type");
                        String hreflang = atts.getValue("hreflang");
                        String title = atts.getValue("title");
                        String length = atts.getValue("length");
                        data_entry_link = new Link(href, rel, type, hreflang, title, length);
                    }
                    else if(qName.equals(TAG_SUMMARY)) {
                        state = STATE_FEED_ENTRY_SUMMARY;
                        data_entry_summary = "";
                        data_entry_summary_src = atts.getValue("src");
                        data_entry_summary_type = atts.getValue("type");
                    }
                    else if(qName.equals(TAG_CATEGORY)) {
                        state = STATE_FEED_ENTRY_CATEGORY;
                        String term = atts.getValue("term");
                        String scheme = atts.getValue("scheme");
                        String label = atts.getValue("label");
                        data_entry_category = new Category(term, scheme, label);
                    }
                    else if(qName.equals(TAG_CONTRIBUTOR)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                        data_entry_contributor_name = "";
                        data_entry_contributor_email = "";
                        data_entry_contributor_uri = "";
                    }
                    else if(qName.equals(TAG_PUBLISHED)) {
                        state = STATE_FEED_ENTRY_PUBLISHED;
                        data_entry_published = "";
                    }
                    else if(qName.equals(TAG_SOURCE)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                        data_entry_source_id = "";
                        data_entry_source_title = "";
                        data_entry_source_updated = "";
                        data_entry_source_rights = "";
                        data_entry_source_rights_type = "";
                    }
                    else if(qName.equals(TAG_RIGHTS)) {
                        state = STATE_FEED_ENTRY_RIGHTS;
                        data_entry_rights = "";
                        data_entry_rights_type = atts.getValue("type");
                    }
                    break;
                case STATE_FEED_ENTRY_AUTHOR:
                    if(qName.equals(TAG_NAME)) {
                        data_entry_author_name = "";
                        state = STATE_FEED_ENTRY_AUTHOR_NAME;
                    }
                    else if(qName.equals(TAG_URI)) {
                        data_entry_author_uri = "";
                        state = STATE_FEED_ENTRY_AUTHOR_URI;
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        data_entry_author_email = "";
                        state = STATE_FEED_ENTRY_AUTHOR_EMAIL;
                    }
                    break;
                    
                case STATE_FEED_ENTRY_CONTRIBUTOR:
                    if(qName.equals(TAG_NAME)) {
                        data_entry_contributor_name = "";
                        state = STATE_FEED_ENTRY_CONTRIBUTOR_NAME;
                    }
                    else if(qName.equals(TAG_URI)) {
                        data_entry_contributor_uri = "";
                        state = STATE_FEED_ENTRY_CONTRIBUTOR_URI;
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        data_entry_contributor_email = "";
                        state = STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL;
                    }
                    break;
                    
                case STATE_FEED_ENTRY_SOURCE:
                    if(qName.equals(TAG_ID)) {
                        data_entry_source_id = "";
                        state = STATE_FEED_ENTRY_SOURCE_ID;
                    }
                    else if(qName.equals(TAG_TITLE)) {
                        data_entry_source_title = "";
                        state = STATE_FEED_ENTRY_SOURCE_TITLE;
                    }
                    else if(qName.equals(TAG_UPDATED)) {
                        data_entry_source_updated = "";
                        state = STATE_FEED_ENTRY_SOURCE_UPDATED;
                    }
                    else if(qName.equals(TAG_RIGHTS)) {
                        data_entry_source_rights_type = atts.getValue("type");
                        data_entry_source_rights = "";
                        state = STATE_FEED_ENTRY_SOURCE_RIGHTS;
                    }
                    break;
            }
        }
        
        
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //parent.log("  END" + state +" --- " + qName);
            
            switch(state) {
                case STATE_FEED: {
                    if(qName.equals(TAG_FEED)) {
                        state = STATE_START;
                    }
                    break;
                }
                case STATE_FEED_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        if(theFeed != null && isValid(data_feed_title)) {
                            try {
                                data_feed_title = postProcessFeedText(data_feed_title, data_feed_title_type);
                                theFeed.setBaseName(data_feed_title + " (Bing feed)");
                                theFeed.setDisplayName(DEFAULT_LANG, data_feed_title);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_LINK: {
                    if(qName.equals(TAG_LINK)) {
                        if(theFeed != null && data_feed_link != null) {
                            try {
                                createLinkStruct(data_feed_link, theFeed, getFeedType());
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_UPDATED: {
                    if(qName.equals(TAG_UPDATED)) {
                        if(theFeed != null && isValid(data_feed_updated)) {
                            try {
                                Topic updatedType = getOrCreateTopic(FEED_UPDATED_SI,"Bing Feed Updated");
                                Topic dateTopic = getDateTopic(data_feed_updated);
                                Association a = tm.createAssociation(updatedType);
                                a.addPlayer(theFeed, getFeedType());
                                a.addPlayer(dateTopic, updatedType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR: {
                    if(qName.equals(TAG_AUTHOR)) {
                        if(theFeed != null && isValid(data_feed_author_name)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic authorType = getOrCreateTopic(FEED_AUTHOR_SI,"Bing Feed Author");
                                String authorSI = !isValid(data_feed_author_uri) ? FEED_AUTHOR_SI + "/" + data_feed_author_name : data_feed_author_uri;
                                Topic theAuthor = getOrCreateTopic(authorSI, data_feed_author_name);
                                theAuthor.addType(authorType);
                                Association feedAuthor = tm.createAssociation(authorType);
                                feedAuthor.addPlayer(theFeed, feedType);
                                feedAuthor.addPlayer(theAuthor, authorType);
                                
                                if(isValid(data_feed_author_email)) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theAuthor, emailType, DEFAULT_LANG, data_feed_author_email);
                                }
                                
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ID: {
                    if(qName.equals(TAG_ID)) {
                        if(theFeed != null && isValid(data_feed_id)) {
                            try {                              
                                if(data_feed_id.startsWith("http://")) {
                                    theFeed.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_feed_id));
                                    theFeed.removeSubjectIdentifier(theFeedSI);
                                    theFeed = tm.getTopic(data_feed_id);
                                }
                                else {
                                    Topic idType = getOrCreateTopic(FEED_ID_SI,"Bing Feed Id");
                                    parent.setData(theFeed, idType, DEFAULT_LANG, data_feed_id);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_CATEGORY: {
                    if(qName.equals(TAG_CATEGORY)) {
                        if(theFeed != null && data_feed_category != null) {
                            try {
                                Topic feedType = getFeedType();
                                Topic categoryType = getOrCreateTopic(FEED_CATEGORY_SI,"Bing Feed Category");
                                String term = data_feed_category.getTerm();
                                String label = data_feed_category.getLabel();
                                String scheme = data_feed_category.getScheme();
                                String categorySI = FEED_CATEGORY_SI + "/" + term;
                                String categoryBasename = term;
                                
                                if(isValid(term) && isValid(scheme)) categorySI = scheme + term;
                                if(isValid(label)) categoryBasename = label;
                                
                                Topic theCategory = getOrCreateTopic(categorySI, categoryBasename);
                                theCategory.addType(categoryType);
                                Association feedCategory = tm.createAssociation(categoryType);
                                feedCategory.addPlayer(theFeed, feedType);
                                feedCategory.addPlayer(theCategory, categoryType);
                                
                                if(isValid(scheme)) {
                                    Topic schemeType = getOrCreateTopic(SCHEME_SI,"Scheme");
                                    parent.setData(theCategory, schemeType, DEFAULT_LANG, scheme);
                                }
                                if(isValid(label)) {
                                    theCategory.setDisplayName(DEFAULT_LANG, label);
                                }
                                
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR: {
                    if(qName.equals(TAG_CONTRIBUTOR)) {
                        if(theFeed != null && isValid(data_feed_contributor_name)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic contributorType = getOrCreateTopic(FEED_CONTRIBUTOR_SI,"Bing Feed Contributor");
                                String contributorSI = !isValid(data_feed_contributor_uri) ? FEED_CONTRIBUTOR_SI + "/" + data_feed_contributor_name : data_feed_contributor_uri;
                                Topic theContributor = getOrCreateTopic(contributorSI, data_feed_contributor_name);
                                theContributor.addType(contributorType);
                                Association feedContributor = tm.createAssociation(contributorType);
                                feedContributor.addPlayer(theFeed, feedType);
                                feedContributor.addPlayer(theContributor, contributorType);
                                
                                if(isValid(data_feed_contributor_email)) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theContributor, emailType, DEFAULT_LANG, data_feed_contributor_email);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_GENERATOR: {
                    if(qName.equals(TAG_GENERATOR)) {
                        if(theFeed != null && isValid(data_feed_generator)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic generatorType = getOrCreateTopic(FEED_GENERATOR_SI,"Bing Feed Generator");
                                String generatorSI = FEED_GENERATOR_SI + "/" + data_feed_generator;
                                Topic theGenerator = getOrCreateTopic(generatorSI, data_feed_generator);
                                theGenerator.addType(generatorType);
                                Association feedGenerator = tm.createAssociation(generatorType);
                                feedGenerator.addPlayer(theFeed, feedType);
                                feedGenerator.addPlayer(theGenerator, generatorType);
                                
                                if(isValid(data_feed_generator_uri)) {
                                    Topic uriType = getOrCreateTopic(FEED_GENERATOR_URI_SI,"Bing Feed Generator URI");
                                    parent.setData(theGenerator, uriType, DEFAULT_LANG, data_feed_generator_uri);
                                }
                                if(isValid(data_feed_generator_version)) {
                                    Topic versionType = getOrCreateTopic(FEED_GENERATOR_VERSION_SI,"Bing Feed Generator Version");
                                    parent.setData(theGenerator, versionType, DEFAULT_LANG, data_feed_generator_version);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_ICON: {
                    if(qName.equals(TAG_ICON)) {
                        if(theFeed != null && isValid(data_feed_icon)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic iconType = getOrCreateTopic(FEED_ICON_SI,"Bing Feed Icon");
                                String iconSI = FEED_ICON_SI + "/" + data_feed_icon;
                                if(data_feed_icon.startsWith("http://")) {
                                    iconSI = data_feed_icon;
                                }
                                else if(data_feed_icon.startsWith("/") && data_feed_id.startsWith("http://")) {
                                    String prefix = data_feed_id;
                                    if(prefix.endsWith("/")) prefix = prefix.substring(0, prefix.length()-1); 
                                    iconSI = prefix + data_feed_icon;
                                }
                                Topic theIcon = getOrCreateTopic(iconSI, null);
                                theIcon.setSubjectLocator(new org.wandora.topicmap.Locator(iconSI));
                                theIcon.addType(iconType);
                                Association feedIcon = tm.createAssociation(iconType);
                                feedIcon.addPlayer(theFeed, feedType);
                                feedIcon.addPlayer(theIcon, iconType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_LOGO: {
                    if(qName.equals(TAG_LOGO)) {
                        if(theFeed != null && isValid(data_feed_logo)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic logoType = getOrCreateTopic(FEED_LOGO_SI,"Bing Feed Logo");
                                String logoSI = FEED_LOGO_SI + "/" + data_feed_logo;
                                
                                if(data_feed_icon.startsWith("http://")) {
                                    logoSI = data_feed_logo;
                                }
                                else if(data_feed_logo.startsWith("/") && data_feed_id.startsWith("http://")) {
                                    String prefix = data_feed_id;
                                    if(prefix.endsWith("/")) prefix = prefix.substring(0, prefix.length()-1); 
                                    logoSI = prefix + data_feed_logo;
                                }
                                
                                Topic theLogo = getOrCreateTopic(logoSI, null);
                                theLogo.setSubjectLocator(new org.wandora.topicmap.Locator(logoSI));
                                theLogo.addType(logoType);
                                Association feedLogo = tm.createAssociation(logoType);
                                feedLogo.addPlayer(theFeed, feedType);
                                feedLogo.addPlayer(theLogo, logoType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_RIGHTS: {
                    if(qName.equals(TAG_RIGHTS)) {
                        if(theFeed != null && isValid(data_feed_rights)) {
                            try {
                                data_feed_rights = postProcessFeedText(data_feed_rights, data_feed_rights_type);
                                Topic rightsType = getRightsType();
                                Topic rightsTopic = getRightsTopic(data_feed_rights);
                                
                                if(rightsType != null && rightsTopic != null) {
                                    Association a = tm.createAssociation(rightsType);
                                    a.addPlayer(theFeed, getFeedType());
                                    a.addPlayer(rightsTopic, rightsType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_SUBTITLE: {
                    if(qName.equals(TAG_SUBTITLE)) {
                        if(theFeed != null && isValid(data_feed_subtitle)) {
                            try {
                                data_feed_subtitle = this.postProcessFeedText(data_feed_subtitle, data_feed_subtitle_type);
                                Topic subtitleType = getOrCreateTopic(FEED_SUBTITLE_SI,"Bing Feed Subtitle");
                                parent.setData(theFeed, subtitleType, DEFAULT_LANG, data_feed_subtitle);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                
                
                // **************************************************
                // ********************* ENTRY **********************
                // **************************************************
                
                
                
                
                case STATE_FEED_ENTRY: {
                    if(qName.equals(TAG_ENTRY)) {
                        if(theEntry != null && theFeed != null) {
                            try {
                                Topic entryType = getEntryType();
                                Topic feedType = getFeedType();
                                Association a = tm.createAssociation(entryType);
                                a.addPlayer(theEntry, entryType);
                                a.addPlayer(theFeed, feedType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_ID: {
                    if(qName.equals(TAG_ID)) {
                        if(theEntry != null && isValid(data_entry_id)) {
                            try {                                
                                if(data_entry_id.startsWith("http://")) {
                                    theEntry.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_entry_id));
                                    theEntry.removeSubjectIdentifier(theEntrySI);
                                    theEntry = tm.getTopic(data_entry_id);
                                }
                                else {
                                    Topic idType = getOrCreateTopic(FEED_ENTRY_ID_SI,"Bing Entry Id");
                                    parent.setData(theEntry, idType, DEFAULT_LANG, data_entry_id);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        if(theEntry != null && isValid(data_entry_title)) {
                            try {
                                data_entry_title = postProcessFeedText(data_entry_title, data_entry_title_type);
                                theEntry.setBaseName(data_entry_title + " (Bing entry)");
                                theEntry.setDisplayName(DEFAULT_LANG, data_entry_title);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_UPDATED: {
                    if(qName.equals(TAG_UPDATED)) {
                        if(theEntry != null && isValid(data_entry_updated)) {
                            try {
                                Topic updatedType = getOrCreateTopic(FEED_ENTRY_UPDATED_SI,"Bing Entry Updated");
                                Topic dateTopic = getDateTopic(data_entry_updated);
                                if(dateTopic != null && updatedType != null) {
                                    Association a = tm.createAssociation(updatedType);
                                    a.addPlayer(theEntry, getEntryType());
                                    a.addPlayer(dateTopic, updatedType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR: {
                    if(qName.equals(TAG_AUTHOR)) {
                        if(theEntry != null && isValid(data_entry_author_name)) {
                            try {
                                Topic entryType = getEntryType();
                                Topic authorType = getOrCreateTopic(FEED_ENTRY_AUTHOR_SI,"Bing Entry Author");
                                String authorSI = data_entry_author_uri == null || data_entry_author_uri.length() == 0 ? FEED_ENTRY_AUTHOR_SI + "/" + data_entry_author_name : data_entry_author_uri;
                                Topic theAuthor = getOrCreateTopic(authorSI, data_entry_author_name);
                                theAuthor.addType(authorType);
                                Association entryAuthor = tm.createAssociation(authorType);
                                entryAuthor.addPlayer(theEntry, entryType);
                                entryAuthor.addPlayer(theAuthor, authorType);
                                
                                if(data_entry_author_email != null && data_entry_author_email.length() > 0) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theAuthor, emailType, DEFAULT_LANG, data_entry_author_email);
                                }
                                
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTENT: {
                    if(qName.equals(TAG_CONTENT)) {
                        if(theEntry != null && (isValid(data_entry_content) || isValid(data_entry_content_src ))) {
                            try {
                                if(!isValid(data_entry_content)) {
                                    data_entry_content = IObox.doUrl(new URL(data_entry_content_src));
                                }
                                data_entry_content = this.postProcessFeedText(data_entry_content, data_entry_content_type);
                                Topic contentType = getOrCreateTopic(FEED_ENTRY_CONTENT_SI,"Bing Entry Content");
                                parent.setData(theEntry, contentType, DEFAULT_LANG, data_entry_content);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_LINK: {
                    if(qName.equals(TAG_LINK)) {
                        if(theEntry != null && data_entry_link != null) {
                            try {
                                createLinkStruct(data_entry_link, theEntry, getEntryType());
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SUMMARY: {
                    if(qName.equals(TAG_SUMMARY)) {
                        if(theEntry != null && (isValid(data_entry_summary) || isValid(data_entry_summary_src))) {
                            try {
                                if(!isValid(data_entry_summary)) {
                                    data_entry_summary = IObox.doUrl(new URL(data_entry_summary_src));
                                }
                                data_entry_summary = this.postProcessFeedText(data_entry_summary, data_entry_summary_type);
                                Topic summaryType = getOrCreateTopic(FEED_ENTRY_SUMMARY_SI,"Bing Entry Summary");
                                parent.setData(theEntry, summaryType, DEFAULT_LANG, data_entry_summary);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CATEGORY: {
                    if(qName.equals(TAG_CATEGORY)) {
                        if(theFeed != null && data_entry_category != null) {
                            try {
                                Topic entryType = getEntryType();
                                Topic categoryType = getOrCreateTopic(FEED_CATEGORY_SI,"Bing Entry Category");
                                String term = data_entry_category.getTerm();
                                String label = data_entry_category.getLabel();
                                String scheme = data_entry_category.getScheme();
                                String categorySI = FEED_ENTRY_CATEGORY_SI + "/" + term;
                                String categoryBasename = term;
                                if(isValid(scheme) && isValid(term)) categorySI = scheme+term;
                                if(isValid(label)) categoryBasename = label;
                                
                                Topic theCategory = getOrCreateTopic(categorySI, categoryBasename);
                                theCategory.addType(categoryType);
                                Association entryCategory = tm.createAssociation(categoryType);
                                entryCategory.addPlayer(theEntry, entryType);
                                entryCategory.addPlayer(theCategory, categoryType);
                                
                                if(scheme != null && scheme.length() > 0) {
                                    Topic schemeType = getOrCreateTopic(SCHEME_SI,"Scheme");
                                    parent.setData(theCategory, schemeType, DEFAULT_LANG, scheme);
                                }
                                if(label != null && label.length() > 0) {
                                    theCategory.setDisplayName(DEFAULT_LANG, label);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR: {
                    if(qName.equals(TAG_CONTRIBUTOR)) {
                        if(theEntry != null && isValid(data_entry_contributor_name)) {
                            try {
                                Topic entryType = getEntryType();
                                Topic contributorType = getOrCreateTopic(FEED_ENTRY_CONTRIBUTOR_SI,"Bing Entry Contributor");
                                String contributorSI = data_entry_contributor_uri == null || data_entry_contributor_uri.length() == 0 ? FEED_CONTRIBUTOR_SI + "/" + data_entry_contributor_name : data_entry_contributor_uri;
                                Topic theContributor = getOrCreateTopic(contributorSI, data_entry_contributor_name);
                                theContributor.addType(contributorType);
                                Association feedContributor = tm.createAssociation(contributorType);
                                feedContributor.addPlayer(theEntry, entryType);
                                feedContributor.addPlayer(theContributor, contributorType);
                                
                                if(data_entry_contributor_email != null && data_entry_contributor_email.length() > 0) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theContributor, emailType, DEFAULT_LANG, data_entry_contributor_email);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_PUBLISHED: {
                    if(qName.equals(TAG_PUBLISHED)) {
                        if(theEntry != null && isValid(data_entry_published)) {
                            try {
                                Topic publishedType = getOrCreateTopic(FEED_ENTRY_PUBLISHED_SI,"Bing Entry Published");
                                Topic dateTopic = getDateTopic(data_entry_published);
                                if(dateTopic != null && publishedType != null) {
                                    Association a = tm.createAssociation(publishedType);
                                    a.addPlayer(theEntry, getEntryType());
                                    a.addPlayer(dateTopic, publishedType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE: {
                    if(qName.equals(TAG_SOURCE)) {
                        if(theEntry != null && isValid(data_entry_source_id)) {
                            try {
                                Topic sourceType = getOrCreateTopic(FEED_ENTRY_SOURCE_SI,"Bing Entry Source");
                                Topic theSource = tm.createTopic();
                                Topic theSourceRights = null;
                                Topic theSourceUpdated = null;
                                if(data_entry_source_id.startsWith("http://")) {
                                    theSource.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_entry_source_id));
                                    theSource.setSubjectLocator(new org.wandora.topicmap.Locator(data_entry_source_id));
                                }
                                else {
                                    theSource.addSubjectIdentifier(new org.wandora.topicmap.Locator(FEED_ENTRY_SOURCE_SI+"/"+data_entry_source_id));
                                }
                                if(isValid(data_entry_source_title)) {
                                    theSource.setBaseName(data_entry_source_title);
                                }
                                theSource.addType(sourceType);
                                
                                Association a = tm.createAssociation(sourceType);
                                a.addPlayer(theEntry, getEntryType());
                                a.addPlayer(theSource, sourceType);
                                
                                if(isValid(data_entry_source_rights)) {
                                    Topic rightsType = getRightsType();
                                    theSourceRights = getRightsTopic(postProcessFeedText(data_entry_source_rights, data_entry_source_rights_type));
                                    a.addPlayer(theSourceRights, rightsType);
                                }
                                if(isValid(data_entry_source_updated)) {
                                    Topic dateType = getDateType();
                                    theSourceUpdated = getDateTopic(data_entry_source_updated);
                                    a.addPlayer(theSourceUpdated, dateType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_ID: {
                    if(qName.equals(TAG_ID)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_UPDATED: {
                    if(qName.equals(TAG_UPDATED)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_RIGHTS: {
                    if(qName.equals(TAG_RIGHTS)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_RIGHTS: {
                    if(qName.equals(TAG_RIGHTS)) {
                        if(theEntry != null && isValid(data_entry_rights)) {
                            try {
                                data_entry_rights = postProcessFeedText(data_entry_rights, data_entry_rights_type);
                                Topic rightsType = getRightsType();
                                Topic rightsTopic = getRightsTopic(data_entry_rights);
                                if(rightsType != null && rightsTopic != null) {
                                    Association a = tm.createAssociation(rightsType);
                                    a.addPlayer(theEntry, getEntryType());
                                    a.addPlayer(rightsTopic, rightsType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
            }
        }
        
        
        
        
        
        
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_FEED_ID:
                    data_feed_id+=new String(ch,start,length);
                    break;
                case STATE_FEED_TITLE:
                    data_feed_title+=new String(ch,start,length);
                    break;
                case STATE_FEED_UPDATED:
                    data_feed_updated+=new String(ch,start,length);
                    break;
                case STATE_FEED_AUTHOR_NAME:
                    data_feed_author_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_AUTHOR_URI:
                    data_feed_author_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_AUTHOR_EMAIL:
                    data_feed_author_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_CONTRIBUTOR_NAME:
                    data_feed_contributor_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_CONTRIBUTOR_URI:
                    data_feed_contributor_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_CONTRIBUTOR_EMAIL:
                    data_feed_contributor_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_GENERATOR:
                    data_feed_generator+=new String(ch,start,length);
                    break;
                case STATE_FEED_ICON:
                    data_feed_icon+=new String(ch,start,length);
                    break; 
                case STATE_FEED_LOGO:
                    data_feed_logo+=new String(ch,start,length);
                    break; 
                case STATE_FEED_RIGHTS:
                    data_feed_rights+=new String(ch,start,length);
                    break;
                case STATE_FEED_SUBTITLE:
                    data_feed_subtitle+=new String(ch,start,length);
                    break;
                    
                // *********************** ENTRY *************************
                
                case STATE_FEED_ENTRY_ID:
                    data_entry_id+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_TITLE:
                    data_entry_title+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_UPDATED:
                    data_entry_updated+=new String(ch,start,length);
                    break; 
                case STATE_FEED_ENTRY_AUTHOR_NAME:
                    data_entry_author_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_AUTHOR_URI:
                    data_entry_author_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_AUTHOR_EMAIL:
                    data_entry_author_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTENT:
                    data_entry_content+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SUMMARY:
                    data_entry_summary+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTRIBUTOR_NAME:
                    data_entry_contributor_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTRIBUTOR_URI:
                    data_entry_contributor_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL:
                    data_entry_contributor_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_PUBLISHED:
                    data_entry_published+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_ID:
                    data_entry_source_id+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_TITLE:
                    data_entry_source_title+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_UPDATED:
                    data_entry_source_updated+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_RIGHTS:
                    data_entry_source_rights+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_RIGHTS:
                    data_entry_rights+=new String(ch,start,length);
                    break;
            }
        }
        
        public void warning(SAXParseException exception) throws SAXException {
            parent.log("Warning while parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
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
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------- HELPER CLASSES ----
    // -------------------------------------------------------------------------
    
    
    private class Content {
        private String text;
        private String src;
        private String type;
        
        public Content(String t, String s, String type) {
            this.text = t;
            this.src = s;
            this.type = type;
        }
        
        public String getContent() {
            return text;
        }
        public String getType() {
            return type;
        }
        public String getSrc() {
            return src;
        }
    }
    
    
    private class Category {
        private String term;
        private String scheme;
        private String label;
        
        public Category(String t, String s, String l) {
            this.term = t;
            this.scheme = s;
            this.label = l;
        }
        
        public String getTerm() {
            return term;
        }
        public String getScheme() {
            return scheme;
        }
        public String getLabel() {
            return label;
        }
    }
    
    
    private class Text {
        private String text;
        private String type;
        
        public Text(String t, String type) {
            this.text = t;
            this.type = type;
        }
        
        public String getText() {
            return text;
        }
        public String getType() {
            return type;
        }
    }
    
    
    private class Person {
        private String name;
        private String uri;
        private String email;
        
        public Person(String n, String u, String e) {
            this.name = n;
            this.uri = u;
            this.email = e;
        }
        
        public String getName() {
            return name;
        }
        public String getUri() {
            return uri;
        }
        public String getEmail() {
            return email;
        }
    }
    
    
    private class Link {
        private String href;
        private String rel;
        private String type;
        private String hreflang;
        private String title;
        private String length;
        
        public Link(String h, String r, String ty, String hl, String ti, String len) {
            this.href = h;
            this.rel = r;
            this.type = ty;
            this.hreflang = hl;
            this.title = ti;
            this.length = len;
        }
        
        public String getHref() {
            return href;
        }
        public String getRel() {
            return rel;
        }
        public String getType() {
            return type;
        }
        public String getHrefLang() {
            return hreflang;
        }
        public String getTitle() {
            return title;
        }
        public String getLength() {
            return length;
        }
    }
    
}
