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
 * FreeDBExtractor.java
 *
 * Created on 5. maaliskuuta 2007, 12:39
 *
 */

package org.wandora.application.tools.extractors;


import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.*;
import org.wandora.utils.*;


import java.util.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.*;



/**
 * 

 *
 *
 * @author akivela
 */
public class FreeDBExtractor extends AbstractExtractor implements WandoraTool {
    
    public String locatorPrefix = "http://wandora.org/si/freedb/";
   
    
    /** Creates a new instance of ExtractIconclassKeywords */
    public FreeDBExtractor() {
    }
    
    @Override
    public String getName() {
        return "FreeDB extractor";
    }
    
    @Override
    public String getDescription() {
        return "Extract FreeDB database entries.";
    }
    


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_freedb.png");
    }

    @Override
    public String[] getContentTypes() {
        return new String[]{"text/plain"};
    }

    
    
    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select FreeDB data file(s) or directories containing FreeDB data files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking FreeDB data files!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Ready. No extractions! %1 FreeDB data file(s) and %2 other file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 FreeDB data file(s) and %2 other file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 FreeDB data file(s) and %2 other files crawled!";
            
            case LOG_TITLE: return "FreeDB Extraction Log";
        }
        return "";
    }
    
    
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new BufferedReader(new StringReader(str)), topicMap);
        return answer;
    }
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        URLConnection uc=url.openConnection();
        Wandora.initUrlConnection(uc);
        String enc=uc.getContentEncoding();
        if(enc==null) enc="ISO-8859-1";
        BufferedReader urlReader = new BufferedReader( new InputStreamReader ( uc.getInputStream(),enc ) );
        return _extractTopicsFrom(urlReader, topicMap);
    }

    public boolean _extractTopicsFrom(File keywordFile, TopicMap topicMap) throws Exception {
        boolean result = false;
        BufferedReader breader = null;
        try {
            if(keywordFile == null) {
                log("No FreeDB data file addressed! Using default file name 'freedb.txt'!");
                keywordFile = new File("freedb.txt");
            }
            Reader r=new InputStreamReader(new FileInputStream(keywordFile),"ISO-8859-1");
//            FileReader fr = new FileReader(keywordFile);
//            breader = new BufferedReader(fr);
            breader = new BufferedReader(r);
            result = _extractTopicsFrom(breader, topicMap);
        }
        finally {
            if(breader != null) breader.close();
        }
        return result;
    }
    
    
    
    public boolean _extractTopicsFrom(BufferedReader breader, TopicMap topicMap) throws Exception {
        int discCounter = 0;
        
        try {
            Topic discType = getOrCreateTopic(topicMap, makeSI("disc"), "Disc (freedb)", null);
            Topic yearType = getOrCreateTopic(topicMap, makeSI("year"), "Year (freedb)", null);
            Topic genreType = getOrCreateTopic(topicMap, makeSI("genre"), "Genre (freedb)", null);
            Topic trackType = getOrCreateTopic(topicMap, makeSI("track"), "Track (freedb)", null);
            Topic orderType = getOrCreateTopic(topicMap, makeSI("order"), "Order (freedb)", null);
            Topic artistType = getOrCreateTopic(topicMap, makeSI("artist"), "Artist (freedb)", null);
            Topic lengthType = getOrCreateTopic(topicMap, makeSI("length"), "Length (freedb)", null);
            Topic processedType = getOrCreateTopic(topicMap, makeSI("processedby"), "ProcessedBy (freedb)", null);
            Topic submittedType = getOrCreateTopic(topicMap, makeSI("submittedvia"), "SubmittedVia (freedb)", null);
            Topic idType = getOrCreateTopic(topicMap, makeSI("freedb_id"), "id (freedb)", null);
            
            String line = "";
            String discId = null;
            String discTitle = null;
            String discYear = null;
            Topic discYearTopic = null;
            String discGenre = null;
            Topic discGenreTopic = null;
            String trackData = null;
            String trackName = null;
            Topic trackTopic = null;
            String extraData = null;
            String discBasename = null;
            Topic discTopic = null;
            String artistName = null;
            Topic artistTopic = null;
            Association association = null;
            Hashtable players = null;
            int dtitleCount = 0;
            String discLength = null;
            Topic lengthTopic = null;
            String processedBy = null;
            Topic processedByTopic = null;
            String submittedVia = null;
            Topic submittedViaTopic = null;
            
            line = breader.readLine();
            while(line != null && !forceStop()) {
                if(line.startsWith("# Disc length: ")) {
                    discLength = line.substring(15);
                }
                else if(line.startsWith("# Processed by: ")) {
                    processedBy = line.substring(16);
                }
                else if(line.startsWith("# Submitted via: ")) {
                    submittedVia = line.substring(17);
                }
                
                else if(line.startsWith("DISCID=")) {
                    discTopic = null;
                    try {
                        discId = line.substring(7);
                        discTopic = getOrCreateTopic(topicMap, makeSI("disc/" + discId), null, null, discType);
                        setData(discTopic, idType, "en", discId);

                        if(discLength != null) {
                            lengthTopic = getOrCreateTopic(topicMap, makeSI("length/" + discLength), discLength+" (length)", discLength, lengthType);
                            if(lengthTopic != null) {       
                                players = new Hashtable();
                                players.put(discType, discTopic);
                                players.put(lengthType, lengthTopic);
                                association = topicMap.createAssociation(lengthType);
                                association.addPlayers(players);
                            }
                            discLength = null;
                        }
                        if(processedBy != null) {
                            processedByTopic = getOrCreateTopic(topicMap, makeSI("processedby/" + processedBy), processedBy+" (processedby)", processedBy, processedType);
                            if(processedByTopic != null) {       
                                players = new Hashtable();
                                players.put(discType, discTopic);
                                players.put(processedType, processedByTopic);
                                association = topicMap.createAssociation(processedType);
                                association.addPlayers(players);
                            }
                            processedBy = null;
                        }
                        if(submittedVia != null) {
                            submittedViaTopic = getOrCreateTopic(topicMap, makeSI("submittedvia/" + submittedVia), submittedVia+" (processedby)", submittedVia, submittedType);
                            if(submittedViaTopic != null) {       
                                players = new Hashtable();
                                players.put(discType, discTopic);
                                players.put(submittedType, submittedViaTopic);
                                association = topicMap.createAssociation(submittedType);
                                association.addPlayers(players);
                            }
                            submittedVia = null;
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                else if(line.startsWith("DTITLE=")) {
                    try {
                        if(discTopic != null) {
                            discTitle = line.substring(7).trim();
                            if(dtitleCount == 0) {
                                if(discTitle != null && discTitle.length() > 0) {
                                    int titleDelimiter = discTitle.indexOf(" / ");
                                    if(titleDelimiter == -1) titleDelimiter = discTitle.indexOf(" - ");
                                    if(titleDelimiter == -1) titleDelimiter = discTitle.indexOf(" : ");
                                    if(titleDelimiter > -1) {
                                        artistName = discTitle.substring(0, titleDelimiter);
                                        if(artistName != null && artistName.length() > 0) {
                                            artistTopic = getOrCreateTopic(topicMap, makeSI( "artist/" + artistName ), artistName + " (artist)", artistName, artistType);
                                            if(artistTopic != null) {
                                                players = new Hashtable();
                                                players.put(discType, discTopic);
                                                players.put(artistType, artistTopic);
                                                association = topicMap.createAssociation(artistType);
                                                association.addPlayers(players);
                                            }
                                        }
                                        discTitle = discTitle.substring(titleDelimiter + 3).trim();
                                    }

                                    discTopic.setBaseName(discTitle + " ("+discId+")");
                                    discTopic.setDisplayName("en", discTitle);
                                    dtitleCount++;
                                }
                            }
                            else if(dtitleCount > 0) {
                                String oldBasename = discTopic.getBaseName();
                                String newBasename = oldBasename.substring(0, oldBasename.length() - (" ("+discId+")").length()) + discTitle;
                                discTopic.setBaseName(newBasename + " ("+discId+")");
                                
                                discTopic.setDisplayName("en", discTopic.getDisplayName("en") + discTitle);
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                else if(line.startsWith("DYEAR=")) {
                    try {
                        if(discTopic != null) {
                            discYear = line.substring(6);
                            if(discYear != null && discYear.length() > 0) {
                                discYearTopic = getOrCreateTopic(topicMap, makeSI("year/"+discYear), discYear+" (year)", discYear, yearType);
                                if(discYearTopic != null) {
                                    players = new Hashtable();
                                    players.put(yearType, discYearTopic);
                                    players.put(discType, discTopic);
                                    association = topicMap.createAssociation(yearType);
                                    association.addPlayers(players);
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                else if(line.startsWith("DGENRE=")) {
                    try {
                        if(discTopic != null) {
                            discGenre = line.substring(7);
                            if(discGenre != null && discGenre.length() > 0) {
                                discGenreTopic = getOrCreateTopic(topicMap, makeSI("genre/"+discGenre), discGenre+" (genre)", discGenre, genreType);
                                if(discGenreTopic != null) {
                                    players = new Hashtable();
                                    players.put(genreType, discGenreTopic);
                                    players.put(discType, discTopic);
                                    association = topicMap.createAssociation(genreType);
                                    association.addPlayers(players);
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                else if(line.startsWith("TTITLE")) {
                    try {
                        if(discTopic != null) {
                            trackData = line.substring(6);
                            if(trackData != null && trackData.length() > 0) {
                                String trackNumber = "";
                                int i=0;
                                while("0123456789".indexOf(trackData.charAt(i)) > -1) {
                                    trackNumber = trackNumber + trackData.charAt(i);
                                    i++;
                                }
                                if(trackNumber.length() == 1) trackNumber = "0"+trackNumber;
                                
                                Topic orderTopic = getOrCreateTopic(topicMap, makeSI("order/" + trackNumber), trackNumber + " (order)" , trackNumber, orderType);
                                trackName = trackData.substring(i+1);
                                
                                int trackDelimiter = trackName.indexOf(" / ");
                                if(trackDelimiter == -1) trackDelimiter = trackName.indexOf(" - ");
                                if(trackDelimiter == -1) trackDelimiter = trackName.indexOf(" : ");
                                if(trackDelimiter > -1) {
                                    artistName = trackName.substring(0, trackDelimiter);
                                    artistTopic = getOrCreateTopic(topicMap, makeSI( "artist/" + artistName ), artistName + " (artist)", artistName, artistType);
                                    trackName = trackName.substring(trackDelimiter + 3);
                                }
                                
                                if(trackName != null && trackName.length() > 0) {
                                    trackTopic = getOrCreateTopic(topicMap, makeSI("disc/" + discId + "/track/" + trackNumber), trackName + " ("+discId+"/"+trackNumber+")", trackName, trackType );
                                }

                                if(trackTopic != null && orderTopic != null && discTopic != null && artistTopic != null) {
                                    players = new Hashtable();
                                    players.put(trackType, trackTopic);
                                    players.put(discType, discTopic);
                                    players.put(orderType, orderTopic);
                                    players.put(artistType, artistTopic);
                                    association = topicMap.createAssociation(trackType);
                                    association.addPlayers(players);
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                //setProgress(discCounter);
                line = breader.readLine();
            }
        }
        catch(Exception e) {
            log(e);
        }
        //log("Extracted " + discCounter + " freedb database discs.");
        return true;
    }
    
    
    
    
  
    
    public Topic getOrCreateTopic(TopicMap topicmap, String si, String baseName, String displayName) {
        return getOrCreateTopic(topicmap, new Locator(si), baseName, displayName, null);
    }
    public Topic getOrCreateTopic(TopicMap topicmap, Locator si, String baseName, String displayName) {
        return getOrCreateTopic(topicmap, si, baseName, displayName, null);
    }
    public Topic getOrCreateTopic(TopicMap topicmap, Locator si, String baseName, String displayName, Topic typeTopic) {
        try {
            return ExtractHelper.getOrCreateTopic(si, baseName, displayName, typeTopic, topicmap);
        }
        catch(Exception e) {
            log(e);
        }
        return null;
    }
    
    public Locator makeSI(String str) {
        return new Locator( TopicTools.cleanDirtyLocator(locatorPrefix + str) );
    }

  
    
    @Override
    public boolean useTempTopicMap() {
        return false;
    }

}

