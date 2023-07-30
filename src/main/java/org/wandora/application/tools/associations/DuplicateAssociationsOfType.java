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
 * 
 * DuplicateAssociationsOfType.java
 *
 * Created on 21. joulukuuta 2004, 12:52
 */

package org.wandora.application.tools.associations;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;




/**
 * Duplicate associations of given type. New associations will be typed with a
 * new association type.
 * 
 * @author  akivela
 */


public class DuplicateAssociationsOfType extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;
	
	
	private Topic theTopic = null;
    private Topic oldAssociationType = null;
    public boolean wasCancelled = false;
    public boolean changeRoles = false;
    public boolean removeAssociations = false;
    private Map<Topic,Object> roleMap = new LinkedHashMap<>();

    
    
    public DuplicateAssociationsOfType() {
    }
    public DuplicateAssociationsOfType(boolean shouldChangeRoles) {
        changeRoles = shouldChangeRoles;
    }
    public DuplicateAssociationsOfType(boolean shouldChangeRoles, boolean shouldRemoveAssociations) {
        changeRoles = shouldChangeRoles;
        removeAssociations = shouldRemoveAssociations;
    }
    
    

    @Override
    public String getName() {
        return "Duplicate associations of type";
    }
    
    
    public void makeRoleMap(Wandora admin)  throws TopicMapException {
//        BaseNamePrompt prompt=new BaseNamePrompt(admin.getManager(), admin, true);
        roleMap = new LinkedHashMap<>();
        //System.out.println("Type: " + oldAssociationType.getBaseName());
        Collection<Association> associations = theTopic.getAssociations(oldAssociationType);
        Iterator<Association> associationIterator = associations.iterator(); 
        Association association = null;
        Topic role = null;
        while(associationIterator.hasNext()) {
            association = (Association) associationIterator.next();
            Collection<Topic> roles = association.getRoles();
            for(Iterator<Topic> i2 = roles.iterator(); i2.hasNext(); ) {
                role = (Topic) i2.next();
                if(!roleMap.containsKey(role)) {
                    if(changeRoles) {
/*                        prompt.setTitle("Map role '" + getTopicName(role) + "' to...");
                        prompt.setVisible(true);
                        Topic newRole=prompt.getTopic();*/
                        Topic newRole=admin.showTopicFinder("Map role '"+getTopicName(role)+"' to...");                
                        if(newRole != null) {
                            roleMap.put(role, newRole);
                        }
                        else {
                            int answer = WandoraOptionPane.showConfirmDialog(admin ,
                                "Would you like exclude players of role " + getTopicName(role) + " from the association? " +
                                "Press Yes to exclude players! "+
                                "Press No to use existing role topic!",
                                "Exclude role players or use existing role?",
                                WandoraOptionPane.YES_NO_OPTION);
                            if(answer == WandoraOptionPane.NO_OPTION) {
                                roleMap.put(role, role); // Don't change roles!
                            }
                            else {
                                roleMap.put(role, "EXCLUDE");
                            }
                        }
                    }
                    else {
                        roleMap.put(role, role); // Don't change roles!
                    }
                }
            }
        }
    }
    
    
    
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        theTopic = wandora.getOpenTopic();
        oldAssociationType = (Topic) context.getContextObjects().next();
        wasCancelled = false;
/*      
        BaseNamePrompt prompt=new BaseNamePrompt(admin.getManager(), admin, true);
        prompt.setTitle("Select new association type...");
        prompt.setVisible(true);
        Topic newAssociationType=prompt.getTopic();
*/
        Topic newAssociationType=wandora.showTopicFinder("Select new association type...");                
        if (newAssociationType != null) {
            makeRoleMap(wandora);        
            //System.out.println("new type: " + newAssociationType.getBaseName());
            if(theTopic != null) {
                if(oldAssociationType != null) {
                    Collection<Association> ass = theTopic.getAssociations();
                    TopicMap topicMap = null;
                    List<Association> atemp = new ArrayList<>();
                    for(Iterator<Association> asi = ass.iterator(); asi.hasNext();) {
                        atemp.add(asi.next());
                    }
                    for(int i=0; i<atemp.size(); i++) {
                        Association a = (Association) atemp.get(i);
                        if(a.getType().equals(oldAssociationType)) {
                            topicMap = a.getTopicMap();
                            Association ca = topicMap.createAssociation(newAssociationType);
                            //System.out.println("new type: " + newAssociationType.getBaseName());
                            Collection<Topic> aRoles = a.getRoles();
                            for(Iterator<Topic> aRoleIter = aRoles.iterator(); aRoleIter.hasNext(); ) {
                                Topic role = (Topic) aRoleIter.next();
                                Topic player = a.getPlayer(role);
                                if(roleMap != null) {
                                    if(roleMap.get(role) instanceof Topic) {
                                        Topic mappedRole =(Topic) roleMap.get(role);
                                        if(mappedRole != null && mappedRole instanceof Topic) {
                                            //log("mapped role == " + mappedRole.getBaseName());
                                            ca.addPlayer(player, mappedRole);
                                        }
                                    }
                                }
                                else {
                                    //log("role == " + role.getBaseName());
                                    ca.addPlayer(player, role);
                                }
                            }
                            if(! ca.equals(a) && removeAssociations) {
                                a.remove();
                            }
                        }
                    }
                }
                else {
                    log("Can't solve old association type!");
                }
            }
            else {
                log("Can't solve open topic!");
            }
        }
        else {
            wasCancelled = true;
        }
    }
    
    
}
