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
 */


package org.wandora.application.gui.topicstringify;



import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 * TopicStringifier interface specifies all methods required in a Java
 * class that creates string representations out of topics. String representations
 * are used in Wandora anytime a topic is viewed.
 *
 * @author akivela
 */


public interface TopicStringifier {
    
    /**
     * Initialization method is called before a TopicStringifier is actually used.
     * If initialization returns false, the TopicStringifier should not be used.
     */
    public boolean initialize(Wandora wandora, Context context);
    
    /**
     * toString method is the actual endpoint used to create a string out of a
     * topic.
     */
    public String toString(Topic t);
    
    
    /**
     * Description is a text that describes the TopicStringifier. Text is
     * shown in Wandora's UI whenever user wants more information about the
     * TopicStringifier, for example, as a tooltip text.
     */
    public String getDescription();
    
    
    /**
     * Method returns an icon shown in Wandora UI as a mark of the
     * TopicStringifier. Current Wandora views the icon in the infobar near
     * topic's name.
     */
    public Icon getIcon();
    
    
    
    
    /**
     * Sometimes topic viewer may support string editing feature.
     * This method is used to push string back into a topic. String
     * is stored into the topic.
     */
    public void stringIntoTopic(String oldString, String newString, Topic topic) throws TopicMapException;
    
    
    
    /**
     * If topic viewer supports string editing feature and Wandora can push the
     * string back into the topic, this method should return true. If TopicStringifier
     * can't push a string back into the topic, method should return false. The
     * push is implemented in method stringIntoTopic.
     */
    public boolean supportsStringIntoTopic();
}
