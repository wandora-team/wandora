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
 */



package org.wandora.application.tools.extractors.alchemy;

import java.net.*;
import java.io.*;
import java.util.*;
import org.wandora.application.Wandora;
import org.wandora.application.tools.extractors.ExtractHelper;
import static org.wandora.application.tools.extractors.alchemy.AbstractAlchemyExtractor.ALCHEMY_URL;
import static org.wandora.application.tools.extractors.alchemy.AbstractAlchemyExtractor.sendRequest;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;




/**
 *
 * @author akivela
 */
public class AlchemyRelationExtractor extends AbstractAlchemyExtractor {

    private static boolean DEBUG_STATE_TRANSITIONS_IN_PARSER = true;
    
    private static boolean TRANSFORM_VERB_AND_TENSE = true;
    private static boolean ACTION_AS_PLAYER = true;
    private int relationCounter = 0;
    

    @Override
    public String getName() {
        return "Alchemy relation extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts relations out of given text using AlchemyAPI service. Read more at http://www.alchemyapi.com/.";
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

                String alchemyURL = ALCHEMY_URL+"calls/text/TextGetRelations";
                String alchemyData = "apikey="+URLEncoder.encode(apikey, "utf-8")+"&maxRetrieve=100&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String response = sendRequest(new URL(alchemyURL), alchemyData, "application/x-www-form-urlencoded", "POST");

                System.out.println("Alchemy response: "+response);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemyRelationParser parserHandler = new AlchemyRelationParser(getMasterSubject(), content, topicMap, this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                relationCounter = 0;
                try {
                    reader.parse(new InputSource(new StringReader(response)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                if(relationCounter == 0) {
                    log("AlchemyAPI didn't found any relations.");
                }
                else {
                    log("AlchemyAPI found total " + relationCounter + " relation.");
                }
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






    public class AlchemyRelationParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public AlchemyRelationParser(String term, String data, TopicMap tm, AbstractAlchemyExtractor parent){
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
        public static final String TAG_USAGE="usage";
        public static final String TAG_URL="url";
        public static final String TAG_LANGUAGE="language";
        public static final String TAG_TEXT="text";
        public static final String TAG_RELATIONS="relations";
        public static final String TAG_RELATION="relation";
        
        public static final String TAG_SUBJECT="subject";
        public static final String TAG_SENTIMENT="sentiment";
        public static final String TAG_TYPE="type";
        public static final String TAG_SCORE="score";
        public static final String TAG_MIXED="mixed";
        
        public static final String TAG_ACTION="action";
        public static final String TAG_LEMMATIZED="lemmatized";
        public static final String TAG_VERB="verb";
        public static final String TAG_TENSE="tense";
        public static final String TAG_NEGATED="negated";
        
        public static final String TAG_OBJECT="object";
        public static final String TAG_LOCATION="location";
        
        private static final int STATE_START=0;
        private static final int STATE_RESULTS=1;
        private static final int STATE_RESULTS_STATUS=11;
        private static final int STATE_RESULTS_LANGUAGE=12;
        private static final int STATE_RESULTS_URL=13;
        private static final int STATE_RESULTS_TEXT=14;
        private static final int STATE_RESULTS_USAGE=16;
        private static final int STATE_RESULTS_RELATIONS=15;
        private static final int STATE_RESULTS_RELATIONS_RELATION=151;
        private static final int STATE_RESULTS_RELATIONS_RELATION_SUBJECT=1511;
        private static final int STATE_RESULTS_RELATIONS_RELATION_ACTION=1512;
        private static final int STATE_RESULTS_RELATIONS_RELATION_OBJECT=1513;
        
        private static final int STATE_RESULTS_RELATIONS_RELATION_SUBJECT_TEXT=15111;
        
        private static final int STATE_RESULTS_RELATIONS_RELATION_ACTION_TEXT=15121;
        private static final int STATE_RESULTS_RELATIONS_RELATION_ACTION_LEMMATIZED=15122;
        private static final int STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB=15123;
        private static final int STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TEXT=151231;
        private static final int STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TENSE=151232;
        private static final int STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_NEGATED=151233;
        
        private static final int STATE_RESULTS_RELATIONS_RELATION_OBJECT_TEXT=15131;
        
        
        private int state=STATE_START;


        private String data_status = "";
        private String data_usage = "";
        private String data_language = "";
        private String data_url = "";
        private String data_text = "";
        private String data_subject_text = "";
        private String data_action_text = "";
        private String data_action_lemmatized = "";
        private String data_action_verb_text = "";
        private String data_action_verb_tense = "";
        private String data_action_verb_negated = "";
        private String data_object_text = "";
        
        private boolean object_is_location = false;
        


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
            if(DEBUG_STATE_TRANSITIONS_IN_PARSER) System.out.print(" "+state);
            switch(state) {
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
                    else if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_RESULTS_LANGUAGE;
                        data_language = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS_URL;
                        data_url = "";
                    }
                    else if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_TEXT;
                        data_text = "";
                    }
                    else if(qName.equals(TAG_USAGE)) {
                        state = STATE_RESULTS_USAGE;
                        data_usage = "";
                    }
                    else if(qName.equals(TAG_RELATIONS)) {
                        state = STATE_RESULTS_RELATIONS;
                    }
                    break;
                case STATE_RESULTS_RELATIONS:
                    if(qName.equals(TAG_RELATION)) {
                        data_subject_text = "";
                        data_action_text = "";
                        data_object_text = "";
                        data_action_lemmatized = "";
                        data_action_verb_text = "";
                        data_action_verb_tense = "";
                        data_action_verb_negated = "";
                        state = STATE_RESULTS_RELATIONS_RELATION;
                    }
                    break;

                case STATE_RESULTS_RELATIONS_RELATION:
                    if(qName.equals(TAG_SUBJECT)) {
                        data_subject_text = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_SUBJECT;
                    }
                    else if(qName.equals(TAG_ACTION)) {
                        data_action_text = "";
                        data_action_lemmatized = "";
                        data_action_verb_text = "";
                        data_action_verb_tense = "";
                        data_action_verb_negated = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION;
                    }
                    else if(qName.equals(TAG_OBJECT)) {
                        data_object_text = "";
                        object_is_location = false;
                        state = STATE_RESULTS_RELATIONS_RELATION_OBJECT;
                    }
                    else if(qName.equals(TAG_LOCATION)) {
                        data_object_text = "";
                        object_is_location = true;
                        state = STATE_RESULTS_RELATIONS_RELATION_OBJECT;
                    }
                    break;

                case STATE_RESULTS_RELATIONS_RELATION_SUBJECT:
                    if(qName.equals(TAG_TEXT)) {
                        data_subject_text = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_SUBJECT_TEXT;
                    }
                    break;
                
                case STATE_RESULTS_RELATIONS_RELATION_ACTION:
                    if(qName.equals(TAG_TEXT)) {
                        data_action_text = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_TEXT;
                    }
                    else if(qName.equals(TAG_LEMMATIZED)) {
                        data_action_lemmatized = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_LEMMATIZED;
                    }
                    else if(qName.equals(TAG_VERB)) {
                        data_action_verb_text = "";
                        data_action_verb_tense = "";
                        data_action_verb_negated = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB;
                    }
                    break;
                
                case STATE_RESULTS_RELATIONS_RELATION_OBJECT:
                    if(qName.equals(TAG_TEXT)) {
                        data_object_text = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_OBJECT_TEXT;
                    }
                    break;
                
                case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB:
                    if(qName.equals(TAG_TEXT)) {
                        data_action_verb_text = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TEXT;
                    }
                    else if(qName.equals(TAG_TENSE)) {
                        data_action_verb_tense = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TENSE;
                    }
                    else if(qName.equals(TAG_NEGATED)) {
                        data_action_verb_negated = "";
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_NEGATED;
                    }
                    break;
            }
            if(DEBUG_STATE_TRANSITIONS_IN_PARSER) System.out.println(" -> "+state);
        }







        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(DEBUG_STATE_TRANSITIONS_IN_PARSER) System.out.print("    "+state);
            switch(state) {
                case STATE_RESULTS_RELATIONS_RELATION:
                    if(qName.equals(TAG_RELATION)) {
                        // Process all collected information
                        if(isValid(data_subject_text) && isValid(data_action_text) && isValid(data_object_text)) {
                            try {
                                makeRelation();
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        else {
                            if(isValid(data_subject_text)) {
                                log("Subject text is empty. Skipping found relation.");
                            }
                            else if(isValid(data_action_text)) {
                                log("Action text is empty. Skipping found relation.");
                            }
                            else if(isValid(data_object_text)) {
                                log("Object text is empty. Skipping found relation.");
                            }
                        }

                        state = STATE_RESULTS_RELATIONS;
                    }
                    break;

                case STATE_RESULTS_RELATIONS:
                    if(qName.equals(TAG_RELATIONS)) {
                        state = STATE_RESULTS;
                    }
                    break;
                    
                case STATE_RESULTS:
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_START;
                    }
                    break;


                case STATE_RESULTS_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_URL:
                    if(qName.equals(TAG_URL)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_USAGE:
                    if(qName.equals(TAG_USAGE)) {
                        if(data_usage != null && data_usage.length() > 0) {
                            parent.log(data_usage);
                        }
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS_LANGUAGE:
                    if(qName.equals(TAG_LANGUAGE)) {
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
                    
                //
                case STATE_RESULTS_RELATIONS_RELATION_SUBJECT:
                    if(qName.equals(TAG_SUBJECT)) {
                        state = STATE_RESULTS_RELATIONS_RELATION;
                    }
                    break; 
                case STATE_RESULTS_RELATIONS_RELATION_ACTION:
                    if(qName.equals(TAG_ACTION)) {
                        state = STATE_RESULTS_RELATIONS_RELATION;
                    }
                    break;
                case STATE_RESULTS_RELATIONS_RELATION_OBJECT:
                    if(qName.equals(TAG_OBJECT) && !object_is_location) {
                        state = STATE_RESULTS_RELATIONS_RELATION;
                    }
                    else if(qName.equals(TAG_LOCATION) && object_is_location) {
                        state = STATE_RESULTS_RELATIONS_RELATION;
                    }
                    break;
                    
                    
                case STATE_RESULTS_RELATIONS_RELATION_SUBJECT_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_SUBJECT;
                    }
                    break;
                case STATE_RESULTS_RELATIONS_RELATION_ACTION_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION;
                    }
                    break;
                case STATE_RESULTS_RELATIONS_RELATION_OBJECT_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_OBJECT;
                    }
                    break;
                    
                case STATE_RESULTS_RELATIONS_RELATION_ACTION_LEMMATIZED:
                    if(qName.equals(TAG_LEMMATIZED)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION;
                    }
                    break;
                    
                case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB:
                    if(qName.equals(TAG_VERB)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION;
                    }
                    break;
                    
                case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB;
                    }
                    break;
                case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TENSE:
                    if(qName.equals(TAG_TENSE)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB;
                    }
                    break;
                case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_NEGATED:
                    if(qName.equals(TAG_NEGATED)) {
                        state = STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB;
                    }
                    break;
            }
            if(DEBUG_STATE_TRANSITIONS_IN_PARSER) System.out.println(" -> "+state);
        }






        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_RESULTS_STATUS:
                    data_status += new String(ch,start,length);
                    break;
                case STATE_RESULTS_LANGUAGE:
                    data_language += new String(ch,start,length);
                    break;
                case STATE_RESULTS_URL:
                    data_url += new String(ch,start,length);
                    break;
                case STATE_RESULTS_TEXT:
                    data_text += new String(ch,start,length);
                    break;
                case STATE_RESULTS_USAGE: 
                    data_usage += new String(ch,start,length);
                    break;
                    
               case STATE_RESULTS_RELATIONS_RELATION_SUBJECT_TEXT:
                    data_subject_text += new String(ch,start,length);
                    break;
               case STATE_RESULTS_RELATIONS_RELATION_ACTION_TEXT:
                    data_action_text += new String(ch,start,length);
                    break;
               case STATE_RESULTS_RELATIONS_RELATION_OBJECT_TEXT:
                    data_object_text += new String(ch,start,length);
                    break;
                   
               case STATE_RESULTS_RELATIONS_RELATION_ACTION_LEMMATIZED:
                    data_action_lemmatized += new String(ch,start,length);
                    break;
                   
               case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TEXT:
                    data_action_verb_text += new String(ch,start,length);
                    break;
               case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_TENSE:
                    data_action_verb_tense += new String(ch,start,length);
                    break;
               case STATE_RESULTS_RELATIONS_RELATION_ACTION_VERB_NEGATED:
                    data_action_verb_negated += new String(ch,start,length);
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

        
        private boolean isValid(String str) {
            if(str == null) return false;
            if(str.length() == 0) return false;
            return true;
        }
        
        private boolean isValid(Topic t) {
            if(t == null) return false;
            try {
                if(t.isRemoved()) return false;
            }
            catch(Exception e) {
                return false;
            }
            return true;
        }
        
        
        /**
         * The AlchemyRelationParser passes execution here whenever the relation tag
         * closes in the response XML. The method creates topics and associations for
         * the relation.
        **/
        private void makeRelation() throws Exception {

            Topic subjectTopic = getSubjectTopic(data_subject_text, tm);
            Topic actionTopic = getActionTopic(data_action_text, tm);
            Topic objectTopic = getObjectTopic(data_object_text, tm);

            if(isValid(subjectTopic) && isValid(actionTopic) && isValid(objectTopic)) {
                Topic subjectType = getSubjectType(tm);
                Topic actionType = getActionType(tm);
                Topic objectType = getObjectType(tm);
                
                if(TRANSFORM_VERB_AND_TENSE) {
                    if(isValid(actionType)) {
                        if(isValid(data_action_verb_text) && isValid(data_action_verb_tense)) {
                            Topic verbTopic = getVerbTopic(data_action_verb_text, tm);
                            Topic tenseTopic = getTenseTopic(data_action_verb_tense, tm);

                            if(isValid(verbTopic) && isValid(tenseTopic)) {
                                Topic tenseType = getTenseType(tm);
                                Topic verbType = getVerbType(tm);

                                Association actionVerb = tm.createAssociation(verbType);
                                actionVerb.addPlayer(verbTopic, verbType);
                                actionVerb.addPlayer(tenseTopic, tenseType);
                                actionVerb.addPlayer(actionTopic, actionType);
                            }
                        }
                    }
                }

                if(isValid(subjectType) && isValid(actionType) && isValid(objectType)) {
                    if(ACTION_AS_PLAYER) {
                        Topic relationType = getRelationType(tm);
                        if(isValid(relationType)) {
                            Association relation = tm.createAssociation(relationType);
                            relation.addPlayer(subjectTopic, subjectType);
                            relation.addPlayer(actionTopic, actionType);
                            relation.addPlayer(objectTopic, objectType);
                            
                            if(isValid(masterTopic)) {
                                Topic topicType = getTopicType(tm);
                                if(isValid(topicType)) {
                                    relation.addPlayer(masterTopic, topicType);
                                }
                            }
                            
                            relationCounter++;
                        }
                    }
                    else { // ACTION AS TYPE
                        Association relation = tm.createAssociation(actionType);
                        relation.addPlayer(subjectTopic, subjectType);
                        relation.addPlayer(objectTopic, objectType);
                        
                        if(isValid(masterTopic)) {
                            Topic topicType = getTopicType(tm);
                            if(isValid(topicType)) {
                                relation.addPlayer(masterTopic, topicType);
                            }
                        }
                        
                        relationCounter++;
                    }
                }
            }
        }
    }

}


