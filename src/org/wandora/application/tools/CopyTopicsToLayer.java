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
 * CopyTopicsToTopicmap.java
 *
 * Created on August 24, 2004, 11:05 AM
 */




package org.wandora.application.tools;

import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import java.util.*;
import javax.swing.*;
import org.wandora.topicmap.undowrapper.UndoTopicMap;


/**
 *
 * @author  olli, akivela
 */



public class CopyTopicsToLayer extends AbstractWandoraTool implements WandoraTool {
    public static final int COPY_TOPIC_AS_A_SINGLE_SI_STUB = 101;
    public static final int COPY_TOPIC_AS_A_SI_STUB = 102;
    public static final int COPY_TOPIC_AS_A_STUB = 106;
    public static final int COPY_TOPIC_AS_A_STUB_WITH_VARIANTS = 107;
    public static final int COPY_TOPIC_AS_A_STUB_WITH_OCCURRENCES = 108;
    public static final int COPY_TOPIC = 103;
    public static final int COPY_TOPIC_WITH_ASSOCIATIONS = 104;
    public static final int COPY_DEEP = 105;

    private static int copyCount = 0;
    private static WandoraToolLogger myLogger = null;
    
    private int mode = COPY_DEEP;
    
    private static boolean copyClasses = true;
    private static boolean copyInstances = true;
    private static boolean copyOccurrenceTypes = true;
    private static boolean copyAssociationTypes = true;
    private static boolean copyRoles = true;
    private static boolean copyPlayers = true;
    
    
    
    /**
     * Creates a new instance of CopyTopicsToLayer
     */
    public CopyTopicsToLayer() {
        mode = COPY_TOPIC_AS_A_STUB;
    }
    public CopyTopicsToLayer(int mymode) {
        mode = mymode;
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(wandora == null) return;
        Layer l = wandora.getTopicMap().getSelectedLayer();
        TopicMap target = l.getTopicMap();
        String layerName = l.getName();
        int depth = 1;
        copyCount = 0;
        
        
        if(mode == COPY_TOPIC_AS_A_STUB ||
           mode == COPY_TOPIC_AS_A_STUB_WITH_VARIANTS ||
           mode == COPY_TOPIC_AS_A_STUB_WITH_OCCURRENCES) {
            setDefaultLogger();
            Iterator topics = context.getContextObjects();
            if(topics != null && topics.hasNext()) {
                Topic t = null;
                log("Copying topics to topic map layer '"+layerName+"'.");
                while(topics.hasNext() && !forceStop()) {
                    try {
                        t = (Topic) topics.next();
                        if(t != null && !t.isRemoved()) {
                            setProgress(copyCount++);
                            log("Copying topic '"+getTopicName(t)+"'.");
                            Topic tt = target.createTopic();
                            for(Locator locator : t.getSubjectIdentifiers()) {
                                tt.addSubjectIdentifier(locator);
                            }
                            tt.setBaseName(t.getBaseName());
                            tt.setSubjectLocator(t.getSubjectLocator());
                            
                            if(COPY_TOPIC_AS_A_STUB_WITH_VARIANTS == mode) {
                                for(Set<Topic> scope : t.getVariantScopes()) {
                                    HashSet<Topic> ttScope = new LinkedHashSet();
                                    for(Topic scopeTopic : scope) {
                                        Topic ttScopeTopic = target.createTopic();
                                        ttScopeTopic.addSubjectIdentifier(scopeTopic.getFirstSubjectIdentifier());
                                        ttScope.add(ttScopeTopic);
                                    }
                                    if(!tt.isRemoved()) {
                                        tt.setVariant(ttScope, t.getVariant(scope));
                                    }
                                }
                            }
                            
                            if(COPY_TOPIC_AS_A_STUB_WITH_OCCURRENCES == mode) {
                                for(Topic occurrenceType : t.getDataTypes()) {
                                    for(Topic occurrenceScope : t.getData(occurrenceType).keySet()) {
                                        Topic ttScope = target.createTopic();
                                        ttScope.addSubjectIdentifier(occurrenceScope.getFirstSubjectIdentifier());
                                        Topic ttType = target.createTopic();
                                        ttType.addSubjectIdentifier(occurrenceType.getFirstSubjectIdentifier());
                                        if(!tt.isRemoved() && !ttType.isRemoved() && !ttScope.isRemoved()) {
                                            tt.setData(ttType, ttScope, t.getData(occurrenceType, occurrenceScope));
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
                log("Total "+copyCount+" topics copied to layer "+layerName);
            }
            else {
                log("Context didn't contain any topics. Nothing copied.");
            }
            setState(WAIT);
        }
        
        
        else if(mode == COPY_TOPIC_AS_A_SINGLE_SI_STUB ||
                mode == COPY_TOPIC_AS_A_SI_STUB) {
            setDefaultLogger();
            Iterator topics = context.getContextObjects();
            if(topics != null && topics.hasNext()) {
                Topic t = null;
                log("Copying topics to topic map layer '"+layerName+"'.");
                while(topics.hasNext() && !forceStop()) {
                    try {
                        t = (Topic) topics.next();
                        if(t != null && !t.isRemoved()) {
                            setProgress(copyCount++);
                            log("Copying topic '"+getTopicName(t)+"'.");
                            Topic tt = target.createTopic();
                            if(mode == COPY_TOPIC_AS_A_SINGLE_SI_STUB) {
                                tt.addSubjectIdentifier(t.getFirstSubjectIdentifier());
                            }
                            else {
                                for(Locator locator : t.getSubjectIdentifiers()) {
                                    tt.addSubjectIdentifier(locator);
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Total "+copyCount+" topics copied to layer "+layerName);
            }
            else {
                log("Context didn't contain any topics. Nothing copied.");
            }
            setState(WAIT);
        }
        
        
        else {
            if(mode == COPY_DEEP) {
                try {
                    GenericOptionsDialog god=new GenericOptionsDialog(wandora,
                        "Copy topics to layer",
                        "Copy context topics to current layer '"+layerName+"'.",
                        true,new String[][]{
                            new String[] { "Copy depth","string","1" },
                            new String[] { "Copy association types", "boolean", "true" },
                            new String[] { "Copy roles", "boolean", "true" },
                            new String[] { "Copy players", "boolean", "true" },
                            new String[] { "Copy instances", "boolean", "true" },
                            new String[] { "Copy classes", "boolean", "true" },
                            new String[] { "Copy occurrence types", "boolean", "true" },
                        },wandora);
                    god.setVisible(true);
                    if(god.wasCancelled()) return;
                    Map<String,String> values=god.getValues();        
                    depth=Integer.parseInt(values.get("Copy depth"));

                    copyRoles = Boolean.parseBoolean(values.get("Copy roles"));
                    copyPlayers = Boolean.parseBoolean(values.get("Copy players"));
                    copyAssociationTypes = Boolean.parseBoolean(values.get("Copy association types"));
                    copyClasses = Boolean.parseBoolean(values.get("Copy classes"));
                    copyInstances = Boolean.parseBoolean(values.get("Copy instances"));
                    copyOccurrenceTypes = Boolean.parseBoolean(values.get("Copy occurrence types"));
                }
                catch(Exception e) {
                    log(e);
                }
            }

            // QUICK HACK. SURPASSES THE UNDO/REDO.
            if(target instanceof UndoTopicMap) {
                target = ((UndoTopicMap) target).getWrappedTopicMap();
            }

            setDefaultLogger();
            myLogger = this;
            copyCount = 0;
            HashMap<Locator,Integer> copied = new HashMap<Locator,Integer>();
            Iterator topics = context.getContextObjects();
            if(topics != null && topics.hasNext()) {
                Topic t = null;
                log("Copying topics to topic map layer '"+layerName+"'.");
                while(topics.hasNext() && !forceStop()) {
                    try {
                        t = (Topic) topics.next();
                        if(t != null && !t.isRemoved()) {
                            if(mode == COPY_DEEP) {
                                copyTopicsIn(t,target,t.getTopicMap(),copied,depth);
                            }
                            else {
                                setProgress(copyCount++);
                                log("Copying topic '"+getTopicName(t)+"'.");
                                target.copyTopicIn(t,true);
                                if(mode == COPY_TOPIC_WITH_ASSOCIATIONS) {
                                    target.copyTopicAssociationsIn(t);
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Total "+copyCount+" topics copied to layer "+layerName);
                log("This includes association types, roles and players.");
            }
            else {
                log("Context didn't contain any topics. Nothing copied.");
            }
            setState(WAIT);
        }
    }
    
    
    
    
    public static void copyTopicsIn(Topic t,TopicMap target,TopicMap source, HashMap<Locator,Integer> copied,int depth) throws TopicMapException {
        if(myLogger != null && myLogger.forceStop()) return;
        if(depth>0){
            Integer olddepth=copied.get(t.getOneSubjectIdentifier());
            if(olddepth==null || depth>olddepth){
                for(Locator l : t.getSubjectIdentifiers()) copied.put(l,depth);
                if(olddepth==null) {
                    if(myLogger != null) {
                        myLogger.log("Copying topic '"+getTopicName(t)+"'.");
                        myLogger.setProgress(copyCount++);
                    }
                    target.copyTopicIn(t,true);
                    target.copyTopicAssociationsIn(t);
                }
                if(depth>1) {
                    // **** COPYING CLASSES ****
                    if(copyClasses) {
                        Collection<Topic> types = t.getTypes();
                        for(Iterator<Topic> typeIter = types.iterator(); typeIter.hasNext(); ) {
                            Topic topic = typeIter.next();
                            if(topic != null && !topic.isRemoved()) {
                                copyTopicsIn(topic,target,source,copied,depth-1);
                            }
                        }
                    }
                    // **** COPYING INSTANCES ****
                    if(copyInstances) {
                        Collection<Topic> instances = source.getTopicsOfType(t);
                        for(Iterator<Topic> instanceIter = instances.iterator(); instanceIter.hasNext(); ) {
                            if(myLogger!=null && myLogger.forceStop()) return;
                            Topic topic = instanceIter.next();
                            if(topic != null && !topic.isRemoved()) {
                                copyTopicsIn(topic,target,source,copied,depth-1);
                            }
                        }
                    }
                    // **** COPYING ASSOCIATION TYPES & ROLES & PLAYERS ****
                    for(Association a : t.getAssociations()){
                        if(myLogger!=null && myLogger.forceStop()) return;
                        if(copyAssociationTypes) {
                            copyTopicsIn(a.getType(),target,source,copied,depth-1);
                        }
                        if(copyRoles || copyPlayers) {
                            for(Topic role : a.getRoles()){
                                if(role != null && !role.isRemoved()) {
                                    if(copyRoles) {
                                        copyTopicsIn(role,target,source,copied,depth-1);
                                    }
                                    if(copyPlayers) {
                                        copyTopicsIn(a.getPlayer(role),target,source,copied,depth-1);
                                    }
                                }
                            }
                        }
                    }
                    // **** COPYING OCCURRENCE TYPES ****
                    if(copyOccurrenceTypes) {
                        Collection<Topic> occurrenceTypes = t.getDataTypes();
                        for(Iterator<Topic> occurrenceIter=occurrenceTypes.iterator(); occurrenceIter.hasNext(); ) {
                            Topic topic = occurrenceIter.next();
                            if(topic != null && !topic.isRemoved()) {
                                copyTopicsIn(topic,target,source,copied,depth-1);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/copy_topic.png");
    }
    
    
    @Override
    public String getName() {
        return "Copy topics to layer";
    }


    @Override
    public String getDescription() {
        if(mode == COPY_TOPIC_AS_A_SINGLE_SI_STUB) {
            return "Copy selected topics to active layer as a stub topics including only one subject identifier from the original topic.";
        }
        else if(mode == COPY_TOPIC_AS_A_SI_STUB) {
            return "Copy selected topics to active layer as a stub topics including only the subject identifiers from the original topic.";
        }
        else if(mode == COPY_TOPIC_AS_A_STUB) {
            return "Copy selected topics to active layer as a stub topics including the subject identifiers, the subject locator and the basename from the original topic.";
        }
        else if(mode == COPY_TOPIC_AS_A_STUB_WITH_VARIANTS) {
            return "Copy selected topics to active layer as a stub topics including the subject identifiers, the subject locator and the basename from the original topic. "+
                   "Variant names are copied too. Necessary scope topics will be created automatically.";
        }
        else if(mode == COPY_TOPIC_AS_A_STUB_WITH_OCCURRENCES) {
            return "Copy selected topics to active layer as a stub topics including the subject identifiers, the subject locator and the basename from the original topic. "+
                   "Occurrences are copied too. Necessary type and scope topics will be created automatically.";
        }
        else if(mode == COPY_TOPIC) {
            return "Copy selected topics to active layer. Copy includes subject identifiers, basenames, "+
                   "subject locators, variant names, types and instances. Necessary type and scope topics "+
                   "are created automatically.";
        }
        else if(mode == COPY_TOPIC_WITH_ASSOCIATIONS) {
            return "Copy selected topics and their association to active layer. Copies only stubs of associated topics.";
        }
        else if(mode == COPY_DEEP) {
            return "Copy selected topics and their associations to active layer. User must set the copy depth.";
        }
        else return "Copy selected topics to active layer.";
    }
}
