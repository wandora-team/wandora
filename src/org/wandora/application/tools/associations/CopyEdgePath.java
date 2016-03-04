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
 */

package org.wandora.application.tools.associations;



import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.utils.*;




/**
 * <p>
 * Copies a tabulator separated topic list to system clipboard.
 * Topic list contains player topics of a given association type and a role type.
 * Player topic set is limited to topics that can be reached from the selected
 * topic and selected association.
 * </p>
 * <p>
 * For example, think binary associations:
 * a-b, b-c, d-e, e-f. Here letters a through f represent players of association.
 * As you can easy figure there is an association chain from a to f as you can
 * travel from a to f using the association. Now lets think you have
 * selected association b-c and player b. Running this
 * tool with this selection copies system clipboard a list of topics b, c, d, e, f.
 * If you select association b-c and the player c, your clipboard ends up to contain
 * player topics c, b, a.
 * </p>
 * <p>
 * Tool can be used to locate a root node of tree like association set, for example.
 * Super-subclass relations form usually a tree type association structure.
 * </p>
 * 
 * @see OpenEdgeTopic
 * @author akivela
 */


public class CopyEdgePath extends AbstractWandoraTool implements WandoraTool {
    
    
    public CopyEdgePath() {
        setContext(new AssociationContext());
    }
    
    
    public CopyEdgePath(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Copy edge path";
    }

    @Override
    public String getDescription() {
        return "Copy topics while traversing to the edge of associations.";
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
    
    @Override
    public void execute(Wandora admin, Context context) {      
        try {
            Map<Association,ArrayList<Topic>> associationsWithRoles = null;
            Topic role = null;
            Association a = null;
            StringBuilder pathString = new StringBuilder("");
            
            otherRole = null;
            
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
                    
                    while(associationIterator.hasNext() && !forceStop()) {
                        a = (Association) associationIterator.next();
                        if(a != null) {
                            ArrayList<Topic> roles = associationsWithRoles.get(a);
                            Iterator<Topic> roleIterator = roles.iterator();
                            if(roleIterator.hasNext() && !forceStop()) {
                                role = roleIterator.next();
                                if(role != null) {
                                    try {
                                        Topic outRole = findOtherRole(a, role, admin);
                                        if(outRole != null) {
                                            Topic player = a.getPlayer(role);
                                            ArrayList<Topic> topicPath = TopicTools.getSinglePath(player, a.getType(), role, outRole);
                                            Topic pathTopic = null;
                                            pathString.append(player.getBaseName());
                                            for(Iterator<Topic> pathIter=topicPath.iterator(); pathIter.hasNext(); ) {
                                                pathTopic = pathIter.next();
                                                pathString.append(TopicToString.toString(pathTopic));
                                                if(pathIter.hasNext()) pathString.append("\t");
                                            }
                                            pathString.append("\n");
                                        }
                                    }
                                    catch(Exception e) {
                                        singleLog(e);
                                    }
                                }
                            }
                        }
                    }
                    ClipboardBox.setClipboard(pathString.toString());
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
    
    
    
    
    Topic otherRole = null;
    
    private Topic findOtherRole(Association a, Topic r, Wandora admin) {
        if(otherRole != null) return otherRole;
        
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
                Object answer = WandoraOptionPane.showOptionDialog(admin, "Select second role for association travelsal", "Select second role", WandoraOptionPane.OK_CANCEL_OPTION, allRoles.toArray(), allRoles.iterator().next());
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
