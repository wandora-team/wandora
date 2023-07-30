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

import java.io.IOException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet;
import org.wandora.modules.servlet.ServletModule;
import org.wandora.utils.ListenerList;
import org.wandora.utils.ParallelListenerList;

import jakarta.servlet.ServletException;

/**
 *
 * @author olli
 */


public class WandoraServletModule extends AbstractModule implements ServletModule {

    protected JettyHandler jettyHandler;
    protected WandoraModulesServer server;
    
    protected final ParallelListenerList<ServletModule.RequestListener> requestListeners=new ParallelListenerList<ServletModule.RequestListener>(ServletModule.RequestListener.class);
    
    public WandoraServletModule(WandoraModulesServer server){
        this.server=server;
        jettyHandler=new JettyHandler();
    }
    
    public Handler getJettyHandler(){
        return jettyHandler;
    }
    
    @Override
    public void addRequestListener(RequestListener listener) {
        requestListeners.addListener(listener);
    }

    @Override
    public void removeRequestListener(RequestListener listener) {
        requestListeners.removeListener(listener);
    }

    @Override
    public String getServletURL() {
        if(server.isUseSSL()) {
            return "https://127.0.0.1:"+server.getPort()+"/";
        }
        else {
            return "http://127.0.0.1:"+server.getPort()+"/";
        }
    }

    @Override
    public String getContextPath() {
        return server.getServerPath();
    }
    
    
    private class JettyHandler extends AbstractHandler {

        @Override
		public void handle(
				String target, 
				Request rqst, 
				jakarta.servlet.http.HttpServletRequest request,
				jakarta.servlet.http.HttpServletResponse response)
				throws IOException, jakarta.servlet.ServletException {
        	
            try {
                final ServletException[] se = new ServletException[1];
                final IOException[] ioe = new IOException[1];
                final ActionException[] ae = new ActionException[1];
                final boolean[] handledA = new boolean[]{false};

                final ModulesServlet.HttpMethod method = ModulesServlet.HttpMethod.valueOf(request.getMethod());

                requestListeners.forEach(new ListenerList.EachDelegate<ServletModule.RequestListener>() {
                    private boolean handled=false;
                    @Override
                    public void run(ServletModule.RequestListener listener, Object... params) {
                        if(handled) return;
                        try {
                            handled=listener.handleRequest(request, response, method, null);
                            if(handled) handledA[0]=true;
                        }
                        catch(ServletException ex) {
                            handled=true;
                            se[0]=ex;
                        }
                        catch(IOException ex) {
                            handled=true;
                            ioe[0]=ex;
                        }
                        catch(ActionException ex) {
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
            }
            catch(ServletException | IOException e){
                server.log.error(e);
            }
            
        }

        
    }
}
