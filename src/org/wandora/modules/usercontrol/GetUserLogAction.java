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
package org.wandora.modules.usercontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.AbstractAction;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 *
 * @author olli
 */


public class GetUserLogAction extends AbstractAction {

    protected UserLoggerAction userLogger;
    protected UserStore userStore;
    
    protected String userParamKey;
    protected boolean useLoggedInUser=false;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(this,UserLoggerAction.class, deps);
        manager.requireModule(this,UserStore.class, deps);
        return deps;
    }

    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
        User u=null;
        if(useLoggedInUser) u=user;
        else {
            if(userParamKey==null) return false;
            String userName=req.getParameter(userParamKey);
            if(userName==null || userName.length()==0) return false;
            try{
                u=userStore.getUser(userName);
            }catch(UserStoreException use){
                return false;
            }
        }
        
        if(u==null) return false;
        String logFileS=userLogger.getLogFile(u);
        if(logFileS==null) return false;
        File f=new File(logFileS);
        
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        
        OutputStream out=resp.getOutputStream();
        if(!f.exists()) {
            out.write("Log file doesn't exist.".getBytes());
            out.close();
        }
        else {
            try{
                byte[] buf=new byte[4096];
                int read;
                InputStream in=new FileInputStream(f);
                while( (read=in.read(buf))!=-1 ){
                    out.write(buf,0,read);
                }
                in.close();
                out.close();
            }
            catch(IOException ioe){
                out.write("\nError reading log file.".getBytes());
                out.close();
                logging.warn("Exception sending user log file",ioe);
            }
        }
        return true;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o;
        o=settings.get("userParamKey");
        if(o!=null){
            userParamKey=o.toString();
            useLoggedInUser=false;
        }
        
        o=settings.get("useLoggedInUser");
        if(o!=null) useLoggedInUser=Boolean.parseBoolean(o.toString());
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        userLogger=manager.findModule(this,UserLoggerAction.class);
        userStore=manager.findModule(this,UserStore.class);
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        userLogger=null;
        userStore=null;
        super.stop(manager);
    }
    
}
