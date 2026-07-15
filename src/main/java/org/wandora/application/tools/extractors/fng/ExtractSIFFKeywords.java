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
 * ExtractSIFFKeywords.java
 *
 * Created on 6.6.2006, 19:51
 *
 */

package org.wandora.application.tools.extractors.fng;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Hashtable;

import org.wandora.application.WandoraTool;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicTools;
import org.wandora.utils.Textbox;


/**
 * <p>
 * Class implements special extractor for Sinebrychoff's artwork files.
 * Sinebrychoff's artwork file is basically a database dump file containing
 * artwork specific information. Class extracts only artworks and keywords
 * found in given file(s).
 * </p>
 * <p>
 * About the file format. In the given file artworks are supposed to be separated
 * with "\n\n" characters. Artwork specific data record begins with a line
 * containing artwork identifier (inventory code). Identifier line is recognized
 * with string "@@NO:". Artwork's keyword record line is recognized with "AH:".
 * Keyword record may contain multiple semicolon separated keywords.
 * Below is an example fragment of the Sinebrychoff's artwork file.
 * </p>
 * <p><pre>
 * NO:A IV 3299
 * MU:SFF
 * OM:Valtion taidemuseo
 * NI:INTIALAINEN MINIATYYRI ; Kaksi naista soittaa kahdelle pyh�lle miehelle (sadhulle), Rajput-miniatyyri
 * VV:n 1800
 * MA:vesiv�ri, kulta ja hopea
 * MI:20x12,7
 * ME:merk.
 * MJ:takana
 * MS:Rajasthan School
 * SI:V;SFF
 * HA:osto
 * HT:taiteilija Per Stenius
 * OA:1.6.1959
 * HH:30.000
 * VY:MV
 * VN: 11085
 * P�:maalaus
 * ER:miniatyyri
 * AH:KOHTAUS;musiikki;naiset;pyh�t miehet;intialaiset;koira
 * KI:B.Robinson
 * LI:RV/87
 * </pre>
 * </p>
 * 
 * 
 *
 * @author akivela
 */
public class ExtractSIFFKeywords extends AbstractExtractor implements WandoraTool {
    
	private static final long serialVersionUID = 1L;
	
	public boolean createArtworkTopics = true;
    
    
    /** Creates a new instance of ExtractSIFFKeywords */
    public ExtractSIFFKeywords() {
    }
   
   
    @Override
    public String getName() {
        return "Extract SIFF keywords";
    }
    
    @Override
    public String getDescription() {
        return "Extract keyword and artwork topics from Sinebrychoff artmuseum's artwork text file.";
    }
    
    

    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select SIFF Keyword file(s) or directories containing SIFF Keyword files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking SIFF Keyword files!";
        
            case FILE_PATTERN: return ".*\\.txt";
            
            case DONE_FAILED: return "Ready. No extractions! %1 SIFF keyword(s) and %2 other file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 SIFF keyword(s) and %2 other file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 SIFF keyword(s) and %2 other files crawled!";
            
            case LOG_TITLE: return "SIFF Keyword Extraction Log";
        }
        return "";
    }
    


    
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new BufferedReader(new StringReader(str)), topicMap);
        return answer;
    }
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        BufferedReader urlReader = new BufferedReader( new InputStreamReader ( url.openStream() ) );
        return _extractTopicsFrom(urlReader, topicMap);
    }

    public boolean _extractTopicsFrom(File keywordFile, TopicMap topicMap) throws Exception {
        boolean result = false;
        BufferedReader breader = null;
        try {
            if(keywordFile == null) {
                log("No keyword file addressed! Using default file name!");
                keywordFile = new File("siff_keywords.txt");
            }
            FileReader fr = new FileReader(keywordFile);
            breader = new BufferedReader(fr);
            result = _extractTopicsFrom(breader, topicMap);
        }
        finally {
            if(breader != null) breader.close();
        }
        return result;
    }
    
    
    
    public boolean _extractTopicsFrom(BufferedReader breader, TopicMap topicMap) throws Exception {
        log("Extracting SIFF keywords!");
        int workCounter = 0;
        int keywordCounter = 0;
        
        try {
            Topic artWorkType=topicMap.getTopic("http://www.muusa.net/Teos");
            if(artWorkType == null) {
                artWorkType = topicMap.createTopic();
                artWorkType.addSubjectIdentifier(new Locator("http://www.muusa.net/Teos"));
                artWorkType.setBaseName("Teos");
            }
            Topic asiasanaClassType=topicMap.getTopic("http://www.sinebrychoffintaidemuseo.fi/keywords");
            if(asiasanaClassType == null) {
                asiasanaClassType = topicMap.createTopic();
                asiasanaClassType.addSubjectIdentifier(new Locator("http://www.sinebrychoffintaidemuseo.fi/keywords"));
                asiasanaClassType.setBaseName("asiasana (siff)");
            }
            
            String line = "";
            String[] tokens = null;
            String currentWorkId = null;
            String currentWorkSI = null;
            Topic currentWork = null;
            String asiasanaString = null;
            String[] asiasanat = null;
            String asiasanaSI = null;
            String asiasana = null;
            Topic asiasanaTopic = null;
            String asiasanaDisplayName = null;
            Association asiasanaAssociation = null;
            Hashtable<Topic,Topic> players = null;
            
            line = breader.readLine();
            while(line != null && !forceStop()) {
                if(line.startsWith("@@NO:")) {
                    currentWorkId = Textbox.trimExtraSpaces(line.substring(5));
                    if(currentWorkId.length() > 0) {
                        workCounter++;
                        log("Found artwork '" + currentWorkId + "'");
                        currentWorkSI = TopicTools.cleanDirtyLocator("http://www.muusa.net/E42_Object_Identifier/" + currentWorkId);
                        currentWork = topicMap.getTopic(currentWorkSI);
                        if(currentWork == null && createArtworkTopics) {
                            currentWork = topicMap.createTopic();
                            currentWork.addSubjectIdentifier(new Locator(currentWorkSI));
                            currentWork.addType(artWorkType);
                        }
                        if(currentWork == null) {
                            line = breader.readLine();
                            while(line != null && !line.startsWith("@@NO:")) {
                                line = breader.readLine();
                            }
                            continue;
                        }
                    }
                }
                else if(line.startsWith("AH:")) {
                    asiasanaString = line.substring(3);
                    asiasanat = asiasanaString.split(";");
                    for(int i=0; i<asiasanat.length; i++) {
                        asiasana = Textbox.trimExtraSpaces(asiasanat[i]);
                        if(asiasana.length() > 0) {
                            keywordCounter++;
                            log("Found keyword '" + asiasana + "'");
                            asiasanaSI = TopicTools.cleanDirtyLocator("http://www.sinebrychoffintaidemuseo.fi/keywords/" + asiasana);
                            asiasanaTopic = topicMap.getTopic(asiasanaSI);
                            if(asiasanaTopic == null) {
                                asiasanaTopic = topicMap.createTopic();
                                asiasanaTopic.addSubjectIdentifier(new Locator(asiasanaSI));
                                asiasanaTopic.setBaseName(asiasana + " (asiasana)");
                                asiasanaTopic.addType(asiasanaClassType);
                                asiasanaTopic.setDisplayName("fi", asiasana);
                            }

                            if(currentWork != null) {
                                log("Associating keyword '" + asiasana + "' and artwork '" + currentWorkId + "'!");
                                players = new Hashtable<>();
                                players.put(asiasanaClassType, asiasanaTopic );
                                players.put(artWorkType, currentWork);
                                asiasanaAssociation = topicMap.createAssociation(asiasanaClassType);
                                asiasanaAssociation.addPlayers(players);
                            }
                        }
                    }
                }
                setProgress(keywordCounter);
                line = breader.readLine();
            }
        }
        catch(Exception e) {
            log(e);
        }
        log("Extracted " + workCounter + " artworks.");
        log("Extracted " + keywordCounter + " keywords.");
        return true;
    }
    
    
    
    public boolean useTempTopicMap() {
        return false;
    }

}

