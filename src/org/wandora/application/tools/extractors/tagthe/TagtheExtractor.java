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
 *
 *
 */


package org.wandora.application.tools.extractors.tagthe;

import java.net.*;
import java.io.*;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;

/**
 *
 * @author akivela
 */
public class TagtheExtractor extends AbstractTagtheExtractor {


    @Override
    public String getName() {
        return "Tagthe Extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts terms out of given text using Tagthe web service. Read more at http://www.tagthe.net.";
    }



    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }


    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String data = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(data, topicMap);
    }


    public boolean _extractTopicsFrom(String data, TopicMap topicMap) throws Exception {
        if(data != null && data.length() > 0) {
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

            String extractURL = WEB_SERVICE_URL+"?text="+URLEncoder.encode(content, "utf-8");
            String result = IObox.doUrl(new URL(extractURL));

            //System.out.println("Tagthe returned == "+result);

            javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            javax.xml.parsers.SAXParser parser=factory.newSAXParser();
            XMLReader reader=parser.getXMLReader();
            TagtheExtractorParser parserHandler = new TagtheExtractorParser(getMasterSubject(), content, topicMap, this);
            reader.setContentHandler(parserHandler);
            reader.setErrorHandler(parserHandler);
            try {
                reader.parse(new InputSource(new StringReader(result)));
            }
            catch(Exception e){
                if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
            }
            log("Total " + parserHandler.progress + " terms found by Tagthe");
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }




    // -------------------------------------------------------------------------






    private class TagtheExtractorParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public TagtheExtractorParser(String term, String data, TopicMap tm, TagtheExtractor parent){
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
        private TagtheExtractor parent;

        public static final String TAG_MEMES="memes";
        public static final String TAG_MEME="meme";
        public static final String TAG_DIM="dim";
        public static final String TAG_ITEM="item";

        private static final String ATTRIBUTE_TYPE = "type";

        private static final int STATE_START=0;
        private static final int STATE_MEMES=1;
        private static final int STATE_MEMES_MEME=11;
        private static final int STATE_MEMES_MEME_DIM=111;
        private static final int STATE_MEMES_MEME_DIM_ITEM=1111;

        
       
        private int state=STATE_START;


        private String data_item = "";
        private String data_type = "";


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
                    if(qName.equals(TAG_MEMES)) {
                        state = STATE_MEMES;
                    }
                    break;
                case STATE_MEMES:
                    if(qName.equals(TAG_MEME)) {
                        state = STATE_MEMES_MEME;
                    }
                    break;

                case STATE_MEMES_MEME:
                    if(qName.equals(TAG_DIM)) {
                        data_type = atts.getValue(ATTRIBUTE_TYPE);
                        state = STATE_MEMES_MEME_DIM;
                    }
                    break;

                case STATE_MEMES_MEME_DIM:
                    if(qName.equals(TAG_ITEM)) {
                        data_item = "";
                        state = STATE_MEMES_MEME_DIM_ITEM;
                    }
                    break;
            }
        }







        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_MEMES_MEME_DIM_ITEM:
                    if(qName.equals(TAG_ITEM)) {

                        parent.setProgress( progress++ );
                        if(data_item != null && data_item.length() > 0) {
                            try {
                                if(parent.getCurrentLogger() != null) parent.log("Tagthe found term '"+data_item+"'.");
                                Topic termTopic = parent.getTermTopic(data_item, data_type, tm);
                                if(masterTopic != null && termTopic != null) {
                                    Topic termType = parent.getTermTypeType(tm);
                                    Association a = tm.createAssociation(termType);
                                    a.addPlayer(masterTopic, parent.getTopicType(tm));
                                    a.addPlayer(termTopic, termType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        else {
                            parent.log("Zero length term text found! Rejecting!");
                        }

                        state = STATE_MEMES_MEME_DIM;
                    }
                    break;

                    
                case STATE_MEMES_MEME_DIM:
                    if(qName.equals(TAG_DIM)) {
                        state = STATE_MEMES_MEME;
                    }
                    break;


                    
                case STATE_MEMES_MEME:
                    if(qName.equals(TAG_MEME)) {
                        state = STATE_MEMES;
                    }
                    break;


                case STATE_MEMES:
                    if(qName.equals(TAG_MEMES)) {
                        state = STATE_START;
                    }
                    break;

                
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_MEMES_MEME_DIM_ITEM:
                    data_item+=new String(ch,start,length);
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

