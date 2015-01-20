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
 * ViewPage.java
 *
 * Created on July 14, 2004, 11:20 AM
 */

package org.wandora.piccolo.actions;
import org.wandora.utils.XMLParamAware;
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
 * This action can be used to show a page generated from a velocity template with the context returned
 * by Application.getDefaultContext. Thus you can use this Action when you need a simple action that does not
 * need any special handling in java but can be entirely performed in velocity template. Unless otherwise
 * specified, will cache the pages with a cache key consisting of the template key, template version and
 * user language. This class is XMLParamAware and needs at least templatekey element as a parameter. The
 * template key used to generate the page is taken from this element. If usecache element is specified and
 * it contains the string "false", caching of pages is turned off. Note that by using contextobject elements
 * in piccolo initialization xml, you can add objects to the context returned by Application.getDefaultContext.
 *
 * @author  olli
 */
public class ViewPage implements Action,XMLParamAware {

    private Logger logger;
    private String templateKey;
    private boolean useCache;

    /** Creates a new instance of ViewPage */
    public ViewPage() {
        useCache=true;
        templateKey=null;
    }
    
    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        PageCacheService cache=null;
        if(useCache) cache=(PageCacheService)application.getService("PageCacheService");
        InputStream page=null;
        Template template=application.getTemplate(templateKey,user);
        String cacheKey=template.getKey()+";;"+template.getVersion()+";;"+user.getProperty(User.KEY_LANG);
        if(cache!=null) page=cache.getPage(cacheKey, 0);
        if(page==null){
            HashMap context=new HashMap();
            context.put("request",request);
            context.putAll(application.getDefaultContext(user));
            if(cache!=null){
                OutputStream out=cache.storePage(cacheKey, 0);
                try{
                    template.process(context,out);
                }finally{
                    try{
                        out.close();
                    }catch(Exception e){}
                }
                page=cache.getPage(cacheKey, 0);
            }
            else{
                response.setContentType(template.getMimeType());
                response.setCharacterEncoding(template.getEncoding());
                try{
                    template.process(context,response.getOutputStream());
                }catch(IOException ioe){
                    logger.writelog("WRN","ViewPage couldn't generate page. IOException "+ioe.getMessage());
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
                logger.writelog("WRN","ViewPage couldn't generate page. IOException "+ioe.getMessage());
                return;
            }
            finally{
                try{
                    page.close();                
                }catch(IOException e){}
            }
        }
        else{
            logger.writelog("WRN","ViewPage couldn't generate page.");
            return;
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
                else if(e.getNodeName().equals("usecache")){
                    useCache=processor.getElementContents(e).equalsIgnoreCase("true");
                }
            }
        }
        if(templateKey==null){
            logger.writelog("WRN","template key of ViewPage was not specified");
        }
    }
    
}
