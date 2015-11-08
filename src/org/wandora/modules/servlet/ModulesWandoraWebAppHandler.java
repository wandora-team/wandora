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
package org.wandora.modules.servlet;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.server.WandoraWebApp;
import org.wandora.application.server.WandoraWebAppServer;
import org.wandora.application.server.WebAppHandler;
import org.wandora.application.tools.GenericOptionsPanel;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.utils.ListenerList;
import org.wandora.utils.Options;
import org.wandora.utils.ParallelListenerList;

/**
 * A Wandora WebAppHandler that can be included in the embedded server
 * which sets up a modules framework and the interface between it and
 * Wandora. This is similar to ModulesServlet but for the embedded server instead
 * of a normal servlet container.
 * 
 * @author olli
 */
public class ModulesWandoraWebAppHandler implements WebAppHandler {

    protected final ParallelListenerList<ServletModule.RequestListener> requestListeners=new ParallelListenerList<ServletModule.RequestListener>(ServletModule.RequestListener.class);
    protected ModuleManager moduleManager;
    protected ServletModule servletModule=new _ServletModule();
    
    protected String configFile="modulesconfig.xml";
    protected String staticPath=null;

    protected WandoraWebAppServer server;
    protected WandoraWebApp app;
            
    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getStaticPath() {
        return staticPath;
    }

    public void setStaticPath(String staticPath) {
        this.staticPath = staticPath;
        if(this.staticPath!=null) {
            this.staticPath=this.staticPath.trim();
            if(this.staticPath.length()==0) this.staticPath=null;
        }
    }
    
    
    
    public void addRequestListener(ServletModule.RequestListener listener){
        requestListeners.addListener(listener);
    }
    
    public void removeRequestListener(ServletModule.RequestListener listener){
        requestListeners.removeListener(listener);
    }
    
    public Wandora getWandora(){
        return Wandora.getWandora();
    }
    
    protected void returnNotHandled(HttpServletRequest req, HttpServletResponse resp,ModulesServlet.HttpMethod method) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    
    
    @Override
    public boolean getPage(WandoraWebApp app, WandoraWebAppServer server, String target, final HttpServletRequest request, final HttpServletResponse response) {
        if(target.equals("/")){
            try{
                final ServletException[] se=new ServletException[1];
                final IOException[] ioe=new IOException[1];
                final ActionException[] ae=new ActionException[1];
                final boolean[] handledA=new boolean[]{false};

                final HttpMethod method=HttpMethod.valueOf(request.getMethod());

                requestListeners.forEach(new ListenerList.EachDelegate<ServletModule.RequestListener>(){
                    private boolean handled=false;
                    @Override
                    public void run(ServletModule.RequestListener listener, Object... params) {
                        if(handled) return;
                        try{
                            handled=listener.handleRequest(request, response, method, null);
                            if(handled) handledA[0]=true;
                        }
                        catch(ServletException ex){
                            handled=true;
                            se[0]=ex;
                        }
                        catch(IOException ex){
                            handled=true;
                            ioe[0]=ex;
                        }
                        catch(ActionException ex){
                            handled=true;
                            ae[0]=ex;
                        }
                    }
                });
                if(se[0]!=null) throw se[0];
                else if(ioe[0]!=null) throw ioe[0];
                else if(ae[0]!=null) {
                    throw new ServletException(ae[0]);
                }

                if(!handledA[0]) returnNotHandled(request,response,method);
                
                return true;
            }
            catch(ServletException | IOException e){
                server.log(e);
                return false;
            }
            
        }
        else {
            if(staticPath==null) return server.getStatic(target, app, request, response);
            else return server.getStatic(target,staticPath,app,request,response);
        }
    }

    @Override
    public void init(WandoraWebApp app, WandoraWebAppServer server, Options options) {
        this.server=server;
        this.app=app;
        if(options!=null) {
            configFile=options.get("configFile",configFile);
            staticPath=options.get("staticPath",staticPath);
            if(staticPath!=null) staticPath=staticPath.trim();
            if(staticPath!=null && staticPath.length()==0) staticPath=null;
        }
    }

    @Override
    public void save(WandoraWebApp app, WandoraWebAppServer server, Options options) {
        options.put("configFile", configFile);
        options.put("staticPath", staticPath);
    }

    @Override
    public void start(WandoraWebApp app, WandoraWebAppServer server) {
        moduleManager=new ModuleManager();
        
        try {
            String home=new java.io.File( app.getServerPath()+app.getName() ).getCanonicalPath();
            moduleManager.setVariable("home", home);
        } catch(IOException ioe){ server.log(ioe); }
        moduleManager.setVariable("port", ""+server.getPort());
        moduleManager.setVariable("urlbase","/"+app.getName()+"/");
        moduleManager.setVariable("staticbase","/"+app.getName()+"/");
        
        
//        SimpleLog log=new SimpleLog("ModulesServlet");
//        log.setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        Log log=new Jdk14Logger("ModulesServlet");
        moduleManager.setLogging(log);
        
        moduleManager.addModule(servletModule,new ModuleManager.ModuleSettings("rootServlet"));                
        String configPath;
        if(configFile.startsWith("/") || configFile.startsWith("\\")) configPath=configFile;
        else configPath=app.getPath("config")+configFile;
        moduleManager.readXMLOptionsFile(configPath);
        
        try{
            moduleManager.autostartModules();
        }catch(ModuleException me){
            server.log(me);
        }
    }

    @Override
    public void stop(WandoraWebApp app, WandoraWebAppServer server) {
        if(moduleManager!=null) {
            moduleManager.stopAllModules();
            moduleManager=null;
        }
    }

    @Override
    public ConfigComponent getConfigComponent(WandoraWebApp app, WandoraWebAppServer server) {
        return new ConfigComponent(){
            SimpleField configField;
            SimpleField staticPathField;
            public JPanel panel;
            {
                panel=new JPanel();
                GridBagConstraints gbc=GenericOptionsPanel.makeGBC();
                gbc.anchor=GridBagConstraints.WEST;
                gbc.insets=new java.awt.Insets(0,5,0,5);
                panel.setLayout(new GridBagLayout());
                SimpleLabel myLabel = new SimpleLabel("Config file");
                myLabel.setPreferredSize(new Dimension(100,20));
                myLabel.setHorizontalAlignment(SimpleLabel.RIGHT);
                panel.add(myLabel,gbc);
                gbc.gridx=1;
                gbc.weightx=1.0;
                
                configField=new SimpleField();
                configField.setText( getConfigFile() != null ? getConfigFile() : "" );
                panel.add(configField,gbc);

                gbc.gridy=1;
                gbc.gridx=0;
                gbc.weightx=0.0;
                myLabel = new SimpleLabel("Static path");
                myLabel.setPreferredSize(new Dimension(100,20));
                myLabel.setHorizontalAlignment(SimpleLabel.RIGHT);
                panel.add(myLabel,gbc);
                gbc.gridx=1;
                gbc.weightx=1.0;
                staticPathField=new SimpleField();
                staticPathField.setText( getStaticPath() != null ? getStaticPath() : "" );
                panel.add(staticPathField,gbc);

                gbc.gridx=0;
                gbc.gridy=2;
                gbc.weightx=0.0;
                panel.add(new JPanel(),gbc);
                
               
            }

            public void accept() {
                setConfigFile(configField.getText().trim());
                setStaticPath(staticPathField.getText().trim());
            }
            public void cancel() {
            }
            public JComponent getComponent() {
                return panel;
            }
        };
    }
 
    public class _ServletModule extends AbstractModule implements ServletModule {
        @Override
        public void addRequestListener(RequestListener listener){ ModulesWandoraWebAppHandler.this.addRequestListener(listener); }
        @Override
        public void removeRequestListener(RequestListener listener){ ModulesWandoraWebAppHandler.this.removeRequestListener(listener); }

        @Override
        public String getServletURL() {
            return (server.isUseSSL() ? "https" : "http" )+"://127.0.0.1:"+server.getPort()+"/"+app.getName();
        }

        @Override
        public String getContextPath() {
            return server.getServerPath();
        }
        
        
        
    }
}
