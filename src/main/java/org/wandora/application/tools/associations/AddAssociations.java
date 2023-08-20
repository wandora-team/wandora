/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * AddAssociations.java
 *
 * Created on 24. lokakuuta 2005, 19:52
 *
 */

package org.wandora.application.tools.associations;



import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.SchemaAssociationPrompt;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


/**
 * @deprecated 
 * @see AddSchemalessAssociation
 * @author akivela
 */
public class AddAssociations extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	

	public AddAssociations() {
    }
    public AddAssociations(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Add Associations";
    }

    @Override
    public String getDescription() {
        return "Deprecated. Opens Wandora's schema association editor. "+
                "Editor is used to add associations to the selected topic. "+
                "Only schema defined associations can be added with this tool.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        Iterator contextTopics = context.getContextObjects();
        if(contextTopics == null || !contextTopics.hasNext()) return;

        Topic topic = (Topic) contextTopics.next();
        if( !contextTopics.hasNext() ) {
            if(topic != null && !topic.isRemoved()) {
                SchemaAssociationPrompt prompt = new SchemaAssociationPrompt(wandora,topic,true);
                prompt.setVisible(true);
                if(!prompt.wasCancelled()) {
                    // ACTUAL ASSOCIATION CREATION IS IN THE ASSOCIATION PROMPT CLASS
                }
            }
        }
        else {
            log("Context contains more than one topic! Unable to decide topic to add associations to.");
        }
    }
  
}
