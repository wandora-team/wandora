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
package org.wandora.application.modulesserver;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.AxisServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.servlet.ServletHolder;
import org.wandora.application.Wandora;
import org.wandora.application.server.topicmapservice.TopicMapService;
import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;


/**
 *
 * @author olli
 */



public class Axis2Service extends AbstractModule {

    public static final String ROOT_NAME=TopicMapService.ROOT_NAME;
    
    protected String layerName=ROOT_NAME;
    protected Context jettyContext;
    protected JettyModule jetty;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        manager.requireModule(this, JettyModule.class, deps);
        return deps;
    }
    
    
    
    @Override
    public void stop(ModuleManager manager) {
        Server server=jetty.getJetty();
        boolean running=server.isRunning();
        if(running) try{server.stop();}catch(Exception e){e.printStackTrace();}
        
//        server.removeHandler(jettyContext);
        
        if(running) try{server.start();}catch(Exception e){e.printStackTrace();}
        
        jetty=null;
        
        super.stop(manager); 
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        try{
            jetty=manager.findModule(this, JettyModule.class);
            
            TopicMapService.wandora=Wandora.getWandora();
            TopicMapService.layerName=layerName;
            TopicMapService.tm=null;

            ConfigurationContext context=ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,null);
            AxisConfiguration axisConfig=context.getAxisConfiguration();
//            axisConfig.addParameter("httpContentNegotiation", "true");

            axisConfig.addMessageFormatter("application/json", new org.apache.axis2.json.JSONMessageFormatter());
            axisConfig.addMessageFormatter("application/json/badgerfish", new org.apache.axis2.json.JSONBadgerfishMessageFormatter());
            axisConfig.addMessageFormatter("text/javascript", new org.apache.axis2.json.JSONMessageFormatter());

            axisConfig.addMessageBuilder("application/json", new org.apache.axis2.json.JSONOMBuilder());
            axisConfig.addMessageBuilder("application/json/badgerfish", new org.apache.axis2.json.JSONBadgerfishOMBuilder());
            axisConfig.addMessageBuilder("text/javascript", new org.apache.axis2.json.JSONOMBuilder());

            AxisService service=AxisService.createService(TopicMapService.class.getName(), axisConfig);
            axisConfig.addService(service);

            AxisServlet axisServlet=new AxisServlet(){
                @Override
                protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
                    super.doGet(request, response);
                    response.getOutputStream().flush();
                }
            };
            
            String appName="axis";
            String relativeHome=manager.getVariable("relativeHome");
            if(relativeHome!=null){
                appName=relativeHome;
                if(appName.endsWith(File.separator)) appName=appName.substring(0,appName.length()-1);
            }

            ServletHolder holder=new ServletHolder(axisServlet);
            
            
//            jettyContext=new Context(jetty.getJetty(),"/"+appName,Context.SESSIONS);
//            jettyContext.getServletContext().setAttribute(AxisServlet.CONFIGURATION_CONTEXT, context);
//            jettyContext.addServlet(holder,"/*");


        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        
        super.start(manager);
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("layerName");
        if(o!=null) layerName=o.toString().trim();
        
        super.init(manager, settings);
    }
    
}
