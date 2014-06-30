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
 * TopicNameCopier.java
 *
 * Created on 2. helmikuuta 2007, 16:04
 *
 */

package org.wandora.application.tools.topicnames;

import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;
import org.wandora.utils.*;



/**
 *
 * @author akivela
 */
public class TopicNameCopier extends AbstractWandoraTool implements WandoraTool {
    
    /**
     * Creates a new instance of TopicNameCopier
     */
    public TopicNameCopier() {
    }
    public TopicNameCopier(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Variant name copier";
    }


    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies ALL variant names to clipboard.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying variant names of topic");
            log("Copying variant names of topic");
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String variant = null;
            
            Collection scopes = null;
            Iterator scopeIterator = null;
            Set<Topic> scope = null;
            int progress = 0;
            
            StringBuilder stringBuffer = new StringBuilder("");
            StringBuilder logString = null;

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        progress++;
                        stringBuffer.append(topic.getBaseName());
                        logString = new StringBuilder(""+topic.getBaseName());
                        scopes = topic.getVariantScopes();
                        int count = 0;
                        if(scopes != null) {
                            scopeIterator = scopes.iterator();
                            while(scopeIterator.hasNext()) {
                                try {
                                    scope = (Set<Topic>) scopeIterator.next();
                                    variant = topic.getVariant(scope);
                                    if(variant != null) {
                                        count++;
                                        stringBuffer.append("\n\t").append(variant);
                                        logString.append("\n\t").append(variant);
                                        Iterator<Topic> scopeIter = scope.iterator();
                                        Topic scopeTopic = null;
                                        while(scopeIter.hasNext()) {
                                            scopeTopic = scopeIter.next();
                                            try {
                                                stringBuffer.append("\n\t\t").append(scopeTopic.getOneSubjectIdentifier().toExternalForm());
                                                logString.append("\n\t\t").append(scopeTopic.getOneSubjectIdentifier().toExternalForm());
                                            }
                                            catch(Exception e) {
                                                log(e);
                                            }
                                        }
                                        stringBuffer.append("\n");
                                        logString.append("\n");
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
                        }
                        if(count == 0) {
                            stringBuffer.append("\n\t[NO VARIANTS]");
                            logString.append("\n\t[NO VARIANTS]");
                        }
                    }
                    stringBuffer.append("\n");
                    log(logString.toString());
                }
                catch(Exception e) {
                    log(e);
                }
            }
            ClipboardBox.setClipboard(stringBuffer.toString());
            log("OK");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
