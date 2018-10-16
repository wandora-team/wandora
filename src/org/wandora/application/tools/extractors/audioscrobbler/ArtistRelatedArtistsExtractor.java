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
 * RelatedArtistsExtractor.java
 *
 * Created on 11. toukokuuta 2007, 18:15
 *
 */

package org.wandora.application.tools.extractors.audioscrobbler;



import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;



/**
 * Extractor reads specific XML feed from Audioscrobbler's web api and converts the
 * XML feed to a topic map. Extractor reads the Related Artists feed. Example
 * of Related Artists is found at
 *
 * http://ws.audioscrobbler.com/1.0/artist/Metallica/similar.xml
 *
 * Audioscrobber's web api documentation is found at
 *
 * http://www.audioscrobbler.net/data/webservices/
 *
 * @author akivela
 */
public class ArtistRelatedArtistsExtractor extends AbstractAudioScrobblerExtractor {
    

	
	private static final long serialVersionUID = 1L;


	/**
     * Creates a new instance of ArtistRelatedArtistsExtractor
     */
    public ArtistRelatedArtistsExtractor() {
    }
    
    
        
    @Override
    public String getName() {
        return "Audioscrobbler Artists: Related Artists extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads the Related Artist XML feed from Audioscrobbler's web api and converts the XML feed to a topic map. "+
               "See http://ws.audioscrobbler.com/1.0/artist/Metallica/similar.xml for an example of Related Artists XML feed.";
    }

    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        ArtistRelatedArtistsParser parserHandler = new ArtistRelatedArtistsParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " related artists found!");
        return true;
    }
    

    private static class ArtistRelatedArtistsParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public ArtistRelatedArtistsParser(TopicMap tm,ArtistRelatedArtistsExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress = 0;
        private TopicMap tm;
        private ArtistRelatedArtistsExtractor parent;
        
        public static final String TAG_SIMILARARTISTS="similarartists";
        public static final String TAG_ARTIST="artist";
        public static final String TAG_ARTIST_NAME="name";
        public static final String TAG_ARTIST_MBID="mbid";
        public static final String TAG_ARTIST_MATCH="match";
        public static final String TAG_ARTIST_URL="url";
        public static final String TAG_ARTIST_IMAGE_SMALL="image_small";
        public static final String TAG_ARTIST_IMAGE="image";
        public static final String TAG_ARTIST_STREAMABLE="streamable";
        
        private static final int STATE_START=0;
        private static final int STATE_SIMILARARTISTS=1;
        private static final int STATE_ARTIST=2;
        private static final int STATE_ARTIST_NAME=3;
        private static final int STATE_ARTIST_MBID=4;
        private static final int STATE_ARTIST_MATCH=5;
        private static final int STATE_ARTIST_URL=6;
        private static final int STATE_ARTIST_IMAGE_SMALL=7;
        private static final int STATE_ARTIST_IMAGE=8;
        private static final int STATE_ARTIST_STREAMABLE=9;
        
        private int state=STATE_START;
        

        
        private String data_artist = "";
        private String data_artist_name = "";
        private String data_artist_mbid = "";
        private String data_artist_match = "";
        private String data_artist_url = "";
        private String data_artist_image_small = "";
        private String data_artist_image = "";
        private String data_artist_streamable = "";
        
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
                    if(qName.equals(TAG_SIMILARARTISTS)) {
                        state = STATE_SIMILARARTISTS;
                        String theName = atts.getValue("artist");
                        String theMBID = atts.getValue("mbid");
                        String theStreamable = atts.getValue("streamable");
                        String thePicture = atts.getValue("picture");
                        if(theName != null) {
                            try {
                                Topic artistType = getArtistTypeTopic(tm);
                                theArtist=getArtistTopic(tm, theName, null, theMBID);
                                
                                if(theStreamable != null) {
                                    Topic streamableType = getOrCreateTopic(tm, STREAMABLE_SI,"Streamable");
                                    parent.setData(theArtist, streamableType, LANG, theStreamable);
                                    //theArtist.setData(streamableType, langIndep, theStreamable);
                                }
                                if(thePicture != null) {
                                    Topic imageType = getImageTypeTopic(tm);
                                    Topic image = getImageTopic(tm, thePicture, "artist "+theName);
                                    Association imagea = tm.createAssociation(imageType);
                                    imagea.addPlayer(image, imageType);
                                    imagea.addPlayer(theArtist, artistType);
                                }
                                theArtist.addType(artistType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                case STATE_SIMILARARTISTS:
                    if(qName.equals(TAG_ARTIST)) {
                        state = STATE_ARTIST;
                        data_artist_name = "";
                        data_artist_mbid = "";
                        data_artist_match = "";
                        data_artist_url = "";
                        data_artist_image_small = "";
                        data_artist_image = "";
                        data_artist_streamable = "";
                    }
                    break;
                case STATE_ARTIST:
                    if(qName.equals(TAG_ARTIST_NAME)) {
                        state = STATE_ARTIST_NAME;
                        data_artist_name = "";
                    }
                    else if(qName.equals(TAG_ARTIST_MBID)) {
                        state = STATE_ARTIST_MBID;
                        data_artist_mbid = "";
                    }
                    else if(qName.equals(TAG_ARTIST_MATCH)) {
                        state = STATE_ARTIST_MATCH;
                        data_artist_match = "";
                    }
                    else if(qName.equals(TAG_ARTIST_URL)) {
                        state = STATE_ARTIST_URL;
                        data_artist_url = "";
                    }
                    else if(qName.equals(TAG_ARTIST_IMAGE_SMALL)) {
                        state = STATE_ARTIST_IMAGE_SMALL;
                        data_artist_image_small = "";
                    }
                    else if(qName.equals(TAG_ARTIST_IMAGE)) {
                        state = STATE_ARTIST_IMAGE;
                        data_artist_image = "";
                    }
                    else if(qName.equals(TAG_ARTIST_STREAMABLE)) {
                        state = STATE_ARTIST_STREAMABLE;
                        data_artist_streamable = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_ARTIST: {
                    if(data_artist_mbid.length() > 0){
                        try {
                            Topic artistType=getArtistTypeTopic(tm);
                            Topic similarArtistType=getSimilarArtistsTypeTopic(tm);
                            
                            Topic artistTopic=getArtistTopic(tm, data_artist_name, data_artist_url, data_artist_mbid);
                            if(data_artist_streamable.length() > 0) {
                                Topic streamableType = getStreamableTypeTopic(tm);
                                parent.setData(artistTopic, streamableType, LANG, data_artist_streamable);
                            }
                            artistTopic.addType(artistType);
                            
                            if(theArtist != null) {
                                Association saa=tm.createAssociation(similarArtistType);
                                saa.addPlayer(theArtist, artistType);
                                saa.addPlayer(artistTopic, similarArtistType);
                                if(CONVERT_MATCH && data_artist_match.length() > 0) {
                                    Topic matchTopic = getMatchTopic(tm, data_artist_match);
                                    Topic matchType = getMatchTypeTopic(tm);
                                    saa.addPlayer(matchTopic, matchType);
                                }
                            }
                            if(data_artist_image.length() > 0) {
                                Topic image = getImageTopic(tm, data_artist_image, "artist "+data_artist_name );
                                Topic imageType = getImageTypeTopic(tm);
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
                    state=STATE_SIMILARARTISTS;
                    break;
                }
                case STATE_ARTIST_NAME: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_MBID: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_MATCH: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_URL: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_IMAGE_SMALL: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_IMAGE: {
                    state=STATE_ARTIST;
                    break;
                }
                case STATE_ARTIST_STREAMABLE: {
                    state=STATE_ARTIST;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_ARTIST_NAME:
                    data_artist_name+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_MBID:
                    data_artist_mbid+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_MATCH:
                    data_artist_match+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_URL:
                    data_artist_url+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_IMAGE_SMALL:
                    data_artist_image_small+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_IMAGE:
                    data_artist_image+=new String(ch,start,length);
                    break;
                case STATE_ARTIST_STREAMABLE:
                    data_artist_streamable+=new String(ch,start,length);
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
