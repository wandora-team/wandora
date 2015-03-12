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
 */
package org.wandora.application.tools.extractors.word;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.io.IOUtils;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.application.tools.extractors.word.SimpleWordConfiguration.Bools;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
public class SimpleWordMatchingExtractor extends AbstractExtractor {

    private final String SI_BASE = "http://wandora.org/si/simple-word/";
    private final String WORD_SI = SI_BASE + "word/";
    private final String BN_SUFFIX = "(simple word extractor)";
    private Topic baseTopic = null;
    private SimpleWordConfiguration config;

    @Override
    public String getName(){
        return "Simple Word Matching Extractor";
    }
    @Override
    public String getDescription(){
        return "Matches given words and regular expressions to topic data and"
                + "creates associations between matched topics and given words.";
    }
    
    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void configure(Wandora w, Options o, String p) throws TopicMapException {

        SimpleWordConfigurationDialog d = new SimpleWordConfigurationDialog(w);
        d.openDialog();

        if (d.wasAccepted()) {
            config = d.getConfiguration();
        }

    }

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
    private boolean handleWordList(List<String> words, TopicMap tm)
            throws TopicMapException {

        log("Handling " + words.size() + " words");

        if (config == null) {
            config = new SimpleWordConfiguration();
        }

        baseTopic = ExtractHelper.getOrCreateTopic(SI_BASE, "Simple Word Extractor", tm);

        makeSubclassOfWandoraClass(baseTopic, tm);

        HashMap<String, Set<Topic>> mapping = new HashMap<>();



        for (String word : words) {
            try {
                log("Finding topics for \"" + word + "\"");
                Set<Topic> topics = solveTopics(word, tm);
                mapping.put(word, topics);
            } catch (Exception e) {
                log(e.getMessage());
            }

        }

        for (String word : words) {
            Set<Topic> topics = mapping.get(word);
            if (topics == null) {
                continue; //In case solveTopics failed for this word
            }
            try {
                Topic wt = createWordTopic(word, tm);

                log("Associating " + topics.size() + " topics with \"" + word + "\"");

                for (Topic t : topics) {
                    associateWord(wt, t, tm);
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
    private Set<Topic> solveTopics(String word, TopicMap tm) {

        Context c = getContext();
        Iterator contextIterator = c.getContextObjects();

        Set<Topic> topics = new HashSet<>();

        Object needle;

        if (config.bool(Bools.REGEX)) {
            try {
                needle = Pattern.compile(word);
            } catch (PatternSyntaxException pse) {
                throw new IllegalArgumentException("Invalid regex syntax for "
                        + "pattern \"" + word + "\": " + pse.getMessage());
            }
        } else if (!config.bool(Bools.CASE_SENSITIVE)) {
            needle = word.toLowerCase();
        } else {
            needle = word;
        }

        while (contextIterator.hasNext()) {
            Object o = contextIterator.next();
            if (!(o instanceof Topic)) {
                continue;
            }

            Topic t = (Topic) o;


            try {
                // Check base name
                if (config.bool(Bools.BASE_NAME) && isMatch(needle, t.getBaseName())) {
                    topics.add(t);
                }

                // Check variant names
                if (config.bool(Bools.VARIANT_NAME)) {
                    for (Set<Topic> scope : t.getVariantScopes()) {
                        String variant = t.getVariant(scope);

                        if (!config.bool(Bools.CASE_SENSITIVE)) {
                            variant = variant.toLowerCase();
                        }

                        if (isMatch(needle, variant)) {
                            topics.add(t);
                        }
                    }
                }


                // Check instance data
                if (config.bool(Bools.BASE_NAME)) {
                    for (Topic type : t.getDataTypes()) {

                        Enumeration<String> data = t.getData(type).elements();

                        while (data.hasMoreElements()) {

                            String datum = data.nextElement();

                            if (!config.bool(Bools.CASE_SENSITIVE)) {
                                datum = datum.toLowerCase();
                            }

                            if (isMatch(needle, datum)) {
                                topics.add(t);
                            }
                        }
                    }
                }

            } catch (TopicMapException tme) {
                log(tme.getMessage());
            }



        }

        return topics;
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
        String si = WORD_SI + word;
        String bn = word + " " + BN_SUFFIX;
        Topic w = ExtractHelper.getOrCreateTopic(si, bn, tm);
        ExtractHelper.makeSubclassOf(w, baseTopic, tm);
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
    private void associateWord(Topic w, Topic t, TopicMap tm) throws TopicMapException {

        Topic wordType = ExtractHelper.getOrCreateTopic(WORD_SI + "word",
                "word " + BN_SUFFIX, tm);

        Topic targetType = ExtractHelper.getOrCreateTopic(WORD_SI + "target",
                "target " + BN_SUFFIX, tm);

        Association a = tm.createAssociation(wordType);

        a.addPlayer(w, wordType);
        a.addPlayer(t, targetType);

    }

    private boolean isMatch(Object needle, String haystack) {

        if (needle instanceof Pattern) {

            Pattern p = (Pattern) needle;

            return p.matcher(haystack).find();

        } else if (needle instanceof String) {

            String s = (String) needle;

            int index = haystack.indexOf(s);
            boolean isMatch = (index != -1);

            if (config.bool(Bools.MATCH_WORDS) && isMatch) {

                if (index > 0
                        && Character.isLetterOrDigit(haystack.charAt(index - 1))) {
                    isMatch = false;
                }

                if (index + s.length() < haystack.length()
                        && Character.isLetterOrDigit(haystack.charAt(index + s.length()))) {
                    isMatch = false;
                }

            }

            return isMatch;

        } else {
            throw new UnsupportedOperationException("Match operation failed.");
        }

    }
}
