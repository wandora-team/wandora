/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * MakeOccurrenceFromAssociation.java
 *
 * Created on 25. toukokuuta 2006, 10:57
 *
 */




package org.wandora.application.tools.occurrences;


import java.util.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.*;



/**
 * <p>
 * Transforms given associations to occurrences.
 * </p>
 * <p>
 * Occurrence's type will be association's type. Occurrence is constructed from
 * player's base name.
 * </p>
 * <p>
 * See also <code>MakeAssociationFromOccurrence</code> tool representing
 * symmetric counterpart to <code>MakeOccurrenceFromAssociation</code>.
 * </p>
 * @author akivela
 */



public class MakeOccurrenceFromAssociation extends AbstractWandoraTool implements WandoraTool {

    
    boolean deleteAssociationAndTopic = false;
    
    
    public MakeOccurrenceFromAssociation() {
    }
    
    public MakeOccurrenceFromAssociation(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Make occurrence out of topic's association";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics or associations and makes occurrences using the associated topic.";
    }

    
    @Override
    public void execute(Wandora admin, Context context) {   
        try {
            Iterator associations = null;
            
            
            Topic associationType = null;
            Locator associationTypeLocator = null;
            
            Topic topic = null;
            Topic topicRole = null;
            
            Topic occurrenceTopic = null;
            Topic occurrenceRole = null;

            Topic occurrenceScope = null;
            Locator occurrenceScopeLocator = null;
            
            // If context contains associtions iterate them. Otherwise solve associations...
            if(context instanceof AssociationContext) {
                associations = context.getContextObjects();
            }
            else {
                Iterator topics = context.getContextObjects();                
                if(topics == null || !topics.hasNext()) return;
                ArrayList associationArray = new ArrayList();
                
                associationType = admin.showTopicFinder("Select association type...");                
                if(associationType == null) return;
                associationTypeLocator = associationType.getSubjectIdentifiers().iterator().next();

                topic = null;
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            associationArray.addAll(topic.getAssociations(associationType));
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }

            if(associations == null || !associations.hasNext()) return;
            // Now we have associations. Next solve hooks used to pick up the topic
            // new occurrence is attached and the topic that contains occurrences
            // text as a base name. Both hooks are roles of our associations.
            
            topicRole=admin.showTopicFinder("Select role of topic where new occurrence is attached...");                
            if(topicRole == null) return;

            occurrenceRole=admin.showTopicFinder("Select role of topic that is transformed to occurrence...");                
            if(occurrenceRole == null) return;

            occurrenceScope=admin.showTopicFinder("Select occurrences scope (language)...");                
            if(occurrenceScope == null) return;
            occurrenceScopeLocator = occurrenceScope.getSubjectIdentifiers().iterator().next();
            
            setDefaultLogger();
            setLogTitle("Making occurrences from associations");
            log("Making occurrences from associations");
            
           
            int progress = 0;
            Association a = null;
            String occurrenceText = null;
            
            // Iterate through selected topics...
            while(associations.hasNext() && !forceStop()) {
                try {
                    a = (Association) associations.next();
                    if(a != null && !a.isRemoved()) {
                        progress++;
                        associationType = a.getType();
                        topic = a.getPlayer(topicRole);
                        occurrenceTopic = a.getPlayer(occurrenceRole);
                        
                        // Check if both hooks have catched a valid topic!
                        if(topic != null || occurrenceTopic != null) {
                            occurrenceText = occurrenceTopic.getBaseName();
                            topic.setData(associationType, occurrenceScope, occurrenceText);

                            // Finally deleting association and topic if...
                            if(deleteAssociationAndTopic) {
                                a.remove();
                                occurrenceTopic.remove();
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("OK.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
}
