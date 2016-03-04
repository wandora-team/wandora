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
 * IsContextTopicDirective.java
 *
 * Created on 9. tammikuuta 2008, 15:15
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class IsContextTopicDirective extends FilterDirective {
    
    private Locator topicContext;
    private Directive query;
    
    /** Creates a new instance of IsContextTopicDirective */
    public IsContextTopicDirective(Directive query,Locator topicContext,boolean not) {
        super(query,not);
        this.query=query;
        this.topicContext=topicContext;
    }
    public IsContextTopicDirective(Directive query,Locator topicContext) {
        this(query,topicContext,false);
    }
    public IsContextTopicDirective(Directive query,String topicContext,boolean not) {
        this(query,new Locator(topicContext),not);
    }
    public IsContextTopicDirective(Directive query,String topicContext) {
        this(query,topicContext,false);
    }

 
    protected int _includeRow(ResultRow row,Topic context,TopicMap tm,Object param) throws TopicMapException {
        Locator p=row.getPlayer(topicContext);
        if(p==null) return RES_EXCLUDE;
        Topic player=tm.getTopic(p);
        if(player==null) return RES_IGNORE;
        return (player.mergesWithTopic(context)?RES_INCLUDE:RES_EXCLUDE);
    }
    public boolean isContextSensitive(){
        return true;
    }
    
}
