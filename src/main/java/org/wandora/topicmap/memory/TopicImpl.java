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
 * TopicImpl.java
 *
 * Created on June 10, 2004, 11:30 AM
 */

package org.wandora.topicmap.memory;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicInUseException;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapReadOnlyException;
import org.wandora.topicmap.TopicRemovedException;
import org.wandora.utils.Tuples;
import org.wandora.utils.Tuples.T2;


/**
 *
 * @author  olli
 */
public class TopicImpl extends Topic {
    
    private TopicMapImpl topicMap;
    
    private Map<Topic,Map<Topic,String>> data;
    private Set<Topic> types;
    private Set<Association> associations;
    private Map<Topic,Map<Topic,Collection<Association>>> associationIndex;
    private String baseName;
    private Locator subjectLocator;
    private Set<Locator> subjectIdentifiers;
    private Map<Set<Topic>,String> variants;
    
    private Set<Topic> dataTypeIndex;
    private Set<DataVersionIndexWrapper> dataVersionIndex;
    private Set<Topic> topicTypeIndex;
    private Set<Association> associationTypeIndex;
    private Set<Association> roleTypeIndex;
    private Set<Topic> variantScopeIndex;
    
    private Set<Topic> dependentTopics;
    
    private String id;
    
    private boolean removed;
    private boolean denyRemoveIfCoreTopic = true;
    
    private long editTime;
    private long dependentEditTime;
    
    private Map<String,String> dispNameCache;
    private Map<String,String> sortNameCache;
    
    
    /** Creates a new instance of TopicImpl */
    public TopicImpl(String id, TopicMapImpl topicMap) {
        this.topicMap=topicMap;
        this.id=id;
        initializeTopicImpl();
    }
    
    
    /** Creates a new instance of TopicImpl */
    public TopicImpl(TopicMapImpl topicMap) {
        this.topicMap=topicMap;
        id=getUniqueID();
        initializeTopicImpl();
    }
    
    
    private void initializeTopicImpl() {
        data=Collections.synchronizedMap(new LinkedHashMap());
        types=Collections.synchronizedSet(new LinkedHashSet());
        associations=Collections.synchronizedSet(new LinkedHashSet());
        associationIndex=Collections.synchronizedMap(new LinkedHashMap());
        baseName=null;
        subjectLocator=null;
        subjectIdentifiers=Collections.synchronizedSet(new LinkedHashSet());
        variants=Collections.synchronizedMap(new LinkedHashMap());
        
        dispNameCache=Collections.synchronizedMap(new LinkedHashMap());
        sortNameCache=Collections.synchronizedMap(new LinkedHashMap());
        
        dataTypeIndex=Collections.synchronizedSet(new LinkedHashSet());
        dataVersionIndex=Collections.synchronizedSet(new LinkedHashSet());
        topicTypeIndex=Collections.synchronizedSet(new LinkedHashSet());
        associationTypeIndex=Collections.synchronizedSet(new LinkedHashSet());
        roleTypeIndex=Collections.synchronizedSet(new LinkedHashSet());
        variantScopeIndex=Collections.synchronizedSet(new LinkedHashSet());
        
        removed=false;
    }
    
    
    
    public void clearNameCaches(){
        dispNameCache.clear();
        sortNameCache.clear();
    }
    
    
    @Override
    public String getDisplayName(String lang) throws TopicMapException {
        if(dispNameCache.containsKey(lang)) return dispNameCache.get(lang);
        String name=super.getDisplayName(lang);
        dispNameCache.put(lang,name);
        return name;
    }
    
    
    @Override
    public String getSortName(String lang) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        if(sortNameCache.containsKey(lang)) return sortNameCache.get(lang);
        String name=super.getSortName(lang);
        sortNameCache.put(lang,name);
        return name;
    }
    
    
    private static long idcounter=0;
    public static synchronized String getUniqueID(){
        return "topic"+(idcounter++)+"."+System.currentTimeMillis();
    }
    
    
    @Override
    public String getID() throws TopicMapException {
        return id;
    }
    
    
    @Override
    public void setData(Topic type, Topic version, String value) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        if(removed) throw new TopicRemovedException();
        if(value==null) {
            System.out.println("WRN setData called with null value, redirecting to removeData");
            String old=getData(type,version);
            removeData(type,version); 
            topicMap.topicDataChanged(this,type,version,null,old);
            return;
        }
        ((TopicImpl)type).addedAsDataType(this);
        ((TopicImpl)version).addedAsDataVersion(this,type);
        Map<Topic,String> t=data.get(type);
        if(t==null){
            t=new LinkedHashMap();
            data.put(type,t);
        }
        Object o=t.put(version,value);
        boolean changed=( o==null || !o.equals(value) );
        dependentTopics=null;
        updateEditTime();
        if(changed) {
            topicMap.topicDataChanged(this,type,version,value,(String)o);
        }
    }
    
    
    @Override
    public void setData(Topic type, Hashtable<Topic,String> versionData) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        ((TopicImpl)type).addedAsDataType(this);
        Map<Topic,String> t=data.get(type);
        if(t==null){
            t=new LinkedHashMap();
            data.put(type,t);
        }
        Iterator iter=versionData.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            Topic version=(Topic)e.getKey();
            String value=(String)e.getValue();
            if(value==null) throw new NullPointerException("Cannot set null data.");
            ((TopicImpl)version).addedAsDataVersion(this,type);
            Object o=t.put(version,value);
            boolean changed=( o==null || !o.equals(value) );
            if(changed) {
                topicMap.topicDataChanged(this,type,version,value,(String)o);
            }
        }
        dependentTopics=null;
        updateEditTime();
    }
    
    
    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        Topic t=topicMap.getTopic(l);
        boolean changed=false;
        if(t!=null && t!=this) {
            mergeIn(t);
        }
        else {
            topicMap.addTopicSubjectIdentifier(this,l);
            changed=subjectIdentifiers.add(l);
        }
        updateEditTime();
        if(changed) {
            topicMap.topicSubjectIdentifierChanged(this,l,null);
        }
    }
    
    
    @Override
    public void addType(Topic t) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        ((TopicImpl)t).addedAsTopicType(this);
        topicMap.addTopicType(this,t);
        boolean changed=types.add(t);
        dependentTopics=null;
        updateEditTime();
        if(changed) {
            topicMap.topicTypeChanged(this,t,null);
        }        
    }
    
    
    @Override
    public Collection<Association> getAssociations()  throws TopicMapException {
        return associations;
    }
    
    
    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        Map<Topic,Collection<Association>> s = associationIndex.get(type);
        if(s==null) {
            return new HashSet();
        }
        else {
            Set as = Collections.synchronizedSet(new LinkedHashSet());
            for(Topic role : s.keySet()) {
                as.addAll(s.get(role));
            }
            return as;
        }
    }
    
    
    @Override
    public Collection<Association> getAssociations(Topic type, Topic role) throws TopicMapException {
        Map<Topic,Collection<Association>> s = associationIndex.get(type);
        if(s==null) {
            return new HashSet();
        }
        else {
            Collection<Association> s2 = s.get(role);
            if(s2==null) {
                return new HashSet();
            }
            else {
                return s2;
            }
        }
    }
    
    
    @Override
    public String getBaseName() throws TopicMapException {
        return baseName;
    }
    
    
    @Override
    public String getData(Topic type, Topic version) throws TopicMapException {
        Map<Topic,String> t=data.get(type);
        if(t==null) return null;
        else return t.get(version);
    }
    
    
    @Override
    public Hashtable getData(Topic type) throws TopicMapException {
        Map<Topic,String> t = data.get(type);
        if(t==null) {
            return new Hashtable();
        }
        else {
            Hashtable<Topic,String> ht = new Hashtable();
            ht.putAll(t);
            return ht;
        }
    }
    
    
    @Override
    public Locator getSubjectLocator() throws TopicMapException {
        return subjectLocator;
    }
    
    
    @Override
    public Collection getSubjectIdentifiers() throws TopicMapException {
        return subjectIdentifiers;
    }
    
    
    @Override
    public TopicMap getTopicMap(){
        return topicMap;
    }
    
    
    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        return types;
    }
    
    
    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        return (String)variants.get(scope);
    }
    
    
    @Override
    public boolean isOfType(Topic t) throws TopicMapException {
        return types.contains(t);
    }
    
    
    @Override
    public void removeData(Topic type) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        Map<Topic,String> t = data.remove(type);
        ((TopicImpl)type).removedFromDataType(this);
        if(t != null) {
            for(Topic version : t.keySet()) {
                ((TopicImpl)version).removedFromDataVersion(this,type);
                topicMap.topicDataChanged(this, type, version, null, t.get(version));
            }
        }
        dependentTopics=null;
        updateEditTime();
    }
    
    
    @Override
    public void removeData(Topic type, Topic version) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        Map<Topic,String> t = data.get(type);
        if(t == null) {
            return;
        }
        ((TopicImpl)version).removedFromDataVersion(this,type);
        Object o=t.remove(version);
        if(t.isEmpty()) {
            data.remove(type);
            ((TopicImpl)type).removedFromDataType(this);
        }
        boolean changed=(o!=null);
        if(changed) {
            topicMap.topicDataChanged(this,type,version,null,(String)o);
        }
        dependentTopics=null;
        updateEditTime();
    }
    
    
    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        topicMap.removeTopicSubjectIdentifier(this,l);
        boolean changed = subjectIdentifiers.remove(l);
        updateEditTime();
        if(changed) {
            topicMap.topicSubjectIdentifierChanged(this,null,l);
        }
    }
    
    
    @Override
    public void removeType(Topic t) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        topicMap.removeTopicType(this,t);
        boolean changed=types.remove(t);
        ((TopicImpl)t).removedFromTopicType(this);
        dependentTopics=null;
        updateEditTime();
        if(changed) {
            topicMap.topicTypeChanged(this,null,t);
        }
    }
    
    
    @Override
    public void setBaseName(String name) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        Topic t=null;
        if(name!=null) t=topicMap.getTopicWithBaseName(name);
        String old=baseName;
        topicMap.setTopicName(this,name,baseName);
        boolean changed = ( (baseName!=null || name!=null) && ( baseName==null || name==null || !baseName.equals(name) ) );
        baseName=name;
        if(t!=null && t!=this) {
            mergeIn(t);
        }
        updateEditTime();
        clearNameCaches();
        if(changed) {
            topicMap.topicBaseNameChanged(this,name,old);
        }
    }
    
    
    @Override
    public void setSubjectLocator(Locator l)  throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        Topic t=null;
        if(l!=null) t=topicMap.getTopicBySubjectLocator(l);
        Locator old=subjectLocator;
        topicMap.setTopicSubjectLocator(this,l,subjectLocator);
        boolean changed=( (subjectLocator!=null || l!=null) && ( subjectLocator==null || l==null || !subjectLocator.equals(l) ) );
        subjectLocator=l;
        if(t!=null && t!=this){
            mergeIn(t);
        }
        updateEditTime();
        if(changed) {
            topicMap.topicSubjectLocatorChanged(this,l,old);
        }
    }
    
    
    @Override
    public long getEditTime()  throws TopicMapException {
        return editTime;
    }
    
    
    @Override
    public void setEditTime(long time)  throws TopicMapException {
        editTime=time;
    }
    

    @Override
    public void remove() throws TopicMapException {
        if(removed) return;
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        if(!isDeleteAllowed()) {
            if(!topicTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_TOPICTYPE);
            if(!dataTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_DATATYPE);
            if(!dataVersionIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_DATAVERSION);
            if(!associationTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_ASSOCIATIONTYPE);
            if(!roleTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_ASSOCIATIONROLE);
            if(!variantScopeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_VARIANTSCOPE);
        }

        removed=true;
        
        ArrayList<Association> tempAssociations=new ArrayList();
        tempAssociations.addAll(associations);
        for(Association a : tempAssociations) {
            a.remove();
        }
        
        topicMap.topicRemoved(this);

        removed=false; // need to be false so we can clear the topic

        for(Locator l : subjectIdentifiers) {
            // don't actully remove subject identifiers because in many places they are needed to identify the deleted topic
            topicMap.removeTopicSubjectIdentifier(this,l);            
//            this.removeSubjectIdentifier(l);
        }
        if(subjectLocator!=null) this.setSubjectLocator(null);
        if(baseName!=null) this.setBaseName(null);
        
        ArrayList<Topic> tempTypes = new ArrayList();
        tempTypes.addAll(types);
        for(Topic t : tempTypes) {
            this.removeType(t);
        }
        
        ArrayList<Set<Topic>> tempVariantScopes = new ArrayList();
        tempVariantScopes.addAll(getVariantScopes());
        for(Set<Topic> scope : tempVariantScopes) {
            this.removeVariant(scope);
        }

        ArrayList<Topic> tempDataTypes = new ArrayList();
        tempDataTypes.addAll(getDataTypes());
        for(Topic dataType : tempDataTypes) {
            this.removeData(dataType);
        }
        updateEditTime();
        removed=true;
    }
    
    
    @Override
    public void removeVariant(Set<Topic> scope)  throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();

        Object o=variants.remove(scope);
        boolean changed=(o!=null);
        HashSet allscopes=new HashSet();
        for(Collection c : variants.keySet()) {
            allscopes.addAll(c);
        }
        for(Topic t : scope){
            if(!allscopes.contains(t)) {
                ((TopicImpl) t).removedFromVariantScope(this);
            }
        }
        clearNameCaches();
        updateEditTime();
        if(changed) {
            topicMap.topicVariantChanged(this,scope,null,(String)o);
        }
    }
    
    
    @Override
    public void setVariant(Set<Topic> scope, String name)  throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();

        if(name == null){
            System.out.println("WRN setVariant called with null value, redirecting to removeVariant");
            removeVariant(scope); 
            return;
        }
        for(Topic t : scope) {
            ((TopicImpl) t).addedAsVariantScope(this);
        }
        Object o = null;
        o = variants.put(scope,name);
        boolean changed=( o==null || !o.equals(name));
        clearNameCaches();
        updateEditTime();
        if(changed) {
            topicMap.topicVariantChanged(this,scope,name,(String)o);
        }
    }
    
    
    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        return variants.keySet();
    }
    
    
    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        return data.keySet();
    }
    
    
    @Override
    public boolean isRemoved() throws TopicMapException {
        return removed;
    }
    

    /**
     * Notice, isDeleteAllowed doesn't return true if the topic map is
     * write protected or if the topic is already deleted.
     * 
     * @return
     * @throws TopicMapException 
     */
    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        
        if(!topicTypeIndex.isEmpty()) return false;
        if(!dataTypeIndex.isEmpty()) return false;
        if(!dataVersionIndex.isEmpty()) return false;
        if(!associationTypeIndex.isEmpty()) return false;
        if(!roleTypeIndex.isEmpty()) return false;
        if(!variantScopeIndex.isEmpty()) return false;
        
        if(denyRemoveIfCoreTopic) {
            for(Locator l : subjectIdentifiers) {
                try {
                    if(l != null && l.toExternalForm().startsWith("http://www.topicmaps.org/xtm/1.0/core.xtm"))
                        return false;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
    

    @Override
    public long getDependentEditTime()  throws TopicMapException {
        return dependentEditTime;
    }
    
    @Override
    public void setDependentEditTime(long time)  throws TopicMapException {
        dependentEditTime=time;
    }

    @Override
    public Collection<Topic> getTopicsWithDataType()  throws TopicMapException {
        return dataTypeIndex;
    }
    
    @Override
    public Collection<Association> getAssociationsWithType()  throws TopicMapException {
        return associationTypeIndex;
    }
    
    @Override
    public Collection<Association> getAssociationsWithRole()  throws TopicMapException {
        return roleTypeIndex;
    }

    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        Collection<Topic> dataVersionTopics = new ArrayList<Topic>();
        for(DataVersionIndexWrapper dviw : dataVersionIndex) {
            dataVersionTopics.add(dviw.topic);
        }
        return dataVersionTopics;
    }

    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        return variantScopeIndex;
    }
    
    
    /* ---------------------------------------------------------------------- */
    /* ------------------------------------------------- Internal methods --- */
    /* ---------------------------------------------------------------------- */
    
    
    
    private void makeDependentTopicsSet() throws TopicMapException {
        // ### = might not be needed, depends on what should actually be called dependent
        dependentTopics = new HashSet();
        
        dependentTopics.addAll(topicTypeIndex);

        dependentTopics.addAll(dataTypeIndex);

        dependentTopics.addAll(variantScopeIndex);

        for(DataVersionIndexWrapper ts : dataVersionIndex) {
            dependentTopics.add(ts.topic);
            dependentTopics.add(ts.type); // ###
        }
        
        for(Association a : associationTypeIndex) {
            for(Topic role : a.getRoles()) {
                dependentTopics.add(role); // ###
                dependentTopics.add(a.getPlayer(role));
            }
            if(a.getType() != null)
                dependentTopics.add(a.getType()); // ###
        }
        
        for(Association a : roleTypeIndex) {
            for(Topic role : a.getRoles()) {
                dependentTopics.add(role); // ###
                dependentTopics.add(a.getPlayer(role));
            }
            dependentTopics.add(a.getType()); // ###
        }

        for(Association a : associations) {
            for(Topic role : a.getRoles()) {
                dependentTopics.add(role); // ###
                dependentTopics.add(a.getPlayer(role));
            }
            dependentTopics.add(a.getType()); // ###
        }

        for(Topic type : types) {
            dependentTopics.add(type);
        }
        
        for(Topic dataType : data.keySet()) {
            dependentTopics.add(dataType);
        }
    }
    
    
    private void updateEditTime() throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        editTime=System.currentTimeMillis();
        if(topicMap.trackingDependent()) {
            updateDependentEditTime();
            if(dependentTopics==null) {
                makeDependentTopicsSet();
            }
            for(Topic dependentTopic : dependentTopics) {
                ((TopicImpl) dependentTopic).updateDependentEditTime();
            }
        }
    }
    
    
    private void updateDependentEditTime() throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        dependentEditTime=System.currentTimeMillis();        
    }
    
    
    public void mergeIn(Topic t) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        TopicImpl ti=(TopicImpl)t;
        topicMap.topicsMerged(this,ti);
        // ----- add data ----- 
        for(Topic otype : new ArrayList<Topic>(ti.data.keySet())) {
            Map<Topic,String> hm = ti.data.get(otype);
            Hashtable ht = new Hashtable();
            ht.putAll(hm);
            this.setData(otype, ht);
            ti.removeData(otype);
        }
        // ----- set base name ----- 
        if(ti.getBaseName() != null) {
            String name = ti.getBaseName();
            ti.setBaseName(null);
            this.setBaseName(name);
        }
        // ----- set subject locator ----- 
        if(ti.getSubjectLocator() != null){
            Locator l = ti.getSubjectLocator();
            ti.setSubjectLocator(null);
            this.setSubjectLocator(l);
        }
        // ----- set types ----- 
        for(Topic type : ti.getTypes()){
            if(type == ti) this.addType(this);
            else this.addType(type);
        }
        // ----- set variant names ----- 
        for(Set scope : ti.getVariantScopes()){
            String name = ti.getVariant(scope);
            this.setVariant(scope,name);
        }

        // To prevent ConcurrentModificationException, next changes copy
        // data into a toBeMapped... ArrayLists first.
        
        // ----- change association players ----- 
        ArrayList<T2<Association,Topic>> tobeMappedAssociationRoles = new ArrayList();
        for(Map<Topic,Collection<Association>> roledAssociations : ti.associationIndex.values()) {
            for(Topic role : roledAssociations.keySet()) {
                Collection<Association> as =  roledAssociations.get(role);
                for(Association a : as) {
                    tobeMappedAssociationRoles.add( new T2(a, role) );
                }
            }
        }
        for(T2<Association,Topic> associationAndRole : tobeMappedAssociationRoles) {
            Association a = associationAndRole.e1;
            if(a.isRemoved()) continue;
            Topic role = associationAndRole.e2;
//            a.removePlayer(role);
//            a.addPlayer(this,role);
            // this replaces the old player and is atomic so that there cannot
            // be an unintended merge between the remove and add
            a.addPlayer(this,role); 
        }
        
        // ----- change association types ----- 
        ArrayList<Association> tobeMappedTypedAssociations = new ArrayList();
        tobeMappedTypedAssociations.addAll(ti.associationTypeIndex);
        for(Association a : tobeMappedTypedAssociations) {
            a.setType(this);
        }
        
        // ----- change topic types ----- 
        ArrayList<Topic> tobeMappedTopicTypes = new ArrayList();
        tobeMappedTopicTypes.addAll(ti.topicTypeIndex);
        for(Topic type : tobeMappedTopicTypes){
            type.removeType(t);
            type.addType(this);
        }
        
        // ----- change data types ----- 
        ArrayList<Topic> tobeMappedDataTypes = new ArrayList();
        tobeMappedDataTypes.addAll(ti.dataTypeIndex);
        
        final TopicComparator topicComparator=new TopicComparator();
        final ScopeComparator scopeComparator=new ScopeComparator();
        // The sorting guarantees that merges in identical topic maps
        // always have identical results. Which occurrence ends up being used
        // when types of several collide is undefined but is deterministic
        // with the sorted array. For same rason the array is sorted for
        // some other cases down below, but not all of them need it.
        Collections.sort(tobeMappedDataTypes,topicComparator);
        
        for(Topic dataType : tobeMappedDataTypes) {
            Hashtable val = dataType.getData(t);
            dataType.removeData(t);
            dataType.setData(this, val);
        }
        
        // ----- change data versions ----- 
        ArrayList<DataVersionIndexWrapper> tobeMappedDataVersions = new ArrayList();
        tobeMappedDataVersions.addAll(ti.dataVersionIndex);      
        Collections.sort(tobeMappedDataVersions, new Comparator<DataVersionIndexWrapper>(){
            @Override
            public int compare(DataVersionIndexWrapper o1, DataVersionIndexWrapper o2) {
                int c=topicComparator.compare(o1.topic, o2.topic);
                if(c!=0) return c;
                return topicComparator.compare(o1.type, o2.type);
            }
        });
        for(DataVersionIndexWrapper info : tobeMappedDataVersions) {
            String val=info.topic.getData(info.type,t);
            info.topic.removeData(info.type,t);
            info.topic.setData(info.type,this,val);
        }
        
        // ----- change role types ----- 
        ArrayList<Association> tobeMappedRoleTypes = new ArrayList();
        tobeMappedRoleTypes.addAll(ti.roleTypeIndex);
        for(Association a : tobeMappedRoleTypes) {
            Topic p = a.getPlayer(t);
            // Doing these in this order guarantees that we don't lose anything
            // to unintended merges. There might be a merge after the add, but
            // if that is the case, the two associations would merge in the end
            // anyway, so we still end up with the correct result.
            if(p != null) {
                a.addPlayer(p, this);
            }
            if(!a.isRemoved()) {
                a.removePlayer(t);
            }
        }
        
        // ----- change variant scopes ----- 
        ArrayList<T2<Topic,Set<Topic>>> tobeMappedVariantScopes = new ArrayList();
        for(Topic topic : ti.variantScopeIndex) {
            Set<Set<Topic>> scopes = new LinkedHashSet();
            scopes.addAll(topic.getVariantScopes());
            for(Set<Topic> c : scopes) {
                if(c.contains(t)) {
                    tobeMappedVariantScopes.add(Tuples.t2(topic,c));
                }
            }
        }
        Collections.sort(tobeMappedVariantScopes,new Comparator<Tuples.T2<Topic,Set<Topic>>>(){
            @Override
            public int compare(Tuples.T2<Topic,Set<Topic>> t1, Tuples.T2<Topic,Set<Topic>> t2) {
                int c=topicComparator.compare(t1.e1, t2.e1);
                if(c!=0) return c;
                return scopeComparator.compare(t1.e2, t2.e2);
            }
        });
        for(T2<Topic,Set<Topic>> t2 : tobeMappedVariantScopes) {
            Topic topic = t2.e1;
            Set c = t2.e2;
            Set newscope = Collections.synchronizedSet(new LinkedHashSet());
            newscope.addAll(c);
            newscope.remove(t);
            newscope.add(this);
            String name = topic.getVariant(c);
            topic.removeVariant(c);
            topic.setVariant(newscope,name);
        }
        
        // set subject identifiers, do this last as some other things rely
        // on topics still having subject identifiers
        HashSet<Locator> copied=new LinkedHashSet();
        copied.addAll(ti.getSubjectIdentifiers());
        for(Locator l : copied) {
            ti.removeSubjectIdentifier(l);
            this.addSubjectIdentifier(l);
        }        
        
        // check for duplicate associations
        removeDuplicateAssociations();
        // remove merged topic
        try {
            t.remove();
        }
        catch(TopicInUseException e){
            System.out.println("ERROR couldn't delete merged topic, topic in use. There is a bug in the code if this happens. "+e.getReason());
        }
        topicMap.topicChanged(this);
    }
    
    
    void removeDuplicateAssociations() throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        removeDuplicateAssociations(null);
    }
    
    
    /**
     * Deletes duplicate associations but does not delete the one given as parameter.
     * If duplicate associations are found and the one given as parameter is one of
     * them, then the parameter will remain in the topic map and other equal
     * associations are deleted. If parameter is null, or when parameter is none
     * of the equal associations, then the one that remains in topic map is chosen arbitrarily
     */
    void removeDuplicateAssociations(Association notThis) throws TopicMapException {
        if(removed) throw new TopicMapException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        HashMap<EqualAssociationWrapper,Association> as = new HashMap();
        ArrayList<Association> tobeDeleted = new ArrayList();
        Association remaining = notThis;
        for(Association a : associations) {
            EqualAssociationWrapper eaw = new EqualAssociationWrapper((AssociationImpl) a);
            if(as.containsKey(eaw)) {
                if(notThis!=null && notThis==a){
                    tobeDeleted.add(as.get(eaw));
                    as.put(eaw,a);
                }
                else {
                    tobeDeleted.add(a);
                }
            }
            else {
                if(remaining!=null) remaining=a;
                as.put(eaw,a);
            }
        }
        for(Association a : tobeDeleted){
            topicMap.duplicateAssociationRemoved(remaining,a);
            a.remove();
        }
    }
    
    
    void addInAssociation(Association a,Topic role) throws TopicMapException {
        associations.add(a);
        Topic type=a.getType();
        Map<Topic,Collection<Association>> t = null;
        if(type != null) {
            t=associationIndex.get(type);
        }
        if(t==null){
            t=new LinkedHashMap();
            associationIndex.put(type,t);
        }
        Collection<Association> s = t.get(role);
        if(s==null){
            s=new LinkedHashSet();
            t.put(role,s);
        }
        s.add(a);
        dependentTopics=null;
    }
    
    
    void removeFromAssociation(Association a,Topic role,boolean otherRoles) throws TopicMapException {
        if(!otherRoles){
            associations.remove(a);
        }
        Topic type=a.getType();
        Map<Topic,Collection<Association>> t = associationIndex.get(type);
        Collection<Association> s = t.get(role);
        s.remove(a);
        if(s.isEmpty()) t.remove(role);
        if(t.isEmpty()) associationIndex.remove(type);
        dependentTopics=null;
    }
    
    
    void associationTypeChanged(Association a, Topic type, Topic oldType, Topic role){
        Map<Topic,Collection<Association>> t = associationIndex.get(oldType);
        Collection<Association> s = t.get(role);
        s.remove(a);
        if(s.isEmpty()) t.remove(role);
        if(t.isEmpty()) associationIndex.remove(oldType);
        
        if(type != null){
            t = associationIndex.get(type);
            if(t == null){
                t = new LinkedHashMap();
                associationIndex.put(type,t);
            }
            s = t.get(role);
            if(s == null){
                s = new LinkedHashSet();
                t.put(role,s);
            }
            s.add(a);
        }
        dependentTopics=null;        
    }
    void addedAsTopicType(Topic t){
        topicTypeIndex.add(t);
        dependentTopics=null;
    }
    void removedFromTopicType(Topic t){
        topicTypeIndex.remove(t);
        dependentTopics=null;
    }
    void addedAsDataType(Topic t){
        dataTypeIndex.add(t);
        dependentTopics=null;
    }
    void removedFromDataType(Topic t){
        dataTypeIndex.remove(t);
        dependentTopics=null;
    }
    void addedAsDataVersion(Topic t,Topic type){
        dataVersionIndex.add(new DataVersionIndexWrapper(t,type));
        dependentTopics=null;
    }
    void removedFromDataVersion(Topic t,Topic type){
        dataVersionIndex.remove(new DataVersionIndexWrapper(t,type));
        dependentTopics=null;
    }
    void addedAsAssociationType(Association a){
        associationTypeIndex.add(a);
        dependentTopics=null;
    }
    void removedFromAssociationType(Association a){
        associationTypeIndex.remove(a);
        dependentTopics=null;
    }
    void addedAsRoleType(Association a){
        roleTypeIndex.add(a);
        dependentTopics=null;
    }
    void removedFromRoleType(Association a){
        roleTypeIndex.remove(a);
        dependentTopics=null;
    }
    void addedAsVariantScope(Topic t){
        variantScopeIndex.add(t);
        dependentTopics=null;
    }
    void removedFromVariantScope(Topic t){
        variantScopeIndex.remove(t);
        dependentTopics=null;
    }
        
    
    
    private class EqualAssociationWrapper {
        public AssociationImpl a;
        public EqualAssociationWrapper(AssociationImpl a){
            this.a=a;
        }
        @Override
        public boolean equals(Object o){
            if(o != null && o instanceof EqualAssociationWrapper) {
                return a._equals(((EqualAssociationWrapper)o).a);
            }
            else {
                return false;
            }
        }
        @Override
        public int hashCode(){
            return a._hashCode();
        }
    }
    
    
    private static class TopicComparator implements Comparator<Topic> {
        @Override
        public int compare(Topic o1, Topic o2) {
            try {
                Locator l1=o1.getFirstSubjectIdentifier();
                Locator l2=o2.getFirstSubjectIdentifier();
                if(l1==null && l2==null) return 0;
                else if(l1==null) return -1;
                else if(l2==null) return 1;
                else return l1.compareTo(l2);
            }
            catch(TopicMapException tme){
                tme.printStackTrace();
                return 0;
            }
        }
    }
    
    private static class ScopeComparator implements Comparator<Set<Topic>>{
        private TopicComparator topicComparator=new TopicComparator();
        @Override
        public int compare(Set<Topic> o1, Set<Topic> o2) {
            if(o1.size()<o2.size()) return -1;
            else if(o1.size()>o2.size()) return 1;

            if(o1.size()==1) {
                return topicComparator.compare(o1.iterator().next(),o2.iterator().next());
            }

            List<Topic> l1=new ArrayList<Topic>(o1);
            List<Topic> l2=new ArrayList<Topic>(o2);
            Collections.sort(l1,topicComparator);
            Collections.sort(l2,topicComparator);
            for(int i=0;i<l1.size();i++) {
                int c=topicComparator.compare(l1.get(i),l2.get(i));
                if(c!=0) return c;
            }
            return 0;
        }
    }
    
}



class DataVersionIndexWrapper {
    public Topic topic;
    public Topic type;
    public DataVersionIndexWrapper(Topic topic,Topic type){
        this.topic=topic;
        this.type=type;
    }
    @Override
    public int hashCode(){
        return topic.hashCode()+type.hashCode();
    }
    @Override
    public boolean equals(Object o){
        if(o != null && o instanceof DataVersionIndexWrapper) {
            DataVersionIndexWrapper w=(DataVersionIndexWrapper)o;
            return w.type.equals(type) && w.topic.equals(topic);
        }
        else {
            return false;
        }
    }
}

