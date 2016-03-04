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
 */
package org.wandora.topicmap.query;
import java.util.*;
import org.wandora.topicmap.*;
import org.wandora.query2.*;

/**
 *
 * @author olli
 */
public class QueryTopic extends Topic {

    protected Locator si;
    protected ArrayList<Locator> sis;
    protected QueryTopicMap tm;
    
    public QueryTopic(Locator l,QueryTopicMap tm){
        this.sis=new ArrayList<Locator>();
        sis.add(l);
        this.si=l;
        this.tm=tm;
    }
    
    protected ArrayList<QueryAssociation> makeAssociations(ArrayList<ResultRow> res,Locator type) throws TopicMapException {
        ArrayList<QueryAssociation> ret=new ArrayList<QueryAssociation>();
        if(res.size()==0) return ret;
        Topic typeTopic=new QueryTopic(type,tm);
        
        for(ResultRow row : res){
            boolean include=false;
            for(int i=0;i<row.getNumValues();i++){
                Object value=row.getValue(i);
                if(value==null) continue;
                if( (value instanceof Topic && ((Topic)value).mergesWithTopic(this)) ||
                    (value instanceof Locator && value.equals(si)) ||
                    (value instanceof String && value.equals(si.toExternalForm())) ){
                    include=true;
                    break;
                }
            }
            if(!include) continue;
            
            Hashtable<Topic,Topic> players=new Hashtable<Topic,Topic>();
            for(int i=0;i<row.getNumValues();i++){
                Object value=row.getValue(i);
                if(value==null) continue;
                String role=row.getRole(i);
                if(role.startsWith("#")) role=Directive.DEFAULT_NS+role;
                Locator roleL=new Locator(role);
                if(value instanceof Topic){ value=((Topic)value).getOneSubjectIdentifier(); }
                else if(value instanceof String){ value=new Locator(value.toString()); }
                else if(value instanceof Locator){}
                else continue;
                players.put(new QueryTopic(roleL,tm),new QueryTopic((Locator)value,tm));
            }
            ret.add(new QueryAssociation(typeTopic,players,tm));
        }
        return ret;
    }
    
    private ArrayList<QueryAssociation> getQueryAssociations(Locator type) throws TopicMapException {
        ArrayList<QueryAssociation> ret=tm.getCachedAssociations(si, type);
//        if(ret!=null) return ret;
        
        ret=new ArrayList<QueryAssociation>();
        Topic t=tm.getLayerStack().getTopic(si);
        if(t==null) return ret;
        Directive query=tm.getQueries().get(type);
        if(query==null) return ret;
        try{
            ArrayList<ResultRow> res=query.doQuery(new QueryContext(tm.getLayerStack(),null), new ResultRow(t));
            ret.addAll(makeAssociations(res,type));
        
            tm.cacheAssociations(si, type, ret);
            return ret;
        }catch(QueryException qe){
            qe.printStackTrace();
            return new ArrayList<QueryAssociation>();
        }
    }
    
    private Object getAssociationsLock=new Object();
    private boolean getAssociationsLocked=false;
    
    @Override
    public Collection<Association> getAssociations() throws TopicMapException {
        synchronized(getAssociationsLock){
            if(getAssociationsLocked) return new ArrayList<Association>();
            getAssociationsLocked=true;
            try{
                HashMap<Locator,Directive> queries=tm.getQueries();
                ArrayList<Association> ret=new ArrayList<Association>();
                for(Map.Entry<Locator,Directive> e : queries.entrySet()){
                    Locator type=e.getKey();
                    ArrayList<QueryAssociation> res=getQueryAssociations(type);
                    ret.addAll(res);
                }
                return ret;
            }
            finally {
                getAssociationsLocked=false;
            }
        }
    }

    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        synchronized(getAssociationsLock){
            if(getAssociationsLocked) return new ArrayList<Association>();
            getAssociationsLocked=true;
            try{
                HashMap<Locator,Directive> queries=tm.getQueries();
                ArrayList<Association> ret=new ArrayList<Association>();
                ArrayList<QueryAssociation> res=getQueryAssociations(((QueryTopic)type).si);
                ret.addAll(res);
                return ret;
            }
            finally{
                getAssociationsLocked=false;
            }
        }
    }

    @Override
    public Collection<Association> getAssociations(Topic type, Topic role) throws TopicMapException {
        synchronized(getAssociationsLock){
            if(getAssociationsLocked) return new ArrayList<Association>();
            getAssociationsLocked=true;
            try{
                ArrayList<QueryAssociation> res=getQueryAssociations(((QueryTopic)type).si);
                ArrayList<Association> ret=new ArrayList<Association>();
                for(QueryAssociation a : res){
                    Topic p=a.getPlayer(role);
                    if(p==null) continue;
                    if(p.equals(this)){
                        ret.add(a);
                        continue;
                    }
                }
                return ret;        
            }
            finally{
                getAssociationsLocked=false;
            }
        }
    }

    @Override
    public Collection<Association> getAssociationsWithRole() throws TopicMapException {
        return new ArrayList<Association>();
    }

    @Override
    public Collection<Association> getAssociationsWithType() throws TopicMapException {
        return new ArrayList<Association>();
    }

    @Override
    public String getBaseName() throws TopicMapException {
        return null;
    }

    @Override
    public String getData(Topic type, Topic version) throws TopicMapException {
        return null;
    }

    @Override
    public Hashtable<Topic, String> getData(Topic type) throws TopicMapException {
        return new Hashtable<Topic,String>();
    }

    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public String getID() throws TopicMapException {
        return si.toString();
    }

    @Override
    public Collection<Locator> getSubjectIdentifiers() throws TopicMapException {
        return sis;
    }

    @Override
    public Locator getSubjectLocator() throws TopicMapException {
        return null;
    }

    @Override
    public TopicMap getTopicMap() {
        return tm;
    }

    @Override
    public Collection<Topic> getTopicsWithDataType() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        return null;
    }

    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        return new HashSet<Set<Topic>>();
    }

    @Override
    public boolean isOfType(Topic t) throws TopicMapException {
        return false;
    }
    
    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        return false;
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return false;
    }

    @Override
    public long getDependentEditTime() throws TopicMapException {
        return 0;
    }

    @Override
    public long getEditTime() throws TopicMapException {
        return 0;
    }
    
    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void addType(Topic t) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void remove() throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeData(Topic type, Topic version) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeData(Topic type) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeType(Topic t) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeVariant(Set<Topic> scope) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setBaseName(String name) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setData(Topic type, Hashtable<Topic, String> versionData) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setData(Topic type, Topic version, String value) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setDependentEditTime(long time) throws TopicMapException {
    }

    @Override
    public void setEditTime(long time) throws TopicMapException {
    }

    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setVariant(Set<Topic> scope, String name) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public int hashCode(){
        return si.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryTopic other = (QueryTopic) obj;
        if (this.si != other.si && (this.si == null || !this.si.equals(other.si))) {
            return false;
        }
        return true;
    }
}
