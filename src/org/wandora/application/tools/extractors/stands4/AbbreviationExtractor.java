/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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


import org.wandora.topicmap.*;
import org.wandora.*;
import org.wandora.utils.*;
import org.wandora.utils.Tuples.*;

import java.io.*;
import java.net.*;
import org.xml.sax.*;



/**
 *
 * @author akivela
 */
public class AbbreviationExtractor extends AbstractStands4Extractor {


    private String queryTerm = null;






    /** Creates a new instance of AbbreviationsExtractor */
    public AbbreviationExtractor() {
    }



    @Override
    public String getName() {
        return "Stands4 abbreviations extractor";
    }

    @Override
    public String getDescription(){
        return "Finds words for given acronym using Stands4 web service available at www.abbreviations.com. "+
                "Extractor reads a feed from the web service and converts it to a topic map.";
    }


    public String getTermBase() {
        return ABBREVIATIONS_TERM_BASE;
    }
    public String getTermType() {
        return ABBREVIATIONS_BASE+"type";
    }


    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        try {
            //log(url.toExternalForm());
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
            AbbreviationResultParser parserHandler = new AbbreviationResultParser(getMasterSubject(), queryTerm, topicMap, this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            try {
                reader.parse(new InputSource(new ByteArrayInputStream(result.getBytes(defaultEncoding))));
            }
            catch(Exception e){
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }

            String msg = null;
            if(parserHandler.progress == 0) {
                msg = "Found no words for acronym.";
            }
            else {
                msg = "Found "+parserHandler.progress+" word(s) for acronym.";
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





    private class AbbreviationResultParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public int progress=0;
        private TopicMap tm;
        private AbbreviationExtractor parent;


        private Topic termTopic = null;
        private Topic masterTopic = null;


        public AbbreviationResultParser(String master, String term, TopicMap tm, AbbreviationExtractor parent) {
            this.tm=tm;
            this.parent=parent;

            try {
                if(term != null) {
                    term = term.trim();
                    if(term.length() > 0) {
                        termTopic = getTermTopic(term, tm);
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

            if(masterTopic != null && termTopic != null) {
                try {
                    Association a = tm.createAssociation(getTermType(tm));
                    a.addPlayer(termTopic, getTermType(tm));
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
        public static final String TAG_CATEGORY = "category";

        private static final int STATE_START=0;
        private static final int STATE_RESULTS=2;
        private static final int STATE_RESULTS_RESULT=4;
        private static final int STATE_RESULTS_RESULT_TERM=5;
        private static final int STATE_RESULTS_RESULT_DEFINITION=6;
        private static final int STATE_RESULTS_RESULT_CATEGORY=7;

        private int state=STATE_START;

        private String data_term = null;
        private String data_definition = null;
        private String data_category = null;




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
                        data_category = "";
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
                    else if(qName.equals(TAG_CATEGORY)) {
                        data_category = "";
                        state = STATE_RESULTS_RESULT_CATEGORY;
                    }
                    break;
                case STATE_RESULTS_RESULT_TERM:
                    break;
                case STATE_RESULTS_RESULT_DEFINITION:
                    break;
                case STATE_RESULTS_RESULT_CATEGORY:
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
                                data_term = data_term.trim();
                                Topic termTopic = getTermTopic(data_term, tm);
                                parent.setProgress(progress++);
                                if(termTopic != null) {
                                    Topic abbreviationType = getAbbreviationType(tm);
                                    
                                    Association a = tm.createAssociation(abbreviationType);
                                    a.addPlayer(termTopic, getTermType(tm));

                                    if(data_category != null) {
                                        Topic categoryType = getCategoryType(tm);
                                        Topic categoryTopic = getCategoryTopic(data_category, tm);
                                        a.addPlayer(categoryTopic, categoryType);
                                    }
                                    if(data_definition != null) {
                                        Topic definitionType = getDefinitionType(tm);
                                        Topic definitionTopic = getDefinitionTopic(data_definition, tm);
                                        a.addPlayer(definitionTopic, definitionType);
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
                case STATE_RESULTS_RESULT_CATEGORY: {
                    if(qName.equals(TAG_CATEGORY)) {
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
                case STATE_RESULTS_RESULT_CATEGORY: {
                    data_category += new String(ch,start,length);
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