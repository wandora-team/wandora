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
 * SelectTopicIfInLayer.java
 *
 * Created on 7.1.2012, 12:05
 *
 */

package org.wandora.application.tools.selections;

/**
 *
 * @author akivela
 */

import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.LayeredTopic;


public class SelectTopicIfInLayer extends DoTopicSelection {
    
    
    
    @Override
    public boolean acceptTopic(Topic topic)  {
        try{
            if(topic != null && !topic.isRemoved()) {
                if(topic instanceof LayeredTopic) {
                    Topic ltopic = ((LayeredTopic) topic).getTopicForSelectedLayer();
                    if(ltopic == null || ltopic.isRemoved()) {
                        return false;
                    }
                }
                return true;
            }
        } 
        catch(TopicMapException tme) {
            log(tme);
        }
        return false;
    }
    
    
    
    
    @Override
    public String getName() {
        return "Select if topic locates in selected layer.";
    }
}
