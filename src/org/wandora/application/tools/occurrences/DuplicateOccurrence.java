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
 *
 */

package org.wandora.application.tools.occurrences;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;




/**
 *
 * @author akivela
 */
public class DuplicateOccurrence  extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	
	private Topic occurrenceType = null;
    private Topic masterTopic = null;
    
    
    /** Creates a new instance of DuplicateOccurrence */
    public DuplicateOccurrence() {
        this.occurrenceType=null;
    }
    public DuplicateOccurrence(Context proposedContext) {
        this.setContext(proposedContext);
        this.occurrenceType=null;
    }
    public DuplicateOccurrence(Context proposedContext, Topic occurrenceType) {
        this.setContext(proposedContext);
        this.occurrenceType=occurrenceType;
    }
    public DuplicateOccurrence(Topic occurrenceType) {
        this.occurrenceType=occurrenceType;
    }
    public DuplicateOccurrence(Context proposedContext, Topic occurrenceType, Topic masterTopic) {
        this.setContext(proposedContext);
        this.occurrenceType=occurrenceType;
        this.masterTopic=masterTopic;
    }
    public DuplicateOccurrence(Topic occurrenceType, Topic masterTopic) {
        this.occurrenceType=occurrenceType;
        this.masterTopic=masterTopic;
    }
    

    @Override
    public String getName() {
        return "Duplicate occurrence";
    }


    @Override
    public String getDescription() {
        return "Copy given occurrences to new occurrence type.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Object contextSource = context.getContextSource();
        
        if(contextSource instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) contextSource;
            ot.duplicateType();
        }
        else {
            Iterator<Topic> topics = null;
            if(masterTopic != null) {
                ArrayList<Topic> topicArray = new ArrayList<>();
                topicArray.add(masterTopic);
                topics = topicArray.iterator();
            }
            else {
                topics = context.getContextObjects();
            }
            
            if(topics != null) {
                Topic type = occurrenceType;
                if(type == null || type.isRemoved()) {
                    type = wandora.showTopicFinder("Select occurrence type to be duplicated");
                }
                if(type != null && !type.isRemoved()) {
                    Topic newType = wandora.showTopicFinder("Select new occurrence type");
                    if(newType != null && !newType.isRemoved()) {
                        while(topics.hasNext()) {
                            Topic topic = topics.next();
                            if(topic != null && !topic.isRemoved()) {
                                Hashtable<Topic,String> os = topic.getData(occurrenceType);
                                if(os != null && !os.isEmpty()) {
                                    for(Topic scope : os.keySet()) {
                                        if(scope != null && !scope.isRemoved()) {
                                            String occurrenceText = os.get(scope);
                                            topic.setData(newType, scope, occurrenceText);
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
