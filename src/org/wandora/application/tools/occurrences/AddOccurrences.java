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
 * AddOccurrences.java
 *
 * Created on 24. lokakuuta 2005, 19:57
 *
 */

package org.wandora.application.tools.occurrences;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;
import org.wandora.application.gui.SchemaOccurrencePrompt;
import org.wandora.application.tools.AbstractWandoraTool;


/**
 *
 * @deprecated 
 * @see AddSchemalessOccurrence
 * @author akivela
 */
public class AddOccurrences extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;


	public AddOccurrences() {}
    public AddOccurrences(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "Add occurrences";
    }

    @Override
    public String getDescription() {
        return "Deprecated. Tool opens schema occurrence editor used to add internal occurrences to selected topic. "+
               "Only schema defined occurrences can be added with this tool.";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        Iterator contextTopics = context.getContextObjects();
        if(contextTopics != null && contextTopics.hasNext()) {
            Topic topic = (Topic) contextTopics.next();
            if( !contextTopics.hasNext() && !forceStop() ) {
                try {
                    if(topic != null && !topic.isRemoved()) {
                        SchemaOccurrencePrompt prompt=new SchemaOccurrencePrompt(wandora, true, topic);
                        prompt.setVisible(true);
                        if(!prompt.wasCancelled()) { }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            else {
                log("Context contains more than one topic! Unable to decide which topic to add associations to.");
            }
        }
    }

}
