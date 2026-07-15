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

import java.util.Map;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;

/**
 * A module that makes an Apache Velocity engine available for
 * any VelocityTemplates. You must include this in the modules framework
 * to be able to use VelocityTemplates. All the initialisation parameters
 * are given as is to the engine as initialisation properties. See Velocity
 * documentation for property names. Normal AbstractModule parameter handling
 * still applies as well.
 * 
 * 
 * @author olli
 */


public class VelocityEngineModule extends AbstractModule {

    protected VelocityEngine engine;
    protected Properties engineProperties;
    
    public VelocityEngine getEngine(){
        return engine;
    }
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
        
        engineProperties=new Properties();
        for(Map.Entry<String,Object> e : settings.entrySet()){
            Object value=e.getValue();
            if(!(value instanceof String)) continue;
            String key=e.getKey();
            engineProperties.setProperty(key, value.toString());
        }
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        engine=new VelocityEngine();
        engine.init(engineProperties);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        engine=null;
        super.stop(manager);
    }
    
    
}
