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
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet;
import org.wandora.modules.usercontrol.User;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.memory.TopicMapImpl;

/**
 *
 * @author olli
 */


public class JTMWebApp extends AbstractTopicWebApp {

    protected boolean deepCopy = false;

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("deepCopy");
        if(o!=null) deepCopy=Boolean.parseBoolean(o.toString().trim());
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
                OutputStream out = resp.getOutputStream();
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");

                TopicMap tmOut = new TopicMapImpl();
                tmOut.copyTopicIn(topic, deepCopy);
                tmOut.copyTopicAssociationsIn(topic);
                tmOut.exportJTM(out, tmOut);

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
