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
 * RolesDirective.java
 *
 * Created on 25. lokakuuta 2007, 12:13
 *
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class RolesDirective implements Directive {
    
    private ArrayList<Locator> roles;
    private Directive query;
    
    /** Creates a new instance of RolesDirective 
     *
     *  roles may contain Strings or Locators
     */
    public RolesDirective(Directive query,ArrayList roles) {
        this.query=query;
        this.roles=new ArrayList<Locator>();
        for(Object o : roles){
            if(o instanceof Locator) this.roles.add((Locator)o);
            else this.roles.add(new Locator((String)o));
        }
    }
    public RolesDirective(Directive query,Object ... roles){
        this.query=query;
        this.roles=new ArrayList<Locator>();
        for(int i=0;i<roles.length;i++){
            if(roles[i] instanceof Locator) this.roles.add((Locator)roles[i]);
            else this.roles.add(new Locator((String)roles[i]));
        }        
    }
    public RolesDirective(Directive query,String role1){
        this(query,new Object[]{role1});
    }
    public RolesDirective(Directive query,String role1,String role2){
        this(query,new Object[]{role1,role2});        
    }
    public RolesDirective(Directive query,String role1,String role2,String role3){
        this(query,new Object[]{role1,role2,role3});                
    }

    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        ArrayList<ResultRow> inner=query.query(context);
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();
        for(ResultRow row : inner){
            ArrayList<Object> newValues=new ArrayList<Object>();
            ArrayList<Locator> newRoles=new ArrayList<Locator>();
            for(Locator r : roles){
                Object p=row.getValue(r);
                newValues.add(p);
                newRoles.add(r);
            }
            res.add(new ResultRow(row.getType(),newRoles,newValues));
        }
        return res;
    }
    public boolean isContextSensitive(){
        return query.isContextSensitive();
    }
   
}
