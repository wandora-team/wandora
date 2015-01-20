/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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
 * Topics.java
 */
package org.wandora.query2;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */
public class Topics extends Directive {
    protected boolean useBN;

    public Topics(boolean baseName){
        useBN=baseName;
    }
    public Topics(){
        this(false);
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        Object o=input.getActiveValue();
        if(o!=null){
            try{
                Topic t;
                if(useBN) t=context.getTopicMap().getTopicWithBaseName(o.toString());
                else t=context.getTopicMap().getTopic(o.toString());
                if(t!=null){
                    return input.addValue(Directive.DEFAULT_COL, t).toIterator();
                }
            }
            catch(TopicMapException tme){
                throw new QueryException(tme);
            }
        }
        return new ResultIterator.EmptyIterator();
    }

}
