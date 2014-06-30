/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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
package org.wandora.modules;

import org.wandora.modules.Module;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * An interface for a module that provides database services. An implementation
 * using JDBC is in GenericDatabaseInterface.
 * 
 * @author olli
 */
public interface DatabaseInterface extends Module {

    /**
     * Executes a database query and returns the results.
     * @param query The query to execute.
     * @return The results of the query as a Rows object (essentially an ArrayList).
     * @throws SQLException 
     */
    public Rows query(String query) throws SQLException ;
    /**
     * Executes an update statement.
     * @param query The update statement to execute.
     * @return The number of rows affected by the statement.
     * @throws SQLException 
     */
    public int update(String query) throws SQLException ;
    /**
     * Inserts a row with an auto increment field and returns the 
     * automatically assigned value.
     * @param query The insert statement.
     * @return The automatically assigned incremented value.
     * @throws SQLException 
     */
    public Object insertAutoIncrement(String query) throws SQLException;
    /**
     * Escapes a string so that it can then be safely used as part of a
     * query.
     * @param str The string to be escaped.
     * @return The escaped string.
     */
    public String sqlEscape(String str);
    /**
     * Escapes a string so that it can then be safely used as part of a
     * query with a maximum length of the returned string. If the input
     * value is longer than the maximum length, it will be truncated to fit the
     * length limit.
     * 
     * @param str The string to be escaped.
     * @param length The maximum length.
     * @return The escaped and possibly truncated string.
     */
    public String sqlEscapeLen(String str,int length);
    /**
     * Makes a time stamp value out of a milliseconds time.
     * @param time The time in milliseconds.
     * @return The time stamp as a string.
     */
    public String formatTimestamp(long time);
    
    /**
     * Prepares a statement for later execution.
     * @param query The statement to prepare.
     * @return A PreparedDatabaseStatement object that can be ran later.
     * @throws SQLException 
     */
    public PreparedDatabaseStatement prepareStatement(String query) throws SQLException ;
    
/*    public PreparedStatement prepareStatement(String query) throws SQLException ;
    public Rows queryPrepared(PreparedStatement stmt) throws SQLException ;
    public int updatePrepared(PreparedStatement stmt) throws SQLException ;
    public void runDatabaseRunnable(DatabaseRunnable run) throws SQLException ;*/

    /**
     * A simple helper class for returned rows, essentially an ArrayList of Row
     * objects.
     */
    public static class Rows extends ArrayList<Row> {
        public Rows(){
            super();
        }
    }

    /**
     * A simple helper class for a single returned row, essentially just a HashMap.
     */
    public static class Row extends HashMap<String,Object>{
        public Row(){
            super();
        }
    }

//    public void reconnect();
    
    /**
     * Adds a database connection listener that will be notified when
     * a database connection is established or closed.
     * @param l 
     */
    public void addDatabaseConnectionListener(DatabaseConnectionListener l);
    /**
     * Removes a database connection listener.
     * @param l 
     */
    public void removeDatabaseConnectionListener(DatabaseConnectionListener l);

    public static interface DatabaseConnectionListener {
        public void connectionClosed(DatabaseInterface db);
        public void connectionStarted(DatabaseInterface db);
    }
/*
    public static interface DatabaseRunnable {
        public void run() throws SQLException ;
    }
*/    
    /**
     * A class that holds information about a stored database statement that
     * can be executed later, possibly multiple times.
     */
    public static interface PreparedDatabaseStatement {
        /**
         * Executes this prepared statement as a query and returns the
         * query results.
         * @return The results of the query.
         * @throws SQLException 
         */
        public Rows executeQuery() throws SQLException;
        /**
         * Executes this prepared statement as an update statement.
         * @return The number of rows affected.
         * @throws SQLException 
         */
        public int executeUpdate() throws SQLException;
        /**
         * Closes this prepared statement, indicating that it will not be needed
         * anymore and any resources related to it can be released.
         */
        public void close();
        /**
         * Sets a parameter of the prepared statement. Parameters are indexed
         * starting at 1. You can use any of the other specialised set methods
         * for specific types of parameters.
         * 
         * @param paramIndex The index of the parameter to set.
         * @param param The value of the parameter.
         * @param paramType The type of the parameter, as set in java.sql.Types.
         * @throws SQLException 
         */
        public void setParam(int paramIndex,Object param,int paramType) throws SQLException;
        public void setString(int paramIndex,String o) throws SQLException;
        public void setBoolean(int paramIndex,boolean o) throws SQLException;
        public void setInt(int paramIndex,int o) throws SQLException;
        public void setShort(int paramIndex,short o) throws SQLException;
        public void setLong(int paramIndex,long o) throws SQLException;
        public void setDouble(int paramIndex,double o) throws SQLException;
        public void setFloat(int paramIndex,float o) throws SQLException;
        public void setNull(int paramIndex,int type) throws SQLException;

    }
    
}
