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
 */

package org.wandora.topicmap.linked;


import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class LinkedTopic extends Topic{

    protected Topic wrappedTopic;
    protected LinkedTopicMap topicMap;
    
    public LinkedTopic(Topic wrappedTopic,LinkedTopicMap topicMap){
        this.wrappedTopic=wrappedTopic;
        this.topicMap=topicMap;
    }
    
    public Topic getWrappedTopic(){return wrappedTopic;}
    
    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        wrappedTopic.addSubjectIdentifier(l);
    }

    @Override
    public void addType(Topic t) throws TopicMapException {
        wrappedTopic.addType(topicMap.getUnlinkedTopic(t));
    }

    @Override
    public Collection<Association> getAssociations() throws TopicMapException {
        return topicMap.getLinkedAssociations(wrappedTopic.getAssociations());
    }

    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        return topicMap.getLinkedAssociations(wrappedTopic.getAssociations(topicMap.getUnlinkedTopic(type)));
    }

    @Override
    public Collection<Association> getAssociations(Topic type, Topic role) throws TopicMapException {
        return topicMap.getLinkedAssociations(wrappedTopic.getAssociations(topicMap.getUnlinkedTopic(type),
                                                                    topicMap.getUnlinkedTopic(role)));
    }

    @Override
    public Collection<Association> getAssociationsWithRole() throws TopicMapException {
        return topicMap.getLinkedAssociations(wrappedTopic.getAssociationsWithRole());
    }

    @Override
    public Collection<Association> getAssociationsWithType() throws TopicMapException {
        return topicMap.getLinkedAssociations(wrappedTopic.getAssociationsWithType());
    }

    @Override
    public String getBaseName() throws TopicMapException {
        return wrappedTopic.getBaseName();
    }

    @Override
    public String getData(Topic type, Topic version) throws TopicMapException {
        return wrappedTopic.getData(topicMap.getUnlinkedTopic(type),topicMap.getUnlinkedTopic(version));
    }

    @Override
    public Hashtable<Topic, String> getData(Topic type) throws TopicMapException {
        Hashtable<Topic,String> data=wrappedTopic.getData(topicMap.getUnlinkedTopic(type));
        Hashtable<Topic,String> ret=new Hashtable<Topic,String>();
        for(Map.Entry<Topic,String> e : data.entrySet()){
            ret.put(topicMap.getLinkedTopic(e.getKey()),e.getValue());
        }
        return ret;
    }

    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        return topicMap.getLinkedTopics(wrappedTopic.getDataTypes());
    }

    @Override
    public long getDependentEditTime() throws TopicMapException {
        return wrappedTopic.getDependentEditTime();
    }

    @Override
    public long getEditTime() throws TopicMapException {
        return wrappedTopic.getEditTime();
    }

    @Override
    public String getID() throws TopicMapException {
        return wrappedTopic.getID();
    }

    @Override
    public Collection<Locator> getSubjectIdentifiers() throws TopicMapException {
        return wrappedTopic.getSubjectIdentifiers();
    }

    @Override
    public Locator getSubjectLocator() throws TopicMapException {
        return wrappedTopic.getSubjectLocator();
    }

    @Override
    public TopicMap getTopicMap() {
        return topicMap;
    }

    @Override
    public Collection<Topic> getTopicsWithDataType() throws TopicMapException {
        return topicMap.getLinkedTopics(wrappedTopic.getTopicsWithDataType());
    }

    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        return topicMap.getLinkedTopics(wrappedTopic.getTopicsWithDataVersion());
    }

    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        return topicMap.getLinkedTopics(wrappedTopic.getTopicsWithVariantScope());
    }

    
    
    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        return topicMap.getLinkedTopics(wrappedTopic.getTypes());
    }

    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        return wrappedTopic.getVariant(topicMap.getUnlinkedSetOfTopics(scope));
    }

    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        Set<Set<Topic>> scopes=wrappedTopic.getVariantScopes();
        Set<Set<Topic>> ret=new HashSet<Set<Topic>>();
        for(Set<Topic> scope : scopes){
            ret.add(topicMap.getLinkedTopics(scope));
        }
        return ret;
    }

    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        return wrappedTopic.isDeleteAllowed();
    }

    @Override
    public boolean isOfType(Topic t) throws TopicMapException {
        return wrappedTopic.isOfType(topicMap.getUnlinkedTopic(t));
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return wrappedTopic.isRemoved();
    }

    @Override
    public void remove() throws TopicMapException {
        wrappedTopic.remove();
    }

    @Override
    public void removeData(Topic type, Topic version) throws TopicMapException {
        wrappedTopic.removeData(topicMap.getUnlinkedTopic(type),topicMap.getUnlinkedTopic(version));
    }

    @Override
    public void removeData(Topic type) throws TopicMapException {
        wrappedTopic.removeData(topicMap.getUnlinkedTopic(type));
    }

    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        wrappedTopic.removeSubjectIdentifier(l);
    }

    @Override
    public void removeType(Topic t) throws TopicMapException {
        wrappedTopic.removeType(topicMap.getUnlinkedTopic(t));
    }

    @Override
    public void removeVariant(Set<Topic> scope) throws TopicMapException {
        wrappedTopic.removeVariant(topicMap.getUnlinkedSetOfTopics(scope));
    }

    @Override
    public void setBaseName(String name) throws TopicMapException {
        wrappedTopic.setBaseName(name);
    }

    @Override
    public void setData(Topic type, Hashtable<Topic, String> versionData) throws TopicMapException {
        Hashtable<Topic,String> unwrappedData=new Hashtable<Topic,String>();
        for(Map.Entry<Topic,String> e : versionData.entrySet()){
            unwrappedData.put(topicMap.getUnlinkedTopic(e.getKey()),e.getValue());
        }
        wrappedTopic.setData(topicMap.getUnlinkedTopic(type),unwrappedData);
    }

    @Override
    public void setData(Topic type, Topic version, String value) throws TopicMapException {
        wrappedTopic.setData(topicMap.getUnlinkedTopic(type),topicMap.getUnlinkedTopic(version),value);
    }

    @Override
    public void setDependentEditTime(long time) throws TopicMapException {
        wrappedTopic.setDependentEditTime(time);
    }

    @Override
    public void setEditTime(long time) throws TopicMapException {
        wrappedTopic.setEditTime(time);
    }

    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException {
        wrappedTopic.setSubjectLocator(l);
    }

    @Override
    public void setVariant(Set<Topic> scope, String name) throws TopicMapException {
        wrappedTopic.setVariant(topicMap.getUnlinkedSetOfTopics(scope),name);
    }
    
    @Override
    public int hashCode(){
        return wrappedTopic.hashCode()+topicMap.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(!o.getClass().equals(this.getClass())) return false;
        LinkedTopic lt=(LinkedTopic)o;
        if(lt.topicMap!=topicMap) return false;
        if(!lt.wrappedTopic.equals(wrappedTopic)) return false;
        return true;
    }

}
