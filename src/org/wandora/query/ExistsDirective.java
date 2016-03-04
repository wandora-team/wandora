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
 * ExistsDirective.java
 *
 * Created on 20. helmikuuta 2008, 14:35
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
public class ExistsDirective extends FilterDirective {

    private Directive existsDirective;
    private Locator typeContext;
    
    /** Creates a new instance of ExistsDirective */
    public ExistsDirective(Directive query,Locator typeContext,Directive exists,boolean not) {
        super(query,not);
        this.typeContext=typeContext;
        this.existsDirective=exists;
    }
    public ExistsDirective(Directive query,Locator typeContext,Directive exists) {
        this(query,typeContext,exists,false);
    }
    public ExistsDirective(Directive query,String typeContext,Directive exists,boolean not) {
        this(query,new Locator(typeContext),exists,not);
    }
    public ExistsDirective(Directive query,String typeContext,Directive exists) {
        this(query,new Locator(typeContext),exists,false);
    }
    
    public Object startQuery(QueryContext context) throws TopicMapException {
        return context;
    }
    
    protected int _includeRow(ResultRow row,Topic context,TopicMap tm,Object param) throws TopicMapException {
        if(param==null) return RES_EXCLUDE;
        QueryContext qContext=(QueryContext)param;
        
        Locator p=row.getPlayer(typeContext);
        if(p==null) return RES_EXCLUDE;
        Topic player=tm.getTopic(p);
        if(player==null) return RES_IGNORE;
        ArrayList<ResultRow> res=existsDirective.query(qContext.makeNewWithTopic(player));
        return (res.isEmpty()?RES_EXCLUDE:RES_INCLUDE);        
    }

    // Note no need to override isContextSensitive. existsDirective doesn't affect
    // context sensitivity for same reason as in joinDirective.


}
