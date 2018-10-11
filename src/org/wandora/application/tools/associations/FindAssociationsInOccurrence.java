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
 * FindAssociationsInOccurrence.java
 *
 * Created on 9. maaliskuuta 2007, 12:07
 *
 */

package org.wandora.application.tools.associations;


import java.util.*;
import java.util.regex.*;

import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.topicstringify.TopicToString;



/**
 * Iterates selected topics and recognizes base names in topic's
 * occurrence, and associates topic of the recognized base name to the iterated
 * one.
 *
 * @author akivela
 */
public class FindAssociationsInOccurrence extends AbstractWandoraTool implements WandoraTool {
	
	
	private static final long serialVersionUID = 1L;
	
	
	public static int MAXLEN = 256;
    public String replacement = "";
    public String SITemplate = "http://wandora.org/si/occurrence/%OCCURRENCE%";
    
    
    private boolean createNewTopics = false;
    private boolean askTemplate = true;
    private boolean requiresRefresh = false;
    
    /** Creates a new instance of FindAssociationsInOccurrence */
    public FindAssociationsInOccurrence() {
    }
    
    public FindAssociationsInOccurrence(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Find associations in occurrence";
    }
    @Override
    public String getDescription() {
        return "Iterates through selected topics and recognizes base names in topic's occurrence, and associates topic of the recognized base name to the iterated one.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {   
        try {
            requiresRefresh = false;
            GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Find associations options","Find associations options",true,new String[][]{
                new String[]{"Occurrence type","topic"},
                new String[]{"Occurrence scope","topic"},
                new String[]{"Topic role","topic"},
                new String[]{"Link pattern","string","\\[\\[(.+?)\\]\\]","The regular expression pattern for base names? First capture group in the pattern marks the base name."},
                new String[]{"SI template","string","http://wandora.org/si/occurrence/%OCCURRENCE%"},
                new String[]{"Create topics","boolean","true","Should new topics be created for non existing base names"},
            },wandora);
            god.setVisible(true);
            if(god.wasCancelled()) return;
            Map<String,String> values=god.getValues();
            
            SITemplate = values.get("SI template");
            Topic occurrenceType=wandora.getTopicMap().getTopic(values.get("Occurrence type"));
            if(occurrenceType==null) return;
            Topic occurrenceScope=wandora.getTopicMap().getTopic(values.get("Occurrence scope"));
            if(occurrenceScope==null) return;
            Topic topicRole=wandora.getTopicMap().getTopic(values.get("Topic role"));
            if(topicRole==null) return;
            String recognizePatternString=values.get("Link pattern");
            createNewTopics=Boolean.parseBoolean(values.get("Create topics"));
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Locator occurrenceTypeLocator = occurrenceType.getSubjectIdentifiers().iterator().next();
            Locator occurrenceScopeLocator = occurrenceScope.getSubjectIdentifiers().iterator().next();
            
            // Finally ready to roll...
            setDefaultLogger();
            setLogTitle("Finding associations from occurrences");
            log("Finding associations from occurrences");
            
            TopicMap map = wandora.getTopicMap();
            Topic topic = null;
            String topicName = null;
            Association a = null;
            String occurrence = null;
            int progress = 0;
            int acount = 0;
            int ocount = 0;
            
            Pattern recognizePattern = Pattern.compile(recognizePatternString);
            String recognizedTopicName = null;
            Topic recognizedTopic = null;
            //log("recognizePatternString == '"+recognizePatternString+"'");
            
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
                        
                        // Topic name for gui use... 
                        topicName = TopicToString.toString(topic);
                        hlog("Inspecting topic '"+topicName+"'");
                        
                        occurrenceType = topic.getTopicMap().getTopic(occurrenceTypeLocator);
                        occurrenceScope = topic.getTopicMap().getTopic(occurrenceScopeLocator);
                        occurrence = topic.getData(occurrenceType, occurrenceScope);

                        // Ok, if topic has sufficient occurrence descent deeper...
                        if(occurrence != null && occurrence.length() > 0) {
                            ocount++;
                            hlog("Found occurrence in topic '"+topicName+"'");
                            
                            Matcher m = recognizePattern.matcher(occurrence);
                            while(m.find() && !forceStop()) {
                                for(int i=0; i<m.groupCount(); i++) {
                                    recognizedTopicName = m.group(i);
                                    Matcher m2 = recognizePattern.matcher(recognizedTopicName);
                                    if(m2.find()) {
                                        recognizedTopicName = m2.replaceAll("$1");
                                    }
                                    log("Recognized '"+recognizedTopicName+"' in occurrence");

                                    // Check if required topic already exists
                                    recognizedTopic = map.getTopicWithBaseName(recognizedTopicName);
                                    
                                    // No. It didn't. Create new topic?
                                    if(recognizedTopic == null && createNewTopics) {
                                        // Creating new topic for the occurrence...
                                        recognizedTopic = map.createTopic();

                                        // Giving new topic SI and base name
                                        hlog("Creating new topic for '"+recognizedTopicName+"'");
                                        recognizedTopic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                                        recognizedTopic.setBaseName(recognizedTopicName);
                                    }

                                    if(recognizedTopic != null) {
                                        // Creating new association between old and new topic
                                        hlog("Creating association between '"+recognizedTopicName+"' and '"+topicName+"'.");
                                        requiresRefresh = true;
                                        a = map.createAssociation(occurrenceType);
                                        a.addPlayer(topic, topicRole);
                                        a.addPlayer(recognizedTopic, occurrenceType);
                                        acount++;
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("Total "+progress+" topics investigated.");
            log("Total "+ocount+" occurrences found.");
            log("Total "+acount+" associations created.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    
    
}
