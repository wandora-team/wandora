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
package org.wandora.topicmap.webservice;
import java.util.*;
import org.wandora.topicmap.*;

import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSAssociation;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSOccurrence;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSPlayer;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSTopic;

/**
 *
 * @author olli
 */
public class WebServiceAssociation implements Association {

    private WebServiceTopicMap tm;
    private WSAssociation wsassociation;
    private Topic type;
    private HashMap<Topic,Topic> players;

    public WebServiceAssociation(WebServiceTopicMap tm,WSAssociation wsassociation) throws TopicMapException {
        this.tm=tm;
        this.wsassociation=wsassociation;
        this.type=tm.getTopic(wsassociation.getType());
        this.players=new HashMap<Topic,Topic>();
        WSPlayer[] ps=wsassociation.getPlayers();
        for(int i=0;i<ps.length;i++){
            Topic rt=tm.getTopic(ps[i].getRole());
            Topic mt=tm.getTopic(ps[i].getMember());
            players.put(rt,mt);
        }
    }

    public void addPlayer(Topic player, Topic role) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    public void addPlayers(Map<Topic, Topic> players) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    public Topic getPlayer(Topic role) throws TopicMapException {
        for(Topic t : players.keySet()){
            if(t.mergesWithTopic(role)) return players.get(t);
        }
        return null;
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
