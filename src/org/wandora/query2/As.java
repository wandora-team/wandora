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
 * Compare.java
 *
 */
package org.wandora.query2;

import java.util.ArrayList;

/**
 *
 * @author olli
 */
public class As extends Directive implements DirectiveUIHints.Provider  {

    private String original;
    private String newRole;

    public As(){};
    
    public As(String newRole){
        this.newRole=newRole;
    }

    public As(String original,String newRole){
        this.original=original;
        this.newRole=newRole;
    }

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(As.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(String.class, false, "newRole"),
                }, ""),
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(String.class, false, "originalRole"),
                        new DirectiveUIHints.Parameter(String.class, false, "newRole")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "As",
            "Framework");
        return ret;
    }      
    
    public String getOriginalRole(){
        return original;
    }

    public String getNewRole(){
        return newRole;
    }

    @Override
    public ArrayList<ResultRow> query(QueryContext context,ResultRow input) throws QueryException {
        ArrayList<String> roles=new ArrayList<String>(input.getRoles());
        ArrayList<Object> values=new ArrayList<Object>(input.getValues());
        int i;
        if(original==null) i=input.getActiveColumn();
        else {
            i=roles.indexOf(original);
            if(i==-1) throw new QueryException("Trying to change role \""+original+"\" to \""+newRole+"\" in result row but it doesn't exist.");
        }
        int j=roles.indexOf(newRole);
        if(j>=0 && i!=j) {
//            throw new QueryException("Role \""+newRole+"\" already exists.");
            roles.remove(j);
            values.remove(j);
            if(j<i) i--;
        }
        roles.set(i, newRole);
        return new ResultRow(roles,values,i,true).toList();
    }

    public String debugStringParams(){
        if(original!=null) return "\""+original+"\",\""+newRole+"\"";
        else return "\""+newRole+"\"";
    }
}
