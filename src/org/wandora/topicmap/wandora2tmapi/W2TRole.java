/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2013 Wandora Team
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
package org.wandora.topicmap.wandora2tmapi;

import java.util.HashSet;
import java.util.Set;
import org.tmapi.core.Association;
import org.tmapi.core.Locator;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Role;
import org.tmapi.core.TMAPIRuntimeException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class W2TRole implements Role {

    protected W2TAssociation association;
    protected W2TTopic type;
    protected W2TTopic player;
    
    public W2TRole(W2TAssociation association,W2TTopic type,W2TTopic player){
        this.association=association;
        this.type=type;
        this.player=player;
    }
    
    @Override
    public Association getParent() {
        return association;
    }

    @Override
    public Topic getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(Topic topic) {
        org.wandora.topicmap.Association _a=association.getWrapped();
        org.wandora.topicmap.Topic _type=type.getWrapped();
        try{
            // this replaces the old player
            _a.addPlayer(_type, ((W2TTopic)topic).getWrapped());
            player=(W2TTopic)topic;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Topic getReifier() {
        return null;
    }

    @Override
    public void setReifier(Topic topic) throws ModelConstraintException {
        throw new UnsupportedOperationException("Reification not supported");
    }

    @Override
    public TopicMap getTopicMap() {
        return type.tm;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Set<Locator> getItemIdentifiers() {
        return new HashSet<Locator>();
    }

    @Override
    public void addItemIdentifier(Locator lctr) throws ModelConstraintException {
        throw new UnsupportedOperationException("Item identifiers not supported");
    }

    @Override
    public void removeItemIdentifier(Locator lctr) {
    }

    @Override
    public void remove() {
        org.wandora.topicmap.Association _a=association.getWrapped();
        org.wandora.topicmap.Topic _type=type.getWrapped();
        try{
            _a.removePlayer(_type);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Topic getType() {
        return type;
    }

    @Override
    public void setType(Topic topic) {
        org.wandora.topicmap.Association _a=association.getWrapped();
        try{
            _a.setType(((W2TTopic)topic).getWrapped());
            type=(W2TTopic)topic;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
        
    }
    
}
