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
 * CollectBinaryToNary.java
 *
 * Created on 7. huhtikuuta 2006, 10:53
 *
 */

package org.wandora.application.tools.associations;



import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;

import java.util.*;



/**
 * <p>
 * Tool builds single n-association from n binary associations. 
 * Number of association topics is reduced by one. Examples:
 * </p>
 * 
 * <code>
 *           c
 *          /
 *  (a -- b) -- d      ==>     a -- c -- d -- f   ,   [b]
 *          \
 *           f
 *
 *
 *           c
 *          /
 *  (a) -- b -- d      ==>     a -- c -- d -- f   ,   [b]
 *          \
 *           f
 *
 *  x  = topic
 * [x] = topic x is removed
 * (x) = topic x is in context
 * (x -- y) = association is in context
 * </code>
 *
 *
 * Note: Default context of this tool is <code>AssociationContext</code>!
 *
 * @author akivela
 */


public class CollectBinaryToNary extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	
	private boolean deleteOld = false;
    
    private boolean askNewAssociationType = true;
    private boolean requiresRefresh = false;
    
    

    public CollectBinaryToNary() {
        setContext(new AssociationContext());
    }
    
    
    
    public CollectBinaryToNary(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "Collect binary to n-ary";
    }
    @Override
    public String getDescription() {
        return "Builds single n-association from n binary associations.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            requiresRefresh = false;
            Iterator<Association> associations = null;
            Topic baseTopic = null;
            Association association = null;
            int counter = 0;
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
                associations = context.getContextObjects();
                
                Topic newAssociationType = null;
                if(askNewAssociationType) {
                	wandora.showTopicFinder("Select new type of associations...");
                    if(newAssociationType == null) return;
                }
                
                baseTopic = wandora.getOpenTopic();
                while(associations.hasNext() && !forceStop()) {
                    association = (Association) associations.next();
                    if(association != null && !association.isRemoved()) {
                        if(!askNewAssociationType) newAssociationType=association.getType();
                        breakAssociation(association, baseTopic, newAssociationType);
                        counter++;
                    }
                }
                if(deleteOld) {
                    associations = context.getContextObjects();
                    while(associations.hasNext() && !forceStop()) {
                        association = (Association) associations.next();
                        if(association != null && !association.isRemoved()) {
                            deletePlayers(association, baseTopic);
                        }
                    }
                }
            }
            
            else { // TOPIC CONTEXT!!
                
                Topic addressedAssociationType = wandora.showTopicFinder("Select type of processed associations...");
                if(addressedAssociationType == null) return;
                
                Topic newAssociationType = addressedAssociationType;
                if(askNewAssociationType) {
                	wandora.showTopicFinder("Select new type of associations...");
                    if(newAssociationType == null) return;
                }
                
                Iterator<Topic> baseTopics = context.getContextObjects();
                baseTopic = null;
                associations = null;
                Collection associationCollection = null;
                setDefaultLogger();
                long startTime = System.currentTimeMillis();

                while(baseTopics.hasNext() && !forceStop()) {
                    baseTopic = (Topic) baseTopics.next();
                    if(baseTopic != null && !baseTopic.isRemoved()) {
                        hlog("Processing associations of '"+getTopicName(baseTopic)+"'.");
                        associationCollection = baseTopic.getAssociations(addressedAssociationType);
                        if(associationCollection != null) {
                            associations = associationCollection.iterator();
                            while(associations.hasNext() && !forceStop()) {
                                association = (Association) associations.next();
                                if(association != null && !association.isRemoved()) {
                                    breakAssociation(association, baseTopic, newAssociationType);
                                    counter++;
                                }
                            }
                            if(deleteOld && !forceStop()) {
                                associations = associationCollection.iterator();
                                while(associations.hasNext() && !forceStop()) {
                                    association = (Association) associations.next();
                                    if(association != null && !association.isRemoved()) {
                                        deletePlayers(association, baseTopic);
                                    }
                                }
                            }
                        }
                    }
                }
                long endTime = System.currentTimeMillis();
                log("Execution took "+((endTime-startTime)/1000)+" seconds.");
            }
            log("Total "+counter+" associations processed.");
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    

            

    public void breakAssociation(Association association, Topic baseTopic, Topic newAssociationType) {
        Topic roleTopic = null;
        Topic playerTopic = null;
        Association newAssociation = null;
        Collection<Association> playerAssociations = null;
        Iterator<Association> playerAssociationIterator = null;
        Association playerAssociation = null;
        Collection<Topic> playerAssociationRoles = null;
        Iterator<Topic> playerAssociationRoleIterator = null;
        Topic playerAssociationRole = null;
        Topic playerAssociationPlayer = null;
        Collection<Topic> roles = null;
        Iterator<Topic> roleIterator = null;
        Topic baseRole = null;
        Topic associationType = null;
        HashMap<Topic, Topic> newMembers = new HashMap<Topic, Topic>();
        
        
        System.out.println("-------------in");
        try {
            if(association != null && baseTopic != null) {
                ArrayList<Topic> playerTopics = new ArrayList<Topic>();
                roles = association.getRoles();
                roleIterator = roles.iterator();
                associationType = association.getType();

                // First finding baseTopic's role topic in the association
                while(roleIterator.hasNext()) {
                    roleTopic = roleIterator.next();
                    if(roleTopic != null) {
                        playerTopic = association.getPlayer(roleTopic);
                        if(playerTopic.mergesWithTopic(baseTopic)) {
                            baseRole = roleTopic;
                            System.out.println("1 Adding player '"+getTopicName(baseTopic)+"'  with role '"+roleTopic.getBaseName()+"'.");
                            newMembers.put(roleTopic, baseTopic);
                        }
                        else {
                            playerTopics.add(playerTopic);
                            System.out.println("3 Investigating player: " + getTopicName(playerTopic));
                        }
                    }
                }

                Iterator<Topic> playerIterator = playerTopics.iterator();
                while(playerIterator.hasNext()) {
                    playerTopic = playerIterator.next();

                    System.out.println("4 Investigating player: " + getTopicName(playerTopic));
                    playerAssociations = playerTopic.getAssociations();
                    playerAssociationIterator = playerAssociations.iterator();
                    while(playerAssociationIterator.hasNext()) {
                        playerAssociation = playerAssociationIterator.next();
                        if(playerAssociation != null && !associationType.mergesWithTopic(playerAssociation.getType())) {
                            playerAssociationRoles = playerAssociation.getRoles();
                            playerAssociationRoleIterator = playerAssociationRoles.iterator();
                            while(playerAssociationRoleIterator.hasNext()) {
                                playerAssociationRole = playerAssociationRoleIterator.next();
                                if(!playerAssociationRole.mergesWithTopic(baseRole) && !playerAssociationRole.mergesWithTopic(roleTopic)) {
                                    playerAssociationPlayer = playerAssociation.getPlayer(playerAssociationRole);
                                    //System.out.println("2 Checking '"+getTopicName(playerAssociationPlayer)+"'  with role '"+getTopicName(playerAssociationRole)+"'.");
                                    if(!playerAssociationPlayer.mergesWithTopic(playerTopic) && !playerAssociationPlayer.mergesWithTopic(baseTopic)) {
                                        //System.out.println("2 Adding player '"+getTopicName(playerAssociationPlayer)+"'  with role '"+getTopicName(playerAssociationRole)+"'.");
                                        newMembers.put(playerAssociationRole, playerAssociationPlayer);
                                    }
                                }
                            }
                        }
                    }
                }
                
                if(newMembers.size() > 1) {
                    newAssociation = baseTopic.getTopicMap().createAssociation(newAssociationType);
                    newAssociation.addPlayers(newMembers);
                    requiresRefresh = true;
                    //if(deleteOld) topicsToDelete.add(playerTopic);
                }
            }
        }
        catch(TopicInUseException tiue) {
            try {
                log("Topic '"+getTopicName(playerTopic)+"' is used as an association or occurrence type and can not be removed!");
            }
            catch(Exception e) {
                log(e);
            }
        }
        catch(Exception e) {
            log(e);
        }
        System.out.println("-------------out");
    }
 
    
    
    
    public void deletePlayers(Association association, Topic baseTopic) throws TopicMapException {
        Collection<Topic> roles = association.getRoles();
        Iterator<Topic> roleIterator = roles.iterator();
        Topic playerTopic = null;
        Topic roleTopic = null;
        ArrayList<Topic> topicsToDelete = new ArrayList<Topic>();
        
        while(roleIterator.hasNext()) {
            roleTopic = roleIterator.next();
            if(roleTopic != null) {
                playerTopic = association.getPlayer(roleTopic);
                if(!playerTopic.mergesWithTopic(baseTopic)) {
                    topicsToDelete.add(playerTopic);
                }
            }
        }
        
        Iterator<Topic> deleteIterator = topicsToDelete.iterator(); 
        Topic topic = null;
        while(deleteIterator.hasNext()) {
            topic = deleteIterator.next();
            if(!topic.isRemoved()) {
                //hlog("Deleting topic '"+getTopicName(topic)+"'.");
                topic.remove();
                requiresRefresh = true;
            }
        }
    }

}

