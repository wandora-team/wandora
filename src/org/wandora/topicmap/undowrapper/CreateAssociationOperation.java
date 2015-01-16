/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
import java.util.LinkedHashMap;
import java.util.Map;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */


public class CreateAssociationOperation  extends UndoOperation {

    private TopicMap tm;
    private Locator type;
    private HashMap<Locator,Locator> players;
    private boolean associationAlreadyExists = false;

    public CreateAssociationOperation(TopicMap tm,Locator type,HashMap<Locator,Locator> players) throws UndoException,TopicMapException {
        this.tm = tm;
        this.type = type;
        this.players = players;
        
        Association oa=RemoveAssociationOperation.findAssociation(tm,type,players);
        associationAlreadyExists = (oa!=null);        
    }
    public CreateAssociationOperation(Topic type,HashMap<Topic,Topic> players) throws UndoException,TopicMapException {
        this.tm = type.getTopicMap();
        this.type = type.getOneSubjectIdentifier();
        this.players = new HashMap<Locator,Locator>();
        
        for(Map.Entry<Topic,Topic> e : players.entrySet()){
            Locator rsi=e.getKey().getOneSubjectIdentifier();
            Locator psi=e.getValue().getOneSubjectIdentifier();
            if(rsi==null || psi==null) throw new UndoException();
            this.players.put(rsi,psi);
        }
        
        Association oa=RemoveAssociationOperation.findAssociation(tm,this.type,this.players);
        associationAlreadyExists = (oa!=null);        
    }
    public CreateAssociationOperation(TopicMap tm,Locator type,Locator role,Locator player) throws UndoException,TopicMapException {
        this.tm = tm;
        this.type = type;
        this.players = new HashMap<Locator,Locator>();
        players.put(role,player);
        
        Association oa=RemoveAssociationOperation.findAssociation(tm,type,players);
        associationAlreadyExists = (oa!=null);        
    }
    public CreateAssociationOperation(Topic type,Topic role,Topic player) throws UndoException,TopicMapException {
        this.tm = type.getTopicMap();
        this.type = type.getOneSubjectIdentifier();
        this.players = new HashMap<Locator,Locator>();
        Locator rsi=role.getOneSubjectIdentifier();
        Locator psi=player.getOneSubjectIdentifier();
        if(this.type==null || rsi==null || psi==null) throw new UndoException();
        players.put(rsi,psi);
        
        Association oa=RemoveAssociationOperation.findAssociation(tm,this.type,players);
        associationAlreadyExists = (oa!=null);        
    }
    
    // this constructor is used by some of the other undo operations, but it 
    // should not be used by outside code as the exists flag needs manual handling
    CreateAssociationOperation(Association a,boolean exists) throws UndoException, TopicMapException {
        this.tm=a.getTopicMap();
        
        type=a.getType().getFirstSubjectIdentifier();
        if(type==null) throw new UndoException();
        
        players=new LinkedHashMap<Locator,Locator>();
        
        Collection<Topic> rs=a.getRoles();
//        ArrayList<Topic> rs=new ArrayList<Topic>(a.getRoles());
//        Collections.sort(rs,new TMBox.TopicNameComparator(null));
        for(Topic r : rs){
            Locator rsi=r.getFirstSubjectIdentifier();
            if(rsi==null) throw new UndoException();
            Topic p=a.getPlayer(r);
            Locator psi=p.getFirstSubjectIdentifier();
            if(psi==null) throw new UndoException();
            players.put(rsi,psi);
        }
        
        this.associationAlreadyExists=exists;
    }
    
    // this constructor is only for the clone method, it should not be used by outside code
    private CreateAssociationOperation(TopicMap tm,Locator type,HashMap<Locator,Locator> players, boolean exists) {
        this.tm = tm;
        this.type = type;
        this.players = players;
        this.associationAlreadyExists = exists;
    }
    
    
    Locator getType(){
        return type;
    };
    
    void setType(Locator type) { 
        this.type=type;
        try {
            Association oa=RemoveAssociationOperation.findAssociation(tm,type,players);
            associationAlreadyExists = (oa!=null);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    HashMap<Locator,Locator> getPlayers(){
        return players;
    }
    
    void setPlayers(HashMap<Locator,Locator> players) {
        this.players=players;
        try {
            Association oa=RemoveAssociationOperation.findAssociation(tm,type,players);
            associationAlreadyExists = (oa!=null);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    @Override
    public void redo() throws UndoException {
        try {
            if(!associationAlreadyExists) {
                Topic t=tm.getTopic(type);
                if(t==null) throw new UndoException();
                HashMap<Topic,Topic> ps=new HashMap<Topic,Topic>();
                for(Map.Entry<Locator,Locator> e : players.entrySet()) {
                    Topic r=tm.getTopic(e.getKey());
                    Topic p=tm.getTopic(e.getValue());
                    if(r==null || p==null) throw new UndoException();
                    ps.put(r,p);
                }
                Association a=tm.createAssociation(t);
                if(ps.size()>0) a.addPlayers(ps);
            }
        }
        catch(TopicMapException tme){
            throw new UndoException(tme);
        }
    }

    @Override
    public void undo() throws UndoException {
        try {
            if(!associationAlreadyExists) {
                Association a=RemoveAssociationOperation.findAssociation(tm,type,players);
                if(a != null) {
                    a.remove();
                }
            }
        }
        catch(TopicMapException tme) {
            throw new UndoException(tme);
        }
    }

    @Override
    public String getLabel() {
        return "add association";
    }
    
    @Override
    public CreateAssociationOperation clone(){
        HashMap<Locator,Locator> pclone=new HashMap<Locator,Locator>();
        pclone.putAll(players);
        return new CreateAssociationOperation(tm,type,pclone,associationAlreadyExists);
    }
    
}
