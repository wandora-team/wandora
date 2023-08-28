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
 * StealAssociations.java
 *
 * Created on 13.7.2006, 17:09
 *
 */

package org.wandora.application.tools.associations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;



/**
 * <code>StealAssociations</code> tool swaps one player in association.
 * The effect is an association robbery where new player steals association
 * from the old player. Old player topic is deleted. Association between
 * old player and the thief topic is not stoled. Examples: 
 *
 *            c                 c
 *           /                 /
 *    (a# -- b) -- d    ==>    a -- d    ,    [b]
 *           \                 \
 *            e                 e
 *
 *
 *               c                 c
 *              /                 /
 *    a#   ,  (b) -- d    ==>    a -- d    ,    [b]
 *              \                 \
 *               e                 e
 *
 *
 *  x  = topic
 * [x] = topic x is removed
 * (x) = topic x is in context
 * (x -- y) = association is in context
 *  x# = topic is open in topic panel 
 *
 * @author akivela
 */


public class StealAssociations extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	private boolean requiresRefresh = false;
    private boolean deleteOld = true;
    private boolean askForThief = false;
    
    

    public StealAssociations() {
        setContext(new AssociationContext());
    }
    
    
    
    public StealAssociations(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    
    @Override
    public String getName() {
        return "Steal associations";
    }

    @Override
    public String getDescription() {
        return "Swaps one player in associations and deletes old player topic.";
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
            Topic thief = null;
            Topic victim = null;
            Association a = null;
            int counter = 0;
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
                List<Topic> victims = null;
                Iterator<Topic> victimIterator = null;
                associations = context.getContextObjects();
                Iterator<Topic> roles = null;
                Topic role = null;
                Topic player = null;
                
                thief = wandora.getOpenTopic();
                if(thief == null) return;
                while(associations.hasNext() && !forceStop()) {
                    a = (Association) associations.next();
                    if(a != null && !a.isRemoved()) {
                        victims = new ArrayList<Topic>();
                        roles = a.getRoles().iterator();
                        while(roles.hasNext()) {
                            role = roles.next();
                            player = a.getPlayer(role);
                            if(!player.mergesWithTopic(thief)) {
                                victims.add(player);
                            }
                        }
                        a.remove();
                        victimIterator = victims.iterator();
                        while(victimIterator.hasNext()) {
                            victim = victimIterator.next();
                            stealAssociations(thief, victim);
                            if(deleteOld) victim.remove();
                        }
                        counter++;
                    }
                }
                log("Total "+counter+" associations processed.");
            }
            
            else { // TOPIC CONTEXT!!
                if(askForThief) thief = wandora.showTopicFinder("Select new player topic...");
                else thief = wandora.getOpenTopic();
                if(thief == null) return;
                
                Iterator victims = context.getContextObjects();
                long startTime = System.currentTimeMillis();
                while(victims.hasNext() && !forceStop()) {
                    victim = (Topic) victims.next();
                    if(victim != null && !victim.isRemoved()) {
                        stealAssociations(thief, victim);
                        victim.remove();
                    }
                }
                long endTime = System.currentTimeMillis();
                log("Execution took "+((endTime-startTime)/1000)+" seconds.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    public void stealAssociations(Topic thief, Topic victim) {
        try {
            Iterator<Association> associations = victim.getAssociations().iterator();
            stealAssociations(thief, victim, associations);
        }
        catch(Exception e) {
            log(e);
        }
    }

    
    public void stealAssociations(Topic thief, Topic victim, Iterator<Association> associations) {
        try {
            while(associations.hasNext() && !forceStop()) {
                Association a = associations.next();

                Iterator<Topic> roles = a.getRoles().iterator();
                while(roles.hasNext()) {
                    Topic role = roles.next();
                    Topic player = a.getPlayer(role);
                    if(player.mergesWithTopic(victim)) {
                        requiresRefresh = true;
                        a.removePlayer(role);
                        a.addPlayer(thief, role);
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
}
