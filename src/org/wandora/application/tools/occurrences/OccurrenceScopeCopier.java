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
 * OccurrenceScopeCopier.java
 *
 * Created on 2008-07-16, 17:04
 *
 */

package org.wandora.application.tools.occurrences;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;




/**
 * Copies occurrence to other scope. Optionally removes old occurrence.
 * 
 * @author akivela
 */
public class OccurrenceScopeCopier extends AbstractWandoraTool implements WandoraTool {
    private boolean REMOVE_AFTER_COPY = false;
    private boolean COPY_NULLS = true;
    private boolean OVERWRITE_OLDIES = true;
    
    

    public OccurrenceScopeCopier() {
        REMOVE_AFTER_COPY = false;
    }
    public OccurrenceScopeCopier(Context preferredContext) {
        REMOVE_AFTER_COPY = false;
        setContext(preferredContext);
    }
    public OccurrenceScopeCopier(boolean removeAfterCopy) {
        REMOVE_AFTER_COPY = false;
    }
    public OccurrenceScopeCopier(boolean removeAfterCopy, Context preferredContext) {
        REMOVE_AFTER_COPY = removeAfterCopy;
        setContext(preferredContext);
    }
    
   
    
    @Override
    public String getName() {
        return "Occurrence scope copier";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies or moves user specified occurrences to other scope.";
    }
    
  
    public void execute(Wandora admin, Context context) {   
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            GenericOptionsDialog god=new GenericOptionsDialog(admin,
                ( REMOVE_AFTER_COPY ? "Move occurrences to other type and scope" : "Copy occurrences to type and other scope" ),
                "To change occurrence scopes please address source and target scopes.",true,new String[][]{
                new String[]{"Type of source occurrences","topic","","Type of changed occurrences"},
                new String[]{"Scope of source occurrences","topic","","Scope i.e. language of changed occurrences"},
                new String[]{"Type of target occurrences","topic","","Type of new occurrences"},
                new String[]{"Scope of target occurrences","topic","","Scope i.e. language of new occurrences"},
                new String[]{"Overwrite existing occurrences?","boolean","false","Save existing target occurrences?"},
                new String[]{"Copy also null occurrences?","boolean","false","Set target occurrences although source variant is null?"},
            },admin);
            god.setVisible(true);
            if(god.wasCancelled()) return;
            
            Map<String,String> values=god.getValues();
            
            COPY_NULLS = "true".equals( values.get("Copy also null occurrences?") ) ? true : false;
            OVERWRITE_OLDIES = "true".equals( values.get("Overwrite existing occurrences?") ) ? true : false;

            Topic sourceTypeTopic = null;
            Topic sourceScopeTopic = null;

            Topic targetTypeTopic = null;
            Topic targetScopeTopic = null;

            TopicMap tm = null;

            setDefaultLogger();
            if( REMOVE_AFTER_COPY ) { setLogTitle("Moving occurrences"); }
            else { setLogTitle("Copying occurrences"); }

            if( OVERWRITE_OLDIES ) log("Overwriting existing occurrences.");
            if( COPY_NULLS ) log("Copying also null occurrences.");
            
            int progress = 0;
            int processed = 0;
            Topic topic = null;
            String occurrence = "";
            String targetOccurrence = null;

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        tm = topic.getTopicMap();
                        try {
                            sourceTypeTopic = null;
                            sourceScopeTopic = null;
                            targetTypeTopic = null;
                            targetScopeTopic = null;

                            sourceTypeTopic = tm.getTopic(values.get("Type of source occurrences"));
                            sourceScopeTopic = tm.getTopic(values.get("Scope of source occurrences"));
                            targetTypeTopic = tm.getTopic(values.get("Type of target occurrences"));
                            targetScopeTopic = tm.getTopic(values.get("Scope of target occurrences"));
                        }
                        catch(Exception e) { }

                        if(sourceTypeTopic != null && sourceScopeTopic != null && targetTypeTopic != null && targetScopeTopic != null) {
                            progress++;
                            occurrence = topic.getData(sourceTypeTopic, sourceScopeTopic);
                            targetOccurrence = topic.getData(targetTypeTopic, targetScopeTopic);
                            if(occurrence != null) {
                                if(REMOVE_AFTER_COPY) topic.setData(targetTypeTopic, targetScopeTopic, null);
                                if(targetOccurrence == null || OVERWRITE_OLDIES) {
                                    topic.setData(targetTypeTopic, targetScopeTopic, occurrence);
                                    processed++;
                                }
                            }
                            else {
                                if(COPY_NULLS) {
                                    if(targetOccurrence == null || OVERWRITE_OLDIES) {
                                        topic.setData(targetTypeTopic, targetScopeTopic, null);
                                        processed++;
                                    }
                                }
                            }
                        }
                        else {
                            if(sourceTypeTopic == null) log("Can't find source type for "+getTopicName(topic)+". Skipping topic.");
                            else if(sourceScopeTopic == null) log("Can't find source scope for "+getTopicName(topic)+". Skipping topic.");
                            else if(targetTypeTopic == null) log("Can't find target type for "+getTopicName(topic)+". Skipping topic.");
                            else if(targetScopeTopic == null) log("Can't find target scope for "+getTopicName(topic)+". Skipping topic.");
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("Processed total " + processed + " occurrences."); 
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
}