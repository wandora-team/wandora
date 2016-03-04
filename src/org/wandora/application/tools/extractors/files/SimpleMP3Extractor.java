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
 * SimpleMP3Extractor.java
 *
 * Created on October 1, 2004, 3:46 PM
 */

package org.wandora.application.tools.extractors.files;



import org.farng.mp3.*;
import org.farng.mp3.id3.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import org.wandora.application.tools.extractors.AbstractExtractor;




/**
 *
 * @author  akivela
 */

public class SimpleMP3Extractor extends AbstractExtractor implements WandoraTool {
    private String tempFilename = "temp/wandora_temp.tmp";

    private String baseLocator = "http://wandora.org/si/mp3";

    private int extractionCounter = 0;
    private int foundCounter = 0;

    
   
    @Override
    public String getName() {
        return "Extract MP3 metadata...";
    }
    
    
    @Override
    public String getDescription(){
        return "Extracts metadata from MP3 files. Both ID3V1 and ID3V2 metadata tags are supported!";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf1c7);
    }
    
    
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }
    
    
    
    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select MP3 file(s) or directories!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking MP3 files with ID3V1 or ID3V2 metadata!";
        
            case FILE_PATTERN: return ".*\\.(mp3|MP3|Mp3|MPEG3|mpeg3)";
            
            case DONE_FAILED: return "Ready. No extractions! %1 mp3 file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 mp3 file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 mp3 files crawled!";
            
            case LOG_TITLE: return "MP3 ID3V[12] Extraction Log";
        }
        return "";
    }
    
        
    @Override
    public synchronized void extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        try {
            foundCounter++;
            setProgress(foundCounter);
            String urlString = url.toExternalForm();
            log("Extracting from url '" + croppedUrlString(urlString)+"'");
            TopicMap tempMap = new org.wandora.topicmap.memory.TopicMapImpl();
            URLConnection uc = null;
            if(getWandora() != null) {
                uc = getWandora().wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            Object content = uc.getContent();
            IObox.saveBFile(tempFilename, (InputStream) content);
            File tempFile = new File(tempFilename);
            if(_extractTopicsFrom(tempFile, tempMap)) {
                Topic t = tempMap.getTopicBySubjectLocator(buildSL(tempFile));
                t.setSubjectLocator(new Locator(urlString));
                Locator l = t.getOneSubjectIdentifier();
                t.addSubjectIdentifier(new Locator(urlString));
                t.removeSubjectIdentifier(l);
                topicMap.mergeIn(tempMap);
                extractionCounter++;
            }
            else {
                log("Found no valid metadata in '"+croppedUrlString(urlString)+"'");
            }
        }
        catch (Exception e) {
            log("Exception occurred '" + e.toString()+"'", e);
        }
        log("Wait while extracting metadata out of MP3 files!");
    }



    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        throw(new Exception(STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE));
    }
    
    public boolean _extractTopicsFrom(URL file, TopicMap topicMap) throws Exception {
        return false;
    }

    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null || file.isDirectory()) return false;
        try {
            MP3File mp3 = new MP3File(file);

            if(mp3 != null) {

                String length = null;
                String bpm = null;
                String lan = null;
                String media = null;
                String frequency = null;
                String bitrate = null;


                String album = null;
                String artist = null;
                String genre = null;
                String title = null;
                String identifier = null;
                String year = null;


                if(mp3.hasID3v2Tag()) {
                    AbstractID3v2 id = mp3.getID3v2Tag();

                    AbstractID3v2Frame frame = id.getFrame("TALB");
                    if(frame != null) {
                        FrameBodyTALB body = (FrameBodyTALB) frame.getBody();
                        album = body.getText();
                    }

                    frame = id.getFrame("TPE1");
                    if(frame != null) {
                        FrameBodyTPE1 body = (FrameBodyTPE1) frame.getBody();
                        artist = body.getText();
                    }

                    frame = id.getFrame("TCON");
                    if(frame != null) {
                        FrameBodyTCON body = (FrameBodyTCON) frame.getBody();
                        genre = body.getText();
                    }

                    frame = id.getFrame("TIT2");
                    if(frame != null) {
                        FrameBodyTIT2 body = (FrameBodyTIT2) frame.getBody();
                        title = body.getText();
                    }

                    frame = id.getFrame("TDOR");
                    if(frame != null) {
                        FrameBodyTDOR body = (FrameBodyTDOR) frame.getBody();
                        year = body.getText();
                    }

                    frame = id.getFrame("TLEN");
                    if(frame != null) {
                        FrameBodyTLEN body = (FrameBodyTLEN) frame.getBody();
                        length = body.getText();
                    }

                    frame = id.getFrame("TBPM");
                    if(frame != null) {
                        FrameBodyTBPM body = (FrameBodyTBPM) frame.getBody();
                        bpm = body.getText();
                    }

                    frame = id.getFrame("TLAN");
                    if(frame != null) {
                        FrameBodyTLAN body = (FrameBodyTLAN) frame.getBody();
                        lan = body.getText();
                    }

                    frame = id.getFrame("TMED");
                    if(frame != null) {
                        FrameBodyTMED body = (FrameBodyTMED) frame.getBody();
                        media = body.getText();
                    }

                }
                else if(mp3.hasID3v1Tag()) {
                    ID3v1 id = mp3.getID3v1Tag();
                    album = id.getAlbum();
                    artist = id.getArtist();
                    genre = GenreSolver.get(id.getGenre());
                    title = id.getTitle();
                    identifier = id.getIdentifier();
                    year = id.getYear();
                }

                if(mp3.hasID3v1Tag() || mp3.hasID3v2Tag()) {
                    Topic wandoraClass = topicMap.createTopic();
                    wandoraClass.addSubjectIdentifier(new Locator(TMBox.WANDORACLASS_SI));
                    wandoraClass.setBaseName("Wandora class");

                    Topic lengthType = createTopic(topicMap, "length");
                    Topic frequencyType = createTopic(topicMap, "frequency");
                    Topic bitrateType = createTopic(topicMap, "bitrate");
                    Topic isVariableBitRateType = createTopic(topicMap, "isVariableBitRate");
                    Topic isCopyProtectedType = createTopic(topicMap, "isCopyProtected");

                    Topic containsType = createTopic(topicMap, "contains");
                    Topic hasAlbumType = createTopic(topicMap, "hasAlbum");
                    Topic hasTitleType = createTopic(topicMap, "hasTitle");
                    Topic isGenreType = createTopic(topicMap, "isGenre");
                    Topic timeType = createTopic(topicMap, "timeApellation");
                    Topic identifiesType = createTopic(topicMap, "identifies");

                    Topic mp3Type = createTopic(topicMap, "MP3", wandoraClass);
                    Topic albumType = createTopic(topicMap, "album", mp3Type);
                    Topic artistType = createTopic(topicMap, "artist", mp3Type);
                    Topic genreType = createTopic(topicMap, "genre", mp3Type);
                    Topic titleType = createTopic(topicMap, "title", mp3Type);
                    Topic yearType = createTopic(topicMap, "year", mp3Type);
                    Topic identifierType = createTopic(topicMap, "identifier", mp3Type);

                    Topic albumT = null;
                    Topic artistT = null;
                    Topic genreT = null;
                    Topic yearT = null;
                    Topic identifierT = null;
                    Topic titleT = null;

                    if(title != null && title.length()>0) titleT = createTopic(topicMap, "title/" + title, " (title)", title, titleType);

                    if(album != null && album.length()>0) albumT = createTopic(topicMap, "album/" + album, " (album)", album, albumType);
                    if(artist != null && artist.length()>0) artistT = createTopic(topicMap, "artist/" + artist, " (artist)", artist, artistType);
                    if(genre != null) genreT = createTopic(topicMap, "genre/" + genre, " (genre)", "" + genre, genreType);
                    if(year != null && year.length()>0) yearT = createTopic(topicMap, "year/" + year, " (year)", year, yearType);
                    if(identifier != null && identifier.length()>0) identifierT = createTopic(topicMap, "identifier/" + identifier, " (identifier)", identifier, identifierType);

                    if(titleT != null && albumT != null) createAssociation(topicMap, containsType, new Topic[] { titleT, albumT } );
                    if(artistT != null && albumT != null) createAssociation(topicMap, hasAlbumType, new Topic[] { artistT, albumT } );
                    if(artistT != null && titleT != null) createAssociation(topicMap, hasTitleType, new Topic[] { artistT, titleT } );
                    if(titleT != null && genreT != null) createAssociation(topicMap, isGenreType, new Topic[] { titleT, genreT } );
                    if(titleT != null && identifierT != null) createAssociation(topicMap, identifiesType, new Topic[] { titleT, identifierT } );
                    if(titleT != null && yearT != null) createAssociation(topicMap, timeType, new Topic[] { titleT, yearT } );

                    if(titleT != null) {
                        titleT.setSubjectLocator(buildSL(mp3.getMp3file()));

                        Topic lanT = topicMap.getTopic(TMBox.LANGINDEPENDENT_SI);
                        if(lanT == null) {
                            lanT = topicMap.createTopic();
                            lanT.addSubjectIdentifier(new Locator(TMBox.LANGINDEPENDENT_SI));
                            lanT.setBaseName("Language independent");
                        }

                        Hashtable hash = null;
                        if(frequency != null) {
                            hash = new Hashtable();
                            hash.put(lanT, frequency);
                            titleT.setData(frequencyType,hash);
                        }

                        if(bitrate != null) {
                            hash = new Hashtable();
                            hash.put(lanT, bitrate);
                            titleT.setData(bitrateType,hash);
                        }

                        if(length != null) {
                            hash = new Hashtable();
                            hash.put(lanT, length);
                            titleT.setData(lengthType,hash);
                        }
                    }
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        catch (Exception e) {
            log("Exception occurred while extracting from mp3 file.", e);
        }
        return false;
    }


    
    
    @Override
    public Locator buildSI(String siend) {
        if(!baseLocator.endsWith("/")) baseLocator = baseLocator + "/";
        return new Locator(TopicTools.cleanDirtyLocator(baseLocator + siend));
    }

    

    private final String[] contentTypes=new String[] { "audio/mp3", "audio/mpeg", "audio/x-mp3", "audio/x-mpeg", "audio/m3u", "audio/x-m3u" };



    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

 
    
    
    
    
    // -------------------------------------------------------------------------
    // --- GENRE SOLVER --------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    public static class GenreSolver {

        private static String[][] genreCodes = {
            { "0", "Blues" },
            { "1", "Classic Rock" },
            { "2", "Country" },
            { "3", "Dance" },
            { "4", "Disco" },
            { "5", "Funk" },
            { "6", "Grunge" },
            { "7", "Hip-Hop" },
            { "8", "Jazz" },
            { "9", "Metal" },
            { "10", "New Age" },
            { "11", "Oldies" },
            { "12", "Other" },
            { "13", "Pop" },
            { "14", "R&B" },
            { "15", "Rap" },
            { "16", "Reggae" },
            { "17", "Rock" },
            { "18", "Techno" },
            { "19", "Industrial" },
            { "20", "Alternative" },
            { "21", "Ska" },
            { "22", "Death Metal" },
            { "23", "Pranks" },
            { "24", "Soundtrack" },
            { "25", "Euro-Techno" },
            { "26", "Ambient" },
            { "27", "Trip-Hop" },
            { "28", "Vocal" },
            { "29", "Jazz+Funk" },
            { "30", "Fusion" },
            { "31", "Trance" },
            { "32", "Classical" },
            { "33", "Instrumental" },
            { "34", "Acid" },
            { "35", "House" },
            { "36", "Game" },
            { "37", "Sound Clip" },
            { "38", "Gospel" },
            { "39", "Noise" },
            { "40", "AlternRock" },
            { "41", "Bass" },
            { "42", "Soul" },
            { "43", "Punk" },
            { "44", "Space" },
            { "45", "Meditative" },
            { "46", "Instrumental Pop" },
            { "47", "Instrumental Rock" },
            { "48", "Ethnic" },
            { "49", "Gothic" },
            { "50", "Darkwave" },
            { "51", "Techno-Industrial" },
            { "52", "Electronic" },
            { "53", "Pop-Folk" },
            { "54", "Eurodance" },
            { "55", "Dream" },
            { "56", "Southern Rock" },
            { "57", "Comedy" },
            { "58", "Cult" },
            { "59", "Gangsta" },
            { "60", "Top 40" },
            { "61", "Christian Rap" },
            { "62", "Pop/Funk" },
            { "63", "Jungle" },
            { "64", "Native American" },
            { "65", "Cabaret" },
            { "66", "New Wave" },
            { "67", "Psychadelic" },
            { "68", "Rave" },
            { "69", "Showtunes" },
            { "70", "Trailer" },
            { "71", "Lo-Fi" },
            { "72", "Tribal" },
            { "73", "Acid Punk" },
            { "74", "Acid Jazz" },
            { "75", "Polka" },
            { "76", "Retro" },
            { "77", "Musical" },
            { "78", "Rock & Roll" },
            { "79", "Hard Rock" },
            { "80", "Folk" },
            { "81", "Folk-Rock" },
            { "82", "National Folk" },
            { "83", "Swing" },
            { "84", "Fast Fusion" },
            { "85", "Bebob" },
            { "86", "Latin" },
            { "87", "Revival" },
            { "88", "Celtic" },
            { "89", "Bluegrass" },
            { "90", "Avantgarde" },
            { "91", "Gothic Rock" },
            { "92", "Progressive Rock" },
            { "93", "Psychedelic Rock" },
            { "94", "Symphonic Rock" },
            { "95", "Slow Rock" },
            { "96", "Big Band" },
            { "97", "Chorus" },
            { "98", "Easy Listening" },
            { "99", "Acoustic" },
            { "100", "Humour" },
            { "101", "Speech" },
            { "102", "Chanson" },
            { "103", "Opera" },
            { "104", "Chamber Music" },
            { "105", "Sonata" },
            { "106", "Symphony" },
            { "107", "Booty Bass" },
            { "108", "Primus" },
            { "109", "Porn Groove" },
            { "110", "Satire" },
            { "111", "Slow Jam" },
            { "112", "Club" },
            { "113", "Tango" },
            { "114", "Samba" },
            { "115", "Folklore" },
            { "116", "Ballad" },
            { "117", "Power Ballad" },
            { "118", "Rhythmic Soul" },
            { "119", "Freestyle" },
            { "120", "Duet" },
            { "121", "Punk Rock" },
            { "122", "Drum Solo" },
            { "123", "A capella" },
            { "124", "Euro-House" },
            { "125", "Dance Hall" },
        };
        
        
        public static String get(int genreCode) {
            if(genreCode > -1) {
                for(int i=0; i<genreCodes.length; i++) {
                    if(genreCodes[i][0].equals("" + genreCode)) {
                        return genreCodes[i][1];
                    }
                }
            }
            return "" + genreCode;
        }
    
    }
    
    
    
   
}
    
   