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
 * DeleteSymmetricAssociation.java
 *
 * Created on 16. elokuuta 2008, 18:41
 *
 */

package org.wandora.application.tools.associations;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.table.AssociationTable;
import java.util.*;


/**
 * <p>
 * Deletes symmetric association for a given association. Symmetric
 * association is an association where two players have been swapped. For example
 * [P1:R1, P2:R2, P3:R3]'s symmetric association is [P2:R1, P1:R2, P3:R3].
 * </p>
 * <p>
 * If tool is given both symmetric associations then both associations will be
 * removed.
 * </p>
 * 
 * @see SwapPlayers
 * @see CreateSymmetricAssociation
 * 
 * @author akivela
 */
public class DeleteSymmetricAssociation extends AbstractWandoraTool implements WandoraTool {
    private boolean requiresRefresh = false;
    

    public DeleteSymmetricAssociation() {
        setContext(new AssociationContext());
    }
    public DeleteSymmetricAssociation(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Delete symmetric associations";
    }
    @Override
    public String getDescription() {
        return "Deletes symmetric associations where players have been swapped. "+
               "if selection contains both symmetric associations then both assosiations "+
               "will be removed.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        requiresRefresh = false;
        Iterator associations = context.getContextObjects();
        Association association = null;
        int count = 0;
        Set<Topic> symmetricRoles = new HashSet<Topic>();
        Collection<Topic> roles = null;
        Topic role;
        Topic[] symmetricRoleArray = null;
        Collection<Association> allAssociations = null;
        TopicMap topicMap = null;
        Topic atype = null;
        
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
                    log("Deleting symmetric associations.");
                    setProgressMax(ac);
                }
                
                // Ok, all selected associations are binary. Easy job.
                if(symmetricRoles.size() == 2) {
                    symmetricRoleArray = symmetricRoles.toArray( new Topic[] {} );
                    associations = context.getContextObjects();
                    while(associations.hasNext() && !forceStop()) {
                        association = (Association) associations.next();
                        if(association != null && !association.isRemoved()) {
                            topicMap = association.getTopicMap();
                            atype = association.getType();
                            allAssociations = topicMap.getAssociationsOfType(atype);
                            count += deleteSymmetricAssociation(association, symmetricRoleArray[0], symmetricRoleArray[1], allAssociations);
                            setProgress(count);
                        }
                    }
                    log("Total " + count + " associations deleted.");
                }
                
                // Associations are not binary. Have to investigate more detailed which players to use as symmetry pair.
                else if(symmetricRoles.size() > 2) {
                    AssociationTable associationTable = null;
                    Object contextSource = context.getContextSource();
                    if(contextSource instanceof AssociationTable) {
                        associationTable = (AssociationTable) context.getContextSource();
                    }
                    if(associationTable != null) {
                        Map<Association,ArrayList<Topic>> associationsWithSelectedRoles = associationTable.getSelectedAssociationsWithSelectedRoles();
                        Set<Association> associationKeys = associationsWithSelectedRoles.keySet();
                        Iterator<Association> associationKeyIterator = associationKeys.iterator();
                        ArrayList<Topic> symmetricRoleArrayList = null;
                        while(associationKeyIterator.hasNext() && !forceStop()) {
                            association = (Association) associationKeyIterator.next();
                            if(association != null && !association.isRemoved()) {
                                symmetricRoleArrayList = associationsWithSelectedRoles.get(association);
                                if(symmetricRoleArrayList != null && symmetricRoleArrayList.size() == 2) {
                                    symmetricRoleArray = symmetricRoleArrayList.toArray( new Topic[] {} );
                                    topicMap = association.getTopicMap();
                                    atype = association.getType();
                                    allAssociations = topicMap.getAssociationsOfType(atype);
                                    count += deleteSymmetricAssociation(association, symmetricRoleArray[0], symmetricRoleArray[1], allAssociations);
                                    setProgress(count);
                                }
                                else {
                                    log("Number of selected players is less than two or greater than two. Skipping association.");
                                }
                            }
                        }
                        log("Total " + count + " associations deleted.");
                    }
                }
                else {
                    log("Number of association players is less than two. Aborting.");
                }
            }
        }   
        catch(Exception e) {
            singleLog(e);
        }
    }
    
    
    
    
    
    public int deleteSymmetricAssociation(Association association, Topic role1, Topic role2, Collection<Association> allAssociations) {
        int deleteCount = 0;
        try {
            if(association == null || association.isRemoved()) return 0;
            if(role1 == null && role2 == null) return 0;
            if(role1.isRemoved() || role2.isRemoved()) return 0;
            if(allAssociations == null || allAssociations.isEmpty()) return 0;
            
            Topic player1 = association.getPlayer(role1);
            Topic player2 = association.getPlayer(role2);
            if(player1 != null && player2 != null) {
                if(!player1.isRemoved() && !player2.isRemoved()) {
                    requiresRefresh = true;
                    Topic type = association.getType();
                    Collection<Topic> originalRoles = association.getRoles();
                    for(Association otherAssociation : allAssociations) {
                        boolean isSymmetric = true;
                        if(otherAssociation == null || otherAssociation.isRemoved()) continue;
                        for(Topic originalRole : originalRoles) {
                            if(role1.mergesWithTopic(originalRole)) {
                                if(!player2.mergesWithTopic(otherAssociation.getPlayer(role1))) {
                                    isSymmetric = false;
                                    break;
                                }
                            }
                            else if(role2.mergesWithTopic(originalRole)) {
                                if(!player1.mergesWithTopic(otherAssociation.getPlayer(role2))) {
                                    isSymmetric = false;
                                    break;
                                }
                            }
                            else {
                                Topic originalPlayer = association.getPlayer(originalRole);
                                Topic otherPlayer = otherAssociation.getPlayer(originalRole);
                                if(!originalPlayer.mergesWithTopic(otherPlayer)) {
                                    isSymmetric = false;
                                    break;
                                }
                            }
                        }
                        if(isSymmetric) {
                            otherAssociation.remove();
                            deleteCount++;
                        }
                    }
                }
                else {
                    log("Association contains removed topics. Skipping!");
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
        return deleteCount;
    }
}
