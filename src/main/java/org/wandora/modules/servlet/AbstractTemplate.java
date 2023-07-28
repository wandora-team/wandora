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

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;

/**
 * <p>
 * A base implementation for templates. Performs common actions to most
 * templates. Among these, parses from the initialisation parameters values for
 * many of the template variables, these are defined in variables named
 * templateKey, templateVersion, templateMimeType and templateEncoding. Also
 * templateCaching and templateFile which are not in the Template interface but
 * are common template features.
 * </p>
 * <p>
 * A default template context can also be specified in the initialisation
 * parameters using the parameter name prefix "context.". The rest of parameter
 * name is used as the variable name in the context.
 * </p>
 * <p>
 * This base implementation automatically registers the template to a TemplateManager
 * at module start, as all templates should do to be usable.
 * </p>
 * 
 * @author olli
 */


public abstract class AbstractTemplate extends AbstractModule implements Template {

    protected TemplateManager templateManager;
    
    protected String templateKey=null;
    protected String templateVersion="";
    protected String templateMime="text/html";
    protected String templateEncoding="UTF-8";
    protected boolean templateCaching=true;
    
    protected String templateFile=null;
    
    protected HashMap<String,Object> templateContext;
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        
        Object o;
        o=settings.get("templateKey");
        if(o!=null) templateKey=o.toString();
        
        o=settings.get("templateVersion");
        if(o!=null) templateVersion=o.toString();
        
        o=settings.get("templateMimeType");
        if(o!=null) templateMime=o.toString();
        
        o=settings.get("templateEncoding");
        if(o!=null) templateEncoding=o.toString();
        
        o=settings.get("templateCaching");
        if(o!=null) templateCaching=Boolean.parseBoolean(o.toString());
        
        o=settings.get("templateFile");
        if(o!=null) templateFile=o.toString();
        
        templateContext=new HashMap<String,Object>();
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("context.")){
                templateContext.put(key.substring("context.".length()),e.getValue());                
            }
        }
    }
    
    @Override
    public Map<String,Object> getTemplateContext(){
        return templateContext;
    }

    protected String getFullTemplatePath(){
        return templateManager.getTemplatePath()+templateFile;
    }
            
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.requireModule(this,TemplateManager.class, deps);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        templateManager=manager.findModule(this,TemplateManager.class);
        templateManager.registerTemplate(this);
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        if(this.templateManager!=null) templateManager.unregisterTemplate(this);
        templateManager=null;
        super.stop(manager);
    }

    
    
    @Override
    public String getKey() {
        return templateKey;
    }

    @Override
    public String getVersion() {
        return templateVersion;
    }

    @Override
    public String getMimeType() {
        return templateMime;
    }

    @Override
    public String getEncoding() {
        return templateEncoding;
    }

    @Override
    public abstract void process(Map<String, Object> params, OutputStream output);
    
}
