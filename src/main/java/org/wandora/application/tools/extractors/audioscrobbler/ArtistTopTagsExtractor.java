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
 * TopTagsExtractor.java
 *
 * Created on 12. toukokuuta 2007, 12:44
 *
 */

package org.wandora.application.tools.extractors.audioscrobbler;



import java.io.InputStream;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * Extractor reads specific XML feed from Audioscrobbler's web api and converts the
 * XML feed to a topic map. Extractor reads the Top Tags feed. Example
 * of Top Tags is found at
 *
 * http://ws.audioscrobbler.com/1.0/artist/Metallica/toptags.xml
 *
 * Audioscrobber's web api documentation is found at
 *
 * http://www.audioscrobbler.net/data/webservices/
 *
 * @author akivela
 */
public class ArtistTopTagsExtractor extends AbstractAudioScrobblerExtractor {
    

	private static final long serialVersionUID = 1L;




	/** Creates a new instance of ArtistTopTagsExtractor */
    public ArtistTopTagsExtractor() {
    }
    
    
    

        
    @Override
    public String getName() {
        return "Audioscrobbler Artists: Top Tags extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the Top Tags XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/artist/Metallica/toptags.xml for an example of Top Tags XML feed.";
    }

    
    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        ArtistTopTagsParser parserHandler = new ArtistTopTagsParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " top tags with nonzero count found!");
        return true;
    }
    

    
    
    private static class ArtistTopTagsParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public ArtistTopTagsParser(TopicMap tm,ArtistTopTagsExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private ArtistTopTagsExtractor parent;
        
        public static final String TAG_TOPTAGS="toptags";
        public static final String TAG_TAG="tag";
        public static final String TAG_NAME="name";
        public static final String TAG_COUNT="count";
        public static final String TAG_URL="url";
              
       
        private static final int STATE_START=0;
        private static final int STATE_TOPTAGS=1;
        private static final int STATE_TAG=2;
        private static final int STATE_NAME=3;
        private static final int STATE_COUNT=4;
        private static final int STATE_URL=5;
               
        private int state=STATE_START;
        
        
        private String data_artist = "";
        private String data_tag_name = "";
        private String data_tag_count = "";
        private String data_tag_url = "";
        
        private Topic theArtist;
        

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
                    if(qName.equals(TAG_TOPTAGS)) {
                        state = STATE_TOPTAGS;
                        String theArtistString = atts.getValue("artist");
                        if(theArtistString != null) {
                            try {
                                theArtist=getArtistTopic(tm, theArtistString);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                case STATE_TOPTAGS:
                    if(qName.equals(TAG_TAG)) {
                        state = STATE_TAG;
                        data_tag_name = "";
                        data_tag_count = "";
                        data_tag_url = "";
                    }
                    break;
                case STATE_TAG:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_NAME;
                        data_tag_name = "";
                    }
                    else if(qName.equals(TAG_COUNT)) {
                        state = STATE_COUNT;
                        data_tag_count = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_URL;
                        data_tag_url = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_TAG: {
                    if(data_tag_name.length() > 0) {
                        try {
                            Topic tagType=getTagTypeTopic(tm);
                            Topic tagTopic=getTagTopic(tm, data_tag_name, data_tag_url);
                            if(theArtist != null) {
                                Topic artistType = getArtistTypeTopic(tm);
                                Association ta=tm.createAssociation(tagType);
                                ta.addPlayer(theArtist, artistType);
                                ta.addPlayer(tagTopic, tagType);
                                parent.setProgress(++progress);

                                if(CONVERT_COUNTS && data_tag_count.length() > 0) {
                                    try {
                                        int count = Integer.parseInt(data_tag_count);
                                        if(count > 0) {
                                            try {
                                                Topic countTopic = getCountTopic(tm, data_tag_count);
                                                Topic countType = getCountTypeTopic(tm);
                                                ta.addPlayer(countTopic, countType);
                                            }
                                            catch(Exception e) {
                                                parent.log(e);
                                            }
                                        }
                                    }
                                    catch(Exception e) {
                                        // PASS THIS EXCEPTION. PARSEINT FAILED. OK!
                                    }
                                }
                            }
                        }
                        catch(TopicMapException tme){
                            parent.log(tme);
                        }
                    }
                    state=STATE_TOPTAGS;
                    break;
                }
                case STATE_NAME: {
                    state=STATE_TAG;
                    break;
                }
                case STATE_COUNT: {
                    state=STATE_TAG;
                    break;
                }
                case STATE_URL: {
                    state=STATE_TAG;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_NAME:
                    data_tag_name+=new String(ch,start,length);
                    break;
                case STATE_COUNT:
                    data_tag_count+=new String(ch,start,length);
                    break;
                case STATE_URL:
                    data_tag_url+=new String(ch,start,length);
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
