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
 * MakeAssociationWithOccurrence.java
 *
 * Created on 25. toukokuuta 2006, 10:57
 *
 */

package org.wandora.application.tools.associations;


import java.util.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import static org.wandora.application.tools.AbstractWandoraTool.getTopicName;




/**
 * <p>
 * <code>MakeAssociationWithOccurrence</code> transforms occurrences
 * to associations creating first new topic representing the occurrence and then
 * associating the old and the created topic.
 * </p>
 * <p>
 * Association's type will be occurrence's type. New topic created for the occurrence
 * is given role identical to occurrence's type. Topic that contains the original
 * occurrence is given role pointed by the user. Tool may also delete original
 * occurrence if <code>deleteOccurrence</code> is set true.
 * </p>
 * <p>
 * The operation is lossy. Occurrences loose new line characters in transformation.
 * Tool also truncates long occurrences.
 * </p>
 * <p>
 * This tool is useful to refactor topic map created with data base import
 * for example. See also <code>MakeOccurrenceFromAssociation</code> tool representing
 * symmetric counterpart to <code>MakeAssociationWithOccurrence</code>.
 * </p>
 * 
 * @author akivela
 */



public class MakeAssociationWithOccurrence extends AbstractWandoraTool implements WandoraTool {
    public static int MAXLEN = 256;
    public String replacement = "";
    public String SITemplate = "http://wandora.org/si/occurrence/%OCCURRENCE%";
    
    
    private boolean deleteOccurrence = false;
    private boolean askTemplate = true;
    private boolean requiresRefresh = false;
    

    public MakeAssociationWithOccurrence() {
    }
    
    public MakeAssociationWithOccurrence(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Make association with topic's occurrences";
    }
    @Override
    public String getDescription() {
        return "Iterates through selected topics and makes new topic from topic's occurrences and associates original topic to the new one.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora admin, Context context) {   
        try {
            SITemplate = "http://wandora.org/si/occurrence/%OCCURRENCE%";
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
                        
            Topic occurrenceType=admin.showTopicFinder("Select occurrence type...");                
            if(occurrenceType == null) return;
            Locator occurrenceTypeLocator = occurrenceType.getSubjectIdentifiers().iterator().next();
            
            Topic occurrenceScope=admin.showTopicFinder("Select occurrence scope...");                
            if(occurrenceScope == null) return;
            Locator occurrenceScopeLocator = occurrenceScope.getSubjectIdentifiers().iterator().next();
            
            Topic topicRole=admin.showTopicFinder("Select topic's role in association...");                
            if(topicRole == null) return;

            if(askTemplate) {
                SITemplate = WandoraOptionPane.showInputDialog(admin, "Make subject identifier to new topic using following template. String '%OCCURRENCE%' is replaced with the occurrence.", SITemplate);
                if(SITemplate == null || SITemplate.length() == 0 || !SITemplate.contains("%OCCURRENCE%")) {
                    int a = WandoraOptionPane.showConfirmDialog(admin, "Your template string '"+ SITemplate +"' does not contain '%OCCURRENCE%'. This results identical subject identifiers and topic merges. Are you sure you want to continue?", "Invalid template given", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                    if(a != WandoraOptionPane.YES_OPTION) return;
                }
            }
            
            setDefaultLogger();
            setLogTitle("Making associations from occurrences");
            log("Making associations from occurrences");
            
            Topic topic = null;
            String topicName = null;
            Topic newTopic = null;
            String SIString = null;
            int progress = 0;
            TopicMap map = admin.getTopicMap();
            Association a = null;
            String occurrence = null;
            Locator l = null;

            ArrayList<Topic> dtopics = new ArrayList<Topic>();
            while(topics.hasNext() && !forceStop()) {
                dtopics.add((Topic) topics.next());
            }
            topics = dtopics.iterator();
            
            // Iterate through selected topics...
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        progress++;
                        hlog("Inspecting topic '"+getTopicName(topic)+"'");
                        
                        occurrenceType = topic.getTopicMap().getTopic(occurrenceTypeLocator);
                        occurrenceScope = topic.getTopicMap().getTopic(occurrenceScopeLocator);
                        occurrence = topic.getData(occurrenceType, occurrenceScope);

                        // Ok, if topic has sufficient occurrence descent deeper...
                        if(occurrence != null && occurrence.length() > 0) {
                            log("Processing occurrence of topic '"+getTopicName(topic)+"'");
                            // First occurrence is modified to suit as the SI and base name... 
                            if(occurrence.length() > MAXLEN) {
                                occurrence = occurrence.substring(0, MAXLEN);
                            }
                            if(occurrence.contains("\n") || occurrence.contains("\r")) {
                                occurrence = occurrence.replaceAll("\r", replacement);
                                occurrence = occurrence.replaceAll("\n", replacement);
                            }
                            occurrence = occurrence.trim();
                            
                            requiresRefresh = true;
                            SIString = SITemplate;
                            SIString = SIString.replaceAll("%OCCURRENCE%", occurrence);
                            l = new Locator(TopicTools.cleanDirtyLocator(SIString));
                            
                            // Check if required topic already exists
                            newTopic = map.getTopic(l);
                            if(newTopic == null) newTopic = map.getTopicWithBaseName(occurrence);
                            if(newTopic == null) {
                                // Creating new topic for the occurrence...
                                newTopic = map.createTopic();

                                // Giving new topic SI and base name
                                hlog("Creating new topic from occurrence '"+occurrence+"'");
                                newTopic.addSubjectIdentifier(l);
                                newTopic.setBaseName(occurrence);
                            }
                            
                            // Topic name for gui use... 
                            topicName = getTopicName(topic);

                            // Creating new association between old and new topic
                            hlog("Creating association between '"+occurrence+"' and '"+topicName+"'.");
                            a = map.createAssociation(occurrenceType);
                            a.addPlayer(newTopic, occurrenceType);
                            a.addPlayer(topic, topicRole);
                            
                            // Finally deleting occurrence if...
                            if(deleteOccurrence) {
                                topic.removeData(occurrenceType);
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    

}
