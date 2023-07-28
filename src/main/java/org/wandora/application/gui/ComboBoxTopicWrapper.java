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
 * ComboBoxTopicWrapper.java
 *
 * Created on June 15, 2004, 4:53 PM
 */

package org.wandora.application.gui;


import org.wandora.topicmap.*;
/**
 *
 * @author  olli
 */
public class ComboBoxTopicWrapper {
    public Topic topic;
    public ComboBoxTopicWrapper(Topic topic){
        this.topic=topic;
    }
    @Override
    public String toString(){
        try{
            String name=topic.getBaseName();
            if(name == null) {
                Locator l = topic.getOneSubjectIdentifier();
                if(l != null) name = l.toExternalForm();
            }
            if(name == null) {
                if(topic.isRemoved()) return "[removed]";
                else return "[unnamed]";
            }
            else {
                if(name.length()>100) name=name.subSequence(0,100)+"...";
                return name;
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION;
            return "[exception retrieving topic name]";
        }
    }
}
