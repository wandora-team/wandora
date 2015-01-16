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
 */
package org.wandora.application.modulesserver;

 
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet;
import org.wandora.modules.topicmap.ViewTopicAction;
import org.wandora.modules.usercontrol.User;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 * <p>
 * SameAsService implements a simple web service that returns all subject identifiers
 * of a given topic. Given topic is recognized with a single subject identifier
 * given in URL parameter.
 * </p>
 * <p>
 * The response format is similar to the JSON response format
 * of the sameAs.org service. Thus, Wandora can be used as a part of LOD machinery,
 * a web service returning similar URIs.
 * </p>
 * <p>
 * Wandora features a subject expander, SameAsAnywhereSubjectExpander, that
 * can be used to retrieve subjects provided by the SameAsService.
 * </p>
 *
 * @author akivela
 */


public class SameAsService  extends AbstractTopicWebApp {



    
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        super.init(manager, settings);
    }
    
    
    
    
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
        try {
            String query = req.getParameter(topicRequestKey);
            if(query == null) query = req.getParameter("uri");
            if(query != null) query = query.trim();
            
            if(query!=null && query.length()==0) query=null;
                        
            Topic topic = resolveTopic(query);
            if(query == null && topic != null) {
                query = topic.getOneSubjectIdentifier().toExternalForm();
            }

            if(query != null) {
                OutputStream out = resp.getOutputStream();
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");

                String json = makeJSON(getSameAs(query, topic));
                out.write(json.getBytes());

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
    
    

    @Override
    public Topic resolveTopic(String query) {
        TopicMap tm = tmManager.getTopicMap();
        if(tm==null) return null;
        try {
            Topic t=null;
            if(query!=null) t = ViewTopicAction.getTopic(query, tm);
            return t;
        }
        catch(TopicMapException tme){
            return null;
        }
    }
    
    
    
    protected String makeJSON(SameAs sameas) {
        StringBuilder sb = new StringBuilder("");
        sb.append("[");
        sb.append("{");
        sb.append("\"uri\": ").append(encodeJSON(sameas.url)).append(",");
        sb.append("\"numDuplicates\": \"").append(sameas.getSameCount()).append("\",");
        sb.append("\"duplicates\": [");
        ArrayList<String> sames = sameas.sames;
        for(int i=0; i<sames.size(); i++) {
            String url = sames.get(i);
            sb.append(encodeJSON(url));
            if(i < sames.size()-1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
    
    
    
    protected SameAs getSameAs(String u, Topic t) throws TopicMapException {
        SameAs sameas = new SameAs(u);
        if(t != null && !t.isRemoved()) {
            for(Locator l : t.getSubjectIdentifiers()) {
                sameas.addSame(l.toExternalForm());
            }
        }
        else {
            sameas.addSame(u);
        }
        return sameas;
    }
    
    

    protected String encodeJSON(String string) {
        if(string == null || string.length() == 0) {
             return "\"\"";
         }

         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         sb.append("\"");
         
         String       t;

         for(i = 0; i < len; i += 1) {
             c = string.charAt(i);
             switch (c) {
             case '\\':
             case '"':
                 sb.append('\\');
                 sb.append(c);
                 break;
             case '/':
 //                if (b == '<') {
                     sb.append('\\');
 //                }
                 sb.append(c);
                 break;
             case '\b':
                 sb.append("\\b");
                 break;
             case '\t':
                 sb.append("\\t");
                 break;
             case '\n':
                 sb.append("\\n");
                 break;
             case '\f':
                 sb.append("\\f");
                 break;
             case '\r':
                sb.append("\\r");
                break;
             default:
                 if(c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         
         sb.append("\"");
         return sb.toString();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    /**
     * SameAs is a helper class to store the URL and all similar URLs.
    **/
    protected class SameAs {
        public String url = null;
        public ArrayList<String> sames = new ArrayList();
        
        public SameAs() {
            
        }
        
        public SameAs(String u) {
            url = u;
        }
        
        public void addSame(String u) {
            if(sames == null) sames = new ArrayList();
            sames.add(u);
        }
        
        public int getSameCount() {
            if(sames == null) sames = new ArrayList();
            return sames.size();
        }
    }
    
}