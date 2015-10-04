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
 * 
 */

package org.wandora.topicmap.linked;
import org.wandora.topicmap.*;
import java.util.*;
import org.wandora.application.Wandora;
import org.wandora.topicmap.layered.*;

/**
 *
 * @author olli
 */
public class LinkedTopicMap extends TopicMap implements TopicMapListener, ContainerTopicMapListener {

    protected TopicMap linkedMap;
    protected Layer linkedLayer;
    protected ContainerTopicMap wrappedContainer;
    protected ArrayList<TopicMapListener> topicMapListeners;
    
    protected String wrappedName;
    
    public LinkedTopicMap(TopicMap linkedMap){
        setLinkedMap(linkedMap);
        this.topicMapListeners=new ArrayList<TopicMapListener>();
    }
    
    public LinkedTopicMap(String wrappedName){
        this.wrappedName=wrappedName;
        this.topicMapListeners=new ArrayList<TopicMapListener>();
    }
    
    // -------------------------------------------------------------------------
    
    public TopicMap getLinkedTopicMap(){
        if(linkedMap==null && !findLinkedMap()) return null;
        return linkedMap;
    }
    
    protected void unlinkMap(){
        if(linkedLayer!=null) wrappedName=linkedLayer.getName();
        linkedMap=null;
        linkedLayer=null;
    }
    
    protected void setLinkedMap(Layer l){
        linkedLayer=l;
        linkedMap=l.getTopicMap();
        wrappedName=l.getName();
        if(wrappedContainer!=null) wrappedContainer.removeContainerListener(this);
        wrappedContainer=(ContainerTopicMap)linkedMap.getParentTopicMap();
        if(wrappedContainer!=null) wrappedContainer.addContainerListener(this);
    }
    
    protected void setLinkedMap(TopicMap tm){
        linkedMap=tm;
        TopicMap root=this.getRootTopicMap();
        if(root==null || root==this || !(root instanceof ContainerTopicMap)) return;
        ContainerTopicMap rootStack=(ContainerTopicMap)root;
        for(Layer l : rootStack.getTreeLayers()){
            if(l.getTopicMap()==tm){
                linkedLayer=l;
                wrappedName=l.getName();
                if(wrappedContainer!=null) wrappedContainer.removeContainerListener(this);
                wrappedContainer=(ContainerTopicMap)linkedMap.getParentTopicMap();
                if(wrappedContainer!=null) wrappedContainer.addContainerListener(this);
                break;
            }
        }
    }
    
    protected boolean findLinkedMap(){
        if(wrappedName==null) return false;
        TopicMap root=this.getRootTopicMap();
        if(root==null || root==this || !(root instanceof ContainerTopicMap)) return false;
        ContainerTopicMap rootStack=(ContainerTopicMap)root;
        Layer l=rootStack.getTreeLayer(wrappedName);
        if(l==null) return false;
        setLinkedMap(l);
        if(topicMapListeners.size()>0) linkedMap.addTopicMapListener(this);
        return true;
    }
    
    public LinkedTopic getLinkedTopic(Topic t){
        if(t==null) return null;
        return new LinkedTopic(t,this);
    }
    
    public LinkedAssociation getLinkedAssociation(Association a){
        if(a==null) return null;
        return new LinkedAssociation(a,this);
    }
    
    public Collection<Topic> getLinkedTopics(Collection<Topic> topics){
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(Topic t : topics){
            ret.add(getLinkedTopic(t));
        }
        return ret;
    }
    
    public Set<Topic> getLinkedTopics(Set<Topic> topics){
        HashSet<Topic> ret=new HashSet<Topic>();
        for(Topic t : topics){
            ret.add(getLinkedTopic(t));
        }
        return ret;
    }
    
    public Collection<Association> getLinkedAssociations(Collection<Association> associations){
        ArrayList<Association> ret=new ArrayList<Association>();
        for(Association a : associations){
            ret.add(getLinkedAssociation(a));
        }
        return ret;
    }
    
    public Topic getUnlinkedTopic(Topic t){
        if(t==null) return null;
        return ((LinkedTopic)t).getWrappedTopic();
    }
    
    public Association getUnlinkedAssociation(Association a){
        if(a==null) return null;
        return ((LinkedAssociation)a).getWrappedAssociation();
    }
    
    public Collection<Topic> getUnlinkedTopics(Collection<Topic> topics){
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(Topic t : topics){
            ret.add(getUnlinkedTopic(t));
        }
        return ret;        
    }
    
    public Set<Topic> getUnlinkedSetOfTopics(Set<Topic> topics){
        HashSet<Topic> ret=new HashSet<Topic>();
        for(Topic t : topics){
            ret.add(getUnlinkedTopic(t));
        }
        return ret;        
    }
    
    public Collection<Association> getUnlinkedAssociations(Collection<Association> associations){
        ArrayList<Association> ret=new ArrayList<Association>();
        for(Association a : associations){
            ret.add(getUnlinkedAssociation(a));
        }
        return ret;
    }
    
    public List<TopicMapListener> getTopicMapListeners(){
        return topicMapListeners;
    }
    
    @Override
    public void addTopicMapListener(TopicMapListener listener) {
        if(topicMapListeners.isEmpty() && linkedMap!=null) linkedMap.addTopicMapListener(this);
        if(!topicMapListeners.contains(listener)) topicMapListeners.add(listener);
    }

    @Override
    public void removeTopicMapListener(TopicMapListener listener) {
        topicMapListeners.add(listener);
        if(topicMapListeners.size()==0 && linkedMap!=null) linkedMap.removeTopicMapListener(this);
    }
    
    @Override
    public void clearTopicMap() throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(linkedMap==null && !findLinkedMap()) return;
        linkedMap.clearTopicMap();
    }

    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return;
        linkedMap.clearTopicMapIndexes();
    }

    @Override
    public Association copyAssociationIn(Association a) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(linkedMap==null && !findLinkedMap()) return null;
        return linkedMap.copyAssociationIn(a);
    }

    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(linkedMap==null && !findLinkedMap()) return;
        linkedMap.copyTopicAssociationsIn(t);
    }

    @Override
    public Topic copyTopicIn(Topic t, boolean deep) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(linkedMap==null && !findLinkedMap()) return null;
        return linkedMap.copyTopicIn(t,deep);
    }

    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(linkedMap==null && !findLinkedMap()) return null;
        return getLinkedAssociation(linkedMap.createAssociation(getUnlinkedTopic(type)));
    }

    @Override
    public Topic createTopic() throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(linkedMap==null && !findLinkedMap()) return null;
        return getLinkedTopic(linkedMap.createTopic());
    }

    @Override
    public void disableAllListeners() {
        if(linkedMap==null && !findLinkedMap()) return;
        linkedMap.disableAllListeners();
    }

    @Override
    public void enableAllListeners() {
        if(linkedMap==null && !findLinkedMap()) return;
        linkedMap.enableAllListeners();
    }

    @Override
    public Iterator<Association> getAssociations() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return new ArrayList<Association>().iterator();
        final Iterator<Association> iter=linkedMap.getAssociations();
        return new Iterator<Association>(){

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Association next() {
                return getLinkedAssociation(iter.next());
            }

            public void remove() {
                iter.remove();
            }
        };
    }

    @Override
    public Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return new ArrayList<Association>();
        return getLinkedAssociations(linkedMap.getAssociationsOfType(getUnlinkedTopic(type)));
    }

    @Override
    public int getNumAssociations() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return 0;
        return linkedMap.getNumAssociations();
    }

    @Override
    public int getNumTopics() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return 0;
        return linkedMap.getNumTopics();
    }

    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return null;
        return linkedMap.getStatistics(options);
    }

    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return null;
        return getLinkedTopic(linkedMap.getTopic(si));
    }

    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return null;
        return getLinkedTopic(linkedMap.getTopicBySubjectLocator(sl));
    }

    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return null;
        return getLinkedTopic(linkedMap.getTopicWithBaseName(name));
    }

    @Override
    public Iterator<Topic> getTopics() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return new ArrayList<Topic>().iterator();
        final Iterator<Topic> iter=linkedMap.getTopics();
        return new TopicIterator(){

            @Override
            public void dispose() {
                if(iter instanceof TopicIterator) ((TopicIterator)iter).dispose();
                else while(iter.hasNext()) iter.next();
            }

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Topic next() {
                return getLinkedTopic(iter.next());
            }

            public void remove() {
                iter.remove();
            }
            
        };
    }

    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return new Topic[sis.length];
        Topic[] ts=linkedMap.getTopics(sis);
        Topic[] ret=new Topic[ts.length];
        for(int i=0;i<ts.length;i++){
            ret[i]=getLinkedTopic(ts[i]);
        }
        return ret;
    }

    @Override
    public Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return null;
        return getLinkedTopics(linkedMap.getTopicsOfType(getUnlinkedTopic(type)));
    }

    @Override
    public boolean isTopicMapChanged() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return false;
        return linkedMap.isTopicMapChanged();
    }

    @Override
    public boolean resetTopicMapChanged() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return false;
        return linkedMap.resetTopicMapChanged();
    }

    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return new ArrayList<Topic>();
        return getLinkedTopics(linkedMap.search(query,options));
    }

    @Override
    public void setTrackDependent(boolean v) throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return;
        linkedMap.setTrackDependent(v);
    }

    @Override
    public boolean trackingDependent() throws TopicMapException {
        if(linkedMap==null && !findLinkedMap()) return false;
        return linkedMap.trackingDependent();
    }

    // -------------------------------------------------- TOPIC MAP LISTENER ---
    
    @Override
    public void associationChanged(Association a) throws TopicMapException {
        Association wa=getLinkedAssociation(a);
        for(TopicMapListener listener : topicMapListeners){
            listener.associationChanged(wa);
        }
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        Association wa=getLinkedAssociation(a);
        Topic wrole=getLinkedTopic(role);
        Topic wNewPlayer=getLinkedTopic(newPlayer);
        Topic wOldPlayer=getLinkedTopic(oldPlayer);
        for(TopicMapListener listener : topicMapListeners){
            listener.associationPlayerChanged(wa,wrole,wNewPlayer,wOldPlayer);
        }
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        Association wa=getLinkedAssociation(a);
        for(TopicMapListener listener : topicMapListeners){
            listener.associationRemoved(wa);
        }
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        Association wa=getLinkedAssociation(a);
        Topic wNewType=getLinkedTopic(newType);
        Topic wOldType=getLinkedTopic(oldType);
        for(TopicMapListener listener : topicMapListeners){
            listener.associationTypeChanged(wa,wNewType,wOldType);
        }
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicBaseNameChanged(wt,newName,oldName);
        }
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicChanged(wt);
        }
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        Topic wtype=getLinkedTopic(type);
        Topic wversion=getLinkedTopic(version);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicDataChanged(wt,wtype,wversion,newValue,oldValue);
        }
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicRemoved(wt);
        }
    }

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicSubjectIdentifierChanged(wt,added,removed);
        }
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicSubjectLocatorChanged(wt,newLocator,oldLocator);
        }
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        Topic wadded=getLinkedTopic(added);
        Topic wremoved=getLinkedTopic(removed);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicTypeChanged(wt,wadded,wremoved);
        }
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        Topic wt=getLinkedTopic(t);
        Collection<Topic> wscope=getLinkedTopics(scope);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicVariantChanged(wt,wscope,newName,oldName);
        }
    }

    // -------------------------------------------------------------------------
    
    @Override
    public void layerAdded(Layer l) {}

    @Override
    public void layerChanged(Layer oldLayer, Layer newLayer) {
        if(oldLayer==linkedLayer) unlinkMap();
    }

    @Override
    public void layerRemoved(Layer l) {
        if(l==linkedLayer) unlinkMap();
    }

    @Override
    public void layerStructureChanged() {
        unlinkMap();
    }

    @Override
    public void layerVisibilityChanged(Layer l) {}
    
}
