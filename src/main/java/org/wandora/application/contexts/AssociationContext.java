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
 * AssociationContext.java
 *
 * Created on 7. huhtikuuta 2006, 12:29
 *
 */

package org.wandora.application.contexts;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.wandora.application.gui.simple.AssociationTypeLinkBasename;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.gui.topicpanels.GraphTopicPanel;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;




/**
 *
 * @author akivela
 */
public class AssociationContext extends LayeredTopicContext implements Context {
    

    public boolean removeDuplicates = true; 
    
    
    @Override
    public Iterator getContextObjects() {
        Object contextSource = getContextSource();
        if(contextSource instanceof AssociationTable) {
            return ((AssociationTable) contextSource).getSelectedAssociations().iterator();
        }
        else if(contextSource instanceof AssociationTypeLinkBasename) {
            return ((AssociationTypeLinkBasename) contextSource).getAssociationTable().getAllAssociations().iterator();
        }
        else if(contextSource instanceof GraphTopicPanel) {
            return ((GraphTopicPanel) contextSource).getContextAssociations().iterator();
        }
        else {
            return getAssociationsOf( super.getContextObjects() );
        }
    }
    
    
    
    public Iterator<Association> getAssociationsOf(Iterator topics) {
        if(topics == null) return null;
        List<Association> contextAssociations = new ArrayList<>();
        Collection<Association> associations = null;
        Topic topic = null;
        Association association = null;

        while(topics.hasNext()) {
            try {
                topic = (Topic) topics.next();
                if(topic == null) continue;
                if(removeDuplicates) {
                    associations = topic.getAssociations();
                    for(Iterator<Association> associationIterator = associations.iterator(); associationIterator.hasNext(); ) {
                        association = associationIterator.next();
                        if(association != null && !contextAssociations.contains(association)) {
                            contextAssociations.add(association);
                        }
                    }
                }
                else {
                    contextAssociations.addAll( topic.getAssociations() );
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        return contextAssociations.iterator();
    }

    
}
