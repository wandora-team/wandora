/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * SelectTopicIfSL.java
 *
 * Created on 12. huhtikuuta 2006, 10:53
 *
 */

package org.wandora.application.tools.selections;



import org.wandora.topicmap.*;



/**
 * This tool is an example of topic table selection tools where selection
 * is carried out using topics in table cells ie. the cell is selected if
 * the topic in the cell is accepted by <code>acceptCell</code> method. 
 * 
 * Tool selects topic table cell if topic contains subject locator. Otherwise
 * cell is unselected.
 *
 * @author akivela
 */


public class SelectTopicIfSL extends DoTopicSelection {
    

    
    @Override
    public boolean acceptTopic(Topic topic)  {
        try {
            if(topic == null || topic.isRemoved()) return false;
            Locator SL = topic.getSubjectLocator();
            if(SL != null && SL.toExternalForm().length() > 0) return true;
            return false;
        } catch(TopicMapException tme) {
            log(tme);
            return false;
        }
    }
    
    
    @Override
    public String getName() {
        return "Select topics with SL";
    }
}
