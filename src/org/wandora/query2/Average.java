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
 * Average.java
 *
 */
package org.wandora.query2;

/**
 *
 * @author olli
 */
public class Average extends Directive{
    private Directive directive;
    public Average(Directive directive){
        this.directive=directive;
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
        double sum=0.0;
        int count=0;
        while(iter.hasNext()) {
            ResultRow row=iter.next();
            Object v=row.getActiveValue();
            count++;
            if(v==null) continue;
            double d=Double.parseDouble(v.toString());
            sum+=d;
        }
        String val=""+(sum/count);
        return input.addValue(DEFAULT_COL, val).toIterator();
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }


}
