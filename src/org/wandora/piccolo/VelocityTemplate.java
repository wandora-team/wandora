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
 * VelocityTemplate.java
 *
 * Created on July 8, 2004, 4:16 PM
 */

package org.wandora.piccolo;
import org.wandora.utils.XMLParamAware;
import org.wandora.utils.*;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
/**
 *
 * @author  olli, akivela
 */
public class VelocityTemplate implements XMLParamAware,org.wandora.piccolo.Template {
    
    private String key;
    private String version;
    private String template;
    private String mimeType;
    private String encoding;
    private org.apache.velocity.Template vTemplate;
    private boolean caching;
    
    private Logger logger;
    
    /** Creates a new instance of Template */
    public VelocityTemplate() {
        mimeType="text/html";
        encoding="UTF-8";
        caching=true;
    }
    
    
    public void setTemplate(String template) {
        this.template = template;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void setVersion(String v) {
        this.version = v;
    }
    public void setCaching(boolean caching) {
        this.caching = caching;
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("key")){
                    key=processor.getElementContents(e);
                }
                else if(e.getNodeName().equals("version")){
                    version=processor.getElementContents(e);                    
                }
                else if(e.getNodeName().equals("template")){
                    template=processor.getElementContents(e);                    
                }
                else if(e.getNodeName().equals("mimetype")){
                    mimeType=processor.getElementContents(e);                    
                }
                else if(e.getNodeName().equals("encoding")){
                    encoding=processor.getElementContents(e);
                }
                else if(e.getNodeName().equals("caching")){
                    caching=!processor.getElementContents(e).equalsIgnoreCase("false");
                }
                else{
                    logger.writelog("WRN","Unknown element named "+e.getNodeName()+" while processing template element.");
                }
                // TODO: check null values for key, version and template
            }
        }
    }
    
    public String getKey(){return key;}
    public String getVersion(){return version;}
    public String getTemplate(){return template;}
    public String getMimeType(){return mimeType;}
    public String getEncoding(){return encoding;}
    
    public void process(java.util.HashMap params, java.io.OutputStream output){
        Template vTemplate=this.vTemplate;
        if(vTemplate==null || !caching){
            try{
                vTemplate=Velocity.getTemplate(template,encoding);
            }catch(ResourceNotFoundException rnfe){
                logger.writelog("WRN","Velocity template "+template+" not found.");
            }catch(ParseErrorException pee){
                logger.writelog("WRN","Parse error in velocity template "+template+": "+pee.getMessage());                        
            }catch(Exception ex){
                logger.writelog("WRN","Couldn't load velocity template "+template+": "+ex.getMessage());
                logger.writelog("DBG",ex);
            }
            if(caching){
                this.vTemplate=vTemplate;
            }
        }
        VelocityContext context=new VelocityContext();
        Iterator iter=params.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            context.put(e.getKey().toString(),e.getValue());
        }
//        StringWriter writer=new StringWriter();
        try{
            Writer writer=new BufferedWriter(new OutputStreamWriter(output,getEncoding()));
            vTemplate.merge(context, writer);
            writer.flush();
        }catch(Exception ex){
            logger.writelog("WRN","Couldn't merge template "+template+".",ex);            
            Throwable cause=ex.getCause();
            if(cause!=null){
                logger.writelog("WRN","Cause",cause);
            }
        }
/*        try{
            return writer.toString().getBytes(encoding);
        }catch(UnsupportedEncodingException uee){
            logger.writelog("WRN","Unsupported encoding "+encoding+".");
            return writer.toString().getBytes();
        }*/
    }
    
}
