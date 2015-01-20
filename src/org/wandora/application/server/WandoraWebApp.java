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
package org.wandora.application.server;


import java.io.*;
import org.wandora.utils.Options;
/**
 *
 * @author olli
 */
public class WandoraWebApp {
    public static final Class defaultHandlerClass=VelocityWebAppHandler.class;

    protected boolean enabled;
    protected boolean localonly;
    protected String name;
    protected String user;
    protected String pass;
    protected WebAppHandler handler;
    protected String serverPath;
    protected WandoraWebAppServer server;
    protected boolean started;
    protected String openPath;
    
    
    

    public WandoraWebApp(String name,WandoraWebAppServer server){
        this.name=name;
        this.server=server;
        this.serverPath=server.getServerPath();
        this.started=false;
        this.openPath=null;
    }

    public void start(WandoraWebAppServer server){
        started=true;
        if(isEnabled()) handler.start(this, server);
    }
    public void stop(WandoraWebAppServer server){
        started=false;
        if(isEnabled()) handler.stop(this,server);
    }

    public String getName(){return name;}
    public boolean isEnabled(){return enabled;}
    public void setEnabled(boolean b){
        if(enabled!=b){
            enabled=b;
            if(started){
                if(enabled) handler.start(this,server);
                else handler.stop(this,server);
            }
        }
    }
    public boolean isLocalOnly(){return localonly;}
    public void setLocalOnly(boolean b){localonly=b;}
    public String getUser(){return user;}
    public void setUser(String s){user=((s==null || s.length()==0)?null:s);}
    public String getPassword(){return pass;}
    public void setPassword(String s){pass=s;}
    public WebAppHandler getHandler(){return handler;}
    public void setHandler(WebAppHandler h){handler=h;}
    public String getServerPath(){return serverPath;}
    public void setServerPath(String s){serverPath=s;}
    public String getOpenPath(){return openPath;}
    public void setOpenPath(String p){openPath=p;}
    

    public void readOptions(){
        File optionsFile=new File(serverPath+name+File.separator+"config.xml");
        if(optionsFile.exists()){
            Options o=new Options("file://"+optionsFile.getAbsolutePath());
            enabled=o.getBoolean("enabled", false);
            localonly=o.getBoolean("localonly", true);
            user=o.get("user", "").trim();
            pass=o.get("password", "").trim();
            openPath=o.get("openPath", "").trim();
            if(user.length()==0) user=null;
            String handlerS=o.get("handler");
            if(handlerS==null) handler=new VelocityWebAppHandler();
            else {
                try{
                    handler=(WebAppHandler)Class.forName(handlerS).newInstance();
                }catch(Exception e){
                    e.printStackTrace();
                    handler=null;
                }
            }
            handler.init(this,server,o);
        }
        else {
            openPath=null;
            enabled=false;
            localonly=true;
            user=null;
            pass=null;
            handler=new VelocityWebAppHandler();
            handler.init(this, server, null);
        }
    }
    public void writeOptions(){
        File optionsFile=new File(serverPath+name+File.separator+"config.xml");
        Options o=new Options("file://"+optionsFile.getAbsolutePath());
        o.put("enabled",""+enabled);
        o.put("localonly",""+localonly);
        o.put("user",user);
        o.put("password",pass);
        o.put("handler",handler.getClass().getName());
        o.put("openPath",openPath);
        handler.save(this,server,o);
        o.save();
    }
    public String getPath(String path){
        return serverPath+name+File.separator+path+File.separator;        
    }
    public String getTemplatePath(){
        return getPath("templates");
    }
    public String getStaticPath(){
        return getPath("static");
    }
}
