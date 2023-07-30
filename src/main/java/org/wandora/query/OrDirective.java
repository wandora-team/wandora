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
 * OrDirective.java
 *
 * Created on 29. lokakuuta 2007, 11:08
 *
 */

package org.wandora.query;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 * @deprecated
 *
 * @author olli
 */
public class OrDirective extends FilterDirective {
    
    private FilterDirective[] filters;
    
    /** Creates a new instance of OrDirective */
    public OrDirective(Directive query,boolean not,FilterDirective ... filters) {
        super(query,not);
        this.filters=filters;
    }
    public OrDirective(Directive query,boolean not,FilterDirective filter1) {
        this(query,not,new FilterDirective[]{filter1});
    }
    public OrDirective(Directive query,boolean not,FilterDirective filter1,FilterDirective filter2) {
        this(query,not,new FilterDirective[]{filter1,filter2});
    }
    public OrDirective(Directive query,boolean not,FilterDirective filter1,FilterDirective filter2,FilterDirective filter3) {
        this(query,not,new FilterDirective[]{filter1,filter2,filter3});
    }
    public OrDirective(Directive query,boolean not,FilterDirective filter1,FilterDirective filter2,FilterDirective filter3,FilterDirective filter4) {
        this(query,not,new FilterDirective[]{filter1,filter2,filter3,filter4});
    }

    public Object startQuery(QueryContext context) throws TopicMapException {
        Object[] params=new Object[filters.length];
        for(int i=0;i<filters.length;i++){
            params[i]=filters[i].startQuery(context);
        }
        return params;
    }
    public void endQuery(QueryContext context, Object param) throws TopicMapException {
        Object[] params=(Object[])param;
        for(int i=0;i<filters.length;i++){
            filters[i].endQuery(context,params[i]);
        }
    }

    protected int _includeRow(ResultRow row, Topic context, TopicMap tm, Object param) throws TopicMapException {
        Object[] params=(Object[])param;
        boolean ignore=true;
        for(int i=0;i<filters.length;i++){
            int r=filters[i]._includeRow(row,context,tm,params[i]);
            if(r==RES_INCLUDE) return RES_INCLUDE;
            else if(r==RES_EXCLUDE) ignore=false;
        }
        if(ignore) return RES_IGNORE;
        else return RES_EXCLUDE;
    }
    public boolean isContextSensitive(){
        for(int i=0;i<filters.length;i++){
            if(filters[i].isContextSensitive()) return true;
        }
        return super.isContextSensitive();
    }

    
}
