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
 * Association.java
 *
 * Created on June 10, 2004, 11:12 AM
 */

package org.wandora.topicmap;
import java.util.*;
/**
 *
 * @author  olli
 */
public interface Association {
    /**
     * Gets the type of this association.
     */
    public Topic getType() throws TopicMapException ;
    /**
     * Sets the type of this association replacing the previous type.
     */
    public void setType(Topic t) throws TopicMapException ;
    /**
     * Gets the player with the specified role.
     */
    public Topic getPlayer(Topic role) throws TopicMapException ;
    /**
     * Sets the player with the specified role replacing previous player with
     * that role if it exists already.
     */
    public void addPlayer(Topic player,Topic role) throws TopicMapException ;
    /**
     * Adds players to the association. Players are given in a map which
     * maps roles to players.
     */
    public void addPlayers(Map<Topic,Topic> players) throws TopicMapException ;
    /**
     * Removes the player with the specified role.
     */
    public void removePlayer(Topic role) throws TopicMapException ;
    /**
     * Gets a Collection of Topics containing the roles of this association.
     */
    public Collection<Topic> getRoles() throws TopicMapException ;
    /**
     * Gets the topic map this association belongs to.
     */
    public TopicMap getTopicMap();
    /**
     * Removes this association.
     */
    public void remove() throws TopicMapException ;
    /**
     * Tests if this association has been removed and thus this object is now
     * invalid.
     */
    public boolean isRemoved() throws TopicMapException ;
}
