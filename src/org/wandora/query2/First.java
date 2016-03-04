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
 * First.java
 *
 *
 */
package org.wandora.query2;

import java.util.NoSuchElementException;

/**
 *
 * @author olli
 */
public class First extends Directive implements DirectiveUIHints.Provider {
    private Directive directive;
    private int count;
    
    public First(){}
    
    public First(int count,Directive directive){
        this.directive=directive;
        this.count=count;
    }
    public First(Directive directive){
        this(1,directive);
    }

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(First.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Directive.class, false, "directive")
                }, ""),
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Integer.class, false, "count"),
                    new DirectiveUIHints.Parameter(Directive.class, false, "directive")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "First",
            "Structure");
        return ret;
    }        
    
    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        return directive.startQuery(context);
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        directive.endQuery(context);
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        ResultIterator iter=directive.queryIterator(context, input);
        if(!iter.hasNext() || count==0) return new ResultIterator.EmptyIterator();
        else {
            if(count==1) {
                ResultIterator ret=iter.next().toIterator();
                iter.dispose();
                return ret;
            }
            else {
                return new FirstIterator(count,iter);
            }
        }
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }

    private class FirstIterator extends ResultIterator {

        public int counter;
        public int count;
        public ResultIterator iter;

        public FirstIterator(int count,ResultIterator iter) throws QueryException {
            this.count=count;
            this.iter=iter;
            counter=0;
        }

        @Override
        public void dispose() throws QueryException {
            iter.dispose();
        }

        @Override
        public boolean hasNext() throws QueryException {
            if(counter<count && this.iter.hasNext()) return true;
            else return false;
        }

        @Override
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(hasNext()){
                counter++;
                return iter.next();
            }
            else throw new NoSuchElementException();
        }

        @Override
        public void reset() throws QueryException {
            counter=0;
            iter.reset();
        }

    }

}
