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
 * CreateSymmetricAssociation.java
 *
 * Created on 16. elokuuta 2008, 18:41
 *
 */

package org.wandora.application.tools.associations;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;


/**
 * <p>
 * Creates symmetric associations for given associations. Symmetric association
 * has two players swapped. For example association's [P1:R1, P2:R2, P3:R3] symmetric
 * pairs are [P2:R1, P1:R2, P3:R3].
 * </p>
 * <p>
 * If given association contains more than two players, only selected players 
 * are swapped. If selection contains more or less than two players, tool
 * aborts without changes.
 * </p>
 * 
 * @see DeleteSymmetricAssociation
 * @see SwapPlayers
 * 
 * @author akivela
 */
public class CreateSymmetricAssociation extends AbstractWandoraTool implements WandoraTool {
	
	
	private static final long serialVersionUID = 1L;
	
	
    private boolean requiresRefresh = false;
    

    public CreateSymmetricAssociation() {
        setContext(new AssociationContext());
    }
    public CreateSymmetricAssociation(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Create symmetric associations";
    }
    @Override
    public String getDescription() {
        return "Creates symmetric association where players have been swapped.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        requiresRefresh = false;
        Iterator associations = context.getContextObjects();
        Association association = null;
        int count = 0;
        Set<Topic> symmetricRoles = new HashSet<Topic>();
        Collection<Topic> roles = null;
        Topic role;
        Topic[] symmetricRoleArray = null;
        
        try {
            if(associations != null && associations.hasNext()) {
            
                int i = 0;
                int ac = 0;
                // First collect all roles in context associations.
                while(associations.hasNext() && !forceStop()) {
                    association = (Association) associations.next();
                    if(association != null && !association.isRemoved()) {
                        ac++;
                        roles = association.getRoles();
                        for(Iterator<Topic> it = roles.iterator(); it.hasNext(); ) {
                            role = it.next();
                            if(!symmetricRoles.contains(role)) {
                                symmetricRoles.add(role);
                            }
                        }
                    }
                }
                // Check if decent logging is required.
                if(ac > 100) {
                    setDefaultLogger();
                    log("Creating symmetric associations.");
                    setProgressMax(ac);
                }
                
                // Ok, all selected associations are binary. Easy job.
                if(symmetricRoles.size() == 2) {
                    symmetricRoleArray = symmetricRoles.toArray( new Topic[] {} );
                    associations = context.getContextObjects();
                    while(associations.hasNext() && !forceStop()) {
                        association = (Association) associations.next();
                        if(association != null && !association.isRemoved()) {
                            count += createSymmetricAssociation(association, symmetricRoleArray[0], symmetricRoleArray[1]);
                            setProgress(count);
                        }
                    }
                    log("Total " + count + " associations created.");
                }
                
                // Associations are not binary. Have to investigate more detailed which players to use as symmetry pair.
                else if(symmetricRoles.size() > 2) {
                    AssociationTable associationTable = null;
                    Object contextSource = context.getContextSource();
                    if(contextSource instanceof AssociationTable) {
                        associationTable = (AssociationTable) context.getContextSource();
                    }
                    if(associationTable != null) {
                        Map<Association,List<Topic>> associationsWithSelectedRoles = associationTable.getSelectedAssociationsWithSelectedRoles();
                        Set<Association> associationKeys = associationsWithSelectedRoles.keySet();
                        Iterator<Association> associationKeyIterator = associationKeys.iterator();
                        List<Topic> symmetricRoleArrayList = null;
                        while(associationKeyIterator.hasNext() && !forceStop()) {
                            association = associationKeyIterator.next();
                            if(association != null && !association.isRemoved()) {
                                symmetricRoleArrayList = associationsWithSelectedRoles.get(association);
                                if(symmetricRoleArrayList != null && symmetricRoleArrayList.size() == 2) {
                                    symmetricRoleArray = symmetricRoleArrayList.toArray( new Topic[] {} );
                                    count += createSymmetricAssociation(association, symmetricRoleArray[0], symmetricRoleArray[1]);
                                    setProgress(count);
                                }
                                else {
                                    log("Number of selected players is less than two or greater than two. Skipping association.");
                                }
                            }
                        }
                        log("Total " + count + " associations created.");
                    }
                    else {
                        log("The number of association players is greater than two. Aborting.");
                    }
                }
                else {
                    log("The number of association players is less than two. Aborting.");
                }
            }
        }   
        catch(Exception e) {
            singleLog(e);
        }
    }
    
    
    
    
    
    public int createSymmetricAssociation(Association association, Topic role1, Topic role2) {
        int createCount = 0;
        try {
            if(association == null || association.isRemoved()) return 0;
            if(role1 == null || role2 == null) return 0;
            if(role1.isRemoved() || role2.isRemoved()) return 0;
            
            Topic player1 = association.getPlayer(role1);
            Topic player2 = association.getPlayer(role2);
            if(player1 != null && player2 != null) {
                if(!player1.isRemoved() && !player2.isRemoved()) {
                    requiresRefresh = true;
                    TopicMap topicMap = association.getTopicMap();
                    Topic type = association.getType();
                    Association symmetricAssociation = topicMap.createAssociation(type);
                    createCount++;
                    symmetricAssociation.addPlayer(player1, role2);
                    symmetricAssociation.addPlayer(player2, role1);
                    Collection<Topic> originalRoles = association.getRoles();
                    for(Topic originalRole : originalRoles) {
                        if(role1.mergesWithTopic(originalRole)) continue;
                        else if(role2.mergesWithTopic(originalRole)) continue;
                        else {
                            symmetricAssociation.addPlayer(association.getPlayer(originalRole), originalRole);
                        }
                    }
                }
                else {
                    log("Association contains removed topics. Skipping.");
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
        return createCount;
    }
}
