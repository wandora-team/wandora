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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;

/**
 * <p>
 * A very simple user store which reads user names and passwords and roles
 * straight from the initialisation parameters. Does not support editing users or 
 * any other options than password and roles. As such, it's really only useful for
 * rudimentary access control and even then, the passwords are stored in plain.
 * Nevertheless, this can make an easy to set up user store for development which
 * can then later be replaced with something more suitable.
 * </p>
 * <p>
 * The user data is read from the initialisation variable users. The contents of
 * this is a list of users, each user on its own line. Each user line is of the
 * following format:
 * </p>
 * <pre>
 * username;0;password;role1,role2,role3
 * </pre>
 * <p>
 * The digit 0 is meant to indicate the format of the rest of the line, with the
 * idea that in the future, this class could support more options on each user
 * line. At the moment, this is the only supported format however. The last part 
 * may contain any number of roles, separated by commas.
 * </p>
 * <p>
 * The password is stored in the user object using BasicUserAuthenticator.PASSWORD_KEY
 * as the key.
 * </p>
 *
 * @author olli
 */


public class StaticUserStore extends AbstractModule implements UserStore{
    protected Map<String,User> users;

    protected String userData;
    
    private Pattern userPattern1=Pattern.compile("^((?:[^;]|\\\\;|\\\\\\\\)*);(\\d+);(.*)$");
    private Pattern userPattern2=Pattern.compile("^((?:[^;]|\\\\;|\\\\\\\\)*);(.*$)");
    protected void parseUsers(String userData){
        users=new LinkedHashMap<String,User>();
        
        String[] lines=userData.split("\n");
        
        for(int i=0;i<lines.length;i++){
            String line=lines[i].trim();
            if(line.length()==0) continue;
            
            Matcher m=userPattern1.matcher(line);
            if(m.matches()) {
                
                String user=m.group(1).replaceAll("\\\\([\\\\;])", "$1");
                String dataType=m.group(2);
                String rest=m.group(3);
                
                if(Integer.parseInt(dataType)==0){
                    m=userPattern2.matcher(rest);
                    if(m.matches()){
                        String password=m.group(1).replaceAll("\\\\([\\\\;])", "$1");
                        String rolesS=m.group(2).replaceAll("\\\\([\\\\;])", "$1");
                        
                        String[] rolesA=rolesS.split(",");
                        ArrayList<String> roles=new ArrayList<String>();
                        for(int j=0;j<rolesA.length;j++) roles.add(rolesA[j].trim());
                        
                        User u=new SimpleUser(user, new HashMap<String,String>(), roles);
                        u.setOption(BasicUserAuthenticator.PASSWORD_KEY, password);
                        users.put(user,u);
                        
                        continue;
                    }
                }
                
            }
            
            // Successful parsing will use continue so if we get here then there was an error
            logging.warn("Unable to parse line "+(i+1)+" of user data");
        }
        
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        userData=(String)settings.get("users");
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        parseUsers(userData);
        super.start(manager);
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User getUser(String user) {
        return users.get(user);
    }

    @Override
    public Collection<User> findUsers(String key, String value) {
        if(!key.equals(BasicUserAuthenticator.PASSWORD_KEY)) return new ArrayList<User>();
        
        ArrayList<User> ret=new ArrayList<User>();
        for(User u : users.values()){
            String option=u.getOption(key);
            if(option!=null && option.equals(value)) ret.add(u);
        }
        return ret;
    }

    
}
