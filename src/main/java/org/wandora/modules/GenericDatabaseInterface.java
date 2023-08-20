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
 */
package org.wandora.modules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.wandora.utils.ListenerList;
import org.wandora.utils.Tuples;
import org.wandora.utils.Tuples.T2;

/**
 * A general purpose implementation of the DatabaseInterface using JDBC
 * and thus being able to operate with many different relational
 * databases. The connection details of the JDBC connection are given as
 * module parameters in init with parameter names driver, connectionstring,
 * username and password. These contain, respectively, the full class name of the
 * JDBC driver to use, the connection string used by the driver, the user name
 * and the password.
 * 
 * 
 * @author olli
 */
public class GenericDatabaseInterface extends AbstractModule implements DatabaseInterface {

    protected ListenerList<DatabaseConnectionListener> listeners=new ListenerList<DatabaseConnectionListener>(DatabaseConnectionListener.class);

    protected String driver;
    protected String connectionString;
    protected String userName;
    protected String password;

//    protected String initScript;

    protected BasicDataSource connectionPool;
    
//    protected Connection connection;
    
//    protected int connectionRetries=5;

//    protected SimpleDateFormat timestampFormat=new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss.SSS''");


    @Override
    public String sqlEscape(String str){
        str=str.replace("'", "''");
        return str;
    }

    @Override
    public String sqlEscapeLen(String str,int length){
        if(str.length()>length) str=str.substring(0,length);
        str=sqlEscape(str);
        return str;
    }

    @Override
    public String formatTimestamp(long time){
//        Date d=new Date();
//        d.setTime(time);
//        return timestampFormat.format(d);

        // mysql doesn't support milliseconds in timestamps so it's better to store them with just milliseconds
        return ""+time;
    }


    public Rows makeRows(ResultSet rs) throws SQLException {
        Rows ret=new Rows();
        ResultSetMetaData md=rs.getMetaData();

        int columnCount=md.getColumnCount();
        String[] columns=new String[columnCount];
        for(int i=0;i<columnCount;i++){columns[i]=md.getColumnLabel(i+1).toLowerCase();}

        while(rs.next()){
            Row row=new Row();
            for(int i=0;i<columnCount;i++){
                Object o=rs.getObject(i+1);
                row.put(columns[i], o);
            }
            ret.add(row);
        }

        return ret;
    }

/*
    @Override
    public void runDatabaseRunnable(DatabaseRunnable run) throws SQLException {
        int retries=connectionRetries;
        while(true){
            try{
                run.run();
                break;
            }catch(SQLException sqle){
                retries--;
                if(retries<0) throw sqle;
                else {
                    if(retries<connectionRetries-1) try{Thread.sleep(10000);}catch(InterruptedException ie){}
                    reconnect();
                }
            }
        }
    }
*/

    @Override
    public Rows query(String query) throws SQLException {
        Connection connection=connectionPool.getConnection();
        if(connection==null) throw new SQLException("Couldn't get connection from the connection pool");
        Statement stmt=null;
        ResultSet rs=null;
        try{
            stmt=connection.createStatement();
            rs=stmt.executeQuery(query);
            return makeRows(rs);
        } finally{
            if(rs!=null) try{ rs.close(); } catch(SQLException ignore){}
            if(stmt!=null) try{ stmt.close(); } catch(SQLException ignore){}
            if(connection!=null) try{ connection.close(); } catch(SQLException ignore){}
        }
        
        /*
        int retries=connectionRetries;
        while(true){
            try{
                Statement stmt=connection.createStatement();
                try{
                    ResultSet rs=stmt.executeQuery(query);
                    return makeRows(rs);
                }
                finally{ stmt.close(); }
            } catch(SQLException sqle){
                retries--;
                if(retries<0) throw sqle;
                else {
                    if(retries<connectionRetries-1) try{Thread.sleep(10000);}catch(InterruptedException ie){}
                    reconnect();
                }
            }
        }
        */
    }

    @Override
    public int update(String query) throws SQLException {
        return update(query,null);
    }

    // generatedKeys is a container for another return value
    public int update(String query,Rows[] generatedKeys) throws SQLException {
        Connection connection=connectionPool.getConnection();
        if(connection==null) throw new SQLException("Couldn't get connection from the connection pool");
        Statement stmt=null;
        ResultSet rs=null;
        try{
            stmt=connection.createStatement();
            if(generatedKeys!=null && generatedKeys.length>0){
                int ret=stmt.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
                rs=stmt.getGeneratedKeys();
                generatedKeys[0]=makeRows(rs);
                return ret;
            }
            else {
                return stmt.executeUpdate(query);
            }
            
        } finally{
            if(rs!=null) try{ rs.close(); } catch(SQLException ignore){}
            if(stmt!=null) try{ stmt.close(); } catch(SQLException ignore){}
            if(connection!=null) try{ connection.close(); } catch(SQLException ignore){}
        }
        
        /*
        int retries=connectionRetries;
        while(true){
            try{
                Statement stmt=connection.createStatement();
                try{
                    if(generatedKeys!=null && generatedKeys.length>0){
                        int ret=stmt.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
                        ResultSet rs=stmt.getGeneratedKeys();
                        generatedKeys[0]=makeRows(rs);
                        return ret;
                    }
                    else {
                        return stmt.executeUpdate(query);
                    }
                }
                finally{ stmt.close(); }
            }catch(SQLException sqle){
                retries--;
                if(retries<0) throw sqle;
                else {
                    if(retries<connectionRetries-1) try{Thread.sleep(10000);}catch(InterruptedException ie){}
                    reconnect();
                }
            }
        }
        */
    }

//    private final Object autoIncrementLock=new Object();
    @Override
    public Object insertAutoIncrement(String query) throws SQLException {
        // this synchronisation is no longer needed with the connection pool
        // since connections are not shared
        
//        synchronized(autoIncrementLock){
            Rows[] generatedKeys=new Rows[1];
            update(query,generatedKeys);

            return generatedKeys[0].get(0).entrySet().iterator().next().getValue();
//        }
    }

/*    
    @Override
    public Rows queryPrepared(PreparedStatement stmt) throws SQLException {
        return makeRows(stmt.executeQuery());
    }
    @Override
    public int updatePrepared(PreparedStatement stmt) throws SQLException {
        return stmt.executeUpdate();
    }
*/
/*
    @Override
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return null;
        
        Connection connection=connectionPool.getConnection();
        if(connection!=null) try {
                return connection.prepareStatement(query);
            } finally{ connection.close(); }
        else throw new SQLException("Couldn't get connection from the connection pool");
    }
    */
/*
    public void connect() throws ClassNotFoundException,SQLException {
        // it seems hsqldb (or jdbc?) messes up logging, store handlers and then reset them after connecting
        Logger l=null;
        Level lev=null;
        Handler[] hs=null;

        if(connectionString.startsWith("jdbc:hsqldb:")){
            l=Logger.getLogger("");
            lev=l.getLevel();
            hs=l.getHandlers();
        }

        logging.info("Connecting to database");
        if(connection!=null) connection.close();
        Class.forName(driver);
        connection=DriverManager.getConnection(connectionString,userName,password);

        if(connectionString.startsWith("jdbc:hsqldb:")){
            Handler[] hs2=l.getHandlers();
            for(int i=0;i<hs2.length;i++){
                l.removeHandler(hs2[i]);
            }
            for(int i=0;i<hs.length;i++){
                l.addHandler(hs[i]);
            }
            l.setLevel(lev);
        }

        if(connection==null){
            logging.error("Couldn't connect to database. getConnection returned null.");
            return;
        }
        logging.info("Connected to database");

        if(initScript!=null){
            logging.info("Running database init script.");
            try{
                StringBuffer sb=new StringBuffer();
                BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(initScript),"UTF-8"));
                String line=null;
                while( (line=in.readLine())!=null ) {
                    sb.append(line);
                    sb.append("\n");
                }

                String[] lines=sb.toString().split(";");
                Statement stmt=connection.createStatement();
                for(int i=0;i<lines.length;i++){
                    lines[i]=lines[i].trim();
                    if(lines[i].length()>0) stmt.addBatch(lines[i]);
                }
                stmt.executeBatch();
                stmt.close();
            }
            catch(IOException ioe){
                logging.error("Couldn't run init script.",ioe);
            }
            catch(SQLException sqle2){
                logging.error("Couldn't run init script.",sqle2);
            }
        }
        fireDatabaseStarted();
    }

    protected final Object reconnectLock=new Object();
    @Override
    public void reconnect(){
        synchronized(reconnectLock){
            try{
                // this tests if the connection is working
                Statement stmt=connection.createStatement();
                stmt.executeQuery("select NOW();");
                stmt.close();

                // if we get here then the connection is working and nothing needs to be done
                return;
            }catch(SQLException e){}

            fireDatabaseClosed();

            try{
                connect();
                isRunning=true;
            }
            catch(ClassNotFoundException cnfe){
                logging.error("Couldn't reconnect to database.",cnfe);
            }
            catch(SQLException sqle){
                logging.error("Couldn't reconnect to database.",sqle);
            }
        }
    }
*/
/*
    @Override
    public void reconnect() {
    }
*/  
    
    protected void initConnectionPool(){
        connectionPool=new BasicDataSource();
        connectionPool.setDriverClassName(driver);
        connectionPool.setUrl(connectionString);
        connectionPool.setUsername(userName);
        connectionPool.setPassword(password);

        connectionPool.setValidationQuery("select 1");
        connectionPool.setTestOnBorrow(true);
        // connectionPool.setValidationQueryTimeout(1);
        
        fireDatabaseStarted();        
    }
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        driver=(String)settings.get("driver");
        connectionString=(String)settings.get("connectionstring");
        userName=(String)settings.get("username");
        password=(String)settings.get("password");
//        initScript=(String)settings.get("initscript");
        
//        Object o=settings.get("retries");
//        if(o!=null) connectionRetries=Integer.parseInt(o.toString());
        
        super.init(manager,settings);
    }
/*
    @Override
    public HashMap<String, Object> saveOptions() {
        HashMap<String,Object> ret=super.saveOptions();
        ret.put("driver",driver);
        ret.put("connectionstring",connectionString);
        ret.put("username",userName);
        ret.put("password", password);
//        ret.put("initscript",initScript);
        return ret;
    }
*/
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }


    @Override
    public void start(ModuleManager manager) throws ModuleException {
/*        try{
            connect();
            isRunning=true;
        }
        catch(ClassNotFoundException cnfe){
            throw new ModuleException("Couldn't connect to database",cnfe);
        }
        catch(SQLException sqle){
            throw new ModuleException("Couldn't connect to database",sqle);
        }
        // no call to super.start, isRunning is set after connect or in reconnect
        */
        
        initConnectionPool();
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
/*        if(connection!=null) {
            try{
                connection.close();
            }catch(SQLException sqle){
            }
            connection=null;
        }*/
        
        try{
            connectionPool.close();
        }
        catch(SQLException sqle){
            logging.warn("Exception closing connection pool",sqle);
        }

        fireDatabaseClosed();
        
        super.stop(manager);
    }

    @Override
    public PreparedDatabaseStatement prepareStatement(String query) throws SQLException {
        return new GenericPreparedDatabaseStatement(query);
    }

    
    
    public void addDatabaseConnectionListener(DatabaseConnectionListener l){
        listeners.addListener(l);
    }

    public void removeDatabaseConnectionListener(DatabaseConnectionListener l){
        listeners.removeListener(l);
    }


    protected void fireDatabaseStarted(){
        listeners.fireEvent("connectionStarted", this);
    }

    protected void fireDatabaseClosed(){
        listeners.fireEvent("connectionStarted", this);
    }
    
    protected class GenericPreparedDatabaseStatement implements PreparedDatabaseStatement {
        
        private String query;
        private ArrayList<T2<Integer,Object>> params;
        
        private Connection connection;
        private PreparedStatement statement;
        
        private int numRetries=1;
        
        public GenericPreparedDatabaseStatement(String query){
            this.query=query;
            params=new ArrayList<T2<Integer, Object>>();
        }
        
        private void openConnection() throws SQLException {
            close();
            
            logging.info("Opening connection");
            connection=connectionPool.getConnection();
            if(connection==null) throw new SQLException("Couldn't get connection from the connection pool for prepared statement");
            
            statement=connection.prepareStatement(query);
            for(int i=0;i<params.size();i++){
                T2<Integer,Object> param=params.get(i);
                if(param!=null) statement.setObject(i, param.e2, param.e1);
            }
        }

        @Override
        public void close() {
            try{
                if(statement!=null) statement.close();
                if(connection!=null) connection.close();
            }catch(SQLException ignore){}
            statement=null;
            connection=null;
        }
        
        @Override
        public Rows executeQuery() throws SQLException {
            if(statement==null) openConnection();
            int attempt=0;
            while(true){
                try{
                    ResultSet rs=statement.executeQuery();
                    try{
                        return makeRows(rs);
                    }
                    finally{
                        if(rs!=null) try{ rs.close(); } catch(SQLException ignore){}
                    }
                }catch(SQLException sqle){
                    logging.info("Connection closed, retrying");
                    attempt++;
                    if(attempt>numRetries) throw sqle;
                    else {
                        if(attempt>1) try{ Thread.sleep(1000); } catch(InterruptedException ignore){}
                        openConnection();
                    }
                }
            }
        }

        @Override
        public int executeUpdate() throws SQLException {
            if(statement==null) openConnection();
            int attempt=0;
            while(true){
                try{
                    return statement.executeUpdate();
                }catch(SQLException sqle){
                    attempt++;
                    if(attempt>numRetries) throw sqle;
                    else {
                        if(attempt>1) try{ Thread.sleep(1000); } catch(InterruptedException ignore){}
                        openConnection();
                    }
                }
            }
        }

        @Override
        public void setParam(int paramIndex, Object param, int paramType) throws SQLException {
            while(this.params.size()<=paramIndex) this.params.add(null);
            this.params.set(paramIndex, Tuples.t2(paramType, param));
            
            if(statement!=null){
                statement.setObject(paramIndex, param, paramType);
            }
        }

        @Override
        public void setBoolean(int paramIndex, boolean o) throws SQLException {
            setParam(paramIndex,o,java.sql.Types.BOOLEAN);
        }

        @Override
        public void setDouble(int paramIndex, double o) throws SQLException {
            setParam(paramIndex,o,java.sql.Types.DOUBLE);
        }

        @Override
        public void setFloat(int paramIndex, float o) throws SQLException {
            setParam(paramIndex,o,java.sql.Types.FLOAT);
        }

        @Override
        public void setInt(int paramIndex, int o) throws SQLException {
            setParam(paramIndex,o,java.sql.Types.INTEGER);
        }

        @Override
        public void setLong(int paramIndex, long o) throws SQLException {
            setParam(paramIndex,o,java.sql.Types.BIGINT);
        }

        @Override
        public void setNull(int paramIndex, int type) throws SQLException {
            setParam(paramIndex,null,type);
        }

        @Override
        public void setShort(int paramIndex, short o) throws SQLException {
            setParam(paramIndex,o,java.sql.Types.SMALLINT);
        }
        
        @Override
        public void setString(int paramIndex, String o) throws SQLException {
            setParam(paramIndex,o,java.sql.Types.VARCHAR);
        }
        
        
        
    }
}
