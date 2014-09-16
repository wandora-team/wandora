/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public abstract class AbstractUmbelRelationExtractor extends AbstractUmbelExtractor {
    

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
                            logApiRequest(s);
                            Topic baseConceptTopic = null; // getConceptTopic(getUmbelConceptURI(s), s, 0, topicMap);
                            ArrayList<Topic> conceptTopics = new ArrayList();
                            HashMap<Topic,Integer> distances = new HashMap();
                            Iterator concepts = response.keys();
                            while(concepts.hasNext() && !forceStop()) {
                                Object concept = concepts.next();
                                JSONObject conceptDetails = response.getJSONObject(concept.toString());
                                if(conceptDetails != null) {
                                    String label = robustGet(conceptDetails, "pref-label");
                                    int distance = robustGetInt(conceptDetails, "distance");
                                    if(distance == 1) {
                                        baseConceptTopic = getConceptTopic(concept.toString(), label, topicMap);
                                    }
                                    if(getOnlyImmediateNeighbours) {
                                        if(distance == 2) {
                                            Topic conceptTopic = getConceptTopic(concept.toString(), label, topicMap);
                                            conceptTopics.add(conceptTopic);
                                            distances.put(conceptTopic, distance);
                                        }
                                    }
                                    else if(distance > filterDistancesBelow) {
                                        Topic conceptTopic = getConceptTopic(concept.toString(), label, topicMap);
                                        conceptTopics.add(conceptTopic);
                                        distances.put(conceptTopic, distance);
                                    }
                                }
                            }
                            if(baseConceptTopic != null && !baseConceptTopic.isRemoved() && conceptTopics.size() > 0) {
                                for(Topic conceptTopic : conceptTopics) {
                                    if(forceStop()) break;
                                    if(conceptTopic != null && !conceptTopic.isRemoved()) {
                                        Association a = topicMap.createAssociation(getAssociationType(topicMap));
                                        if(a != null) {
                                            a.addPlayer(conceptTopic, getRoleTopicForConcept(topicMap));
                                            a.addPlayer(baseConceptTopic, getRoleTopicForBaseConcept(topicMap));
                                            if(ADD_DISTANCE_AS_PLAYER) {
                                                int distance = distances.get(conceptTopic);
                                                if(distance > 0) {
                                                    a.addPlayer(getDistanceTopic(distance,topicMap), getDistanceTypeTopic(topicMap));
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
    
    
    
    public abstract void logApiRequest(String str);
    public abstract Topic getAssociationType(TopicMap topicMap) throws TopicMapException;
    public abstract Topic getRoleTopicForConcept(TopicMap topicMap) throws TopicMapException;
    public abstract Topic getRoleTopicForBaseConcept(TopicMap topicMap) throws TopicMapException;
    
    
    
    
}
