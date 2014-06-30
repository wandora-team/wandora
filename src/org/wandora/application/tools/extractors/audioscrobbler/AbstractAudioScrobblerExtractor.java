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
 * AbstractAudioScrobblerExtractor.java
 * 
 */


package org.wandora.application.tools.extractors.audioscrobbler;

import java.net.*;
import java.io.*;
import javax.swing.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;




/**
 *
 * @author akivela
 */
public abstract class AbstractAudioScrobblerExtractor extends AbstractExtractor {
    
    // Default language of occurrences and variant names.
    public static String LANG = "en";
    
    
    public static boolean CONVERT_COUNTS = false;
    
    // 
    public static boolean CONVERT_REACH = false;
    
    // Add match strength as a third player to the association between similar artists.
    public static boolean CONVERT_MATCH = false;
    
    // Add track index as a third player to the association between album and track.
    public static boolean CONVERT_TRACK_INDEX = true;
    
    
    // Convert tag usage counts as tag topic occurrences or topics associated to tag topics.
    public static boolean OCCURRENCE_COUNTS = true;
    
    
    /**
     * Try to retrieve topic before new is created. Setting this true may speed
     * the extraction but extraction may loose some data as topic is created only once.
     */
    public static boolean USE_EXISTING_TOPICS = true;
    
    
    
    

    public static final String SIPREFIX="http://www.last.fm/";
    public static final String MUSIC_SI=SIPREFIX+"music";

    public static final String ARTIST_SI=SIPREFIX+"artist";

    public static final String SIMILAR_ARTIST_SI=ARTIST_SI+"/similar";
    public static final String MATCH_SI=ARTIST_SI+"/match";
    public static final String IMAGE_SI=SIPREFIX+"image";
    public static final String MBID_SI=SIPREFIX+"mbid";
    public static final String STREAMABLE_SI=SIPREFIX+"streamable";
    
    
    public static final String ALBUM_SI=SIPREFIX+"album";
    public static final String TRACK_SI=SIPREFIX+"track";
    public static final String INDEX_SI=SIPREFIX+"index";
    public static final String REACH_SI=SIPREFIX+"reach";
    public static final String RELEASEDATE_SI=ALBUM_SI+"/releasedate";
    
    public static final String TAG_SI=SIPREFIX+"tag";
    public static final String COUNT_SI=SIPREFIX+"count";

    

    
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_lastfm.png");
    }
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    
    private final String[] contentTypes=new String[] { "text/xml", "application/xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }



    public abstract boolean _extractTopicsFrom(InputStream inputStream, TopicMap topicMap) throws Exception;
    
    
    
    // ******** TOPIC MAPS *********
    
    
    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }



    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    
    
    // ******** TYPE TOPICS **********
    
    
    protected static Topic getArtistTypeTopic(TopicMap tm) throws TopicMapException {
        Topic artistType = getOrCreateTopic(tm, ARTIST_SI, "last.fm artist");
        Topic scClass = getScrobblerClassTopic(tm);
        makeSubclassOf(tm, artistType, scClass);
        return artistType;
    }

    protected static Topic getImageTypeTopic(TopicMap tm) throws TopicMapException {
        Topic imageType = getOrCreateTopic(tm, IMAGE_SI, "last.fm image");
        Topic scClass = getScrobblerClassTopic(tm);
        makeSubclassOf(tm, imageType, scClass);
        return imageType;
    }
    
    protected static Topic getAlbumTypeTopic(TopicMap tm) throws TopicMapException {
        Topic albumType = getOrCreateTopic(tm, ALBUM_SI, "last.fm album");
        Topic scClass = getScrobblerClassTopic(tm);
        makeSubclassOf(tm, albumType, scClass);
        return albumType;
    }
        
    protected static Topic getTrackTypeTopic(TopicMap tm) throws TopicMapException {
        Topic trackType=getOrCreateTopic(tm, TRACK_SI, "last.fm track");
        Topic scClass = getScrobblerClassTopic(tm);
        makeSubclassOf(tm, trackType, scClass);
        return trackType;
    }
    
    protected static Topic getTagTypeTopic(TopicMap tm) throws TopicMapException {
        Topic tagType=getOrCreateTopic(tm, TAG_SI, "last.fm tag");
        Topic scClass = getScrobblerClassTopic(tm);
        makeSubclassOf(tm, tagType, scClass);
        return tagType;
    }
    
    protected static Topic getIndexTypeTopic(TopicMap tm) throws TopicMapException {
        Topic indexType=getOrCreateTopic(tm, INDEX_SI, "last.fm index");
        return indexType;
    }
    protected static Topic getReachTypeTopic(TopicMap tm) throws TopicMapException {
        Topic indexType=getOrCreateTopic(tm, REACH_SI, "last.fm reach");
        return indexType;
    }
    protected static Topic getMatchTypeTopic(TopicMap tm) throws TopicMapException {
        Topic matchType=getOrCreateTopic(tm, MATCH_SI, "last.fm match");
        return matchType;
    }
    
    protected static Topic getCountTypeTopic(TopicMap tm) throws TopicMapException {
        Topic countType=getOrCreateTopic(tm, COUNT_SI, "last.fm count");
        return countType;
    }
    
    protected static Topic getStreamableTypeTopic(TopicMap tm) throws TopicMapException {
        Topic countType=getOrCreateTopic(tm, STREAMABLE_SI, "last.fm streamable");
        return countType;
    }
    
    protected static Topic getSimilarArtistsTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SIMILAR_ARTIST_SI, "last.fm similar artists");
        return type;
    }
    
    protected static Topic getScrobblerClassTopic(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, "http://www.last.fm", "Last.fm");
        t.addType(getWandoraClassTopic(tm));
        return t;
    }


    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    protected static Topic getMBIDTypeTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, MBID_SI, "MBID");
    }

    protected static Topic getDefaultLangTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, "http://www.topicmaps.org/xtm/1.0/language.xtm#en", "Language EN");
    }
    
    // *********** LASTFM TOPICS ***********
    
    
    
    
    protected static Topic getArtistTopic(TopicMap tm, String theArtistString) throws TopicMapException {
        String artistSI = MUSIC_SI + "/" + urlEncode(theArtistString);
        Topic theArtist = null;
        if(USE_EXISTING_TOPICS) theArtist = tm.getTopic(artistSI);
        if(theArtist == null) {
            Topic artistType = getArtistTypeTopic(tm);
            theArtist=tm.createTopic();
            theArtist.addSubjectIdentifier(tm.createLocator(artistSI));
            theArtist.setBaseName(theArtistString + " (last.fm artist)");
            theArtist.setDisplayName(LANG, theArtistString);
            theArtist.addType(artistType);
        }
        return theArtist;
    }
    
    
    
    protected static Topic getArtistTopic(TopicMap tm, String theArtistString, String artistUrl, String artistMBID) throws TopicMapException {
        Topic artistTopic = getArtistTopic(tm, theArtistString);
        if(artistUrl != null && artistUrl.length() > 0) {
            artistTopic.addSubjectIdentifier(tm.createLocator(artistUrl));
        }
        if(artistMBID != null && artistMBID.length() > 0) {
            artistTopic.setData(getMBIDTypeTopic(tm), getDefaultLangTopic(tm), artistMBID);
        }
        return artistTopic;
    }
    
    
    protected static Topic getAlbumTopic(TopicMap tm, String theAlbumString, String theArtistString) throws TopicMapException {
        String albumSI = MUSIC_SI + "/" + urlEncode(theArtistString) + "/" + urlEncode(theAlbumString);
        Topic theAlbum = null;
        if(USE_EXISTING_TOPICS) theAlbum = tm.getTopic(albumSI);
        if(theAlbum == null) {
            Topic albumType = getAlbumTypeTopic(tm);
            theAlbum=tm.createTopic();
            theAlbum.addSubjectIdentifier(tm.createLocator(albumSI));
            theAlbum.setBaseName(theAlbumString + " (last.fm album)");
            theAlbum.setDisplayName(LANG, theAlbumString);
            theAlbum.addType(albumType);
        }
        return theAlbum;
    }
    
    protected static Topic getAlbumTopic(TopicMap tm, String theAlbumString, String theAlbumUrl, String theArtistString) throws TopicMapException {
        Topic theAlbum = getAlbumTopic(tm, theAlbumString, theArtistString);
        org.wandora.topicmap.Locator si = theAlbum.getOneSubjectIdentifier();
        if(theAlbumUrl != null && theAlbumUrl.length() > 0) {
            if(!theAlbumUrl.equals(si.toExternalForm())) {
                theAlbum.addSubjectIdentifier(tm.createLocator(theAlbumUrl));
                // REMOVE DEFAULT SI
                theAlbum.removeSubjectIdentifier(si);
            }
        }
        return theAlbum;
    }

    protected static Topic getAlbumTopic(TopicMap tm, String theAlbumString, String theAlbumUrl, String theAlbumMBID, String theArtistString) throws TopicMapException {
        Topic theAlbum = getAlbumTopic(tm, theAlbumString, theAlbumUrl, theArtistString);
        if(theAlbumMBID != null && theAlbumMBID.length() > 0) {
            theAlbum.setData(getMBIDTypeTopic(tm), getDefaultLangTopic(tm), theAlbumMBID);
        }
        return theAlbum;
    }

                            
    
    protected static Topic getTrackTopic(TopicMap tm, String theTrackString, String albumString, String artistString) throws TopicMapException {
        String trackSI = MUSIC_SI + "/" + urlEncode(artistString) + "/" + urlEncode(albumString) + "/" + urlEncode(theTrackString);
        Topic theTrack = null;
        if(USE_EXISTING_TOPICS) theTrack = tm.getTopic(trackSI);
        if(theTrack == null) { 
            Topic trackType=getTrackTypeTopic(tm);
            theTrack=tm.createTopic();
            theTrack.addSubjectIdentifier(tm.createLocator(trackSI));
            theTrack.setBaseName(theTrackString+" (last.fm track)");
            theTrack.setDisplayName(LANG, theTrackString);
            theTrack.addType(trackType);
        }
        return theTrack;
    }
        
        
    
    
    
    protected static Topic getTrackTopic(TopicMap tm, String theTrackString, String theTrackUrl, String albumString, String artistString) throws TopicMapException {
        Topic theTrack = getTrackTopic(tm, theTrackString, albumString, artistString);
        if(theTrackUrl != null && theTrackUrl.length() > 0) {
            theTrack.addSubjectIdentifier(tm.createLocator(theTrackUrl));
        }
        return theTrack;
    }
    
    
    
    
    protected static Topic getImageTopic(TopicMap tm, String imageUrl, String owner) throws TopicMapException {
        Topic image = null;
        if(USE_EXISTING_TOPICS) image = tm.getTopic(imageUrl);
        if(image == null) {
            image = tm.createTopic();
            image.addSubjectIdentifier(tm.createLocator(imageUrl));
            image.setSubjectLocator(tm.createLocator(imageUrl));
            image.setBaseName("Image of " + owner);
            Topic imageType = getImageTypeTopic(tm);
            image.addType(imageType);
        }
        return image;
    }
    
    protected static Topic getTagTopic(TopicMap tm, String tag) throws TopicMapException {
        String tagSI = TAG_SI + "/" + urlEncode(tag);
        Topic tagTopic = null;
        if(USE_EXISTING_TOPICS) tagTopic = tm.getTopic(tagSI);
        if(tagTopic == null) {
            tagTopic = tm.createTopic();
            tagTopic.addSubjectIdentifier(tm.createLocator(tagSI));
            tagTopic.setBaseName(tag+" (last.fm tag)");
            tagTopic.setDisplayName(LANG, tag);
            Topic tagType = getTagTypeTopic(tm);
            tagTopic.addType(tagType);
        }
        return tagTopic;
    }
    
    
    
    protected static Topic getTagTopic(TopicMap tm, String tag, String tagUrl) throws TopicMapException {
        Topic tagTopic = getTagTopic(tm, tag);
        if(tagUrl != null && tagUrl.length() > 0) tagTopic.addSubjectIdentifier(tm.createLocator(tagUrl));
        return tagTopic;
    }
    
    
    
    protected static Topic getIndexTopic(TopicMap tm, int index) throws TopicMapException {
        String indexSI = INDEX_SI + "/" + index;
        Topic indexTopic = null;
        if(USE_EXISTING_TOPICS) indexTopic = tm.getTopic(indexSI);
        if(indexTopic == null) {
            indexTopic = tm.createTopic();
            indexTopic.addSubjectIdentifier(tm.createLocator(indexSI));
            indexTopic.setBaseName(index+" (last.fm)");
            indexTopic.setDisplayName(LANG, index+"");
        }
        return indexTopic;
    }
    
    protected static Topic getReachTopic(TopicMap tm, String r) throws TopicMapException {
        String reachSI = REACH_SI + "/" + urlEncode(r);
        Topic reachTopic = null;
        if(USE_EXISTING_TOPICS) reachTopic = tm.getTopic(reachSI);
        if(reachTopic == null) {
            reachTopic = tm.createTopic();
            reachTopic.addSubjectIdentifier(tm.createLocator(reachSI));
            reachTopic.setBaseName(r+" (last.fm)");
            reachTopic.setDisplayName(LANG, r);
            reachTopic.addType(getReachTypeTopic(tm));
        }
        return reachTopic;
    }
    
    
    
    protected static Topic getMatchTopic(TopicMap tm, String m) throws TopicMapException {
        String matchSI = MATCH_SI + "/" + urlEncode(m);
        Topic matchTopic = null;
        if(USE_EXISTING_TOPICS) matchTopic = tm.getTopic(matchSI);
        if(matchTopic == null) {
            matchTopic = tm.createTopic();
            matchTopic.addSubjectIdentifier(tm.createLocator(matchSI));
            matchTopic.setBaseName(m+" (last.fm)");
            matchTopic.setDisplayName(LANG, m);
            Topic matchType = getMatchTypeTopic(tm);
            matchTopic.addType(matchType);
        }
        return matchTopic;
    }
    
    
    protected static Topic getCountTopic(TopicMap tm, String c) throws TopicMapException {
        String countSI =COUNT_SI + "/" + urlEncode(c);
        Topic countTopic = null;
        if(USE_EXISTING_TOPICS) countTopic = tm.getTopic(countSI);
        if(countTopic == null) {
            countTopic = tm.createTopic();
            countTopic.addSubjectIdentifier(tm.createLocator(countSI));
            countTopic.setBaseName(c+" (last.fm)");
            countTopic.setDisplayName(LANG, c);
            Topic countType = getCountTypeTopic(tm);
            countTopic.addType(countType);
        }
        return countTopic;
    }

}
