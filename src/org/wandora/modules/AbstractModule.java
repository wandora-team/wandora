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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;

/**
 * A basic implementation of the Module interface. Implements all the methods
 * but doesn't do anything useful on its own. The base implementation holds the
 * initialisation and running states and does the bare minimum in the init, 
 * getDependencies, start and stop methods. Typically you will want to override
 * at least some of them to add your own logic but still call the base
 * implementation too.
 * 
 * @author olli
 */
public abstract class AbstractModule implements Module {

    protected boolean isInitialized;
    protected boolean isRunning;
    protected boolean autoStart;

    /**
     * A logging module that is set in the requireLogging method. See
     * its documentation for more details.
     */
    protected LoggingModule loggingModule;
    /**
     * A Log implementation that is set in the require Logging method. See
     * its documentation for more details.
     */
    protected Log logging;

    /**
     * The module manager that handles this module. First set in the init
     * method.
     */
    protected ModuleManager moduleManager;
    
    public AbstractModule(){
        isInitialized=false;
        isRunning=false;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        this.moduleManager=manager;
        isInitialized=true;
    }

/*    
    @Override
    public HashMap<String,Object> saveOptions(){
        HashMap<String,Object> ret=new HashMap<String,Object>();
        return ret;
    }
*/

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        return new ArrayList<Module>();
    }

    /**
     * <p>
     * A helper method that adds logging into dependencies. Typically you
     * will call this in your overridden getDependencies method. After that you 
     * can use the logging facilities of the logging instance variable.
     * AbstractModule does not automatically require logging, but it provides
     * some functionality for the typical case where you do need logging.
     * </p>
     * <p>
     * Note that typically modules are not usable before they have been started,
     * and the logging module may only have been started just before calling
     * start of this module. As such, the module would be unusable before that, in
     * particular in init and getDependencies methods. However, logging modules
     * should be built in such a way that they are usable immediately
     * after having been initialised. In other words, you can assume that you 
     * can use it as soon as you have a reference to it. You may also call
     * requireLogging already in init method to have logging available there.
     * You should still also add it to the dependencies later in getDependencies.
     * </p>
     * 
     * @param manager
     * @param dependencies
     * @return
     * @throws ModuleException 
     */
    protected Collection<Module> requireLogging(ModuleManager manager,Collection<Module> dependencies) throws ModuleException {
        if(dependencies==null) dependencies=new ArrayList<Module>();
        loggingModule=manager.requireModule(this,LoggingModule.class, dependencies);
        logging=loggingModule.getLog(this);
        return dependencies;
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        isRunning=true;
    }

    @Override
    public void stop(ModuleManager manager) {
        isRunning=false;
    }
    

    /**
     * Makes a string representation of this module using ModuleManager.moduleToString.
     * @return A string representation of this module.
     */
    @Override
    public String toString(){
        return moduleManager.moduleToString(this);
    }

}
