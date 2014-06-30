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
 * IsOfType.java
 *
 *
 */
package org.wandora.query2;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class IsOfType extends WhereDirective {

    private TopicOperand topicOp;

    public IsOfType(Object o){
        this.topicOp=new TopicOperand(o);
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        topicOp.endQuery(context);
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        return topicOp.startQuery(context);
    }

    @Override
    public boolean includeRow(QueryContext context, ResultRow input) throws QueryException {
        Object o=input.getActiveValue();
        if(o==null) return false;

        try{
            if(!(o instanceof Topic)){
                o=context.getTopicMap().getTopic(o.toString());
                if(o==null) return false;
            }
            Topic type=topicOp.getOperandTopic(context, input);
            if(type==null) return false;
            Topic t=(Topic)o;
            if(t.isOfType(type)) return true;
            else return false;
        }catch(TopicMapException tme){
            throw new QueryException(tme);
        }
    }

}
