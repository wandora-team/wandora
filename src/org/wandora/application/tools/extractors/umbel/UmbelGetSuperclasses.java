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
 */


package org.wandora.application.tools.extractors.umbel;


import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class UmbelGetSuperclasses extends AbstractUmbelRelationExtractor {

	private static final long serialVersionUID = 1L;

	public static final String API_URL = "http://umbel.org/ws/super-classes/ext/";

    
    

    @Override
    public String getName(){
        return "Superclass Umbel concept extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extract superclass concepts from Umbel.";
    }
    
    
    @Override
    public String getApiRequestUrlFor(String str) {
        return API_URL + urlEncode(str);
    }

    
    @Override
    public void logApiRequest(String str) {
        log("Getting superclass concepts of '"+str+"'.");
    }
    

    @Override
    public Topic getAssociationType(TopicMap topicMap) throws TopicMapException {
        return getSuperclassSubclassTypeTopic(topicMap);
    }
    
    @Override
    public Topic getRoleTopicForConcept(TopicMap topicMap) throws TopicMapException {
        return getSuperclassTypeTopic(topicMap);
    }
    
    @Override
    public Topic getRoleTopicForBaseConcept(TopicMap topicMap) throws TopicMapException {
        return getSubclassTypeTopic(topicMap);
    }


    
}
