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
 * IsTopicDirective.java
 *
 * Created on 25. lokakuuta 2007, 11:57
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
public class IsTopicDirective extends FilterDirective {
    
    private Locator topicContext;
    private Locator topic;
    private Directive query;
    
    /** Creates a new instance of IsTopicDirective */
    public IsTopicDirective(Directive query,Locator topicContext,Locator topic,boolean not) {
        super(query,not);
        this.query=query;
        this.topicContext=topicContext;
        this.topic=topic;
    }
    public IsTopicDirective(Directive query,Locator topicContext,Locator topic) {
        this(query,topicContext,topic,false);
    }
    public IsTopicDirective(Directive query,String topicContext,String topic,boolean not) {
        this(query,new Locator(topicContext),new Locator(topic),not);
    }
    public IsTopicDirective(Directive query,String topicContext,String topic) {
        this(query,topicContext,topic,false);
    }

    public Object startQuery(QueryContext context) throws TopicMapException {
        TopicMap tm=context.getContextTopic().getTopicMap();
        Topic t=tm.getTopic(topic);        
        return t;
    }
    
    protected int _includeRow(ResultRow row,Topic context,TopicMap tm,Object param) throws TopicMapException {
        if(param==null) return RES_EXCLUDE;
        Topic t=(Topic)param;
        
        Locator p=row.getPlayer(topicContext);
        if(p==null) return RES_EXCLUDE;
        Topic player=tm.getTopic(p);
        if(player==null) return RES_IGNORE;
        return (player.mergesWithTopic(t)?RES_INCLUDE:RES_EXCLUDE);
    }
}
