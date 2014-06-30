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
 * DuplicateTopics.java
 *
 * Created on September 22, 2004, 1:20 PM
 */

package org.wandora.application.tools;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.util.*;
import javax.swing.*;


/**
 * Duplicate context topics. Duplicated topic is modified in order to prevent 
 * instant merge. Topic's base name is attached a string '(copy n)' and 
 * subject identifiers a string '_copyn' where n is first unused number.
 *
 * @author  akivela
 */



public class DuplicateTopics extends AbstractWandoraTool implements WandoraTool {
    
    
    public boolean duplicateAssociations = true;
    public boolean copyInstances = true;
    
    public boolean askName=true;
    

    public DuplicateTopics() {
    }
    public DuplicateTopics(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_duplicate.png");
    }

    @Override
    public String getName() {
        return "Duplicate Topics";
    }

    @Override
    public String getDescription() {
        return "Duplicate context topics. "+
               "Duplicated topic is modified in order to prevent instant merge. "+
               "Duplicated topic base name is attached string '(copy n)' and SIs string '_copyn' "+
               "where n is first unused number.";
    }
    
    
    
    @Override
    public void execute(Wandora w, Context context) {
        Iterator topics = getContext().getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        TopicMap tm = w.getTopicMap();
        
        Topic topic = null;
        while(topics.hasNext() && !forceStop()) {
            try {
                topic = (Topic) topics.next();
                if(topic != null && !topic.isRemoved()) {
                    Topic ltopic = tm.getTopic(topic.getOneSubjectIdentifier());
                    duplicateTopic(ltopic, tm, w);
                }
            } catch(Exception e) {
                log(e);
            }
        }
    }
    

    
    public Topic duplicateTopic(Topic original, TopicMap tm, Wandora w)  throws TopicMapException {
        // --- copy topic and associations
        TopicMap copyMap = new org.wandora.topicmap.memory.TopicMapImpl();
        
        Topic copy = copyMap.copyTopicIn(original, false);
        if(duplicateAssociations) {
            Collection assocs = original.getAssociations();
            Association a;
            for(Iterator iter = assocs.iterator(); iter.hasNext();) {
                a = (Association) iter.next();
                copyMap.copyAssociationIn(a);
            }
        }
       
        // --- resolve new subject base name
        if(original.getBaseName() != null) {
            String newBaseName = original.getBaseName() + " (copy)";
            int c = 2;
            while(tm.getTopicWithBaseName(newBaseName) != null && c<10000) {
                newBaseName = original.getBaseName() + " (copy " + c + ")";
                c++;
            }

            if(askName){
                while(true){
                    String input=WandoraOptionPane.showInputDialog(w, "Enter new base name for the topic", newBaseName);
                    if(input==null) return null;
                    newBaseName=input;
                    if(tm.getTopicWithBaseName(input)!=null){
                        int a=WandoraOptionPane.showConfirmDialog(w,"Topic with base name '"+input+"' already exists and will be merged with new topic. Do you want to continue?");
                        if(a==WandoraOptionPane.CANCEL_OPTION) return null;
                        else if(a==WandoraOptionPane.YES_OPTION) break;
                    }
                    else break;
                }
            }

            copy.setBaseName(newBaseName);
        }

        // --- resolve new subject locator
        if(original.getSubjectLocator() != null) {
            int c = 2;
            Locator newSubjectLocator = new Locator(original.getSubjectLocator().toExternalForm() + "_copy");
            while(tm.getTopicBySubjectLocator(newSubjectLocator) != null && c<10000) {
                newSubjectLocator = new Locator(original.getSubjectLocator().toExternalForm() + "_copy" + c);
                c++;
            }
            copy.setSubjectLocator(newSubjectLocator);
        }

        // --- resolve new subject identifiers
        Collection sis = copy.getSubjectIdentifiers();
        Vector siv = new Vector();
        for(Iterator iter = sis.iterator(); iter.hasNext(); ) {
            siv.add(iter.next());
        }
        Locator lo = null;
        Locator l = null;
        for(int i=0; i<siv.size(); i++) {
            lo = (Locator) siv.elementAt(i);
            copy.removeSubjectIdentifier(lo);
            l = (Locator) new Locator(lo.toExternalForm() + "_copy");
            int c = 2;
            while(tm.getTopic(l) != null && c<10000) {
                l = (Locator) new Locator(lo.toExternalForm() + "_copy" + c);
                c++;
            }
            copy.addSubjectIdentifier(l);
        }

        //log("Merging duplicated topic to topic map...");
        tm.mergeIn(copyMap);
        copy = tm.getTopic(l);

        // --- attach instances
        if(copy != null && copyInstances) {
            Collection col = tm.getTopicsOfType(original);
            Iterator iter=col.iterator();
            Topic t = null;
            while(iter.hasNext()) {
                t=(Topic)iter.next();
                if(t.isOfType(original)) {
                    t.addType(copy);
                }
            }
        }
        return copy;
    }
}