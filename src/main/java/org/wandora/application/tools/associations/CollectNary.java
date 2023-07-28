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
 * CollectNary.java
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

/**
 * <p>
 * Tool builds n-association from selected associations. 
 * </p>
 * 
 * <code>
 * 
 * Four example associations:
 * 
 *   a -- b1 -- c1
 *   a -- b2 -- c2
 *   a -- b3 -- c3
 *   a -- b4 -- c4
 *
 * are transformed to 
 *
 *  a -- c1 -- c2 -- c3 -- c4
 *  (where b1, b2, b3 and b4 are roles)
 * 
 * or
 * 
 *  a -- b1 -- b2 -- b3 -- b4
 *   (where c1, c2, c3 and c4 are roles)
 *
 * </code>
 *
 *
 * Note: Default context of this tool is <code>AssociationContext</code>!
 *
 * 
 * @author akivela
 */


public class CollectNary extends AbstractWandoraTool implements WandoraTool {
    


	private static final long serialVersionUID = 1L;

	
	private boolean requiresRefresh = false;
    
    

    public CollectNary() {
        setContext(new AssociationContext());
    }
    
    
    
    public CollectNary(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "Collect to n-ary";
    }
    @Override
    public String getDescription() {
        return "Creates one n-association from n binary associations.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            requiresRefresh = false;
            Iterator associations = null;
            
            Topic newAssociationType = null;
            Topic groupingRole = null;
            Topic roleRole = null;
            Topic playerRole = null;
            boolean deleteSourceAssociations = false;
            
            TopicMap tm = wandora.getTopicMap();
            Association association = null;
            int counter = 0;
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
                associations = context.getContextObjects();
                
                if(associations == null || !associations.hasNext()) return;

                // ASK USER ABOUT THE ROLES AND TYPE TOPICS!
                GenericOptionsDialog god=new GenericOptionsDialog(
                		wandora,
                        "Collect n-ary associations",
                        "Combine selected associations using given grouping role.",
                        true,
                        new String[][]{
                    new String[]{"Grouping role","topic","", "Which player topic sets up a group of role-player pairs put into a single association."},
                    new String[]{"Role of role topic","topic","","Where is the role topic in association."},
                    new String[]{"Role of player topic","topic","","Where is the player topic in association."},
                    new String[]{"New association type","topic","", "What is the type of created associations."},
                    new String[]{"Delete source associations","boolean","false", "Should Wandora delete source associations afterward."},
                },wandora);
                god.setVisible(true);
                if(god.wasCancelled()) return;
                
                Map<String,String> values=god.getValues();
                
                if(values.get("Role of role topic")!=null && values.get("Role of role topic").length()>0) {
                    roleRole = tm.getTopic(values.get("Role of role topic"));
                }
                if(roleRole == null) {
                    log("Given role topic for roles is illegal (null)! Aborting.");
                    return;
                }
                
                if(values.get("Role of player topic")!=null && values.get("Role of player topic").length()>0) {
                    playerRole = tm.getTopic(values.get("Role of player topic"));
                }
                if(playerRole == null) {
                    log("Given role topic for players is illegal (null)! Aborting.");
                    return;
                }
                
                if(values.get("Grouping role")!=null && values.get("Grouping role").length()>0) {
                    groupingRole = tm.getTopic(values.get("Grouping role"));
                }
                if(groupingRole == null) {
                    log("Given grouping role topic is illegal (null)! Aborting.");
                    return;
                }
                
                if(values.get("New association type")!=null && values.get("New association type").length()>0) {
                    newAssociationType = tm.getTopic(values.get("New association type"));
                }
                if(newAssociationType == null) {
                    log("Given association type topic is illegal (null)! Aborting.");
                    return;
                }
                
                if(values.get("Delete source associations")!=null && values.get("Delete source associations").length()>0) {
                    deleteSourceAssociations = "true".equals(values.get("Delete source associations"));
                }
                
                ArrayList<Association> associationCollection = new ArrayList<Association>();
                while(associations.hasNext() && !forceStop()) {
                    association = (Association) associations.next();
                    try {
                        if(association != null && !association.isRemoved()) {
                            associationCollection.add(association);
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                collectAssociations(associationCollection, groupingRole, roleRole, playerRole, newAssociationType, deleteSourceAssociations, tm);
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    
    public void collectAssociations(Collection<Association> associations, Topic groupingRole, Topic roleRole, Topic playerRole, Topic newAssociationType, boolean deleteSourceAssociations, TopicMap tm) throws Exception {
        // ***** Collect association groups
        HashMap<Topic, Collection<Association>> collectedAssociations = new HashMap();
        for(Association a : associations) {
            if(a != null && !a.isRemoved()) {
                Topic groupingPlayer = a.getPlayer(groupingRole);
                if(groupingPlayer != null) {
                    Collection<Association> groupedAssociations = collectedAssociations.get(groupingPlayer);
                    if(groupedAssociations == null) groupedAssociations = new ArrayList<Association>();
                    groupedAssociations.add(a);
                    collectedAssociations.put(groupingPlayer, groupedAssociations);
                }
            }
        }
        // ***** Make new grouped associations
        for(Topic groupingTopic : collectedAssociations.keySet()) {
            Collection<Association> groupedAssociations = collectedAssociations.get(groupingTopic);
            Association newA = tm.createAssociation(newAssociationType);
            for(Association a : groupedAssociations) {
                Topic role = a.getPlayer(roleRole);
                Topic player = a.getPlayer(playerRole);
                if(role != null && player != null) {
                    newA.addPlayer(player, role);
                    for(Topic otherRole : a.getRoles()) {
                        if(otherRole != null && !otherRole.isRemoved()) {
                            if(!otherRole.mergesWithTopic(roleRole) && !otherRole.mergesWithTopic(playerRole)) {
                                if(a.getPlayer(otherRole) != null) {
                                    newA.addPlayer(a.getPlayer(otherRole), otherRole);
                                }
                            }
                        }
                    }
                }
                else {
                    // 
                }
            }
        }
        // ***** Delete source associations (optional)
        if(deleteSourceAssociations) {
            for(Association a : associations) {
                a.remove();
            }
        }
    }
    
    
    

            
    
}
