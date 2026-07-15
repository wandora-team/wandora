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
 *
 * WandoraLogger.java
 */
package org.wandora.utils.logger;



/**
 * 
 * @author akikivela
 *
 */
public class Log4j2Logger {

    org.apache.logging.log4j.Logger realLogger = org.apache.logging.log4j.LogManager.getLogger(Log4j2Logger.class);
    
    
    private Log4j2Logger(Class<?> clazz) {
        realLogger = org.apache.logging.log4j.LogManager.getLogger(clazz);
    }
    
    
    public static Log4j2Logger getLogger(Class<?> clazz) {
        return new Log4j2Logger(clazz);
    }
    
    
    
    public void debug(String str) {
        realLogger.debug(str);
    }
    
    public void info(String str) {
        realLogger.info(str);
    }
    
    public void warn(String str) {
        realLogger.warn(str);
    }
    
    public void error(String str) {
        realLogger.warn(str);
    }
    
    
    
    public void writelog(String level, String s) {
        if("INF".equalsIgnoreCase(level)) {
            info(s);
        }
        else if("DBG".equalsIgnoreCase(level)) {
            debug(s);
        }
        else if("ERR".equalsIgnoreCase(level)) {
            error(s);
        }
        else {
            info(s);
        }
    }
}
