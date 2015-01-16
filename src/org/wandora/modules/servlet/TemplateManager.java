/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;

/**
 * <p>
 * A module where all Templates register themselves so that Actions,
 * or other modules too, can find them. The other modules refer to the
 * templates with their template key, not the module name. In addition, a 
 * template version may also be specified. The combination of key and version
 * has to be unique, there can be several templates with same key but different
 * versions. While the key must match exactly to the requested template key,
 * version matching is not exact. Instead a best match is used. See getTemplate
 * method documentation for more details.
 * </p>
 * <p>
 * Manager specific default context variables can be specified in the initialisation
 * parameters using the parameter name prefix "context.". The rest of the parameter
 * is the context variable name. Note that template specific parameters can similarly
 * be defined in the Template modules too (assuming they derive from the AbstractTemplate
 * class).
 * </p>
 * <p>
 * A templatePath initialisation parameter can be given, which should point to 
 * a directory containing template files. Templates are not required to use this,
 * in fact, templates are not required to access any files at all, but it is
 * good practice to respect it. However, note that VelocityTemplates in particular 
 * do not use this. Instead the template directory for velocity templates is defined
 * in the VelocityEngineModule.
 * </p>
 * <p>
 * A TemplateContextProvider module can be used to provide the default context.
 * This is an optional dependency and will be used if it is found in the module
 * manager but not having one is not an error. You can set the ignoreContextProvider
 * initialisation parameter to true to override this behaviour. Both a context
 * provider and context initialisation parameters in this class can be used at 
 * the same time. These contexts are merged into one default context. In case
 * of context keys clashing, initialisation parameters of this module override
 * the values of the context provider.
 * </p>
 * 
 * @author olli
 */

public class TemplateManager extends AbstractModule {
    
    protected final HashMap<String,ArrayList<Template>> templateMap=new HashMap<String,ArrayList<Template>>();
    
    protected final HashMap<String,Object> defaultContext=new HashMap<String,Object>();
    
    protected TemplateContextProvider contextProvider=null;
    protected boolean ignoreContextProvider=false;

    protected String templatePath="";
    
    /**
     * Gets the manager specific default context which should be used
     * as a basis for template contexts.
     * @return The manager specific default template context.
     */
    public HashMap<String,Object> getDefaultContext(){
        if(contextProvider!=null && !ignoreContextProvider){
            HashMap<String,Object> ret=new HashMap<String,Object>();
            ret.putAll(contextProvider.getTemplateBaseContext());
            ret.putAll(defaultContext);
            return ret;
        }
        else return defaultContext;
    }
    
    /**
     * Puts an object in the manager specific default template context.
     * @param key The key of the template variable.
     * @param o The value of the template variable.
     */
    public void putStaticContext(String key,Object o){
        defaultContext.put(key,o);
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("context.")){
                defaultContext.put(key.substring("context.".length()),e.getValue());
            }
        }
        
        Object o=settings.get("templatePath");
        if(o!=null) {
            templatePath=o.toString();
            if(templatePath.length()>0 && !templatePath.endsWith(File.separator)) templatePath+=File.separator;
        }
        
        o=settings.get("ignoreContextProvider");
        if(o!=null) ignoreContextProvider=Boolean.parseBoolean(o.toString().trim());
        
    }

    @Override
    public void stop(ModuleManager manager) {
        contextProvider=null;
        super.stop(manager);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        contextProvider=manager.findModule(this, TemplateContextProvider.class);
        super.start(manager);
    }
    
    
    
    /**
     * Gets the specified path for template files.
     * @return The template path.
     */
    public String getTemplatePath(){
        return templatePath;
    }
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.optionalModule(this, TemplateContextProvider.class, deps);
        requireLogging(manager, deps);
        return deps;
    }

    /**
     * Gets the template using the specified key and version. The key
     * must match exactly with the key the template was registered with but
     * version matching is fuzzy. If multiple versions of the same template
     * have been registered, the one with the most matching characters from the
     * start is used. You can always use null for version if you don't use
     * different template versions.
     * 
     * @param key The key of the template to get.
     * @param version The version of the template, or null if not applicable.
     * @return The template that should be used, or null if no suitable template was found.
     */
    public Template getTemplate(String key,String version){
        synchronized(templateMap){
            ArrayList<Template> templates=templateMap.get(key);
            if(templates==null || templates.isEmpty()) return null;
            if(templates.size()==1) return templates.get(0);
            
            Template best=null;
            int bestScore=-1;
            if(version==null) version="";
            version=version.toLowerCase();
            
            for(Template t : templates){
                String tVersion=t.getVersion();
                if(tVersion==null) tVersion="";
                tVersion=tVersion.toLowerCase();
                
                int i=0;
                for(i=0;i<tVersion.length() && i<version.length();i++){
                    if(tVersion.charAt(i)!=version.charAt(i)){
                        break;
                    }
                }
                if(i>bestScore){
                    bestScore=i;
                    best=t;
                }
            }
            return best;
        }
    }
    
    /**
     * Register a template for this manager. After this, the manager may
     * return the template when getTemplate is called.
     * 
     * @param template The template to register.
     */
    public void registerTemplate(Template template){
        synchronized(templateMap){
            String key=template.getKey();
            if(key==null) {
                logging.warn("Tried to register template with null key");
                return;
            }
            ArrayList<Template> templates=templateMap.get(key);
            if(templates==null){
                templates=new ArrayList<Template>();
                templateMap.put(key,templates);
            }
            templates.add(template);
        }
    }
    
    /**
     * Unregisters a template. After this, the template will no longer be 
     * returned as a result of getTemplate.
     * @param template The template to unregister.
     */
    public void unregisterTemplate(Template template){
        synchronized(templateMap){
            String key=template.getKey();
            if(key==null) return;
            ArrayList<Template> templates=templateMap.get(key);
            if(templates==null) return;
            templates.remove(template);
            if(templates.isEmpty()) templateMap.remove(key);
        }
    }
}
