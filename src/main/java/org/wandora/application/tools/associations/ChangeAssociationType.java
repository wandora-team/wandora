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
 * ChangeAssociationType.java
 *
 * Created on 10. elokuuta 2006, 16:02
 *
 */

package org.wandora.application.tools.associations;


import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;



/**
 * <p>
 * Tool is used to changes association type of given associations. Multiple
 * association can be modified at once. Tool requests new type topic.
 * </p>
 * 
 * @see ChangeAssociationRole
 * @author akivela
 */
public class ChangeAssociationType extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	
	private boolean requiresRefresh = false;

    public ChangeAssociationType() {
        setContext(new AssociationContext());
    }
    public ChangeAssociationType(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Change association type";
    }
    @Override
    public String getDescription() {
        return "Changes association type topics.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        requiresRefresh = false;
        Iterator<Association> associations = context.getContextObjects();
        Association association = null;
        int count = 0;
        
        
        if(associations != null && associations.hasNext()) {
            Topic newType = wandora.showTopicFinder("Select new association type...");
            
            if(newType == null) return;
            
            Topic oldType = null;
            while(associations.hasNext() && !forceStop()) {
                association = (Association) associations.next();
                if(association != null && !association.isRemoved()) {
                    oldType = association.getType();
                    if(oldType != null && !oldType.mergesWithTopic(newType)) {
                        requiresRefresh = true;
                        association.setType(newType);
                        count++;
                    }
                }
            }
        }
        log("Total " + count + " association type topics changed.");
    }
 
}
