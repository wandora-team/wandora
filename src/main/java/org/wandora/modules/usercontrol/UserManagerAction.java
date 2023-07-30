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
import java.util.Enumeration;
import java.util.Map;

import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.GenericTemplateAction;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.servlet.Template;

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author olli
 */


public class UserManagerAction extends GenericTemplateAction {

    protected ModifyableUserStore userStore;
    
    
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(ModifyableUserStore.class, deps);
        return deps;
    }

    @Override
    protected String getCacheKey(HttpServletRequest req, HttpMethod method, String action) {
        // caching is possible only for userlist and viewuser
        String editAction=req.getParameter("editAction");
        if(editAction==null) editAction="userlist";
        if(!editAction.equals("viewuser") && !editAction.equals("userlist")) return null;
        else return super.getCacheKey(req, method, action);
    }

    
    
    @Override
    protected Map<String, Object> getTemplateContext(Template template, HttpServletRequest req, HttpMethod method, String action, User user) throws ActionException {
        String editAction=req.getParameter("editaction");
        if(editAction==null || editAction.length()==0) editAction="userlist";
        
        Map<String,Object> params=super.getTemplateContext(template, req, method, action, user);
        
        String userName=req.getParameter("user");
        if(userName!=null) userName=userName.trim();
        if(userName!=null && userName.length()==0) userName=null;
        
        User userObject=null;
        String view="userlist";
        String error=null;
        try{
            if(editAction.equals("userlist")){
                // no side effects, do nothing
            }
            else if(editAction.equals("viewuser")){
                view="user";
                // just check that the user is valid
                if(userName!=null) userObject=userStore.getUser(userName);
            }
            else if(editAction.equals("edituser") || editAction.equals("edituserlist")){
                view="user";
                if(editAction.equals("edituserlist")) view="userlist";

                if(userName!=null) {
                    userObject=userStore.getUser(userName);
                    if(userObject!=null){

                        Enumeration<String> paramNames=req.getParameterNames();
                        while(paramNames.hasMoreElements()){
                            String key=paramNames.nextElement();
                            String[] values=req.getParameterValues(key);
                            for(String value : values){
                                value=value.trim();
                                if(key.equals("setoption")){
                                    int ind=value.indexOf("=");
                                    if(ind<0) userObject.setOption(value, "");
                                    else {
                                        String k=value.substring(0,ind);
                                        String v=value.substring(ind+1);
                                        userObject.setOption(k,v);
                                    }
                                }
                                else if(key.equals("removeoption")){
                                    userObject.removeOption(value);
                                }
                                else if(key.equals("addrole")){
                                    userObject.addRole(value);
                                }
                                else if(key.equals("removerole")){
                                    userObject.removeRole(value);
                                }
                            }
                        }
                        if(!userObject.saveUser()) error="NOEDIT";
                    }
                }
            }
            else if(editAction.equals("deleteuser")){
                if(userName!=null) {
                    if(!userStore.deleteUser(userName)) error="NODELETE";
                }
            }
            else if(editAction.equals("newuser")){
                view="user";
                if(userName!=null) {
                    userObject=userStore.newUser(userName);
                    if(userObject==null) error="NONEW";
                }
            }
            else return null;
            
            if(view.equals("user") && userObject==null) {
                if(error==null) error="INVALIDUSER";
                view="userlist";
            }

            if(view.equals("userlist")){
                params.put("allUsers",userStore.getAllUsers());
            }
            else {
                params.put("user",userObject);
            }
            
        }catch(UserStoreException use){
            if(error==null) error="USERSTORE";
        }
                
        params.put("editView",view);
        params.put("error",error);
        
        return params;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        forwardRequestParameters.add("editAction");
        forwardRequestParameters.add("user");
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        userStore=manager.findModule(ModifyableUserStore.class);
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        userStore=null;
        
        super.stop(manager);
    }
    
}
