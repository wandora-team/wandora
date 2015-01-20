/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * PasteAssociationsOfType.java
 *
 * Created on 22. joulukuuta 2004, 12:27
 */

package org.wandora.application.tools.oldies;




import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.utils.*;
import java.util.*;




/**
 *
 * @author  akivela
 */
public class PasteAssociationsOfType extends AbstractWandoraTool implements WandoraTool {

    private Topic associationType = null;
    private boolean HTMLOutput = false;
    


    
    
    @Override
    public String getName() {
        return "Paste association of type";
    }
    
    
    
    
    public void execute(Wandora wandora, Context context) {
        TopicMap topicMap = wandora.getTopicMap();
        Topic topicOpen = wandora.getOpenTopic();
        
        if(topicOpen != null) {
            if(associationType == null) {
                Iterator associationTypes = getContext().getContextObjects();
                if(associationTypes != null && associationTypes.hasNext()) {
                    associationType = (Topic) associationTypes.next();
                }
            }
            if(associationType != null) {
                if(WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to paste associations using text on clipboard?","Confirm paste", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                    Association a = null;
                    String tabText = ClipboardBox.getClipboard();
                    StringTokenizer st = new StringTokenizer(tabText, "\n");
                    try {
                        String aType = null;
                        if(st.hasMoreTokens()) aType = st.nextToken();
                        else { 
                            WandoraOptionPane.showMessageDialog(wandora ,"Clipboard contains no association type! No associations created!","Association type missing!",WandoraOptionPane.ERROR_MESSAGE); 
                            return;
                        }

                        String aRoles = null;
                        if(st.hasMoreTokens()) aRoles = st.nextToken();
                        else { 
                            WandoraOptionPane.showMessageDialog(wandora ,"Clipboard contains no association roles!\nNo associations created!","Association roles missing!",WandoraOptionPane.ERROR_MESSAGE); 
                            return;
                        }
                        Vector aRoleVector = vectorize(aRoles, "\t");

                        if(!st.hasMoreTokens()) {
                            WandoraOptionPane.showMessageDialog(wandora ,"Clipboard contains no association players! No associations created!","Association players missing!",WandoraOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        int count = 0;
                        int line = 0;
                        boolean hasAssociations = false;
                        boolean forceStop = false;
                        while(st.hasMoreTokens() && aType != null && aRoles != null && !forceStop) {
                            line++;
                            hasAssociations = true;
                            Vector playerVector = vectorize(st.nextToken(), "\t");                       
                            if(playerVector.size() == aRoleVector.size()) {
                                if(playerVector.contains(topicOpen.getBaseName()) || aRoleVector.contains(topicOpen.getBaseName())) {
                                    Vector aPlayerTopicVector = getTopics(wandora, topicMap, playerVector);
                                    Vector aRoleTopicVector = getTopics(wandora, topicMap, aRoleVector);
                                    a = null;
                                    for(int i=0; i<playerVector.size(); i++) {
                                        Topic player = (Topic) aPlayerTopicVector.elementAt(i);
                                        Topic role = (Topic) aRoleTopicVector.elementAt(i);
                                        if(player != null && role != null) {
                                            if(a == null) {
                                                a = topicMap.createAssociation(getTopic(wandora, topicMap, aType));
                                                count++;
                                            }
                                            a.addPlayer(player, role);
                                        }
                                    }
                                }
                                else {
                                    int answer = WandoraOptionPane.showConfirmDialog(wandora ,"Association must contain a player or a role equal to the edited topic. " +
                                                                            "Association on line " + line + " fulfills neither of these requirements! " +
                                                                            "Would you like to cancel the operation?",
                                                                            "Required player or role missing!",
                                                                            WandoraOptionPane.YES_NO_OPTION);
                                    if(answer == WandoraOptionPane.YES_OPTION) forceStop = true;
                                }
                            }
                            else {
                                int answer = WandoraOptionPane.showConfirmDialog(wandora ,"Number of players is not equal to number of roles! " +
                                                                                "Rejecting association! " +
                                                                                "Would you like to cancel the operation?",
                                                                                "Player number mismatch number of roles!",
                                                                                WandoraOptionPane.YES_NO_OPTION);
                                if(answer == WandoraOptionPane.YES_OPTION) forceStop = true;
                            }
                        }
                        if(hasAssociations && count == 0) {
                            WandoraOptionPane.showMessageDialog(wandora ,"Association creation failed! No associations created!","No associations created!",WandoraOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch (Exception e) {
                        log("Exception '" + e.toString()+ "' occurred while creating associations!", e);
                    }
                }
            }
            else {
                log("Clipboard contains no association type! Can't solve association type! No associations created!");
            }
        }
        else {
            log("Can't solve open topic!");
        }
        
    }    
    
    
    
    
    public Vector vectorize(String str, String delim) {
        Vector v = new Vector();
        if(str != null && str.length() > 0) {
            StringTokenizer sto = new StringTokenizer(str, delim);
            while(sto.hasMoreTokens()) {
                v.add(sto.nextToken());
            }
        }
        return v;
    }
    
    
    
    public Topic getTopic(Wandora admin, TopicMap topicMap, String topicName)  throws TopicMapException {
        if(topicName != null) {
            topicName = Textbox.trimExtraSpaces(topicName);
            if(topicName.length() > 0) {
                Topic t = topicMap.getTopicWithBaseName(topicName);
                if(t == null) {
                    t = topicMap.createTopic();
                    t.setBaseName(topicName);
                    long time = System.currentTimeMillis();
                    String si = "http://wandora.org/si/" + time;
                    int counter = 1;
                    while(topicMap.getTopic(si) != null && counter++ < 1000) {
                        si = "http://wandora.org/si/" + time + "-" + counter;
                    }
                    t.addSubjectIdentifier(new Locator(si));
                    WandoraOptionPane.showMessageDialog(admin ,"Topic does not exists! Created new topic with base name '" + topicName + "'.","New topic created!",WandoraOptionPane.INFORMATION_MESSAGE);
                }
                return t;
            }
        }
        return null;
    }
   
    
    
    public Vector getTopics(Wandora admin, TopicMap topicMap, Vector topicNames)  throws TopicMapException {
        Vector topics = new Vector();
        for(int i=0; i<topicNames.size(); i++) {
            topics.add(getTopic(admin, topicMap, (String) topicNames.elementAt(i)));
        }
        return topics;
    }
    
    
}
