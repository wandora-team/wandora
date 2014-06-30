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

package org.wandora.modules.servlet;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.ModuleManager.ModuleSettings;
import org.wandora.modules.servlet.ServletModule.RequestListener;
import org.wandora.utils.ListenerList;
import org.wandora.utils.ParallelListenerList;

/**
 * A HttpServlet that sets up the modules framework. This is the actual
 * servlet implementation that the servlet container interfaces with. It sets up
 * the modules framework at servlet initialisation and adds some basic modules
 * into it before loading the configuration file and initialising all other
 * modules. It sets a root ServletModule, with the module name rootServlet, so
 * that actions can then interface with the actual servlet container. It also
 * sets the variable servletHome to what should be the WEB-INF directory of
 * the webapp. After this, it loads the modules framework configuration file from
 * a file specified in the servlet configuration init parameter modulesconfig.
 * If not specified, it tries modulesconfig.xml as the default value.
 * 
 * @author olli
 */

public class ModulesServlet extends HttpServlet{

    protected final ParallelListenerList<RequestListener> requestListeners=new ParallelListenerList<RequestListener>(RequestListener.class);
    protected ModuleManager moduleManager;
    protected ServletModule servletModule=new _ServletModule();
    
    public static final String DEFAULT_BIND_ADDRESS="http://localhost:8080";
    protected String bindAddress=null;
    
    protected String servletHome;
    
    public void addRequestListener(RequestListener listener){
        requestListeners.addListener(listener);
    }
    
    public void removeRequestListener(RequestListener listener){
        requestListeners.removeListener(listener);
    }
    
    @Override
    public void destroy() {
        moduleManager.stopAllModules();
        super.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        moduleManager=new ModuleManager();
        
        servletHome=config.getServletContext().getRealPath("/")+"WEB-INF";
        moduleManager.setVariable("servletHome", servletHome);
        
//        SimpleLog log=new SimpleLog("ModulesServlet");
//        log.setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        Log log=new Jdk14Logger("ModulesServlet");
        moduleManager.setLogging(log);
        String configFile=config.getInitParameter("modulesconfig");
        if(configFile==null) {
            configFile=config.getInitParameter("modulesConfig");
            if(configFile==null) configFile="modulesconfig.xml";
        }
        
        bindAddress=config.getInitParameter("bindaddress");
        if(bindAddress==null){
            bindAddress=config.getInitParameter("bindAddress");
        }
        
        moduleManager.addModule(servletModule,new ModuleSettings("rootServlet"));                
        moduleManager.readXMLOptionsFile(configFile);
        
        if(bindAddress==null){
            bindAddress=moduleManager.getVariable("bindAddress");
            if(bindAddress==null) bindAddress=DEFAULT_BIND_ADDRESS;
        }
        
        try{
            moduleManager.autostartModules();
        }catch(ModuleException me){
            throw new ServletException("Unable to start modules.",me);
        }
    }

    protected void returnNotHandled(HttpServletRequest req, HttpServletResponse resp,HttpMethod method) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    protected void doMethod(final HttpServletRequest req, final HttpServletResponse resp,final HttpMethod method) throws ServletException, IOException {
        final ServletException[] se=new ServletException[1];
        final IOException[] ioe=new IOException[1];
        final ActionException[] ae=new ActionException[1];
        final boolean[] handledA=new boolean[]{false};
        
        requestListeners.forEach(new ListenerList.EachDelegate<RequestListener>(){
            private boolean handled=false;
            @Override
            public void run(RequestListener listener, Object... params) {
                if(handled) return;
                try{
                    handled=listener.handleRequest(req, resp, method, null);
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
        
        if(!handledA[0]) returnNotHandled(req,resp,method);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException  {
        doMethod(req, resp, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doMethod(req, resp, HttpMethod.POST);
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doMethod(req, resp, HttpMethod.DELETE);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doMethod(req, resp, HttpMethod.PUT);
    }

    public static enum HttpMethod {
        GET,POST,DELETE,HEAD,OPTIONS,PUT,TRACE
    }
    
    public class _ServletModule extends AbstractModule implements ServletModule {
        @Override
        public void addRequestListener(RequestListener listener){ ModulesServlet.this.addRequestListener(listener); }
        @Override
        public void removeRequestListener(RequestListener listener){ ModulesServlet.this.removeRequestListener(listener); }

        @Override
        public String getServletURL() {
            String path=ModulesServlet.this.getServletContext().getContextPath();
            return bindAddress+path;
        }

        @Override
        public String getContextPath() {
            return servletHome+"/";
        }
        
    }
    
}
