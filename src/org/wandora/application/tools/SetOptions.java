/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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



import java.util.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.utils.*;



/**
 *
 * @author akivela
 */
public class SetOptions extends AbstractWandoraTool implements WandoraTool {

    private HashMap options = new HashMap();
    private boolean requiresRefresh = false;
    
    
    
    public SetOptions() {
    }
    public SetOptions(String key, int value, boolean rf) {
        requiresRefresh = rf;
        options.put(key, "" + value);
    }
    public SetOptions(String key, String value, boolean rf) {
        requiresRefresh = rf;
        options.put(key, value);
    }
    
    
    public void setOption(String key, String value) {
        if(key != null && value != null) {
            options.put(key, value);
        }
    }
    public void setOptions(HashMap subOptions) {
        if(subOptions != null) {
            options.putAll(subOptions);
        }
    }


    @Override
    public String getName() {
        return "Set options";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        Options globalOptions = wandora.getOptions();
        if(globalOptions != null) {
            String key = null;
            String value = null;
            for(Iterator keys = options.keySet().iterator(); keys.hasNext(); ) {
                try {
                    key = (String) keys.next();
                    value = (String) options.get(key);
                    globalOptions.put(key, value);
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
