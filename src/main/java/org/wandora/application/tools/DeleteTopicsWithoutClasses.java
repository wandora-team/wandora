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
 * DeleteTopicsWithoutClasses.java
 *
 * Created on 12.6.2006, 17:47
 *
 */

package org.wandora.application.tools;



import java.util.Collection;

import org.wandora.application.WandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;



/**
 *
 * @author akivela
 */
public class DeleteTopicsWithoutClasses extends DeleteTopics implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	@Override
    public String getName() {
        return "Delete topics without classes";
    }

    @Override
    public String getDescription() {
        return "Delete context topics without classes (i.e. types).";
    }
    
    @Override
    public boolean shouldDelete(Topic topic)  throws TopicMapException {
        try {
            if(topic != null && !topic.isRemoved()) {
                Collection<Topic> types = topic.getTypes();
                if(types == null || types.isEmpty()) {
                    if(confirm) {
                        return confirmDelete(topic);
                    }
                    else {
                        return true;
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return false;
    }
    
}
