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
 * PiccoloServlet.java
 *
 * Created on July 9, 2004, 10:55 AM
 */

package org.wandora.piccolo;
import org.wandora.utils.XMLParamProcessor;
import javax.servlet.*;
import javax.servlet.http.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
/**
 * A HttpServlet that initializes Piccolo context and passes request actions specified in the action map of
 * the piccolo context. Piccolo is initialized with an xml file processed by XMLParamProcessor. The config
 * file name is taken from init parameter "configfile". A Logger is added to the XMLParamProcessor symbol
 * table before parsing with key "logger". This can be used by the application or any actions or other
 * services to write log entries. The config file should create exactly one object with element name
 * "application" and it should be an instance of Application. This application will receive the http requests.
 * The config file may specify any number of objects with "shutdownhook" element name. They must be
 * instances of PiccoloShutdownHook and their doShutdown method is called when the servlet context is
 * being destroyed and application clean up should be performed.
 *
 * @author  olli
 */
public class PiccoloServlet extends HttpServlet {
    // quick and dirty expiration; milliseconds from epoch; set to 0 to disable; remember to use letter l at the end to make it a long
    public static final long EXPIRY_DATE=0;//1096578000000l;
    
    private Application application;
    private boolean initialized;
    private Vector shutdownHooks;
    
    private int hits;
    private long starttime;
    private static final long statlength=60000;
    private boolean expired=false;
    
    /** Creates a new instance of PiccoloServlet */
    public PiccoloServlet() {
        initialized=false;
        shutdownHooks=new Vector();
    }
    
    public void init(){
        synchronized(this){
            if(!initialized){
                try{
                    initialize();
                    initialized=true;
                }catch(Exception e){
                    log("Couldn't initialize piccolo.",e);
                }
            }
        }
    }
    
    private void initialize() throws Exception {
        log("Initializing piccolo");
        
        if(EXPIRY_DATE!=0){
            if(System.currentTimeMillis()>EXPIRY_DATE){
                initialized=true;
                expired=true;
                log("Servlet expired.");
                return;
            }
        }
        
        XMLParamProcessor processor=new XMLParamProcessor();
        Logger.setLogger(new ServletLogger());
        processor.addObjectToTable("logger", Logger.getLogger());
        processor.addObjectToTable("servlet", this);
        String paramfile=getInitParameter("configfile");
        if(paramfile==null) paramfile="piccoloconfig.xml";
        Document doc=null;
        try{
            doc=processor.parseDocument(paramfile);
        }catch(Exception e){
            log("Warning, Exception parsing config file",e);
            return;
        }
        try{
            processor.processElement(doc.getDocumentElement());
        }catch(Exception e){
            log("Warning, Exception processing config file",e);
        }
        HashMap symbolTable=processor.getSymbolTable();
        Iterator iter=symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            String type=processor.getObjectType((String)e.getKey());
            if(type==null) continue;
            if(type.equals("application")){
                try{
                    application=(Application)e.getValue();
                }catch(ClassCastException cce){
                    log("Warning, class "+e.getValue().getClass().getName()+" is not instance of Application.");
                }
            }
            else if(type.equals("shutdownhook")){
                Object o=e.getValue();
                if(!(o instanceof PiccoloShutdownHook)){
                    log("Warning, class "+o.getClass().getName()+" is not instance of PiccoloShutdownHook.");
                }
                else{
                    shutdownHooks.add(o);
                }
            }
        }
        if(application==null){
            log("Warning, no application tag in configuration file!");
        }
        log("Initialization done");
    }
    
    protected void doGet(HttpServletRequest request,HttpServletResponse response){
        if(!initialized){
            synchronized(this){
                if(!initialized){
                    try{
                        initialize();
                        initialized=true;
                    }catch(Exception e){
                        log("Couldn't initialize piccolo.",e);
                    }
                }
            }
        }
        if(expired) return;
        if(starttime==0){
            starttime=System.currentTimeMillis();
            hits=0;
        }
        application.handleRequest(request,response);
        
        // statistics gathering may be off a few hits when under heavy load because of synchronization issues
        long time=System.currentTimeMillis();
        if(time-starttime>statlength){
            synchronized(this){
                if(time-starttime>statlength){
                    long length=time-starttime;
                    log("Statistics: "+hits+" hits / "+(length/1000)+"s = "+((double)hits/((double)length/1000.0))+"/s");
                    hits=0;
                    starttime=time;
                }
            }
        }
        hits++;
    }
    protected void doPost(HttpServletRequest request,HttpServletResponse response){
        doGet(request,response);        
    }
    
    
    public void destroy(){
        Iterator iter=shutdownHooks.iterator();
        while(iter.hasNext()){
            Object o=iter.next();
            ((PiccoloShutdownHook)o).doShutdown();
        }
        super.destroy();
    }
    
    public class ServletLogger extends Logger {
        
        public void writelog(String level, String s) {
            PiccoloServlet.this.log("["+level+"] "+s);
        }
        
    }
    
}
