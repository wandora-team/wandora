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
 */

package org.wandora.application.gui.topicstringify;




import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.topicmap.Topic;


/**
 * This is a static class to make string representations out of topics. String
 * representation is created with a user given stringifier 
 * (any class implementing the TopicStringifierInterface).
 * An UI element viewing topics as strings should use the static 
 * toString(topic) method of this class to transform topics to string. 
 *
 * @author akivela
 */


public class TopicToString {
    

    
    private static TopicStringifier topicStringifier = null;
    private static TopicStringifier defaultTopicStringifier = new DefaultTopicStringifier();
    
    
    
    
    public static void initialize(Wandora wandora) {
        try {
            // TODO: Read used stringifier class from options.
            // TODO: Save current stringifier name and parameters to options.
        }
        catch(Exception e) {
            // NOTHING
        }
    }
    

    public static TopicStringifier getStringifier() {
        return topicStringifier;
    }
    
    
    public static void setStringifier(TopicStringifier r) {
        topicStringifier = r;
    }
    
    
    public static Icon getIcon() {
        if(topicStringifier != null) {
            return topicStringifier.getIcon();
        }
        else {
            return defaultTopicStringifier.getIcon();
        }
    }
    
    
    
    /*
     * Method is used to create string representation out of a topic. Method
     * uses given TopicStringifier to create a string. If no TopicStringifier is
     * given, method passes the call to defaultTopicStringifier.
     */
    public static String toString(Topic t) {
        if(topicStringifier != null) {
            return topicStringifier.toString(t);
        }
        else {
            return defaultTopicStringifier.toString(t);
        }
    }

}
