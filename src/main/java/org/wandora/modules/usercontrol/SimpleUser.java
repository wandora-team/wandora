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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * A basic user implementation. Implements all the methods of the abstract
 * base class. Roles and options are stored in memory in a simple ArrayList and
 * a HashMap, respectively.
 * 
 * @author olli
 */


public class SimpleUser extends User {

    protected String userName;
    protected Map<String,String> options;
    protected List<String> roles;
    @JsonIgnore
    protected boolean changed=false;
    @JsonIgnore
    protected UserStore userStore=null;
    
    public SimpleUser(){
    }
    
    public SimpleUser(String userName){
        this(userName,new LinkedHashMap<String,String>(),new ArrayList<String>());
    }
    
    public SimpleUser(String userName, Map<String,String> options, List<String> roles) {
        this(userName, options, roles, null);
    }
    
    public SimpleUser(String userName, Map<String,String> options, List<String> roles, UserStore userStore) {
        this.userName = userName;
        this.options = options;
        this.roles = roles;
        this.userStore = userStore;
    }
    
    public void setUserName(String s) {
        this.userName=s;
    }
    
    @Override
    public String getUserName() {
        return userName;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    @JsonIgnore
    @Override
    public Collection<String> getOptionKeys() {
        return options.keySet();
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @JsonIgnore
    @Override
    public void removeOption(String optionKey) {
        options.remove(optionKey);
        changed=true;
    }
    
    
    @JsonIgnore
    @Override
    public String getOption(String optionKey) {
        return options.get(optionKey);
    }
    
    public void setRoles(Collection<String> roles){
        this.roles=new ArrayList<String>(roles);
    }

    @Override
    public Collection<String> getRoles() {
        return roles;
    }

    @JsonIgnore
    @Override
    public void addRole(String role) {
        if(!roles.contains(role)) roles.add(role);
    }

    @JsonIgnore
    @Override
    public void removeRole(String role) {
        roles.remove(role);
    }
    
    

    @JsonIgnore
    @Override
    public void setOption(String optionKey, String value) {
        this.options.put(optionKey, value);
        changed=true;
    }
    
    @JsonIgnore
    public void resetChanged(){
        changed=false;
    }
    
    @JsonIgnore
    public boolean isChanged(){
        return changed;
    }

    @JsonIgnore
    @Override
    public boolean saveUser() throws UserStoreException {
        if(userStore!=null && userStore instanceof ModifyableUserStore){
            return ((ModifyableUserStore)userStore).saveUser(this);
        }
        else return false;
    }

    @JsonIgnore
    public UserStore getUserStore() {
        return userStore;
    }

    @JsonIgnore
    public void setUserStore(UserStore userStore) {
        this.userStore = userStore;
    }
    
    @JsonIgnore
    public SimpleUser duplicate(){
        return new SimpleUser(userName, new LinkedHashMap<String,String>(options), new ArrayList<String>(roles),userStore);
    }
}
