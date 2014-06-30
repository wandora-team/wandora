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
 * AllTopicsDirective.java
 *
 * Created on 4. tammikuuta 2008, 14:09
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
public class AllTopicsDirective implements Directive {
    
    private Locator resultType;
    private Locator resultRole;
    
    /** Creates a new instance of AllTopicsDirective */
    public AllTopicsDirective(Locator resultType,Locator resultRole) {
        this.resultType=resultType;
        this.resultRole=resultRole;
    }
    public AllTopicsDirective(String resultType,String resultRole) {
        this(new Locator(resultType),new Locator(resultRole));
    }
    public AllTopicsDirective() {
        this(new Locator(XTMPSI.TOPIC),new Locator(XTMPSI.TOPIC));
    }
    
    public ArrayList<ResultRow> query(TopicMap tm,QueryContext context) throws TopicMapException {
        Iterator<Topic> iter=tm.getTopics();
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();
        while(iter.hasNext()){
            Topic t=iter.next();
            ResultRow r=new ResultRow(resultType,resultRole,t.getOneSubjectIdentifier());
            res.add(r);
        }
        return res;
    }
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        return query(context.getContextTopic().getTopicMap(),context);
    }

    public boolean isContextSensitive(){
        return false;
    }

}
