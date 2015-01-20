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
 * DeleteTopics.java
 *
 * Created on September 22, 2004, 12:33 PM
 */

package org.wandora.application.tools;


import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import java.util.*;
import javax.swing.*;
import org.wandora.utils.Tuples.T2;

/**
 *
 * @author  akivela
 */
public class DeleteTopics extends AbstractWandoraTool implements WandoraTool {

    public boolean forceDelete = true;
    public boolean confirm = true;
    public boolean shouldContinue = true;
    
    protected Wandora admin = null;
    private String topicName = null;
       
    public DeleteTopics() {
        //setContext(new TopicContext());
    }
    public DeleteTopics(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_delete.png");
    }

    @Override
    public String getName() {
        return "Delete topics";
    }

    @Override
    public String getDescription() {
        return "Delete topics.";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        this.admin = admin;
        ArrayList<Topic> topicsToDelete = new ArrayList<Topic>();
        Iterator topics = context.getContextObjects();
        Topic topic = null;
        Topic ltopic = null;
        int count = 0;
        ConfirmResult r_all = null;
        ConfirmResult r = null;
        yesToAll = false;
        shouldContinue = true;
        
        if(topics != null && topics.hasNext()) {
            while(topics.hasNext() && shouldContinue && !forceStop()) {
                topic = (Topic) topics.next();
                if(topic != null && !topic.isRemoved()) {
                    if(topic instanceof LayeredTopic) {
                        ltopic = ((LayeredTopic) topic).getTopicForSelectedLayer();
                        if(ltopic == null || ltopic.isRemoved()) {
                            int answer = WandoraOptionPane.showConfirmDialog(admin,"Topic '"+getTopicName(topic)+"' doesn't exist in selected layer. Would you like to proceed deleting other topics?", "Topic not in selected layer", WandoraOptionPane.OK_CANCEL_OPTION);
                            if(answer == WandoraOptionPane.CANCEL_OPTION) shouldContinue = false;
                            continue;
                        }
                        else {
                            topic = ltopic;
                        }
                    }
                    topicName = getTopicName(topic);
                    //hlog("Investigating topic '" + topicName + "'.");
                    if(topic.isDeleteAllowed()) {
                        if(shouldDelete(topic)) {
                            try {
                                if(r_all == null) r = TMBox.checkTopicRemove(admin,topic);
                                else r = r_all;

                                if(r == ConfirmResult.cancel) break;
                                else if(r == ConfirmResult.no) continue;
                                else if(r == ConfirmResult.notoall) { r_all = r; continue; }
                                else if(r == ConfirmResult.yestoall) { r_all = r; }

                                topicsToDelete.add(topic);
                                count++;
                            }
                            catch(Exception e2) {
                                log(e2);
                            }
                        }
                    }
                    else {
                        int answer = WandoraOptionPane.showConfirmDialog(admin,"Unable to delete topic '"+getTopicName(topic)+"'. Topic is an occurrent type or association role or association type or has instances. Would you like to remove related occurrences, associations and instances to ensure that the topic can be deleted?", "Ensure topic deletion", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                        if(answer == WandoraOptionPane.YES_OPTION) {
                            prepareTopicRemove(topic, admin);
                            topicsToDelete.add(topic);
                            count++;
                        }
                        else if(answer == WandoraOptionPane.NO_OPTION) continue;
                        else if(answer == WandoraOptionPane.CANCEL_OPTION) shouldContinue = false;
                        else shouldContinue = false;
                    }
                }
            }
            if((count > 0 || shouldContinue) && !forceStop()) {
                setDefaultLogger();
                int rcount = 0;
                for(int i=0; i<count && !forceStop(); i++) {
                    try {
                        topic = topicsToDelete.get(i);
                        if(topic != null && !topic.isRemoved()) {
                            topic.remove();
                            rcount++;
                            if((i % 100) == 0) hlog((count-i) + " topics to delete.");
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Total " + rcount + " topics deleted!");
                setState(WAIT);
            }
        }
        topicsToDelete = null;
    }
    
    
    
    public boolean shouldDelete(Topic topic)  throws TopicMapException {
        if(confirm) {
            return confirmDelete(topic);
        }
        else {
            return true;
        }
    }
    
    public boolean yesToAll = false;
    public boolean confirmDelete(Topic topic) throws TopicMapException  {
        if(yesToAll) {
            return true;
        }
        else {
            setState(INVISIBLE);
            //topicName = topic.getBaseName();
            //if(topicName == null) topicName = topic.getOneSubjectIdentifier().toExternalForm();

            String confirmMessage = "Would you like delete topic '" + topicName + "' in selected layer?";
            int answer = WandoraOptionPane.showConfirmDialog(admin, confirmMessage,"Confirm delete", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
            setState(VISIBLE);
            if(answer == WandoraOptionPane.YES_OPTION) {
                return true;
            }
            if(answer == WandoraOptionPane.YES_TO_ALL_OPTION) {
                yesToAll = true;
                return true;
            }
            else if(answer == WandoraOptionPane.CANCEL_OPTION) {
                shouldContinue = false;
            }
            return false;
        }
    }
    
    
    public void removeClasses(Topic t)  throws TopicMapException {
        Collection<Topic> types = t.getTypes();
        if(types != null) {
            Iterator<Topic> typeIterator = types.iterator();
            while(typeIterator.hasNext()) {
                t.removeType(typeIterator.next());
            }
        }
    }
    
    
    public void removeInstances(Topic t)  throws TopicMapException {
        Collection<Topic> instances = t.getTopicMap().getTopicsOfType(t);
        if(instances != null) {
            Iterator<Topic> instanceIterator = instances.iterator();
            while(instanceIterator.hasNext()) {
                instanceIterator.next().removeType(t);
            }
        }
    }
    
    
    
    protected void prepareTopicRemove(Topic to, Wandora w) throws TopicMapException {
        if(to == null || w == null) return;
        
        TopicMap tm = w.getTopicMap();
        if(tm == null) return;
        Topic t = tm.getTopic(to.getOneSubjectIdentifier());
        if(t == null) return;
        

        Collection<Topic> instances = tm.getTopicsOfType(t);
        for(Topic instance : instances) {
            instance.removeType(t);
        }
        Iterator<Association> associations = tm.getAssociations();
        Association association = null;
        ArrayList<Association> asociationsToBeRemoved = new ArrayList();
        while(associations.hasNext()) {
            association = associations.next();
            if(t.mergesWithTopic(association.getType()) || association.getPlayer(t) != null) {
                asociationsToBeRemoved.add(association);
            }
        }
        for(Association a : asociationsToBeRemoved) {
            a.remove();
        }
        Iterator<Topic> topics = tm.getTopics();
        Topic topic;
        while(topics.hasNext()) {
            topic = topics.next();
            if(!topic.getData(t).isEmpty()) {
                topic.removeData(t);
            }
            Collection<Topic> dataTypes = topic.getDataTypes();
            for(Topic dataType : dataTypes) {
                topic.removeData(dataType, t);
            }
            Set<Set<Topic>> variantScopes = topic.getVariantScopes();
            for(Set<Topic> variantScope : variantScopes) {
                if(variantScope.contains(t)) {
                    String variant = topic.getVariant(variantScope);
                    topic.removeVariant(variantScope);
                    variantScope.remove(t);
                    if(!variantScope.isEmpty()) {
                        if(topic.getVariant(variantScope) == null) {
                            topic.setVariant(variantScope, variant);
                        }
                    }
                }
            }
        }
    }
}
