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
 * AddSubjectIdentifier.java
 *
 * Created on 24. lokakuuta 2005, 20:08
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.TMBox;
import java.util.*;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.utils.DataURL;



/**
 * WandoraTool that adds a subject identifier to selected topics. If selection
 * contains two or more topics, all selected topics are merged automatically after
 * subject identifier addition.
 *
 * @author akivela
 */


public class AddSubjectIdentifier extends AbstractWandoraTool implements WandoraTool {
    
	private static final long serialVersionUID = 1L;
	
	private boolean shouldRefresh = false;
    
    public AddSubjectIdentifier() {}
    public AddSubjectIdentifier(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Add subject identifier";
    }

    @Override
    public String getDescription() {
        return "Adds subject identifier to selected topics. "+
               "If two topics have same subject identifier, they merge.";
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        shouldRefresh = false;
        Iterator contextTopics = getContext().getContextObjects();
        if(contextTopics != null && contextTopics.hasNext()) {
            Topic topic = (Topic) contextTopics.next();
            
            if( !contextTopics.hasNext() ) {
                if(topic != null && !topic.isRemoved()) {
                    AddSubjectIdentifierPanel sip = new AddSubjectIdentifierPanel();
                    String newSubjectIdentifier = sip.open(wandora, "Enter new subject identifier for '" + TopicToString.toString(topic) + "'.", "Add subject identifier");
                    if(newSubjectIdentifier != null) {
                        newSubjectIdentifier = newSubjectIdentifier.trim();
                        try {
                            boolean isValid = DataURL.isDataURL(newSubjectIdentifier);
                            if(!isValid) new java.net.URL(newSubjectIdentifier);
                            Locator l = topic.getTopicMap().createLocator(newSubjectIdentifier);
                            if(TMBox.checkSubjectIdentifierChange(wandora,topic,l,true) == ConfirmResult.yes) {
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
                log("Context contains more than one topic! Uncertain which topic to add subject identifier to.");
            }
        }
    }
    

    @Override
    public boolean requiresRefresh() {
        return shouldRefresh;
    }
}
