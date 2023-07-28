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
 * 
 * ModifySchemalessAssociation.java
 *
 * Created on 18. helmikuuta 2008, 12:18
 *
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
public class ModifySchemalessAssociation extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	/** Creates a new instance of ModifySchemalessAssociation */
    public ModifySchemalessAssociation() {
        setContext(new AssociationContext());
    }
    public ModifySchemalessAssociation(Context preferredContext) {
        setContext(preferredContext);
    }

    @Override
    public String getName() {
        return "Modify Schemaless Association";
    }

    @Override
    public String getDescription() {
        return "Opens association editor. "+
                "Editor is used to add and modify associations in Wandora.";
    }


    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Iterator contextAssociations = context.getContextObjects();
        if(contextAssociations == null || !contextAssociations.hasNext()) return;
        
        Association association = (Association) contextAssociations.next();
        if( !contextAssociations.hasNext() ) {
            if(association != null) {
                //Topic topic = wandora.getOpenTopic();
                //FreeAssociationPrompt prompt = new FreeAssociationPrompt(wandora,topic,association);
                FreeAssociationPrompt prompt = new FreeAssociationPrompt(wandora, association);
                prompt.setVisible(true);
            }
        }
        else {
            log("Context contains more than one association! Unable to decide association to modify.");
        }
    }
    
    
}
