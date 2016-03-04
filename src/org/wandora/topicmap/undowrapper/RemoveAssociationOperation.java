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
 */



package org.wandora.topicmap.undowrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */


public class RemoveAssociationOperation  extends UndoOperation  {

    private TopicMap tm;
    private Locator type;
    private HashMap<Locator,Locator> players;
    
    public RemoveAssociationOperation(Association a) throws UndoException, TopicMapException {
        this.tm=a.getTopicMap();
        
        // getFirstSubjectIdentifier is used as opposed to getOneSubjectIdentifier
        // to make sure that same association results in same locators
        // and the operations will be the same according to the equals check and
        // hashCode.
        
        type=a.getType().getFirstSubjectIdentifier();
        if(type==null) throw new UndoException();
        
        players=new HashMap<Locator,Locator>();
        
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
    }

    Locator getType(){return type;};
    void setType(Locator type){this.type=type;}
    HashMap<Locator,Locator> getPlayers(){return players;}
    void setPlayers(HashMap<Locator,Locator> players){this.players=players;}
    
    
    
    public static Association findAssociation(TopicMap tm,Locator type,HashMap<Locator,Locator> players) throws TopicMapException, UndoException {
        Topic t=tm.getTopic(type);
        
        if(players.isEmpty()){
            if(t==null) throw new UndoException();
            Collection<Association> as=tm.getAssociationsOfType(t);
            for(Association a : as){
                if(a.getRoles().isEmpty()) {
                    return a;
                }
            }
            // association not found
            return null;
        }
        
        Collection<Map.Entry<Locator,Locator>> ms=players.entrySet();
/*        ArrayList<Map.Entry<Locator,Locator>> ms=new ArrayList<Map.Entry<Locator,Locator>>(players.entrySet());
        Collections.sort(ms,new Comparator<Map.Entry<Locator,Locator>>(){
            @Override
            public int compare(Map.Entry<Locator, Locator> o1, Map.Entry<Locator, Locator> o2) {
                int c=o1.getKey().compareTo(o2.getKey());
                if(c!=0) return c;
                else return o1.getValue().compareTo(o2.getValue());
            }
        });*/
        
        Map.Entry<Locator,Locator> firstm=ms.iterator().next();
        Locator fpSi=firstm.getValue(); // first player
        Locator frSi=firstm.getKey(); // first role
        
        Topic fp=tm.getTopic(fpSi);
        Topic fr=tm.getTopic(frSi);
        if(fp==null || fr==null || t==null) throw new UndoException();

        Collection<Association> as=fp.getAssociations(t, fr);
        
        AssociationsLoop: for(Association a : as){
            Collection<Topic> roles=a.getRoles();
            if(roles.size()!=players.size()) continue;
            
            RolesLoop: for(Topic role : roles){
                Collection<Locator> rSis=role.getSubjectIdentifiers();
                Collection<Locator> pSis=a.getPlayer(role).getSubjectIdentifiers();
                
                for(Map.Entry<Locator,Locator> m : ms){
                    if(!rSis.contains(m.getKey())) continue ;
                    if(!pSis.contains(m.getValue())) continue ;
                    
                    // this member matches, check next role
                    continue RolesLoop;
                }
                
                // no member found for the role so the association can't match
                continue AssociationsLoop;
            }
            
            // all Roles matched so this is the association we're looking for
            return a;
        }
        
/*        
        AssociationsLoop: for(Association a : as){
            ArrayList<Topic> roles=new ArrayList<Topic>(a.getRoles());
            boolean[] used=new boolean[roles.size()];
            
            MembersLoop: for(Map.Entry<Locator,Locator> m : ms){
                for(int i=0;i<roles.size();i++){
                    Topic role=roles.get(i);
                    Collection<Locator> rSis=role.getSubjectIdentifiers();
                    if(!rSis.contains(m.getKey())) continue;
                    Collection<Locator> pSis=a.getPlayer(role).getSubjectIdentifiers();
                    if(!pSis.contains(m.getValue())) continue;
                    
                    used[i]=true;
                    
                    continue MembersLoop;
                }
                
                // no matcing player found for member, check next association
                continue AssociationsLoop;
            }
            
            // a match was found for all members
            for(int i=0;i<used.length;i++){
                if(!used[i]) {
                    // not all roles of the association were used, meaning
                    // that this isn't the association we want, check next
                    continue AssociationsLoop;
                }
            }
            // all members in the map matched and all roles of the association
            // were used, return this association
            return a;
        }*/
        
        return null;
    }
    
    
    
    
    @Override
    public void undo() throws UndoException {
        try{
            Topic t=tm.getTopic(type);
            if(t==null) throw new UndoException();
            HashMap<Topic,Topic> ps=new HashMap<Topic,Topic>();
            for(Map.Entry<Locator,Locator> e : players.entrySet()){
                Topic r=tm.getTopic(e.getKey());
                Topic p=tm.getTopic(e.getValue());
                if(r==null || p==null) throw new UndoException();
                ps.put(r,p);
            }
            Association a=tm.createAssociation(t);
            if(ps.size()>0) a.addPlayers(ps);
        }catch(TopicMapException tme){throw new UndoException(tme);}
    }

    
    
    
    @Override
    public void redo() throws UndoException {
        try{
            Association a=findAssociation(tm,type,players);
            if(a==null) throw new AssociationNotFoundException();
            else a.remove();
        }catch(TopicMapException tme){throw new UndoException(tme);}
    }

    @Override
    public String getLabel() {
        return "remove association";
    }

    @Override
    public UndoOperation combineWith(UndoOperation previous) {
        if(previous instanceof CreateAssociationOperation){
            CreateAssociationOperation add=(CreateAssociationOperation)previous;
            if(type.equals(add.getType()) && players.equals(add.getPlayers())) {
                return new NoOperation(); 
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoveAssociationOperation other = (RemoveAssociationOperation) obj;
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        if (this.players != other.players && (this.players == null || !this.players.equals(other.players))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 71 * hash + (this.players != null ? this.players.hashCode() : 0);
        return hash;
    }

    public static class AssociationNotFoundException extends UndoException {
        public AssociationNotFoundException(){
            super();
        }
        public AssociationNotFoundException(String message){
            super(message);
        }
    }
}
