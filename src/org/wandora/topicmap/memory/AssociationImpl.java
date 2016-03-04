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
 * AssociationImpl.java
 *
 * Created on June 10, 2004, 11:30 AM
 */


package org.wandora.topicmap.memory;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * TODO: maybe we should check for duplicate associations when modifying association and throw an exception if found
 *
 * @author  olli
 */
public class AssociationImpl implements Association {
    private TopicMapImpl topicMap;
    private Topic type;
    private Map<Topic,Topic> players;
    private boolean removed;
    
    
    /** Creates a new instance of AssociationImpl */
    public AssociationImpl(TopicMapImpl topicMap, Topic type)  throws TopicMapException {
        this.topicMap=topicMap;
        players=Collections.synchronizedMap(new LinkedHashMap());
        setType(type);
        removed=false;
    }
    
    
    @Override
    public Topic getPlayer(Topic role) {
        return players.get(role);
    }
    
    
    @Override
    public Collection getRoles() {
        return players.keySet();
    }
    
    
    @Override
    public TopicMap getTopicMap() {
        return topicMap;
    }
    
    
    @Override
    public Topic getType() {
        return type;
    }
    
    
    @Override
    public void setType(Topic t) throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        topicMap.setAssociationType(this,t,type);
        Topic oldType=type;
        if(type!=null) ((TopicImpl)type).removedFromAssociationType(this);
        boolean changed=( (type!=null || t!=null) && ( type==null || t==null || !type.equals(t) ) );
        type=t;
        if(t!=null)
            ((TopicImpl)t).addedAsAssociationType(this);
        Iterator iter=players.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry e=(Map.Entry)iter.next();
            ((TopicImpl)e.getValue()).associationTypeChanged(this,t,oldType,(Topic)e.getKey());
        }
        if(changed) {
            topicMap.associationTypeChanged(this,t,oldType);
            if(topicMap.getConsistencyCheck()) checkRedundancy();
        }
    }
    
    
    @Override
    public void addPlayer(Topic player, Topic role)  throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(role == null || player == null) return;
        
//        if(players.containsKey(role)) return; // TODO: exception, note also that DatabaseAssociation replaces old
//        ((TopicImpl)player).addInAssociation(this,role);
//        ((TopicImpl)role).addedAsRoleType(this);
        TopicImpl oldPlayer=null;
        if(players.containsKey(role)) {
            oldPlayer=(TopicImpl)players.get(role);
            if(oldPlayer.equals(player)) return; // don't need to do anything
            
            players.remove(role);
            oldPlayer.removeFromAssociation(this, role, players.values().contains(oldPlayer));
            ((TopicImpl)player).addInAssociation(this,role);
        }
        else {
            ((TopicImpl)player).addInAssociation(this,role);
            ((TopicImpl)role).addedAsRoleType(this);
        }

        players.put(role,player);
        if(oldPlayer==null || !oldPlayer.equals(player)) {
            topicMap.associationPlayerChanged(this,role,player,oldPlayer);
            if(topicMap.getConsistencyCheck()) {
                checkRedundancy();
            }
        }
    }
    
    
    @Override
    public void addPlayers(Map<Topic,Topic> newPlayers) throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        boolean changed=false;
        for(Map.Entry<Topic,Topic> e : newPlayers.entrySet()) {
            TopicImpl role=(TopicImpl)e.getKey();
            TopicImpl player=(TopicImpl)e.getValue();
            
//            if(players.containsKey(role)) continue; // TODO: same as above
//            player.addInAssociation(this,role);
//            role.addedAsRoleType(this);
            TopicImpl oldPlayer=null;
            if(players.containsKey(role)) {
                oldPlayer=(TopicImpl)players.get(role);
                if(oldPlayer.equals(player)) continue;
                
                players.remove(role);
                oldPlayer.removeFromAssociation(this, role, players.values().contains(oldPlayer));
                player.addInAssociation(this,role);
            }
            else {
                player.addInAssociation(this,role);
                role.addedAsRoleType(this);                
            }
            
            players.put(role,player);
            if(oldPlayer==null || !oldPlayer.equals(player)) {
                changed=true;
                topicMap.associationPlayerChanged(this,role,player,oldPlayer);
            }
        }
        if(topicMap.getConsistencyCheck()) checkRedundancy();
    }
    
    
    @Override
    public void removePlayer(Topic role)  throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        TopicImpl t=(TopicImpl)players.get(role);
        if(t!=null) {
            players.remove(role);        
            t.removeFromAssociation(this,role,players.values().contains(t));
            ((TopicImpl)role).removedFromRoleType(this);
            topicMap.associationPlayerChanged(this,role,null,t);
            if(!removed) {
                if(topicMap.getConsistencyCheck()) checkRedundancy();
            }
        }
    }
    
    
    @Override
    public void remove()  throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();

        removed=true;
        topicMap.associationRemoved(this);
        
        ArrayList<Topic> roles = new ArrayList(players.keySet());
        for(Topic role : roles) {
            TopicImpl t=(TopicImpl)players.get(role);
            if(t!=null) {
                players.remove(role);        
                t.removeFromAssociation(this,role,players.values().contains(t));
                ((TopicImpl)role).removedFromRoleType(this);
                topicMap.associationPlayerChanged(this,role,null,t);
            }
        }
        
        // set type null
        topicMap.setAssociationType(this, null, type);
        Topic oldType=type;
        if(type != null) ((TopicImpl)type).removedFromAssociationType(this);
        type = null;
        if(oldType != null) {
            topicMap.associationTypeChanged(this, null, oldType);
        }
    }
    
    
    @Override
    public boolean isRemoved(){
        return removed;
    }
    
    
    void checkRedundancy() throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(players.isEmpty()) return;
        if(type==null) return;
        Collection<AssociationImpl> smallest=null;
        for(Topic role : players.keySet()) {
            Topic player = players.get(role);
            Collection c = player.getAssociations(type,role);
            if(smallest==null || c.size()<smallest.size()) {
                smallest=c;
            }
        }
        Set<Association> delete = new HashSet();
        for(AssociationImpl a : smallest) {
            if(a==this) continue;
            if(a._equals(this)) {
                delete.add(a);
            }
        }
        for(Association a : delete) { 
            topicMap.duplicateAssociationRemoved(this,a);
            a.remove();
        }
    }
    
    
    int _hashCode() {
        return players.hashCode()+type.hashCode();
    }
    
    
    boolean _equals(AssociationImpl a) {
        if(a == null) return false;
        if((players == null && a.players != null) || (players != null && a.players == null)) return false;
        if(players != null && a.players != null && players.size() != a.players.size()) return false;
        if(type != a.type) return false;
        
        if(players != null && a.players != null) {
            for(Topic r : players.keySet()) {
                if(players.get(r) != a.players.get(r)) {
                    return false;
                }
            }
        }
        return true;
    }
}
