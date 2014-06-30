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
 *
 * Roles.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;
/**
 *
 * @author olli
 */
public class Roles extends Directive {
    private String[] roles;
    private boolean not;

    public Roles(String[] roles){
        this(roles,false);
    }
    public Roles(String[] roles,boolean not){
        this.roles=roles;
        this.not=not;
    }
    public Roles(String s1){this(new String[]{s1});}
    public Roles(String s1,String s2){this(new String[]{s1,s2});}
    public Roles(String s1,String s2,String s3){this(new String[]{s1,s2,s3});}
    public Roles(String s1,String s2,String s3,String s4){this(new String[]{s1,s2,s3,s4});}

    public Roles(String s1,boolean not){this(new String[]{s1},not);}
    public Roles(String s1,String s2,boolean not){this(new String[]{s1,s2},not);}
    public Roles(String s1,String s2,String s3,boolean not){this(new String[]{s1,s2,s3},not);}
    public Roles(String s1,String s2,String s3,String s4,boolean not){this(new String[]{s1,s2,s3,s4},not);}

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        ArrayList<String> newRoles=new ArrayList<String>();
        ArrayList<Object> newValues=new ArrayList<Object>();
        int active=0;
        if(not==false){
            for(int i=0;i<roles.length;i++){
                Object value=null;
                for(int j=0;j<input.getNumValues();j++){
                    if(input.getRole(j).equals(roles[i])){
                        value=input.getValue(j);
                        if(input.getActiveColumn()==j) active=i;
                        break;
                    }
                }
                newRoles.add(roles[i]);
                newValues.add(value);
            }
        }
        else{
            Outer: for(int j=0;j<input.getNumValues();j++){
                String inRole=input.getRole(j);
                for(int i=0;i<roles.length;i++){
                    if(inRole.equals(roles[i])) {
                        continue Outer;
                    }
                }
                newRoles.add(inRole);
                newValues.add(input.getValue(j));
            }
        }
        return new ResultRow(newRoles,newValues,active,true).toIterator();
    }


}
