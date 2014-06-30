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
 */



package org.wandora.topicmap.undowrapper;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */


public class UndoAssociation implements Association {
    
    private Association wrapped;
    private UndoTopicMap topicMap;
    
    UndoAssociation(Association wrapped,UndoTopicMap topicMap){
        this.wrapped=wrapped;
        this.topicMap=topicMap;
    }
    
    private boolean undoCreated() throws TopicMapException {
        return !wrapped.getRoles().isEmpty();
    }
    
    public Association getWrapped(){
        return wrapped;
    }

    @Override
    public Topic getType() throws TopicMapException {
        return topicMap.wrapTopic(wrapped.getType());
    }

    @Override
    public void setType(Topic t) throws TopicMapException {
        Topic wtype=((UndoTopic)t).getWrapped();
        try {
            if(undoCreated()) topicMap.addUndoOperation(ModifyAssociationOperation.setType(wrapped, wtype));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.setType(wtype);
    }

    @Override
    public Topic getPlayer(Topic role) throws TopicMapException {
        return topicMap.wrapTopic(wrapped.getPlayer(((UndoTopic)role).getWrapped()));
    }

    @Override
    public void addPlayer(Topic player, Topic role) throws TopicMapException {
        Topic wrole=((UndoTopic)role).getWrapped();
        Topic wplayer=((UndoTopic)player).getWrapped();
        
        if(wrapped.getRoles().isEmpty()) {
            try {
                topicMap.addUndoOperation(new CreateAssociationOperation(wrapped.getType(),wrole,wplayer));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }                                    
        }
        else {
            try {
                topicMap.addUndoOperation(ModifyAssociationOperation.addPlayer(wrapped, wrole, wplayer));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        }
        wrapped.addPlayer(wplayer,wrole);
    }

    @Override
    public void addPlayers(Map<Topic, Topic> players) throws TopicMapException {
        HashMap<Topic,Topic> ps=new HashMap<Topic,Topic>();
        for(Map.Entry<Topic,Topic> e : players.entrySet()){
            ps.put(((UndoTopic)e.getKey()).getWrapped(),((UndoTopic)e.getValue()).getWrapped());
        }
        
        if(wrapped.getRoles().isEmpty()) {
            try {
                topicMap.addUndoOperation(new CreateAssociationOperation(wrapped.getType(),ps));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }                                    
        }
        else {
            try {
                topicMap.addUndoOperation(ModifyAssociationOperation.addPlayers(wrapped, ps));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }                                    
        }
        
        wrapped.addPlayers(ps);
        
    }

    @Override
    public void removePlayer(Topic role) throws TopicMapException {
        Topic wrole=((UndoTopic)role).getWrapped();
        
        if(wrapped.getRoles().size()==1 && wrapped.getPlayer(role)!=null) {
            try {
                topicMap.addUndoOperation(new RemoveAssociationOperation(wrapped));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }                                    
        }
        else {
            try {
                topicMap.addUndoOperation(ModifyAssociationOperation.removePlayer(wrapped,wrole));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }                                                
        }
        
        wrapped.removePlayer(wrole);
    }

    @Override
    public Collection<Topic> getRoles() throws TopicMapException {
        return topicMap.wrapTopics(wrapped.getRoles());
    }

    @Override
    public TopicMap getTopicMap() {
        return topicMap;
    }

    @Override
    public void remove() throws TopicMapException {
        try {
            topicMap.addUndoOperation(new RemoveAssociationOperation(wrapped));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }                                                
        
        wrapped.remove();
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return wrapped.isRemoved();
    }
    
}
