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

import java.util.HashMap;
import java.util.Map;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */


public class ModifyAssociationOperation extends UndoOperation {

    private RemoveAssociationOperation removeOperation;
    private CreateAssociationOperation addOperation;
    
    private boolean nop=false; // don't do anything if this is true
    
    private ModifyAssociationOperation() {
    }
    
    
    
    public static ModifyAssociationOperation setType(Association a, Topic type) throws TopicMapException, UndoException {
        ModifyAssociationOperation ret=new ModifyAssociationOperation();
        
        Topic oldType=a.getType();
        if(oldType.mergesWithTopic(type)) {
            ret.nop=true;
            return ret;
        }
                
        ret.removeOperation=new RemoveAssociationOperation(a);
        ret.addOperation=new CreateAssociationOperation(a,false);
        ret.addOperation.setType(type.getFirstSubjectIdentifier());
        return ret;
    }
    
    
    public static ModifyAssociationOperation addPlayers(Association a, Map<Topic,Topic> members) throws TopicMapException, UndoException {
        ModifyAssociationOperation ret=new ModifyAssociationOperation();
        ret.removeOperation=new RemoveAssociationOperation(a);
        ret.addOperation=new CreateAssociationOperation(a,false);
        
        // make sure that at least one of the players actually changes something, then turn this to false
        ret.nop=true;
        
        HashMap<Locator,Locator> players=ret.addOperation.getPlayers();
        for(Map.Entry<Topic,Topic> e : members.entrySet()){
            if(ret.nop){
                Topic oldPlayer=a.getPlayer(e.getKey());
                if(oldPlayer==null || !e.getValue().mergesWithTopic(oldPlayer)) ret.nop=false;
            }
            
            Locator rsi=e.getKey().getFirstSubjectIdentifier();
            Locator psi=e.getValue().getFirstSubjectIdentifier();
            if(rsi==null || psi==null) throw new UndoException();
            players.put(rsi,psi);
        }
        ret.addOperation.setPlayers(players);
        return ret;        
    }
    
    
    public static ModifyAssociationOperation addPlayer(Association a, Topic role, Topic player) throws TopicMapException, UndoException {
        ModifyAssociationOperation ret=new ModifyAssociationOperation();
        
        Topic oldPlayer=a.getPlayer(role);
        if(oldPlayer!=null && player!=null && oldPlayer.mergesWithTopic(player)){
            ret.nop=true; // the operation doesn't actually change anything at all.
            return ret;
        }
        if(player==null && oldPlayer==null){
            ret.nop=true;
            return ret;
        }
        
        ret.removeOperation=new RemoveAssociationOperation(a);
        ret.addOperation=new CreateAssociationOperation(a,false);
        HashMap<Locator,Locator> players=ret.addOperation.getPlayers();
        if(player==null) players.remove(role.getFirstSubjectIdentifier());
        else players.put(role.getFirstSubjectIdentifier(),player.getFirstSubjectIdentifier());
        ret.addOperation.setPlayers(players);
        return ret;        
    }
    
    
    public static ModifyAssociationOperation removePlayer(Association a, Topic role) throws TopicMapException, UndoException {
        return addPlayer(a,role,null);
    }
    
    
    // -------------------------------------------------------------------------
    
    @Override
    public void undo() throws UndoException {
        if(nop) return;
        addOperation.undo();
        removeOperation.undo();
    }

    @Override
    public void redo() throws UndoException {
        if(nop) return;
        removeOperation.redo();
        addOperation.redo();
    }

    @Override
    public String getLabel() {
        return "modify association";
    }

    @Override
    public UndoOperation combineWith(UndoOperation previous) {
        if(previous instanceof CreateAssociationOperation){
            if(nop) return previous;
            UndoOperation op=removeOperation.combineWith(previous);
            if(op!=null && op instanceof NoOperation){
                return addOperation.clone();
            }
        }
        return null;
    }
    
    
}
