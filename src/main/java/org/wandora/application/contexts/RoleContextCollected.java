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
 * RoleContextCollected.java
 *
 * Created on 8. huhtikuuta 2006, 21:07
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
public class RoleContextCollected extends AssociationContext implements Context {
    

    
    /**
     * Creates a new instance of RoleContextCollected
     */
    public RoleContextCollected() {
    }

    
    @Override
    public Iterator getContextObjects() {
        return getRolesOf( super.getContextObjects() );
    }
    

    
    public Iterator getRolesOf(Iterator associations) {
        if(associations == null) return null;
        List<Topic> contextTopics = new ArrayList<>();
        
        Association association = null;
        Collection<Topic> roleTopics = null;
        Topic roleTopic = null;

        while(associations.hasNext()) {
            try {
                association = (Association) associations.next();
                if(association == null) continue;
                roleTopics = association.getRoles();
                if(roleTopics != null && roleTopics.size() > 0) {
                    for(Iterator roleIterator = roleTopics.iterator(); roleIterator.hasNext(); ) {
                        try {
                            roleTopic = (Topic) roleIterator.next();
                            if(removeDuplicates) {
                                if( !contextTopics.contains(roleTopic) ) {
                                    contextTopics.add( roleTopic );
                                }
                            }
                            else {
                                contextTopics.add( roleTopic );
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
