/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 * DeleteFromTopics.java
 *
 * Created on 6. tammikuuta 2005, 12:47
 */

package org.wandora.application.tools;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;



/**
 *
 * @author  akivela
 */
public class DeleteFromTopics extends AbstractWandoraTool {


	private static final long serialVersionUID = 1L;

	public static final int LOOSE_INSTANCES = 100;
    public static final int LOOSE_INSTANCES_IN_CONTEXT = 101;
    public static final int DELETE_INSTANCE_TOPICS = 110;
    
    public static final int LOOSE_CLASSES = 120;
    public static final int LOOSE_SINGLE_CLASS = 130;
    public static final int LOOSE_CLASSES_OF_CURRENT = 140;
    
    public static final int DELETE_TYPED_TEXTDATA = 21;
    public static final int DELETE_TEXTDATAS = 22;
    
    public static final int DELETE_TYPED_ASSOCIATIONS_OF_CURRENT = 3800;

    //public static final int DELETE_TYPED_ASSOCIATIONS = 320;
    //public static final int DELETE_ASSOCIATIONS_OF_TYPE = 350;
    
    public static final int DELETE_ASSOCIATED_TOPICS = 380;
    
    //public static final int DELETE_SLS = 41;
    
    //public static final int DELETE_SIS_WITH_REGEX = 800;
    
    
    public int whatToDelete = 0;

    TopicMap topicMap = null;
    Topic topicOpen = null;
    boolean requiresRefresh = false;
    
    
    public DeleteFromTopics() {
        whatToDelete = 0;
    }
    public DeleteFromTopics(int orders) {
        whatToDelete = orders;
    }
    public DeleteFromTopics(Context<?> preferredContext, int orders) {
        setContext(preferredContext);
        whatToDelete = orders;
    }
    
    
        
    
    
    @Override
    public String getName() {
        return "Delete from topics";
    }

    @Override
    public String getDescription() {
        return "Delete selected elements such as instances in selected topics.";
    }
    

    
    
    @Override
    public void execute(Wandora wandora, Context<?> context) throws TopicMapException {
        Topic topic = null;
        Iterator<?> topics = context.getContextObjects();

        // Delete from topics.
        switch(whatToDelete) {
            case LOOSE_INSTANCES: {
                try {
                    topic = (Topic) topics.next();
                    if(WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to delete all instances of '" + getTopicName(topic) + "'?","Confirm instance delete", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION) {
                        Collection<Topic> instances = topic.getTopicMap().getTopicsOfType(topic);
                        Topic instance = null;
                        Iterator<Topic> it=instances.iterator();
                        while(it.hasNext() && !forceStop()) {
                            try {
                                instance = (Topic) it.next();
                                if( !instance.isRemoved() ) {
                                    instance.removeType(topic);
                                    requiresRefresh = true;
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    log(e);
                }
                break;
            }

            
            case LOOSE_INSTANCES_IN_CONTEXT: {
                Topic openTopic = wandora.getOpenTopic();
                if(openTopic != null) {
                    if(WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to delete selected instances of '" + getTopicName(openTopic) + "'?","Confirm instance delete", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                        setDefaultLogger();
                        Topic instance = null;
                        while(topics.hasNext() && !forceStop()) {
                            try {
                                instance = (Topic) topics.next();
                                if( !instance.isRemoved() ) {
                                    log("Deleting instance '"+ getTopicName(instance) +"' from topic '" + getTopicName(openTopic) + "'");
                                    instance.removeType(openTopic);
                                    requiresRefresh = true;
                                }
                            }
                            catch (Exception e) {
                                log("Exception occurred while deleting instances from topic", e);
                            }
                        }
                        setState(WAIT);
                    }
                }
                break;
            }
            
            
            case DELETE_INSTANCE_TOPICS: {
                if(WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to delete all instance topics?","Confirm delete", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                    TopicMap tm = wandora.getTopicMap();
                    setDefaultLogger();
                    while(topics.hasNext() && !forceStop()) {
                        Topic typeTopic = (Topic) topics.next();
                        Iterator<Topic> iter=new ArrayList<>(tm.getTopicsOfType(typeTopic)).iterator();
                        Topic t = null;
                        List<Topic> deleteThese = new ArrayList<>();
                        while(iter.hasNext() && !forceStop()) {
                            t=(Topic)iter.next();
                            if(!t.isRemoved() && t.isOfType(typeTopic)) {
                                deleteThese.add(t);
                                hlog("Preparing instance topic " + getTopicName(t) + "");
                            }
                        }
                        int s = deleteThese.size();
                        for(int i=0; i<s && !forceStop(); i++) {
                            try {
                                topic = (Topic) deleteThese.get(i);
                                if(!topic.isRemoved() && topic.isDeleteAllowed()) {
                                    log("Deleting instance topic '" + getTopicName(topic) + "'");
                                    topic.remove();
                                    requiresRefresh = true;
                                }
                            }
                            catch (Exception e) {
                                log(e);
                            }
                        }
                    }
                    setState(WAIT);
                }
                break;
            }
            
            
            case LOOSE_CLASSES_OF_CURRENT: {
                Topic currentTopic = wandora.getOpenTopic();
                Topic typeTopic = null;
                if(currentTopic != null) {
                    String message = "Delete selected classes from topic "+getTopicName(currentTopic)+"?";
                    int r = WandoraOptionPane.showConfirmDialog(wandora, message, "Confirm delete", WandoraOptionPane.YES_NO_OPTION);
                    if(r == WandoraOptionPane.YES_OPTION) {
                        setDefaultLogger();
                        while(topics.hasNext() && !forceStop()) {
                            try {
                                typeTopic = (Topic) topics.next();
                                if(typeTopic != null && !typeTopic.isRemoved()) {
                                    currentTopic.removeType(typeTopic);
                                    log("Deleting class '" + getTopicName(typeTopic) + "' from topic '" + getTopicName(currentTopic) + "'");
                                    requiresRefresh = true;
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        setState(WAIT);
                    }
                }
                break;
            }
            
            
            case LOOSE_SINGLE_CLASS: {
/*                BaseNamePrompt prompt=new BaseNamePrompt(admin.getManager(), admin, true);
                prompt.setTitle("Select class to be removed from topics...");
                prompt.setVisible(true);
                Topic t=prompt.getTopic();*/
                Topic t=wandora.showTopicFinder("Select class to be removed from topics...");                
                if (t == null) return;

                setDefaultLogger();
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(!topic.isRemoved()) {
                            log("Deleting class '" + getTopicName(t) + "' from topic '" + getTopicName(topic) + "'");
                            topic.removeType(t);
                            requiresRefresh = true;
                        }
                    }
                    catch (Exception e) {
                        log("Exception occurred while deleting class", e);
                        try { Thread.sleep(1000); }
                        catch (Exception timeout) {}
                    }
                }
                setState(WAIT);
                break;
            }
            

            case LOOSE_CLASSES: {
                Topic classTopic = null;
                setDefaultLogger();
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        Collection<Topic> classTopics = topic.getTypes();
                        if(classTopics != null) {
                            Iterator<Topic> classIterator = classTopics.iterator();
                            while(classIterator.hasNext()) {
                                classTopic = classIterator.next();
                                if(classTopic != null && !classTopic.isRemoved()) {
                                    log("Deleting class '" + getTopicName(classTopic) + "' from topic '" + getTopicName(topic) + "'");
                                    topic.removeType(classTopic);
                                    requiresRefresh = true;
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        log("Exception occurred while deleting classes from topic", e);
                        try { Thread.sleep(1000); }
                        catch (Exception timeout) {}
                    }
                }
                setState(WAIT);
                break;
            }
            
            case DELETE_TYPED_TEXTDATA: {
                Topic occurrenceType=wandora.showTopicFinder("Select occurrence type to be removed from topics...");                
                if(occurrenceType == null) return;
                setDefaultLogger();
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            log("Deleting occurrence of type '" + getTopicName(occurrenceType) + "' from topic '" + getTopicName(topic) + "'");
                            topic.removeData(occurrenceType);
                            requiresRefresh = true;
                        }
                    }
                    catch (Exception e) {
                        log("Exception occurred while deleting occurrence", e);
                        try { Thread.sleep(1000); }
                        catch (Exception timeout) {}
                    }
                }
                setState(WAIT);
                break;
            }
            
            
            case DELETE_TEXTDATAS: {
                if(WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to delete all occurrences?","Confirm delete", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                    setDefaultLogger();
                    while(topics.hasNext() && !forceStop()) {
                        try {
                            topic = (Topic) topics.next();
                            hlog("Inspecting topic '"+getTopicName(topic)+"'.");
                            Collection<Topic> types = topic.getDataTypes();
                            for(Iterator<Topic> i2 = types.iterator(); i2.hasNext(); ) {
                                Topic type = (Topic) i2.next();
                                if(type != null && !type.isRemoved()) {
                                    log("Deleting occurrences from topic '"+getTopicName(topic)+"'");
                                    topic.removeData(type);
                                    requiresRefresh = true;
                                }
                            }
                        }
                        catch (Exception e) {
                            log("Exception occurred while deleting occurrences", e);
                            try { Thread.sleep(1000); }
                            catch (Exception timeout) {}
                        }
                    }
                    setState(WAIT);
                }
                break;
            }

            case DELETE_TYPED_ASSOCIATIONS_OF_CURRENT: {
                setDefaultLogger();
                Topic associationType = null;
                Topic currentTopic = null;
                String tname = null;
                try {
                    
                    associationType=(Topic) context.getContextObjects().next();
                    tname = getTopicName(associationType);
                    currentTopic = wandora.getOpenTopic();
                    log("Deleting associations of type '" + tname + "' from topic '" + getTopicName(currentTopic) +"'");
                    Collection<Association> associations = currentTopic.getAssociations(associationType);
                    for(Iterator<Association> i2 = associations.iterator(); i2.hasNext() && !forceStop(); ) {
                        Association association = (Association) i2.next();
                        if(association != null && !association.isRemoved()) {
                            association.remove();
                            requiresRefresh = true;
                        }
                    }
                }
                catch (Exception e) {
                    log("Exception '" + e.toString() + "'\n occurred while deleting associations", e);
                    try { Thread.sleep(1000); }
                    catch (Exception timeout) {}
                }
                setState(WAIT);
                break;
            }
            
            
            case DELETE_ASSOCIATED_TOPICS: {
                if(WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to delete associated topics?","Confirm delete", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                    setDefaultLogger();
                    while(topics.hasNext() && !forceStop()) {
                        topic = (Topic) topics.next();
                        Collection<Association> assocs = topic.getAssociations();
                        Association a;
                        Topic player;
                        ArrayList<Association> assocv = new ArrayList<>();
                        ArrayList<Topic> topicv = new ArrayList<>();
                        for(Iterator<Association> iter = assocs.iterator(); iter.hasNext();) {
                            a = (Association) iter.next();
                            Collection<Topic> roles = a.getRoles();
                            for(Iterator<Topic> roleiter = roles.iterator(); roleiter.hasNext();) {
                                player = (Topic) a.getPlayer((Topic) roleiter.next());
                                if (!player.equals(topic)) {
                                    hlog("Preparing topic '" + getTopicName(player) + "'");
                                    topicv.add(player);
                                }
                            }
                            assocv.add(a);
                        }
                        int c = assocv.size();
                        for(int i=0; i<c; i++) {
                            a = (Association) assocv.get(i);
                            log("Deleting association of found topic! "+ (c-i) + " associations to delete.");
                            a.remove();
                            requiresRefresh = true;
                        }
                        c = topicv.size();
                        for(int i=0; i<c; i++) {
                            try {
                                player = (Topic) topicv.get(i);
                                if(!player.isRemoved() && player.isDeleteAllowed()) {
                                    log("Deleting topic '" + getTopicName(player) + "'. " + (c-i) + " topics to delete.");
                                    player.remove();
                                    requiresRefresh = true;
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                    }
                    setState(WAIT);
                }
            }
            
            // REFACTORED TO SubjectLocatorRemover
            /*
            case DELETE_SLS: {
                if(WandoraOptionPane.showConfirmDialog(admin,"Are you sure you want to delete subject locators\nfrom all instance topics?","Confirm delete", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                    int c = 0;
                    setDefaultLogger();
                    while(topics.hasNext() && !forceStop()) {
                        try {
                            c++;
                            topic = (Topic) topics.next();
                            log("Deleting subject locators from " + getTopicName(topic) + ", " + c + " done...");
                            topic.setSubjectLocator(null);
                            requiresRefresh = true;
                        }
                        catch (Exception e) {
                            log(e);
                        }
                    }
                    setState(WAIT);
                }
                else {
                    return;
                }
                break;
            }
            */
            
            
            default: {
                log("Delete from all instances cathed illegal action type (" + whatToDelete + ").");
            }
             
        }       
    }
    
      
    /*
    public Topic[] makeTopicArray(Collection collection) {
        Topic[] topics = new Topic[0];
        if(collection != null) {
            topics = new Topic[collection.size()];
            int i = 0;
            for(Iterator it = collection.iterator(); it.hasNext(); ) {
                try {
                    topics[i] = (Topic) it.next();
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
        return topics;
    }
    
    */
    

    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
}
