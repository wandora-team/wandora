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
 * IsOfTypeDirective.java
 *
 * Created on 25. lokakuuta 2007, 11:30
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
public class IsOfTypeDirective extends FilterDirective {
    
    private Locator typeContext;
    private Locator type;
    
    /** Creates a new instance of IsOfTypeDirective */
    public IsOfTypeDirective(Directive query,Locator typeContext,Locator type,boolean not) {
        super(query,not);
        this.query=query;
        this.typeContext=typeContext;
        this.type=type;
        this.not=not;
    }
    public IsOfTypeDirective(Directive query,Locator typeContext,Locator type) {
        this(query,typeContext,type,false);
    }
    public IsOfTypeDirective(Directive query,String typeContext,String type,boolean not) {
        this(query,new Locator(typeContext),new Locator(type),not);
    }
    public IsOfTypeDirective(Directive query,String typeContext,String type) {
        this(query,typeContext,type,false);
    }
    
    public Object startQuery(QueryContext context) throws TopicMapException {
        TopicMap tm=context.getContextTopic().getTopicMap();
        Topic typeTopic=tm.getTopic(type);        
        return typeTopic;
    }
    
    protected int _includeRow(ResultRow row,Topic context,TopicMap tm,Object param) throws TopicMapException {
        if(param==null) return RES_EXCLUDE;
        Topic typeTopic=(Topic)param;
        
        Locator p=row.getPlayer(typeContext);
        if(p==null) return RES_EXCLUDE;
        Topic player=tm.getTopic(p);
        if(player==null) return RES_IGNORE;
        return (player.isOfType(typeTopic)?RES_INCLUDE:RES_EXCLUDE);
    }
    
}
