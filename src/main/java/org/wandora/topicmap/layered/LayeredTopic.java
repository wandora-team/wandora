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
 * LayeredTopic.java
 *
 * Created on 8. syyskuuta 2005, 11:30
 */

package org.wandora.topicmap.layered;
import static org.wandora.utils.Tuples.t2;
import static org.wandora.utils.Tuples.t3;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapReadOnlyException;
import org.wandora.utils.Delegate;
import org.wandora.utils.KeyedHashMap;
import org.wandora.utils.Tuples.T2;
import org.wandora.utils.Tuples.T3;
/**
 * <p>
 * A LayeredTopic is a collection of topics in different layers that together
 * merge with each other. Note that the collection may contain two topics
 * which do not directly merge with each other but with the addition to some
 * other topics, they all merge together. These topics are stored in an ordered
 * collection with topics of top layers first and bottom layers last. Topics of
 * same layer are ordered according to the smallest (lexicographically) subject identifier.
 * This is somewhat arbitrary but will give a consistent ordering instead of random
 * order.
 * </p><p>
 * When information is queried, the collection with all the individual topics
 * is iterated and depending on the data being queried, either the first non null
 * value is restored or all values are combined. Example of the former case is getting
 * the base name and example for the latter case is getting subject identifiers.
 * In general methods that return a collection of something combine the results
 * from all individual topics (removing duplicates) and methods that return
 * a single value return the first non null value encountered. Because topics are
 * ordered in the topic collection, information from top layers is returned
 * before information of bottom layers when only one non null value is returned.
 * </p><p>
 * Methods that modify a LayeredTopic actually modify one of the individual
 * topics. The topic to be modified is chosen to be a topic of the selected
 * layer of the layer stack. If such a topic does not exist one is generated
 * in the layer. In case there are several such topics, one of them is chosen.
 * Also the parameters to the modifying methods might not exist in the layer, 
 * in which case stubs of them are automatically generated.
 * </p>
 * @author olli
 */
public class LayeredTopic extends Topic {

    /**
     * Comparator used to order the topics LayeredTopic consists of. Topics
     * are first ordered according to the position of the layer of the topic
     * and then according to the smallest subject identifier of the topic.
     */
    class LayerOrderComparator implements Comparator<Topic>{
        private String getMinSI(Topic t){
            String min=null;
            try{
                for(Locator l : t.getSubjectIdentifiers()){
                    String s=l.toExternalForm();
                    if(min==null) min=s;
                    else if(s.compareTo(min)<0) min=s;
                }
                if(min==null) min="";
                return min;
            }catch(TopicMapException tme){
                tme.printStackTrace();
                return "";
            }            
        }
        public int compare(Topic t1,Topic t2){
            int p=layerStack.getLayer(t1).getZPos()-layerStack.getLayer(t2).getZPos();
            if(p==0) return getMinSI(t1).compareTo(getMinSI(t2));
            else return p;
        }
    }
    
    /** The topics this LayeredTopic consists of */
    protected Vector<Topic> topics;
    /** The layer stack for this topic */
    protected LayerStack layerStack;
    
    /** 
     * Creates a new instance of LayeredTopic. This layered topic will consist
     * of the given topics and them alone. Thus the layer stack must not contain
     * a topic that merges with any of the topics in the collection that isn't
     * already in the collection.
     */
    public LayeredTopic(Collection<Topic> topics,LayerStack layerStack) {
        this.layerStack=layerStack;
        this.topics=new Vector<Topic>();
        this.topics.addAll(topics);
        reorderLayers();
    }
    /** 
     * Creates a new instance of LayeredTopic. This layered topic will consist
     * of the one given topic and that alone. Thus the layer stack must not contain
     * a topic that merges with that topic.
     */
    public LayeredTopic(Topic t,LayerStack layerStack){
        this.layerStack=layerStack;
        this.topics=new Vector<Topic>();
        this.topics.add(t);
        reorderLayers();
    }
    
    /**
     * Two LayeredTopics are equal if their topics collections are equal.
     * Note that this largely depends on the equals check of the
     * individual topics.
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof LayeredTopic){
            if(hashCode!=((LayeredTopic)o).hashCode) return false;
            return topics.equals(((LayeredTopic)o).topics);
        }
        else return false;
    }
    
    // hashCode is updated whenever topics collection changes.
    private int hashCode=0;
    @Override
    public int hashCode(){
        return hashCode;
    }
    
    protected void ambiguity(String s){
        layerStack.ambiguity(s);
    }
    protected AmbiguityResolution resolveAmbiguity(String event){
        return layerStack.resolveAmbiguity(event,null);
    }
    protected AmbiguityResolution resolveAmbiguity(String event,String msg){
        return layerStack.resolveAmbiguity(event,msg);
    }
    
    /**
     * Remakes the collection of topics this layered topic consists of.
     * Will use the first topic in the topics collection as the base.
     * @see #remakeLayered(Topic)
     */
    public void remakeLayered() throws TopicMapException {
        if(topics.size()>0) remakeLayered(topics.iterator().next());
    }
    
    /**
     * Makes a set having one key for each topic in the given collection.
     * This can be used to later compare two topic collections and see if they
     * are the same.
     */
    private HashSet<String> makeKeySet(Collection<Topic> topics){
        HashSet<String> keys=new LinkedHashSet<String>();
        Delegate <String,Topic> keyMaker=layerStack.new TopicAndLayerKeyMaker();
        for(Topic t : topics){
            keys.add(keyMaker.invoke(t));
        }
        return keys;
    }
    
    /**
     * Remake this LayeredTopic using the given topic as the base. After a
     * layered topic has been modified, the topics in topics collection
     * might not all merge anymore. After base name, subject locator or
     * subject identifier is changed, the layered topic must be remade.
     * The modification may have caused a split in which case there are more
     * than one choice what the this LayeredTopic instance may become. The
     * parameter of this method is used to collect all other merging topics
     * and thus decides which of the splitted topics this instance becomes.
     * Usually there isn't any right way to choose this base topic and it is
     * chosen arbitrarily like is done in the remakeLayered method which takes
     * no parameters.
     */ 
    public void remakeLayered(Topic t) throws TopicMapException {
        HashSet<String> oldKeys=makeKeySet(topics);
        topics=new Vector<Topic>();
        topics.addAll(layerStack.collectTopics(t));
        HashSet<String> newKeys=makeKeySet(topics);
        if(!oldKeys.equals(newKeys)) {
//            layerStack.removeTopicFromIndex(getOneSubjectIdentifier());
            layerStack.topicChanged(this);
        }
        reorderLayers();
    }
    
    /**
     * Get all topics of selected layer that are part of this LayeredTopic.
     */
    public Collection<Topic> getTopicsForSelectedLayer(){
        return getTopicsForLayer(layerStack.getSelectedLayer());
    }
    
    /**
     * Get all topics of the given layer that are part of this LayeredTopic.
     */
    public Collection<Topic> getTopicsForLayer(Layer l){
        Vector<Topic> v=new Vector<Topic>();
        for(Topic t : topics){
            if(l==layerStack.getLayer(t)){
                v.add(t);
            }
        }
        return v;
    }
    
    /**
     * Get all topics this layered topic consists of in the order they appear
     * in layers. Topics of top layers will appear first in the collection.
     */
    public Collection<Topic> getTopicsForAllLayers(){
        return topics;
    }
    
    /**
     * Gets one of the topics of the selected layer that is part of this layered
     * topic or null if no such topic exists.
     */
    public Topic getTopicForSelectedLayer(){
        return getTopicForLayer(layerStack.getSelectedLayer());
    }
    
    /**
     * Gets one of the topics of given layer that is part of this layered
     * topic or null if no such topic exists.
     */
    public Topic getTopicForLayer(Layer l){
        Collection<Topic> c=getTopicsForLayer(l);
        if(c.isEmpty()) return null;
        else return c.iterator().next();
    }
    
    /**
     * Sort the topics collection. This needs to be redone in remakeLayered or
     * after visibility or order of layers has been changed.
     */
    public void reorderLayers(){
        hashCode=topics.hashCode();
        Collections.sort(topics,new LayerOrderComparator());        
    }
    
    /**
     * Returns the id of the first topic in the topics collection with layer name
     * hash code as prefix. Using layer name prefix makes sure that the returned ID is
     * unique (provided that layer topic map implementations return unique IDs).
     * Hash code is used since layer name is user specified and may contain unwanted
     * characters. Note that the ID will change based on what layers are visible.
     */
    public String getID() throws TopicMapException {
        if(topics==null || topics.isEmpty()) return null;
        Topic t=topics.get(0);
        Layer l=layerStack.getLayer(t);
        return "L"+l.getName().hashCode()+"---"+t.getID();
    }
    
    /**
     * Gets all the subject identifiers of all the topics that this layered topic
     * consists of.
     */
    @Override
    public Collection<Locator> getSubjectIdentifiers() throws TopicMapException {
        HashSet<Locator> sis = new LinkedHashSet<Locator>();
        for(Topic t : topics){
            sis.addAll(t.getSubjectIdentifiers());
        }
        return sis;
    }
    
    /**
     * Copies a stub of this topic to the given topic map. This can be used
     * to make a stub of this topic when it is needed in a layer that doesn't
     * have a topic for this layered topic.
     */
    public Topic copyStubTo(TopicMap tm) throws TopicMapException {
        Topic t=null;
        Collection<Locator> sis=getSubjectIdentifiers();
        if(!sis.isEmpty()) {
            t=tm.createTopic();
            t.addSubjectIdentifier(sis.iterator().next());
        }
        else {
            String bn=getBaseName();
            if(bn!=null && bn.length()>0){
                t=tm.createTopic();
                t.addSubjectIdentifier(sis.iterator().next());                        
            }
        }
        remakeLayered();
        return t;
    }
    
    
    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> ts=getTopicsForSelectedLayer();
        Topic t=null;
        if(ts==null || ts.isEmpty()) {
            AmbiguityResolution res=resolveAmbiguity("addSubjectIdentifier.noSelected","No topic in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                t=copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(t==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(ts.size()>1) ambiguity("Several topics in selected layer (addSubjectIdentifier");
            t=ts.iterator().next();
        }
        if(!t.getSubjectIdentifiers().contains(l)){
            t.addSubjectIdentifier(l);
            remakeLayered(t);
        }
    }
    
    
    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> ts=getTopicsForSelectedLayer();
        if(ts==null || ts.size()==0) {
            ambiguity("No topic in selected layer, nothing done (addSubjectIdentifier)");
            return;
        }
//        layerStack.removeTopicFromIndex(l);
        Topic changed=null;
        for(Topic t : ts){
            if(t.getSubjectIdentifiers().contains(l)){
                if(changed!=null){
                    ambiguity("Several topics in selected layer with subject identifier (removeSubjectIdentifier");
                    break;
                }
                else{
                    t.removeSubjectIdentifier(l);
                    changed=t;
                }
            }
        }
        if(changed!=null) remakeLayered(changed);
    }
    
    
    /**
     * Returns the layer that is being used to get the base name for this topic.
     * That is the first layer that contains a topic in this layered topic that has
     * a non null base name.
     */
    public Topic getBaseNameSource() throws TopicMapException {
        for(Topic t : topics){
            String bn=t.getBaseName();
            if(bn!=null && bn.length()>0) 
//                return layerStack.getLayer(t);
                return t;
        }
        return null;
    }
        
    
    @Override
    public String getBaseName() throws TopicMapException {
        for(Topic t : topics){
            String bn=t.getBaseName();
            if(bn!=null && bn.length()>0) return bn;
        }
        return null;
    }
    
    
    @Override
    public void setBaseName(String name) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> ts=getTopicsForSelectedLayer();
        Topic t=null;
        if(ts.isEmpty()) {
            AmbiguityResolution res=resolveAmbiguity("setBaseName.noSelected","No topic in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                t=copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(t==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(ts.size()>1) ambiguity("Several topics in selected layer (setBaseName)");
            t=ts.iterator().next();
        }
        t.setBaseName(name);
        remakeLayered(t);
    }
    
    
    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        Vector<Topic> v=new Vector<Topic>();
        for(Topic t : topics){
            v.addAll(t.getTypes());
        }
        return layerStack.makeLayeredTopics(v);
    }
    
    
    @Override
    public void addType(Topic type) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        LayeredTopic lt=(LayeredTopic)type;
        Collection<Topic> types=lt.getTopicsForSelectedLayer();
        Topic stype=null;
        if(types.isEmpty()){
            AmbiguityResolution res=resolveAmbiguity("addType.type.noSelected","No type in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                stype=lt.copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(stype==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(types.size()>1) ambiguity("Several types in selected layer (addType)");
            stype=types.iterator().next();
        }
        Collection<Topic> ts=getTopicsForSelectedLayer();
        Topic t=null;
        if(ts.isEmpty()){
            AmbiguityResolution res=resolveAmbiguity("addType.topic.noSelected","No topic in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                t=copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(t==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(ts.size()>1) ambiguity("Several topics in selected layer (addType)");
            t=ts.iterator().next();
        }
        t.addType(stype);
    }
    
    
    @Override
    public void removeType(Topic type) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        LayeredTopic lt=(LayeredTopic)type;
        Collection<Topic> types=lt.getTopicsForSelectedLayer();
        if(types.isEmpty()){
            ambiguity("No type in selected layer, nothing done (removeType)");
            return;
        }
        else if(types.size()>1) ambiguity("Several types in selected layer (removeType)");
        Collection<Topic> ts=getTopicsForSelectedLayer();
        if(ts.isEmpty()){
            ambiguity("No topic in selected layer, nothing done (removeType)");
            return;
        }
        else if(ts.size()>1) ambiguity("Several topics in selected layer (removeType)");
        ts.iterator().next().removeType(types.iterator().next());
    }
    
    
    @Override
    public boolean isOfType(Topic type) throws TopicMapException {
        LayeredTopic lt=(LayeredTopic)type;
        for(Topic t : topics){
            Layer l=layerStack.getLayer(t);
            for(Topic t2 : lt.topics){
                if(layerStack.getLayer(t2)==l){
                    if(t.isOfType(t2)) return true;
                }
            }
        }
        return false;
    }

    
    /**
     * Creates a scope from a collection of LayeredTopics that can be used for the
     * given layer. Note that the given layer might not contain all topics needed
     * for the scope in which case this will return null.
     */
    protected Set<Topic> createScope(Set<Topic> layeredScope,Layer l) throws TopicMapException {
        return createScope(layeredScope,l,false);
    }
    
    
    /**
     * Creates a scope from a collection of LayeredTopics that can be used for the
     * given layer. Note that the given layer might not contain all topics needed
     * for the scope. If copyTopics is true then stubs of these topics are created
     * in the layer, otherwise null is returned if any of the needed topics isn't
     * found in the layer.
     */
    protected Set<Topic> createScope(Set<Topic> layeredScope,Layer l,boolean copyTopics) throws TopicMapException {
        // TODO: doesn't handle correctly theoretical case where scope topics get merged
        HashSet<Topic> ret=new LinkedHashSet<Topic>();
        for(Topic t : layeredScope){
            LayeredTopic lt=(LayeredTopic)t;
            Collection<Topic> ts=lt.getTopicsForLayer(l);
            Topic t2=null;
            if(ts.isEmpty()){
                if(copyTopics){
                    t2=lt.copyStubTo(l.getTopicMap());
                    if(t2==null) {
                        ambiguity("Cannot copy topic to selected layer");
                        return null;
                    }
                }
                else {
                    ambiguity("No topic in layer (createScope)");
                    return null;
                }
            }
            else {
                if(ts.size()>1) ambiguity("Several topics in layer (createScope)");
                t2=ts.iterator().next();
            }
            ret.add(t2);
        }
        return ret;
    }
    

    /**
     * Tries to find a variant scope in the given (non layered) topic that matches
     * the scope consisting of LayeredTopics. If such a scope doesn't exist null
     * is returned. In case there are several of such scopes, one of them is returned
     * chosen arbitrarily.
     */
    public Set<Topic> getScopeOfLayeredScope(Topic t, Set<Topic> layeredScope) throws TopicMapException {
        Layer l =layerStack.getLayer(t);
        Set<Set<Topic>> scopes=t.getVariantScopes();
        LoopA: for(Set<Topic> s : scopes){
            HashSet<Topic> used=new LinkedHashSet<Topic>();
            LoopB: for(Topic st : s ){
                // Note that st cannot belong to several LayeredTopics in scope
                // because otherwise those multiple LayeredTopics would be merged and be the same topic
                for(Topic lst : layeredScope){
                    LayeredTopic lt=(LayeredTopic)lst;
                    // TODO: depends on equals check
                    if(lt != null && lt.topics != null && lt.topics.contains(st)){
                        used.add(lst);
                        continue LoopB; // check rest of the topics in s
                    }
                }
                continue LoopA; // st didn't belong to any topic in scope, test next scope
            }
            // all in s belong to some LayeredTopic in scope, now test if all LayeredTopics
            // in scope have something in s
            if(used.size()!=layeredScope.size()) continue;

            // found matching scope (there might be several matching scopes)
            return s;
        }
        return null;
    }
    
    
    /**
     * Returns the layer that is used to get the variant for the given scope.
     */
    public T2<Topic,Set<Topic>> getVariantSource(Set<Topic> scope) throws TopicMapException {
        for(Topic t : topics){
            Set<Topic> s=getScopeOfLayeredScope(t,scope);
            if(s!=null) {
                return t2(t,scope);
//                return layerStack.getLayer(t);
            }
        }
        return null;
    }
    
    
    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        String ret=null;
        for(Topic t : topics){
            Set<Topic> s=getScopeOfLayeredScope(t,scope);
            if(s!=null) {
                String val=t.getVariant(s);
                if(ret!=null && !ret.equals(val)){
                    ambiguity("Several matching variants (getVariant)");
                    return ret;
                }
                ret=val;
            }
        }
        return ret;
    }
    
    
    @Override
    public void setVariant(Set<Topic> scope,String name) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> ts=getTopicsForSelectedLayer();
        Topic selectedTopic=null;
        if(ts.isEmpty()){
            AmbiguityResolution res=resolveAmbiguity("setVariant.topic.noSelected","No topic in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                selectedTopic=copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(selectedTopic==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(ts.size()>1) ambiguity("Several topics in selected layer (setVariant)");
            selectedTopic=ts.iterator().next();
        }
        Set<Topic> s=getScopeOfLayeredScope(selectedTopic,scope);
        if(s==null) {
            s=createScope(scope, layerStack.getSelectedLayer(),false);
            if(s==null) {
                AmbiguityResolution res=resolveAmbiguity("setVariant.scope.noSelected","Topics in scope not in selected layer");
                if(res==AmbiguityResolution.addToSelected){
                    s=createScope(scope,layerStack.getSelectedLayer(),true);
                    if(s==null){
                        ambiguity("Cannot copy scope to selected layer");
                        return;
                    }
                }
                else throw new RuntimeException("Not implemented");
            }
        }
        selectedTopic.setVariant(s,name);
    }
    
    
    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        // TODO: doesn't handle correctly theoretical case where scope topics get merged
        Set<Set<Topic>> ret=new LinkedHashSet<Set<Topic>>();
        HashMap<Topic,LayeredTopic> collectedMap=new HashMap<Topic,LayeredTopic>();
        for(Topic t : topics){
            Set<Set<Topic>> scopes=t.getVariantScopes();
            for(Set<Topic> scope : scopes){
                HashSet<Topic> layeredScope=new LinkedHashSet<Topic>();
                for(Topic st : scope){
                    LayeredTopic lt=collectedMap.get(st);
                    if(lt==null) {
                        Collection<Topic> collected=layerStack.collectTopics(st);
                        lt=new LayeredTopic(collected,layerStack);
                        for(Topic ct : collected) collectedMap.put(ct,lt);
                    }
                    layeredScope.add(lt);
                }
                ret.add(layeredScope);
            }
        }
        return ret;
    }
    
    
    @Override
    public void removeVariant(Set<Topic> scope) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        // TODO: if multiple, remove all or one? in none ask?
        boolean removed=false;
        for(Topic selectedTopic : getTopicsForSelectedLayer()){
            Set<Topic> s=getScopeOfLayeredScope(selectedTopic,scope);
            if(s!=null){
                if(removed){
                    ambiguity("Several variants in selected layer (removeVariant)");
                    return;
                }
                selectedTopic.removeVariant(s);
                removed=true;
            }
        }
    }
    
    
    /**
     * Returns the topic that is used to get data with specified type and version and
     * the type and version topics for the source topic layer.
     */
    public T3<Topic,Topic,Topic> getDataSource(Topic type,Topic version) throws TopicMapException {
        LayeredTopic lt=(LayeredTopic)type;
        LayeredTopic lv=(LayeredTopic)version;
        
        String found=null;
        for(Topic t : topics){
            Layer l=layerStack.getLayer(t);
            for(Topic st : lt.getTopicsForLayer(l)){
                for(Topic sv : lv.getTopicsForLayer(l)){
                    String data=t.getData(st,sv);
                    if(data!=null && data.length()>0) {
                        return t3(t,st,sv);
//                        return layerStack.getLayer(t);
                    }
                }
            }
        }
        return null;
    }
    
    
    @Override
    public String getData(Topic type,Topic version) throws TopicMapException {
        LayeredTopic lt=(LayeredTopic)type;
        LayeredTopic lv=(LayeredTopic)version;
        
        String found=null;
        for(Topic t : topics){
            Layer l=layerStack.getLayer(t);
            for(Topic st : lt.getTopicsForLayer(l)){
                for(Topic sv : lv.getTopicsForLayer(l)){
                    String data=t.getData(st,sv);
                    if(data!=null && data.length()>0) {
                        if(found!=null && !found.equals(data)){
                            ambiguity("Several data versions in layer (getData)");
                            return found;
                        }
                        found=data;
                    }
                }
            }
        }
        return found;
    }
    
    
    @Override
    public Hashtable<Topic,String> getData(Topic type) throws TopicMapException {
        LayeredTopic lt=(LayeredTopic)type;
        Hashtable<Topic,String> ret=new Hashtable<Topic,String>();
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        
        for(Topic t : topics){
            for(Topic st : lt.getTopicsForLayer(layerStack.getLayer(t))){
                Hashtable<Topic,String> data=t.getData(st);
                if(data!=null && data.size()>0){
                    for(Map.Entry<Topic,String> e : data.entrySet()){
                        LayeredTopic lversion=layerStack.getLayeredTopic(e.getKey(), layeredTopics);
                        String val=e.getValue();
                        String old=ret.put(lversion,val);
                        if(old!=null && !old.equals(val)){
                            ambiguity("Several data for given type and version (getData)");
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    
    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        HashSet<Topic> used=new LinkedHashSet<Topic>();
        HashSet<Topic> ret=new LinkedHashSet<Topic>();
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        
        for(Topic t : topics){
            for(Topic dt : t.getDataTypes()){
                if(used.contains(dt)) continue;
                LayeredTopic lt=layerStack.getLayeredTopic(dt,layeredTopics);
                ret.add(lt);
            }
        }
        return ret;
        
    }
    
    
    @Override
    public void setData(Topic type,Hashtable<Topic,String> versionData) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        for(Map.Entry<Topic,String> e : versionData.entrySet()){
            setData(type,e.getKey(),e.getValue());
        }
    }
    
    
    @Override
    public void setData(Topic type,Topic version,String value) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        LayeredTopic lt=(LayeredTopic)type;
        LayeredTopic lv=(LayeredTopic)version;
        Collection<Topic> ts=getTopicsForSelectedLayer();
        Topic t=null;
        if(ts.isEmpty()){
            AmbiguityResolution res=resolveAmbiguity("setData.topic.noSelected","No topic in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                t=copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(t==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(ts.size()>1) ambiguity("Several topics in selected layer (setData)");
            t=ts.iterator().next();
        }
        Collection<Topic> ltype=lt.getTopicsForSelectedLayer();
        Topic stype=null;
        if(ltype.isEmpty()){
            AmbiguityResolution res=resolveAmbiguity("setData.type.noSelected","No type in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                stype=lt.copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(stype==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(ltype.size()>1) ambiguity("Several types in selected layer (setData)");
            stype=ltype.iterator().next();
        }
        Collection<Topic> lversion=lv.getTopicsForSelectedLayer();
        Topic sversion=null;
        if(lversion.isEmpty()){
            AmbiguityResolution res=resolveAmbiguity("setData.version.noSelected","No version in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                sversion=lv.copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(sversion==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(lversion.size()>1) ambiguity("Several versions in selected layer (setData)");
            sversion=lversion.iterator().next();
        }
        t.setData(stype,sversion,value);
    }
    
    
    @Override
    public void removeData(Topic type,Topic version) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        LayeredTopic lt=(LayeredTopic)type;
        LayeredTopic lv=(LayeredTopic)version;
        boolean removed=false;
        for(Topic selectedTopic : getTopicsForSelectedLayer()){
            for(Topic st : lt.getTopicsForSelectedLayer()){
                for(Topic sv : lv.getTopicsForSelectedLayer()){
                    String d=selectedTopic.getData(st,sv);
                    if(d!=null && d.length()>0) {
                        if(removed){
                            ambiguity("Several type and version matches (removeData)");
                            return;
                        }
                        selectedTopic.removeData(st,sv);
                        removed=true;
                    }
                }
            }
        }
    }
    
    
    @Override
    public void removeData(Topic type) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        LayeredTopic lt=(LayeredTopic)type;
        boolean removed=false;
        for(Topic selectedTopic : getTopicsForSelectedLayer()){
            for(Topic st : lt.getTopicsForSelectedLayer()){
                Hashtable<Topic,String> ht=selectedTopic.getData(st);
                if(ht!=null && !ht.isEmpty()){
                    if(removed){
                        ambiguity("several type matches (removeData)");
                        return;
                    }
                    selectedTopic.removeData(st);
                    removed=true;
                }
            }
        }        
    }
    
    
    public Topic getSubjectLocatorSource() throws TopicMapException {
        for(Topic t : topics){
            Locator l=t.getSubjectLocator();
            if(l!=null){
//                return layerStack.getLayer(t);
                return t;
            }
        }
        return null;
    }
    
    
    @Override
    public Locator getSubjectLocator() throws TopicMapException {
        Locator ret=null;
        for(Topic t : topics){
            Locator l=t.getSubjectLocator();
            if(l!=null) {
                if(ret!=null && !ret.equals(l)){
                    ambiguity("Several locators (getSubjectLocator)");
                    return ret;
                }
                ret=l;
            }
        }
        return ret;
    }
    
    
    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> ts=getTopicsForSelectedLayer();
        Topic t=null;
        if(ts.isEmpty()) {
            AmbiguityResolution res=resolveAmbiguity("setSubjectLocator.noSelected","No topic in selected layer");
            if(res==AmbiguityResolution.addToSelected){
                t=copyStubTo(layerStack.getSelectedLayer().getTopicMap());
                if(t==null){
                    ambiguity("Cannot copy topic to selected layer");
                    return;
                }
            }
            else throw new RuntimeException("Not implemented");
        }
        else {
            if(ts.size()>1) ambiguity("Several topics in selected layer (setSubjectLocator");
            t=ts.iterator().next();
        }
        if(t.getSubjectLocator()==null || !t.getSubjectLocator().equals(l)){
            t.setSubjectLocator(l);
            remakeLayered(t);
        }
    }
    
    
    @Override
    public TopicMap getTopicMap(){
        return layerStack;
    }
    
    
    public LayerStack getLayerStack(){
        return layerStack;
    }
    
    
    /**
     * Adds LayeredAssociations into associations set based on the possible players
     * for each role given in the players map. When converting individual associations
     * into LayeredAssociations, each role may have several possible
     * players because roles may have been merged. This method is used to create
     * LayeredAssociations with all possible player assignments. The getAssociations
     * methods construct the players map and call this method with chosen and roles
     * parameters as null and an empty associations set. When the method returns associations 
     * set will contain all possible LayeredAssociations.
     *
     * The method works recursively by choosing one player for each role at a time.
     * The chosen players are stored in the chosen Vector. When one player has
     * been chosen for each player, a LayeredAssociation is created and added to
     * the set which is later returned.
     *
     */
    private void _addAssociations(HashSet<Association> associations,KeyedHashMap<LayeredTopic,Vector<LayeredTopic>> players,Vector<LayeredTopic> roles,Vector<LayeredTopic> chosen,LayeredTopic type) throws TopicMapException{
        if(chosen==null) chosen=new Vector<LayeredTopic>();
        if(roles==null){
            roles=new Vector<LayeredTopic>();
            roles.addAll(players.keySet());
        }
        if(chosen.size()==roles.size()){
            LayeredAssociation la=new LayeredAssociation(layerStack,type);
            int counter=0;
            for(LayeredTopic role : roles){
                la.addLayeredPlayer(chosen.elementAt(counter++),role);
            }
            associations.add(la);
        }
        else{
            int chosenSize=chosen.size();
            Topic role=roles.elementAt(chosenSize);
            Vector<LayeredTopic> ps=players.get(role);
            for(LayeredTopic p : ps){
                while(chosen.size()>chosenSize){
                    chosen.remove(chosen.size()-1);
                }
                chosen.add(p);
                _addAssociations(associations,players,roles,chosen,type);
            }
        }
    }
    
    
    /**
     * Returns associations of this topic. Note that because roles may get
     * merged, one individual association may become multiple LayeredAssociations.
     */
    @Override
    public Collection<Association> getAssociations() throws TopicMapException {
        HashSet<Association> associations=new LinkedHashSet<Association>();

        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        for(Topic t : topics){
            Collection<Association> c=t.getAssociations();
            for(Association a : c ){
                LayeredTopic lt=layerStack.getLayeredTopic(a.getType(),layeredTopics);
//                LayeredAssociation la=new LayeredAssociation(layerStack,lt);
                Collection<Topic> roles=a.getRoles();
                KeyedHashMap<LayeredTopic,Vector<LayeredTopic>> players=new KeyedHashMap<LayeredTopic,Vector<LayeredTopic>>(new TopicKeyMaker());
                for(Topic role : roles){
                    LayeredTopic lrole=layerStack.getLayeredTopic(role,layeredTopics);
                    Topic player=a.getPlayer(role);
                    LayeredTopic lplayer=layerStack.getLayeredTopic(player,layeredTopics);

                    Vector<LayeredTopic> ps=players.get(lrole);
                    if(ps==null){
                        ps=new Vector<LayeredTopic>();
                        players.put(lrole,ps);
                    }
                    ps.add(lplayer);

//                    la.addLayeredPlayer(lplayer,lrole);
                }
//                associations.add(la);
                _addAssociations(associations,players,null,null,lt);
            }
        }        
        return associations;
    }
    
    
    
    /**
     * See notes in getAssociations().
     */
    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        HashSet<Association> associations=new LinkedHashSet<Association>();
        LayeredTopic lt=(LayeredTopic)type;
        for(Topic t : topics){
            for(Topic st : lt.getTopicsForLayer(layerStack.getLayer(t))){
                Collection<Association> c=t.getAssociations(st);
                for(Association a : c ){
//                    LayeredAssociation la=new LayeredAssociation(layerStack,lt);
                    Collection<Topic> roles=a.getRoles();
                    KeyedHashMap<LayeredTopic,Vector<LayeredTopic>> players=new KeyedHashMap<LayeredTopic,Vector<LayeredTopic>>(new TopicKeyMaker());
                    for(Topic role : roles){
                        LayeredTopic lrole=layerStack.getLayeredTopic(role,layeredTopics);
                        Topic player=a.getPlayer(role);
                        LayeredTopic lplayer=layerStack.getLayeredTopic(player,layeredTopics);
                    
                        Vector<LayeredTopic> ps=players.get(lrole);
                        if(ps==null){
                            ps=new Vector<LayeredTopic>();
                            players.put(lrole,ps);
                        }
                        ps.add(lplayer);

//                        la.addLayeredPlayer(lplayer,lrole);
                    }
//                    associations.add(la);
                    _addAssociations(associations,players,null,null,lt);
                }
            }
        }        
        return associations;
    }
    
    
    /**
     * See notes in getAssociations().
     */
    @Override
    public Collection<Association> getAssociations(Topic type,Topic role) throws TopicMapException {
        LayeredTopic lt=(LayeredTopic)type;
        LayeredTopic lr=(LayeredTopic)role;
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        HashSet<Association> associations=new LinkedHashSet<Association>();
        for(Topic t : topics){
            for(Topic st : lt.getTopicsForLayer(layerStack.getLayer(t))){
                for(Topic sr : lr.getTopicsForLayer(layerStack.getLayer(t))){
                    Collection<Association> c=t.getAssociations(st,sr);
                    for(Association a : c ){
//                        LayeredAssociation la=new LayeredAssociation(layerStack,lt);
                        Collection<Topic> roles=a.getRoles();
                        KeyedHashMap<LayeredTopic,Vector<LayeredTopic>> players=new KeyedHashMap<LayeredTopic,Vector<LayeredTopic>>(new TopicKeyMaker());
                        for(Topic arole : roles){
                            LayeredTopic lrole=layerStack.getLayeredTopic(arole,layeredTopics);
                            Topic player=a.getPlayer(arole);
                            LayeredTopic lplayer=layerStack.getLayeredTopic(player,layeredTopics);
                    
                            Vector<LayeredTopic> ps=players.get(lrole);
                            if(ps==null){
                                ps=new Vector<LayeredTopic>();
                                players.put(lrole,ps);
                            }
                            ps.add(lplayer);

//                            la.addLayeredPlayer(lplayer,lrole);
                        }
//                        associations.add(la);
                        _addAssociations(associations,players,null,null,lt);
                    }
                }
            }
        }        
        return associations;
    }
    
    
    @Override
    public void remove() throws TopicMapException {
        if(layerStack.isReadOnly()) throw new TopicMapReadOnlyException();
        Collection<Topic> ts=getTopicsForSelectedLayer();
        if(ts.isEmpty()){
            ambiguity("no topic in selected layer (remove)");
            return;
        }
        else if(ts.size()>1) ambiguity("several topics in selected layer (remove)");
//        layerStack.removeTopicFromIndex(getOneSubjectIdentifier());
        ts.iterator().next().remove();
    }
    
    
    @Override
    public long getEditTime() throws TopicMapException {
        long max=-1;
        for(Topic t : topics){
            long time=t.getEditTime();
            if(time>max) max=time;
        }
        return max;
    }
    
    
    @Override
    public void setEditTime(long time) throws TopicMapException {
        // TODO: edit time of what?
        for(Topic t : getTopicsForSelectedLayer()){
            t.setEditTime(time);
        }
    }
    
    
    @Override
    public long getDependentEditTime() throws TopicMapException {
        long max=-1;
        for(Topic t : topics){
            long time=t.getDependentEditTime();
            if(time>max) max=time;
        }
        return max;        
    }
    
    
    @Override
    public void setDependentEditTime(long time) throws TopicMapException {
        // TODO: edit time of what?
        for(Topic t : getTopicsForSelectedLayer()){
            t.setDependentEditTime(time);
        }        
    }
    
    
    @Override
    public boolean isRemoved() throws TopicMapException {
        // TODO: how should this work?
        if(topics.isEmpty()) return true;
        Collection<Topic> ts=getTopicsForSelectedLayer();
        if(ts.isEmpty()){
            ambiguity("no topic in selected layer (isRemoved)");
            return false;
        }
        else if(ts.size()>1) ambiguity("several topics in selected layer (isRemoved)");
        return ts.iterator().next().isRemoved();
    }
    
    
    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        Collection<Topic> ts=getTopicsForSelectedLayer();
        if(ts.isEmpty()){
            ambiguity("no topic in selected layer (isDeleteAllowed)");
            return false;
        }
        else if(ts.size()>1) ambiguity("several topics in selected layer (isDeleteAllowed)");
        return ts.iterator().next().isDeleteAllowed();
    }
    
    
    @Override
    public Collection<Topic> getTopicsWithDataType() throws TopicMapException {
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        HashSet<Topic> ret=new LinkedHashSet<Topic>();
        for(Topic t : topics){
            Collection<Topic> c=t.getTopicsWithDataType();
            for(Topic to : c){
                LayeredTopic lt=layerStack.getLayeredTopic(to,layeredTopics);
                ret.add(lt);
            }
        }
        return ret;
    }
    
    
    @Override
    public Collection<Association> getAssociationsWithType() throws TopicMapException {
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        HashSet<Association> associations=new LinkedHashSet<Association>();
        for(Topic t : topics){
            Collection<Association> c=t.getAssociationsWithType();
            for(Association a : c ){
                LayeredTopic lt=layerStack.getLayeredTopic(a.getType(),layeredTopics);
                LayeredAssociation la=new LayeredAssociation(layerStack,lt);
                Collection<Topic> roles=a.getRoles();
                for(Topic role : roles){
                    LayeredTopic lrole=layerStack.getLayeredTopic(role,layeredTopics);
                    Topic player=a.getPlayer(role);
                    LayeredTopic lplayer=layerStack.getLayeredTopic(player,layeredTopics);
                    la.addLayeredPlayer(lplayer,lrole);
                }
                associations.add(la);
            }
        }        
        return associations;
    }
    
    
    @Override
    public Collection<Association> getAssociationsWithRole() throws TopicMapException {
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        HashSet<Association> associations=new LinkedHashSet<Association>();
        for(Topic t : topics){
            Collection<Association> c=t.getAssociationsWithRole();
            for(Association a : c ){
                LayeredTopic lt=layerStack.getLayeredTopic(a.getType(),layeredTopics);
                LayeredAssociation la=new LayeredAssociation(layerStack,lt);
                Collection<Topic> roles=a.getRoles();
                for(Topic role : roles){
                    LayeredTopic lrole=layerStack.getLayeredTopic(role,layeredTopics);
                    Topic player=a.getPlayer(role);
                    LayeredTopic lplayer=layerStack.getLayeredTopic(player,layeredTopics);
                    la.addLayeredPlayer(lplayer,lrole);
                }
                associations.add(la);
            }
        }        
        return associations;
    }

    
    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        HashSet<Topic> ret=new LinkedHashSet<Topic>();
        for(Topic t : topics){
            Collection<Topic> c=t.getTopicsWithDataVersion();
            for(Topic to : c){
                LayeredTopic lt=layerStack.getLayeredTopic(to,layeredTopics);
                ret.add(lt);
            }
        }
        return ret;
    }
    

    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        KeyedHashMap<Topic,LayeredTopic> layeredTopics=new KeyedHashMap<Topic,LayeredTopic>(layerStack.new TopicAndLayerKeyMaker());
        HashSet<Topic> ret=new LinkedHashSet<Topic>();
        for(Topic t : topics){
            Collection<Topic> c=t.getTopicsWithVariantScope();
            for(Topic to : c){
                LayeredTopic lt=layerStack.getLayeredTopic(to,layeredTopics);
                ret.add(lt);
            }
        }
        return ret;
    }
   
    
    @Override
    public boolean mergesWithTopic(Topic topic) throws TopicMapException {
        if(topic == null) return false;
        for(Topic t : topics){
            if(t.getBaseName()!=null && topic.getBaseName()!=null && t.getBaseName().equals(topic.getBaseName())) 
                return true;
            for(Locator l : t.getSubjectIdentifiers()) {
                if(topic.getSubjectIdentifiers().contains(l)) return true;
            }
            if(t.getSubjectLocator()!=null && topic.getSubjectLocator()!=null && t.getSubjectLocator().equals(topic.getSubjectLocator())) 
                return true;            
        }
        return false;
    }
    
    
    public static class TopicKeyMaker implements Delegate<String,LayeredTopic> {
        public String invoke(LayeredTopic t){
            String min=null;
            try{
                for(Locator l : t.getSubjectIdentifiers()) {
                    String s=l.toExternalForm();
                    if(min==null) min=s;
                    else if(s.compareTo(min)<0) min=s;
                }
            }
            catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION
            }
            return min;
        }
    }
}


