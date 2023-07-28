/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2023 Wandora Team
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
 * From.java
 *
 */
package org.wandora.query2;
import java.util.*;
/**
 *
 * @author olli
 */
public class From extends Directive implements DirectiveUIHints.Provider  {

    private Directive to;
    private Directive from;

    public From(){}
    
    public From(Directive to,Directive from){
        this.to=to;
        this.from=from;
    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(From.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(Directive.class, false, "to"),
                        new DirectiveUIHints.Parameter(Directive.class, false, "from")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "From",
            "Framework");
        return ret;
    }    

/*    @Override
    public ArrayList<ResultRow> query(QueryContext context,ResultRow input,String as) throws QueryException {
        ArrayList<ResultRow> res=from.query(context,input,null);
        ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
        for(ResultRow row : res){
            ArrayList<ResultRow> res2=to.query(context,row,null);
            for(ResultRow row2 : res2){
                ret.add(row2);
            }
        }
        return ret;
    }*/

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean r=to.startQuery(context);
        r&=from.startQuery(context);
        return r;
    }
    @Override
    public void endQuery(QueryContext context) throws QueryException {
        to.endQuery(context);
        from.endQuery(context);
    }



    @Override
    public ResultIterator queryIterator(QueryContext context,ResultRow input) throws QueryException {
        return new FromIterator(context,input);
    }

    @Override
    public boolean isStatic(){
        return from.isStatic();
    }

    public String debugStringParams(String indent){
        return debugStringInner(new Directive[]{to,from},indent);
    }

    private class FromIterator extends ResultIterator {
        public ResultIterator fromIter;
        public ResultIterator toIter;
        public QueryContext context;
        public ResultRow input;
        public ResultRow nextRow;

        public FromIterator(QueryContext context,ResultRow input) throws QueryException {
            this.context=context;
            this.input=input;
            this.fromIter=from.queryIterator(context, input);
/*            if( !(fromIter instanceof ResultIterator.BufferedIterator) &&
                    !(fromIter instanceof ResultIterator.ListIterator) &&
                    !(fromIter instanceof ResultIterator.EmptyIterator) &&
                    !(fromIter instanceof ResultIterator.SingleIterator) )
                fromIter=new ResultIterator.BufferedIterator(fromIter);*/
//            this.fromIter=new ResultIterator.ListIterator(from.query(context,input,null));
        }

        public boolean hasNext() throws QueryException {
            if(nextRow!=null) return true;
            if(context.checkInterrupt()) throw new QueryException("Execution interrupted");
            while( (toIter==null || !toIter.hasNext()) && fromIter.hasNext() ){
                ResultRow row=fromIter.next();
                toIter=to.queryIterator(context, row);
            }

            if(toIter!=null && toIter.hasNext()) {
                nextRow=toIter.next();
                return true;
            }
            else return false;
        }
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(hasNext()) {
                ResultRow temp=nextRow;
                nextRow=null;
                return temp;
            }
            else throw new NoSuchElementException();
        }
        public void dispose() throws QueryException {
            fromIter.dispose();
            if(toIter!=null) toIter.dispose();
        }

        public void reset() throws QueryException {
            fromIter=from.queryIterator(context, input);
            toIter=null;
            nextRow=null;
        }
    }
}
