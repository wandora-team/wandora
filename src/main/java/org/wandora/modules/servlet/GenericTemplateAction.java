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
package org.wandora.modules.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.usercontrol.User;

/**
 * <p>
 * A general purpose action that uses templates for output. This can be used
 * on its own or subclassed in a few different ways.
 * </p>
 * <p>
 * Standalone use just takes some template specified in initialisation parameters,
 * with parameter name templateKey, and gives it the default context returned by
 * the template manager. You can add objects into the context with initialisation
 * parameters of this action too, by prefixing the context variable name with
 * "context.". The default context, with possible action specific additions,
 * is passed to the template which can then produce the desired page.
 * </p>
 * <p>
 * To make the produced page depend on the HTTP request parameters, you can forward
 * some of them directly into the template context. To do this, list the request
 * parameters you would like forwarded in an initialisation parameter named 
 * forwardRequestParameters, separated by semicolons. Then the contents of the
 * user provided request parameters are copied into the template context. Remember
 * to sanity check any values thus forwarded. You can also set default values for
 * such request parameters which are used if none are provided in the request. Do
 * this by prefixing the initialisation parameter name with "defaultRequestParameter."
 * and then adding the parameter name and put the value in the contents of the 
 * param element.
 * </p>
 * <p>
 * Simplest way to subclass is to just override the getTemplateContext method.
 * It should use getDefaultTemplateContext as basis and then add whatever is needed
 * in the context. If needed, you can then do any side effects here as well, like
 * modifying a database or something similar.
 * </p>
 * <p>
 * For even more control, override the handleAction method. Then you have to call
 * the template yourself and write the output to the HTTP response object.
 * </p>
 * <p>
 * This class extends CachedAction so if it is likely that the same page be
 * generated often, it may be of use to cache the page. To do this, set the
 * initialisation parameter caching to true, as per normal CachedAction
 * behaviour. The default implementation will use the action name and all
 * forwarded request parameters in the cache key. Assuming the default context
 * doesn't contain anything dynamic, this should be adequate.
 * </p>
 * 
 * @author olli
 */


public class GenericTemplateAction extends CachedAction {

    protected String requestContextKey="request";
    
    protected TemplateManager templateManager;
    protected String templateKey;
    
    protected List<String> forwardRequestParameters;
    protected Map<String,String> defaultRequestParameters;
    
    protected final Map<String,Object> staticContext=new HashMap<String,Object>();
    
    /**
     * Adds a value to the action specific static template context. The
     * key value pair will be passed on to templates in the context.
     * @param key The name of the variable in the context.
     * @param o The value of the context variable.
     */
    public void putStaticContext(String key,Object o){
        staticContext.put(key,o);
    }
    
    protected Template getTemplate(HttpServletRequest req, HttpMethod method, String action){
        return templateManager.getTemplate(templateKey, null);
    }
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.requireModule(this,TemplateManager.class, deps);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        
        forwardRequestParameters=new ArrayList<String>();
        defaultRequestParameters=new HashMap<String,String>();
        
        Object o=settings.get("templateKey");
        if(o!=null) templateKey=o.toString();
        
        o=settings.get("forwardRequestParameters");
        if(o!=null) {
            String[] split=o.toString().split("[;\n]");
            for(int i=0;i<split.length;i++){
                String s=split[i].trim();
                if(s.length()>0) forwardRequestParameters.add(s);
            }
        }
        
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("context.")){
                staticContext.put(key.substring("context.".length()),e.getValue());
            }
            else if(key.startsWith("defaultRequestParameter.")){
                String param=key.substring("defaultRequestParameter.".length());
                defaultRequestParameters.put(param,e.getValue().toString());
            }
        }
        
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        templateManager=manager.findModule(this,TemplateManager.class);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        templateManager=null;
        super.stop(manager);
    }
    
    /**
     * Gets the static context to for templates. Includes action specific
     * items added with putStaticContext as well as the template manager
     * default context.
     * @return  The static template context.
     */
    protected Map<String,Object> getStaticTemplateContext(){
        Map<String,Object> params=new LinkedHashMap<String,Object>();
        params.putAll(templateManager.getDefaultContext());
        params.putAll(staticContext);
        return params;
    }
    
    /**
     * Returns a map of the http request parameters that are to be
     * forwarded to the template context.
     * @param req The HTTP request.
     * @return A Map containing the forwarded parameters and their values.
     */
    protected Map<String,String> getForwardedParameters(HttpServletRequest req){
        Map<String,String> params=new LinkedHashMap<String, String>();
        for(String s : forwardRequestParameters){
            String value=req.getParameter(s);
            if(value==null) {
                value=defaultRequestParameters.get(s);
                if(value==null) continue;
            }
            params.put(s,value);
        }
        return params;
    }
    
    /**
     * <p>
     * Gets the template context used for a request. Contains the static
     * context as well forwarded request parameters. The static context contains
     * both action specific context items and the template manager default context.
     * In addition, the logged in user, if applicable, is added with the variable
     * name user. Usually you don't want anything user specific cached so as a
     * safety measure, the user variable is not added if caching is turned on.
     * If you for some reason do, you will have to override this method.
     * </p>
     * <p>
     * This should be the main overriding point for extending classes. You can
     * perform any tasks needed and then add things in the context. Often you
     * don't need to override anything else besides this and the basic Module
     * methods.
     * </p>
     * 
     * @param template The template to be used.
     * @param req The HTTP request.
     * @param method The method of the HTTP request.
     * @param action The action parameter parsed from the HTTP request.
     * @param user The logged in user, or null if not logged in.
     * @return The template context used with processing the template for this action.
     * @throws ActionException 
     */
    protected Map<String,Object> getTemplateContext(Template template, HttpServletRequest req, HttpMethod method, String action, User user) throws ActionException {
        Map<String,Object> context=getStaticTemplateContext();
        context.put(requestContextKey,req);
        context.putAll(template.getTemplateContext());
        context.putAll(getForwardedParameters(req));
        
        // Generally you don't want to cache anything that's user specific. If
        // for some reason you do, just override getTemplateContext in your action
        // and add user in the context. Also add user name to cache key params if
        // needed.
        if(!caching) context.put("user",user);
        
        return context;
    }
    
    /**
     * Returns the parameters used in building the cache key. This implementation
     * uses the request action parameter and all parameters that have been
     * configured to be forwarded from the request into the template context. 
     * 
     * @param req The HTTP request for which cache key parameters are resolved.
     * @param method The method of the HTTP request.
     * @param action The parsed action parameter from the request.
     * @return 
     */
    protected Map<String,String> getCacheKeyParams(HttpServletRequest req, HttpMethod method, String action){
        Map<String,String> params=new LinkedHashMap<String,String>();
        params.putAll(getForwardedParameters(req));
        params.put("requestAction",action);
        return params;
    }
    
    @Override
    protected String getCacheKey(HttpServletRequest req, HttpMethod method, String action){
        return buildCacheKey(getCacheKeyParams(req, method, action));
    }

    @Override
    protected void returnOutput(InputStream cacheIn, HttpServletResponse resp) throws IOException {
        try{
            Map<String,String> metadata=readMetadata(cacheIn);

            String mimetype=metadata.get("contenttype");
            if(mimetype!=null) resp.setContentType(mimetype);
            String encoding=metadata.get("encoding");
            if(encoding!=null) resp.setCharacterEncoding(encoding);
        
            super.returnOutput(cacheIn, resp);
        }
        finally{
            cacheIn.close();
        }
    }

    
    
    @Override
    protected boolean doOutput(HttpServletRequest req, HttpMethod method, String action, OutputProvider out, User user) throws ServletException, IOException, ActionException {
        Template template=getTemplate(req, method, action);
        if(template==null) return false;

        Map<String,Object> context=getTemplateContext(template, req, method, action, user);
        if(context==null) return false;
        
        Map<String,String> metadata=new LinkedHashMap<String,String>();
        metadata.put("contenttype",template.getMimeType());
        metadata.put("encoding",template.getEncoding());
        
        OutputStream outStream=out.getOutputStream();
        try{
            writeMetadata(metadata, outStream);
            template.process(context, outStream);
        }
        finally{
            outStream.close();
        }
        return true;
    }
    
}
