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
 *
 *
 * 
 *
 * TopicImpl.java
 *
 * Created on June 10, 2004, 11:30 AM
 */

package org.wandora.topicmap.memory;


import org.wandora.topicmap.*;
import java.util.*;
import org.wandora.utils.Tuples;


/**
 *
 * @author  olli
 */
public class TopicImpl extends Topic {
    
    private TopicMapImpl topicMap;
    
    private Hashtable<Topic,Hashtable<Topic,String>> data;
    private HashSet<Topic> types;
    private HashSet<Association> associations;
    private Hashtable<Topic,Hashtable<Topic,String>> associationIndex;
    private String baseName;
    private Locator subjectLocator;
    private HashSet<Locator> subjectIdentifiers;
    private Hashtable<Set<Topic>,String> variants;
    
    private HashSet<Topic> dataTypeIndex;
    private HashSet<DataVersionIndexWrapper> dataVersionIndex;
    private HashSet<Topic> topicTypeIndex;
    private HashSet<Association> associationTypeIndex;
    private HashSet<Association> roleTypeIndex;
    private HashSet<Topic> variantScopeIndex;
    
    private HashSet dependentTopics;
    
    private String id;
    
    private boolean removed;
    private boolean denyRemoveIfCoreTopic = true;
    
    private long editTime;
    private long dependentEditTime;
    
    private Hashtable dispNameCache;
    private Hashtable sortNameCache;
    
    /** Creates a new instance of TopicImpl */
    public TopicImpl(TopicMapImpl topicMap) {
        this.topicMap=topicMap;
        id=getUniqueID();
        data=new Hashtable();
        types=new HashSet();
        associations=new HashSet();
        associationIndex=new Hashtable();
        baseName=null;
        subjectLocator=null;
        subjectIdentifiers=new HashSet();
        variants=new Hashtable();
        
        dispNameCache=new Hashtable();
        sortNameCache=new Hashtable();
        
        dataTypeIndex=new HashSet();
        dataVersionIndex=new HashSet();
        topicTypeIndex=new HashSet();
        associationTypeIndex=new HashSet();
        roleTypeIndex=new HashSet();
        variantScopeIndex=new HashSet();
        
        removed=false;
    }
    
    public void clearNameCaches(){
        dispNameCache.clear();
        sortNameCache.clear();
    }
    
    
    @Override
    public String getDisplayName(String lang) throws TopicMapException {
        if(dispNameCache.containsKey(lang)) return (String)dispNameCache.get(lang);
        String name=super.getDisplayName(lang);
        dispNameCache.put(lang,name);
        return name;
    }
    
    
    @Override
    public String getSortName(String lang) throws TopicMapException {
        if(sortNameCache.containsKey(lang)) return (String)sortNameCache.get(lang);
        String name=super.getSortName(lang);
        sortNameCache.put(lang,name);
        return name;
    }
    
    private static long idcounter=0;
    public static synchronized String getUniqueID(){
        return "topic"+(idcounter++);
    }
    
    @Override
    public String getID() throws TopicMapException {
        return id;
    }
    
    @Override
    public void setData(Topic type, Topic version, String value) throws TopicMapException {
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
        Hashtable t=(Hashtable)data.get(type);
        if(t==null){
            t=new Hashtable();
            data.put(type,t);
        }
        Object o=t.put(version,value);
        boolean changed=( o==null || !o.equals(value) );
        dependentTopics=null;
        updateEditTime();
        if(changed) topicMap.topicDataChanged(this,type,version,value,(String)o);
    }
    
    
    @Override
    public void setData(Topic type, Hashtable<Topic,String> versionData) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        ((TopicImpl)type).addedAsDataType(this);
        Hashtable t=(Hashtable)data.get(type);
        if(t==null){
            t=new Hashtable();
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
            if(changed) topicMap.topicDataChanged(this,type,version,value,(String)o);
        }
        dependentTopics=null;
        updateEditTime();
    }
    
    
    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        Topic t=topicMap.getTopic(l);
        boolean changed=false;
        if(t!=null && t!=this){
            mergeIn(t);
        }
        else{
            topicMap.addTopicSubjectIdentifier(this,l);
            changed=subjectIdentifiers.add(l);
        }
        updateEditTime();
        if(changed) topicMap.topicSubjectIdentifierChanged(this,l,null);
    }
    
    @Override
    public void addType(Topic t) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        ((TopicImpl)t).addedAsTopicType(this);
        topicMap.addTopicType(this,t);
        boolean changed=types.add(t);
        dependentTopics=null;
        updateEditTime();
        if(changed) topicMap.topicTypeChanged(this,t,null);        
    }
    
    @Override
    public Collection<Association> getAssociations()  throws TopicMapException {
        return associations;
    }
    
    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        Hashtable s=(Hashtable)associationIndex.get(type);
        if(s==null) return new HashSet();
//        else return s.values();
        else{
            HashSet as=new HashSet();
            Iterator iter=s.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry e=(Map.Entry)iter.next();
                as.addAll((Collection)e.getValue());
            }
            return as;
        }
    }
    
    @Override
    public Collection<Association> getAssociations(Topic type, Topic role) throws TopicMapException {
        Hashtable s=(Hashtable)associationIndex.get(type);
        if(s==null) return new HashSet();
        else {
            Collection s2=(Collection)s.get(role);
            if(s2==null) return new HashSet();
            else return s2;
        }
    }
    
    @Override
    public String getBaseName() throws TopicMapException {
        return baseName;
    }
    
    @Override
    public String getData(Topic type, Topic version) throws TopicMapException {
        Hashtable t=(Hashtable)data.get(type);
        if(t==null) return null;
        else return (String)t.get(version);
    }
    
    @Override
    public Hashtable getData(Topic type) throws TopicMapException {
        Hashtable t=(Hashtable)data.get(type);
        if(t==null) return new Hashtable();
        else return t;
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
        Hashtable t=(Hashtable)data.remove(type);
        ((TopicImpl)type).removedFromDataType(this);
        if(t!=null){
            Iterator iter=t.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry e=(Map.Entry)iter.next();
                Topic version=(Topic)e.getKey();
                ((TopicImpl)version).removedFromDataVersion(this,type);
                topicMap.topicDataChanged(this,type,version,null,(String)e.getValue());
            }
        }
        dependentTopics=null;
        updateEditTime();
    }
    
    
    @Override
    public void removeData(Topic type, Topic version) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        Hashtable t=(Hashtable)data.get(type);
        if(t==null) {
            return;
        }
        ((TopicImpl)version).removedFromDataVersion(this,type);
        Object o=t.remove(version);
        if(t.isEmpty()) {
            data.remove(type);
            ((TopicImpl)type).removedFromDataType(this);
        }
        boolean changed=(o!=null);
        if(changed) topicMap.topicDataChanged(this,type,version,null,(String)o);
        dependentTopics=null;
        updateEditTime();
    }
    
    
    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        topicMap.removeTopicSubjectIdentifier(this,l);
        boolean changed=subjectIdentifiers.remove(l);
        updateEditTime();
        if(changed) topicMap.topicSubjectIdentifierChanged(this,null,l);
    }
    
    @Override
    public void removeType(Topic t) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        topicMap.removeTopicType(this,t);
        boolean changed=types.remove(t);
        ((TopicImpl)t).removedFromTopicType(this);
        dependentTopics=null;
        updateEditTime();
        if(changed) topicMap.topicTypeChanged(this,null,t);
    }
    
    @Override
    public void setBaseName(String name) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        Topic t=null;
        if(name!=null) t=topicMap.getTopicWithBaseName(name);
        String old=baseName;
        topicMap.setTopicName(this,name,baseName);
        boolean changed=( (baseName!=null || name!=null) && ( baseName==null || name==null || !baseName.equals(name) ) );
        baseName=name;
        if(t!=null && t!=this){
            mergeIn(t);
        }
        updateEditTime();
        clearNameCaches();
        if(changed) topicMap.topicBaseNameChanged(this,name,old);
    }
    
    @Override
    public void setSubjectLocator(Locator l)  throws TopicMapException {
        if(removed) throw new TopicRemovedException();
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
        if(changed) topicMap.topicSubjectLocatorChanged(this,l,old);
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
        if(removed) throw new TopicRemovedException();
        if(!isDeleteAllowed()) {
            if(!topicTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_TOPICTYPE);
            if(!dataTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_DATATYPE);
            if(!dataVersionIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_DATAVERSION);
            if(!associationTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_ASSOCIATIONTYPE);
            if(!roleTypeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_ASSOCIATIONROLE);
            if(!variantScopeIndex.isEmpty()) throw new TopicInUseException(this,TopicInUseException.USEDIN_VARIANTSCOPE);
        }

        removed=true;
        
        ArrayList temp=new ArrayList();
        temp.addAll(associations);
        Iterator iter=temp.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            a.remove();
        }
        
        topicMap.topicRemoved(this);

        removed=false; // need to be false so we can clear the topic
        
        iter=subjectIdentifiers.iterator();
        while(iter.hasNext()){
            Locator l=(Locator)iter.next();
            // don't actully remove subject identifiers because in many places they are needed to identify the deleted topic
            topicMap.removeTopicSubjectIdentifier(this,l);            
//            this.removeSubjectIdentifier(l);
        }
        if(subjectLocator!=null) this.setSubjectLocator(null);
        if(baseName!=null) this.setBaseName(null);
        
        temp=new ArrayList();
        temp.addAll(types);
        iter=temp.iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            this.removeType(t);
        }
        temp=new ArrayList();
        temp.addAll(getVariantScopes());
        iter=temp.iterator();
        while(iter.hasNext()){
            this.removeVariant((Set)iter.next());
        }
        temp=new ArrayList();
        temp.addAll(getDataTypes());
        iter=temp.iterator();
        while(iter.hasNext()){
            this.removeData((Topic)iter.next());
        }
        updateEditTime();
        removed=true;
    }
    
    @Override
    public void removeVariant(Set<Topic> scope)  throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        Object o=variants.remove(scope);
        boolean changed=(o!=null);
        HashSet allscopes=new HashSet();
        Iterator iter=variants.keySet().iterator();
        while(iter.hasNext()){
            Collection c=(Collection)iter.next();
            allscopes.addAll(c);
        }
        iter=scope.iterator();
        while(iter.hasNext()){
            TopicImpl t=(TopicImpl)iter.next();
            if(!allscopes.contains(t)) t.removedFromVariantScope(this);
        }
        clearNameCaches();
        updateEditTime();
        if(changed) topicMap.topicVariantChanged(this,scope,null,(String)o);
    }
    
    @Override
    public void setVariant(Set<Topic> scope, String name)  throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        if(name==null){
            System.out.println("WRN setVariant called with null value, redirecting to removeVariant");
            removeVariant(scope); 
            return;
        }
        Iterator iter=scope.iterator();
        while(iter.hasNext()){
            TopicImpl t=(TopicImpl)iter.next();
            t.addedAsVariantScope(this);
        }
        Object o = null;
        o = variants.put(scope,name);
        boolean changed=( o==null || !o.equals(name));
        clearNameCaches();
        updateEditTime();
        if(changed) topicMap.topicVariantChanged(this,scope,name,(String)o);
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
    
    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        if(!topicTypeIndex.isEmpty()) return false;
        if(!dataTypeIndex.isEmpty()) return false;
        if(!dataVersionIndex.isEmpty()) return false;
        if(!associationTypeIndex.isEmpty()) return false;
        if(!roleTypeIndex.isEmpty()) return false;
        if(!variantScopeIndex.isEmpty()) return false;
        
        if(denyRemoveIfCoreTopic) {
            Iterator i = subjectIdentifiers.iterator();
            if(i != null) {
                Locator l = null;
                while(i.hasNext()) {
                    try {
                        l = (Locator) i.next();
                        if(l.toExternalForm().startsWith("http://www.topicmaps.org/xtm/1.0/core.xtm"))
                            return false;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
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
        ArrayList dataVersionTopics = new ArrayList<Topic>();
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
        dependentTopics=new HashSet();
        Iterator iter=topicTypeIndex.iterator();
        while(iter.hasNext()){
            dependentTopics.add(iter.next());
        }
        iter=dataTypeIndex.iterator();
        while(iter.hasNext()){
            dependentTopics.add(iter.next());
        }
        iter=variantScopeIndex.iterator();
        while(iter.hasNext()){
            dependentTopics.add(iter.next());
        }
        iter=dataVersionIndex.iterator();
        while(iter.hasNext()){
            DataVersionIndexWrapper ts=(DataVersionIndexWrapper)iter.next();
//            Topic[] ts=(Topic[])iter.next();
            dependentTopics.add(ts.topic);
            dependentTopics.add(ts.type); // ###
        }
        iter=associationTypeIndex.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Iterator iter2=a.getRoles().iterator();
            while(iter2.hasNext()){
                Topic role=(Topic)iter2.next();
                dependentTopics.add(role); // ###
                dependentTopics.add(a.getPlayer(role));
            }
            if(a.getType()!=null)
                dependentTopics.add(a.getType()); // ###
        }
        iter=roleTypeIndex.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Iterator iter2=a.getRoles().iterator();
            while(iter2.hasNext()){
                Topic role=(Topic)iter2.next();
                dependentTopics.add(role); // ###
                dependentTopics.add(a.getPlayer(role));
            }
            dependentTopics.add(a.getType()); // ###
        }
        iter=associations.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Iterator iter2=a.getRoles().iterator();
            while(iter2.hasNext()){
                Topic role=(Topic)iter2.next();
                dependentTopics.add(role); // ###
                dependentTopics.add(a.getPlayer(role));
            }
            dependentTopics.add(a.getType()); // ###
        }
        iter=types.iterator();
        while(iter.hasNext()){
            dependentTopics.add(iter.next());
        }
        iter=data.keySet().iterator();
        while(iter.hasNext()){
            dependentTopics.add(iter.next());
        }
    }
    
    private void updateEditTime() throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        editTime=System.currentTimeMillis();
        if(topicMap.trackingDependent()){
            updateDependentEditTime();
            if(dependentTopics==null) makeDependentTopicsSet();
            Iterator iter=dependentTopics.iterator();
            while(iter.hasNext()){
                ((TopicImpl)iter.next()).updateDependentEditTime();
            }
        }
    }
    
    private void updateDependentEditTime() throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        dependentEditTime=System.currentTimeMillis();        
    }
    
    public void mergeIn(Topic t) throws TopicMapException {
        if(removed) throw new TopicRemovedException();
        TopicImpl ti=(TopicImpl)t;
        topicMap.topicsMerged(this,ti);
        // add data
        Iterator iter=new ArrayList(ti.data.entrySet()).iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            setData((Topic)e.getKey(),(Hashtable)e.getValue());
            ti.removeData((Topic)e.getKey());
        }
        // set base name
        if(ti.getBaseName()!=null) {
            String name=ti.getBaseName();
            ti.setBaseName(null);
            this.setBaseName(name);
        }
        // set subject locator
        if(ti.getSubjectLocator()!=null){
            Locator l=ti.getSubjectLocator();
            ti.setSubjectLocator(null);
            this.setSubjectLocator(l);
        }
        // set types
        iter=ti.getTypes().iterator();
        while(iter.hasNext()){
            Topic type=(Topic)iter.next();
            if(type==ti) this.addType(this);
            else this.addType(type);
        }
        // set variant names
        iter=ti.getVariantScopes().iterator();
        while(iter.hasNext()){
            Set scope=(Set)iter.next();
            String name=ti.getVariant(scope);
            this.setVariant(scope,name);
        }

        // in many cases, can't map while iterating, would cause ConcurrentModificationException
        ArrayList tobeMapped=new ArrayList();
        // change association players
        iter=ti.associationIndex.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            Iterator iter2=((Hashtable)e.getValue()).entrySet().iterator();
            while(iter2.hasNext()){
                Map.Entry e2=(Map.Entry)iter2.next();
                Topic role=(Topic)e2.getKey();
                Set set=(Set)e2.getValue();
                Iterator iter3=set.iterator();
                while(iter3.hasNext()){
                    Association a=(Association)iter3.next();
                    tobeMapped.add(a);
                    tobeMapped.add(role);
                }
            }
        }
        for(int i=0;i<tobeMapped.size();i+=2){
            Association a=(Association)tobeMapped.get(i);
            if(a.isRemoved()) continue;
            Topic role=(Topic)tobeMapped.get(i+1);
//            a.removePlayer(role);
//            a.addPlayer(this,role);
            // this replaces the old player and is atomic so that there cannot
            // be an unintended merge between the remove and add
            a.addPlayer(this,role); 
        }
        // change association types
        tobeMapped=new ArrayList();
        iter=ti.associationTypeIndex.iterator();
        while(iter.hasNext()){
            tobeMapped.add(iter.next());
        }
        for(int i=0;i<tobeMapped.size();i++){
            ((Association)tobeMapped.get(i)).setType(this);
        }
        // change topic types
        tobeMapped=new ArrayList();
        iter=ti.topicTypeIndex.iterator();
        while(iter.hasNext()){
            tobeMapped.add(iter.next());
        }
        for(int i=0;i<tobeMapped.size();i++){
            ((Topic)tobeMapped.get(i)).removeType(t);
            ((Topic)tobeMapped.get(i)).addType(this);
        }
        // change data types
        tobeMapped=new ArrayList();
        iter=ti.dataTypeIndex.iterator();
        while(iter.hasNext()){
            tobeMapped.add(iter.next());
        }
        
        final TopicComparator topicComparator=new TopicComparator();
        final ScopeComparator scopeComparator=new ScopeComparator();
        // The sorting guarantees that merges in identical topic maps
        // always have identical results. Which occurrence ends up being used
        // when types of several collide is undefined but is deterministic
        // with the sorted array. For same rason the array is sorted for
        // some other cases down below, but not all of them need it.
        Collections.sort(tobeMapped,topicComparator);
        
        for(int i=0;i<tobeMapped.size();i++){
            Hashtable val=((Topic)tobeMapped.get(i)).getData(t);
            ((Topic)tobeMapped.get(i)).removeData(t);
            ((Topic)tobeMapped.get(i)).setData(this,val);
        }
        // change data versions
        tobeMapped=new ArrayList();
        iter=ti.dataVersionIndex.iterator();
        while(iter.hasNext()){
            tobeMapped.add(iter.next());
        }        
        Collections.sort(tobeMapped,new Comparator<DataVersionIndexWrapper>(){
            @Override
            public int compare(DataVersionIndexWrapper o1, DataVersionIndexWrapper o2) {
                int c=topicComparator.compare(o1.topic, o2.topic);
                if(c!=0) return c;
                return topicComparator.compare(o1.type,o2.type);
            }
        });
        for(int i=0;i<tobeMapped.size();i++){
//            Topic[] info=(Topic[])tobeMapped.get(i);
            DataVersionIndexWrapper info=(DataVersionIndexWrapper)tobeMapped.get(i);
            String val=info.topic.getData(info.type,t);
            info.topic.removeData(info.type,t);
            info.topic.setData(info.type,this,val);
        }
        // change role types
        tobeMapped=new ArrayList();
        tobeMapped.addAll(ti.roleTypeIndex);
        for(int i=0;i<tobeMapped.size();i++){
            Association a=((Association)tobeMapped.get(i));
            Topic p=a.getPlayer(t);
//            a.removePlayer(t);
//            if(p!=null) a.addPlayer(p,this);
            // Doing these in this order guarantees that we don't lose anything
            // to unintended merges. There might be a merge after the add, but
            // if that is the case, the two associations would merge in the end
            // anyway, so we still end up with the correct result.
            if(p!=null) a.addPlayer(p,this);
            a.removePlayer(t);
        }
        // change variant scopes
        tobeMapped=new ArrayList();
        iter=ti.variantScopeIndex.iterator();
        while(iter.hasNext()){
            Topic topic=(Topic)iter.next();
            Collection scopes=new HashSet();
            scopes.addAll(topic.getVariantScopes());
            Iterator iter2=scopes.iterator();
            while(iter2.hasNext()){
                Collection c=(Collection)iter2.next();
                if(c.contains(t)){
                    tobeMapped.add(Tuples.t2(topic,c));
//                    tobeMapped.add(topic);
//                    tobeMapped.add(c);
                }
            }
        }
        Collections.sort(tobeMapped,new Comparator<Tuples.T2<Topic,Set<Topic>>>(){
            @Override
            public int compare(Tuples.T2<Topic,Set<Topic>> t1, Tuples.T2<Topic,Set<Topic>> t2) {
                int c=topicComparator.compare(t1.e1, t2.e1);
                if(c!=0) return c;
                return scopeComparator.compare(t1.e2, t2.e2);
            }
        });
        for(int i=0;i<tobeMapped.size();i++){
            Tuples.T2 t2=(Tuples.T2)tobeMapped.get(i);
            Topic topic=(Topic)t2.e1;
            Set c=(Set)t2.e2;
//            Topic topic=(Topic)tobeMapped.get(i);
//            Set c=(Set)tobeMapped.get(i+1);
            HashSet newscope=new HashSet();
            newscope.addAll(c);
            newscope.remove(t);
            newscope.add(this);
            String name=topic.getVariant(c);
            topic.removeVariant(c);
            topic.setVariant(newscope,name);
        }
        
        // set subject identifiers, do this last as some other things rely
        // on topics still having subject identifiers
        HashSet copied=new HashSet();
        copied.addAll(ti.getSubjectIdentifiers());
        iter=copied.iterator();
        while(iter.hasNext()){
            Locator l=(Locator)iter.next();
            ti.removeSubjectIdentifier(l);
            this.addSubjectIdentifier(l);
        }        
        
        // check for duplicate associations
        removeDuplicateAssociations();
        // remove merged topic
        try{
            t.remove();
        }catch(TopicInUseException e){
            System.out.println("ERROR couldn't delete merged topic, topic in use. There is a bug in the code if this happens. "+e.getReason());
        }
        topicMap.topicChanged(this);
    }
    
    
    void removeDuplicateAssociations() throws TopicMapException {
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
        Hashtable as=new Hashtable();
        ArrayList tobeDeleted=new ArrayList();
        Iterator iter=associations.iterator();
        Association remaining=notThis;
        while(iter.hasNext()){
            AssociationImpl a=(AssociationImpl)iter.next();
            EqualAssociationWrapper eaw=new EqualAssociationWrapper(a);
            if(as.containsKey(eaw)){
                if(notThis!=null && notThis==a){
                    tobeDeleted.add(as.get(eaw));
                    as.put(eaw,a);
                }
                else tobeDeleted.add(a);
            }
            else {
                if(remaining!=null) remaining=a;
                as.put(eaw,a);
            }
        }
        iter=tobeDeleted.iterator();
        while(iter.hasNext()){
            AssociationImpl a=(AssociationImpl)iter.next();
            topicMap.duplicateAssociationRemoved(remaining,a);
            a.remove();
        }
    }
    
    void addInAssociation(Association a,Topic role) throws TopicMapException {
        associations.add(a);
        Topic type=a.getType();
        Hashtable t = null;
        if(type != null) {
            t=(Hashtable)associationIndex.get(type);
        }
        if(t==null){
            t=new Hashtable();
            associationIndex.put(type,t);
        }
        HashSet s=(HashSet)t.get(role);
        if(s==null){
            s=new HashSet();
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
        Hashtable t=(Hashtable)associationIndex.get(type);
        HashSet s=(HashSet)t.get(role);
        s.remove(a);
        if(s.isEmpty()) t.remove(role);
        if(t.isEmpty()) associationIndex.remove(type);
        dependentTopics=null;
    }
    
    
    void associationTypeChanged(Association a,Topic type,Topic oldType,Topic role){
        Hashtable t=(Hashtable)associationIndex.get(oldType);
        HashSet s=(HashSet)t.get(role);
        s.remove(a);
        if(s.isEmpty()) t.remove(role);
        if(t.isEmpty()) associationIndex.remove(oldType);
        
        if(type!=null){
            t=(Hashtable)associationIndex.get(type);
            if(t==null){
                t=new Hashtable();
                associationIndex.put(type,t);
            }
            s=(HashSet)t.get(role);
            if(s==null){
                s=new HashSet();
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
            try{
                Locator l1=o1.getFirstSubjectIdentifier();
                Locator l2=o2.getFirstSubjectIdentifier();
                if(l1==null && l2==null) return 0;
                else if(l1==null) return -1;
                else if(l2==null) return 1;
                else return l1.compareTo(l2);
            }catch(TopicMapException tme){
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

            ArrayList<Topic> l1=new ArrayList<Topic>(o1);
            ArrayList<Topic> l2=new ArrayList<Topic>(o2);
            Collections.sort(l1,topicComparator);
            Collections.sort(l2,topicComparator);
            for(int i=0;i<l1.size();i++){
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

