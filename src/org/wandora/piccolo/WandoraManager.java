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
 *
 * 
 *
 * WandoraManager.java
 *
 * Created on June 7, 2004, 9:51 AM
 */

package org.wandora.piccolo;



import org.wandora.exceptions.WandoraException;
import org.wandora.piccolo.SearchResultItem;
//import org.wandora.piccolo.AdminSocketServer;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.ReaderWriterLock;
import org.wandora.*;
import java.util.*;
import java.io.*;
import java.net.*;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.indexer.*;
import org.wandora.tools.*;
import org.wandora.utils.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;

/**
 *
 * @author  olli
 */
public class WandoraManager implements Runnable, TopicMapListener {
    
    public static final String WANDORACLASS_SI = TMBox.WANDORACLASS_SI;
    public static final String ASSOCIATIONTYPE_SI = TMBox.ASSOCIATIONTYPE_SI;
    public static final String ASSOCIATIONROLE_SI = TMBox.ASSOCIATIONROLE_SI;
    public static final String ROLE_SI = TMBox.ROLE_SI;
    public static final String LANGINDEPENDENT_SI = TMBox.LANGINDEPENDENT_SI;
    public static final String ASSOCIATIONROLECATEGORIES_SI = "http://wandora.org/si/core/associationrolecategories";
    public static final String OCCURRENCETYPE_SI = TMBox.OCCURRENCETYPE_SI;
    public static final String HIDELEVEL_SI = TMBox.HIDELEVEL_SI;
    public static final String CATEGORYHIERARCHY_SI = "http://wandora.org/si/common/category-hierarchy";
    public static final String SUPERCATEGORY_SI = "http://wandora.org/si/common/supercategory";
    public static final String SUBCATEGORY_SI = "http://wandora.org/si/common/subcategory";
    public static final String ENTRYTIME_SI = "http://wandora.org/si/common/entry-time";

    public static final String VARIANT_NAME_VERSION_SI = TMBox.VARIANT_NAME_VERSION_SI;
    public static final String LANGUAGE_SI = TMBox.LANGUAGE_SI;
    
    
    public static final int LOCK_READ=ReaderWriterLock.LOCK_READ;
    public static final int LOCK_WRITE=ReaderWriterLock.LOCK_WRITE;
    
    public static final String PROP_LAZYINDEXUPDATE="wandora.lazyindexupdate";
    public static final String PROP_INDEXPATH="wandora.indexpath";
    public static final String PROP_AUTOSAVEFILE="wandora.autosave.dir";
    public static final String PROP_AUTOSAVEHISTORYFILE="wandora.autosave.historydir";
    public static final String PROP_AUTOSAVEINTERVAL="wandora.autosave.interval";
    public static final String PROP_KEEPHISTORY="wandora.autosave.keephistory";
    public static final String PROP_FACTORYFEATURES="wandora.factory.features.";
    public static final String PROP_FACTORYPROPERTIES="wandora.factory.properties.";
    public static final String PROP_SHUTDOWNHOOK="wandora.environment.shutdownhook";
    public static final String PROP_AUTOSAVEMAPS="wandora.autosave.maps";
    public static final String PROP_ADMINPASS="wandora.admin.password";
    public static final String PROP_PROJECTFILE="wandora.projectfile";
    public static final String PROP_PROJECTFILE_AUTOUPDATE="wandora.projectfile.autoupdate";
    
    private ReaderWriterLock tmLock;
    private ReaderWriterLock searchLock;

    private IndexSearcher searcher;
    
    private TopicMap topicMap;
    
    private HashMap topicMaps;
    
    private HashSet changedTopics;
    private HashSet changedTopicsShallow;
    
    private TMBox tmBox;
    
    private String searchIndexPath;
    
    private String projectFile;
    private long projectFileDateStamp;
    private boolean projectFileAutoUpdate;

    private Thread workerThread;
    private boolean running;
    private int runCount = 0;
    
    private long saveInterval;
    private boolean keepHistory;
    
    private boolean lazyIndexUpdate;
    private boolean indexNeedsUpdate;
    
    private String autoSaveFile;
    private String historySaveFile;
    private ArrayList autoSaveMaps;
    
    private org.wandora.piccolo.Logger logger;
    
    private static String adminPass=null;
    
    public WandoraManager() {
        this(new SystemLogger());
    }
    public WandoraManager(org.wandora.piccolo.Logger logger) {
        this(logger,new Properties(),"");
    }
    public WandoraManager(Properties properties) {
        this(properties,"");
    }
    public WandoraManager(Properties properties,String prefix) {
        this(new SystemLogger(),properties,prefix);
    }
    public WandoraManager(org.wandora.piccolo.Logger logger,Properties properties,String prefix) {
        this.logger=logger;
        tmLock=new ReaderWriterLock();
        searchLock=new ReaderWriterLock();        
        
        topicMaps=new HashMap();
        changedTopics=new HashSet();
        changedTopicsShallow=new HashSet();
        
        indexNeedsUpdate=false;
              
        keepHistory=properties.getProperty(prefix+PROP_KEEPHISTORY,"false").equalsIgnoreCase("true");
        
        searchIndexPath=properties.getProperty(prefix+PROP_INDEXPATH,"");
        autoSaveFile=properties.getProperty(prefix+PROP_AUTOSAVEFILE,"");
        historySaveFile=properties.getProperty(prefix+PROP_AUTOSAVEHISTORYFILE,"");
        saveInterval=Long.parseLong(properties.getProperty(prefix+PROP_AUTOSAVEINTERVAL,"1440"));
        lazyIndexUpdate=new Boolean(properties.getProperty(prefix+PROP_LAZYINDEXUPDATE,"false")).booleanValue();
        String maps=properties.getProperty(prefix+PROP_AUTOSAVEMAPS,"");
        java.util.StringTokenizer st=new java.util.StringTokenizer(maps,",");
        adminPass=properties.getProperty(prefix+PROP_ADMINPASS,null);
        autoSaveMaps=new ArrayList();
        while(st.hasMoreTokens()){
            autoSaveMaps.add(st.nextToken());
        }
        
        if(properties.getProperty(prefix+PROP_SHUTDOWNHOOK,"false").equalsIgnoreCase("true"))
            Runtime.getRuntime().addShutdownHook(new Thread(new WandoraShutdownHook()));
        
        projectFile=properties.getProperty(prefix+PROP_PROJECTFILE,"");
        projectFileAutoUpdate=properties.getProperty(prefix+PROP_PROJECTFILE_AUTOUPDATE,"false").equalsIgnoreCase("true");
        System.out.println("projectFile == "+projectFile);
        if(projectFile != null && projectFile.length() > 0) {
            try {
                System.out.println("initializing projectfile "+projectFile);
                LayerStack ls = new LayerStack(projectFile);
                initialize(ls);
                File f = new File(projectFile);
                projectFileDateStamp = f.lastModified();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        workerThread=new Thread(this);
        running=true;
        workerThread.start();
    }
    
    public void initialize(TopicMap tm) throws TopicMapException {
        logger.writelog("DBG","Wandora initializing");
        if(this.topicMap!=null) this.topicMap.removeTopicMapListener(this);
        this.topicMap=tm;
        tm.addTopicMapListener(this);
        changedTopics.clear();
        changedTopicsShallow.clear();
        if(searchIndexPath!=null && searchIndexPath.length()>0) {
            try{
                buildIndex(searchIndexPath,tm);
            }catch(IOException e){
                logger.writelog("WRN","Couldn't build search index");
            }
        }
    }
    
    public void addTopicMap(String key,TopicMap tm){
        topicMaps.put(key,tm);
    }
    public TopicMap getTopicMap(String key){
        return (TopicMap)topicMaps.get(key);
    }
    
    private static long baseLocatorCounter=0;
    public static synchronized String getFreeBaseLocator(){
        return "http://wandora.org/si/"+(baseLocatorCounter++);
    }
    
    public TopicMap createTopicMap(){
        return new org.wandora.topicmap.memory.TopicMapImpl();
    }
    
    public TopicMap readTopicMap(String file) throws IOException, TopicMapException {
        FileInputStream fis=new FileInputStream(file);
        TopicMap tm=readTopicMap(fis);
        fis.close();
        return tm;
    }
    public TopicMap readTopicMap(String file,String key) throws IOException, TopicMapException {
        FileInputStream fis=new FileInputStream(file);
        TopicMap tm=readTopicMap(fis,key);
        fis.close();
        return tm;
    }

    public TopicMap readTopicMap(InputStream in) throws IOException, TopicMapException {
        logger.writelog("DBG","Wandora reading topicmap");
        TopicMap tm=createTopicMap();
        boolean v=tm.trackingDependent();
        tm.setTrackDependent(false);
        tm.importXTM(in);
        long time=System.currentTimeMillis();
        Iterator iter=tm.getTopics();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            t.setDependentEditTime(time);
        }
        tm.setTrackDependent(v);
        logger.writelog("DBG","Topicmap read");
        return tm;
    }
    public TopicMap readTopicMap(InputStream in,String key) throws IOException, TopicMapException {
        TopicMap tm=readTopicMap(in);
        addTopicMap(key,tm);
        return tm;
    }

    public void serializeTopicMap(TopicMap tm,OutputStream out) throws IOException,WandoraException {
        if(!lockTopicMap(LOCK_READ)) throw new WandoraException("Could not lock topicmap");
        try{
            tm.exportXTM(out);
        }finally{ releaseTopicMap(LOCK_READ); }
    }
    
    public void mergeInTopicMapsNoLock(Collection urls) throws WandoraException {
        Iterator iter=urls.iterator();
        while(iter.hasNext()){
            Object o=iter.next();
            URL url=null;
            if(o instanceof URL) url=(URL)o;
            else if(o instanceof String) try{
                url=new URL((String)o);
            }catch(MalformedURLException mue){
            }

            if(url==null) continue;
            try{                
                InputStream in=url.openStream();
                mergeInTopicMapNoLock(in);
            } catch(IOException ioe){
            }
        }
    }
    
    public void mergeInTopicMaps(Collection urls) throws WandoraException {
        if(!lockTopicMap(LOCK_WRITE)) throw new WandoraException("Could not lock topicmap");
        try{
            mergeInTopicMapsNoLock(urls);
        }finally{ releaseTopicMap(LOCK_WRITE); }
    }
    
    public void mergeInTopicMap(InputStream in) throws IOException,WandoraException {
        TopicMap tm=readTopicMap(in);
        mergeInTopicMap(tm);
    }
    public void mergeInTopicMapNoLock(InputStream in) throws IOException,WandoraException {
        TopicMap tm=readTopicMap(in);
        mergeInTopicMapNoLock(tm);
    }
    
    public void mergeInTopicMap(TopicMap tm) throws WandoraException {
        if(!lockTopicMap(LOCK_WRITE)) throw new WandoraException("Could not lock topicmap");
        try{
            topicMap.mergeIn(tm);
        }finally{ releaseTopicMap(LOCK_WRITE); }
    }
    public void mergeInTopicMapNoLock(TopicMap tm) throws WandoraException {
        topicMap.mergeIn(tm);
    }
    
    public Locator createLocator(String uri){
        try{
            return topicMap.createLocator(uri,"URI");
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
            return null;
        }
    }
    public TopicMap getTopicMap(){
        return topicMap;
    }
/*    public TopicMap getTopicMap(String locator){
        return getTopicMap(createLocator(locator));
    }
    public TopicMap getTopicMap(Locator locator){
        return topicMapSystem.getTopicMap(locator);
    }*/
    
    public Object lockObject(int depth,Topic object,int mode) {
        if(!tmLock.getLock(mode)) return null;
        else return new LockHandle(mode);
    }
    
    public boolean lockTopicMap(int mode){
//        logger.writelog("DBG","Lock status: "+tmLock.numReaders+" "+tmLock.numWriters+" "+tmLock.waitingWriters+" ("+mode+")");
        return tmLock.getLock(mode);
//        logger.writelog("DBG","Locked "+b);
//        return b;
    }
    public void releaseTopicMap(int mode){
        tmLock.releaseLock(mode);
    }
    public Object lockObject(LockCallBack callback,Set start,int mode){
        return lockObject(0,null,mode);
    }        
    public void releaseObject(Object handle){
        tmLock.releaseLock(((LockHandle)handle).mode);
    }
    
/*    public boolean isObjectValid(TopicMapObject o){
        return (topicMap.getObjectById(o.getObjectId())!=null);
    }
    */
/*    public TopicMapObject getObjectByID(String id){
        return topicMap.getObjectById(id);
    }*/
    public Topic getTopicBySI(String si){
        try{
            return topicMap.getTopic(si);
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
            return null;
        }
    }
    public Topic getTopicBySI(Locator si){
        try{
            return topicMap.getTopic(si);
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
            return null;
        }
    }
    public Collection getTopicsOfType(String si){
        try{
            Topic t=topicMap.getTopic(si);
            if(t==null) return new HashSet();
            else return topicMap.getTopicsOfType(t);
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
            return null;
        }
    }
    public Collection getTopicsOfType(Locator l){
        try{
            Topic t=topicMap.getTopic(l);
            if(t==null) return new HashSet();
            else return topicMap.getTopicsOfType(t);
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
            return null;
        }
    }
    public Topic getTopicByName(String name){
        try{
            return topicMap.getTopicWithBaseName(name);
        }catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
            return null;
        }
    }
    
    public boolean indexNeedsUpdate(){
        return indexNeedsUpdate;
    }
    public boolean isLazyIndexUpdate(){
        if(searchIndexPath==null || searchIndexPath.length()==0) return false;
        return lazyIndexUpdate;
    }
    
    public Collection search(String query) throws WandoraException {
        return search(query,null);
    }
    public Collection search(String query,String lang) throws WandoraException {
        if(!searchLock.getReaderLock()) throw new WandoraException("Could not lock search system");
        try{
            return searchTopic(query,searchIndexPath,lang);
        } 
        catch(IOException ioe){
            throw new WandoraException("Error processing search. "+ioe.getMessage());
        }
        finally { searchLock.releaseReaderLock(); }
    }
    public void updateSearchIndex() throws WandoraException {
        if(searchIndexPath==null || searchIndexPath.length()==0) return;
        if(lazyIndexUpdate){
            logger.writelog("DBG","Marking index for lazy update.");
            indexNeedsUpdate=true;
            return;
        }
        logger.writelog("DBG","Updating search index; changed "+changedTopics.size()+" shallow "+changedTopicsShallow.size());
        if(!lockTopicMap(LOCK_READ)) throw new WandoraException("Could not lock topic map");
        try{
            if(!searchLock.getWriterLock()) throw new WandoraException("Could not lock search system");
            try{
                synchronized(changedTopics){
                    logger.writelog("DBG","Topicmap and index locked, now updating");
                    updateIndex(changedTopics,changedTopicsShallow,searchIndexPath,getTopicMap());
                    changedTopics.clear();
                    changedTopicsShallow.clear();
                }
            } 
            catch(IOException ioe){
                logger.writelog("WRN","Couldn't update index "+ioe.getMessage());
                throw new WandoraException("Error updating index. "+ioe.getMessage());
            }
            finally { searchLock.releaseWriterLock(); }
        }finally { releaseTopicMap(LOCK_READ); }
        logger.writelog("DBG","Search index updated");
    }
    public void rebuildSearchIndex() throws WandoraException {
        rebuildSearchIndex(false);
    }
    public void rebuildSearchIndex(boolean checkNeedsUpdate) throws WandoraException {
        if(searchIndexPath==null || searchIndexPath.length()==0) return;
        if(!lockTopicMap(LOCK_READ)) throw new WandoraException("Could not lock topic map");
        try{
            if(!searchLock.getWriterLock()) throw new WandoraException("Could not lock search system");
            try{
                if(checkNeedsUpdate && !indexNeedsUpdate) return;
                synchronized(changedTopics){
                    buildIndex(searchIndexPath,topicMap);
                    changedTopics.clear();
                    changedTopicsShallow.clear();
                    indexNeedsUpdate=false;
                }
            } 
            catch(IOException ioe){
                throw new WandoraException("Error building index. "+ioe.getMessage());                
            }
            finally { searchLock.releaseWriterLock(); }
        }finally { releaseTopicMap(LOCK_READ); }
        
    }
    public TMBox getTMBox(){
        return tmBox;
    }
    

    private void closeSearcher() throws IOException {
        if(searcher!=null){
            searcher.close();
            searcher=null;
        }
    }

    private void buildIndex(String indexPath,TopicMap tm) throws IOException  {
        if(searchIndexPath==null || searchIndexPath.length()==0) return;
        logger.writelog("DBG","Wandora building search index");
        closeSearcher();
        TopicMapIndexBuilder indexBuilder=new TopicMapIndexBuilder(logger);
        indexBuilder.processTopicMap(getTopicMap(), indexPath);
        logger.writelog("DBG","Search index built");
    }

    /**
     * @param changed The subject indicators of the changed topics as Strings. Only one subject indicator per
     *                changed topic is needed although it doesn't matter if it contains several subject indicators
     *                of the same topic.
     */
    private void updateIndex(Set changed,Set changedShallow,String indexPath,TopicMap tm) throws IOException,TopicMapException {
        closeSearcher();
        TopicMapIndexBuilder indexBuilder=new TopicMapIndexBuilder(logger);
        indexBuilder.updateTopics(changed,changedShallow,indexPath,tm);
    }
    
    private Collection searchTopic(String word, int searchIndex) throws IOException{
        return searchTopic(word,new Integer(searchIndex).toString(),null);
    }    
    private Collection searchTopic(String word, String searchIndex) throws IOException{
        return searchTopic(word,searchIndex,null);
    }
    private Collection searchTopic(String word, String searchIndex, String lang) throws IOException{
        if(searcher==null){
            synchronized(this){
                if (searcher==null) {
                    try {
                        IndexReader reader=IndexReader.open(new File(searchIndex));
                        searcher=new IndexSearcher(reader);
                    }
                    catch(IOException e) {
                        logger.writelog("ERR", "No search index available! Unable to search topic map!!");
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        Query q=TopicSearch.parseQuery(word.toLowerCase());

        HashMap h=new HashMap(); // store info about handled topics here to remove duplicate hits (one hit for the topic itself and the other for its occurrence)
        Hits hits=searcher.search(q);
        Collection searchResult = new ArrayList();
        int numHits=hits.length();
        if(numHits>500) numHits=500;
        for(int i=0;i<numHits;i++){
            double score=hits.score(i);
            String topic=hits.doc(i).get("topic");
                        if(topic.indexOf("\n")!=-1) topic=topic.substring(0,topic.indexOf("\n"));
                        topic=topic.trim();
            String type=hits.doc(i).get("type");
            String keywords=hits.doc(i).get("keywords");
            // text is not stored
            String url=hits.doc(i).get("url");
                        // LogWriter.println("DBG","Topic '"+topic+"' in search results");
            if(h.get(topic)==null) {
                h.put(topic,topic); // just put something in the hash map so it doesn't return null any more
                Topic t = null;
                try {
                    t = getTopicBySI(topic);
                }
                catch(Exception e) {
                    logger.writelog("DBG","Catched "+e.getClass().getName()+" when parsing search results. "+e.getMessage());
                    continue;
                }
                if(t==null){
                    logger.writelog("DBG","Search found topic '"+topic+"' but topic not found in topic map");
                    continue;
                }
                searchResult.add(new SearchResultItem(t,score,lang));
            }
        }
        return searchResult;
    }
    
    public static void main(String[] args) throws Exception {
        Properties properties=new Properties();
        properties.put(PROP_SHUTDOWNHOOK,"true");
        properties.put(PROP_ADMINPASS,"nimda");
        WandoraManager manager=new WandoraManager(properties);
        FileInputStream fis=new FileInputStream(args[0]);
        System.out.println("reading topicmap");
        TopicMap tm=manager.readTopicMap(fis);
        fis.close();
        manager.addTopicMap("default",tm);
/*        if(args.length>1){
            fis=new FileInputStream(args[1]);
            TopicMap tm2=manager.readTopicMap(fis);
            fis.close();
            org.wandora.topicmap.merged.MergedTopicMap merged=
                    new org.wandora.topicmap.merged.MergedTopicMap();
            merged.addTopicMap("tm1",tm);
            merged.addTopicMap("tm2",tm2);
            manager.addTopicMap("secondary",tm2);
            manager.addTopicMap("merged",merged);
        }*/
        manager.initialize(tm);
//        AdminSocketServer server=new AdminSocketServer(manager);        
//        System.out.println("starting server");
//        server.start();
    }
    
    public void saveTopicMaps(boolean locked) throws IOException, TopicMapException {
        if(locked){
            saveTopicMapsNoLock();
            return;
        }
        if(lockTopicMap(WandoraManager.LOCK_READ)){
            try{
                saveTopicMapsNoLock();
            }
            finally{
                releaseTopicMap(WandoraManager.LOCK_READ);
            }                    
        }        
    }
    public void saveTopicMapsNoLock() throws IOException, TopicMapException {
        Iterator iter=this.autoSaveMaps.iterator();
        while(iter.hasNext()){
            String map=(String)iter.next();
            TopicMap tm=getTopicMap(map);
            if(tm!=null){
                String file=autoSaveFile+"autosave-"+map+".xtm";
                OutputStream out=new java.io.FileOutputStream(file);
                tm.exportXTM(out);
                out.close();
                
                // TODO: might be a good idea to move copying of history files outside locks
                if(keepHistory){
                    java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
                    String historyFile=historySaveFile+"history-"+map+"-"+sdf.format(new Date())+".xtm.gz";
                    FileInputStream fis=new FileInputStream(file);
                    FileOutputStream fos=new FileOutputStream(historyFile);
                    java.util.zip.GZIPOutputStream gos=new java.util.zip.GZIPOutputStream(fos);
                    int r=0;
                    byte[] buf=new byte[4096];
                    while( (r=fis.read(buf))!=-1 ){
                        gos.write(buf,0,r);
                    }
                    gos.finish();
                    fos.close();
                    fis.close();
                }
            }
            else{
                logger.writelog("WRN","Can't save map with key "+map+". No such map.");
            }
        }        
    }
    
    public void run() {
        while(running){
            try{
                Thread.sleep(60000);
                runCount++;
            }
            catch(InterruptedException ie){ }
            if(running){
                try{
                   if(saveInterval > 0 && runCount % saveInterval == 0) {
                        saveTopicMaps(false);
                   }
                   if(projectFileAutoUpdate) {
                       try {
                            File f = new File(projectFile);
                            if(f.exists()) {
                                long newProjectFileDateStamp = f.lastModified();
                                if(newProjectFileDateStamp != projectFileDateStamp) {
                                    if(topicMap != null) topicMap.removeTopicMapListener(this);
                                    topicMap = null;
                                    changedTopics.clear();
                                    changedTopicsShallow.clear();
                                    LayerStack ls = new LayerStack(projectFile);
                                    initialize(ls);
                                    projectFileDateStamp = newProjectFileDateStamp;
                                }
                            }
                       }
                       catch(Exception e) {
                           e.printStackTrace();
                       }
                   }
                }
                catch(Exception e){
                    logger.writelog("WRN","Auto save failed.",e);
                }
            }
        }
    }
    
    public void doShutdown(){
        logger.writelog("INF","Shutting down wandora");
        // stop save thread
        running=false;
        if(workerThread!=null) workerThread.interrupt();
        // lock topicmap so nobody can edit or read it any more
        lockTopicMap(WandoraManager.LOCK_WRITE);
        if(workerThread!=null){
            try{
                workerThread.join();
            }
            catch(InterruptedException e){
            }
        }
        // save topic map
        try{
            saveTopicMaps(true);
        }
        catch(Exception e){
            logger.writelog("WRN","Save failed",e);
        }
        logger.writelog("INF","Shutdown sequence complete");        
    }

    public void topicChanged(Topic topic) throws TopicMapException {
        topicChanged(topic,false);
    }
    public void topicChanged(Topic topic,boolean shallow) throws TopicMapException {
        // Add topic with two locators because one may just have been added. In this case
        // the topic won't be found from the index using that locator.
        Locator l1=null,l2=null;
        Iterator iter=topic.getSubjectIdentifiers().iterator();
        if(iter.hasNext()) l1=(Locator)iter.next();
        if(iter.hasNext()) l2=(Locator)iter.next();
        if(l1!=null){
            synchronized(changedTopics){
                if(!shallow) {
                    changedTopics.add(l1.toExternalForm());
                    if(l2!=null) changedTopics.add(l2.toExternalForm());
                }
                else{ 
                    changedTopicsShallow.add(l1.toExternalForm());
                    if(l2!=null) changedTopicsShallow.add(l2.toExternalForm());
                }
            }
        }
    }
    
    public void associationChanged(Association a)  throws TopicMapException {
        Iterator iter=a.getRoles().iterator();
        while(iter.hasNext()){
            Topic role=(Topic)iter.next();
            Topic player=a.getPlayer(role);
            topicChanged(player,true);
        }
    }
    
    public void associationRemoved(Association a)  throws TopicMapException {
        associationChanged(a);
    }
    
    public void topicRemoved(Topic t)  throws TopicMapException {
        topicChanged(t);
    }
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException{
        topicChanged(t);
    }
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException{
        topicChanged(t);        
    }
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        topicChanged(t);        
    }
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        topicChanged(t);        
    }
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        topicChanged(t);        
    }
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        topicChanged(t);        
    }
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        associationChanged(a);
    }
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        associationChanged(a);        
    }

    
    
    public boolean isAdminUser(String user,String password){
        if(adminPass==null) return false;
        if(user.equals("admin") && password.equals(adminPass)) return true;
        else return false;
    }
    
    private class WandoraShutdownHook implements Runnable {

        public void run() {
            doShutdown();
        }

    }
        
}
class LockHandle {
    public int mode;
    public LockHandle(int mode){
        this.mode=mode;
    }
}
class SystemLogger extends org.wandora.piccolo.Logger {

    public void writelog(String level, String s) {
        System.out.println("["+level+"] "+s);
    }

}
