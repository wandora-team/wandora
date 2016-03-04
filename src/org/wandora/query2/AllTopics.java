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
 * AllTopics.java
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
public class AllTopics extends Directive implements DirectiveUIHints.Provider {

    public AllTopics(){

    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(AllTopics.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                }, "")
            },
            Directive.getStandardAddonHints(),
            "AllTopics",
            "Topic map");
        return ret;
    }         

    @Override
    public boolean isStatic() {
        return true;
    }


    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        return new AllTopicsIterator(context);
    }

    private static class AllTopicsIterator extends ResultIterator {
        public QueryContext context;
        public Iterator<Topic> iter;
        public AllTopicsIterator(QueryContext context) throws QueryException {
            this.context=context;
            try{
                this.iter=context.getTopicMap().getTopics();
            }catch(TopicMapException tme){
                throw new QueryException(tme);
            }
        }
        public boolean hasNext() throws QueryException {
            if(context.checkInterrupt()) throw new QueryException("Execution interrupted");
            return iter.hasNext();
        }
        public ResultRow next() throws NoSuchElementException,QueryException {
            Topic t=iter.next();
            return new ResultRow(t);
        }
        public void dispose() throws QueryException {
            // see notes in TopicIterator about disposing of the iterator properly
            if(iter instanceof TopicIterator) ((TopicIterator)iter).dispose();
            else while(iter.hasNext()) {iter.next();}
        }
        public void reset() throws QueryException {
            dispose(); // see note in dispose
            try{
                this.iter=context.getTopicMap().getTopics();
            }catch(TopicMapException tme){
                throw new QueryException(tme);
            }
        }
    }
}
