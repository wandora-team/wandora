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
 * FilterDirective.java
 *
 * Created on 29. lokakuuta 2007, 9:49
 *
 */

package org.wandora.query;
import java.util.ArrayList;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 * @deprecated
 *
 * @author olli
 */
public abstract class FilterDirective implements Directive {
    
    public static final int RES_INCLUDE=1; // include unless not
    public static final int RES_EXCLUDE=-1; // exclude unless not
    public static final int RES_IGNORE=0; // do not include, regardless of value of not
    
    protected Directive query;    
    protected boolean not;
    
    /** Creates a new instance of FilterDirective */
    public FilterDirective(Directive query,boolean not) {
        this.query=query;
        this.not=not;
    }
    
    /**
     * Called when evaluation of this filter query is about to start. The return value is passed
     * as a parameter to _includeRow and endQuery later. Returning a null value should mean that
     * _includeRow will return RES_EXCLUDE for all rows. So if you don't need a parameter object,
     * just return any object (this, new Object() or whatever) and simply ignore it later.
     */
    public Object startQuery(QueryContext context) throws TopicMapException {return new Object();}
    /**
     * Called when query evaluation is done. If startQuery returned null, this method might not be
     * called.
     */
    public void endQuery(QueryContext context,Object param) throws TopicMapException {}
    
    /**
     * Return one of RES_INCLUDE, RES_EXCLUDE or RES_INGORE to indicate how to filter the
     * row. RES_INCLUDE means that the row should be included in the resurt except when
     * negation if query is desired. RES_EXCLUDE is the opposite, row is not included except
     * in the case of negation. RES_IGNORE means that row is not included regardless of query
     * being evaluated in negation mode or not. This can be because for example the association
     * doesn't have the assumed players.
     */
    protected abstract int _includeRow(ResultRow row,Topic context,TopicMap tm,Object param) throws TopicMapException ;
    
    public boolean includeRow(ResultRow row,Topic context,TopicMap tm,Object param) throws TopicMapException {
        int r=_includeRow(row,context,tm,param);
        return ((r==1)!=not && r!=0);
    }
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {

        Object param=startQuery(context);
        if(param==null && !not) return new ArrayList<ResultRow>();

        if(query instanceof JoinDirective){
            ArrayList<ResultRow> result=((JoinDirective)query).query(context,this,param);
            endQuery(context,param);
            return result;
        }
        else{
            ArrayList<ResultRow> inner=query.query(context);
            ArrayList<ResultRow> result=new ArrayList<ResultRow>();
            Topic contextTopic=context.getContextTopic();
            TopicMap tm=contextTopic.getTopicMap();
            for(ResultRow row : inner){
                if(includeRow(row,contextTopic,tm,param)) result.add(row);
            }
            endQuery(context,param);
            return result;
        }
    }

    public boolean isContextSensitive(){
        return query.isContextSensitive();
    }

}
