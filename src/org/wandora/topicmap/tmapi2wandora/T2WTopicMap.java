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

package org.wandora.topicmap.tmapi2wandora;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.tmapi.index.LiteralIndex;
import org.tmapi.index.ScopedIndex;
import org.tmapi.index.TypeInstanceIndex;
import org.wandora.topicmap.*;


/**
 * This is a Wandora TopicMap that wraps inside it a TMAPI topic map.
 * This is a read-only implementation, you cannot edit the TMAPI topic map
 * through this. However, you can always create a new Wandora topic map,
 * then merge in a TMAPI topic map using this wrapper, after which you have the
 * same topic map in a Wandora implementation which you can then edit.
 * 
 * @author olli
 */


public class T2WTopicMap extends TopicMap {
    
    public static final String TOPIC_NAME_SI="http://psi.topicmaps.org/iso13250/model/topic-name";
    public static final String TYPE_STRING_SI="http://www.w3.org/TR/xmlschema-2/#string";
    

    protected org.tmapi.core.TopicMap tm;
    protected TypeInstanceIndex typeIndex;
    protected LiteralIndex literalIndex;
    protected ScopedIndex scopedIndex;

    
    public T2WTopicMap(org.tmapi.core.TopicMap tm){
        this.tm=tm;
        typeIndex=tm.getIndex(TypeInstanceIndex.class);
        literalIndex=tm.getIndex(LiteralIndex.class);
        scopedIndex=tm.getIndex(ScopedIndex.class);
    }
    
    
    @Override
    public void close() {
    }
    
    public TypeInstanceIndex getTypeIndex(){
        return typeIndex;
    }
    
    public LiteralIndex getLiteralIndex(){
        return literalIndex;
    }
    
    public ScopedIndex getScopedIndex(){
        return scopedIndex;
    }
    
    public Collection<Topic> wrapTopics(Collection<org.tmapi.core.Topic> ts){
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(org.tmapi.core.Topic t : ts){
            ret.add(new T2WTopic(this,t));
        }
        return ret;
    }
    
    public Collection<Association> wrapAssociations(Collection<org.tmapi.core.Association> as){
        ArrayList<Association> ret=new ArrayList<Association>();
        for(org.tmapi.core.Association a : as){
            ret.add(new T2WAssociation(this,a));
        }
        return ret;
    }
    
    public Collection<Locator> wrapLocators(Collection<org.tmapi.core.Locator> ls) throws TopicMapException {
        ArrayList<Locator> ret=new ArrayList<Locator>();
        for(org.tmapi.core.Locator l : ls){
            ret.add(createLocator(l.toExternalForm()));
        }
        return ret;
    }
        
    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        org.tmapi.core.Topic t=tm.getTopicBySubjectIdentifier(tm.createLocator(si.toExternalForm()));
        if(t==null) return null;
        return new T2WTopic(this,t);
    }

    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        org.tmapi.core.Topic t=tm.getTopicBySubjectLocator(tm.createLocator(sl.toExternalForm()));
        if(t==null) return null;
        return new T2WTopic(this,t);
    }

    @Override
    public Topic createTopic(String id) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }
    
    @Override
    public Topic createTopic() throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException {
        Collection<org.tmapi.core.Topic> ts=typeIndex.getTopics(((T2WTopic)type).getWrapped());
        return wrapTopics(ts);
    }

    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        Collection<org.tmapi.core.Name> ns=literalIndex.getNames(name);
        
        for(org.tmapi.core.Name n : ns){
            org.tmapi.core.Topic type=n.getType();
            
            for(org.tmapi.core.Locator l : type.getSubjectIdentifiers()){
                if(l.toExternalForm().equals(TOPIC_NAME_SI)){
                    return new T2WTopic(this,n.getParent());
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<Topic> getTopics() throws TopicMapException {
        final Iterator<org.tmapi.core.Topic> iter=tm.getTopics().iterator();
        return new Iterator<Topic>(){
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            @Override
            public Topic next() {
                return new T2WTopic(T2WTopicMap.this,iter.next());
            }
            @Override
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }

    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(String si : sis){
            ret.add(getTopic(si));
        }
        return ret.toArray(new Topic[ret.size()]);
    }

    @Override
    public Iterator<Association> getAssociations() throws TopicMapException {
        final Iterator<org.tmapi.core.Association> iter=tm.getAssociations().iterator();
        return new Iterator<Association>(){
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            @Override
            public Association next() {
                return new T2WAssociation(T2WTopicMap.this,iter.next());
            }
            @Override
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }

    @Override
    public Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException {
        Collection<org.tmapi.core.Association> as=typeIndex.getAssociations(((T2WTopic)type).getWrapped());
        return wrapAssociations(as);
    }

    @Override
    public int getNumTopics() throws TopicMapException {
        return tm.getTopics().size();
    }

    @Override
    public int getNumAssociations() throws TopicMapException {
        return tm.getAssociations().size();
    }

    @Override
    public Topic copyTopicIn(Topic t, boolean deep) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public Association copyAssociationIn(Association a) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void setTrackDependent(boolean v) throws TopicMapException {
    }

    @Override
    public boolean trackingDependent() throws TopicMapException {
        return true;
    }

    @Override
    public void addTopicMapListener(TopicMapListener listener) {
        // topic map isn't edited so no method of the listener ever need be called
    }

    @Override
    public void removeTopicMapListener(TopicMapListener listener) {
    }

    @Override
    public List<TopicMapListener> getTopicMapListeners() {
        return new ArrayList<TopicMapListener>();
        // this should really return the list
    }

    @Override
    public void disableAllListeners() {
    }

    @Override
    public void enableAllListeners() {
        // listeners aren't used anyway because editing isn't supported
    }

    @Override
    public boolean isTopicMapChanged() throws TopicMapException {
        return false; // no editing, never changed
    }

    @Override
    public boolean resetTopicMapChanged() throws TopicMapException {
        return false;
    }

    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options) throws TopicMapException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearTopicMap() throws TopicMapException {
        throw new UnsupportedOperationException("Editing not supported");
    }

    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
    }
    
}
