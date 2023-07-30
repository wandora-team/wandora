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
package org.wandora.modules;
import java.util.Collection;
import java.util.Map;
/**
 * The interface all modules must implement. Typically you will want to
 * extend AbstractModule instead of implementing this directly. It'll contain 
 * basic implementation for all methods and perform some commonly needed functions.
 * 
 * @author olli
 */
public interface Module {
    /**
     * <p>
     * Initialises the module. After constructor, this is the first method 
     * called in the life cycle of a module. It should not perform anything
     * time consuming or anything with notable outside side effects. It should
     * only read the parameters and initialise the module so that it can later
     * be started. Note that a module being initialised doesn't mean that it
     * necessarily will ever be started.
     * </p>
     * <p>
     * A ModuleException may be thrown if
     * something vital is missing from the parameters or they are not sensible.
     * In some cases you may not want to throw an exception even if vital
     * initialisation information is missing. If, for example, it is possible that 
     * the module is initialised in some other way between the init and the start
     * method calls. A ModuleException may also be thrown at the start method
     * if the module is still not initialised.
     * </p>
     * 
     * @param manager The module manager handling this module. You may keep a
     *                 reference to it if needed.
     * @param parametersThe module parameters parsed from the configuration file.
     * @throws ModuleException 
     */
    public void init(ModuleManager manager,Map<String,Object> parameters) throws ModuleException;
    
    /**
     * Returns all the modules this module depends on. This method will be
     * called after init and the dependencies are allowed to depend on the
     * parameters passed to init. The modules that this module depends on
     * must be returned as a collection. In addition, if any required module is
     * not present, a MissingDependencyException should be thrown. Typically you
     * will use the requireModule and optionalModule methods of the manager to
     * make sure that the modules exist and add them to the collection.
     * requireModule will automatically throw the exception too if the module is
     * not found whereas optionalModule does not.
     * 
     * @param manager The module manager handling this module.
     * @return A collection of modules this module depends on.
     * @throws ModuleException 
     */
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException;
//    public HashMap<String,Object> saveOptions();
    
    /**
     * Starts the module. This will be called after init and getDependencies 
     * and signals that the module should now begin to perform whatever it was
     * designed to do. This method call however should not block for long, so 
     * any time consuming tasks should be started in their own threads. You will
     * typically get references to the required modules again in start using the
     * findModule method of the manager and then store those references to instance
     * variables for later use. You may throw a ModuleException if the module
     * is not in a state where it can be started or it immediately becomes
     * obvious that it cannot be started with the provided settings.
     * 
     * @param manager The module manager handling this module.
     * @throws ModuleException 
     */
    public void start(ModuleManager manager) throws ModuleException;
    /**
     * Stops the module. Should stop all threads used by the module, free
     * any large data structures and perform other cleanup. It is possible that
     * the module will be started again later with a start method call (and
     * foregoing init and getDependencies calls).
     * The module should be left in a state where this is possible.
     * 
     * @param manager The module manager handling this module.
     */
    public void stop(ModuleManager manager);
    /**
     * Checks if the module is initialised. Should return true if init
     * has been called and it did not throw an exception.
     * @return 
     */
    public boolean isInitialized();
    /**
     * Checks if the module is running. Should return true if start has
     * been called and it did not throw an exception and stop has not been
     * called yet.
     * @return 
     */
    public boolean isRunning();
}
