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




import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 *
 * @author akivela
 */


public class WandoraLoggerAdapter implements Logger {

    
    
    public static final int LOG_ALL=0;
    public static final int LOG_TRACE=0;
    public static final int LOG_DEBUG=1;
    public static final int LOG_INFO=2;
    public static final int LOG_WARN=3;
    public static final int LOG_ERROR=4;
    public static final int LOG_FATAL=5;
    public static final int LOG_NONE=6;
    
    
    private StringBuilder logData = null;
    private int logLevel = LOG_ERROR;
    
    
    
    public WandoraLoggerAdapter(String name) {
        logData = new StringBuilder();
    }
    public WandoraLoggerAdapter(String name, int level) {
        logData = new StringBuilder();
        logLevel = level;
    }
    
    @Override
    public String getName() {
        return "Wandora logger";
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }
    
    
    
    // -------------------------------------------------------------------------

    @Override
    public boolean isTraceEnabled() {
        return logLevel <= LOG_TRACE;
    }

    @Override
    public void trace(String string) {
        if(isTraceEnabled()) {
            log(string);
        }
    }

    @Override
    public void trace(String string, Object o) {
        if(isTraceEnabled()) {
            log(string);
            log(o);
        }
    }

    @Override
    public void trace(String string, Object o, Object o1) {
        if(isTraceEnabled()) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void trace(String string, Object[] os) {
        if(isTraceEnabled()) {
            log(string);
            log(os);
        }
    }

    @Override
    public void trace(String string, Throwable thrwbl) {
        if(isTraceEnabled()) {
            log(string);
            log(thrwbl);
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logLevel <= LOG_TRACE;
    }

    @Override
    public void trace(Marker marker, String string) {
        if(isTraceEnabled(marker)) {
            log(string);
        }
    }

    @Override
    public void trace(Marker marker, String string, Object o) {
        if(isTraceEnabled(marker)) {
            log(string);
            log(o);
        }
    }

    @Override
    public void trace(Marker marker, String string, Object o, Object o1) {
        if(isTraceEnabled(marker)) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void trace(Marker marker, String string, Object[] os) {
        if(isTraceEnabled(marker)) {
            log(string);
            log(os);
        }
    }

    @Override
    public void trace(Marker marker, String string, Throwable thrwbl) {
        if(isTraceEnabled(marker)) {
            log(string);
            log(thrwbl);
        }
    }

    
    // -------------------------------------------------------------------------
    
    @Override
    public boolean isDebugEnabled() {
        return logLevel <= LOG_DEBUG;
    }

    @Override
    public void debug(String string) {
        if(isDebugEnabled()) {
            log(string);
        }
    }

    @Override
    public void debug(String string, Object o) {
        if(isDebugEnabled()) {
            log(string);
            log(o);
        }
    }

    @Override
    public void debug(String string, Object o, Object o1) {
        if(isDebugEnabled()) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void debug(String string, Object[] os) {
        if(isDebugEnabled()) {
            log(string);
            log(os);
        }
    }

    @Override
    public void debug(String string, Throwable thrwbl) {
        if(isDebugEnabled()) {
            log(string);
            log(thrwbl);
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logLevel <= LOG_DEBUG;
    }

    @Override
    public void debug(Marker marker, String string) {
        if(isDebugEnabled(marker)) {
            log(string);
        }
    }

    @Override
    public void debug(Marker marker, String string, Object o) {
        if(isDebugEnabled(marker)) {
            log(string);
            log(o);
        }
    }

    @Override
    public void debug(Marker marker, String string, Object o, Object o1) {
        if(isDebugEnabled(marker)) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void debug(Marker marker, String string, Object[] os) {
        if(isDebugEnabled(marker)) {
            log(string);
            log(os);
        }
    }

    @Override
    public void debug(Marker marker, String string, Throwable thrwbl) {
        if(isDebugEnabled(marker)) {
            log(string);
            log(thrwbl);
        }
    }

    // -------------------------------------------------------------------------
    
    @Override
    public boolean isInfoEnabled() {
        return logLevel <= LOG_INFO;
    }

    @Override
    public void info(String string) {
        if(isInfoEnabled()) {
            log(string);
        }
    }

    @Override
    public void info(String string, Object o) {
        if(isInfoEnabled()) {
            log(string);
            log(o);
        }
    }

    @Override
    public void info(String string, Object o, Object o1) {
        if(isInfoEnabled()) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void info(String string, Object[] os) {
        if(isInfoEnabled()) {
            log(string);
            log(os);
        }
    }

    @Override
    public void info(String string, Throwable thrwbl) {
        if(isInfoEnabled()) {
            log(string);
            log(thrwbl);
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logLevel <= LOG_INFO;
    }

    @Override
    public void info(Marker marker, String string) {
        if(isInfoEnabled(marker)) {
            log(string);
        }
    }

    @Override
    public void info(Marker marker, String string, Object o) {
        if(isInfoEnabled(marker)) {
            log(string);
            log(o);
        }
    }

    @Override
    public void info(Marker marker, String string, Object o, Object o1) {
        if(isInfoEnabled(marker)) {
            log(string);
            log(o1);
        }
    }

    @Override
    public void info(Marker marker, String string, Object[] os) {
        if(isInfoEnabled(marker)) {
            log(string);
            log(os);
        }
    }

    @Override
    public void info(Marker marker, String string, Throwable thrwbl) {
        if(isInfoEnabled(marker)) {
            log(string);
            log(thrwbl);
        }
    }

    // -------------------------------------------------------------------------
    
    @Override
    public boolean isWarnEnabled() {
        return logLevel <= LOG_WARN;
    }

    @Override
    public void warn(String string) {
        if(isWarnEnabled()) {
            log(string);
        }
    }

    @Override
    public void warn(String string, Object o) {
        if(isWarnEnabled()) {
            log(string);
            log(o);
        }
    }

    @Override
    public void warn(String string, Object[] os) {
        if(isWarnEnabled()) {
            log(string);
            log(os);
        }
    }

    @Override
    public void warn(String string, Object o, Object o1) {
        if(isWarnEnabled()) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void warn(String string, Throwable thrwbl) {
        if(isWarnEnabled()) {
            log(string);
            log(thrwbl);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logLevel <= LOG_WARN;
    }

    @Override
    public void warn(Marker marker, String string) {
        if(isWarnEnabled(marker)) {
            log(string);
        }
    }

    @Override
    public void warn(Marker marker, String string, Object o) {
        if(isWarnEnabled(marker)) {
            log(string);
            log(o);
        }
    }

    @Override
    public void warn(Marker marker, String string, Object o, Object o1) {
        if(isWarnEnabled(marker)) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void warn(Marker marker, String string, Object[] os) {
        if(isWarnEnabled(marker)) {
            log(string);
            log(os);
        }
    }

    @Override
    public void warn(Marker marker, String string, Throwable thrwbl) {
        if(isWarnEnabled(marker)) {
            log(string);
            log(thrwbl);
        }
    }
    
    // -------------------------------------------------------------------------

    @Override
    public boolean isErrorEnabled() {
        return logLevel <= LOG_ERROR;
    }

    @Override
    public void error(String string) {
        if(isErrorEnabled()) {
            log(string);
        }
    }

    @Override
    public void error(String string, Object o) {
        if(isErrorEnabled()) {
            log(string);
            log(o);
        }
    }

    @Override
    public void error(String string, Object o, Object o1) {
        if(isErrorEnabled()) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void error(String string, Object[] os) {
        if(isErrorEnabled()) {
            log(string);
            log(os);
        }
    }

    @Override
    public void error(String string, Throwable thrwbl) {
        if(isErrorEnabled()) {
            log(string);
            log(thrwbl);
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logLevel <= LOG_ERROR;
    }

    @Override
    public void error(Marker marker, String string) {
        if(isErrorEnabled(marker)) {
            log(string);
        }
    }

    @Override
    public void error(Marker marker, String string, Object o) {
        if(isErrorEnabled(marker)) {
            log(string);
            log(o);
        }
    }

    @Override
    public void error(Marker marker, String string, Object o, Object o1) {
        if(isErrorEnabled(marker)) {
            log(string);
            log(o);
            log(o1);
        }
    }

    @Override
    public void error(Marker marker, String string, Object[] os) {
        if(isErrorEnabled(marker)) {
            log(string);
            log(os);
        }
    }

    @Override
    public void error(Marker marker, String string, Throwable thrwbl) {
        if(isErrorEnabled(marker)) {
            log(string);
            log(thrwbl);
        }
    }
    
    // -------------------------------------------------------------------------
    
    private void log(Object o) {
        if(o == null) return;
        
        if(o instanceof String) {
            System.out.println(o);
        }
        else if(o instanceof Throwable) {
            Throwable thrwbl = (Throwable) o;
            // thrwbl.printStackTrace();
        }
        else {
            System.out.println(o.toString());
        }
    }
    
    
}
