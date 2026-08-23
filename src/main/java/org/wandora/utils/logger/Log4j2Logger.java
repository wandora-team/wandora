/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 
 * @author akikivela
 *
 */
public class Log4j2Logger extends Logger {

    org.apache.logging.log4j.Logger realLogger = org.apache.logging.log4j.LogManager.getLogger(Log4j2Logger.class);
    
    
    private Log4j2Logger(Class<?> clazz) {
        realLogger = org.apache.logging.log4j.LogManager.getLogger(clazz);
    }
    
    
    public static Log4j2Logger getLogger(Class<?> clazz) {
        return new Log4j2Logger(clazz);
    }
    
    
    
    public void debug(String message) {
        realLogger.debug(message);
    }
    
    public void debug(String message, Object... params) {
    	realLogger.debug(message, params);
    }
    
    public void debug(String message, Throwable throwable) {
    	realLogger.debug(message, throwable);
    }
    
    public void debug(Throwable throwable) {
        realLogger.debug(ExceptionUtils.getStackTrace(throwable));
    }
    
    
    
    public void info(String message) {
        realLogger.info(message);
    }
    
    public void info(String message, Object... params) {
    	realLogger.info(message, params);
    }
    
    public void info(String message, Throwable throwable) {
    	realLogger.info(message, throwable);
    }
    
    public void info(Throwable throwable) {
        realLogger.info(ExceptionUtils.getStackTrace(throwable));
    }
    
    
    
    public void warn(String str) {
        realLogger.warn(str);
    }
    
    public void warn(String message, Object... params) {
    	realLogger.warn(message, params);
    }
    
    public void warn(String message, Throwable throwable) {
    	realLogger.warn(message, throwable);
    }
    
    public void warn(Throwable throwable) {
        realLogger.warn(ExceptionUtils.getStackTrace(throwable));
    }
    
    
    
    public void error(String str) {
        realLogger.error(str);
    }
    
    public void error(String message, Object... params) {
    	realLogger.error(message, params);
    }
    
    public void error(String message, Throwable throwable) {
    	realLogger.error(message, throwable);
    }
    
    public void error(Throwable throwable) {
        realLogger.error(ExceptionUtils.getStackTrace(throwable));
    }
    
    
    
    @Override
    public void writelog(String level, String s) {
        if ("INF".equalsIgnoreCase(level)) {
            info(s);
        }
        else if ("DBG".equalsIgnoreCase(level)) {
            debug(s);
        }
        else if ("ERR".equalsIgnoreCase(level)) {
            error(s);
        }
        else {
            info(s);
        }
    }
}
