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
 * MakeAssociationWithClassInstance.java
 *
 * Created on 25. toukokuuta 2012
 *
 */

package org.wandora.application.tools.associations;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;


/**
 * <p>
 * <code>MakeAssociationWithClassInstance</code> transforms default class-instance
 * relations to any associations.
 * </p>
 * 
 * @author akivela
 */



public class MakeAssociationWithClassInstance extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;
	
	
	private boolean deleteClassInstance = false;
    private boolean requiresRefresh = false;
    

    public MakeAssociationWithClassInstance() {
    }
    
    public MakeAssociationWithClassInstance(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Make association out of class-instance relation";
    }
    @Override
    public String getDescription() {
        return "Transforms class-instance relations to any associations.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {   
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
                        
            Topic associationType=wandora.showTopicFinder("Select association type...");                
            if(associationType == null) return;

            Topic instanceRole=wandora.showTopicFinder("Select role topic for instances...");                
            if(instanceRole == null) return;

            Topic classRole=wandora.showTopicFinder("Select role topic for classes...");                
            if(classRole == null) return;
            
            setDefaultLogger();
            setLogTitle("Making associations from class-instance relations");
            log("Making associations from default class-instance relations");
            
            Topic topic = null;
            int progress = 0;
            TopicMap map = wandora.getTopicMap();
            Association a = null;

            ArrayList<Topic> dtopics = new ArrayList<Topic>();
            while(topics.hasNext() && !forceStop()) {
                dtopics.add((Topic) topics.next());
            }
            topics = dtopics.iterator();
            
            // Iterate through selected topics...
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        progress++;
                        hlog("Inspecting topic '"+getTopicName(topic)+"'");
                        
                        Collection<Topic> types = new ArrayList<Topic>(topic.getTypes());

                        // Ok, if topic has types...
                        if(!types.isEmpty()) {
                            for(Topic type : types) {
                                if(forceStop()) break;
                                if(type != null && !type.isRemoved()) {
                                    log("Processing types of topic '"+getTopicName(topic)+"'");

                                    requiresRefresh = true;

                                    // Creating new association between instance and class topics
                                    hlog("Creating association between '"+getTopicName(topic)+"' and '"+getTopicName(type)+"'.");
                                    a = map.createAssociation(associationType);
                                    a.addPlayer(type, classRole);
                                    a.addPlayer(topic, instanceRole);

                                    // Finally deleting class-instance relation if...
                                    if(deleteClassInstance) {
                                        topic.removeType(type);
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    

}
