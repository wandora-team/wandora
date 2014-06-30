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
 *
 */




package org.wandora.application.tools.occurrences.refine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author akivela
 */
public abstract class AbstractOccurrenceExtractor extends AbstractWandoraTool implements  WandoraTool {
    private Context preferredContext = null;



    public AbstractOccurrenceExtractor() {
        
    }
    public AbstractOccurrenceExtractor(Context context) {
        this.preferredContext = context;
    }






    public abstract boolean _extractTopicsFrom(String occurrenceData, Topic masterTopic, TopicMap topicMap, Wandora wandora) throws Exception;



    public void execute(Wandora admin, Context context) {
        Iterator topics = null;
        if(preferredContext != null) topics = preferredContext.getContextObjects();
        else topics = context.getContextObjects();

        if(topics != null && topics.hasNext()) {
            try {
                Topic topic = null;
                int total = 0;
                int count = 0;
                TopicMap tm = admin.getTopicMap();

                GenericOptionsDialog god=new GenericOptionsDialog(admin,
                    "Extract information from occurrences",
                    "To extract information from occurrences please address type and scope of occurrences.",true,new String[][]{
                    new String[]{"Type of occurrences","topic","","Type of changed occurrences"},
                    new String[]{"Scope of occurrences","topic","","Scope i.e. language of changed occurrences"},
                },admin);
                god.setVisible(true);
                if(god.wasCancelled()) return;

                Map<String,String> values=god.getValues();

                Topic typeTopic = null;
                Topic scopeTopic = null;

                typeTopic = tm.getTopic(values.get("Type of occurrences"));
                scopeTopic = tm.getTopic(values.get("Scope of occurrences"));

                setDefaultLogger();
                setLogTitle("Extracting from occurrences...");

                ArrayList<Topic> dtopics = new ArrayList<Topic>();
                while(topics.hasNext() && !forceStop()) {
                    dtopics.add((Topic) topics.next());
                }
                topics = dtopics.iterator();

                while(topics.hasNext() && !forceStop()) {
                    try {
                        total++;
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            if(typeTopic != null) {
                                Hashtable<Topic,String> scopedOccurrences = topic.getData(typeTopic);
                                if(scopedOccurrences != null && scopedOccurrences.size() > 0) {
                                    if(scopeTopic != null) {
                                        String occurrence = scopedOccurrences.get(scopeTopic);
                                        if(occurrence != null) {
                                            count++;
                                            _extractTopicsFrom(occurrence, topic, tm, admin);
                                        }
                                    }
                                    else {
                                        Enumeration<Topic> scopeTopics = scopedOccurrences.keys();
                                        while(scopeTopics.hasMoreElements()) {
                                            Topic oscopeTopic = scopeTopics.nextElement();
                                            String occurrence = scopedOccurrences.get(oscopeTopic);
                                            if(occurrence != null && occurrence.length() > 0) {
                                                count++;
                                                _extractTopicsFrom(occurrence, topic, tm, admin);
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Collection<Topic> occurrenceTypes = topic.getDataTypes();
                                for(Topic type : occurrenceTypes) {
                                    typeTopic = type;
                                    Hashtable<Topic,String> scopedOccurrences = topic.getData(type);
                                    Enumeration<Topic> scopeTopics = scopedOccurrences.keys();

                                    while(scopeTopics.hasMoreElements()) {
                                        Topic oscopeTopic = scopeTopics.nextElement();
                                        String occurrence = scopedOccurrences.get(oscopeTopic);
                                        if(occurrence != null && occurrence.length() > 0) {
                                            count++;
                                            _extractTopicsFrom(occurrence, topic, tm, admin);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        log(e);
                    }

                    // WAIT FOR A WHILE BEFORE SENDING ANOTHER REQUEST
                    if(topics.hasNext() && !forceStop()) {
                        try { Thread.currentThread().sleep(100); }
                        catch(Exception e) {}
                    }
                }
                log("Total " + total + " topics investigated!");
                log("Total " + count + " occurrences extracted!");
            }
            catch (Exception e) {
                log(e);
            }
            setState(WAIT);
        }
    }




}
