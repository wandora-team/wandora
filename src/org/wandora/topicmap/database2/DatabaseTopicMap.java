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
 * DatabaseTopicMap.java
 *
 * Created on 7. marraskuuta 2005, 11:28
 */

package org.wandora.topicmap.database2;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.wandora.topicmap.*;
import java.util.Hashtable;



/**
 *
 * @author olli, akivela
 */
public class DatabaseTopicMap extends AbstractDatabaseTopicMap {
    
    protected boolean changed;
    
    /** The WeakTopicIndex used to index topics. Note that index needs information
     * about changes in topic map and should also be used to construct new
     * topics to make sure that there are not two different instances of the same topic.
     * @see WeakTopicIndex
     */
    protected WeakTopicIndex topicIndex;
    
    protected ArrayList<TopicMapListener> topicMapListeners;
    protected ArrayList<TopicMapListener> disabledListeners;
    

    

    
    /**
     * <p>
     * A flag indicating that the <code>topicIndex</code> is a full index of
     * everything existing in this topic map. Normally it is not. However, when
     * you import something in an empty topic map, you can stop the index
     * cleaner thread that is normally deleting rarely used topics from
     * index. Because all created topics and associations are added to the index, this will
     * result in an index containing everything in the topic map.
     * </p><p>
     * The index is done with weak references so the actual objects might not
     * be found in the index but even if this is the case, the index will contain
     * the information if such an object exists in the actual database or not.
     * </p>
     */
    protected boolean completeIndexes=false;

    protected Object indexLock=new Object();

    
    
    
    /** Creates a new instance of DatabaseTopicMap */
    public DatabaseTopicMap(String dbDriver, String dbConnectionString, String dbUser, String dbPassword) throws SQLException {
        this(dbDriver, dbConnectionString, dbUser, dbPassword, null);
    }
    
    
    public DatabaseTopicMap(String dbDriver, String dbConnectionString, String dbUser, String dbPassword, Object connectionParams) throws SQLException {
        this(dbDriver, dbConnectionString, dbUser, dbPassword, null, connectionParams);
    }
    
    
    public DatabaseTopicMap(String dbDriver, String dbConnectionString, String dbUser, String dbPassword, String initScript) throws SQLException {
        this(dbDriver, dbConnectionString, dbUser, dbPassword, initScript, null);
    }
    
    
    public DatabaseTopicMap(String dbDriver, String dbConnectionString, String dbUser, String dbPassword, String initScript, Object connectionParams) throws SQLException {
        super(dbDriver, dbConnectionString, dbUser, dbPassword, initScript, connectionParams);
        topicMapListeners=new ArrayList<TopicMapListener>();
        topicIndex=new WeakTopicIndex();
        changed=false;
    }
    

    
    
    
    /**
     * Deletes everything in the topic map by clearing the database tables.
     */
    @Override
    public void clearTopicMap() throws TopicMapException {
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        
        topicIndex.stopCleanerThread();
        executeUpdate("delete from MEMBER");
        executeUpdate("delete from ASSOCIATION");
        executeUpdate("delete from VARIANTSCOPE");
        executeUpdate("delete from VARIANT");
        executeUpdate("delete from DATA");
        executeUpdate("delete from TOPICTYPE");
        executeUpdate("delete from SUBJECTIDENTIFIER");
        executeUpdate("delete from TOPIC");
        topicIndex=new WeakTopicIndex();
        changed=true;
    }
    
    
    /**
     * Clears the topic index (cache) containing recently accessed database topics.
     * Any topic found in the index is not retrieved from the database and it is
     * impossible for DatabaseTopicMap to detect any changes in the database. Thus
     * the index may contain outdated information and you will need to manually
     * clear the index to force retrieving of topics directly from the database.
     */
    @Override
    public void clearTopicMapIndexes() {
        topicIndex.stopCleanerThread();
        topicIndex = new WeakTopicIndex();
    }
    
    

    
    public void printIndexDebugInfo(){
        topicIndex.printDebugInfo();
    }
    

    
    
    /* 
     * Method checks if database topic map contains topics without subject
     * identifiers and inserts default SI to each such topic.
     *
     * These unindentified topics occur rarely if a tool or import fails for
     * example. As unidentified topics cause serious problems if used in
     * Wandora application user should have a back door to make database
     * topic map consistent. 
     */
    public void checkSIConsistency() throws TopicMapException {
        checkSIConsistency(getLogger());
    }
    
    
    public void checkSIConsistency(TopicMapLogger logger) throws TopicMapException {
        if(unconnected) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        logger.log("Finding topic's without SIs!");
        String query = "select TOPICID from TOPIC left join SUBJECTIDENTIFIER on SUBJECTIDENTIFIER.TOPIC=TOPIC.TOPICID where SI is NULL";
        Collection<Map<String,Object>> res = executeQuery(query);
        String topicID = null;
        String defaultSI = null;
        logger.log("Found total "+res.size()+" topics without SIs!");
        for(Map<String,Object> row : res) {
            topicID = row.get("TOPICID").toString();
            defaultSI = TopicTools.createDefaultLocator().toExternalForm();
            logger.log("Inserting SI '"+defaultSI+" to topic with id '"+topicID+"'.");
            if(topicID != null && defaultSI != null) {
                executeUpdate("insert into SUBJECTIDENTIFIER (TOPIC, SI) values ('"+escapeSQL(topicID)+"', '"+escapeSQL(defaultSI)+"')");
            }
        }
    }
    
    
    
    @Override
    public void checkAssociationConsistency(TopicMapLogger logger) throws TopicMapException {
        if(unconnected) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
//        if(true) return; // consistency disabled
        if(logger != null) logger.log("Checking association consistency!");
        int counter=0;
        int deleted=0;
        Collection<Map<String,Object>> res=executeQuery("select distinct PLAYER from MEMBER");
        for(Map<String,Object> row : res){
            String player=row.get("PLAYER").toString();
            HashSet<ArrayList<String>> associations=new LinkedHashSet<ArrayList<String>>(500);
            Collection<Map<String,Object>> res2=executeQuery(
                    "select ASSOCIATION.*,M2.* from ASSOCIATION,MEMBER as M1, MEMBER as M2 "+
                    "where M1.PLAYER='"+escapeSQL(player)+"' and M1.ASSOCIATION=ASSOCIATIONID and "+
                    "M2.ASSOCIATION=ASSOCIATION.ASSOCIATIONID order by ASSOCIATION.ASSOCIATIONID,M2.ROLE"
                    );
            String associationid="";
            ArrayList<String> v=new ArrayList<String>(9);
            for(Map<String,Object> row2 : res2) {
                String id=row2.get("ASSOCIATIONID").toString();
                if(!associationid.equals(id)){
                    if(!associationid.equals("")) {
                        if(!associations.add(v)) {
                            logger.hlog("Deleting association with id "+associationid);
                            executeUpdate("delete from MEMBER where ASSOCIATION='"+escapeSQL(associationid)+"'");
                            executeUpdate("delete from ASSOCIATION where ASSOCIATIONID='"+escapeSQL(associationid)+"'");
                            deleted++;
                        }
                        v=new ArrayList<String>(9);
                        String type=row2.get("TYPE").toString();
                        v.add(type);
                    }
                    associationid=id;
                }
                String r=row2.get("ROLE").toString();
                String p=row2.get("PLAYER").toString();
                v.add(r); v.add(p);
            }
            if(!associations.add(v)) {
                logger.hlog("Deleting association with id "+associationid);
                executeUpdate("delete from MEMBER where ASSOCIATION='"+escapeSQL(associationid)+"'");
                executeUpdate("delete from ASSOCIATION where ASSOCIATIONID='"+escapeSQL(associationid)+"'");
                deleted++;
            }
                    
            counter++;
//            System.out.println("Counter "+counter);
        }
        if(logger != null) logger.log("Association consistency deleted "+deleted+" associations!");
    }

    
    /**
     * Closes the topic map. This closes the topic index freeing some resources
     * and closes the database connection. Topic map cannot be used or reopened
     * after it has been closed.
     */
    @Override
    public void close(){
        topicIndex.destroy();
        super.close();
    }
    
    
    
    // --------------------------------------------------------- BUILD TOPIC ---
    
    /**
     * Builds a database topic from a database query result row. The row should contain
     * (at least) three columns with names "TOPICID", "BASENAME" and "SUBJECTLOCATOR"
     * that are used to build the topic. 
     */
    public DatabaseTopic buildTopic(Map<String,Object> row) throws TopicMapException {
        return buildTopic(row.get("TOPICID"), row.get("BASENAME"), row.get("SUBJECTLOCATOR"));
    }
    
    
    /**
     * Builds a database topic when given the topic id, basename and subject locator.
     */
    public DatabaseTopic buildTopic(Object id, Object baseName, Object subjectLocator) throws TopicMapException {
        return buildTopic((String)id, (String)baseName, (String)subjectLocator);
    }
    
    
    /**
     * Builds a database topic when given the topic id, basename and subject locator.
     */
    public DatabaseTopic buildTopic(String id, String baseName, String subjectLocator) throws TopicMapException {
        DatabaseTopic dbt=topicIndex.getTopicWithID(id);
        if(dbt == null){
            dbt = topicIndex.createTopic(id,this);
            dbt.initialize(baseName,subjectLocator);
        }
        else {
            if(baseName != null) {
                dbt.setBaseName(baseName);
            }
            if(subjectLocator != null) {
                dbt.setSubjectLocator(new Locator(subjectLocator));
            }
        }
        return dbt;
    }
    
    
    // --------------------------------------------------- BUILD ASSOCIATION ---
    
    /**
     * Builds a database association from a database query result row. The row should contain
     * (at least) four columns with names "ASSOCIATIONID", "TOPICID", "BASENAME" and "SUBJECTLOCATOR"
     * where all but the "ASSOCIATIONID" are the core properties of the association type topic.
     */
    public DatabaseAssociation buildAssociation(Map<String,Object> row) throws TopicMapException {
        return buildAssociation(row.get("ASSOCIATIONID"), row.get("TOPICID"), row.get("BASENAME"), row.get("SUBJECTLOCATOR"));
    }
    
    
    public DatabaseAssociation buildAssociation(Object associationId,Object typeId,Object typeName,Object typeSL) throws TopicMapException {
        return buildAssociation((String)associationId, (String)typeId, (String)typeName, (String)typeSL);
    }
    
    
    public DatabaseAssociation buildAssociation(String associationId, String typeId, String typeName, String typeSL) throws TopicMapException {
        DatabaseAssociation dba=topicIndex.getAssociation(associationId,this);
        if(dba!=null) return dba;
        DatabaseTopic type=buildTopic(typeId,typeName,typeSL);
        return buildAssociation(associationId,type);
    }
    
    
    public DatabaseAssociation buildAssociation(String associationId, DatabaseTopic type){
        DatabaseAssociation dba=topicIndex.getAssociation(associationId,this);
        if(dba==null){
            dba=topicIndex.createAssociation(associationId,this);
            dba.initialize(type);
        }
        return dba;
    }
    
    
    // --------------------------------------------------------- QUERY TOPIC ---
    
    
    /**
     * Executes a database query and returns results as a collection of topics.
     * The result set must have the same columns used by buildTopic method.
     */
    public Collection<Topic> queryTopic(String query)  throws TopicMapException {
        Collection<Map<String,Object>> res = executeQuery(query);
        Collection<Topic> ret = new ArrayList<Topic>();
        for(Map<String,Object> row : res){
            ret.add(buildTopic(row));
        }
        return ret;
    }
    
    
    /**
     * Same as queryTopic but only returns the first topic in the result set or
     * null if the result set is empty. 
     */ 
    public Topic querySingleTopic(String query)  throws TopicMapException {
        Collection<Topic> c=queryTopic(query);
        if(c.isEmpty()) return null;
        else return c.iterator().next();
    }
    
    
    /**
     * Executes a database query and returns results as a collection of associations.
     * The result set must have the same columns used by buildAssociation method.
     */
    public Collection<Association> queryAssociation(String query) throws TopicMapException {
        Collection<Map<String,Object>> res = executeQuery(query);
        ArrayList<Association> ret = new ArrayList<Association>();
        for(Map<String,Object>row : res){
            ret.add(buildAssociation(row));
        }
        return ret;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public Topic getTopic(Collection<Locator> SIs) throws TopicMapException {
        for(Locator l : SIs){
            Topic t=getTopic(l);
            if(t!=null) return t;
        }
        return null;
    }
    
    
    
    public void topicSIChanged(DatabaseTopic t,Locator deleted,Locator added){
        topicIndex.topicSIChanged(t,deleted,added);
    }
    
    
    public void topicBNChanged(Topic t,String old) throws TopicMapException {
        topicIndex.topicBNChanged(t,old);
    }
    
    
    @Override
    public Topic getTopic(Locator si) throws TopicMapException {
        if(unconnected) return null;
        if(topicIndex.containsKeyWithSI(si)) {
            Topic t=topicIndex.getTopicWithSI(si);
            // may return null if, A) has mapping that no topic with si exists or
            // B) there is a mapping but the weak referenc has been cleared
            if(t!=null) return t; // clear case, return
            else if(topicIndex.isNullSI(si)) {
                // if no topic exists, return null, otherwise refetch the topic
                return null;
            }
        }
        else if(completeIndexes) return null; // index is complete so if nothing is mentioned then there is no such topic
        
        Topic t=querySingleTopic("select TOPIC.* "
                + "from TOPIC,SUBJECTIDENTIFIER "
                + "where TOPIC=TOPICID "
                + "and SI='"+escapeSQL(si.toExternalForm())+"'");
        if(t==null) {
            topicIndex.addNullSI(si);
        }
        return t;
    }
    
    
    @Override
    public Topic getTopicBySubjectLocator(Locator sl) throws TopicMapException {
        if(unconnected) return null;
        return querySingleTopic("select * from TOPIC "
                +"where SUBJECTLOCATOR='"
                +escapeSQL(sl.toExternalForm())+"'");
    }
    
    
    @Override
    public Topic createTopic(String id) throws TopicMapException {
        if(unconnected) return null;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        DatabaseTopic t = topicIndex.newTopic(id, this);
        return t;
    }
    
    
    @Override
    public Topic createTopic() throws TopicMapException {
        if(unconnected) return null;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        DatabaseTopic t=topicIndex.newTopic(this);
        return t;
    }
    
    
    @Override
    public Association createAssociation(Topic type) throws TopicMapException {
        if(unconnected) return null;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        DatabaseAssociation a=topicIndex.newAssociation((DatabaseTopic)type,this);
        return a;
    }
    
    
    @Override
    public Collection<Topic> getTopicsOfType(Topic type) throws TopicMapException {
        if(unconnected) return new ArrayList<Topic>();
        Collection<Topic> res=queryTopic("select TOPIC.* "
                +"from TOPIC,TOPICTYPE "
                +"where TOPIC=TOPICID "
                +"and TYPE='"+escapeSQL(type.getID())+"'");
        Map<String,DatabaseTopic> collected=new LinkedHashMap<String,DatabaseTopic>();
        for(Topic t : res){
            collected.put(t.getID(),(DatabaseTopic)t);
        }
        DatabaseTopic.fetchAllSubjectIdentifiers(
                executeQuery("select SUBJECTIDENTIFIER.* "
                        +"from SUBJECTIDENTIFIER,TOPIC,TOPICTYPE "
                        +"where SUBJECTIDENTIFIER.TOPIC=TOPIC.TOPICID "
                        +"and TOPIC.TOPICID=TOPICTYPE.TOPIC "
                        +"and TOPICTYPE.TYPE='"+escapeSQL(type.getID())+"' "
                        +"order by SUBJECTIDENTIFIER.TOPIC"),
                             collected, this);
        return res;
    }
    
    
    
    @Override
    public Topic getTopicWithBaseName(String name) throws TopicMapException {
        if(unconnected) return null;
        if(topicIndex.containsKeyWithBN(name)) {
            // see notes in getTopic(Locator)
            Topic t=topicIndex.getTopicWithBN(name);
            if(t!=null) return t;
            else if(topicIndex.isNullBN(name)) return null;
        }
        else if(completeIndexes) return null;
        Topic t=querySingleTopic("select * from TOPIC where BASENAME='"+escapeSQL(name)+"'");
        if(t==null) topicIndex.addNullBN(name);
        return t;
    }
    
    
    
    @Override
    public Iterator<Topic> getTopics() {
        final Iterator<Map<String,Object>> rowIterator = getRowIterator("select * from TOPIC", "TOPICID");

        return new Iterator<Topic>() {
            
            @Override
            public boolean hasNext() {
                return rowIterator.hasNext();
            }

            @Override
            public Topic next() {
                if(!hasNext()) {
                    throw new NoSuchElementException();
                }
                Map<String,Object> row = rowIterator.next();
                try {
                    return buildTopic(row);
                }
                catch(TopicMapException tme) {
                    tme.printStackTrace(); // TODO EXCEPTION
                    return null;
                }
            }

            @Override
            public void remove() {
                rowIterator.remove();
            }
        };
    }
    
    
    
    @Override
    public Topic[] getTopics(String[] sis) throws TopicMapException {
        if(unconnected) {
            return new Topic[0];
        }
        Topic[] ret=new Topic[sis.length];
        for(int i=0;i<sis.length;i++){
            ret[i]=getTopic(sis[i]);
        }
        return ret;
    }
    
    
    /**
     * Note that you must iterate through all rows because statement and result set
     * will only be closed after last row has been fetched.
     */
    @Override
    public Iterator<Association> getAssociations() {

        final Iterator<Map<String,Object>> rowIterator = getRowIterator("select * from ASSOCIATION,TOPIC where TOPICID=TYPE", "TOPIC.TOPICID");

        return new Iterator<Association>() {
            
            @Override
            public boolean hasNext() {
                return rowIterator.hasNext();
            }

            @Override
            public Association next() {
                if(!hasNext()) {
                    throw new NoSuchElementException();
                }
                Map<String,Object> row = rowIterator.next();
                try {
                    return buildAssociation(row);
                }
                catch(TopicMapException tme) {
                    tme.printStackTrace(); // TODO EXCEPTION
                    return null;
                }
            }

            @Override
            public void remove() {
                rowIterator.remove();
            }
        };
    }
    
    
    @Override
    public Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException {
        if(unconnected) {
            return new ArrayList<Association>();
        }
        return queryAssociation("select * "
                + "from ASSOCIATION,TOPIC "
                + "where TYPE=TOPICID "
                + "and TOPICID='"+escapeSQL(type.getID())+"'");
    }
    
    
    
    @Override
    public int getNumTopics() {
        int count = executeCountQuery("select count(*) from TOPIC");
        return count;
    }
    
    
    
    @Override
    public int getNumAssociations() {
        int count = executeCountQuery("select count(*) from ASSOCIATION");
        return count;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------- COPY TOPICIN / ASSOCIATIONIN ---
    // -------------------------------------------------------------------------
    
    
    
    
    private Topic _copyTopicIn(Topic t, boolean deep, HashMap<Topic,Locator> copied) throws TopicMapException {
        return _copyTopicIn(t,deep,false,copied);
    }
    
    
    private Topic _copyTopicIn(Topic t, boolean deep, boolean stub, HashMap<Topic,Locator> copied) throws TopicMapException {
        
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
        if(nt==null) {
            nt=createTopic();
        }

        for(Locator l : t.getSubjectIdentifiers()){
            nt.addSubjectIdentifier(l);
        }
        
        if(nt.getSubjectIdentifiers().isEmpty()) {
            System.out.println("Warning no subject indicators in topic. Creating default SI.");
            long randomNumber = System.currentTimeMillis() + Math.round(Math.random() * 99999);
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

            for(Topic type : t.getTypes()){
                Topic ntype=_copyTopicIn(type,deep,true,copied);
                nt.addType(ntype);
            }

            for(Set<Topic> scope : t.getVariantScopes()) {
                Set<Topic> nscope=new LinkedHashSet();
                for(Topic st : scope){
                    Topic nst=_copyTopicIn(st,deep,true,copied);
                    nscope.add(nst);
                }
                nt.setVariant(nscope, t.getVariant(scope));
            }

            for(Topic type : t.getDataTypes()) {
                Topic ntype=_copyTopicIn(type,deep,true,copied);
                Hashtable<Topic,String> versiondata=t.getData(type);
                for(Map.Entry<Topic,String> e : versiondata.entrySet()){
                    Topic version=e.getKey();
                    String data=e.getValue();
                    Topic nversion=_copyTopicIn(version,deep,true,copied);
                    nt.setData(ntype,nversion,data);
                }
            }
        }
        return nt;
    }
    
    
    
    private Association _copyAssociationIn(Association a) throws TopicMapException {
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
        
        for(Topic role : a.getRoles()) {
            Topic nrole = null;
            if(role.getSubjectIdentifiers().isEmpty()) {
                System.out.println("Warning, topic has no subject identifiers. Creating default SI!");
                //role.addSubjectIdentifier(new Locator("http://wandora.org/si/temp/" + System.currentTimeMillis()));
            }
            else {
                nrole=getTopic((Locator)role.getSubjectIdentifiers().iterator().next());
            }
            if(nrole==null) nrole=copyTopicIn(role,false);
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
            na.addPlayer(nplayer,nrole);
        }
        return na;
    }

    
    
    @Override
    public Association copyAssociationIn(Association a) throws TopicMapException {
        if(unconnected) return null;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Association n = _copyAssociationIn(a);
        Topic minTopic = null;
        int minCount = Integer.MAX_VALUE;
        for(Topic role : n.getRoles()) {
            Topic t = n.getPlayer(role);
            if(t.getAssociations().size()<minCount){
                minCount = t.getAssociations().size();
                minTopic = t;
            }
        }
        if(minTopic != null) {
            ((DatabaseTopic)minTopic).removeDuplicateAssociations();
        }
        return n;
    }
    
    
    @Override
    public Topic copyTopicIn(Topic t, boolean deep)  throws TopicMapException {
        if(unconnected) return null;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        return _copyTopicIn(t, deep, false, new LinkedHashMap());
    }
    
    
    @Override
    public void copyTopicAssociationsIn(Topic t) throws TopicMapException {
        if(unconnected) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        Topic nt=getTopic((Locator)t.getSubjectIdentifiers().iterator().next());
        if(nt==null) nt=copyTopicIn(t,false);
        for(Association a : t.getAssociations()) {
            _copyAssociationIn(a);
        }
        ((DatabaseTopic)nt).removeDuplicateAssociations();        
    }
    
    

    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------ IMPORT / MERGE ---
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void importXTM(java.io.InputStream in, TopicMapLogger logger) throws java.io.IOException,TopicMapException {
        if(unconnected) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        int numTopics=getNumTopics();
        if(numTopics==0) {
            System.out.println("Merging to empty topic map");
            topicIndex.stopCleanerThread();
            completeIndexes=topicIndex.isFullIndex();
        }
        else {
            System.out.println("Merging to non-empty topic map (numTopcis="+numTopics+")");
        }

        boolean old=getConsistencyCheck();
        setConsistencyCheck(false);
        boolean check=!getConsistencyCheck();            

        super.importXTM(in, logger);
        topicIndex.clearTopicCache();
        topicIndex.startCleanerThread();
        completeIndexes=false;

        if(check && old) checkAssociationConsistency();
        setConsistencyCheck(old);
    }
    
    
    @Override
    public void importLTM(java.io.InputStream in, TopicMapLogger logger) throws java.io.IOException,TopicMapException {
        if(unconnected) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        int numTopics=getNumTopics();
        if(numTopics==0) {
            System.out.println("Merging to empty topic map");
            topicIndex.stopCleanerThread();
            completeIndexes=topicIndex.isFullIndex();
        }
        else {
            System.out.println("Merging to non-empty topic map (numTopcis="+numTopics+")");
        }
        
        boolean old=getConsistencyCheck();
        setConsistencyCheck(false);
        boolean check=!getConsistencyCheck();            
        
        super.importLTM(in, logger);
        topicIndex.clearTopicCache();
        topicIndex.startCleanerThread();
        completeIndexes=false;
        
        if(check && old) checkAssociationConsistency();
        setConsistencyCheck(old);
    }  
    
    
    @Override
    public void importLTM(java.io.File in) throws java.io.IOException,TopicMapException {
        if(unconnected) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        int numTopics=getNumTopics();
        if(numTopics==0) {
            System.out.println("Merging to empty topic map");
            topicIndex.stopCleanerThread();
            completeIndexes=topicIndex.isFullIndex();
        }
        else System.out.println("Merging to non-empty topic map (numTopcis="+numTopics+")");
        
        boolean old=getConsistencyCheck();
        setConsistencyCheck(false);
        boolean check=!getConsistencyCheck();            

        super.importLTM(in);
        topicIndex.clearTopicCache();
        topicIndex.startCleanerThread();
        completeIndexes=false;

        if(check && old) checkAssociationConsistency();
        setConsistencyCheck(old);
    }    
    
   
    // -------------------------------------------------------------------------
    
    
    @Override
    public void mergeIn(TopicMap tm, TopicMapLogger tmLogger) throws TopicMapException {
        if(unconnected) return;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        int numTopics=getNumTopics();
        if(numTopics==0) {
            tmLogger.log("Merging to empty topic map");
            topicIndex.stopCleanerThread();
            completeIndexes=topicIndex.isFullIndex();
        }
        else tmLogger.log("Merging to non-empty topic map (numTopics="+numTopics+")");
        
        int targetNumTopics=tm.getNumTopics();
        int targetNumAssociations=tm.getNumAssociations();
        
        int tcount=0;
        HashMap copied=new LinkedHashMap();
        {
            // note that in some topic map implementations (DatabaseTopicMap specifically)
            // you must always iterate through all topics to close the result set thus
            // don't break the while loop even after forceStop
            Iterator<Topic> iter=tm.getTopics();
            while(iter.hasNext()) {
                Topic t = null;
                try {
                    t=iter.next();
                    if(!tmLogger.forceStop()) {
                        _copyTopicIn(t,true,false,copied);
                    }
                    tcount++;
                }
                catch (Exception e) {
                    tmLogger.log("Unable to copy topic (" + t + ").",e);
                }
                if((tcount%1000)==0) tmLogger.hlog("Copied "+tcount+"/"+targetNumTopics+" topics and 0/"+targetNumAssociations+" associations");
            }
        }
        if(!tmLogger.forceStop()){
            HashSet<Topic> endpoints=new LinkedHashSet<Topic>();
            int acount=0;
            Iterator<Association> iter=tm.getAssociations();
            while(iter.hasNext()) {
                try {
                    Association a=iter.next();
                    if(!tmLogger.forceStop()) {
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
                    }
                    acount++;
                    if((acount%1000)==0) tmLogger.hlog("Copied "+tcount+"/"+targetNumTopics+" topics and "+acount+"/"+targetNumAssociations+" associations");
                }
                catch (Exception e) {
                    tmLogger.log("Unable to copy association.",e);
                }
            }
            tmLogger.log("merged "+tcount+" topics and "+acount+" associations");
            tmLogger.log("cleaning associations");
            for(Topic t : endpoints){
                ((DatabaseTopic)t).removeDuplicateAssociations();
            }        
        }
        topicIndex.startCleanerThread();
        completeIndexes=false;
    }
    
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------------- INDEX ---
    // -------------------------------------------------------------------------
    

    /**
     * Tries to set complete index attribute. This is only possible if the topic map is
     * empty (getNumTopics returns 0). If this is the case, will stop index cleaner
     * thread and set the completeIndexes attribute to true.
     * @see #completeIndexes
     */
    public void setCompleteIndex(){
        if(getNumTopics()==0) {
            topicIndex.stopCleanerThread();
            completeIndexes=topicIndex.isFullIndex();
        }
    }
    
    
    /**
     * Restarts the topic index cleaner thread. You need to call this at some point
     * if you stopped it with setCompleteIndex.
     */
    public void resetCompleteIndex(){
        topicIndex.clearTopicCache();
        topicIndex.startCleanerThread();
        completeIndexes=false;
    }
    
    
    // ------------------------------------------------------------ TRACKING ---
    
    
    @Override
    public boolean trackingDependent(){
        return false;
    }
    
    
    @Override
    public void setTrackDependent(boolean v){
        // TODO: dependent times and tracking dependent
    }

    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------- TOPIC MAP CHANGED ---
    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean isTopicMapChanged(){
        return changed;
    }
    
    
    @Override
    public boolean resetTopicMapChanged(){
        boolean old=changed;
        changed=false;
        return old;
    }
    
    

    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------- SEARCH ---
    // -------------------------------------------------------------------------
    
    
   
    @Override
    public Collection<Topic> search(String query, TopicMapSearchOptions options) {
        if(unconnected) return new ArrayList<Topic>();
        ArrayList<Topic> searchResult = new ArrayList<Topic>();
        int MAX_SEARCH_RESULTS = 999;
        if(options.maxResults>=0 && options.maxResults<MAX_SEARCH_RESULTS) MAX_SEARCH_RESULTS=options.maxResults;        
        if(query == null || query.length()<4) {
            return searchResult;
        }
        
        String dbquery = "%" + escapeSQL(query) + "%";
        System.out.println("Search starts");
        String union="";
        // --- Basename ---
        if(options.searchBasenames) {
            if(union.length()>0) union+=" union ";
            union+=" select TOPIC.* from TOPIC where BASENAME like '"+dbquery+"'";
        }

        // --- Variant names ---
        if(options.searchVariants) {
            if(union.length()>0) union+=" union ";
            union+=" select TOPIC.* from TOPIC,VARIANT where TOPICID=VARIANT.TOPIC and VARIANT.VALUE like '"+dbquery+"'";
        }


        // --- text occurrences ---
        if(options.searchOccurrences) {
            if(union.length()>0) union+=" union ";
            union+=" select TOPIC.* from TOPIC,DATA where DATA.TOPIC=TOPICID and DATA.DATA like '"+dbquery+"'";
        }


        // --- locator ---
        if(options.searchSL) {
            if(union.length()>0) union+=" union ";
            union+=" select TOPIC.* from TOPIC where SUBJECTLOCATOR like '"+dbquery+"'";
        }


        // --- sis ---
        if(options.searchSIs) {
            if(union.length()>0) union+=" union ";
            union+=" select TOPIC.* from TOPIC,SUBJECTIDENTIFIER where TOPICID=SUBJECTIDENTIFIER.TOPIC and SUBJECTIDENTIFIER.SI like '"+dbquery+"'";
        }
        try{
            Collection<Topic> res=queryTopic(union);
            int counter=0;
            for(Topic t : res){
                if(t.getOneSubjectIdentifier() == null) {
                    System.out.println("Warning: Topic '"+t.getBaseName()+"' has no SI!");
                }
                else {
                    searchResult.add(t);
                    counter++;
                    if(counter>=MAX_SEARCH_RESULTS) break;
                }
            }
        }
        catch(Exception e){e.printStackTrace();}
        
        System.out.println("Search ends with " + searchResult.size() + " hits.");
        return searchResult;
    }
 
    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- STATISTICS ---
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public TopicMapStatData getStatistics(TopicMapStatOptions options) throws TopicMapException {
        if(options == null) return null;
        int option = options.getOption();
        int count = 0;
        switch(option) {
            case TopicMapStatOptions.NUMBER_OF_TOPICS: {
                return new TopicMapStatData(this.getNumTopics());
            }
            case TopicMapStatOptions.NUMBER_OF_TOPIC_CLASSES: {
                count = executeCountQuery("select count(DISTINCT TYPE) from TOPICTYPE");
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATIONS: {
                return new TopicMapStatData(this.getNumAssociations());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_PLAYERS: {
                count = executeCountQuery("select count(DISTINCT MEMBER.PLAYER) from MEMBER");
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_ROLES: {
                count = executeCountQuery("select count(DISTINCT MEMBER.ROLE) from MEMBER");
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_TYPES: {
                count = executeCountQuery("select count(DISTINCT ASSOCIATION.TYPE) from ASSOCIATION");
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_BASE_NAMES: {
                count = executeCountQuery("select count(*) from TOPIC where BASENAME is not null");
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_OCCURRENCES: {
                count = executeCountQuery("select count(*) from DATA");
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_IDENTIFIERS: {
                count = executeCountQuery("select count(*) from SUBJECTIDENTIFIER");
                return new TopicMapStatData(count);
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_LOCATORS: {
                count = executeCountQuery("select count(*) from TOPIC where SUBJECTLOCATOR is not null");
                return new TopicMapStatData(count);
            }
        }
        return new TopicMapStatData();
    }
    
    
    

    // -------------------------------------------------------------------------
    // --------------------------------------------------- TOPICMAP LISTENER ---
    // -------------------------------------------------------------------------
    
    
    
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
    
    /*    
    public TopicMapListener setTopicMapListener(TopicMapListener listener){
        TopicMapListener old=topicMapListener;
        topicMapListener=listener;
        return old;
    }
    */
    
    
    
    // ----------------------------------------- TOPICMAP LISTENER INTERFACE ---
    
    
    void topicRemoved(Topic t) throws TopicMapException {
        topicIndex.topicRemoved(t);
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicRemoved(t);        
        }
    }
    
    
    void associationRemoved(Association a) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.associationRemoved(a);        
        }
    }
    
    
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException{
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicSubjectIdentifierChanged(t,added,removed);
        }
    }
    
    
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException{
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicBaseNameChanged(t,newName,oldName);
        }
    }
    
    
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicTypeChanged(t,added,removed);
        }
    }
    
    
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicVariantChanged(t,scope,newName,oldName);
        }
    }
    
    
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicDataChanged(t,type,version,newValue,oldValue);
        }
    }
    
    
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicSubjectLocatorChanged(t,newLocator,oldLocator);
        }
    }
    
    
    public void topicChanged(Topic t) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.topicChanged(t);
        }
    }
    
    
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.associationTypeChanged(a,newType,oldType);        
        }
    }
    
    
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.associationPlayerChanged(a,role,newPlayer,oldPlayer);        
        }
    }
    
    
    public void associationChanged(Association a) throws TopicMapException{
        changed=true;
        for(TopicMapListener listener : topicMapListeners){
            listener.associationChanged(a);
        }
    }
    

    
    // -------------------------------------------------------------------------
    
    
    public void commit() throws SQLException, TopicMapException {
        super.commit();
    }
    
}
