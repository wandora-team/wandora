/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * VariantRemover.java
 *
 * Created on 22. toukokuuta 2006, 17:04
 *
 */

package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;




/**
 * Implements <code>WandoraAdminTool</code> that removes user specified variant names
 * in context topics.
 *
 * @author akivela
 */
public class VariantRemover extends AbstractWandoraTool implements WandoraTool {
    
    /**
     * Creates a new instance of VariantRemover
     */
    public VariantRemover() {
    }
    public VariantRemover(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Variant name remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes user specified variant names.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            Topic typeTopic = wandora.showTopicFinder("Select type of variants to be removed...");
            if(typeTopic == null) return;
            
            Topic scopeTopic = wandora.showTopicFinder("Select scope of variants to be removed...");

            if(typeTopic != null && scopeTopic != null) {

                setDefaultLogger();
                setLogTitle("Removing variant names");
                log("Removing variant names of type '" + getTopicName(typeTopic) + "' and scope '" + getTopicName(scopeTopic) + "'.");

                Set<Topic> scope = null;
                Locator typeTopicSI = typeTopic.getOneSubjectIdentifier();
                Locator scopeTopicSI = scopeTopic.getOneSubjectIdentifier();
                
                int progress = 0;
                int removed = 0;
                Topic topic = null;
                
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            progress++;
                            
                            typeTopic = topic.getTopicMap().getTopic(typeTopicSI);
                            scopeTopic = topic.getTopicMap().getTopic(scopeTopicSI);
                            
                            if(typeTopic != null && scopeTopic != null) {
                                scope = new HashSet<Topic>();
                                scope.add(typeTopic);
                                scope.add(scopeTopic);

                                if(topic.getVariant(scope) != null) {
                                    topic.removeVariant(scope);
                                    removed++;
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Removed total " + removed + " variant names."); 
                setState(WAIT);
            }
        }
        catch (Exception e) {
            log(e);
        }
    }
    

    
}
