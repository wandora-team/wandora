/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
import org.wandora.utils.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import org.wandora.topicmap.*;
import org.wandora.piccolo.utils.*;
import org.wandora.application.gui.UIBox;



/**
 * <p>
 * WandoraHttpServer implements simple HTTP server application used in Wandora to
 * browse Wandora stored topics and associations with external WWW browser.
 * </p>
 * <p>
 * However, Wandora uses WandoraJettyServer instead of WandoraHttpServer for
 * embedded HTTP server. This class has been saved for optional usage.
 * </p>
 * 
 * @see WandoraJettyServer
 * 
 * @deprecated 
 * @author olli
 */
public class WandoraHttpServer extends HTTPServer {

    private static final int defaultPort=8898;
    
    public static final String OPTION_AUTOSTART="httpserver.autostart";
    public static final String OPTION_PORT="httpserver.port";
    public static final String OPTION_LOCALONLY="httpserver.localonly";
    public static final String OPTION_STATICPATH="httpserver.staticpath";
    public static final String OPTION_TEMPLATEPATH="httpserver.templatepath";
    public static final String OPTION_TEMPLATEFILE="httpserver.templatefile";
    public static final String OPTION_USESSL="httpserver.usessl";
    public static final String OPTION_USERNAME="httpserver.username";
    public static final String OPTION_PASSWORD="httpserver.password";
    
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
    
    public WandoraHttpServer(Wandora wandora){
        super(resolvePort(wandora));
        this.wandora=wandora;
        
        Options options=wandora.getOptions();
        readOptions(options);
    }
    
    public void readOptions(Options options){
        localOnly=!options.isFalse(OPTION_LOCALONLY);
        autoStart=options.isTrue(OPTION_AUTOSTART);
        port=options.getInt(OPTION_PORT);
        staticPath=options.get(OPTION_STATICPATH,"resources/gui/server/");
        templatePath=options.get(OPTION_TEMPLATEPATH,"resources/gui/server/templates/");
        templateFile=options.get(OPTION_TEMPLATEFILE,"viewtopic.vhtml");
        useSSL=options.isTrue(OPTION_USESSL);
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
        options.put(OPTION_USESSL, ""+useSSL);
        options.put(OPTION_USERNAME, (loginUser==null?"":loginUser));
        options.put(OPTION_PASSWORD, (loginPass==null?"":loginPass));
    }
    
    private void updateStatusIcon(){
        if(running) {
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
    
    @Override
    public void start(){
        super.start();
        
        if(statusButton!=null){
            updateStatusIcon();
            iconThread=new IconThread();
            iconThread.start();
        }
    }
    
    @Override
    public void stopServer(){
        super.stopServer();
        while(iconThread!=null && iconThread.isAlive()){
            iconThread.interrupt();
            try{
                iconThread.join();
            }catch(InterruptedException ie){}
        }
        if(statusButton!=null) updateStatusIcon();
        iconThread=null;
    }
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
            context.put("urlencoder",new org.wandora.piccolo.utils.URLEncoder("UTF-8"));
            context.put("javascriptencoder",new JavaScriptEncoder());
            context.put("tmbox",new TMBox());
            context.put("urlbase","topic");
            context.put("staticbase","");
            Properties p=new Properties();
            p.setProperty("textbox.shortnamelength", "70");
            context.put("textbox",new TextBox(p));
            context.put("intparser",new Integer(0));
            context.put("helper",new org.wandora.utils.velocity.GenericVelocityHelper());
            context.put("topicmap",wandora.getTopicMap());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    protected void handleRequest(Socket s) throws IOException {
        if(localOnly && !s.getInetAddress().isLoopbackAddress()) {
            s.close();
        }
        else {
            if(iconThread!=null && iconThread.isAlive()) {
                synchronized(iconThread){
                    lastHit=System.currentTimeMillis();
                    iconThread.notify();
                }
            }
            super.handleRequest(s);
        }
    }
    
    @Override
    protected boolean getPage(OutputStream out, String[] parameters) {
        try{
            if(parameters[0].equals("/topic")){
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
                    writeInternalServerError(out, "Exception processing template", e);
                    return true;                    
                }

                String temp;
                String si=getParamValue("topic",parameters);
                Topic topic=null;
                if(si==null || si.length()==0) {
                    topic=wandora.getOpenTopic();
                    if(topic==null){
                        try{
                            writeSimpleHTML(out,"404 Not Found","404 Not Found<br />Wandora application does not have any topic open.");
                        }catch(IOException ioe){ioe.printStackTrace();}
                        return true;
                    }
                }
                else {
                    topic=wandora.getTopicMap().getTopic(si);
                    if(topic==null){
                        try{
                            writeSimpleHTML(out,"404 Not Found","404 Not Found<br />Topic with subject identifier "+si+" not found.");
                        }catch(IOException ioe){ioe.printStackTrace();}
                        return true;
                    }
                }

                int pagenum=1;
                temp=getParamValue("page",parameters);
                if(temp!=null){
                    try{
                        pagenum=Integer.parseInt(temp);
                    }catch(NumberFormatException e){}
                }
                MockupRequest request=new MockupRequest(parameters);
                TopicFilter filter=new TopicFilter();

                temp=getParamValue("lang",parameters);
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

                Writer writer=new OutputStreamWriter(out,"UTF-8");
                try{
                    vTemplate.merge(context, writer);
                }
                catch(Exception e){
                    e.printStackTrace();
                    writeInternalServerError(out, "Exception processing template", e);
                    return true;
                }
                writer.flush();
                return true;
            }
            else if(parameters[0].equals("/plugin")){
                org.wandora.application.gui.WandoraOptionPane.showMessageDialog(wandora, getParamValue("page",parameters));
                return false;
            }
            else{
                String file=URLDecoder.decode(parameters[0],"UTF-8");
                while(file.startsWith("/") || file.startsWith("\\")) file=file.substring(1);
                if(file.indexOf("..")!=-1) return false;
                File f=new File(staticPath+file);
                if(!f.exists() || f.isDirectory()){
                    try{
                        writeSimpleHTML(out,"404 Not Found","404 Not Found<br />File \""+file+"\" not found.<br />To open the current topic in Wandora application click <a href=\"/topic\">here</a>.");
                    }catch(IOException ioe){ioe.printStackTrace();}
                    return true;
                }
                returnFile(out,f);
                return true;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    public class IconThread extends Thread {
        public static final int hitTime=1000;
        @Override
        public void run(){
            while(running){
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

    public class MockupRequest {
        public String[] params;
        public MockupRequest(String[] params){
            this.params=params;
        }
        public String getParameter(String name){
            return getParamValue(name,params);
        }
    }
    
    public class TopicFilter {
        public boolean topicVisible(Topic t) {return true;}
        public boolean associationVisible(Association a) {return true;}
        public Collection filterTopics(Collection topics) {return topics;}
        public Collection filterAssociations(Collection associations) {return associations;}
    }
}
