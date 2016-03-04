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
 * 
 * DuplicateAssociations.java
 *
 * Created on 21. joulukuuta 2004, 12:52
 */

package org.wandora.application.tools.associations;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.util.*;
import org.wandora.application.tools.AbstractWandoraTool;




/**
 * Duplicates context associations. New associations are given a new association 
 * type addressed by Wandora user.
 * 
 * @author  akivela
 */


public class DuplicateAssociations extends AbstractWandoraTool implements WandoraTool {

    private Topic oldAssociationType = null;
    public boolean wasCancelled = false;
    public boolean changeRoles = false;
    public boolean removeAssociations = false;
    private Hashtable roleMap = new Hashtable();
    
    
    public DuplicateAssociations() {
        setContext(new AssociationContext());
    }
    

    @Override
    public String getName() {
        return "Duplicate associations";
    }
    
    
    public void makeRoleMap(Wandora admin)  throws TopicMapException {
        Iterator contextAssociations = getContext().getContextObjects();
        if(contextAssociations == null || !contextAssociations.hasNext()) return;
        if(roleMap == null) roleMap = new Hashtable();

        //BaseNamePrompt prompt=new BaseNamePrompt(admin.getManager(), admin, true);
        //Topic topicOpen = admin.getOpenTopic();
        //Collection associations = topicOpen.getAssociations(oldAssociationType);
        Association association = null;
        Topic role = null;
        while(contextAssociations.hasNext()) {
            association = (Association) contextAssociations.next();
            Collection roles = association.getRoles();
            for(Iterator i2 = roles.iterator(); i2.hasNext(); ) {
                role = (Topic) i2.next();
                if(!roleMap.containsKey(role)) {
                    if(changeRoles) {                           
/*                        prompt.setTitle("Map role '" + getTopicName(role) + "' to...");
                        prompt.setVisible(true);
                        Topic newRole=prompt.getTopic();*/
                        Topic newRole=admin.showTopicFinder("Map role '" + getTopicName(role) + "' to...");                
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
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        wasCancelled = false;
/*        BaseNamePrompt prompt=new BaseNamePrompt(admin.getManager(), admin, true);
        prompt.setTitle("Select new association type...");
        prompt.setVisible(true);
        Topic newAssociationType=prompt.getTopic();*/
        Topic newAssociationType=admin.showTopicFinder("Select new association type...");                
        if (newAssociationType != null) {
            makeRoleMap(admin);
            Topic topicOpen = admin.getOpenTopic();           
            if(topicOpen != null) {
                if(oldAssociationType != null) {
                    Collection ass = topicOpen.getAssociations();
                    TopicMap topicMap = admin.getTopicMap();
                    Vector atemp = new Vector();
                    for(Iterator asi = ass.iterator(); asi.hasNext();) {
                        atemp.add(asi.next());
                    }
                    for(int i=0; i<atemp.size(); i++) {
                        Association a = (Association) atemp.elementAt(i);
                        if(a.getType().equals(oldAssociationType)) {
                            Association ca = topicMap.createAssociation(topicOpen);
                            ca.setType(newAssociationType);                          
                            Collection aRoles = a.getRoles();
                            for(Iterator aRoleIter = aRoles.iterator(); aRoleIter.hasNext(); ) {
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
                    admin.openTopic(topicOpen);
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
