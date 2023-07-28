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
 *
 * Exists.java
 *
 *
 */
package org.wandora.query2;

/**
 *
 * @author olli
 */
public class Exists extends WhereDirective implements DirectiveUIHints.Provider {
    private Directive directive;
    
    public Exists(){}
    
    public Exists(Directive directive){
        this.directive=directive;
    }

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Exists.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Directive.class, false, "directive")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Exists",
            "Where directive");
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
    public boolean includeRow(QueryContext context, ResultRow input) throws QueryException {
        ResultIterator iter=directive.queryIterator(context, input);
        if(iter.hasNext()){
            iter.dispose();
            return true;
        }
        else {
            iter.dispose();
            return false;
        }
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directive,indent);
    }

}
