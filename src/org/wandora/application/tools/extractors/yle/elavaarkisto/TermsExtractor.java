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
 * TermsExtractor.java
 *
 * Created on 2015-04-30
 */

package org.wandora.application.tools.extractors.yle.elavaarkisto;


import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 * Finnish broadcasting company (YLE) has published Elava arkisto 
 * metadata under the CC-BY-SA 4.0 license. Wandora features a set of extractors
 * that transfer the published metadata to Topic Maps. This extractor is one of
 * these extractor. The metadata and it's documentation is available at 
 * 
 * http://elavaarkisto.kokeile.yle.fi/data/
 * http://elavaarkisto.kokeile.yle.fi/data/terms.json
 *
 * @author akivela
 */


public class TermsExtractor extends AbstractElavaArkistoExtractor {
    
    public static boolean EXTRACT_TITLE = true;
    public static boolean EXTRACT_TYPE = true;
    public static boolean EXTRACT_KEYWORD = true;
    public static boolean EXTRACT_FIRST_AND_LASTNAME = true;
    public static boolean EXTRACT_EXACT_MATCH = true;
    public static boolean EXTRACT_ALSO_KNOWN_AS = true;
    public static boolean EXTRACT_DISAMBIGUATION_HINT = false;
    
    
    

    @Override
    public String getName() {
        return "YLE Elava arkisto terms extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "YLE Elava arkisto terms extractor reads JSON feeds like http://elavaarkisto.kokeile.yle.fi/data/terms.json";
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        try {
            return _extractTopicsFrom(new JSONArray(str), tm);
        }
        catch(Exception e) {
            return _extractTopicsFrom(new JSONObject(str), tm); 
        }
    }
    
    
    public boolean _extractTopicsFrom(JSONArray json, TopicMap tm) throws Exception {
        setProgressMax(json.length());
        for(int i=0; i<json.length() && !forceStop(); i++) {
            _extractTopicsFrom(json.getJSONObject(i), tm);
            setProgress(i);
        }
        return true;
    }
    
    

    public boolean _extractTopicsFrom(JSONObject json, TopicMap tm) throws Exception {
        if(json.has("id")) {
            String id = json.getString("id");
            Topic termTopic = getElavaArkistoTermTopic(id, tm);
            Topic termType = getElavaArkistoTermType(tm);
            if(EXTRACT_TITLE && json.has("title")) {
                String basename = null;
                JSONObject titles = json.getJSONObject("title");
                for(Iterator i = titles.keys(); i.hasNext(); ) {
                    Object langKey = i.next();
                    String lang = langKey.toString();
                    String title = titles.getString(lang);
                    if("und".equalsIgnoreCase(lang)) {
                        // Set language independent display name
                        termTopic.setDisplayName(null, title);
                    }
                    else {
                        termTopic.setDisplayName(lang, title);
                    }
                    
                    
                    if("und".equalsIgnoreCase(lang) || "fi".equalsIgnoreCase(lang)) {
                        basename = title;
                    }
                    if("sv".equalsIgnoreCase(lang) && basename == null) {
                        basename = title;
                    }
                }
                if(basename != null) {
                    termTopic.setBaseName(basename+" ("+id+")");
                }
            }
            if(EXTRACT_TYPE && json.has("type")) {
                JSONArray types = json.getJSONArray("type");
                Topic termTypeType = getElavaArkistoTermTypeType(tm);
                for(int i=0; i<types.length(); i++) {
                    String type = types.getString(i);
                    Topic termTypeTopic = getElavaArkistoTermTypeTopic(type, tm);
                    if(termTypeTopic != null) {
                        Association a = tm.createAssociation(termTypeType);
                        a.addPlayer(termTopic, termType);
                        a.addPlayer(termTypeTopic, termTypeType);
                    }
                }
            }
            if(EXTRACT_KEYWORD && json.has("keyword")) {
                String keyword = json.getString("keyword");
                Topic keywordTypeTopic = getElavaArkistoKeywordType(tm);
                Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                if(keywordTypeTopic != null && langIndependent != null) {
                    termTopic.setData(keywordTypeTopic, langIndependent, keyword);
                }
            }
            if(EXTRACT_FIRST_AND_LASTNAME && json.has("firstname")) {
                String firstname = json.getString("firstname");
                Topic firstnameTypeTopic = getElavaArkistoFirstnameType(tm);
                Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                if(firstnameTypeTopic != null && langIndependent != null) {
                    termTopic.setData(firstnameTypeTopic, langIndependent, firstname);
                }
            }
            if(EXTRACT_FIRST_AND_LASTNAME && json.has("lastname")) {
                String lastname = json.getString("lastname");
                Topic lastnameTypeTopic = getElavaArkistoLastnameType(tm);
                Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                if(lastnameTypeTopic != null && langIndependent != null) {
                    termTopic.setData(lastnameTypeTopic, langIndependent, lastname);
                }
            }
            if(EXTRACT_DISAMBIGUATION_HINT && json.has("disambiguationHint")) {
                
            }
            if(EXTRACT_EXACT_MATCH && json.has("exactMatch")) {
                JSONArray exactMatches = json.getJSONArray("exactMatch");
                if(exactMatches != null) {
                    for(int i=0; i<exactMatches.length(); i++) {
                        JSONObject exactMatch = exactMatches.getJSONObject(i);
                        if(exactMatch != null) {
                            if(exactMatch.has("externalId")) {
                                String externalId = exactMatch.getString("externalId");
                                Topic exactMatchTopic = getElavaArkistoExactMatchTopic(externalId, tm);
                                Topic exactMatchTypeTopic = getElavaArkistoExactMatchType(tm);

                                if(exactMatchTypeTopic != null) {
                                    Association a = tm.createAssociation(exactMatchTypeTopic);
                                    a.addPlayer(termTopic, termType);
                                    a.addPlayer(exactMatchTopic, exactMatchTypeTopic);
                                }
                                
                                if(exactMatch.has("label")) {
                                    String label = exactMatch.getString("label");
                                    if(label != null) {
                                        exactMatchTopic.setBaseName(label + " ("+externalId + ")");
                                        exactMatchTopic.setDisplayName(null, label);
                                    }
                                }
                                if(exactMatch.has("disambiguation")) {
                                    String disambiguation = exactMatch.getString("disambiguation");
                                    Topic disambiguationType = getElavaArkistoExactMatchDisambiguationType(tm);
                                    Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                                    if(disambiguationType != null && langIndependent != null) {
                                        exactMatchTopic.setData(disambiguationType, langIndependent, disambiguation);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(EXTRACT_ALSO_KNOWN_AS && json.has("alsoKnownAs")) {
                String alsoKnownAs = json.getString("alsoKnownAs");
                Topic alsoKnownAsTypeTopic = getElavaArkistoAlsoKnownAsType(tm);
                Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                if(alsoKnownAsTypeTopic != null && langIndependent != null) {
                    termTopic.setData(alsoKnownAsTypeTopic, langIndependent, alsoKnownAs);
                }
            }
        }
        return true;
    }
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    

    public static final String ELAVA_ARKISTO_TERM_SI = ELAVA_ARKISTO_SI+"/term";
    public static final String ELAVA_ARKISTO_TERM_TYPE_SI = ELAVA_ARKISTO_SI+"/term-type";
    public static final String ELAVA_ARKISTO_TERM_KEYWORD_SI = ELAVA_ARKISTO_SI+"/term-keyword";
    public static final String ELAVA_ARKISTO_TERM_LASTNAME_SI = ELAVA_ARKISTO_SI+"/term-lastname";
    public static final String ELAVA_ARKISTO_TERM_FIRSTNAME_SI = ELAVA_ARKISTO_SI+"/term-firstname";
    public static final String ELAVA_ARKISTO_TERM_ALSOKNOWNAS_SI = ELAVA_ARKISTO_SI+"/term-also-known-as";
    
    
    public static final String ELAVA_ARKISTO_TERM_EXACT_MATCH_SI = ELAVA_ARKISTO_SI+"/exact-match";
    public static final String ELAVA_ARKISTO_TERM_EXACT_MATCH_DISAMBIGUATION_SI = ELAVA_ARKISTO_SI+"/exact-match-disambiguation";
    
    public static final String ELAVA_ARKISTO_LASTNAME_TYPE_SI = ELAVA_ARKISTO_SI+"/lastname";
    public static final String ELAVA_ARKISTO_FIRSTNAME_TYPE_SI = ELAVA_ARKISTO_SI+"/firstname";
    
    public static final String ELAVA_ARKISTO_ALSOKNOWNAS_TYPE_SI = ELAVA_ARKISTO_SI+"/also-known-as";
    public static final String ELAVA_ARKISTO_KEYWORD_TYPE_SI = ELAVA_ARKISTO_SI+"/keyword";
    
    // --------------------------------------------------------------- TYPES ---

    

    
    
    public Topic getElavaArkistoKeywordType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_KEYWORD_TYPE_SI, "Elava-arkisto keyword", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoAlsoKnownAsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_ALSOKNOWNAS_TYPE_SI, "Elava-arkisto also known as", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoLastnameType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_LASTNAME_TYPE_SI, "Elava-arkisto lastname", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoFirstnameType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_FIRSTNAME_TYPE_SI, "Elava-arkisto firstname", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoExactMatchDisambiguationType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_TERM_EXACT_MATCH_DISAMBIGUATION_SI, "Elava-arkisto exact match disambiguation", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoExactMatchType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_TERM_EXACT_MATCH_SI, "Elava-arkisto exact match", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoExactMatchTopic(String id, TopicMap tm) throws TopicMapException {
        String si = id;
        Topic topic = null;
        try {
            topic = tm.getTopic(si);
            if(topic == null) {
                topic = tm.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }
    
    
    
    public Topic getElavaArkistoTermType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_TERM_SI, "Elava-arkisto term", getElavaArkistoType(tm), tm);
        return type;
    }
    
   
    
    public Topic getElavaArkistoTermTopic(String id, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_TERM_SI+"/"+urlEncode(id);
        Topic termTopic = null;
        try {
            termTopic = tm.getTopic(si);
            if(termTopic == null) {
                termTopic = tm.createTopic();
                termTopic.addSubjectIdentifier(new Locator(si));
                termTopic.addType(getElavaArkistoTermType(tm));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return termTopic;
    }
    
    
    public Topic getElavaArkistoTermTypeType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_TERM_TYPE_SI, "Elava-arkisto term type", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoTermTypeTopic(String type, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_TERM_TYPE_SI+"/"+urlEncode(type);
        Topic termTypeTopic = null;
        try {
            termTypeTopic = tm.getTopic(si);
            if(termTypeTopic == null) {
                termTypeTopic = tm.createTopic();
                termTypeTopic.addSubjectIdentifier(new Locator(si));
                termTypeTopic.setBaseName(type+" (term type)");
                termTypeTopic.setDisplayName(null, type);
                termTypeTopic.addType(getElavaArkistoTermTypeType(tm));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return termTypeTopic;
    }
    
    
}
