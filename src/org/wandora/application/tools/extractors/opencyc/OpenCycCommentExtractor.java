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
 * 
 * OpenCycCommentExtractor.java
 */



package org.wandora.application.tools.extractors.opencyc;

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
public class OpenCycCommentExtractor extends AbstractOpenCycExtractor {
    
    /** Creates a new instance of OpenCycCommentExtractor */
    public OpenCycCommentExtractor() {
    }
    @Override
    public String getName() {
        return "OpenCyc comment extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the constant comment XML feed from OpenCyc's web api and converts the XML feed to a topic map. "+
               "See http://65.99.218.242:8080/RESTfulCyc/Constant/Food/comment for an example of such XML feed.";
    }


    
    
    public String getMasterTerm(String u) {
        try {
            if(u.endsWith("/comment")) {
                u = u.substring(0, u.length()-8);
                int i = u.lastIndexOf('/');
                if(i > 0) {
                    u = u.substring(i+1);
                    return u;
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return null;
    }
    



    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        CycCommentParser parserHandler = new CycCommentParser(getMasterSubject(), topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " Cyc constant comments found!");
        return true;
    }
    

    
    
    private static class CycCommentParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        String masterTerm = null;
        
        
        public CycCommentParser(String term, TopicMap tm, AbstractOpenCycExtractor parent){
            this.masterTerm = term;
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private AbstractOpenCycExtractor parent;
        
        public static final String TAG_CYCLIFY="cyclify";
        public static final String TAG_CONSTANT="constant";
        public static final String TAG_COMMENT="comment";

        private static final int STATE_START=0;
        private static final int STATE_CYCLIFY=2;
        private static final int STATE_CYCLIFY_CONSTANT=4;
        private static final int STATE_CYCLIFY_CONSTANT_COMMENT=5;

               
        private int state=STATE_START;
        
        
        private String data_comment;
        private String data_guid;
        
        
        private Topic theTopic;

        
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
                    if(qName.equals(TAG_CYCLIFY)) {
                        state = STATE_CYCLIFY;
                        data_guid = null;
                    }
                    break;
                case STATE_CYCLIFY:
                    if(qName.equals(TAG_CONSTANT)) {
                        state = STATE_CYCLIFY_CONSTANT;
                        data_guid = atts.getValue("guid");
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT:
                    if(qName.equals(TAG_COMMENT)) {
                        state = STATE_CYCLIFY_CONSTANT_COMMENT;
                        data_comment = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_CYCLIFY_CONSTANT_COMMENT: {
                    if(qName.equals(TAG_COMMENT)) {
                        state=STATE_CYCLIFY_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT: {
                    if(qName.equals(TAG_CONSTANT)) {
                        if(data_comment != null && data_comment.length() > 0) {
                            try {
                                parent.setProgress(progress++);
                                theTopic = getTermTopic(data_guid, masterTerm, tm);
                                Topic occurrenceType = getCommentTypeTopic(tm);
                                parent.setData(theTopic, occurrenceType, LANG, data_comment);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state=STATE_CYCLIFY;
                    }
                    break;
                }
                case STATE_CYCLIFY: {
                    if(qName.equals(TAG_CYCLIFY)) {
                        state=STATE_START;
                    }
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_CYCLIFY_CONSTANT_COMMENT:
                    data_comment+=new String(ch,start,length);
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
