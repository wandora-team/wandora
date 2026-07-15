/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://www.wandora.org/
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
 * MarcXMLExtractor.java
 *
 * Created on 2010-06-30
 *
 */


package org.wandora.application.tools.extractors.marcxml;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicTools;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 *
 * @author akivela
 */

public class MarcXMLExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;

	public static boolean TRIM_DATAS = false;

    public static boolean INCLUDE_INDX_IN_ASSOCIATIONS = true;
    public static boolean SOLVE_FIELD_NAMES = true;
    public static boolean SOLVE_SUBFIELD_NAMES = true;
    public static boolean CONVERT_LEADERS = true;

    public static String EXCLUDE_FIELDS = "";
    public static String INCLUDE_FIELDS = "";
    public static String EXCLUDE_SUBFIELDS = "";
    public static String INCLUDE_SUBFIELDS = "";

    public static String RECORD_SI_PATTERN = "";
    public static String BASENAME_PATTERN = "";

    protected static String MARC_SI = "http://www.loc.gov/marc/bibliographic/bdintro.html";
    protected static String IND_SI = "http://www.loc.gov/marc/bibliographic/bdintro.html#indicator";
    protected static String SUBFIELDCODE_SI = "http://www.loc.gov/marc/bibliographic/bdintro.html#subfield";

    protected static String LEADER_SI = "http://www.loc.gov/marc/bibliographic/bdleader.html";

    protected static String FIELD_SI = "http://www.loc.gov/marc/bibliographic/";
    protected static String FIELD_SI_TEMPLATE = "http://www.loc.gov/marc/bibliographic/bd___x___.html";
   

    protected static String DATA_SI = "http://www.wandora.org/marcxml/data";
    protected static String RECORD_SI = "http://www.wandora.org/marcxml/record";

    private static String defaultEncoding = "UTF-8"; //"ISO-8859-1";
    private static String defaultLang = "en";

    private Map<String,String> excludeFields = null;
    private Map<String,String> includeFields = null;
    private Map<String,String> excludeSubfields = null;
    private Map<String,String> includeSubfields = null;

    private List<String> recordSIPatterns = null;
    private List<String> basenamePatterns = null;



    /** Creates a new instance of MarcXMLExtractor */
    public MarcXMLExtractor() {
    }


    @Override
    public String getName() {
        return "MarcXML extractor";
    }

    @Override
    public String getDescription(){
        return "Converts MarcXML documents to topics maps.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_marcxml.png");
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


    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"MARCXML extract options","MARCXML extract options",true,new String[][]{
            new String[] {
                "Record SI patterns",
                "string",
                RECORD_SI_PATTERN,
                "Comma separated list of subject identifier patterns for records. Refer MARCXML (sub)fields with tags like ___a@035___. Example: http://www.wandora.org/si/___a@035___",
            },
            new String[] {
                "Record base name patterns",
                "string",
                BASENAME_PATTERN,
                "Comma separated list of base name patterns for records. Refer MARCXML (sub)fields with tags like ___a@245___. Example: ___a@245___ - ___b@245___",
            },
            new String[] {
                "Trim datas",
                "boolean",
                (TRIM_DATAS ? "true" : "false"),
                "Should extractor remove trailing special characters in data fields."
            },
            new String[] {
                "Solve field names",
                "boolean",
                (SOLVE_FIELD_NAMES ? "true" : "false"),
                "Should extractor solve field names? Fields are converted to association types"
            },
            new String[] {
                "Solve subfield names",
                "boolean",
                (SOLVE_SUBFIELD_NAMES ? "true" : "false"),
                "Should extractor solve subfield names? Subfields are converted to association roles."
            },
            new String[] {
                "Convert MARC indicators",
                "boolean",
                (INCLUDE_INDX_IN_ASSOCIATIONS ? "true" : "false"),
                "Should extractor add ind1 and ind2 attributes of MARCXML to associations as players?"
            },
            new String[] {
                "Convert leaders",
                "boolean",
                (CONVERT_LEADERS ? "true" : "false"),
                "Should extractor convert record leaders also?"
            },
            new String[] {
                "Include fields",
                "string",
                INCLUDE_FIELDS,
                "Comma separated list of included field codes"
            },
            new String[] {
                "Exclude fields",
                "string",
                EXCLUDE_FIELDS,
                "Comma separated list of excluded field codes"
            },
            new String[] {
                "Include subfields",
                "string",
                INCLUDE_SUBFIELDS,
                "Comma separated list of included subfield codes"
            },
            new String[] {
                "Exclude subfields",
                "string",
                EXCLUDE_SUBFIELDS,
                "Comma separated list of excluded subfield codes"
            },
            new String[] {
                "MARCXML encoding",
                "string",
                defaultEncoding,
                "Encoding of MARCXML file"
            },
            new String[] {
                "Default language",
                "string",
                defaultLang,
                "Language used in occurrences for example. Use two letter acronyms."
            }
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;

        Map<String, String> values = god.getValues();

        parseSIPatterns( values.get("Record SI patterns") );
        parseBasenamePatterns( values.get("Record base name patterns"));

        CONVERT_LEADERS = ("true".equals(values.get("Convert leaders")) ? true : false );
        TRIM_DATAS = ("true".equals(values.get("Trim datas")) ? true : false );
        SOLVE_FIELD_NAMES = ("true".equals(values.get("Solve field names")) ? true : false );
        SOLVE_SUBFIELD_NAMES = ("true".equals(values.get("Solve subfield names")) ? true : false );
        INCLUDE_INDX_IN_ASSOCIATIONS = ("true".equals(values.get("Convert MARC indicators")) ? true : false );
        INCLUDE_FIELDS = values.get("Include fields");
        EXCLUDE_FIELDS = values.get("Exclude fields");
        INCLUDE_SUBFIELDS = values.get("Include subfields");
        EXCLUDE_SUBFIELDS = values.get("Exclude subfields");

        excludeFields = parseFieldCodes(EXCLUDE_FIELDS);
        includeFields = parseFieldCodes(INCLUDE_FIELDS);
        excludeSubfields = parseSubfieldCodes(EXCLUDE_SUBFIELDS);
        includeSubfields = parseSubfieldCodes(INCLUDE_SUBFIELDS);

        defaultEncoding = values.get("MARCXML encoding");
        defaultLang = values.get("Default language");
    }


    private Map<String,String> parseFieldCodes(String str) {
        String[] strs = str.split(",");
        Map<String,String> codes = new LinkedHashMap<>();
        for(int i=0; i<strs.length; i++) {
            String s = strs[i];
            s.trim();
            if(s.length() > 0) {
                if(s.indexOf("-") > -1) {
                    int si0 = 0;
                    int si1 = 999;
                    String sa[] = s.split("-");
                    String s0 = sa[0].trim();
                    String s1 = sa[1].trim();
                    try { si0 = Integer.parseInt( s0 ); }
                    catch(Exception e) {}
                    try { si1 = Integer.parseInt( s1 ); }
                    catch(Exception e) {}
                    for(int j=si0; j<=si1; j++) {
                        String code = ""+j;
                        if(code.length() == 1) code = "00"+code;
                        else if(code.length() == 2) code = "0"+code;
                        codes.put(code, code);
                        System.out.println("field code: "+code);
                    }
                }
                else {
                    codes.put(s,s);
                }
            }
        }
        return codes;
    }




    public Map<String,String> parseSubfieldCodes(String str) {
        String[] strs = str.split(",");
        Map<String,String> codes = new LinkedHashMap<>();
        for(int i=0; i<strs.length; i++) {
            String s = strs[i];
            s.trim();
            if(s.length() > 0) {
                codes.put(s,s);
            }
        }
        return codes;
    }



    public void parseSIPatterns(String patterns) {
        RECORD_SI_PATTERN = patterns;
        recordSIPatterns = new ArrayList<>();
        if(RECORD_SI_PATTERN != null) {
            String[] siPatterns = RECORD_SI_PATTERN.split(",");
            String siPattern = null;
            for(int i=0; i<siPatterns.length; i++) {
                siPattern = siPatterns[i];
                siPattern.trim();
                if(siPattern.length() > 0) {
                    recordSIPatterns.add(siPattern);
                }
            }
        }
    }


    public void parseBasenamePatterns(String patterns) {
        BASENAME_PATTERN = patterns;
        basenamePatterns = new ArrayList<>();
        if(BASENAME_PATTERN != null) {
            String[] namePatterns = BASENAME_PATTERN.split(",");
            String namePattern = null;
            for(int i=0; i<namePatterns.length; i++) {
                namePattern = namePatterns[i];
                namePattern.trim();
                if(namePattern.length() > 0) {
                    basenamePatterns.add(namePattern);
                }
            }
        }
    }



    
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
            
            parseSIPatterns( RECORD_SI_PATTERN );
            parseBasenamePatterns( BASENAME_PATTERN );

            excludeFields = parseFieldCodes(EXCLUDE_FIELDS);
            includeFields = parseFieldCodes(INCLUDE_FIELDS);
            excludeSubfields = parseSubfieldCodes(EXCLUDE_SUBFIELDS);
            includeSubfields = parseSubfieldCodes(INCLUDE_SUBFIELDS);

            if(!excludeFields.isEmpty()) {
                log("Exclude fields "+EXCLUDE_FIELDS);
            }
            else if(!includeFields.isEmpty()) {
                log("Include fields "+INCLUDE_FIELDS);
            }

            if(!excludeSubfields.isEmpty()) {
                log("Exclude subfields "+EXCLUDE_SUBFIELDS);
            }
            else if(!includeSubfields.isEmpty()) {
                log("Include subfields "+INCLUDE_SUBFIELDS);
            }

            // ---- Parse results ----
            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            MarcXMLParser parserHandler = new MarcXMLParser(topicMap, this);
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



    private class MarcXMLParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {


        public MarcXMLParser(TopicMap tm, MarcXMLExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }



        public int progress=0;
        private TopicMap tm;
        private MarcXMLExtractor parent;

        public static final String TAG_COLLECTION = "collection";
        public static final String TAG_RECORD = "record";

        public static final String TAG_LEADER = "leader";
        public static final String TAG_CONTROLFIELD = "controlfield";

        public static final String TAG_DATAFIELD = "datafield";
        public static final String TAG_SUBFIELD = "subfield";



        private static final int STATE_START=0;
        private static final int STATE_COLLECTION=2;
        private static final int STATE_COLLECTION_RECORD=4;
        private static final int STATE_COLLECTION_RECORD_LEADER=41;
        private static final int STATE_COLLECTION_RECORD_CONTROLFIELD=42;
        private static final int STATE_COLLECTION_RECORD_DATAFIELD=43;
        private static final int STATE_COLLECTION_RECORD_DATAFIELD_SUBFIELD=431;
        

        private int state=STATE_START;

        private String data_leader = null;

        private String data_controlfield = null;
        private String data_controlfield_tag = null;
        private String data_datafield = null; // NOT USED!
        private MarcField datafield = null;
        private String data_subfield = null;
        
        private String data_datafield_tag = null;
        private String data_datafield_ind1 = null;
        private String data_datafield_ind2 = null;
        private String data_subfield_code = null;

        private List<MarcField> data_datafields = null;
        private Map<String,String> data_controlfields = null;

        private List<String> subjectIdentifiers = null;
        private List<String> basenames = null;


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
                    if(elementEquals(qName, TAG_COLLECTION)) {
                        state = STATE_COLLECTION;
                    }
                    break;
                case STATE_COLLECTION:
                    if(elementEquals(qName, TAG_RECORD)) {
                        data_leader = "";
                        data_controlfield = "";
                        data_datafield = "";
                        data_subfield = "";

                        data_controlfields = new LinkedHashMap<>();
                        data_datafields = new ArrayList<>();

                        subjectIdentifiers = new ArrayList<>();
                        if(recordSIPatterns != null && !recordSIPatterns.isEmpty()) {
                            for(String recordSIPattern : recordSIPatterns) {
                                if(recordSIPattern != null && recordSIPattern.length() > 0) {
                                    subjectIdentifiers.add(recordSIPattern);
                                }
                            }
                        }
                        basenames = new ArrayList<String>();
                        if(basenamePatterns != null && !basenamePatterns.isEmpty()) {
                            for(String namePattern : basenamePatterns) {
                                if(namePattern != null && namePattern.length() > 0) {
                                    basenames.add(namePattern);
                                }
                            }
                        }
                        state = STATE_COLLECTION_RECORD;
                    }
                    break;
                case STATE_COLLECTION_RECORD:
                    if(elementEquals(qName, TAG_LEADER)) {
                        data_leader = "";
                        state = STATE_COLLECTION_RECORD_LEADER;
                    }
                    else if(elementEquals(qName, TAG_CONTROLFIELD)) {
                        data_controlfield = "";
                        data_controlfield_tag = atts.getValue("tag");
                        state = STATE_COLLECTION_RECORD_CONTROLFIELD;
                    }
                    else if(elementEquals(qName, TAG_DATAFIELD)) {
                        data_datafield = "";
                        data_datafield_tag = atts.getValue("tag");
                        data_datafield_ind1 = atts.getValue("ind1");
                        data_datafield_ind2 = atts.getValue("ind2");

                        datafield = new MarcField(data_datafield_tag, data_datafield_ind1, data_datafield_ind2);
                        state = STATE_COLLECTION_RECORD_DATAFIELD;
                    }
                    break;
                case STATE_COLLECTION_RECORD_DATAFIELD:
                    if(elementEquals(qName, TAG_SUBFIELD)) {
                        data_subfield = "";
                        data_subfield_code = atts.getValue("code");
                        state = STATE_COLLECTION_RECORD_DATAFIELD_SUBFIELD;
                    }
                    break;
                
            }
        }



        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_COLLECTION_RECORD_DATAFIELD_SUBFIELD: {
                    if(elementEquals(qName, TAG_SUBFIELD)) {
                        String subfieldCode = data_subfield_code.trim();
                        if(datafield != null) {
                            if(!excludeSubfields.containsKey(subfieldCode)) {
                                if(!includeSubfields.isEmpty() && includeSubfields.containsKey(subfieldCode)) {
                                    datafield.addSubfield(subfieldCode, data_subfield);
                                }
                                else if(includeSubfields.isEmpty()) {
                                    datafield.addSubfield(subfieldCode, data_subfield);
                                }
                            }
                        }

                        // BUILD SIs
                        String fieldCode = data_datafield_tag.trim();
                        if(subjectIdentifiers != null && subjectIdentifiers.size() > 0) {
                            try {
                                List<String> updatedSubjectIdentifiers = new ArrayList<String>();
                                for(String si : subjectIdentifiers) {
                                    if(si != null) {
                                        if( si.indexOf( "___"+subfieldCode+"@"+fieldCode+"___" ) > -1) {
                                            try {
                                                si = si.replaceAll("___"+subfieldCode+"@"+fieldCode+"___", URLEncoder.encode(data_subfield, defaultEncoding));
                                            }
                                            catch(Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        updatedSubjectIdentifiers.add(si);
                                    }
                                }
                                subjectIdentifiers = updatedSubjectIdentifiers;
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // BUILD BASENAMEs
                        if(basenames != null && basenames.size() > 0) {
                            try {
                                List<String> updatedBasenames = new ArrayList<>();
                                for(String n : basenames) {
                                    if(n != null) {
                                        if( n.indexOf( "___"+subfieldCode+"@"+fieldCode+"___" ) > -1) {
                                            try {
                                                n = n.replaceAll("___"+subfieldCode+"@"+fieldCode+"___", data_subfield);
                                            }
                                            catch(Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        updatedBasenames.add(n);
                                    }
                                }
                                basenames = updatedBasenames;
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }

                        state = STATE_COLLECTION_RECORD_DATAFIELD;
                    }
                    break;
                }
                case STATE_COLLECTION_RECORD_DATAFIELD: {
                    if(elementEquals(qName, TAG_DATAFIELD)) {
                        String fieldCode = data_datafield_tag.trim();
                        if(!excludeFields.containsKey(fieldCode)) {
                            if(!includeFields.isEmpty() && includeFields.containsKey(fieldCode)) {
                                //System.out.println("Adding a field: "+fieldCode);
                                data_datafields.add(datafield);
                            }
                            else if(includeFields.isEmpty()) {
                                //System.out.println("Adding a field: "+fieldCode);
                                data_datafields.add(datafield);
                            }
                        }
                        state = STATE_COLLECTION_RECORD;
                    }
                    break;
                }
                case STATE_COLLECTION_RECORD_CONTROLFIELD: {
                    if(elementEquals(qName, TAG_CONTROLFIELD)) {
                        String fieldCode = data_controlfield_tag.trim();
                        if(!excludeFields.containsKey(fieldCode)) {
                            if(!includeFields.isEmpty() && includeFields.containsKey(fieldCode)) {
                                //System.out.println("Adding a control field: "+fieldCode);
                                data_controlfields.put(fieldCode, data_controlfield);
                            }
                            else if(includeFields.isEmpty()) {
                                //System.out.println("Adding a control field: "+fieldCode);
                                data_controlfields.put(fieldCode, data_controlfield);
                            }
                        }
                        state = STATE_COLLECTION_RECORD;
                    }
                    break;
                }

                case STATE_COLLECTION_RECORD_LEADER: {
                    if(elementEquals(qName, TAG_LEADER)) {
                        state = STATE_COLLECTION_RECORD;
                    }
                    break;
                }
                case STATE_COLLECTION_RECORD: {
                    if(elementEquals(qName, TAG_RECORD)) {
                        progress++;
                        setProgress(progress / 100);
                        if(progress % 100 == 0) hlog("Found "+progress+" MARCXML records.");
                        topicalize(data_leader, data_datafields, data_controlfields, subjectIdentifiers, basenames, tm);
                        state = STATE_COLLECTION;
                    }
                    break;
                }
                case STATE_COLLECTION: {
                    if(elementEquals(qName, TAG_COLLECTION)) {
                        state = STATE_START;
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
                case STATE_COLLECTION_RECORD_LEADER: {
                    data_leader += str;
                    break;
                }
                case STATE_COLLECTION_RECORD_CONTROLFIELD: {
                    data_controlfield += str;
                    break;
                }
                case STATE_COLLECTION_RECORD_DATAFIELD: {
                    data_datafield += str;
                    break;
                }
                case STATE_COLLECTION_RECORD_DATAFIELD_SUBFIELD: {
                    data_subfield += str;
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




    private String[] titleCodes = new String[] {
        "220"
    };
    private String[] authorCodes = new String[] {
        "100"
    };


    protected void topicalize(String leader, List<MarcField> datafields, Map<String,String> controlfields, List<String> subjectIdentifiers, List<String> basenames, TopicMap tm) {
        if(!(leader == null && datafields.isEmpty() && controlfields.isEmpty() && tm == null && subjectIdentifiers.isEmpty() && basenames.isEmpty())) {
            try {
                String recordHook = null;
                Topic recordTopic = tm.createTopic();
                Topic recordTypeTopic = getRecordType(tm);
                boolean requiresAdditionalSI = true;

                if(subjectIdentifiers != null && subjectIdentifiers.size() > 0) {
                    for(String si : subjectIdentifiers ) {
                        if(si != null) {
                            if(si.indexOf("___") == -1) {
                                recordTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(si));
                                requiresAdditionalSI = false;
                                recordHook = si;
                            }
                        }
                    }
                    recordTopic = tm.getTopic(recordHook);
                }
                if(requiresAdditionalSI) {
                    org.wandora.topicmap.Locator si = TopicTools.createDefaultLocator();
                    recordTopic.addSubjectIdentifier(si);
                    recordHook = si.toExternalForm();
                }

                if(basenames != null && basenames.size() > 0) {
                    String bn = null;
                    for(String n : basenames ) {
                        if(n != null) {
                            if(n.indexOf("___") == -1) {
                                recordTopic.setBaseName( n );
                                bn = n;
                            }
                        }
                    }
                    recordTopic = tm.getTopicWithBaseName(bn);
                }

                recordTopic.addType( recordTypeTopic );

                // **** LEADER
                if(CONVERT_LEADERS && leader != null && leader.length() > 0) {
                    Topic leaderType = getLeaderType(tm);
                    Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang(defaultLang));
                    recordTopic.setData(leaderType, langTopic, leader);
                }

                // **** PROCESS CONTROL FIELDS
                if(controlfields != null && !controlfields.isEmpty()) {
                    for(String controlfield : controlfields.keySet()) {
                        String controlvalue = controlfields.get(controlfield);
                        if( controlfield != null && controlvalue != null) {
                            processControlField( controlfield, controlvalue, recordTopic, recordTypeTopic, tm);
                        }
                    }
                }

                // **** PROCESS DATA FIELDS
                if(datafields != null && !datafields.isEmpty()) {
                    for( MarcField datafield : datafields ) {
                        if(datafield != null) {

                            // *** DEFAULT CONVERSION BRANCH ***
                            String tag = datafield.getTag();
                            if(tag != null && tag.length() > 0) {
                                Topic tagTopic = getFieldTopic(tag, tm);

                                Topic ind1Topic = null;
                                Topic ind1Type = null;
                                Topic ind2Topic = null;
                                Topic ind2Type = null;
                                if(INCLUDE_INDX_IN_ASSOCIATIONS) {
                                    String ind1 = datafield.getInd1();
                                    if( ind1 != null ) {
                                        ind1 = ind1.trim();
                                        if( ind1.length() > 0 ) {
                                            ind1Topic = getInd1Topic( ind1.trim(), tag, tm );
                                            ind1Type = getInd1Type( tag, tm );
                                        }
                                    }
                                    String ind2 = datafield.getInd2();
                                    if( ind2 != null ) {
                                        ind2 = ind2.trim();
                                        if( ind2.length() > 0 ) {
                                            ind2Topic = getInd2Topic( ind2.trim(), tag, tm );
                                            ind2Type = getInd2Type( tag, tm );
                                        }
                                    }
                                }

                                // ****** CREATE ASSOCIATION
                                Association a = tm.createAssociation(tagTopic);
                                
                                recordTopic = tm.getTopic(recordHook);
                                a.addPlayer(recordTopic, recordTypeTopic);

                                Map<String,Integer> usedSubFields = new LinkedHashMap<>();
                                for( MarcSubfield subfield : datafield.getSubfields() ) {
                                    String subcode = subfield.getCode();
                                    String subvalue = subfield.getValue();
                                    if(usedSubFields.containsKey( subcode )) {
                                        //log("Warning: Subfield code '"+subcode+"' used more than once in same datafield '"+tag+"'.");
                                        Integer count = (Integer) usedSubFields.get(subcode);
                                        int counti = count.intValue();
                                        counti++;
                                        usedSubFields.put(subcode, Integer.valueOf(counti));

                                        Topic subfieldCodeTopic = getSubFieldCodeTopic( subcode, counti, tag, datafield.getInd1(), datafield.getInd2(), tm);
                                        Topic subfieldDataTopic = getSubFieldDataTopic( subvalue, tag, datafield.getInd1(), datafield.getInd2(), tm);
                                        if( subfieldCodeTopic != null && subfieldDataTopic != null) {
                                            a.addPlayer(subfieldDataTopic, subfieldCodeTopic);
                                        }
                                    }
                                    else {
                                        usedSubFields.put(subcode, Integer.valueOf(1));
                                        Topic subfieldCodeTopic = getSubFieldCodeTopic( subcode, tag, datafield.getInd1(), datafield.getInd2(), tm);
                                        Topic subfieldDataTopic = getSubFieldDataTopic( subvalue, tag, datafield.getInd1(), datafield.getInd2(), tm);
                                        if( subfieldCodeTopic != null && subfieldDataTopic != null) {
                                            a.addPlayer(subfieldDataTopic, subfieldCodeTopic);
                                        }
                                    }
                                }

                                if(INCLUDE_INDX_IN_ASSOCIATIONS) {
                                    if(ind1Topic != null && ind1Type != null) {
                                        a.addPlayer(ind1Topic, ind1Type);
                                    }
                                    if(ind2Topic != null && ind2Type != null) {
                                        a.addPlayer(ind2Topic, ind2Type);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }

        }
    }


    public void processControlField( String field, String data, Topic record, Topic type, TopicMap tm )  throws TopicMapException {
        Topic fieldTopic = getFieldTopic( field, tm );
        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang(defaultLang));
        record.setData(fieldTopic, langTopic, data);
    }


    



    // -------------------------------------------------------------------------


    public Topic getLeaderType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, LEADER_SI, "Leader (MARC)", getMARCClass(tm));
    }




    // --------------



    public Topic getFieldTopic(String field, TopicMap tm) throws TopicMapException {
        String bn = null;
        if(SOLVE_FIELD_NAMES) bn = MarcField.getFieldName(field);
        return getOrCreateTopic(tm, makeFieldSI(field), bn, getFieldType(tm));
    }

    public Topic getFieldType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, FIELD_SI, "Field (MARC)", getMARCClass(tm));
    }

    protected String makeFieldSI(String field) {
        if(field != null) {
            String fieldSI = FIELD_SI_TEMPLATE.replace("___x___", field);
            return fieldSI;
        }
        return null;
    }


    // ---


    public Topic getSubFieldCodeType( TopicMap tm ) throws TopicMapException {
        return getOrCreateTopic(tm, SUBFIELDCODE_SI, "Subfield code (MARC)", getMARCClass(tm));
    }


    
    public Topic getSubFieldCodeTopic( String subfied, int counter, String field, String ind1Modifier, String ind2Modifier, TopicMap tm) throws TopicMapException {
        String si = makeFieldSI(field) + "#" + subfied+"+"+counter;
        String bn = null;
        if(SOLVE_SUBFIELD_NAMES) bn = MarcSubfield.getSubfieldName( field, subfied ) + " ("+counter+")";
        return getOrCreateTopic(tm, si, bn, getSubFieldCodeType(tm));
    }


    public Topic getSubFieldCodeTopic( String subfied, String field, String ind1Modifier, String ind2Modifier, TopicMap tm) throws TopicMapException {
        String si = makeFieldSI(field) + "#" + subfied;
        String bn = null;
        if(SOLVE_SUBFIELD_NAMES) bn = MarcSubfield.getSubfieldName( field, subfied );
        return getOrCreateTopic(tm, si, bn, getSubFieldCodeType(tm));
    }



    public Topic getSubFieldDataTopic( String data, String tagModifier, String ind1Modifier, String ind2Modifier, TopicMap tm) throws TopicMapException {
        if(data != null) {
            data = data.trim();
            if(data.length() > 0) {
                String niceData = data.trim();
                niceData = niceData.replace("\n", " ");
                niceData = niceData.replace("\r", " ");
                niceData = niceData.replace("\t", " ");
                niceData = niceData.trim();
                if(TRIM_DATAS) {
                    if(niceData.endsWith(",")) niceData = niceData.substring(0, niceData.length()-1);
                    if(niceData.endsWith(";")) niceData = niceData.substring(0, niceData.length()-1);
                    if(niceData.endsWith(":")) niceData = niceData.substring(0, niceData.length()-1);
                    if(niceData.endsWith("/")) niceData = niceData.substring(0, niceData.length()-1);
                    if(niceData.endsWith("+")) niceData = niceData.substring(0, niceData.length()-1);
                    niceData = niceData.trim();

                    data = data.trim();
                    if(data.endsWith(",")) data = data.substring(0, data.length()-1);
                    if(data.endsWith(";")) data = data.substring(0, data.length()-1);
                    if(data.endsWith(":")) data = data.substring(0, data.length()-1);
                    if(data.endsWith("/")) data = data.substring(0, data.length()-1);
                    if(data.endsWith("+")) data = data.substring(0, data.length()-1);
                    data = data.trim();
                }
                if(niceData.length() > 128) {
                    niceData = niceData.substring(0, 128);
                    niceData = niceData + " ("+data.hashCode()+")";
                }
                Topic dataTopic=getOrCreateTopic(tm, DATA_SI+"/"+data.hashCode(), niceData);
                Topic dataTypeTopic = getDataType(tm);
                Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang(defaultLang));
                // **** Store original field data to an occurrence!
                dataTopic.setData(dataTypeTopic, langTopic, data);
                dataTopic.addType(dataTypeTopic);
                return dataTopic;
            }
        }
        return null;
    }

    public Topic getDataType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, DATA_SI, "Data (MARC)", getMARCClass(tm));
    }


    // ---


    public Topic getInd1Topic(String ind1, String tag, TopicMap tm) throws TopicMapException {
        String si = FIELD_SI_TEMPLATE.replace("___x___", tag);
        si = si+"#indicator1."+ind1;
        return getOrCreateTopic(tm, si, getIndicatorValueName(tag, "1", ind1), getInd1Type(tag, tm));
    }

    public Topic getInd1Type(String tag, TopicMap tm) throws TopicMapException {
        String si = FIELD_SI_TEMPLATE.replace("___x___", tag);
        si = si+"#indicator1";
        return getOrCreateTopic(tm, si, MarcField.getFieldIndicatorName(tag, "1"), getIndType(tm));
    }


    public Topic getIndType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, IND_SI, "Indicator (MARC)", getMARCClass(tm));
    }


    // ---


    public Topic getInd2Topic(String ind2, String tag, TopicMap tm) throws TopicMapException {
        String si = FIELD_SI_TEMPLATE.replace("___x___", tag);
        si = si+"#indicator2."+ind2;
        return getOrCreateTopic(tm, si, getIndicatorValueName(tag, "2", ind2), getInd2Type(tag, tm));
    }

    public Topic getInd2Type(String tag, TopicMap tm) throws TopicMapException {
        String si = FIELD_SI_TEMPLATE.replace("___x___", tag);
        si = si+"#indicator2";
        return getOrCreateTopic(tm, si, MarcField.getFieldIndicatorName(tag, "2"), getIndType(tm));
    }


    public String getIndicatorName(String field, String indicatorId, String value) {
        String indicatorName = MarcField.getFieldIndicatorName(field, indicatorId);
        if( indicatorName != null ) return indicatorName+" ("+value+"@"+field+"#ind"+indicatorId+")";
        else return value+"@"+field+"#ind"+indicatorId;
    }

    public String getIndicatorValueName(String field, String indicatorId, String value) {
        String indicatorValueName = MarcField.getFieldIndicatorValue(field, indicatorId, value);
        if( indicatorValueName != null ) return indicatorValueName;
        else return value;
    }


    // ---


    public Topic getRecordType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, RECORD_SI, "Record (MARC)", getMARCClass(tm));
    }

    // ---


    public Topic getMARCClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, MARC_SI, "MARC", getWandoraClass(tm) );
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





    // ----------------




}
