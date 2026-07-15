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
 */
package org.wandora.application.tools.exporters.iiifexport;

import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicHashSet;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class SelectionInstancesIIIFBuilder extends SimpleSelectionIIIFBuilder {
    
    @Override
    public String getBuilderName(){
        return "Selection Instances Builder";
    }
    
 
    @Override
    protected void processTopics(Wandora wandora,Context context, Sequence sequence,IIIFExport tool) throws TopicMapException {
        TopicHashSet processed=new TopicHashSet();
        
        Iterator iter=context.getContextObjects();
        while(iter.hasNext()){
            Object o=iter.next();
            if(!(o instanceof Topic)) continue;
            Topic t=(Topic)o;
            for(Topic instance : t.getTopicMap().getTopicsOfType(t)){
                if(processed.add(instance)){
                    processTopic(instance, sequence, tool);            
                }
            }            
        }        
    }
    
}
