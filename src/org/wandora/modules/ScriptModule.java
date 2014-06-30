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
package org.wandora.modules;

import java.util.HashMap;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.wandora.utils.ScriptManager;

/**
 * <p>
 * A base module implementation that adds some scripting functionality
 * into the module. Most of the other modules derive from this instead of from
 * AbstractModule directly, and it may be a good idea for you to do so as well,
 * even if you don't immediately need any of the scripting functions. By deriving
 * from ScriptModule, you can later add scripts in the configuration file that
 * are executed when your module is initialised, started or stopped.
 * </p>
 * <p>
 * To add scripts in the configuration file, define them in parameters with names
 * startScript, stopScript or initScript. The scripts will then be ran at module
 * start, stop or init, respectively. You may also specify the script engine to
 * use with scriptEngine parameter, otherwise the default engine will be used,
 * which is probably Mozilla Rhino ECMA Script.
 * </p>
 * <p>
 * Some variables are added to the script's environment before it's executed.
 * moduleManager will contain the module manager, scriptModule contains this
 * module object and persistentObjects contains a HashMap where you can
 * store any other objects you would like to refer to later. The persistentObjects
 * is created at module init and will not be cleared during the lifetime of the
 * module. However, the objects stored in there are not saved to disk or any
 * other persistent storage, they are persistent only during the lifetime of the
 * module object.
 * </p>
 * 
 * @author olli
 */

public class ScriptModule extends AbstractModule {

    protected String engine;
    protected String startScript;
    protected String stopScript;
    protected String initScript;
    protected ScriptManager scriptManager;
    protected HashMap<String,Object> persistentObjects;
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o;
        o=settings.get("scriptEngine");
        if(o!=null) engine=o.toString();
        
        o=settings.get("startScript");
        if(o!=null) startScript=o.toString();
        
        o=settings.get("stopScript");
        if(o!=null) stopScript=o.toString();

        o=settings.get("initScript");
        if(o!=null) initScript=o.toString();
        
        persistentObjects=new HashMap<String,Object>();
        
        if(initScript!=null){
            try{
                ScriptEngine scriptEngine=getScriptEngine();
                scriptEngine.put("settings",settings);
                scriptManager.executeScript(initScript, scriptEngine);
            }catch(ScriptException se){
                throw new ModuleException(se);
            }            
        }
        
        super.init(manager, settings);
    }
    
    protected ScriptEngine getScriptEngine(){
        if(scriptManager==null) scriptManager=new ScriptManager();
        ScriptEngine scriptEngine=scriptManager.getScriptEngine(engine==null?ScriptManager.getDefaultScriptEngine():engine);
        scriptEngine.put("moduleManager", moduleManager);
        scriptEngine.put("persistentObjects", persistentObjects);
        scriptEngine.put("scriptModule", this);
        return scriptEngine;
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if(startScript!=null){
            try{
                scriptManager.executeScript(startScript, getScriptEngine());
            }catch(ScriptException se){
                throw new ModuleException(se);
            }
        }
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        if(stopScript!=null){
            try{
                scriptManager.executeScript(stopScript, getScriptEngine());
            }catch(ScriptException se){
                logging.warn(se);
            }
        }
        super.stop(manager);
    }
    
}
