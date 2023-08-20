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
 *
 *
 * 
 *
 */
package org.wandora.topicmap.database2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;




/**
 *
 * @author akivela
 */
public abstract class AbstractDatabaseTopicMap extends TopicMap {
    
    private int queryCounter=1;

    
    // connection info about current database
    private String dbDriver;
    private String dbConnectionString;
    private String dbUser;
    private String dbPassword;
    
    private Object connectionParams;
    
    protected boolean unconnected=false;
    
    
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
     * Note that this is different than topic map read only property. This property
     * tells the state of the connection: is the connection a read only connection
     * and does the user have privileges to modify the database. These can only
     * be changed by connecting to the database using different settings.
     */
    protected boolean isDBReadOnly = false;
    
    
    /**
     * The default stored connection
     */
    protected Connection connection=null;
    
    
    


    
    public AbstractDatabaseTopicMap(String dbDriver,String dbConnectionString, String dbUser, String dbPassword,String initScript,Object connectionParams) throws SQLException {
        this.dbDriver=dbDriver;
        this.dbConnectionString=dbConnectionString;
        this.dbUser=dbUser;
        this.dbPassword=dbPassword;
        this.connectionParams=connectionParams;

        if(dbConnectionString.startsWith("jdbc:mysql")) databaseFlavour="mysql";
        else databaseFlavour="generic";

        try {
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
    
    
    

    
    /**
     * Gets the used jdbc database driver class.
     */
    public String getDBDriver() {
        return dbDriver;
    }
    
    /**
     * Gets the used jdbc database connection string.
     */
    public String getDBConnectionString() {
        return dbConnectionString;
    }
    
    /**
     * Gets the used database user name.
     */
    public String getDBUser() {
        return dbUser;
    }
    
    
    /**
     * Gets the used database password.
     */
    public String getDBPassword() {
        return dbPassword;
    }
    
    

    
    /** A connection parameters object may be stored in the database topic map.
     * It is not used by the database topic map, only stored by it. Currently
     * this is used to store higher level connection information than the basic
     * driver, connection string, user name, password. This makes it possible
     * to modify the stored connection in Wandora. The connection parameters
     * object is set at the constructor.
     */
    public Object getConnectionParams() {
        return connectionParams;
    }
    
         
            
    public String getDatabaseFlavour() {
        return databaseFlavour;
    }
    
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------- READ ONLY ---
    // -------------------------------------------------------------------------
    
    
    

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
    protected boolean testReadOnly() {
        if(connection==null) return true;
        try{
            Connection con=connection;
            Statement stmt=con.createStatement();
            stmt.executeUpdate("UPDATE TOPIC set TOPICID='READONLYTEST' where TOPICID='READONLYTEST';");
            stmt.close();
            return false;
        } 
        catch(SQLException sqle){
//            sqle.printStackTrace();
            return true;
        }
    }

    
    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- CONNECTION ---
    // -------------------------------------------------------------------------
    
    
    /**
     * Checks if the database connection is active.  
     */
    @Override
    public boolean isConnected(){
        return !unconnected;
    }
    
    
    


    
    /**
     * Gets the connection used with database queries. If the old stored connection
     * has been closed for any reason, tries to create a new connection.
     */
    protected Connection getConnection() {
        synchronized(queryLock) {
            if(unconnected) {
                return null;
            }
            if(connection==null) {
                connection=createConnection(false);
                isDBReadOnly=testReadOnly();
            }
            if(connection==null) {
                unconnected=true; 
                return null;}
            try {
                if(connection.isClosed()) {
                    System.out.println("SQL connection closed. Opening new connection!");
                    connection=createConnection(false);
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
    protected Connection createConnection(boolean autocommit) {
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
    
    
    
    /**
     * Closes the topic map. This closes the database connection. 
     * Topic map cannot be used or reopened
     * after it has been closed.
     */
    @Override
    public void close() {
        log("at close of database topicmap******");
        if(connection!=null){
            try {
                log("Close database connection ******");
                connection.close();
            }
            catch(SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
    
    

    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------- EXECUTE QUERY ---
    // -------------------------------------------------------------------------
    
    

    private final Object queryLock = new Object();
    
    private QueryQueue updateQueue = new QueryQueue();
    
    
    
    public boolean executeUpdate(String query) throws TopicMapException{
        try {
            return updateQueue.queue(query);
        }
        catch(SQLException sqle) {
            throw new TopicMapSQLException(sqle);
        }
    }
    
    
    
    
    
    
    
    /*
    private boolean executeUpdate(String query, Connection con) throws TopicMapException {
        synchronized(queryLock) {
            queryCounter++;
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                logQuery(query);
                stmt.executeUpdate(query);
            }
            catch(SQLException sqle) {
                sqle.printStackTrace();
                throw new TopicMapException(sqle);
            }
            finally {
                if(stmt != null) {
                    try {
                        stmt.close();
                    } 
                    catch(SQLException sqle) {
                        throw new TopicMapException(sqle);
                    }
                }
            }
        }
        return true;
    }
    */
    
    
    /**
     * Executes a database query and returns the results as a collection. Each
     * element in the collection is a Map and represents a row of the result. 
     * The Map maps column names to the objects returned by query.
     */
    public Collection<Map<String,Object>> executeQuery(String query) throws TopicMapException {
        return executeQuery(query, getConnection());
    }
    
    
    private Collection<Map<String,Object>> executeQuery(String query, Connection con) throws TopicMapException {
        try {
            updateQueue.commit();
        }
        catch(SQLException sqle) {
            throw new TopicMapSQLException(sqle);
        }
        
        synchronized(queryLock) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                queryCounter++;
                logQuery(query);
                stmt=con.createStatement();
                rs=stmt.executeQuery(query);
                ResultSetMetaData metaData=rs.getMetaData();
                int columns=metaData.getColumnCount();
                String[] columnNames=new String[columns];
                for(int i=0;i<columns;i++) {
                    columnNames[i]=metaData.getColumnName(i+1);
                }

                ArrayList<Map<String,Object>> rows=new ArrayList<Map<String,Object>>();
                while(rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<String,Object>();
                    for(int i=0;i<columns;i++) {
                        // Column names are transformed to uppercase.
                        row.put(columnNames[i].toUpperCase(), rs.getObject(i+1));
                        //System.out.println("  "+columnNames[i]+"="+rs.getObject(i+1));
                    }
                    //System.out.println("---");
                    rows.add(row);
                }
                rs.close();
                stmt.close();
                return rows;
            }
            catch(SQLException sqle) {
                try {
                    if(rs != null) rs.close();
                } catch(Exception e) { e.printStackTrace(); }
                try {
                    if(stmt != null) stmt.close();
                } catch(Exception e) { e.printStackTrace(); }

                sqle.printStackTrace();
                throw new TopicMapSQLException(sqle);
            }
        }
    }
    
    
    
    
    protected int executeCountQuery(String query) {
        try {
            updateQueue.commit();
        }
        catch(TopicMapException | SQLException e) {
            e.printStackTrace();
        }
        int count = 0;
        
        synchronized(queryLock) {
            if(!unconnected) {
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    queryCounter++;
                    logQuery(query);
                    Connection con=getConnection();
                    stmt=con.createStatement();
                    rs=stmt.executeQuery(query);
                    rs.next();
                    count=rs.getInt(1);
                    rs.close();
                    stmt.close();
                    return count;
                }
                catch(SQLException sqle) {
                    sqle.printStackTrace();
                }
                finally {
                    if(rs != null) {
                        try {
                            rs.close();
                        } catch(Exception e) { e.printStackTrace(); }
                    }
                    if(stmt != null) {
                        try {
                            stmt.close();
                        } catch(Exception e) { e.printStackTrace(); }
                    }
                }
            }
        }
        return count;
    }
    
    
    
    
    
    private void logQuery(String query) {
        if(query != null) {
            System.out.println(query.substring(0, query.length() > 512 ? 512 : query.length()));
        }
    }
    
    
    

    public Iterator<Map<String,Object>> getRowIterator(final String query, final String orderby) {

        if(unconnected) {
            return new Iterator<Map<String,Object>>() {
                @Override
                public boolean hasNext() {
                    return false;
                } 
                @Override
                public Map<String,Object> next() {
                    throw new NoSuchElementException();
                } 
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        else {

            return new Iterator<Map<String,Object>>() {
                boolean hasNext = false;
                int offset = 0;
                int limit = 1000;
                List<Map<String,Object>> rows = null;
                int rowIndex = 0;

                private void prepare() {
                    try {
                        String newQuery = query +" order by "+orderby+ " limit "+limit+" offset "+offset;
                        rows = new ArrayList<>(executeQuery(newQuery));
                        offset += limit;
                        rowIndex = 0;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        hasNext = false;
                    }
                }

                @Override
                public boolean hasNext() {
                    if(rows == null || rowIndex >= rows.size()) {
                        prepare();
                    }

                    if(rows != null && rowIndex < rows.size()) {
                        return true;
                    }
                    return false;
                }

                @Override
                public Map<String,Object> next() {
                    if(!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Map<String,Object> row = rows.get(rowIndex++);
                    return row;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    
    
    
    
    
    // -------------------------------------------------------------------------

    
    public void commit() throws SQLException, TopicMapException {
        updateQueue.commit();
    }
    
    
    
    // -------------------------------------------------------------------------
    

    
    /**
     * Turns a collection of strings into sql syntax representing a collection of strings
     * that can be used with 'in' clauses. The returned string contains each string
     * in the collection escaped inside single quotes, each separated by commas
     * and all contain inside parenthesis. For example: ('string1','string2','string3').
     */
    protected String collectionToSQL(Collection<String> col){
        StringBuilder sb=new StringBuilder("(");
        for(String s : col){
            if(sb.length()>1) sb.append(",");
            sb.append("'").append(escapeSQL(s)).append("'");
        }
        sb.append(")");
        return sb.toString();
    }
    
    
    
    
    /**
     * Escapes a string so that it can be used in an sql query.
     */
    protected String escapeSQL(String s) {
        if(s != null) {
            s=s.replace("'","''");
            if(databaseFlavour.equals("mysql")) {
                s=s.replace("\\","\\\\");
            }
        }
        return s;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    class QueryQueue {
        
        private final List<String> queryQueue = Collections.synchronizedList(new ArrayList<String>());
        private int autoCommitQueueSize = 50;
        private int maxTries = 10;
        
        
        
        public boolean queue(String query) throws SQLException, TopicMapException {
            boolean autoCommit = false;
            synchronized(queryQueue) {
                queryQueue.add(query);
                autoCommit = (queryQueue.size() > autoCommitQueueSize);
            }
            if(autoCommit) {
                commit();
            }
            return true;
        }
        
        
        public void commit() throws SQLException, TopicMapException {
            synchronized(queryQueue) {
                if(!queryQueue.isEmpty()) {
                    Connection connection = getConnection();
                    int tries = 0;
                    while(!queryQueue.isEmpty() && ++tries < maxTries) {
                        try {
                            for(String query : queryQueue) {
                                synchronized(queryLock) {
                                    queryCounter++;
                                    Statement stmt = connection.createStatement();
                                    logQuery(query);
                                    stmt.executeUpdate(query);
                                    stmt.close();
                                }
                            }
                            connection.commit();
                            queryQueue.clear();
                        }
                        catch(SQLException sqle) {
                            sqle.printStackTrace();
                            connection.rollback();
                            try {
                                Thread.sleep(100);
                            } 
                            catch (InterruptedException ex) {
                                // OK. Wakeup.
                            }
                        }
                    }
                    if(tries >= maxTries) {
                        throw new TopicMapException(new SQLException());
                    }
                }
            }
        }

    }
    
    
}
