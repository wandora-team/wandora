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
 * Literals.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;

/**
 *
 * @author olli
 */
public class Literals extends Directive implements DirectiveUIHints.Provider {

    private ArrayList<ResultRow> result;

    public Literals(){
        result=new ArrayList<ResultRow>();
    }
    
    public Literals(String[] strings) {
        result=new ArrayList<ResultRow>();
        for(int i=0;i<strings.length;i++){
            ResultRow r=new ResultRow(strings[i]);
            result.add(r);
        }
    }

    public Literals(String s1){this(new String[]{s1});}
    public Literals(String s1,String s2){this(new String[]{s1,s2});}
    public Literals(String s1,String s2,String s3){this(new String[]{s1,s2,s3});}
    public Literals(String s1,String s2,String s3,String s4){this(new String[]{s1,s2,s3,s4});}

    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Literals.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(String.class, true, "value")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Literals",
            "Primitive");
        return ret;
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

    @Override
    public String debugStringParams(){
        StringBuffer ret=new StringBuffer();
        for(int i=0;i<result.size();i++){
            if(i>0) ret.append(",");
            ret.append("\""+result.get(i).getActiveValue()+"\"");
        }
        return ret.toString();
    }
}
