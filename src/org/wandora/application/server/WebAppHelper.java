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
 */


package org.wandora.application.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.application.Wandora;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class WebAppHelper {
    
    
    
    public static Topic getRequestTopic(WandoraWebAppServer server, String target, HttpServletRequest request, HttpServletResponse response) throws TopicMapException {
        Wandora wandora=server.getWandora();
        Topic topic=null;
        String si=request.getParameter("topic");
        if(si==null || si.length()==0) si=request.getParameter("si");
        if(si==null || si.length()==0) {
            String sl=request.getParameter("sl");
            if(sl==null || sl.length()==0){
                topic=wandora.getOpenTopic();
                if(topic==null){
                    server.writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Wandora application does not have any topic open and no topic is specified in http parameters.");
                }
            }
            else{
                topic=wandora.getTopicMap().getTopicBySubjectLocator(sl);
                if(topic==null) {
                    server.writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Topic with subject locator "+sl+" not found.");
                }
            }
        }
        else {
            topic=wandora.getTopicMap().getTopic(si);
            if(topic==null) {
                topic = wandora.getTopicMap().getTopicWithBaseName(si);
            }
            if(topic==null) {
                server.writeResponse(response,HttpServletResponse.SC_NOT_FOUND,"404 Not Found<br />Topic with subject identifier "+si+" not found.");
            }
        }
        return topic;
    }
    
    
    
}
