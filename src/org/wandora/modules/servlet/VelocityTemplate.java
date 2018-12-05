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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.velocityhelpers.InstanceMaker;
import org.wandora.modules.velocityhelpers.URLEncoder;

/**
 * <p>
 * A Template implementation using the Apache Velocity templating engine.
 * The engine itself is required as a separate module, VelocityEngineModule. 
 * The base functionality is defined in the AbstractTemplate base class and only
 * the actual template processing is done here.
 * </p>
 * <p>
 * A few items are automatically added in the template context, in addition
 * to the ones specified in initialisation parameters, as described in
 * AbstractTemplate documentation. These are used to overcome some of the
 * limitations in Velocity template language. The urlencoder parameter contains
 * a URLEncoder object which can help with encoding values used in URLs.
 * listmaker variable contains an InstanceMaker that can make ArrayLists, it is
 * difficult to create new lists in Velocity without this. These helper classes
 * are defined in org.wandora.modules.velocityhelpers along with other similar
 * helpers, which you may want to add to the context manually.
 * </p>
 * 
 * @author olli
 */


public class VelocityTemplate extends AbstractTemplate {

    protected org.apache.velocity.Template vTemplate;
    protected VelocityEngineModule engineModule;
    
    protected synchronized VelocityEngine getVelocityEngine(){
        Properties props=new Properties();
        props.setProperty("webapp.resource.loader.path", templateManager.getTemplatePath());
        VelocityEngine engine = new VelocityEngine(props);
        return engine;
    }
    
    @Override
    protected String getFullTemplatePath(){
        // don't add the template path of the manager as that's added to the velocity engine search path already
        return templateFile;
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.optionalModules(VelocityEngineModule.class, deps);
        return deps;
    }
    
    @Override
    public void start(ModuleManager manager) throws ModuleException {
        engineModule=manager.findModule(this,VelocityEngineModule.class);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        engineModule=null;
        vTemplate=null;
        super.stop(manager);
    }

    @Override
    public Map<String, Object> getTemplateContext() {
        Map<String,Object> context=super.getTemplateContext();
        if(!context.containsKey("urlencoder")) context.put("urlencoder",new URLEncoder());
        if(!context.containsKey("listmaker")) context.put("listmaker",new InstanceMaker(java.util.ArrayList.class));
        return context;
    }

    
    
    @Override
    public void process(Map<String, Object> params, OutputStream output) {
        org.apache.velocity.Template vTemplate=this.vTemplate;
        if(vTemplate==null || !templateCaching){
            String templatePath=getFullTemplatePath();
            try {
                if(engineModule!=null) {
                    vTemplate=engineModule.getEngine().getTemplate(templatePath,templateEncoding);
                }
                else {
                    vTemplate=Velocity.getTemplate(templatePath,templateEncoding);
                }
            }catch(ResourceNotFoundException rnfe){
                logging.warn("Velocity template "+templatePath+" not found.",rnfe);
            }catch(ParseErrorException pee){
                logging.warn("Parse error in velocity template "+templatePath+": "+pee.getMessage());                        
            }catch(Exception ex){
                logging.warn("Couldn't load velocity template "+templatePath+": "+ex.getMessage());
                logging.debug(ex);
            }
            this.vTemplate=vTemplate;
        }
        VelocityContext context=new VelocityContext();
        Iterator iter=params.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            context.put(e.getKey().toString(),e.getValue());
        }

        try{
            Writer writer=new BufferedWriter(new OutputStreamWriter(output,getEncoding()));
            vTemplate.merge(context, writer);
            writer.flush();
        }catch(Throwable ex){
            logging.warn("Couldn't merge template "+getFullTemplatePath(),ex);            
            Throwable cause=ex.getCause();
            if(cause!=null){
                logging.warn("Cause",cause);
            }
        }
    }
    
}
