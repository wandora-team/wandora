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

import java.util.Collection;
import java.util.HashMap;
import org.wandora.modules.*;

/**
 * This is a module you might include in the modules config to move users
 * from one user store to another. It's intended to be a one time move. Add
 * it to the config file once, set it to autostart, start the server and it'll do
 * its job. Then turn off the server and remove the module from the config.
 * 
 * The user stores between which users are copied are specified with init params.
 * Each store must be specified by name.
 *
 * @author olli
 */


public class UserStoreCopyTool extends AbstractModule {

    protected UserStore fromStore;
    protected ModifyableUserStore toStore;
    
    protected String fromStoreName;
    protected String toStoreName;
    
    @Override
    public Collection<org.wandora.modules.Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<org.wandora.modules.Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        
        if(fromStoreName!=null){
            fromStore=manager.findModule(this, fromStoreName, UserStore.class);
            if(fromStore==null) throw new MissingDependencyException(UserStore.class,fromStoreName);
        }
        if(toStoreName!=null){
            toStore=manager.findModule(this, toStoreName, ModifyableUserStore.class);
            if(toStore==null) throw new MissingDependencyException(ModifyableUserStore.class,toStoreName);
        }
        
        return deps;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o;
        o=settings.get("fromStore");
        if(o!=null) fromStoreName=o.toString();
        
        o=settings.get("toStore");
        if(o!=null) toStoreName=o.toString();
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if(fromStore==null || toStore==null){
            if(fromStoreName==null) logging.error("fromStore not specified in init parameters");
            if(toStoreName==null) logging.error("toStore not specified in init parameters");
            throw new ModuleException("User stores not found");
        }
        else {
            try{
                performCopy();
            }catch(UserStoreException use){
                logging.error(use);
            }
        }
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        super.stop(manager);
    }
    
    public void performCopy() throws UserStoreException{
        logging.info("Coping user store");
        
        Collection<User> users=fromStore.getAllUsers();
        
        for(User user : users){
            User newUser=toStore.getUser(user.getUserName());
            if(newUser==null) newUser=toStore.newUser(user.getUserName());
            
            for(String role : user.getRoles()){
                newUser.addRole(role);
            }
            
            for(String key : user.getOptionKeys()){
                String value=user.getOption(key);
                newUser.setOption(key, value);
            }
            newUser.saveUser();
        }
        
        logging.info("User copying done");
    }
    
}
