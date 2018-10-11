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
 * InsertPlayer.java
 *
 * Created on 2. marraskuuta 2007, 13:29
 *
 */

package org.wandora.application.tools.associations;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;



/**
 * <p>
 * Insert new player to given associations. Tool requests
 * player and role topics, and adds them to given associations. Same
 * player-role pair is added to every association.
 * </p>
 *
 * @author akivela
 */



public class InsertPlayer extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	
	private boolean requiresRefresh = false;
    
    
    public InsertPlayer() {
        setContext(new AssociationContext());
    }
    
    
    public InsertPlayer(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    @Override
    public String getName() {
        return "Insert player to association(s)";
    }

    @Override
    public String getDescription() {
        return "Insert new player to given association(s)";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            requiresRefresh = false;
            Topic player = null;
            Topic role = null;
            Association a = null;
            int counter = 0;
            
            setDefaultLogger();
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
                Iterator associations = context.getContextObjects();
                if(associations.hasNext()) {
                    //System.out.println("admin == "+wandora);
                    GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Select player and role","Select player and it's role in associations. Selecting existing role overrides old player.",true,new String[][]{
                        new String[]{"Role","topic",null},
                        new String[]{"Player","topic",null},
                    },wandora);
                    setState(INVISIBLE);
                    god.setVisible(true);
                    if(!god.wasCancelled()) {
                        Map<String,String> values=god.getValues();
                        String roleSI = values.get("Role");
                        String playerSI = values.get("Player");
                        TopicMap topicmap = wandora.getTopicMap();
                        role = topicmap.getTopic(roleSI);
                        player = topicmap.getTopic(playerSI);
                    }
                    setState(VISIBLE);
                    
                    if(role == null) {
                        log("Intended role topic not found!");
                    }
                    if(player == null) {
                        log("Intended player topic not found!");
                    }

                    if(role != null && player != null) {
                        log("Inserting player '"+getTopicName(player)+"' in role '"+getTopicName(role)+"' to associations.");
                        while(associations.hasNext() && !forceStop()) {
                            a = (Association) associations.next();
                            if(a != null && !a.isRemoved()) {
                                try {
                                    counter++;
                                    a.addPlayer(player, role);
                                    requiresRefresh = true;
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                            else {
                                log("Found association is null. Rejecting association.");
                            }
                        }
                        log("Total "+counter+" players inserted to associations!");
                    }
                }
                else {
                    log("Selection contains no associations!");
                }
            }
            else {
                log("Illegal context found. Expecting association context.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    

}