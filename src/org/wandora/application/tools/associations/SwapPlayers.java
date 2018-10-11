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
 * SwapPlayers.java
 *
 * Created on 16. elokuuta 2008, 16:25
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
 * Changes given associations by swapping two players in associations.
 * For example, if binary association contains players with roles P1:R1 and P2:R2
 * then player swap results association with players P2:R1 and P1:R2.
 * </p>
 * <p>
 * If given associations are not binary then selected columns in association table
 * indicate swapped players. If selection contains more or less than two columns
 * tool aborts.
 * </p>
 * <p>
 * Tool can be used to fix symmetric associations where roles have almost same
 * semantic meaning. For example RDF triplets are directed and RDF import may
 * result symmetric association duplicates that are not elegant in topic maps.
 * Swapping players of such symmetric association duplicates causes Wandora
 * to merge symmetric associations.
 * </p>
 * 
 * @see DeleteSymmetricAssociation
 * @see CreateSymmetricAssociation
 * 
 * @author akivela
 */


public class SwapPlayers extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private boolean requiresRefresh = false;
    

    public SwapPlayers() {
        setContext(new AssociationContext());
    }
    public SwapPlayers(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Swap players";
    }
    @Override
    public String getDescription() {
        return "Swaps association player roles. If symmetric association already exists "+
               "then player swap merges symmetric associations. Feature can be used to remove "+
               "symmetric duplicates.";
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
        Set<Topic> swapRoles = new HashSet<Topic>();
        Collection<Topic> roles = null;
        Topic role;
        Topic[] swapRoleArray = null;
        
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
                            if(!swapRoles.contains(role)) {
                                swapRoles.add(role);
                            }
                        }
                    }
                }
                // Check if decent logging is required.
                if(ac > 100) {
                    setDefaultLogger();
                    log("Swapping players in associations.");
                    setProgressMax(ac);
                }
                
                // Ok, all selected associations are binary. Easy job.
                if(swapRoles.size() == 2) {
                    swapRoleArray = swapRoles.toArray( new Topic[] {} );
                    associations = context.getContextObjects();
                    while(associations.hasNext() && !forceStop()) {
                        association = (Association) associations.next();
                        if(association != null && !association.isRemoved()) {
                            count += swapPlayers(association, swapRoleArray[0], swapRoleArray[1]);
                            setProgress(count);
                        }
                    }
                    log("Total " + count + " associations processed.");
                }
                
                // Associations are not binary. Have to investigate more detailed which players to swap.
                else if(swapRoles.size() > 2) {
                    AssociationTable associationTable = null;
                    Object contextSource = context.getContextSource();
                    if(contextSource instanceof AssociationTable) {
                        associationTable = (AssociationTable) context.getContextSource();
                    }
                    if(associationTable != null) {
                        Map<Association,ArrayList<Topic>> associationsWithSelectedRoles = associationTable.getSelectedAssociationsWithSelectedRoles();
                        Set<Association> associationKeys = associationsWithSelectedRoles.keySet();
                        Iterator<Association> associationKeyIterator = associationKeys.iterator();
                        ArrayList<Topic> swapRoleArrayList = null;
                        while(associationKeyIterator.hasNext() && !forceStop()) {
                            association = (Association) associationKeyIterator.next();
                            if(association != null && !association.isRemoved()) {
                                swapRoleArrayList = associationsWithSelectedRoles.get(association);
                                if(swapRoleArrayList != null && swapRoleArrayList.size() == 2) {
                                    swapRoleArray = swapRoleArrayList.toArray( new Topic[] {} );
                                    count += swapPlayers(association, swapRoleArray[0], swapRoleArray[1]);
                                    setProgress(count);
                                }
                                else {
                                    log("Number of players is less than two or greater than two. Skipping association.");
                                }
                            }
                        }
                        log("Total " + count + " associations processed.");
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
    
    
    
    
    
    public int swapPlayers(Association association, Topic role1, Topic role2) {
        int swapCount = 0;
        try {
            if(association == null || association.isRemoved()) return 0;
            if(role1 == null || role2 == null) return 0;
            if(role1.isRemoved() || role2.isRemoved()) return 0;
            Topic player1 = association.getPlayer(role1);
            Topic player2 = association.getPlayer(role2);
            if(player1 != null && player2 != null) {
                if(!player1.isRemoved() && !player2.isRemoved()) {
                    requiresRefresh = true;
                    association.removePlayer(role1);
                    association.removePlayer(role2);
                    association.addPlayer(player1, role2);
                    association.addPlayer(player2, role1);
                    swapCount++;
                }
                else {
                    log("Association contains removed topics. Skipping!");
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
        return swapCount;
    }
}
