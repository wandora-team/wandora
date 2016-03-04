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
 */
package org.wandora.topicmap.tmapi2wandora;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import org.wandora.topicmap.*;
/**
 *
 * @author olli
 */

/*
 This currently trusts equals check to identify equal topics for the
 tmapi part. The spec says that tmapi equals method must just compare 
 objects with ==. Tmapi doesn't provide other starightforward ways to
 check topic map equality of two topics so it is assumed that each topic
 is represented by only one java object.
*/
 

public class T2WTopic extends Topic {
    
    protected T2WTopicMap tm;
    protected org.tmapi.core.Topic t;
    
    public T2WTopic(T2WTopicMap tm,org.tmapi.core.Topic t){
        this.tm=tm;
        this.t=t;
    }
    
    public org.tmapi.core.Topic getWrapped(){
        return t;
    }

    @Override
    public String getID() throws TopicMapException {
        String s=t.getId();
        if(s!=null) return s;
        else return ""+t.hashCode();
    }

    @Override
    public Collection<Locator> getSubjectIdentifiers() throws TopicMapException {
        Set<org.tmapi.core.Locator> ls=t.getSubjectIdentifiers();
        return tm.wrapLocators(ls);
    }

    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    protected org.tmapi.core.Name _getBaseName() {
        org.tmapi.core.TopicMap _tm=t.getParent();
        org.tmapi.core.Topic type=_tm.getTopicBySubjectIdentifier(_tm.createLocator(T2WTopicMap.TOPIC_NAME_SI));
        if(type==null) return null;
        Set<org.tmapi.core.Name> ns=t.getNames(type);
        
        for(org.tmapi.core.Name n : ns){
            if(n.getScope().isEmpty()) return n;
        }        
        return null;
    }
    
    @Override
    public String getBaseName() throws TopicMapException {
        org.tmapi.core.Name n=_getBaseName();
        if(n==null) return null;
        else return n.getValue();
    }

    @Override
    public void setBaseName(String name) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        return tm.wrapTopics(t.getTypes());
    }

    @Override
    public void addType(Topic t) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void removeType(Topic t) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public boolean isOfType(Topic type) throws TopicMapException {
        return t.getTypes().contains(((T2WTopic)type).getWrapped());
    }

    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        org.tmapi.core.Name name=_getBaseName();
        if(name==null) return null;
        
        Set<org.tmapi.core.Topic> _scope=new HashSet<org.tmapi.core.Topic>();
        for(Topic t : scope){
            _scope.add(((T2WTopic)t).getWrapped());
        }
        
        for(org.tmapi.core.Variant v : name.getVariants()){
            if(name.getScope().equals(_scope)) return v.getValue();
        }
        
        return null;
    }

    @Override
    public void setVariant(Set<Topic> scope, String name) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        org.tmapi.core.Name name=_getBaseName();
        if(name==null) return new HashSet<Set<Topic>>();
        
        HashSet<Set<Topic>> ret=new HashSet<Set<Topic>>();
        for(org.tmapi.core.Variant variant : name.getVariants()){
            Set<org.tmapi.core.Topic> _scope=variant.getScope();
            HashSet<Topic> scope=new HashSet<Topic>();
            for(org.tmapi.core.Topic _theme : _scope){
                scope.add(new T2WTopic(tm,_theme));
            }
            ret.add(scope);
        }
        return ret;
    }

    @Override
    public void removeVariant(Set<Topic> scope) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public String getData(Topic type, Topic version) throws TopicMapException {
        Set<org.tmapi.core.Occurrence> os=t.getOccurrences(((T2WTopic)type).getWrapped());
        for(org.tmapi.core.Occurrence o : os){
            Set<org.tmapi.core.Topic> scope=o.getScope();
            if(scope.size()==1){
                org.tmapi.core.Topic _version=scope.iterator().next();
                if(_version==((T2WTopic)version).getWrapped()) {
                    return o.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public Hashtable<Topic, String> getData(Topic type) throws TopicMapException {
        Hashtable<Topic,String> ret=new Hashtable<Topic,String>();
        Set<org.tmapi.core.Occurrence> os=t.getOccurrences(((T2WTopic)type).getWrapped());
        for(org.tmapi.core.Occurrence o : os){
            Set<org.tmapi.core.Topic> scope=o.getScope();
            if(scope.size()==1){
                org.tmapi.core.Topic _version=scope.iterator().next();
                ret.put(new T2WTopic(tm,_version),o.getValue());
            }
        }
        return ret;
    }

    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        HashSet<org.tmapi.core.Topic> _ret=new HashSet<org.tmapi.core.Topic>();
        Set<org.tmapi.core.Occurrence> os=t.getOccurrences();
        for(org.tmapi.core.Occurrence o : os){
            Set<org.tmapi.core.Topic> scope=o.getScope();
            if(scope.size()==1){
                org.tmapi.core.Topic _type=o.getType();
                _ret.add(_type);
            }
        }
        return tm.wrapTopics(_ret);
    }

    @Override
    public void setData(Topic type, Hashtable<Topic, String> versionData) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void setData(Topic type, Topic version, String value) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void removeData(Topic type, Topic version) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void removeData(Topic type) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Locator getSubjectLocator() throws TopicMapException {
        Set<org.tmapi.core.Locator> ls=t.getSubjectLocators();
        if(ls.isEmpty()) return null;
        else if(ls.size()==1) return tm.createLocator(ls.iterator().next().toExternalForm());
        else {
            ArrayList<String> sort=new ArrayList<String>();
            for(org.tmapi.core.Locator l : ls){
                sort.add(l.toExternalForm());
            }
            Collections.sort(sort);
            return tm.createLocator(sort.get(0));
        }
    }

    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public TopicMap getTopicMap() {
        return tm;
    }

    @Override
    public Collection<Association> getAssociations() throws TopicMapException {
        Set<org.tmapi.core.Role> rs=t.getRolesPlayed();
        HashSet<org.tmapi.core.Association> _ret=new HashSet<org.tmapi.core.Association>();
        for(org.tmapi.core.Role r : rs){
            _ret.add(r.getParent());
        }
        return tm.wrapAssociations(_ret);
    }

    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        Set<org.tmapi.core.Role> rs=t.getRolesPlayed();
        HashSet<org.tmapi.core.Association> _ret=new HashSet<org.tmapi.core.Association>();
        for(org.tmapi.core.Role r : rs){
            org.tmapi.core.Association _a=r.getParent();
            if(_a.getType().equals(((T2WTopic)type).getWrapped()))
                _ret.add(_a);
        }
        return tm.wrapAssociations(_ret);
    }

    @Override
    public Collection<Association> getAssociations(Topic type, Topic role) throws TopicMapException {
        HashSet<org.tmapi.core.Association> _ret=new HashSet<org.tmapi.core.Association>();
        
        for(org.tmapi.core.Role r : t.getRolesPlayed(((T2WTopic)role).getWrapped(), ((T2WTopic)type).getWrapped())) {
            _ret.add(r.getParent());
        }
        return tm.wrapAssociations(_ret);
    }

    @Override
    public void remove() throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public long getEditTime() throws TopicMapException {
        return 0; // should we still maybe keep track of editing time even though editing is not supported?
    }

    @Override
    public void setEditTime(long time) throws TopicMapException {
    }

    @Override
    public long getDependentEditTime() throws TopicMapException {
        return 0;
    }

    @Override
    public void setDependentEditTime(long time) throws TopicMapException {
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return false;
    }

    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        return false; // editing not supported so delete not allowed either
    }

    @Override
    public Collection<Topic> getTopicsWithDataType() throws TopicMapException {
        Collection<org.tmapi.core.Occurrence> os=tm.getTypeIndex().getOccurrences(t);
        HashSet<org.tmapi.core.Topic> _ret=new HashSet<org.tmapi.core.Topic>();
        for(org.tmapi.core.Occurrence o : os){
            if(o.getScope().size()==1)
                _ret.add(o.getParent());
        }
        return tm.wrapTopics(_ret);
    }

    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        Collection<org.tmapi.core.Occurrence> os=tm.getScopedIndex().getOccurrences(t);
        HashSet<org.tmapi.core.Topic> _ret=new HashSet<org.tmapi.core.Topic>();
        for(org.tmapi.core.Occurrence o : os){
            if(o.getScope().size()==1)
                _ret.add(o.getParent());
        }
        return tm.wrapTopics(_ret);
    }

    @Override
    public Collection<Association> getAssociationsWithType() throws TopicMapException {
        Collection<org.tmapi.core.Association> as=tm.getTypeIndex().getAssociations(t);
        HashSet<org.tmapi.core.Association> _ret=new HashSet<org.tmapi.core.Association>();
        for(org.tmapi.core.Association a : as){
            _ret.add(a);
        }
        return tm.wrapAssociations(_ret);
    }

    @Override
    public Collection<Association> getAssociationsWithRole() throws TopicMapException {
        Collection<org.tmapi.core.Role> rs=tm.getTypeIndex().getRoles(t);
        HashSet<org.tmapi.core.Association> _ret=new HashSet<org.tmapi.core.Association>();
        for(org.tmapi.core.Role r : rs){
            _ret.add(r.getParent());
        }
        return tm.wrapAssociations(_ret);
    }

    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        org.tmapi.core.TopicMap _tm=t.getParent();
        org.tmapi.core.Topic nameType=_tm.getTopicBySubjectIdentifier(_tm.createLocator(T2WTopicMap.TOPIC_NAME_SI));
        if(nameType==null) return new ArrayList<Topic>();
        
        Collection<org.tmapi.core.Variant> vs=tm.getScopedIndex().getVariants(t);
        HashSet<org.tmapi.core.Topic> _ret=new HashSet<org.tmapi.core.Topic>();
        for(org.tmapi.core.Variant v : vs){
            org.tmapi.core.Name _name=v.getParent();
            if(_name.getType().equals(nameType))
                _ret.add(_name.getParent());
        }
        return tm.wrapTopics(_ret);
    }
    
}
