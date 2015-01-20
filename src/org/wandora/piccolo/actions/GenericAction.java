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
 *
 * 
 *
 * GenericAction.java
 *
 * Created on 22. heinäkuuta 2005, 9:25
 */

package org.wandora.piccolo.actions;
import org.wandora.utils.XMLParamAware;
import org.wandora.piccolo.Action;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.Template;
import org.wandora.piccolo.VelocityTemplate;
import org.wandora.piccolo.Application;
import org.wandora.piccolo.SimpleLogger;
import org.wandora.piccolo.User;
import org.wandora.piccolo.*;
import org.wandora.piccolo.services.PageCacheService;
import org.wandora.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;

/**
 *
 * @author olli, akivela
 */
public class GenericAction implements Action, XMLParamAware {
    
    protected Logger logger;
    protected String templateKey;
    
    
    
    /**
     * Creates a new instance of GenericAction
     */
    public GenericAction() {
    
    }
    
   
   
   
   
    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        Template template = solveTemplate(user, request, application);           
        WandoraManager manager=(WandoraManager)application.getService("WandoraManager");
        if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
            try{
                HashMap context=new HashMap();
                //request.setCharacterEncoding(template.getEncoding());
                context.putAll(application.getDefaultContext(user)); 
                recycleParameters(request, context);
                context.put("request",request);                

                response.setContentType(template.getMimeType());
                response.setCharacterEncoding(template.getEncoding());
                try{
                    template.process(context,response.getOutputStream());
                }
                catch(IOException ioe){
                    logger.writelog("WRN","GenericTopicMapAction couldn't generate page. IOException "+ioe.getMessage());
                }
                return;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            finally{
                manager.releaseTopicMap(WandoraManager.LOCK_READ);
            }
        }
    }
    
    
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("templatekey")){
                    templateKey=processor.getElementContents(e);
                }
            }
        }
        if(templateKey==null){
            logger.writelog("WRN","template key of GenericAction was not specified");
        }
    }
    
   
    
    public Template solveTemplate(User user, javax.servlet.ServletRequest request, Application application) {
        Template template = null;
        String templateParameter = request.getParameter("template");
        if(templateParameter != null && templateParameter.length() > 0) {
            template= application.getTemplate(templateParameter,user);
            if(template == null) {
                template = new VelocityTemplate();
                ((VelocityTemplate) template).setTemplate(templateParameter);
                ((VelocityTemplate) template).setCaching(false);
            }
            if(template != null) {
                String mimeParameter = request.getParameter("mime");
                if(mimeParameter == null) mimeParameter = request.getParameter("mimetype");
                if(mimeParameter == null) mimeParameter = request.getParameter("mime-type");
                if(mimeParameter != null) {
                    ((VelocityTemplate) template).setMimeType(mimeParameter);
                }
            }
        }
        if(template == null && templateKey != null) {
            template=application.getTemplate(templateKey,user);
        }
        return template;
    }
    
    
    
    
    public void recycleParameters(javax.servlet.ServletRequest request, HashMap context) {
        for(Enumeration e = request.getParameterNames();e.hasMoreElements(); ) {
            try {
                String n = (String) e.nextElement();
                if(n != null && n.length()>0) {
                    context.put(n, request.getParameter(n));
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    
}
