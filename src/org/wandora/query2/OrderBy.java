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
 * OrderBy.java
 *
 */
package org.wandora.query2;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author olli
 */


public class OrderBy extends Directive {
    private Comparator<Object> comparator;
    private Directive directive;
    public OrderBy(Directive directive){
        this(directive,new Comparator<Object>(){
            public int compare(Object v1, Object v2) {
                if(v1==null){
                    if(v2==null) return 0;
                    else return -1;
                }
                else if(v2==null) return 1;
                else return v1.toString().compareTo(v2.toString());
            }
        });
    }
    public OrderBy(Directive directive,Comparator<Object> comparator){
        this.directive=directive;
        this.comparator=comparator;
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
        ArrayList<ResultRow> res=directive.query(context, input);
        Collections.sort(res, new Comparator<ResultRow>(){
            public int compare(ResultRow o1, ResultRow o2) {
                Object v1=o1.getActiveValue();
                Object v2=o2.getActiveValue();
                return comparator.compare(v1, v2);
            }
        });
        return new ResultIterator.ListIterator(res);
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }

    
}
