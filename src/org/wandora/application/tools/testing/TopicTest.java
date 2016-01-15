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
 */


package org.wandora.application.tools.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author akivela
 */
public class TopicTest extends AbstractWandoraTool implements WandoraTool {
    
    
    
    @Override
    public String getName() {
        return "Various topic tests";
    }

    @Override
    public String getDescription() {
        return "Various topic tests.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Various topic tests");
            
            TopicMap tm = wandora.getTopicMap();
            ArrayList<String> sis = new ArrayList();
            int numberOfTestTopics = 200;
            int numberOfFails = 0;
            
            log("Number of test topics is "+numberOfTestTopics+".");
            
            // ----- CREATION -----
            if(!forceStop()) {
                hlog("Topic creation test.");
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                for(int i=0; i<numberOfTestTopics; i++) {
                    String si = "http://wandora.org/si/test-topic/"+System.currentTimeMillis()+"/"+i;
                    Topic t = tm.createTopic();
                    t.addSubjectIdentifier(new Locator(si));
                    sis.add(si);
                    if(forceStop()) break; 
                }
                int newTopicCount = tm.getNumTopics();
                long testTime = System.currentTimeMillis() - startTime; 
                if(newTopicCount == topicCount+numberOfTestTopics) {
                    log("Topic creation test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic creation test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // ----- FIND BY SUBJECT IDENTIFIER -----
            if(!forceStop()) {
                hlog("Topic find test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    if(forceStop()) break; 
                }
                for(int i=0; i<200; i++) {
                    String si = sis.get((int) Math.floor(Math.random()*sis.size()));
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    if(forceStop()) break; 
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic find test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic find test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // ----- SET BASENAME-----
            if(!forceStop()) {
                hlog("Topic set basename test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        for(int i=0; i<100; i++) {
                            String basename = getRandomString();
                            t.setBaseName(basename);

                            String basename2 = t.getBaseName();
                            if(!basename.equals(basename2)) {
                                success = false;
                                log("Failed to restore basename "+basename);
                            }
                        }
                    }
                    if(forceStop()) break;
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic set basename test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic set basename test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // ----- SET VARIANT NAME -----
            if(!forceStop()) {
                hlog("Topic variant name test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        String name = getRandomString();
                        t.setDisplayName("en", name);
                        
                        String name2 = t.getDisplayName("en");
                        if(!name.equals(name2)) {
                            success = false;
                            log("Failed to restore name "+name);
                        }
                    }
                    if(forceStop()) break;
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic variant name test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic variant name test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            


            // ----- SET VARIANT NAME -----
            if(!forceStop()) {
                hlog("Topic variant name test 2.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                List<String> sis2 = new ArrayList<String>(sis);
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        for(int j=0; j<100; j++) {
                            Set<Topic> scope = new HashSet();
                            for(int i=0; i<1+Math.round(Math.random()*Math.min(10, numberOfTestTopics)); i++) {
                                scope.add(tm.getTopic(sis2.get((int) Math.floor(Math.random() * sis2.size()))));
                            }

                            String name = getRandomString();
                            t.setVariant(scope, name);

                            Set<Topic> scope2 = new HashSet();
                            scope2.addAll(scope);
                            String name2 = t.getVariant(scope2);
                            if(!name.equals(name2)) {
                                success = false;
                                log("Failed to restore name "+name);
                            }
                            if(forceStop()) break;
                        }
                        if(forceStop()) break;
                    }
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic variant name test 2 passed in "+testTime+"ms.");
                }
                else {
                    log("Topic variant name test 2 failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // ----- DELETE VARIANT NAME -----
            if(!forceStop()) {
                hlog("Topic delete variant name test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        Set<Set<Topic>> scopes = t.getVariantScopes();
                        for(Set<Topic> scope : scopes) {
                            t.removeVariant(scope);
                        }
                        scopes = t.getVariantScopes();
                        if(!scopes.isEmpty()) {
                            success = false;
                            log("Failed to delete all variant names.");
                        }
                        if(forceStop()) break;
                    }
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic delete variant name test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic delete variant name test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            

            // ----- SET OCCURRENCE -----
            if(!forceStop()) {
                hlog("Topic occurrence test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                List<String> sis2 = new ArrayList<String>(sis);
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        for(int j=0; j<100; j++) {
                            Topic type = tm.getTopic(sis2.get((int) Math.floor(Math.random() * sis2.size())));
                            Topic version = tm.getTopic(sis2.get((int) Math.floor(Math.random() * sis2.size())));

                            String occurrence = getRandomString();
                            t.setData(type, version, occurrence);

                            String occurrence2 = t.getData(type, version);
                            if(!occurrence.equals(occurrence2)) {
                                success = false;
                                log("Failed to restore occurrence "+occurrence);
                            }
                            if(forceStop()) break;
                        }
                        if(forceStop()) break;
                    }
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic occurrence test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic occurrence test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            

            // ----- DELETE OCCURRENCE -----
            if(!forceStop()) {
                hlog("Topic occurrence delete test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        Collection<Topic> dataTypes = t.getDataTypes();
                        for(Topic dataType : dataTypes) {
                            if(Math.random() > 0.5) {
                                t.removeData(dataType);
                            }
                            else {
                                Hashtable<Topic,String> datas = t.getData(dataType);
                                for(Topic version : datas.keySet()) {
                                    t.removeData(dataType, version);
                                }
                            }
                        }
                        dataTypes = t.getDataTypes();
                        if(!dataTypes.isEmpty()) {
                            success = false;
                            log("Failed to delete all occurrences.");
                        }
                        if(forceStop()) break;
                    }
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic occurrence delete test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic occurrence delete test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }

            
            // ----- DELETE BASENAME -----
            if(!forceStop()) {
                hlog("Topic delete basename test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        t.setBaseName(null);
                        
                        String basename = t.getBaseName();
                        if(basename != null) {
                            success = false;
                            log("Failed to delete basename "+basename);
                        }
                    }
                    if(forceStop()) break;
                }
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic delete basename test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic delete basename test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // ----- DELETE TOPIC -----
            if(!forceStop()) {
                hlog("Topic delete test.");
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                    }
                    else {
                        t.remove();
                    }
                    if(forceStop()) break;
                }
                int newTopicCount = tm.getNumTopics();
                long testTime = System.currentTimeMillis() - startTime; 
                if(topicCount-numberOfTestTopics == newTopicCount) {
                    log("Topic delete test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic delete test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            // ----- END OF TESTS -----
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
    
    
    
    
    private String getRandomString() {
        StringBuilder stringBuilder = new StringBuilder("");
        for(long i=Math.round(100+Math.random()*2000); i>0; i--) {
            stringBuilder.append((char) Math.floor(1+Math.random()*9000));
        }
        return stringBuilder.toString();
    }
    
    
    
}
