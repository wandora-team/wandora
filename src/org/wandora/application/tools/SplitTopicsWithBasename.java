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
 * 
 * SplitTopicsWithBasename.java
 *
 * Created on 13. heinäkuuta 2006, 16:41
 *
 */

package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import org.wandora.*;
import java.util.*;
import javax.swing.*;



/**
 * WandoraTool splitting a topic with a regular expression applied to topic's base name.
 * As a result, topic map contains one topic for each identified base name part.
 * To prevent immediate merge subject identifiers and subject locators are
 * modified a bit.
 *
 * @author akivela
 */
public class SplitTopicsWithBasename extends AbstractWandoraTool implements WandoraTool {
    public static boolean SKIP_WHITE_SPACE_SPLIT_PARTS = true;
    

    public boolean duplicateAssociations = true;
    public boolean copyInstances = true;
    
    public boolean askName=false;
    public static String splitString = "";
    private int topicCounter = 0;
    private int splitCounter = 0;
            
            
    
    
    public SplitTopicsWithBasename() {
    }
    public SplitTopicsWithBasename(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_split.png");
    }


    @Override
    public String getName() {
        return "Split topics with base name";
    }

    @Override
    public String getDescription() {
        return "Tool splits topics with base name.";
    }
    
    
    
    @Override
    public void execute(Wandora w, Context context) {       
        Iterator topics = getContext().getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        TopicMap tm = w.getTopicMap();
        
        splitString = WandoraOptionPane.showInputDialog(w, "Enter regular expression string used to split base name:", splitString);
        
        if(splitString == null || splitString.length() == 0) return;

        String splitStringCopy = splitString;
        Topic topic = null;
        duplicateAssociations = true;
        copyInstances = true;
        askName=false;
        splitCounter = 0;
        topicCounter = 0;
        setDefaultLogger();
        
        while(topics.hasNext() && !forceStop()) {
            try {
                topic = (Topic) topics.next();
                Topic ltopic = tm.getTopic(topic.getOneSubjectIdentifier());
                splitTopic(ltopic, splitStringCopy, tm, w);
            } 
            catch(Exception e) {
                log(e);
            }
        }
        if(topicCounter == 0) log("No topics splitted.");
        else if(topicCounter == 1) log("One topic splitted.");
        else log("Total "+topicCounter+" topics splitted.");
        
        if(splitCounter == 0) log("Created no splitted topics.");
        else if(splitCounter == 1) log("Created one splitted topic.");
        else log("Created total "+splitCounter+" splitted topics.");
        setState(WAIT);
    }
    

    
    public void splitTopic(Topic original, String splitString, TopicMap topicMap, Wandora w)  throws TopicMapException {
        if(original == null) return;
        if(original.getBaseName() == null) {
            log("Topic has no basename. Can't split.");
            return;
        }
        if(original.getBaseName().length() == 0) {
            log("Topic's basename length is zero. Can't split.");
        }
        String[] splitParts = original.getBaseName().split(splitString);
        if(splitParts.length < 2) {
            log("No split parts found for topic '"+ getTopicName(original) +"'.");
            return;
        }
        
        Topic split = null;
        String splitBasename = null;

        for(int i=1; i<splitParts.length; i++) {
            splitBasename = splitParts[i];
            if(splitBasename == null || splitBasename.length() == 0) {
                log("Zero length split part found for topic '"+ getTopicName(original) +"'.");
                continue;
            }
            if(SKIP_WHITE_SPACE_SPLIT_PARTS) {
                if(splitBasename.trim().length() == 0) {
                    log("White space split part found for topic '"+ getTopicName(original) +"'.");
                    continue;
                }
            }

            // --- copy topic and associations ---
            TopicMap splitMap = new org.wandora.topicmap.memory.TopicMapImpl();
            splitCounter++;
            
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
            String newBaseName = splitBasename;
            
            if(askName){
                while(true){
                    String input=WandoraOptionPane.showInputDialog(w, "Enter new base name for the topic", newBaseName);
                    if(input==null) return;
                    newBaseName=input;
                    if(topicMap.getTopicWithBaseName(input)!=null){
                        int a=WandoraOptionPane.showConfirmDialog(w, "Topic with base name '"+input+"' already exists and will be merged with new topic. Do you want to continue?");
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
            ArrayList<Locator> siv = new ArrayList();
            siv.addAll(sis);

            Locator l = null;
            for(Locator lo : siv) {
                split.removeSubjectIdentifier(lo);
                l = (Locator) new Locator(lo.toExternalForm() + "_split");
                int c = 2;
                while(topicMap.getTopic(l) != null && c<10000) {
                    l = (Locator) new Locator(lo.toExternalForm() + "_split" + c);
                    c++;
                }
                split.addSubjectIdentifier(l);
            }

            //log("Merging splitted topic to original map...");
            topicMap.mergeIn(splitMap);
            split = topicMap.getTopicWithBaseName(newBaseName);
            
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
        
        // Change base name of the original topic....
        original.setBaseName(splitParts[0]);
        topicCounter++;
    }
}
