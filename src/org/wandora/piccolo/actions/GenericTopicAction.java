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
 *
 * 
 *
 * FlashAjassa.java
 *
 * Created on 3. toukokuuta 2005, 18:25
 */

package org.wandora.piccolo.actions;

import org.wandora.piccolo.WandoraManager;
import org.wandora.utils.XMLParamAware;
import org.wandora.piccolo.Action;
import org.wandora.piccolo.Template;
import org.wandora.piccolo.Application;
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
 * @author  akivela
 */



public class GenericTopicAction extends GenericAction implements Action, XMLParamAware {
    
    
    
    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        WandoraManager manager=(WandoraManager)application.getService("WandoraManager");
        if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
            try {
                Template template = solveTemplate(user, request, application);
                if(template != null) {
                    InputStream page=null;
                    HashMap context=new HashMap();
                    
                    recycleParameters(request, context);
                    context.put("request",request);
                    context.put("topicmap",manager.getTopicMap());
                    context.putAll(application.getDefaultContext(user));
                                     
                    Topic topic = solveTopic(request, manager.getTopicMap());
                    if(topic != null) {
                        context.put("topic", topic);
                    }

                    response.setContentType(template.getMimeType());
                    response.setCharacterEncoding(template.getEncoding());
                    try {
                        template.process(context,response.getOutputStream());
                    }
                    catch(IOException ioe) {
                        logger.writelog("WRN","GenericTopicAction couldn't generate page. IOException "+ioe.getMessage());
                    }
                }
                return;
            }
            finally{
                manager.releaseTopicMap(WandoraManager.LOCK_READ);
            }
        }
        else{
            logger.writelog("WRN","GenericTopicAction couldn't lock topic");
            return;
        }
    }
    
    
   
    
    /*
     * Method tries to identify requested topic. Request has been sent within
     * "si" URL parameter. Identification examines subject identifiers first.
     * If no topic with matching subject identifier is found, method looks if
     * URL parameter matches any subject locator. Finally if both previous
     * searches fail, URL parameter is used to look for basenames.
     *
     * If URL parameter "si" matches topic's subject identifier, subject
     * locator or basename the topic is returned when execution returns.
     * If no topic is found or search raises exception method returns null.
     */
    public Topic solveTopic(javax.servlet.ServletRequest request, TopicMap topicMap) {
        String si=request.getParameter("si");
        if(si != null) {
            si = si.trim();
            try {
                Topic topic = topicMap.getTopic(si);
                if(topic == null) {
                    topic = topicMap.getTopicBySubjectLocator(si);
                }
                if(topic == null) {
                    topic = topicMap.getTopicWithBaseName(si);
                }
                if(topic != null) {
                    return topic;
                }
            }
            catch(TopicMapException tme){
                logger.writelog("ERR","TopicMapException",tme);
            }
        }
        return null;
    }
    
}
