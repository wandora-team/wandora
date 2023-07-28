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
 * DeleteAssociationsInTopicWithType.java
 *
 * Created on 20.7.2006, 21:41
 *
 */

package org.wandora.application.tools.associations;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;



/**
 *
 * @author akivela
 */
public class DeleteAssociationsInTopicWithType extends DeleteAssociationsInTopic implements WandoraTool {
    
	private static final long serialVersionUID = 1L;
	
	
    private Topic associationType = null;
    

    public DeleteAssociationsInTopicWithType() {
    }
    public DeleteAssociationsInTopicWithType(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        associationType=null;
        associationType=wandora.showTopicFinder("Select type of association to be removed from topics...");                
        if(associationType != null) {
            super.execute(wandora, context);
        }
    }
    
    
    @Override
    public Collection<Association> solveTopicAssociations(Topic topic) throws TopicMapException {
        TopicMap tm = topic.getTopicMap();
        ArrayList<Association> associations = new ArrayList<>();
        Topic at = null;
        for(Locator l : associationType.getSubjectIdentifiers()) {
            at = tm.getTopic(l);
            if(at != null && !at.isRemoved()) {
                associations.addAll( topic.getAssociations(at) );
            }
        }
        return associations;
    }
    
}
