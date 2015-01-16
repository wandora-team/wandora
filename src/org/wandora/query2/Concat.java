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
 * Concat.java
 *
 */
package org.wandora.query2;

/**
 *
 * @author olli
 */


public class Concat extends Directive  {
    private Directive directive;
    private String delim="; ";
    public Concat(Directive directive,String delim){
        this.directive=directive;
        this.delim=delim;
    }
    public Concat(Directive directive){
        this(directive,"; ");
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
        StringBuilder sb=new StringBuilder();
        boolean first=true;
        while(iter.hasNext()) {
            if(!first) sb.append(delim);
            else first=false;
            
            ResultRow row=iter.next();
            Object v=row.getActiveValue();
            if(v==null) continue;
            String s=v.toString();
            sb.append(s);
        }
        return input.addValue(DEFAULT_COL, sb.toString()).toIterator();
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }
    
}
