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
 * 
 * 
 * SetOptions.java
 *
 * Created on 9. marraskuuta 2005, 21:30
 *
 */

package org.wandora.application.tools;



import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.utils.Options;



/**
 *
 * @author akivela
 */
public class SetOptionsLocal extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;
	
	private Map<String,String> options = new LinkedHashMap<>();
    private boolean requiresRefresh = false;
    private Options localOptions = null;
    
    
    public SetOptionsLocal() {
    }
    public SetOptionsLocal(Options localOpts, String key, int value, boolean rf) {
        requiresRefresh = rf;
        localOptions = localOpts;
        options.put(key, "" + value);
    }
    public SetOptionsLocal(Options localOpts, String key, String value, boolean rf) {
        requiresRefresh = rf;
        localOptions = localOpts;
        options.put(key, value);
    }
    
    
    public void setOption(String key, String value) {
        if(key != null && value != null) {
            options.put(key, value);
        }
    }
    public void setOptions(Map<String,String> subOptions) {
        if(subOptions != null) {
            options.putAll(subOptions);
        }
    }


    @Override
    public String getName() {
        return "Set local options";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        if(localOptions != null) {
            String key = null;
            String value = null;
            for(Iterator<String> keys = options.keySet().iterator(); keys.hasNext(); ) {
                try {
                    key = (String) keys.next();
                    value = (String) options.get(key);
                    localOptions.put(key, value);
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
    }


    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
}
