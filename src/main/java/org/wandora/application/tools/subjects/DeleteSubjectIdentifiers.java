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
 * DeleteSubjectIdentifiers.java
 *
 * Created on 21.7.2006, 16:57
 *
 */

package org.wandora.application.tools.subjects;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.SIContext;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.layered.LayeredTopic;


/**
 * Deletes subject identifiers. Exact behavior depends on given context. If
 * context is <code>SIContext</code> the tools deletes all subject
 * identifiers in their topics (unless the subject identifier is the only subject
 * identifier in the topic). If context contains topics the tool deletes all but one 
 * subject identifiers of context topics.
 *
 * @author akivela
 */
public class DeleteSubjectIdentifiers extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;




	public DeleteSubjectIdentifiers() {
    }
    public DeleteSubjectIdentifiers(Context context) {
        setContext(context);
    }
    

    @Override
    public String getName() {
        return "Delete subject identifiers";
    }

    @Override
    public String getDescription() {
        return "Removes subject identifiers in topics.";
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = wandora.getTopicMap();
        
        if(context instanceof SIContext) {
            Iterator<Locator> subjectIdentifiers = context.getContextObjects();
            Collection<Locator> subjectIdentifiersToDelete = getSubjectIdentifiers(subjectIdentifiers);
            if(subjectIdentifiersToDelete != null && !subjectIdentifiersToDelete.isEmpty()) {
                int deleteCount = 0;
                int siCount = 0;
                int shouldDelete = WandoraOptionPane.NO_OPTION;
                for(Locator si : subjectIdentifiersToDelete) {
                    if(si != null) {
                        siCount++;
                        Topic topic = topicmap.getTopic(si);
                        if(topic != null && !topic.isRemoved()) {
                            if(topic.getSubjectIdentifiers().size() > 1) {
                                if(shouldDelete != WandoraOptionPane.YES_TO_ALL_OPTION) {
                                    shouldDelete = WandoraOptionPane.showConfirmDialog(wandora, getConfirmMessage(topic, si), "Confirm delete", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                }
                                if(shouldDelete == WandoraOptionPane.YES_TO_ALL_OPTION || shouldDelete == WandoraOptionPane.YES_OPTION) {
                                    topic.removeSubjectIdentifier(si);
                                    deleteCount++;
                                }
                                if(shouldDelete == WandoraOptionPane.CANCEL_OPTION || shouldDelete == WandoraOptionPane.CLOSED_OPTION) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if(siCount == deleteCount) {
                    log("Deleted " + deleteCount + " subject identifiers.");
                }
                else {
                    log("Deleted " + deleteCount + " subject identifiers out of "+siCount+" inspected.");
                }
            }
            else {
                log("Found no subject identifiers to delete.");
            }
        }
        
        // ***** HANDLE OTHER CONTEXTS *****
        else {
            Iterator<Topic> topics = getContext().getContextObjects();
            Topic topic = null;
            Topic topicInSelectedLayer = null;
            int deleteCount = 0;
            int inspectionCount = 0;
            ConfirmResult r_all = null;
            ConfirmResult r = null;
            String topicName = null;
            int shouldDelete = WandoraOptionPane.NO_OPTION;

            if(topics != null) {
                do {
                    topic = topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        // First solve local topic in selected layer. If there is
                        // no local topic, ask user if she wishes to cancel to deletion.
                        if(topic instanceof LayeredTopic) {
                            topicInSelectedLayer = ((LayeredTopic) topic).getTopicForSelectedLayer();
                            if(topicInSelectedLayer == null || topicInSelectedLayer.isRemoved()) {
                                int a = WandoraOptionPane.showConfirmDialog(wandora, "Topic '"+TopicToString.toString(topic)+"' doesn't exist in selected layer.", "Topic not in selected layer", WandoraOptionPane.OK_CANCEL_OPTION);
                                if(a == WandoraOptionPane.CANCEL_OPTION) break;
                                continue;
                            }
                            else {
                                topic = topicInSelectedLayer;
                            }
                        }
                        
                        // Local topic exists and Wandora can continue with the
                        // topic. First Wandora creates a collection of topic's
                        // subject identifiers and then iterates the collection
                        // in order to delete subject identifiers in the collection.
                        // Notice, we check if the topic has more than one subject
                        // identifier and prevent deletion if there is only one
                        // subject locator.
                        inspectionCount++;
                        Collection<Locator> sis = new ArrayList<>(getSubjectIdentifiers(topic));
                        int localDeleteCount = 0;
                        
                        for(Locator si : sis) {
                            if(topic.getSubjectIdentifiers().size() > 1) {
                                if(shouldDelete != WandoraOptionPane.YES_TO_ALL_OPTION) {
                                    shouldDelete = WandoraOptionPane.showConfirmDialog(wandora, getConfirmMessage(topic, si), "Delete subject identifier?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                }
                                if(shouldDelete == WandoraOptionPane.YES_TO_ALL_OPTION || shouldDelete == WandoraOptionPane.YES_OPTION) {
                                    try {
                                        if(r_all == null) r = TMBox.checkSubjectIdentifierChange(wandora, topic, si, false);
                                        else r = r_all;

                                        if(r == ConfirmResult.cancel) break;
                                        else if(r == ConfirmResult.no) continue;
                                        else if(r == ConfirmResult.notoall) { r_all = r; continue; }
                                        else if(r == ConfirmResult.yestoall) { r_all = r; }

                                        topic.removeSubjectIdentifier(si);
                                        localDeleteCount++;
                                        deleteCount++;
                                    }
                                    catch(Exception e2) {
                                        log(e2);
                                    }
                                }
                            }
                        }
                        
                        // Progress is logged only if the user has selected to 
                        // delete all subject identifiers.
                        if(shouldDelete == WandoraOptionPane.YES_TO_ALL_OPTION) {
                            topicName = TopicToString.toString(topic);
                            setDefaultLogger();
                            if(localDeleteCount == 0) {
                                log("Deleted no subject identifiers in topic '" + topicName + "'.");
                            }
                            else if(localDeleteCount == 1) {
                                log("Deleted one subject identifiers in topic '" + topicName + "'.");
                            }
                            else {
                                log("Deleted "+localDeleteCount+" subject identifiers in topic '" + topicName + "'.");
                            }
                        }
                    }
                }
                while(topics.hasNext() && !forceStop() && 
                        shouldDelete != WandoraOptionPane.CANCEL_OPTION && 
                        shouldDelete != WandoraOptionPane.CLOSED_OPTION);
            }
            setDefaultLogger();
            log("Investigated " + inspectionCount + " topics.");
            log("Deleted " + deleteCount + " subject identifiers.");
            log("Ready.");
            setState(WAIT);
        }
        
        
    }
    
    
    
    
    /**
     * Method is used to get a collection of subject identifier out of topic
     * that should be deleted.
     * Extending class implementing a specific subject identifier remover should
     * override this method.
     * 
     * @param topic
     * @return Collection of locators
     * @throws TopicMapException 
     */
    protected Collection<Locator> getSubjectIdentifiers(Topic topic) throws TopicMapException {
        ArrayList<Locator> subjectIdentifiersToDelete = new ArrayList<>();
        Iterator<Locator> subjectIdentifiersOfTopic = topic.getSubjectIdentifiers().iterator();
        subjectIdentifiersOfTopic.next(); // Hop over == save first locator
        while(subjectIdentifiersOfTopic.hasNext()) {
            subjectIdentifiersToDelete.add(subjectIdentifiersOfTopic.next());
        }
        return subjectIdentifiersToDelete;
    }
    
    
    /**
     * If tool context is SIContext the tool can narrow the set of deleted subject
     * identifiers here. Method takes the context objects as argument and returns
     * a collection of locators i.e. subject identifiers that should be deleted.
     * Extending class implementing a specific subject identifier remover should
     * override this method.
     * 
     * @param subjectIdentifiers
     * @return
     * @throws TopicMapException 
     */
    protected Collection<Locator> getSubjectIdentifiers(Iterator<Locator> subjectIdentifiers) throws TopicMapException {
        ArrayList<Locator> subjectIdentifiersToDelete = new ArrayList<>();
        while(subjectIdentifiers.hasNext()) {
            subjectIdentifiersToDelete.add(subjectIdentifiers.next());
        }
        return subjectIdentifiersToDelete;
    }
    
    
    
    
    protected String getConfirmMessage(Topic topic, Locator si) {
        String topicName = TopicToString.toString(topic);
        String siString = si.toExternalForm();
        if(siString.length() > 256) {
            siString = siString.substring(0, 256) + "...";
        }
        String confirmMessage = "Delete topic's '" + topicName + "' subject identifier '" + siString + "'?";
        return confirmMessage;
    }

    
}
