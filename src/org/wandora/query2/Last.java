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
 * Last.java
 *
 */
package org.wandora.query2;
import java.util.*;
/**
 *
 * @author olli
 */
public class Last extends Directive implements DirectiveUIHints.Provider {
    private Directive directive;
    private int count;
    
    public Last(){}

    public Last(int count,Directive directive){
        this.count=count;
        this.directive=directive;
    }
    public Last(Directive directive){
        this(1,directive);
    }

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Last.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Directive.class, false, "directive")
                }, ""),
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Integer.class, false, "count"),
                    new DirectiveUIHints.Parameter(Directive.class, false, "directive")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Last",
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
        if(count>1){
            LinkedList<ResultRow> history=new LinkedList<ResultRow>();
            while(iter.hasNext()){
                history.add(iter.next());
                while(history.size()>count) history.removeFirst();
            }
            return new ResultIterator.ListIterator(new ArrayList<ResultRow>(history));
        }
        else{
            ResultRow last=null;
            while(iter.hasNext()) last=iter.next();
            if(last==null) return new ResultIterator.EmptyIterator();
            else return last.toIterator();
        }
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }

}
