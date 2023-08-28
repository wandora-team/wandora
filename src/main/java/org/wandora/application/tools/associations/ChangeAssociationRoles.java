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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


/**
 * <p>
 * The tool is used to change role topics in given associations. Multiple roles and associations
 * can be modified at once. Tool looks at the *selected* roles in association table and
 * assumes all selected roles should be changed. Tool requests new role using a topic
 * selection dialog.
 * </p>
 * 
 * @author akivela
 */


public class ChangeAssociationRoles extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	
	private boolean requiresRefresh = false;
    

    public ChangeAssociationRoles() {
        setContext(new AssociationContext());
    }
    public ChangeAssociationRoles(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Change association roles";
    }

    @Override
    public String getDescription() {
        return "Changes multiple role topics in associations at once.";
    }

    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        requiresRefresh = false;
        Map<Association, List<Topic>> associationsWithOldRoles = null;
        
        try {
            Object contextSource = context.getContextSource();
            if(contextSource instanceof AssociationTable) {
                AssociationTable associationTable = (AssociationTable) contextSource;
                associationsWithOldRoles = associationTable.getSelectedAssociationsWithSelectedRoles();
            }

            if(associationsWithOldRoles == null || associationsWithOldRoles.isEmpty()) {
                log("Associations not selected. Aborting.");
                return;
            }

            Topic newRole = wandora.showTopicFinder("Select new role type...");

            if(newRole == null) {
                log("New role not selected. Aborting.");
                return;
            }

            if(associationsWithOldRoles.size() > 1000) {
                setDefaultLogger();
                log("Changing roles in associations.");
                setProgressMax(associationsWithOldRoles.size());
            }

            // Finally do the change.
            int count = 0;
            Topic player = null;
            for(Association association : associationsWithOldRoles.keySet()) {
                if(forceStop()) break;
                if(association != null && !association.isRemoved()) {
                    List<Topic> oldRoles = associationsWithOldRoles.get(association);
                    for(Topic oldRole : oldRoles) {
                        System.out.println("oldrole == "+oldRole);
                        player = association.getPlayer(oldRole);
                        System.out.println("    player == "+player);
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
