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

import java.util.Collection;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.servlet.SendEmailAction;
import org.wandora.modules.servlet.Template;

/**
 *
 * @author olli
 */


public class PasswordResendAction extends SendEmailAction {

    protected UserStore userStore;
    protected String emailKey="email"; // the key used in the user object to store email
    protected String emailParam="email"; // the key used in http request to supply email

    @Override
    protected Map<String, Object> getTemplateContext(Template template, HttpServletRequest req, HttpMethod method, String action, User user) throws ActionException {
        Map<String,Object> context=super.getTemplateContext(template, req, method, action, user);
        
        String userEmail=req.getParameter(emailParam);
        if(userEmail==null || userEmail.length()==0) return null;
        
        Collection<User> us=null;
        try{
            us=userStore.findUsers(emailKey, userEmail);
        } catch(UserStoreException use){
            return null;
        }

        if(us.isEmpty()) return null;

        if(us.size()>1) logging.info("More than one user has email "+userEmail+", using only first");
        User u=us.iterator().next();

        context.put("user",u);
        context.put("email",userEmail);

        return context;
    }


    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(UserStore.class, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o;
        o=settings.get("emailKey");
        if(o!=null) emailKey=o.toString();
        
        o=settings.get("emailParam");
        if(o!=null) emailParam=o.toString();
        
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
    
}
