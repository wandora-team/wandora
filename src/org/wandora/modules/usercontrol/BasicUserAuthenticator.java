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
package org.wandora.modules.usercontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.utils.Base64;

/**
 * <p>
 * A simple user authenticator that performs standard HTTP authentication
 * using the BASIC scheme. Even if you use this over a secure connection, this
 * might still not be suitable for a production environment, except in specific
 * circumstances. The reason is that all user password are stored as plain text
 * in the user objects. Thus collecting user supplied password would be a very
 * bad idea and go against good security practices. However, if the user passwords
 * are assigned by the system, or there is only a limited number of users who
 * are aware of the risks, this may still be a usable solution. In any case,
 * this can be used in a development environment and later replaced with a more
 * suitable solution.
 * </p>
 * <p>
 * You can set the realm for the authentication using the realm initialisation
 * parameter.
 * </p>
 * 
 * @author olli
 */


public class BasicUserAuthenticator extends AbstractModule implements UserAuthenticator {

    public static final String PASSWORD_KEY="password";
    
    protected String realm;
    
    protected UserStore userStore;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.requireModule(UserStore.class, deps);
        requireLogging(manager, deps);
        return deps;
    }
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("realm");
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
        resp.setHeader("WWW-Authenticate","BASIC realm=\""+realm+"\"");
        resp.sendError(resp.SC_UNAUTHORIZED);
        return new AuthenticationResult(false, null, true);
    }
    
    @Override
    public AuthenticationResult authenticate(String requiredRole, HttpServletRequest req, HttpServletResponse resp, HttpMethod method) throws IOException, AuthenticationException {
        String auth=req.getHeader("Authorization");
        if(auth==null) return replyNotAuthorized(realm,resp);
        if(!auth.toUpperCase().startsWith("BASIC ")) return replyNotAuthorized(realm,resp);
        
        String encoded=auth.substring(6);
        String credentials=new String(Base64.decode(encoded));
        int ind=credentials.indexOf(":");
        String userName;
        String password="";
        if(ind<0) userName=credentials;
        else {
            userName=credentials.substring(0,ind);
            password=credentials.substring(ind+1);
        }
        
        try{
            User user=userStore.getUser(userName);

            if(user!=null) {
                String storedPassword=user.getOption(PASSWORD_KEY);
                if(storedPassword!=null && !storedPassword.equals(password)) return replyNotAuthorized(realm, resp);

                if(requiredRole!=null && !user.isOfRole(requiredRole)) return replyNotAuthorized(realm,resp); 
                else return new AuthenticationResult(true, user, false);
            }
            else return replyNotAuthorized(realm,resp); 
        }catch(UserStoreException use){
            throw new AuthenticationException(use);
        }
    }
    
}
