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
 * OpenCycDenotationsExtractor.java
 */


package org.wandora.application.tools.extractors.opencyc;

import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;



/**
 *
 * @author akivela
 */
public class OpenCycDenotationsExtractor extends AbstractOpenCycExtractor {
    
    /** Creates a new instance of OpenCycDenotationsExtractor */
    public OpenCycDenotationsExtractor() {
    }
    @Override
    public String getName() {
        return "OpenCyc denotations extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the denotations XML feed from OpenCyc's web api and converts the XML feed to a topic map. "+
               "See http://65.99.218.242:8080/RESTfulCyc/denotation/president for an example of such XML feed.";
    }


    public String getMasterTerm(String u) {
        return null;
    }
    



    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        CycDenotationsParser parserHandler = new CycDenotationsParser(getMasterSubject(), topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " Cyc denotations found!");
        return true;
    }
    

    
    
    private static class CycDenotationsParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        String masterTerm = null;
        
        
        public CycDenotationsParser(String term, TopicMap tm, AbstractOpenCycExtractor parent){
            this.masterTerm = term;
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private AbstractOpenCycExtractor parent;
        
        public static final String TAG_CYCLIFY="cyclify";
        public static final String TAG_DENOTATIONS="denotations";
        public static final String TAG_LIST="list";
        public static final String TAG_CONSTANT="constant";
        public static final String TAG_GUID="guid";
        public static final String TAG_NAME="name";
        public static final String TAG_DISPLAYNAME="displayname";

        private static final int STATE_START=0;
        private static final int STATE_CYCLIFY=2;
        private static final int STATE_CYCLIFY_DENOTATIONS=4;
        private static final int STATE_CYCLIFY_DENOTATIONS_CONSTANT=5;
        private static final int STATE_CYCLIFY_DENOTATIONS_CONSTANT_GUID=6;
        private static final int STATE_CYCLIFY_DENOTATIONS_CONSTANT_NAME=7;
        private static final int STATE_CYCLIFY_DENOTATIONS_CONSTANT_DISPLAYNAME=8;
               
        private int state=STATE_START;
        
        
        private String data_denotations_string = "";
        private String data_guid = "";
        private String data_name = "";
        private String data_displayname = "";
        
        
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
                    if(qName.equals(TAG_DENOTATIONS)) {
                        state = STATE_CYCLIFY_DENOTATIONS;
                        data_denotations_string = atts.getValue("string");
                    }
                    break;
                case STATE_CYCLIFY_DENOTATIONS:
                    if(qName.equals(TAG_LIST)) {
                    }
                    else if(qName.equals(TAG_CONSTANT)) {
                        state = STATE_CYCLIFY_DENOTATIONS_CONSTANT;
                    }
                    break;
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT:
                    if(qName.equals(TAG_GUID)) {
                        state = STATE_CYCLIFY_DENOTATIONS_CONSTANT_GUID;
                        data_guid = "";
                    }
                    else if(qName.equals(TAG_NAME)) {
                        state = STATE_CYCLIFY_DENOTATIONS_CONSTANT_NAME;
                        data_name = "";
                    }
                    else if(qName.equals(TAG_DISPLAYNAME)) {
                        state = STATE_CYCLIFY_DENOTATIONS_CONSTANT_DISPLAYNAME;
                        data_displayname = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT_DISPLAYNAME: {
                    if(qName.equals(TAG_DISPLAYNAME)) {
                        state=STATE_CYCLIFY_DENOTATIONS_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state=STATE_CYCLIFY_DENOTATIONS_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT_GUID: {
                    if(qName.equals(TAG_GUID)) {
                        state=STATE_CYCLIFY_DENOTATIONS_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT: {
                    if(qName.equals(TAG_CONSTANT)) {
                        if(data_displayname != null && data_displayname.length() > 0) {
                            try {
                                parent.setProgress(progress++);
                                theTopic = getTermTopic(data_guid, data_name, tm);
                                theTopic.setDisplayName(LANG, data_displayname);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state=STATE_CYCLIFY_DENOTATIONS;
                    }
                    break;
                }
                case STATE_CYCLIFY_DENOTATIONS: {
                    if(qName.equals(TAG_DENOTATIONS)) {
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
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT_DISPLAYNAME:
                    data_displayname+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT_NAME:
                    data_name+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_DENOTATIONS_CONSTANT_GUID:
                    data_guid+=new String(ch,start,length);
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