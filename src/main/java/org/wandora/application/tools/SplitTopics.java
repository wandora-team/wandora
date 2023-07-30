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
 * 
 * SplitTopics.java
 *
 * Created on 12.6.2006, 18:19
 *
 */

package org.wandora.application.tools;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 * WandoraTool splitting a topic with several subject identifiers. A new topic is
 * created for each subject identifier. Thus, split can be seen as a counter operation
 * of merge. To prevent immediate merge new topic's will be given a slightly modified
 * base name and subject locator. Split topics are similar in associations,
 * instances, classes and occurrences.
 *
 * @author akivela
 */
public class SplitTopics extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public boolean duplicateAssociations = true;
    public boolean copyInstances = true;
    
    public boolean askName=false;
    

    public SplitTopics() {
    }
    
    public SplitTopics(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_split.png");
    }

    @Override
    public String getName() {
        return "Split topic";
    }


    @Override
    public String getDescription() {
        return "Splits topic using subject identifiers.";
    }
    
    
    
    @Override
    public void execute(Wandora w, Context context) {
        duplicateAssociations = true;
        copyInstances = true;
        askName=false;
        
        Iterator topics = getContext().getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        
        TopicMap tm = w.getTopicMap();
        Topic topic = null;
        setDefaultLogger();
        while(topics.hasNext() && !forceStop()) {
            try {
                topic = (Topic) topics.next();
                if(topic != null) {
                    if(!topic.isRemoved()) {
                        Topic ltopic = tm.getTopic(topic.getOneSubjectIdentifier());
                        splitTopic(ltopic, tm, w);
                    }
                }
            } 
            catch(Exception e) {
                log(e);
            }
        }
    }
    

    
    public void splitTopic(Topic original, TopicMap topicMap, Wandora w)  throws TopicMapException {
        Collection<Locator> originalSIs = original.getSubjectIdentifiers();
        if(originalSIs == null || originalSIs.isEmpty() || originalSIs.size() == 1) {
            log("Topic '"+ getTopicName(original) +"' has only one subject identifier. Can't split.");
            return;
        }
        
        ArrayList<Locator> SIParts = new ArrayList<Locator>();
        SIParts.addAll(originalSIs);
        
        Topic split = null;
        
        for(Locator splitSI : SIParts) {
            String osplitSI = splitSI.toExternalForm();
            // --- copy topic and associations ---
            TopicMap splitMap = new org.wandora.topicmap.memory.TopicMapImpl();

            split = splitMap.copyTopicIn(original, false);
            if(duplicateAssociations) {
                Collection<Association> associations = original.getAssociations();
                if(associations != null && !associations.isEmpty()) {
                    for(Association association : associations) {
                        splitMap.copyAssociationIn(association);
                    }
                }
            }

            // --- resolve new subject base name ---
            if(original.getBaseName() != null) {
                String newBaseName = original.getBaseName() + " (split)";
                int c = 2;
                while(topicMap.getTopicWithBaseName(newBaseName) != null && c<10000) {
                    newBaseName = original.getBaseName() + " (split " + c + ")";
                    c++;
                }

                if(askName){
                    while(true){
                        String input=WandoraOptionPane.showInputDialog(w,"Enter new base name for the topic", newBaseName);
                        if(input==null) return;
                        newBaseName=input;
                        if(topicMap.getTopicWithBaseName(input)!=null){
                            int a=WandoraOptionPane.showConfirmDialog(w,"Topic with base name '"+input+"' already exists and will be merged with new topic. Do you want to continue?");
                            if(a==WandoraOptionPane.CANCEL_OPTION) return;
                            else if(a==WandoraOptionPane.YES_OPTION) break;
                        }
                        else break;
                    }
                }

                split.setBaseName(newBaseName);
            }

            // --- resolve new subject locator ---
            if(original.getSubjectLocator() != null) {
                int c = 2;
                Locator newSubjectLocator = new Locator(original.getSubjectLocator().toExternalForm() + "_copy");
                while(topicMap.getTopicBySubjectLocator(newSubjectLocator) != null && c<10000) {
                    newSubjectLocator = new Locator(original.getSubjectLocator().toExternalForm() + "_copy" + c);
                    c++;
                }
                split.setSubjectLocator(newSubjectLocator);
            }
           
            // --- resolve new subject identifiers ---
            for(Locator si : SIParts) {
                if(!si.equals(splitSI)) {
                    split.removeSubjectIdentifier(si);
                }
            }
            original.removeSubjectIdentifier(splitSI);
            
            //log("Merging splitted topic to original map...");
            topicMap.mergeIn(splitMap);
            split = topicMap.getTopic(splitSI);

            // --- attach instances ---
            if(split != null && copyInstances) {
                Collection<Topic> instances = topicMap.getTopicsOfType(original);
                if(instances != null && !instances.isEmpty()) {
                    for(Topic instance : instances) {
                        instance.addType(split);
                    }
                }
            }
        }
    }
    
}
