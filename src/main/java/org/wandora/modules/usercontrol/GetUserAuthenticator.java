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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet;


/**
 * <p>
 * A simple user authenticator that takes the user name and password from
 * the HTTP request parameters. Note that this is a bad solution for general
 * authentication and should only be used in very specific circumstances. The
 * passwords are not encrypted in the request and they are stored in plain text
 * on the server side as well. 
 * </p>
 * <p>
 * One use case where using this would be acceptable is when all passwords are
 * empty and only the user name is needed. For example, the user name might be
 * a server assigned API key for accessing some service.
 * </p>
 * <p>
 * You can set the request parameters where user name and password are read from
 * using initialisation parameters userParam and passwordParam, respectively.
 * </p>
 * 
 * @author olli
 */

public class GetUserAuthenticator extends AbstractModule implements UserAuthenticator {
    public static final String PASSWORD_KEY="password";
    protected String userParam="user";
    protected String passwordParam="password";
    protected String realm="";
    
    protected UserStore userStore;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.requireModule(UserStore.class, deps);
        requireLogging(manager, deps);
        return deps;
    }
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        
        Object o;
        o=settings.get("userParam");
        if(o!=null) userParam=o.toString();
        
        o=settings.get("passwordParam");
        if(o!=null) passwordParam=o.toString();
        if(passwordParam.length()==0) passwordParam=null;
        
        o=settings.get("realm");
        if(o!=null) realm=o.toString();
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        userStore=manager.findModule(UserStore.class);
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        userStore=null;
        
        super.stop(manager);
    }

    
    protected AuthenticationResult replyNotAuthorized(String realm, HttpServletResponse resp) throws IOException {
        return replyNotAuthorized(realm, resp, null);
    }
    
    protected AuthenticationResult replyNotAuthorized(String realm, HttpServletResponse resp, User user) throws IOException {
        return new AuthenticationResult(false, user, false);
    }
    
    @Override
    public AuthenticationResult authenticate(String requiredRole, HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method) throws IOException, AuthenticationException {
        
        String userName=req.getParameter(userParam);
        if(userName==null || userName.length()==0) return replyNotAuthorized(realm, resp);
        
        String password=null;
        if(passwordParam!=null) password=req.getParameter(passwordParam);
        
        try{
            User user=userStore.getUser(userName);

            if(user!=null) {
                String storedPassword=user.getOption(PASSWORD_KEY);
                if(storedPassword!=null && !storedPassword.equals(password)) return replyNotAuthorized(realm, resp);

                if(requiredRole!=null && !user.isOfRole(requiredRole)) return replyNotAuthorized(realm,resp,user); 
                else return new AuthenticationResult(true, user, false);
            }
            else return replyNotAuthorized(realm,resp); 
        }catch(UserStoreException use){
            throw new AuthenticationException(use);
        }
    }
    
}
