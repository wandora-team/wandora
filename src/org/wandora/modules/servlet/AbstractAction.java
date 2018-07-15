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
package org.wandora.modules.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.*;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.usercontrol.User;

/**
 * <p>
 * The base class for action modules, that is modules that respond to
 * an HTTP request and return some content. It implements 
 * ServletModule.RequestListener which makes it possible to register itself
 * to ServletModules. In addition, it also implements ActionHandler which is
 * needed if the action is used inside one of the context classes.
 * </p>
 * <p>
 * If you extend this class directly, your main point of extension is the only
 * abstract method, handleAction, and the basic Module initialisation methods.
 * If you wish to be notified of every HTTP request without any filtering, 
 * override the handleRequest instead.
 * </p>
 * <p>
 * This action reads parameters with following names from the initialisation
 * parameters. actionParamKey can be used to specify the HTTP request parameter
 * which contains the action id, this defaults to "action". action parameter
 * contains the id of this action, alternatively, you can use parameter actions
 * and provide a comma separated list of multiple ids, the default is to have no
 * id associated with this action. defaultAction parameter can be set
 * to "true" to make this a default action. If the action id is not specified
 * in the request and this is a default action, it'll try to handle the action.
 * Any parameter with its name prefixed with "httpheader." will be used to add
 * custom HTTP headers in the response. The part following this prefix in the
 * parameter name is not
 * important, it only needs to be unique in the module. The actual header
 * name and value are parsed from the contents of the element. It should
 * contain the header name followed by a colon character (:) followed by
 * the value.
 * </p>
 * <p>
 * Note that custom HTTP headers can be added in the initialisation parameters
 * but they don't get automatically in the response. Instead, you will need
 * to call setHttpHeaders in your handleAction code to have them added.
 * </p>
 * 
 * @author olli
 */

public abstract class AbstractAction extends ScriptModule implements ServletModule.RequestListener, ActionHandler {

    protected boolean isDefaultAction=false;
    
    protected ServletModule servletModule;
    protected String actionParamKey="action";
    
    protected final Collection<String> handledActions=new HashSet<String>();
    
    protected final HashMap<String,String> httpHeaders=new HashMap<String,String>();
    
    protected void addHandledAction(String action){
        this.handledActions.add(action);
    }
    
    protected void setActionParamKey(String key){
        this.actionParamKey=key;
    }
    
    @Override
    public Collection<org.wandora.modules.Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<org.wandora.modules.Module> deps=super.getDependencies(manager);
        manager.requireModule(this,ServletModule.class, deps);
        return deps;
    }
    
    protected boolean replacementsInitialized=false;
    protected ReplacementsModule replacements=null;
    /**
     * Performs basic string replacement using a ReplacementsModule if
     * one is available. Otherwise returns the value as is. See
     * ReplacementsModule for more information. Adds certain things from the
     * request in the context that will be passed to the replacements module.
     * Context variable user contains the user, req contains the request and
     * action contains the action name.
     * 
     * @param value The value in which replacement is to be done.
     * @param req The HTTP request where this call originated.
     * @param method The method of the HTTP request.
     * @param action The parsed action.
     * @param user The logged in user, or null if not applicable.
     * @return The value after replacements have been done.
     */
    public String doReplacements(String value,HttpServletRequest req, ModulesServlet.HttpMethod method, String action, User user){
        if(!replacementsInitialized){
            // Could add some kind of locking but as it is, it doesn't really
            // matter if two threads do this initialization.
            replacements=moduleManager.findModule(this, ReplacementsModule.class);
            replacementsInitialized=true;
        }
        
        // if replacements module is not used, then just return the string as is
        if(replacements==null) return value;
        
        HashMap<String,Object> context=new HashMap<String,Object>();
        context.put("user", user);
        context.put("request", req);
        context.put("action", action);
        
        return replacements.replaceValue(value, context);
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object actionParamKeyO=settings.get("actionParamKey");
        if(actionParamKeyO!=null) this.actionParamKey=actionParamKeyO.toString();
        Object actionsO=settings.get("actions");
        if(actionsO!=null){
            String[] actions=actionsO.toString().split(",");
            for(String a : actions){
                a=a.trim();
                if(a.length()>0) this.addHandledAction(a);
            }
        }
        Object actionO=settings.get("action");
        if(actionO!=null){
            String action=actionO.toString();
            if(action.length()>0) this.addHandledAction(action);
        }
        
        Object defaultO=settings.get("defaultAction");
        if(defaultO!=null) isDefaultAction=Boolean.parseBoolean(defaultO.toString());
        
        // Http header parsing is done here in AbstractAction but they don't
        // get automatically added to every request. Actions have to
        // call setHttpHeaders to do that. This must be done prior to writing
        // anything else to the response but after the action is sure that it's
        // going to handle the action. CachedAction automatically does it as it
        // has a mechanism to detect this point in action. For all other cases,
        // the action implementation needs to do it.
        for(Map.Entry<String,Object> e : settings.entrySet()){
            if(e.getKey().startsWith("httpheader")){
                String value=e.getValue().toString();
                int ind=value.indexOf(":");
                if(ind>0){
                    String key=value.substring(0,ind).trim();
                    value=value.substring(ind+1).trim();
                    httpHeaders.put(key,value);
                }
            }
        }
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        servletModule=manager.findModule(this,ServletModule.class);
        servletModule.addRequestListener(this);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        if(servletModule!=null) servletModule.removeRequestListener(this);
        servletModule=null;
        super.stop(manager);
    }
    
    /**
     * Sets HTTP headers from a Map of key value pairs. You typically pass
     * the field httpHeaders to this method if you wish to add them in the 
     * response.
     * 
     * @param resp The HTTP response object.
     * @param headers A Map containing the headers to be added.
     */
    protected void setHttpHeaders(HttpServletResponse resp,Map<String,String> headers) {
        for(Map.Entry<String,String> e : headers.entrySet()){
            resp.setHeader(e.getKey(), e.getValue());
        }
    }

    /**
     * Handles this action. Prior to calling this action, it's already been
     * determined that this action should try to handle the action by either
     * matching the action request parameter with the action id of this action,
     * or this being the default action. Perform any operations needed here and
     * then write the response into the response object. You will typically want
     * to call setHttpHeaders to set the HTTP headers in the response after you
     * know that you definitely will handle the request but before you write
     * anything else into the response stream. If for whatever reason this 
     * method shouldn't handle the request after all, return false. Otherwise
     * return true.
     * 
     * @param req The HTTP Request.
     * @param resp The HTTP response.
     * @param method The HTTP method of the request.
     * @param action The parsed action id.
     * @param user The logged in user, or null if not applicable.
     * @return True if the action was handled and a response sent. False otherwise
     *          in which case other actions may try to handle this action.
     * @throws ServletException
     * @throws IOException
     * @throws ActionException 
     */
    public abstract boolean handleAction(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, User user) throws ServletException, IOException, ActionException ;
    
    @Override
    public boolean isHandleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method) {
        String action=null;
        if(this.actionParamKey!=null) action=req.getParameter(this.actionParamKey);
        if(isDefaultAction && (action==null || action.length()==0) ) return true;
        if(this.actionParamKey==null || this.handledActions.isEmpty() ) return false;
        if(action==null) return false;
        return this.handledActions.contains(action);
    }
    
    @Override
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, User user) throws ServletException, IOException, ActionException {
        if(isHandleAction(req, resp, method)) {
            String action=null;
            if(actionParamKey!=null) action=req.getParameter(actionParamKey);
            if(action==null && isDefaultAction && !handledActions.isEmpty()) action=handledActions.iterator().next();
            return handleAction(req, resp, method, action, user);
        }
        else return false;
    }
    
}
