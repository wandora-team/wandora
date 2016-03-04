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


package org.wandora.application.tools.extractors;

import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;
import java.io.*;
import java.net.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;

/**
 * Extractor takes text as input and transforms sentences to associations where
 * words are association players.
 *
 * @author akivela
 */
public class Sentences2Associations  extends AbstractExtractor implements WandoraTool {

    public static boolean ADD_SOURCE_AS_PLAYER = true;
    
    

    public static String SOURCE_SI = "http://wandora.org/si/sentence-source";
    public static String WORD_SI_BASE = "http://wandora.org/si/word/";
    public static String ROLE_SI_BASE = "http://wandora.org/si/word-slot/";
    public static String SENTENCE_SI_BASE = "http://wandora.org/si/sentence";
    public static String ORDER_SI_BASE = "http://wandora.org/si/sentence/";
    public static String SENTENCES_TO_ASSOCIATIONS = "http://wandora.org/si/sentences2associations";


    private URL basePath = null;
    
    
    /** Creates a new instance of Sentences2Associations */
    public Sentences2Associations() {
    }

    @Override
    public String getName() {
        return "Sentence extractor";
    }

    @Override
    public String getDescription() {
        return "Creates a topic for each word and an association for each sentence. Sentence words are association players.";
    }


    @Override
    public boolean useTempTopicMap(){
        return false;
    }


    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select sentence document(s) or directories containing sentence documents!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking sentence documents!";

            case FILE_PATTERN: return ".*";

            case DONE_FAILED: return "Ready. No extractions! %1 sentence file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 sentence file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 sentence file(s) crawled!";

            case LOG_TITLE: return "Sentences to associations extraction Log";
        }
        return "";
    }


    
    @Override
    public boolean browserExtractorConsumesPlainText() {
        return true;
    }

   
    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            basePath = new URL(request.getSource());
        }
        catch(Exception e) { e.printStackTrace(); }
        String s = super.doBrowserExtract(request, wandora);
        basePath = null;
        return s;
    }

    
    

    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;

        try {
            basePath = url;
            URLConnection uc = null;
            if(getWandora() != null) {
                uc = getWandora().wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            _extractTopicsFromStream(url.toExternalForm(), uc.getInputStream(), topicMap);
            basePath = null;
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url\n" + url.toExternalForm(), e);
            takeNap(1000);
        }
        basePath = null;
        return false;
    }





    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null || file.isDirectory()) return false;
        try {
            basePath = file.toURI().toURL();
            _extractTopicsFromStream(file.getPath(), new FileInputStream(file), topicMap);
            basePath = null;
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from file " + file.getName(), e);
            takeNap(1000);
        }
        basePath = null;
        return false;
    }

    
    
    

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        String stringLocator = "http://wandora.org/si/sentences2associations/";
        try {
            _extractTopicsFromStream(stringLocator, new ByteArrayInputStream(str.getBytes()), tm);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from string '" + str + "'.", e);
            takeNap(1000);
        }
        return false;
    }


    public void _extractTopicsFromStream(String locator, InputStream inputStream, TopicMap tm) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer word = null;
            Topic wordTopic = null;
            Topic orderTopic = null;
            Topic roleTopic = null;
            
            int count = 0;
            int associationCount = 0;

            log("Prosessing word stream!");
            int c = reader.read();
            word = new StringBuffer("");
            ArrayList<String> words = new ArrayList();


            while(c != -1) {
                count++;
                setProgress(count / 100);

                while(!isWordDelimiter(c) && !isSentenceDelimiter(c)) {
                    word.append((char) c);
                    c = reader.read();
                }
                if(isWordDelimiter(c) || isSentenceDelimiter(c)) {
                    if(word != null) {
                        String wordStr = word.toString();
                        if(wordStr.trim().length() > 0) {
                            log("Found word '"+wordStr+"'.");
                            words.add(wordStr);
                        }
                        word = new StringBuffer("");
                    }
                }
                if(isSentenceDelimiter(c)) {
                    if(words.size() > 0) {
                        log("Processing sentence with "+words.size()+" words.");
                        associationCount++;
                        Association a = tm.createAssociation(getSentenceTopic(tm));
                        
                        if(ADD_SOURCE_AS_PLAYER) {
                            if(basePath != null) {
                                Topic sourceTopic = getOrCreateTopic(tm, basePath.toExternalForm(), null);
                                Topic sourceRole = getOrCreateTopic(tm, SOURCE_SI, "sentence-source" );
                                if(sourceTopic != null && sourceRole != null) {
                                    a.addPlayer(sourceTopic, sourceRole);
                                }
                            }
                        }
                        
                        orderTopic = getOrCreateTopic(tm, ORDER_SI_BASE+associationCount, "sentence-"+associationCount);
                        roleTopic = getOrCreateTopic(tm, ROLE_SI_BASE+"order", "sentence-order" );
                        if(orderTopic != null) {
                            orderTopic.addType(getSentenceTopic(tm));
                            if(roleTopic != null) {
                                a.addPlayer(orderTopic, roleTopic);
                            }
                        }

                        for(int i=0; i<words.size(); i++) {
                            String w = words.get(i);
                            if(w != null) {
                                wordTopic = getWordTopic(tm, w);
                                String istr = ""+i;
                                if(istr.length() == 1) istr = "0"+istr;
                                roleTopic = getOrCreateTopic(tm, ROLE_SI_BASE+i, "slot-"+istr );
                                if(wordTopic != null && roleTopic != null) {
                                    a.addPlayer(wordTopic, roleTopic);
                                }
                            }
                        }
                        words = new ArrayList();
                    }
                }
                while((isWordDelimiter(c) || isSentenceDelimiter(c)) && c != -1) {
                    c = reader.read();
                }
            }
            log("Total "+associationCount+" sentence associations created!");
            log("Ok");
        }
        catch(Exception e) {
            log(e);
        }
    }




    public boolean isWordDelimiter(int c) {
        if(c == ' ') return true;
        if(c == ',') return true;
        else return false;
    }


    public boolean isSentenceDelimiter(int c) {
        if(c == '.') return true;
        if(c == '\n') return true;
        if(c == -1) return true;
        else return false;
    }



    public Topic getSentenceTopic(TopicMap tm) throws TopicMapException {
        Topic s = getOrCreateTopic(tm, SENTENCE_SI_BASE, "sentence");

        Topic stype = getSentenceTypeTopic(tm);
        ExtractHelper.makeSubclassOf(s, stype, tm);

        return s;
    }


    public Topic getWordTopic(TopicMap tm, String word) throws TopicMapException {
        String encWord = word;
        try { encWord = URLEncoder.encode(word, "UTF-8"); }
        catch(Exception e) { /* PASS */ }
        Topic wordTopic = getOrCreateTopic(tm, WORD_SI_BASE+encWord, word);
        Topic wordTypeTopic = getOrCreateTopic(tm, WORD_SI_BASE, "word");
        wordTopic.addType(wordTypeTopic);

        Topic stype = getSentenceTypeTopic(tm);
        ExtractHelper.makeSubclassOf(wordTypeTopic, stype, tm);
        return wordTopic;
    }


    
    public Topic getSentenceTypeTopic(TopicMap tm) throws TopicMapException {
        Topic s = getOrCreateTopic(tm, SENTENCES_TO_ASSOCIATIONS, "Sentences to associations");
        Topic wc = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
        ExtractHelper.makeSubclassOf(s, wc, tm);
        return s;
    }
    

    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------




    public static final String[] contentTypes=new String[] { "text/plain" };
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
}
