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
 * MakeSubjectLocatorFromOccurrence.java
 *
 * Created on 4. heinäkuuta 2006, 11:07
 *
 */

package org.wandora.application.tools.subjects;


import java.util.*;

import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import org.wandora.*;


/**
 * Add context topics a subject locator created using topic's occurrence.
 * Each added subject locator is created by injecting topic's occurrence string 
 * into a subject locator template string. Tool asks the subject locator template string
 * from the user.
 * 
 * @author akivela
 */
public class MakeSubjectLocatorFromOccurrence extends AbstractWandoraTool implements WandoraTool {
    private static int MAXLEN = 256;
    private String replacement = "";
    private String SLTemplate = "http://wandora.org/si/%OCCURRENCE%";
    
    private boolean askTemplate = true;
    private boolean overWrite = true;
    

    public MakeSubjectLocatorFromOccurrence() {
    }
    
    public MakeSubjectLocatorFromOccurrence(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
   @Override
    public String getName() {
        return "Make SL from occurrence";
    }

   @Override
    public String getDescription() {
        return "Iterates through selected topics and constructs subject locator for each topic with given occurrence.";
    }

    
    @Override
    public void execute(Wandora admin, Context context) {   
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic occurrenceType=admin.showTopicFinder("Select occurrence type...");                
            if(occurrenceType == null) return;
            Locator occurrenceTypeLocator = occurrenceType.getSubjectIdentifiers().iterator().next();
            
            Topic occurrenceScope=admin.showTopicFinder("Select occurrence scope...");                
            if(occurrenceScope == null) return;
            Locator occurrenceScopeLocator = occurrenceScope.getSubjectIdentifiers().iterator().next();

            if(SLTemplate == null || SLTemplate.length() == 0) {
                SLTemplate = "http://wandora.org/si/%OCCURRENCE%";
            }
            if(askTemplate) {
                SLTemplate = WandoraOptionPane.showInputDialog(admin, "Make subject locator using following template. String '%OCCURRENCE%' is replaced with topic's occurrence.", SLTemplate);
                if(SLTemplate == null) return;
                if(SLTemplate.length() == 0 || !SLTemplate.contains("%OCCURRENCE%")) {
                    int a = WandoraOptionPane.showConfirmDialog(admin, "Your template string '"+ SLTemplate +"' does not contain '%OCCURRENCE%'. This results identical subject locators and topic merges. Are you sure you want to continue?", "Invalid template given", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                    if(a != WandoraOptionPane.YES_OPTION) return;
                }
            }
            
            setDefaultLogger();
            setLogTitle("Making SL from occurrence");
            log("Making subject locator from occurrence");
            
            Topic topic = null;
            String occurrence = null;
            String SLString = null;
            int progress = 0;
            int progressMax = 0;
            
            ArrayList<Object> dt = new ArrayList<Object>();
            while(topics.hasNext() && !forceStop()) {
                dt.add(topics.next());
                progressMax++;
            }
            
            setProgressMax(progressMax);
            topics = dt.iterator();

            // Iterate through selected topics...
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        if(overWrite || topic.getSubjectLocator() == null) {
                            progress++;
                            setProgress(progress);
                            
                            occurrenceType = topic.getTopicMap().getTopic(occurrenceTypeLocator);
                            occurrenceScope = topic.getTopicMap().getTopic(occurrenceScopeLocator);
                            occurrence = topic.getData(occurrenceType, occurrenceScope);

                            // Ok, if topic has sufficient occurrence descent deeper...
                            if(occurrence != null && occurrence.length() > 0) {
                                // First occurrence is modified to suit as the SI and base name... 
                                if(occurrence.length() > MAXLEN) {
                                    occurrence.substring(0, MAXLEN);
                                }
                                if(occurrence.indexOf("\n") != -1 || occurrence.indexOf("\r") != -1) {
                                    occurrence = occurrence.replaceAll("\r", replacement);
                                    occurrence = occurrence.replaceAll("\n", replacement);
                                }
                                occurrence = occurrence.trim();

                                SLString = SLTemplate;
                                SLString = SLString.replaceAll("%OCCURRENCE%", occurrence);
                                SLString = TopicTools.cleanDirtyLocator(SLString);

                                log("Setting topic '"+getTopicName(topic)+"' subject locator '"+SLString+"'.");
                                topic.setSubjectLocator(new Locator(SLString));
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
