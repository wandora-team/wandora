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
            
            // ----- CREATION -----
            {
                hlog("Topic creation test.");
                long startTime = System.currentTimeMillis();
                int topicCount = tm.getNumTopics();
                for(int i=0; i<numberOfTestTopics; i++) {
                    String si = "http://wandora.org/si/test-topic/"+System.currentTimeMillis()+"/"+i;
                    Topic t = tm.createTopic();
                    t.addSubjectIdentifier(new Locator(si));
                    sis.add(si);
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
            {
                hlog("Topic find test.");
                long startTime = System.currentTimeMillis();
                boolean success = true;
                for(String si : sis) {
                    Topic t = tm.getTopic(si);
                    if(t == null) {
                        log("Can't find topic for subject identifier "+si);
                        success = false;
                    }
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
            
            // ----- DELETE -----
            {
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
    
    
    
    
    
    
    
}
