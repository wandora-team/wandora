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
 * ContextIsOfTypeDirective.java
 *
 * Created on 25. lokakuuta 2007, 15:07
 *
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class ContextIsOfTypeDirective implements Directive {
    
    private Locator type;
    private Directive query;
    private boolean not;
    
    /** Creates a new instance of ContextIsOfTypeDirective */
    public ContextIsOfTypeDirective(Directive query,Locator type,boolean not) {
        this.query=query;
        this.type=type;
        this.not=not;
    }
    public ContextIsOfTypeDirective(Directive query,Locator type) {
        this(query,type,false);
    }
    public ContextIsOfTypeDirective(Directive query,String type,boolean not) {
        this(query,new Locator(type),not);
    }
    public ContextIsOfTypeDirective(Directive query,String type) {
        this(query,type,false);
    }
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        
        Topic typeTopic=tm.getTopic(type);
        if(typeTopic==null) return new ArrayList<ResultRow>();
        if(contextTopic.isOfType(typeTopic)==not) return new ArrayList<ResultRow>();
        
        return query.query(context);
    }

    public boolean isContextSensitive(){
        return true;
    }

}
