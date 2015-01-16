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
 * QueryContext.java
 *
 */
package org.wandora.query2;
import java.util.*;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */
public class QueryContext {
    private String lang;
    private TopicMap tm;
    private HashMap<String,Object> parameters;
    private boolean interrupt;

    /** Creates a new instance of QueryContext */
    public QueryContext(TopicMap tm,String lang,HashMap<String,Object> parameters) {
        this.tm=tm;
        this.lang=lang;
        this.parameters=parameters;
        interrupt=false;
    }
    public QueryContext(String lang,HashMap<String,Object> parameters) {
        this.lang=lang;
        this.parameters=parameters;
        interrupt=false;
    }
    public QueryContext(String lang) {
        this(null,lang,new HashMap<String,Object>());
    }
    public QueryContext(TopicMap tm,String lang) {
        this(tm,lang,new HashMap<String,Object>());
    }

    public boolean checkInterrupt(){
        return interrupt;
    }

    public void interrupt(){
        interrupt=true;
    }

    public Object getParameter(String key){
        return parameters.get(key);
    }

    public void setParameter(String key,Object o){
        parameters.put(key,o);
    }

    public String getContextLanguage(){
        return lang;
    }

    public void setTopicMap(TopicMap tm){
        this.tm=tm;
    }

    public TopicMap getTopicMap(){
        return tm;
    }

}
