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
 * ResultRow.java
 *
 */
package org.wandora.query2;
import java.util.*;

/**
 *
 * @author olli
 */
public class ResultRow {

    private ArrayList<String> roles;
    private ArrayList<Object> values;

    private int activeColumn;

    private int hashCode=-1;

    public ResultRow(){
        this(new ArrayList<String>(),new ArrayList<Object>(),-1,true);
    }

    public ResultRow(List<String> roles,List<Object> values,int activeColumn,boolean reuse) {
        if(reuse && roles instanceof ArrayList && values instanceof ArrayList){
            this.roles=(ArrayList)roles;
            this.values=(ArrayList)values;
        }
        else{
            this.roles=new ArrayList<String>(roles);
            this.values=new ArrayList<Object>(values);
        }
        this.activeColumn=activeColumn;
    }

    public ResultRow(List<String> roles,List<Object> values,int activeColumn) {
        this(roles,values,activeColumn,false);
    }
    public ResultRow(List<String> roles,List<Object> values) {
        this(roles,values,0);
    }

    public ResultRow(Object value){
        this.roles=new ArrayList<String>();
        this.values=new ArrayList<Object>();
        roles.add(Directive.DEFAULT_COL);
        values.add(value);
        activeColumn=0;
    }

    public int getNumValues(){return values.size();}
    public Object getValue(int index){return values.get(index);}
    public String getRole(int index){return roles.get(index);}

    public int getActiveColumn(){return activeColumn;}

    public Object getActiveValue(){return values.get(activeColumn);}
    public String getActiveRole(){return roles.get(activeColumn);}

    public ArrayList<Object> getValues() {
        return values;
    }

    public Object get(String role) {
        int ind=roles.indexOf(role);
        if(ind==-1) return null;
        return values.get(ind);
    }

    public Object getValue(String role) throws QueryException {
        int ind=roles.indexOf(role);
        if(ind==-1) throw new QueryException("Role \""+role+"\" not in result row.");
        return values.get(ind);
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public ResultRow join(ResultRow row) throws QueryException {
        ArrayList<String> newRoles=new ArrayList<String>(this.roles);
        ArrayList<Object> newValues=new ArrayList<Object>(this.values);

        for(int i=0;i<row.getNumValues();i++){
            String role=row.getRole(i);
            Object val=row.getValue(i);
            int ind=newRoles.indexOf(role);
            if(ind>=0) {
//                if(!role.equals(Directive.DEFAULT_COL)) throw new QueryException("Role \""+role+"\" already exists in result row.");
                newValues.set(ind, val);
            }
            else {
                newRoles.add(role);
                newValues.add(val);
            }
        }

        return new ResultRow(newRoles,newValues,this.activeColumn,true);
    }

    public int hashCode(){
        if(hashCode==-1) hashCode = roles.hashCode()+values.hashCode();
        return hashCode;
    }

    public boolean equals(Object o){
        if(!(o instanceof ResultRow)) return false;
        ResultRow r=(ResultRow)o;
        if(r==this) return true;
        if(hashCode()!=r.hashCode()) return false;
        return r.roles.equals(this.roles) && r.values.equals(this.values);
    }

    public ArrayList<ResultRow> toList(){
        ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
        ret.add(this);
        return ret;
    }

    public ResultIterator toIterator(){
        return new ResultIterator.SingleIterator(this);
    }

    public ResultRow addValue(Object value) throws QueryException {
        return addValue(Directive.DEFAULT_COL,value);
    }

    public ResultRow addValue(String role,Object value) throws QueryException {
        return addValues(new String[]{role},new Object[]{value});
    }

    public ResultRow addValues(String[] roles,Object[] values) throws QueryException {
        ArrayList<String> newRoles=new ArrayList<String>(this.roles);
        ArrayList<Object> newValues=new ArrayList<Object>(this.values);

        int ind=0;
        for(int i=0;i<roles.length;i++){
            String role=roles[i];
            Object value=values[i];

            ind=newRoles.indexOf(role);
            if(ind>=0){
//                if(!role.equals(Directive.DEFAULT_COL)) throw new QueryException("Role \""+role+"\" already exists in result row.");
                newValues.set(ind, value);
            }
            else {
                ind=newRoles.size();
                newRoles.add(role);
                newValues.add(value);
            }
        }
        return new ResultRow(newRoles,newValues,ind,true);
    }
}
