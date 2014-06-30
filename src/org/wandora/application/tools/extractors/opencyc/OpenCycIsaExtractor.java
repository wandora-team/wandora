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
 * OpenCycIsaExtractor.java
 * 
 */

package org.wandora.application.tools.extractors.opencyc;


import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;


/**
 *
 * @author akivela
 */
public class OpenCycIsaExtractor extends AbstractOpenCycExtractor {
    
    /** Creates a new instance of OpenCycIsaExtractor */
    public OpenCycIsaExtractor() {
    }
    
    
    
    @Override
    public String getName() {
        return "OpenCyc isa extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the isa XML feed from OpenCyc's web api and converts the XML feed to a topic map. "+
               "See http://65.99.218.242:8080/RESTfulCyc/Constant/EiffelTower/isa for an example of such XML feed.";
    }


    public String getMasterTerm(String u) {
        try {
            if(u.endsWith("/isa")) {
                u = u.substring(0, u.length()-"/isa".length());
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
        CycIsaParser parserHandler = new CycIsaParser(getMasterSubject(), topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " Cyc isa's found!");
        return true;
    }
    

    
    
    private static class CycIsaParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        String masterTerm = null;
        
        
        public CycIsaParser(String term, TopicMap tm, AbstractOpenCycExtractor parent){
            this.masterTerm = term;
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private AbstractOpenCycExtractor parent;
        
        public static final String TAG_CYCLIFY="cyclify";
        public static final String TAG_CONSTANT="constant";
        public static final String TAG_LIST="list";
        public static final String TAG_ISA="isa";
        public static final String TAG_GUID="guid";
        public static final String TAG_NAME="name";
        public static final String TAG_DISPLAYNAME="displayname";

        private static final int STATE_START=0;
        private static final int STATE_CYCLIFY=2;
        private static final int STATE_CYCLIFY_CONSTANT=3;
        private static final int STATE_CYCLIFY_CONSTANT_ISA=4;
        private static final int STATE_CYCLIFY_CONSTANT_ISA_LIST=5;
        private static final int STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT=6;
        private static final int STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_GUID=7;
        private static final int STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_NAME=8;
        private static final int STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_DISPLAYNAME=9;

        
        private int state=STATE_START;
        
        
        private String data_the_guid = "";
        
        private String data_isa_guid = "";
        private String data_isa_name = "";
        private String data_isa_displayname = "";
        
        
        private Topic theTopic;
        private Topic isaTopic;

        
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
                    }
                    break;
                case STATE_CYCLIFY:
                    if(qName.equals(TAG_CONSTANT)) {
                        state = STATE_CYCLIFY_CONSTANT;
                        data_the_guid = atts.getValue("guid");
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT:
                    if(qName.equals(TAG_ISA)) {
                        state = STATE_CYCLIFY_CONSTANT_ISA;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_ISA:
                    if(qName.equals(TAG_LIST)) {
                        state = STATE_CYCLIFY_CONSTANT_ISA_LIST;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_ISA_LIST:
                    if(qName.equals(TAG_CONSTANT)) {
                        data_isa_guid = "";
                        data_isa_name = "";
                        data_isa_displayname = "";
                        state = STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT:
                    if(qName.equals(TAG_GUID)) {
                        state = STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_GUID;
                    }
                    else if(qName.equals(TAG_NAME)) {
                        state = STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_NAME;
                    }
                    else if(qName.equals(TAG_DISPLAYNAME)) {
                        state = STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_DISPLAYNAME;
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_DISPLAYNAME: {
                    if(qName.equals(TAG_DISPLAYNAME)) {
                        state=STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state=STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_GUID: {
                    if(qName.equals(TAG_GUID)) {
                        state=STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT: {
                    if(qName.equals(TAG_CONSTANT)) {
                        try {
                            if(data_the_guid != null && data_the_guid.length() > 0) {
                                theTopic = getTermTopic(data_the_guid, masterTerm, tm);
                            }
                            if(data_isa_guid != null && data_isa_guid.length() > 0) {
                                isaTopic = getTermTopic(data_isa_guid, data_isa_name, tm);
                                isaTopic.setDisplayName(LANG, data_isa_displayname);
                                
                                parent.setProgress(progress++);
                                
                                if(theTopic != null && isaTopic != null) {
                                    if(ISA_EQUALS_INSTANCE) {
                                        theTopic.addType(isaTopic);
                                    }
                                    else {
                                        Topic isaType = getIsaTypeTopic(tm);
                                        Association isaa = tm.createAssociation(isaType);                                       
                                        isaa.addPlayer(isaTopic, getCollectionTypeTopic(tm));
                                        isaa.addPlayer(theTopic, getInstanceTypeTopic(tm));
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        state=STATE_CYCLIFY_CONSTANT_ISA_LIST;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_ISA_LIST: {
                    if(qName.equals(TAG_LIST)) {
                        state=STATE_CYCLIFY_CONSTANT_ISA;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_ISA: {
                    if(qName.equals(TAG_ISA)) {
                        state=STATE_CYCLIFY_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT: {
                    if(!qName.equals(TAG_CONSTANT)) {
                        state=STATE_CYCLIFY;
                    }
                    break;
                }
                case STATE_CYCLIFY: {
                    state=STATE_START;
                    break;
                }
            }
        }
        
        
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_DISPLAYNAME:
                    data_isa_displayname+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_NAME:
                    data_isa_name+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_ISA_LIST_CONSTANT_GUID:
                    data_isa_guid+=new String(ch,start,length);
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
