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
 * DeleteAssociationsInTopic.java
 *
 * Created on 20. heinäkuuta 2006, 20:55
 *
 */

package org.wandora.application.tools.associations;


import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;

import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;
import static org.wandora.application.tools.AbstractWandoraTool.getTopicName;

/**
 * Deletes associations in a certain topic i.e. associations where this player
 * plays a role.
 *
 * @author akivela
 */
public class DeleteAssociationsInTopic extends AbstractWandoraTool implements WandoraTool {
    
    
    public boolean forceDelete = true;
    public boolean confirm = true;
    public boolean shouldContinue = true;
    
    protected Wandora admin = null;
    private String associationName = null;
    
    
    
    /** Creates a new instance of DeleteAssociationsInTopic */
    public DeleteAssociationsInTopic() {
    }
    public DeleteAssociationsInTopic(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Delete Association(s)";
    }
    @Override
    public String getDescription() {
        return "Deletes associations between topics.";
    }
    
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        this.admin = admin;
        ArrayList<Association> associationsToDelete = new ArrayList<Association>();
        Iterator topics = getContext().getContextObjects();
        Collection<Association> associations = null;
        Iterator<Association> ai = null;
        Association association = null;
        Topic topic = null;
        Topic ltopic = null;
        String topicName = null;
        int count = 0;
        ConfirmResult r_all = null;
        ConfirmResult r = null;
        
        
        yesToAll = false;
        shouldContinue = true;
        setDefaultLogger();
        
        if(topics != null && topics.hasNext()) {
            while(topics.hasNext() && shouldContinue && !forceStop()) {
                topic = (Topic) topics.next();
                if(topic != null && !topic.isRemoved()) {
                    if(topic instanceof LayeredTopic) {
                        ltopic = ((LayeredTopic) topic).getTopicForSelectedLayer();
                        if(ltopic == null || ltopic.isRemoved()) {
                            setState(INVISIBLE);
                            int answer = WandoraOptionPane.showConfirmDialog(admin,"Topic '"+getTopicName(topic)+"' doesn't exist in selected layer.", "Topic not in selected layer", WandoraOptionPane.OK_CANCEL_OPTION);
                            setState(VISIBLE);
                            if(answer == WandoraOptionPane.CANCEL_OPTION) shouldContinue = false;
                            continue;
                        }
                        else {
                            topic = ltopic;
                        }
                    }
                    topicName = getTopicName(topic);
                    hlog("Investigating associations of a topic '" + topicName + "'.");
                    associations = solveTopicAssociations(topic);

                    if(associations != null && associations.size() > 0) {
                        ai = associations.iterator();
                        while(ai.hasNext()) {
                            association = ai.next();
                            if(association != null && !association.isRemoved()) {
                                if(shouldDelete(topic, association)) {
                                    associationsToDelete.add(association);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
            hlog(count + " association to delete.");
            ai = associationsToDelete.iterator();
            int i = 0;
            while(ai.hasNext()) {
                i++;
                association = ai.next();
                if(association != null && !association.isRemoved()) {
                    association.remove();
                    if((i % 100) == 0) hlog((count-i) + " association to delete.");
                }
            }
        }
        log("Total " + count + " associations deleted.");
        setState(WAIT);
        associationsToDelete = null;
    }
    
    
    
    
    
    
    
    public Collection<Association> solveTopicAssociations(Topic topic) throws TopicMapException {
        return topic.getAssociations();
    }
    
    
    
    
    public boolean shouldDelete(Topic topic, Association association) throws TopicMapException {
        if(confirm) {
            return confirmDelete(association);
        }
        else {
            return true;
        }
    }
    
    
    public boolean yesToAll = false;
    public boolean confirmDelete(Association association) throws TopicMapException {
        if(yesToAll) {
            hlog("Found association to delete.");
            return true;
        }
        else {
            setState(INVISIBLE);
            associationName = buildAssociationName(association);
            String confirmMessage = "Would you like delete "+associationName+"?";
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

    
    
    public String buildAssociationName(Association association) throws TopicMapException {
        if(association == null) return "[null]";
        
        String typeName = getTopicName(association.getType());
        Iterator roleIterator = association.getRoles().iterator();
        StringBuilder playerDescription = new StringBuilder("");
        while(roleIterator.hasNext()) {
            Topic role = (Topic) roleIterator.next();
            Topic player = association.getPlayer(role);
            playerDescription.append("'").append(getTopicName(player)).append("'");
            if(roleIterator.hasNext()) playerDescription.append(" and ");
        }
        return "'" + typeName + "' association between " + playerDescription.toString();
    }
    
}
