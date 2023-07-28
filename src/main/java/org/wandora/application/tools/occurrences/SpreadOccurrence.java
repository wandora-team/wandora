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
 * SpreadOccurrence.java
 *
 * Created on 3. Maaliskuuta 2008
 *
 */
package org.wandora.application.tools.occurrences;



import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;



/**
 *
 * @author akivela
 */
public class SpreadOccurrence extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	public SpreadOccurrence() {
    }
    public SpreadOccurrence(Context proposedContext) {
        this.setContext(proposedContext);
    }


    @Override
    public String getName() {
        return "Spread occurrence";
    }

    @Override
    public String getDescription() {
        return "Copies selected occurrence to all other scopes.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Object source = getContext().getContextSource();

        // This tool is used mainly in context of occurrence table
        if(source instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) source;
            ot.spread();
        }

        // But general branch allows also any occurrence spread...
        else {
            Iterator topics = context.getContextObjects();
            if(!topics.hasNext()) return;

            Topic otype = wandora.showTopicFinder("Select source occurrence type...");
            if( otype == null ) return;

            Topic oscope = wandora.showTopicFinder("Select source occurrence language...");
            if( oscope == null ) return;

            Collection<Topic> langs = wandora.getTopicMap().getTopicsOfType(XTMPSI.LANGUAGE);
            if(langs == null || langs.isEmpty()) return;

            setDefaultLogger();
            log("Spreading addressed occurrence to all other scopes.");

            Topic topic = null;
            String spreadOccurrence = null;

            Iterator<Topic> typeIterator = null;
            Collection<Topic> types = null;
            Hashtable<Topic, String> occurrences = null;
            //Iterator occurrenceIterator = null;
            Topic type = null;
            Topic scope = null;
            int progress = 0;
            int count = 0;
            ArrayList updatedOccurrences = new ArrayList();

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        hlog("Investigating topic '" + getTopicName(topic) + "'.");
                        progress++;
                        setProgress(progress);
                        types = topic.getDataTypes();
                        if(types != null) {
                            typeIterator = types.iterator();
                            while(typeIterator.hasNext() && !topic.isRemoved()) {
                                try {
                                    type = (Topic) typeIterator.next();
                                    spreadOccurrence = null;
                                    if(type != null && type.mergesWithTopic(otype)) {
                                        occurrences = topic.getData(type);
                                        if(occurrences != null) {
                                            // First, look for the occurrence text that will be spread!
                                            for(Enumeration occurrenceScopes = occurrences.keys(); occurrenceScopes.hasMoreElements();) {
                                                scope = (Topic) occurrenceScopes.nextElement();
                                                if(scope != null && scope.mergesWithTopic(oscope)) {
                                                    spreadOccurrence = occurrences.get(scope);
                                                }
                                            }
                                            if(spreadOccurrence != null) {
                                                // Then, iterate through available languages and spread the text
                                                for(Iterator occurrenceScopes = langs.iterator(); occurrenceScopes.hasNext();) {
                                                    scope = (Topic) occurrenceScopes.next();
                                                    if(scope != null && !scope.mergesWithTopic(oscope)) {
                                                        updatedOccurrences.add(topic);
                                                        updatedOccurrences.add(type);
                                                        updatedOccurrences.add(scope);
                                                        updatedOccurrences.add(spreadOccurrence);
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
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }

            log("Updating occurrences.");
            setProgress(0);
            setProgressMax(updatedOccurrences.size());
            progress = 0;
            String so = null;
            for(Iterator i = updatedOccurrences.iterator(); i.hasNext() && !forceStop();) {
                try {
                    setProgress(++progress);
                    topic = (Topic) i.next();
                    type = (Topic) i.next();
                    scope = (Topic) i.next();
                    so = (String) i.next();
                    if(topic != null && type != null && scope != null && so != null) {
                        topic.setData(type, scope, so);
                        count++;
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("Total "+count+" occurrences changed!");
        }
    }


    @Override
    public boolean requiresRefresh() {
        return true;
    }
}
