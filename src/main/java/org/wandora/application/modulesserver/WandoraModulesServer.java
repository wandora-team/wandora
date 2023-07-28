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
package org.wandora.application.modulesserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.modules.LoggingModule;
import org.wandora.utils.Options;

/**
 *
 * @author olli
 */


public class WandoraModulesServer {
    private static final int defaultPort=8898;

    public static final String OPTION_AUTOSTART="httpmodulesserver.autostart";
    public static final String OPTION_PORT="httpmodulesserver.port";
    public static final String OPTION_LOCALONLY="httpmodulesserver.localonly";
    public static final String OPTION_SERVERPATH="httpmodulesserver.serverpath";
    public static final String OPTION_USESSL="httpmodulesserver.usessl";
    public static final String OPTION_KEYSTORE_FILE="httpmodulesserver.keystore.file";
    public static final String OPTION_KEYSTORE_PASSWORD="httpmodulesserver.keystore.password";
    public static final String OPTION_LOGLEVEL="httpmodulesserver.loglevel";
    
    private Wandora wandora;
    
    private int port;
    
    private boolean useSSL;
    private String keystoreFile;
    private String keystorePassword;
    
    private String serverPath;
    private boolean localOnly=true;
    private boolean autoStart=false;    
    
    private javax.swing.JButton statusButton;
    private javax.swing.Icon onIcon;
    private javax.swing.Icon offIcon;
    private javax.swing.Icon hitIcon;

    private Thread iconThread;
    private long lastHit;
    
    protected Log rootLog;
    protected Log log;
    protected int logLevel=LoggingModule.SubLog.LOG_WARN;
    
    protected Server jettyServer;
    protected ServerConnector serverConnector;
    protected MimeTypes mimeTypes;

    protected WandoraModuleManager moduleManager;
    protected WandoraServletModule servletModule;

    
    
    public WandoraModulesServer(Wandora wandora){
        this.wandora = wandora;
        jettyServer = new Server();

        rootLog = new Jdk14Logger("ModulesServlet");
        log = rootLog;

        Options options = wandora.getOptions();
        readOptions(options);
        
        mimeTypes = new MimeTypes();
        
        initModuleManager();
        readBundleDirectories();
    }
    
    
    public void initModuleManager() {
        moduleManager = new WandoraModuleManager(this);
        moduleManager.setLogging(log);
        moduleManager.setVariable("home", getServerPath());
        
        log.info("Reading modules server base config");
        File f = new File(getServerPath(),"baseconfig.xml");
        if(f.exists()) {
            try {
                moduleManager.readXMLOptionsFile(f.getCanonicalPath());
            } catch(IOException ioe){
                log.error("Unable to read base config for modules server",ioe);
            }
        }
        else log.warn("baseconfig.xml not found for modules server");
        
        servletModule=new WandoraServletModule(this);
        moduleManager.addModule(servletModule,"rootServlet");
        moduleManager.addModule(new JettyModule(jettyServer),"jetty");
    }
    
    
    public void readBundleDirectories(){
        log.info("Scanning bundles");
        try {
            moduleManager.readBundles();
            moduleManager.initAllModules();
        } catch(Exception e){
            log.error("Unable to read bundles", e);
        }
    }
    
    
    public Server getJetty() {
        return jettyServer;
    }

    
    public Wandora getWandora() {
        return wandora;
    }

    
    public ArrayList<ModulesWebApp> getWebApps() {
        return moduleManager.findModulesRecursive(ModulesWebApp.class);
    }
    
    
    public void readOptions(Options options) {
        localOnly =! options.isFalse(OPTION_LOCALONLY);
        autoStart = options.isTrue(OPTION_AUTOSTART);
        port = options.getInt(OPTION_PORT, defaultPort);
        serverPath = options.get(OPTION_SERVERPATH, "resources/server/");        
        try {
            serverPath = new File(serverPath).getCanonicalPath();
        } catch (IOException ex) {
            log.warn("Couldn't resolve canonical path for server",ex);
        }
        
        useSSL = options.isTrue(OPTION_USESSL);
        keystoreFile = options.get(OPTION_KEYSTORE_FILE, "conf/keystore/keystore");
        try {
            keystoreFile = new File(keystoreFile).getCanonicalPath();
        } catch (IOException ex) {
            log.warn("Couldn't resolve canonical file for keystore",ex);
        }
        keystorePassword = options.get(OPTION_KEYSTORE_PASSWORD, "wandora");
        
        setLogLevel(options.getInt(OPTION_LOGLEVEL, LoggingModule.SubLog.LOG_WARN));
    }

    
    public void writeOptions(Options options){
        options.put(OPTION_LOCALONLY, ""+localOnly);
        options.put(OPTION_AUTOSTART, ""+autoStart);
        options.put(OPTION_PORT, ""+port);
        options.put(OPTION_SERVERPATH, serverPath);
        options.put(OPTION_USESSL, ""+useSSL);
        options.put(OPTION_KEYSTORE_FILE, ""+keystoreFile);
        options.put(OPTION_KEYSTORE_PASSWORD, ""+keystorePassword);
        options.put(OPTION_LOGLEVEL, ""+logLevel);
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
        this.statusButton = button;
        this.onIcon = UIBox.getIcon(onIcon);
        this.offIcon = UIBox.getIcon(offIcon);
        this.hitIcon = UIBox.getIcon(hitIcon);
        updateStatusIcon();
    }
    

    public boolean isRunning(){
        return jettyServer != null && jettyServer.isRunning();
    }

    
    public void start() {
        try {
            if(useSSL) {
                SslContextFactory.Server sslCtxFactory = new SslContextFactory.Server();

                sslCtxFactory.setKeyStorePath(keystoreFile);
                sslCtxFactory.setKeyStorePassword(keystorePassword);
                SslConnectionFactory https = new SslConnectionFactory(sslCtxFactory, "http/1.1");
                
                HttpConfiguration httpConfiguration = new HttpConfiguration();
                httpConfiguration.setSecurePort(port);
                httpConfiguration.setSecureScheme("https");
                httpConfiguration.addCustomizer(new SecureRequestCustomizer());
                ConnectionFactory http = new HttpConnectionFactory(httpConfiguration);
                
                serverConnector = new ServerConnector(jettyServer, https, http);
                serverConnector.setPort(port);
            }
            else {
                HttpConfiguration httpConfiguration = new HttpConfiguration();
                ConnectionFactory http = new HttpConnectionFactory(httpConfiguration);
                
                serverConnector = new ServerConnector(jettyServer, http);
                serverConnector.setPort(port);
            }

            jettyServer.setConnectors(new Connector[] { serverConnector });
            jettyServer.setHandler(servletModule.getJettyHandler());
            moduleManager.autostartModules();            
            jettyServer.start();
            
            if(statusButton!=null) {
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
                jettyServer.setHandler(null);
                if(serverConnector != null) {
                    jettyServer.removeConnector(serverConnector);
                    serverConnector.close();
                    serverConnector.stop();
                }
                jettyServer.setConnectors(null);
            }

            moduleManager.stopAllModules();
            
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
    
    
    public void setPort(int p) {
        port=p;
    }
    
    public int getPort() {
        return port;
    }
    
    
    public void setUseSSL(boolean b) {
        useSSL=b;
    }
    
    public boolean isUseSSL() {
        return useSSL;
    }
    
    
    public void setKeystoreFile(String filename) {
        keystoreFile = filename;
    }
    
    
    public String getKeystoreFile() {
        return keystoreFile;
    }
    
    
    public void setKeystorePassword(String password) {
        keystorePassword = password;
    }
    
    
    public String getKeystorePassword() {
        return keystorePassword;
    }
    
    
    public void setServerPath(String p) {
        serverPath=p;
    }
    
    public String getServerPath() {
        return serverPath;
    }
    
    
    public boolean isLocalOnly() {
        return localOnly;
    }
    
    public void setLocalOnly(boolean b) {
        localOnly=b;
    }
    
    
    public boolean isAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean b) {
        autoStart=b;
    }
    
    
    public String getServletURL() {
        return servletModule.getServletURL();
    }
    
    
    public void setLogLevel(int l) {
        logLevel=l;
        if(logLevel>LoggingModule.SubLog.LOG_TRACE) log=new LoggingModule.SubLog("", rootLog, logLevel);
    }
    
    public int getLogLevel() {
        return logLevel;
    }

    
    public static int resolvePort(Wandora wandora){
        return wandora.getOptions().getInt(OPTION_PORT,defaultPort);
    }    
    
    

    // -------------------------------------------------------------------------
    
    
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


}

