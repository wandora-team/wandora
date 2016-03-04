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
 * SelectTopicWithAssociationType.java
 *
 * Created on 26. heinäkuuta 2006, 11:51
 *
 */

package org.wandora.application.tools.selections;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;

import java.util.*;

/**
 * Tool selects topics in topic table if topic plays a role in association with
 * given type. Tool lets user to select topics that have associations of certain
 * type.
 *
 * @author akivela
 */
public class SelectTopicWithAssociationType extends DoTopicSelection {
    
    Topic type = null;

    
    
    @Override
    public void execute(Wandora admin, Context context) {
        try {
            type = admin.showTopicFinder("Select association type...");
            if(type != null) {
                super.execute(admin, context);
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    @Override
    public boolean acceptTopic(Topic topic)  {
        try {
            if(type != null) {
                Collection<Association> a = topic.getAssociations(type);
                if(a != null && a.size() > 0) return true;
            }
        }
        catch(TopicMapException tme) {
            log(tme);
        }
        return false;
    }
    
    @Override
    public String getName() {
        return "Select topics with association type";
    }
    
    @Override
    public String getDescription() {
        return "Select if topic has association of given type.";
    }
    
}
