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
 * ChangeAssociationRole.java
 *
 * Created on 10. elokuuta 2006, 16:25
 *
 */

package org.wandora.application.tools.associations;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.util.*;


/**
 * <p>
 * Tool is used to change role topic in given associations. Multiple associations
 * can be modified at once. Tool requests both old role topic and new role topic.
 * </p>
 * 
 * @author akivela
 */


public class ChangeAssociationRole extends AbstractWandoraTool implements WandoraTool {
	
	private static final long serialVersionUID = 1L;
	
	
    private boolean requiresRefresh = false;
    

    public ChangeAssociationRole() {
        setContext(new AssociationContext());
    }
    public ChangeAssociationRole(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Change association role";
    }

    @Override
    public String getDescription() {
        return "Changes one role topic in associations.";
    }

    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        requiresRefresh = false;
        Iterator<Association> associations = context.getContextObjects();
        Association association = null;
        int count = 0;
        Collection<Topic> oldRoles = new ArrayList<Topic>();
        Collection<Topic> roles;
        Topic role;
        
        try {
            if(associations != null && associations.hasNext()) {
            
                int i = 0;
                int ac = 0;
                // First collect all roles in context associations.
                while(associations.hasNext() && !forceStop()) {
                    association = (Association) associations.next();
                    if(association != null) {
                        ac++;
                        roles = association.getRoles();
                        for(Iterator<Topic> it = roles.iterator(); it.hasNext(); ) {
                            role = it.next();
                            if(!oldRoles.contains(role)) {
                                oldRoles.add(role);
                            }
                        }
                    }
                }
            
                // Then solve names of the roles. Notice duplicate removal....
                ArrayList<String> oldRoleNames = new ArrayList<String>();
                role = null;
                String roleName = null;
                for(Iterator<Topic> it=oldRoles.iterator(); it.hasNext(); ) {
                    role = it.next();
                    if(role != null && !role.isRemoved()) {
                        if(role.getBaseName() != null) roleName = role.getBaseName();
                        else roleName = role.getOneSubjectIdentifier().toExternalForm();
                        if(roleName != null && !oldRoleNames.contains(roleName)) {
                            oldRoleNames.add(roleName);
                        }
                    }
                }
                String[] oldRoleNameArray = oldRoleNames.toArray(new String[] {});

                // Ask user about the old and new role.
                String oldRoleBasename = WandoraOptionPane.showOptionDialog(wandora, "Select role to be changed", "Changed role", WandoraOptionPane.OK_CANCEL_OPTION, oldRoleNameArray, oldRoleNameArray[0]);
                if(oldRoleBasename == null) return;
                
                Topic oldRole = wandora.getTopicMap().getTopicWithBaseName(oldRoleBasename);
                if(oldRole == null) {
                    // SI instead of Basename
                    oldRole = wandora.getTopicMap().getTopic(oldRoleBasename);
                }

                if(oldRole == null) {
                    log("Old role not selected. Aborting.");
                    return;
                }
                
                Topic newRole = wandora.showTopicFinder("Select new role type...");

                if(newRole == null) {
                    log("New role not selected. Aborting.");
                    return;
                }
                if(oldRole.mergesWithTopic(newRole)) {
                    log("New role merges with the old role. Operation has no effect. Aborting.");
                    return;
                }

                // Finally do the change.
                if(ac > 1000) {
                    setDefaultLogger();
                    log("Changing role in associations.");
                    setProgressMax(ac);
                }
                associations = context.getContextObjects();
                Topic player = null;
                while(associations.hasNext() && !forceStop()) {
                    association = (Association) associations.next();
                    if(association != null) {
                        player = association.getPlayer(oldRole);
                        if(player != null) {
                            requiresRefresh = true;
                            association.addPlayer(player, newRole);
                            association.removePlayer(oldRole);
                        }
                        count++;
                        setProgress(count);
                    }
                }
            }
            log("Total " + count + " role topics changed.");
        }   
        catch(Exception e) {
            singleLog(e);
        }
    }
}
