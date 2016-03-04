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
 *
 *
 * Count.java
 *
 *
 */
package org.wandora.query2;

/**
 *
 * @author olli
 */
public class Count extends Directive implements DirectiveUIHints.Provider {
    private Directive directive;
    
    public Count(){}
    
    public Count(Directive directive){
        this.directive=directive;
    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Count.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Directive.class, false, "directive")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Count",
            "Aggregate");
        return ret;
    }         

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        directive.endQuery(context);
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        return directive.startQuery(context);
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        ResultIterator iter=directive.queryIterator(context, input);
        int count=0;
        while(iter.hasNext()) {
            iter.next();
            count++;
        }
        return input.addValue(DEFAULT_COL, ""+count).toIterator();
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }


}
