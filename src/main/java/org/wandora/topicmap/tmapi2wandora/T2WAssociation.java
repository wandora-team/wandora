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
package org.wandora.topicmap.tmapi2wandora;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
/**
 *
 * @author olli
 */


public class T2WAssociation implements Association {

    protected T2WTopicMap tm;
    protected org.tmapi.core.Association a;
    
    public T2WAssociation(T2WTopicMap tm,org.tmapi.core.Association a){
        this.tm=tm;
        this.a=a;
    }
    
    @Override
    public Topic getType() throws TopicMapException {
        return new T2WTopic(tm,a.getType());
    }

    @Override
    public void setType(Topic t) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Topic getPlayer(Topic role) throws TopicMapException {
        org.tmapi.core.Topic _role=((T2WTopic)role).getWrapped();
        Set<org.tmapi.core.Role> rs=a.getRoles(_role);
        if(rs.isEmpty()) return null;
        if(rs.size()==1){
            org.tmapi.core.Topic _player=rs.iterator().next().getPlayer();
            return new T2WTopic(tm,_player);
        }
        else {
            // The association has several players with the same role type.
            // We could return just one of them but maybe it's better to ignore
            // them all.
            return null;
        }
    }

    @Override
    public void addPlayer(Topic player, Topic role) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void addPlayers(Map<Topic, Topic> players) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void removePlayer(Topic role) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Collection<Topic> getRoles() throws TopicMapException {
        // See the comment in getPlayer about duplicate role types.
        // We ignore completely those role types here which complicates this
        // method a little bit.
        
        HashSet<org.tmapi.core.Topic> _roles=new HashSet<org.tmapi.core.Topic>();
        HashSet<org.tmapi.core.Topic> _ret=new HashSet<org.tmapi.core.Topic>();
        for(org.tmapi.core.Role r : a.getRoles()) {
            if(!_roles.add(r.getType())){
                _ret.remove(r.getType()); // if the role was already there then ignore it completely
            }
            else _ret.add(r.getType());
        }
        
        return tm.wrapTopics(_ret);
    }

    @Override
    public TopicMap getTopicMap() {
        return tm;
    }

    @Override
    public void remove() throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return false;
    }
    
}
