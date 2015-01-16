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
 * GetQTRef.java
 *
 * Created on 4. toukokuuta 2005, 15:18
 */

package org.wandora.piccolo.actions;



import org.wandora.utils.XMLParamAware;
import org.wandora.piccolo.Action;
import org.wandora.piccolo.Logger;
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
import java.net.*;



/**
 *
 * @author  akivela
 */
public class GetQTRef implements Action,XMLParamAware {
    
    public String responseTemplate = "<?xml version=\"1.0\"?><?quicktime type=\"application/x-quicktime-media-link\"?><embed autoplay=\"true\" src=\"%URL%\"/>";
    private Logger logger;
    
    /** Creates a new instance of GetUrl */
    public GetQTRef() {
    }
    
    public synchronized void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        try {
            logger.writelog("INF", "Entering GetQTRef action!");
            String url=request.getParameter("url");
            
            response.setContentType("video/quicktime");
            //response.setContentType("application/x-quicktime-media-link");
            OutputStream out=response.getOutputStream();
            
            String responseString = "";
            if(url != null) responseString = responseTemplate.replaceAll("%URL%", url);
            
            out.write(responseString.getBytes());

            out.flush();
            out.close();
            logger.writelog("INF", "Exiting GetQTRef action!");
        } catch(Exception e) {
            logger.writelog("WRN", "GetQTRef couldn't get url. Exception "+e.getMessage());
            logger.writelog("WRN", e);
        }
    }
    

    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();
    }
    
}
