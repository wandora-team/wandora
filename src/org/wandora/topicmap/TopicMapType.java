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
 * 
 *
 * TopicMapType.java
 *
 * Created on 21. marraskuuta 2005, 13:44
 */

package org.wandora.topicmap;


import org.wandora.topicmap.packageio.PackageOutput;
import org.wandora.topicmap.packageio.PackageInput;
import org.wandora.utils.Options;
import org.wandora.application.*;
import java.io.*;
import javax.swing.*;
/**
 * TopicMapType is a class that makes it possible to use a topic map implementation
 * in Wandora. The class extending TopicMap implements everything directly
 * affecting the contents of the
 * topic map. Another class implementing this class, TopicMapType, must implement some
 * methods that are needed by Wandora to make it possible to load and save
 * topic maps, create new topic maps and configure new and existing topic maps.
 * A new topic map implementation may be very useful in some cases without
 * implementing TopicMapType but it cannot be used with Wandora.
 *
 * @author olli
 */
public interface TopicMapType {
    
    /**
     * Gets a name for topic map type.
     */
    public String getTypeName();
    /**
     * Create a new topic map with parameters given by TopicMapConfigurationPanel.getParameters.
     */
    public TopicMap createTopicMap(Object params)  throws TopicMapException ;
    /**
     * Modifies an existing topic map with parameters given by TopicMapConfigurationPanel.getParameters.
     * The method may create a completely new topic map if needed.
     */
    public TopicMap modifyTopicMap(TopicMap tm, Object params)  throws TopicMapException ;
    /**
     * Get a configuration panel for the topic map. 
     */
    public TopicMapConfigurationPanel getConfigurationPanel(Wandora admin, Options options);
    /**
     * Get a configuration panel that can be used to modify an existing topic map.
     */
    public TopicMapConfigurationPanel getModifyConfigurationPanel(Wandora admin, Options options, TopicMap tm);

    /**
     * Packages a topic map so it can be loaded later by unpackageTopicMap. 
     * Different implementations may choose to store the topic map in the package
     * or only store configuration parameters and have the topicmap in some other
     * place such as an external database.
     */
    public void packageTopicMap(TopicMap tm,PackageOutput out,String path, TopicMapLogger logger) throws IOException,TopicMapException;
    /**
     * Unpackages and creates a topic map.
     */
    public TopicMap unpackageTopicMap(PackageInput in,String path, TopicMapLogger logger,Wandora wandora) throws IOException,TopicMapException;
    public TopicMap unpackageTopicMap(TopicMap tm, PackageInput in, String path, TopicMapLogger logger,Wandora wandora) throws IOException,TopicMapException;
    
    /**
     * Get a topic map implementation specific menu structure for this topic map type.
     */   
    public JMenuItem[] getTopicMapMenu(TopicMap tm,Wandora admin);
    /**
     * Get an icon that can be used to represent this type of topic map.
     */
    public Icon getTypeIcon();
}
