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
 * OpenCycGenlsExtractor.java
 * 
 */


package org.wandora.application.tools.extractors.opencyc;


import java.util.*;

import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;

/**
 *
 * @author akivela
 */
public class OpenCycGenlsExtractor extends AbstractOpenCycExtractor {
    
    /** Creates a new instance of OpenCycIsaExtractor */
    public OpenCycGenlsExtractor() {
    }
    
    
    
    @Override
    public String getName() {
        return "OpenCyc genls extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the genls XML feed from OpenCyc's web api and converts the XML feed to a topic map. "+
               "See http://65.99.218.242:8080/RESTfulCyc/Constant/Tower/genls for an example of such XML feed.";
    }


    public String getMasterTerm(String u) {
        try {
            if(u.endsWith("/genls")) {
                u = u.substring(0, u.length()-"/genls".length());
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
        CycGenlsParser parserHandler = new CycGenlsParser(getMasterSubject(), topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " Cyc genls' found!");
        return true;
    }
    

    
    
    private static class CycGenlsParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        String masterTerm = null;
        
        
        public CycGenlsParser(String term, TopicMap tm, AbstractOpenCycExtractor parent){
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
        public static final String TAG_GENLS="genls";
        public static final String TAG_GUID="guid";
        public static final String TAG_NAME="name";
        public static final String TAG_DISPLAYNAME="displayname";
        
        public static final String TAG_NAT="nat";
        public static final String TAG_FUNCTOR="functor";
        public static final String TAG_ARG="arg";
        

        private static final int STATE_START=0;
        private static final int STATE_CYCLIFY=2;
        private static final int STATE_CYCLIFY_CONSTANT=3;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS=4;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST=5;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT=6;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_GUID=7;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_NAME=8;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_DISPLAYNAME=9;

        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT = 200;
        
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR = 210;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_GUID = 212;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_NAME = 213;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_DISPLAYNAME = 214;
        
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG = 220;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_GUID = 222;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_NAME = 223;
        private static final int STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_DISPLAYNAME = 224;
        
        private int state=STATE_START;
        
        
        private String data_the_guid = "";
        
        private String data_genls_guid = "";
        private String data_genls_name = "";
        private String data_genls_displayname = "";
        
        private String data_functor_guid = "";
        private String data_functor_name = "";
        private String data_functor_displayname = "";
        
        private String data_arg_guid = "";
        private String data_arg_name = "";
        private String data_arg_displayname = "";
        
        private ArrayList args = new ArrayList();
        
        private Topic theTopic;
        private Topic genlsTopic;

        
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
                    if(qName.equals(TAG_GENLS)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS:
                    if(qName.equals(TAG_LIST)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST:
                    if(qName.equals(TAG_CONSTANT)) {
                        data_genls_guid = "";
                        data_genls_name = "";
                        data_genls_displayname = "";
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT;
                    }
                    else if(qName.equals(TAG_NAT)) {
                        data_functor_guid = "";
                        data_functor_name = "";
                        data_functor_displayname = "";
                        data_arg_guid = "";
                        data_arg_name = "";
                        data_arg_displayname = "";
                        args = new ArrayList();
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT:
                    if(qName.equals(TAG_GUID)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_GUID;
                    }
                    else if(qName.equals(TAG_NAME)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_NAME;
                    }
                    else if(qName.equals(TAG_DISPLAYNAME)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_DISPLAYNAME;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT:
                    if(qName.equals(TAG_FUNCTOR)) {
                        data_functor_guid = "";
                        data_functor_name = "";
                        data_functor_displayname = "";
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR;
                    }
                    if(qName.equals(TAG_ARG)) {
                        data_arg_guid = "";
                        data_arg_name = "";
                        data_arg_displayname = "";
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR:
                    if(qName.equals(TAG_GUID)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_GUID;
                    }
                    else if(qName.equals(TAG_NAME)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_NAME;
                    }
                    else if(qName.equals(TAG_DISPLAYNAME)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_DISPLAYNAME;
                    }
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG:
                    if(qName.equals(TAG_GUID)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_GUID;
                    }
                    else if(qName.equals(TAG_NAME)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_NAME;
                    }
                    else if(qName.equals(TAG_DISPLAYNAME)) {
                        state = STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_DISPLAYNAME;
                    }
                    break;
            }
        }
        
        
        
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_DISPLAYNAME: {
                    if(qName.equals(TAG_DISPLAYNAME)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_GUID: {
                    if(qName.equals(TAG_GUID)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT: {
                    if(qName.equals(TAG_CONSTANT)) {
                        try {
                            if(data_the_guid != null && data_the_guid.length() > 0) {
                                theTopic = getTermTopic(data_the_guid, masterTerm, tm);
                            }
                            if(data_genls_guid != null && data_genls_guid.length() > 0) {
                                genlsTopic = getTermTopic(data_genls_guid, data_genls_name, tm);
                                genlsTopic.setDisplayName(LANG, data_genls_displayname);

                                parent.setProgress(progress++);
                                
                                if(theTopic != null && genlsTopic != null) {
                                    makeSubclassOf(tm, theTopic, genlsTopic);
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST;
                    }
                    break;
                }
                // -------------------------
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_DISPLAYNAME: {
                    if(qName.equals(TAG_DISPLAYNAME)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_GUID: {
                    if(qName.equals(TAG_GUID)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR: {
                    if(qName.equals(TAG_FUNCTOR)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT;
                    }
                    break;
                }
                // ----
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_DISPLAYNAME: {
                    if(qName.equals(TAG_DISPLAYNAME)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_GUID: {
                    if(qName.equals(TAG_GUID)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG: {
                    if(qName.equals(TAG_FUNCTOR)) {
                        args.add( data_arg_guid );
                        args.add( data_arg_name );
                        args.add( data_arg_displayname );
                        
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT;
                    }
                    break;
                }
                
                // ---
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT: {
                    if(qName.equals(TAG_NAT)) {
                        try {
                            if(data_the_guid != null && data_the_guid.length() > 0) {
                                theTopic = getTermTopic(data_the_guid, masterTerm, tm);
                            }
                            if(data_functor_guid != null && data_functor_guid.length() > 0) {
                                Topic functorTopic = getTermTopic(data_functor_guid, data_functor_name, tm);
                                functorTopic.setDisplayName(LANG, data_functor_displayname);
                                
                                parent.setProgress(progress++);
                                
                                Association a = tm.createAssociation( tm.getTopic(XTMPSI.SUPERCLASS_SUBCLASS ));
                                a.addPlayer(theTopic, tm.getTopic(XTMPSI.SUBCLASS));
                                a.addPlayer(functorTopic, getFunctorType(tm));
                                
                                for(Iterator<String> i=args.iterator(); i.hasNext();) {
                                    try {
                                        String arg_guid = i.next();
                                        String arg_name = i.next();
                                        String arg_displayname = i.next();

                                        int argNum = 1;
                                        if(arg_guid != null && arg_guid.length() > 0) {
                                            Topic argTopic = getTermTopic(arg_guid, arg_name, tm);
                                            if(arg_displayname != null && arg_displayname.length() > 0) argTopic.setDisplayName(LANG, arg_displayname);
                                            a.addPlayer(argTopic, getArgType(tm, argNum));
                                            argNum++;
                                        }
                                    }
                                    catch(Exception e) {
                                        parent.log(e);
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        state=STATE_CYCLIFY_CONSTANT_GENLS_LIST;
                    }
                    break;
                }
                
                // -----
                
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST: {
                    if(qName.equals(TAG_LIST)) {
                        state=STATE_CYCLIFY_CONSTANT_GENLS;
                    }
                    break;
                }
                case STATE_CYCLIFY_CONSTANT_GENLS: {
                    if(qName.equals(TAG_GENLS)) {
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
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_DISPLAYNAME:
                    data_genls_displayname+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_NAME:
                    data_genls_name+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_CONSTANT_GUID:
                    data_genls_guid+=new String(ch,start,length);
                    break;
                    
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_DISPLAYNAME:
                    data_functor_displayname+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_NAME:
                    data_functor_name+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_FUNCTOR_GUID:
                    data_functor_guid+=new String(ch,start,length);
                    break;
                    
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_DISPLAYNAME:
                    data_arg_displayname+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_NAME:
                    data_arg_name+=new String(ch,start,length);
                    break;
                case STATE_CYCLIFY_CONSTANT_GENLS_LIST_NAT_ARG_GUID:
                    data_arg_guid+=new String(ch,start,length);
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