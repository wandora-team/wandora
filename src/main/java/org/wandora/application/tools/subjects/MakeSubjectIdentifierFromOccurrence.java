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
 * MakeSubjectIdentifierFromOccurrence.java
 *
 * Created on 4.7.2006, 11:07
 *
 */

package org.wandora.application.tools.subjects;


import java.util.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;


/**
 * Add context topics a subject identifier created using topic's occurrence.
 * Each added subject identifier is created by injecting topic's occurrence string 
 * into a subject identifier template string.
 * 
 * @author akivela
 */
public class MakeSubjectIdentifierFromOccurrence extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private static int MAXLEN = 256;
    private String replacement = "";
    private String SITemplate = "http://wandora.org/si/%OCCURRENCE%";
    
    private boolean askTemplate = true;
    

    public MakeSubjectIdentifierFromOccurrence() {
    }
    
    public MakeSubjectIdentifierFromOccurrence(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Make subject identifier with an occurrence";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and adds each topic new subject identifier made with given occurrence.";
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

            if(SITemplate == null || SITemplate.length() == 0) {
                SITemplate = "http://wandora.org/si/%OCCURRENCE%";
            }
            if(askTemplate) {
                SITemplate = WandoraOptionPane.showInputDialog(admin, "Make subject identifier using following template. String '%OCCURRENCE%' is replaced with topic's occurrence.", SITemplate);
                if(SITemplate == null) return;
                if(SITemplate.length() == 0 || !SITemplate.contains("%OCCURRENCE%")) {
                    int a = WandoraOptionPane.showConfirmDialog(admin, "Your template string '"+ SITemplate +"' does not contain '%OCCURRENCE%'. This results identical subject identifiers and topic merges. Are you sure you want to continue?", "Invalid template given", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                    if(a != WandoraOptionPane.YES_OPTION) return;
                }
            }
            
            setDefaultLogger();
            setLogTitle("Making subject identifier with an occurrence");
            log("Making subject identifier with an occurrence");
            
            Topic topic = null;
            String occurrence = null;
            String SIString = null;
            int progress = 0;
            int progressMax = 0;

            ArrayList<Object> dt = new ArrayList<Object>();
            while(topics.hasNext() && !forceStop()) {
                dt.add(topics.next());
                progressMax++;
            }
            
            topics = dt.iterator();
            setProgressMax(progressMax);
            
            // Iterate through selected topics...
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
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
                            
                            SIString = SITemplate;
                            SIString = SIString.replaceAll("%OCCURRENCE%", occurrence);
                            SIString = TopicTools.cleanDirtyLocator(SIString);
                            
                            log("Adding topic '"+getTopicName(topic)+"' subject identifier '"+SIString+"'.");
                            topic.addSubjectIdentifier(new Locator(SIString));
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
