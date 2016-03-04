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
 * JoinDirective.java
 *
 * Created on 25. lokakuuta 2007, 11:38
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
public class JoinDirective implements Directive {
    
    private Directive query;
    private Locator joinContext;
    private Directive joinQuery;
    
    /** Creates a new instance of JoinDirective */
    public JoinDirective(Directive query,Directive joinQuery) {
        this(query,(Locator)null,joinQuery);
    }
    public JoinDirective(Directive query,Locator joinContext,Directive joinQuery) {
        this.query=query;
        this.joinContext=joinContext;
        this.joinQuery=joinQuery;
    }
    public JoinDirective(Directive query,String joinContext,Directive joinQuery) {
        this(query,new Locator(joinContext),joinQuery);
    }

    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        return query(context,null,null);
    }

    public ArrayList<ResultRow> query(QueryContext context,FilterDirective filter,Object filterParam) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        ArrayList<ResultRow> inner=query.query(context);
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();

        ArrayList<ResultRow> cachedJoin=null;
        boolean useCache=!joinQuery.isContextSensitive();


        for(ResultRow row : inner){
            Topic t=null;
            if(joinContext!=null){
                Locator c=row.getPlayer(joinContext);
                if(c==null) continue;
                t=tm.getTopic(c);
            }
            else t=context.getContextTopic();
            if(t==null) continue;

            ArrayList<ResultRow> joinRes;
            if(!useCache || cachedJoin==null){
                joinRes=joinQuery.query(context.makeNewWithTopic(t));
                if(useCache) cachedJoin=joinRes;
            }
            else joinRes=cachedJoin;

            for(ResultRow joinRow : joinRes){
                ResultRow joined=ResultRow.joinRows(row,joinRow);
                if(filter!=null && !filter.includeRow(joined, contextTopic, tm, filterParam)) continue;
                res.add(joined);
            }
        }
        return res;
    }
    public boolean isContextSensitive(){
        return query.isContextSensitive();
        // note joinQuery gets context from query so it's sensitivity is same
        // as that of query
    }
    
}
