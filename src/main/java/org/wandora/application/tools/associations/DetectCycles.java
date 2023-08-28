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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicTools;



/**
 *
 * @author akivela
 */
public class DetectCycles extends AbstractWandoraTool implements WandoraTool {

	
	private static final long serialVersionUID = 1L;
	
	

    public DetectCycles() {
        setContext(new AssociationContext());
    }


    public DetectCycles(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public String getName() {
        return "Detect cycle";
    }
    @Override
    public String getDescription() {
        return "Find cycles in addressed association path. Tool doesn't necessarily return all existing cycles.";
    }
    @Override
    public boolean requiresRefresh() {
        return false;
    }

    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Map<Association,List<Topic>> associationsWithRoles = null;
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
                            List<Topic> roles = associationsWithRoles.get(a);
                            Iterator<Topic> roleIterator = roles.iterator();
                            if(roleIterator.hasNext() && !forceStop()) {
                                role = roleIterator.next();
                                if(role != null) {
                                    try {
                                        Topic outRole = findOtherRole(a, role, wandora);
                                        if(outRole != null) {
                                            Topic player = a.getPlayer(role);
                                            setDefaultLogger();
                                            log("Seeking cycles in associations of type '"+TopicToString.toString(a.getType())+"'.");
                                            log("Seeking direction is from role '"+TopicToString.toString(role)+"' to role '"+TopicToString.toString(outRole)+"'.");
                                            log("Starting from player '"+TopicToString.toString(player)+"'.");
                                            List<List<Topic>> cycles = TopicTools.getCyclePaths(player, a.getType(), role, outRole);
                                            if(cycles.isEmpty()) {
                                                log("Found no cycles in addressed association path.");
                                            }
                                            else {
                                                if(cycles.size() == 1) log("Found at least "+cycles.size()+" cycle in addressed association path.");
                                                else log("Found at least "+cycles.size()+" cycles in addressed association path.");
                                                for(int i=0;i<cycles.size(); i++) {
                                                    List<Topic> cycle = cycles.get(i);
                                                    log("Cycle "+(i+1)+", length "+(cycle.size()-1)+":");
                                                    for(Topic t : cycle) {
                                                        log("  "+TopicToString.toString(t));
                                                    }
                                                }
                                            }
                                        }
                                        log("Ready.");
                                        setState(WAIT);
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
                    singleLog("No associations found in context.");
                }
            }
            else {
                singleLog("Illegal context found. Expecting association context.");
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
