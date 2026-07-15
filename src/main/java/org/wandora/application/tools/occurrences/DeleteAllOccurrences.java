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


package org.wandora.application.tools.occurrences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class DeleteAllOccurrences extends AbstractWandoraTool implements WandoraTool {
    
	
	private static final long serialVersionUID = 1L;
	
    
    public DeleteAllOccurrences(Context proposedContext) {
        this.setContext(proposedContext);
    }
    
    

    @Override
    public String getName() {
        return "Delete all occurrences";
    }


    @Override
    public String getDescription() {
        return "Delete all occurrences of given topic.";
    }

    
    

    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Object contextSource = context.getContextSource();

        Iterator topics = getContext().getContextObjects();
        Topic topic = null;
        int count = 0;

        ArrayList<Topic> allOccurrenceTypes = new ArrayList<Topic>();

        if(topics!= null && topics.hasNext()) {
            while(topics.hasNext() && !forceStop()) {
                topic = (Topic) topics.next();
                if(topic != null && !topic.isRemoved()) {
                    Collection<Topic> types=topic.getDataTypes();
                    if(types.isEmpty()) continue;
                    for(Topic type : types) {
                        try {
                            Collection<Topic> scopes = new ArrayList<>(topic.getData(type).keySet());
                            for(Topic scope : scopes) {
                                topic.removeData(type, scope);
                                count++;
                            }
                            count++;
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                }
            }
            
        }
    }

    
}
