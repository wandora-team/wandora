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
 * TagTopAlbumsExtractor.java
 *
 * Created on 17. toukokuuta 2007, 18:19
 *
 */

package org.wandora.application.tools.extractors.audioscrobbler;


import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;


/**
 *
 * @author akivela
 */
public class TagTopAlbumsExtractor extends AbstractAudioScrobblerExtractor {
    

	private static final long serialVersionUID = 1L;


	/** Creates a new instance of TagTopAlbumsExtractor */
    public TagTopAlbumsExtractor() {
    }
     
    @Override
    public String getName() {
        return "Audioscrobbler Tags: Top Albums extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the Top Albums with Tag XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/tag/Rock/topalbums.xml for an example of such an XML feed.";
    }

    
    
    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        TagTopAlbumsParser parserHandler = new TagTopAlbumsParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " top albums found!");
        return true;
    }
    

    private static class TagTopAlbumsParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public TagTopAlbumsParser(TopicMap tm, TagTopAlbumsExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private TagTopAlbumsExtractor parent;
        
        public static final String TAG_TAG="tag";
        public static final String TAG_ALBUM="album";
        public static final String TAG_ARTIST="artist";
        public static final String TAG_URL="url";
        public static final String TAG_MBID="mbid";
        public static final String TAG_COVERART="coverart";
        public static final String TAG_COVERART_LARGE="large";
        public static final String TAG_COVERART_MEDIUM="medium";
        public static final String TAG_COVERART_SMALL="small";
        
        private static final int STATE_START=0;
        private static final int STATE_TAG=1;
        private static final int STATE_ALBUM=2;
        private static final int STATE_ALBUM_ARTIST=3;
        private static final int STATE_ALBUM_ARTIST_MBID=4;
        private static final int STATE_ALBUM_ARTIST_URL=5;
        private static final int STATE_ALBUM_URL=6;
        private static final int STATE_ALBUM_COVERART=8;
        private static final int STATE_ALBUM_COVERART_LARGE=9;
        private static final int STATE_ALBUM_COVERART_MEDIUM=10;
        private static final int STATE_ALBUM_COVERART_SMALL=11;
               
        private int state=STATE_START;
        

        
        private String data_album_streamable;
        private String data_album_count;
        private String data_album_name;
        private String data_album_url;
        
        private String data_artist_mbid;
        private String data_artist_url;
        private String data_artist_name;

        private String data_album_coverart_large;
        private String data_album_coverart_medium;
        private String data_album_coverart_small;
        
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
                        String theTagString = atts.getValue("tag");
                        String theCountString = atts.getValue("count");
                        if(theTagString != null && theTagString.length() > 0) {
                            try {
                                theTag=getTagTopic(tm, theTagString);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                case STATE_TAG:
                    if(qName.equals(TAG_ALBUM)) {
                        data_album_name = atts.getValue("name");
                        data_album_count = atts.getValue("count");
                        data_album_streamable = atts.getValue("streamable");
                        state = STATE_ALBUM;
                    }
                    break;
                case STATE_ALBUM:
                    if(qName.equals(TAG_ARTIST)) {
                        state = STATE_ALBUM_ARTIST;
                        data_artist_name = atts.getValue("name");
                        data_artist_mbid = "";
                        data_artist_url = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_ALBUM_URL;
                        data_album_url = "";
                    }
                    else if(qName.equals(TAG_COVERART)) {
                        state = STATE_ALBUM_COVERART;
                        data_album_coverart_small = "";
                        data_album_coverart_medium = "";
                        data_album_coverart_large = "";
                    }
                    break;
                case STATE_ALBUM_ARTIST:
                    if(qName.equals(TAG_MBID)) {
                        state = STATE_ALBUM_ARTIST_MBID;
                        data_artist_mbid = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_ALBUM_ARTIST_URL;
                        data_artist_url = "";
                    }
                    break;
                case STATE_ALBUM_COVERART:
                    if(qName.equals(TAG_COVERART_SMALL)) {
                        state = STATE_ALBUM_COVERART_SMALL;
                        data_album_coverart_small = "";
                    }
                    else if(qName.equals(TAG_COVERART_MEDIUM)) {
                        state = STATE_ALBUM_COVERART_MEDIUM;
                        data_album_coverart_medium = "";
                    }
                    else if(qName.equals(TAG_COVERART_LARGE)) {
                        state = STATE_ALBUM_COVERART_LARGE;
                        data_album_coverart_large = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_ALBUM: {
                    if(data_album_name != null && data_album_name.length() > 0) {
                        try {
                            Topic albumTopic=getAlbumTopic(tm, data_album_name, data_album_url, data_artist_name);
                            
                            Topic albumType=getAlbumTypeTopic(tm);
                            String data_album_coverart = "";
                            if(data_album_coverart_large.length() > 0) {
                                data_album_coverart = data_album_coverart_large;
                            }
                            else if(data_album_coverart_medium.length() > 0) {
                                data_album_coverart = data_album_coverart_medium;
                            }
                            else if(data_album_coverart_small.length() > 0) {
                                data_album_coverart = data_album_coverart_small;
                            }
                            if(data_album_coverart.length() > 0) {
                                try {
                                    Topic imageType = getImageTypeTopic(tm);
                                    Topic image = getImageTopic(tm, data_album_coverart, "album "+data_album_name);
                                    Association imagea = tm.createAssociation(imageType);
                                    imagea.addPlayer(image, imageType);
                                    imagea.addPlayer(albumTopic, albumType);
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                            
                            if(theTag != null) {
                                Topic tagType = getTagTypeTopic(tm);
                                Association a = tm.createAssociation(tagType);
                                a.addPlayer(theTag, tagType);
                                a.addPlayer(albumTopic, albumType);
                                if(CONVERT_COUNTS && data_album_count != null && data_album_count.length() > 0) {
                                    Topic countType = getCountTypeTopic(tm);
                                    Topic countTopic = getCountTopic(tm,data_album_count);
                                    a.addPlayer(countTopic, countType);
                                }
                            }
                            
                            if(data_artist_name != null && data_artist_name.length() > 0) {
                                Topic artistType = getArtistTypeTopic(tm);
                                System.out.println("creating artist topic: "+data_artist_name+", "+data_artist_url);
                                Topic artistTopic = getArtistTopic(tm, data_artist_name, data_artist_url, data_artist_mbid);
                                Association a = tm.createAssociation(albumType);
                                a.addPlayer(artistTopic, artistType);
                                a.addPlayer(albumTopic, albumType);
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
                
                case STATE_ALBUM_ARTIST: {
                    state=STATE_ALBUM;
                    break;
                }
                // --- Closing album's inner ---
                case STATE_ALBUM_COVERART: {
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_ALBUM_URL: {
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_ALBUM_ARTIST_URL: {
                    state=STATE_ALBUM_ARTIST;
                    break;
                }
                case STATE_ALBUM_ARTIST_MBID: {
                    state=STATE_ALBUM_ARTIST;
                    break;
                }
                // --- Closing cover art's ---
                case STATE_ALBUM_COVERART_LARGE: {
                    state=STATE_ALBUM_COVERART;
                    break;
                }
                case STATE_ALBUM_COVERART_MEDIUM: {
                    state=STATE_ALBUM_COVERART;
                    break;
                }
                case STATE_ALBUM_COVERART_SMALL: {
                    state=STATE_ALBUM_COVERART;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_ALBUM_ARTIST_MBID:
                    data_artist_mbid+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_ARTIST_URL:
                    data_artist_url+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_URL:
                    // This is effectively same as artist's url.
                    // Not using as it causes artists and album merge!
                    //
                    // data_album_url+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_COVERART_LARGE:
                    data_album_coverart_large+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_COVERART_MEDIUM:
                    data_album_coverart_medium+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_COVERART_SMALL:
                    data_album_coverart_small+=new String(ch,start,length);
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
