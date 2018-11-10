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
 * XMLSubj3ctRecordExtractor.java
 *
 * Created on 2009-05-14
 *
 */
package org.wandora.application.tools.extractors.subj3ct;


import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;
import org.wandora.utils.Tuples.*;

import java.util.*;
import java.io.*;
import javax.swing.*;

import java.net.*;
import org.xml.sax.*;




/**
 * Unfortunately it looks like the Subj3ct web service has been closed down.
 * Thus, this tool is deprecated and you probably can't make it working.
 * However, the tool is still kept in Wandora's source code for the time being.
 * 
 * @author akivela
 */

public class XMLSubj3ctRecordExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;

	public static final boolean OVERRIDE_BASENAME = true;
    public static final boolean DESCRIPTION_AS_OCCURRENCE = true;
    public static final boolean DESCRIPTION_AS_PLAYER = false;

    protected String SUBJ3CT = "http://subj3ct.com/";
    protected String SI_BASE = "http://subj3ct.com/schema/data/";
    protected String PROVENANCE_SI = SI_BASE+"Provenance";
    protected String DESCRIPTION_SI = SI_BASE+"Description";
    protected String RECORDLINK_SI = SI_BASE+"Recordlink";
    protected String TRUST_SI = SI_BASE+"Trust";
    protected String EQUIVALENCE_SI = SI_BASE+"Equivalence";
    protected String EQUIVALENT_TOPIC_SI = SI_BASE+"Equivalent topic";
    protected String REPRESENTATION_SI = SI_BASE+"Representation";
    protected String REPRESENTATION_TOPIC_SI = SI_BASE+"Representation topic";
    protected String TOPIC_SI = SI_BASE+"Topic";
    
   
    protected String SUBJ3CT_TYPE_SI = SI_BASE;

    private String defaultEncoding = "UTF-8";



    
    /** Creates a new instance of XMLSubj3ctRecordExtractor */
    public XMLSubj3ctRecordExtractor() {
    }
    

    @Override
    public String getName() {
        return "Subj3ct record XML extractor";
    }
    
    @Override
    public String getDescription(){
        return "Converts subject records of Networked Planet's Subj3ct service to topics maps.";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_subj3ct.png");
    }


    private final String[] contentTypes=new String[] { "text/xml", "application/xml" };

    
    
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }

    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String in = IObox.doUrl(url);
        return _extractTopicsFrom(in, topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception { 
        String str = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(str, topicMap);
    }
    
    
    public boolean _extractTopicsFrom(String in, TopicMap topicMap) throws Exception {        
        try {
            String result = in;
            //System.out.println("Result = "+result);

            // ---- Parse results ----
            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            Subj3ctResultParser parserHandler = new Subj3ctResultParser(topicMap, this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            try{
                reader.parse(new InputSource(new ByteArrayInputStream(result.getBytes(defaultEncoding))));
            }
            catch(Exception e){
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }

            String msg = null;
            if(parserHandler.progress == 0) {
                msg = "Found no subject records.";
            }
            else {
                msg = "Found "+parserHandler.progress+" subject records(s).";
            }
            if(msg != null) log(msg);
        }
        catch (Exception ex) {
           log(ex);
        }
        return true;
    }
    
    

    
    public Topic getProvenanceTopic(String provinance, TopicMap tm) throws TopicMapException {
        if(provinance != null) {
            provinance = provinance.trim();
            if(provinance.length() > 0) {
                Topic provinanceTopic=getOrCreateTopic(tm, provinance);
                Topic provinanceTypeTopic = getProvenanceType(tm);
                provinanceTopic.addType(provinanceTypeTopic);
                return provinanceTopic;
            }
        }
        return null;
    }

    

    
    public Topic getProvenanceType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PROVENANCE_SI, "Subj3ct provenance");
        Topic subj3ctClass = getSubj3ctClass(tm);
        makeSubclassOf(tm, type, subj3ctClass);
        return type;
    }
    
    
    
    public Topic getTrustTopic(String trust, TopicMap tm) throws TopicMapException {
        if(trust != null) {
            trust = trust.trim();
            if(trust.length() > 0) {
                Topic trustTopic=getOrCreateTopic(tm, TRUST_SI+"/"+trust, trust);
                Topic trustTypeTopic = getTrustType(tm);
                trustTopic.addType(trustTypeTopic);
                return trustTopic;
            }
        }
        return null;
    }
    
    
    public Topic getTrustType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TRUST_SI, "Subj3ct trust");
        Topic subj3ctClass = getSubj3ctClass(tm);
        makeSubclassOf(tm, type, subj3ctClass);
        return type;
    }

    
    
    public Topic getDescriptionTopic(String description, TopicMap tm) throws TopicMapException {
        if(description != null) {
            description = description.trim();
            if(description.length() > 0) {
                String niceDescription = description.replace("\n", " ");
                niceDescription = niceDescription.replace("\r", " ");
                niceDescription = niceDescription.replace("\t", " ");
                if(niceDescription.length() > 256) niceDescription = niceDescription.substring(0, 255);
                Topic descriptionTopic=getOrCreateTopic(tm, TopicTools.createDefaultLocator().toExternalForm(), niceDescription);
                Topic descriptionTypeTopic = getDescriptionType(tm);
                Topic langTopic = tm.getTopic(XTMPSI.getLang("en"));
                descriptionTopic.setData(descriptionTypeTopic, langTopic, description);
                descriptionTopic.addType(descriptionTypeTopic);
                return descriptionTopic;
            }
        }
        return null;
    }
    
    
    public Topic getDescriptionType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DESCRIPTION_SI, "Subj3ct description");
        Topic subj3ctClass = getSubj3ctClass(tm);
        makeSubclassOf(tm, type, subj3ctClass);
        return type;
    }

    public Topic getRecordlinkTopic(String recordlink, TopicMap tm) throws TopicMapException {
        if(recordlink != null) {
            recordlink = recordlink.trim();
            if(recordlink.length() > 0) {
                Topic recordlinkTopic=getOrCreateTopic(tm, recordlink);
                Topic recordlinkTypeTopic = getRecordlinkType(tm);
                recordlinkTopic.addType(recordlinkTypeTopic);
                return recordlinkTopic;
            }
        }
        return null;
    }

    public Topic getRecordlinkType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, RECORDLINK_SI, "Subj3ct recordlink");
        Topic subj3ctClass = getSubj3ctClass(tm);
        makeSubclassOf(tm, type, subj3ctClass);
        return type;
    }

    public Topic getTopicType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TOPIC_SI, "Subj3ct topic");
        Topic subj3ctClass = getSubj3ctClass(tm);
        makeSubclassOf(tm, type, subj3ctClass);
        return type;
    }

    public Topic getSubj3ctType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SUBJ3CT_TYPE_SI, "Subj3ct record");
        return type;
    }
    
    public Topic getEquivalenceType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EQUIVALENCE_SI, "Equivalence");
        return type;
    }
    
    public Topic getEquivalentTopicType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EQUIVALENT_TOPIC_SI, "Equivalent topic");
        return type;
    }
    
    public Topic getRepresentationType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, REPRESENTATION_SI, "Representation");
        return type;
    }
    
    public Topic getRepresentationTopicType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, REPRESENTATION_TOPIC_SI, "Representation topic");
        return type;
    }

    public Topic getSubj3ctClass(TopicMap tm) throws TopicMapException {
        Topic s = getOrCreateTopic(tm, SUBJ3CT,"Subj3ct");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, s, w);
        return s;
    }


    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    
    // --------
    
    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    
    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private class Subj3ctResultParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        
        public Subj3ctResultParser(TopicMap tm, XMLSubj3ctRecordExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        
        
        public int progress=0;
        private TopicMap tm;
        private XMLSubj3ctRecordExtractor parent;
        
        public static final String TAG_SERVICEEXCEPTION = "ServiceException";
        public static final String TAG_CODE = "Code";
        public static final String TAG_MESSAGE = "Message";

        public static final String TAG_SEARCHRESULT = "SearchResult";
        public static final String TAG_QUERY = "Query";
        public static final String TAG_TAKEN = "Taken";
        public static final String TAG_SKIPPED = "Skipped";
        public static final String TAG_TOTALNUMBERRESULTS = "TotalNumberResults";
        public static final String TAG_SUBJECTS = "Subjects";

        public static final String TAG_SUBJECT = "Subject";
        public static final String TAG_IDENTIFIER = "Identifier";
        public static final String TAG_PROVENANCE = "Provenance";
        public static final String TAG_NAME = "Name";
        public static final String TAG_DESCRIPTION = "Description";
        public static final String TAG_RECORDLINK = "RecordLink";
        public static final String TAG_TRUST = "Trust";
        public static final String TAG_EQUIVALENCESTATEMENTS = "EquivalenceStatements";
        public static final String TAG_EQUIVALENCESTATEMENT = "EquivalenceStatement";
        public static final String TAG_REPRESENTATIONSTATEMENTS = "RepresentationStatements";
        public static final String TAG_REPRESENTATIONSTATEMENT = "RepresentationStatement";
        public static final String TAG_EQUIVALENTIDENTIFIER  = "EquivalentIdentifier";
        public static final String TAG_REPRESENTATIONURI  = "RepresentationUri";
        
        private static final int STATE_START=0;
        private static final int STATE_SUBJECT=2;
        private static final int STATE_SUBJECT_IDENTIFIER=4;
        private static final int STATE_SUBJECT_PROVENANCE=5;
        private static final int STATE_SUBJECT_NAME=6;
        private static final int STATE_SUBJECT_DESCRIPTION=7;
        private static final int STATE_SUBJECT_RECORDLINK=8;
        private static final int STATE_SUBJECT_TRUST=9;
        
        private static final int STATE_SUBJECT_EQUIVALENCESTATEMENTS=20;
        private static final int STATE_SUBJECT_EQUIVALENCESTATEMENT=21;
        private static final int STATE_SUBJECT_EQUIVALENCESTATEMENT_EQUIVALENTIDENTIFIER=22;
        private static final int STATE_SUBJECT_EQUIVALENCESTATEMENT_PROVENANCE=23;
        private static final int STATE_SUBJECT_EQUIVALENCESTATEMENT_TRUST=24;
        
        private static final int STATE_SUBJECT_REPRESENTATIONSTATEMENTS=30;
        private static final int STATE_SUBJECT_REPRESENTATIONSTATEMENT=31;
        private static final int STATE_SUBJECT_REPRESENTATIONSTATEMENT_REPRESENTATIONURI=32;
        private static final int STATE_SUBJECT_REPRESENTATIONSTATEMENT_PROVENANCE=33;
        private static final int STATE_SUBJECT_REPRESENTATIONSTATEMENT_TRUST=34;

        private static final int STATE_SERVICEEXCEPTION = 100;
        private static final int STATE_SERVICEEXCEPTION_CODE = 101;
        private static final int STATE_SERVICEEXCEPTION_MESSAGE = 102;

        private static final int STATE_SEARCHRESULT = 50;
        private static final int STATE_SEARCHRESULT_QUERY = 51;
        private static final int STATE_SEARCHRESULT_TOTALNUMBERRESULTS = 52;
        private static final int STATE_SEARCHRESULT_SKIPPED = 53;
        private static final int STATE_SEARCHRESULT_TAKEN = 54;
        private static final int STATE_SEARCHRESULT_SUBJECTS = 55;

        private int state=STATE_START;

        private String data_message = null; // ServiceException

        private String data_subject_identifier = null;
        private String data_subject_provenance = null;
        private String data_subject_name = null;
        private String data_subject_description = null;
        private String data_subject_recordlink = null;
        private String data_subject_trust = null;
        
        private String data_equivalence_identifier = null;
        private String data_equivalence_provenance = null;
        private String data_equivalence_trust = null;
        
        private String data_representation_uri = null;
        private String data_representation_provenance = null;
        private String data_representation_trust = null;
        
        private ArrayList<T3<String,String,String>> data_equivalences = null;
        private ArrayList<T3<String,String,String>> data_representations = null;
        
        private boolean isQueryResult = false;

        private String data_searchresult_query = null;
        private String data_searchresult_totalnumberresults = null;
        private String data_searchresult_skipped = null;
        private String data_searchresult_taken = null;


        

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
                    if(qName.equals(TAG_SEARCHRESULT)) {
                        isQueryResult = true;
                        data_searchresult_query = "";
                        data_searchresult_totalnumberresults = "";
                        data_searchresult_skipped = "";
                        data_searchresult_taken = "";
                        state = STATE_SEARCHRESULT;
                    }
                    else if(qName.equals(TAG_SUBJECT)) {
                        isQueryResult = false;
                        data_subject_identifier = "";
                        data_subject_provenance = "";
                        data_subject_name = "";
                        data_subject_description = "";
                        data_subject_recordlink = "";
                        data_subject_trust = "";
                        data_equivalences = new ArrayList<T3<String,String,String>>();
                        data_representations = new ArrayList<T3<String,String,String>>();
                        state = STATE_SUBJECT;
                    }
                    else if(qName.equals(TAG_SERVICEEXCEPTION)) {
                        data_message = "";
                        state = STATE_SERVICEEXCEPTION;
                    }
                    break;
                case STATE_SEARCHRESULT:
                    if(qName.equals(TAG_QUERY)) {
                        data_searchresult_query = "";
                        state = STATE_SEARCHRESULT_QUERY;
                    }
                    else if(qName.equals(TAG_TOTALNUMBERRESULTS)) {
                        data_searchresult_totalnumberresults = "";
                        state = STATE_SEARCHRESULT_TOTALNUMBERRESULTS;
                    }
                    else if(qName.equals(TAG_SKIPPED)) {
                        data_searchresult_skipped = "";
                        state = STATE_SEARCHRESULT_SKIPPED;
                    }
                    else if(qName.equals(TAG_TAKEN)) {
                        data_searchresult_taken = "";
                        state = STATE_SEARCHRESULT_TAKEN;
                    }
                    else if(qName.equals(TAG_SUBJECTS)) {
                        state = STATE_SEARCHRESULT_SUBJECTS;
                    }
                    break;
                case STATE_SERVICEEXCEPTION:
                    if(qName.equals(TAG_CODE)) {
                        state = STATE_SERVICEEXCEPTION_CODE;
                    }
                    else if(qName.equals(TAG_MESSAGE)) {
                        data_message = "";
                        state = STATE_SERVICEEXCEPTION_MESSAGE;
                    }
                    break;
                case STATE_SEARCHRESULT_SUBJECTS:
                    if(qName.equals(TAG_SUBJECT)) {
                        data_subject_identifier = "";
                        data_subject_provenance = "";
                        data_subject_name = "";
                        data_subject_description = "";
                        data_subject_recordlink = "";
                        data_subject_trust = "";
                        data_equivalences = new ArrayList<T3<String,String,String>>();
                        data_representations = new ArrayList<T3<String,String,String>>();
                        state = STATE_SUBJECT;
                    }
                    break;
                case STATE_SUBJECT:
                    if(qName.equals(TAG_IDENTIFIER)) {
                        data_subject_identifier = "";
                        state = STATE_SUBJECT_IDENTIFIER;
                    }
                    else if(qName.equals(TAG_PROVENANCE)) {
                        data_subject_provenance = "";
                        state = STATE_SUBJECT_PROVENANCE;
                    }
                    else if(qName.equals(TAG_NAME)) {
                        data_subject_name = "";
                        state = STATE_SUBJECT_NAME;
                    }
                    else if(qName.equals(TAG_DESCRIPTION)) {
                        data_subject_description = "";
                        state = STATE_SUBJECT_DESCRIPTION;
                    }
                    else if(qName.equals(TAG_RECORDLINK)) {
                        data_subject_recordlink = "";
                        state = STATE_SUBJECT_RECORDLINK;
                    }
                    else if(qName.equals(TAG_TRUST)) {
                        data_subject_trust = "";
                        state = STATE_SUBJECT_TRUST;
                    }
                    else if(qName.equals(TAG_EQUIVALENCESTATEMENTS)) {
                        state = STATE_SUBJECT_EQUIVALENCESTATEMENTS;
                    }
                    else if(qName.equals(TAG_REPRESENTATIONSTATEMENTS)) {
                        state = STATE_SUBJECT_REPRESENTATIONSTATEMENTS;
                    }
                    break;
                case STATE_SUBJECT_EQUIVALENCESTATEMENTS:
                    if(qName.equals(TAG_EQUIVALENCESTATEMENT)) {
                        data_equivalence_identifier = "";
                        data_equivalence_provenance = "";
                        data_equivalence_trust = "";
                        state = STATE_SUBJECT_EQUIVALENCESTATEMENT;
                    }
                    break;
                case STATE_SUBJECT_EQUIVALENCESTATEMENT:
                    if(qName.equals(TAG_EQUIVALENTIDENTIFIER)) {
                        data_equivalence_identifier = "";
                        state = STATE_SUBJECT_EQUIVALENCESTATEMENT_EQUIVALENTIDENTIFIER;
                    }
                    else if(qName.equals(TAG_PROVENANCE)) {
                        data_equivalence_provenance = "";
                        state = STATE_SUBJECT_EQUIVALENCESTATEMENT_PROVENANCE;
                    }
                    else if(qName.equals(TAG_TRUST)) {
                        data_equivalence_trust = "";
                        state = STATE_SUBJECT_EQUIVALENCESTATEMENT_TRUST;
                    }
                    break;
                    
                case STATE_SUBJECT_REPRESENTATIONSTATEMENTS:
                    if(qName.equals(TAG_REPRESENTATIONSTATEMENT)) {
                        data_representation_uri = "";
                        data_representation_provenance = "";
                        data_representation_trust = "";
                        state = STATE_SUBJECT_REPRESENTATIONSTATEMENT;
                    }
                    break;
                    
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT:
                    if(qName.equals(TAG_REPRESENTATIONURI)) {
                        data_representation_uri = "";
                        state = STATE_SUBJECT_REPRESENTATIONSTATEMENT_REPRESENTATIONURI;
                    }
                    else if(qName.equals(TAG_PROVENANCE)) {
                        data_representation_provenance = "";
                        state = STATE_SUBJECT_REPRESENTATIONSTATEMENT_PROVENANCE;
                    }
                    else if(qName.equals(TAG_TRUST)) {
                        data_representation_trust = "";
                        state = STATE_SUBJECT_REPRESENTATIONSTATEMENT_TRUST;
                    }
                    break;
            }
        }
        
        
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_SERVICEEXCEPTION_CODE: {
                    if(qName.equals(TAG_CODE)) {
                        state = STATE_SERVICEEXCEPTION;
                    }
                    break;
                }
                case STATE_SERVICEEXCEPTION_MESSAGE: {
                    if(qName.equals(TAG_MESSAGE)) {
                        state = STATE_SERVICEEXCEPTION;
                    }
                    break;
                }
                case STATE_SERVICEEXCEPTION: {
                    if(qName.equals(TAG_SERVICEEXCEPTION)) {
                        parent.log("Service exception occurred in Subj3ct:\n"+data_message);
                        state = STATE_START;
                    }
                    break;
                }

                case STATE_SEARCHRESULT_QUERY: {
                    if(qName.equals(TAG_QUERY)) {
                        state = STATE_SEARCHRESULT;
                    }
                    break;
                }
                case STATE_SEARCHRESULT_TOTALNUMBERRESULTS: {
                    if(qName.equals(TAG_TOTALNUMBERRESULTS)) {
                        state = STATE_SEARCHRESULT;
                    }
                    break;
                }
                case STATE_SEARCHRESULT_SKIPPED: {
                    if(qName.equals(TAG_SKIPPED)) {
                        state = STATE_SEARCHRESULT;
                    }
                    break;
                }
                case STATE_SEARCHRESULT_TAKEN: {
                    if(qName.equals(TAG_TAKEN)) {
                        state = STATE_SEARCHRESULT;
                    }
                    break;
                }

                case STATE_SEARCHRESULT: {
                    if(qName.equals(TAG_SEARCHRESULT)) {
                        if(data_searchresult_taken != null) {
                            try {
                                int taken = Integer.parseInt(data_searchresult_taken);
                                if(taken != progress) {
                                    parent.log("Warning: Search results contain "+taken+" subjects. Prosessed "+progress+" subjects.");
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_START;
                    }
                    break;
                }

                case STATE_SEARCHRESULT_SUBJECTS: {
                    if(qName.equals(TAG_SUBJECTS)) {
                        state = STATE_SEARCHRESULT;
                    }
                    break;
                }

                case STATE_SUBJECT: {
                    if(qName.equals(TAG_SUBJECT)) {
                        parent.setProgress(progress++);
                        try {
                            if(data_subject_identifier.length() > 0) {
                                data_subject_identifier = URLDecoder.decode(data_subject_identifier, defaultEncoding);
                                Topic topic = parent.getOrCreateTopic(tm, data_subject_identifier);
                                data_subject_name = data_subject_name.trim();
                                if(data_subject_name.length() > 0) {
                                    if(topic.getBaseName() == null || parent.OVERRIDE_BASENAME) {
                                        topic.setBaseName(data_subject_name);
                                    }
                                }
                                Topic topicTypeTopic = parent.getTopicType(tm);
                                if(topicTypeTopic != null) {
                                    topic.addType(topicTypeTopic);
                                }

                                data_subject_description = data_subject_description.trim();
                                if(DESCRIPTION_AS_OCCURRENCE && data_subject_description != null && data_subject_description.length() > 0) {
                                    Topic descriptionType = parent.getDescriptionType(tm);
                                    Topic langTopic = tm.getTopic(XTMPSI.getLang("en"));
                                    if(descriptionType != null && langTopic != null) {
                                        topic.setData(descriptionType, langTopic, data_subject_description);
                                    }
                                }

                                if(data_subject_provenance.length() > 0 || data_subject_recordlink.length() > 0 || data_subject_trust.length() > 0 || (DESCRIPTION_AS_PLAYER && data_subject_description.length() > 0)) {
                                    Topic subj3ctTypeTopic = parent.getSubj3ctType(tm);
                                    Association a = tm.createAssociation(subj3ctTypeTopic);
                                    a.addPlayer(topic, topicTypeTopic);
                                    if(data_subject_provenance.length() > 0) {
                                        data_subject_provenance = URLDecoder.decode(data_subject_provenance, defaultEncoding);
                                        Topic provenanceTopic = parent.getProvenanceTopic(data_subject_provenance, tm);
                                        Topic provenanceTypeTopic = parent.getProvenanceType(tm);
                                        a.addPlayer(provenanceTopic, provenanceTypeTopic);
                                    }
                                    if(data_subject_recordlink.length() > 0) {
                                        data_subject_recordlink = URLDecoder.decode(data_subject_recordlink, defaultEncoding);
                                        Topic recordlinkTopic = parent.getRecordlinkTopic(data_subject_recordlink, tm);
                                        Topic recordlinkType = parent.getRecordlinkType(tm);
                                        a.addPlayer(recordlinkTopic, recordlinkType);
                                    }
                                    if(data_subject_trust.length() > 0) {
                                        Topic trustTopic = parent.getTrustTopic(data_subject_trust, tm);
                                        Topic trustType = parent.getTrustType(tm);
                                        a.addPlayer(trustTopic, trustType);
                                    }
                                    if(DESCRIPTION_AS_PLAYER && data_subject_description.length() > 0) {
                                        Topic descriptionType = parent.getDescriptionType(tm);
                                        Topic descriptionTopic = parent.getDescriptionTopic(data_subject_description, tm);
                                        a.addPlayer(descriptionTopic, descriptionType);
                                    }
                                }
                                
                                if(data_equivalences != null) {
                                    if(data_equivalences.size() > 0) {
                                        for(T3<String,String,String> equivalence : data_equivalences) {
                                            if(equivalence != null) {
                                                String eidentifier = equivalence.e1;
                                                String eprovenance = equivalence.e2;
                                                String etrust = equivalence.e3;
                                                
                                                if(eidentifier != null && eidentifier.length() > 0) {
                                                    eidentifier = URLDecoder.decode(eidentifier, defaultEncoding);
                                                    Topic equivalenceType = parent.getEquivalenceType(tm);
                                                    Topic equivalentTopicType = parent.getEquivalentTopicType(tm);
                                                    Topic equivalentTopic = parent.getOrCreateTopic(tm, eidentifier);
                                                                                                      
                                                    Association eassociation = tm.createAssociation(equivalenceType);
                                                    eassociation.addPlayer(topic, topicTypeTopic);
                                                    eassociation.addPlayer(equivalentTopic, equivalentTopicType);
                                                    
                                                    if(eprovenance != null && eprovenance.length() > 0) {
                                                        eprovenance = URLDecoder.decode(eprovenance, defaultEncoding);
                                                        Topic provenanceTopic = parent.getProvenanceTopic(eprovenance, tm);
                                                        Topic provenanceTypeTopic = parent.getProvenanceType(tm);
                                                        eassociation.addPlayer(provenanceTopic, provenanceTypeTopic);
                                                    }
                                                    if(etrust != null && etrust.length() > 0) {
                                                        Topic trustTopic = parent.getTrustTopic(etrust, tm);
                                                        Topic trustTypeTopic = parent.getTrustType(tm);
                                                        eassociation.addPlayer(trustTopic, trustTypeTopic);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                if(data_representations != null) {
                                    if(data_representations.size() > 0) {
                                        for(T3<String,String,String> representation : data_representations) {
                                            if(representation != null) {
                                                String ruri = representation.e1;
                                                String rprovenance = representation.e2;
                                                String rtrust = representation.e3;
                                                
                                                if(ruri != null && ruri.length() > 0) {
                                                    ruri = URLDecoder.decode(ruri, defaultEncoding);
                                                    Topic representationType = parent.getRepresentationType(tm);
                                                    Topic representationTopicType = parent.getRepresentationTopicType(tm);
                                                    Topic representationTopic = parent.getOrCreateTopic(tm, ruri);
                                                    
                                                    topicTypeTopic = parent.getTopicType(tm);
                                                    
                                                    Association rassociation = tm.createAssociation(representationType);
                                                    rassociation.addPlayer(topic, topicTypeTopic);
                                                    rassociation.addPlayer(representationTopic, representationTopicType);
                                                    
                                                    if(rprovenance != null && rprovenance.length() > 0) {
                                                        rprovenance = URLDecoder.decode(rprovenance, defaultEncoding);
                                                        Topic provenanceTopic = parent.getProvenanceTopic(rprovenance, tm);
                                                        Topic provenanceTypeTopic = parent.getProvenanceType(tm);
                                                        rassociation.addPlayer(provenanceTopic, provenanceTypeTopic);
                                                    }
                                                    if(rtrust != null && rtrust.length() > 0) {
                                                        Topic trustTopic = parent.getTrustTopic(rtrust, tm);
                                                        Topic trustTypeTopic = parent.getTrustType(tm);
                                                        rassociation.addPlayer(trustTopic, trustTypeTopic);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        if(isQueryResult)
                            state = STATE_SEARCHRESULT_SUBJECTS;
                        else
                            state = STATE_START;
                    }
                    break;
                }
                case STATE_SUBJECT_IDENTIFIER: {
                    if(qName.equals(TAG_IDENTIFIER)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_PROVENANCE: {
                    if(qName.equals(TAG_PROVENANCE)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_DESCRIPTION: {
                    if(qName.equals(TAG_DESCRIPTION)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_RECORDLINK: {
                    if(qName.equals(TAG_RECORDLINK)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_TRUST: {
                    if(qName.equals(TAG_TRUST)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENTS: {
                    if(qName.equals(TAG_EQUIVALENCESTATEMENTS)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_REPRESENTATIONSTATEMENTS: {
                    if(qName.equals(TAG_REPRESENTATIONSTATEMENTS)) {
                        state=STATE_SUBJECT;
                    }
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENT: {
                    if(qName.equals(TAG_EQUIVALENCESTATEMENT)) {
                        data_equivalences.add(new T3(data_equivalence_identifier, data_equivalence_provenance, data_equivalence_trust));
                        state=STATE_SUBJECT_EQUIVALENCESTATEMENTS;
                    }
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENT_EQUIVALENTIDENTIFIER: {
                    if(qName.equals(TAG_EQUIVALENTIDENTIFIER)) {
                        state=STATE_SUBJECT_EQUIVALENCESTATEMENT;
                    }
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENT_PROVENANCE: {
                    if(qName.equals(TAG_PROVENANCE)) {
                        state=STATE_SUBJECT_EQUIVALENCESTATEMENT;
                    }
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENT_TRUST: {
                    if(qName.equals(TAG_TRUST)) {
                        state=STATE_SUBJECT_EQUIVALENCESTATEMENT;
                    }
                    break;
                }
                
                
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT: {
                    if(qName.equals(TAG_REPRESENTATIONSTATEMENT)) {
                        data_representations.add(new T3(data_representation_uri, data_representation_provenance, data_representation_trust));
                        state=STATE_SUBJECT_REPRESENTATIONSTATEMENTS;
                    }
                    break;
                }
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT_REPRESENTATIONURI: {
                    if(qName.equals(TAG_REPRESENTATIONURI)) {
                        state=STATE_SUBJECT_REPRESENTATIONSTATEMENT;
                    }
                    break;
                }
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT_PROVENANCE: {
                    if(qName.equals(TAG_PROVENANCE)) {
                        state=STATE_SUBJECT_REPRESENTATIONSTATEMENT;
                    }
                    break;
                }
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT_TRUST: {
                    if(qName.equals(TAG_TRUST)) {
                        state=STATE_SUBJECT_REPRESENTATIONSTATEMENT;
                    }
                    break;
                }
            }
        }
        
        
        
        
        
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state) {
                case STATE_SEARCHRESULT_QUERY: {
                    data_searchresult_query += new String(ch,start,length);
                    break;
                }
                case STATE_SEARCHRESULT_TOTALNUMBERRESULTS: {
                    data_searchresult_totalnumberresults += new String(ch,start,length);
                    break;
                }
                case STATE_SEARCHRESULT_SKIPPED: {
                    data_searchresult_skipped += new String(ch,start,length);
                    break;
                }
                case STATE_SEARCHRESULT_TAKEN: {
                    data_searchresult_taken += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_IDENTIFIER: {
                    data_subject_identifier += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_PROVENANCE: {
                    data_subject_provenance += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_NAME: {
                    data_subject_name += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_DESCRIPTION: {
                    data_subject_description += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_RECORDLINK: {
                    data_subject_recordlink += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_TRUST: {
                    data_subject_trust += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENT_EQUIVALENTIDENTIFIER: {
                    data_equivalence_identifier += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENT_PROVENANCE: {
                    data_equivalence_provenance += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_EQUIVALENCESTATEMENT_TRUST: {
                    data_equivalence_trust += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT_REPRESENTATIONURI: {
                    data_representation_uri += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT_PROVENANCE: {
                    data_representation_provenance += new String(ch,start,length);
                    break;
                }
                case STATE_SUBJECT_REPRESENTATIONSTATEMENT_TRUST: {
                    data_representation_trust += new String(ch,start,length);
                    break;
                }
                default: 
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
