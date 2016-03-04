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
 * RemovePlayer.java
 *
 * Created on 2. marraskuuta 2007, 10:18
 */

package org.wandora.application.tools.associations;

import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.util.*;


/**
 *
 * @see InsertPlayer
 * @author akivela
 */
public class RemovePlayer extends AbstractWandoraTool implements WandoraTool {
    private boolean requiresRefresh = false;
    private boolean shouldContinue = true;
    
    
    /** Creates a new instance of RemovePlayer */
    public RemovePlayer() {
        setContext(new AssociationContext());
    }
    
    
    public RemovePlayer(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    @Override
    public String getName() {
        return "Remove player in association";
    }

    @Override
    public String getDescription() {
        return "Removes players in associations";
    }
    
    
    @Override
    public void execute(Wandora admin, Context context) {      
        try {
            requiresRefresh = false;
            Map<Association,ArrayList<Topic>> associationsWithRoles = null;
            Topic role = null;
            Association a = null;
            int aCounter = 0;
            int removeCounter = 0;
            shouldContinue = true;
            yesToAll = false;
            
            setDefaultLogger();
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
                Iterator associations = context.getContextObjects();
                AssociationTable associationTable = null;
                Object contextSource = context.getContextSource();
                if(contextSource instanceof AssociationTable) {
                    associationTable = (AssociationTable) context.getContextSource();
                }
                if(associationTable != null) {
                    associationsWithRoles = associationTable.getSelectedAssociationsWithSelectedRoles();
                            
                }
                
                if(associationsWithRoles != null && associationsWithRoles.size() > 0) {
                    Set<Association> associationSet = associationsWithRoles.keySet();
                    Iterator<Association> associationIterator = associationSet.iterator();
                    
                    while(associationIterator.hasNext() && !forceStop() && shouldContinue) {
                        a = (Association) associationIterator.next();
                        if(a != null) {
                            aCounter++;
                            ArrayList<Topic> roles = associationsWithRoles.get(a);
                            Iterator<Topic> roleIterator = roles.iterator();
                            while(roleIterator.hasNext() && !forceStop() && shouldContinue) {
                                role = roleIterator.next();
                                if(role != null) {
                                    try {
                                        if(confirmRemove(admin, a, role)) {
                                            requiresRefresh = true;
                                            a.removePlayer(role);
                                            removeCounter++;
                                        }
                                    }
                                    catch(Exception e) {
                                        log(e);
                                    }
                                }
                            }
                        }
                    }
                    log("Total "+aCounter+" associations processed!");
                    log("Total "+removeCounter+" players removed in associations!");
                }
                else {
                    log("No associations found in context!");
                }
            }
            else {
                log("Illegal context found! Expecting association context!");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    private boolean yesToAll = false;
    public boolean confirmRemove(Wandora admin, Association association, Topic role)  throws TopicMapException {
        if(association == null || association.getType() == null || role == null) return false;
        if(yesToAll) return true;

        String typeName = getTopicName(association.getType());
        String roleName = getTopicName(role);
        String confirmMessage = "Would you like remove player in role '" + roleName + "' from association of type '" + typeName + "'?";
        int answer = WandoraOptionPane.showConfirmDialog(admin, confirmMessage,"Confirm player remove", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION );
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
