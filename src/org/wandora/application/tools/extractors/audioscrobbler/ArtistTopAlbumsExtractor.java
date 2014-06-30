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
 * TopAlbumsExtractor.java
 *
 * Created on 13. toukokuuta 2007, 16:44
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
 * Extractor reads specific XML feed from Audioscrobbler's web api and converts the
 * XML feed to a topic map. Extractor reads the Top Albums feed. Example
 * of Top Albums is found at
 *
 * http://ws.audioscrobbler.com/1.0/artist/Metallica/topalbums.xml
 *
 * Audioscrobber's web api documentation is found at
 *
 * http://www.audioscrobbler.net/data/webservices/
 *
 * @author akivela
 */
public class ArtistTopAlbumsExtractor extends AbstractAudioScrobblerExtractor {
    
    /** Creates a new instance of ArtistTopAlbumsExtractor */
    public ArtistTopAlbumsExtractor() {
    }
     
    @Override
    public String getName() {
        return "Audioscrobbler Artists: Top Albums extractor";
    }


    @Override
    public String getDescription(){
        return "Extractor reads the Top Albums XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/artist/Metallica/topalbums.xml for an example of Top Albums XML feed.";
    }

    
    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        ArtistTopAlbumsParser parserHandler = new ArtistTopAlbumsParser(topicMap,this);
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
    

    
    
    
    private static class ArtistTopAlbumsParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public ArtistTopAlbumsParser(TopicMap tm,ArtistTopAlbumsExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        private TopicMap tm;
        private ArtistTopAlbumsExtractor parent;
        
        public static final String TAG_TOPALBUMS="topalbums";
        public static final String TAG_ALBUM="album";
        public static final String TAG_NAME="name";
        public static final String TAG_MBID="mbid";
        public static final String TAG_REACH="reach";
        public static final String TAG_URL="url";
        public static final String TAG_IMAGE="image";
        public static final String TAG_IMAGE_LARGE="large";
        public static final String TAG_IMAGE_MEDIUM="medium";
        public static final String TAG_IMAGE_SMALL="small";
       
        private static final int STATE_START=0;
        private static final int STATE_TOPALBUMS=1;
        private static final int STATE_ALBUM=2;
        private static final int STATE_NAME=3;
        private static final int STATE_MBID=4;
        private static final int STATE_REACH=5;
        private static final int STATE_URL=6;
        private static final int STATE_IMAGE=7;
        private static final int STATE_IMAGE_LARGE=8;
        private static final int STATE_IMAGE_MEDIUM=9;
        private static final int STATE_IMAGE_SMALL=10;
               
        private int state=STATE_START;
                
        private String data_album_name = "";
        private String data_album_mbid = "";
        private String data_album_reach = "";
        private String data_album_url = "";
        private String data_album_image_large = "";
        private String data_album_image_medium = "";
        private String data_album_image_small = "";
        
        private Topic theArtist;
        private String theArtistString = "";
        
        
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
                    if(qName.equals(TAG_TOPALBUMS)) {
                        state = STATE_TOPALBUMS;
                        theArtistString = atts.getValue("artist");
                        if(theArtistString != null) {
                            try {
                                theArtist = getArtistTopic(tm, theArtistString);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                case STATE_TOPALBUMS:
                    if(qName.equals(TAG_ALBUM)) {
                        state = STATE_ALBUM;
                        data_album_name = "";
                        data_album_mbid = "";
                        data_album_reach = "";
                        data_album_url = "";
                        data_album_image_large = "";
                        data_album_image_medium = "";
                        data_album_image_small = "";
                    }
                    break;
                case STATE_ALBUM:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_NAME;
                        data_album_name = "";
                    }
                    else if(qName.equals(TAG_MBID)) {
                        state = STATE_MBID;
                        data_album_mbid = "";
                    }
                    else if(qName.equals(TAG_REACH)) {
                        state = STATE_REACH;
                        data_album_reach = "";
                    }
                    else if(qName.equals(TAG_URL)) {
                        state = STATE_URL;
                        data_album_url = "";
                    }
                    else if(qName.equals(TAG_IMAGE)) {
                        state = STATE_IMAGE;
                        data_album_image_large = "";
                        data_album_image_medium = "";
                        data_album_image_small = "";
                    }
                    break;
               case STATE_IMAGE:
                   if(qName.equals(TAG_IMAGE_LARGE)) {
                        state = STATE_IMAGE_LARGE;
                        data_album_image_large = "";
                   }
                   else if(qName.equals(TAG_IMAGE_MEDIUM)) {
                        state = STATE_IMAGE_MEDIUM;
                        data_album_image_medium = "";
                   }
                   else if(qName.equals(TAG_IMAGE_SMALL)) {
                        state = STATE_IMAGE_SMALL;
                        data_album_image_small = "";
                   }
                   break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_ALBUM: {
                    if(data_album_name.length() > 0) {
                        try {
                            Topic albumType=getAlbumTypeTopic(tm);
                            Topic albumTopic=getAlbumTopic(tm, data_album_name, data_album_url, data_album_mbid, theArtistString);
                            Topic artistType = getArtistTypeTopic(tm);
                            Association ta=tm.createAssociation(albumType);
                            ta.addPlayer(theArtist, artistType);
                            ta.addPlayer(albumTopic, albumType);
                            parent.setProgress(++progress);                            
                            if(CONVERT_REACH && data_album_reach.length() > 0) {
                                try {
                                    Topic reachTopic = getReachTopic(tm, data_album_reach);
                                    Topic reachType = getReachTypeTopic(tm);
                                    ta.addPlayer(reachTopic, reachType);
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                            
                            String data_album_image = "";
                            if(data_album_image_large.length() > 0) {
                                data_album_image = data_album_image_large;
                            }
                            else if(data_album_image_medium.length() > 0) {
                                data_album_image = data_album_image_medium;
                            }
                            else if(data_album_image_small.length() > 0) {
                                data_album_image = data_album_image_small;
                            }
                            if(data_album_image.length() > 0) {
                                try {
                                    Topic image = getImageTopic(tm, data_album_image, "album "+data_album_name);
                                    Topic imageType = getImageTypeTopic(tm);
                                    Association imagea = tm.createAssociation(imageType);
                                    imagea.addPlayer(image, imageType);
                                    imagea.addPlayer(albumTopic, albumType);
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                        }
                        catch(TopicMapException tme){
                            parent.log(tme);
                        }
                    }
                    state=STATE_TOPALBUMS;
                    break;
                }
                case STATE_NAME: {
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_MBID: {
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_REACH: {
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_URL: {
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_IMAGE: {
                    state=STATE_ALBUM;
                    break;
                }
                case STATE_IMAGE_LARGE: {
                    state=STATE_IMAGE;
                    break;
                }
                case STATE_IMAGE_MEDIUM: {
                    state=STATE_IMAGE;
                    break;
                }
                case STATE_IMAGE_SMALL: {
                    state=STATE_IMAGE;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_NAME:
                    data_album_name+=new String(ch,start,length);
                    break;
                case STATE_MBID:
                    data_album_mbid+=new String(ch,start,length);
                    break;
                case STATE_REACH:
                    data_album_reach+=new String(ch,start,length);
                    break;
                case STATE_URL:
                    data_album_url+=new String(ch,start,length);
                    break;
                case STATE_IMAGE_LARGE:
                    data_album_image_large+=new String(ch,start,length);
                    break;
                case STATE_IMAGE_MEDIUM:
                    data_album_image_medium+=new String(ch,start,length);
                    break;
                case STATE_IMAGE_SMALL:
                    data_album_image_small+=new String(ch,start,length);
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
