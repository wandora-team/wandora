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
 */
package org.wandora.topicmap.query;
import java.util.*;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */
public class QueryAssociation implements Association {

    protected QueryTopicMap tm;
    protected Topic type;
    protected Hashtable<Topic,Topic> players;
    
    public QueryAssociation(Topic type,Hashtable<Topic,Topic> players,QueryTopicMap tm){
        this.type=type;
        this.players=players;
        this.tm=tm;
    }
    
    public Topic getPlayer(Topic role) throws TopicMapException {
        return players.get(role);
    }

    public Collection<Topic> getRoles() throws TopicMapException {
        return players.keySet();
    }

    public TopicMap getTopicMap() {
        return tm;
    }

    public Topic getType() throws TopicMapException {
        return type;
    }
    
    public void addPlayer(Topic player, Topic role) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    public void addPlayers(Map<Topic, Topic> players) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    public boolean isRemoved() throws TopicMapException {
        return false;
    }

    public void remove() throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    public void removePlayer(Topic role) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    public void setType(Topic t) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

}
