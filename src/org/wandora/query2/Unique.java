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
 * Recursive.java
 *
 */
package org.wandora.query2;
import java.util.*;

/**
 *
 * @author olli
 */
public class Unique extends Directive implements DirectiveUIHints.Provider {
    private Directive directive;
    
    public Unique(){}
    
    public Unique(Directive directive){
        this.directive=directive;
    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Unique.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Directive.class, false, "directive")
                }, ""),
            },
            Directive.getStandardAddonHints(),
            "Unique",
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
        return new UniqueIterator(context,input);
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }

    private class UniqueIterator extends ResultIterator {

        public QueryContext context;
        public ResultRow input;
        public ResultIterator iter;
        public HashSet<ResultRow> included;
        public ResultRow next;

        public UniqueIterator(QueryContext context,ResultRow input) throws QueryException {
            this.context=context;
            this.input=input;
            iter=directive.queryIterator(context, input);
            included=new HashSet<ResultRow>();
        }

        @Override
        public void dispose() throws QueryException {
            iter.dispose();
            included=null;
        }

        @Override
        public boolean hasNext() throws QueryException {
            if(next!=null) return true;
            while(iter.hasNext()) {
                next=iter.next();
                if(included.add(next)) return true;
            }
            next=null;
            return false;
        }

        @Override
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(hasNext()){
                ResultRow ret=next;
                next=null;
                return ret;
            }
            else throw new NoSuchElementException();
        }

        @Override
        public void reset() throws QueryException {
            iter.reset();
            included=new HashSet<ResultRow>();
            next=null;
        }

    }
}
