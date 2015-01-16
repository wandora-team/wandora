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
 * CopyTopicRoles.java
 *
 * Created on 13. huhtikuuta 2006, 11:57
 *
 */

package org.wandora.application.tools;

import org.wandora.topicmap.TopicMapException;
import org.wandora.application.contexts.*;


/**
 * Copies association role topics of associations in selected topics to clipboard.
 *
 * @author akivela
 */




public class CopyTopicRoles extends CopyTopics {
    

    public CopyTopicRoles() throws TopicMapException {
    }
    
    @Override
    public String getName() {
        return "Copy topic roles";
    }

    @Override
    public String getDescription() {
        return "Copies association role topics of associations in selected topics to clipboard.";
    }
    
    @Override
    public void initialize(int copyOrders, int includeOrders) {
        super.initialize(copyOrders, includeOrders);
        setContext(new RoleContext());
    }
    
}
