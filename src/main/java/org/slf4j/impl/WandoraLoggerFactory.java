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



package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.wandora.application.Wandora;
import org.wandora.utils.Options;

/**
 *
 * @author akivela
 */


public class WandoraLoggerFactory implements ILoggerFactory {

    private Map<String, WandoraLoggerAdapter> loggerMap;
    
    private Map<String, Integer> loggingLevels = new HashMap<>();  

    
    
    public WandoraLoggerFactory() {
        loggingLevels = new HashMap<>();
        loggerMap = new HashMap<String, WandoraLoggerAdapter>();
        initializeLoggerFactory();
    }
    
    
    
    
    @Override
    public Logger getLogger(String name) {
        synchronized (loggerMap) {
            if (!loggerMap.containsKey(name)) {
                // System.out.println("Creating logger for '"+name+"' with level "+getLoggingLevel(name));
                WandoraLoggerAdapter logger = new WandoraLoggerAdapter(name);
                logger.setLogLevel(getLoggingLevel(name));
                loggerMap.put(name, logger);
            }
 
            return loggerMap.get(name);
        }
    }
    
    
    private int getLoggingLevel(String name) {
        if(name != null) {
            for(String logRegex : loggingLevels.keySet()) {
                if(name.matches(logRegex)) {
                    Integer i = loggingLevels.get(logRegex);
                    if(i != null) {
                        return i.intValue();
                    }
                    else {
                        // System.out.println("Warning, log level is null");
                    }
                }
            }
        }
        
        return WandoraLoggerAdapter.LOG_INFO;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    private void initializeLoggerFactory() {
        Wandora wandora = Wandora.getWandora();
        if(wandora != null) {
            Options options = wandora.getOptions();
            if(options != null) {
                int i = 0;
                String regex;
                int level;
                do {
                    regex = options.get("loggerRules.loggerRule["+i+"].nameRegex");
                    level = options.getInt("loggerRules.loggerRule["+i+"].logLevel", 9999);
                    
                    if(regex != null && level != 9999) {
                        loggingLevels.put(regex, level);
                    }
                    i++;
                }
                while(regex != null && i < 1000);
            }
        }
    }
    
    
}
