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
 */
package org.wandora.modules.usercontrol;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.AbstractAction;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 *
 * @author olli
 */

public class UserLoggerAction extends AbstractAction {

    // these are static so that same log files can be shared by multiple actions
    protected static final Object fileWait=new Object();
    protected static final HashSet openFiles=new HashSet();
    
    protected String entryHeader;
    protected HashMap<String,String> logParams;
    
    protected String logDir;
    
    public String getLogFile(User user){
        try{
            String userName=URLEncoder.encode(user.getUserName(), "UTF-8");
            if(userName.equals(".") || userName.equals("..")) return null;
            return logDir+userName;
        }catch(UnsupportedEncodingException uee){
            throw new RuntimeException(uee); // shouldn't happen, UTF-8 is required in all java implementations
        }
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps= super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("logDir");
        if(o!=null) logDir=o.toString();
        
        o=settings.get("entryHeader");
        if(o!=null) entryHeader=o.toString();
        
        logParams=new HashMap<String,String>();
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("logParam.")){
                key=key.substring("logParam.".length());
                String value=e.getValue().toString().trim();
                if(value.length()==0) value=key;
                
                logParams.put(key,value);
            }
        }
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        super.stop(manager);
    }
    
    protected Writer openFile(String fileName){
        long t=System.currentTimeMillis();
        synchronized(fileWait){
            while(openFiles.contains(fileName) && System.currentTimeMillis()-t<2000) {
                try{
                    fileWait.wait(2000);
                }catch(InterruptedException ie){
                    return null;
                }
            }
            if(openFiles.contains(fileName)) return null;
            try{
                OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(fileName,true),"UTF-8");
                openFiles.add(fileName);
                return out;
            }catch(FileNotFoundException fnfe){
                logging.warn("Couldn't open user log file for writing",fnfe);
                return null;
            }catch(UnsupportedEncodingException uee){
                throw new RuntimeException(uee); // shouldn't happen, UTF-8 is required in all java implementations                
            }
        }
    }
    
    protected void closeFile(String fileName,Writer out){
        synchronized(fileWait){
            try{
                out.close();
            }catch(IOException ioe){ logging.warn("Couldn't close user log file",ioe); }
            openFiles.remove(fileName);
            fileWait.notifyAll();
        }
    }
    
    protected String getEntryParams(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, User user){
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<String,String> e : logParams.entrySet()){
            String key=e.getKey();
            String value=e.getValue();
            
            String paramValue;
            String replacedValue=doReplacements(value, req, method, action, user);
            if(replacedValue.equals(value)) paramValue=req.getParameter(value);
            else paramValue=replacedValue;
            
            if(paramValue==null) paramValue="";
            
            paramValue=paramValue.replace("\\", "\\\\").replace("\"","\\\"");
            if(sb.length()>0) sb.append("; ");
            sb.append(key).append("=\"").append(paramValue).append("\"");
        }
        return sb.toString();
    }
    
    protected SimpleDateFormat logDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected boolean writeEntry(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, User user,Writer out) {
        StringBuilder entry=new StringBuilder();
        
        entry.append(logDateFormat.format(new Date()));
        if(entryHeader!=null) entry.append(" ").append(entryHeader);
        
        String params=getEntryParams(req, resp, method, action, user);
        
        if(params!=null && params.length()>0) {
            entry.append(": ").append(params);
        }
        entry.append("\n");
        
        try{
            out.write(entry.toString());
        }catch(IOException ioe){
            logging.warn("Couldn't write log entry",ioe);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, User user) throws ServletException, IOException {
        String logFileName=getLogFile(user);
        if(logFileName==null) return false;
        Writer out=openFile(logFileName);
        if(out!=null){
            try{
                return writeEntry(req, resp, method, action, user, out);
            }
            finally{
                closeFile(logFileName, out);
            }
        }
        else return false;
    }
    
}
