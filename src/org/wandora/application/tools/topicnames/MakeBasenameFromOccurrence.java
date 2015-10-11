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
 * MakeBasenameFromOccurrence.java
 *
 * Created on 25. toukokuuta 2006, 10:57
 *
 */

package org.wandora.application.tools.topicnames;


import java.util.*;

import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import org.wandora.*;



/**
 * This tool can be used to fill topic base name with topic's occurrence.
 * For example <code>SimpleRDFImport</code> results topics without
 * base name but each RDF resource label is text occurrence and can be converted
 * to base name. Base name is constructed using <code>template</code> string.
 * All %OCCURRENCE% strings in template are replaced with topic's occurrence
 * string. As occurrences may be identical, tool may result topic merges.
 * 
 * Before base name is set, new line characters are removed from occurrence
 * texts and text length is limited to <code>MAXLEN</code>.
 *
 * @author akivela
 */



public class MakeBasenameFromOccurrence extends AbstractWandoraTool implements WandoraTool {
    public static int MAXLEN = 256;
    public String replacement = "";
    public String template = "%OCCURRENCE%";
    
    boolean overWrite = false;
    boolean askTemplate = true;
    boolean narrowIdentity = false;
    
    
    
    /** Creates a new instance of MakeBasenameWithOccurrence */
    public MakeBasenameFromOccurrence() {
    }
    
    public MakeBasenameFromOccurrence(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Copy occurrence to topic base name";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and fills empty base names with occurrence.";
    }

    
    public void execute(Wandora wandora, Context context) {
        try {
            template = "%OCCURRENCE%";
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            Topic occurrenceType=wandora.showTopicFinder("Select occurrence type...");
            if(occurrenceType == null) return;
            Locator occurrenceTypeLocator = occurrenceType.getSubjectIdentifiers().iterator().next();
            
            Topic occurrenceScope=wandora.showTopicFinder("Select occurrence scope...");
            if(occurrenceScope == null) return;
            Locator occurrenceScopeLocator = occurrenceScope.getSubjectIdentifiers().iterator().next();
            
            if(askTemplate) {
                template = WandoraOptionPane.showInputDialog(wandora, "Make base name from occurrence using following template. String '%OCCURRENCE%' is replaced with the occurrence.", "%OCCURRENCE%");
                if(template == null || template.length() == 0 || !template.contains("%OCCURRENCE%")) {
                    int a = WandoraOptionPane.showConfirmDialog(wandora, "Your template string '"+ template +"' does not contain '%OCCURRENCE%'. This results identical base names and topic merges. Are you sure you want to continue?", "Invalid template given", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                    if(a != WandoraOptionPane.YES_OPTION) return;
                }
            }
            
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Changing base name may cause topic merges and SI explosion in certain topics. Would you like to limit number of SIs in a single topic to 10?", "Narrow identity?", WandoraOptionPane.YES_NO_CANCEL_OPTION);
            if(a == WandoraOptionPane.CANCEL_OPTION) return;
            else if(a == WandoraOptionPane.YES_OPTION) narrowIdentity = true;
            else if(a == WandoraOptionPane.NO_OPTION) narrowIdentity = false;
            
            setDefaultLogger();
            setLogTitle("Copying occurrence to base name");
            log("Copying occurrence to topic's base name");
            
            Topic topic = null;
            String basename = null;
            int progress = 0;
            String occurrence = null;
            int count = 0;

            ArrayList<Object> dt = new ArrayList<Object>();
            while(topics.hasNext() && !forceStop()) {
                dt.add(topics.next());
            }
            topics = dt.iterator();
            
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        basename = topic.getBaseName();
                        if(overWrite || basename == null) {
                            occurrenceType = topic.getTopicMap().getTopic(occurrenceTypeLocator);
                            occurrenceScope = topic.getTopicMap().getTopic(occurrenceScopeLocator);
                            occurrence = topic.getData(occurrenceType, occurrenceScope);
                            
                            if(occurrence != null && occurrence.length() > 0) {
                                if(occurrence.length() > MAXLEN) {
                                    occurrence.substring(0, MAXLEN);
                                }
                                if(occurrence.indexOf("\n") != -1 || occurrence.indexOf("\r") != -1) {
                                    occurrence = occurrence.replaceAll("\r", replacement);
                                    occurrence = occurrence.replaceAll("\n", replacement);
                                }
                                occurrence = occurrence.trim();
                                basename = template;
                                basename = basename.replaceAll("%OCCURRENCE%", occurrence);
                                log("Adding topic base name '"+basename+"'");
                                topic.setBaseName(basename);
                                count++;
                                
                                if(narrowIdentity) {
                                    Collection<Locator> sis = topic.getSubjectIdentifiers();
                                    int s = sis.size();
                                    ArrayList<Locator> deleteThese = new ArrayList<Locator>();
                                    if(s > 10) {
                                        int n = 0;
                                        Iterator<Locator> sii = sis.iterator();
                                        while(s-n>10 && sii.hasNext()) {
                                            deleteThese.add(sii.next());
                                            n++;
                                        }
                                        Iterator<Locator> deleteIterator = deleteThese.iterator();
                                        while(deleteIterator.hasNext()) {
                                            topic.removeSubjectIdentifier(deleteIterator.next());
                                        }
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
            log("Total "+progress+" topics investigated!");
            log("Total "+count+" basenames set!");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    

}
