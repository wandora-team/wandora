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
import java.lang.ref.WeakReference;
import java.util.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.layered.Layer;



/**
 *
 * @author olli
 */


public class UndoTopicMap extends TopicMap {

    private TopicMap wrapped;
    private UndoBuffer undoBuffer;
    private boolean undoDisabled=false;
    
    private final WeakHashMap<Topic,WeakReference<UndoTopic>> topicIndex=new WeakHashMap<Topic,WeakReference<UndoTopic>>();
    
    public UndoTopicMap(TopicMap wrapped, boolean skipEmptyOperations) {
        this.wrapped=wrapped;
        this.undoBuffer=new UndoBuffer();
        this.undoBuffer.setSkipEmptyOperations(skipEmptyOperations);
    }
    
    public UndoTopicMap(TopicMap wrapped) {
        this.wrapped=wrapped;
        this.undoBuffer=new UndoBuffer();
    }
    
    
    public void close() {
        this.wrapped.close();
    }
    
    
    public void setUndoDisabled(boolean value){
        this.undoDisabled=value;
    }
    
    public UndoBuffer getUndoBuffer(){
        return undoBuffer;
    }
    
    public TopicMap getWrappedTopicMap() {
        return wrapped;
    }
    
    public void handleUndoException(UndoException ue){
        ue.printStackTrace();
    }
    
    void addUndoOperation(UndoOperation op){
        if(undoDisabled) {
            // clear undo buffer because it would get out of sync with the topic map otherwise
            clearUndoBuffer(); 
            return;
        }
        undoBuffer.addOperation(op);
    }
    
    UndoTopic wrapTopic(Topic t){
        if(t==null) return null;
        synchronized(topicIndex){
            UndoTopic ret=null;
            WeakReference<UndoTopic> ref=topicIndex.get(t);
            if(ref!=null) ret=ref.get();
            if(ret==null){
                ret=new UndoTopic(t,this);
                topicIndex.put(t,new WeakReference<UndoTopic>(ret));
                return ret;
            }
            else return ret;
        }
    }
    
    ArrayList<Topic> wrapTopics(Collection<Topic> topics){
        ArrayList<Topic> ret=new ArrayList<Topic>();
        for(Topic t : topics){
            ret.add(wrapTopic(t));
        }
        return ret;
    }
    Topic[] wrapTopics(Topic[] topics){
        Topic[] ret=new Topic[topics.length];
        for(int i=0;i<topics.length;i++){
            ret[i]=wrapTopic(topics[i]);
        }
        return ret;
    }
    
    Association wrapAssociation(Association a){
        if(a==null) return null;
        return new UndoAssociation(a,this);
    }
    
    Collection<Association> wrapAssociations(Collection<Association> as){
        ArrayList<Association> ret=new ArrayList<Association>();
        for(Association a : as){
            ret.add(wrapAssociation(a));
        }
        return ret;
    }
    
    
    // -------------------------------------------------------------------------

    @Override
    public TopicMap getParentTopicMap() {
        return wrapped.getParentTopicMap();
    }
    
    @Override
    public TopicMap getRootTopicMap() {
        return wrapped.getRootTopicMap();
    }
    
    @Override
    public void setParentTopicMap(TopicMap parent) {
        if(wrapped != null) wrapped.setParentTopicMap(parent);
    }
    

    // -------------------------------------------------------------------------
    
    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        return wrapTopic(wrapped.getTopic(si));
    }

    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        return wrapTopic(wrapped.getTopicBySubjectLocator(sl));
    }

    @Override
    public Topic createTopic(String id) throws TopicMapException {
        // Create topic operation is not done here because the topic doesn't have
        // a subject identifier yet. UndoTopic does it when the first subject
        // identifier is added.
        return wrapTopic(wrapped.createTopic(id));
    }
    
    @Override
    public Topic createTopic() throws TopicMapException {
        // Create topic operation is not done here because the topic doesn't have
        // a subject identifier yet. UndoTopic does it when the first subject
        // identifier is added.
        return wrapTopic(wrapped.createTopic());
    }

    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        // Create association operatien is not done here because the association
        // is empty. UndoAssociation does it when the first player is added.
        return wrapAssociation(wrapped.createAssociation(((UndoTopic)type).getWrapped()));
    }

    @Override
    public Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException {
        return wrapTopics(wrapped.getTopicsOfType(((UndoTopic)type).getWrapped()));
    }

    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        return wrapTopic(wrapped.getTopicWithBaseName(name));
    }

    @Override
    public Iterator<Topic> getTopics() throws TopicMapException {
        final Iterator<Topic> iter=wrapped.getTopics();
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
                return wrapTopic(iter.next());
            }
            public void remove() {throw new UnsupportedOperationException();}
        };
    }

    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        return wrapTopics(wrapped.getTopics(sis));
    }

    @Override
    public Iterator<Association> getAssociations() throws TopicMapException {
        final Iterator<Association> iter=wrapped.getAssociations();
        return new Iterator<Association>(){
            public boolean hasNext() {
                return iter.hasNext();
            }
            public Association next() {
                return wrapAssociation(iter.next());
            }
            public void remove() {throw new UnsupportedOperationException();}
        };
    }

    @Override
    public Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException {
        return wrapAssociations(wrapped.getAssociationsOfType(((UndoTopic)type).getWrapped()));
    }

    @Override
    public int getNumTopics() throws TopicMapException {
        return wrapped.getNumTopics();
    }

    @Override
    public int getNumAssociations() throws TopicMapException {
        return wrapped.getNumAssociations();
    }

    @Override
    public Topic copyTopicIn(Topic t, boolean deep) throws TopicMapException {
        return wrapTopic(wrapped.copyTopicIn(((UndoTopic)t).getWrapped(),deep));
    }

    @Override
    public Association copyAssociationIn(Association a) throws TopicMapException {
        return wrapAssociation(wrapped.copyAssociationIn(((UndoAssociation)a).getWrapped()));
    }

    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        wrapped.copyTopicAssociationsIn(((UndoTopic)t).getWrapped());
    }

    @Override
    public void setTrackDependent(boolean v) throws TopicMapException {
        wrapped.setTrackDependent(v);
    }

    @Override
    public boolean trackingDependent() throws TopicMapException {
        return wrapped.trackingDependent();
    }

    private HashMap<TopicMapListener,TopicMapListenerWrapper> wrappedListeners=new HashMap<TopicMapListener,TopicMapListenerWrapper>();
    private class TopicMapListenerWrapper implements TopicMapListener {
        private TopicMapListener wrapped;
        public TopicMapListenerWrapper(TopicMapListener l){
            wrapped=l;
        }
        public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
            wrapped.topicSubjectIdentifierChanged(wrapTopic(t), added, removed);
        }
        public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
            wrapped.topicBaseNameChanged(wrapTopic(t), newName, oldName);
        }
        public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
            wrapped.topicTypeChanged(wrapTopic(t), wrapTopic(added), wrapTopic(removed));
        }
        public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
            wrapped.topicVariantChanged(wrapTopic(t), wrapTopics(scope), newName, oldName);
        }
        public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
            wrapped.topicDataChanged(wrapTopic(t), wrapTopic(type), wrapTopic(version), newValue, oldValue);
        }
        public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
            wrapped.topicSubjectLocatorChanged(wrapTopic(t), newLocator, oldLocator);
        }
        public void topicRemoved(Topic t) throws TopicMapException {
            wrapped.topicRemoved(wrapTopic(t));
        }
        public void topicChanged(Topic t) throws TopicMapException {
            wrapped.topicChanged(wrapTopic(t));
        }
        public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
            wrapped.associationTypeChanged(wrapAssociation(a), wrapTopic(newType), wrapTopic(oldType));
        }
        public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
            wrapped.associationPlayerChanged(wrapAssociation(a), wrapTopic(role), wrapTopic(newPlayer), wrapTopic(oldPlayer));
        }
        public void associationRemoved(Association a) throws TopicMapException {
            wrapped.associationRemoved(wrapAssociation(a));
        }
        public void associationChanged(Association a) throws TopicMapException {
            wrapped.associationChanged(wrapAssociation(a));
        }
    }
    
    @Override
    public void addTopicMapListener(TopicMapListener listener) {
        TopicMapListenerWrapper wrapper=new TopicMapListenerWrapper(listener);
        wrappedListeners.put(listener,wrapper);
        wrapped.addTopicMapListener(wrapper);
    }

    @Override
    public void removeTopicMapListener(TopicMapListener listener) {
        TopicMapListenerWrapper wrapper=wrappedListeners.get(listener);
        if(listener==null) return;
        wrapped.removeTopicMapListener(wrapper);
    }

    @Override
    public List<TopicMapListener> getTopicMapListeners() {
        return new ArrayList<TopicMapListener>(wrappedListeners.values());
    }

    @Override
    public void disableAllListeners() {
        wrapped.disableAllListeners();
    }

    @Override
    public void enableAllListeners() {
        wrapped.enableAllListeners();
    }

    @Override
    public boolean isTopicMapChanged() throws TopicMapException {
        return wrapped.isTopicMapChanged();
    }

    @Override
    public boolean resetTopicMapChanged() throws TopicMapException {
        return wrapped.resetTopicMapChanged();
    }

    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options) throws TopicMapException {
        return wrapTopics(wrapped.search(query,options));
    }

    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        return wrapped.getStatistics(options);
    }

    public void clearUndoBuffer() {
        undoBuffer.clear();
    }
    
    @Override
    public void clearTopicMap() throws TopicMapException {
        // this operation cannot be undone, clear undo buffer too
        clearUndoBuffer();
        wrapped.clearTopicMap();
    }

    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
        wrapped.clearTopicMapIndexes();
    }

    @Override
    public void checkAssociationConsistency(TopicMapLogger logger) throws TopicMapException {
        wrapped.checkAssociationConsistency(logger);
    }
    
    @Override
    public boolean getConsistencyCheck() throws TopicMapException {
        return wrapped.getConsistencyCheck();
    }
    @Override
    public boolean isConnected() throws TopicMapException {
        return wrapped.isConnected();
    }
    
    @Override
    public void setReadOnly(boolean readOnly) {
        wrapped.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return wrapped.isReadOnly();
    }
    
    @Override
    public void setConsistencyCheck(boolean value) throws TopicMapException {
        wrapped.setConsistencyCheck(value);
    }
    
    
    @Override
    public void mergeIn(TopicMap tm) throws TopicMapException {
        wrapped.mergeIn(tm);
    }

    @Override
    public void mergeIn(TopicMap tm,TopicMapLogger tmLogger) throws TopicMapException {
        wrapped.mergeIn(tm,tmLogger);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
}
