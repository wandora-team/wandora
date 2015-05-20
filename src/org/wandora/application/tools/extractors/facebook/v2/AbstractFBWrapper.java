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
 */

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.types.NamedFacebookType;
import com.restfb.types.Page;
import com.restfb.types.Place;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


abstract class AbstractFBWrapper {
    
    private static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";

    private static final String SI_BASE = AbstractFBGraphExtractor.SI_BASE;
    
    protected static Topic getWandoraClassTopic(TopicMap tm)
            throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }
    
    protected static Topic getFacebookTopic(TopicMap tm)
            throws TopicMapException {
        Topic t = getOrCreateTopic(tm, SI_BASE, "Facebook Graph Extractor");
        makeSubclassOf(tm, t, getWandoraClassTopic(tm));
        return t;
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si)
            throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn)
            throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }
    
    protected static Topic getOrCreateType(TopicMap tm, String type)
            throws TopicMapException {
        String urlType = type.toLowerCase().replaceAll(" ", "_");
        return ExtractHelper.getOrCreateTopic(SI_BASE + "type/" + urlType, type + " (Facebook type)", tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass)
            throws TopicMapException {

        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, LANG_SI);
    }
    
    abstract String getType();
    
    abstract String getSIBase();
    
    Topic mapToTopicMap(TopicMap tm) throws TopicMapException {
        String si = tm.makeSubjectIndicatorAsLocator().toString();
        
        return mapToTopicMap(tm, si);
    };
    
    Topic mapToTopicMap(TopicMap tm, String si) throws TopicMapException {
        
        Topic entityTopic = getOrCreateTopic(tm, si);
        Topic entityType = getOrCreateType(tm, getType());
        Topic facebookTopic = getFacebookTopic(tm);
        makeSubclassOf(tm, entityType, facebookTopic);
        
        entityTopic.addType(entityType);
        
        return entityTopic;
    };
    
    protected Topic mapNamedType(NamedFacebookType type, TopicMap tm) throws TopicMapException{
        Topic t;
       // ToDo: resolve appropriate wrapper automatically
        if(type instanceof Place){
            Place place = (Place)type;
            PlaceWrapper placeWrapper = new PlaceWrapper(place);
            t = placeWrapper.mapToTopicMap(tm);
        } else if (type instanceof Page){
            Page page = (Page)type;
            PageWrapper pageWrapper = new PageWrapper(page);
            t = pageWrapper.mapToTopicMap(tm);
        } else {
            StubWrapper stub = new StubWrapper(type);
            t = stub.mapToTopicMap(tm);
        }
        
        return t;
    }
    
    
    protected void associateNamedType(TopicMap tm, NamedFacebookType type, String name, Topic parent, Topic parentType) throws TopicMapException{
        Topic topic = mapNamedType(type, tm);
        Topic topicType = getOrCreateType(tm, name);
        
        Association employerAssoc = tm.createAssociation(topicType);
        employerAssoc.addPlayer(parent, parentType);
        employerAssoc.addPlayer(topic, topicType);
    }
    
    protected static WandoraToolLogger logger;
    static void setLogger(WandoraToolLogger l) {
        logger = l;
    }
    
}
