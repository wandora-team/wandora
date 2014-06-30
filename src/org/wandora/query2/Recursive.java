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
 * Recursive.java
 *
 */
package org.wandora.query2;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class Recursive extends Directive {
    private Directive recursion;
    private int maxDepth;
    private boolean onlyLeaves;

    public Recursive(Directive recursion, int maxDepth, boolean onlyLeaves){
        this.recursion=recursion;
        this.maxDepth=maxDepth;
        this.onlyLeaves=onlyLeaves;
    }
    public Recursive(Directive recursion, int maxDepth){
        this(recursion,maxDepth,false);
    }
    public Recursive(Directive recursion){
        this(recursion,-1,false);
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        return recursion.startQuery(context);
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        recursion.endQuery(context);
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        return new RecursionIterator(context,input);
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(recursion,indent);
    }




    private class RecursionIterator extends ResultIterator {

        public QueryContext context;
        public ResultRow input;
        public ResultIterator iter;
        public HashSet<Object> processed;
        public LinkedList<ResultIterator> queue;
        public LinkedList<ResultIterator> nextQueue;
        public int depth;

        public ResultRow nextRow;

        public RecursionIterator(QueryContext context, ResultRow input) throws QueryException {
            this.context=context;
            this.input=input;
            reset();
        }

        @Override
        public void dispose() throws QueryException {
            if(iter!=null) iter.dispose();
            if(queue!=null){
                for(ResultIterator ri : queue){
                    ri.dispose();
                }
            }
            if(nextQueue!=null){
                for(ResultIterator ri : nextQueue){
                    ri.dispose();
                }
            }
        }

        @Override
        public boolean hasNext() throws QueryException {
            if(nextRow!=null) return true;
            if(maxDepth!=-1 && depth>maxDepth) return false;

            while(true){
                while(!iter.hasNext()){
                    iter.dispose();
                    if(queue.isEmpty()){
                        queue=nextQueue;
                        nextQueue=new LinkedList<ResultIterator>();
                        depth++;
                        if(maxDepth!=-1 && depth>maxDepth) return false;
                        if(queue.isEmpty()) return false;
                    }
                    iter=queue.removeFirst();
                }

                nextRow=iter.next();
                Object val=nextRow.getActiveValue();
                if(processed.add(val)){
                    ResultIterator newIter=recursion.queryIterator(context, input.addValue(input.getActiveRole(), val));
                    if(newIter.hasNext()) nextQueue.add(newIter);
                    if(onlyLeaves && newIter.hasNext()){
                        nextRow=null;
                    }
                    else return true;
                }
            }
        }

        @Override
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(hasNext()){
                ResultRow ret=nextRow;
                nextRow=null;
                return ret;
            }
            else throw new NoSuchElementException();
        }

        @Override
        public void reset() throws QueryException {
            dispose();
            processed=new HashSet<Object>();
            queue=new LinkedList<ResultIterator>();
            nextQueue=new LinkedList<ResultIterator>();
            iter=recursion.queryIterator(context, input);
            nextRow=null;
            depth=0;
        }

    }
}
