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
 *
 */


package org.wandora.application.tools.extractors.mads;


import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import javax.swing.*;

import org.xml.sax.*;

/**
 *
 * @author akivela
 */
public class MadsExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;
	
	protected static String MADS_SI = "http://www.loc.gov/marc/bibliographic/bdintro.html";
    protected static String AUTHORITY_SI = "http://www.loc.gov/marc/bibliographic/bdintro.html#indicator";
   
    private static String defaultEncoding = "UTF-8"; //"ISO-8859-1";
    private static String defaultLang = "en";


    /** Creates a new instance of MadsExtractor */
    public MadsExtractor() {
    }


    @Override
    public String getName() {
        return "MADS extractor";
    }

    @Override
    public String getDescription(){
        return "Converts MADS XML documents to topics maps.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_mads.png");
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


    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------



    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String in = IObox.doUrl(url);
        InputSource inSource = new InputSource(new ByteArrayInputStream(in.getBytes(defaultEncoding)));
        inSource.setEncoding(defaultEncoding);
        return _extractTopicsFrom(inSource, topicMap);
    }


    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        String in = IObox.loadFile(file);
        InputSource inSource = new InputSource(new ByteArrayInputStream(in.getBytes(defaultEncoding)));
        inSource.setEncoding(defaultEncoding);
        return _extractTopicsFrom(inSource, topicMap);
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }
    


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        InputSource inSource = new InputSource(in);
        inSource.setEncoding(defaultEncoding);
        return _extractTopicsFrom(inSource, topicMap);
    }


    public boolean _extractTopicsFrom(InputSource in, TopicMap topicMap) throws Exception {
        try {
            setDefaultLogger();
            setProgressMax(100);

            // ---- Parse results ----
            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            MadsXMLParser parserHandler = new MadsXMLParser(topicMap, this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            try {
                reader.parse(in);
            }
            catch(Exception e){
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }

            String msg = null;
            if(parserHandler.progress == 0) {
                msg = "Found no records.";
            }
            else {
                msg = "Found "+parserHandler.progress+" record(s).";
            }
            if(msg != null) log(msg);
        }
        catch (Exception ex) {
           log(ex);
        }
        return true;
    }


    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    private class MadsXMLParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {


        public MadsXMLParser(TopicMap tm, MadsExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }



        public int progress=0;
        private TopicMap tm;
        private MadsExtractor parent;

        public static final String TAG_MADS = "mads";
        public static final String TAG_AUTHORITY = "authority";
        public static final String TAG_RELATED = "related";
        public static final String TAG_VARIANT = "variant";

        public static final String TAG_NAME = "name";
        public static final String TAG_TITLEINFO = "titleInfo";
        public static final String TAG_TOPIC = "topic";
        public static final String TAG_TEMPORAL = "temporal";
        public static final String TAG_GENRE = "genre";
        public static final String TAG_GEOGRAPHIC = "geographic";
        public static final String TAG_HIERARCHICALGEOGRAPHIC = "hierarchicalGeographic";
        public static final String TAG_OCCUPATION = "occupation";

        public static final String TAG_NOTE = "note";
        public static final String TAG_AFFILIATION = "affiliation";
        public static final String TAG_URL = "url";
        public static final String TAG_IDENTIFIER = "identifier";
        public static final String TAG_FIELDOFACTIVITY = "fieldOfActivity";
        public static final String TAG_EXTENSION = "extension";
        public static final String TAG_RECORDINFO = "recordInfo";

        // NAME
        public static final String TAG_NAMEPART = "namePart";
        public static final String TAG_DESCRIPTION = "description";

        // AFFILIATION
        public static final String TAG_ORGANIZATION = "organization";
        public static final String TAG_POSITION = "position";
        public static final String TAG_ADDRESS = "address";
        public static final String TAG_EMAIL = "email";
        public static final String TAG_PHONE = "phone";
        public static final String TAG_FAX = "fax";
        public static final String TAG_HOURS = "hours";
        public static final String TAG_DATEVALID = "dateValid";

        // ADDRESS
        public static final String TAG_STREET = "street";
        public static final String TAG_CITY = "city";
        public static final String TAG_STATE = "state";
        public static final String TAG_COUNTRY = "country";
        public static final String TAG_POSTCODE = "postcode";

        public static final String ATTRIBUTE_VERSION = "version";

        public static final String ATTRIBUTE_AUTHORITY = "authority";
        public static final String ATTRIBUTE_TYPE = "type";
        public static final String ATTRIBUTE_LANG = "lang";
        public static final String ATTRIBUTE_ID = "ID";

        // URL
        public static final String ATTRIBUTE_DATELASTACCESSED = "dateLastAccessed";
        public static final String ATTRIBUTE_DISPLAYLABEL = "displayLabel";
        public static final String ATTRIBUTE_NOTE = "note";
        public static final String ATTRIBUTE_USAGE = "usage";

        public static final String ATTRIBUTE_INVALID = "invalid";
        public static final String ATTRIBUTE_ALTREPGROUP = "altRepGroup";

        public static final String ATTRIBUTE_SCRIPT = "script";
        public static final String ATTRIBUTE_TRANSLITERATION = "transliteration";



        // STATES
        private static final int STATE_START=0;
        private static final int STATE_MADS=2;

        private static final int STATE_MADS_AUTHORITY=4;
        private static final int STATE_MADS_RELATED=5;
        private static final int STATE_MADS_VARIANT=6;

        private static final int STATE_MADS_AUTHORITY_NAME=42;
        private static final int STATE_MADS_AUTHORITY_NAME_NAMEPART=421;
        private static final int STATE_MADS_AUTHORITY_NAME_DESCRIPTION=422;

        private static final int STATE_MADS_AUTHORITY_TITLEINFO=43;
        private static final int STATE_MADS_AUTHORITY_TOPIC=44;
        private static final int STATE_MADS_AUTHORITY_TEMPORAL=45;
        private static final int STATE_MADS_AUTHORITY_GENRE=46;
        private static final int STATE_MADS_AUTHORITY_GEOGRAPHIC=47;
        private static final int STATE_MADS_AUTHORITY_HIERARCHICALGEOGRAPHIC=48;
        private static final int STATE_MADS_AUTHORITY_OCCUPATION=49;

        private static final int STATE_MADS_RELATED_NAME=52;
        private static final int STATE_MADS_RELATED_NAME_NAMEPART=521;
        private static final int STATE_MADS_RELATED_NAME_DESCRIPTION=522;

        private static final int STATE_MADS_RELATED_TITLEINFO=53;
        private static final int STATE_MADS_RELATED_TOPIC=54;
        private static final int STATE_MADS_RELATED_TEMPORAL=55;
        private static final int STATE_MADS_RELATED_GENRE=56;
        private static final int STATE_MADS_RELATED_GEOGRAPHIC=57;
        private static final int STATE_MADS_RELATED_HIERARCHICALGEOGRAPHIC=58;
        private static final int STATE_MADS_RELATED_OCCUPATION=59;

        private static final int STATE_MADS_VARIANT_NAME=62;
        private static final int STATE_MADS_VARIANT_NAME_NAMEPART=621;
        private static final int STATE_MADS_VARIANT_NAME_DESCRIPTION=622;
        
        private static final int STATE_MADS_VARIANT_TITLEINFO=63;
        private static final int STATE_MADS_VARIANT_TOPIC=64;
        private static final int STATE_MADS_VARIANT_TEMPORAL=65;
        private static final int STATE_MADS_VARIANT_GENRE=66;
        private static final int STATE_MADS_VARIANT_GEOGRAPHIC=67;
        private static final int STATE_MADS_VARIANT_HIERARCHICALGEOGRAPHIC=68;
        private static final int STATE_MADS_VARIANT_OCCUPATION=69;

        public static final int STATE_MADS_NOTE = 101;
        public static final int STATE_MADS_AFFILIATION = 102;
        public static final int STATE_MADS_URL = 103;
        public static final int STATE_MADS_IDENTIFIER = 104;
        public static final int STATE_MADS_FIELDOFACTIVITY = 105;
        public static final int STATE_MADS_EXTENSION = 106;
        public static final int STATE_MADS_RECORDINFO = 107;

        /* RECORDINFO
         * 		<xs:element ref="recordContentSource"/>
			<xs:element ref="recordCreationDate"/>
			<xs:element ref="recordChangeDate"/>
			<xs:element ref="recordIdentifier"/>
			<xs:element ref="languageOfCataloging"/>
			<xs:element ref="recordOrigin"/>
			<xs:element ref="descriptionStandard"/>

         */

        public static final int STATE_MADS_AFFILIATION_ORGANIZATION = 1021;
        public static final int STATE_MADS_AFFILIATION_POSITION = 1022;
        public static final int STATE_MADS_AFFILIATION_ADDRESS = 1023;
        public static final int STATE_MADS_AFFILIATION_EMAIL = 1024;
        public static final int STATE_MADS_AFFILIATION_PHONE = 1025;
        public static final int STATE_MADS_AFFILIATION_FAX = 1026;
        public static final int STATE_MADS_AFFILIATION_HOURS = 1027;
        public static final int STATE_MADS_AFFILIATION_DATEVALID = 1028;


        public static final int STATE_MADS_AFFILIATION_ADDRESS_STREET = 10231;
        public static final int STATE_MADS_AFFILIATION_ADDRESS_CITY = 10232;
        public static final int STATE_MADS_AFFILIATION_ADDRESS_STATE = 10233;
        public static final int STATE_MADS_AFFILIATION_ADDRESS_COUNTRY = 10234;
        public static final int STATE_MADS_AFFILIATION_ADDRESS_POSTCODE = 10235;
        
        private int state=STATE_START;


        private MadsModel mads = null;
        private MadsModel.MadsAuthority authority = null;
        private MadsModel.MadsRelated related = null;
        private MadsModel.MadsVariant variant = null;

        private MadsModel.MadsGenre genre = null;
        private MadsModel.MadsGeographic geographic = null;
        private MadsModel.MadsName name = null;
        private MadsModel.MadsOccupation occupation = null;
        private MadsModel.MadsHierarchicalGeographic hierarchicalGeographic = null;
        private MadsModel.MadsTemporal temporal = null;
        private MadsModel.MadsTitleInfo titleInfo = null;
        private MadsModel.MadsTopic topic = null;

        private MadsModel.MadsAddress address = null;
        private MadsModel.MadsAffiliation affiliation = null;
        private MadsModel.MadsDateValid dateValid = null;
        private MadsModel.MadsDescription description = null;
        private MadsModel.MadsEmail email = null;
        private MadsModel.MadsExtension extension = null;
        private MadsModel.MadsFax fax = null;
        private MadsModel.MadsHours hours = null;
        private MadsModel.MadsIdentifier identifier = null;
        private MadsModel.MadsNamePart namePart = null;
        private MadsModel.MadsNote note = null;
        
        private MadsModel.MadsOrganization organization = null;
        private MadsModel.MadsPhone phone = null;
        private MadsModel.MadsPosition position = null;
        private MadsModel.MadsRecordInfo recordInfo = null;
        private MadsModel.MadsUrl url = null;
        private MadsModel.MadsFieldOfActivity fieldOfActivity = null;


        private String astreet = null;
        private String acity = null;
        private String astate = null;
        private String acountry = null;
        private String apostcode = null;



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
                    if(elementEquals(qName, TAG_MADS)) {
                        mads = new MadsModel();
                        mads.id = atts.getValue(ATTRIBUTE_ID);
                        mads.version = atts.getValue(ATTRIBUTE_VERSION);
                        state = STATE_MADS;
                    }
                    break;
                case STATE_MADS:
                    if(elementEquals(qName, TAG_AUTHORITY)) {
                        authority = mads.new MadsAuthority();
                        state = STATE_MADS_AUTHORITY;
                    }
                    else if(elementEquals(qName, TAG_RELATED)) {
                        related = mads.new MadsRelated();
                        state = STATE_MADS_RELATED;
                    }
                    else if(elementEquals(qName, TAG_VARIANT)) {
                        variant = mads.new MadsVariant();
                        state = STATE_MADS_VARIANT;
                    }
                    
                    // ***** Additional elements *****
                    else if(elementEquals(qName, TAG_NOTE)) {
                        note = mads.new MadsNote();
                        state = STATE_MADS_NOTE;
                    }
                    else if(elementEquals(qName, TAG_AFFILIATION)) {
                        affiliation = mads.new MadsAffiliation();
                        state = STATE_MADS_AFFILIATION;
                    }
                    else if(elementEquals(qName, TAG_URL)) {
                        url = mads.new MadsUrl();
                        url.dateLastAccessed = atts.getValue(ATTRIBUTE_DATELASTACCESSED);
                        url.displayLabel = atts.getValue(ATTRIBUTE_DISPLAYLABEL);
                        url.note = atts.getValue(ATTRIBUTE_NOTE);
                        url.usage = atts.getValue(ATTRIBUTE_USAGE);
                        state = STATE_MADS_URL;
                    }
                    else if(elementEquals(qName, TAG_IDENTIFIER)) {
                        identifier = mads.new MadsIdentifier();
                        identifier.type = atts.getValue(ATTRIBUTE_TYPE);
                        identifier.displayLabel = atts.getValue(ATTRIBUTE_DISPLAYLABEL);
                        identifier.invalid = atts.getValue(ATTRIBUTE_INVALID);
                        identifier.altRepGroup = atts.getValue(ATTRIBUTE_ALTREPGROUP);
                        state = STATE_MADS_IDENTIFIER;
                    }
                    else if(elementEquals(qName, TAG_FIELDOFACTIVITY)) {
                        fieldOfActivity = mads.new MadsFieldOfActivity();
                        state = STATE_MADS_FIELDOFACTIVITY;
                    }
                    else if(elementEquals(qName, TAG_EXTENSION))  {
                        extension = mads.new MadsExtension();
                        extension.displayLabel = atts.getValue(ATTRIBUTE_DISPLAYLABEL);
                        state = STATE_MADS_EXTENSION;
                    }
                    else if(elementEquals(qName, TAG_RECORDINFO))  {
                        recordInfo = mads.new MadsRecordInfo();
                        recordInfo.displayLabel = atts.getValue(ATTRIBUTE_DISPLAYLABEL);
                        recordInfo.lang = atts.getValue(ATTRIBUTE_LANG);
                        recordInfo.script = atts.getValue(ATTRIBUTE_SCRIPT);
                        recordInfo.transliteration = atts.getValue(ATTRIBUTE_TRANSLITERATION);
                        state = STATE_MADS_RECORDINFO;
                    }
                    break;


                case STATE_MADS_AUTHORITY:
                    if(elementEquals(qName, TAG_NAME)) {
                        name = mads.new MadsName();
                        name.type = atts.getValue(ATTRIBUTE_TYPE);
                        name.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_NAME;
                    }
                    else if(elementEquals(qName, TAG_TITLEINFO)) {
                        titleInfo = mads.new MadsTitleInfo();
                        titleInfo.type = atts.getValue(ATTRIBUTE_TYPE);
                        titleInfo.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_TITLEINFO;
                    }
                    else if(elementEquals(qName, TAG_TOPIC)) {
                        topic = mads.new MadsTopic();
                        topic.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_TOPIC;
                    }
                    else if(elementEquals(qName, TAG_TEMPORAL)) {
                        temporal = mads.new MadsTemporal();
                        temporal.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_TEMPORAL;
                    }
                    else if(elementEquals(qName, TAG_GENRE)) {
                        genre = mads.new MadsGenre();
                        genre.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_GENRE;
                    }
                    else if(elementEquals(qName, TAG_GEOGRAPHIC)) {
                        geographic = mads.new MadsGeographic();
                        geographic.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_GEOGRAPHIC;
                    }
                    else if(elementEquals(qName, TAG_HIERARCHICALGEOGRAPHIC)) {
                        hierarchicalGeographic = mads.new MadsHierarchicalGeographic();
                        hierarchicalGeographic.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_HIERARCHICALGEOGRAPHIC;
                    }
                    else if(elementEquals(qName, TAG_OCCUPATION)) {
                        occupation = mads.new MadsOccupation();
                        occupation.authority = atts.getValue(ATTRIBUTE_AUTHORITY);
                        state = STATE_MADS_AUTHORITY_OCCUPATION;
                    }
                    break;

                case STATE_MADS_RELATED:
                    if(elementEquals(qName, TAG_NAME)) {
                        name = mads.new MadsName();
                        state = STATE_MADS_RELATED_NAME;
                    }
                    else if(elementEquals(qName, TAG_TITLEINFO)) {
                        titleInfo = mads.new MadsTitleInfo();
                        state = STATE_MADS_RELATED_TITLEINFO;
                    }
                    else if(elementEquals(qName, TAG_TOPIC)) {
                        topic = mads.new MadsTopic();
                        state = STATE_MADS_RELATED_TOPIC;
                    }
                    else if(elementEquals(qName, TAG_TEMPORAL)) {
                        temporal = mads.new MadsTemporal();
                        state = STATE_MADS_RELATED_TEMPORAL;
                    }
                    else if(elementEquals(qName, TAG_GENRE)) {
                        genre = mads.new MadsGenre();
                        state = STATE_MADS_RELATED_GENRE;
                    }
                    else if(elementEquals(qName, TAG_GEOGRAPHIC)) {
                        geographic = mads.new MadsGeographic();
                        state = STATE_MADS_RELATED_GEOGRAPHIC;
                    }
                    else if(elementEquals(qName, TAG_HIERARCHICALGEOGRAPHIC)) {
                        hierarchicalGeographic = mads.new MadsHierarchicalGeographic();
                        state = STATE_MADS_RELATED_HIERARCHICALGEOGRAPHIC;
                    }
                    else if(elementEquals(qName, TAG_OCCUPATION)) {
                        occupation = mads.new MadsOccupation();
                        state = STATE_MADS_RELATED_OCCUPATION;
                    }
                    break;

                case STATE_MADS_VARIANT:
                    if(elementEquals(qName, TAG_NAME)) {
                        name = mads.new MadsName();
                        state = STATE_MADS_VARIANT_NAME;
                    }
                    else if(elementEquals(qName, TAG_TITLEINFO)) {
                        titleInfo = mads.new MadsTitleInfo();
                        state = STATE_MADS_VARIANT_TITLEINFO;
                    }
                    else if(elementEquals(qName, TAG_TOPIC)) {
                        topic = mads.new MadsTopic();
                        state = STATE_MADS_VARIANT_TOPIC;
                    }
                    else if(elementEquals(qName, TAG_TEMPORAL)) {
                        temporal = mads.new MadsTemporal();
                        state = STATE_MADS_VARIANT_TEMPORAL;
                    }
                    else if(elementEquals(qName, TAG_GENRE)) {
                        genre = mads.new MadsGenre();
                        state = STATE_MADS_VARIANT_GENRE;
                    }
                    else if(elementEquals(qName, TAG_GEOGRAPHIC)) {
                        geographic = mads.new MadsGeographic();
                        state = STATE_MADS_VARIANT_GEOGRAPHIC;
                    }
                    else if(elementEquals(qName, TAG_HIERARCHICALGEOGRAPHIC)) {
                        hierarchicalGeographic = mads.new MadsHierarchicalGeographic();
                        state = STATE_MADS_VARIANT_HIERARCHICALGEOGRAPHIC;
                    }
                    else if(elementEquals(qName, TAG_OCCUPATION)) {
                        occupation = mads.new MadsOccupation();
                        state = STATE_MADS_VARIANT_OCCUPATION;
                    }
                    break;

               
                    
                // ***** NAMES *****

                case STATE_MADS_AUTHORITY_NAME:
                    if(elementEquals(qName, TAG_NAMEPART)) {
                        namePart = mads.new MadsNamePart();
                        state = STATE_MADS_AUTHORITY_NAME_NAMEPART;
                    }
                    else if(elementEquals(qName, TAG_DESCRIPTION))  {
                        description = mads.new MadsDescription();
                        state = STATE_MADS_AUTHORITY_NAME_DESCRIPTION;
                    }
                    break;


                case STATE_MADS_VARIANT_NAME:
                    if(elementEquals(qName, TAG_NAMEPART)) {
                        namePart = mads.new MadsNamePart();
                        state = STATE_MADS_VARIANT_NAME_NAMEPART;
                    }
                    else if(elementEquals(qName, TAG_DESCRIPTION))  {
                        description = mads.new MadsDescription();
                        state = STATE_MADS_VARIANT_NAME_DESCRIPTION;
                    }
                    break;

                case STATE_MADS_RELATED_NAME:
                    if(elementEquals(qName, TAG_NAMEPART)) {
                        namePart = mads.new MadsNamePart();
                        state = STATE_MADS_RELATED_NAME_NAMEPART;
                    }
                    else if(elementEquals(qName, TAG_DESCRIPTION))  {
                        description = mads.new MadsDescription();
                        state = STATE_MADS_RELATED_NAME_DESCRIPTION;
                    }
                    break;



               // ****** AFFILIATION ******
               case STATE_MADS_AFFILIATION:
                    if(elementEquals(qName, TAG_ORGANIZATION)) {
                        organization = mads.new MadsOrganization();
                        state = STATE_MADS_AFFILIATION_ORGANIZATION;
                    }
                    else if(elementEquals(qName, TAG_POSITION))  {
                        position = mads.new MadsPosition();
                        state = STATE_MADS_AFFILIATION_POSITION;
                    }
                    else if(elementEquals(qName, TAG_ADDRESS))  {
                        address = mads.new MadsAddress();
                        state = STATE_MADS_AFFILIATION_ADDRESS;
                    }
                    else if(elementEquals(qName, TAG_EMAIL))  {
                        email = mads.new MadsEmail();
                        state = STATE_MADS_AFFILIATION_EMAIL;
                    }
                    else if(elementEquals(qName, TAG_PHONE))  {
                        phone = mads.new MadsPhone();
                        state = STATE_MADS_AFFILIATION_PHONE;
                    }
                    else if(elementEquals(qName, TAG_FAX))  {
                        fax = mads.new MadsFax();
                        state = STATE_MADS_AFFILIATION_FAX;
                    }
                    else if(elementEquals(qName, TAG_HOURS))  {
                        hours = mads.new MadsHours();
                        state = STATE_MADS_AFFILIATION_HOURS;
                    }
                    else if(elementEquals(qName, TAG_DATEVALID))  {
                        dateValid = mads.new MadsDateValid();
                        state = STATE_MADS_AFFILIATION_DATEVALID;
                    }
                    break;


                case STATE_MADS_AFFILIATION_ADDRESS:
                    if(elementEquals(qName, TAG_STREET)) {
                        astreet = "";
                        state = STATE_MADS_AFFILIATION_ADDRESS_STREET;
                    }
                    else if(elementEquals(qName, TAG_CITY))  {
                        acity = "";
                        state = STATE_MADS_AFFILIATION_ADDRESS_CITY;
                    }
                    else if(elementEquals(qName, TAG_STATE))  {
                        astate = "";
                        state = STATE_MADS_AFFILIATION_ADDRESS_STATE;
                    }
                    else if(elementEquals(qName, TAG_COUNTRY))  {
                        acountry = "";
                        state = STATE_MADS_AFFILIATION_ADDRESS_COUNTRY;
                    }
                    else if(elementEquals(qName, TAG_POSTCODE))  {
                        apostcode = "";
                        state = STATE_MADS_AFFILIATION_ADDRESS_POSTCODE;
                    }
                    break;
            }
        }





        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {

                // ****** VARIANT ******

                case STATE_MADS_VARIANT_NAME: {
                    if(elementEquals(qName, TAG_NAME)) {
                        state = STATE_MADS_VARIANT;
                    }
                    break;
                }
                case STATE_MADS_VARIANT_TITLEINFO: {
                    if(elementEquals(qName, TAG_TITLEINFO)) {
                        state = STATE_MADS_VARIANT;
                    }
                    break;
                }
                case STATE_MADS_VARIANT_TOPIC: {
                    if(elementEquals(qName, TAG_TOPIC)) {
                        state = STATE_MADS_VARIANT;
                    }
                    break;
                }
                case STATE_MADS_VARIANT_TEMPORAL: {
                    if(elementEquals(qName, TAG_TEMPORAL)) {
                        state = STATE_MADS_VARIANT;
                    }
                    break;
                }
                case STATE_MADS_VARIANT_GENRE: {
                    if(elementEquals(qName, TAG_GENRE)) {
                        state = STATE_MADS_VARIANT;
                    }
                    break;
                }
                case STATE_MADS_VARIANT_GEOGRAPHIC: {
                    if(elementEquals(qName, TAG_GEOGRAPHIC)) {
                        state = STATE_MADS_VARIANT;
                    }
                    break;
                }
                case STATE_MADS_VARIANT_HIERARCHICALGEOGRAPHIC: {
                    if(elementEquals(qName, TAG_HIERARCHICALGEOGRAPHIC)) {
                        state = STATE_MADS_VARIANT;
                    }
                    break;
                }

                // ****** RELATED ******

                case STATE_MADS_RELATED_NAME: {
                    if(elementEquals(qName, TAG_NAME)) {
                        state = STATE_MADS_RELATED;
                    }
                    break;
                }
                case STATE_MADS_RELATED_TITLEINFO: {
                    if(elementEquals(qName, TAG_TITLEINFO)) {
                        state = STATE_MADS_RELATED;
                    }
                    break;
                }
                case STATE_MADS_RELATED_TOPIC: {
                    if(elementEquals(qName, TAG_TOPIC)) {
                        state = STATE_MADS_RELATED;
                    }
                    break;
                }
                case STATE_MADS_RELATED_TEMPORAL: {
                    if(elementEquals(qName, TAG_TEMPORAL)) {
                        state = STATE_MADS_RELATED;
                    }
                    break;
                }
                case STATE_MADS_RELATED_GENRE: {
                    if(elementEquals(qName, TAG_GENRE)) {
                        state = STATE_MADS_RELATED;
                    }
                    break;
                }
                case STATE_MADS_RELATED_GEOGRAPHIC: {
                    if(elementEquals(qName, TAG_GEOGRAPHIC)) {
                        state = STATE_MADS_RELATED;
                    }
                    break;
                }
                case STATE_MADS_RELATED_HIERARCHICALGEOGRAPHIC: {
                    if(elementEquals(qName, TAG_HIERARCHICALGEOGRAPHIC)) {
                        state = STATE_MADS_RELATED;
                    }
                    break;
                }


                // ****** AUTHORITY ******

                
                case STATE_MADS_AUTHORITY_NAME: {
                    if(elementEquals(qName, TAG_NAME)) {
                        state = STATE_MADS_AUTHORITY;
                    }
                    break;
                }
                case STATE_MADS_AUTHORITY_TITLEINFO: {
                    if(elementEquals(qName, TAG_TITLEINFO)) {
                        state = STATE_MADS_AUTHORITY;
                    }
                    break;
                }
                case STATE_MADS_AUTHORITY_TOPIC: {
                    if(elementEquals(qName, TAG_TOPIC)) {
                        state = STATE_MADS_AUTHORITY;
                    }
                    break;
                }
                case STATE_MADS_AUTHORITY_TEMPORAL: {
                    if(elementEquals(qName, TAG_TEMPORAL)) {
                        state = STATE_MADS_AUTHORITY;
                    }
                    break;
                }
                case STATE_MADS_AUTHORITY_GENRE: {
                    if(elementEquals(qName, TAG_GENRE)) {
                        state = STATE_MADS_AUTHORITY;
                    }
                    break;
                }
                case STATE_MADS_AUTHORITY_GEOGRAPHIC: {
                    if(elementEquals(qName, TAG_GEOGRAPHIC)) {
                        state = STATE_MADS_AUTHORITY;
                    }
                    break;
                }
                case STATE_MADS_AUTHORITY_HIERARCHICALGEOGRAPHIC: {
                    if(elementEquals(qName, TAG_HIERARCHICALGEOGRAPHIC)) {
                        state = STATE_MADS_AUTHORITY;
                    }
                    break;
                }

                
                // ******* NAMES ******
                
                case STATE_MADS_AUTHORITY_NAME_NAMEPART: {
                    if(elementEquals(qName, TAG_NAMEPART)) {
                        state = STATE_MADS_AUTHORITY_NAME;
                    }
                    break;
                }

                case STATE_MADS_AUTHORITY_NAME_DESCRIPTION: {
                    if(elementEquals(qName, TAG_DESCRIPTION)) {
                        state = STATE_MADS_AUTHORITY_NAME;
                    }
                    break;
                }

                case STATE_MADS_RELATED_NAME_NAMEPART: {
                    if(elementEquals(qName, TAG_NAMEPART)) {
                        state = STATE_MADS_RELATED_NAME;
                    }
                    break;
                }

                case STATE_MADS_RELATED_NAME_DESCRIPTION: {
                    if(elementEquals(qName, TAG_DESCRIPTION)) {
                        state = STATE_MADS_RELATED_NAME;
                    }
                    break;
                }

                case STATE_MADS_VARIANT_NAME_NAMEPART: {
                    if(elementEquals(qName, TAG_NAMEPART)) {
                        state = STATE_MADS_VARIANT_NAME;
                    }
                    break;
                }

                case STATE_MADS_VARIANT_NAME_DESCRIPTION: {
                    if(elementEquals(qName, TAG_DESCRIPTION)) {
                        state = STATE_MADS_VARIANT_NAME;
                    }
                    break;
                }


                // ****** AFFILIATION *******

                case STATE_MADS_AFFILIATION_ORGANIZATION: {
                    if(elementEquals(qName, TAG_ORGANIZATION)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_POSITION: {
                    if(elementEquals(qName, TAG_POSITION)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_ADDRESS: {
                    if(elementEquals(qName, TAG_ADDRESS)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_EMAIL: {
                    if(elementEquals(qName, TAG_EMAIL)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_PHONE: {
                    if(elementEquals(qName, TAG_PHONE)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_FAX: {
                    if(elementEquals(qName, TAG_FAX)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_HOURS: {
                    if(elementEquals(qName, TAG_HOURS)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_DATEVALID: {
                    if(elementEquals(qName, TAG_DATEVALID)) {
                        state = STATE_MADS_AFFILIATION;
                    }
                    break;
                }


                // ****** AFFILIATION - ADDRESS ******

                case STATE_MADS_AFFILIATION_ADDRESS_STREET: {
                    if(elementEquals(qName, TAG_STREET)) {
                        state = STATE_MADS_AFFILIATION_ADDRESS;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_ADDRESS_CITY: {
                    if(elementEquals(qName, TAG_CITY)) {
                        state = STATE_MADS_AFFILIATION_ADDRESS;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_ADDRESS_STATE: {
                    if(elementEquals(qName, TAG_STATE)) {
                        state = STATE_MADS_AFFILIATION_ADDRESS;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_ADDRESS_COUNTRY: {
                    if(elementEquals(qName, TAG_COUNTRY)) {
                        state = STATE_MADS_AFFILIATION_ADDRESS;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION_ADDRESS_POSTCODE: {
                    if(elementEquals(qName, TAG_POSTCODE)) {
                        state = STATE_MADS_AFFILIATION_ADDRESS;
                    }
                    break;
                }

                // ****** MADS >  ******


                case STATE_MADS_AUTHORITY: {
                    if(elementEquals(qName, TAG_AUTHORITY)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_RELATED: {
                    if(elementEquals(qName, TAG_RELATED)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_VARIANT: {
                    if(elementEquals(qName, TAG_VARIANT)) {
                        state = STATE_MADS;
                    }
                    break;
                }


                case STATE_MADS_NOTE: {
                    if(elementEquals(qName, TAG_NOTE)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_AFFILIATION: {
                    if(elementEquals(qName, TAG_AFFILIATION)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_URL: {
                    if(elementEquals(qName, TAG_URL)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_IDENTIFIER: {
                    if(elementEquals(qName, TAG_IDENTIFIER)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_FIELDOFACTIVITY: {
                    if(elementEquals(qName, TAG_FIELDOFACTIVITY)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_EXTENSION: {
                    if(elementEquals(qName, TAG_EXTENSION)) {
                        state = STATE_MADS;
                    }
                    break;
                }
                case STATE_MADS_RECORDINFO: {
                    if(elementEquals(qName, TAG_RECORDINFO)) {
                        state = STATE_MADS;
                    }
                    break;
                }
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            String str = new String(ch,start,length);
            try {
                str = new String(str.getBytes(), defaultEncoding);
            }
            catch(Exception e) {}
            switch(state) {
                case STATE_MADS_IDENTIFIER: {
                    //data_identifier += str;
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


        private boolean elementEquals(String e, String str) {
            if(e != null && str != null) {
                if(e.equals(str)) return true;
                if(e.indexOf(":") > -1) {
                    String[] eParts = e.split(":");
                    if(str.equals(eParts[eParts.length-1])) return true;
                }
            }
            return false;
        }
    }



    



    // -------------------------------------------------------------------------




    // -------------------------------------------------------------------------



    // -------------------------------------------------------------------------




    // ---


    public Topic getAuthorityType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, AUTHORITY_SI, "Authority (MADS)", getMadsClass(tm));
    }

    // ---


    public Topic getMadsClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, MADS_SI, "MADS", getWandoraClass(tm) );
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


    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        return getOrCreateTopic(tm, si, bn, null);
    }
    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn, Topic type) throws TopicMapException {
        if(si!=null){
            Topic t=tm.getTopic(si);
            if(t==null){
                t=tm.createTopic();
                t.addSubjectIdentifier(tm.createLocator(si));
                if(bn != null) t.setBaseName(bn);
                if(type != null) t.addType(type);
            }
            return t;
        }
        else {
            Topic t=tm.getTopicWithBaseName(bn);
            if(t==null){
                t=tm.createTopic();
                if(bn!=null) t.setBaseName(bn);
                if(si!=null) t.addSubjectIdentifier(tm.createLocator(si));
                else t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                if(type != null) t.addType(type);
            }
            return t;
        }
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





    // ----------------


}
