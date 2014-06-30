/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * TagTopArtistsExtractor.java
 *
 * Created on 17. toukokuuta 2007, 17:22
 */

package org.wandora.application.tools.extractors.audioscrobbler;


import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;

import org.wandora.application.gui.*;
import javax.swing.*;

/**
 *
 * @author akivela
 */
public class TagTopArtistsExtractor extends AbstractAudioScrobblerExtractor {
    
    /**
     * Creates a new instance of TagTopArtistsExtractor
     */
    public TagTopArtistsExtractor() {
    }
    
    
        
    @Override
    public String getName() {
        return "Audioscrobbler Tags: Top Artists extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extractor reads the Top Artists with Tag XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/tag/Rock/topartists.xml for an example of such an XML feed.";
    }

    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        TagTopArtistParser parserHandler = new TagTopArtistParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " top artists with the tag found!");
        return true;
    }
    

    private static class TagTopArtistParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public TagTopArtistParser(TopicMap tm,TagTopArtistsExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress = 0;
        private TopicMap tm;
        private TagTopArtistsExtractor parent;
        
        public static final String TAG_TAG="tag";
        public static final String TAG_ARTIST="artist";
        public static final String TAG_ARTIST_MBID="mbid";
        public static final String TAG_ARTIST_URL="url";
        public static final String TAG_ARTIST_THUMBNAIL="thumbnail";
        public static final String TAG_ARTIST_IMAGE="image";
        
        private static final int STATE_START=0;
        private static final int STATE_TAG=1;
        private static final int STATE_ARTIST=2;
        private static final int STATE_ARTIST_MBID=4;
        private static final int STATE_ARTIST_URL=6;
        private static final int STATE_ARTIST_THUMBNAIL=7;
        private static final int STATE_ARTIST_IMAGE=8;
        
        private int state=STATE_START;
        

        
        private String data_artist_name = "";
        private String data_artist_count = "";
        private String data_artist_streamable = "";
        private String data_artist_mbid = "";
        private String data_artist_url = "";
        private String data_artist_thumbnail = "";
        private String data_artist_image = "";
        
        
        private Topic theTag;
        
        
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
                    if(qName.equals(TAG_TAG)) {
                        state = STATE_TAG;
                        String theTagName = atts.getValue("tag");
                        String theTagCount = atts.getValue("count");
                        if(theTagName != null) {
                            try {
                                theTag = getTagTopic(tm, theTagName);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                case STATE_TAG:
                    if(qName.equals(TAG_ARTIST)) {
                        state = STATE_ARTIST;
                        data_artist_name = atts.getValue("name");
                        data_artist_count = atts.getValue("count");
                        data_artist_streamable = atts.getValue("streamable");
                        data_artist_mbid = "";
                        data_artist_url = "";
                        data_artist_thumbnail = "";
                        data_artist_image = "";
                    }
                    break;
                case STATE_ARTIST:
                    if(qName.equals(TAG_ARTIST_MBID)) {
                        state = STATE_ARTIST_MBID;
                        data_artist_mbid = "";
                    }
                    else if(qName.equals(TAG_ARTIST_URL)) {
                        state = STATE_ARTIST_URL;
                        data_artist_url = "";
                    }
                    else if(qName.equals(TAG_ARTIST_THUMBNAIL)) {
                        state = STATE_ARTIST_THUMBNAIL;
                        data_artist_thumbnail = "";
                    }
                    else if(qName.equals(TAG_ARTIST_IMAGE)) {
                        state = STATE_ARTIST_IMAGE;
                        data_artist_image = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_ARTIST: {
                    if(data_artist_name != null && data_artist_name.length() > 0){
                        try{
                            Topic artistType=getArtistTypeTopic(tm);
                            Topic tagType=getTagTypeTopic(tm);
                            
                            Topic artistTopic=getArtistTopic(tm, data_artist_name, data_artist_url, data_artist_mbid);
                            if(data_artist_streamable.length() > 0) {
                                Topic streamableType = getStreamableTypeTopic(tm);
                                parent.setData(artistTopic, streamableType, LANG, data_artist_streamable);
                            }

                            if(artistTopic != null && theTag != null) {
                                Association a=tm.createAssociation(tagType);
                                a.addPlayer(artistTopic, artistType);
                                a.addPlayer(theTag, tagType);
                                if(CONVERT_COUNTS && data_artist_count.length() > 0) {
                                    Topic countTopic = getCountTopic(tm, data_artist_count);
                                    Topic countType = getCountTypeTopic(tm);
                                    a.addPlayer(countTopic, countType);
                                }
                            }
                            if(data_artist_image == null || data_artist_image.length() == 0) {
                                if(data_artist_thumbnail != null && data_artist_thumbnail.length() > 0) {
                                    data_artist_image = data_artist_thumbnail;
                                }
                            }
                            if(data_artist_image != null && data_artist_image.length() > 0) {
                                Topic imageType = getImageTypeTopic(tm);
                                Topic image = getImageTopic(tm, data_artist_image, "artist "+data_artist_name);
                                Association imagea = tm.createAssociation(imageType);
                                imagea.addPlayer(image, imageType);
                                imagea.addPlayer(artistTopic, artistType);
                            }
                            parent.setProgress(++progress);
                        }
                        catch(TopicMapException tme){
                            parent.log(tme);
                        }
                    }
                    state=STATE_TAG;
                    break;
                }
                case STATE_ARTIST_MBID: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_URL: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_THUMBNAIL: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_IMAGE: {
                    state=STATE_ARTIST;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_ARTIST_MBID:
                    data_artist_mbid+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_URL:
                    data_artist_url+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_THUMBNAIL:
                    data_artist_thumbnail+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_IMAGE:
                    data_artist_image+=new String(ch,start,length);
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
