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

package org.wandora.application.tools.server;

import java.awt.Desktop;
import java.net.URI;
import java.util.Map;

import javax.swing.Icon;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.topicpanels.webview.WebViewPanel;
import org.wandora.application.modulesserver.ModulesWebApp;
import org.wandora.application.modulesserver.WandoraModulesServer;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author olli
 */
public class HTTPServerTool extends AbstractWandoraTool {
    
	private static final long serialVersionUID = 1L;
	
	public static final int CONFIGURE=1;
    public static final int START=2;
    public static final int STOP=4;
    public static final int CONFIGURE_AND_START=CONFIGURE+START;
    public static final int UPDATE_MENU=8;
    public static final int START_AND_MENU=START+UPDATE_MENU;
    public static final int STOP_AND_MENU=STOP+UPDATE_MENU;
    public static final int OPEN_PAGE=16;
    public static final int OPEN_PAGE_IN_BROWSER_TOPIC_PANEL=32;
    
    private int mode;
    private String forceUrl = null;
    private Object param2 = null;
    private ModulesWebApp webApp = null;
    
    
    public HTTPServerTool(int mode, String fu, Object param2) {
        super();
        this.mode=mode;
        this.forceUrl = fu;
        this.param2 = param2;
    }
    
    
    public HTTPServerTool(int mode, String fu){
        super();
        this.mode=mode;
        this.forceUrl = fu;
    }

    public HTTPServerTool(int mode, ModulesWebApp webApp, Object param2) {
        super();
        this.mode=mode;
        this.webApp = webApp;
        this.param2 = param2;
    }
    
    
    public HTTPServerTool(int mode, ModulesWebApp webApp){
        super();
        this.mode=mode;
        this.webApp=webApp;
    }
    
    public HTTPServerTool(int mode){
        super();
        this.mode=mode;
    }
    
    

    @Override
    public Icon getIcon() {
        if((mode&CONFIGURE)!=0) return UIBox.getIcon("gui/icons/server_configure.png");
        if((mode&START)!=0) return UIBox.getIcon("gui/icons/server_start.png");
        if((mode&STOP)!=0) return UIBox.getIcon("gui/icons/server_stop.png");
        if((mode&OPEN_PAGE)!=0) return UIBox.getIcon("gui/icons/server_open.png");
        if((mode&OPEN_PAGE_IN_BROWSER_TOPIC_PANEL)!=0) return UIBox.getIcon("gui/icons/server_open.png");
        return super.getIcon();
    }

    @Override
    public String getName() {
        if((mode&CONFIGURE)!=0) return "HTTP server configuration";
        if((mode&START)!=0) return "Start HTTP server";
        if((mode&STOP)!=0) return "Stop HTTP server";
        if((mode&OPEN_PAGE)!=0) return "Open topic with Wandora HTTP server...";
        if((mode&OPEN_PAGE_IN_BROWSER_TOPIC_PANEL)!=0) return "Open topic in Browser topic panel";
        return "Wandora HTTP server tool";
    }
    
    @Override
    public String getDescription() {
        if((mode&CONFIGURE)!=0) return "Open Wandora HTTP server configuration dialog...";
        if((mode&START)!=0) return "Start Wandora HTTP server";
        if((mode&STOP)!=0) return "Stop Wandora HTTP server";
        if((mode&OPEN_PAGE)!=0) return "Open current topic with external browser and Wandora's HTTP server...";
        if((mode&OPEN_PAGE_IN_BROWSER_TOPIC_PANEL)!=0) return "Open current topic with Browser topic panel and Wandora's HTTP server...";
        return "Wandora HTTP server tool";
    }
    

    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        
        if((mode&CONFIGURE) != 0) {

            WandoraModulesServer server = wandora.getHTTPServer();
            
            String u=null; //s.getLoginUser();
            String p=null; //s.getLoginPassword();
            if(u==null) u="";
            if(p==null) p="";
            
            String[] logLevels={"trace","debug","info","warn","error","fatal","none"};
            
            GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Wandora HTTP server settings","Wandora HTTP server settings",true,new String[][]{
                new String[]{"Auto start","boolean",""+server.isAutoStart(),"Start server automatically when you start Wandora"},
                new String[]{"Port","string",""+server.getPort(),"Port the server is listening to"},
                new String[]{"Local only","boolean",""+server.isLocalOnly(),"Allow only local connections"},
                new String[]{"Use SSL","boolean",""+server.isUseSSL(),"Should server use SSL"},
                new String[]{"Keystore location","string",""+server.getKeystoreFile(),"Where SSL keystore file locates? Keystore is used only if you have selected to use SLL."},
                new String[]{"Keystore password","string",""+server.getKeystorePassword(),"Keystore's password. Keystore is used only if you have selected to use SLL."},
//                new String[]{"User name","string",u,"User name. Leave empty for anonymous login"},
//                new String[]{"Password","password",p,"Password for the user if user name field is used"},
                new String[]{"Server path","string",server.getServerPath(),"Path where Wandora web apps are deployed"},
//                new String[]{"Static content path","string",s.getStaticPath(),"Path where static files are located"},
//                new String[]{"Template path","string",s.getTemplatePath(),"Path where Velocity templates are located"},
//                new String[]{"Template","string",s.getTemplateFile(),"Template file used to create a topic page"},
                new String[]{"Log level","combo:"+StringUtils.join(logLevels,";"),logLevels[server.getLogLevel()],"Lowest level of log messages that are printed"},
            },wandora);
            god.setSize(800, 400);
            if(wandora != null) wandora.centerWindow(god);
            god.setVisible(true);
            
            if(god.wasCancelled()) return;

            boolean running = server.isRunning();
            if(running) server.stopServer();

            Map<String,String> values = god.getValues();
            
            server.setAutoStart(Boolean.parseBoolean(values.get("Auto start")));
            server.setPort(Integer.parseInt(values.get("Port")));
            server.setLocalOnly(Boolean.parseBoolean(values.get("Local only")));
            server.setUseSSL(Boolean.parseBoolean(values.get("Use SSL")));
            server.setKeystoreFile(values.get("Keystore location"));
            server.setKeystorePassword(values.get("Keystore password"));
//            server.setLogin(values.get("User name"),values.get("Password"));
//            server.setStaticPath(values.get("Static content path"));
//            server.setTemplatePath(values.get("Template path"));
//            server.setTemplateFile(values.get("Template"));
            server.setServerPath(values.get("Server path"));
            server.setLogLevel(ArrayUtils.indexOf(logLevels, values.get("Log level")));

            server.writeOptions(wandora.getOptions());

            server.initModuleManager();
            server.readBundleDirectories();
            
            if(running) server.start();

            wandora.menuManager.refreshServerMenu();
        }
        
        if((mode&START) != 0) {
            wandora.startHTTPServer();
        }

        else if((mode&STOP) != 0) {
            wandora.stopHTTPServer();            
        }
        
        if((mode&UPDATE_MENU) != 0) {
            wandora.menuManager.refreshServerMenu();
        }
        
        if((mode&OPEN_PAGE) !=0) {
            try {
                if(!wandora.getHTTPServer().isRunning()) {
                    int a = WandoraOptionPane.showConfirmDialog(wandora, "HTTP server is not running at the moment. Would you like to start the server first?", "Start HTTP server?", WandoraOptionPane.OK_CANCEL_OPTION);
                    if( a == WandoraOptionPane.OK_OPTION) {
                        wandora.startHTTPServer();
                        wandora.menuManager.refreshServerMenu();
                    }
                    else if( a == WandoraOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                WandoraModulesServer s = wandora.getHTTPServer();
                
                String uri = (s.isUseSSL() ? "https" : "http")+"://127.0.0.1:"+s.getPort()+"/topic";
                if(forceUrl != null) uri = forceUrl;
                else if(webApp!=null) {
                    uri=webApp.getAppStartPage();
                    if(uri==null) {
                        WandoraOptionPane.showMessageDialog(wandora, "Can't launch selected webapp. Webapp says it's URI is null.");
                        return;
                    }
                }
                
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(uri));
                }
                catch(Exception e) {
                    log(e);
                }
            }
            catch(Exception e) {
                wandora.handleError(e);
            }            
        }
        
        if((mode&OPEN_PAGE_IN_BROWSER_TOPIC_PANEL)!=0){
            try {
                if(!wandora.getHTTPServer().isRunning()) {
                    int a = WandoraOptionPane.showConfirmDialog(wandora, "HTTP server is not running at the moment. Would you like to start the server first?", "Start HTTP server?", WandoraOptionPane.OK_CANCEL_OPTION);
                    if( a == WandoraOptionPane.OK_OPTION) {
                        wandora.startHTTPServer();
                        wandora.menuManager.refreshServerMenu();
                    }
                }
                WandoraModulesServer s=wandora.getHTTPServer();
                String uri = (s.isUseSSL() ? "https" : "http")+"://127.0.0.1:"+s.getPort()+"/topic";
                if(forceUrl != null) uri = forceUrl;
                else if(webApp!=null) {
                    uri=webApp.getAppStartPage();
                    if(uri==null) {
                        WandoraOptionPane.showMessageDialog(wandora, "Can't launch selected webapp. Webapp says it's URI is null.");
                        return;
                    }
                }
                
                try {
                    if(param2 != null) {
                        if(param2 instanceof WebViewPanel) {
                            WebViewPanel browserTopicPanel = (WebViewPanel) param2;
                            browserTopicPanel.browse(uri);
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            catch(Exception e) {
                wandora.handleError(e);
            }            
        }
    }

}
