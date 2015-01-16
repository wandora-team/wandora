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
 * TagTopTagsExtractor.java
 *
 * Created on 17. toukokuuta 2007, 14:00
 *
 */

package org.wandora.application.tools.extractors.audioscrobbler;


import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;

import org.wandora.application.gui.*;
import javax.swing.*;


/**
 *
 * @author akivela
 */
public class TagTopTagsExtractor extends AbstractAudioScrobblerExtractor {
    

    
    
    
    /** Creates a new instance of TagTopTagsExtractor */
    public TagTopTagsExtractor() {
    }

    
    @Override
    public String getName() {
        return "Audioscrobbler Tags: Top Tags extractor";
    }

    
    @Override
    public String getDescription(){
        return "Extractor reads the Top Tags XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/tag/toptags.xml for an example of Top Tags XML feed.";
    }
    
    
    

    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        TagTopTagsParser parserHandler = new TagTopTagsParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " top tags found!");
        return true;
    }
    

    private static class TagTopTagsParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public TagTopTagsParser(TopicMap tm, TagTopTagsExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private TagTopTagsExtractor parent;
        
        public static final String TAG_TOPTAGS="toptags";
        public static final String TAG_TAG="tag";
              
       
        private static final int STATE_START=0;
        private static final int STATE_TOPTAGS=1;
        private static final int STATE_TAG=2;
               
        private int state=STATE_START;
        
       
        private String data_tag_name = "";
        private String data_tag_count = "";
        private String data_tag_url = "";
        
        private Topic theTag;
        
        
        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START: {
                    if(qName.equals(TAG_TOPTAGS)) {
                        state = STATE_TOPTAGS;
                    }
                    //parent.log("start");
                    break;
                }
                case STATE_TOPTAGS: {
                    if(qName.equals(TAG_TAG)) {
                        state = STATE_TAG;
                        data_tag_name = atts.getValue("name");
                        data_tag_count = atts.getValue("count");
                        data_tag_url = atts.getValue("url");
                        if(data_tag_name != null && data_tag_name.length() > 0) {
                            try {
                                Topic tagTopic=getTagTopic(tm, data_tag_name, data_tag_url);
                                if(CONVERT_COUNTS && data_tag_count != null && data_tag_count.length() > 0) {
                                    Topic tagType=getTagTypeTopic(tm);
                                    Topic countType=getCountTypeTopic(tm);
                                    if(OCCURRENCE_COUNTS) {
                                        parent.setData(tagTopic, countType, LANG, data_tag_count);
                                    }
                                    else {
                                        Topic countTopic=getCountTopic(tm, data_tag_count);
                                        Association a = tm.createAssociation(countType);
                                        a.addPlayer(countTopic, countType);
                                        a.addPlayer(tagTopic, tagType);
                                    }
                                }
                                progress++;
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_TAG: {
                    break;
                }
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_TAG: {
                    state=STATE_TOPTAGS;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            // NOTHING HERE...
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
