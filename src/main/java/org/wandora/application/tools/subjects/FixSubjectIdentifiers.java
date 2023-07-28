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
 * FixSubjectIdentifiers.java
 *
 * Created on 6. tammikuuta 2005, 13:38
 */

package org.wandora.application.tools.subjects;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;


import java.util.*;



/**
 * <code>FixSubjectIdentifiers</code> iterates through all subject identifiers in context
 * topics and replaces all invalid characters. Invalid characters in subject identifiers may cause
 * problems if subject identifiers are used to acquire topics within
 * web application.
 *
 * Subject identifier changes may cause topic merges.
 *
 * @author  akivela
 */
public class FixSubjectIdentifiers extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;


	public FixSubjectIdentifiers() {
    }
    public FixSubjectIdentifiers(Context context) {
        setContext(context);
    }
    

    @Override
    public String getName() {
        return "Fix subject identifiers";
    }

    @Override
    public String getDescription() {
        return "Clean up all illegal characters in subject identifiers.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Iterator<Topic> contextTopics = context.getContextObjects();
        if(contextTopics != null && contextTopics.hasNext()) {
            if(WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to fix subject identifiers?","Fix subject identifiers?", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                setDefaultLogger();
                setLogTitle("Fixing subject identifiers");
                Collection<Locator> subjectIdentifiers = null;
                int progress = 0;

                ArrayList<Topic> topics = new ArrayList<>();
                while(contextTopics.hasNext() && !forceStop()) {
                    topics.add(contextTopics.next());
                }
  
                for(Topic topic : topics) {
                    try {
                        if(forceStop()) break;
                        if(topic != null  && !topic.isRemoved()) {
                            setProgress(progress++);
                            subjectIdentifiers = topic.getSubjectIdentifiers();
                            if(subjectIdentifiers.isEmpty()) {
                                Locator newSubjectIdentifier = TopicTools.createDefaultLocator();
                                log("Topic has no subject identifiers at all. Adding subject identifier " + newSubjectIdentifier.toExternalForm());
                                topic.addSubjectIdentifier(newSubjectIdentifier);
                            }
                            else {
                                for(Locator subjectIdentifier : subjectIdentifiers) {
                                    if(forceStop()) break;
                                    if(subjectIdentifier != null) {
                                        hlog("Investigating subject identifier \n"+getPrintable(subjectIdentifier.toExternalForm()));
                                        if(TopicTools.isDirtyLocator(subjectIdentifier)) {
                                            Locator newSubjectIdentifier = fixSI(topic, subjectIdentifier);
                                            if(newSubjectIdentifier != null) {
                                                log("Fixed subject identifier \n" + newSubjectIdentifier.toExternalForm());
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
                log("Ready.");
                setState(WAIT);
            }
        }
    }
                    

    
    
    protected Locator fixSI(Topic topic, Locator locator)  throws TopicMapException {
        if(locator != null) {
            Locator cleanedLocator = TopicTools.cleanDirtyLocator(locator);
            if(topic != null && !topic.isRemoved()) {
                int counter = 1;
                Topic anotherTopic = topic.getTopicMap().getTopic(cleanedLocator);
                while(anotherTopic != null && !anotherTopic.equals(topic)) {
                    String cleanedLocatorString = cleanedLocator.toExternalForm();
                    cleanedLocator = new Locator(cleanedLocatorString + "_" + counter);
                    anotherTopic = topic.getTopicMap().getTopic(cleanedLocator);
                }
                if(!cleanedLocator.equals(locator)) {
                    topic.addSubjectIdentifier(cleanedLocator);
                    topic.removeSubjectIdentifier(locator);
                }
            }
            return cleanedLocator;
        }
        return null;
    }


    /**
     * Used to trim long subject identifiers shorter for prettier printing. 
     */
    private String getPrintable(String str) {
        if(str.length() > 128) {
            return str.substring(0, 127) + "...";
        }
        return str;
    }
}
