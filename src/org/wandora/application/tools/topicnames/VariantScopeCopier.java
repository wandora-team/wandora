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
 * VariantScopeCopier.java
 *
 * Created on 2008-07-16, 17:04
 *
 */

package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;




/**
 * Implements <code>WandoraAdminTool</code> that copies or removes variant names
 * to other scope.
 * 
 * @author akivela
 */
public class VariantScopeCopier extends AbstractWandoraTool implements WandoraTool {
    private boolean REMOVE_AFTER_COPY = false;
    private boolean COPY_NULLS = false;
    private boolean OVERWRITE_OLDIES = true;
    
    
    /**
     * Creates a new instance of VariantScopeCopier
     */
    public VariantScopeCopier() {
        REMOVE_AFTER_COPY = false;
    }
    public VariantScopeCopier(Context preferredContext) {
        REMOVE_AFTER_COPY = false;
        setContext(preferredContext);
    }
    public VariantScopeCopier(boolean removeAfterCopy) {
        REMOVE_AFTER_COPY = false;
    }
    public VariantScopeCopier(boolean removeAfterCopy, Context preferredContext) {
        REMOVE_AFTER_COPY = removeAfterCopy;
        setContext(preferredContext);
    }
    
   
    

    @Override
    public String getName() {
        return "Variant scope copier";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies or moves user specified variant names to other scope.";
    }
    
  
    public void execute(Wandora wandora, Context context) {
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            GenericOptionsDialog god=new GenericOptionsDialog(wandora,
                ( REMOVE_AFTER_COPY ? "Move variant name to other type and scope" : "Copy variant name to other type and scope" ),
                "To change variant name types and scopes please address source and target types and scopes.",true,new String[][]{
                new String[]{"Type of source variants","topic","","Variant name type of changed names (display name for example)"},
                new String[]{"Scope of source variants","topic","","Variant name scope i.e. language of changed names (English for example)"},
                new String[]{"Type of target variants","topic","","Variant name type of new names (display name for example)"},
                new String[]{"Scope of target variants","topic","","Variant name scope i.e. language of new names (English for example)"},
                new String[]{"Overwrite existing names?","boolean","false","Save existing target names?"},
                new String[]{"Copy also null names?","boolean","false","Set target variant although source variant is null?"},
            },wandora);
            god.setVisible(true);
            if(god.wasCancelled()) return;
            
            Map<String,String> values=god.getValues();
            
            COPY_NULLS = "true".equals( values.get("Copy also null names?") ) ? true : false;
            OVERWRITE_OLDIES = "true".equals( values.get("Overwrite existing names?") ) ? true : false;

            Topic sourceTypeTopic = null;
            Topic sourceScopeTopic = null;

            Topic targetTypeTopic = null;
            Topic targetScopeTopic = null;

            TopicMap tm = null;
            Set<Topic> sourceScope = null;
            Set<Topic> targetScope = null;

            setDefaultLogger();
            if( REMOVE_AFTER_COPY ) { setLogTitle("Moving variant names to other scope"); }
            else { setLogTitle("Copying variant names to other scope"); }

            if( OVERWRITE_OLDIES ) log("Overwriting existing names.");
            if( COPY_NULLS ) log("Copying also null names.");
            
            int progress = 0;
            int processed = 0;
            Topic topic = null;
            String variant = "";
            String targetVariant = null;

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

                            sourceTypeTopic = tm.getTopic(values.get("Type of source variants"));
                            sourceScopeTopic = tm.getTopic(values.get("Scope of source variants"));
                            targetTypeTopic = tm.getTopic(values.get("Type of target variants"));
                            targetScopeTopic = tm.getTopic(values.get("Scope of target variants"));
                        }
                        catch(Exception e) { }

                        if(sourceTypeTopic != null && sourceScopeTopic != null && targetTypeTopic != null && targetScopeTopic != null) {
                            sourceScope = new HashSet<Topic>();
                            sourceScope.add(sourceScopeTopic);
                            sourceScope.add(sourceTypeTopic);

                            targetScope = new HashSet<Topic>();
                            targetScope.add(targetScopeTopic);
                            targetScope.add(targetTypeTopic);

                            progress++;
                            variant = topic.getVariant(sourceScope);
                            targetVariant = topic.getVariant(targetScope);
                            Set<Set<Topic>> scopes = topic.getVariantScopes();
                            
                            // System.out.println("scopes "+scopes);
                            // System.out.println("scopes size "+scopes.size());
                            // System.out.println("Processing "+getTopicName(topic)+" with variant "+variant);
                            
                            if(variant != null) {
                                if(REMOVE_AFTER_COPY) topic.setVariant(sourceScope, null);
                                if(targetVariant == null || OVERWRITE_OLDIES) {
                                    topic.setVariant(targetScope, variant);
                                    processed++;
                                }
                            }
                            else {
                                if(COPY_NULLS) {
                                    if(targetVariant == null || OVERWRITE_OLDIES) {
                                        topic.setVariant(targetScope, null);
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
            log("Processed total " + processed + " variant names."); 
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    

    
}
