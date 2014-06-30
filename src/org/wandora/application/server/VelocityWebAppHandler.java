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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.utils.Options;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import org.wandora.application.Wandora;
import org.wandora.application.tools.GenericOptionsPanel;
import org.wandora.application.gui.simple.*;
import org.wandora.topicmap.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;


/**
 *
 * @author olli
 */
public class VelocityWebAppHandler implements WebAppHandler {

    protected VelocityEngine velocityEngine;

    protected String templateFile;

    public VelocityWebAppHandler(){
    }
    
    protected VelocityContext makeHandlerContext(WandoraWebApp app, WandoraWebAppServer server, String target, HttpServletRequest request){
        return new VelocityContext();
    }

    public boolean getPage(WandoraWebApp app, WandoraWebAppServer server, String target, HttpServletRequest request, HttpServletResponse response) {
        if(target.equals("/")){
            VelocityContext context=makeHandlerContext(app, server, target, request);

            if(velocityEngine==null){
                velocityEngine = new VelocityEngine();
                velocityEngine.setProperty("file.resource.loader.path", app.getTemplatePath() );
                velocityEngine.setProperty("velocimacro.library.autoreload",  true);
                
                try{
                    velocityEngine.init();
                }catch(Exception e){server.log(e);}
            }
            Template vTemplate=null;

            try{
                vTemplate=velocityEngine.getTemplate(templateFile,"UTF-8");
            }catch(Exception e){
                e.printStackTrace();
                server.writeResponse(response,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Exception processing template",e);
                return true;
            }

            try{
                Wandora wandora=server.getWandora();
                String temp;
                Topic topic=null;
                String si=request.getParameter("topic");
                if(si==null || si.length()==0) si=request.getParameter("si");
                if(si==null || si.length()==0) {
                    String sl=request.getParameter("sl");
                    if(sl==null || sl.length()==0){
                        topic=wandora.getOpenTopic();
                        if(topic==null) {
                            topic=wandora.getTopicMap().getTopic(TMBox.WANDORACLASS_SI);
                            if(topic==null) {
                                server.writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Wandora application does not have any topic open and no topic is specified in http parameters.");
                                return true;
                            }
                        }
                    }
                    else {
                        topic=wandora.getTopicMap().getTopicBySubjectLocator(sl);
                        if(topic==null) {
                            server.writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Topic with subject locator "+sl+" not found.");
                            return true;
                        }
                    }
                }
                else {
                    topic=wandora.getTopicMap().getTopic(si);
                    if(topic==null){
                        server.writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Topic with subject identifier "+si+" not found.");
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

                server.getDefaultContext(context,app);

                response.setCharacterEncoding("UTF-8");
                Writer writer=response.getWriter();
                vTemplate.merge(context, writer);
                writer.flush();
            }
            catch(TopicMapException tme){
                server.log(tme);
                server.writeResponse(response,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Exception processing template",tme);
            }
            catch(IOException ioe){
                server.log(ioe);
                server.writeResponse(response,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Exception processing template",ioe);
            }
            return true;
        }
        else {
            return server.getStatic(target, app, request, response);
        }
    }

    
    
    public ConfigComponent getConfigComponent(WandoraWebApp app, final WandoraWebAppServer server) {
        return new ConfigComponent(){
            GenericOptionsPanel gop;
            SimpleField templateField;
            public JPanel panel;
            {
                panel=new JPanel();
                GridBagConstraints gbc=GenericOptionsPanel.makeGBC();
                gbc.anchor=GridBagConstraints.WEST;
                gbc.insets=new java.awt.Insets(0,5,0,5);
                panel.setLayout(new GridBagLayout());
                SimpleLabel myLabel = new SimpleLabel("Template");
                myLabel.setPreferredSize(new Dimension(100,20));
                myLabel.setHorizontalAlignment(SimpleLabel.RIGHT);
                panel.add(myLabel,gbc);
                gbc.gridx=1;
                gbc.weightx=1.0;
                templateField=new SimpleField();
                templateField.setText( getTemplate() != null ? getTemplate() : "" );
                panel.add(templateField,gbc);

                gbc.gridx=0;
                gbc.gridy=1;
                gbc.weightx=0.0;
                panel.add(new JPanel(),gbc);
            }

            public void accept() {
                setTemplate(templateField.getText().trim());
            }
            public void cancel() {
            }
            public JComponent getComponent() {
                return panel;
            }
        };
    }


    public String getTemplate(){return templateFile;}
    public void setTemplate(String s){templateFile=s;}

    public void init(WandoraWebApp app, WandoraWebAppServer server,Options options){
        if(options==null) templateFile="viewtopic.vhtml";
        else templateFile=options.get("template","viewtopic.vhtml");
    }
    public void save(WandoraWebApp app, WandoraWebAppServer server,Options options){
        options.put("template",templateFile);
    }
    public void start(WandoraWebApp app, WandoraWebAppServer server){
    }
    public void stop(WandoraWebApp app, WandoraWebAppServer server){
        velocityEngine = null;
    }

    public class TopicFilter {
        public boolean topicVisible(Topic t) {return true;}
        public boolean associationVisible(Association a) {return true;}
        public Collection filterTopics(Collection topics) {return topics;}
        public Collection filterAssociations(Collection associations) {return associations;}
    }

}
