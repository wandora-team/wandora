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

package org.wandora.application;


import org.wandora.application.tools.browserextractors.*;

import org.wandora.utils.Options;
import org.wandora.utils.XMLbox;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import org.wandora.topicmap.*;
import org.wandora.utils.velocity.*;
import org.wandora.application.gui.UIBox;
import org.wandora.utils.Base64;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;


/**
 * <p>
 * WandoraJettyServer implements Wandora's embedded HTTP server used to
 * browse Wandora stored topics and associations with external WWW browser.
 * Implementation uses Jetty HTTP server.
 * </p>
 * <p>
 * Firefox plugin communicates with Wandora using the WandorJettyServer
 * implementation too.
 * </p>
 * 
 * @deprecated 
 * @see WandoraHttpServer
 * @author olli
 */
public class WandoraJettyServer {

    private static final int defaultPort=8898;
    
    public static final String OPTION_AUTOSTART="httpserver.autostart";
    public static final String OPTION_PORT="httpserver.port";
    public static final String OPTION_LOCALONLY="httpserver.localonly";
    public static final String OPTION_STATICPATH="httpserver.staticpath";
    public static final String OPTION_TEMPLATEPATH="httpserver.templatepath";
    public static final String OPTION_TEMPLATEFILE="httpserver.templatefile";
    public static final String OPTION_USERNAME="httpserver.username";
    public static final String OPTION_PASSWORD="httpserver.password";
    
    private int port;
    private String loginUser;
    private String loginPass;
    private Wandora wandora;
    private String staticPath;
    private String templateFile;
    private String templatePath;
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
    private MimeTypes mimeTypes;
    
    private BrowserExtractorManager extractorManager;
    
    
    public WandoraJettyServer(Wandora wandora){
        this.wandora=wandora;
        
        Options options=wandora.getOptions();
        readOptions(options);
        requestHandler=new JettyHandler();
        
        mimeTypes=new MimeTypes();
        
        extractorManager=new BrowserExtractorManager(wandora);  
    }
    
    
    private class JettyHandler extends AbstractHandler {
        public void handle(String target, Request rqst, HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
            if(localOnly && !request.getRemoteAddr().equals("127.0.0.1")) {
                ((Request)request).setHandled(true);
                return;
            }
            if(loginUser!=null && loginUser.length()>0){
                String auth=request.getHeader("Authorization");
                boolean ok=false;
                if(auth!=null){
                    int ind=auth.indexOf(" ");
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
            if(getPage(target,request,response)){
                ((Request)request).setHandled(true);                
            }
        }
    }
    
    
    public void readOptions(Options options){
        localOnly=!options.isFalse(OPTION_LOCALONLY);
        autoStart=options.isTrue(OPTION_AUTOSTART);
        port=options.getInt(OPTION_PORT);
        staticPath=options.get(OPTION_STATICPATH,"resources/gui/server/");
        templatePath=options.get(OPTION_TEMPLATEPATH,"resources/gui/server/templates/");
        templateFile=options.get(OPTION_TEMPLATEFILE,"viewtopic.vhtml");
        loginUser=options.get(OPTION_USERNAME);
        loginPass=options.get(OPTION_PASSWORD);
        if(loginUser!=null && loginUser.length()==0) loginUser=null;
    }
    
    
    public void writeOptions(Options options){
        options.put(OPTION_LOCALONLY, ""+localOnly);
        options.put(OPTION_AUTOSTART, ""+autoStart);
        options.put(OPTION_PORT, ""+port);
        options.put(OPTION_STATICPATH, staticPath);
        options.put(OPTION_TEMPLATEPATH, templatePath);
        options.put(OPTION_TEMPLATEFILE, templateFile);
        options.put(OPTION_USERNAME, (loginUser==null?"":loginUser));
        options.put(OPTION_PASSWORD, (loginPass==null?"":loginPass));
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
            jettyServer=new Server(port);

            HttpConfiguration httpConfiguration = new HttpConfiguration();
            ConnectionFactory c = new HttpConnectionFactory(httpConfiguration);

            ServerConnector serverConnector = new ServerConnector(jettyServer, c);
            serverConnector.setPort(port);
            
            jettyServer.setConnectors(new Connector[] { serverConnector });

            jettyServer.setHandler(requestHandler);
            jettyServer.start();
        
            if(statusButton!=null){
                updateStatusIcon();
                iconThread=new IconThread();
                iconThread.start();
            }
        }
        catch(Exception e){
            wandora.handleError(e);
        }
    }
    
    
    public void stopServer(){
        try{
            if(jettyServer!=null) {
                jettyServer.stop();
            }
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
    
    
    public void setLoginUser(String s){loginUser=s;}
    public String getLoginUser(){return loginUser;}
    public void setLoginPassword(String s){loginPass=s;}
    public String getLoginPassword(){return loginPass;}
    public void setLogin(String u,String p){loginUser=u;loginPass=p;}
    public void setPort(int p){port=p;}
    public int getPort(){return port;}
    public void setStaticPath(String p){staticPath=p;}
    public String getStaticPath(){return staticPath;}
    public void setTemplateFile(String p){templateFile=p;}
    public String getTemplateFile(){return templateFile;}
    public void setTemplatePath(String p){templatePath=p;}
    public String getTemplatePath(){return templatePath;}
    public boolean isLocalOnly(){return localOnly;}
    public void setLocalOnly(boolean b){localOnly=b;}
    public boolean isAutoStart(){return autoStart;}
    public void setAutoStart(boolean b){autoStart=b;}
    
    
    public static int resolvePort(Wandora wandora){
        return wandora.getOptions().getInt(OPTION_PORT,defaultPort);
    }
    
    
    protected void getDefaultContext(VelocityContext context){
        try{
            context.put("collectionmaker",new InstanceMaker("java.util.HashSet"));
            context.put("listmaker",new InstanceMaker("java.util.ArrayList"));
            context.put("mapmaker",new InstanceMaker("java.util.HashMap"));
            context.put("stackmaker",new InstanceMaker("java.util.Stack"));
            context.put("urlencoder",new org.wandora.utils.velocity.URLEncoder("UTF-8"));
            context.put("javascriptencoder",new JavaScriptEncoder());
            context.put("tmbox",new TMBox());
            context.put("urlbase","topic");
            context.put("staticbase","");
            Properties p=new Properties();
            p.setProperty("textbox.shortnamelength", "70");
            context.put("textbox",new TextBox(p));
            context.put("intparser",new Integer(0));
            context.put("vhelper",new org.wandora.utils.velocity.GenericVelocityHelper());
            context.put("helper", new org.wandora.topicmap.TopicTools());
            context.put("topicmap",wandora.getTopicMap());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    protected void writeResponse(HttpServletResponse response,int code,String message){
        writeResponse(response,code,message,null);
    }
    
    
    protected void writeResponse(HttpServletResponse response,int code,String message,Throwable e){
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
    
    
    protected PrintWriter startPluginResponse(HttpServletResponse response,int code,String text){
        try{
            PrintWriter writer=new PrintWriter(new OutputStreamWriter(response.getOutputStream(),"UTF-8"));
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<wandoraplugin>");
            writer.println("  <resultcode>"+code+"</resultcode>");
            writer.println("  <resulttext>"+text+"</resulttext>");
            return writer;
        } catch(IOException ioe){ioe.printStackTrace(); return null;}
    }
    
    
    protected boolean getPage(String target,HttpServletRequest request,HttpServletResponse response) {
        try{
            if(target.equals("/topic")){
                VelocityContext context=new VelocityContext();

                if(velocityEngine==null){
                    velocityEngine = new VelocityEngine();
                    velocityEngine.setProperty("file.resource.loader.path", templatePath );
                    velocityEngine.init();
                }
                Template vTemplate=null;
                
                try{
                    vTemplate=velocityEngine.getTemplate(templateFile,"UTF-8");
                }catch(Exception e){
                    e.printStackTrace();
                    writeResponse(response,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Exception processing template",e);
                    return true;                    
                }

                String temp;
                Topic topic=null;
                String si=request.getParameter("topic");
                if(si==null || si.length()==0) si=request.getParameter("si");
                if(si==null || si.length()==0) {
                    
                    String sl=request.getParameter("sl");
                    if(sl==null || sl.length()==0){
                        topic=wandora.getOpenTopic();
                        if(topic==null){
                            writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Wandora application does not have any topic open.");
                            return true;
                        }
                    }
                    else{
                        topic=wandora.getTopicMap().getTopicBySubjectLocator(sl);
                        if(topic==null){
                        writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Topic with subject locator "+sl+" not found.");
                        return true;
                        }
                    }
                }
                else {
                    topic=wandora.getTopicMap().getTopic(si);
                    if(topic==null){
                        writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Topic with subject identifier "+si+" not found.");
                        return true;
                    }
                }

                int pagenum=1;
                temp=request.getParameter("page");
                if(temp!=null){
                    try{
                        pagenum=Integer.parseInt(temp);
                    }catch(NumberFormatException e){}
                }
                TopicFilter filter=new TopicFilter();

                temp=request.getParameter("lang");
                if(temp==null) temp="en";
                String lang=temp;

                context.put("topic",topic);
                context.put("filter",filter);
                context.put("request",request);
                context.put("page",new Integer(pagenum));
                context.put("lang",lang);
                context.put("wandora",wandora);
                context.put("manager",wandora);

                getDefaultContext(context);

                response.setCharacterEncoding("UTF-8");
                Writer writer=response.getWriter();
                try{
                    vTemplate.merge(context, writer);
                }
                catch(Exception e){
                    e.printStackTrace();
                    writeResponse(response,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Exception processing template",e);                    
                    return true;
                }
                writer.flush();
                return true;
            }
            else if(target.equals("/plugin")){
                String content=request.getParameter("content");
                String page=request.getParameter("page");
                String selectionStart=request.getParameter("selectionStart");
                String selectionEnd=request.getParameter("selectionEnd");
                String selectionText=request.getParameter("selectionText");
                int sStart=-1;
                int sEnd=-1;
                try{
                    if(selectionStart!=null && selectionStart.length()>0) sStart=Integer.parseInt(selectionStart);
                    if(selectionEnd!=null && selectionEnd.length()>0) sEnd=Integer.parseInt(selectionEnd);
                }
                catch(NumberFormatException nfe){nfe.printStackTrace();}
                String action=request.getParameter("action");
                String app=request.getParameter("application");
                if(action==null || action.length()==0) action="doextract";
                if(action.equalsIgnoreCase("getextractors")){
                    response.setContentType("text/xml");
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter writer=startPluginResponse(response,0,"OK");
                    
                    BrowserExtractRequest extractRequest=new BrowserExtractRequest(page, content, null, app,sStart,sEnd,selectionText);
                    
                    String[] methods=extractorManager.getExtractionMethods(extractRequest);
                    
                    for(int i=0;i<methods.length;i++){
                        writer.println("  <method>"+methods[i]+"</method>");
                    }
                    
                    writer.println("</wandoraplugin>");
                    writer.flush();
                    return true;
                }
                else if(action.equalsIgnoreCase("doextract")){
                    String method=request.getParameter("method");
                    if(method!=null && method.length()>0){
                        String[] methods=method.split(";");
                        String message=null;
                        for(String m : methods){
                            BrowserExtractRequest extractRequest=new BrowserExtractRequest(page, content, m, app,sStart,sEnd,selectionText);
                            message=extractorManager.doPluginExtract(extractRequest);
                            if(message!=null && message.startsWith(BrowserPluginExtractor.RETURN_ERROR)){
                                break;
                            }
                        }
                        response.setContentType("text/xml");
                        response.setCharacterEncoding("UTF-8");
                        PrintWriter writer;
                        if(message==null || !message.startsWith(BrowserPluginExtractor.RETURN_ERROR)){
                            writer=startPluginResponse(response,0,"OK");
                            if(message!=null){
                                int ind=message.indexOf(" ");
                                if(ind>0 && ind<10) message=message.substring(ind+1);
                                writer.print("<returnmessage>");
                                writer.print(XMLbox.cleanForXML(message));
                                writer.println("</returnmessage>");
                            }
                        }
                        else{
                            writer=startPluginResponse(response,3,message);
                        }
                        writer.println("</wandoraplugin>");
                        writer.flush();
                        return true;                
                    }
                    else{
                        PrintWriter writer=startPluginResponse(response,2,"No method provided for doextract");
                        writer.println("</wandoraplugin>");
                        writer.flush();
                        return true;
                    }
                }
                else{
                    response.setContentType("text/xml");
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter writer=startPluginResponse(response,1,"Invalid action");
                    writer.println("</wandoraplugin>");
                    writer.flush();
                    return true;
                }
/*                System.out.println("plugin request, page="+page);
                System.out.println("  selectionSrart="+selectionStart);
                System.out.println("  selectionEnd="+selectionEnd);
                if(selection!=null) System.out.println("  selection="+selection);
                System.out.println("  contentLength="+content.length());
                System.out.println("  content="+content);
                return false;*/
            }
            else{
                String file=URLDecoder.decode(target,"UTF-8");
                while(file.startsWith("/") || file.startsWith("\\")) file=file.substring(1);
                if(file.indexOf("..")!=-1) return false;
                File f=new File(staticPath+file);
                if(!f.exists() || f.isDirectory()){
                    writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />File \""+file+"\" not found.<br />To open the current topic in Wandora application click <a href=\"/topic\">here</a>.");                    
                    return true;
                }
                returnFile(response,f);
                return true;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
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


    public class TopicFilter {
        public boolean topicVisible(Topic t) {return true;}
        public boolean associationVisible(Association a) {return true;}
        public Collection filterTopics(Collection topics) {return topics;}
        public Collection filterAssociations(Collection associations) {return associations;}
    }

}
