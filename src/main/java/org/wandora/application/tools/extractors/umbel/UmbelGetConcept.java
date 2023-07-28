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
 */


package org.wandora.application.tools.extractors.umbel;


import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.Tuples;

/**
 *
 * @author akivela
 */


public class UmbelGetConcept extends AbstractUmbelExtractor {

	private static final long serialVersionUID = 1L;

	public static final String API_URL = "http://umbel.org/ws/concept/";

    
    @Override
    public String getName(){
        return "Umbel concept extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extract concepts from Umbel knowledge graph.";
    }
    
    @Override
    public String getApiRequestUrlFor(String str) {
        return API_URL + urlEncode(str);
    }
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        if(str != null && topicMap != null) {
            String[] strs = str.split(UMBEL_CONCEPT_STRING_SPLITTER);
            if(strs != null && strs.length > 0) {
                for(String s : strs) {
                    if(s != null && s.length() > 0) {
                        String spiRequestUrl = getApiRequestUrlFor(s);
                        System.out.println("Trying: " + spiRequestUrl);
                        JSONObject response = performRequest(spiRequestUrl, s);
                        if(response != null) {
                            log("Getting concept '"+s+"'.");
                            if(response.has("resultset")) {
                                JSONObject resultSet = response.getJSONObject("resultset");
                                if(resultSet.has("subject")) {
                                    JSONArray subjects = resultSet.getJSONArray("subject");
                                    for(int i=0; i<subjects.length(); i++) {
                                        JSONObject subject = subjects.getJSONObject(i);
                                        String uri = robustGet(subject, "uri");
                                        if(uri != null && uri.length() > 0) {
                                            
                                            Topic conceptTopic = getConceptTopic(uri, topicMap);

                                            if(subject.has("predicate")) {
                                                JSONArray predicates = subject.getJSONArray("predicate");
                                                for(int j=0; j<predicates.length(); j++) {
                                                    JSONObject predicate = predicates.getJSONObject(j);
                                                    Iterator keys = predicate.keys();
                                                    while(keys.hasNext()) {
                                                        Object key = keys.next();
                                                        if(key != null) {
                                                            String keyStr = key.toString();
                                                            if(UMBEL_PREF_LABEL_URI.equalsIgnoreCase(keyStr)) {
                                                                handleOccurrencePredicate(conceptTopic, predicate, keyStr, topicMap);
                                                            }
                                                            else if(UMBEL_ALT_LABEL_URI.equalsIgnoreCase(keyStr)) {
                                                                handleOccurrencePredicate(conceptTopic, predicate, keyStr, topicMap);
                                                            }
                                                            else if(UMBEL_DEFINITION_URI.equalsIgnoreCase(keyStr)) {
                                                                handleOccurrencePredicate(conceptTopic, predicate, keyStr, topicMap);
                                                            }
                                                            else {
                                                                handlePredicate(conceptTopic, predicate, keyStr, topicMap);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    
    private void handlePredicate(Topic conceptTopic, JSONObject predicate, String predicateURI, TopicMap topicMap) {
        try {
            JSONObject relatedJSON = predicate.getJSONObject(predicateURI);
            String relatedConceptURI = relatedJSON.getString("uri");
            if(relatedConceptURI != null && relatedConceptURI.length() > 0) {
                Topic relatedConceptTopic = getConceptTopic(relatedConceptURI, topicMap);
                Tuples.T3<Topic,Topic,Topic> associationTopics = getAssociationTopicsForUmbelPredicate(predicateURI, topicMap);
                if(associationTopics != null) {
                    Association a = topicMap.createAssociation(associationTopics.e1);
                    a.addPlayer(relatedConceptTopic, associationTopics.e2);
                    a.addPlayer(conceptTopic, associationTopics.e3);
                }
                if(relatedJSON.has("reify")) {
                    JSONArray detailsJSON = relatedJSON.getJSONArray("reify");
                    for(int i=0; i<detailsJSON.length(); i++) {
                        try {
                            JSONObject detailJSON = detailsJSON.getJSONObject(i);
                            if(detailJSON.has("type") && detailJSON.has("value")) {
                                String type = detailJSON.getString("type");
                                String value = detailJSON.getString("value");
                                if(type != null && value != null && type.length()>0 && value.length() > 0) {
                                    if(type.equalsIgnoreCase("iron:prefLabel")) {
                                        Topic occurrenceTypeTopic = getTopic(UMBEL_PREF_LABEL_URI, topicMap);
                                        relatedConceptTopic.setData(occurrenceTypeTopic, getTopic(XTMPSI.getLang(LANG), topicMap), value);
                                    }
                                }
                            }
                        }
                        catch(Exception de) {
                            log(de);
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    private void handleOccurrencePredicate(Topic conceptTopic, JSONObject predicate, String key, TopicMap topicMap) {
        try {
            String value = predicate.getString(key);
            if(value != null && value.length() > 0) {
                Topic occurrenceTypeTopic = getTopic(key, topicMap);
                conceptTopic.setData(occurrenceTypeTopic, getTopic(XTMPSI.getLang(LANG), topicMap), value);
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    

    

    
}
