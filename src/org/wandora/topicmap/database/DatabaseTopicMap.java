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

package org.wandora.topicmap.database;
import org.wandora.utils.sqlproxy.SQLProxyClient;
import org.wandora.topicmap.*;
import static org.wandora.utils.Tuples.*;
import java.util.*;
import java.sql.*;

import org.wandora.utils.sqlproxy.*;

/**
 *
 * @author olli
 */
public class DatabaseTopicMap extends TopicMap {
    
    protected boolean changed;
    
    /** The WeakTopicIndex used to index topics. Note that index needs information
     * about changes in topic map and should also be used to construct new
     * topics to make sure that there are not two different instances of the same topic.
     * @see WeakTopicIndex
     */
    protected WeakTopicIndex topicIndex;
    
    protected ArrayList<TopicMapListener> topicMapListeners;
    protected ArrayList<TopicMapListener> disabledListeners;
    
    // connection info about current database
    protected String dbDriver;
    protected String dbConnectionString;
    protected String dbUser;
    protected String dbPassword;
    
    protected SQLProxyClient sqlProxy;
    
    /**
     * <p>
     * The database flavor. Some operations need to be handled differently
     * with different database vendors. This field is used to store what
     * kind of database is being used. Currently it may have following values
     * </p>
     * <pre>
     *  "mysql" - MySQL database
     *  "generic" - Any other database presumed to be sufficiently standard compliant
     * </pre>
     * <p>
     * It is set automatically based on the connection string used.
     * </p>
     */
    protected String databaseFlavour;
    /**
     * Gets the used jdbc database driver class.
     */
    public String getDBDriver(){return dbDriver;}
    /**
     * Gets the used jdbc database connection string.
     */
    public String getDBConnectionString(){return dbConnectionString;}
    /**
     * Gets the used database user name.
     */
    public String getDBUser(){return dbUser;}
    /**
     * Gets the used database password.
     */
    public String getDBPassword(){return dbPassword;}
    
    protected Object connectionParams;
    /** A connection parameters object may be stored in the database topic map.
     * It is not used by the database topic map, only stored by it. Currently
     * this is used to store higher level connection information than the basic
     * driver, connection string, user name, password. This makes it possible
     * to modify the stored connection in Wandora. The connection parameters
     * object is set at the constructor.
     */
    public Object getConnectionParams(){return connectionParams;}
    
    //protected boolean consistencyCheck=true;
    
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
    
    protected boolean unconnected=false;
    
    /**
     * Note that this is different than topic map read only property. This property
     * tells the state of the connection: is the connection a read only connection
     * and does the user have privileges to modify the database. These can only
     * be changed by connecting to the database using different settings.
     */
    protected boolean isDBReadOnly = false;
    
    protected Object indexLock=new Object();
//    protected LinkedHashMap<Locator,Topic> siIndex;
//    protected LinkedHashMap<String,Topic> bnIndex;
    
    
    /** Creates a new instance of DatabaseTopicMap */
    public DatabaseTopicMap(String dbDriver,String dbConnectionString, String dbUser, String dbPassword) throws SQLException {
        this(dbDriver,dbConnectionString,dbUser,dbPassword,null);
    }
    
    
    public DatabaseTopicMap(String dbDriver,String dbConnectionString, String dbUser, String dbPassword,Object connectionParams) throws SQLException {
        this(dbDriver,dbConnectionString,dbUser,dbPassword,null,connectionParams);
    }
    
    
    public DatabaseTopicMap(String dbDriver,String dbConnectionString, String dbUser, String dbPassword,String initScript) throws SQLException {
        this(dbDriver,dbConnectionString,dbUser,dbPassword,initScript,null);
    }
    
    
    public DatabaseTopicMap(String dbDriver,String dbConnectionString, String dbUser, String dbPassword,String initScript,Object connectionParams) throws SQLException {
        this.dbDriver=dbDriver;
        this.dbConnectionString=dbConnectionString;
        this.dbUser=dbUser;
        this.dbPassword=dbPassword;
        this.connectionParams=connectionParams;
        
        topicMapListeners=new ArrayList<TopicMapListener>();
        
        if(dbConnectionString.startsWith("sqlproxy:")){
            try{
                sqlProxy=SQLProxyClient.createProxy(dbConnectionString,dbUser,dbPassword);
                databaseFlavour=sqlProxy.getFlavour();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        else {
            if(dbConnectionString.startsWith("jdbc:mysql")) databaseFlavour="mysql";
            else databaseFlavour="generic";
        }
        
        if(sqlProxy==null){
            try{
                getConnection();

                if(initScript!=null && initScript.trim().length()>0 && !unconnected){
                    Statement stmt=getConnection().createStatement();
                    stmt.execute(initScript);
                    isDBReadOnly=testReadOnly(); // readOnly status may change because of init script
                }
            }
            catch(SQLException sqle){
                sqle.printStackTrace();
                unconnected=true;
            }
        }

//        siIndex=new LinkedHashMap<Locator,Topic>();
//        bnIndex=new LinkedHashMap<String,Topic>();
        
        topicIndex=new WeakTopicIndex();
        changed=false;
    }
    
    
    String getDatabaseFlavour() {
        return databaseFlavour;
    }
    
    
    /**
     * Does the topic map only allow reading or both reading and writing.
     * Note that this is different than topic map read only property. This property
     * tells the state of the connection: is the connection a read only connection
     * and does the user have privileges to modify the database. These can only
     * be changed by connecting to the database using different settings.
     */
    @Override
    public boolean isReadOnly() {
        return isDBReadOnly && isReadOnly;
    }
    
    
    /**
     * Tests if the connection allows modifying the topic map. The test is done
     * using an update sql statement that does not change anything in the database
     * but should raise an exception if modifying is not allowed. Note that this
     * only tests modifying the topic table and most database implementations
     * allow specifying different privileges for each tables.
     */
    public boolean testReadOnly() {
        if(connection==null) return true;
        try{
            Connection con=connection;
            Statement stmt=con.createStatement();;
            stmt.executeUpdate("UPDATE TOPIC set TOPICID='READONLYTEST' where TOPICID='READONLYTEST';");
            stmt.close();
            return false;
        }catch(SQLException sqle){
//            sqle.printStackTrace();
            return true;
        }
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
    
    
    /**
     * Checks if the database connection is active.  
     */
    @Override
    public boolean isConnected(){
        return !unconnected;
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
        Collection<Map<String,Object>> res=executeQuery(query);
        String topicID = null;
        String defaultSI = null;
        logger.log("Found total "+res.size()+" topics without SIs!");
        for(Map<String,Object> row : res) {
            topicID=row.get("TOPICID").toString();
            defaultSI=TopicTools.createDefaultLocator().toExternalForm();
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
            HashSet<Vector<String>> associations=new LinkedHashSet<Vector<String>>(500);
            Collection<Map<String,Object>> res2=executeQuery(
                    "select ASSOCIATION.*,M2.* from ASSOCIATION,MEMBER as M1, MEMBER as M2 "+
                    "where M1.PLAYER='"+escapeSQL(player)+"' and M1.ASSOCIATION=ASSOCIATIONID and "+
                    "M2.ASSOCIATION=ASSOCIATION.ASSOCIATIONID order by ASSOCIATION.ASSOCIATIONID,M2.ROLE"
                    );
            String associationid="";
            Vector<String> v=new Vector<String>(9);
            for(Map<String,Object> row2 : res2){
                String id=row2.get("ASSOCIATIONID").toString();
                if(!associationid.equals(id)){
                    if(!associationid.equals("")){
                        if(!associations.add(v)){
                            logger.hlog("Deleting association with id "+associationid);
                            executeUpdate("delete from MEMBER where ASSOCIATION='"+escapeSQL(associationid)+"'");
                            executeUpdate("delete from ASSOCIATION where ASSOCIATIONID='"+escapeSQL(associationid)+"'");
                            deleted++;
                        }
                        v=new Vector<String>(9);
                        String type=row2.get("TYPE").toString();
                        v.add(type);
                    }
                    associationid=id;
                }
                String r=row2.get("ROLE").toString();
                String p=row2.get("PLAYER").toString();
                v.add(r); v.add(p);
            }
            if(!associations.add(v)){
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
        if(sqlProxy!=null){
            try{
                sqlProxy.close();
            }catch(java.io.IOException ioe){ioe.printStackTrace();}
        }
        else if(connection!=null){
            try{
                connection.close();
            }catch(SQLException sqle){sqle.printStackTrace();}
        }
    }
    
    
    // the default stored connection
    protected Connection connection=null;
    
    
    /**
     * Gets the connection used with database queries. If the old stored connection
     * has been closed for any reason, tries to create a new connection.
     */
    public Connection getConnection(){
        if(sqlProxy!=null) {
            return null;
        }
        synchronized(queryLock){
            if(unconnected) {
                return null;
            }
            if(connection==null) {
                connection=createConnection(true);
                isDBReadOnly=testReadOnly();
            }
            if(connection==null) {
                unconnected=true; 
                return null;}
            try {
                if(connection.isClosed()) {
                    System.out.println("SQL connection closed. Opening new connection!");
                    connection=createConnection(true);
                    isDBReadOnly=testReadOnly();
                }
            }
            catch (SQLException sqle) {
                System.out.println("SQL exception occurred while acquiring connection:");
                sqle.printStackTrace();
                System.out.println("Trying to open new connection!");
                connection=createConnection(true);
                isDBReadOnly=testReadOnly();
            }
            return connection;
        }
    }
    
    
    /**
     * Creates a new database connection using the connection parameters given
     * to the constructor.
     */
    public Connection createConnection(boolean autocommit) {
        if(sqlProxy!=null) return null;
        try {
            Class.forName(dbDriver);
            Connection con = DriverManager.getConnection(dbConnectionString,dbUser,dbPassword);        
            if(autocommit) {
                con.setAutoCommit(true);
            }
            else {
                con.setAutoCommit(false);
            }
            // System.out.println("Database connection created");
            return con;
        }
        catch(Exception e){
            System.out.println("Database connection failed with");
            System.out.println("Driver: " + dbDriver);
            System.out.println("Connection string: " + dbConnectionString);
            System.out.println("User: " + dbUser);
            System.out.println("Password: " + dbPassword);
            e.printStackTrace();
            return null;
        }
    }
    
    
    private int queryCounter=1;

    
    /**
     * Turns a collection of strings into sql syntax representing a collection of strings
     * that can be used with 'in' clauses. The returned string contains each string
     * in the collection escaped inside single quotes, each separated by commas
     * and all contain inside parenthesis. For example: ('string1','string2','string3').
     */
    public String collectionToSQL(Collection<String> col){
        StringBuilder sb=new StringBuilder("(");
        for(String s : col){
            if(sb.length()>1) sb.append(",");
            sb.append("'").append(escapeSQL(s)).append("'");
        }
        sb.append(")");
        return sb.toString();
    }
    
    
    // ------------------------------------------------------- EXECUTE QUERY ---
    
    
    
    // A stored statement that can be reused instead of always creating new statement objects
    // private Statement storedStatement=null;
    private final Object queryLock = new Object();
    
    
    public boolean executeUpdate(String query)  throws TopicMapException{
        return executeUpdate(query, getConnection());
    }
    
    
    public boolean executeUpdate(String query, Connection con) throws TopicMapException {
        synchronized(queryLock) {
            queryCounter++;
    //        /*if((queryCounter%10)==0)*/ System.out.println("DBG execute update "+queryCounter+" "+query);
            if(sqlProxy!=null) {
                try{
                    sqlProxy.executeUpdate(query);
                    return true;
                }
                catch(Exception e) {
                    e.printStackTrace(); 
                    throw new TopicMapSQLException(e);
                }
            }
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                System.out.println(query);
                stmt.executeUpdate(query);
                stmt.close();
                return true;
            }
            catch(SQLException sqle) {
                if(stmt != null) {
                    try {
                        stmt.close();
                        con.close();
                    } catch(Exception e) { 
                        e.printStackTrace(); 
                    }
                }
                sqle.printStackTrace(); 
                throw new TopicMapSQLException(sqle);
            }
        }
    }
    
    
    /**
     * Executes a database query and returns the results as a collection. Each
     * element in the collection is a Map and represents a row of the result. 
     * The Map maps column names to the objects returned by query.
     */
    public Collection<Map<String,Object>> executeQuery(String query) throws TopicMapException {
        return executeQuery(query, getConnection());
    }
    
    
    public Collection<Map<String,Object>> executeQuery(String query, Connection con) throws TopicMapException {
        synchronized(queryLock) {
            try{
                queryCounter++;
                if(sqlProxy!=null) {
                    try {
                        return sqlProxy.executeQuery(query);
                    }
                    catch(Exception e) {
                        e.printStackTrace(); 
                        throw new TopicMapSQLException(e);
                    }
                }
                
    //            /*if((queryCounter%10)==0)*/ System.out.println("DBG execute query "+queryCounter+" "+query);
                Statement stmt=con.createStatement();
                ResultSet rs=stmt.executeQuery(query);
                ResultSetMetaData metaData=rs.getMetaData();
                int columns=metaData.getColumnCount();
                String[] columnNames=new String[columns];
                for(int i=0;i<columns;i++) {
                    columnNames[i]=metaData.getColumnName(i+1);
                }

                ArrayList<Map<String,Object>> rows=new ArrayList<Map<String,Object>>();
                while(rs.next()) {
                    Map<String,Object> row=new LinkedHashMap<String,Object>();
                    for(int i=0;i<columns;i++) {
                        row.put(columnNames[i],rs.getObject(i+1));
                    }
                    rows.add(row);
                }
                rs.close();
                stmt.close();
                return rows;
            }
            catch(SQLException sqle) {
                sqle.printStackTrace();
                throw new TopicMapSQLException(sqle);
            }
        }
    }
    
    
    // --------------------------------------------------------- BUILD TOPIC ---
    
    /**
     * Builds a database topic from a database query result row. The row should contain
     * (at least) three columns with names "TOPICID", "BASENAME" and "SUBJECTLOCATOR"
     * that are used to build the topic. 
     */
    public DatabaseTopic buildTopic(Map<String,Object> row) throws TopicMapException {
        return buildTopic(row.get("TOPICID"),row.get("BASENAME"),row.get("SUBJECTLOCATOR"));
    }
    
    
    /**
     * Builds a database topic when given the topic id, basename and subject locator.
     */
    public DatabaseTopic buildTopic(Object id, Object baseName, Object subjectLocator) throws TopicMapException {
        return buildTopic((String) id, (String) baseName, (String) subjectLocator);
    }
    
    
    /**
     * Builds a database topic when given the topic id, basename and subject locator.
     */
    public DatabaseTopic buildTopic(String id, String baseName, String subjectLocator) throws TopicMapException {
        DatabaseTopic dbt=topicIndex.getTopicWithID(id);
        if(dbt==null) {
            dbt=topicIndex.createTopic(id, this);
            dbt.initialize(baseName, subjectLocator);
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
        return buildAssociation(row.get("ASSOCIATIONID"),row.get("TOPICID"),row.get("BASENAME"),row.get("SUBJECTLOCATOR"));
    }
    
    
    public DatabaseAssociation buildAssociation(Object associationId,Object typeId,Object typeName,Object typeSL) throws TopicMapException {
        return buildAssociation((String)associationId,(String)typeId,(String)typeName,(String)typeSL);
    }
    
    
    public DatabaseAssociation buildAssociation(String associationId,String typeId,String typeName,String typeSL) throws TopicMapException {
        DatabaseAssociation dba=topicIndex.getAssociation(associationId,this);
        if(dba!=null) return dba;
        DatabaseTopic type=buildTopic(typeId,typeName,typeSL);
        return buildAssociation(associationId,type);
    }
    
    
    public DatabaseAssociation buildAssociation(String associationId,DatabaseTopic type){
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
        return queryTopic(query,getConnection());
    }
    
    
    public Collection<Topic> queryTopic(String query,Connection con)  throws TopicMapException {
        Collection<Map<String,Object>> res=executeQuery(query,con);
        Collection<Topic> ret=new ArrayList<Topic>();
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
        return queryAssociation(query,getConnection());
    }
    
    
    public Collection<Association> queryAssociation(String query,Connection con) throws TopicMapException {
        Collection<Map<String,Object>> res=executeQuery(query,con);
        Vector<Association> ret=new Vector<Association>();
        for(Map<String,Object>row : res){
            ret.add(buildAssociation(row));
        }
        return ret;
    }
    
    
    /**
     * Escapes a string so that it can be used in an sql query.
     */
    public String escapeSQL(String s){
        s=s.replace("'","''");
        if(databaseFlavour.equals("mysql")) s=s.replace("\\","\\\\");
        return s;
    }
    
    
    public Topic getTopic(Collection<Locator> SIs) throws TopicMapException {
        for(Locator l : SIs){
            Topic t=getTopic(l);
            if(t!=null) return t;
        }
        return null;
    }
    
    
/*    
    protected void clearSiQueue(){
        synchronized(indexLock){
            long t=System.currentTimeMillis()-10000;
            while(!siQueue.isEmpty()){
                T2<Long,Locator> first=siQueue.getFirst();
                if(first.e1<t){
                    siIndex.remove(first.e2);
                    siQueue.removeFirst();
                }
                else break;
            }
        }
    }
    protected void clearBnQueue(){
        synchronized(indexLock){
            long t=System.currentTimeMillis()-10000;
            while(!bnQueue.isEmpty()){
                T2<Long,String> first=bnQueue.getFirst();
                if(first.e1<t){
                    bnIndex.remove(first.e2);
                    bnQueue.removeFirst();
                }
                else break;
            }
        }
    }*/
    
/*    protected void resetIndexes(){
        synchronized(indexLock){
            if(!siIndex.isEmpty()){
                siIndex=new LinkedHashMap<Locator,Topic>();
            }
            if(!bnIndex.isEmpty()){
                bnIndex=new LinkedHashMap<String,Topic>();
            }
        }
    }*/
/*    
    void addTopicToSIIndex(Topic t){
        synchronized(indexLock){
            long time=System.currentTimeMillis();
            for(Locator l : t.getSubjectIdentifiers()){
                siIndex.put(l,t);
            }
        }
    }
    void addTopicToBNIndex(Topic t){
        synchronized(indexLock){
            long time=System.currentTimeMillis();
            String bn=t.getBaseName();
            if(bn==null || bn.length()==0) return;;
            bnIndex.put(bn,t);
        }
    }*/
    
    
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
        DatabaseTopic t=topicIndex.newTopic(id, this);
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
        if(unconnected) {
            return new TopicIterator(){
                @Override
                public boolean hasNext(){return false;} 
                @Override
                public Topic next(){throw new NoSuchElementException();} 
                @Override
                public void remove(){throw new UnsupportedOperationException();}
                @Override
                public void dispose(){}
            };
        }
        
        try {
            if(sqlProxy!=null) {
                throw new UnsupportedOperationException();
            }
            
            final Connection con=createConnection(true);
            final Statement stmt=con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            if(databaseFlavour.equals("mysql")) {
                // A signal to mysql not to fetch all rows but stream them instead http://dev.mysql.com/doc/connector/j/en/cj-implementation-notes.html
                stmt.setFetchSize(Integer.MIN_VALUE);
            }
            final ResultSet rs=stmt.executeQuery("select * from TOPIC");
            ResultSetMetaData metaData=rs.getMetaData();
            final int columns=metaData.getColumnCount();
            final String[] columnNames=new String[columns];
            for(int i=0;i<columns;i++){
                columnNames[i]=metaData.getColumnName(i+1);
            }

            // NOTE: You must iterate through all rows because statement and result set
            //       will only be closed after last row has been fetched. Or alternatively
            //       call dispose when you're done with the iterator.
            return new TopicIterator() {
                boolean calledNext=false;
                boolean hasNext=false;
                boolean end=false;
                boolean disposed=false;
                
                @Override
                public void dispose() {
                    if(disposed) return;
                    disposed=true;
                    end=true;
                    try {
                        rs.close();
                        stmt.close();
                        con.close();
                    }
                    catch(SQLException sqle) {
                        sqle.printStackTrace();
                    }
                }

                @Override
                public boolean hasNext() {
                    if(end) return false;
                    if(calledNext) return hasNext;
                    else {
                        try {
                            hasNext=rs.next();
                            calledNext=true;
                            if(hasNext==false){
                                dispose();
                            }
                            return hasNext;
                        } 
                        catch(SQLException sqle) {
                            sqle.printStackTrace();
                            return false;
                        }
                    }
                }
                
                @Override
                public Topic next() {
                    if(!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Map<String,Object> row=new LinkedHashMap<String,Object>();
                    try {
                        for(int i=0;i<columns;i++) {
                            row.put(columnNames[i],rs.getObject(i+1));
                        }
                    }
                    catch(SQLException sqle) {
                        sqle.printStackTrace();
                        return null;
                    }
                    calledNext=false;
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
                    throw new UnsupportedOperationException();
                }
            };
        }
        catch(SQLException sqle){
            sqle.printStackTrace(); 
            return null;
        }
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
    public Iterator<Association> getAssociations(){
        if(unconnected) {
            return new Iterator<Association>(){
                @Override
                public boolean hasNext(){
                    return false;
                } 
                @Override
                public Association next(){
                    throw new NoSuchElementException();
                } 
                @Override
                public void remove(){
                    throw new UnsupportedOperationException();
                }
            };
        }     
        try{
            if(sqlProxy!=null) throw new UnsupportedOperationException();
            
            final Connection con=createConnection(true);
            final Statement stmt=con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            if(databaseFlavour.equals("mysql")) stmt.setFetchSize(Integer.MIN_VALUE);
            final ResultSet rs=stmt.executeQuery("select * from ASSOCIATION,TOPIC where TOPICID=TYPE");
            ResultSetMetaData metaData=rs.getMetaData();
            final int columns=metaData.getColumnCount();
            final String[] columnNames=new String[columns];
            for(int i=0;i<columns;i++){
                columnNames[i]=metaData.getColumnName(i+1);
            }

            return new Iterator<Association>(){
                boolean calledNext=false;
                boolean hasNext=false;
                boolean end=false;

                @Override
                public boolean hasNext(){
                    if(end) return false;
                    if(calledNext) return hasNext;
                    else {
                        try{
                            hasNext=rs.next();
                            calledNext=true;
                            if(hasNext==false){
                                rs.close();
                                stmt.close();
                                con.close();
                                end=true;
                            }
                            return hasNext;
                        }catch(SQLException sqle){sqle.printStackTrace();return false;}
                    }
                }
                
                @Override
                public Association next(){
                    if(!hasNext()) throw new NoSuchElementException();
                    HashMap<String,Object> row=new LinkedHashMap<String,Object>();
                    try{
                        for(int i=0;i<columns;i++){
                            row.put(columnNames[i],rs.getObject(i+1));
                        }
                    }catch(SQLException sqle){sqle.printStackTrace();return null;}
                    calledNext=false;
                    try{
                        return buildAssociation(row);
                    }catch(TopicMapException tme){
                        tme.printStackTrace(); // TODO EXCEPTION
                        return null;
                    }
                }
                
                @Override
                public void remove(){
                    throw new UnsupportedOperationException();
                }
            };
        }
        catch(SQLException sqle){
            sqle.printStackTrace(); 
            return null;
        }        
    }
    
    
    @Override
    public Collection<Association> getAssociationsOfType(Topic type) throws TopicMapException {
        if(unconnected) return new Vector<Association>();
        return queryAssociation("select * "
                + "from ASSOCIATION,TOPIC "
                + "where TYPE=TOPICID "
                + "and TOPICID='"+escapeSQL(type.getID())+"'");
    }
    
    
    @Override
    public int getNumTopics(){
        if(unconnected) return 0;
        if(sqlProxy!=null){
            try{
                Collection<Map<String,Object>> res=sqlProxy.executeQuery("select count(*) from TOPIC");
                Map<String,Object> row=res.iterator().next();
                return ((Number)row.get(row.keySet().iterator().next())).intValue();
            }
            catch(Exception e){
                e.printStackTrace(); 
                return 0;
            }
        }        
        try{
            Connection con=getConnection();
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select count(*) from TOPIC");
            rs.next();
            int count=rs.getInt(1);
            rs.close();
            stmt.close();
            return count;
        }
        catch(SQLException sqle){
            sqle.printStackTrace(); 
            return 0;
        }        
    }
    
    
    @Override
    public int getNumAssociations() {
        if(unconnected) return 0;
        if(sqlProxy!=null) {
            try {
                Collection<Map<String,Object>> res=sqlProxy.executeQuery("select count(*) from ASSOCIATION");
                Map<String,Object> row=res.iterator().next();
                return ((Number)row.get(row.keySet().iterator().next())).intValue();
            }
            catch(Exception e) {
                e.printStackTrace(); 
                return 0;
            }
        }        
        try {
            Connection con=getConnection();
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select count(*) from ASSOCIATION");
            rs.next();
            int count=rs.getInt(1);
            rs.close();
            stmt.close();
            return count;        
        }
        catch(SQLException sqle) {
            sqle.printStackTrace(); 
            return 0;
        }        
    }
    
    
    // ---------------------------------------- COPY TOPICIN / ASSOCIATIONIN ---
    
    
    private Topic _copyTopicIn(Topic t,boolean deep,Hashtable copied) throws TopicMapException {
        return _copyTopicIn(t,deep,false,copied);
    }
    
    
    private Topic _copyTopicIn(Topic t,boolean deep,boolean stub,Hashtable<Topic,Locator> copied) throws TopicMapException {
        
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
        Association n=_copyAssociationIn(a);
        Topic minTopic=null;
        int minCount=Integer.MAX_VALUE;
        for(Topic role : n.getRoles()) {
            Topic t=n.getPlayer(role);
            if(t.getAssociations().size()<minCount){
                minCount=t.getAssociations().size();
                minTopic=t;
            }
        }
        ((DatabaseTopic)minTopic).removeDuplicateAssociations();        
        return n;
    }
    
    
    @Override
    public Topic copyTopicIn(Topic t, boolean deep)  throws TopicMapException {
        if(unconnected) return null;
        if(isReadOnly()) throw new TopicMapReadOnlyException();
        return _copyTopicIn(t,deep,false,new Hashtable());
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
    
    
    
    // ------------------------------------------------------ IMPORT / MERGE ---
    
    
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
        else System.out.println("Merging to non-empty topic map (numTopcis="+numTopics+")");

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
        else System.out.println("Merging to non-empty topic map (numTopcis="+numTopics+")");
        
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
    
   
    @Override
    public void mergeIn(TopicMap tm,TopicMapLogger tmLogger) throws TopicMapException {
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
        Hashtable copied=new Hashtable();
        {
            // note that in some topic map implementations (DatabaseTopicMap specifically)
            // you must always iterate through all topics to close the result set thus
            // don't break the while loop even after forceStop
            Iterator<Topic> iter=tm.getTopics();
            while(iter.hasNext()){
                Topic t = null;
                try {
                    t=iter.next();
                    if(!tmLogger.forceStop()) _copyTopicIn(t,true,false,copied);
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
    
    
    
    // --------------------------------------------------------------- INDEX ---
    

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

    
    // --------------------------------------------------- TOPICMAP LISTENER ---
    
    
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
    
    
    // -------------------------------------------------------------- SEARCH ---
    
   
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
 
    
    // ---------------------------------------------------------- STATISTICS ---
    
    
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
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(DISTINCT TYPE) from TOPICTYPE");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATIONS: {
                return new TopicMapStatData(this.getNumAssociations());
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_PLAYERS: {
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(DISTINCT MEMBER.PLAYER) from MEMBER");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_ROLES: {
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(DISTINCT MEMBER.ROLE) from MEMBER");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
            case TopicMapStatOptions.NUMBER_OF_ASSOCIATION_TYPES: {
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(DISTINCT ASSOCIATION.TYPE) from ASSOCIATION");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
            case TopicMapStatOptions.NUMBER_OF_BASE_NAMES: {
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(*) from TOPIC where BASENAME is not null");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
            case TopicMapStatOptions.NUMBER_OF_OCCURRENCES: {
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(*) from DATA");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_IDENTIFIERS: {
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(*) from SUBJECTIDENTIFIER");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
            case TopicMapStatOptions.NUMBER_OF_SUBJECT_LOCATORS: {
                if(unconnected) break;
                try{
                    count = executeCountQuery("select count(*) from TOPIC where SUBJECTLOCATOR is not null");
                    return new TopicMapStatData(count);
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                    break;
                }
            }
        }
        return new TopicMapStatData();
    }
    
    
    
    
    private int executeCountQuery(String query) throws SQLException {
        if(sqlProxy!=null){
            try{
                Collection<Map<String,Object>> res=sqlProxy.executeQuery(query);
                Map<String,Object> row=res.iterator().next();
                return ((Number)row.get(row.keySet().iterator().next())).intValue();
            }catch(Exception e){
                throw new SQLException("Exception querying sqlproxy: "+e.getMessage());
            }
        }        
        Connection con=getConnection();
        Statement stmt=con.createStatement();
        ResultSet rs=stmt.executeQuery(query);
        rs.next();
        int count=rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }
    
}
