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
 */

package org.wandora.application.tools.associations;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicTools;



/**
 * <p>This tool is a companion for CopyEdgePath tool. While CopyEdgePath 
 * copies the player chain to system clipboard, OpenEdgeTopic travels to 
 * the last player and open it to Wandora's topic panel for detailed 
 * inspection.</p>
 *
 * <p>Think for example associations a-b, b-c, c-d. Letters represent 
 * player topics. All associations have same type and same roles. Now 
 * think you have selected association a-b and further the player topic a. 
 * Executing this tool in this context opens topic d to Wandora's 
 * topic panel. </p>
 * 
 * @see CopyEdgePath
 * 
 * @author akivela
 */
public class OpenEdgeTopic extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	
	public OpenEdgeTopic() {
        setContext(new AssociationContext());
    }
    
    
    public OpenEdgeTopic(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Open edge of associations";
    }
    @Override
    public String getDescription() {
        return "Find and open edge topic along given association(s)";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            Map<Association,ArrayList<Topic>> associationsWithRoles = null;
            Topic role = null;
            Association a = null;
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
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
                    
                    if(associationIterator.hasNext() && !forceStop()) {
                        a = (Association) associationIterator.next();
                        if(a != null) {
                            ArrayList<Topic> roles = associationsWithRoles.get(a);
                            Iterator<Topic> roleIterator = roles.iterator();
                            if(roleIterator.hasNext() && !forceStop()) {
                                role = roleIterator.next();
                                if(role != null) {
                                    try {
                                        Topic outRole = findOtherRole(a, role, wandora);
                                        if(outRole != null) {
                                            Topic player = a.getPlayer(role);
                                            Topic rootTopic = TopicTools.getEdgeTopic(player, a.getType(), role, outRole);
                                            wandora.openTopic(rootTopic);
                                        }
                                    }
                                    catch(Exception e) {
                                        singleLog(e);
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    singleLog("No associations found in context!");
                }
            }
            else {
                singleLog("Illegal context found! Expecting association context!");
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
    }
    
    
    
    private Topic findOtherRole(Association a, Topic r, Wandora wandora) {
        Topic otherRole = null;
        try {
            Collection<Topic> allRoles = a.getRoles();
            if(allRoles.size() < 3) {
                for(Iterator<Topic> roleIterator = allRoles.iterator(); roleIterator.hasNext(); ) {
                    otherRole = roleIterator.next();
                    if(otherRole != null && !otherRole.isRemoved()) {
                        if(!otherRole.mergesWithTopic(r)) {
                            return otherRole;
                        }
                    }
                }
            }
            else {
                allRoles.remove(r);
                Object answer = WandoraOptionPane.showOptionDialog(wandora, "Select second role for association travelsal", "Select second role", WandoraOptionPane.OK_CANCEL_OPTION, allRoles.toArray(), allRoles.iterator().next());
                if(answer instanceof Topic) {
                    return (Topic) answer;
                }
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        return null;
    }
}
