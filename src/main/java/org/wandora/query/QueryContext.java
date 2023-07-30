/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * QueryContext.java
 *
 * Created on 1. helmikuuta 2008, 13:07
 *
 */

package org.wandora.query;
import java.util.HashMap;

import org.wandora.topicmap.Topic;

/**
 * @deprecated
 *
 * @author olli
 */
public class QueryContext {
    
    private Topic topic;
    private String lang;
    private HashMap<String,Object> parameters;
    
    /** Creates a new instance of QueryContext */
    public QueryContext(Topic topic,String lang,HashMap<String,Object> parameters) {
        this.topic=topic;
        this.lang=lang;
        this.parameters=parameters;
    }
    public QueryContext(Topic topic,String lang) {
        this(topic,lang,new HashMap<String,Object>());
    }
    
    public Object getParameter(String key){
        return parameters.get(key);
    }
    
    public void setParameter(String key,Object o){
        parameters.put(key,o);
    }
    
    public Topic getContextTopic(){
        return topic;
    }
    
    public String getContextLanguage(){
        return lang;
    }
    
    public QueryContext makeNewWithTopic(Topic t){
        return new QueryContext(t,lang,parameters);
    }
}
