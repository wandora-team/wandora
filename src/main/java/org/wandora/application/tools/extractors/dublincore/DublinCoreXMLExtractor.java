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
 * DublinCoreXMLExtractor.java
 *
 * Created on 2010-06-30
 *
 */


package org.wandora.application.tools.extractors.dublincore;


import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;

import java.io.*;
import javax.swing.*;

import java.net.*;
import org.xml.sax.*;




/**
 * Converts simple Dublin Core XML feed to a topic map.
 *
 * Unsupported XML elements:
 * 
 * isVersionOf, hasVersion, isReplacedBy, replaces, isRequiredBy,
 * requires, isPartOf, hasPart, isReferencedBy, references,
 * isFormatOf, hasFormat, conformsTo, spatial, temporal, mediator,
 * dateAccepted, dateCopyrighted, dateSubmitted, educationLevel,
 * accessRights, bibliographicCitation, license, rightsHolder,
 * provenance, instructionalMethod, accrualMethod, accrualPeriodicity,
 * accrualPolicy, Agent, AgentClass, BibliographicResource, FileFormat,
 * Frequency, Jurisdiction, LicenseDocument, LinguisticSystem, Location,
 * LocationPeriodOrJurisdiction
 *
 * Also, XML element attribute support is very narrow.
 *
 * @author akivela
 *
 *
 */

public class DublinCoreXMLExtractor extends AbstractExtractor {
	
	private static final long serialVersionUID = 1L;

	public static boolean IDENTIFIER_AS_SI = true;

    public static boolean DESCRIPTION_AS_PLAYER = false;
    public static boolean DESCRIPTION_AS_OCCURRENCE = true;
    public static boolean APPEND_OCCURRENCE_DESCRIPTION = true;

    public static boolean APPEND_OCCURRENCE_ALTERNATIVE = true;
    public static boolean APPEND_OCCURRENCE_TABLEOFCONTENTS = true;
    public static boolean APPEND_OCCURRENCE_ABSTRACT = true;

    

    protected String SI_BASE = "http://purl.org/dc/elements/1.1/";

    protected String RECORD_SI = SI_BASE + "record";
    protected String CREATOR_SI = SI_BASE + "creator";
    protected String TYPE_SI = SI_BASE + "type";
    protected String PUBLISHER_SI = SI_BASE + "publisher";
    protected String CONTRIBUTOR_SI = SI_BASE + "contributor";
    protected String DATE_SI = SI_BASE + "date";
    protected String DESCRIPTION_SI = SI_BASE + "description";
    protected String LANGUAGE_SI = SI_BASE + "language";
    protected String SUBJECT_SI = SI_BASE + "subject";
    protected String IDENTIFIER_SI = SI_BASE + "identifier";
    protected String FORMAT_SI = SI_BASE + "format";


    protected String SOURCE_SI = SI_BASE + "source";
    protected String RELATION_SI = SI_BASE + "relation";
    protected String COVERAGE_SI = SI_BASE + "coverage";
    protected String RIGHTS_SI = SI_BASE + "rights";
    protected String AUDIENCE_SI = SI_BASE + "audience";
    protected String ALTERNATIVE_SI = SI_BASE + "alternative";

    protected String TABLEOFCONTENTS_SI = SI_BASE + "tableOfContents";
    protected String ABSTRACT_SI = SI_BASE + "abstract";
    protected String CREATED_SI = SI_BASE + "created";
    protected String VALID_SI = SI_BASE + "valid";
    protected String AVAILABLE_SI = SI_BASE + "available";
    protected String ISSUED_SI = SI_BASE + "issued";
    protected String MODIFIED_SI = SI_BASE + "modified";
    protected String EXTENT_SI = SI_BASE + "extent";
    protected String MEDIUM_SI = SI_BASE + "medium";








    private String defaultEncoding = "ISO-8859-1";




    /** Creates a new instance of DublinCoreXMLExtractor */
    public DublinCoreXMLExtractor() {
    }


    @Override
    public String getName() {
        return "Simple Dublin Core XML extractor";
    }

    @Override
    public String getDescription(){
        return "Converts simple Dublin Core XML documents to topics maps.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_dc.png");
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


    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String in = IObox.doUrl(url);
        return _extractTopicsFrom(in, topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String str = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(str, topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(String in, TopicMap topicMap) throws Exception {
        try {
            String result = in;
            //System.out.println("Result = "+result);

            // ---- Parse results ----
            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            DublinCoreXMLParser parserHandler = new DublinCoreXMLParser(topicMap, this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            try{
                reader.parse(new InputSource(new ByteArrayInputStream(result.getBytes())));
            }
            catch(Exception e){
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }

            String msg = null;
            if(parserHandler.progress == 0) {
                msg = "Found no records.";
            }
            else {
                msg = "Found "+parserHandler.progress+" records(s).";
            }
            if(msg != null) log(msg);
        }
        catch (Exception ex) {
           log(ex);
        }
        return true;
    }



    // -------------------------------------------------------------------------



    

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
        return getOrCreateTopic(tm, DESCRIPTION_SI, "Description (DC)", getDCClass(tm));
    }

    // ---

    public Topic getIdentifierTopic(String identifier, TopicMap tm) throws TopicMapException {
        if(identifier != null) {
            identifier = identifier.trim();
            if(identifier.length() > 0) {
                Topic identifierTopic=getOrCreateTopic(tm, identifier, identifier, getIdentifierType(tm));
                return identifierTopic;
            }
        }
        return null;
    }

    public Topic getIdentifierType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, IDENTIFIER_SI, "Identifier (DC)", getDCClass(tm));
    }


    // ---

    public Topic getSubjectTopic(String subject, TopicMap tm) throws TopicMapException {
        return getTopic(tm, subject, SUBJECT_SI, getSubjectType(tm));
    }

    public Topic getSubjectType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SUBJECT_SI, "Subject (DC)", getDCClass(tm));
    }


    // ---

    public Topic getTypeTopic(String type, TopicMap tm) throws TopicMapException {
        return getTopic(tm, type, TYPE_SI, getTypeType(tm));
    }

    public Topic getTypeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TYPE_SI, "Type (DC)", getDCClass(tm));
    }


    // ---


    public Topic getFormatTopic(String format, TopicMap tm) throws TopicMapException {
        return getTopic(tm, format, FORMAT_SI, getFormatType(tm));
    }

    public Topic getFormatType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, FORMAT_SI, "Format (DC)", getDCClass(tm));
    }


    // ---



     public Topic getDateTopic(String date, TopicMap tm) throws TopicMapException {
        return getTopic(tm, date, DATE_SI, getDateType(tm));
    }

    public Topic getDateType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, DATE_SI, "Date (DC)", getDCClass(tm));
    }


    // ---



     public Topic getPublisherTopic(String publisher, TopicMap tm) throws TopicMapException {
        return getTopic(tm, publisher, PUBLISHER_SI, getPublisherType(tm));
    }

    public Topic getPublisherType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, PUBLISHER_SI, "Publisher (DC)", getDCClass(tm));
    }


    // ---



    public Topic getContributorTopic(String contributor, TopicMap tm) throws TopicMapException {
        return getTopic(tm, contributor, CONTRIBUTOR_SI, getContributorType(tm));
    }

    public Topic getContributorType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, CONTRIBUTOR_SI, "Contributor (DC)", getDCClass(tm));
    }


    // ---



    public Topic getLanguageTopic(String language, TopicMap tm) throws TopicMapException {
        return getTopic(tm, language, LANGUAGE_SI, getLanguageType(tm));
    }

    public Topic getLanguageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, LANGUAGE_SI, "Language (DC)", getDCClass(tm));
    }

    // ----

    public Topic getCreatorTopic(String creator, TopicMap tm) throws TopicMapException {
        return getTopic(tm, creator, CREATOR_SI, getCreatorType(tm));
    }

    public Topic getCreatorType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, CREATOR_SI, "Creator (DC)", getDCClass(tm));
    }


// ----

    public Topic getSourceTopic(String source, TopicMap tm) throws TopicMapException {
        return getTopic(tm, source, SOURCE_SI, getSourceType(tm));
    }

    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source (DC)", getDCClass(tm));
    }


    // ----

    public Topic getRelationTopic(String relation, TopicMap tm) throws TopicMapException {
        return getTopic(tm, relation, RELATION_SI, getRelationType(tm));
    }

    public Topic getRelationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, RELATION_SI, "Relation (DC)", getDCClass(tm));
    }


    // ----

    public Topic getCoverageTopic(String coverage, TopicMap tm) throws TopicMapException {
        return getTopic(tm, coverage, COVERAGE_SI, getCoverageType(tm));
    }

    public Topic getCoverageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, COVERAGE_SI, "Coverage (DC)", getDCClass(tm));
    }


    // ----

    public Topic getRightsTopic(String rights, TopicMap tm) throws TopicMapException {
        return getTopic(tm, rights, RIGHTS_SI, getRightsType(tm));
    }

    public Topic getRightsType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, RIGHTS_SI, "Rights (DC)", getDCClass(tm));
    }


    // ----

    public Topic getAudienceTopic(String audience, TopicMap tm) throws TopicMapException {
        return getTopic(tm, audience, AUDIENCE_SI, getAudienceType(tm));
    }

    public Topic getAudienceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, AUDIENCE_SI, "Audience (DC)", getDCClass(tm));
    }


    // ----

    public Topic getAlternativeTopic(String alt, TopicMap tm) throws TopicMapException {
        return getTopic(tm, alt, ALTERNATIVE_SI, getAlternativeType(tm));
    }

    public Topic getAlternativeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALTERNATIVE_SI, "Alternative (DC)", getDCClass(tm));
    }



    // ----


    public Topic getTableOfContentsTopic(String toc, TopicMap tm) throws TopicMapException {
        return getTopic(tm, toc, TABLEOFCONTENTS_SI, getTableOfContentsType(tm));
    }

    public Topic getTableOfContentsType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TABLEOFCONTENTS_SI, "TableOfContents (DC)", getDCClass(tm));
    }

    // ----


    public Topic getAbstractTopic(String abst, TopicMap tm) throws TopicMapException {
        return getTopic(tm, abst, ABSTRACT_SI, getAbstractType(tm));
    }

    public Topic getAbstractType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ABSTRACT_SI, "Abstract (DC)", getDCClass(tm));
    }


    // ----


    public Topic getCreatedTopic(String created, TopicMap tm) throws TopicMapException {
        return getTopic(tm, created, CREATED_SI, getCreatedType(tm));
    }

    public Topic getCreatedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, CREATED_SI, "Created (DC)", getDCClass(tm));
    }

    // ----


    public Topic getValidTopic(String valid, TopicMap tm) throws TopicMapException {
        return getTopic(tm, valid, VALID_SI, getValidType(tm));
    }

    public Topic getValidType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, VALID_SI, "Valid (DC)", getDCClass(tm));
    }


    // ----


    public Topic getAvailableTopic(String available, TopicMap tm) throws TopicMapException {
        return getTopic(tm, available, AVAILABLE_SI, getAvailableType(tm));
    }

    public Topic getAvailableType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, AVAILABLE_SI, "Available (DC)", getDCClass(tm));
    }


    // ----


    public Topic getIssuedTopic(String issued, TopicMap tm) throws TopicMapException {
        return getTopic(tm, issued, ISSUED_SI, getIssuedType(tm));
    }

    public Topic getIssuedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ISSUED_SI, "Issued (DC)", getDCClass(tm));
    }


    // ----


    public Topic getModifiedTopic(String modified, TopicMap tm) throws TopicMapException {
        return getTopic(tm, modified, MODIFIED_SI, getModifiedType(tm));
    }

    public Topic getModifiedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, MODIFIED_SI, "Modified (DC)", getDCClass(tm));
    }


    // ----


    public Topic getExtentTopic(String extent, TopicMap tm) throws TopicMapException {
        return getTopic(tm, extent, EXTENT_SI, getExtentType(tm));
    }

    public Topic getExtentType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, EXTENT_SI, "Extent (DC)", getDCClass(tm));
    }



    // ----


    public Topic getMediumTopic(String medium, TopicMap tm) throws TopicMapException {
        return getTopic(tm, medium, MEDIUM_SI, getMediumType(tm));
    }

    public Topic getMediumType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, MEDIUM_SI, "Medium (DC)", getDCClass(tm));
    }


    
    // ---------------------------------------------


    public Topic getRecordType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, RECORD_SI, "Record (DC)", getDCClass(tm));
    }

    public Topic getDCClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SI_BASE, "Dublin Core", getWandoraClass(tm));
    }
    
    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }


    // --------


    public Topic getTopic(TopicMap tm, String str, String SIBase, Topic type) throws TopicMapException {
        if(str != null && SIBase != null) {
            str = str.trim();
            if(str.length() > 0) {
                Topic strTopic=getOrCreateTopic(tm, makeSI(SIBase, str), str, type);
                return strTopic;
            }
        }
        return null;
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return getOrCreateTopic(tm, si, bn, null);
    }

    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn, Topic type) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, type, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }






    protected String makeSI(String base, String endPoint) {
        String end = endPoint;
        try {
            end = URLEncoder.encode(endPoint, defaultEncoding);
        }
        catch(Exception e) {
            log(e);
        }
        String si = base + "/" + end;
        return si;
    }

    // -------------------------------------------------------------------------



    private class DublinCoreXMLParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {


        public DublinCoreXMLParser(TopicMap tm, DublinCoreXMLExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }



        public int progress=0;
        private TopicMap tm;
        private DublinCoreXMLExtractor parent;

        public static final String TAG_METADATA = "metadata";
        
        public static final String TAG_DC = "dc";
        public static final String TAG_RECORD = "record";
        
        public static final String TAG_TITLE= "title";
        public static final String TAG_CREATOR = "creator";
        public static final String TAG_TYPE = "type";
        public static final String TAG_PUBLISHER = "publisher";
        public static final String TAG_DATE = "date";
        public static final String TAG_LANGUAGE = "language";
        public static final String TAG_DESCRIPTION = "description";
        public static final String TAG_SUBJECT = "subject";
        public static final String TAG_IDENTIFIER = "identifier";
        public static final String TAG_CONTRIBUTOR = "contributor";
        public static final String TAG_FORMAT = "format";

        public static final String TAG_SOURCE = "source";
        public static final String TAG_RELATION = "relation";
        public static final String TAG_COVERAGE = "coverage";
        public static final String TAG_RIGHTS = "rights";
        public static final String TAG_AUDIENCE = "audience";
        public static final String TAG_ALTERNATIVE = "alternative";

        public static final String TAG_TABLEOFCONTENTS = "tableOfContents";
        public static final String TAG_ABSTRACT = "abstract";
        public static final String TAG_CREATED = "created";
        public static final String TAG_VALID = "valid";
        public static final String TAG_AVAILABLE = "available";
        public static final String TAG_ISSUED = "issued";
        public static final String TAG_MODIFIED = "modified";
        public static final String TAG_EXTENT = "extent";
        public static final String TAG_MEDIUM = "medium";

        public static final String ATTR_LANG = "lang";
        public static final String ATTR_TYPE = "type";

        private static final int STATE_START=0;
        private static final int STATE_DC=2;
        private static final int STATE_DC_TITLE=21;
        private static final int STATE_DC_CREATOR=22;
        private static final int STATE_DC_TYPE=23;
        private static final int STATE_DC_LANGUAGE=24;
        private static final int STATE_DC_DATE=25;
        private static final int STATE_DC_DESCRIPTION=26;
        private static final int STATE_DC_SUBJECT=27;
        private static final int STATE_DC_PUBLISHER=28;
        private static final int STATE_DC_IDENTIFIER=29;
        private static final int STATE_DC_CONTRIBUTOR=30;
        private static final int STATE_DC_FORMAT=31;

        private static final int STATE_DC_SOURCE = 32;
        private static final int STATE_DC_RELATION = 33;
        private static final int STATE_DC_COVERAGE = 34;
        private static final int STATE_DC_RIGHTS = 35;
        private static final int STATE_DC_AUDIENCE = 36;
        private static final int STATE_DC_ALTERNATIVE = 37;

        private static final int STATE_DC_TABLEOFCONTENTS = 38;
        private static final int STATE_DC_ABSTRACT = 39;
        private static final int STATE_DC_CREATED = 40;
        private static final int STATE_DC_VALID = 41;
        private static final int STATE_DC_AVAILABLE = 42;
        private static final int STATE_DC_ISSUED = 43;
        private static final int STATE_DC_MODIFIED = 44;
        private static final int STATE_DC_EXTENT = 45;
        private static final int STATE_DC_MEDIUM = 46;

        private int state=STATE_START;

        private String data_title = null;
        private String data_title_lang = null;
        private String data_title_type = null;

        private String data_creator = null;
        private String data_type = null;
        private String data_publisher = null;
        private String data_date = null;
        private String data_language = null;
        private String data_description = null;
        private String data_description_lang = null;
        private String data_subject = null;
        private String data_identifier = null;
        private String data_identifier_type = null;
        private String data_contributor = null;
        private String data_format = null;

        private String data_source = null;
        private String data_relation = null;
        private String data_coverage = null;
        private String data_rights = null;
        private String data_audience = null;
        private String data_alternative = null;
        private String data_alternative_lang = null;

        private String data_tableofcontents = null;
        private String data_tableofcontents_lang = null;
        private String data_abstract = null;
        private String data_abstract_lang = null;
        
        private String data_created = null;
        private String data_valid = null;
        private String data_available = null;
        private String data_issued = null;
        private String data_modified = null;
        private String data_extent = null;
        private String data_medium = null;

        private Topic dcTopic = null;
        private org.wandora.topicmap.Locator dcIdentifier = null;


        
        private void associatiateWithRecord( TopicMap tm, Topic player, Topic role ) {
            if(dcTopic != null) {
                try {
                    if(player != null) {
                        Topic recordType = getRecordType( tm );
                        Association a = tm.createAssociation(role);
                        a.addPlayer(dcTopic, recordType);
                        a.addPlayer(player, role);
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }





        @Override
        public void startDocument() throws SAXException {
        }
        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            //System.out.println("found tag: "+qName+", localName: "+localName);
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(equalTags(qName,TAG_DC) || equalTags(qName,TAG_METADATA) || equalTags(qName,TAG_RECORD)) {
                        try {
                            dcTopic = tm.createTopic();
                            dcIdentifier = TopicTools.createDefaultLocator();
                            dcTopic.addSubjectIdentifier(dcIdentifier);
                            Topic recordType = getRecordType(tm);
                            dcTopic.addType(recordType);
                        }
                        catch(Exception e) {
                            log(e);
                        }
                        state = STATE_DC;
                    }
                    break;
                case STATE_DC:
                    if(equalTags(qName,TAG_TITLE)) {
                        data_title = "";
                        data_title_lang = atts.getValue(ATTR_LANG);
                        if(data_title_lang == null) data_title_lang = atts.getValue("XML:lang");
                        data_title_type = atts.getValue(ATTR_TYPE);
                        state = STATE_DC_TITLE;
                    }
                    else if(equalTags(qName,TAG_CREATOR)) {
                        data_creator = "";
                        state = STATE_DC_CREATOR;
                    }
                    else if(equalTags(qName,TAG_TYPE)) {
                        data_type = "";
                        state = STATE_DC_TYPE;
                    }
                    else if(equalTags(qName,TAG_PUBLISHER)) {
                        data_publisher = "";
                        state = STATE_DC_PUBLISHER;
                    }
                    else if(equalTags(qName,TAG_DATE)) {
                        data_date = "";
                        state = STATE_DC_DATE;
                    }
                    else if(equalTags(qName,TAG_LANGUAGE)) {
                        data_language = "";
                        state = STATE_DC_LANGUAGE;
                    }
                    else if(equalTags(qName,TAG_DESCRIPTION)) {
                        data_description = "";
                        data_description_lang = atts.getValue(ATTR_LANG);
                        if(data_description_lang == null) data_description_lang = atts.getValue("XML:lang");
                        state = STATE_DC_DESCRIPTION;
                    }
                    else if(equalTags(qName,TAG_SUBJECT)) {
                        data_subject = "";
                        state = STATE_DC_SUBJECT;
                    }
                    else if(equalTags(qName,TAG_IDENTIFIER)) {
                        data_identifier = "";
                        data_identifier_type = atts.getValue(ATTR_TYPE);
                        state = STATE_DC_IDENTIFIER;
                    }
                    else if(equalTags(qName,TAG_CONTRIBUTOR)) {
                        data_contributor = "";
                        state = STATE_DC_CONTRIBUTOR;
                    }
                    else if(equalTags(qName,TAG_FORMAT)) {
                        data_format = "";
                        state = STATE_DC_FORMAT;
                    }


                    else if(equalTags(qName,TAG_SOURCE)) {
                        data_source = "";
                        state = STATE_DC_SOURCE;
                    }
                    else if(equalTags(qName,TAG_RELATION)) {
                        data_relation = "";
                        state = STATE_DC_RELATION;
                    }
                    else if(equalTags(qName,TAG_COVERAGE)) {
                        data_coverage = "";
                        state = STATE_DC_COVERAGE;
                    }
                    else if(equalTags(qName,TAG_RIGHTS)) {
                        data_rights = "";
                        state = STATE_DC_RIGHTS;
                    }
                    else if(equalTags(qName,TAG_AUDIENCE)) {
                        data_audience = "";
                        state = STATE_DC_AUDIENCE;
                    }
                    else if(equalTags(qName,TAG_ALTERNATIVE)) {
                        data_alternative = "";
                        data_alternative_lang = atts.getValue(ATTR_LANG);
                        if(data_alternative_lang == null) data_alternative_lang = atts.getValue("XML:lang");
                        state = STATE_DC_ALTERNATIVE;
                    }
                    else if(equalTags(qName,TAG_TABLEOFCONTENTS)) {
                        data_tableofcontents = "";
                        data_tableofcontents_lang = atts.getValue(ATTR_LANG);
                        if(data_tableofcontents_lang == null) data_tableofcontents_lang = atts.getValue("XML:lang");
                        state = STATE_DC_TABLEOFCONTENTS;
                    }
                    else if(equalTags(qName,TAG_ABSTRACT)) {
                        data_abstract = "";
                        data_abstract_lang = atts.getValue(ATTR_LANG);
                        if(data_abstract_lang == null) data_abstract_lang = atts.getValue("XML:lang");
                        state = STATE_DC_ABSTRACT;
                    }
                    else if(equalTags(qName,TAG_CREATED)) {
                        data_created = "";
                        state = STATE_DC_CREATED;
                    }
                    else if(equalTags(qName,TAG_VALID)) {
                        data_valid = "";
                        state = STATE_DC_VALID;
                    }
                    else if(equalTags(qName,TAG_AVAILABLE)) {
                        data_available = "";
                        state = STATE_DC_AVAILABLE;
                    }
                    else if(equalTags(qName,TAG_ISSUED)) {
                        data_issued = "";
                        state = STATE_DC_ISSUED;
                    }
                    else if(equalTags(qName,TAG_MODIFIED)) {
                        data_modified = "";
                        state = STATE_DC_MODIFIED;
                    }
                    else if(equalTags(qName,TAG_EXTENT)) {
                        data_extent = "";
                        state = STATE_DC_EXTENT;
                    }
                    else if(equalTags(qName,TAG_MEDIUM)) {
                        data_medium = "";
                        state = STATE_DC_MEDIUM;
                    }

                    break;
                
            }
        }



        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_DC: {
                    if(equalTags(qName,TAG_DC) || equalTags(qName,TAG_METADATA) || equalTags(qName,TAG_RECORD)) {
                        progress++;
                        state = STATE_START;
                    }
                    break;
                }
                case STATE_DC_TITLE: {
                    if(equalTags(qName,TAG_TITLE)) {
                        if(dcTopic != null && data_title != null) {
                            data_title = data_title.trim();
                            if(data_title.length() > 0) {
                                try {
                                    if(dcTopic.getBaseName() == null) {
                                        dcTopic.setBaseName( data_title );
                                    }
                                    if(data_title_lang == null || data_title_lang.length() == 0) {
                                        data_title_lang = "en";
                                    }
                                    dcTopic.setDisplayName(data_title_lang, data_title);
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_CREATOR: {
                    if(equalTags(qName,TAG_CREATOR)) {
                        if(dcTopic != null && data_creator != null && data_creator.length() > 0) {
                            try {
                                Topic creatorTopic = getCreatorTopic( data_creator, tm );
                                if(creatorTopic != null) {
                                    Topic creatorType = getCreatorType( tm );
                                    Topic recordType = getRecordType( tm );
                                    Association a = tm.createAssociation(creatorType);
                                    a.addPlayer(dcTopic, recordType);
                                    a.addPlayer(creatorTopic, creatorType);
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_CONTRIBUTOR: {
                    if(equalTags(qName,TAG_CONTRIBUTOR)) {
                        if(dcTopic != null && data_contributor != null && data_contributor.length() > 0) {
                            try {
                                Topic contributorTopic = getContributorTopic( data_contributor, tm );
                                if(contributorTopic != null) {
                                    Topic contributorType = getContributorType( tm );
                                    Topic recordType = getRecordType( tm );
                                    Association a = tm.createAssociation(contributorType);
                                    a.addPlayer(dcTopic, recordType);
                                    a.addPlayer(contributorTopic, contributorType);
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_TYPE: {
                    if(equalTags(qName,TAG_TYPE)) {
                        if(dcTopic != null && data_creator != null && data_creator.length() > 0) {
                            try {
                                String[] types = data_type.split(",");
                                String type = null;
                                for(int i=0; i<types.length; i++) {
                                    type = types[i];
                                    type = type.trim();
                                    if(type.length() > 0) {
                                        Topic typeTopic = getTypeTopic( type, tm );
                                        dcTopic.addType(typeTopic);
                                    }
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_PUBLISHER: {
                    if(equalTags(qName,TAG_PUBLISHER)) {
                        if(dcTopic != null && data_publisher != null && data_publisher.length() > 0) {
                            try {
                                Topic publisherTopic = getPublisherTopic( data_publisher, tm );
                                if(publisherTopic != null) {
                                    Topic publisherType = getPublisherType( tm );
                                    Topic recordType = getRecordType( tm );
                                    Association a = tm.createAssociation(publisherType);
                                    a.addPlayer(dcTopic, recordType);
                                    a.addPlayer(publisherTopic, publisherType);
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_DATE: {
                    if(equalTags(qName,TAG_DATE)) {
                        if(dcTopic != null && data_date != null && data_date.length() > 0) {
                            try {
                                Topic dateTopic = getDateTopic( data_date, tm );
                                if(dateTopic != null) {
                                    Topic dateType = getDateType( tm );
                                    Topic recordType = getRecordType( tm );
                                    Association a = tm.createAssociation(dateType);
                                    a.addPlayer(dcTopic, recordType);
                                    a.addPlayer(dateTopic, dateType);
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_LANGUAGE: {
                    if(equalTags(qName,TAG_LANGUAGE)) {
                        if(dcTopic != null && data_language != null && data_language.length() > 0) {
                            String[] languages = data_language.split(",");
                            for(int i=0; i<languages.length; i++) {
                                String lan = languages[i];
                                lan = lan.trim();
                                if( lan.length() > 0) {
                                    try {
                                        Topic languageTopic = getLanguageTopic( lan, tm );
                                        if(languageTopic != null) {
                                            Topic languageType = getLanguageType( tm );
                                            Topic recordType = getRecordType( tm );
                                            Association a = tm.createAssociation(languageType);
                                            a.addPlayer(dcTopic, recordType);
                                            a.addPlayer(languageTopic, languageType);
                                        }
                                    }
                                    catch(Exception e) {
                                        log(e);
                                    }
                                }
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_DESCRIPTION: {
                    if(equalTags(qName,TAG_DESCRIPTION)) {
                        if(dcTopic != null && data_description != null && data_description.length() > 0) {
                            try {
                                data_description = data_description.trim();
                                if(DESCRIPTION_AS_OCCURRENCE) {
                                    Topic descriptionType = getDescriptionType(tm);
                                    if(data_description_lang == null || data_description_lang.length() == 0) {
                                        data_description_lang = "en";
                                    }
                                    Topic langTopic = tm.getTopic(XTMPSI.getLang(data_description_lang));
                                    if(descriptionType != null && langTopic != null) {
                                        String o = null;
                                        if(APPEND_OCCURRENCE_DESCRIPTION) o = dcTopic.getData(descriptionType, langTopic);
                                        if(o == null) o = data_description;
                                        else o = o + "\n\n" + data_description;
                                        dcTopic.setData(descriptionType, langTopic, o);
                                    }
                                }
                                if(DESCRIPTION_AS_PLAYER) {
                                    Topic recordType = getRecordType( tm );
                                    Topic descriptionType = getDescriptionType(tm);
                                    Topic descriptionTopic = getDescriptionTopic(data_description, tm);
                                    Association a = tm.createAssociation(descriptionType);
                                    a.addPlayer(dcTopic, recordType);
                                    a.addPlayer(descriptionTopic, descriptionType);
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_SUBJECT: {
                    if(equalTags(qName,TAG_SUBJECT)) {
                        if(dcTopic != null && data_subject != null && data_subject.length() > 0) {
                            String[] subjects = data_subject.split(",");
                            for(int i=0; i<subjects.length; i++) {
                                String subject = subjects[i];
                                subject = subject.trim();
                                if( subject.length() > 0) {
                                    try {
                                        Topic subjectTopic = getSubjectTopic( subject, tm );
                                        if(subjectTopic != null) {
                                            Topic subjectType = getSubjectType( tm );
                                            Topic recordType = getRecordType( tm );
                                            Association a = tm.createAssociation(subjectType);
                                            a.addPlayer(dcTopic, recordType);
                                            a.addPlayer(subjectTopic, subjectType);
                                        }
                                    }
                                    catch(Exception e) {
                                        log(e);
                                    }
                                }
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_IDENTIFIER: {
                    if(equalTags(qName,TAG_IDENTIFIER)) {
                        if(dcTopic != null && data_identifier != null) {
                            try {
                                data_identifier = data_identifier.trim();
                                if(data_identifier.length() > 0) {
                                    if(IDENTIFIER_AS_SI) {
                                        dcTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_identifier));
                                    }
                                    else {
                                        Topic identifierTopic = getIdentifierTopic( data_identifier, tm );
                                        if(identifierTopic != null) {
                                            Topic identifierType = getIdentifierType( tm );
                                            Topic recordType = getRecordType( tm );
                                            Association a = tm.createAssociation(identifierType);
                                            a.addPlayer(dcTopic, recordType);
                                            a.addPlayer(identifierTopic, identifierType);
                                        }
                                    }
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_FORMAT: {
                    if(equalTags(qName,TAG_FORMAT)) {
                        if(dcTopic != null && data_format != null && data_format.length() > 0) {
                            try { associatiateWithRecord( tm, getFormatTopic(data_format, tm), getFormatType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }


                case STATE_DC_SOURCE: {
                    if(equalTags(qName,TAG_SOURCE)) {
                        if(dcTopic != null && data_source != null && data_source.length() > 0) {
                            try { associatiateWithRecord( tm, getSourceTopic(data_source, tm), getSourceType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_RELATION: {
                    if(equalTags(qName,TAG_RELATION)) {
                        if(dcTopic != null && data_relation != null && data_relation.length() > 0) {
                            try { associatiateWithRecord( tm, getRelationTopic(data_relation, tm), getRelationType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_COVERAGE: {
                    if(equalTags(qName,TAG_COVERAGE)) {
                        if(dcTopic != null && data_coverage != null && data_coverage.length() > 0) {
                            try { associatiateWithRecord( tm, getCoverageTopic(data_coverage, tm), getCoverageType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_RIGHTS: {
                    if(equalTags(qName,TAG_RIGHTS)) {
                        if(dcTopic != null && data_rights != null && data_rights.length() > 0) {
                            try { associatiateWithRecord( tm, getRightsTopic(data_rights, tm), getRightsType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_AUDIENCE: {
                    if(equalTags(qName,TAG_AUDIENCE)) {
                        if(dcTopic != null && data_audience != null && data_audience.length() > 0) {
                            try { associatiateWithRecord( tm, getAudienceTopic(data_audience, tm), getAudienceType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_ALTERNATIVE: {
                    if(equalTags(qName,TAG_ALTERNATIVE)) {
                        if(dcTopic != null && data_alternative != null) {
                            data_alternative = data_alternative.trim();
                            if(data_alternative.length() > 0) {
                                try {
                                    Topic alternativeType = getAlternativeType(tm);
                                    if(data_alternative_lang == null || data_alternative_lang.length() == 0) {
                                        data_alternative_lang = "en";
                                    }
                                    Topic langTopic = tm.getTopic(XTMPSI.getLang(data_alternative_lang));
                                    if(alternativeType != null && langTopic != null) {
                                        String o = null;
                                        if(APPEND_OCCURRENCE_ALTERNATIVE) o = dcTopic.getData(alternativeType, langTopic);
                                        if(o == null) o = data_alternative;
                                        else o = o + "\n\n" + data_alternative;
                                        dcTopic.setData(alternativeType, langTopic, o);
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_TABLEOFCONTENTS: {
                    if(equalTags(qName,TAG_TABLEOFCONTENTS)) {
                        if(dcTopic != null && data_tableofcontents != null) {
                            data_tableofcontents = data_tableofcontents.trim();
                            if(data_tableofcontents.length() > 0) {
                                try {
                                    Topic tableofcontentsType = getTableOfContentsType(tm);
                                    if(data_tableofcontents_lang == null || data_tableofcontents_lang.length() == 0) {
                                        data_tableofcontents_lang = "en";
                                    }
                                    Topic langTopic = tm.getTopic(XTMPSI.getLang(data_tableofcontents_lang));
                                    if(tableofcontentsType != null && langTopic != null) {
                                        String o = null;
                                        if(APPEND_OCCURRENCE_TABLEOFCONTENTS) o = dcTopic.getData(tableofcontentsType, langTopic);
                                        if(o == null) o = data_tableofcontents;
                                        else o = o + "\n\n" + data_tableofcontents;
                                        System.out.println("Setting toc: "+o);
                                        dcTopic.setData(tableofcontentsType, langTopic, o);
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

                case STATE_DC_ABSTRACT: {
                    if(equalTags(qName,TAG_ABSTRACT)) {
                        if(dcTopic != null && data_abstract != null) {
                            data_abstract = data_abstract.trim();
                            if(data_abstract.length() > 0) {
                                try {
                                    Topic abstractType = getAbstractType(tm);
                                    if(data_abstract_lang == null || data_abstract_lang.length() == 0) {
                                        data_abstract_lang = "en";
                                    }
                                    Topic langTopic = tm.getTopic(XTMPSI.getLang(data_abstract_lang));
                                    if(abstractType != null && langTopic != null) {
                                        String o = null;
                                        if(APPEND_OCCURRENCE_ABSTRACT) o = dcTopic.getData(abstractType, langTopic);
                                        if(o == null) o = data_abstract;
                                        else o = o + "\n\n" + data_abstract;
                                        dcTopic.setData(abstractType, langTopic, o);
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_CREATED: {
                    if(equalTags(qName,TAG_CREATED)) {
                        if(dcTopic != null && data_created != null && data_created.length() > 0) {
                            try { associatiateWithRecord( tm, getCreatedTopic(data_created, tm), getCreatedType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_VALID: {
                    if(equalTags(qName,TAG_VALID)) {
                        if(dcTopic != null && data_valid != null && data_valid.length() > 0) {
                            try { associatiateWithRecord( tm, getValidTopic(data_valid, tm), getValidType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_AVAILABLE: {
                    if(equalTags(qName,TAG_AVAILABLE)) {
                        if(dcTopic != null && data_available != null && data_available.length() > 0) {
                            try { associatiateWithRecord( tm, getAvailableTopic(data_available, tm), getAvailableType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_ISSUED: {
                    if(equalTags(qName,TAG_ISSUED)) {
                        if(dcTopic != null && data_issued != null && data_issued.length() > 0) {
                            try { associatiateWithRecord( tm, getIssuedTopic(data_issued, tm), getIssuedType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_MODIFIED: {
                    if(equalTags(qName,TAG_MODIFIED)) {
                        if(dcTopic != null && data_modified != null && data_modified.length() > 0) {
                            try { associatiateWithRecord( tm, getModifiedTopic(data_modified, tm), getModifiedType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_EXTENT: {
                    if(equalTags(qName,TAG_EXTENT)) {
                        if(dcTopic != null && data_extent != null && data_extent.length() > 0) {
                            try { associatiateWithRecord( tm, getExtentTopic(data_extent, tm), getExtentType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }
                case STATE_DC_MEDIUM: {
                    if(equalTags(qName,TAG_MEDIUM)) {
                        if(dcTopic != null && data_medium != null && data_medium.length() > 0) {
                            try { associatiateWithRecord( tm, getMediumTopic(data_medium, tm), getMediumType(tm) ); }
                            catch(Exception e) { log(e); }
                        }
                        state = STATE_DC;
                    }
                    break;
                }

            }
        }


        



        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state) {
                case STATE_DC_TITLE: {
                    data_title += new String(ch,start,length);
                    break;
                }
                case STATE_DC_CREATOR: {
                    data_creator += new String(ch,start,length);
                    break;
                }
                case STATE_DC_TYPE: {
                    data_type += new String(ch,start,length);
                    break;
                }
                case STATE_DC_PUBLISHER: {
                    data_publisher += new String(ch,start,length);
                    break;
                }
                case STATE_DC_CONTRIBUTOR: {
                    data_contributor += new String(ch,start,length);
                    break;
                }
                case STATE_DC_DATE: {
                    data_date += new String(ch,start,length);
                    break;
                }
                case STATE_DC_LANGUAGE: {
                    data_language += new String(ch,start,length);
                    break;
                }
                case STATE_DC_DESCRIPTION: {
                    data_description += new String(ch,start,length);
                    break;
                }
                case STATE_DC_SUBJECT: {
                    data_subject += new String(ch,start,length);
                    break;
                }
                case STATE_DC_IDENTIFIER: {
                    data_identifier += new String(ch,start,length);
                    break;
                }
                case STATE_DC_FORMAT: {
                    data_format += new String(ch,start,length);
                    break;
                }
                


                case STATE_DC_SOURCE: {
                    data_source += new String(ch,start,length);
                    break;
                }
                case STATE_DC_RELATION: {
                    data_relation += new String(ch,start,length);
                    break;
                }
                case STATE_DC_COVERAGE: {
                    data_coverage += new String(ch,start,length);
                    break;
                }
                case STATE_DC_RIGHTS: {
                    data_rights += new String(ch,start,length);
                    break;
                }
                case STATE_DC_AUDIENCE: {
                    data_audience += new String(ch,start,length);
                    break;
                }
                case STATE_DC_ALTERNATIVE: {
                    data_alternative += new String(ch,start,length);
                    break;
                }

                case STATE_DC_TABLEOFCONTENTS: {
                    data_tableofcontents += new String(ch,start,length);
                    break;
                }
                case STATE_DC_ABSTRACT: {
                    data_abstract += new String(ch,start,length);
                    break;
                }
                case STATE_DC_CREATED: {
                    data_created += new String(ch,start,length);
                    break;
                }
                case STATE_DC_VALID: {
                    data_valid += new String(ch,start,length);
                    break;
                }
                case STATE_DC_AVAILABLE: {
                    data_available += new String(ch,start,length);
                    break;
                }
                case STATE_DC_ISSUED: {
                    data_issued += new String(ch,start,length);
                    break;
                }
                case STATE_DC_MODIFIED: {
                    data_modified += new String(ch,start,length);
                    break;
                }
                case STATE_DC_EXTENT: {
                    data_extent += new String(ch,start,length);
                    break;
                }
                case STATE_DC_MEDIUM: {
                    data_medium += new String(ch,start,length);
                    break;
                }
                default:
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



    private boolean equalTags(String t1, String t2) {
        if(t1 == null || t2 == null) return false;
        if(t1.indexOf(":") > -1) {
            String[] t1s = t1.split(":");
            t1 = t1s[t1s.length-1];
        }
        //System.out.println("Comparing: "+t1+" and "+t2);
        return t1.equalsIgnoreCase(t2);
    }



    // -------------------------------------------------------------------------


}
