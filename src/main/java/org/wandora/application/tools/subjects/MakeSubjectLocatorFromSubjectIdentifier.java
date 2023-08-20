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
 * MakeSubjectLocatorFromSubjectIdentifier.java
 *
 * Created on 23. lokakuuta 2007, 17:34
 *
 */

package org.wandora.application.tools.subjects;

import static org.wandora.application.gui.ConfirmResult.yes;
import static org.wandora.application.gui.ConfirmResult.yestoall;

import java.util.Collection;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.ApplicationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.LayeredTopicContext;
import org.wandora.application.contexts.SIContext;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;


/**
 * Copies topic's subject identifier to topic's subject locator.
 *
 * @author akivela
 */
public class MakeSubjectLocatorFromSubjectIdentifier extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;


	public MakeSubjectLocatorFromSubjectIdentifier() {
    }
    public MakeSubjectLocatorFromSubjectIdentifier(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Copy subject identifier to subject locator";
    }

    @Override
    public String getDescription() {
        return "Adds selected subject identifier to the topic as a subject locator.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        
        
        if(context instanceof SIContext) {
            Iterator sis = context.getContextObjects();
            if(sis.hasNext()) {
                try {
                    Locator si = (Locator) sis.next();
                    TopicMap topicmap = wandora.getTopicMap();
                    Topic t = topicmap.getTopic(si);
                    t.setSubjectLocator(si);
                    if(sis.hasNext()) {
                        setDefaultLogger();
                        log("First subject identifier was added to the topic as a subject locator.");
                        log("Skipping rest of the subject identifiers.");
                        log("Ready.");
                        setState(WAIT);
                    }
                }
                catch(Exception e) {
                    singleLog(e);
                }
            }
        }
        
        else if(context instanceof ApplicationContext) {
            Iterator<Topic> topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            try {
                while(topics.hasNext()) {
                    Topic topic = topics.next();
                    Locator subjectIdentifier = topic.getFirstSubjectIdentifier();
                    topic.setSubjectLocator(subjectIdentifier);
                }
            }
            catch(Exception e) {
                log(e);
            }

        }
        
        else if(context instanceof LayeredTopicContext) {
            Iterator<Topic> topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            setDefaultLogger();
            try {
                log("Copying subject identifier to subject locator.");

                Collection<Locator> subjectIdentifiers = null;
                Topic topic = null;
                Locator subjectLocator = null;
                String subjectLocatorString = null;
                Locator locator = null;
                ConfirmResult result = yes;
                int progress = 0;

                while(topics.hasNext() && !forceStop(result)) {
                    try {
                        topic = topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            setProgress(progress++);

                            subjectIdentifiers = topic.getSubjectIdentifiers();
                            if(subjectIdentifiers.size() > 0) {
                                subjectLocator = subjectIdentifiers.iterator().next();
                                if(subjectLocator != null) {
                                    subjectLocatorString = subjectLocator.toExternalForm();
                                    if(subjectLocatorString != null) {
                                        log("Adding topic '"+getTopicName(topic)+"' subject locator\n"+subjectLocatorString);
                                        locator = new Locator(subjectLocatorString);
                                        if(result != yestoall) {
                                            result = TMBox.checkSubjectIdentifierChange(wandora,topic,locator,true, true);
                                        }
                                        if(result == yes || result == yestoall) {
                                            topic.setSubjectLocator(locator);
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
            }
            catch (Exception e) {
                log(e);
            }
            log("Ready.");
            setState(WAIT);
        }
    }
    
}
