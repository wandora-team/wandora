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
 */


package org.wandora.application.tools.testing;

import java.util.ArrayList;
import java.util.Collection;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author akivela
 */
public class AssociationTest extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	@Override
    public String getName() {
        return "Various association tests";
    }

    @Override
    public String getDescription() {
        return "Various association tests.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Various association tests");
            
            TopicMap tm = wandora.getTopicMap();
            ArrayList<String> sis = new ArrayList<>();
            int numberOfTestTopics = 30;
            int numberOfFails = 0;
            int numberOfRepeats = 1;
            int maxNumberOfRoles = 15;
            
            
            // --------------------------------------------- CREATE TOPICS -----
            if(!forceStop()) {
                hlog("Topic creation.");
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                setProgress(0);
                setProgressMax(numberOfTestTopics);
                for(int i=0; i<numberOfTestTopics; i++) {
                    String si = "http://wandora.org/si/test-topic/"+System.currentTimeMillis()+"/"+i;
                    Topic t = tm.createTopic();
                    t.addSubjectIdentifier(new Locator(si));
                    t.setBaseName("t"+(i<10 ? "0" : "")+i);
                    sis.add(si);
                    setProgress(i);
                    if(forceStop()) break; 
                }
                int newTopicCount = tm.getNumTopics();
                long testTime = System.currentTimeMillis() - startTime; 
                if(newTopicCount != topicCount+numberOfTestTopics) {
                    log("Topic creation failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }

            // --------------------------------------- CREATE ASSOCIATIONS -----
            if(!forceStop()) {
                hlog("Association creation test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                setProgress(0);
                setProgressMax(maxNumberOfRoles);
                
                int numberOfAssociations = tm.getNumAssociations();
                
                String typesi = sis.get(0);
                Topic type = tm.getTopic(typesi);

                ArrayList<Association> associations = new ArrayList<Association>();
                
                for(int k=1; k<maxNumberOfRoles; k++) {
                    setProgress(k);
                    ArrayList<Topic> roles = new ArrayList<Topic>();
                    for(int j=0; j<k; j++) {
                        String rolesi = sis.get(j);
                        Topic role = tm.getTopic(rolesi);
                        roles.add(role);
                    }
                    for(int i=0; i<numberOfTestTopics-k; i++) {
                        Association a = tm.createAssociation(type);
                        for(int j=0; j<k; j++) {
                            String playersi = sis.get(i+j);
                            Topic player = tm.getTopic(playersi);
                            a.addPlayer(player, roles.get(j));
                        }
                        associations.add(a);
                    }
                }
                
                int numberOfAssociationsAfterwards = tm.getNumAssociations();
                int delta = numberOfAssociationsAfterwards-numberOfAssociations;
                
                if(delta != numberOfTestTopics-1) {
                    log("Wrong number of associations created. Associations don't merge properly.");
                    success = false;
                }
                
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Association creation test passed in "+testTime+"ms.");
                }
                else {
                    log("Association creation test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            // ------------------------------------------ GET ASSOCIATIONS -----
            if(!forceStop()) {
                hlog("Association access test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                
                Topic t0 = tm.getTopic(sis.get(0));
                Collection<Association> assocs = tm.getAssociationsOfType(t0);
                
                if(assocs.size() != numberOfTestTopics-1) {
                    log("Number of requested associations is wrong.");
                    success = false;
                }
                
                for(Association a : assocs) {
                    Topic type = a.getType();
                    if(!t0.mergesWithTopic(type)) {
                        log("Association type is not the expected one.");
                        success = false;
                    }
                }
                
                for(int i=0; i<maxNumberOfRoles; i++) {
                    Topic ti = tm.getTopic(sis.get(i));
                    Collection<Association> tia = ti.getAssociations();
                    int n = Math.min(i+1, maxNumberOfRoles-1);
                    if(tia.size() != n) {
                        log("Number of associations is not the expected one ("+n+").");
                        success = false;
                    }
                }
                
                for(Association a : assocs) {
                    Topic p = a.getPlayer(t0);
                    int n = Integer.parseInt(p.getBaseName().substring(1));
                    for(int i=1; i<maxNumberOfRoles; i++) {
                        if(n+i < maxNumberOfRoles) {
                            Topic ri = tm.getTopic(sis.get(i));
                            Topic pi = a.getPlayer(ri);
                            
                            if(pi != null) {
                                Topic px = tm.getTopic(sis.get(n+i));
                                if(!pi.mergesWithTopic(px)) {
                                    log("Association has wrong player.");
                                    success = false;
                                }
                            }
                        }
                    }
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Association access test passed in "+testTime+"ms.");
                }
                else {
                    log("Association access test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // --------------------------------------- DELETE ASSOCIATIONS -----
            if(!forceStop()) {
                hlog("Association delete test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                
                Topic t0 = tm.getTopic(sis.get(0));
                Collection<Association> assocs = tm.getAssociationsOfType(t0);
                
                for(Association a : assocs) {
                    a.remove();
                }
                
                assocs = tm.getAssociationsOfType(t0);
                if(assocs.size() > 0) {
                    log("Failed to remove all associations");
                    success = false;
                }
                
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Association delete test passed in "+testTime+"ms.");
                }
                else {
                    log("Association delete test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            // ---------------------------------------------- END OF TESTS -----
            if(numberOfFails > 0) {
                log("Failed "+numberOfFails+" tests.");
            }
            else {
                log("Passed all tests.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    
}
