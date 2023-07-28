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
 * MobyThesaurusExtractor.java
 *
 * Created on 15. lokakuuta 2007, 18:08
 *
 */

package org.wandora.application.tools.extractors;


import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * <p>
 * Tool reads Moby thesaurus file and converts if to a topic map. Moby thesaurus
 * file is a simple text file where each line defines single word and related
 * words. For example:
 * <p>
 * <p>
 * word1 relatedWord1 relatedWord2 relatedWord3 relatedWord4 ...<br>
 * word2 relatedWord1 relatedWord2 relatedWord3 relatedWord4 ...<br>
 * </p>
 * <p>
 * This extractor creates a topic for each word (including related words) and
 * a binary association for each word-relatedWord pair. If word has four related
 * words then extractor creates four associations. Notice the word may be a
 * related word for some other word, increasing the overall number of associations
 * one word eventually gets.
 * </p>
 * <p>
 * Moby thesaurus is public domain and can be acquired from
 * http://www.gutenberg.org/etext/3202
 * </p>
 * <p>
 * As the Moby thesaurus contains hundreds of thousands words Wandora
 * requires at least 2G of memory to extract complete thesaurus.
 * </p>
 * 
 * @author akivela
 */
public class MobyThesaurusExtractor extends AbstractExtractor implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public String locatorPrefix = "http://wandora.org/si/moby/";
    public boolean ANTISYMMETRIC_ASSOCIATIONS = true;
    public boolean REMOVE_RARE_WORDS = false;
    
    
    
    /** Creates a new instance of MobyThesaurusExtractor */
    public MobyThesaurusExtractor() {
    }
    
    @Override
    public String getName() {
        return "Moby thesaurus extractor";
    }
    
    @Override
    public String getDescription() {
        return "Extract Moby thesaurus database.";
    }
    
    


    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select Moby thesaurus data file(s) or directories containing Moby thesaurus data files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking Moby thesaurus data files!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Ready. No extractions! %1 Moby thesaurus data file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 Moby thesaurus data file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 Moby thesaurus data file(s) crawled!";
            
            case LOG_TITLE: return "Moby thesaurus extraction Log";
        }
        return "";
    }
    

    @Override
    public boolean browserExtractorConsumesPlainText() {
        return true;
    }


    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        BufferedReader urlReader = new BufferedReader( new InputStreamReader ( url.openStream() ) );
        return _extractTopicsFrom(urlReader, topicMap);
    }

    public boolean _extractTopicsFrom(File thesaurusFile, TopicMap topicMap) throws Exception {
        boolean result = false;
        BufferedReader breader = null;
        try {
            if(thesaurusFile == null) {
                log("No Moby thesaurus data file addressed! Using default file name 'mobythesaurus.txt'!");
                thesaurusFile = new File("mobythesaurus.txt");
            }
            FileReader fr = new FileReader(thesaurusFile);
            breader = new BufferedReader(fr);
            result = _extractTopicsFrom(breader, topicMap);
        }
        finally {
            if(breader != null) breader.close();
        }
        return result;
    }



    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new BufferedReader(new StringReader(str)), topicMap);
        return answer;
    }



    
    public boolean _extractTopicsFrom(BufferedReader breader, TopicMap topicMap) throws Exception {

        int basewordCounter = 0;
        int associationCounter = 0;
        int wordCounter = 0;
        
        try {
            Topic relatedType = getOrCreateTopic(topicMap, makeSI("schema/related-words"), "moby-related-words", null);
            Topic role1 = getOrCreateTopic(topicMap, makeSI("schema/word1"), "word-1", null);
            Topic role2 = getOrCreateTopic(topicMap, makeSI("schema/word2"), "word-2", null);
            
            String line = "";
            Association association = null;
            String[] words;
            Topic baseword;
            String basewordString;
            Topic relatedWord;
            String relatedWordString;
            
            line = breader.readLine();
            while(line != null && !forceStop()) {
                words = line.split(",");
                if(words.length > 0) {
                    basewordString = words[0];
                    if(basewordString != null) {
                        basewordString = basewordString.trim();
                        if(basewordString.length() > 0) {
                            basewordCounter++;
                            wordCounter++;
                            baseword = getOrCreateTopic(topicMap, makeSI(basewordString), basewordString, null);
                            hlog("Found word '"+basewordString+"'.");
                            for(int i=1; i<words.length && !forceStop(); i++) {
                                relatedWordString = words[i];
                                if(relatedWordString != null) {
                                    relatedWordString = relatedWordString.trim();
                                    if(relatedWordString.length() > 0 && !relatedWordString.equals(basewordString)) {
                                        relatedWord = getOrCreateTopic(topicMap, makeSI(relatedWordString), relatedWordString, null);
                                        wordCounter++;
                                        if(ANTISYMMETRIC_ASSOCIATIONS || !associationExists(baseword, relatedWord, relatedType)) {
                                            association = topicMap.createAssociation(relatedType);
                                            association.addPlayer(baseword, role1);
                                            association.addPlayer(relatedWord, role2);
                                            associationCounter++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                setProgress(basewordCounter);
                line = breader.readLine();
            }
            
            if(REMOVE_RARE_WORDS) {
                log("Removing rare words with maximum one connection!");
                Topic wordTopic = null;
                ArrayList<Topic> rareWords = new ArrayList<Topic>();
                for( Iterator<Topic> wordTopicIter = topicMap.getTopics(); wordTopicIter.hasNext(); ) {
                    wordTopic = wordTopicIter.next();
                    if(wordTopic.getAssociations().size() < 2) {
                        rareWords.add(wordTopic);
                    } 
                }
                for(Iterator<Topic> wordTopicIter = rareWords.iterator(); wordTopicIter.hasNext(); ) {
                    wordTopic = wordTopicIter.next();
                    if(!wordTopic.mergesWithTopic(role1) && !wordTopic.mergesWithTopic(role2) && !wordTopic.mergesWithTopic(relatedType)) {
                        if(wordTopic.isDeleteAllowed()) {
                            log("Removing '"+getTopicName(wordTopic)+"'.");
                            wordTopic.remove();
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        
       
        log("Found total "+basewordCounter+" basewords");
        log("Found total "+wordCounter+" words");
        log("Created total "+associationCounter+" associations");
        return true;
    }
    
    
    

    
    
    
    
    
    
    public boolean associationExists(Topic t1, Topic t2, Topic at) {
        if(t1 == null || t2 == null || at == null) return false;
        try {
            Collection<Association> c = t1.getAssociations(at);
            Association a = null;
            Collection<Topic> roles = null;
            Topic player = null;
            for(Iterator<Association> i=c.iterator(); i.hasNext(); ) {
                a = i.next();
                roles = a.getRoles();
                for(Iterator<Topic> it = roles.iterator(); it.hasNext(); ) {
                    player = a.getPlayer(it.next());
                    if(player != null && t2.mergesWithTopic(player)) return true;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
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
