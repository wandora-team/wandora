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
 * TopicSelector.java
 *
 * Created on 17. helmikuuta 2006, 13:22
 */

package org.wandora.application.gui;
import org.wandora.topicmap.*;
/**
 *
 * This is the interface for topic selectors. Topic selectors are used to select
 * on or more topics from the topic map. Topics in Wandora may be selected in
 * different ways. For example through a tree model or directly entering the
 * subject identifier of a topic.
 *
 * @author olli
 */
public interface TopicSelector {
    /**
     * Returns the selected topic. If no topic is selected returns null. If
     * multiple topics are selected, returns one of the selected topics. 
     */
    public Topic getSelectedTopic();
    /**
     * Returns an array of selected topics. If no topics are selected returns an
     * array of 0 length;
     */
    public Topic[] getSelectedTopics();
    
    /**
     * Returns the panel used to make selection. Usually your class should extend
     * javax.swing.JPanel (or equivalent) and then just return this.
     */
    public java.awt.Component getPanel();
    
    /**
     * Gets the name of the selector.
     */
    public String getSelectorName();
    
    public void init();
    public void cleanup();
}
