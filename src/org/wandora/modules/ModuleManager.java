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
import org.wandora.utils.ListenerList;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.logging.Log;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wandora.utils.ScriptManager;
import org.wandora.utils.Tuples;
import org.wandora.utils.Tuples.T3;

/**
 * <p>
 * Main manager for the modules framework. ModuleManager does all the work
 * connecting modules with each other, solving dependencies, loading configuration
 * and setting up the whole framework.
 * </p>
 * <p>
 * To use this, just make a new instance of ModuleManager and then either load
 * a configuration file or add some modules manually, initialise them, and
 * finally start the modules. You can also first add some modules manually and
 * then load the rest with a configuration file. You typically do this to
 * establish some kind of an attachment point for the modules or to add a
 * logging module or some such beforehand.
 * </p>
 * <p>
 * You may also add variables before loading the configuration file, these can
 * then be used in the configuration file. For example, if you know something
 * about the environment, such as the network port the service is running, the
 * working directory or something similar, you could add these as variables.
 * Then the configuration file can use them instead of having them hardcoded.
 * </p>
 * <p>
 * After adding the modules, you have to initialise them. Initialisation of
 * modules needs a map of module properties. If you read the modules from
 * a configuration file, the properties will also have been read and are stored
 * for later use. When you call initAllModules, all modules that are not yet
 * initialised will be initialised with the parameters read from the configuration
 * file. Prior to this, you can initialise modules manually if you wish.
 * </p>
 * <p>
 * After initialisation, you need to start the modules. Typically you will call
 * autostartModules, which will start all modules that were configured to be
 * started automatically. Alternatively, you can also use startAllModules which
 * will start every single module that hasn't been started yet. Or you can
 * always start modules individually too with startModule or
 * startModuleWithDependencies. startModule will throw an exception if the
 * dependencies haven't been started yet whereas the other method simply starts
 * them too.
 * </p>
 * http://wandora.orgwiki/Wandora_modules_framework for additional
 * documentation about the modules framework.</p>
 *
 * @author olli
 */
public class ModuleManager {
    /**
     * A list containing all the modules this manager takes care of.
     */
    protected ArrayList<Module> modules;
    
    /**
     * Initialisation parameters that will be passed to module init method, read
     * from the config file.
     */
    protected HashMap<Module,HashMap<String,Object>> moduleParams;

    /**
     * Other settings about modules besides the parameters. Includes things
     * like auto restart, priority, name, and explicit services used.
     */
    protected HashMap<Module,ModuleSettings> moduleSettings;
    
    /**
     * Logger used by the manager. Can be set with setLog, otherwise it's
     * automatically captured by addModule when a logging module is first added.
     */
    protected Log log;

    protected ListenerList<ModuleListener> moduleListeners;
    
    /**
     * Modules that are used by some other running module are put here so that
     * they cannot be stopped before the depending module is stopped.
     */
    protected HashMap<Module,ArrayList<Module>> isRequiredBy;
    
    /**
     * Variables used in the configuration file.
     */
    protected LinkedHashMap<String,String> variables;

    public ModuleManager(){
        modules=new ArrayList<Module>();
        moduleParams=new HashMap<Module,HashMap<String,Object>>();
        moduleListeners=new ListenerList<ModuleListener>(ModuleListener.class);
        moduleSettings=new HashMap<Module,ModuleSettings>();
        isRequiredBy=new HashMap<Module,ArrayList<Module>>();
        variables=new LinkedHashMap<String,String>();
    }

    /**
     * Gets module settings. The settings include everything else about the
     * module except the implementation specific parameters. For example,
     * module name, autostart status, priority and explicitly specified
     * dependencies.
     */
    public ModuleSettings getModuleSettings(Module m){
        return moduleSettings.get(m);
    }
    /**
     * Checks if module is to be started automatically.
     */
    public boolean isModuleAutoStart(Module m){
        return moduleSettings.get(m).autoStart;
    }
    /**
     * Gets the module name.
     */
    public String getModuleName(Module m){
        return moduleSettings.get(m).name;
    }
    
    /**
     * Add a listener that will be notified about changes in modules in this manager.
     */
    public void addModuleListener(ModuleListener l){
        moduleListeners.addListener(l);
    }
    /**
     * Removes a previously registered module listener.
     */
    public void removeModuleListener(ModuleListener l){
        moduleListeners.removeListener(l);
    }

    /**
     * Sets the logging for the module manager itself. Modules will typically
     * get a logging module as a dependency instead of using the manager logger
     * directly. If no manager logging is set when a logging module is first
     * added, that module will automatically be used for manager logging as well.
     */
    public void setLogging(Log log){
        this.log=log;
    }
    
    /**
     * Returns the logger used for manager logging. Modules should not use
     * this logger, instead they should depend on a logging module and use the
     * logging module for logging.
     */
    public Log getLogger(){
        return this.log;
    }

    /**
     * Returns a list of all modules added to this manager.
     */
    public synchronized ArrayList<Module> getAllModules(){
        ArrayList<Module> ret=new ArrayList<Module>();
        ret.addAll(modules);
        return ret;
    }

    /**
     * Finds a module that is of the specified class or one extending it.
     * Typically you look for modules with an interface and will then get a module
     * that implements the interface. In case several such modules exist, the one
     * with the highest priority is selected. In case there is a tie, one of them is
     * arbitrarily selected.
     * 
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @return The module found or null if no such module exists.
     */
    public synchronized <A extends Module> A findModule(Class<A> cls){
        return findModule((String)null,cls);
    }
    /**
     * Finds a module that is of the specified class or one extending it.
     * Typically you look for modules with an interface and will then get a module
     * that implements the interface. This method takes a module as context, the
     * context is the module requesting another module. It may affect what module
     * is returned. With the default implementation this will happen if module
     * dependencies were explicitly defined in the config file. But in future,
     * other selection methods may be added which also depend on the requesting
     * module.
     * 
     * @param context The module which is requesting the module. This may affect 
     *                 what module is returned.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @return The module found or null if no such module exists.
     */
    public synchronized <A extends Module> A findModule(Module context,Class<A> cls){
        ModuleSettings settings=getModuleSettings(context);
        return findModule(context,settings.serviceNames.get(cls),cls);
    }
    /**
     * Finds a module that is of the specified class or one extending it.
     * Typically you look for modules with an interface and will then get a module
     * that implements the interface. This method takes the name of the module
     * requested. No other module will be returned except the module named such.
     * 
     * @param context The module which is requesting the module. This may affect 
     *                 what module is returned.
     * @param instanceName The instance name of the requested module.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @return The module found or null if no such module exists.
     * @return 
     */
    public synchronized <A extends Module> A findModule(Module context,String instanceName,Class<A> cls){
        A ret=null;
        int best=-1; // do not select automatically anything with a lower priority than 0
        if(instanceName!=null) best=Integer.MIN_VALUE; // but with a specified name do
        for(Module m : modules){
            if(context!=null && m==context) continue;
            if(cls.isAssignableFrom(m.getClass())){
                String name=getModuleName(m);
                if(instanceName==null || (name!=null && instanceName.equals(name)) ){
                    ModuleSettings settings=getModuleSettings(m);
                    if(settings.servicePriority>best) {
                        best=settings.servicePriority;
                        ret=(A)m;
                    }
                }
            }
        }
        return ret;
    }
    /**
     * Finds a module that is of the specified class or one extending it.
     * Typically you look for modules with an interface and will then get a module
     * that implements the interface. This method takes the name of the module
     * requested. No other module will be returned except the module named such.
     * 
     * @param instanceName The instance name of the requested module.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @return The module found or null if no such module exists.
     */
    public synchronized <A extends Module> A findModule(String instanceName,Class<A> cls){
        return findModule(null,instanceName,cls);
    }
    /**
     * Finds all modules that are of the specified class or one extending it.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @return The found modules or an empty list if no such modules exist.
     */
    public synchronized <A extends Module> ArrayList<A> findModules(Class<A> cls){
        ArrayList<A> ret=new ArrayList<A>();
        for(Module m : modules){
            if(cls.isAssignableFrom(m.getClass())){
                ret.add((A)m);
            }
        }
        Collections.sort(ret, new Comparator<A>(){
            @Override
            public int compare(A o1, A o2) {
                ModuleSettings s1=getModuleSettings(o1);
                ModuleSettings s2=getModuleSettings(o2);
                return s2.servicePriority-s1.servicePriority;
            }
        });
        return ret;
    }

    /**
     * Find a required module and throw a MissingDependencyException if
     * one cannot be found. This method is usually used in the getDependencies
     * method of modules. If a dependency isn't found, it'll automatically throw
     * a suitable exception. If the module i found, it will be added to the
     * provided collection which getDependencies needs to return later.
     * 
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module.
     * @throws MissingDependencyException
     */
    public synchronized <A extends Module> A requireModule(Class<A> cls,Collection<Module> dependencies) throws MissingDependencyException {
        return requireModule((String)null,cls,dependencies);
    }
    /**
     * Find a required module and throw a MissingDependencyException if
     * one cannot be found. This method is usually used in the getDependencies
     * method of modules. If a dependency isn't found, it'll automatically throw
     * a suitable exception. If the module i found, it will be added to the
     * provided collection which getDependencies needs to return later. This 
     * method takes a module as context, the context is the module requesting
     * another module. It may affect what module
     * is returned. With the default implementation this will happen if module
     * dependencies were explicitly defined in the config file. But in future,
     * other selection methods may be added which also depend on the requesting
     * module.
     * 
     * @param context The module which is requesting the module. This may affect 
     *                 what module is returned.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module.
     * @throws MissingDependencyException
     */
    public synchronized <A extends Module> A requireModule(Module context,Class<A> cls,Collection<Module> dependencies) throws MissingDependencyException {
        ModuleSettings settings=getModuleSettings(context);
        return requireModule(context,settings.serviceNames.get(cls),cls,dependencies);
    }
    /**
     * Find a required module and throw a MissingDependencyException if
     * one cannot be found. This method is usually used in the getDependencies
     * method of modules. If a dependency isn't found, it'll automatically throw
     * a suitable exception. If the module i found, it will be added to the
     * provided collection which getDependencies needs to return later. This
     * method takes the name of the module
     * requested. No other module will be returned except the module named such.
     * 
     * @param context The module which is requesting the module. This may affect 
     *                 what module is returned.
     * @param instanceName The instance name of the requested module.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module.
     * @throws MissingDependencyException
     */
    public synchronized <A extends Module> A requireModule(Module context,String instanceName, Class<A> cls,Collection<Module> dependencies) throws MissingDependencyException {
        Module m=findModule(context,instanceName,cls);
        if(m==null) throw new MissingDependencyException(cls,instanceName);
        else {
            if(!dependencies.contains(m)) dependencies.add(m);
            return (A)m;
        }        
    }
    /**
     * Find a required module and throw a MissingDependencyException if
     * one cannot be found. This method is usually used in the getDependencies
     * method of modules. If a dependency isn't found, it'll automatically throw
     * a suitable exception. If the module i found, it will be added to the
     * provided collection which getDependencies needs to return later. This
     * method takes the name of the module
     * requested. No other module will be returned except the module named such.
     * 
     * @param instanceName The instance name of the requested module.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module.
     * @throws MissingDependencyException
     */
    public synchronized <A extends Module> A requireModule(String instanceName, Class<A> cls,Collection<Module> dependencies) throws MissingDependencyException {
        return requireModule((Module)null,instanceName,cls,dependencies);
    }

    /**
     * Look for a module that will be used if found but is not required.
     * Unlike requireModule, no exception is thrown if the module is not found.
     * If it is found, then this method behaves like requireModule and the
     * module is added in the provided dependencies collection as well as returned
     * from the method. 
     * 
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module or null if one cannot be found.
     */
    public synchronized <A extends Module> A optionalModule(Class<A> cls,Collection<Module> dependencies) {
        return optionalModule((String)null,cls,dependencies);
    }
    /**
     * Look for a module that will be used if found but is not required.
     * Unlike requireModule, no exception is thrown if the module is not found.
     * If it is found, then this method behaves like requireModule and the
     * module is added in the provided dependencies collection as well as returned
     * from the method. This method takes a module as context, the context is
     * the module requesting another module. It may affect what module
     * is returned. With the default implementation this will happen if module
     * dependencies were explicitly defined in the config file. But in future,
     * other selection methods may be added which also depend on the requesting
     * module.
     * 
     * @param context The module which is requesting the module. This may affect 
     *                 what module is returned.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module or null if one cannot be found.
     */
    public synchronized <A extends Module> A optionalModule(Module context,Class<A> cls,Collection<Module> dependencies) {
        ModuleSettings settings=getModuleSettings(context);
        return optionalModule(context,settings.serviceNames.get(cls),cls,dependencies);
    }
    /**
     * Look for a module that will be used if found but is not required.
     * Unlike requireModule, no exception is thrown if the module is not found.
     * If it is found, then this method behaves like requireModule and the
     * module is added in the provided dependencies collection as well as returned
     * from the method. This method takes the name of the module
     * requested. No other module will be returned except the module named such.
     * 
     * @param context The module which is requesting the module. This may affect 
     *                 what module is returned.
     * @param instanceName The instance name of the requested module.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module or null if one cannot be found.
     */
    public synchronized <A extends Module> A optionalModule(Module context,String instanceName,Class<A> cls,Collection<Module> dependencies) {
        Module m=findModule(context,instanceName, cls);
        if(m==null) return null;
        else {
            if(!dependencies.contains(m)) dependencies.add(m);
            return (A)m;
        }
    }
    /**
     * Look for a module that will be used if found but is not required.
     * Unlike requireModule, no exception is thrown if the module is not found.
     * If it is found, then this method behaves like requireModule and the
     * module is added in the provided dependencies collection as well as returned
     * from the method. This method takes the name of the module
     * requested. No other module will be returned except the module named such.
     * 
     * @param instanceName The instance name of the requested module.
     * @param cls The class or interface that the module has to implement or extend or
     *             be a direct instance of.
     * @param dependencies The collection where all dependencies are gathered and
     *                      where the found module is added.
     * @return The found module or null if one cannot be found.
     */
    public synchronized <A extends Module> A optionalModule(String instanceName,Class<A> cls,Collection<Module> dependencies) {
        return optionalModule((Module)null,instanceName,cls,dependencies);
    }
    public synchronized <A extends Module> ArrayList<A> optionalModules(Class<A> cls,Collection<Module> dependencies) {
        ArrayList<A> ms=findModules(cls);
        for(A m : ms){
            if(!dependencies.contains(m)) dependencies.add(m);
        }
        return ms;
    }

    
    /**
     * Adds a module to this manager with default settings and no module
     * parameters.
     * @param module The module to be added.
     */
    public void addModule(Module module){
        addModule(module,new HashMap<String,Object>(),new ModuleSettings());
    }
    /**
     * Adds a module to this manager with default settings and no module
     * parameters but with the provided name.
     * @param module The module to be added.
     * @param name The module name;
     */
    public void addModule(Module module,String name){
        ModuleSettings settings=new ModuleSettings();
        settings.name=name;
        addModule(module,new HashMap<String,Object>(),settings);
    }
    
    /**
     * Adds a module to this manager with the given module settings but
     * empty module parameters. Settings are general module settings, like
     * module name, priority etc. whereas module parameters are module options
     * specific to the implementation and used only by the module, not the module
     * manager.
     * 
     * @param module The module to be added.
     * @param settings The settings for the module.
     */
    public void addModule(Module module,ModuleSettings settings){
        addModule(module,new HashMap<String,Object>(),settings);
    }
    /**
     * Adds a module to this manager with the given module parameters and
     * default settings. Settings are general module settings, like
     * module name, priority etc. whereas module parameters are module options
     * specific to the implementation and used only by the module, not the module
     * manager.
     * 
     * @param module The module to be added.
     * @param params The module parameters.
     */
    public synchronized void addModule(Module module,HashMap<String,Object> params){
        addModule(module,params,new ModuleSettings());
    }

    /**
     * Adds a module to this manager with the given module parameters and
     * settings. Settings are general module settings, like
     * module name, priority etc. whereas module parameters are module options
     * specific to the implementation and used only by the module, not the module
     * manager.
     * 
     * @param module The module to be added.
     * @param params The module parameters.
     * @param settings  The module settings.
     */
    public synchronized void addModule(Module module,HashMap<String,Object> params,ModuleSettings settings){
        if(log==null && module instanceof LoggingModule){
            try{
                module.init(this, params);
            }catch(ModuleException me){
                me.printStackTrace();
            }
            log=((LoggingModule)module).getLog("ModuleManager");
        }
        modules.add(module);
        moduleParams.put(module,params);
        moduleSettings.put(module, settings);
        if(log!=null) log.info("Adding module "+moduleToString(module));
    }

    /**
     * Removes a module from the manager. Tries to stop the module first if
     * it is running. This can potentially lead to different kinds of exceptions,
     * notably a ModuleInUseException if some other module is using the module
     * being removed.
     * 
     * @param module The module to be removed.
     * @throws ModuleInUseException 
     */
    public synchronized void removeModule(Module module) throws ModuleException {
        if(log!=null) log.info("Removing module "+moduleToString(module));
        stopModule(module);
        modules.remove(module);
        moduleParams.remove(module);
        moduleSettings.remove(module);
    }

    /**
     * Creates a string representation of a module class. Mostly intended for
     * debugging and logging.
     * 
     * @param cls The class of the module.
     * @param instanceName The instance name of the module or null.
     * @return 
     */
    public String moduleToString(Class<?> cls,String instanceName){
        return cls.getSimpleName()+(instanceName==null?"":"("+instanceName+")");
    }

    /**
     * Creates a string representation of a module. Mostly intended for
     * debugging and logging.
     * 
     * @param module The module.
     * @return 
     */
    public String moduleToString(Module module){
        String name=getModuleName(module);
        return module.getClass().getSimpleName()+(name==null?"":"("+name+")");
    }

    /**
     * Initialises all modules that have not yet been initialised. If the
     * module information was read from a configuration file along with module
     * parameters, those parameters will be used for the initialisation.
     * 
     * @throws ModuleException 
     */
    public synchronized void initAllModules() throws ModuleException {
        for(Module m : modules){
            try{
                if(!m.isInitialized()) {
                    if(log!=null) log.info("Initializing module "+moduleToString(m));
                    m.init(this, moduleParams.get(m));
                }
            }
            catch(ModuleException me){
                if(log!=null) log.warn("Couldn't initialize module "+moduleToString(m),me);
            }
        }
    }

    /**
     * Starts all modules that have not yet been started and that were set to
     * start automatically in module settings. Will also start all the
     * dependencies of such modules, even if they themselves were not set to be
     * started automatically.
     */
    public void autostartModules() throws ModuleException {
        autostartModules(modules);
    }
    
    /**
     * In a given modules collection, starts all modules that have not
     * yet been started and that were set to start automatically in module
     * settings. Will also start all the
     * dependencies of such modules, even if they themselves were not set to be
     * started automatically.
     */
    public synchronized void autostartModules(Collection<Module> modules) throws ModuleException {
        initAllModules();
        for(Module m : modules){
            try{
                if(!m.isRunning()) {
                    if(isModuleAutoStart(m)){
                        startModuleWithDependencies(m);
                    }
                }
            }
            catch(ModuleException me){
                if(log!=null) log.warn("Couldn't autostart module "+moduleToString(m),me);
            }
        }
    }

    /**
     * Starts all modules that have not yet been started.
     */
    public synchronized void startAllModules() throws ModuleException {
        initAllModules();
        for(Module m : modules){
            try{
                if(!m.isRunning()) {
                    startModuleWithDependencies(m);
                }
            }
            catch(ModuleException me){
                if(log!=null) log.warn("Couldn't start module "+moduleToString(m),me);
            }
        }
    }

    /**
     * Stops all running modules.
     */
    public synchronized void stopAllModules(){
        for(Module m : modules){
            try{
                if(m.isRunning()) {
                    stopCascading(m);
                }
            }
            catch(ModuleException me){
                if(log!=null) log.warn("Couldn't stop module "+moduleToString(m),me);
            }
        }
    }

    /**
     * Stops a single module along with all modules that depend on it.
     * @param module The module to be stopped.
     */
    public synchronized void stopCascading(Module module) throws ModuleException {
        if(!module.isRunning()) return;
        ArrayList<Module> req=isRequiredBy.get(module);
        if(req!=null){
            while(!req.isEmpty()){ // do this way to avoid concurrent modification
                Module r=req.get(req.size()-1);
                stopCascading(r);
            }
        }
        stopModule(module);
    }

    /**
     * Stops a single module, but fails if other modules depend on it. A
     * ModuleInUseException will be throw if some other running module depends on
     * the module to be stopped.
     * 
     * @param module The module to be stopped.
     * @throws ModuleException 
     */
    public synchronized void stopModule(Module module) throws ModuleException {
        if(!module.isRunning()) return;
        ArrayList<Module> req=isRequiredBy.get(module);
        if(req==null || req.isEmpty()) {
        if(log!=null) log.info("Stopping module "+moduleToString(module));
            moduleListeners.fireEvent("moduleStopping", module);
            module.stop(this);
            if(!module.isRunning()){
                moduleListeners.fireEvent("moduleStopped", module);
                Collection<Module> deps=module.getDependencies(this);
                for(Module dep : deps){
                    ArrayList<Module> r=isRequiredBy.get(dep);
                    r.remove(module);
                }
            }
            else {
                if(log!=null) log.warn("Stopped module but isRunning is true "+moduleToString(module));
            }
        }
        else throw new ModuleInUseException(req);
    }

    /**
     * Starts a module as well as all modules it depends on.
     * 
     * @param module The module to be started.
     * @throws ModuleException 
     */
    public synchronized void startModuleWithDependencies(Module module) throws ModuleException {
        // TODO: Proper cyclic dependency detection so that we don't get a stack
        // overflow in such a case.
        if(!module.isInitialized()) module.init(this, moduleParams.get(module));
        if(module.isRunning()) return;
        Collection<Module> deps=module.getDependencies(this);
        for(Module dep : deps){
            if(!dep.isRunning()) startModuleWithDependencies(dep);
        }
        startModule(module);
    }
    /**
     * Starts a module. If any dependencies haven't yet been started, will throw
     * a MissingDependencyException. use startModuleWithDependencies to
     * automatically start all dependencies as well.
     * 
     * @param module The module to be started.
     * @throws ModuleException 
     */
    public synchronized void startModule(Module module) throws ModuleException {
        if(!module.isInitialized()) module.init(this, moduleParams.get(module));
        if(module.isRunning()) return;

        Collection<Module> deps=module.getDependencies(this);
        for(Module dep : deps){
            if(!dep.isRunning()) throw new MissingDependencyException(dep.getClass(),getModuleName(dep));
        }

        if(log!=null) log.info("Starting module "+moduleToString(module));
        moduleListeners.fireEvent("moduleStarting", module);
        module.start(this);
        if(module.isRunning()){
            moduleListeners.fireEvent("moduleStarted", module);
            for(Module dep : deps){
                ArrayList<Module> l=isRequiredBy.get(dep);
                if(l==null) {
                    l=new ArrayList<Module>();
                    isRequiredBy.put(dep,l);
                }
                l.add(module);
            }
        }
        else{
            if(log!=null) log.warn("Started module but isRunning is false "+moduleToString(module));
        }
    }
/*
    // Note that these two methods have several problems. They don't handle 
    // parameters that are not simple Strings. They also mess up ordering
    // of elements which may in some cases be significant. They are also
    // a bit outdated and don't handle variables and some other newer
    // things at all. 
    public static void writeXMLOptions(Writer out,HashMap<String,Object> options) throws IOException {
        for(Map.Entry<String,Object> e : options.entrySet()){
            Object valueO=e.getValue();
            if(valueO==null) continue;
            String value=valueO.toString();
            value=value.replace("&","&amp;");
            value=value.replace("<","&lt;");
            out.write("\t\t<param key=\""+e.getKey()+"\">"+value+"</param>\n");
        }
    }
    
    public void writeXMLOptionsFile(String writeFileName){
        try{
            OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(writeFileName),"UTF-8");

            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<options>\n");

            ArrayList<Module> modules=getAllModules();
            for(Module m : modules){
                out.write("\t<module class=\""+m.getClass().getName()+"\" autostart=\""+m.isAutoStart()+"\">\n");
                HashMap<String,Object> options=m.saveOptions();
                writeXMLOptions(out,options);
                out.write("\t</module>\n\n");
            }

            out.write("</options>");
            out.close();
        }catch(IOException ioe){
            if(log!=null) log.error("Unable to save options file.",ioe);
        }
    }
*/    
    
    /**
     * Parses a single param element and returns its value. Handles all the
     * different cases of how a param elements value can be determined.
     * 
     * @param e The xml param element.
     * @return The value of the parameter.
     */
    public Object parseXMLParamElement(Element e) throws ReflectiveOperationException, IllegalArgumentException, ScriptException {
        String instance=e.getAttribute("instance");
        if(instance!=null && instance.length()>0){
            Class cls=Class.forName(instance);
            HashMap<String,Object> params=parseXMLOptionsElement(e);
            if(!params.isEmpty()){
                Collection<Object> constructorParams=params.values();
                Constructor[] cs=cls.getConstructors();
                ConstructorLoop: for(int i=0;i<cs.length;i++){
                    Constructor c=cs[i];
                    Class[] paramTypes=c.getParameterTypes();
                    if(paramTypes.length!=constructorParams.size()) continue;
                    
                    int j=-1;
                    for(Object o : constructorParams){
                        j++;
                        if(o==null) {
                            if(!paramTypes[j].isPrimitive()) continue; 
                            else continue ConstructorLoop;
                        }
                        
                        if(paramTypes[j].isPrimitive()){
                            if(paramTypes[j]==int.class){ if(o.getClass()!=Integer.class) continue ConstructorLoop; }
                            else if(paramTypes[j]==long.class){ if(o.getClass()!=Long.class) continue ConstructorLoop; }
                            else if(paramTypes[j]==double.class){ if(o.getClass()!=Double.class) continue ConstructorLoop; }
                            else if(paramTypes[j]==float.class){ if(o.getClass()!=Float.class) continue ConstructorLoop; }
                            else if(paramTypes[j]==byte.class){ if(o.getClass()!=Byte.class) continue ConstructorLoop; }
                            else continue ConstructorLoop; //did we forget some primitive type?
                        }
                        else if(!o.getClass().isAssignableFrom(paramTypes[j])) continue ConstructorLoop;
                    }
                    
                    return c.newInstance(constructorParams.toArray());
                }              
                throw new NoSuchMethodException("Couldn't find a constructor that matches parameters parsed from XML.");
            }
            else {
                return cls.newInstance();
            }
        }
        
        String clas=e.getAttribute("class");
        if(clas!=null && clas.length()>0){
            Class cls=Class.forName(clas);
            return cls;
        }
        
        if(e.hasAttribute("null")) return null;
        
        if(e.hasAttribute("script")){
            String engine=e.getAttribute("script");
            if(engine.length()==0 || engine.equalsIgnoreCase("default")) engine=ScriptManager.getDefaultScriptEngine();
            ScriptManager scriptManager=new ScriptManager();
            ScriptEngine scriptEngine=scriptManager.getScriptEngine(engine);
            scriptEngine.put("moduleManager",this);
            scriptEngine.put("element",e);

            try{
                String script=((String)xpath.evaluate("text()",e,XPathConstants.STRING)).trim();
                return scriptManager.executeScript(script, scriptEngine);
            }catch(XPathExpressionException xpee){ throw new RuntimeException(xpee); }
        }
        
        if(e.hasAttribute("module")){
            String moduleName=e.getAttribute("module").trim();
            return new ModuleDelegate(this,moduleName);
        }
        
        try{
            String value=((String)xpath.evaluate("text()",e,XPathConstants.STRING)).trim();
            return replaceVariables(value, variables);
        }catch(XPathExpressionException xpee){ throw new RuntimeException(xpee); }
    }
    
    private static XPath xpath=null;
    /**
     * Reads all the options inside the module element. Currently only
     * processing params elements but in the future could handle other elements
     * too.
     * 
     * @param e The module element contents of which are to be processed.
     * @return The parameters defined in the element.
     * @throws ReflectiveOperationException
     * @throws ScriptException 
     */
    public HashMap<String,Object> parseXMLOptionsElement(Element e) throws ReflectiveOperationException, ScriptException {
        if(xpath==null) xpath=XPathFactory.newInstance().newXPath();
        
        LinkedHashMap<String,Object> params=new LinkedHashMap<String,Object>();

        try{
            NodeList nl2=(NodeList)xpath.evaluate("param",e,XPathConstants.NODESET);
            for(int j=0;j<nl2.getLength();j++){
                Element e2=(Element)nl2.item(j);
                String key=e2.getAttribute("key");
                Object value=parseXMLParamElement(e2);

                if(key!=null && value!=null) params.put(key.trim(), value);
            }
        }catch(XPathExpressionException xpee){
            throw new RuntimeException(xpee); // hardcoded xpath expressions so this shouldn't really happen
        }
        
        return params;
    }
    
    /**
     * Reads module settings from a module element. The settings include
     * autostarting, priority, name read from attributes of the element
     * and useService elements defined inside the module element.
     * 
     * @param e The module element.
     * @param source The source identifier for the module, could be the file name
     *                where the module comes from or something else.
     * @return Parsed module settings.
     * @throws ClassNotFoundException 
     */
    public static ModuleSettings parseXMLModuleSettings(Element e,String source) throws ClassNotFoundException {
        if(xpath==null) xpath=XPathFactory.newInstance().newXPath();
        
        ModuleSettings settings=new ModuleSettings();
        
        String autostartS=e.getAttribute("autostart");
        if(autostartS!=null && autostartS.equalsIgnoreCase("false")) settings.autoStart=false;
        else settings.autoStart=true;
        
        String priorityS=e.getAttribute("priority");
        if(priorityS!=null && priorityS.length()>0) settings.servicePriority=Integer.parseInt(priorityS);
        
        String name=e.getAttribute("name");
        if(name!=null && name.length()>0) settings.name=name;
        
        settings.source=source;
        
        try{
            NodeList nl=(NodeList)xpath.evaluate("useService",e,XPathConstants.NODESET);
            for(int j=0;j<nl.getLength();j++){
                Element e2=(Element)nl.item(j);
                String service=e2.getAttribute("service");
                String value=e2.getAttribute("value");
                
                Class serviceClass=Class.forName(service);
                if(!Module.class.isAssignableFrom(serviceClass)){
                    throw new ClassCastException("The specified service is not a module");
                }
                settings.serviceNames.put((Class<? extends Module>)serviceClass,value);
            }
        }catch(XPathExpressionException xpee){
            throw new RuntimeException(xpee); // hardcoded xpath expressions so this shouldn't really happen
        }        

        return settings;
    }
    
    /**
     * Reads a module element and returns the module, its module
     * parameters and module settings. Basically handles everything about
     * a module element.
     * 
     * @param e The module element.
     * @param source The source identifier for the module, could be the file name
     *                where the module comes from or something else.
     * @return A tuple containing the module itself, parsed module parameters
     *          and parsed module settings.
     * @throws ReflectiveOperationException
     * @throws ScriptException 
     */
    public T3<Module,HashMap<String,Object>,ModuleSettings> parseXMLModuleElement(Element e,String source) throws ReflectiveOperationException, ScriptException {
        String moduleClass=e.getAttribute("class");
        Module module=(Module)Class.forName(moduleClass).newInstance();

        HashMap<String,Object> params;
        if(module instanceof XMLOptionsHandler){
            params=((XMLOptionsHandler)module).parseXMLOptionsElement(this,e,source);
        }
        else {
            params=parseXMLOptionsElement(e);
        }
        
        ModuleSettings settings=parseXMLModuleSettings(e,source);

        return Tuples.t3(module,params,settings);
    }
    
    /**
     * Gets the value of a defined variable. The variable may have been
     * defined explicitly with setVariable or parsed from a configuration
     * file.
     * 
     * @param key The name of the variable.
     * @return The value of the variable or null if it is undefined.
     */
    public String getVariable(String key){
        return variables.get(key);
    }
    
    /**
     * Performs variable replacement on a string. All occurrences of
     * "${variableName}" where variableName is a defined variable will be
     * replaced with the value of the variable.
     * 
     * @param value The string in which variable replacement will be done.
     * @param variables A map containing all the defined variables.
     * @return 
     */
    public String replaceVariables(String value,HashMap<String,String> variables){
        for(Map.Entry<String,String> e : variables.entrySet()){
            value=value.replace("${"+e.getKey()+"}", e.getValue());
        }
        return value;
    }
    
    /**
     * Sets a variable. Use null value to remove a variable definition. Will
     * overwrite old value if the variable had already been set.
     * 
     * @param key The name of the variable to set.
     * @param value The value of the variable or null to remove the definition.
     */
    public void setVariable(String key,String value){
        if(value==null) variables.remove(key);
        else variables.put(key,value);
    }
    
    /**
     * Parses a variable element and defines that variable for future use.
     * 
     * @param e The variable element.
     * @param variables The variable map into which the variable is defined.
     */
    public void parseXMLVariable(Element e,HashMap<String,String> variables){
        if(xpath==null) xpath=XPathFactory.newInstance().newXPath();
        
        try{
            String key=e.getAttribute("key");
            String value=((String)xpath.evaluate("text()",e,XPathConstants.STRING)).trim();
            value=replaceVariables(value,variables);
            setVariable(key,value);
        }catch(XPathExpressionException xpee){
            throw new RuntimeException(xpee); // hardcoded xpath expressions so this shouldn't really happen
        }        
    }

    /**
     * Parses an handles include element. This may add a large number of modules
     * or do other changes in the module manager.
     * 
     * @param e The include element.
     * @return A list of all the modules added.
     */
    public Collection<Module> parseXMLInclude(Element e){
        ArrayList<Module> ret=new ArrayList<Module>();
        
        String src=e.getAttribute("src").trim();
        if(src.length()>0) ret.addAll(readXMLOptionsFile(src));
        
        return ret;
    }
    
    /**
     * Parses an XML element as if it was the root element of
     * a full configuration file. Creates all the modules
     * defined in the element. Also sets whatever variables are defined and
     * uses those when parsing the rest of the element.
     * 
     * @param doc The element which contains the config definitions.
     * @param source The source identifier, e.g. file name, where this element
     *                came from.
     * @return A list of all the modules added.
     */
    public Collection<Module> parseXMLConfigElement(Node doc,String source){
        try{
            ArrayList<Module> ret=new ArrayList<Module>();
            xpath=XPathFactory.newInstance().newXPath();
            
            NodeList nl=(NodeList)xpath.evaluate("//variable",doc,XPathConstants.NODESET);
            for(int i=0;i<nl.getLength();i++){
                Element e=(Element)nl.item(i);
                parseXMLVariable(e,variables);
            }
            
            nl=(NodeList)xpath.evaluate("//include",doc,XPathConstants.NODESET);
            for(int i=0;i<nl.getLength();i++){
                Element e=(Element)nl.item(i);
                ret.addAll(parseXMLInclude(e));
            }

            nl=(NodeList)xpath.evaluate("//module",doc,XPathConstants.NODESET);
            for(int i=0;i<nl.getLength();i++){
                Element e=(Element)nl.item(i);
                T3<Module,HashMap<String,Object>,ModuleSettings> parsed=parseXMLModuleElement(e,source);
                addModule(parsed.e1,parsed.e2,parsed.e3);
            }
            
            /*
              Modules are added to ret here instead of adding parsed.e1 in the previous
              loop because a single module element may in some cases result in
              multiple modules being added to the manager. This can happen, for
              example, if the module implements XMLOptionsHandler and then adds
              other modules in there.
            */
            for(Module m : modules) {
                ModuleSettings settings=getModuleSettings(m);
                if(settings!=null && settings.source!=null && settings.source.equals(source))
                    ret.add(m);
            }
            
            return ret;
        }catch(Exception ex){
            if(log!=null) log.error("Error reading options file", ex);
            return null;
        }
        
    }
    
    /**
     * Reads and parses a whole xml configuration file. Creates all the modules
     * defined in the file. Also sets whatever variables are defined in the file and
     * uses those when parsing the rest of the file.
     * 
     * @param optionsFileName 
     * @return A list of all the modules added.
     */
    public Collection<Module> readXMLOptionsFile(String optionsFileName){
        try{
            DocumentBuilderFactory docBuilderFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder=docBuilderFactory.newDocumentBuilder();
            Node doc=docBuilder.parse(new File(optionsFileName));
            return parseXMLConfigElement(doc,optionsFileName);
        }catch(Exception e){
            if(log!=null) log.error("Error reading options file", e);
            return null;
        }            

    }
    
    /**
     * A class that contains all the different module settings. Module
     * settings are attributes the module manager itself uses. These do not
     * include the module parameters which are attributes the module uses to
     * initialise itself.
     */
    public static class ModuleSettings {

        public ModuleSettings() {
        }

        public ModuleSettings(boolean autoStart) {
            this.autoStart=autoStart;
        }
        
        public ModuleSettings(String name){
            this.name=name;
        }
        
        public ModuleSettings(String name,boolean autoStart,int servicePriority, HashMap<Class<? extends Module>,String> serviceNames){
            this.name=name;
            this.autoStart=autoStart;
            this.servicePriority=servicePriority;
            if(serviceNames!=null) this.serviceNames=serviceNames;
        }
        
        /**
         * The explicitly defined dependencies.
         */
        public HashMap<Class<? extends Module>,String> serviceNames=new HashMap<Class<? extends Module>,String>();
        /**
         * Module priority when used as a service.
         */
        public int servicePriority=0;
        /**
         * Whether to autostart the module.
         */
        public boolean autoStart=true;
        /**
         * Module name.
         */
        public String name=null;
        
        /**
         * The source where this module came from. Could be a file name
         * or some other identifier.
         */
        public String source=null;
    }
}
