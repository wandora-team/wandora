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
 * 
 *
 * TopicMapCorfigurationPanel.java
 *
 * Created on 21. marraskuuta 2005, 13:44
 */

package org.wandora.topicmap;

/**
 *
 * @author olli
 */
public abstract class TopicMapConfigurationPanel extends javax.swing.JPanel {

    /**
     * Get the parameters user entered. The returned object may be of any type,
     * usually determined by the TopicMapType used to create the configuration
     * panel. The TopicMapType is used to construct a new topic map with the
     * parameter object.
     */
    public abstract Object getParameters();
    
}
