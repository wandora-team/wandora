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
    
    
    

	private static final long serialVersionUID = 1L;




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
            int numberOfRepeats = 1;
            
            log("Number of test topics is "+numberOfTestTopics+".");
            
            
            // -------------------------------------------------- CREATION -----
            if(!forceStop()) {
                hlog("Topic creation test.");
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                setProgress(0);
                setProgressMax(numberOfTestTopics);
                for(int i=0; i<numberOfTestTopics; i++) {
                    String si = "http://wandora.org/si/test-topic/"+System.currentTimeMillis()+"/"+i;
                    Topic t = tm.createTopic();
                    t.addSubjectIdentifier(new Locator(si));
                    sis.add(si);
                    setProgress(i);
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
            
            
            // -------------------------------- FIND BY SUBJECT IDENTIFIER -----
            if(!forceStop()) {
                hlog("Topic find test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    setProgress(p++);
                    if(forceStop()) break; 
                }
                p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
                for(int i=0; i<numberOfTestTopics; i++) {
                    String si = sis.get((int) Math.floor(Math.random()*sis.size()));
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    setProgress(p++);
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
            
            
            
            // -------------------- SUBJECT IDENTIFIER ADDITION AND REMOVE -----
            if(!forceStop()) {
                hlog("Topic add and remove subject identifier test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                int p=0;
                setProgress(0);
                setProgressMax(1);
                
                String si = sis.get(0);
                Topic t = tm.getTopic(si);
                
                Locator testsi = new Locator("http://wandora.org/si/test-subject-identifiers/"+System.currentTimeMillis());
                t.addSubjectIdentifier(testsi);
                
                Collection<Locator> tsis = t.getSubjectIdentifiers();
                if(!tsis.contains(testsi)) {
                    log("Topic doesn't contain added subject identifier.");
                    success = false;
                }
                if(!tsis.contains(new Locator(si))) {
                    log("Topic doesn't contain original subject identifier after another subject identifier is added.");
                    success = false;
                }
                
                Topic t0 = tm.getTopic(si);
                Topic t1 = tm.getTopic(testsi.toExternalForm());
                
                if(!t0.mergesWithTopic(t1)) {
                    log("Two subject identifiers added to a topic resolve different topic. Shouldn't.");
                    success = false;
                }
                
                t.removeSubjectIdentifier(testsi);
                tsis = t.getSubjectIdentifiers();
                if(tsis.contains(testsi)) {
                    log("Topic contains a subject identifier even when it is removed.");
                    success = false;
                }
                
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic add and remove subject identifier test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic add and remove subject identifier test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // ----------------------------------------------- SET BASENAME-----
            if(!forceStop()) {
                hlog("Topic set basename test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        for(int i=0; i<numberOfRepeats; i++) {
                            String basename = getRandomString();
                            t.setBaseName(basename);

                            String basename2 = t.getBaseName();
                            if(!basename.equals(basename2)) {
                                success = false;
                                log("Failed to restore basename "+basename);
                            }
                            if(forceStop()) break;
                        }
                    }
                    setProgress(p++);
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
            
            
            // ------------------------------------------ SET VARIANT NAME -----
            if(!forceStop()) {
                hlog("Topic variant name test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
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
                            log("Failed to restore topic's variant name "+name);
                        }
                    }
                    setProgress(p++);
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
            


            // ------------------------------------------ SET VARIANT NAME -----
            if(!forceStop()) {
                hlog("Topic variant name test 2.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                List<String> sis2 = new ArrayList<String>(sis);
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        for(int j=0; j<numberOfRepeats; j++) {
                            Set<Topic> scope = new HashSet<>();
                            for(int i=0; i<1+Math.round(Math.random()*Math.min(10, numberOfTestTopics)); i++) {
                                scope.add(tm.getTopic(sis2.get((int) Math.floor(Math.random() * sis2.size()))));
                            }

                            String name = getRandomString();
                            t.setVariant(scope, name);

                            Set<Topic> scope2 = new HashSet<>();
                            scope2.addAll(scope);
                            String name2 = t.getVariant(scope2);
                            if(!name.equals(name2)) {
                                success = false;
                                log("Failed to restore topic's variant name "+name);
                            }
                            if(forceStop()) break;
                        }
                        setProgress(p++);
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
            
            
            // --------------------------------------- DELETE VARIANT NAME -----
            if(!forceStop()) {
                hlog("Topic delete variant name test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
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
                            log("Failed to delete all variant names of a topic.");
                        }
                        setProgress(p++);
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
            

            // -------------------------------------------- SET OCCURRENCE -----
            if(!forceStop()) {
                hlog("Topic occurrence test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                List<String> sis2 = new ArrayList<String>(sis);
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
                    else {
                        for(int j=0; j<numberOfRepeats; j++) {
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
                        setProgress(p++);
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
            

            // ----------------------------------------- DELETE OCCURRENCE -----
            if(!forceStop()) {
                hlog("Topic occurrence delete test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
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
                            log("Failed to delete all occurrences of a topic.");
                        }
                        setProgress(p++);
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

            
            // ------------------------------------------- DELETE BASENAME -----
            if(!forceStop()) {
                hlog("Topic delete basename test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
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
                            log("Failed to delete topic's basename "+basename);
                        }
                    }
                    setProgress(p++);
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
            
            
            // ---------------------------------------------- MERGE TOPICS -----
            if(!forceStop()) {
                hlog("Topic merge test.");
                boolean success = true;
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                int p=0;
                setProgress(p);
                setProgressMax(1);
                String si0 = sis.get(0);
                String si1 = sis.get(1);
                
                Topic t0 = tm.getTopic(si0);
                Topic t1 = tm.getTopic(si1);
                
                t0.addSubjectIdentifier(new Locator(si1));
                
                if(!t1.isRemoved()) {
                    log("Merged topic is not marked removed. Should be marked removed.");
                    success = false;
                }
                if(t0.isRemoved()) {
                    log("Merging topic is marked removed. Shouldn't be marked removed.");
                    success = false;
                }
                
                t0 = tm.getTopic(si0);
                t1 = tm.getTopic(si1);
                
                if(t0 == null || !t0.mergesWithTopic(t1)) {
                    log("Subject identifiers don't return same topic after merge. Should return.");
                    success = false;
                }
                
                int topicCountAfterMerge = tm.getNumTopics();
                
                if(topicCountAfterMerge >= topicCount) {
                    log("Number of topics hasn't decreased during the merge. Should have decreased by one.");
                    success = false;
                }
                
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic merge test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic merge test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // -------------------------------------------- MERGE TOPICS 2 -----
            if(!forceStop()) {
                hlog("Topic merge test 2.");
                boolean success = true;
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                int p=0;
                setProgress(p);
                setProgressMax(1);
                String si0 = sis.get(2);
                String si1 = sis.get(3);
                
                Topic t0 = tm.getTopic(si0);
                Topic t1 = tm.getTopic(si1);
                
                String randomBasename = getRandomString();
                t1.setBaseName(randomBasename);
                t0.setBaseName(randomBasename); // Merges t1 into t0.

                if(!t1.isRemoved()) {
                    log("Merged topic is not marked removed. Should be removed.");
                    success = false;
                }
                if(t0.isRemoved()) {
                    log("Merging topic is marked removed. Shouldn't be removed.");
                    success = false;
                }
                
                t0 = tm.getTopic(si0);
                t1 = tm.getTopic(si1);
                Topic t2 = tm.getTopicWithBaseName(randomBasename);
                
                if(t0 == null || !t0.mergesWithTopic(t1)) {
                    log("Subject identifiers don't return same topic after merge. Should return.");
                    success = false;
                }
                if(t2 == null || !t2.mergesWithTopic(t0)) {
                    log("Basename doesn't return same topic as the subject identifier. Should return.");
                    success = false;
                }
                if(t2 == null || !t2.mergesWithTopic(t1)) {
                    log("Basename doesn't return same topic as the subject identifier. Should return.");
                    success = false;
                }
                
                int topicCountAfterMerge = tm.getNumTopics();
                
                if(topicCountAfterMerge >= topicCount) {
                    log("Number of topics hasn't decreased during the merge. Should have decreased by one.");
                    success = false;
                }
                
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic merge test 2 passed in "+testTime+"ms.");
                }
                else {
                    log("Topic merge test 2 failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            
            // -------------------------------------------- MERGE TOPICS 3 -----
            if(!forceStop()) {
                hlog("Topic merge test 3.");
                boolean success = true;
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                int p=0;
                setProgress(p);
                setProgressMax(1);
                String si0 = sis.get(4);
                String si1 = sis.get(5);
                
                Topic t0 = tm.getTopic(si0);
                Topic t1 = tm.getTopic(si1);
                
                String bn0 = getRandomString();
                String bn1 = getRandomString();
                
                t1.setBaseName(bn1);
                t0.setBaseName(bn0);
                
                Locator sl = new Locator("http://wandora.org/si/test-subject-locator-merging/"+System.currentTimeMillis());
                
                t1.setSubjectLocator(sl);
                t0.setSubjectLocator(sl); // Merges t1 into t0.

                if(!t1.isRemoved()) {
                    log("Merged topic is not marked removed. Should be removed.");
                    success = false;
                }
                if(t0.isRemoved()) {
                    log("Merging topic is marked removed. Shouldn't be removed.");
                    success = false;
                }
                
                String bna = t0.getBaseName();
                
                if(bn0.equals(bna)) {
                    log("After merge the basename of the merged topic is the name of the merging topic. Shouldn't be.");
                    success = false;
                }
                if(!bn1.equals(bna)) {
                    log("After merge the basename of the merged topic is not the name of the removed topic. Should be.");
                    success = false;
                }
                
                t0 = tm.getTopic(si0);
                t1 = tm.getTopic(si1);
                
                if(!t0.mergesWithTopic(t1)) {
                    log("Subject identifiers don't return same topic after the merge. Should return.");
                    success = false;
                }

                int topicCountAfterMerge = tm.getNumTopics();
                
                if(topicCountAfterMerge >= topicCount) {
                    log("Number of topics hasn't decreased during the merge. Should have decreased by one.");
                    success = false;
                }
                
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic merge test 3 passed in "+testTime+"ms.");
                }
                else {
                    log("Topic merge test 3 failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            // ------------------------------------------------ TEST TYPES -----
            if(!forceStop()) {
                hlog("Topic type test.");
                boolean success = true;
                long startTime = System.currentTimeMillis();
                int p=0;
                setProgress(p);
                setProgressMax(1);
                String si0 = sis.get(6);
                String si1 = sis.get(7);
                
                Topic t0 = tm.getTopic(si0);
                Topic t1 = tm.getTopic(si1);
                
                t0.addType(t1);
                
                if(!t0.isOfType(t1)) {
                    log("Failed to retrieve type with isOfType.");
                    success = false;
                }
                
                Collection<Topic> types = t0.getTypes();
                if(types == null || !types.contains(t1)) {
                    log("Failed to retrieve type with getTypes.");
                    success = false;
                }
                
                t0.removeType(t1);
                
                if(t0.isOfType(t1)) {
                    log("IsOfType still gets the type even if removed.");
                    success = false;
                }
                
                types = t0.getTypes();
                if(types != null && types.contains(t1)) {
                    log("GetTypes still gets the type even if removed.");
                    success = false;
                }
                
                long testTime = System.currentTimeMillis() - startTime; 
                if(success) {
                    log("Topic type test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic type test failed in "+testTime+"ms.");
                    numberOfFails++;
                }
            }
            
            // ---------------------------------------------- DELETE TOPIC -----
            if(!forceStop()) {
                hlog("Topic delete test.");
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                int deleteCount = 0;
                int p=0;
                setProgress(p);
                setProgressMax(numberOfTestTopics);
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t != null) {
                        if(!t.isRemoved()) {
                            deleteCount++;
                        }
                        t.remove();
                    }
                    setProgress(p++);
                    if(forceStop()) break;
                }
                int newTopicCount = tm.getNumTopics();
                long testTime = System.currentTimeMillis() - startTime;

                if(deleteCount > 0 && topicCount-deleteCount == newTopicCount) {
                    log("Topic delete test passed in "+testTime+"ms.");
                }
                else {
                    log("Topic delete test failed in "+testTime+"ms.");
                    log("Number of topics before delete was "+topicCount);
                    log("Number of topics after delete was "+newTopicCount);
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
    
    
    
    
    private String getRandomString() {
        StringBuilder stringBuilder = new StringBuilder("");
        for(long i=Math.round(100+Math.random()*2000); i>0; i--) {
            stringBuilder.append((char) Math.floor(1+Math.random()*9000));
        }
        return stringBuilder.toString();
    }
    
    
    
}
