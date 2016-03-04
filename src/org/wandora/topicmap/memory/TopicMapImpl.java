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
 * TopicMapImpl.java
 *
 * Created on June 10, 2004, 11:30 AM
 */

package org.wandora.topicmap.memory;



import org.wandora.topicmap.*;
import java.util.*;
import java.util.regex.*;
/**
 *
 * @author  olli, ak
 */
public class TopicMapImpl extends TopicMap {
    
//    private TopicMapListener topicMapListener;
    private List<TopicMapListener> topicMapListeners;
    private List<TopicMapListener> disabledListeners;
    
    private int topicMapID; // for debugging;
    private static int topicMapCounter=0; // for debugging (not thread safe)
    
    /**
     * Indexes topics according to their id.
     */
    private Map<String, Topic> idIndex;
    /**
     * Indexes topics according to their type.
     */
    private Map<Topic,Collection<Topic>> typeIndex;
    /**
     * Indexes topics according to subject identifiers.
     */
    private Map<Locator,Topic> subjectIdentifierIndex;
    /**
     * Indexes topics according to subject locators.
     */
    private Map<Locator,Topic> subjectLocatorIndex;
    /**
     * Indexes topics according to base names.
     */
    private Map<String,Topic> nameIndex;
    /**
     * Indexes associations according to their type.
     */
    private Map<Topic,Collection<Association>> associationTypeIndex;
    
    /**
     * All topics in this topic map.
     */
    private Set<Topic> topics;
    /**
     * All associations in this topic map. 
     */
    private Set<Association> associations;
    
    
    private boolean trackDependent;
    
    private boolean topicMapChanged;
    
    
    /** Creates a new instance of TopicMapImpl */
    public TopicMapImpl(String topicmapFile) {
        this();
        try {
            importTopicMap(topicmapFile);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public TopicMapImpl() {
        topicMapID = topicMapCounter++;
        idIndex = Collections.synchronizedMap(new LinkedHashMap<String,Topic>()); 
        typeIndex = Collections.synchronizedMap(new LinkedHashMap<Topic,Collection<Topic>>());
        subjectIdentifierIndex = Collections.synchronizedMap(new LinkedHashMap<Locator,Topic>());
        subjectLocatorIndex = Collections.synchronizedMap(new LinkedHashMap<Locator,Topic>());
        nameIndex = Collections.synchronizedMap(new LinkedHashMap<String,Topic>());
        associationTypeIndex = Collections.synchronizedMap(new LinkedHashMap<Topic,Collection<Association>>());
        topics = Collections.synchronizedSet(new LinkedHashSet<Topic>());
        associations = Collections.synchronizedSet(new LinkedHashSet<Association>());
        trackDependent = false;
        topicMapChanged = false;
        topicMapListeners = Collections.synchronizedList(new ArrayList<TopicMapListener>());
    }

    
    @Override
    public void clearTopicMap() throws TopicMapException{
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        idIndex = Collections.synchronizedMap(new LinkedHashMap<String,Topic>()); 
        typeIndex = Collections.synchronizedMap(new LinkedHashMap<Topic,Collection<Topic>>());
        subjectIdentifierIndex = Collections.synchronizedMap(new LinkedHashMap<Locator,Topic>());
        subjectLocatorIndex = Collections.synchronizedMap(new LinkedHashMap<Locator,Topic>());
        nameIndex = Collections.synchronizedMap(new LinkedHashMap<String,Topic>());
        associationTypeIndex = Collections.synchronizedMap(new LinkedHashMap<Topic,Collection<Association>>());
        topics = Collections.synchronizedSet(new LinkedHashSet<Topic>());
        associations = Collections.synchronizedSet(new LinkedHashSet<Association>());
        topicMapChanged = true;
    }
    
    
    @Override
    public void clearTopicMapIndexes() throws TopicMapException {
        /* Do nothing!
         * Especially do not clear all the *Index objects. Because all data
         * is in memory all the time, they can never be out of date and thus
         * never require refreshing. It is assumed that they always contain
         * complete data.
         */
    }
    
    
    @Override
    public void close() {
    }
    
    
    /**
     * Checks association consistency and fixes any inconsistencies. Two
     * associations are said to be inconsistent if they have the same type and
     * same players with same roles, that is they represent the exact same
     * association. If several such associations exist all but one of them need
     * to be removed.
     */
    @Override
    public void checkAssociationConsistency(TopicMapLogger logger) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        ArrayList<Association> clonedAssociations = new ArrayList<Association>();
        clonedAssociations.addAll(associations);
        if(logger != null) {
            System.out.println("associations.size=="+associations.size());
            int s = clonedAssociations.size();
            logger.setProgressMax(s);
        }
        AssociationImpl ai = null;
        int i=0;
        for(Association a : clonedAssociations) {
            if(logger != null) {
                logger.setProgress(i++);
            }
            if(a != null && !a.isRemoved() && a instanceof AssociationImpl) {
                ai = (AssociationImpl) a;
                ai.checkRedundancy();
            }
        }
        System.out.println("associations.size=="+associations.size());
    }
    
    @Override
    public List<TopicMapListener> getTopicMapListeners(){
        return topicMapListeners;
    }
    
    @Override
    public void addTopicMapListener(TopicMapListener listener){
        topicMapListeners.add(listener);
    }
    
    @Override
    public void removeTopicMapListener(TopicMapListener listener){
        topicMapListeners.remove(listener);
    }
    
    @Override
    public void disableAllListeners(){
        if(disabledListeners==null){
            disabledListeners=topicMapListeners;
            topicMapListeners=new ArrayList<TopicMapListener>();
        }
    }
    
    @Override
    public void enableAllListeners(){
        if(disabledListeners!=null){
            topicMapListeners=disabledListeners;
            disabledListeners=null;
        }
    }
    
/*    public TopicMapListener setTopicMapListener(TopicMapListener listener){
        TopicMapListener old=topicMapListener;
        topicMapListener=listener;
        return old;
    }*/
    
    protected TopicImpl constructTopic(String id) throws TopicMapException {
        return new TopicImpl(id, this);
    }
    
    protected TopicImpl constructTopic() throws TopicMapException {
        return new TopicImpl(this);
    }
    
    @Override
    public Topic createTopic(String id) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Topic t = null;
        if(idIndex.get(id) == null) {
            t = constructTopic(id);
            topics.add(t);
            idIndex.put(t.getID(), t);
        }
        else {
            t = idIndex.get(id);
        }
        return t;
    }
    
    @Override
    public Topic createTopic() throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        TopicImpl t=constructTopic();
        topics.add(t);
        idIndex.put(t.getID(), t);
//        if(topicMapListener!=null) topicMapListener.topicChanged(t);
        return t;
    }
    
    protected AssociationImpl constructAssociation(Topic type) throws TopicMapException {
        return new AssociationImpl(this,type);
    }
    
    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Association a=constructAssociation(type);
        associations.add(a);
//        if(topicMapListener!=null) topicMapListener.associationChanged(a);
        return a;
    }
    
    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        if(si == null) return null;
        return subjectIdentifierIndex.get(si);
    }
    
    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        if(name == null) return null;
        return nameIndex.get(name);
    }
    
    @Override
    public Collection getTopicsOfType(Topic type)  throws TopicMapException{
        if(type == null) return new ArrayList();
        Collection s=typeIndex.get(type);
        if(s==null) return new HashSet();
        else return s;
    }
    
    // Note that this isn't part of the Wandora topic map API, it's used
    // in the tmapi wrapper 
    public Collection getTypeTopics() throws TopicMapException {
        return new ArrayList(typeIndex.keySet());
    }
    
    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        if(sl == null) return null;
        return subjectLocatorIndex.get(sl);
    }
    
    @Override
    public Iterator getTopics() throws TopicMapException {
        // TODO: synchronization of iterator?
        final Iterator<Topic> iter=topics.iterator();
        return new TopicIterator(){
            boolean disposed=false;
            @Override
            public void dispose() {
                disposed=true;
            }
            @Override
            public boolean hasNext() {
                if(disposed) return false;
                else return iter.hasNext();
            }
            @Override
            public Topic next() {
                return iter.next();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
    
    
    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        if(sis == null) return new Topic[] {};
        Topic[] topics = new Topic[sis.length];
        for(int i=0; i<sis.length; i++) {
            topics[i]=getTopic(sis[i]);
        }
        return topics;
    }
    
    
    
    @Override
    public Iterator getAssociations() throws TopicMapException {
        return associations.iterator();
    }
    
    @Override
    public Collection getAssociationsOfType(Topic type) throws TopicMapException {
        if(type == null) return new ArrayList();
        Collection<Association> s=associationTypeIndex.get(type);
        if(s==null) return new HashSet();
        else return s;
    }    
    
    public Topic getTopic(Collection SIs) throws TopicMapException{
        Iterator iter=SIs.iterator();
        while(iter.hasNext()){
            Locator l=(Locator)iter.next();
            Topic t=getTopic(l);
            if(t!=null) return t;
        }
        return null;
    }
    
    private static int idCounter=(int)(100000*Math.random());
    private synchronized int getIDCounter(){
        if(idCounter>=1000000) idCounter=0;
        return idCounter++;
    }
    
    private Topic _copyTopicIn(Topic t,boolean deep,Hashtable copied) throws TopicMapException{
        return _copyTopicIn(t,deep,false,copied);
    }
    
    private Topic _copyTopicIn(Topic t,boolean deep,boolean stub,Hashtable copied) throws TopicMapException{
        
        if(copied.containsKey(t)) {
            // Don't return the topic that was created when t was copied because it might have been merged with something
            // since then. Instead get the topic with one of the subject identifiers of the topic.
            Locator l=(Locator)copied.get(t);
            return getTopic(l);
        }
        // first check if the topic would be merged, if so, edit the equal topic directly instead of creating new
        // and letting them merge later
        Topic nt=getTopic(t.getSubjectIdentifiers());
        if(nt==null && t.getBaseName()!=null) nt=getTopicWithBaseName(t.getBaseName());
        if(nt==null && t.getSubjectLocator()!=null) nt=getTopicBySubjectLocator(t.getSubjectLocator());
        if(nt==null && idIndex.containsKey(t.getID())) nt=idIndex.get(t.getID());
        if(nt==null) {
            nt=createTopic(t.getID());
        }

        boolean newer=(t.getEditTime()>=nt.getEditTime());
        
        Iterator iter=t.getSubjectIdentifiers().iterator();
        while(iter.hasNext()){
            Locator l=(Locator)iter.next();
            nt.addSubjectIdentifier(l);
        }
        
        if(nt.getSubjectIdentifiers().isEmpty()) {
            System.out.println("Warning! No subject indicators in topic. Creating default SI.");
            String randomNumber = System.currentTimeMillis()+"-"+getIDCounter();
            nt.addSubjectIdentifier(new Locator("http://wandora.org/si/temp/" + randomNumber));
        }
        copied.put(t,(Locator)nt.getSubjectIdentifiers().iterator().next());
        
        
        if(nt.getSubjectLocator()==null && t.getSubjectLocator()!=null){
            nt.setSubjectLocator(t.getSubjectLocator()); // TODO: raise error if different?
        }
        
        if(nt.getBaseName()==null && t.getBaseName()!=null){
            nt.setBaseName(t.getBaseName());
        }
        
        if( (!stub) || deep) {

            iter=t.getTypes().iterator();
            while(iter.hasNext()){
                Topic type=(Topic)iter.next();
                Topic ntype=_copyTopicIn(type,deep,true,copied);
                nt.addType(ntype);
            }

            iter=t.getVariantScopes().iterator();
            while(iter.hasNext()){
                Set scope=(Set)iter.next();
                Set nscope=new LinkedHashSet();
                Iterator iter2=scope.iterator();
                while(iter2.hasNext()){
                    Topic st=(Topic)iter2.next();
                    Topic nst=_copyTopicIn(st,deep,true,copied);
                    nscope.add(nst);
                }
                nt.setVariant(nscope, t.getVariant(scope));
            }

            iter=t.getDataTypes().iterator();
            while(iter.hasNext()){
                Topic type=(Topic)iter.next();
                Topic ntype=_copyTopicIn(type,deep,true,copied);
                Hashtable versiondata=t.getData(type);
                Iterator iter2=versiondata.entrySet().iterator();
                while(iter2.hasNext()){
                    Map.Entry e=(Map.Entry)iter2.next();
                    Topic version=(Topic)e.getKey();
                    String data=(String)e.getValue();
                    Topic nversion=_copyTopicIn(version,deep,true,copied);
                    nt.setData(ntype,nversion,data);
                }
            }
        }
        return nt;
    }
    
    
    
    private Association _copyAssociationIn(Association a) throws TopicMapException{
        Topic type=a.getType();
        Topic ntype=null;
        if(type.getSubjectIdentifiers().isEmpty()) {
            System.out.println("Warning, topic has no subject identifiers.");
        }
        else {
            ntype=getTopic((Locator)type.getSubjectIdentifiers().iterator().next()); 
        }
        if(ntype==null) ntype=copyTopicIn(type,false);
        
        Association na=createAssociation(ntype);
        
        HashMap<Topic,Topic> players = new LinkedHashMap<Topic,Topic>();
        
        for(Topic role : a.getRoles()) {
            Topic nrole = null;
            if(role.getSubjectIdentifiers().isEmpty()) {
                System.out.println("Warning, topic has no subject identifiers. Creating default SI!");
                //role.addSubjectIdentifier(new Locator("http://wandora.org/si/temp/" + System.currentTimeMillis()));
            }
            else {
                nrole=getTopic((Locator)role.getSubjectIdentifiers().iterator().next());
            }
            if(nrole==null) {
                nrole=copyTopicIn(role,false);
            }
            Topic player=a.getPlayer(role);
            Topic nplayer = null;
            if(player.getSubjectIdentifiers().isEmpty()) {
                System.out.println("Warning, topic has no subject identifiers. Creating default SI!");
                //player.addSubjectIdentifier(new Locator("http://wandora.org/si/temp/" + System.currentTimeMillis()));
            }
            else {
                nplayer=getTopic((Locator)player.getSubjectIdentifiers().iterator().next());
            }
            if(nplayer==null) nplayer=copyTopicIn(player,false);
            //na.addPlayer(nplayer,nrole);
            players.put(nrole,nplayer);
        }
        na.addPlayers(players);
        return na;
    }

    
    
    @Override
    public Association copyAssociationIn(Association a) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Association n=_copyAssociationIn(a);
        Topic minTopic=null;
        int minCount=Integer.MAX_VALUE;
        Iterator iter2=n.getRoles().iterator();
        while(iter2.hasNext()){
            Topic role=(Topic)iter2.next();
            Topic t=n.getPlayer(role);
            if(t.getAssociations().size()<minCount){
                minCount=t.getAssociations().size();
                minTopic=t;
            }
        }
        ((TopicImpl)minTopic).removeDuplicateAssociations(n);
        return n;
    }
    
    @Override
    public Topic copyTopicIn(Topic t, boolean deep)  throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        return _copyTopicIn(t,deep,false,new Hashtable());
    }
    
    
    @Override
    public void mergeIn(TopicMap tm)  throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Iterator iter=tm.getTopics();
        Hashtable copied=new Hashtable();
        int tcount=0;
        while(iter.hasNext()){
            Topic t = null;
            try {
                t=(Topic)iter.next();
                _copyTopicIn(t,true,false,copied);
                tcount++;
            }
            catch (Exception e) {
                System.out.println("Unable to copy topic (" + t + ").");
                e.printStackTrace();
            }
        }
        HashSet endpoints=new LinkedHashSet();
        iter=tm.getAssociations();
        int acount=0;
        while(iter.hasNext()) {
            try {
                Association a=(Association)iter.next();
                Association na=_copyAssociationIn(a);
                Topic minTopic=null;
                int minCount=Integer.MAX_VALUE;
                Iterator iter2=na.getRoles().iterator();
                while(iter2.hasNext()){
                    Topic role=(Topic)iter2.next();
                    Topic t=na.getPlayer(role);
                    if(t.getAssociations().size()<minCount){
                        minCount=t.getAssociations().size();
                        minTopic=t;
                    }
                }
                endpoints.add(minTopic);
                acount++;
            }
            catch (Exception e) {
                System.out.println("Unable to copy association.");
                e.printStackTrace();
            }
        }
        // System.out.println("merged "+tcount+" topics and "+acount+" associations");
        iter=endpoints.iterator();
        while(iter.hasNext()){
            TopicImpl t=(TopicImpl)iter.next();
            if(t != null) t.removeDuplicateAssociations();
        }
    }
    
    
    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Topic nt=getTopic((Locator)t.getSubjectIdentifiers().iterator().next());
        if(nt==null) nt=copyTopicIn(t,false);
        Iterator iter=t.getAssociations().iterator();
        while(iter.hasNext()){
            _copyAssociationIn((Association)iter.next());
        }
        ((TopicImpl)nt).removeDuplicateAssociations();
    }
    
    public void addTopicSubjectIdentifier(Topic t,Locator l) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        subjectIdentifierIndex.put(l,t);
    }
    
    public void removeTopicSubjectIdentifier(Topic t,Locator l) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        subjectIdentifierIndex.remove(l);
    }
    
    public void setTopicSubjectLocator(Topic t,Locator l,Locator oldLocator) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(oldLocator!=null) subjectLocatorIndex.remove(oldLocator);
        if(l!=null) subjectLocatorIndex.put(l,t);
    }
    
    public void removeTopicSubjectLocator(Topic t,Locator l) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        subjectLocatorIndex.remove(l);
    }
    
    public void addTopicType(Topic t,Topic type) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Collection s=typeIndex.get(type);
        if(s==null) {
            s=new LinkedHashSet();
            typeIndex.put(type,s);
        }
        s.add(t);
    }
    
    public void removeTopicType(Topic t,Topic type) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Collection s=typeIndex.get(type);
        if(s==null) return;
        s.remove(t);
    }
    
    public void setTopicName(Topic t,String name,String oldname) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(oldname!=null) nameIndex.remove(oldname);
        if(name!=null) nameIndex.put(name,t);
    }
    
    public void setAssociationType(Association a,Topic type,Topic oldtype) throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        if(oldtype!=null) { // note: old type can be null only when setting the initial type
            Collection<Association> s=associationTypeIndex.get(oldtype);
            if(s!=null){
                s.remove(a);
            }
        }
        if(type!=null){ // note: type can be null only when destroying association
            Collection<Association> s=associationTypeIndex.get(type);
            if(s==null) {
                s=new LinkedHashSet();
                associationTypeIndex.put(type,s);
            }
            s.add(a);
        }
    }
    
    
    // -------------------------------------------------- TOPIC MAP LISTENER ---
    
    
    public void topicRemoved(Topic t) throws TopicMapException {
        topicMapChanged=true;
        idIndex.remove(t.getID());
        topics.remove(t);
        for(TopicMapListener listener : topicMapListeners){
            listener.topicRemoved(t);        
        }
    }
    
    public void associationRemoved(Association a) throws TopicMapException {
        topicMapChanged=true;
        associations.remove(a);
        for(TopicMapListener listener : topicMapListeners){
            listener.associationRemoved(a);        
        }
    }
    
    public void topicsMerged(Topic newtopic,Topic deletedtopic){
    }
    
    public void duplicateAssociationRemoved(Association a,Association removeda){
    }
    
    @Override
    public int getNumAssociations()  throws TopicMapException{
        return associations.size();
    }    
    
    @Override
    public int getNumTopics()  throws TopicMapException{
        return topics.size();
    }
    
    @Override
    public boolean trackingDependent(){
        return trackDependent;
    }
    @Override
    public void setTrackDependent(boolean v){
        trackDependent=v;
    }
    
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException{
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicSubjectIdentifierChanged(t,added,removed);
        }
    }
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException{
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicBaseNameChanged(t,newName,oldName);
        }
    }
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicTypeChanged(t,added,removed);
        }
    }
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicVariantChanged(t,scope,newName,oldName);
        }
    }
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicDataChanged(t,type,version,newValue,oldValue);
        }
    }
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicSubjectLocatorChanged(t,newLocator,oldLocator);
        }
    }
    public void topicChanged(Topic t) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicChanged(t);
        }
    }
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.associationTypeChanged(a,newType,oldType);        
        }
    }
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.associationPlayerChanged(a,role,newPlayer,oldPlayer);        
        }
    }
    public void associationChanged(Association a) throws TopicMapException {
        topicMapChanged=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.associationChanged(a);
        }
    }
    
    
    // --------------------------------------------- TOPIC MAP LISTENER ENDS ---
    
    
    @Override
    public boolean resetTopicMapChanged(){
        boolean b=topicMapChanged;
        topicMapChanged=false;
        return b;
    }
    
    @Override
    public boolean isTopicMapChanged(){
        return topicMapChanged;
    }
    
    
    
    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options)  throws TopicMapException{
        ArrayList<Topic> searchResult = new ArrayList<Topic>();
        Iterator topicIterator = getTopics();
        Topic t = null;
        Pattern p = Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE );
        
        while(topicIterator.hasNext()) {
            if(options.maxResults>=0 && searchResult.size()>=options.maxResults) break;
            
            try {
                t = (Topic) topicIterator.next();
                // --- Basename ---
                if(options.searchBasenames) {
                    String name = t.getBaseName();
                    if(searchMatch(name, p)) {
                        searchResult.add(t);
                        continue;
                    }
                }
                
                
                // --- Variant names ---
                if(options.searchVariants) {
                    Set varcol = t.getVariantScopes();
                    Iterator variants = varcol.iterator();
                    boolean matches = false;
                    while(!matches && variants.hasNext()) {
                        Set scope = (Set)variants.next();
                        String name = t.getVariant(scope);
                        if(searchMatch(name, p)) {
                            searchResult.add(t);
                            matches = true;
                            break;
                        }
                    }
                    if(matches) continue;
                }

                
                // --- text occurrences ---
                if(options.searchOccurrences) {
                    boolean matches = false;
                    Iterator iter = t.getDataTypes().iterator();
                    while(!matches && iter.hasNext()) {
                        Topic type=(Topic)iter.next();
                        Hashtable versiondata=t.getData(type);
                        Iterator iter2=versiondata.entrySet().iterator();
                        while(!matches && iter2.hasNext()){
                            Map.Entry e=(Map.Entry) iter2.next();
                            Topic version=(Topic)e.getKey();
                            String data=(String)e.getValue();
                            if(searchMatch(data, p)) {
                                searchResult.add(t);
                                matches = true;
                                break;
                            }
                        }
                    }
                    if(matches) continue;
                }
                
                
                // --- locator ---
                if(options.searchSL) {
                    Locator locator = t.getSubjectLocator();
                    if(locator != null) {
                        if(searchMatch(locator.toExternalForm(), p)) {
                            searchResult.add(t);
                            continue; 
                        }
                    }
                }
                
                
                // --- sis ---
                if(options.searchSIs) {
                    Collection sis = t.getSubjectIdentifiers();
                    Iterator siiter = sis.iterator();
                    Locator locator = null;
                    boolean matches = false;
                    while(!matches && siiter.hasNext()) {
                        locator = (Locator) siiter.next();
                        if(locator != null) {
                            if(searchMatch(locator.toExternalForm(), p)) {
                                searchResult.add(t);
                                matches = true;
                                break;
                            }
                        }
                    }
                    if(matches) continue;
                }
                               
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return searchResult;
    }
 
    
    
    private boolean searchMatch(String s, Pattern p) {
        try {
            Matcher m = p.matcher(s);
            if(m != null) {
                return m.find();
            }
        } catch (Exception e) {}
        return false;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        if(options == null) return null;
        int option = options.getOption();
        switch(option) {
            case TopicMapStatOptions.NUMBER_OF_TOPICS: {
                return new TopicMapStatData(topics.size());
            }
            case TopicMapStatOptions.NUMBER_OF_TOPIC_CLASSES: {
                
                // TODO: WHY typeIndex IS NOT GOOD HERE.
                // UNDO/REDO CAUSES THE typeIndex LEAK.
                
                HashSet typeTopics = new LinkedHashSet();
                synchronized(topics) {
                    for(Topic t : topics) {
                        if(t != null && !t.isRemoved()) {
                            Collection<Topic> cts = t.getTypes();
                            if(cts != null && !cts.isEmpty()) {
                                for(Topic ct : cts) {
                                    typeTopics.add(ct);
                                }
                            }
                        }
                    }
                }
                return new TopicMapStatData(typeTopics.size());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATIONS: {
                return new TopicMapStatData(associations.size());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_PLAYERS: {
                HashSet associationPlayers = new LinkedHashSet();
                Collection associationRoles = null;
                Iterator<Association> associationIter = null;
                Iterator<Topic> associationRoleIter = null;
                Association association = null;
                Topic role = null;
                synchronized(associations) {
                    associationIter = associations.iterator();
                    while(associationIter.hasNext()) {
                        association = associationIter.next();
                        if(association != null && !association.isRemoved()) {
                            associationRoles = association.getRoles();
                            if(associationRoles != null) {
                                synchronized(associationRoles) {
                                    if(!associationRoles.isEmpty()) {
                                        associationRoleIter = associationRoles.iterator();
                                        while(associationRoleIter.hasNext()) {
                                            role = associationRoleIter.next();
                                            if(role != null && !role.isRemoved()) {
                                                associationPlayers.add(association.getPlayer(role));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return new TopicMapStatData(associationPlayers.size());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_ROLES: {
                HashSet associationRoles = new LinkedHashSet();
                Association association = null;
                synchronized(associations) {
                    Iterator<Association> associationIter = associations.iterator();
                    while(associationIter.hasNext()) {
                        association = associationIter.next();
                        if(association != null && !association.isRemoved()) {
                            associationRoles.addAll( association.getRoles() );
                        }
                    }
                }
                return new TopicMapStatData(associationRoles.size());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_TYPES: {
                
                // TODO: WHY associationTypeIndex IS NOT GOOD HERE.
                // UNDO/REDO CAUSES THE associationTypeIndex LEAK.
                
                HashSet associationTypes = new LinkedHashSet();
                Topic typeTopic = null;
                synchronized(associations) {
                    Iterator<Association> associationsIterator = associations.iterator();
                    while(associationsIterator.hasNext()) {
                        Association a = associationsIterator.next();
                        if(a != null && !a.isRemoved()) {
                            typeTopic = a.getType();
                            associationTypes.add(typeTopic);
                        }
                    }
                }
                return new TopicMapStatData(associationTypes.size());
            }
            case TopicMapStatOptions.NUMBER_OF_BASE_NAMES: {
                return new TopicMapStatData(this.nameIndex.size());
            }
            case TopicMapStatOptions.NUMBER_OF_OCCURRENCES: {
                int count=0;
                Topic t = null;
                Collection<Topic> dataTypes = null;
                synchronized(topics) {
                    Iterator topicIter=topics.iterator();
                    while(topicIter.hasNext()) {
                        t=(Topic) topicIter.next();
                        if(t != null) {
                            dataTypes = t.getDataTypes();
                            if(dataTypes != null && !dataTypes.isEmpty()) {
                                for(Topic dataType : dataTypes) {
                                    Hashtable<Topic,String> scopedOccurrence = t.getData(dataType);
                                    count += scopedOccurrence.size();
                                }
                            }
                        }
                    }
                }
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_IDENTIFIERS: {
                return new TopicMapStatData(this.subjectIdentifierIndex.size());
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_LOCATORS: {
                return new TopicMapStatData(this.subjectLocatorIndex.size());
            }
        }
        return new TopicMapStatData();
    }
}
