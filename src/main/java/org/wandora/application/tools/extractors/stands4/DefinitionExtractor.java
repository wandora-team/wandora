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
 */


package org.wandora.application.tools.extractors.stands4;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
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
public class DefinitionExtractor extends AbstractStands4Extractor {


	private static final long serialVersionUID = 1L;

	private String queryTerm = null;






    /** Creates a new instance of DefinitionsExtractor */
    public DefinitionExtractor() {
    }



    @Override
    public String getName() {
        return "Stands4 definitions extractor";
    }

    @Override
    public String getDescription(){
        return "Finds definitions for a given word using Stands4 web service. Definition service provided by www.definitions.net. "+
                "Extractor reads a feed from the web service and converts it to a topic map.";
    }



    public String getTermBase() {
        return DEFINITIONS_TERM_BASE;
    }
    public String getTermType() {
        return DEFINITIONS_BASE+"type";
    }


    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        try {
            queryTerm = solveQueryTerm(url);
        }
        catch(Exception e) {
            log(e);
        }
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
            DefinitionResultParser parserHandler = new DefinitionResultParser(getMasterSubject(), queryTerm, topicMap, this);
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
                msg = "Found no terms.";
            }
            else {
                msg = "Found "+parserHandler.progress+" (complex) terms.";
            }
            if(msg != null) log(msg);
        }
        catch (Exception ex) {
           log(ex);
        }
        queryTerm = null;
        return true;
    }




    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------





    private class DefinitionResultParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public int progress=0;
        private TopicMap tm;
        private DefinitionExtractor parent;


        private Topic aTermTopic = null;
        private Topic masterTopic = null;


        public DefinitionResultParser(String master, String term, TopicMap tm, DefinitionExtractor parent) {
            this.tm=tm;
            this.parent=parent;

            try {
                if(term != null) {
                    term = term.trim();
                    if(term.length() > 0) {
                        aTermTopic = getTermTopic(term, tm);
                    }
                }
            }
            catch(Exception e) {
                parent.log(e);
            }

            try {
                if(master != null) {
                    masterTopic = tm.getTopicWithBaseName(master);
                    if(masterTopic == null) masterTopic = tm.getTopic(master);
                }
            }
            catch(Exception e) {
                parent.log(e);
            }

            if(masterTopic != null && aTermTopic != null) {
                try {
                    Association a = tm.createAssociation(getTermType(tm));
                    a.addPlayer(aTermTopic, getTermType(tm));
                    a.addPlayer(masterTopic, getSourceType(tm));
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
        }


        public static final String TAG_RESULTS = "results";
        public static final String TAG_RESULT = "result";
        public static final String TAG_TERM = "term";
        public static final String TAG_DEFINITION = "definition";
        public static final String TAG_PARTOFSPEECH = "partofspeach";
        public static final String TAG_EXAMPLE = "example";
        public static final String TAG_SYNONYMS = "synonyms";

        private static final int STATE_START=0;
        private static final int STATE_RESULTS=2;
        private static final int STATE_RESULTS_RESULT=4;
        private static final int STATE_RESULTS_RESULT_TERM=5;
        private static final int STATE_RESULTS_RESULT_DEFINITION=6;
        private static final int STATE_RESULTS_RESULT_PARTOFSPEECH=7;
        private static final int STATE_RESULTS_RESULT_EXAMPLE=8;
        private static final int STATE_RESULTS_RESULT_SYNONYMS=9;


        private int state=STATE_START;

        private String data_term = null;
        private String data_definition = null;
        private String data_partofspeech = null;
        private String data_example = null;
        private String data_synonyms = null;


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
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_RESULTS;
                    }
                    break;
                case STATE_RESULTS:
                    if(qName.equals(TAG_RESULT)) {
                        data_term = "";
                        data_definition = "";
                        data_partofspeech = "";
                        data_example = "";
                        data_synonyms = "";
                        state = STATE_RESULTS_RESULT;
                    }
                    break;
                case STATE_RESULTS_RESULT:
                    if(qName.equals(TAG_TERM)) {
                        data_term = "";
                        state = STATE_RESULTS_RESULT_TERM;
                    }
                    else if(qName.equals(TAG_DEFINITION)) {
                        data_definition = "";
                        state = STATE_RESULTS_RESULT_DEFINITION;
                    }
                    else if(qName.equals(TAG_PARTOFSPEECH)) {
                        data_partofspeech = "";
                        state = STATE_RESULTS_RESULT_PARTOFSPEECH;
                    }
                    else if(qName.equals(TAG_EXAMPLE)) {
                        data_example = "";
                        state = STATE_RESULTS_RESULT_EXAMPLE;
                    }
                    else if(qName.equals(TAG_SYNONYMS)) {
                        data_synonyms = "";
                        state = STATE_RESULTS_RESULT_SYNONYMS;
                    }
                    break;
                case STATE_RESULTS_RESULT_TERM:
                    break;
                case STATE_RESULTS_RESULT_DEFINITION:
                    break;
                case STATE_RESULTS_RESULT_PARTOFSPEECH:
                    break;
                case STATE_RESULTS_RESULT_EXAMPLE:
                    break;
                case STATE_RESULTS_RESULT_SYNONYMS:
                    break;
            }
        }



        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {

                case STATE_RESULTS: {
                    if(qName.equals(TAG_RESULTS)) {
                        state = STATE_START;
                    }
                    break;
                }


                case STATE_RESULTS_RESULT: {
                    if(qName.equals(TAG_RESULT)) {
                        try {
                            if(data_term != null && data_term.length() > 0) {
                                parent.log("Found term complex '"+data_term+"'.");
                                parent.setProgress(progress++);
                                Topic termTopic = getTermComplexTopic(data_term, data_definition, tm);

                                if(termTopic != null) {

                                    if(data_definition != null && data_definition.length() > 0) {
                                        data_definition = data_definition.trim();
                                        Topic definitionType = getDefinitionType(tm);
                                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang(defaultLang));
                                        termTopic.setData(definitionType, langTopic, data_definition);
                                    }

                                    if(data_example != null && data_example.length() > 0) {
                                        data_example = data_example.trim();
                                        Topic exampleType = getExampleType(tm);
                                        Topic langTopic = getOrCreateTopic(tm, XTMPSI.getLang(defaultLang));
                                        termTopic.setData(exampleType, langTopic, data_example);
                                    }

                                    if(data_partofspeech != null && data_partofspeech.length() > 0) {
                                        Topic partOfSpeedTopic = getPartOfSpeechTopic(data_partofspeech, tm);
                                        Topic partOfSpeedType = getPartOfSpeechType(tm);
                                        Topic termType = getTermType(tm);
                                        Association pofs = tm.createAssociation(partOfSpeedType);
                                        pofs.addPlayer(partOfSpeedTopic, partOfSpeedType);
                                        pofs.addPlayer(termTopic, termType);
                                    }

                                    if(data_synonyms != null && data_synonyms.length() > 0) {
                                        String[] synonyms = data_synonyms.split(",");
                                        for(int i=0; i<synonyms.length ; i++) {
                                            String synonym = synonyms[i].trim();
                                            if(synonym.length() > 0) {
                                                Topic synonymTerm = getTermTopic( synonym, tm );
                                                Topic synonymTermType = getSynonymTermType(tm);
                                                Topic termType = getTermType(tm);
                                                Association sa = tm.createAssociation(synonymTermType);
                                                sa.addPlayer(termTopic, termType);
                                                sa.addPlayer(synonymTerm, synonymTermType);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        state = STATE_RESULTS;
                    }
                    break;
                }

                case STATE_RESULTS_RESULT_TERM: {
                    if(qName.equals(TAG_TERM)) {
                        state = STATE_RESULTS_RESULT;
                    }
                    break;
                }
                case STATE_RESULTS_RESULT_DEFINITION: {
                    if(qName.equals(TAG_DEFINITION)) {
                        state = STATE_RESULTS_RESULT;
                    }
                    break;
                }
                case STATE_RESULTS_RESULT_PARTOFSPEECH: {
                    if(qName.equals(TAG_PARTOFSPEECH)) {
                        state = STATE_RESULTS_RESULT;
                    }
                    break;
                }
                case STATE_RESULTS_RESULT_EXAMPLE: {
                    if(qName.equals(TAG_EXAMPLE)) {
                        state = STATE_RESULTS_RESULT;
                    }
                    break;
                }
                case STATE_RESULTS_RESULT_SYNONYMS: {
                    if(qName.equals(TAG_SYNONYMS)) {
                        state = STATE_RESULTS_RESULT;
                    }
                    break;
                }
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state) {
                case STATE_RESULTS_RESULT_TERM: {
                    data_term += new String(ch,start,length);
                    break;
                }
                case STATE_RESULTS_RESULT_DEFINITION: {
                    data_definition += new String(ch,start,length);
                    break;
                }
                case STATE_RESULTS_RESULT_PARTOFSPEECH: {
                    data_partofspeech += new String(ch,start,length);
                    break;
                }
                case STATE_RESULTS_RESULT_EXAMPLE: {
                    data_example += new String(ch,start,length);
                    break;
                }
                case STATE_RESULTS_RESULT_SYNONYMS: {
                    data_synonyms += new String(ch,start,length);
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
