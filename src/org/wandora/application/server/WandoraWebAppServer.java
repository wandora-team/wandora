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
package org.wandora.application.server;
import org.wandora.application.*;
import org.wandora.utils.Options;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.velocity.VelocityContext;

import org.apache.velocity.app.VelocityEngine;

import org.wandora.topicmap.*;
import org.wandora.piccolo.utils.*;
import org.wandora.application.gui.UIBox;
import org.wandora.utils.Base64;

import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.AbstractHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.security.*;
import org.mortbay.jetty.nio.*;


/**
 *
 * @author olli
 */
public class WandoraWebAppServer {
    private static final int defaultPort=8898;

    public static final String OPTION_AUTOSTART="httpserver.autostart";
    public static final String OPTION_PORT="httpserver.port";
    public static final String OPTION_LOCALONLY="httpserver.localonly";
    public static final String OPTION_SERVERPATH="httpserver.serverpath";
    public static final String OPTION_USESSL="httpserver.usessl";

    private int port;
    private boolean useSSL;
    private Wandora wandora;
    private String serverPath;
    private VelocityEngine velocityEngine;
    private boolean localOnly=true;
    private boolean autoStart=false;

    private javax.swing.JButton statusButton;
    private javax.swing.Icon onIcon;
    private javax.swing.Icon offIcon;
    private javax.swing.Icon hitIcon;

    private Thread iconThread;
    private long lastHit;

    private Server jettyServer;
    private Handler requestHandler;
    private org.mortbay.jetty.MimeTypes mimeTypes;

    private HashMap<String,WandoraWebApp> webApps;

    public WandoraWebAppServer(Wandora wandora){
        this.wandora=wandora;
        jettyServer=new Server();

        Options options=wandora.getOptions();
        readOptions(options);
        requestHandler=new JettyHandler();

        mimeTypes=new org.mortbay.jetty.MimeTypes();

        webApps=new HashMap<String,WandoraWebApp>();
        ArrayList<WandoraWebApp> apps=scanWebApps();
        for(WandoraWebApp app : apps){
            webApps.put(app.getName(),app);
        }

    }

    public void returnNotFound(HttpServletResponse response){
        returnNotFound(response,"");
    }
    public void returnNotFound(HttpServletResponse response,String message){
        writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />"+message);
    }

    private class JettyHandler extends AbstractHandler {
        public void handle(String target,HttpServletRequest request,HttpServletResponse response, int dispatch) throws IOException,ServletException {
            System.out.println("Jettyrequest");
            if(localOnly && !request.getRemoteAddr().equals("127.0.0.1")) {
                ((Request)request).setHandled(true);
                return;
            }

            WandoraWebApp app=null;
            String appString=null;
            if(target.startsWith("/")) target=target.substring(1);
            int ind=target.indexOf("/");
            if(ind>-1) appString=target.substring(0,ind);
            else appString=target;
            app=webApps.get(appString);
            if(app!=null){
                if(!app.isEnabled()) app=null;
                else if(app.isLocalOnly() && !request.getRemoteAddr().equals("127.0.0.1")) app=null;
            }

            if(app==null) {
                returnNotFound(response,"File \""+target+"\" not found.");
                ((Request)request).setHandled(true);
                return;
            }
            else {
                if(ind>-1) target=target.substring(ind);
                else target="/";
            }

            String loginUser=app.getUser();
            String loginPass=app.getPassword();

            if(loginUser!=null && loginUser.length()>0){
                String auth=request.getHeader("Authorization");
                boolean ok=false;
                if(auth!=null){
                    ind=auth.indexOf(" ");
                    if(ind!=-1){
                        auth=auth.substring(ind+1);
                        byte[] authbs=Base64.decode(auth);
                        if(new String(authbs).equals(loginUser+":"+loginPass)) ok=true;
                    }
                }
                if(!ok){
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setHeader("WWW-Authenticate", "Basic realm=\"Wandora\"");
                    ((Request)request).setHandled(true);
                    return;
                }
            }
            if(iconThread!=null && iconThread.isAlive()) {
                synchronized(iconThread){
                    lastHit=System.currentTimeMillis();
                    iconThread.notify();
                }
            }
            if(app.getHandler().getPage(app,WandoraWebAppServer.this,target,request,response)){
                ((Request)request).setHandled(true);
            }
        }
    }

    public HashMap<String,WandoraWebApp> getWebApps(){
        return webApps;
    }
    public void setWebApp(WandoraWebApp app){
        webApps.put(app.name,app);
    }
    public boolean removeWebApp(String key){
        return webApps.remove(key)!=null;
    }

    public void log(String s){
        System.out.println(s);
    }
    public void log(Throwable t){
        t.printStackTrace();
    }
    public void log(String s,Throwable t){
        log(s);
        log(t);
    }

    public Server getJetty(){
        return jettyServer;
    }

    public Wandora getWandora(){
        return wandora;
    }

    public void readOptions(Options options){
        localOnly=!options.isFalse(OPTION_LOCALONLY);
        autoStart=options.isTrue(OPTION_AUTOSTART);
        port=options.getInt(OPTION_PORT);
        serverPath=options.get(OPTION_SERVERPATH,"resources/server/");
        useSSL=options.isTrue(OPTION_USESSL);
    }

    public void writeOptions(Options options){
        options.put(OPTION_LOCALONLY, ""+localOnly);
        options.put(OPTION_AUTOSTART, ""+autoStart);
        options.put(OPTION_PORT, ""+port);
        options.put(OPTION_SERVERPATH, serverPath);
        options.put(OPTION_USESSL, ""+useSSL);
    }

    private void updateStatusIcon(){
        if(jettyServer!=null && jettyServer.isRunning()) {
            statusButton.setIcon(this.onIcon);
            statusButton.setToolTipText("Http server is running. Click to stop.");
        }
        else {
            statusButton.setIcon(this.offIcon);
            statusButton.setToolTipText("Http server is not running. Click to start.");
        }

    }

    public void setStatusComponent(javax.swing.JButton button,String onIcon,String offIcon,String hitIcon){
        this.statusButton=button;
        this.onIcon=UIBox.getIcon(onIcon);
        this.offIcon=UIBox.getIcon(offIcon);
        this.hitIcon=UIBox.getIcon(hitIcon);
        updateStatusIcon();
    }

    public boolean isRunning(){
        return jettyServer!=null && jettyServer.isRunning();
    }

    public void start(){
        try{
            Connector c;
/*            if(useSSL) c=new SslSocketConnector();
            else c=new SocketConnector();*/
            if(useSSL) c=new SslSelectChannelConnector();
            else c=new SelectChannelConnector();
            c.setPort(port);
            jettyServer.setConnectors(new Connector[]{c});

/*            Handler[] oldHandlers=null;
            if(jettyServer.getHandlers()!=null) oldHandlers=jettyServer.getHandlers();
            else if(jettyServer.getHandler()!=null) oldHandlers=new Handler[]{jettyServer.getHandler()};
            else oldHandlers=new Handler[0];
            Handler[] handlers=new Handler[oldHandlers.length+1];
            System.arraycopy(oldHandlers, 0, handlers, 1, oldHandlers.length);
            handlers[0]=requestHandler;
            jettyServer.setHandlers(handlers);*/

            jettyServer.setHandler(null);
            
            jettyServer.addHandler(requestHandler);
            for(WandoraWebApp app : webApps.values()){
                app.start(this);
            }

            jettyServer.start();

            if(statusButton!=null){
                updateStatusIcon();
                iconThread=new IconThread();
                iconThread.start();
            }
        }catch(Exception e){
            wandora.handleError(e);
        }
    }

    public void stopServer(){
        try{
            for(WandoraWebApp app : webApps.values()){
                app.stop(this);
            }

            if(jettyServer!=null) jettyServer.stop();
            while(iconThread!=null && iconThread.isAlive()){
                iconThread.interrupt();
                try{
                    iconThread.join();
                }catch(InterruptedException ie){}
            }
            if(statusButton!=null) updateStatusIcon();
            iconThread=null;
        }catch(Exception e){
            wandora.handleError(e);
        }
    }
    public void setPort(int p){port=p;}
    public int getPort(){return port;}
    public void setUseSSL(boolean b){useSSL=b;}
    public boolean isUseSSL(){return useSSL;}
    public void setServerPath(String p){serverPath=p;}
    public String getServerPath(){return serverPath;}
    public boolean isLocalOnly(){return localOnly;}
    public void setLocalOnly(boolean b){localOnly=b;}
    public boolean isAutoStart(){return autoStart;}
    public void setAutoStart(boolean b){autoStart=b;}

    public static int resolvePort(Wandora wandora){
        return wandora.getOptions().getInt(OPTION_PORT,defaultPort);
    }

    protected void getDefaultContext(VelocityContext context,WandoraWebApp app){
        try{
            context.put("collectionmaker",new InstanceMaker("java.util.HashSet"));
            context.put("listmaker",new InstanceMaker("java.util.ArrayList"));
            context.put("mapmaker",new InstanceMaker("java.util.HashMap"));
            context.put("stackmaker",new InstanceMaker("java.util.Stack"));
            context.put("urlencoder",new org.wandora.piccolo.utils.URLEncoder("UTF-8"));
            context.put("javascriptencoder",new JavaScriptEncoder());
            context.put("tmbox",new TMBox());
            context.put("urlbase","/"+app.getName()+"/");
            context.put("staticbase","/"+app.getName()+"/");
            Properties p=new Properties();
            p.setProperty("textbox.shortnamelength", "70");
            context.put("textbox",new TextBox(p));
            context.put("intparser",new Integer(0));
            context.put("vhelper",new org.wandora.utils.velocity.GenericVelocityHelper());
            context.put("helper", new org.wandora.topicmap.TopicTools());
            context.put("queryrunner", new org.wandora.query2.QueryRunner());
            context.put("tmqlrunner", new org.wandora.topicmap.TMQLRunner());
            context.put("topicmap",wandora.getTopicMap());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void writeResponse(HttpServletResponse response,int code,String message){
        writeResponse(response,code,message,null);
    }
    public void writeResponse(HttpServletResponse response,int code,String message,Throwable e){
        response.setStatus(code);
        response.setContentType("text/html");
        String page="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"+
                    "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"+
                    "<title>HTTP "+code+"</title>"+
                    "</head><body>"+message;
        if(e!=null){
            StringWriter s=new StringWriter();
            PrintWriter p=new PrintWriter(s);
            e.printStackTrace(p);
            p.flush();
            page+="<br /><br />"+s.toString();
        }
        page+="</body></html>";
        response.setCharacterEncoding("UTF-8");
        try{
            response.getWriter().write(page);
        }catch(IOException ioe){ioe.printStackTrace();}
    }

    public boolean getStatic(String target,String staticPath,WandoraWebApp app,HttpServletRequest request,HttpServletResponse response) {
        try{
            String file=URLDecoder.decode(target,"UTF-8");
            while(file.startsWith("/") || file.startsWith("\\")) file=file.substring(1);
            if(file.indexOf("..")!=-1) return false;
            File f=new File(staticPath+file);
            if(!f.exists() || f.isDirectory()){
                returnNotFound(response,"File \""+file+"\" not found.");
                return true;
            }
            returnFile(response,f);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;        
    }
    public boolean getStatic(String target,WandoraWebApp app,HttpServletRequest request,HttpServletResponse response) {
        return getStatic(target,app.getStaticPath(),app,request,response);
    }

    public void returnFile(HttpServletResponse response,File f) throws IOException {
        if(!f.exists() || f.isDirectory()){
            writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found");
            return;
        }

        Object o=mimeTypes.getMimeByExtension(f.getName());
        if(o!=null) response.setContentType(o.toString());
        else response.setContentType("text/plain");
        OutputStream out=response.getOutputStream();

        InputStream in=new FileInputStream(f);
        byte[] buf=new byte[8192];
        int read=-1;
        while((read=in.read(buf))!=-1){
            out.write(buf,0,read);
        }
        in.close();
    }

    protected ArrayList<WandoraWebApp> scanWebApps(){
        ArrayList<WandoraWebApp> ret=new ArrayList<WandoraWebApp>();
        File webAppDir=new File(serverPath);
        if(webAppDir.exists()){
            File[] l=webAppDir.listFiles();
            for(int i=0;i<l.length;i++){
                if(l[i].isDirectory()){
                    File optionsFile=new File(l[i].getAbsolutePath()+File.separator+"config.xml");
                    if(optionsFile.exists()){
                        WandoraWebApp app=new WandoraWebApp(l[i].getName(),this);
                        app.readOptions();
                        ret.add(app);
                    }
                }
            }
        }
        return ret;
    }

    public class IconThread extends Thread {
        public static final int hitTime=1000;
        @Override
        public void run(){
            while(jettyServer!=null && jettyServer.isRunning()){
                synchronized(this){
                    long time=System.currentTimeMillis();
                    if(time-lastHit<hitTime) statusButton.setIcon(hitIcon);
                    else statusButton.setIcon(onIcon);
                    time=hitTime-(time-lastHit);
                    try{
                        if(time<=0) this.wait();
                        else this.wait(time);
                    }
                    catch(InterruptedException ie){}
                }
            }
        }
    }

    public Map<String,String> getOpenUrls() {
        Map<String,String> urlMap = new LinkedHashMap();
        WandoraWebApp app = null;
        for(String appName : webApps.keySet()) {
            app = webApps.get(appName);
            if(!app.isEnabled()) appName = "["+appName+"]";
            String url = "http://127.0.0.1:"+getPort()+"/"+appName;
            String openPath = app.getOpenPath();
            if(openPath != null) url = url + "/" + openPath;
            urlMap.put(appName, url);
        }
        return urlMap;
    }
    

/*    public class MockupRequest {
        public String[] params;
        public MockupRequest(String[] params){
            this.params=params;
        }
        public String getParameter(String name){
            return getParamValue(name,params);
        }
    }*/
    

}
