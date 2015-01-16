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
 * OccurrenceRegexReplacerOne.java
 *
 * Created on 10. heinäkuuta 2007, 13:00
 *
 */



package org.wandora.application.tools.occurrences;

import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.util.*;

/**
 * Applies given regular expression to user addressed occurrences.
 * User must select the occurrence type and language. Regular expression
 * may change the occurrence text.
 *
 * @author akivela
 */
public class OccurrenceRegexReplacerOne extends AbstractWandoraTool implements WandoraTool {
    RegularExpressionEditor editor = null;


    public OccurrenceRegexReplacerOne() {
        setContext(new TopicContext());
    }
    public OccurrenceRegexReplacerOne(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Occurrence regular expression replacer";
    }

    @Override
    public String getDescription() {
        return "Applies given regular expression to user addressed occurrences. "+
               "User must select the occurrence type and language.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        try {
            Topic otype = wandora.showTopicFinder("Select occurrence type...");
            if( otype == null ) return;

            Topic oscope = wandora.showTopicFinder("Select occurrence language...");
            if( oscope == null ) return;

            editor = RegularExpressionEditor.getReplaceExpressionEditor(wandora);
            editor.approve = false;
            editor.setVisible(true);
            if(editor.approve == true) {

                setDefaultLogger();
                log("Transforming occurrences with regular expression.");

                Topic topic = null;
                Topic type = null;
                Topic scope = null;

                String occurrence = null;
                String newOccurrence = null;
                
                Iterator typeIterator = null;
                ArrayList updatedOccurrences = new ArrayList();
                Collection<Topic> types = null;
                Hashtable<Topic, String> occurrences = null;

                int progress = 0;
                int count = 0;

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
                                        if(type != null && type.mergesWithTopic(otype)) {
                                            occurrences = topic.getData(type);
                                            if(occurrences != null) {
                                                for(Enumeration occurrenceScopes = occurrences.keys(); occurrenceScopes.hasMoreElements();) {
                                                    scope = (Topic) occurrenceScopes.nextElement();
                                                    if(scope != null && scope.mergesWithTopic(oscope)) {
                                                        occurrence = occurrences.get(scope);
                                                        newOccurrence = editor.replace(occurrence);
                                                        if(newOccurrence != null && !occurrence.equals(newOccurrence)) {
                                                            updatedOccurrences.add(topic);
                                                            updatedOccurrences.add(type);
                                                            updatedOccurrences.add(scope);
                                                            updatedOccurrences.add(newOccurrence);
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
                log("Updating changed occurrences.");
                setProgress(0);
                setProgressMax(updatedOccurrences.size());
                progress = 0;
                String newo = null;
                for(Iterator i = updatedOccurrences.iterator(); i.hasNext() && !forceStop();) {
                    try {
                        setProgress(++progress);
                        topic = (Topic) i.next();
                        type = (Topic) i.next();
                        scope = (Topic) i.next();
                        newo = (String) i.next();
                        if(topic != null && type != null && scope != null && newo != null) {
                            topic.setData(type, scope, newo);
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
        catch (Exception e) {
            log(e);
        }
        setState(WAIT);
    }

}
