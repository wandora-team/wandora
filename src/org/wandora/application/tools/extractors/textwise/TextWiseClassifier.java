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
 * TextWiseClassifier.java
 *
 * Created on 2008-09-18
 *
 */
package org.wandora.application.tools.extractors.textwise;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;
import java.io.*;
import javax.swing.*;

import java.net.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.xml.sax.*;




/**
 *
 * @author akivela
 */
public class TextWiseClassifier extends AbstractExtractor {
    public static final boolean USE_WEIGHTS = true;

    private static final String API_ADDRESS = "http://api.semantichacker.com/__TOKEN__/concept/";
    
    protected String ROOT_SI = "http://www.semantichacker.com";

    protected String DIMENSION_SI = "http://www.semantichacker.com/dimension";
    protected String CLASSIFICATION_SI = "http://www.semantichacker.com/classification";
    protected String WEIGHT_SI = "http://www.semantichacker.com/weight";
    
    protected String TOPIC_SI = "http://wandora.org/si/topic";
    protected String SOURCE_SI = "http://wandora.org/si/source";
    protected String DOCUMENT_SI = "http://wandora.org/si/document";
    
    
    private String contentType = "text";
    private String defaultEncoding = "ISO-8859-1";
    private String token = "";




    
    /** Creates a new instance of TextWiseClassifier */
    public TextWiseClassifier() {
    }
    

    @Override
    public String getName() {
        return "TextWise classifier";
    }
    
    @Override
    public String getDescription(){
        return "Classifies texts documents and fragments using TextWise.";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_textwise.png");
    }
    
    private final String[] contentTypes=new String[] { "text/plain", "text/html" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }





    // -------------------------------------------------------------------------



    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        setWandora(wandora);
        return ExtractHelper.doBrowserExtractForClassifiers(this, request, wandora, defaultEncoding);
    }


    
    // -------------------------------------------------------------------------


    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String in = ExtractHelper.getContent(url);
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
            String content;
            try {
                content = XMLbox.cleanUp(in);
                if(content == null || content.length() < 1) {
                    // Tidy failed to fix the file...
                    content = in;
                    contentType = "html";
                }
                else {
                    // Ok, Tidy fixed the html/xml document
                    content = XMLbox.getAsText(content, defaultEncoding);
                    //System.out.println("content after getAsText: "+content);
                    contentType = "text";
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                content = in;
                contentType = "text";
            }

            // Force token request! Remove "true" to skip the token request.
            if(true || token == null || token.length() == 0) {
                setState(INVISIBLE);
                if(token == null) token = "";
                token = WandoraOptionPane.showInputDialog(getWandora(), "Please provide valid TextWise token for classification:", token);
                setState(VISIBLE);
                if(token == null || token.length() == 0) {
                    log("Invalid TextWise token provided! Aborting!");
                    return true;
                }
            }
            if(content.length() > 1000) content = content.substring(0,999);
            String encodedContent = URLEncoder.encode(content, defaultEncoding);
            String request = API_ADDRESS;
            request = request.replaceAll("__TOKEN__", token.trim());
            request = request + "?filter="+contentType+"&content="+encodedContent+"";
            
            String result = IObox.doUrl(new URL(request));
            //System.out.println("Result = "+result);

            // ---- Parse results ----
            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            TextWiseResultParser parserHandler = new TextWiseResultParser(getMasterSubject(), content, topicMap,this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            try{
                reader.parse(new InputSource(new ByteArrayInputStream(result.getBytes())));
            }
            catch(Exception e){
                e.printStackTrace();
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }
            log("Total " + parserHandler.progress + " TextWise dimensions found for classified document!");
        }
        catch (Exception ex) {
           log(ex);
           ex.printStackTrace();
        }
        clearMasterSubject();
        return true;
    }
    
    

    public String solveTitle(String content) {
        if(content == null || content.length() == 0) return "empty-document";
        
        boolean forceTrim = false;
        String title;
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
    
    
    public Topic getDimensionTopic(String dimension, TopicMap tm) throws TopicMapException {
        if(dimension != null) {
            dimension = dimension.trim(); 
            if(dimension.length() > 0) {
                Topic dimensionTopic=getOrCreateTopic(tm, DIMENSION_SI+"/"+dimension, dimension);
                Topic dimensionTypeTopic = getDimensionType(tm);
                dimensionTopic.addType(dimensionTypeTopic);
                return dimensionTopic;
            }
        }
        return null;
    }

    

    
    public Topic getDimensionType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIMENSION_SI, "TextWise dimension");
        Topic s = getTextWiseClass(tm);
        makeSubclassOf(tm, type, s);
        return type;
    }
    
    
    
    public Topic getWeightTopic(String weight, TopicMap tm) throws TopicMapException {
        if(weight != null) {
            weight = weight.trim(); 
            if(weight.length() > 0) {
                Topic weightTopic=getOrCreateTopic(tm, WEIGHT_SI+"/"+weight, weight);
                Topic weightTypeTopic = getWeightType(tm);
                weightTopic.addType(weightTypeTopic);
                return weightTopic;
            }
        }
        return null;
    }
    
    
    public Topic getWeightType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, WEIGHT_SI, "TextWise weight");
        Topic s = getTextWiseClass(tm);
        makeSubclassOf(tm, type, s);
        return type;
    }
    
    

    public Topic getTextWiseClass(TopicMap tm) throws TopicMapException {
        Topic s = getOrCreateTopic(tm, ROOT_SI, "Semantic hacker");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, s, w);
        return s;
    }



    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    
    public Topic getClassificationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, CLASSIFICATION_SI,"TextWise classification");
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
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    
    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private static class TextWiseResultParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        Topic masterTopic = null;
        
        
        public TextWiseResultParser(Topic t, TopicMap tm, TextWiseClassifier parent){
            this.masterTopic=t;
            this.tm=tm;
            this.parent=parent;
        }
        
        public TextWiseResultParser(String term, String text, TopicMap tm, TextWiseClassifier parent){
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
        private TextWiseClassifier parent;
        
        public static final String TAG_RESPONSE ="response";
        public static final String TAG_CONCEPT="conceptExtractorResponse";
        public static final String TAG_DIMENSION="concept";
        public static final String TAG_MESSAGE  = "message";
        
        private static final int STATE_START=0;
        private static final int STATE_RESPONSE=2;
        private static final int STATE_SIGNATURE=4;
        private static final int STATE_DIMENSION=5;
        private static final int STATE_MESSAGE=6;
        
        private int state=STATE_START;


        
        
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
                    if(qName.equals(TAG_RESPONSE)) {
                        state = STATE_RESPONSE;
                    }
                    break;
                case STATE_RESPONSE:
                    if(qName.equals(TAG_CONCEPT)) {
                        state = STATE_SIGNATURE;
                    }
                    else if(qName.equals(TAG_MESSAGE)) {
                        // Error message
                        String code = atts.getValue("code");
                        String message = atts.getValue("string");
                        parent.log("TextWise api returned error message ("+code+"): "+message);
                        state = STATE_MESSAGE;
                    }
                    break;
                case STATE_SIGNATURE:
                    if(qName.equals(TAG_DIMENSION)) {
                        String label = atts.getValue("label");
                        //String index = atts.getValue("index");
                        String weight = atts.getValue("weight");
                        
                        if(weight != null) {
                            parent.setProgress(progress++);
                            try {
                                Topic dimensionTopic = parent.getDimensionTopic(label, tm);
                                if(masterTopic != null && dimensionTopic != null) {
                                    Topic classificationType = parent.getClassificationType(tm);
                                    Association a = tm.createAssociation(classificationType);
                                    a.addPlayer(masterTopic, parent.getTopicType(tm));
                                    a.addPlayer(dimensionTopic, parent.getDimensionType(tm));
                                    if(USE_WEIGHTS && weight != null) {
                                        Topic weightType = parent.getWeightType(tm);
                                        Topic weightTopic = parent.getWeightTopic(weight, tm);
                                        a.addPlayer(weightTopic, weightType);
                                    }
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_DIMENSION;
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_DIMENSION: {
                    if(qName.equals(TAG_DIMENSION)) {
                        state = STATE_SIGNATURE;
                    }
                    break;
                }
                case STATE_SIGNATURE: {
                    if(qName.equals(TAG_DIMENSION)) {
                        state=STATE_RESPONSE;
                    }
                    break;
                }
                case STATE_RESPONSE: {
                    if(qName.equals(TAG_RESPONSE)) {
                        state=STATE_RESPONSE;
                    }
                    break;
                }
                case STATE_MESSAGE: {
                    if(qName.equals(TAG_MESSAGE)) {
                        state=STATE_RESPONSE;
                    }
                    break;
                }
            }
        }
        
        
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
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
