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
 * ExtractKiasmaKeywords.java
 *
 * Created on 3. huhtikuuta 2006, 21:27
 *
 */

package org.wandora.application.tools.extractors.fng;


import org.wandora.utils.Textbox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TopicTools;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;
import org.wandora.topicmap.Association;
import org.wandora.application.WandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.application.tools.extractors.*;
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



/**
 * <p>
 * Class implements an extraction tool for Kiasma artmuseum's iconclass keyword
 * file. File contains artworks and linked iconclass keywords. File is originally
 * created and updated with Microsoft Excel and later exported as tab limited
 * text file. Thus all records in file are separated with tabulator on new line
 * character.
 *</p>
 * <p>
 * First column of keyword file contains artwork's identifier (inventory code).
 * Second column of file contains artist's name. Third column contains artwork's name
 * Remaining one to four columns contains iconclass keywords linked to the artwork.
 * First remaining column contains iconclass code of the keyword and latter code's
 * English, Finnish and Swedish representation. Below is an example of single
 * artwork in file. In order to make example more readable all tab characters
 * have been changed to sets of spaces.
 * <p>
 * <p><pre>
 * N-1992-225   Trockel, Rosemarie    Nimetön          41B2      liesi, hella, uuni yms.	
 *                                                   49D452      hexahedron, cube	
 *                                               22C4(WHITE)	 värit, pigmentit ja maalit: valkoinen	
 * </pre></p>
 *
 *
 * @author akivela
 */
public class ExtractKiasmaKeywords extends AbstractExtractor implements WandoraTool {
    
    
    /**
     * Creates a new instance of ExtractKiasmaKeywords
     */
    public ExtractKiasmaKeywords() {
    }
   
   
    @Override
    public String getName() {
        return "Extract Kiasma keywords";
    }
    
    @Override
    public String getDescription() {
        return "Extract Iconclass keywords and artworks from Kiasma's iconclass file.";
    }
    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select Kiasma keyword file(s) or directories containing Kiasma keyword files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking Kiasma keyword files!";
        
            case FILE_PATTERN: return ".*\\.txt";
            
            case DONE_FAILED: return "Done! No extractions! %1 Kiasma keyword(s) and %2 other file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 Kiasma keyword(s) and %2 other file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 Kiasma keyword(s) and %2 other files crawled!";
            
            case LOG_TITLE: return "Kiasma Keyword Extraction Log";
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
                log("No keyword file addressed! Using default file name 'kiasma_keywords.txt'!");
                keywordFile = new File("kiasma_keywords.txt");
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
        int workCounter = 0;
        int keywordCounter = 0;
        log("Extracting keywords...");
        try {
            Topic artWorkType=topicMap.getTopic("http://www.muusa.net/Teos");
            if(artWorkType == null) {
                artWorkType = topicMap.createTopic();
                artWorkType.addSubjectIdentifier(new Locator("http://www.muusa.net/Teos"));
                artWorkType.setBaseName("Teos");
            }
            Topic iconclassType=topicMap.getTopic("http://www.iconclass.nl/");
            if(iconclassType == null) {
                iconclassType = topicMap.createTopic();
                iconclassType.addSubjectIdentifier(new Locator("http://www.iconclass.nl/"));
                iconclassType.setBaseName("Iconclass");
            }
                       
            String line = "";
            String[] tokens = null;
            String currentWorkId = null;
            String currentWorkSI = null;
            Topic currentWork = null;
            //String iconclassSI = null;
            Locator iconclassSILocator = null;
            String iconclassCode = null;
            String iconclassBasename = null;
            Topic iconclassTopic = null;
            String iconclassDisplayName = null;
            Association iconclassAssociation = null;
            Hashtable players = null;
           
            line = breader.readLine();
            while(line != null && !forceStop()) {
                tokens = line.split("\t");
                for(int i=0; i<tokens.length; i++) { // Trim results!
                    if(tokens[i] != null && tokens[i].length() > 0) {
                        //System.out.print("trimming " + tokens[i]);
                        tokens[i] = Textbox.trimExtraSpaces(tokens[i]);
                        //System.out.print(" --> " + tokens[i]);
                    }
                }
                if(tokens.length > 1) {
                    if(tokens[0].length() > 0) { // Inventory number of artwork!
                        currentWorkId = tokens[0];
                        currentWorkSI = TopicTools.cleanDirtyLocator("http://www.muusa.net/E42_Object_Identifier/" + currentWorkId);
                        currentWork = topicMap.getTopic(currentWorkSI);
                        if(currentWork == null) {
                            currentWork = topicMap.getTopicWithBaseName(currentWorkId);
                        }
                        if(currentWork != null) {
                            log("Found artwork for '" + currentWorkId + "'.");
                            workCounter++;
                        }
                        else {
                            log("Artwork missing '" + currentWorkSI + "'.");
                        }
                    }
                    if(currentWork != null) {
                        //System.out.println("tokens length:" + tokens.length);
                        if(tokens.length > 3 && tokens[3].length() > 0) { // Iconclass code exists!
                            iconclassCode = tokens[3];
                            log("Found iconclass keyword '" + iconclassCode + "'.");
                            iconclassSILocator = getLocatorForIconclass(iconclassCode);
                            iconclassTopic = topicMap.getTopic(iconclassSILocator);
                            if(iconclassTopic == null) {
                                keywordCounter++;
                                iconclassBasename = iconclassCode;
                                iconclassTopic = topicMap.createTopic();
                                iconclassTopic.addSubjectIdentifier(iconclassSILocator);
                                if(tokens.length > 4 && tokens[4].length() > 0) { // Iconclass en name exists!
                                    iconclassBasename += " - " + tokens[4];
                                    iconclassTopic.setDisplayName("en", tokens[4]);
                                }
                                iconclassTopic.setBaseName(iconclassBasename + " (iconclass)");
                                if(tokens.length > 5 && tokens[5].length() > 0) { // Iconclass fi name exists!
                                    iconclassTopic.setDisplayName("fi", tokens[5]);
                                }
                                iconclassTopic.addType(iconclassType);
                                iconclassTopic.addSubjectIdentifier(new Locator(TopicTools.cleanDirtyLocator("http://www.iconclass.nl/" + iconclassCode)));
                                
                                // **** BUILD SUPER-CLASSES FOR THE ICONCLASS TOPIC! ****
                                String iconclassSubCode = iconclassCode;
                                String iconclassSuperCode = null;
                                for(int i=Math.min(3, iconclassCode.length()); i>=0; i--) {
                                    iconclassSuperCode = iconclassCode.substring(0,i);
                                    if(! iconclassSuperCode.matches("[0-9A-Z]+")) continue;
                                    createIconclassSubSuperRelation( iconclassSubCode, iconclassSuperCode, topicMap );
                                    iconclassSubCode = iconclassSuperCode;
                                }
                            }
                            else {
                                iconclassDisplayName = iconclassTopic.getDisplayName("en");
                                if(tokens.length > 4 && tokens[4].length() > 0 && (iconclassDisplayName == null || iconclassDisplayName.length() == 0)) {
                                    iconclassTopic.setDisplayName("en", tokens[4]);
                                    iconclassTopic.setBaseName(iconclassCode + " - " + tokens[4] + " (iconclass)");
                                    if(tokens.length > 5 && tokens[5].length() > 0) { // Iconclass fi name exists!
                                        iconclassTopic.setDisplayName("fi", tokens[5]);
                                    }
                                }
                            }
                            players = new Hashtable();
                            players.put(iconclassType, iconclassTopic);
                            players.put(artWorkType, currentWork);
                            iconclassAssociation = topicMap.createAssociation(iconclassType);
                            iconclassAssociation.addPlayers(players);
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
    
    
    
    
    
    public void createIconclassSubSuperRelation(String sub, String sup, TopicMap topicMap) {
        try {
            Topic superClass = getOrCreateIconclassTopic(topicMap, getLocatorForIconclass(sup));
            Topic subClass = getOrCreateIconclassTopic(topicMap, getLocatorForIconclass(sub));

            Topic supersubclassType = getOrCreateTopic(topicMap, XTMPSI.SUPERCLASS_SUBCLASS);
            Topic superclassType = getOrCreateTopic(topicMap, XTMPSI.SUPERCLASS);
            Topic subclassType = getOrCreateTopic(topicMap, XTMPSI.SUBCLASS);
            
            if(superClass != null && subClass != null && supersubclassType != null && superclassType != null && subclassType != null ) {
                if(subClass.getBaseName() == null) {
                    subClass.setBaseName(sub + " (iconclass)");
                }
                if(superClass.getBaseName() == null) {
                    superClass.setBaseName(sup + " (iconclass)");
                }
                
                Hashtable players = new Hashtable();
                players.put(superclassType, superClass);
                players.put(subclassType, subClass);
                Association supeclassAssociation = topicMap.createAssociation(supersubclassType);
                supeclassAssociation.addPlayers(players);
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    public Topic getOrCreateIconclassTopic(TopicMap topicmap, String si) {
        return getOrCreateIconclassTopic(topicmap, new Locator(si));
    }
    
    
    public Topic getOrCreateIconclassTopic(TopicMap topicmap, Locator si) {
        Topic topic = getOrCreateTopic(topicmap, si);
        try {
            if(topic != null) topic.addType(topicmap.getTopic("http://www.iconclass.nl/"));
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }
    
    
    
    public Topic getOrCreateTopic(TopicMap tm, String si) {
        return getOrCreateTopic(tm, new Locator(si));
    }
    
    public Topic getOrCreateTopic(TopicMap tm, Locator si) {
        Topic topic = null;
        try {
            topic = tm.getTopic(si);
            if(topic == null) {
                topic = tm.createTopic();
                topic.addSubjectIdentifier(si);
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }
    
    
    public Locator getLocatorForIconclass(String iconclassCode) {
        try {
            return new Locator( TopicTools.cleanDirtyLocator("http://wandora.org/si/iconclass/" + iconclassCode) );
        }
        catch(Exception e) {
            log(e);
            return null;
        }
    }
    
    
    
    
    
    
    @Override
    public boolean useTempTopicMap() {
        return false;
    }
    
}
