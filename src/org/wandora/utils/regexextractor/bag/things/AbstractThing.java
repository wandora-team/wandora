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
 */


package org.wandora.utils.regexextractor.bag.things;


import org.wandora.utils.regexextractor.Toolbox;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import org.wandora.utils.regexextractor.*;
import org.wandora.utils.regexextractor.bag.*;


public abstract class AbstractThing extends Object implements Thing {
    
    private int dirty = 0;
    
    protected transient Bag bag;
    
    public void init(Bag bag) {
        this.bag = bag;
    }
    
    
    public boolean isDirty() {
        return (dirty!=0);
    }
    
    public void clearDirty() {
        dirty = 0;
    }
    
    protected void smudge( int level ) {
        dirty = level;
        if( level>1 ) {
//            System.out.println("AT -> "+level);
        }
    }
    
    public Object[] supportedRules() {
        Vector supported = new Vector();
        try {
            Class myClass = this.getClass();
            Field[] fields = myClass.getFields();
            for (int i=0; i<fields.length; i++) {
                try {
                    String fieldValue = (String) fields[i].get(this);
                    if (fieldValue.startsWith("GET_")) supported.add(fieldValue);
                    if (fieldValue.startsWith("SET_")) supported.add(fieldValue);
                } catch(Exception e2) {} 
            }
        } catch(Exception e1) {}
        return Toolbox.vectorToArray(supported);
    }
    
    
    public boolean supportsRule(Object getObject) {
        if (getObject != null) {
            Object[] supported = supportedRules();
            if( supported!=null ) {
                for (int i=0; i<supported.length; i++) {
                    try {
                        if (supported[i].equals(getObject)) return true;
                    } catch(Exception e2) {}
                }
            }
        }
        return false;
    }
    
    
    
    public int compareTo(Object o) {
        if (o != null && o instanceof Thing) {
            if (((Thing) o).size() > this.size()) return 1;
            if (((Thing) o).size() < this.size()) return -1;
        }
        return 0;
    }
    
}

