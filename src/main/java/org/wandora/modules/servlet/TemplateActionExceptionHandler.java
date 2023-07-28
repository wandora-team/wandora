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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.wandora.modules.usercontrol.User;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 * <p>
 * An implementation of ActionExceptionHandler, a module that handles
 * exceptions thrown by other actions. This handler uses a template to
 * produce the result, much like GeneritTemplateAction. This behaves much
 * the same way as GenericTemplateHandler, but does not extend from it, the
 * reason being a circular dependency problem (See ActionExceptionHandler
 * documentation for more about this).
 * </p>
 * <p>
 * This uses initialisation parameters templateKey, forwardRequestParameters and
 * prefixes context. and defaultRequestParameter. in exactly the same way as
 * GenericTemplateAction does, refer to its documentation.
 * </p>
 * <p>
 * Three additional variables are added to the template context. exception contains
 * the exception that was caught, errorMessage contains the error message from the
 * exception, this may or may not be user friendly. Finally, trace contains a 
 * stack trace of the exception.
 * </p>
 *
 * @author olli
 */

public class TemplateActionExceptionHandler extends AbstractModule implements ActionExceptionHandler {

    public static final String EXCEPTION_ACTION="$EXCEPTION";

    protected String requestContextKey="request";
    
    protected TemplateManager templateManager;
    protected String templateKey;
    
    protected ArrayList<String> forwardRequestParameters;
    protected HashMap<String,String> defaultRequestParameters;
    
    protected final HashMap<String,Object> staticContext=new HashMap<String,Object>();
    
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
    
    protected HashMap<String,Object> getStaticTemplateContext(){
        HashMap<String,Object> params=new HashMap<String,Object>();
        params.putAll(templateManager.getDefaultContext());
        params.putAll(staticContext);
        return params;
    }
    
    protected LinkedHashMap<String,String> getForwardedParameters(HttpServletRequest req){
        LinkedHashMap<String,String> params=new LinkedHashMap<String, String>();
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

    protected Template getTemplate(HttpServletRequest req, HttpMethod method, ActionException exception) {
        return templateManager.getTemplate(templateKey, null);
    }

    protected HashMap<String, Object> getTemplateContext(Template template, HttpServletRequest req, HttpMethod method, User user, ActionException ae) {
        HashMap<String,Object> context=getStaticTemplateContext();
        context.put(requestContextKey,req);
        context.putAll(template.getTemplateContext());
        context.putAll(getForwardedParameters(req));
        context.put("user",user);
        
        String errorMessage="Unspecified error";
        String trace=null;
        if(ae!=null) {
            Throwable e=ae;
            while(e!=null){
                String message=e.getMessage();
                if(message!=null && !(e.getCause()!=null && message.equals(e.getCause().toString())) ) {
                    errorMessage=message;
                    break;
                }
                e=e.getCause();
            }
            
            StringWriter sw=new StringWriter();
            PrintWriter pw=new PrintWriter(sw);
            ae.printStackTrace(pw);
            pw.flush();
            trace=sw.toString();
        }
        context.put("exception",ae);
        context.put("errorMessage",errorMessage);
        context.put("errorTrace",trace);
        
        return context;
    }
    
    
    
    @Override
    public boolean handleActionException(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, User user, ActionException ae) throws ServletException, IOException, ActionException  {
        Template template=getTemplate(req, method, ae);
        if(template==null) return false;
        HashMap<String,Object> context=getTemplateContext(template, req, method, user, ae);
        if(context==null) return false;
        
        String contentType=template.getMimeType();
        String encoding=template.getEncoding();
        if(contentType!=null) resp.setContentType(contentType);
        if(encoding!=null) resp.setCharacterEncoding(encoding);
        
        template.process(context, resp.getOutputStream());
        return true;
    }

    
}
