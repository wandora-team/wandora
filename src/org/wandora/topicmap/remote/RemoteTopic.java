/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * RemoteTopic.java
 *
 * Created on August 16, 2004, 12:25 PM
 */

package org.wandora.topicmap.remote;
import org.wandora.topicmap.*;
import org.wandora.topicmap.memory.*;
import java.util.*;

/**
 *
 * @author  olli
 */
public class RemoteTopic extends TopicImpl {
    
    private RemoteTopicMap topicMap;
    
    /** Creates a new instance of RemoteTopic */
    public RemoteTopic(RemoteTopicMap topicMap) {
        super(topicMap);
        this.topicMap=topicMap;
    }
    
    private void makeFull() throws TopicMapException {
//        topicMap.refreshTopic(this);
        topicMap.makeFull(this);
    }
    
    @Override
    public Collection getAssociations()  throws TopicMapException {
        makeFull();
        return super.getAssociations();
    }
    
    @Override
    public Collection getAssociations(Topic type) throws TopicMapException  {
        makeFull();
        return super.getAssociations(type);
    }
    
    @Override
    public Collection getAssociations(Topic type, Topic role)  throws TopicMapException {
        makeFull();
        return super.getAssociations(type,role);
    }
    
    @Override
    public String getBaseName()  throws TopicMapException {
        return super.getBaseName();
    }
    
    @Override
    public Hashtable getData(Topic type)  throws TopicMapException {
        return super.getData(type);
    }
    
    @Override
    public String getData(Topic type, Topic version)  throws TopicMapException {
        return super.getData(type,version);
    }
    
    @Override
    public Collection getDataTypes() throws TopicMapException  {
        return super.getDataTypes();
    }
    
    // note that this method is not very usefull with RemoteTopics
    @Override
    public long getDependentEditTime()  throws TopicMapException {
        return super.getDependentEditTime();
    }
    
    @Override
    public long getEditTime()  throws TopicMapException {
        return super.getEditTime();
    }
    
    @Override
    public String getID()  throws TopicMapException {
        return super.getID();
    }
    
    @Override
    public Collection getSubjectIdentifiers()  throws TopicMapException {
        return super.getSubjectIdentifiers();
    }
    
    @Override
    public Locator getSubjectLocator()  throws TopicMapException {
        return super.getSubjectLocator();
    }
    
    @Override
    public TopicMap getTopicMap() {
        return topicMap;
    }
    
    @Override
    public Collection getTypes()  throws TopicMapException {
        makeFull();
        return super.getTypes();
    }
    
    @Override
    public String getVariant(Set scope)  throws TopicMapException {
        return super.getVariant(scope);
    }
    
    @Override
    public Set getVariantScopes()  throws TopicMapException {
        return super.getVariantScopes();
    }
    
    // note that the behaviour of this method in RemoteTopcis is somewhat fuzzy, i.e. it is based only on the currently fetched topic map
    @Override
    public boolean isDeleteAllowed()  throws TopicMapException {
        return super.isDeleteAllowed();
    }
    
    @Override
    public boolean isOfType(Topic t)  throws TopicMapException {
        return super.isOfType(t);
    }
    
    @Override
    public boolean isRemoved()  throws TopicMapException {
        return super.isRemoved();
    }
    
    @Override
    public void removeData(Topic type)  throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        Iterator iter=this.getData(type).entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            Topic version=(Topic)e.getKey();
            topicMap.removeTopicData(this,type,version);
        }
        super.removeData(type);
    }
    
    @Override
    public void removeData(Topic type, Topic version)  throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        topicMap.removeTopicData(this,type,version);
        super.removeData(type,version);
    }
    
    @Override
    public void removeVariant(Set scope) throws TopicMapException  {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        topicMap.removeTopicVariant(this,scope);
        super.removeVariant(scope);
    }
    
    @Override
    public void setData(Topic type, Hashtable versionData)  throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        Iterator iter=versionData.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            Topic version=(Topic)e.getKey();
            topicMap.setTopicData(this, type,version);
        }
        super.setData(type,versionData);
    }
    
    @Override
    public void setData(Topic type, Topic version, String value)  throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        String orig=getData(type,version);
        if(orig==null || !orig.equals(value)) topicMap.setTopicData(this,type,version);
        super.setData(type,version,value);
    }
    
    @Override
    public void setBaseName(String name) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        String orig=getBaseName();
        if(name==null || !name.equals(orig)) topicMap.removeTopicBaseName(this);
        super.setBaseName(name);
    }
        
    @Override
    public void setVariant(Set scope, String name)  throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        String orig=getVariant(scope);
        if(name==null || !name.equals(orig)) topicMap.removeTopicVariant(this,scope);
        super.setVariant(scope,name);
    }
    @Override
    public void mergeIn(Topic t) throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        topicMap.importing++;
        super.mergeIn(t);
        topicMap.importing--;
    }

    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException  {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        super.addSubjectIdentifier(l);
    }    
    
    @Override
    public void addType(Topic t)  throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        super.addType(t);
    }    
    
    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException  {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        super.removeSubjectIdentifier(l);
    }    
    
    @Override
    public void removeType(Topic t) throws TopicMapException  {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        super.removeType(t);
    }    
    
    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException  {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        makeFull();
        super.setSubjectLocator(l);
    }    
    
    // TODO: new operations are needed in ServerInterface to implement these
    @Override
    public Collection getAssociationsWithRole() {
        throw new RuntimeException("Not implemented");
    }
    @Override
    public Collection getAssociationsWithType() {
        throw new RuntimeException("Not implemented");
    }
    @Override
    public Collection getTopicsWithDataType() {
        throw new RuntimeException("Not implemented");
    }
    
}
