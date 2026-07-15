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

/**
 *
 * A container class for other modules. The purpose of this is to keep
 * a reference to a module that will be created later. This can be used to
 * refer to other modules in a module configuration file where the reference
 * might occur before the module is created. A ModuleDelegate is then created
 * and the actual module can be retrieved later with getModule.
 * 
 * @author olli
 */

public class ModuleDelegate {
    protected ModuleManager manager;
    protected String moduleName;
    
    public ModuleDelegate(ModuleManager manager, String name){
        this.manager=manager;
        this.moduleName=name;
    }
    
    public Module getModule(){
        return manager.findModule(moduleName, Module.class);
    }
}
