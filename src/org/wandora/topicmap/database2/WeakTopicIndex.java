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
 * WeakTopicIndex.java
 *
 * Created on 14. marraskuuta 2005, 13:54
 */

package org.wandora.topicmap.database2;


import java.lang.ref.*;
import java.util.*;
import org.wandora.topicmap.*;
import org.wandora.utils.MultiHashMap;


/**
 * <p>
 * WeakTopicIndex keeps an index of every topic and association created with it.
 * Database topic map creates all topics and associations with it instead of
 * creating them directly. The index is done
 * with <code>WeakReference</code>s so that Java garbage collector can collect
 * the indexed topics at any time if there are no strong references to them
 * elsewhere. With this kind of index, it is possible to make sure that
 * at any time there is at most one topic object for each individual topic in the
 * database but at the same time allow garbage collector to clean topics that are
 * not referenced anywhere else than the index.
 * </p>
 * <p>
 * WeakTopicIndex needs to have a cleaner thread running that cleans the WeakReferences
 * when garbage collector has collected the referred objects. 
 * </p>
 * <p>
 * In addition, a strong reference to 2000 most used topics is maintained. This
 * acts as a sort of cache to keep frequently accessed topics in memory even if
 * strong references are not kept outside the index. This cache is maintained with
 * the <code>WeakTopicIndex.CacheList</code> class.
 * </p>
 *
 * @author olli
 */
public class WeakTopicIndex implements Runnable {
    
    /**
     * <p>
     * Cache list is a list of objects where the most used objects are at the top
     * of the list and least used objects are at the bottom. New objects are
     * added somewhere in the middle but near the bottom. Objects are accessed
     * which either moves them up in the list or adds them at the specified
     * add point if they are not in the list allready.
     * </p><p>
     * The idea is that a limited number of objects from a very large object pool
     * are kept in memeory and it is prefered that objects that are used the most
     * stay in the list while objects that are used little fall off the list.
     * Objects are not added right at the bottom to avoid them being falling off
     * the list straight away and not having any chance of moving up.
     * </p><p>
     * List is implemented as a linked list with pointers to the top, bottom
     * and the marked add position.
     * </p>
     */
    public static class CacheList<E> {
        private static class ListItem<E> {
            public ListItem<E> next=null;
            public ListItem<E> prev=null;
            public E item;
            public ListItem(E item){
                this.item=item;
            }
        }
        
        /** The maximum size of the list */
        public static final int cacheSize=2000;
        /** The position at which new objects are added */
        public static final int markPos=200;
        private int size=0;
        private ListItem<E> last=null;
        private ListItem<E> first=null;
        private ListItem<E> mark=null;
        
        private HashMap<E,ListItem<E>> map;
        public CacheList(){
            map=new HashMap<E,ListItem<E>>(cacheSize);
        }
        
        private void addAtMark(E e){
            ListItem<E> item=new ListItem<E>(e);
            if(size<=markPos){
                if(last==null) last=first=item;
                else{
                    last.next=item;
                    item.prev=last;
                    last=item;
                }
                if(size==markPos) mark=item;
                size++;
            }
            else if(size<cacheSize){
                item.prev=mark.prev;
                mark.prev.next=item;
                mark.prev=item;
                item.next=mark;
                mark=item;
                size++;
            }
            else{
                item.next=mark.next;
                mark.next.prev=item;
                mark.next=item;
                item.prev=mark;
                mark=item;
                map.remove(first.item);
                first=first.next;
                first.prev=null;
            }
            map.put(e,item);
        }
        
        private void moveUp(ListItem<E> item){
            if(item.next==null) return;
            /* Order before  | order after
             *      a               a       <- may be null
             *      b            c=item
             *   c=item             b
             *      d               d       <- may be null
             */
            ListItem<E> a=item.next.next;
            ListItem<E> b=item.next;
            ListItem<E> c=item;
            ListItem<E> d=item.prev;
            if(a!=null) a.prev=c;
            c.prev=b;
            b.prev=d;
            c.next=a;
            b.next=c;            
            if(d!=null) d.next=b;                
            
            if(b==mark) mark=c;
            else if(c==mark) mark=b;
            if(b==last) last=c;
            if(c==first) first=b;
        }
        
        /**
         * Accesses an object moving it up in the list or adding it to the
         * list if it isn't in it yet.
         */
        public void access(E e){
            ListItem<E> item=map.get(e);
            if(item!=null) {
                moveUp(item);
//                System.out.println("CACHE move up "+size);
            }
            else {
//                System.out.println("CACHE add at mark "+size);
                addAtMark(e);
            }
        }
        
        public int size(){
            return size;
        }
        
        public void printDebugInfo(){
            ListItem<E> prev=null;
            ListItem<E> item=first;
            int counter1=0;
            HashSet<E> uniqTest=new HashSet<E>();
            while(item!=null){
                uniqTest.add(item.item);
                prev=item;
                item=item.next;
                counter1++;
            }
            boolean check1=(prev==last);
            item=last;
            int counter2=0;
            while(item!=null){
                prev=item;
                item=item.prev;
                counter2++;
            }
            boolean check2=(prev==first);
            System.out.println("Cachelist size "+counter1+", "+counter2+", "+size+", "+uniqTest.size()+", "+check1+", "+check2+")");
        }
    }
    
    
    private boolean running;
    private Thread thread;
    
    // reference queue for topics to receive notifications when topics are garbage collected
    private ReferenceQueue<DatabaseTopic> topicRefQueue;
    // map to get topics with their IDs
    private HashMap<String,WeakReference<DatabaseTopic>> topicIDIndex;
    // map to get topics with their subject identifiers
    private HashMap<Locator,WeakReference<DatabaseTopic>> topicSIIndex;
    // map to get topics with their base names
    private HashMap<String,WeakReference<DatabaseTopic>> topicBNIndex;

    /* Inverse indexes are needed to get the id/si/base name of previously stored
     * topics with the WeakReferenc after the reference has been cleared and the
     * actual topic is not anymore available.
     */
    private HashMap<WeakReference<DatabaseTopic>,String> topicInvIDIndex;
    private MultiHashMap<WeakReference<DatabaseTopic>,Locator> topicInvSIIndex;
    private HashMap<WeakReference<DatabaseTopic>,String> topicInvBNIndex;
    
    // reference queue for associations to receive notifications when associations are garbage collected
    private ReferenceQueue<DatabaseAssociation> associationRefQueue;
    // map to get oassociationns with their IDs
    private HashMap<String,WeakReference<DatabaseAssociation>> associationIDIndex;
    
    // inverse index for associations
    private HashMap<WeakReference<DatabaseAssociation>,String> associationInvIDIndex;
    
    /**
     * A CacheList that keeps (strong) references to topics so they do not get
     * carbage collected. This keeps (roughly speaking) the 2000 most used
     * topics in memory at all times and allows them to be retrieved quickly
     * from the weak indexes. Without keeping strong references to them they
     * would be garbage collected and the indexes would be of little use.
     */
    private CacheList<DatabaseTopic> topicCache;
    
    /**
     * if references queues are being used, that is if we are keeping track
     * of topics/associations that have been garbage collected
     */
    private boolean useRefQueue=true;
    
    /** Creates a new instance of WeakTopicIndex */
    public WeakTopicIndex() {
        useRefQueue=true;

        topicIDIndex=new HashMap<String,WeakReference<DatabaseTopic>>();
        topicSIIndex=new HashMap<Locator,WeakReference<DatabaseTopic>>();
        topicBNIndex=new HashMap<String,WeakReference<DatabaseTopic>>();
        topicInvIDIndex=new HashMap<WeakReference<DatabaseTopic>,String>();
        topicInvSIIndex=new MultiHashMap<WeakReference<DatabaseTopic>,Locator>();
        topicInvBNIndex=new HashMap<WeakReference<DatabaseTopic>,String>();
        associationIDIndex=new HashMap<String,WeakReference<DatabaseAssociation>>();
        associationInvIDIndex=new HashMap<WeakReference<DatabaseAssociation>,String>();
        
        topicCache=new CacheList<DatabaseTopic>();
        
        if(useRefQueue){
            running=true;
            thread=new Thread(this);
            topicRefQueue=new ReferenceQueue<DatabaseTopic>();
            associationRefQueue=new ReferenceQueue<DatabaseAssociation>();
            thread.start();
        }
        else{
            running=false;
            topicRefQueue=null;
            associationRefQueue=null;
        }        
    }
    
    /**
     * Clears the topic cache allowing any topic that isn't (strongly) referenced 
     * elsewhere to be gargbage collected and removed from the index.
     * Note that this does not actually clear the index.
     */
    public void clearTopicCache(){
        topicCache=new CacheList<DatabaseTopic>();
    }
    
    /**
     * Stops the thread that is cleaning indexes of topics/associations that have
     * been garbage collected.
     */
    public void stopCleanerThread(){
        if(useRefQueue){
            System.out.println("Stopping index cleaner");
            useRefQueue=false;

            topicInvIDIndex=null;
            topicInvSIIndex=null;
            topicInvBNIndex=null;
            associationInvIDIndex=null;
            
            running=false;
            if(thread!=null) thread.interrupt();
            thread=null;
            
            topicRefQueue=null;
            associationRefQueue=null;
        }
    }
    /**
     * Restarts the thread that is cleaning indexes of topics/associations that
     * have been garbage collected.
     */ 
    public void startCleanerThread(){
        if(!useRefQueue){
            System.out.println("Starting index cleaner");
            useRefQueue=true;
            running=false;
            if(thread!=null) thread.interrupt();

            
            HashMap<String,WeakReference<DatabaseTopic>> newTopicIDIndex=new HashMap<String,WeakReference<DatabaseTopic>>();
            HashMap<WeakReference<DatabaseTopic>,String> newTopicInvIDIndex=new HashMap<WeakReference<DatabaseTopic>,String>();
            HashMap<String,WeakReference<DatabaseAssociation>> newAssociationIDIndex=new HashMap<String,WeakReference<DatabaseAssociation>>();
            HashMap<WeakReference<DatabaseAssociation>,String> newAssociationInvIDIndex=new HashMap<WeakReference<DatabaseAssociation>,String>();

            topicRefQueue=new ReferenceQueue<DatabaseTopic>();
            associationRefQueue=new ReferenceQueue<DatabaseAssociation>();
            
            for(WeakReference<DatabaseTopic> ref : topicIDIndex.values()){
                DatabaseTopic t=ref.get();
                if(t==null) continue;
                WeakReference<DatabaseTopic> r=new WeakReference<DatabaseTopic>(t,topicRefQueue);
                newTopicIDIndex.put(t.getID(),r);
                newTopicInvIDIndex.put(r,t.getID());
            }
            for(WeakReference<DatabaseAssociation> ref : associationIDIndex.values()){
                DatabaseAssociation a=ref.get();
                if(a==null) continue;
                WeakReference<DatabaseAssociation> r=new WeakReference<DatabaseAssociation>(a,associationRefQueue);
                newAssociationIDIndex.put(a.getID(),r);
                newAssociationInvIDIndex.put(r,a.getID());
            }
            
            topicIDIndex=newTopicIDIndex;
            topicInvIDIndex=newTopicInvIDIndex;
            associationIDIndex=newAssociationIDIndex;
            associationInvIDIndex=newAssociationInvIDIndex;
            topicSIIndex=new HashMap<Locator,WeakReference<DatabaseTopic>>();
            topicBNIndex=new HashMap<String,WeakReference<DatabaseTopic>>();
            topicInvSIIndex=new MultiHashMap<WeakReference<DatabaseTopic>,Locator>();
            topicInvBNIndex=new HashMap<WeakReference<DatabaseTopic>,String>();
            
            thread=new Thread(this);
            running=true;
            thread.start();            
        }
    }
    
    /**
     * If the indexes are complete. Note that even though they are complete some
     * of the indexed topics/associations may have been garbage collected and
     * index only contains information that something has been in there.
     * This makes it possible to determine if some topic exists but isn't
     * anymore in the index or if it doesn't exist.
     */
    public boolean isFullIndex(){
        return !useRefQueue;
    }
    
    public void destroy(){
        running=false;
    }

    /**
     * Creates a new topic and adds it to the index.
     */
    public synchronized DatabaseTopic newTopic(String id, DatabaseTopicMap tm) throws TopicMapException {
        if(topicIDIndex.containsKey(id)) {
            WeakReference<DatabaseTopic> ref = topicIDIndex.get(id);
            return topicAccessed(ref.get());
        }
        else {
            DatabaseTopic t=new DatabaseTopic(id,tm);
            t.create();
            WeakReference<DatabaseTopic> ref=new WeakReference<DatabaseTopic>(t,topicRefQueue);
            topicIDIndex.put(t.getID(),ref);
            if(useRefQueue) topicInvIDIndex.put(ref,t.getID());
            return topicAccessed(t);
        }
    }
    
    /**
     * Creates a new topic and adds it to the index.
     */
    public synchronized DatabaseTopic newTopic(DatabaseTopicMap tm) throws TopicMapException {
        DatabaseTopic t=new DatabaseTopic(tm);
        t.create();
        WeakReference<DatabaseTopic> ref=new WeakReference<DatabaseTopic>(t,topicRefQueue);
        topicIDIndex.put(t.getID(),ref);
        if(useRefQueue) topicInvIDIndex.put(ref,t.getID());
        return topicAccessed(t);
    }
    
    /**
     * Accesses a topic moving it up or adding it to the CacheList.
     */ 
    private synchronized DatabaseTopic topicAccessed(DatabaseTopic topic){
        if(topic==null) return null;
        topicCache.access(topic);
//        topicCache.printDebugInfo();
        return topic;
    }

    /**
     * Constructs a DatabaseTopic with the given ID.
     */
    public synchronized DatabaseTopic createTopic(String id, DatabaseTopicMap tm) {
        if(id == null) {
            System.out.println("Warning: DatabaseTopic's id will be null. This will cause problems.");
        }
        DatabaseTopic t=getTopicWithID(id);
        if(t!=null) return topicAccessed(t);
        t = new DatabaseTopic(id, tm);
        id = t.getID();
        WeakReference<DatabaseTopic> ref=new WeakReference<DatabaseTopic>(t,topicRefQueue);
        topicIDIndex.put(id,ref);
        if(useRefQueue) topicInvIDIndex.put(ref,id);
        return topicAccessed(t);
    }
    /* The containsKey method check if the appropriate index contains an entry
     * with the given key. The isNull methods check if the index returns null
     * for the key, that is either they don't have an entry for the key or they
     * have a null entry. Use containsKey methods to distinguish between the two.
     * The getTopicWith methods get a topic from the appropriate index and
     * return null if the index doesn't have an entry for the key, the entry
     * is null or the entry is a WeakReference that has been cleared but not
     * yet cleaned by the cleaner thread.
     */    
    /* addNull methods can be used to add null entries in the index. These should
     * be interpreted to mean that a topic does not exist with the given key.
     */
    /* topic*Changed and topicRemoved methods are used to update indexes according
     * to changes in the topic map.
     */
    public synchronized DatabaseTopic getTopicWithID(String id){
        WeakReference<DatabaseTopic> ref=topicIDIndex.get(id);
        if(ref!=null) {
            return topicAccessed(ref.get());
        }
        else return null;
    }

    public synchronized boolean containsKeyWithID(String id){
        return topicIDIndex.containsKey(id);
    }
    public synchronized boolean containsKeyWithSI(Locator si){
        return topicSIIndex.containsKey(si);
    }
    public synchronized boolean containsKeyWithBN(String bn){
        return topicBNIndex.containsKey(bn);
    }
    public synchronized boolean isNullSI(Locator si){
        return topicSIIndex.get(si)==null;
    }
    public synchronized boolean isNullBN(String bn){
        return topicBNIndex.get(bn)==null;
    }
    public synchronized DatabaseTopic getTopicWithSI(Locator si){
        WeakReference<DatabaseTopic> ref=topicSIIndex.get(si);
        if(ref!=null) {
            return topicAccessed(ref.get());
        }
        else return null;
    }
    public synchronized DatabaseTopic getTopicWithBN(String bn){
        WeakReference<DatabaseTopic> ref=topicBNIndex.get(bn);
        if(ref!=null) {
            return topicAccessed(ref.get());
        }
        else return null;
    }

    public synchronized void topicSIChanged(DatabaseTopic t,Locator deleted,Locator added){
        if(deleted!=null && topicSIIndex.containsKey(deleted) && (topicSIIndex.get(deleted)==null || topicSIIndex.get(deleted).get()==t)) {
            WeakReference<DatabaseTopic> tref=topicSIIndex.get(deleted);
            if(tref!=null && useRefQueue) topicInvSIIndex.remove(tref);
            topicSIIndex.remove(deleted);
        }
        if(added!=null) {
            WeakReference<DatabaseTopic> ref=topicIDIndex.get(t.getID());
            topicSIIndex.put(added,ref);
            if(useRefQueue) topicInvSIIndex.addUniq(ref,added);
        }
    }
    public synchronized void addNullSI(Locator si){
        topicSIIndex.put(si,null);
    }
    public synchronized void addNullBN(String bn){
        topicBNIndex.put(bn,null);
    }
    public synchronized void topicBNChanged(Topic t,String old) throws TopicMapException {
        if(old!=null && topicBNIndex.containsKey(old) && (topicBNIndex.get(old)==null || topicBNIndex.get(old).get()==t)) {
            WeakReference<DatabaseTopic> tref=topicBNIndex.get(old);
            if(tref!=null && useRefQueue) topicInvBNIndex.remove(tref);
            topicBNIndex.remove(old);
        }
        if(t.getBaseName()!=null) {
            WeakReference<DatabaseTopic> ref=topicIDIndex.get(t.getID());
            topicBNIndex.put(t.getBaseName(),ref);
            if(useRefQueue) topicInvBNIndex.put(ref,t.getBaseName());
        }
    }
    public synchronized void topicRemoved(Topic t) throws TopicMapException {
        for(Locator l : t.getSubjectIdentifiers()){
            if(topicSIIndex.containsKey(l) && (topicSIIndex.get(l)==null || topicSIIndex.get(l).get()==t)){
                WeakReference<DatabaseTopic> tref=topicSIIndex.get(l);
                if(tref!=null && useRefQueue) topicInvSIIndex.remove(tref);
                topicSIIndex.remove(l);
            }
        }
        String old=t.getBaseName();
        if(old!=null && topicBNIndex.containsKey(old) && (topicBNIndex.get(old)==null || topicBNIndex.get(old).get()==t)) {
            WeakReference<DatabaseTopic> tref=topicBNIndex.get(old);
            if(tref!=null && useRefQueue) topicInvBNIndex.remove(tref);
            topicBNIndex.remove(t.getBaseName());
        }
    }
        
    public synchronized DatabaseAssociation newAssociation(DatabaseTopic type,DatabaseTopicMap tm) throws TopicMapException {
        DatabaseAssociation a=new DatabaseAssociation(type,tm);
        a.create();
        WeakReference<DatabaseAssociation> ref=new WeakReference<DatabaseAssociation>(a,associationRefQueue);
        associationIDIndex.put(a.getID(),ref);
        if(useRefQueue) associationInvIDIndex.put(ref,a.getID());
        return a;
    }
    
    public synchronized DatabaseAssociation createAssociation(String id,DatabaseTopicMap tm){
        DatabaseAssociation a=getAssociation(id,tm);
        if(a!=null) return a;
                
        a=new DatabaseAssociation(id,tm); 
        WeakReference<DatabaseAssociation> ref=new WeakReference<DatabaseAssociation>(a,associationRefQueue);
        associationIDIndex.put(id,ref);
        if(useRefQueue) associationInvIDIndex.put(ref,id);
        return a;        
    }
    
    public synchronized DatabaseAssociation getAssociation(String id,DatabaseTopicMap tm){
        WeakReference<DatabaseAssociation> ref=associationIDIndex.get(id);
        if(ref!=null) {
            return ref.get();
        }
        else return null;
    }


    /**
     * Removes entries that refer to the given WeakReferenc from topic indexes.
     * This is used after a topic has been gargbage collected and it is not needed
     * anymore in the indexes.
     */
    private synchronized void removeTopicKey(Reference<? extends DatabaseTopic> ref){
//        System.out.println("Removing topic from index");
        String id=topicInvIDIndex.get(ref); // will produce null pointer exception if !useRefQueue, but that shouldn't happen
        if(id!=null) topicIDIndex.remove(id);
        topicInvIDIndex.remove(ref);
        String bn=topicInvBNIndex.get(ref);
        if(bn!=null) topicBNIndex.remove(bn);
        topicInvBNIndex.remove(ref);
        Collection<Locator> sis=topicInvSIIndex.get(ref);
        if(sis!=null){
            for(Locator l : sis){
                topicSIIndex.remove(l);
            }
        }
        topicInvSIIndex.remove(ref);
    }
        
    private synchronized void removeAssociationKey(Reference<? extends DatabaseAssociation> ref){
//        System.out.println("Removing association from index");
        String id=associationInvIDIndex.get(ref);
        if(id!=null) associationIDIndex.remove(id);
        associationInvIDIndex.remove(ref);
    }
    
    public void printDebugInfo(){
        System.out.println("Index sizes "+topicIDIndex.size()+", "+topicInvIDIndex.size()+", "+associationIDIndex.size()+", "+associationInvIDIndex.size()+", "+topicBNIndex.size()+", "+topicInvBNIndex.size()+", "+topicSIIndex.size()+", "+topicInvSIIndex.size());
        topicCache.printDebugInfo();
    }

    public void run(){
        while(running && Thread.currentThread()==thread){
            try{
                Reference<? extends DatabaseTopic> ref=topicRefQueue.poll();
                if(ref!=null) removeTopicKey(ref);
                Reference<? extends DatabaseAssociation> ref2=associationRefQueue.poll();
                if(ref2!=null) removeAssociationKey(ref2);
                if(ref==null && ref2==null){
                    Thread.sleep(1000);
                }
//                System.out.println("Index sizes "+topicIDIndex.size()+", "+topicBNIndex.size()+", "+topicSIIndex.size()+", "+associationIDIndex.size());
            }catch(InterruptedException ie){}
        }
    }
    
}
