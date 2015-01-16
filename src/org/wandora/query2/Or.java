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
 * Or.java
 *
 *
 */
package org.wandora.query2;

/**
 *
 * @author olli
 */
public class Or extends WhereDirective {

    private WhereDirective[] directives;

    public Or(WhereDirective[] directives){
        this.directives=directives;
    }
    public Or(WhereDirective d1,WhereDirective d2){
        this(new WhereDirective[]{d1,d2});
    }
    public Or(WhereDirective d1,WhereDirective d2,WhereDirective d3){
        this(new WhereDirective[]{d1,d2,d3});
    }
    public Or(WhereDirective d1,WhereDirective d2,WhereDirective d3,WhereDirective d4){
        this(new WhereDirective[]{d1,d2,d3,d4});
    }
    public Or(WhereDirective d1,WhereDirective d2,WhereDirective d3,WhereDirective d4,WhereDirective d5){
        this(new WhereDirective[]{d1,d2,d3,d4,d5});
    }
    public Or(WhereDirective d1,WhereDirective d2,WhereDirective d3,WhereDirective d4,WhereDirective d5,WhereDirective d6){
        this(new WhereDirective[]{d1,d2,d3,d4,d5,d6});
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        for(int i=0;i<directives.length;i++){directives[i].endQuery(context);}
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean r=true;
        for(int i=0;i<directives.length;i++){r&=directives[i].startQuery(context);}
        return r;
    }



    @Override
    public boolean includeRow(QueryContext context, ResultRow input) throws QueryException {
        boolean cond=false;
        for(int i=0;i<directives.length;i++){
            if(directives[i].includeRow(context, input)) return true;
        }
        return false;
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directives,indent);
    }



}
