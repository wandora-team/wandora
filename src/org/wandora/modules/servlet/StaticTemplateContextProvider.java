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
 *
 */
package org.wandora.modules.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;

/**
 * <p>
 * This class can be used to define a default context for template
 * managers. See TemplateManager for more details. 
 * </p>
 * <p>
 * The context for this module is defined in the initialisation parameters the
 * same way as in TemplateManager or templates. Parameters with the prefix
 * "context." are added to the context with the prefix removed from the parameter
 * name.
 * </p>
 * 
 * @author olli
 */


public class StaticTemplateContextProvider extends AbstractModule implements TemplateContextProvider {

    protected Map<String,Object> context;
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        context=new HashMap<String, Object>();
        
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("context.")){
                context.put(key.substring("context.".length()),e.getValue());
            }
        }
        
        context=Collections.unmodifiableMap(context);
        
        super.init(manager, settings); 
    }

    
    @Override
    public Map<String, Object> getTemplateBaseContext() {
        return context;
    }
    
}
