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
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.GenericTemplateAction;
import org.wandora.modules.servlet.ModulesServlet;
import org.wandora.modules.servlet.Template;

/**
 * <p>
 * An action that changes something about a user. Can either be used
 * on its own as a GenericTemplateAction, or as part of a ChainedAction
 * where this action just does the user modification and some other action in
 * the chain sends the response. If you intend to use this in chain mode, set
 * the initialisation parameter chainMode to true, that will suppress all output
 * from this action.
 * </p>
 * <p>
 * What changes are done to the user are specified in the initialisation 
 * parameters. Thus this action is not suitable for situations where the change
 * to be made to the user is specified in the HTTP request. You can use this for
 * static changes to a user, for example to promote a user to the administrator
 * role. The change is always the same, adding the administrator role.
 * </p>
 * <p>
 * Small dynamic behaviour however can be added using the replacements system.
 * This is only applied to the property values to be set, not the property keys
 * or any role changes. You can use this, for example, to set a time stamp
 * in the user properties for every request and thus have the last user access
 * time.
 * </p>
 * <p>
 * The changes to be made are specified using three initialisation parameters.
 * These are addRoles, removeRoles and setProperties. addRoles and removeRoles
 * specify roles which are to be added or removed, respectively. Each is a
 * semicolon or line feed separated list of roles. The setProperties is list
 * of key value pairs, separated by semicolons or line feeds. In each pair, the
 * key and value are separated by an equals sign.
 * </p>
 * 
 * @author olli
 */

public class ChangeUserAction extends GenericTemplateAction {
    
    protected HashMap<String,String> setProperties;
    
    protected ArrayList<String> removeRoles;
    protected ArrayList<String> addRoles;
    
    /*
     * If this action is set to chainMode then all output is suppressed and
     * the action can be used as the first action in a ChainedAction. Otherwise
     * the action generates normal GenericTemplaceAction output.
     */
    protected boolean chainMode=false;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    @Override
    protected HashMap<String, Object> getTemplateContext(Template template, HttpServletRequest req, ModulesServlet.HttpMethod method, String action, User user) throws ActionException {
        if(user!=null){
            for(String r : removeRoles){
                user.removeRole(r);
            }
            for(String r : addRoles){
                user.addRole(r);
            }
            for(Map.Entry<String,String> e : setProperties.entrySet()){
                String key=e.getKey();
                String value=e.getValue();
                value=doReplacements(value, req, method, action, user);
                if(value==null || value.length()==0) user.removeOption(key);
                else user.setOption(key, value);
            }
            boolean isActivated=true;
            try{
                if(!user.saveUser()) isActivated=false;
            }catch(UserStoreException use){
                throw new ActionException(use);
            }
            
            if(chainMode){
                return new HashMap<String, Object>(); // just return non-null to indicate success
            }
            else {
                HashMap<String, Object> context=super.getTemplateContext(template, req, method, action, user);
                context.put("isActivated",isActivated);
                return context;
            }
        }
        else return null;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        setProperties=new HashMap<String,String>();
        
        removeRoles=new ArrayList<String>();
        addRoles=new ArrayList<String>();
        
        Object o=settings.get("removeRoles");
        if(o!=null){
            String[] split=o.toString().split("[;\\n]");
            for(int i=0;i<split.length;i++){
                String s=split[i].trim();
                if(s.length()>0) removeRoles.add(s);
            }
        }
        
        o=settings.get("addRoles");
        if(o!=null){
            String[] split=o.toString().split("[;\\n]");
            for(int i=0;i<split.length;i++){
                String s=split[i].trim();
                if(s.length()>0) addRoles.add(s);
            }
        }
        
        o=settings.get("setProperties");
        if(o!=null){
            String[] split=o.toString().split("[;\\n]");
            for(int i=0;i<split.length;i++){
                String s=split[i].trim();
                
                int ind=s.indexOf("=");
                String key=s.substring(0,ind).trim();
                String value=s.substring(ind+1).trim();
                
                setProperties.put(key,value);
            }
        }        
        
        o=settings.get("chainMode");
        if(o!=null) chainMode=Boolean.parseBoolean(o.toString());
        
        super.init(manager, settings);
    }
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
        if(chainMode){
            Object o=getTemplateContext(null, req, method, action, user);
            if(o==null) return false;
            else return true;
        }
        else return super.handleAction(req, resp, method, action, user);
    }    
}
