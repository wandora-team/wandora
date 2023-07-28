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
package org.wandora.modules.usercontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.utils.JsonMapper;

/**
 * <p>
 * A user store that reads users from a json file. Also supports modifying
 * the users and automatically saving the modified user store.
 * </p>
 * <p>
 * Auto saving is turned on by default. It can be turned off with the
 * initialisation parameter autoSave and setting it to false. The file name
 * must be specified with the initialisation parameter userFile.
 * </p>
 * 
 * @author olli
 */


public class FileUserStore extends AbstractModule implements ModifyableUserStore {

    protected final Map<String,User> users=new LinkedHashMap<String,User>();
    protected String userFile;
    
    protected long saveTime=10000;
    protected boolean autoSaveRunning=false;
    protected boolean autoSave=true;
    protected final Object saveMonitor=new Object();
    protected Thread saveThread;
    protected boolean changed=false;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o=settings.get("autoSave");
        if(o!=null) autoSave=Boolean.parseBoolean(o.toString());
        
        o=settings.get("userFile");
        if(o!=null) userFile=o.toString();
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if(userFile==null) throw new ModuleException("userFile not specified");
        
        if(!readJsonUsers(userFile)) throw new ModuleException("Couldn't read userFile");
        changed=false;
        
        if(autoSave){
            autoSaveRunning=true;
            saveThread=new Thread(new Runnable(){
                @Override
                public void run() {
                    saveThreadRun();
                }
            });
            saveThread.start();
        }
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        if(autoSaveRunning){
            autoSaveRunning=false;
            synchronized(saveMonitor){
                saveMonitor.notifyAll();
            }
            try{
                saveThread.join();
            }catch(InterruptedException ie){}
            saveThread=null;
        }
        
        super.stop(manager);
    }
    
    
    
    protected boolean readJsonUsers(String userFile){
        try{
            SimpleUser[] readUsers=new JsonMapper().readValue(new File(userFile), SimpleUser[].class);
            synchronized(users){
                users.clear();
                for(int i=0;i<readUsers.length;i++){
                    readUsers[i].setUserStore(this);
                    users.put(readUsers[i].getUserName(),readUsers[i]);
                }
                return true;
            }
        }catch(IOException ioe){
            logging.warn("Couldn't read Json user file",ioe);
            return false;
        }
    }
    
    protected void writeJsonUsers(String userFile){
        try{
            synchronized(users){
                logging.info("Saving users");
                new JsonMapper().writeValue(new File(userFile), users.values());
                changed=false;
            }
        }catch(IOException ioe){
            logging.warn("Couldn't write Json user file",ioe);
        }
    }
    
    @Override
    public Collection<User> getAllUsers() {
        synchronized(users){
            ArrayList<User> ret=new ArrayList<User>();
            for(User u : users.values()){
                ret.add(((SimpleUser)u).duplicate());
            }
            return ret;
        }
    }

    @Override
    public User getUser(String user) {
        synchronized(users){
            User u=users.get(user);
            if(u==null) return null;
            return ((SimpleUser)u).duplicate();
        }
    }

    @Override
    public User newUser(String user) {
        synchronized(users){
            if(users.containsKey(user)) return null;
            User u=new SimpleUser(user);
            ((SimpleUser)u).setUserStore(this);
            users.put(user,u);
            changed=true;
            return u;
        }
    }

    @Override
    public boolean saveUser(User user) {
        synchronized(users){
            User u=users.get(user.getUserName());
            if(u==null) return false;
            
            ((SimpleUser)u).setOptions(new HashMap<String,String>(((SimpleUser)user).getOptions()));
            ((SimpleUser)u).setRoles(new ArrayList<String>(((SimpleUser)user).getRoles()));
            changed=true;
        }
        return true;
    }

    @Override
    public boolean deleteUser(String user) {
        synchronized(users){
            if(users.containsKey(user)){
                users.remove(user);
                changed=true;
                return true;
            }
            else return false;
        }
    }
    
    @Override
    public Collection<User> findUsers(String key, String value) {
        ArrayList<User> ret=new ArrayList<User>();
        for(User u : users.values()){
            String option=u.getOption(key);
            if(option!=null && option.equals(value)) ret.add(u);
        }
        return ret;
    }    
    
    protected void saveThreadRun(){
        while(autoSaveRunning){
            synchronized(saveMonitor){
                try{
                    saveMonitor.wait(saveTime);
                }catch(InterruptedException ie){}
            }
            if(changed){
                writeJsonUsers(userFile);
            }
        }
    }
    
}
