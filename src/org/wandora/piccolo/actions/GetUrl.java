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
 * GetUrl.java
 *
 * Created on November 9, 2004, 11:06 AM
 */


package org.wandora.piccolo.actions;
import org.wandora.utils.XMLParamAware;
import org.wandora.application.Wandora;
import org.wandora.piccolo.*;
import org.wandora.piccolo.services.PageCacheService;
import org.wandora.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import java.net.*;


/**
 *
 * @author  akivela
 */
public class GetUrl implements Action,XMLParamAware {
    
    private Logger logger;
    
    /** Creates a new instance of GetUrl */
    public GetUrl() {
    }
    
    public synchronized void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        try {
            String urls=request.getParameter("url");
            URL url = new URL(urls);
            URLConnection con = url.openConnection();
            Wandora.initUrlConnection(con);
            con.setUseCaches(false);
            InputStream in = con.getInputStream();
            
            response.setContentType(con.getRequestProperty("Content-Type"));
            OutputStream out=response.getOutputStream();
            byte[] buf=new byte[4096];
            int r=0;
            while( (r=in.read(buf))!=-1 ){
                out.write(buf,0,r);
            }
            in.close();
            out.flush();
            out.close();
        } catch(Exception e) {
            logger.writelog("WRN", "GetUrl couldn't get url. Exception "+e.getMessage());
            logger.writelog("WRN", e);
        }
    
    
    }
    
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
    }
    
}
