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
 * SplitToBinaryAssociations.java
 *
 * Created on 1. marraskuuta 2007, 16:33
 *
 */

package org.wandora.application.tools.associations;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;



/**
 * <p>
 * Tool splits given n-ary association (n>2) to (n-1) 
 * binary associations. Selected player is used in every generated
 * binary association. For example, think 5-ary association with player c
 * selected:
 * </p>
 * <code>
 *
 *
 *     a-b-(c)-d-e       -->        a-c, b-c, d-c, e-c
 *
 *
 * </code>
 *
 * @author akivela
 */
public class SplitToBinaryAssociations extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private boolean requiresRefresh = false;
    
    
    /** Creates a new instance of SplitToBinaryAssociations */
    public SplitToBinaryAssociations() {
        setContext(new AssociationContext());
    }
    
    
    public SplitToBinaryAssociations(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Split to binary associations";
    }
    @Override
    public String getDescription() {
        return "Splits n-ary associations to several binary associations.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            requiresRefresh = false;
            TopicMap topicmap = wandora.getTopicMap();
            Iterator associations = null;
            Topic baseRole = null;
            Topic base = null;
            Topic player = null;
            Topic role = null;
            Association a = null;
            Association splita = null;
            int oldCounter = 0;
            int newCounter = 0;
            
            int answer = WandoraOptionPane.showConfirmDialog(wandora, "Tool splits selected n-ary associations to binary associations. Selected player topic is interpreted as the base player. The base player is same in every created association. The other part of created binary associations is one of the other players in source associations. Are you sure you want convert selected associations to binary associations?", "Convert to binary associations?");
            if(answer != WandoraOptionPane.YES_OPTION) return;
            
            setDefaultLogger();
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
                associations = context.getContextObjects();
                
                base = wandora.getOpenTopic();
                if(base == null) {
                    log("Base topic not found. Rejecting association.");
                }
                else {
                    log("Found base topic " + TopicToString.toString(base));
                    while(associations.hasNext() && !forceStop()) {
                        baseRole = null;
                        role = null;
                        player = null;
                        a = (Association) associations.next();
                        Iterator<Topic> roles = null;
                        Map<Topic,Topic> splits = new LinkedHashMap<>();
                        if(a != null && !a.isRemoved()) {
                            roles = a.getRoles().iterator();
                            while(roles.hasNext()) {
                                role = roles.next();
                                player = a.getPlayer(role);

                                if(!player.mergesWithTopic(base)) {
                                    splits.put(role, player);
                                }
                                else {
                                    log("Found base role " + TopicToString.toString(role));
                                    baseRole = role;
                                }
                            }

                            if(splits.size() < 2) { log("Found less than 2 players in selected association. Rejecting association."); continue; }
                            if(baseRole == null) { log("Base topic role not found! Rejecting association!"); continue; }

                            oldCounter++;
                            a.remove();
                            Iterator<Topic> splitIterator = splits.keySet().iterator();
                            while(splitIterator.hasNext() && !forceStop()) {
                                try {
                                    role = (Topic) splitIterator.next();
                                    player = (Topic) splits.get(role);
                                    if(role != null && player != null) {
                                        requiresRefresh = true;
                                        splita = topicmap.createAssociation(role);
                                        splita.addPlayer(player, role);
                                        splita.addPlayer(base, baseRole);
                                        newCounter++;
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                    }
                    log("Total "+oldCounter+" associations splitted.");
                    log("Total "+newCounter+" binary associations created.");
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
