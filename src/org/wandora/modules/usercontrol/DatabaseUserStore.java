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
 */
package org.wandora.modules.usercontrol;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.wandora.modules.*;
import org.wandora.modules.DatabaseInterface.Row;
import org.wandora.modules.DatabaseInterface.Rows;

/**
 * <p>
 * A user store which stores users in a relational database. Needs a 
 * module implementing the DatabaseInterface to work. The user data is stored
 * in three tables, which need to be created beforehand. Following sql script
 * will generate the tables on MySQL, you may need to adjust it slightly for
 * other database systems.
 * </p>
 *
<pre>
create table USERS(
  ID bigint AUTO_INCREMENT primary key,
  USERNAME varchar(256) not null collate utf8_bin
);
 
create table USER_ROLES(
  USERID bigint not null references USERS(ID),
  ROLE varchar(256) not null collate utf8_bin
);
 
create table USER_PROPS(
  USERID bigint not null references USERS(ID),
  PROPKEY varchar(256) not null collate utf8_bin,
  PROPVALUE varchar(2048) collate utf8_bin
);
</pre>
 *
 * <p>
 * You may prefix each table with a prefix of your choosing. Specify it in the
 * initialisation parameters with tablePrefix.
 * </p>
 * <p>
 * Any changes to the user store are immediately stored in the database when
 * saveUser is called.
 * </p>
 * 
 * @author olli
 */


public class DatabaseUserStore extends AbstractModule implements ModifyableUserStore {

    protected String tablePrefix="";
    
    protected DatabaseInterface database;
    
    protected final HashMap<String,DBUser> users=new HashMap<String,DBUser>();
    
    protected void handleSQLException(SQLException sqle) throws UserStoreException {
        logging.warn(sqle);
        throw new UserStoreException("Error accessing user database",sqle);
    }
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(this, DatabaseInterface.class, deps);
        return deps;
    }
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o;
        o=settings.get("tablePrefix");
        if(o!=null) tablePrefix=o.toString();
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        database=manager.findModule(this,DatabaseInterface.class);
        try{
            fetchUsers();
        }
        catch(UserStoreException e){
            throw new ModuleException(e);
        }
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        database=null;
        super.stop(manager);
    }

    
    @Override
    public boolean deleteUser(String user) throws UserStoreException{
        if(!commitDeleteUser(user)) return false;
        synchronized(users){
            users.remove(user);
            return true;
        }
    }

    @Override
    public User newUser(String user) throws UserStoreException{
        synchronized(users){
            if(users.containsKey(user)) return null;
            DBUser u=commitNewUser(user);
            if(u==null) return null;
            u.setUserStore(this);
            users.put(user, u);
            return u;
        }
    }

    @Override
    public boolean saveUser(User user) throws UserStoreException{
        if(!commitUser((DBUser)user)) return false;
        synchronized(users){
            users.put(user.getUserName(),((DBUser)user).duplicate());
            return true;
        }
    }

    @Override
    public Collection<User> findUsers(String key, String value) throws UserStoreException{
        synchronized(users){
            ArrayList<User> ret=new ArrayList<User>();
            for(DBUser u : users.values()){
                String option=u.getOption(key);
                if(option!=null && option.equals(value)) ret.add(u.duplicate());
            }
            return ret;
        }
    }

    protected void fetchUsers() throws UserStoreException {
        HashMap<String,DBUser> newUsers=new HashMap<String,DBUser>();
        try{
            ArrayList<User> ret=new ArrayList<User>();
            Rows userRows=database.query("select * from "+tablePrefix+"USERS order by ID");
            Rows roleRows=database.query("select * from "+tablePrefix+"USER_ROLES order by USERID");
            Rows propRows=database.query("select * from "+tablePrefix+"USER_PROPS order by USERID");
            
            int rolePointer=0;
            int propPointer=0;
            
            for(Row userRow : userRows){
                String userName=(String)userRow.get("username");
                long id=(Long)userRow.get("id");
                DBUser user=new DBUser(id,userName);
                user.setUserStore(this);
                
                while(rolePointer<roleRows.size()){
                    Row roleRow=roleRows.get(rolePointer);
                    if((Long)roleRow.get("userid")!=id) break;
                    rolePointer++;
                    String role=(String)roleRow.get("role");
                    user.addRole(role);
                }
                
                while(propPointer<propRows.size()){
                    Row propRow=propRows.get(propPointer);
                    if((Long)propRow.get("userid")!=id) break;
                    propPointer++;
                    String key=(String)propRow.get("propkey");
                    String value=(String)propRow.get("propvalue");
                    user.setOption(key, value);
                }
                
                newUsers.put(userName,user);
            }
        }catch(SQLException sqle){
            handleSQLException(sqle);
            return; // handleSQLException throws a UserException so nothing actually gets returned
        }        
        synchronized(users){
            users.clear();
            users.putAll(newUsers);
        }
    }
    
    private final Object modifyLock=new Object();
    protected boolean commitUser(DBUser user) throws UserStoreException{
        try{
            long id=user.getId();
            synchronized(modifyLock){
                database.update("delete from "+tablePrefix+"USER_ROLES where USERID="+id);
                database.update("delete from "+tablePrefix+"USER_PROPS where USERID="+id);
                
                if(!user.getRoles().isEmpty()){
                    StringBuilder sb=new StringBuilder(
                            "insert into "+tablePrefix+"USER_ROLES (USERID,ROLE) values ");
                    boolean first=true;
                    for(String role : user.getRoles()){
                        if(!first) sb.append(", ");
                        else first=false;
                        sb.append("(").append(id).append(",'").append(database.sqlEscapeLen(role,256)).append("')");
                    }
                    database.update(sb.toString());
                }
                
                if(!user.getOptions().isEmpty()) {
                    StringBuilder sb=new StringBuilder(
                            "insert into "+tablePrefix+"USER_PROPS (USERID,PROPKEY,PROPVALUE) values ");
                    boolean first=true;
                    for(Map.Entry<String,String> e : user.getOptions().entrySet()){
                        String key=e.getKey();
                        String value=e.getValue();
                        if(!first) sb.append(", ");
                        else first=false;
                        sb.append("(").append(id).append(",'").append(database.sqlEscapeLen(key,256)).append("','").append(database.sqlEscapeLen(value,256)).append("')");
                    }
                    database.update(sb.toString());                    
                }
                
                // USER table itself doesn't have any modifyable information so no need to update that
                
                return true;
            }
        }catch(SQLException sqle){
            handleSQLException(sqle);
            return false; // handleSQLException throws a UserException so nothing actually gets returned
        }
    }
    
    protected boolean commitDeleteUser(String userName) throws UserStoreException{
        try{
            synchronized(modifyLock) {
                Rows rows=database.query("select * from "+tablePrefix+"USERS where USERNAME='"+database.sqlEscape(userName)+"'");
                if(rows.isEmpty()) return false;
                long id=(Long)rows.get(0).get("id");
                database.update("delete from "+tablePrefix+"USER_ROLES where USERID="+id);
                database.update("delete from "+tablePrefix+"USER_PROPS where USERID="+id);
                database.update("delete from "+tablePrefix+"USERS where ID="+id);
                return true;
            }
        }catch(SQLException sqle){
            handleSQLException(sqle);
            return false; // handleSQLException throws a UserException so nothing actually gets returned
        }
    }
    
    protected DBUser commitNewUser(String userName) throws UserStoreException{
        try{
            synchronized(modifyLock){
                long id=(Long)database.insertAutoIncrement("insert into "+tablePrefix+"USERS (USERNAME) values ("+
                    "'"+database.sqlEscapeLen(userName, 256) +"')");
                DBUser u=new DBUser(id,userName);
                return u;
            }
        }
        catch(SQLException sqle){
            handleSQLException(sqle);
            return null; // handleSQLException throws a UserException so nothing actually gets returned
        }
    }
    
    @Override
    public Collection<User> getAllUsers() throws UserStoreException {
        synchronized(users){
            ArrayList<User> ret=new ArrayList<User>();
            for(DBUser user : users.values()){
                ret.add(user.duplicate());
            }
            return ret;
        }
    }

    @Override
    public User getUser(String user) throws UserStoreException {
        synchronized(users){
            return users.get(user);
        }
    }

    protected static class DBUser extends SimpleUser {

        public DBUser(long id,String userName, HashMap<String, String> options, ArrayList<String> roles, UserStore userStore) {
            super(userName, options, roles, userStore);
            this.id=id;
        }

        public DBUser(long id,String userName, HashMap<String, String> options, ArrayList<String> roles) {
            super(userName, options, roles);
            this.id=id;
        }

        public DBUser(long id,String userName) {
            super(userName);
            this.id=id;
        }

        public DBUser(long id) {
            this.id=id;
        }

        private long id;
        public long getId(){return id;}

        @Override
        public DBUser duplicate() {
            return new DBUser(id,userName, new HashMap<String,String>(options), new ArrayList<String>(roles),userStore);
        }
        
    }
}
