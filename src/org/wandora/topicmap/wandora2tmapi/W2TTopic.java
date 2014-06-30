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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.tmapi.core.IdentityConstraintException;
import org.tmapi.core.Locator;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Name;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Reifiable;
import org.tmapi.core.Role;
import org.tmapi.core.TMAPIRuntimeException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicInUseException;
import org.tmapi.core.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class W2TTopic implements Topic {

    protected W2TTopicMap tm;
    protected org.wandora.topicmap.Topic t;
    
    protected boolean strictFailure=true;
    
    public W2TTopic(W2TTopicMap tm,org.wandora.topicmap.Topic t){
        this.tm=tm;
        this.t=t;
    }

    public boolean isStrictFailure() {
        return strictFailure;
    }

    public void setStrictFailure(boolean strictFailure) {
        if(strictFailure==false) throw new RuntimeException("not implemented yet");
        this.strictFailure = strictFailure;
    }
    
    
    
    public org.wandora.topicmap.Topic getWrapped(){
        return t;
    }
    
    @Override
    public TopicMap getParent() {
        return tm;
    }

    @Override
    public void addItemIdentifier(Locator lctr) {
        throw new UnsupportedOperationException("Item identifiers not supported");
    }

    @Override
    public Set<Locator> getSubjectIdentifiers() {
        try{
            return tm.wrapLocators(t.getSubjectIdentifiers());
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    /*
     The tmapi specifies an autoMerge feature which can be set off or on when
     creating the topic map. If off, merges should not happen automatically in
     addSubjectIdentifier etc, instead an exception should be thrown in such a
     case. Currently there is no way to change this setting however and automerge
     is assumed to be turned on always.
    */
    
    @Override
    public void addSubjectIdentifier(Locator lctr) throws IdentityConstraintException, ModelConstraintException {
        try{
            t.addSubjectIdentifier(tm.tm.createLocator(lctr.toExternalForm()));
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public void removeSubjectIdentifier(Locator lctr) {
        try{
            t.removeSubjectIdentifier(tm.tm.createLocator(lctr.toExternalForm()));
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }

    @Override
    public Set<Locator> getSubjectLocators() {
        try{
            org.wandora.topicmap.Locator l=t.getSubjectLocator();
            HashSet<Locator> ret=new HashSet<Locator>();
            if(l!=null) ret.add(new W2TLocator(l));
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public void addSubjectLocator(Locator lctr) throws IdentityConstraintException, ModelConstraintException {
        try{
            if(t.getSubjectLocator()==null) t.setSubjectLocator(tm.tm.createLocator(lctr.toExternalForm()));
            else throw new UnsupportedOperationException("Multiple subject locators in a topic not supported");
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public void removeSubjectLocator(Locator lctr) {
        try{
            if(t.getSubjectLocator()!=null && lctr!=null){
                if(t.getSubjectLocator().toExternalForm().equals(lctr.toExternalForm())) t.setSubjectLocator(null);
            }
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    public Name _getName(){
        try{
            if(t.getBaseName()!=null) return new W2TName(tm,this);
            else return null;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }
    
    /*
     If the base name of a topic is null, then it will not have any Names
     and thus no variant names. But Wandora topic map model does allow variant
     names without a base name. TMAPI doesn't allow null value in the name but
     empty string would be allowed. This however might cause problems with
     merge handling.
    */
    
    @Override
    public Set<Name> getNames() {
        HashSet<Name> ret=new HashSet<Name>();
        Name name=_getName();
        if(name!=null) ret.add(name);
        return ret;
    }

    @Override
    public Set<Name> getNames(Topic topic) {
        // all names have the default type so this is either equal to getNames or an empty set
        if(topic.getSubjectIdentifiers().contains(tm.createLocator(W2TTopicMap.TOPIC_NAME_SI))) return getNames();
        else return new HashSet<Name>();
    }

    /*
     The name methods are violating the specification slightly when it comes ot Name
     type. Each Name must have a type instead of the type being null. The createName
     methods that don't take a type should automatically use the default name type
     http://psi.topicmaps.org/iso13250/model/topic-name
    */
    
    @Override
    public Name createName(Topic type, String value, Topic... scope) throws ModelConstraintException {
        if(_getName()!=null) throw new UnsupportedOperationException("Only one name per topic is supported");
        else {
            if(scope.length>0) throw new UnsupportedOperationException("Scoped names are not supported");
            if(!type.getSubjectIdentifiers().contains(tm.createLocator(W2TTopicMap.TOPIC_NAME_SI)))
                throw new UnsupportedOperationException("only default name type is supported");
            try{
                t.setBaseName(value);
                return _getName();
            }catch(TopicMapException tme){
                throw new TMAPIRuntimeException(tme);
            }
        }
    }

    @Override
    public Name createName(Topic type, String value, Collection<Topic> scope) throws ModelConstraintException {
        return createName(type,value,scope.toArray(new Topic[0]));
    }

    @Override
    public Name createName(String value, Topic... scope) throws ModelConstraintException {
        return createName(tm.getTopicBySubjectIdentifier(W2TTopicMap.TOPIC_NAME_SI),value,scope);
    }

    @Override
    public Name createName(String value, Collection<Topic> scope) throws ModelConstraintException {
        return createName(tm.getTopicBySubjectIdentifier(W2TTopicMap.TOPIC_NAME_SI),value,scope.toArray(new Topic[0]));
    }

    @Override
    public Set<Occurrence> getOccurrences() {
        try{
            HashSet<Occurrence> ret=new HashSet<Occurrence>();
            for(org.wandora.topicmap.Topic _type : t.getDataTypes()){
                W2TTopic type=new W2TTopic(tm,_type);
                Hashtable<org.wandora.topicmap.Topic,String> occs=t.getData(_type);

                for(Map.Entry<org.wandora.topicmap.Topic,String> e : occs.entrySet()){
                    org.wandora.topicmap.Topic lang=e.getKey();
                    ret.add(new W2TOccurrence(this,type,lang!=null?new W2TTopic(tm,lang):null));
                }
            }
            
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Set<Occurrence> getOccurrences(Topic type) {
        try{
            Hashtable<org.wandora.topicmap.Topic,String> occs=t.getData(((W2TTopic)type).t);
            HashSet<Occurrence> ret=new HashSet<Occurrence>();
            
            for(Map.Entry<org.wandora.topicmap.Topic,String> e : occs.entrySet()){
                org.wandora.topicmap.Topic lang=e.getKey();
                ret.add(new W2TOccurrence(this,(W2TTopic)type,lang!=null?new W2TTopic(tm,lang):null));
            }
            
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }
    
    /*
     Datatypes aren't supported in Wandora. It should automatically be assigned
     a value xsd:string or xsd:anyURI but a null is used here instead.
    */

    @Override
    public Occurrence createOccurrence(Topic type, String value, Topic... scope) throws ModelConstraintException {
        return createOccurrence(type,value,tm.createLocator(W2TTopicMap.TYPE_STRING_SI),scope);
    }

    @Override
    public Occurrence createOccurrence(Topic type, String value, Collection<Topic> scope) throws ModelConstraintException {
        return createOccurrence(type,value,tm.createLocator(W2TTopicMap.TYPE_STRING_SI),scope);
    }

    @Override
    public Occurrence createOccurrence(Topic type, Locator value, Topic... scope) throws ModelConstraintException {
        throw new UnsupportedOperationException("Only string occurrences are supported");
        //return createOccurrence(type,value.toExternalForm(),null,scope);
    }

    @Override
    public Occurrence createOccurrence(Topic type, Locator value, Collection<Topic> scope) throws ModelConstraintException {
        throw new UnsupportedOperationException("Only string occurrences are supported");
//        return createOccurrence(type,value.toExternalForm(),null,scope);
    }

    @Override
    public Occurrence createOccurrence(Topic type, String value, Locator dataType, Topic... scope) throws ModelConstraintException {
        if(!dataType.toExternalForm().equals(W2TTopicMap.TYPE_STRING_SI)) throw new UnsupportedOperationException("Only string occurrences are supported");
        if(scope.length!=1) throw new UnsupportedOperationException("The scope must have exactly one topic indicating the language");
        
        Topic version=scope[0];
        
        try{
            String data=t.getData(((W2TTopic)type).getWrapped(), ((W2TTopic)version).getWrapped());
            if(data!=null) throw new UnsupportedOperationException("Only one occurrence for each type/language pair allowed");
            
            t.setData(((W2TTopic)type).getWrapped(), ((W2TTopic)version).getWrapped(), value);
            
            return new W2TOccurrence(this,(W2TTopic)type,(W2TTopic)version);
        }
        catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
        
    }

    @Override
    public Occurrence createOccurrence(Topic type, String value, Locator dataType, Collection<Topic> scope) throws ModelConstraintException {
        return createOccurrence(type,value,dataType,scope.toArray(new Topic[0]));
    }

    protected Set<Role> _getRolesPlayed(Topic type, Topic associationType){
        try{
            HashSet<Role> ret=new HashSet<Role>();
            Collection<org.wandora.topicmap.Association> as;
            if(associationType==null) as=t.getAssociations();
            else as=t.getAssociations(((W2TTopic)associationType).t);
            
            for(org.wandora.topicmap.Association a : as){
                W2TAssociation w2ta=new W2TAssociation(tm,a);
                for(org.wandora.topicmap.Topic role : a.getRoles()){
                    if(type!=null && !((W2TTopic)type).t.mergesWithTopic(role)) continue;
                    
                    W2TTopic w2trole=new W2TTopic(tm, role);
                    org.wandora.topicmap.Topic player=a.getPlayer(role);
                    if(player.mergesWithTopic(this.t)){
                        ret.add(new W2TRole(w2ta, w2trole, this));
                    }
                }
            }
            return ret;
        }
        catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }
    
    @Override
    public Set<Role> getRolesPlayed() {
        return _getRolesPlayed(null,null);
    }

    @Override
    public Set<Role> getRolesPlayed(Topic type) {
        return _getRolesPlayed(type,null);
    }

    @Override
    public Set<Role> getRolesPlayed(Topic type, Topic associationType) {
        return _getRolesPlayed(type,associationType);
    }

    @Override
    public Set<Topic> getTypes() {
        try{
            return tm.wrapTopics(t.getTypes());
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public void addType(Topic topic) throws ModelConstraintException {
        try{
            t.addType(((W2TTopic)topic).getWrapped());
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }

    @Override
    public void removeType(Topic topic) {
        try{
            t.removeType(((W2TTopic)topic).getWrapped());
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }

    @Override
    public Reifiable getReified() {
        return null;
    }

    @Override
    public void mergeIn(Topic topic) throws ModelConstraintException {
        try{
            org.wandora.topicmap.Topic w=((W2TTopic)topic).getWrapped();
            org.wandora.topicmap.Locator si=w.getOneSubjectIdentifier();
            if(si==null){
                si=t.getTopicMap().makeSubjectIndicatorAsLocator();
                w.addSubjectIdentifier(si);
                t.addSubjectIdentifier(si);
                t.removeSubjectIdentifier(si);
            }
            else {
                t.addSubjectIdentifier(si);
            }
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public void remove() throws TopicInUseException {
        try{
            if(!t.isDeleteAllowed()) throw new TopicInUseException(this,"Topic in use");
            else t.remove();
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public TopicMap getTopicMap() {
        return tm;
    }

    @Override
    public String getId() {
        try{
            return t.getID();
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Set<Locator> getItemIdentifiers() {
        return new HashSet<Locator>();
    }

    @Override
    public void removeItemIdentifier(Locator lctr) {
        // there will never be any item identifiers so nothing needs to be done
    }

    
}
