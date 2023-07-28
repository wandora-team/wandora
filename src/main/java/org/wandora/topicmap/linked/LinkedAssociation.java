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

package org.wandora.topicmap.linked;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class LinkedAssociation implements Association {

    protected Association wrappedAssociation;
    protected LinkedTopicMap topicMap;
    
    public LinkedAssociation(Association wrappedAssociation,LinkedTopicMap topicMap){
        this.wrappedAssociation=wrappedAssociation;
        this.topicMap=topicMap;
    }
    
    public Association getWrappedAssociation() {
        return wrappedAssociation;
    }
    
    @Override
    public void addPlayer(Topic player, Topic role) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        wrappedAssociation.addPlayer(topicMap.getUnlinkedTopic(player),topicMap.getUnlinkedTopic(role));
    }

    @Override
    public void addPlayers(Map<Topic, Topic> players) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        Map<Topic,Topic> unwrapped=new HashMap<Topic,Topic>();
        for(Map.Entry<Topic,Topic> e : players.entrySet()){
            unwrapped.put(topicMap.getUnlinkedTopic(e.getKey()),topicMap.getUnlinkedTopic(e.getValue()));
        }
        wrappedAssociation.addPlayers(unwrapped);
    }

    @Override
    public Topic getPlayer(Topic role) throws TopicMapException {
        return topicMap.getLinkedTopic(wrappedAssociation.getPlayer(topicMap.getUnlinkedTopic(role)));
    }

    @Override
    public Collection<Topic> getRoles() throws TopicMapException {
        return topicMap.getLinkedTopics(wrappedAssociation.getRoles());
    }

    @Override
    public TopicMap getTopicMap() {
        return topicMap;
    }

    @Override
    public Topic getType() throws TopicMapException {
        return topicMap.getLinkedTopic(wrappedAssociation.getType());
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return wrappedAssociation.isRemoved();
    }

    @Override
    public void remove() throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        wrappedAssociation.remove();
    }

    @Override
    public void removePlayer(Topic role) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        wrappedAssociation.removePlayer(topicMap.getUnlinkedTopic(role));
    }

    @Override
    public void setType(Topic t) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        wrappedAssociation.setType(topicMap.getUnlinkedTopic(t));
    }

    @Override
    public int hashCode(){
        return wrappedAssociation.hashCode()+topicMap.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(!o.getClass().equals(this.getClass())) return false;
        LinkedAssociation lt=(LinkedAssociation)o;
        if(lt.topicMap!=topicMap) return false;
        if(!lt.wrappedAssociation.equals(wrappedAssociation)) return false;
        return true;
    }
    
}
