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
 */

package org.wandora.application.tools.extractors.word;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


abstract class AbstractWordExtractor extends AbstractExtractor{
    
    private Topic baseTopic = null;
    private WordConfiguration config;
    
    
    //--------------------------------------------------------------------------
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        List<String> words = Files.readAllLines(f.toPath(), Charset.forName("UTF-8"));
        return handleWordList(words, t);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        String str = IOUtils.toString(u.openStream());
        return _extractTopicsFrom(str, t);
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        String[] strArray = str.split("\\r?\\n");
        return handleWordList(Arrays.asList(strArray), t);
    }
    
    abstract WordConfiguration getConfig();
    
    /**
     *
     * Associates each topic in current context with a word if the word is found
     * in the topic instance data.
     *
     * First look up topics for each word, then create topics from words and
     * associate words with corresponding existing topics.
     *
     * @param words a list of words to look for in instance data
     * @param tm
     * @return whether the process succeeded
     */
    protected boolean handleWordList(List<String> words, TopicMap tm)
            throws TopicMapException {

        log("Handling " + words.size() + " words");

        if (config == null) {
            config = getConfig();
        }

        baseTopic = ExtractHelper.getOrCreateTopic(getSIBase(), getName(), tm);

        makeSubclassOfWandoraClass(baseTopic, tm);

        HashMap<String, HashMap<Topic,Float>> mapping = new HashMap<>();



        for (String word : words) {
            try {
                log("Finding topics for \"" + word + "\"");
                HashMap<Topic,Float> topics = solveTopics(word, tm);
                mapping.put(word, topics);
            } catch (Exception e) {
                log(e.getMessage());
            }

        }

        for (String word : words) {
            HashMap<Topic,Float> matches = mapping.get(word);
            if (matches == null) {
                continue; //In case solveTopics failed for this word
            }
            try {
                Topic wt = createWordTopic(word, tm);
                
                log("Associating " + matches.keySet().size() + " topics with \"" + word + "\"");

                for (Topic t : matches.keySet()) {
                    
                    Topic st = createScoreTopic(matches.get(t), tm);
                    associateWord(wt, t, st, tm);
                }
            } catch (TopicMapException tme) {
                log(tme.getMessage());
            }
        }

        return true;

    }

    /**
     * Find topics with content matching 'word' according to given configuration
     *
     * @param word
     * @param tm
     * @return a set of topics matching word
     */
    private HashMap<Topic, Float> solveTopics(String word, TopicMap tm) {

        Context c = getContext();
        Iterator contextIterator = c.getContextObjects();

        HashMap<Topic, Float> scores = new HashMap<>();
        
        Object needle = this.formNeedle(word);

        while (contextIterator.hasNext()) {
            Object o = contextIterator.next();
            if (!(o instanceof Topic)) {
                continue;
            }

            Topic t = (Topic) o;
            
            try {
                // Check base name
                if (config.getBaseName()) {
                    float score = isMatch(needle, t.getBaseName());
                    if(score > 0){
                        if(!scores.containsKey(t) || scores.get(t) < score){
                            scores.put(t, score);
                        }
                    }
                    
                }

                // Check variant names
                if (config.getVariantName()) {
                    for (Set<Topic> scope : t.getVariantScopes()) {
                        String variant = t.getVariant(scope);

                        if (!config.getCaseSensitive()) {
                            variant = variant.toLowerCase();
                        }

                        float score = isMatch(needle, variant);
                        if(score > 0){
                            if(!scores.containsKey(t) || scores.get(t) < score){
                                scores.put(t, score);
                            }
                        }
                        
                    }
                }


                // Check instance data
                if (config.getInstanceData()) {
                    for (Topic type : t.getDataTypes()) {

                        Enumeration<String> data = t.getData(type).elements();

                        while (data.hasMoreElements()) {

                            String datum = data.nextElement();

                            if (!config.getCaseSensitive()) {
                                datum = datum.toLowerCase();
                            }

                            float score = isMatch(needle, datum);
                            if(score > 0){
                                if(!scores.containsKey(t) || scores.get(t) < score){
                                    scores.put(t, score);
                                }
                            }
                        }
                    }
                }

            } catch (TopicMapException tme) {
                log(tme.getMessage());
            }



        }

        return scores;
    }

    /**
     * Create a Topic representing word
     *
     * @param word
     * @param tm
     * @return
     * @throws TopicMapException if topic creation fails
     */
    private Topic createWordTopic(String word, TopicMap tm) throws TopicMapException {
        
        String bnSuffix = getBNSuffix();
        
        String si = getSIBase() + "word/" + word;
        String bn = word + " " + bnSuffix;
        Topic w = ExtractHelper.getOrCreateTopic(si, bn, tm);
        ExtractHelper.makeSubclassOf(w, baseTopic, tm);
        return w;
    }
    
    private Topic createScoreTopic(Float get, TopicMap tm) throws TopicMapException {
        
        String bnSuffix = getBNSuffix();
        
        String si = getSIBase() + "score/"  + get.toString();
        String bn = get.toString() + " " + bnSuffix;
        Topic w = ExtractHelper.getOrCreateTopic(si, bn, tm);
        return w;
    }

    /**
     * Associate the Topic word with a Topic t
     *
     * @param w
     * @param t
     * @param tm
     * @throws TopicMapException if creating the association fails
     */
    private void associateWord(Topic w, Topic t, Topic s, TopicMap tm) throws TopicMapException {

        String bnSuffix = getBNSuffix();
        
        Topic wordType = ExtractHelper.getOrCreateTopic(getSIBase() + "word",
                "word " + bnSuffix, tm);

        Topic targetType = ExtractHelper.getOrCreateTopic(getSIBase() + "target",
                "target " + bnSuffix, tm);
        
        

        Association a = tm.createAssociation(wordType);

        a.addPlayer(w, wordType);
        a.addPlayer(t, targetType);
        
        if(config.getAssociateScore()){
            
            Topic scoreType = ExtractHelper.getOrCreateTopic(getSIBase() + "score",
                    "score " + bnSuffix, tm);
            a.addPlayer(s, scoreType);

        }
        
    }
    
    abstract protected Object formNeedle(String s);
    
    abstract protected String getBNSuffix();
    abstract protected String getSIBase();

    
    abstract protected float isMatch(Object needle, String haystack);

    
    
}
