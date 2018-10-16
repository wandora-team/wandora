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
 * TopTracksExtractor.java
 *
 * Created on 12. toukokuuta 2007, 19:14
 *
 */

package org.wandora.application.tools.extractors.audioscrobbler;



import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;


/**
 * Extractor reads specific XML feed from Audioscrobbler's web api and converts the
 * XML feed to a topic map. Extractor reads the Top Tracks feed. Example
 * of Top Tracks is found at
 *
 * http://ws.audioscrobbler.com/1.0/artist/Metallica/toptracks.xml
 *
 * Audioscrobber's web api documentation is found at
 *
 * http://www.audioscrobbler.net/data/webservices/
 *
 * @author akivela
 */
public class ArtistTopTracksExtractor extends AbstractAudioScrobblerExtractor {
    

	private static final long serialVersionUID = 1L;


	/** Creates a new instance of ArtistTopTracksExtractor */
    public ArtistTopTracksExtractor() {
    }
     
    @Override
    public String getName() {
        return "Audioscrobbler Artists: Top Tracks extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the Top Tracks XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/artist/Metallica/toptracks.xml for an example of Top Tracks XML feed.";
    }

    
    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        ArtistTopTracksParser parserHandler = new ArtistTopTracksParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " top tracks found!");
        return true;
    }
    

    private static class ArtistTopTracksParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public ArtistTopTracksParser(TopicMap tm,ArtistTopTracksExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private ArtistTopTracksExtractor parent;
        
        public static final String TAG_MOSTKNOWNTRACKS="mostknowntracks";
        public static final String TAG_TRACK="track";
        public static final String TAG_NAME="name";
        public static final String TAG_MBID="mbid";
        public static final String TAG_REACH="reach";
        public static final String TAG_URL="url";
              
       
        private static final int STATE_START=0;
        private static final int STATE_MOSTKNOWNTRACKS=1;
        private static final int STATE_TRACK=2;
        private static final int STATE_NAME=3;
        private static final int STATE_MBID=4;
        private static final int STATE_REACH=5;
        private static final int STATE_URL=6;
               
        private int state=STATE_START;
        
        
        private String data_track_name = "";
        private String data_track_mbid = "";
        private String data_track_reach = "";
        private String data_track_url = "";
        
        private Topic theArtist;
        private String theArtistString = "_";
        private String theAlbumString = "_";
        
        
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
                    if(qName.equals(TAG_MOSTKNOWNTRACKS)) {
                        state = STATE_MOSTKNOWNTRACKS;
                        theAlbumString = "_";
                        theArtistString = atts.getValue("artist");
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
                case STATE_MOSTKNOWNTRACKS:
                    if(qName.equals(TAG_TRACK)) {
                        state = STATE_TRACK;
                        data_track_name = "";
                        data_track_mbid = "";
                        data_track_reach = "";
                        data_track_url = "";
                    }
                    break;
                case STATE_TRACK:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_NAME;
                        data_track_name = "";
                    }
                    else if(qName.equals(TAG_MBID)) {
                        state = STATE_MBID;
                        data_track_mbid = "";
                    }
                    else if(qName.equals(TAG_REACH)) {
                        state = STATE_REACH;
                        data_track_reach = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_URL;
                        data_track_url = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_TRACK: {
                    if(data_track_name.length() > 0) {
                        try {
                            Topic trackType=getTrackTypeTopic(tm);
                            Topic trackTopic=getTrackTopic(tm, data_track_name, data_track_mbid, theAlbumString, theArtistString);
                            Topic artistType=getArtistTypeTopic(tm);
                            Association ta=tm.createAssociation(trackType);
                            ta.addPlayer(theArtist, artistType);
                            ta.addPlayer(trackTopic, trackType);
                            if(CONVERT_REACH && data_track_reach.length() > 0) {
                                try {
                                    Topic reachTopic = getReachTopic(tm, data_track_reach);
                                    Topic reachType = getReachTypeTopic(tm);
                                    ta.addPlayer(reachTopic, reachType);
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                            parent.setProgress(++progress);
                        }
                        catch(TopicMapException tme){
                            parent.log(tme);
                        }
                    }
                    state=STATE_MOSTKNOWNTRACKS;
                    break;
                }
                case STATE_NAME: {
                    state=STATE_TRACK;
                    break;
                }
                case STATE_MBID: {
                    state=STATE_TRACK;
                    break;
                }
                case STATE_REACH: {
                    state=STATE_TRACK;
                    break;
                }
                case STATE_URL: {
                    state=STATE_TRACK;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_NAME:
                    data_track_name+=new String(ch,start,length);
                    break;
                case STATE_MBID:
                    data_track_mbid+=new String(ch,start,length);
                    break;
                case STATE_REACH:
                    data_track_reach+=new String(ch,start,length);
                    break;
                case STATE_URL:
                    data_track_url+=new String(ch,start,length);
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
