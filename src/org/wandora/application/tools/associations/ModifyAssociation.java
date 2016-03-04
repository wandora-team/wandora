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
 * 
 * ModifyAssociation.java
 *
 * Created on 24. lokakuuta 2005, 19:52
 *
 */

package org.wandora.application.tools.associations;



import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.io.*;
import java.util.*;
import org.wandora.application.tools.AbstractWandoraTool;


/**
 *
 * @author akivela
 * @deprecated 
 */
public class ModifyAssociation extends AbstractWandoraTool implements WandoraTool {
    
    
    public ModifyAssociation() {
        setContext(new AssociationContext());
    }
    public ModifyAssociation(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Modify Association";
    }

    @Override
    public String getDescription() {
        return "Deprecated. Opens association editor. "+
                "Editor is used to add and modify associations.";
    }


    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Iterator contextAssociations = context.getContextObjects();
        if(contextAssociations == null || !contextAssociations.hasNext()) return;
        
        Association association = (Association) contextAssociations.next();
        if( !contextAssociations.hasNext() ) {
            if(association != null) {
                Topic topic = admin.getOpenTopic();
                SchemaAssociationPrompt prompt = new SchemaAssociationPrompt(admin,topic,true,association);
                prompt.setVisible(true);
            }
        }
        else {
            log("Context contains more than one association! Unable to decide association to modify.");
        }
    }
  
    

}
