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
 * OpenCycInstanceExtractor.java
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
public class OpenCycInstanceExtractor extends AbstractOpenCycExtractor {
    
	private static final long serialVersionUID = 1L;




	/** Creates a new instance of OpenCycInstanceExtractor */
    public OpenCycInstanceExtractor() {
    }
    
    
    
    @Override
    public String getName() {
        return "OpenCyc instance extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the instance XML feed from OpenCyc's web api and converts the XML feed to a topic map. "+
               "See http://65.99.218.242:8080/RESTfulCyc/Constant/Dog/instances for an example of such XML feed.";
    }


    public String getMasterTerm(String u) {
        try {
            if(u.endsWith("/instances")) {
                u = u.substring(0, u.length()-"/instances".length());
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
        CycInstanceParser parserHandler = new CycInstanceParser(getMasterSubject(), topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " Cyc instances found!");
        return true;
    }
    

    
    
    private static class CycInstanceParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        String masterTerm = null;
        
        
        public CycInstanceParser(String term, TopicMap tm, AbstractOpenCycExtractor parent){
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
        public static final String TAG_INSTANCES="instances";
        public static final String TAG_GUID="guid";
        public static final String TAG_NAME="name";
        public static final String TAG_DISPLAYNAME="displayname";
        public static final String TAG_NAT="nat";

        private static final int STATE_START=0;
        private static final int STATE_CYCLIFY=2;
        private static final int STATE_CYCLIFY_CONSTANT=3;
        private static final int STATE_CYCLIFY_CONSTANT_INSTANCES=4;
        private static final int STATE_CYCLIFY_CONSTANT_INSTANCES_LIST=5;
        private static final int STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT=6;
        private static final int STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_GUID=7;
        private static final int STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_NAME=8;
        private static final int STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_DISPLAYNAME=9;
        
        private static final int STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_NAT = 100;
        
        
        
        private int state=STATE_START;
        
        
        private String data_the_guid = "";
        
        private String data_instance_guid = "";
        private String data_instance_name = "";
        private String data_instance_displayname = "";
        
        
        private Topic theTopic;
        private Topic instanceTopic;

        
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
                    if(qName.equals(TAG_INSTANCES)) {
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_INSTANCES:
                    if(qName.equals(TAG_LIST)) {
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES_LIST;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST:
                    if(qName.equals(TAG_CONSTANT)) {
                        data_instance_guid = "";
                        data_instance_name = "";
                        data_instance_displayname = "";
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT;
                    }
                    if(qName.equals(TAG_NAT)) {
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_NAT;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT:
                    if(qName.equals(TAG_GUID)) {
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_GUID;
                    }
                    else if(qName.equals(TAG_NAME)) {
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_NAME;
                    }
                    else if(qName.equals(TAG_DISPLAYNAME)) {
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_DISPLAYNAME;
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_DISPLAYNAME: {
                    if(qName.equals(TAG_DISPLAYNAME)) {
                        state=STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state=STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_GUID: {
                    if(qName.equals(TAG_GUID)) {
                        state=STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT: {
                    if(qName.equals(TAG_CONSTANT)) {
                        try {
                            if(data_the_guid != null && data_the_guid.length() > 0) {
                                theTopic = getTermTopic(data_the_guid, masterTerm, tm);
                            }
                            if(data_instance_guid != null && data_instance_guid.length() > 0) {
                                instanceTopic = getTermTopic(data_instance_guid, data_instance_name, tm);
                                instanceTopic.setDisplayName(LANG, data_instance_displayname);
                                parent.setProgress(progress++);
                                
                                if(theTopic != null && instanceTopic != null) {
                                    if(ISA_EQUALS_INSTANCE) {
                                        instanceTopic.addType(theTopic);
                                    }
                                    else {
                                        Topic isaType = getIsaTypeTopic(tm);
                                        Association isaa = tm.createAssociation(isaType);
                                        isaa.addPlayer(instanceTopic, getInstanceTypeTopic(tm));
                                        isaa.addPlayer(theTopic, getCollectionTypeTopic(tm));
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        state=STATE_CYCLIFY_CONSTANT_INSTANCES_LIST;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_NAT:
                    if(qName.equals(TAG_NAT)) {
                        state = STATE_CYCLIFY_CONSTANT_INSTANCES_LIST;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST: {
                    if(qName.equals(TAG_LIST)) {
                        state=STATE_CYCLIFY_CONSTANT_INSTANCES;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_INSTANCES: {
                    if(qName.equals(TAG_INSTANCES)) {
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
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_DISPLAYNAME:
                    data_instance_displayname+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_NAME:
                    data_instance_name+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_INSTANCES_LIST_CONSTANT_GUID:
                    data_instance_guid+=new String(ch,start,length);
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
