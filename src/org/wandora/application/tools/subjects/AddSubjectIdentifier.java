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
 * AddSubjectIdentifier.java
 *
 * Created on 24. lokakuuta 2005, 20:08
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.TMBox;
import java.util.*;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.tools.AbstractWandoraTool;



/**
 * WandoraTool that adds a subject identifier to selected topics. If selection
 * contains two or more topics, all selected topics are merged automatically after
 * subject identifier addition.
 *
 * @author akivela
 */


public class AddSubjectIdentifier extends AbstractWandoraTool implements WandoraTool {
    
    private boolean shouldRefresh = false;
    
    public AddSubjectIdentifier() {}
    public AddSubjectIdentifier(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Add SI";
    }

    @Override
    public String getDescription() {
        return "Adds subject identifier (SI) to selected topics. "+
               "If two topics have same SI, they merge.";
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        shouldRefresh = false;
        Iterator contextTopics = getContext().getContextObjects();
        if(contextTopics != null && contextTopics.hasNext()) {
            Topic topic = (Topic) contextTopics.next();
            
            if( !contextTopics.hasNext() ) {
                if(topic != null && !topic.isRemoved()) {
                    String topicName = ( topic.getBaseName() == null ? topic.getOneSubjectIdentifier().toExternalForm() :  topic.getBaseName());
                    AddSubjectIdentifierPanel sip = new AddSubjectIdentifierPanel();
                    String newSI = sip.open(wandora, "Enter new subject identifier for '" + topicName + "'.", "Add subject identifier");
                    if(newSI != null) {
                        newSI = newSI.trim();
                        try {
                            java.net.URL u=new java.net.URL(newSI);
                            Locator l=topic.getTopicMap().createLocator(newSI);
                            if(TMBox.checkSubjectIdentifierChange(wandora,topic,l,true)!=ConfirmResult.yes) {
                                return;
                            }
                            else {
                                shouldRefresh = true;
                                topic.addSubjectIdentifier(l);
                            }
                        }
                        catch(java.net.MalformedURLException mue) {
                            log("Malformed subject identifier given. Subject identifier should be a valid URL. Rejecting subject identifier!");
                        }
                    }
                }
            }
            else {
                log("Context contains more than one topic! Uncertain which topic to add SI to.");
            }
        }
    }
    

    @Override
    public boolean requiresRefresh() {
        return shouldRefresh;
    }
}
