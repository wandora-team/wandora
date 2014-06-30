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
 * Variant2.java
 *
 *
 */

package org.wandora.query2;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * Alternative version of Variant directive that can be used to get
 * variants of any scope, not just variants of the type + language scheme.
 *
 * @author olli
 */


public class Variant2 extends Directive {
    private TopicOperand[] scope;


    public Variant2(Object ... scope){
        this.scope=new TopicOperand[scope.length];
        for(int i=0;i<scope.length;i++) this.scope[i]=new TopicOperand(scope[i]);
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean ret=true;
        for(int i=0;i<scope.length;i++){
            ret&=scope[i].startQuery(context);
        }
        return ret;
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        for(int i=0;i<scope.length;i++){
            scope[i].endQuery(context);
        }
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        Object o=input.getActiveValue();
        if(o==null) return new ResultIterator.EmptyIterator();
        try{
            if(!(o instanceof Topic)){
                TopicMap tm=context.getTopicMap();
                o=tm.getTopic(o.toString());
                if(o==null) return new ResultIterator.EmptyIterator();
            }
            HashSet<Topic> scope=new HashSet<Topic>();
            for(int i=0;i<this.scope.length;i++){
                scope.add(this.scope[i].getOperandTopic(context, input));
            }
            String name=((Topic)o).getVariant(scope);
            return input.addValue(DEFAULT_COL, name).toIterator();
        }catch(TopicMapException tme){
            throw new QueryException(tme);
        }

    }

 
}
