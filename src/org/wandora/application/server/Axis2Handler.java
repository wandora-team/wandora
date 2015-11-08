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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;

import org.wandora.utils.Options;
import org.wandora.application.tools.GenericOptionsPanel;
import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.layered.*;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.servlet.ServletHolder;


import org.wandora.application.server.topicmapservice.TopicMapService;

/**
 *
 * @author olli
 */
public class Axis2Handler implements WebAppHandler {

//    public SimpleHTTPServer server;

    public static final String ROOT_NAME=TopicMapService.ROOT_NAME;

    public Context jettyContext;
    public WandoraWebApp webApp;
    public String layer;

    public Axis2Handler(){
    }

    public boolean getPage(WandoraWebApp app, WandoraWebAppServer server, String target, HttpServletRequest request, HttpServletResponse response) {
        return false;
    }

    public void init(WandoraWebApp app, WandoraWebAppServer server, Options options) {
        layer=options.get("layer",ROOT_NAME);
    }

    public void start(WandoraWebApp app, WandoraWebAppServer server){
        try{
            TopicMapService.wandora=server.getWandora();
            TopicMapService.layerName=layer;
            TopicMapService.tm=null;
            webApp=app;

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

//            ServletHolder holder=new ServletHolder(axisServlet);
//            jettyContext=new Context(server.getJetty(),"/"+app.getName(),Context.SESSIONS);
//            jettyContext.getServletContext().setAttribute(AxisServlet.CONFIGURATION_CONTEXT, context);
//            jettyContext.addServlet(holder,"/*");

//            SimpleHTTPServer simple=new SimpleHTTPServer(context,8900);
//            simple.start();


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public ConfigComponent getConfigComponent(WandoraWebApp app, final WandoraWebAppServer server) {
        final Wandora wandora=server.getWandora();
        return new ConfigComponent(){
            GenericOptionsPanel gop;
            JComboBox comboBox;
            public JPanel panel;
            {
                panel=new JPanel();
                GridBagConstraints gbc=gop.makeGBC();
                gbc.anchor=gbc.WEST;
                gbc.insets=new java.awt.Insets(0,5,0,5);
                panel.setLayout(new GridBagLayout());
                SimpleLabel myLabel = new SimpleLabel("Exported layer");
                myLabel.setPreferredSize(new Dimension(100,20));
                myLabel.setHorizontalAlignment(SimpleLabel.RIGHT);
                panel.add(myLabel,gbc);
                gbc.gridx=1;
                gbc.weightx=1.0;
                comboBox=new SimpleComboBox();
                comboBox.setPreferredSize(new Dimension(250,21));
                comboBox.setEditable(false);
                fillComboBox();
                setSelectedLayer(layer);
                panel.add(comboBox,gbc);
                
                gbc.gridx=0;
                gbc.gridy=1;
                gbc.weightx=0.0;
                panel.add(new JPanel(),gbc);
            }

            public void accept() {
                layer=comboBox.getSelectedItem().toString().trim();
            }
            public void cancel() {
            }
            public JComponent getComponent() {
                return panel;
            }

            public void setSelectedLayer(String name){
                int size=comboBox.getModel().getSize();
                for(int i=0;i<size;i++){
                    Object o=comboBox.getModel().getElementAt(i);
                    if(o.toString().trim().equals(name)) {
                        comboBox.setSelectedIndex(i);
                        return;
                    }
                }
            }

            protected void fillComboBox(){
                comboBox.removeAllItems();
                comboBox.addItem(ROOT_NAME);
                fillComboBox(wandora.getTopicMap(),"  ");
            }
            protected void fillComboBox(ContainerTopicMap container,String prefix){
                for(Layer l : container.getLayers()){
                    comboBox.addItem(prefix+l.getName());
                    if(l.getTopicMap() instanceof ContainerTopicMap){
                        fillComboBox((ContainerTopicMap)l.getTopicMap(),prefix+"  ");
                    }
                }
            }

            public String getSelectedLayerName(){
                Object o=comboBox.getSelectedItem();
                if(o==null) return null;
                return o.toString().trim();
            }

        };
    }

    public void stop(WandoraWebApp app, WandoraWebAppServer server){
        boolean running=server.getJetty().isRunning();
        if(running) try{server.getJetty().stop();}catch(Exception e){e.printStackTrace();}
        
        
//        server.getJetty().removeHandler(jettyContext);
        
        
        if(running) try{server.getJetty().start();}catch(Exception e){e.printStackTrace();}
    }

    public void save(WandoraWebApp app, WandoraWebAppServer server,Options options) {
        options.put("layer",layer);
    }
}
