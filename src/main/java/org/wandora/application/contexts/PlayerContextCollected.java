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
 * PlayerContextCollected.java
 *
 * Created on 8. huhtikuuta 2006, 20:15
 *
 */

package org.wandora.application.contexts;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;


/**
 * @author akivela
 */
public class PlayerContextCollected extends AssociationContext implements Context {
    
    
    
    /**
     * Creates a new instance of PlayerContextCollected
     */
    public PlayerContextCollected() {
    }
    
    
    
    @Override
    public Iterator getContextObjects() {
        return getPlayersOf( super.getContextObjects() );
    }
    
    
    
    public Iterator getPlayersOf(Iterator associations) {
        if(associations == null) return null;
        List<Topic> contextTopics = new ArrayList<>();
        
        Association association = null;
        Collection<Topic> roleTopics = null;
        Topic playerTopic = null;
        Topic roleTopic = null;

        while(associations.hasNext()) {
            try {
                association = (Association) associations.next();
                if(association == null) continue;
                roleTopics = association.getRoles();
                if(roleTopics != null && roleTopics.size() > 0) {
                    for(Iterator<Topic> roleIterator = roleTopics.iterator(); roleIterator.hasNext(); ) {
                        try {
                            roleTopic = (Topic) roleIterator.next();
                            playerTopic = association.getPlayer(roleTopic);
                            if(playerTopic == null) continue;
                            if(removeDuplicates) {
                                if( !contextTopics.contains(playerTopic) ) {
                                    contextTopics.add( playerTopic );
                                }
                            }
                            else {
                                contextTopics.add( playerTopic );
                            }
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        return contextTopics.iterator();
    }
}
