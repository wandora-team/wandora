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
 * OpenCalaisExtractor.java
 *
 * Created on 2008-07-22, 13:18
 *
 */




package org.wandora.application.tools.extractors.opencalais;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.extractors.opencalais.webservice.*;
import org.wandora.utils.*;

import java.io.*;
import javax.swing.*;

import java.net.*;
import org.xml.sax.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;




/**
 *
 * @author akivela
 */
public class OpenCalaisClassifier extends AbstractExtractor {
    
    protected static final boolean EXTRACT_RELEVANCE = true;
    protected static final boolean EXTRACT_SCORE = true;
    
    protected static final String CALAIS_SI = "http://www.opencalais.com";
    protected static final String CALAIS_TOPIC_SI = "http://www.opencalais.com/topic";
    protected static final String TAG_SI = "http://www.opencalais.com/tag";
    protected static final String OPENCALAIS_TAG_CLASSIFICATION_SI = "http://www.opencalais.com/classification/tag";
    protected static final String OPENCALAIS_TOPIC_CLASSIFICATION_SI = "http://www.opencalais.com/classification/topic";
    protected static final String TOPIC_SI = "http://wandora.org/si/topic";
    protected static final String SOURCE_SI = "http://wandora.org/si/source";
    protected static final String DOCUMENT_SI = "http://wandora.org/si/document";
    
    protected static final String RELEVANCE_SI = "http://www.opencalais.com/relevance";
    protected static final String SCORE_SI = "http://www.opencalais.com/score";
    
    
    private String contentType = "text/plain";
    private String defaultEncoding = "UTF-8"; // WAS: "ISO-8859-1";
    private String params = "<c:params xmlns:c=\"http://s.opencalais.com/1/pred/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"+
            "<c:processingDirectives c:contentType=\"__CONTENTTYPE__\" c:outputFormat=\"Text/Simple\">"+
            "</c:processingDirectives>"+
            "<c:userDirectives c:allowDistribution=\"false\" c:allowSearch=\"false\" c:externalID=\"17cabs901\" c:submitter=\"Wandora\">"+
            "</c:userDirectives>"+
            "<c:externalMetadata>"+
            "</c:externalMetadata>"+
            "</c:params>";
    

    
    /** Creates a new instance of OpenCalaisClassifier */
    public OpenCalaisClassifier() {
    }
    

    @Override
    public String getName() {
        return "OpenCalais classifier";
    }
    
    @Override
    public String getDescription(){
        return "Classifies texts documents and fragments using OpenCalais.";
    }
    

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_opencalais.png");
    }
    
    private final String[] contentTypes=new String[] { "text/plain", "text/html" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String in = ExtractHelper.getContent(url);
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
            String content = null;
            try {
                content = XMLbox.cleanUp(in);
                if(content == null || content.length() < 1) {
                    // Tidy failed to fix the file...
                    content = in;
                    contentType = "text/html";
                }
                else {
                    // Ok, Tidy fixed the html/xml document
                    content = XMLbox.getAsText(content, defaultEncoding);
                    //System.out.println("content after getAsText: "+content);
                    contentType = "text/txt";
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                content = in;
                contentType = "text/raw";
            }

            String key = solveAPIKey();
            String paramsXML = params.replace("__CONTENTTYPE__", contentType);
            CalaisClient client = new CalaisClient("http://api.opencalais.com/enlighten/");
            String result = client.enlighten(key, content, paramsXML);

            // System.out.println("Result = "+result);
            if(result.startsWith("<Error")) {
                log("OpenCalais could not process given document!");
                int startIndex = result.indexOf("<Exception>");
                int endIndex = result.indexOf("</Exception>");
                if(startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    String errorMessage = result.substring(startIndex+11, endIndex);
                    log(errorMessage);
                }
                log("No documents processed!");
            }
            else {
                // ---- Parse results ----
                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                OpenCalaisSimpleResultParser parserHandler = new OpenCalaisSimpleResultParser(getMasterSubject(), content, topicMap,this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try{
                    reader.parse(new InputSource(new ByteArrayInputStream(result.getBytes(defaultEncoding))));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                log("Total " + parserHandler.progress + " OpenCalais tags parsed!");
            }
        }
        catch (Exception ex) {
           log(ex);
        }
        clearMasterSubject();
        return true;
    }



    /* ---------------------------------------------------------------------- */



    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        setWandora(wandora);
        return ExtractHelper.doBrowserExtractForClassifiers(this, request, wandora, defaultEncoding);
    }

    @Override
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        return true;
    }

    @Override
    public String getBrowserExtractorName() {
        return "Classify with OpenCalais";
    }


    /* ---------------------------------------------------------------------- */

    

    public String solveTitle(String content) {
        if(content == null || content.length() == 0) return "empty-document";
        
        boolean forceTrim = false;
        String title = null;
        int i = content.indexOf("\n");
        if(i > 0) title = content.substring(0, i);
        else {
            title = content.substring(0, Math.min(80, content.length()));
            forceTrim = true;
        }
        
        if(title != null && (forceTrim || title.length() > 80)) {
            title = title.substring(0, Math.min(80, title.length()));
            while(!title.endsWith(" ") && title.length()>10) {
                title = title.substring(0, title.length()-1);
            }
            title = Textbox.trimExtraSpaces(title) + "...";
        }
        return title;
    }
        
    
    
    
    public void fillDocumentTopic(Topic textTopic, TopicMap topicMap, String content) {
        try {
            String trimmedText = Textbox.trimExtraSpaces(content);
            if(trimmedText != null && trimmedText.length() > 0) {
                Topic contentType = createTopic(topicMap, "document-text");
                setData(textTopic, contentType, "en", trimmedText);
            }
            String title = solveTitle(trimmedText);
            if(title != null) {
                textTopic.setBaseName(title + " (" + content.hashCode() + ")");
                textTopic.setDisplayName("en", title);
            }
            Topic documentType = getDocumentType(topicMap);
            textTopic.addType(documentType);
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    public Topic getRelevanceTopic(String relevance, TopicMap tm) throws TopicMapException {
        if(relevance != null) {
            relevance = relevance.trim(); 
            if(relevance.length() > 0) {
                Topic relevanceTopic=getOrCreateTopic(tm, RELEVANCE_SI+"/"+urlEncode(relevance), relevance);
                Topic relevanceTypeTopic = getRelevanceType(tm);
                relevanceTopic.addType(relevanceTypeTopic);
                return relevanceTopic;
            }
        }
        return null;
    }
    
    
    
    public Topic getScoreTopic(String score, TopicMap tm) throws TopicMapException {
        if(score != null) {
            score = score.trim(); 
            if(score.length() > 0) {
                Topic scoreTopic=getOrCreateTopic(tm, SCORE_SI+"/"+urlEncode(score), score);
                Topic scoreTypeTopic = getScoreType(tm);
                scoreTopic.addType(scoreTypeTopic);
                return scoreTopic;
            }
        }
        return null;
    }
    
    
    
    public Topic getCalaisTopic(String topic, String taxonomy, TopicMap tm) throws TopicMapException {
        if(topic != null) {
            topic = topic.trim(); 
            if(topic.length() > 0) {
                Topic taxonomyTopic = getOrCreateTopic(tm, CALAIS_TOPIC_SI+"/"+urlEncode(taxonomy), taxonomy);
                Topic topicTypeTopic = getCalaisTopicType(tm);
                taxonomyTopic.addType(topicTypeTopic);
                
                Topic calaisTopic=getOrCreateTopic(tm, CALAIS_TOPIC_SI+"/"+urlEncode(taxonomy)+"/"+urlEncode(topic), topic);
                calaisTopic.addType(taxonomyTopic);
                return calaisTopic;
            }
        }
        return null;
    }
    
    
    
    public Topic getTagTopic(String tag, TopicMap tm) throws TopicMapException {
        if(tag != null) {
            tag = tag.trim(); 
            if(tag.length() > 0) {
                Topic tagTopic=getOrCreateTopic(tm, TAG_SI+"/"+urlEncode(tag), tag);
                Topic tagTypeTopic = getTagType(tm);
                tagTopic.addType(tagTypeTopic);
                return tagTopic;
            }
        }
        return null;
    }

    
    public Topic getTagValue(String val, String tag, TopicMap tm) throws TopicMapException {
        Topic tagValue = null;
        if(val != null) {
            val = val.trim();
            if(val.length() > 0) {
                tagValue=getOrCreateTopic(tm, TAG_SI+"/"+urlEncode(tag)+"/"+urlEncode(val), val);
                if(tag != null) {
                    tag = tag.trim();
                    if(tag.length() > 0) {
                        Topic tagTopic = getTagTopic(tag, tm);
                        tagValue.addType(tagTopic);
                    }
                }
            }
        }
        return tagValue;
    }
    
    
    public Topic getCalaisTopicType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, CALAIS_TOPIC_SI, "OpenCalais topic");
        Topic calaisClass = getCalaisClass(tm);
        makeSubclassOf(tm, type, calaisClass);
        return type;
    }
    
    public Topic getTagType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TAG_SI, "OpenCalais tag");
        Topic calaisClass = getCalaisClass(tm);
        makeSubclassOf(tm, type, calaisClass);
        return type;
    }
    
    
    public Topic getRelevanceType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, RELEVANCE_SI, "OpenCalais relevance");
        return type;
    }
    
    
    public Topic getScoreType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SCORE_SI, "OpenCalais score");
        return type;
    }
    


    public Topic getCalaisClass(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, CALAIS_SI,"OpenCalais");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, t, w);
        return t;
    }


    
    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    
    public Topic getCalaisTagClassificationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OPENCALAIS_TAG_CLASSIFICATION_SI,"OpenCalais tag classification");
    }
    
    public Topic getCalaisTopicClassificationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OPENCALAIS_TOPIC_CLASSIFICATION_SI,"OpenCalais topic classification");
    }
    
    public Topic getTopicType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TOPIC_SI, "Topic");
    }
    
    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }
    
    public Topic getDocumentType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, DOCUMENT_SI, "Document");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
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
    
    
    
    public class OpenCalaisSimpleResultParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        Topic masterTopic = null;
        
        
        public OpenCalaisSimpleResultParser(Topic t, TopicMap tm, OpenCalaisClassifier parent){
            this.masterTopic=t;
            this.tm=tm;
            this.parent=parent;
        }
        
        public OpenCalaisSimpleResultParser(String term, String text, TopicMap tm, OpenCalaisClassifier parent){
            try {
                if(term != null) {
                    masterTopic = tm.getTopicWithBaseName(term);
                    if(masterTopic == null) masterTopic = tm.getTopic(term);
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            if(masterTopic == null && text != null && text.length() > 0) {
                try {
                    masterTopic = tm.createTopic();
                    masterTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                    parent.fillDocumentTopic(masterTopic, tm, text);
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
            this.tm=tm;
            this.parent=parent;
        }
        
        
        
        public int progress=0;
        private TopicMap tm;
        private OpenCalaisClassifier parent;
        
        public static final String TAG_OPENCALAISSIMPLE="OpenCalaisSimple";
        public static final String TAG_DESCRIPTION="Description";
        public static final String TAG_CALAISSESSIONID="CalaisSessionId";
        public static final String TAG_ALLOWDISTRIBUTION="allowDistribution";
        public static final String TAG_ALLOWSEARCH="allowSearch";
        public static final String TAG_EXTERNALID="externalID";
        public static final String TAG_ID="id";
        public static final String TAG_ABOUT="about";
        public static final String TAG_CALAISSIMPLEOUTPUTFORMAT = "CalaisSimpleOutputFormat";
        public static final String TAG_TOPICS="Topics";
        public static final String TAG_TOPIC="Topic";
        
        private static final int STATE_START=0;
        private static final int STATE_OPENCALAISSIMPLE=2;
        private static final int STATE_OPENCALAISSIMPLE_DESCRIPTION=4;
        private static final int STATE_OPENCALAISSIMPLE_DESCRIPTION_CALAISSESSIONID=5;
        private static final int STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWDISTRIBUTION=6;
        private static final int STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWSEARCH=7;
        private static final int STATE_OPENCALAISSIMPLE_DESCRIPTION_EXTERNALID=8;
        private static final int STATE_OPENCALAISSIMPLE_DESCRIPTION_ID=9;
        private static final int STATE_OPENCALAISSIMPLE_DESCRIPTION_ABOUT=10;
        private static final int STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT=11;
        private static final int STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TAG=12;
        private static final int STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS=20;
        private static final int STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS_TOPIC=21;
        
        private int state=STATE_START;
        
        
        private String data_calaissessionid = "";
        private String data_allowdistribution = "";
        private String data_allowsearch = "";
        private String data_externalid = "";
        private String data_id = "";
        private String data_about = "";
        
        private String data_topic = "";
        private String data_topicScore = "";
        private String data_topicTaxonomy = "";

        private String attribute_count = "";
        private String attribute_relevance = "";
               
        private String tag = "";
        private String tagValue = "";

        
        
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
                    if(qName.equals(TAG_OPENCALAISSIMPLE)) {
                        state = STATE_OPENCALAISSIMPLE;
                    }
                    break;
                case STATE_OPENCALAISSIMPLE:
                    if(qName.equals(TAG_DESCRIPTION)) {
                        state = STATE_OPENCALAISSIMPLE_DESCRIPTION;
                    }
                    else if(qName.equals(TAG_CALAISSIMPLEOUTPUTFORMAT)) {
                        tag = "";
                        tagValue = "";
                        state = STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT;
                    }
                    break;
                case STATE_OPENCALAISSIMPLE_DESCRIPTION:
                    if(qName.equals(TAG_CALAISSESSIONID)) {
                        data_calaissessionid = "";
                        state = STATE_OPENCALAISSIMPLE_DESCRIPTION_CALAISSESSIONID;
                    }
                    else if(qName.equals(TAG_ALLOWDISTRIBUTION)) {
                        data_allowdistribution = "";
                        state = STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWDISTRIBUTION;
                    }
                    else if(qName.equals(TAG_ALLOWSEARCH)) {
                        data_allowsearch = "";
                        state = STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWSEARCH;
                    }
                    else if(qName.equals(TAG_EXTERNALID)) {
                        data_externalid = "";
                        state = STATE_OPENCALAISSIMPLE_DESCRIPTION_EXTERNALID;
                    }
                    else if(qName.equals(TAG_ID)) {
                        data_id = "";
                        state = STATE_OPENCALAISSIMPLE_DESCRIPTION_ID;
                    }
                    else if(qName.equals(TAG_ABOUT)) {
                        data_about = "";
                        state = STATE_OPENCALAISSIMPLE_DESCRIPTION_ABOUT;
                    }
                    break;
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT: {
                    if(qName.equals(TAG_TOPICS)) {
                        state = STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS;
                    }
                    else {
                        state = STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TAG;
                        tag = qName;
                        tagValue = "";
                        attribute_count = atts.getValue("count");
                        attribute_relevance = atts.getValue("relevance");
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS: {
                    if(qName.equals(TAG_TOPIC)) {
                        state = STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS_TOPIC;
                        data_topic = "";
                        data_topicScore = atts.getValue("Score");
                        data_topicTaxonomy = atts.getValue("Taxonomy");
                    }
                    break;
                }
            }
        }
        
        
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TAG: {
                    if(qName.equals(tag)) {
                        try {
                            parent.setProgress( progress++ );
                            Topic tagValueTopic = parent.getTagValue(tagValue, tag, tm);
                            if(masterTopic != null && tagValueTopic != null) {
                                Topic metadataType = parent.getCalaisTagClassificationType(tm);
                                Association a = tm.createAssociation(metadataType);
                                a.addPlayer(masterTopic, parent.getTopicType(tm));
                                a.addPlayer(tagValueTopic, parent.getTagType(tm));
                                if(EXTRACT_RELEVANCE) {
                                    Topic relevance = parent.getRelevanceTopic(attribute_relevance, tm);
                                    Topic relevanceType = parent.getRelevanceType(tm);
                                    if(relevance != null && !relevance.isRemoved() && relevanceType != null && !relevanceType.isRemoved()) {
                                        a.addPlayer(relevance, relevanceType);
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        state=STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT: {
                    if(qName.equals(TAG_CALAISSIMPLEOUTPUTFORMAT)) {
                        state=STATE_OPENCALAISSIMPLE;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE: {
                    if(qName.equals(TAG_OPENCALAISSIMPLE)) {
                        state=STATE_START;
                    }
                    break;
                }
                
                // ---- DESCRIPTION ----
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_CALAISSESSIONID: {
                    if(qName.equals(TAG_CALAISSESSIONID)) {
                        state=STATE_OPENCALAISSIMPLE_DESCRIPTION;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWDISTRIBUTION: {
                    if(qName.equals(TAG_ALLOWDISTRIBUTION)) {
                        state=STATE_OPENCALAISSIMPLE_DESCRIPTION;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWSEARCH: {
                    if(qName.equals(TAG_ALLOWSEARCH)) {
                        state=STATE_OPENCALAISSIMPLE_DESCRIPTION;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_EXTERNALID: {
                    if(qName.equals(TAG_EXTERNALID)) {
                        state=STATE_OPENCALAISSIMPLE_DESCRIPTION;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ID: {
                    if(qName.equals(TAG_ID)) {
                        state=STATE_OPENCALAISSIMPLE_DESCRIPTION;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ABOUT: {
                    if(qName.equals(TAG_ABOUT)) {
                        state=STATE_OPENCALAISSIMPLE_DESCRIPTION;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_DESCRIPTION: {
                    if(qName.equals(TAG_DESCRIPTION)) {
                        state=STATE_OPENCALAISSIMPLE;
                    }
                    break;
                }
                
                // --- TOPICS & TOPIC ---
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS_TOPIC: {
                    if(qName.equals(TAG_TOPIC)) {
                        
                        try {
                            parent.setProgress( progress++ );
                            Topic calaisTopic = parent.getCalaisTopic(data_topic, data_topicTaxonomy, tm);
                            if(masterTopic != null && calaisTopic != null) {
                                Topic metadataType = parent.getCalaisTopicClassificationType(tm);
                                Association a = tm.createAssociation(metadataType);
                                a.addPlayer(masterTopic, parent.getTopicType(tm));
                                a.addPlayer(calaisTopic, parent.getCalaisTopicType(tm));
                                if(EXTRACT_SCORE) {
                                    Topic score = parent.getScoreTopic(data_topicScore, tm);
                                    Topic scoreType = parent.getScoreType(tm);
                                    if(score != null && !score.isRemoved() && scoreType != null && !scoreType.isRemoved()) {
                                        a.addPlayer(score, scoreType);
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        
                        state = STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS;
                    }
                    break;
                }
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS: {
                    if(qName.equals(TAG_TOPICS)) {
                        state = STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT;
                    }
                    break;
                }
            }
        }
        
        
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_CALAISSESSIONID:
                    data_calaissessionid+=new String(ch,start,length);
                    break;
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWDISTRIBUTION:
                    data_allowdistribution+=new String(ch,start,length);
                    break;
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ALLOWSEARCH:
                    data_allowsearch+=new String(ch,start,length);
                    break;
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_EXTERNALID:
                    data_externalid+=new String(ch,start,length);
                    break;
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ID:
                    data_id+=new String(ch,start,length);
                    break;
                case STATE_OPENCALAISSIMPLE_DESCRIPTION_ABOUT:
                    data_about+=new String(ch,start,length);
                    break;
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TAG:
                    tagValue+=new String(ch,start,length);
                    break;
                case STATE_OPENCALAISSIMPLE_CALAISSIMPLEOUTPUTFORMAT_TOPICS_TOPIC:
                    data_topic+=new String(ch,start,length);
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




    // -------------------------------------------------------------------------




    private static String apikey = null;
    public String solveAPIKey(Wandora wandora) {
        setWandora(wandora);
        return solveAPIKey();
    }
    public String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(getWandora(), "Please give valid apikey for OpenCalais.", apikey, "OpenCalais apikey", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return apikey;
    }




    public void forgetAuthorization() {
        apikey = null;
    }



    // -------------------------------------------------------------------------



    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        OpenCalaisConfiguration dialog=new OpenCalaisConfiguration(admin,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }


    // -------------------------------------------------------------------------


}
