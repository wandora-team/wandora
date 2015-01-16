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
 * 
 * AddFreeAssociation.java
 *
 * Created on 14. kesäkuuta 2006, 10:04
 */

package org.wandora.application.tools.associations;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.util.*;
import org.wandora.application.tools.AbstractWandoraTool;


/**
 *
 * @author olli
 */
public class AddSchemalessAssociation extends AbstractWandoraTool {
    

    /** Creates a new instance of AddFreeAssociation */
    public AddSchemalessAssociation() {
        setContext(new LayeredTopicContext());
    }

    public AddSchemalessAssociation(Context preferredContext) {
        setContext(preferredContext);
    }

    @Override
    public String getName() {
        return "Add associations";
    }

    @Override
    public String getDescription() {
        return "Open association editor. "+
                "Association editor is used to add and edit associations.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        Iterator contextTopics = context.getContextObjects();
        if(contextTopics == null || !contextTopics.hasNext()) return;

        Topic topic = (Topic) contextTopics.next();
        if( !contextTopics.hasNext() ) {
            if(topic != null && !topic.isRemoved()) {
                FreeAssociationPrompt d=new FreeAssociationPrompt(wandora,topic);
                d.setVisible(true);
            }
        }
        else {
            log("Context contains more than one topic! Unable to decide topic to add associations to.");
        }
    }
  
    
}
