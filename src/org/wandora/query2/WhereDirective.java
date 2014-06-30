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
 *
 * WhereDirective.java
 *
 */
package org.wandora.query2;
import java.util.*;

/**
 *
 * @author olli
 */
public abstract class WhereDirective extends FilterDirective {

    public ResultRow processRow(QueryContext context,ResultRow input) throws QueryException {
        if(includeRow(context,input)) return input;
        else return null;
    }

    public abstract boolean includeRow(QueryContext context,ResultRow input) throws QueryException;

    public WhereDirective and(WhereDirective d){
        return new And(this,d);
    }

    public WhereDirective and(String c1,String comp,String c2){
        return and(new Compare(c1,comp,c2));
    }

    public WhereDirective or(WhereDirective d){
        return new Or(this,d);
    }

    public WhereDirective or(String c1,String comp,String c2){
        return or(new Compare(c1,comp,c2));
    }

    public WhereDirective not(){
        return new Not(this);
    }
}
