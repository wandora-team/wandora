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
 * TopicMapModel.java
 *
 * Created on 5.6.2007, 15:18
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import org.wandora.topicmap.*;
import java.util.*;
import static org.wandora.utils.Tuples.*;


/**
 *
 * @author olli
 */
public class TopicMapModel {
    
    private TopicHashMap<TopicNode> topicIndex;
    private HashMap<AssociationWrapper, AssociationEdge> associationEdgeIndex;
    private HashMap<AssociationWrapper, AssociationNode> associationNodeIndex;
    //                 type,instance
    private HashMap<T2<Node,Node>,InstanceEdge> instanceEdgeIndex;
    
    private HashMap<OccurrenceWrapper, OccurrenceEdge> occurrenceEdgeIndex;
    private HashMap<OccurrenceWrapper, OccurrenceNode> occurrenceNodeIndex;
    
    private VModel vModel;
    
    //private HashMap<TopicNode,Color> typeColors;
    
    private TopicMap topicMap;
    
    
    
    /** Creates a new instance of TopicMapModel */
    public TopicMapModel(VModel vModel,TopicMap topicMap) {
        this.vModel=vModel;
        this.topicMap=topicMap;
        topicIndex=new TopicHashMap<TopicNode>();
        associationEdgeIndex=new HashMap<AssociationWrapper,AssociationEdge>();
        associationNodeIndex=new HashMap<AssociationWrapper,AssociationNode>();
        occurrenceEdgeIndex=new HashMap<OccurrenceWrapper,OccurrenceEdge>();
        occurrenceNodeIndex=new HashMap<OccurrenceWrapper,OccurrenceNode>();
        instanceEdgeIndex=new HashMap<T2<Node,Node>,InstanceEdge>();
    }
    
    
    
    public TopicMap getTopicMap(){
        return topicMap;
    }
    
    
    
    
    
    public boolean topicIsIndexed(Topic t){
        return topicIndex.containsKey(t);
    }
    
    
    
    
    public boolean associationIsIndexed(Association a){
        try{
            AssociationWrapper wrapper=new AssociationWrapper(a);
            if(wrapper.type==null || wrapper.roles.length==0) return false;
            if(wrapper.roles.length==2) return associationEdgeIndex.containsKey(wrapper);
            else return associationNodeIndex.containsKey(wrapper);
        }
        catch(TopicMapException tme){ 
            tme.printStackTrace(); 
            return false;
        }
    }
    
    
    
    
    public TopicNode getNodeFor(Topic t) {
        TopicNode ret=topicIndex.get(t);
        if(ret!=null) return ret;
        ret=new TopicNode(t,this);
        topicIndex.put(t,ret);
        return ret;
    }
    
    
    
    
    public AssociationNode getNodeFor(Association a) {
        try {
            AssociationWrapper wrapper=new AssociationWrapper(a);
            if(wrapper.roles.length!=2){
                AssociationNode n=associationNodeIndex.get(wrapper);
                if(n!=null) return n;
                else {
                    n=new AssociationNode(a,this);
                    associationNodeIndex.put(wrapper,n);
                    return n;
                }
            }
            else return null;
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); 
            return null;
        }
    }
    
    
    
    
    public OccurrenceNode getNodeFor(Topic carrier, Topic type, Topic scope, String str) {
        try {
            OccurrenceWrapper wrapper=new OccurrenceWrapper(carrier, type, scope, str);
            OccurrenceNode n=occurrenceNodeIndex.get(wrapper);
            if(n!=null) return n;
            else {
                n=new OccurrenceNode(carrier, type, scope, str, this);
                occurrenceNodeIndex.put(wrapper, n);
                return n;
            }
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); 
            return null;
        }
    }

    
    
    
    
    
    public Edge getEdgeFor(Association a){
        try {
            AssociationWrapper wrapper=new AssociationWrapper(a);
            if(wrapper.roles.length==2){
                AssociationEdge e=associationEdgeIndex.get(wrapper);
                if(e!=null) return e;
                else {
                    e=new AssociationEdge(a,this);
                    associationEdgeIndex.put(wrapper,e);
                    return e;
                }
            }
            else return null;
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); 
            return null;
        }
    }
    
    
    public InstanceEdge getInstanceEdgeFor(Topic type, Topic instance) {
        Node ntype=getNodeFor(type);
        Node ninstance=getNodeFor(instance);
        InstanceEdge e=instanceEdgeIndex.get(t2(ntype,ninstance));
        if(e==null){
            e=new InstanceEdge(type,instance,this);
            instanceEdgeIndex.put(t2(ntype,ninstance),e);
        }
        return e;
    }
    
    
    
    
    public OccurrenceEdge getOccurrenceEdgeFor(Topic carrier, Topic occurrenceType, Topic occurrenceScope, String occurrence) {
        try {
            OccurrenceWrapper wrapper=new OccurrenceWrapper(carrier, occurrenceType, occurrenceScope, occurrence);
            OccurrenceEdge e=occurrenceEdgeIndex.get(wrapper);
            if(e!=null) return e;
            else {
                e=new OccurrenceEdge(carrier, occurrenceType, occurrenceScope, occurrence, this);
                occurrenceEdgeIndex.put(wrapper,e);
                return e;
            }
        }
        catch(TopicMapException tme) {
            tme.printStackTrace(); 
            return null;
        }
    }
    
    
    
    
    /* ---------------------------------------------------------------------- */
    

    
    private class AssociationWrapper {
        public Node type;
        public Node[] roles;
        public Node[] players;
        
        public int hashCode;
        
        public AssociationWrapper(Association a) throws TopicMapException {
            Collection<Topic> roles=a.getRoles();
            this.roles=new Node[roles.size()];
            this.players=new Node[roles.size()];
            if(roles.isEmpty() || a.getType()==null) return;
            this.type=getNodeFor(a.getType());
            int counter=0;
            hashCode=this.type.hashCode();
            for(Topic role : roles){
                Topic player=a.getPlayer(role);
                this.roles[counter]=getNodeFor(role);
                this.players[counter]=getNodeFor(player);
                hashCode^=this.roles[counter].hashCode()+counter;
                hashCode^=this.players[counter].hashCode()+counter+roles.size();
                counter++;
            }
        }
        
        @Override
        public int hashCode(){
            return hashCode;
        }
        
        @Override
        public boolean equals(Object o){
            if(!(o instanceof AssociationWrapper)) return false;
            AssociationWrapper a=(AssociationWrapper)o;
            if(a.hashCode!=this.hashCode) return false;
            if(this.roles.length!=a.roles.length) return false;
            ILoop: for(int i=0;i<this.roles.length;i++){
                for(int j=0;j<a.roles.length;j++){
                    if(this.roles[i].equals(a.roles[j])){
                        if(this.players[i].equals(a.players[i])) continue ILoop;
                        else return false;
                    }
                }
                return false;
            }
            return true;
        }
    }
    
    
    
    
    /* ---------------------------------------------------------------------- */
    
    
    
    private class OccurrenceWrapper {
        public Node carrier;
        public Node type;
        public Node scope;
        public String occurrence;
        
        public int hashCode;
        
        public OccurrenceWrapper(Topic carrier, Topic type, Topic scope, String occurrence) throws TopicMapException {
            this.carrier=getNodeFor(carrier);
            this.type=getNodeFor(type);
            this.scope=getNodeFor(scope);
            this.occurrence=occurrence;
            
            hashCode=this.type.hashCode();
            hashCode^=this.carrier.hashCode()+1;
            hashCode^=this.scope.hashCode()+2;
            hashCode^=this.occurrence.hashCode()+3;
        }
        
        @Override
        public int hashCode(){
            return hashCode;
        }
        
        @Override
        public boolean equals(Object o){
            if(!(o instanceof OccurrenceWrapper)) return false;
            OccurrenceWrapper ow=(OccurrenceWrapper)o;
            if(ow.hashCode!=this.hashCode) return false;
            if(!ow.carrier.equals(carrier)) return false;
            if(!ow.type.equals(type)) return false;
            if(!ow.scope.equals(scope)) return false;
            if(!ow.occurrence.equals(occurrence)) return false;
            return true;
        }
    }



    
}
