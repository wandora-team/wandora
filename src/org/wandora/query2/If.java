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
 * If.java
 *
 *
 */
package org.wandora.query2;

import java.util.ArrayList;

/**
 *
 * @author olli
 */
public class If extends Directive {
    private Directive cond;
    private Directive then;
    private Directive els;
    public If(Directive cond,Directive then,Directive els){
        this.cond=cond;
        this.then=then;
        this.els=els;
    }
    public If(WhereDirective cond,Directive then){
        this(cond,then,new Empty());
    }
    public If(WhereDirective cond){
        this(cond,new Identity(),new Empty());
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        cond.endQuery(context);
        then.endQuery(context);
        els.endQuery(context);
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean r=true;
        r&=cond.startQuery(context);
        r&=then.startQuery(context);
        r&=els.startQuery(context);
        return r;
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        ResultIterator condIter=cond.queryIterator(context, input);
        boolean dispose=true;
        try{
            if(condIter.hasNext()){
                if(then instanceof COND) {
                    dispose=false;
                    return condIter;
                }
                else return then.queryIterator(context, input);
            }
            else{
                if(els instanceof COND){
                    dispose=false;
                    return condIter;
                }
                else return els.queryIterator(context, input);
            }
        }
        finally{
            if(dispose) condIter.dispose();
        }
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(new Directive[]{cond,then,els},indent);
    }

    public static class COND extends Directive {
        // this class serves only as a marker, it is never actually queried
        public COND(){}

        @Override
        public ArrayList<ResultRow> query(QueryContext context, ResultRow input) throws QueryException {
            throw new QueryException("If.COND should not be queried directly.");
        }

        @Override
        public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
            throw new QueryException("If.COND should not be queried directly.");
        }

    }
}
