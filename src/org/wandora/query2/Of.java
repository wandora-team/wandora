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
 * Compare.java
 *
 */
package org.wandora.query2;

import java.util.ArrayList;

/**
 *
 * @author olli
 */
public class Of extends Directive implements DirectiveUIHints.Provider {

    private String role;

    public Of(){}
    
    public Of(String role){
        this.role=role;
    }

    public String getRole(){
        return role;
    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Of.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(String.class, false, "role"),
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Of",
            "Framework");
        return ret;
    }       

    @Override
    public ResultIterator queryIterator(QueryContext context,ResultRow input) throws QueryException {
        ArrayList<String> roles=new ArrayList<String>(input.getRoles());
        ArrayList<Object> values=new ArrayList<Object>(input.getValues());
        int i=roles.indexOf(role);
        if(i==-1) throw new QueryException("Role \""+role+"\" not found");
        return new ResultRow(roles,values,i,true).toIterator();

    }

    public String debugStringParams(){return "\""+role+"\"";}
}
