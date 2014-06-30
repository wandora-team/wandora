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


public class W2TAssociation implements Association {

    protected W2TTopicMap tm;
    protected org.wandora.topicmap.Association a;
    
    public W2TAssociation(W2TTopicMap tm,org.wandora.topicmap.Association a){
        this.tm=tm;
        this.a=a;
    }
    
    public org.wandora.topicmap.Association getWrapped(){
        return a;
    }
    
    @Override
    public TopicMap getParent() {
        return tm;
    }

    @Override
    public Set<Role> getRoles() {
        try{
            HashSet<Role> ret=new HashSet<Role>();
            for(org.wandora.topicmap.Topic role : a.getRoles() ){
                org.wandora.topicmap.Topic player=a.getPlayer(role);
                ret.add(new W2TRole(this, new W2TTopic(tm,role), new W2TTopic(tm,player)));
            }
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Set<Topic> getRoleTypes() {
        try{
            return tm.wrapTopics(a.getRoles());
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Set<Role> getRoles(Topic type) {
        try{
            org.wandora.topicmap.Topic player=a.getPlayer(((W2TTopic)type).t);
            HashSet<Role> ret=new HashSet<Role>();
            if(player!=null) ret.add(new W2TRole(this, (W2TTopic)type, new W2TTopic(tm,player)));
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Role createRole(Topic type, Topic player) throws ModelConstraintException {
        try{
            if(a.getPlayer(((W2TTopic)type).getWrapped())!=null)
                throw new UnsupportedOperationException("Multiple players with same role in an association are not supported");
            
            a.addPlayer(((W2TTopic)player).getWrapped(), ((W2TTopic)type).getWrapped());
            return new W2TRole(this,(W2TTopic)type,(W2TTopic)player);
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
        return tm;
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
        // There will never be any item identifiers so nothing needs to be done
    }

    @Override
    public void remove() {
        try{
            a.remove();
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Topic getType() {
        try{
            return new W2TTopic(tm,a.getType());
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public void setType(Topic topic) {
        try{
            a.setType(((W2TTopic)topic).getWrapped());
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Set<Topic> getScope() {
        return new HashSet<Topic>();
    }

    @Override
    public void addTheme(Topic topic) throws ModelConstraintException {
        throw new UnsupportedOperationException("Scoped associations not supported");
    }

    @Override
    public void removeTheme(Topic topic) {
        // there is no themes in the scope so nothing needs to be done
    }

    
}
