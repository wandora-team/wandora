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


package org.wandora.application.tools.extractors.bighugethesaurus;


import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;

import java.io.*;
import javax.swing.*;
import java.net.*;
import org.xml.sax.*;



/**
 *
 * @author akivela
 */
public class XMLBigHugeThesaurusExtractor  extends AbstractExtractor {

	private static final long serialVersionUID = 1L;
	
	public static final boolean INCLUDE_PART_OF_SPEECH = true;
    public static final boolean RELATIONSHIP_AS_ASSOCIATION_TYPE = true;

    private String queryTerm = null;

    protected String SI_BASE = "http://words.bighugelabs.com/";

    protected String PARTOFSPEECH_SI = SI_BASE+"part-of-speech";
    protected String TERM_SI = SI_BASE+"term";
    protected String RELATEDTERM_SI = SI_BASE+"related-term";

    protected String RELATIONSHIP_SI = SI_BASE+"relationship";

    protected String SYNONYM_TYPE_SI = SI_BASE+"synonym";
    protected String ANTONYM_TYPE_SI = SI_BASE+"antonym";
    protected String RELATED_TYPE_SI = SI_BASE+"related";
    protected String SIMILAR_TYPE_SI = SI_BASE+"similar";
    protected String USERSUGGESTION_TYPE_SI = SI_BASE+"user-suggested";

    protected String SOURCE_SI = "http://wandora.org/si/source";

    private String defaultEncoding = "ISO-8859-1";




    /** Creates a new instance of XMLBigHugeThesaurusExtractor */
    public XMLBigHugeThesaurusExtractor() {
    }



    @Override
    public String getName() {
        return "Big Huge Thesaurus XML extractor.";
    }

    @Override
    public String getDescription(){
        return "Converts Big Huge Thesaurus XML feeds to topics maps. Thesaurus service provided by words.bighugelabs.com.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_bighugethesaurus.png");
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
        try {
            String urlStr = url.toExternalForm();
            int i = urlStr.indexOf("/xml");
            if(i != -1) {
                urlStr = urlStr.substring(0, i);
                i = urlStr.lastIndexOf("/");
                if(i != -1) {
                    queryTerm = urlStr.substring(i+1);
                    try {
                        queryTerm = URLDecoder.decode(queryTerm, defaultEncoding);
                    }
                    catch(Exception e) {
                        // DO NOTHING...
                    }
                }
            }
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
            BigHugeThesaurusResultParser parserHandler = new BigHugeThesaurusResultParser(getMasterSubject(), queryTerm, topicMap, this);
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
                msg = "Found related words.";
            }
            else {
                msg = "Found "+parserHandler.progress+" related word(s).";
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



    public Topic getRelationshipType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, RELATIONSHIP_SI, "Relationship");
        Topic bhtClass = getBigHugeClass(tm);
        makeSubclassOf(tm, type, bhtClass);
        return type;
    }


    // -------


    public Topic getPartOfSpeechTopic(String p, TopicMap tm) throws TopicMapException {
        if(p != null) {
            p = p.trim();
            if(p.length() > 0) {
                Topic partOfSpeechTopic=getOrCreateTopic(tm, p);
                Topic partOfSpeechTypeTopic = getPartOfSpeechType(tm);
                partOfSpeechTopic.addType(partOfSpeechTypeTopic);
                return partOfSpeechTopic;
            }
        }
        return null;
    }




    public Topic getPartOfSpeechType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PARTOFSPEECH_SI, "Part of speech");
        Topic bhtClass = getBigHugeClass(tm);
        makeSubclassOf(tm, type, bhtClass);
        return type;
    }


    // -------

    public Topic getTermTopic(String term, TopicMap tm) throws TopicMapException {
        if(term != null) {
            term = term.trim();
            if(term.length() > 0) {
                String si = null;
                try {
                    si = SI_BASE+URLEncoder.encode(term, defaultEncoding);
                }
                catch(Exception e) {
                    si = SI_BASE+term;
                }
                Topic termTopic=getOrCreateTopic(tm, si, term);
                Topic termTypeTopic = getTermType(tm);
                termTopic.addType(termTypeTopic);
                return termTopic;
            }
        }
        return null;
    }

    // -------

    public Topic getTermType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TERM_SI, "Term");
        Topic bhtClass = getBigHugeClass(tm);
        makeSubclassOf(tm, type, bhtClass);
        return type;
    }


    public Topic getRelatedTermType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, RELATEDTERM_SI, "Related term");
        return type;
    }

    // ---- RELATIONSHIPS ----

    public Topic getSynonymType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SYNONYM_TYPE_SI, "Synonym");
        Topic relationshipType = getRelationshipType(tm);
        makeSubclassOf(tm, type, relationshipType);
        return type;
    }

    public Topic getAntonymType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ANTONYM_TYPE_SI, "Antonym");
        Topic relationshipType = getRelationshipType(tm);
        makeSubclassOf(tm, type, relationshipType);
        return type;
    }

    public Topic getRelatedType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, RELATED_TYPE_SI, "Related");
        Topic relationshipType = getRelationshipType(tm);
        makeSubclassOf(tm, type, relationshipType);
        return type;
    }

    public Topic getSimilarType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SIMILAR_TYPE_SI, "Similar");
        Topic relationshipType = getRelationshipType(tm);
        makeSubclassOf(tm, type, relationshipType);
        return type;
    }

    public Topic getUserSuggestionsType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, USERSUGGESTION_TYPE_SI, "User suggestion");
        Topic relationshipType = getRelationshipType(tm);
        makeSubclassOf(tm, type, relationshipType);
        return type;
    }


    // ------------------------
    
    public Topic getBigHugeClass(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, SI_BASE,"Big Huge Thesaurus");
        t.addType(getWandoraClass(tm));
        return t;
    }


    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }


    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }


    // --------

    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    

    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }




    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------





    private class BigHugeThesaurusResultParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public int progress=0;
        private TopicMap tm;
        private XMLBigHugeThesaurusExtractor parent;

        
        private Topic termTopic = null;
        private Topic masterTopic = null;


        public BigHugeThesaurusResultParser(String master, String term, TopicMap tm, XMLBigHugeThesaurusExtractor parent) {
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


        public static final String TAG_WORDS = "words";
        public static final String TAG_W = "w";
        public static final String TAG_MESSAGE = "Message";

        private static final int STATE_START=0;
        private static final int STATE_WORDS=2;
        private static final int STATE_WORDS_W=4;

        private int state=STATE_START;

        private String data_w = null;
        private String data_p = null;
        private String data_r = null;




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
                    if(qName.equals(TAG_WORDS)) {
                        state = STATE_WORDS;
                    }
                    break;
                case STATE_WORDS:
                    if(qName.equals(TAG_W)) {
                        data_w = "";
                        data_p = atts.getValue("p");
                        data_r = atts.getValue("r");
                        state = STATE_WORDS_W;
                    }
                    break;
                case STATE_WORDS_W:
                    break;
            }
        }



        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {

                case STATE_WORDS: {
                    if(qName.equals(TAG_WORDS)) {
                        state = STATE_START;
                    }
                    break;
                }
                

                case STATE_WORDS_W: {
                    if(qName.equals(TAG_W)) {
                        parent.setProgress(progress++);
                        try {
                            if(data_w != null && data_w.length() > 0) {
                                data_w = data_w.trim();
                                Topic otherTermTopic = getTermTopic(data_w, tm);
                                if(termTopic != null && otherTermTopic != null) {

                                    Topic relationshipType = null;
                                    if("syn".equalsIgnoreCase(data_r)) {
                                        relationshipType = getSynonymType(tm);
                                    }
                                    else if("ant".equalsIgnoreCase(data_r)) {
                                        relationshipType = getSynonymType(tm);
                                    }
                                    else if("rel".equalsIgnoreCase(data_r)) {
                                        relationshipType = getRelatedType(tm);
                                    }
                                    else if("sim".equalsIgnoreCase(data_r)) {
                                        relationshipType = getSimilarType(tm);
                                    }
                                    else if("usr".equalsIgnoreCase(data_r)) {
                                        relationshipType = getUserSuggestionsType(tm);
                                    }

                                    if(RELATIONSHIP_AS_ASSOCIATION_TYPE) {
                                        if(relationshipType != null) {
                                            Association relationship = tm.createAssociation(relationshipType);
                                            relationship.addPlayer(termTopic, getTermType(tm));
                                            relationship.addPlayer(otherTermTopic, getRelatedTermType(tm));

                                            if(INCLUDE_PART_OF_SPEECH) {
                                                Topic partOfSpeedTopic = getPartOfSpeechTopic(data_p, tm);
                                                Topic partOfSpeedType = getPartOfSpeechType(tm);
                                                if(partOfSpeedTopic != null && partOfSpeedType != null) {
                                                    relationship.addPlayer(partOfSpeedTopic, partOfSpeedType);
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        if(relationshipType != null) {
                                            Topic relationshipTypeType = getRelationshipType(tm);
                                            Association relationship = tm.createAssociation(relationshipTypeType);
                                            relationship.addPlayer(termTopic, getTermType(tm));
                                            relationship.addPlayer(otherTermTopic, getRelatedTermType(tm));
                                            relationship.addPlayer(relationshipType, relationshipTypeType);

                                            if(INCLUDE_PART_OF_SPEECH) {
                                                Topic partOfSpeedTopic = getPartOfSpeechTopic(data_p, tm);
                                                Topic partOfSpeedType = getPartOfSpeechType(tm);
                                                if(partOfSpeedTopic != null && partOfSpeedType != null) {
                                                    relationship.addPlayer(partOfSpeedTopic, partOfSpeedType);
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
                        state = STATE_WORDS;
                    }
                    break;
                }
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state) {
                case STATE_WORDS_W: {
                    data_w += new String(ch,start,length);
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