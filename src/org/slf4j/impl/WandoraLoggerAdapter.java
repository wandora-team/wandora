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
 */



package org.slf4j.impl;




import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 *
 * @author akivela
 */


public class WandoraLoggerAdapter implements Logger {

    
    StringBuilder logData = null;
    
    
    public WandoraLoggerAdapter(String name) {
        logData = new StringBuilder();
    }
    
    
    @Override
    public String getName() {
        return "Wandora logger";
    }
    
    // -------------------------------------------------------------------------

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String string) {
        log(string);
    }

    @Override
    public void trace(String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void trace(String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void trace(String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void trace(String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String string) {
        log(string);
    }

    @Override
    public void trace(Marker marker, String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void trace(Marker marker, String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void trace(Marker marker, String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void trace(Marker marker, String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    
    // -------------------------------------------------------------------------
    
    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String string) {
        log(string);
    }

    @Override
    public void debug(String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void debug(String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void debug(String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void debug(String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String string) {
        log(string);
    }

    @Override
    public void debug(Marker marker, String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void debug(Marker marker, String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void debug(Marker marker, String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void debug(Marker marker, String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    // -------------------------------------------------------------------------
    
    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String string) {
        log(string);
    }

    @Override
    public void info(String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void info(String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void info(String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void info(String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public void info(Marker marker, String string) {
        log(string);
    }

    @Override
    public void info(Marker marker, String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void info(Marker marker, String string, Object o, Object o1) {
        log(string);
        log(o1);
    }

    @Override
    public void info(Marker marker, String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void info(Marker marker, String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    // -------------------------------------------------------------------------
    
    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String string) {
        log(string);
    }

    @Override
    public void warn(String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void warn(String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void warn(String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void warn(String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void warn(Marker marker, String string) {
        log(string);
    }

    @Override
    public void warn(Marker marker, String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void warn(Marker marker, String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void warn(Marker marker, String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void warn(Marker marker, String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }
    
    // -------------------------------------------------------------------------

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String string) {
        log(string);
    }

    @Override
    public void error(String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void error(String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void error(String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void error(String string, Throwable thrwbl) {
        log(string);
        log(thrwbl);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void error(Marker marker, String string) {
        log(string);
    }

    @Override
    public void error(Marker marker, String string, Object o) {
        log(string);
        log(o);
    }

    @Override
    public void error(Marker marker, String string, Object o, Object o1) {
        log(string);
        log(o);
        log(o1);
    }

    @Override
    public void error(Marker marker, String string, Object[] os) {
        log(string);
        log(os);
    }

    @Override
    public void error(Marker marker, String string, Throwable thrwbl) {
       log(string);
       log(thrwbl);
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
