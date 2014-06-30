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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.tmapi.core.Association;
import org.tmapi.core.Construct;
import org.tmapi.core.IdentityConstraintException;
import org.tmapi.core.Locator;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Name;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Role;
import org.tmapi.core.TMAPIRuntimeException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.tmapi.core.Variant;
import org.tmapi.index.Index;
import org.tmapi.index.ScopedIndex;
import org.tmapi.index.TypeInstanceIndex;
import org.wandora.topicmap.TopicHashSet;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.layered.Layer;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.topicmap.tmapi2wandora.T2WTopicMap;
import org.wandora.topicmap.undowrapper.UndoException;

/**
 * This is a TMAPI topic map implementation which wraps inside it a
 * Wandora TopicMap. The implementation does support both reading and writing,
 * but there are several restrictions in how the topic map can be written to.
 * These have to do with restrictions in Wandora's topic map model compared to the
 * standard TMDM used by TMAPI. Some structures allowed in TMDM cannot be easily
 * converted to Wandora's data model, UnsupportedOperationExceptions are thrown
 * in such cases.
 *
 * @author olli
 */


public class W2TTopicMap implements TopicMap {
    
    public static final String DEFAULT_TM_LOCATOR="http://wandora.org/si/tmapi/defaultTMLocator";

    protected org.wandora.topicmap.TopicMap original;
    protected org.wandora.topicmap.layered.LayerStack tm;
    protected W2TLocator locator;
    
    public W2TTopicMap(org.wandora.topicmap.TopicMap tm) throws TopicMapException {
        original=tm;
        this.tm=new LayerStack();
        try{
            this.tm.setUseUndo(false);
        }catch(UndoException ue){}
        this.tm.addLayer(new Layer(original, "original", this.tm));
        addCompatibilityLayer();
        
    }
    
    public static final String TOPIC_NAME_SI="http://psi.topicmaps.org/iso13250/model/topic-name";
    public static final String TYPE_STRING_SI="http://www.w3.org/TR/xmlschema-2/#string";
    
    /*
      This wrapper requires a couple of topics to exist in the topic map to work.
      These are added in a separate layers for minimum interference with everything
      else.
    */
    protected void addCompatibilityLayer() throws TopicMapException {
        org.wandora.topicmap.memory.TopicMapImpl c=new org.wandora.topicmap.memory.TopicMapImpl();
        
        c.createTopic().addSubjectIdentifier(c.createLocator(TOPIC_NAME_SI));
        c.createTopic().addSubjectIdentifier(c.createLocator(TYPE_STRING_SI));
        
        Layer l=new Layer(c, "compatibility", this.tm);
        l.setReadOnly(true);
        this.tm.addLayer(l);
    }
    
    public Set<Topic> wrapTopics(Collection<org.wandora.topicmap.Topic> topics){
        HashSet<Topic> ret=new HashSet<Topic>();
        for(org.wandora.topicmap.Topic t : topics){
            ret.add(new W2TTopic(this,t));
        }
        return ret;
    }
    
    public Set<Locator> wrapLocators(Collection<org.wandora.topicmap.Locator> locators){
        HashSet<Locator> ret=new HashSet<Locator>();
        for(org.wandora.topicmap.Locator l : locators){
            ret.add(new W2TLocator(l));
        }
        return ret;
    }
    
    public Set<Association> wrapAssociations(Collection<org.wandora.topicmap.Association> associations){
        HashSet<Association> ret=new HashSet<Association>();
        for(org.wandora.topicmap.Association a : associations){
            ret.add(new W2TAssociation(this,a));
        }
        return ret;        
    }
    
    @Override
    public Construct getParent() {
        return null;
    }

    @Override
    public Set<Topic> getTopics() {
        HashSet<Topic> ret=new HashSet<Topic>();
        try{
            Iterator<org.wandora.topicmap.Topic> iter=tm.getTopics();
            while(iter.hasNext()){
                org.wandora.topicmap.Topic t=iter.next();
                ret.add(new W2TTopic(this,t));
            }
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Locator getLocator() {
        if(locator!=null) return locator;
        else return new W2TLocator(DEFAULT_TM_LOCATOR);
    }
    
    public void setLocator(W2TLocator locator){
        this.locator=locator;
    }

    @Override
    public Set<Association> getAssociations() {
        HashSet<Association> ret=new HashSet<Association>();
        try{
            Iterator<org.wandora.topicmap.Association> iter=tm.getAssociations();
            while(iter.hasNext()){
                org.wandora.topicmap.Association a=iter.next();
                ret.add(new W2TAssociation(this,a));
            }
            return ret;
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Topic getTopicBySubjectIdentifier(Locator lctr) {
        try{
            org.wandora.topicmap.Topic t=tm.getTopic(lctr.toExternalForm()); 
            if(t==null) return null;
            return new W2TTopic(this,t);
        }catch(TopicMapException tme) {
            throw new TMAPIRuntimeException(tme);
        }
    }
    
    public Topic getTopicBySubjectIdentifier(String si) {
        try{
            org.wandora.topicmap.Topic t=tm.getTopic(si); 
            if(t==null) return null;
            return new W2TTopic(this,t);
        }catch(TopicMapException tme) {
            throw new TMAPIRuntimeException(tme);
        }
    }
    

    @Override
    public Topic getTopicBySubjectLocator(Locator lctr) {
        try{
            org.wandora.topicmap.Topic t=tm.getTopicBySubjectLocator(lctr.toExternalForm()); 
            if(t==null) return null;
            return new W2TTopic(this,t);
        }catch(TopicMapException tme) {
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Construct getConstructByItemIdentifier(Locator lctr) {
        return null;
    }

    @Override
    public Construct getConstructById(String string) {
        return null;
    }

    @Override
    public Locator createLocator(String string) throws MalformedIRIException {
        return new W2TLocator(string);
    }

    @Override
    public Topic createTopicBySubjectIdentifier(Locator lctr) throws ModelConstraintException {
        try{
            org.wandora.topicmap.Topic t=tm.getTopic(lctr.toExternalForm());
            if(t==null) {
                t=tm.createTopic();
                t.addSubjectIdentifier(tm.createLocator(lctr.toExternalForm()));
            }
            return new W2TTopic(this,t);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Topic createTopicBySubjectLocator(Locator lctr) throws ModelConstraintException {
        try{
            org.wandora.topicmap.Topic t=tm.getTopicBySubjectLocator(lctr.toExternalForm());
            if(t==null) {
                t=tm.createTopic();
                t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                t.setSubjectLocator(tm.createLocator(lctr.toExternalForm()));
            }
            return new W2TTopic(this,t);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Topic createTopicByItemIdentifier(Locator lctr) throws IdentityConstraintException, ModelConstraintException {
        // no topics have item identifiers ever so this is essentially same as just creating a new one
        return createTopic();
    }

    @Override
    public Topic createTopic() {
        try{
            org.wandora.topicmap.Topic t=tm.createTopic();
            t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
            return new W2TTopic(this,t);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }
    }

    @Override
    public Association createAssociation(Topic type, Topic... scope) throws ModelConstraintException {
        if(scope!=null && scope.length>0) throw new UnsupportedOperationException("Scope not supported in associations");
        try{
            org.wandora.topicmap.Association a=tm.createAssociation(((W2TTopic)type).getWrapped());
            return new W2TAssociation(this,a);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }

    @Override
    public Association createAssociation(Topic type, Collection<Topic> scope) throws ModelConstraintException {
        if(scope!=null && scope.size()>0) throw new UnsupportedOperationException("Scope not supported in associations");
        else return createAssociation(type);
    }

    @Override
    public void close() {
    }

    @Override
    public void mergeIn(TopicMap tm) throws ModelConstraintException {
        try{
            T2WTopicMap wrapped=new T2WTopicMap(tm);
            this.tm.mergeIn(wrapped);
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }

    @Override
    public <I extends Index> I getIndex(Class<I> type) {
        if(type.isAssignableFrom(TypeInstanceIndex.class)) return (I)(new W2TTypeInstanceIndex());
        else if(type.isAssignableFrom(ScopedIndex.class)) return (I)(new W2TScopedIndex());
        else throw new UnsupportedOperationException("Index not supported.");
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
        return this;
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
    }

    @Override
    public void clear() {
        try{
        tm.clearTopicMap();
        }catch(TopicMapException tme){
            throw new TMAPIRuntimeException(tme);
        }        
    }
    
    
    private class W2TTypeInstanceIndex implements TypeInstanceIndex {

        @Override
        public Collection<Topic> getTopics(Topic topic) {
            try{
                return wrapTopics(tm.getTopicsOfType(((W2TTopic)topic).getWrapped()));
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Topic> getTopics(Topic[] topics, boolean matchAll) {
            try{
                TopicHashSet work=new TopicHashSet();
                for(int i=0;i<topics.length;i++){
                    Collection<org.wandora.topicmap.Topic> ts=tm.getTopicsOfType(((W2TTopic)topics[i]).getWrapped());
                    if(matchAll){
                        if(i==0) work.addAll(ts);
                        else {
                            TopicHashSet test=new TopicHashSet(ts);
                            ArrayList<org.wandora.topicmap.Topic> remove=new ArrayList<org.wandora.topicmap.Topic>();
                            for(org.wandora.topicmap.Topic t : ts){
                                if(!test.contains(t)) remove.add(t);
                            }
                            ts.removeAll(remove);
                        }
                    }
                    else {
                        work.addAll(ts);
                    }
                }
                return wrapTopics(work);
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Topic> getTopicTypes() {
            try{
                // Go through all topics, not very efficient.

                TopicHashSet types=new TopicHashSet();

                Iterator<org.wandora.topicmap.Topic> iter=tm.getTopics();
                while(iter.hasNext()){
                    org.wandora.topicmap.Topic t=iter.next();
                    types.addAll(t.getTypes());
                }

                return wrapTopics(types);
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Association> getAssociations(Topic topic) {
            try{
                return wrapAssociations(tm.getAssociationsOfType(((W2TTopic)topic).getWrapped()));
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Topic> getAssociationTypes() {
            try{
                // Go through all topics, not very efficient.

                TopicHashSet types=new TopicHashSet();

                Iterator<org.wandora.topicmap.Association> iter=tm.getAssociations();
                while(iter.hasNext()){
                    org.wandora.topicmap.Association a=iter.next();
                    types.add(a.getType());
                }

                return wrapTopics(types);
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Role> getRoles(Topic topic) {
            try{
                ArrayList<Role> ret=new ArrayList<Role>();
                Collection<org.wandora.topicmap.Association> as=((W2TTopic)topic).getWrapped().getAssociationsWithRole();
                for(org.wandora.topicmap.Association a : as){
                    org.wandora.topicmap.Topic p=a.getPlayer(((W2TTopic)topic).getWrapped());
                    ret.add(new W2TRole(new W2TAssociation(W2TTopicMap.this, a), (W2TTopic)topic, new W2TTopic(W2TTopicMap.this, p)));
                }
                return ret;
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Topic> getRoleTypes() {
            try{
                // Go through all topics, not very efficient.

                TopicHashSet types=new TopicHashSet();

                Iterator<org.wandora.topicmap.Association> iter=tm.getAssociations();
                while(iter.hasNext()){
                    org.wandora.topicmap.Association a=iter.next();
                    
                    types.addAll(a.getRoles());
                }

                return wrapTopics(types);
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Occurrence> getOccurrences(Topic topic) {
            if(topic==null) return new ArrayList<Occurrence>(); // occurrences without type are not supported in Wandora so this is always empty
            try{
                ArrayList<Occurrence> ret=new ArrayList<Occurrence>();
                org.wandora.topicmap.Topic _topic=((W2TTopic)topic).getWrapped();
                Collection<org.wandora.topicmap.Topic> ts=_topic.getTopicsWithDataType();
                for(org.wandora.topicmap.Topic t : ts){
                    W2TTopic w2tt=new W2TTopic(W2TTopicMap.this,t);
                    Hashtable<org.wandora.topicmap.Topic,String> data=t.getData(_topic);
                    for(Map.Entry<org.wandora.topicmap.Topic,String> e : data.entrySet()){
                        ret.add(new W2TOccurrence(w2tt,(W2TTopic)topic,new W2TTopic(W2TTopicMap.this,e.getKey())));
                    }
                }
                return ret;
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Topic> getOccurrenceTypes() {
            try{
                // Go through all topics, not very efficient.

                TopicHashSet types=new TopicHashSet();

                Iterator<org.wandora.topicmap.Topic> iter=tm.getTopics();
                while(iter.hasNext()){
                    org.wandora.topicmap.Topic t=iter.next();
                    
                    types.addAll(t.getDataTypes());
                }

                return wrapTopics(types);
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Name> getNames(Topic topic) {
            if(topic!=null) return new ArrayList<Name>(); // all names in wandora are typeless
            else {
                try{
                    ArrayList<Name> ret=new ArrayList<Name>();
                    Iterator<org.wandora.topicmap.Topic> iter=tm.getTopics();
                    while(iter.hasNext()){
                        org.wandora.topicmap.Topic t=iter.next();
                        if(t.getBaseName()!=null) ret.add(new W2TName(W2TTopicMap.this, new W2TTopic(W2TTopicMap.this, t)));
                    }
                    return ret;
                }catch(TopicMapException tme) {
                    throw new TMAPIRuntimeException(tme);
                }
            }
        }

        @Override
        public Collection<Topic> getNameTypes() {
            // names in wandora are not typed, in the wrapper they all use the default name
            Collection<Topic> ret=new ArrayList<Topic>(); 
            ret.add(W2TTopicMap.this.getTopicBySubjectIdentifier(TOPIC_NAME_SI));
            return ret;
        }

        @Override
        public void open() {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isAutoUpdated() {
            return true;
        }

        @Override
        public void reindex() {
        }
        
    }
    
    private class W2TScopedIndex implements ScopedIndex {

        @Override
        public Collection<Association> getAssociations(Topic topic) {
            if(topic!=null) return new ArrayList<Association>();
            else return W2TTopicMap.this.getAssociations();
        }

        @Override
        public Collection<Association> getAssociations(Topic[] topics, boolean matchAll) {
            if(topics.length==0 && matchAll) return W2TTopicMap.this.getAssociations();
            else return new ArrayList<Association>();
        }

        @Override
        public Collection<Topic> getAssociationThemes() {
            return new ArrayList<Topic>();
        }

        @Override
        public Collection<Occurrence> getOccurrences(Topic topic) {
            if(topic==null) throw new UnsupportedOperationException("Not supported yet."); // should return all occurrences
            org.wandora.topicmap.Topic _topic=((W2TTopic)topic).getWrapped();
            try{
                ArrayList<Occurrence> ret=new ArrayList<Occurrence>();
                Collection<org.wandora.topicmap.Topic> ts=((W2TTopic)topic).getWrapped().getTopicsWithDataVersion();
                for(org.wandora.topicmap.Topic t : ts){
                    W2TTopic w2tt=new W2TTopic(W2TTopicMap.this, t);
                    for(org.wandora.topicmap.Topic dataType : t.getDataTypes()){
                        W2TTopic w2ttype=new W2TTopic(W2TTopicMap.this, dataType);
                        for(Map.Entry<org.wandora.topicmap.Topic,String> o : t.getData(dataType).entrySet()){
                            if(o.getKey().mergesWithTopic(_topic)) {
                                ret.add(new W2TOccurrence(w2tt, w2ttype, (W2TTopic)topic));
                            }
                        }
                    }
                }
                return ret;
            }catch(TopicMapException tme) {
                throw new TMAPIRuntimeException(tme);
            }
        }

        @Override
        public Collection<Occurrence> getOccurrences(Topic[] topics, boolean matchAll) {
            if(topics.length==0){
                if(matchAll) return getOccurrences(null); // for now this throws an exception but this should do the right thing when implemented
                else return new ArrayList<Occurrence>();
            }
            else if(topics.length==1) return getOccurrences(topics[0]);
            else { // .length>1
                if(matchAll) return new ArrayList<Occurrence>(); // no occurrence in wandora has two versions so this is always empty
                
                // assuming the topics array contains unique topics,
                // there should not be duplicates as each occurrence
                // in wandora only has a single version
                ArrayList<Occurrence> ret=new ArrayList<Occurrence>();
                for (Topic topic : topics) {
                    ret.addAll(getOccurrences(topic));
                }
                return ret;
            }
        }

        @Override
        public Collection<Topic> getOccurrenceThemes() {
            return new ArrayList<Topic>();
        }

        @Override
        public Collection<Name> getNames(Topic topic) {
            if(topic==null) throw new UnsupportedOperationException("Not supported yet."); // should return all names
            else return new ArrayList<Name>();
        }

        @Override
        public Collection<Name> getNames(Topic[] topics, boolean matchAll) {
            if(topics.length==0 && matchAll) return getNames(null);
            else return new ArrayList<Name>();
        }

        @Override
        public Collection<Topic> getNameThemes() {
            return new ArrayList<Topic>(); // names in Wandora are not scoped, only variants
        }

        @Override
        public Collection<Variant> getVariants(Topic topic) {
            if(topic==null) return getVariants(new Topic[0],false);
            else return getVariants(new Topic[]{topic},false);
        }

        @Override
        public Collection<Variant> getVariants(Topic[] topics, boolean matchAll) {
            if(topics.length==0){
                if(matchAll) throw new UnsupportedOperationException("Not supported yet."); // should return all names
                else return new ArrayList<Variant>();
            }
            else {
                if(topics.length<2 || !matchAll){
                    try{
                        Collection<org.wandora.topicmap.Topic> ts;

                        if(topics.length==1) ts=((W2TTopic)topics[0]).getWrapped().getTopicsWithVariantScope();
                        else {
                            ts=new TopicHashSet();
                            for(Topic topic : topics){
                                ts.addAll( ((W2TTopic)topic).getWrapped().getTopicsWithVariantScope() );
                            }
                        }

                        ArrayList<Variant> ret=new ArrayList<Variant>();
                        for(org.wandora.topicmap.Topic t : ts){
                            W2TTopic w2tt=new W2TTopic(W2TTopicMap.this, t);
                            W2TName name=new W2TName(W2TTopicMap.this, w2tt);
                            for(Set<org.wandora.topicmap.Topic> scope : t.getVariantScopes()){
                                ScopeLoop: for(org.wandora.topicmap.Topic scopeTopic : scope){
                                    for(Topic topic : topics){
                                        if(scopeTopic.mergesWithTopic( ((W2TTopic)topic).getWrapped() )){
                                            ret.add(new W2TVariant(name, scope));
                                            break ScopeLoop;
                                        }
                                    }
                                }
                            }
                        }
                        return ret;
                    }catch(TopicMapException tme) {
                        throw new TMAPIRuntimeException(tme);
                    }
                }
                else {
                    throw new UnsupportedOperationException("Not supported yet.");
                    // should do an intersection of calling getVariants individually on each topic
                }
            }
        }

        @Override
        public Collection<Topic> getVariantThemes() {
            throw new UnsupportedOperationException("Not supported yet."); // should return all topics used in any variant scope
        }

        @Override
        public void open() {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isAutoUpdated() {
            return true;
        }

        @Override
        public void reindex() {
        }
        
    }
}
