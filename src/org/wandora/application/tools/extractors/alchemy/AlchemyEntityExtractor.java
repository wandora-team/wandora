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
 * AlchemyEntityExtractor.java
 *
 *
 */
package org.wandora.application.tools.extractors.alchemy;


import java.net.*;
import java.io.*;
import java.util.*;
import org.wandora.application.Wandora;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;




/**
 * Get an AlchemyAPI entity for any text and associate entity topic with the
 * text carrier topic.
 *
 * @author akivela
 */
public class AlchemyEntityExtractor extends AbstractAlchemyExtractor {


    @Override
    public String getName() {
        return "Alchemy entity extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts entities out of text using AlchemyAPI. Read more at http://www.alchemyapi.com/";
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

                String alchemyURL = ALCHEMY_URL+"calls/text/TextGetNamedEntities"; // ?apikey="+apikey+"&linkedData=1&disambiguate=1&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String alchemyData = "apikey="+URLEncoder.encode(apikey, "utf-8")+"&linkedData=1&disambiguate=1&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String result = sendRequest(new URL(alchemyURL), alchemyData, "application/x-www-form-urlencoded", "POST");

                System.out.println("Alchemy returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemyEntityParser parserHandler = new AlchemyEntityParser(getMasterSubject(), content, topicMap,this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                log("Total " + parserHandler.progress + " Alchemy entities found!");
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






    public class AlchemyEntityParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public AlchemyEntityParser(String term, String data, TopicMap tm, AbstractAlchemyExtractor parent){
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
        public static final String TAG_LANGUAGE="language";
        public static final String TAG_URL="url";
        public static final String TAG_TEXT="text";
        public static final String TAG_ENTITIES="entities";
        public static final String TAG_ENTITY="entity";
        public static final String TAG_TYPE="type";
        public static final String TAG_RELEVANCE="relevance";
        public static final String TAG_COUNT="count";
        public static final String TAG_DISAMBIGUATED="disambiguated";
        public static final String TAG_NAME="name";
        public static final String TAG_SUBTYPE="subType";
        public static final String TAG_WEBSITE="website";
        public static final String TAG_GEO="geo";
        public static final String TAG_DBPEDIA="dbpedia";
        public static final String TAG_YAGO="yago";
        public static final String TAG_OPENCYC="opencyc";
        public static final String TAG_UMBEL="umbel";
        public static final String TAG_FREEBASE="freebase";
        public static final String TAG_CIAFACTBOOK="ciaFactbook";
        public static final String TAG_CENSUS="census";
        public static final String TAG_GEONAMES="geonames";
        public static final String TAG_MUSICBRAINZ="musicBrainz";
        public static final String TAG_QUOTATIONS="quotations";
        public static final String TAG_QUOTATION="quotation";
        

        private static final int STATE_START=0;
        private static final int STATE_RESULTS=1;
        private static final int STATE_RESULTS_STATUS=11;
        private static final int STATE_RESULTS_LANGUAGE=12;
        private static final int STATE_RESULTS_URL=13;
        private static final int STATE_RESULTS_TEXT=14;
        private static final int STATE_RESULTS_ENTITIES=15;
        private static final int STATE_RESULTS_ENTITIES_ENTITY=151;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_TYPE=1511;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_RELEVANCE=1512;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_COUNT=1513;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_TEXT=1514;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED=1515;
        
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_NAME=151501;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_SUBTYPE=151502;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_WEBSITE=151503;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEO=151504;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_DBPEDIA=151505;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_YAGO=151506;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_OPENCYC=151507;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_UMBEL=151508;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_FREEBASE=151509;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CIAFACTBOOK=151510;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CENSUS=151511;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEONAMES=151512;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_MUSICBRAINZ=151513;
        
        private static final int STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS=1516;
        private static final int STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS_QUOTATION=15161;

        private int state=STATE_START;


        private String data_status = "";
        private String data_language = "";
        private String data_url = "";
        private String data_text = "";
        private String data_entity_type = "";
        private String data_entity_relevance = "";
        private String data_entity_count = "";
        private String data_entity_text = "";
        private String data_entity_disambiguated_name = "";
        private String data_entity_disambiguated_subtype = "";
        private String data_entity_disambiguated_website = "";
        private String data_entity_disambiguated_geo = "";
        private String data_entity_disambiguated_dbpedia = "";
        private String data_entity_disambiguated_yago = "";
        private String data_entity_disambiguated_opencyc = "";
        private String data_entity_disambiguated_umbel = "";
        private String data_entity_disambiguated_freebase = "";
        private String data_entity_disambiguated_ciafactbook = "";
        private String data_entity_disambiguated_census = "";
        private String data_entity_disambiguated_geonames = "";
        private String data_entity_disambiguated_musicbrainz = "";
        private String data_entity_quotation = "";
        private ArrayList<String> data_entity_quotations = new ArrayList<String>();




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
            switch(state){
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
                    else if(qName.equals(TAG_ENTITIES)) {
                        state = STATE_RESULTS_ENTITIES;
                    }
                    break;
                case STATE_RESULTS_ENTITIES:
                    if(qName.equals(TAG_ENTITY)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY;
                        data_entity_type = "";
                        data_entity_relevance = "";
                        data_entity_count = "";
                        data_entity_text = "";

                        data_entity_disambiguated_name = "";
                        data_entity_disambiguated_subtype = "";
                        data_entity_disambiguated_website = "";
                        data_entity_disambiguated_geo = "";
                        data_entity_disambiguated_dbpedia = "";
                        data_entity_disambiguated_yago = "";
                        data_entity_disambiguated_opencyc = "";
                        data_entity_disambiguated_umbel = "";
                        data_entity_disambiguated_freebase = "";
                        data_entity_disambiguated_ciafactbook = "";
                        data_entity_disambiguated_census = "";
                        data_entity_disambiguated_geonames = "";
                        data_entity_disambiguated_musicbrainz = "";

                        data_entity_quotations = new ArrayList<String>();
                        data_entity_quotation = "";
                    }
                    break;

                case STATE_RESULTS_ENTITIES_ENTITY:
                    if(qName.equals(TAG_TYPE)) {
                        data_entity_type = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_TYPE;
                    }
                    else if(qName.equals(TAG_RELEVANCE)) {
                        data_entity_relevance = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_RELEVANCE;
                    }
                    else if(qName.equals(TAG_COUNT)) {
                        data_entity_count = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_COUNT;
                    }
                    else if(qName.equals(TAG_TEXT)) {
                        data_entity_text = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_TEXT;
                    }
                    else if(qName.equals(TAG_DISAMBIGUATED)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;

                        data_entity_disambiguated_name = "";
                        data_entity_disambiguated_subtype = "";
                        data_entity_disambiguated_website = "";
                        data_entity_disambiguated_geo = "";
                        data_entity_disambiguated_dbpedia = "";
                        data_entity_disambiguated_yago = "";
                        data_entity_disambiguated_opencyc = "";
                        data_entity_disambiguated_umbel = "";
                        data_entity_disambiguated_freebase = "";
                        data_entity_disambiguated_ciafactbook = "";
                        data_entity_disambiguated_census = "";
                        data_entity_disambiguated_geonames = "";
                        data_entity_disambiguated_musicbrainz = "";
                    }
                    else if(qName.equals(TAG_QUOTATIONS)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS;
                    }
                    break;

                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED:
                    if(qName.equals(TAG_NAME)) {
                        data_entity_disambiguated_name = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_NAME;
                    }
                    else if(qName.equals(TAG_SUBTYPE)) {
                        data_entity_disambiguated_subtype = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_SUBTYPE;
                    }
                    else if(qName.equals(TAG_WEBSITE)) {
                        data_entity_disambiguated_website = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_WEBSITE;
                    }
                    else if(qName.equals(TAG_GEO)) {
                        data_entity_disambiguated_geo = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEO;
                    }
                    else if(qName.equals(TAG_DBPEDIA)) {
                        data_entity_disambiguated_dbpedia = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_DBPEDIA;
                    }
                    else if(qName.equals(TAG_YAGO)) {
                        data_entity_disambiguated_yago = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_YAGO;
                    }
                    else if(qName.equals(TAG_OPENCYC)) {
                        data_entity_disambiguated_opencyc = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_OPENCYC;
                    }
                    else if(qName.equals(TAG_UMBEL)) {
                        data_entity_disambiguated_umbel = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_UMBEL;
                    }
                    else if(qName.equals(TAG_FREEBASE)) {
                        data_entity_disambiguated_freebase = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_FREEBASE;
                    }
                    else if(qName.equals(TAG_CIAFACTBOOK)) {
                        data_entity_disambiguated_ciafactbook = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CIAFACTBOOK;
                    }
                    else if(qName.equals(TAG_CENSUS)) {
                        data_entity_disambiguated_census = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CENSUS;
                    }
                    else if(qName.equals(TAG_GEONAMES)) {
                        data_entity_disambiguated_geonames = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEONAMES;
                    }
                    else if(qName.equals(TAG_MUSICBRAINZ)) {
                        data_entity_disambiguated_musicbrainz = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_MUSICBRAINZ;
                    }
                    break;

                case STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS:
                    if(qName.equals(TAG_QUOTATION)) {
                        data_entity_quotation = "";
                        state = STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS_QUOTATION;
                    }
                    break;
            }
        }







        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS_QUOTATION: 
                    if(qName.equals(TAG_QUOTATION)) {
                        if(data_entity_quotation != null && data_entity_quotation.length() > 0) {
                            data_entity_quotations.add(data_entity_quotation);
                        }
                        state = STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS:
                    if(qName.equals(TAG_QUOTATIONS)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY:
                    if(qName.equals(TAG_ENTITY)) {

                        //parent.log("Found entity '"+data_entity_text+"'");

                        parent.setProgress( progress++ );
                        if(isValid(data_entity_text)) {
                            try {
                                if(parent.getCurrentLogger() != null) parent.log("Alchemy found named entity '"+data_entity_text+"'.");
                                Topic entityTopic = parent.getEntityTopic(data_entity_text, tm);
                                if(masterTopic != null && entityTopic != null) {
                                    Topic entityType = parent.getEntityType(tm);
                                    Association a = tm.createAssociation(entityType);
                                    a.addPlayer(masterTopic, parent.getTopicType(tm));
                                    a.addPlayer(entityTopic, entityType);
                                    if(parent.EXTRACT_RELEVANCE && data_entity_relevance != null && data_entity_relevance.length() > 0) {
                                        Topic relevance = parent.getRelevanceTopic(data_entity_relevance, tm);
                                        Topic relevanceType = parent.getRelevanceType(tm);
                                        if(relevance != null && !relevance.isRemoved() && relevanceType != null && !relevanceType.isRemoved()) {
                                            a.addPlayer(relevance, relevanceType);
                                        }
                                    }
                                    if(isValid(data_entity_type)) {
                                        Topic entityTypeTopic = getEntityTypeTopic(data_entity_type, tm);
                                        if(entityTypeTopic != null) {
                                            entityTopic.addType(entityTypeTopic);
                                        }
                                    }
                                    if(isValid(data_entity_disambiguated_dbpedia)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_dbpedia, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_yago)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_yago, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_opencyc)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_opencyc, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_umbel)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_umbel, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_freebase)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_freebase, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_ciafactbook)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_ciafactbook, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_census)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_census, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_geonames)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_geonames, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_musicbrainz)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_musicbrainz, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_geo)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_geo, entityTopic, entityType, tm);
                                    }
                                    if(isValid(data_entity_disambiguated_website)) {
                                        createSameAsAssociationWith(data_entity_disambiguated_website, entityTopic, entityType, tm);
                                    }
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        else {
                            parent.log("Zero length entity text found! Rejecting!");
                        }

                        state = STATE_RESULTS_ENTITIES;
                    }
                    break;

                case STATE_RESULTS_ENTITIES:
                    if(qName.equals(TAG_ENTITIES)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS:
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_START;
                    }
                    break;


                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_NAME:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_SUBTYPE:
                    if(qName.equals(TAG_SUBTYPE)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_WEBSITE:
                    if(qName.equals(TAG_WEBSITE)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEO:
                    if(qName.equals(TAG_GEO)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_DBPEDIA:
                    if(qName.equals(TAG_DBPEDIA)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_YAGO:
                    if(qName.equals(TAG_YAGO)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_OPENCYC:
                    if(qName.equals(TAG_OPENCYC)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_UMBEL:
                    if(qName.equals(TAG_UMBEL)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_FREEBASE:
                    if(qName.equals(TAG_FREEBASE)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CIAFACTBOOK:
                    if(qName.equals(TAG_CIAFACTBOOK)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CENSUS:
                    if(qName.equals(TAG_CENSUS)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEONAMES:
                    if(qName.equals(TAG_GEONAMES)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_MUSICBRAINZ:
                    if(qName.equals(TAG_MUSICBRAINZ)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED;
                    }
                    break;

                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED:
                    if(qName.equals(TAG_DISAMBIGUATED)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_TEXT:
                    if(qName.equals(TAG_TEXT)) {
                        //parent.log("Closing entity text with '"+data_entity_text+"'");
                        state = STATE_RESULTS_ENTITIES_ENTITY;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_COUNT:
                    if(qName.equals(TAG_COUNT)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_RELEVANCE:
                    if(qName.equals(TAG_RELEVANCE)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY;
                    }
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_TYPE:
                    if(qName.equals(TAG_TYPE)) {
                        state = STATE_RESULTS_ENTITIES_ENTITY;
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
            }
        }






        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_RESULTS_STATUS:
                    data_status+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_LANGUAGE:
                    data_language+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_URL:
                    data_url+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_TEXT:
                    data_text+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_TYPE:
                    data_entity_type+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_RELEVANCE:
                    data_entity_relevance+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_COUNT:
                    data_entity_count+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_TEXT:
                    data_entity_text+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_NAME:
                    data_entity_disambiguated_name+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_SUBTYPE:
                    data_entity_disambiguated_subtype+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_WEBSITE:
                    data_entity_disambiguated_website+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEO:
                    data_entity_disambiguated_geo+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_DBPEDIA:
                    data_entity_disambiguated_dbpedia+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_YAGO:
                    data_entity_disambiguated_yago+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_OPENCYC:
                    data_entity_disambiguated_opencyc+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_UMBEL:
                    data_entity_disambiguated_umbel+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_FREEBASE:
                    data_entity_disambiguated_freebase+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CIAFACTBOOK:
                    data_entity_disambiguated_ciafactbook+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_CENSUS:
                    data_entity_disambiguated_census+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_GEONAMES:
                    data_entity_disambiguated_geonames+=new String(ch,start,length);
                    break;
                case STATE_RESULTS_ENTITIES_ENTITY_DISAMBIGUATED_MUSICBRAINZ:
                    data_entity_disambiguated_musicbrainz+=new String(ch,start,length);
                    break;

                case STATE_RESULTS_ENTITIES_ENTITY_QUOTATIONS_QUOTATION:
                    data_entity_quotation+=new String(ch,start,length);
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

    }


}
