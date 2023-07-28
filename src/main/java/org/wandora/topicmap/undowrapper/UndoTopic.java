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
 */



package org.wandora.topicmap.undowrapper;

import java.util.*;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */


public class UndoTopic extends Topic {
    
    private UndoTopicMap topicMap;
    private Topic wrapped;
    
    UndoTopic(Topic wrapped,UndoTopicMap topicMap){
        this.wrapped=wrapped;
        this.topicMap=topicMap;
    }
    
    static Set<Topic> getWrappedScope(Set<Topic> s){
        HashSet<Topic> ret=new LinkedHashSet<Topic>();
        for(Topic t : s){
            ret.add(((UndoTopic)t).getWrapped());
        }
        return ret;
    }
    
    Set<Topic> wrapScope(Set<Topic> s){
        HashSet<Topic> ret=new LinkedHashSet<Topic>();
        for(Topic t : s) {
            ret.add(topicMap.wrapTopic(t));
        }
        return ret;
    }
    
    Topic getWrapped(){
        return wrapped;
    }
    
    private boolean undoCreated() throws TopicMapException {
        return !wrapped.getSubjectIdentifiers().isEmpty();
    }

    @Override
    public String getID() throws TopicMapException {
        return wrapped.getID();
    }

    @Override
    public Collection<Locator> getSubjectIdentifiers() throws TopicMapException {
        return wrapped.getSubjectIdentifiers();
    }

    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        if(wrapped.getSubjectIdentifiers().isEmpty()){
            wrapped.addSubjectIdentifier(l);
            try {
                topicMap.addUndoOperation(new CreateTopicOperation(wrapped));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }
        }
        else {
            try {
                topicMap.addUndoOperation(new AddSubjectIdentifierOperation(wrapped, l));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
            wrapped.addSubjectIdentifier(l);
        }
    }

    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        if(wrapped.getSubjectIdentifiers().size()==1 && wrapped.getSubjectIdentifiers().contains(l)){
            try {
                topicMap.addUndoOperation(new RemoveTopicOperation(wrapped));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        }
        else {
            try {
                topicMap.addUndoOperation(new RemoveSubjectIdentifierOperation(wrapped, l));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        }
        wrapped.removeSubjectIdentifier(l);
    }

    @Override
    public String getBaseName() throws TopicMapException {
        return wrapped.getBaseName();
    }

    @Override
    public void setBaseName(String name) throws TopicMapException {
        try {
            if(undoCreated()) topicMap.addUndoOperation(new SetBaseNameOperation(wrapped, name));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.setBaseName(name);
    }

    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        return topicMap.wrapTopics(wrapped.getTypes());
    }

    @Override
    public void addType(Topic t) throws TopicMapException {
        Topic wtype=((UndoTopic)t).getWrapped();
        try {
            if(undoCreated()) topicMap.addUndoOperation(new AddTypeOperation(wrapped, wtype));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.addType(wtype);
    }

    @Override
    public void removeType(Topic t) throws TopicMapException {
        Topic wtype=((UndoTopic)t).getWrapped();
        try {
            if(undoCreated()) topicMap.addUndoOperation(new RemoveTypeOperation(wrapped, wtype));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.removeType(wtype);
    }

    @Override
    public boolean isOfType(Topic t) throws TopicMapException {
        return wrapped.isOfType(((UndoTopic)t).getWrapped());
    }

    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        return wrapped.getVariant(getWrappedScope(scope));
    }

    @Override
    public void setVariant(Set<Topic> scope, String name) throws TopicMapException {
        Set<Topic> wscope=getWrappedScope(scope);
        try {
            if(undoCreated()) topicMap.addUndoOperation(new SetVariantOperation(wrapped,wscope,name));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.setVariant(wscope,name);
    }

    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        Set<Set<Topic>> ret=new LinkedHashSet<Set<Topic>>();
        for(Set<Topic> s : wrapped.getVariantScopes()){
            ret.add(wrapScope(s));
        }
        return ret;
    }

    @Override
    public void removeVariant(Set<Topic> scope) throws TopicMapException {
        Set<Topic> wscope=getWrappedScope(scope);
        try {
            if(undoCreated()) topicMap.addUndoOperation(new SetVariantOperation(wrapped,wscope,null));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.removeVariant(wscope);
    }

    @Override
    public String getData(Topic type, Topic version) throws TopicMapException {
        return wrapped.getData(((UndoTopic)type).getWrapped(),((UndoTopic)version).getWrapped());
    }

    @Override
    public Hashtable<Topic, String> getData(Topic type) throws TopicMapException {
        Hashtable<Topic, String> ret=new Hashtable<Topic, String>();
        for(Map.Entry<Topic,String> e : wrapped.getData(((UndoTopic)type).getWrapped()).entrySet() ){
            ret.put(topicMap.wrapTopic(e.getKey()),e.getValue());
        }
        return ret;
    }

    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        return topicMap.wrapTopics(wrapped.getDataTypes());
    }

    @Override
    public void setData(Topic type, Hashtable<Topic, String> versionData) throws TopicMapException {
        Topic wtype=((UndoTopic)type).getWrapped();
        Hashtable data=new Hashtable<Topic, String>();
        for(Map.Entry<Topic,String> e : versionData.entrySet()){
            Topic wversion=((UndoTopic)e.getKey()).getWrapped();
            String value=e.getValue();
            data.put(wversion,value);
            
            try {
                if(undoCreated()) topicMap.addUndoOperation(new SetOccurrenceOperation(wrapped,wtype,wversion,value));
            } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
            
        }
        wrapped.setData(wtype,data);
    }

    @Override
    public void setData(Topic type, Topic version, String value) throws TopicMapException {
        Topic wtype=((UndoTopic)type).getWrapped();
        Topic wversion=((UndoTopic)version).getWrapped();
        try {
            if(undoCreated()) topicMap.addUndoOperation(new SetOccurrenceOperation(wrapped,wtype,wversion,value));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.setData(wtype,wversion,value);
    }

    @Override
    public void removeData(Topic type, Topic version) throws TopicMapException {
        Topic wtype=((UndoTopic)type).getWrapped();
        Topic wversion=((UndoTopic)version).getWrapped();
        try {
            if(undoCreated()) topicMap.addUndoOperation(new SetOccurrenceOperation(wrapped,wtype,wversion,null));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.removeData(wtype,wversion);
    }

    @Override
    public void removeData(Topic type) throws TopicMapException {
        Topic wtype=((UndoTopic)type).getWrapped();
        Hashtable<Topic,String> oldData=getWrapped().getData(type);
        if(oldData != null) {
            for(Map.Entry<Topic,String> e : oldData.entrySet()){
                Topic wversion=((UndoTopic)e.getKey()).getWrapped();
                try {
                    if(undoCreated()) topicMap.addUndoOperation(new SetOccurrenceOperation(wrapped,wtype,wversion,null));
                } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
            }
            wrapped.removeData(wtype);
        }
    }

    @Override
    public Locator getSubjectLocator() throws TopicMapException {
        return wrapped.getSubjectLocator();
    }

    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException {
        try {
            if(undoCreated()) topicMap.addUndoOperation(new SetSubjectLocatorOperation(wrapped, l));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.setSubjectLocator(l);
    }

    @Override
    public TopicMap getTopicMap() {
        return topicMap;
    }

    @Override
    public Collection<Association> getAssociations() throws TopicMapException {
        return topicMap.wrapAssociations(wrapped.getAssociations());
    }

    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        return topicMap.wrapAssociations(wrapped.getAssociations(((UndoTopic)type).getWrapped()));
    }

    @Override
    public Collection<Association> getAssociations(Topic type, Topic role) throws TopicMapException {
        return topicMap.wrapAssociations(wrapped.getAssociations(((UndoTopic)type).getWrapped(),((UndoTopic)role).getWrapped()));
    }

    @Override
    public void remove() throws TopicMapException {
        try {
            if(undoCreated()) topicMap.addUndoOperation(new RemoveTopicOperation(wrapped));
        } catch(UndoException ue){ topicMap.handleUndoException(ue); }            
        wrapped.remove();
    }

    @Override
    public long getEditTime() throws TopicMapException {
        return wrapped.getEditTime();
    }

    @Override
    public void setEditTime(long time) throws TopicMapException {
        wrapped.setEditTime(time);
    }

    @Override
    public long getDependentEditTime() throws TopicMapException {
        return wrapped.getDependentEditTime();
    }

    @Override
    public void setDependentEditTime(long time) throws TopicMapException {
        wrapped.setDependentEditTime(time);
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return wrapped.isRemoved();
    }

    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        return wrapped.isDeleteAllowed();
    }

    @Override
    public Collection<Topic> getTopicsWithDataType() throws TopicMapException {
        return topicMap.wrapTopics(wrapped.getTopicsWithDataType());
    }

    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        return topicMap.wrapTopics(wrapped.getTopicsWithDataVersion());
    }

    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        return topicMap.wrapTopics(wrapped.getTopicsWithVariantScope());
    }

    
    @Override
    public Collection<Association> getAssociationsWithType() throws TopicMapException {
        return topicMap.wrapAssociations(wrapped.getAssociationsWithType());
    }

    @Override
    public Collection<Association> getAssociationsWithRole() throws TopicMapException {
        return topicMap.wrapAssociations(wrapped.getAssociationsWithType());
    }
    
}
