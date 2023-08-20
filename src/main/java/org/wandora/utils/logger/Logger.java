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
 * Logger.java
 *
 * Created on July 8, 2004, 4:30 PM
 */

package org.wandora.utils.logger;

/**
 *
 * @author  olli, akivela
 */
public abstract class Logger {

    public abstract void writelog(String level, String s);
    
    public void writelog(String s){
        writelog("INF", s);
    }
    
    public String getStackTrace(Throwable e, boolean cause){
        java.io.StringWriter writer=new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(writer));
        if(cause && e.getCause()!=null){
            writer.write("\n");
            writer.write("Cause:\n");
            writer.write(getStackTrace(e.getCause(),true));
        }
        return writer.toString();
    }
    
    public void writelog(String level,String s,Throwable e){
        writelog(level,s+"\n"+getStackTrace(e,true));
    }
    public void writelog(String level,Throwable e){
        writelog(level,getStackTrace(e,true));
    }
    
    
    public static void log(String msg) {
        defaultLogger.writelog(msg);
    }
    
    private static Logger defaultLogger = new SimpleLogger();
    
    
    public static void setLogger(Logger logger){
        defaultLogger=logger;
    }
    
    public static Logger getLogger() {
        return defaultLogger;
    }
    
}
