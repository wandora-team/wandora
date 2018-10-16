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
 * BibtexEntry.java
 *
 * Created on 17. lokakuuta 2007, 11:11
 *
 */

package org.wandora.application.tools.extractors.bibtex;


import java.util.*;
/**
 *
 * @author olli
 */
public class BibtexEntry {
    
    private String type;
    private String id;
    private HashMap<String,Object> values;
    
    /** Creates a new instance of BibtexEntry */
    public BibtexEntry() {
        values=new HashMap<String,Object>();
    }
    public BibtexEntry(String type,String id) {
        this();
        setType(type);
        setID(id);
    }
    
    public String getType(){return type;}
    public void setType(String type){this.type=type;}
    public String getID(){return id;}
    public void setID(String id){this.id=id;}
    
    public Map<String,Object> getValues(){return values;}
    
    public Object getValue(String key){return values.get(key);}
    public void setValue(String key,Object value){values.put(key,value);}
    public void addPerson(String key,BibtexPerson person){
        ArrayList<BibtexPerson> list;
        Object o=values.get(key);
        if(o==null || !(o instanceof ArrayList)){
            list=new ArrayList<BibtexPerson>();
            values.put(key,list);
        }
        else list=(ArrayList<BibtexPerson>)o;
        list.add(person);
    }
    public String getString(String key){
        Object o=values.get(key);
        if(o==null) return null;
        else return o.toString();
    }
}
