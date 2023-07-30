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
 * SelectTopicIfNoOccurrences.java
 *
 * Created on 14.7.2006, 12:13
 *
 */

package org.wandora.application.tools.selections;



import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author akivela
 */
public class SelectTopicIfNoOccurrences extends DoTopicSelection {
    

	private static final long serialVersionUID = 1L;

	@Override
    public boolean acceptTopic(Topic topic)  {
        try {
            if(topic != null && !topic.isRemoved() && topic.getDataTypes().isEmpty()) return true;
            return false;
        } 
        catch(TopicMapException tme) {
            log(tme);
            return false;
        }
    }
    
    @Override
    public String getName() {
        return "Select if topic has no occurrences.";
    }
    
}
