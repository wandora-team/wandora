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
 * ExtractMuusaIconclassBridge.java
 *
 * Created on 5. maaliskuuta 2007, 10:07
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
 * Extractor is an example of solution used to bridge to separate keyword islands.
 * Extractor reads a simple text file with Muusa and Iconclass keywords, and
 * creates a bridge associations to link similar keywords. The extractor was
 * created for Finnish National Gallery's site at http://kokoelmant.fng.fi.
 * </p>
 * <p>
 * <pre>
 *     [Muusa keyword]        [usage]  [Muusa keyword variant]   [Iconclass keyword]
 *
 *     lierihattu (asiasana)     19	                                      41D221		
 *     portaat (asiasana)        19	                                      41A34		
 *     taistelu (asiasana)       18	                                      45		
 *     tupakointi (asiasana)     18	                                      41C7		
 *     kaappi (asiasana)         17	                                      41A254		
 *     portti (asiasana)         17	                                      41A5		
 *     pullot (asiasana)         17	                                      41A77		
 *     ratsastaja (asiasana)     17	                                      46C131
 *</pre>
 * </p>
 *
 * @author akivela
 */
public class ExtractMuusaIconclassBridge extends AbstractExtractor implements WandoraTool {
    
    
    
    /** Creates a new instance of ExtractMuusaIconclassBridge */
    public ExtractMuusaIconclassBridge() {
    }
   
    
    @Override
    public String getName() {
        return "Extract Muusa-Iconclass bridge";
    }
    
    @Override
    public String getDescription() {
        return "Extracts Iconclass and Muusa keywords, and links extracted keywords";
    }
    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select Muusa-Iconclass keyword file(s) or directories containing Kiasma keyword files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking Muusa-Iconclass keyword files!";
        
            case FILE_PATTERN: return ".*\\.txt";
            
            case DONE_FAILED: return "Ready. No extractions! %1 Muusa-Iconclass keyword(s) and %2 other file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 Muusa-Iconclass keyword(s) and %2 other file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 Muusa-Iconclass keyword(s) and %2 other files crawled!";
            
            case LOG_TITLE: return "Muusa-Iconclass Keyword Extraction Log";
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
                log("No keyword file addressed! Using default file name 'muusaiconclass_keywords.txt'!");
                keywordFile = new File("muusaiconclass_keywords.txt");
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
        int iconclassCounter = 0;
        int muusaKeywordCounter = 0;
        int bridgeCounter = 0;
        log("Extracting keyword bridge...");
        try {
            Topic muusaType=topicMap.getTopic("http://www.muusa.net/keyword");
            if(muusaType == null) {
                muusaType = topicMap.createTopic();
                muusaType.addSubjectIdentifier(new Locator("http://www.muusa.net/keyword"));
                muusaType.setBaseName("Keyword (muusa)");
            }
            Topic iconclassType=topicMap.getTopic("http://www.iconclass.nl/");
            if(iconclassType == null) {
                iconclassType = topicMap.createTopic();
                iconclassType.addSubjectIdentifier(new Locator("http://www.iconclass.nl/"));
                iconclassType.setBaseName("Keyword (iconclass)");
            }
            Topic muusaIconclassBridgeType=topicMap.getTopic("http://wandora.org/si/muusa_iconclass_bridge");
            if(muusaIconclassBridgeType == null) {
                muusaIconclassBridgeType = topicMap.createTopic();
                muusaIconclassBridgeType.addSubjectIdentifier(new Locator("http://wandora.org/si/muusa_iconclass_bridge"));
                muusaIconclassBridgeType.setBaseName("Keyword bridge (muusa - iconclass)");
            }

            String line = "";
            String[] tokens = null;
            String muusaKeywordId = null;
            String muusaKeywordSI = null;
            Topic muusaKeyword = null;
            
            //String iconClassSI = null;
            Locator iconclassSILocator = null;
            String iconclassCode = null;
            String iconclassBasename = null;
            Topic iconclassTopic = null;
            String iconclassDisplayName = null;
            Association iconclassAssociation = null;
            Hashtable players = null;
            Association muusaIconclassBridge = null;
            
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
                        muusaKeywordId = tokens[0];
                        muusaKeyword = topicMap.getTopicWithBaseName(muusaKeywordId);

                        if(muusaKeyword != null) {
                            log("Found Muusa keyword for '" + muusaKeywordId + "'.");
                            muusaKeywordCounter++;
                        }
                        else {
                            log("Muusa keyword missing '" + muusaKeywordId + "'.");
                        }
                    }
                    if(muusaKeyword != null) {
                        //System.out.println("tokens length:" + tokens.length);
                        if(tokens.length > 3 && isValidIconclass(tokens[3])) { // Iconclass code exists!
                            iconclassCode = tokens[3];
                            log("Found iconclass keyword '" + iconclassCode + "'.");
                            iconclassSILocator = getLocatorForIconclass(iconclassCode);
                            iconclassTopic = topicMap.getTopic(iconclassSILocator);
                            if(iconclassTopic == null) {
                                iconclassCounter++;
                                iconclassBasename = iconclassCode;
                                iconclassTopic = topicMap.createTopic();
                                iconclassTopic.addSubjectIdentifier(iconclassSILocator);
                                iconclassTopic.setBaseName(iconclassBasename + " (iconclass)");
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

                            if(iconclassTopic != null && muusaKeyword != null) {
                                players = new Hashtable();
                                players.put(iconclassType, iconclassTopic);
                                players.put(muusaType, muusaKeyword);
                                muusaIconclassBridge = topicMap.createAssociation(muusaIconclassBridgeType);
                                muusaIconclassBridge.addPlayers(players);
                                bridgeCounter++;
                            }
                        }
                    }
                }
                setProgress(iconclassCounter);
                line = breader.readLine();
            }
        }
        catch(Exception e) {
            log(e);
        }
        log("Extracted " + muusaKeywordCounter + " Muusa keywords.");
        log("Extracted " + iconclassCounter + " Iconclass keywords.");
        log("Created " + bridgeCounter + " bridges between Iconclass and Muusa keywords.");
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
    
    
    public boolean isValidIconclass(String iconclass) {
        if(iconclass != null && iconclass.length() > 0) {
            if(!iconclass.startsWith("X") && !iconclass.startsWith("x") && !iconclass.startsWith("?")) return true;
        }
        return false;
    }
    
    
    
    public boolean useTempTopicMap() {
        return false;
    }
    
}

