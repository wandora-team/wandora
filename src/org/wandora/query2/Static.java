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
 * Static.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;
/**
 *
 * @author olli
 */
public class Static extends Directive {

    private ArrayList<ResultRow> result;

    public Static(ArrayList<ResultRow> result){
        this.result=result;
    }
    public Static(ResultRow result){
        this.result=new ArrayList<ResultRow>();
        this.result.add(result);
    }

    @Override
    public ArrayList<ResultRow> query(QueryContext context, ResultRow input) throws QueryException {
        return result;
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        if(result.size()==1) return result.get(0).toIterator();
        else return super.queryIterator(context, input);
    }

    @Override
    public boolean isStatic(){
        return true;
    }

}
