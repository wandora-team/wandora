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
 * ViewTopic.java
 *
 * Created on July 9, 2004, 12:27 PM
 */

package org.wandora.piccolo.actions;



import org.wandora.piccolo.*;
import org.wandora.piccolo.services.PageCacheService;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;




/**
 *
 * @author  olli
 */
public class ViewTopic implements Action,XMLParamAware{
    
    private Logger logger;
    private TopicFilter filter;
    
    /** Creates a new instance of ViewTopic */
    public ViewTopic() {
    }
    
    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        WandoraManager manager=(WandoraManager)application.getService("WandoraManager");
        PageCacheService cache=(PageCacheService)application.getService("PageCacheService");
        if(manager.lockTopicMap(WandoraManager.LOCK_READ)){
            try{
                String si=request.getParameter("topic");
                if(si==null) {
                    logger.writelog("DBG","ViewTopic topic parameter was null.");
                    return;
                }
                int pagenum=1;
                if(request.getParameter("page")!=null){
                    try{
                        pagenum=Integer.parseInt(request.getParameter("page"));
                    }catch(NumberFormatException e){}
                }
                InputStream page=null;
                Template template=null;
                TopicFilter filter=null;
                try{filter=this.filter.makeNew(request);}catch(TopicMapException tme){logger.writelog("ERR","TopicMapException",tme);}
                
                Topic topic=manager.getTopicBySI(si);
                if(topic == null) topic=manager.getTopicByName(si);

                if(topic==null){
                    logger.writelog("DBG","Topic "+si+" not found.");
                    template=application.getTemplate("invalidtopic",user);
                    cache=null;
                }
                else {
                    logger.writelog("DBG","Topic "+si+" found.");
                    template=application.getTemplate("viewtopic",user);
                }
                
                String cacheKey=template.getKey()+";;"+template.getVersion()+";;"+user.getProperty(User.KEY_LANG)+";;"+pagenum+";;"+si+";;"+filter.getFilterCacheKey();
                if(cache!=null) {
                    try{page=cache.getPage(cacheKey, topic.getDependentEditTime());}
                    catch(TopicMapException tme){tme.printStackTrace();logger.writelog("ERR","TopicMapException",tme);}
                }
                if(page==null){
                    HashMap context=new HashMap();
                    //System.out.println("TOPIC == " + topic);
                    context.put("topic",topic);
                    context.put("request",request);
                    context.put("page",new Integer(pagenum));
                    context.put("filter",filter);
                    context.putAll(application.getDefaultContext(user));
                    if(cache!=null){
                        long editTime=-1;
                        try{editTime=topic.getDependentEditTime();}
                        catch(TopicMapException tme){tme.printStackTrace();logger.writelog("ERR","TopicMapException",tme);}

                        OutputStream out=cache.storePage(cacheKey, editTime);
                        if(out!=null){
                            try{
                                template.process(context,out);
                            }finally{
                                try{
                                    out.close();
                                }catch(Exception e){}
                            }
                        }
                        page=cache.getPage(cacheKey, editTime);
                    }
                    else{
                        response.setContentType(template.getMimeType());
                        response.setCharacterEncoding(template.getEncoding());
                        try{
                            template.process(context,response.getOutputStream());
                        }catch(IOException ioe){
                            logger.writelog("WRN","ViewTopic couldn't generate page. IOException "+ioe.getMessage());
                        }
                        return;
                    }
                }
                if(page!=null){
                    try{
                        response.setContentType(template.getMimeType());
                        response.setCharacterEncoding(template.getEncoding());
                        OutputStream out=response.getOutputStream();
                        byte[] buf=new byte[4096];
                        int r=0;
                        while( (r=page.read(buf))!=-1 ){
                            out.write(buf,0,r);
                        }
                        out.flush();
                    }catch(java.io.IOException ioe){
                        logger.writelog("WRN","ViewTopic couldn't generate page. IOException "+ioe.getMessage());
                        return;
                    }
                    finally{
                        try{
                            page.close();                
                        }catch(IOException e){}
                    }
                }
                else{
                    logger.writelog("WRN","ViewTopic couldn't generate page.");
                    return;
                }
            }
            finally{
                manager.releaseTopicMap(WandoraManager.LOCK_READ);
            }
        }
        else{
            logger.writelog("WRN","ViewTopic couldn't lock topic");
            return;
        }
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        
        filter=null;
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("filter")){
                    try{
                        filter=(TopicFilter)processor.createObject(e);
                    }catch(Exception ex){
                        logger.writelog("WRN",ex.getClass().getName()+" when processing config file, element "+e.getNodeName()+". "+ex.getMessage());  
                    }
                }
            }
        }
        
        if(filter==null) filter=new AbstractTopicFilter(){
            public boolean topicVisible(Topic t) throws TopicMapException {
                return TMBox.topicVisible(t);
            }
            public String getFilterCacheKey(){return "";}
        };
    }
    
}
