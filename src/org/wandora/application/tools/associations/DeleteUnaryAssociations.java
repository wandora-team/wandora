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
 * DeleteUnaryAssociations.java
 *
 * Created on 2014-03-04, 12:00
 *
 */

package org.wandora.application.tools.associations;


import java.util.Collection;
import java.util.LinkedHashSet;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;




/**
 * Deletes associations that have zero or one player within.
 *
 * @author akivela
 */
public class DeleteUnaryAssociations extends DeleteAssociationsInTopic implements WandoraTool {
    
    public static final boolean LOOK_AT_ASSOCIATION_TYPES_TOO = true;
    

    public DeleteUnaryAssociations() {
    }
    public DeleteUnaryAssociations(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public boolean shouldDelete(Topic topic, Association association) throws TopicMapException {
        try {
            if(association != null && !association.isRemoved()) {
                if(association.getRoles().size() < 2) {
                    return super.confirmDelete(association);
                }
                else {
                    // Do not delete association if it has 2 or more players.
                    return false;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    
    @Override
    public Collection<Association> solveTopicAssociations(Topic topic) throws TopicMapException {
        TopicMap tm = topic.getTopicMap();
        LinkedHashSet<Association> associations = new LinkedHashSet();
        associations.addAll(topic.getAssociations());
        if(LOOK_AT_ASSOCIATION_TYPES_TOO) {
            Topic at = null;
            for(Locator l : topic.getSubjectIdentifiers()) {
                at = tm.getTopic(l);
                if(at != null && !at.isRemoved()) {
                    associations.addAll( topic.getAssociations(at) );
                }
            }
        }
        return associations;
    }
}
