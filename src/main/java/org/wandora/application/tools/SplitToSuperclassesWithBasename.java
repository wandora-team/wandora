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
 * SplitToSuperclassesWithBasename.java
 *
 * Created on 2008-09-19, 16:41
 *
 */


package org.wandora.application.tools;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import org.wandora.topicmap.XTMPSI;


/**
 * WandoraTool splitting a topic with a regular expression applied to topic's base name.
 * As a result, topic map contains one topic for each identified base name part.
 * To prevent immediate merge subject identifiers and subject locators are
 * modified a bit. Topics created out of base name parts are associated with
 * ascending/descending sub-superclass relation. This tool can be used to expose a sub-superclass
 * relation expressed implicitly with a base name.
 * 
 * @author akivela
 */


public class SplitToSuperclassesWithBasename extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	public boolean descending = false;
    public boolean duplicateAssociations = false;
    public boolean copyInstances = false;
    public boolean copyClasses = false;
    
    public boolean askName=false;
    public String splitString = "";
    private int topicCounter = 0;
    private int splitCounter = 0;
    
    
    

    public SplitToSuperclassesWithBasename(boolean desc) {
        this.descending = desc;
    }
    public SplitToSuperclassesWithBasename() {
    }
    public SplitToSuperclassesWithBasename(Context preferredContext, boolean desc) {
        this.descending = desc;
        setContext(preferredContext);
    }
    public SplitToSuperclassesWithBasename(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_split.png");
    }

    @Override
    public String getName() {
        return "Split topic to "+(descending ? "descending" : "ascending")+" superclasses with base name";
    }

    @Override
    public String getDescription() {
        return "Tool splits topics to "+(descending ? "descending" : "ascending")+" superclass chains with base name.";
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        Iterator topics = getContext().getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        
        splitString = WandoraOptionPane.showInputDialog(
        		wandora, 
        		"Enter regular expression string used to split base name:", 
        		splitString);
        
        if(splitString == null || splitString.length() == 0) return;
        
        TopicMap tm = wandora.getTopicMap();
        Topic topic = null;
        topicCounter = 0;
        splitCounter = 0;
        setDefaultLogger();
        while(topics.hasNext() && !forceStop()) {
            try {
                topic = (Topic) topics.next();
                if(topic != null) {
                    if(!topic.isRemoved()) {
                        Topic ltopic = tm.getTopic(topic.getOneSubjectIdentifier());
                        splitTopic(ltopic, splitString, tm, wandora);
                    }
                }
            } catch(Exception e) {
                log(e);
            }
        }
        if(topicCounter == 0) log("No topics to split.");
        else if(topicCounter == 1) log("One topic splitted.");
        else log("Total "+topicCounter+" topics splitted.");
        
        if(splitCounter == 0) log("Created no splitted topics.");
        else if(splitCounter == 1) log("Created one splitted topic.");
        else log("Created total "+splitCounter+" splitted topics.");
        setState(WAIT);
    }
    

    
    
    
    public void splitTopic(Topic original, String splitString, TopicMap topicMap, Wandora admin)  throws TopicMapException {
        if(original == null) return;
        if(original.getBaseName() == null) return;
        
        
        String[] splitParts = original.getBaseName().split(splitString);
        if(splitParts.length < 2) {
            log("No split parts for topic '"+ getTopicName(original) +"'!");
            return;
        }
        
        Topic previousSplit = null;
        Topic split = null;
        String splitBasename = null;

        for(int i=0; i<splitParts.length-1; i++) {
            splitBasename = splitParts[i];
            if(splitBasename == null || splitBasename.length() == 0) {
                log("Invalid zero length split part for topic '"+ getTopicName(original) +"'!");
                continue;
            }

            // --- copy topic and associations ---
            TopicMap splitMap = new org.wandora.topicmap.memory.TopicMapImpl();
            splitCounter++;
            
            split = splitMap.copyTopicIn(original, false);
            if(duplicateAssociations) {
                Collection<Association> assocs = original.getAssociations();
                Association a;
                for(Iterator<Association> iter = assocs.iterator(); iter.hasNext();) {
                    a = (Association) iter.next();
                    splitMap.copyAssociationIn(a);
                }
            }

            // --- resolve new subject base name ---
            String newBaseName = splitBasename;
            
            if(askName){
                while(true){
                    String input=WandoraOptionPane.showInputDialog(admin,"Enter new base name for the topic", newBaseName);
                    if(input==null) return;
                    newBaseName=input;
                    if(topicMap.getTopicWithBaseName(input)!=null){
                        int a=WandoraOptionPane.showConfirmDialog(admin,"Topic with base name '"+input+"' already exists and will be merged with new topic. Do you want to continue?");
                        if(a==WandoraOptionPane.CANCEL_OPTION) return;
                        else if(a==WandoraOptionPane.YES_OPTION) break;
                    }
                    else break;
                }
            }

            split.setBaseName(newBaseName);

            // --- resolve new subject locator ---
            if(original.getSubjectLocator() != null) {
                int c = 2;
                Locator newSubjectLocator = new Locator(original.getSubjectLocator().toExternalForm() + "_split");
                while(topicMap.getTopicBySubjectLocator(newSubjectLocator) != null && c<10000) {
                    newSubjectLocator = new Locator(original.getSubjectLocator().toExternalForm() + "_split" + c);
                    c++;
                }
                split.setSubjectLocator(newSubjectLocator);
            }
           
            // --- resolve new subject identifiers ---
            Collection<Locator> sis = split.getSubjectIdentifiers();
            List<Locator> siv = new ArrayList<>();
            for(Iterator<Locator> iter = sis.iterator(); iter.hasNext(); ) {
                siv.add(iter.next());
            }
            Locator lo = null;
            Locator l = null;
            for(int j=0; j<siv.size(); j++) {
                lo = (Locator) siv.get(j);
                l = (Locator) new Locator(lo.toExternalForm() + "_split");
                int c = 2;
                while((topicMap.getTopic(l) != null || splitMap.getTopic(l) != null) && c<10000) {
                    l = (Locator) new Locator(lo.toExternalForm() + "_split" + c);
                    c++;
                }
                split.addSubjectIdentifier(l);
                split.removeSubjectIdentifier(lo);
            }

            //log("Merging splitted topic to original map...");
            topicMap.mergeIn(splitMap);
            split = topicMap.getTopicWithBaseName(newBaseName);
            
            // --- attach instances ---
            if(split != null && copyInstances) {
                Collection<Topic> col = topicMap.getTopicsOfType(original);
                Iterator<Topic> iter=col.iterator();
                Topic t = null;
                while(iter.hasNext()) {
                    t=(Topic)iter.next();
                    if(t.isOfType(original)) {
                        t.addType(split);
                    }
                }
            }
            
            if(previousSplit != null) {
                if(descending)
                    makeSubclassOf(previousSplit.getTopicMap(), split, previousSplit);
                else
                    makeSubclassOf(split.getTopicMap(), previousSplit, split);
            }
            
            previousSplit = split;
        }
        
        if(descending)
            makeSubclassOf(previousSplit.getTopicMap(), original, previousSplit);
        else
            makeSubclassOf(original.getTopicMap(), previousSplit, original);
        
        
        // Change base name of the original topic....
        original.setBaseName(splitParts[splitParts.length-1]);
        topicCounter++;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        Topic supersubClassTopic = getOrCreateTopic(tm, XTMPSI.SUPERCLASS_SUBCLASS, null);
        Topic subclassTopic = getOrCreateTopic(tm, XTMPSI.SUBCLASS, null);
        Topic superclassTopic = getOrCreateTopic(tm, XTMPSI.SUPERCLASS, null);
        Association ta = tm.createAssociation(supersubClassTopic);
        ta.addPlayer(t, subclassTopic);
        ta.addPlayer(superclass, superclassTopic);
    }
    
    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        if(si!=null){
            Topic t=tm.getTopic(si);
            if(t==null){
                t=tm.createTopic();
                t.addSubjectIdentifier(tm.createLocator(si));
                if(bn!=null) {
                	t.setBaseName(bn);
                }
            }
            return t;
        }
        else {
            Topic t=tm.getTopicWithBaseName(bn);
            if(t==null){
                t=tm.createTopic();
                if(bn!=null) {
                	t.setBaseName(bn);
                }
                else {
                	t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                }
            }
            return t;
        }
    }
}
