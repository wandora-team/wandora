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
 * ExtractIconclassKeywords.java
 *
 * Created on 5. maaliskuuta 2007, 12:39
 *
 */

package org.wandora.application.tools.extractors.fng;



import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;
import java.io.*;
import java.net.*;




/**
 * <p>
 * Tool is used to convert Iconclass keyword files to topic maps.
 * Iconclass is a subject-specific classification system used to
 * annotate images such as artworks. Classification system contains 28 000 hierarchically
 * ordered definitions divided into ten main divisions.
 * </p>
 * <p>
 * Iconclass keyword files are simple text files. Keyword file contains
 * term specifications such as 
 * </p>
 * <p><pre>
 * NOT[34 F 1]
 * TXT[animals threatening man]
 * FIT[ihminen el�inten uhkaamana]
 *  
 * NOT[34 F 11]
 * TXT[man struggling with animals]
 * FIT[ihmisen ja el�inten v�linen kamppailu]
 * 
 * NOT[34 F 11 1]
 * TXT[man struggling with animals as ornamental variant with antithetically placed animals (mostly lions)]
 * FIT[]
 * 
 * NOT[34 F 12]
 * TXT[man killing animal]
 * FIT[ihminen surmaa el�imen]
 * </pre></p>
 *
 * <p>
 * where NOT structure specifies the Iconclass notation (id) of the term.
 * TXT specifies the English description of the term and
 * FIT specifies Finnish description of the term.
 * </p>
 * <p>
 * Iconclass file extraction generates a topic map with
 * a topic for each found Iconclass term. Topic's variant names are
 * English and Finnish descriptions. Iconclass topics are arranged
 * into a super-subclass tree using the Iconclass notation identifier.
 * </p>
 * <p>
 * Wandora doesn't include any Iconclass keyword files. This extractor
 * was created for Finnish National Gallery's artwork site at
 * http://kokoelmat.fng.fi
 * </p>
 * <p>
 * To read more about the Iconclass system see
 * http://www.iconclass.nl.
 * </p>
 * 
 *
 * @author akivela
 */
public class ExtractIconclassKeywords extends AbstractExtractor implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	
	public static boolean CREATE_ICONCLASS_TOPICS = false;
    
   
    
    /** Creates a new instance of ExtractIconclassKeywords */
    public ExtractIconclassKeywords() {
    }
    
    @Override
    public String getName() {
        return "Extract Iconclass keywords";
    }
    
    
    @Override
    public String getDescription() {
        return "Extracts Iconclass keyword files.";
    }
    
    
    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select Iconclass keyword file(s) or directories containing Iconclass keyword files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking Iconclass keyword files!";
        
            case FILE_PATTERN: return ".*\\.txt";
            
            case DONE_FAILED: return "Ready. No extractions! %1 Iconclass keyword(s) and %2 other file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 Iconclass keyword(s) and %2 other file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 Iconclass keyword(s) and %2 other files crawled!";
            
            case LOG_TITLE: return "Iconclass Keyword Extraction Log";
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
                log("No Iconclass keyword file addressed! Using default file name 'iconclass_keywords.txt'!");
                keywordFile = new File("iconclass_keywords.txt");
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
        log("Extracting Iconclass keywords!");
        int iconclassCounter = 0;
        int nameCounter = 0;
        
        try {
            Topic iconclassType = topicMap.getTopic("http://www.iconclass.nl/");
            if(iconclassType == null) {
                iconclassType = topicMap.createTopic();
                iconclassType.addSubjectIdentifier(new Locator("http://www.iconclass.nl/"));
                iconclassType.setBaseName("Keyword (iconclass)");
            }
            
            String line = "";
            String iconclassString = null;
            String iconclassSI = null;
            String iconclassBasename = null;
            Locator iconclassSILocator = null;
            String iconclass = null;
            Topic iconclassTopic = null;
            String iconclassDisplayName = null;
            Association iconclassAssociation = null;
            HashMap players = null;
            
            line = breader.readLine();
            while(line != null && !forceStop()) {
                if(line.startsWith("NOT[")) {
                    iconclassTopic = null;
                    try {
                        iconclassString = removeSpaces(removeBlocks(line.substring(3)));

                        iconclassSILocator = getLocatorForIconclass(iconclassString);
                        iconclassTopic = topicMap.getTopic(iconclassSILocator);
                        if(iconclassTopic == null) {
                            iconclassTopic = topicMap.getTopic( new Locator(TopicTools.cleanDirtyLocator("http://www.iconclass.nl/" + iconclassString)) );
                        }
                        if(CREATE_ICONCLASS_TOPICS && iconclassTopic == null) {
                            iconclassCounter++;
                            iconclassBasename = iconclassString;
                            iconclassTopic = topicMap.createTopic();
                            iconclassTopic.addSubjectIdentifier(iconclassSILocator);
                            iconclassTopic.setBaseName(iconclassBasename + " (iconclass)");
                            iconclassTopic.addType(iconclassType);
                            iconclassTopic.addSubjectIdentifier(new Locator(TopicTools.cleanDirtyLocator("http://www.iconclass.nl/" + iconclassString)));

                            // **** BUILD SUPER-CLASSES FOR THE ICONCLASS TOPIC! ****
                            String iconclassSubCode = iconclassString;
                            String iconclassSuperCode = null;
                            for(int i=Math.min(3, iconclassString.length()); i>=0; i--) {
                                iconclassSuperCode = iconclassString.substring(0,i);
                                if(! iconclassSuperCode.matches("[0-9A-Z]+")) continue;
                                createIconclassSubSuperRelation( iconclassSubCode, iconclassSuperCode, topicMap );
                                iconclassSubCode = iconclassSuperCode;
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                else if(line.startsWith("TXT[")) {
                    String newline = "";
                    while(line.indexOf("]") == -1 && newline != null && !forceStop() ) {
                        newline = breader.readLine();
                        if(newline != null) line += " " + newline;
                    }
                    try {
                        if(iconclassTopic != null) {
                            iconclassDisplayName = removeBlocks(line.substring(3));
                            if(iconclassDisplayName != null && iconclassDisplayName.length() > 0) {
                                iconclassTopic.setDisplayName("en", iconclassDisplayName);
                                nameCounter++;
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                else if(line.startsWith("FIT[")) {
                    String newline = "";
                    while(line.indexOf("]") == -1 && newline != null && !forceStop() ) {
                        newline = breader.readLine();
                        if(newline != null) line += " " + newline;
                    }
                    try {
                        if(iconclassTopic != null) {
                            iconclassDisplayName = removeBlocks(line.substring(3));
                            if(iconclassDisplayName != null && iconclassDisplayName.length() > 0) {
                                iconclassTopic.setDisplayName("fi", iconclassDisplayName);
                                nameCounter++;
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                setProgress(iconclassCounter);
                line = breader.readLine();
            }
        }
        catch(Exception e) {
            log(e);
        }
        log("Extracted " + iconclassCounter + " iconclass keyword topics.");
        log("Extracted " + nameCounter + " names for iconclass topics.");
        return true;
    }
    
    
    
    
    public String removeSpaces(String str) {
        if(str != null) {
            str = str.replaceAll(" ", "");
        }
        return str;
    }
    
    
    
    public String removeBlocks(String str) {
        if(str != null) {
            if(str.indexOf('[') > -1 && str.indexOf(']') > -1) {
                str = str.substring(str.indexOf('[')+1, str.indexOf(']'));
            }
        }
        return str;
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
                
                HashMap players = new HashMap();
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
    
    
    
    @Override
    public boolean useTempTopicMap() {
        return false;
    }

}

