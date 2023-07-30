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
 * ResultRow.java
 *
 * Created on 25. lokakuuta 2007, 11:05
 *
 */

package org.wandora.query;
import java.util.ArrayList;
import java.util.List;

import org.wandora.topicmap.Locator;

/**
 * @deprecated
 *
 * @author olli
 */
public class ResultRow {
    
    private Locator type;
    private ArrayList<Locator> roles;
    private ArrayList<Object> values;
    
    private int hashCode=-1;
    
    /** Creates a new instance of ResultRow */
    public ResultRow(Locator type,List<Locator> roles,List<Object> values) {
        this.type=type;
        this.roles=new ArrayList<Locator>(roles);
        this.values=new ArrayList<Object>(values);
    }
    
    // firstRole and firstPlayer to make clearly different method signatures
    public ResultRow(Locator type,Locator firstRole,Object firstPlayer,Object ... rolesAndValues) {
        this.type=type;
        roles=new ArrayList<Locator>();
        values=new ArrayList<Object>();
        roles.add(firstRole);
        values.add(firstPlayer);
        for(int i=0;i+1<rolesAndValues.length;i+=2){
            roles.add((Locator)rolesAndValues[i]);
            values.add(rolesAndValues[i+1]);
        }
    }
    
    public static ResultRow joinRows(ResultRow ... rows){
        Locator type=rows[0].type;
        ArrayList<Locator> roles=new ArrayList<Locator>(rows[0].roles);
        ArrayList<Object> values=new ArrayList<Object>(rows[0].values);
        for(int i=1;i<rows.length;i++){
            type=rows[i].type;
            for(int j=0;j<rows[i].roles.size();j++){
                Locator r=rows[i].roles.get(j);
                int rIndex=roles.indexOf(r);
                if(rIndex==-1){
                    roles.add(r);
                    values.add(rows[i].values.get(j));
                }
                else{
                    values.set(rIndex,rows[i].values.get(j));
                }
            }
        }
        return new ResultRow(type,roles,values);
    }
    
    
    public Locator getType(){return type;}
    public int getNumValues(){return values.size();}
    public Locator getPlayer(int index){
        Object o=values.get(index);
        if(o instanceof Locator) return (Locator)o;
        else return null;
    }
    public String getText(int index){
        Object o=values.get(index);
        if(o instanceof String) return (String)o;
        else return null;
    }
    public Object getValue(int index){return values.get(index);}
    public Locator getRole(int index){return roles.get(index);}
    public Locator getPlayer(Locator role){
        Object o=getValue(role);
        if(o!=null && o instanceof Locator) return (Locator)o;
        else return null;
    }
    public String getText(Locator role){
        Object o=getValue(role);
        if(o!=null && o instanceof String) return (String)o;
        else return null;        
    }
    public Object getValue(Locator role){
        int index=roles.indexOf(role);
        if(index==-1) return null;
        else return values.get(index);
    }

    public ArrayList<Object> getValues() {
        return values;
    }
    
    public int hashCode(){
        if(hashCode==-1) hashCode = type.hashCode()+roles.hashCode()+values.hashCode();
        return hashCode;
    }
    
    public boolean equals(Object o){
        if(!(o instanceof ResultRow)) return false;
        ResultRow r=(ResultRow)o;
        if(r==this) return true;
        if(hashCode()!=r.hashCode()) return false;
        return r.type.equals(this.type) && r.roles.equals(this.roles) && r.values.equals(this.values);
    }
}
