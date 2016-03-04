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
 * 
 *
 * GenericActionCached.java
 *
 * Created on 12. huhtikuuta 2011
 */

package org.wandora.piccolo.actions;


import org.wandora.piccolo.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.wandora.piccolo.services.PageCacheService;

/**
 *
 * @author akivela
 */
public class GenericActionCached implements Action, XMLParamAware {
    
    protected Logger logger;
    protected String templateKey;
    protected static final long cacheTime = 1000*60*60*24;
    protected PageCacheService cache;

    protected String encoding = "UTF-8";
    protected String mimetype = "text/html";




    /**
     * Creates a new instance of GenericActionCached
     */
    public GenericActionCached() {
    
    }
    
   
   
   
   
    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        long stamp = System.currentTimeMillis() / cacheTime;

        try {
            if(cache == null) cache = (PageCacheService)application.getService("PageCacheService");
            String cacheKey=solveCacheKey(request);
            InputStream pageInput = cache.getPage(cacheKey, stamp);
            if(pageInput != null) {
                response.setContentType(mimetype);
                response.setCharacterEncoding(encoding);
                writeResponse(pageInput, response.getOutputStream());
                pageInput.close();
            }
            else {
                Template template = solveTemplate(user, request, application);
                response.setContentType(mimetype);
                response.setCharacterEncoding(encoding);
                OutputStream out = cache.storePage(cacheKey, stamp);
                HashMap context=new HashMap();
                context.putAll(application.getDefaultContext(user));
                recycleParameters(request, context);
                context.put("request",request);
                template.process(context, out);
                out.close();
                pageInput = cache.getPage(cacheKey, stamp);
                writeResponse(pageInput, response.getOutputStream());
                if(pageInput != null) pageInput.close();
            }
        }
        catch(Exception e) {
            logger.writelog("WRN","PageCache generated an exception "+e.getMessage());
        }
    }


    
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        
        NodeList nl=element.getChildNodes();
        for(int i=0; i<nl.getLength(); i++) {
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("templatekey")){
                    templateKey=XMLParamProcessor.getElementContents(e);
                }
                else if(e.getNodeName().equals("mimetype")) {
                    mimetype=XMLParamProcessor.getElementContents(e);
                }
                else if(e.getNodeName().equals("encoding")) {
                    encoding=XMLParamProcessor.getElementContents(e);
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




    private String solveCacheKey(javax.servlet.ServletRequest request) {
        StringBuilder b = new StringBuilder("");
        ArrayList<String> keys = new ArrayList<String>();
        String n = null;
        for(Enumeration e = request.getParameterNames();e.hasMoreElements(); ) {
            try {
                n = (String) e.nextElement();
                if(n != null && n.length()>0) {
                    keys.add(n);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        Collections.sort(keys);
        for(String k : keys) {
            try {
                b.append(k);
                b.append(request.getParameter(k));
            }
            catch(Exception ex) {
                ex.printStackTrace();
                b.append(System.currentTimeMillis());
            }
        }
        return b.toString();
    }



    private void writeResponse(InputStream input, OutputStream output) {
        try {
            byte[] buf=new byte[128];
            int r=0;
            while( (r=input.read(buf))!=-1 ) {
                output.write(buf,0,r);
            }
            output.flush();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
