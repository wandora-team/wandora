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
 * MakeSuperclassOf.java
 *
 * Created on 28. heinäkuuta 2006, 15:41
 *
 */

package org.wandora.application.tools.associations;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;

/**
 * 
 * @see MakeSubclassOf
 * @author akivela
 */
public class MakeSuperclassOf extends AbstractWandoraTool implements WandoraTool {
    private boolean requiresRefresh = false;
    
    
    public MakeSuperclassOf() {
    }
    public MakeSuperclassOf(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Make superclass of";
    }
    @Override
    public String getDescription() {
        return "Makes context topic superclass of the topic open in topic panel.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        requiresRefresh = false;
        Iterator topics = context.getContextObjects();
        int count = 0;
        boolean shouldContinue = true;
        
        try {
            Topic subTopic = null;
            Topic superTopic = admin.getOpenTopic();
            TopicMap topicmap = superTopic.getTopicMap();
            Association newSuperAssociation = null;
            
            Topic superClassType = getOrCreateTopic("http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass", topicmap);
            Topic superClassRole = getOrCreateTopic("http://www.topicmaps.org/xtm/1.0/core.xtm#subclass", topicmap);
            Topic subClassRole = getOrCreateTopic("http://www.topicmaps.org/xtm/1.0/core.xtm#superclass", topicmap);

            if(superTopic == null) return;

            setDefaultLogger();

            if(topics != null && topics.hasNext()) {
                while(topics.hasNext() && shouldContinue && !forceStop()) {
                    subTopic = (Topic) topics.next();
                    if(subTopic != null && !subTopic.isRemoved()) {

                        log("Making topic '"+getTopicName(subTopic)+"' superclass of '"+getTopicName(superTopic)+"'.");
                        
                        requiresRefresh = true;
                        newSuperAssociation = topicmap.createAssociation(superClassType);
                        newSuperAssociation.addPlayer( superTopic, superClassRole );
                        newSuperAssociation.addPlayer( subTopic, subClassRole );
                    }
                }
            }
           setState(WAIT);
        }
        catch(Exception e) {
            log(e);
        }
        
    }
    
    
    
    public Topic getOrCreateTopic(String locator, TopicMap topicmap) throws TopicMapException {
        Topic topic = null;
        if(locator != null) {
            topic = topicmap.getTopic(locator);
        } 
        if(topic == null) {
            topic = topicmap.createTopic();
            topic.addSubjectIdentifier(new Locator(locator));
        }
        return topic;
    }
}
