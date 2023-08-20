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
 * Empty.java
 *
 *
 */
package org.wandora.query2;

/**
 *
 * @author olli
 */
public class Empty extends Directive implements DirectiveUIHints.Provider {
    public Empty(){}

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Empty.class,new DirectiveUIHints.Constructor[]{
            },
            Directive.getStandardAddonHints(),
            "Empty",
            "Primitive");
        return ret;
    } 
    
    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        return new ResultIterator.EmptyIterator();
    }

}
