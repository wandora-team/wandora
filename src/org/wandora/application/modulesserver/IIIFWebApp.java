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
 *
 */
package org.wandora.application.modulesserver;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.LayeredTopicContext;
import org.wandora.application.tools.exporters.iiifexport.IIIFBuilder;
import org.wandora.application.tools.exporters.iiifexport.IIIFExport;
import org.wandora.application.tools.exporters.iiifexport.Manifest;
import org.wandora.application.tools.exporters.iiifexport.SelectionInstancesIIIFBuilder;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet;
import org.wandora.modules.usercontrol.User;
import org.wandora.topicmap.Topic;


/**
 *
 * @author olli
 */


public class IIIFWebApp extends AbstractTopicWebApp {
    protected IIIFBuilder builder;
    protected boolean prettyPrint=true;
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o=settings.get("iiifBuilder");
        if(o!=null) o=SelectionInstancesIIIFBuilder.class.getName();
        
        try{
            builder=(IIIFBuilder)Class.forName(o.toString()).getDeclaredConstructor().newInstance();            
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
            logging.error(e);
        }
        
        o=settings.get("prettyPrint");
        if(o!=null) prettyPrint=Boolean.parseBoolean(o.toString());
        
        super.init(manager, settings);
    }
    
    
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
                
        try {
            String query=req.getParameter(topicRequestKey);
            if(query!=null) query=query.trim();
            
            if(query!=null && query.length()==0) query=null;
                        
            // resolveTopic will try to get the open topic in Wandora if query==null
            Topic topic = resolveTopic(query);
            if(topic != null) {
                LayeredTopicContext context=new LayeredTopicContext();
                context.setContextSource(topic);
                
                // the export tool itself is only needed for logging
                Manifest m=builder.buildIIIF(Wandora.getWandora(), context, new IIIFExport(){

					private static final long serialVersionUID = 1L;

					@Override
                    public void log(Error e) {
                        logging.error(e);
                    }
                    @Override
                    public void log(Exception e) {
                        logging.error(e);
                    }
                    @Override
                    public void log(String message, Exception e) {
                        logging.error(message, e);
                    }
                    @Override
                    public void log(String message) {
                        logging.info(message);
                    }
                    @Override
                    public void hlog(String message) {
                        logging.info(message);                        
                    }
                });
                
                if(m==null) return false;
                
                StringBuilder sb=new StringBuilder();
                m.toJsonLD().outputJson(sb, prettyPrint?"":null );
                
                OutputStream out = resp.getOutputStream();
                resp.setContentType("application/ld+json");
                resp.setCharacterEncoding("UTF-8");

                out.write(sb.toString().getBytes("UTF-8"));

                out.close();
                
                return true;
            }
            else return false;
        }
        catch(Exception e) {
            logging.error(e);
            return false;
        }
    }    
}
