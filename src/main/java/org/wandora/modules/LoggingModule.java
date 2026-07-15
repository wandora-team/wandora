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
package org.wandora.modules;


import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * <p>
 * A module that provides logging facilities for other modules. Logging
 * modules should follow a slightly different life cycle to other modules. This
 * is because logging might be needed already in the init or getDependencies
 * phase of modules while module functions typically become available only after
 * the module has been started. You should initialise the logging into a state
 * where it can be fully used already in the init method.
 * </p>
 * <p>
 * This default implementation supports two initialisation parameters. First
 * parameter is "log", you can use this to specify the logging mechanism that should
 * be used. This should be an instance of org.apache.commons.logging.Log. If
 * not specified, the logger of the module manager is used, and if that is not
 * specified either, a SimpleLog is created which outputs messages to stderr.
 * </p>
 * <p>
 * You may use the logLevel initialisation parameter to set the lowest level of
 * logging messages that are printed. Messages of lower level than this are
 * simple ignored. The possible values for this are trace, debug, info, warn,
 * error, fatal and none. None is used to suppress logging entirely while providing
 * a logging module for other modules that require it. The default log level
 * is trace, i.e. all logging messages are printed. However, bear in mind that
 * the underlying logger may have its own mechanisms to further filter logging
 * messages. Thus even if this LoggingModule is set to print all messages, the
 * logging level may be filtered to a higher level elsewhere. This is dependent
 * on the logging implementation used.
 * </p>
 * 
 * @author olli
 */
public class LoggingModule extends AbstractModule {

    protected Log log;
    protected int logLevel=SubLog.LOG_TRACE;

    public LoggingModule(){
    }
    public LoggingModule(Log log){
        this.log=log;
    }
    
    public Log getLog(){
        return log;
    }

    public Log getLog(Module module){
        String name=module.toString();
        return new SubLog(name+" - ",log);
    }
    public Log getLog(String moduleName){
        return new SubLog(moduleName+" - ",log);
    }

    public static Log getLog(String moduleName,Log log){
        return new SubLog(moduleName+" - ",log);
    }
    
    public int getLogLevel(){
        return logLevel;
    }
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o=null;
        o=settings.get("log");
        if(o!=null && o instanceof Log) log=(Log)o;
        
        if(log==null) log=manager.getLogger();
        if(log==null) log=new SimpleLog("log");
        
        o=settings.get("logLevel");
        if(o!=null){
            String s=o.toString().trim().toLowerCase();
            if(s.equals("all") || s.equals("trace")) logLevel=SubLog.LOG_TRACE;
            else if(s.equals("debug")) logLevel=SubLog.LOG_DEBUG;
            else if(s.equals("info")) logLevel=SubLog.LOG_INFO;
            else if(s.equals("warn") || s.equals("warning")) logLevel=SubLog.LOG_WARN;
            else if(s.equals("error")) logLevel=SubLog.LOG_ERROR;
            else if(s.equals("fatal")) logLevel=SubLog.LOG_FATAL;
            else if(s.equals("none")) logLevel=SubLog.LOG_NONE;
            
            if(logLevel>SubLog.LOG_TRACE) log=new SubLog("",log,logLevel);
        }
        
        super.init(manager,settings);
    }

    public static class SubLog implements Log {
        protected Log log;
        protected String prefix;
        
        public static final int LOG_ALL=0;
        public static final int LOG_TRACE=0;
        public static final int LOG_DEBUG=1;
        public static final int LOG_INFO=2;
        public static final int LOG_WARN=3;
        public static final int LOG_ERROR=4;
        public static final int LOG_FATAL=5;
        public static final int LOG_NONE=6;
        
        protected int logLevel=LOG_TRACE;
        
        public SubLog(String prefix,Log log,int logLevel){
            this.logLevel=logLevel;
            this.prefix=prefix;
            this.log=log;
        }
        
        public SubLog(String prefix,Log log){
            this.prefix=prefix;
            this.log=log;
        }
        public void debug(Object arg0) {
            if(logLevel<=LOG_DEBUG) log.debug(prefix+arg0);
        }

        public void debug(Object arg0, Throwable arg1) {
            if(logLevel<=LOG_DEBUG) log.debug(prefix+arg0,arg1);
        }

        public void error(Object arg0) {
            if(logLevel<=LOG_ERROR) log.error(prefix+arg0);
        }

        public void error(Object arg0, Throwable arg1) {
            if(logLevel<=LOG_ERROR) log.error(prefix+arg0,arg1);
        }

        public void fatal(Object arg0) {
            if(logLevel<=LOG_FATAL) log.fatal(prefix+arg0);
        }

        public void fatal(Object arg0, Throwable arg1) {
            if(logLevel<=LOG_FATAL) log.fatal(prefix+arg0,arg1);
        }

        public void info(Object arg0) {
            if(logLevel<=LOG_INFO) log.info(prefix+arg0);
        }

        public void info(Object arg0, Throwable arg1) {
            if(logLevel<=LOG_INFO) log.info(prefix+arg0,arg1);
        }

        public boolean isDebugEnabled() {
            return logLevel<=LOG_DEBUG && log.isDebugEnabled();
        }

        public boolean isErrorEnabled() {
            return logLevel<=LOG_ERROR && log.isErrorEnabled();
        }

        public boolean isFatalEnabled() {
            return logLevel<=LOG_FATAL && log.isFatalEnabled();
        }

        public boolean isInfoEnabled() {
            return logLevel<=LOG_INFO && log.isInfoEnabled();
        }

        public boolean isTraceEnabled() {
            return logLevel<=LOG_TRACE && log.isTraceEnabled();
        }

        public boolean isWarnEnabled() {
            return logLevel<=LOG_WARN && log.isWarnEnabled();
        }

        public void trace(Object arg0) {
            if(logLevel<=LOG_TRACE) log.trace(prefix+arg0);
        }

        public void trace(Object arg0, Throwable arg1) {
            if(logLevel<=LOG_TRACE) log.trace(prefix+arg0,arg1);
        }

        public void warn(Object arg0) {
            if(logLevel<=LOG_WARN) log.warn(prefix+arg0);
        }

        public void warn(Object arg0, Throwable arg1) {
            if(logLevel<=LOG_WARN) log.warn(prefix+arg0,arg1);
        }

    }
}
