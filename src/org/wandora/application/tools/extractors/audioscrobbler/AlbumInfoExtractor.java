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
 * AlbumInfoExtractor.java
 *
 * Created on 13. toukokuuta 2007, 19:20
 *
 */

package org.wandora.application.tools.extractors.audioscrobbler;



import java.io.*;
import org.xml.sax.*;

import org.wandora.topicmap.*;



/**
 * Extractor reads specific XML feed from Audioscrobbler's web api and converts the
 * XML feed to a topic map. Extractor reads the Album Info feed. Example
 * of Album Info is found at
 *
 * http://ws.audioscrobbler.com/1.0/album/Metallica/Metallica/info.xml
 *
 * Audioscrobbler's web api documentation is found at
 *
 * http://www.audioscrobbler.net/data/webservices/
 *
 * @author akivela
 */
public class AlbumInfoExtractor extends AbstractAudioScrobblerExtractor {
    
    /** Creates a new instance of AlbumInfoExtractor */
    public AlbumInfoExtractor() {
    }


    @Override
    public String getName() {
        return "Audioscrobbler Album: Info extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the Album Info XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/album/Metallica/Metallica/info.xml for an example of Album Info XML feed.";
    }

    



    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        AlbumInfoParser parserHandler = new AlbumInfoParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " tracks found!");
        return true;
    }
    

    private static class AlbumInfoParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public AlbumInfoParser(TopicMap tm, AlbumInfoExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private AlbumInfoExtractor parent;
        
        public static final String TAG_ALBUM="album";
        public static final String TAG_REACH="reach";
        public static final String TAG_URL="url";
        public static final String TAG_COVERART="coverart";
        public static final String TAG_COVERART_LARGE="large";
        public static final String TAG_COVERART_MEDIUM="medium";
        public static final String TAG_COVERART_SMALL="small";
        public static final String TAG_MBID="mbid";
        public static final String TAG_RELEASEDATE="releasedate";
        public static final String TAG_TRACKS="tracks";
        public static final String TAG_TRACK="track";
        
        private static final int STATE_START=0;
        private static final int STATE_ALBUM=2;
        private static final int STATE_ALBUM_MBID=4;
        private static final int STATE_ALBUM_REACH=5;
        private static final int STATE_ALBUM_URL=6;
        private static final int STATE_ALBUM_RELEASEDATE=7;
        private static final int STATE_ALBUM_COVERART=8;
        private static final int STATE_ALBUM_COVERART_LARGE=9;
        private static final int STATE_ALBUM_COVERART_MEDIUM=10;
        private static final int STATE_ALBUM_COVERART_SMALL=11;
        private static final int STATE_TRACKS=12;
        private static final int STATE_TRACK=13;
        private static final int STATE_TRACK_REACH=14;
        private static final int STATE_TRACK_URL=15;
               
        private int state=STATE_START;
        
        
        private String data_album_reach;
        private String data_album_mbid;
        private String data_album_url;
        private String data_album_releasedate;
        private String data_album_coverart_large;
        private String data_album_coverart_medium;
        private String data_album_coverart_small;
        private String data_track;
        private String data_track_url;
        private String data_track_reach;
        
        private Topic theArtist;
        private Topic theAlbum;
        private int trackIndex = 0;
        
        private String theArtistString = null;
        private String theAlbumString = null;



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
                    if(qName.equals(TAG_ALBUM)) {
                        state = STATE_ALBUM;
                        theArtistString = atts.getValue("artist");
                        theAlbumString = atts.getValue("title");
                        if(theArtistString != null && theAlbumString != null) {
                            try {                                
                                theArtist = getArtistTopic(tm, theArtistString);
                                theAlbum = getAlbumTopic(tm, theAlbumString, theArtistString);
                                
                                Topic albumType = getAlbumTypeTopic(tm);
                                Topic artistType = getArtistTypeTopic(tm);
                                Association a = tm.createAssociation(albumType);
                                a.addPlayer(theAlbum, albumType);
                                a.addPlayer(theArtist, artistType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                case STATE_ALBUM:
                    if(qName.equals(TAG_REACH)) {
                        state = STATE_ALBUM_REACH;
                        data_album_reach = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_ALBUM_URL;
                        data_album_url = "";
                    }
                    else if(qName.equals(TAG_RELEASEDATE)) {
                        state = STATE_ALBUM_RELEASEDATE;
                        data_album_releasedate = "";
                    }
                    else if(qName.equals(TAG_MBID)) {
                        state = STATE_ALBUM_MBID;
                        data_album_mbid = "";
                    }
                    else if(qName.equals(TAG_TRACKS)) {
                        state = STATE_TRACKS;
                        data_album_mbid = "";
                    }
                    else if(qName.equals(TAG_COVERART)) {
                        data_album_coverart_small = "";
                        data_album_coverart_medium = "";
                        data_album_coverart_large = "";
                        state = STATE_ALBUM_COVERART;
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
                case STATE_TRACKS:
                    if(qName.equals(TAG_TRACK)) {
                        data_track = atts.getValue("title");
                        data_track_reach = "";
                        data_track_url = "";
                        state = STATE_TRACK;
                    }
                    break;
                case STATE_TRACK:
                    if(qName.equals(TAG_REACH)) {
                        state = STATE_TRACK_REACH;
                        data_track_reach = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_TRACK_URL;
                        data_track_url = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_TRACK: {
                    if(data_track.length() > 0) {
                        try {
                            Topic trackType=getTrackTypeTopic(tm);
                            Topic trackTopic=getTrackTopic(tm, data_track, data_track_url, theAlbumString, theArtistString);

                            try {
                                ++trackIndex;
                                Association ta=tm.createAssociation(trackType);
                                parent.setProgress(++progress);
                                Topic albumType = getAlbumTypeTopic(tm);
                                ta.addPlayer(theAlbum, albumType);
                                ta.addPlayer(trackTopic, trackType);
                                if(CONVERT_TRACK_INDEX) {
                                    Topic indexTopic = getIndexTopic(tm, trackIndex);
                                    Topic indexType = getIndexTypeTopic(tm);
                                    ta.addPlayer(indexTopic, indexType);                                    
                                }
                                if(CONVERT_REACH && data_track_reach.length() > 0) {
                                    Topic reachTopic = getReachTopic(tm, data_track_reach);
                                    Topic reachType = getReachTypeTopic(tm);
                                    ta.addPlayer(reachTopic, reachType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        catch(TopicMapException tme){
                            parent.log(tme);
                        }
                    }
                    state=STATE_TRACKS;
                    break;
                }
                case STATE_TRACK_REACH: {
                    state=STATE_TRACK;
                    break;
                }
                case STATE_TRACK_URL: {
                    state=STATE_TRACK;
                    break;
                }
                // --- Closing album's inner ---
                case STATE_ALBUM_COVERART: {
                    if(theAlbum != null) {
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
                                Topic image = getImageTopic(tm, data_album_coverart, "album "+theAlbumString);
                                Topic albumType = getAlbumTypeTopic(tm);
                                Topic imageType = getImageTypeTopic(tm);
                                Association imagea = tm.createAssociation(imageType);
                                imagea.addPlayer(image, imageType);
                                imagea.addPlayer(theAlbum, albumType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_ALBUM_URL: {
                    if(theAlbum != null && data_album_url.length() > 0) {
                        try {
                            theAlbum.addSubjectIdentifier(tm.createLocator(data_album_url));
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_ALBUM_REACH: {
                    // DO NOTHING AT THE MOMENT
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_ALBUM_RELEASEDATE: {
                    if(theAlbum != null && data_album_releasedate != null) {
                        data_album_releasedate = data_album_releasedate.trim();
                        if(data_album_releasedate.length() > 0) {
                            try {
                                Topic releasedateType = getOrCreateTopic(tm, RELEASEDATE_SI, "Release date");
                                parent.setData(theAlbum, releasedateType, LANG, data_album_releasedate);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_ALBUM_MBID: {
                    if(theAlbum != null && data_album_mbid.length() > 0) {
                        try {
                            theAlbum.addSubjectIdentifier(tm.createLocator(MBID_SI + "/" + data_album_mbid));
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_TRACKS: {
                    state=STATE_ALBUM;
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
                case STATE_TRACK_REACH:
                    data_track_reach+=new String(ch,start,length);
                    break;
                case STATE_TRACK_URL:
                    data_track_url+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_MBID:
                    data_album_mbid+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_REACH:
                    data_album_reach+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_URL:
                    data_album_url+=new String(ch,start,length);
                    break;
                case STATE_ALBUM_RELEASEDATE:
                    data_album_releasedate+=new String(ch,start,length);
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
