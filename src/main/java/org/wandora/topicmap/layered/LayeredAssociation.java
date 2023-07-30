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
 *
 * 
 *
 * LayeredAssociation.java
 *
 * Created on 8. syyskuuta 2005, 11:30
 */

package org.wandora.topicmap.layered;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Vector;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapReadOnlyException;
import org.wandora.utils.KeyedHashMap;

/**
 *
 * Unlike LayeredTopic, LayeredAssociation is not a collection of individual
 * associations of different layers. Of course it can still be treated like
 * this but the implementation does not keep a collection of associations the
 * way LayeredTopic does. Instead the association simply has a LayeredTopic
 * for association type and a map that maps roles to players, both
 * LayeredTopics.
 *
 * Whenever LayeredAssociation is being modified an association of the
 * selected layer that matches this layered association needs to be searched for.
 * If such an association is found, it can be used for the modification. Finding
 * the matching association is made difficult by the fact that role topics of an
 * individual association may have merged. In this case two (or more) roles are showing as
 * only one role in LayeredAssociation. Although these kind of topic merges
 * might be rare, they are still possible and may have clever practical uses.
 *
 * In cases where role topics have merged, several LayeredAssociations are
 * created in the layer stack, one for each possible combination of roles. For
 * example, suppose that an association has roles named 1,2,3 and 4 with players
 * A,B,C and D respectively. Suppose that in the layer stack roles 1 and 2 merge
 * and roles 3 and 4 merge. Then the individual association is showing as four
 * associations in the layer stack. (1:A,3:C), (1:B,3:C), (1:A,3:D) and (1:B,3:D).
 * Each of these (and only these) matches the individual association. Editing
 * any of them will affect all of the LayeredAssociations because they all
 * originate from the same association.
 *
 * @author olli
 */
public class LayeredAssociation implements Association {
    
    protected LayeredTopic type;
    protected LayerStack layerStack;
    //                  role        ,player
    protected KeyedHashMap<LayeredTopic,LayeredTopic> players;
    
    /** Creates a new instance of LayeredAssociation */
    public LayeredAssociation(LayerStack layerStack, LayeredTopic type) {
        this.layerStack=layerStack;
        this.type=type;
        players=new KeyedHashMap<LayeredTopic,LayeredTopic>(new LayeredTopic.TopicKeyMaker());
    }
    
    public boolean equals(Object o){
        if(o instanceof LayeredAssociation){
            if(hashCode()!=((LayeredAssociation)o).hashCode()) return false;
            return type.equals(((LayeredAssociation)o).type) && 
                   players.equals(((LayeredAssociation)o).players);
        }
        else return false;
    }
    
    private Integer hashCode=null;;
    public int hashCode(){
        if(hashCode==null) hashCode=players.hashCode()+type.hashCode();
        return hashCode;
    }
    
    protected void ambiguity(String s){
        layerStack.ambiguity(s);
    }
    
    public Topic getType(){
        return type;
    }
    
    @Override
    public void setType(Topic t) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> types=((LayeredTopic)t).getTopicsForSelectedLayer();
        if(types.size()==0){
            ambiguity("No type for selected layer (setType)");
            return;
        }
        else if(types.size()>1) ambiguity("Several types for selected layer (setType)");
        hashCode=null;
        Association a=findAssociationForLayer(layerStack.getSelectedLayer());
        if(a != null) {
            a.setType(types.iterator().next());
            type=(LayeredTopic)t;
        }
    }
    
    @Override
    public Topic getPlayer(Topic role){
        return players.get((LayeredTopic)role);
    }
    
    /**
     * Checks if the given non layered association matches this association.
     */
    public boolean associationMatchesThis(Association a) throws TopicMapException {
//        if(a.getRoles().size()!=getRoles().size()) return false;
        Layer l=layerStack.getLayer(a.getTopicMap());
        Collection<Topic> typeTopics=type.getTopicsForLayer(l);
        HashSet<LayeredTopic> usedRole=new LinkedHashSet<LayeredTopic>();
        for( Topic role : a.getRoles() ){
            Topic player=a.getPlayer(role);
            boolean found=false;
            for( Map.Entry<LayeredTopic,LayeredTopic> e : players.entrySet() ){
//                if(usedRole.contains(e.getKey())) continue;
                if(!e.getKey().mergesWithTopic(role)) continue;
                found=true;
                if(!e.getValue().mergesWithTopic(player)) continue;
                
                usedRole.add(e.getKey());
                break;
            }
            if(!found) return false;
        }
        if(usedRole.size()==players.size()) return true;
        else return false;
    }
    
    /**
     * Finds an association in the given layer that matches this LayredAssociation.
     * Returns null if no such association is found.
     */
    public Association findAssociationForLayer(Layer l)  throws TopicMapException {
        for(Map.Entry<LayeredTopic,LayeredTopic> e : players.entrySet()){
            LayeredTopic p=e.getValue();
            LayeredTopic r=e.getKey();
            for(Topic sp : p.getTopicsForLayer(l)){
                for(Topic sr : r.getTopicsForLayer(l)){
                    for(Topic st : type.getTopicsForLayer(l)){
                        for(Association a : sp.getAssociations(st, sr)){
                            if(associationMatchesThis(a)){
                                return a;
                            }
                        }
                    }
                }
            }
            break;
        }
        return null;
    }
    
    /**
     * Finds all associations in the given layer that match this LayeredAssociation.
     */ 
    public Collection<Association> findAssociationsForLayer(Layer l)  throws TopicMapException {
        HashSet<Association> ret=new LinkedHashSet<Association>();
        for(Map.Entry<LayeredTopic,LayeredTopic> e : players.entrySet()){
            LayeredTopic p=e.getValue();
            LayeredTopic r=e.getKey();
            for(Topic sp : p.getTopicsForLayer(l)){
                for(Topic sr : r.getTopicsForLayer(l)){
                    for(Topic st : type.getTopicsForLayer(l)){
                        for(Association a : sp.getAssociations(st, sr)){
                            if(associationMatchesThis(a)){
                                ret.add(a);
                            }
                        }
                    }
                }
            }
            break;
        }
        return ret;
    }
    
    public Association findAssociationForSelectedLayer()  throws TopicMapException {
        return findAssociationForLayer(layerStack.getSelectedLayer());
    }
    
    /**
     * Adds a player to this LayeredAssociation object. Does not actually modify
     * the association, this method is used to construct LayeredAssociation objects
     * that will later be returned outside the topic map implementation when they
     * are fully constructed.
     */
    void addLayeredPlayer(LayeredTopic player,LayeredTopic role){
        hashCode=null;
        players.put(role,player);
    }
    
    /**
     * Adds a player to the association and modifies the appropriate individual
     * association accordingly. Note that if this association isn't already
     * in the selected layer, it will be copied there. Also any roles and players
     * (the ones being added or existing ones) will be copied to the selected
     * layer if they aren't there already.
     */
    @Override
    public void addPlayer(Topic player,Topic role) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> lplayer=((LayeredTopic)player).getTopicsForSelectedLayer();
        Topic splayer=null;
        if(lplayer.isEmpty()){
            AmbiguityResolution res=layerStack.resolveAmbiguity("addPlayer.player.noSelected","No player in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                splayer=((LayeredTopic)player).copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(splayer==null){
                    ambiguity("Cannot copy topic to selected layer");
                    throw new TopicMapException("Cannot copy topic to selected layer");
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(lplayer.size()>1) ambiguity("Several players in selected layer (addPlayer)");
            splayer=lplayer.iterator().next();
        }
        Collection<Topic> lrole=((LayeredTopic)role).getTopicsForSelectedLayer();
        Topic srole=null;
        if(lrole.isEmpty()){
            AmbiguityResolution res=layerStack.resolveAmbiguity("addPlayer.role.noSelected","No role in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                srole=((LayeredTopic)role).copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(srole==null){
                    ambiguity("Cannot copy topic to selected layer");
                    throw new TopicMapException("Cannot copy topic to selected layer");
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(lrole.size()>1) ambiguity("Several roles in selected layer (addPlayer)");
            srole=lrole.iterator().next();
        }
        // TODO: doesn't handle multiple association matches
        Association a=null;
        if(players.isEmpty()){
            Layer l=layerStack.getSelectedLayer();
            Collection<Topic> c=type.getTopicsForSelectedLayer();
            Topic st=null;
            if(c.isEmpty()){
                AmbiguityResolution res=layerStack.resolveAmbiguity("addPlayer.type.noSelected","No type in selected layer");
                if(res==AmbiguityResolution.addToSelected){
                    st=((LayeredTopic)type).copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                    if(st==null){
                        ambiguity("Cannot copy topic to selected layer");
                        throw new TopicMapException("Cannot copy topic to selected layer");
                    }
                }
                else throw new RuntimeException("Not implemented");
            }
            else{
                if(c.size()>1) ambiguity("Multiple possible types in layer (createAssociation)");
                st=c.iterator().next();
            }
            a=l.getTopicMap().createAssociation(st);
        }
        else a=findAssociationForLayer(layerStack.getSelectedLayer());
        if(a!=null) {
            a.addPlayer(splayer,srole);
            hashCode=null;
            players.put((LayeredTopic)role,(LayeredTopic)player);
        }
        else ambiguity("No matching association found in selected layer (addPlayer)");
    }
    
    @Override
    public void addPlayers(Map<Topic,Topic> players) throws TopicMapException {
        for(Map.Entry<Topic,Topic> e : players.entrySet()){
            addPlayer(e.getValue(), e.getKey()); // PARAMETER ORDER: PLAYER, ROLE
        }
    }
    
    @Override
    public void removePlayer(Topic role) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> lrole=((LayeredTopic)role).getTopicsForSelectedLayer();
        if(lrole.isEmpty()){
            ambiguity("No role in selected layer, nothing done (removePlayer)");
            return;
        }
        else if(lrole.size()>1) ambiguity("Several roles in selected layer (removePlayer)");
        
        for(Map.Entry<LayeredTopic,LayeredTopic> e : players.entrySet()){
            LayeredTopic p=e.getValue();
            LayeredTopic r=e.getKey();
            for(Topic sp : p.getTopicsForSelectedLayer()){
                for(Topic sr : r.getTopicsForSelectedLayer()){
                    for(Topic st : type.getTopicsForSelectedLayer()){
                        for(Association a : sp.getAssociations(st, sr)){
                            if(associationMatchesThis(a)){
                                for(Topic lr : lrole){
                                    if(a.getPlayer(lr)!=null){
                                        a.removePlayer(lr);
                                        hashCode=null;
                                        players.remove((LayeredTopic)role);
                                        return;
                                        // TODO: doesn't handle multiple matches
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        ambiguity("No matching association found in selected layer (removePlayer)");
    }
    
    @Override
    public Collection<Topic> getRoles(){
        Vector<Topic> v=new Vector<Topic>();
        v.addAll(players.keySet());
        return v;
    }
    
    @Override
    public TopicMap getTopicMap(){
        return layerStack;
    }
    
    @Override
    public void remove() throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        // TODO: doesn't handle multiple matches
        Association a=findAssociationForLayer(layerStack.getSelectedLayer());
        if(a==null) ambiguity("No matching assaciation found in selected layer (remove)");
        else a.remove();
    }
    
    @Override
    public boolean isRemoved() throws TopicMapException {
        // TODO: doesn't handle multiple matches
        Association a=findAssociationForLayer(layerStack.getSelectedLayer());
        if(a==null) {
            ambiguity("No matching assaciation found in selected layer (isRemoved)");
            return false;
        }
        else return a.isRemoved();
    }
    
}
